package cn.sharesdk.js;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;

import com.mob.tools.utils.Hashon;
import com.mob.tools.utils.UIHandler;

public class ShareSDKUtils extends WebViewClient implements Callback {
	private static final String API_OPEN = "open";
	private static final String API_CLOSE = "close";
	private static final String API_SET_PLATFORM_CONF = "setPlatformConfig";
	private static final String API_AUTHORIZE = "authorize";
	private static final String API_REMOVE_AUTHORIZATION = "cancelAuthorize";
	private static final String API_IS_VALID = "hasAuthorized";
	private static final String API_GET_USER_INFO = "getUserInfo";
	private static final String API_GET_AUTH_INFO = "getAuthInfo";
	private static final String API_SHARE = "shareContent";
	private static final String API_MULTI_SHARE = "oneKeyShareContent";
	private static final String API_ONE_KEY_SHARE = "showShareMenu";
	private static final String API_SHOW_SHARE_VIEW = "showShareView";
	private static final String API_GET_FRIEND_LIST = "getFriendList";
	private static final String API_FOLLOW_FRIEND = "followFriend";
	
	public static final int MSG_LOAD_URL = 1; // load js script
	public static final int MSG_JS_CALL = 2; // process js callback on ui thread
	
	private WebView webview;
	private SSDKWebViewClient wvClient;
	private Hashon hashon;
	private Context context;
	
	public static ShareSDKUtils prepare(WebView webview, WebViewClient wvClient) {
		return new ShareSDKUtils(webview, wvClient);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private ShareSDKUtils(WebView webview, WebViewClient wbClient) {
		hashon = new Hashon();
		
		this.webview = webview;
		context = this.webview.getContext().getApplicationContext();
		this.wvClient = new SSDKWebViewClient(this);
		this.wvClient.setWebViewClient(wbClient);
		this.webview.setWebViewClient(this.wvClient);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.addJavascriptInterface(this, "JSInterface");
	}
	
	/* process js init function */
	void onInit() {
		// platform type: 1 for android, 2 for ios
		String script = "javascript:$sharesdk._init(1);";
		Message msg = new Message();
		msg.what = MSG_LOAD_URL;
		msg.obj = script;
		UIHandler.sendMessage(msg, this);
	}
	
	/** 
	 * receive js callback
	 * <p>
     * respons: {
     *   "seqId" : "111111",
     *   "platform" : 1,
     *   "state" : 1, // Success = 1, Fail = 2, Cancel = 3
     *   "data" : "user or share response",
     *   "method":"geiUserInfo",
     *   "callback" : "function string",
     *   "error" :
     *   {
     *      "error_level" : 1,
     *      "error_code" : 11,
     *      "error_msg" : "adsfdsaf",
     *   }
     * }
	 */
	public void jsCallback(String seqId, String api, String data, String callback) {
		// this is in webview core thread, not in ui thread
		Message msg = new Message();
		msg.what = MSG_JS_CALL;
		msg.obj = new Object[] {seqId, api, data, callback};
		UIHandler.sendMessage(msg, this);
	}
	
	/** receive js log */
	public void jsLog(String msg) {
		Log.w("ShareSDK for JS", msg == null ? "" : msg);
	}
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_LOAD_URL: {
				webview.loadUrl(String.valueOf(msg.obj));
			}
			break;
			case MSG_JS_CALL: {
				jsCallback((Object[]) msg.obj);
			}
			break;
		}
		return false;
	}
	
	/**
	 * callback main method
	 * Every request is allocated by this function call,
	 * Including the number of requests, the callback and data
	 * @param objs
	 */
	private void jsCallback(Object[] objs) {
		String seqId = (String) objs[0];
		String api = (String) objs[1];
		String data = (String) objs[2];
		String callback = (String) objs[3];

		HashMap<String, Object> req = null;
		try {
			req = hashon.fromJson(data);
			if (req == null) {
				Throwable t = new Throwable("wrong request data: " + data);
				onRequestFailed(seqId, api, callback, null, t);
				return;
			}
		} catch(Throwable t) {
			onRequestFailed(seqId, api, callback, null, t);
			return;
		}
		String oriCallback = (String) req.get("callback");
		HashMap<String, Object> resp = new HashMap<String, Object>();
		resp.put("seqId", seqId);
		resp.put("state", 1);
		resp.put("method", api);
		resp.put("callback", oriCallback);
		if (API_OPEN.equals(api)) {
			initSDK(req);
		} else if (API_CLOSE.equals(api)) {
			stopSDK();
		} else if (API_SET_PLATFORM_CONF.equals(api)) {
			setPlatformConfig(req);
		} else if (API_AUTHORIZE.equals(api)) {
			authorize(seqId, api, callback, oriCallback, req);
			return; // callback by JSPlatfromActionListener
		} else if (API_REMOVE_AUTHORIZATION.equals(api)) {
			removeAuthorization(req);
		} else if (API_IS_VALID.equals(api)) {
			resp.put("platform", req.get("platform"));
			resp.put("data", isVAlid(req));
		} else if (API_GET_USER_INFO.equals(api)) {
			getUserInfo(seqId, api, callback, oriCallback, req);
			return; // callback by JSPlatfromActionListener
		} else if (API_GET_AUTH_INFO.equals(api)) {
			resp.put("platform", req.get("platform"));
			resp.put("data", getAuthInfo(req));
		} else if (API_SHARE.equals(api)) {
			share(seqId, api, callback, oriCallback, req);
			return; // callback by JSPlatfromActionListener
		} else if (API_MULTI_SHARE.equals(api)) {
			multishare(seqId, api, callback, oriCallback, req);
			return; // callback by JSPlatfromActionListener
		} else if (API_ONE_KEY_SHARE.equals(api)) {
			onekeyShare(seqId, api, callback, oriCallback, req);
			return; // callback by JSPlatfromActionListener
		} else if (API_SHOW_SHARE_VIEW.equals(api)) {
			showShareView(seqId, api, callback, oriCallback, req);
			return; // callback by JSPlatfromActionListener
		}else if (API_GET_FRIEND_LIST.equals(api)) {
			getFriendList(seqId, api, callback, oriCallback, req);
			return; // callback by JSPlatfromActionListener
		}else if (API_FOLLOW_FRIEND.equals(api)) {
			jsLog("follow friend");
			followFriend(seqId, api, callback, oriCallback, req);
			return; // callback by JSPlatfromActionListener
		} else {
			Throwable t = new Throwable("unknown api type: " + api);
			onRequestFailed(seqId, api, callback, oriCallback, t);
			return;
		}
		
		Message msg = new Message();
		msg.what = MSG_LOAD_URL;
		msg.obj = "javascript:" + callback + "(" + hashon.fromHashMap(resp) + ");";
		UIHandler.sendMessage(msg, this);
	}
	
	private HashMap<String, Object> throwableToMap(Throwable t) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("msg", t.getMessage());
		ArrayList<HashMap<String, Object>> traces = new ArrayList<HashMap<String, Object>>();
		for (StackTraceElement trace : t.getStackTrace()) {
			HashMap<String, Object> element = new HashMap<String, Object>();
			element.put("cls", trace.getClassName());
			element.put("method", trace.getMethodName());
			element.put("file", trace.getFileName());
			element.put("line", trace.getLineNumber());
			traces.add(element);
		}
		map.put("stack", traces);
		Throwable cause = t.getCause();
		if (cause != null) {
			map.put("cause", throwableToMap(cause));
		}
		return map;
	}
	
	//request fail
	private void onRequestFailed(String seqId, String api, String callback, String oriCallback, Throwable t) {
		HashMap<String, Object> resp = new HashMap<String, Object>();
		resp.put("seqId", seqId);
		resp.put("state", 2);
		resp.put("method", api);
		resp.put("callback", oriCallback);
		HashMap<String, Object> error = new HashMap<String, Object>();
		error.put("error_level", 1);
		error.put("error_code", 0);
		error.put("error_msg", t.getMessage());
		error.put("error_detail", throwableToMap(t));
		resp.put("error", error);
		
		Message msg = new Message();
		msg.what = MSG_LOAD_URL;
		msg.obj = "javascript:" + callback + "(" + hashon.fromHashMap(resp) + ");";
		UIHandler.sendMessage(msg, this);
	}
	
	// ============================ Java Actions ============================
	
	/**
	 * To initialize the SDK
	 * @param params
	 */
	private void initSDK(HashMap<String, Object> params) {
		String appkey = (String) params.get("appkey");
		boolean enableStatistics = !"false".equals(params.get("enableStatistics"));
		ShareSDK.initSDK(context, appkey, enableStatistics);
	}
	
	/**
	 * stop SDK
	 */
	private void stopSDK() {
		ShareSDK.stopSDK(context);
	}
	
	private void setPlatformConfig(HashMap<String, Object> params) {
//		try {
//			int platformId = (Integer) params.get("platform");
//			String platform = ShareSDK.platformIdToName(platformId);
//			@SuppressWarnings("unchecked")
//			HashMap<String, Object> devInfo = (HashMap<String, Object>) params.get("config");
//			ShareSDK.setPlatformDevInfo(platform, devInfo);
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
	}
	
	/**
	 * authorize
	 * @param seqId
	 * @param api
	 * @param callback
	 * @param oriCallback
	 * @param params
	 */
	private void authorize(String seqId, String api, String callback, String oriCallback, HashMap<String, Object> params) {
		int platformId = (Integer) params.get("platform");
		String platformName = ShareSDK.platformIdToName(platformId);
		Platform platform = ShareSDK.getPlatform(context, platformName);
		JSPlatformActionListener pa = new JSPlatformActionListener();
		pa.setCallback(this);
		pa.setSeqId(seqId);
		pa.setJSCallback(callback);
		pa.setOriCallback(oriCallback);
		pa.setApi(api);
		platform.setPlatformActionListener(pa);
		platform.authorize();
	}
	
	/**
	 * Cancel the authorization
	 * @param params
	 */
	private void removeAuthorization(HashMap<String, Object> params) {
		int platformId = (Integer) params.get("platform");
		String platformName = ShareSDK.platformIdToName(platformId);
		Platform platform = ShareSDK.getPlatform(context, platformName);
		platform.removeAccount(true);
	}
	
	/**
	 * Determine whether the third party platform
	 * @param params
	 * @return
	 */
	private boolean isVAlid(HashMap<String, Object> params) {
		int platformId = (Integer) params.get("platform");
		String platformName = ShareSDK.platformIdToName(platformId);
		Platform platform = ShareSDK.getPlatform(context, platformName);
		return platform.isValid();
	}
	
	/**
	 * After landing authorized user information
	 * @param seqId
	 * @param api
	 * @param callback
	 * @param oriCallback
	 * @param params
	 */
	private void getUserInfo(String seqId, String api, String callback, String oriCallback, HashMap<String, Object> params) {
		int platformId = (Integer) params.get("platform");
		String platformName = ShareSDK.platformIdToName(platformId);
		Platform platform = ShareSDK.getPlatform(context, platformName);
		JSPlatformActionListener pa = new JSPlatformActionListener();
		pa.setCallback(this);
		pa.setSeqId(seqId);
		pa.setJSCallback(callback);
		pa.setOriCallback(oriCallback);
		pa.setApi(api);
		platform.setPlatformActionListener(pa);
		platform.SSOSetting(true);
		platform.showUser(null);
	}
	
	private String getAuthInfo(HashMap<String, Object> params){
		String authInfo = "{}";
		int platformId = (Integer) params.get("platform");
		String platformName = ShareSDK.platformIdToName(platformId);
		Platform platform = ShareSDK.getPlatform(context, platformName);
		if(platform.isValid()){
			HashMap<String, Object> platformDbMap = new HashMap<String, Object>();
			PlatformDb db = platform.getDb();
			platformDbMap.put("expiresIn", db.getExpiresIn());
			platformDbMap.put("expiresTime", db.getExpiresTime());
			platformDbMap.put("token", db.getToken());
			platformDbMap.put("tokenSecret()", db.getTokenSecret());
			platformDbMap.put("userGender", db.getUserGender());
			platformDbMap.put("userId", db.getUserId());
			platformDbMap.put("userName", db.getUserName());
			platformDbMap.put("userIcon", db.getUserIcon());
			platformDbMap.put("platformName", db.getPlatformNname());
			platformDbMap.put("platformVersion", db.getPlatformVersion());
			authInfo = hashon.fromHashMap(platformDbMap);
		}
		return authInfo;
	}
	
	private void share(String seqId, String api, String callback, String oriCallback, HashMap<String, Object> params) {
		int platformId = (Integer) params.get("platform");
		boolean isSSO = (Boolean) params.get("isSSO");
		String platformName = ShareSDK.platformIdToName(platformId);
		Platform platform = ShareSDK.getPlatform(context, platformName);
		JSPlatformActionListener pa = new JSPlatformActionListener();
		pa.setCallback(this);
		pa.setSeqId(seqId);
		pa.setJSCallback(callback);
		pa.setOriCallback(oriCallback);
		pa.setApi(api);
		platform.setPlatformActionListener(pa);
		@SuppressWarnings("unchecked")
		HashMap<String, Object> shareParams = (HashMap<String, Object>) params.get("shareParams");
		ShareParams sp = new ShareParams(shareParams);
		int shareType = sp.getShareType();
		sp.setShareType(iosTypeToAndroidType(shareType));
		platform.SSOSetting(isSSO);
		platform.share(sp);
	}
	
	/**
	 * The ios type into android type
	 * @param type
	 * @return
	 */
	private int iosTypeToAndroidType(int type) {
		switch (type) {
			case 1: return Platform.SHARE_IMAGE;
			case 2: return Platform.SHARE_WEBPAGE;
			case 3: return Platform.SHARE_MUSIC;
			case 4: return Platform.SHARE_VIDEO;
			case 5: return Platform.SHARE_APPS;
			case 6: 
			case 7: return Platform.SHARE_EMOJI;
			case 8: return Platform.SHARE_FILE;
		}
        return Platform.SHARE_TEXT;
	}
	
	private void multishare(String seqId, String api, String callback, String oriCallback, HashMap<String, Object> params) {
		@SuppressWarnings("unchecked")
		ArrayList<Integer> platforms = (ArrayList<Integer>) params.get("platforms");
		@SuppressWarnings("unchecked")
		HashMap<String, Object> shareParams = (HashMap<String, Object>) params.get("shareParams");
		ShareParams sp = new ShareParams(shareParams);
		for (Integer platformId : platforms) {
			String platformName = ShareSDK.platformIdToName(platformId.intValue());
			Platform platform = ShareSDK.getPlatform(context, platformName);
			JSPlatformActionListener pa = new JSPlatformActionListener();
			pa.setCallback(this);
			pa.setSeqId(seqId);
			pa.setJSCallback(callback);
			pa.setOriCallback(oriCallback);
			pa.setApi(api);
			platform.setPlatformActionListener(pa);
			platform.share(sp);
		}
	}
	
	private String onekeyShare(String seqId, String api, String callback, String oriCallback, HashMap<String, Object> params) {
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>) params.get("shareParams");
		if (map != null) {
			OnekeyShare oks = new OnekeyShare();
			if (map.containsKey("title")) {
				oks.setTitle(String.valueOf(map.get("title")));
			}
			if (map.containsKey("titleUrl")) {
				oks.setTitleUrl(String.valueOf(map.get("titleUrl")));
			}
			if (map.containsKey("text")) {
				oks.setText(String.valueOf(map.get("text")));
			}
			if (map.containsKey("imagePath")) {
				oks.setImagePath(String.valueOf(map.get("imagePath")));
			}
			if (map.containsKey("imageUrl")) {
				oks.setImageUrl(String.valueOf(map.get("imageUrl")));
			}
			if (map.containsKey("comment")) {
				oks.setComment(String.valueOf(map.get("comment")));
			}
			if (map.containsKey("site")) {
				oks.setSite(String.valueOf(map.get("site")));
			}
			if (map.containsKey("url")) {
				oks.setUrl(String.valueOf(map.get("url")));
			}
			if (map.containsKey("siteUrl")) {
				oks.setSiteUrl(String.valueOf(map.get("siteUrl")));
			}
			if(params.containsKey("isSSO")){
				boolean isSSO = (Boolean) params.get("isSSO");
				if(isSSO){
					oks.disableSSOWhenAuthorize();
				}
			}
			if(params.containsKey("theme")){
				String theme = (String) params.get("theme");
				if("shybule".equals(theme)){
					oks.setTheme(OnekeyShareTheme.SKYBLUE);
				}else {
					oks.setTheme(OnekeyShareTheme.CLASSIC);
				}
			}
			JSPlatformActionListener pa = new JSPlatformActionListener();
			pa.setCallback(this);
			pa.setSeqId(seqId);
			pa.setJSCallback(callback);
			pa.setOriCallback(oriCallback);
			pa.setApi(api);
			oks.setCallback(pa);
			oks.setDialogMode();
			if (webview != null) {
				oks.setEditPageBackground(webview);
			}
			oks.show(context);
		}
		return null;
	}
	
	/**
	 * A key to pop up share selection box
	 * @param seqId
	 * @param api
	 * @param callback
	 * @param oriCallback
	 * @param params
	 * @return
	 */
	private String showShareView(String seqId, String api, String callback, String oriCallback, HashMap<String, Object> params) {
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>) params.get("shareParams");
		if (map != null) {
			OnekeyShare oks = new OnekeyShare();
			if (map.containsKey("title")) {
				oks.setTitle(String.valueOf(map.get("title")));
			}
			if (map.containsKey("text")) {
				oks.setText(String.valueOf(map.get("text")));
			}
			if (map.containsKey("imagePath")) {
				oks.setImagePath(String.valueOf(map.get("imagePath")));
			}
			if (map.containsKey("imageUrl")) {
				oks.setImageUrl(String.valueOf(map.get("imageUrl")));
			}
			if (map.containsKey("comment")) {
				oks.setComment(String.valueOf(map.get("comment")));
			}
			if (map.containsKey("site")) {
				oks.setSite(String.valueOf(map.get("site")));
			}
			if (map.containsKey("url")) {
				oks.setUrl(String.valueOf(map.get("url")));
			}
			if (map.containsKey("siteUrl")) {
				oks.setSiteUrl(String.valueOf(map.get("siteUrl")));
			}
			JSPlatformActionListener pa = new JSPlatformActionListener();
			pa.setCallback(this);
			pa.setSeqId(seqId);
			pa.setJSCallback(callback);
			pa.setOriCallback(oriCallback);
			pa.setApi(api);
			oks.setCallback(pa);
			int platform = (Integer) params.get("platform");
			String platformName = ShareSDK.platformIdToName(platform);
			oks.setPlatform(platformName);
			oks.setDialogMode();
			if (webview != null) {
				oks.setEditPageBackground(webview);
			}
			oks.show(context);
		}
		return null;
	}
	
	/**
	 * Through the accounts say to get friends list
	 * @param seqId
	 * @param api
	 * @param callback
	 * @param oriCallback
	 * @param params
	 */
	private void getFriendList(String seqId, String api, String callback, String oriCallback, HashMap<String, Object> params){
		int platformId = (Integer) params.get("platform");
		boolean isSSO = (Boolean) params.get("isSSO");
		int page = (Integer) params.get("page");
		int count = (Integer) params.get("count");
		String account = (String) params.get("account");
		String platformName = ShareSDK.platformIdToName(platformId);
		Platform platform = ShareSDK.getPlatform(context, platformName);
		JSPlatformActionListener pa = new JSPlatformActionListener();
		pa.setCallback(this);
		pa.setSeqId(seqId);
		pa.setJSCallback(callback);
		pa.setOriCallback(oriCallback);
		pa.setApi(api);
		platform.setPlatformActionListener(pa);
		platform.SSOSetting(isSSO);
		platform.listFriend(count, page, account);
	}
	
	/**
	 * Through the accounts say to get friends list
	 * @param seqId
	 * @param api
	 * @param callback
	 * @param oriCallback
	 * @param params
	 */
	private void followFriend(String seqId, String api, String callback, String oriCallback, HashMap<String, Object> params){
		int platformId = (Integer) params.get("platform");
		String friendName = (String) params.get("friendName");
		jsLog("friendName = " + friendName);
		String platformName = ShareSDK.platformIdToName(platformId);
		Platform platform = ShareSDK.getPlatform(context, platformName);
		JSPlatformActionListener pa = new JSPlatformActionListener();
		pa.setCallback(this);
		pa.setSeqId(seqId);
		pa.setJSCallback(callback);
		pa.setOriCallback(oriCallback);
		pa.setApi(api);
		platform.setPlatformActionListener(pa);
		platform.followFriend(friendName);
	}
	
}
