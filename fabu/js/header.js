/**
 * Created by Administrator on 2016/11/1.
 */
var GLOBAL=GLOBAL||{};
Ext.onReady(function(){
  //当改变窗口时框架高度也变化
  $(window).resize(function(){
    $("#ifra").height($(window).height()-100)
  })
  $(".icon_wrap").hover(function(){
    $(this).find(".left").addClass("le");
    $(this).find(".right").addClass("ri");
    $(".select").css({display:"none"});
    $(this).siblings(".select").css({display:"block"});
  });
  $(".nav11").mouseleave(function(){
    $(this).find(".left").removeClass("le");
    $(this).find(".right").removeClass("ri");
    $(".select").css({display:"none"});
  });
  returnInfo();
  $(".select").delegate(".se","click",function(){
    if($(this).attr("iLink")){
      var iLink=$(this).attr("iLink");
      var oLiId=$(this).attr("id");
      var pathName=$(this).parent().siblings(".icon_wrap").text()+"/"+$(this).text();
      loadP(iLink,pathName,oLiId)
    }
  })
  loadHashPage();
});

function changePwd(){
  GLOBAL.pwdBox=new Ext.custom.basicWindow({
    title:"修改密码",
    width:500,
    height:300,
    bodyPadding:30,
    items:[{
      xtype:"panel",
      items:[
        new Ext.custom.middletextfield({
          itemId:"pwd",
          fieldLabel: "当前密码",
          beforeLabelTextTpl: required,
          inputType:"password",
          margin:10
        }),
        new Ext.custom.middletextfield({
          itemId:"newPwd",
          fieldLabel:"新密码",
          inputType:"password",
          beforeLabelTextTpl: required,
          margin:10
        }),
        new Ext.custom.middletextfield({
          itemId:"truePwd",
          fieldLabel:"确认密码",
          inputType:"password",
          beforeLabelTextTpl: required,
          margin:10
        })
      ]
    },{
      xtype:"panel",
      layout:"hbox",
      items:[{
        xtype:"button",
        width:100,
        height:34,
        text:"确定",
        margin:"20 0 0 80",
        handler:function(){
          var oldPwd=GLOBAL.pwdBox.down("#pwd").getValue();
          var newPwd=GLOBAL.pwdBox.down("#newPwd").getValue();
          var truePwd=GLOBAL.pwdBox.down("#truePwd").getValue();
          if(oldPwd==""||newPwd==""||truePwd==""){
            Ext.Msg.alert("提示","密码不能为空");
          }else if(oldPwd==newPwd){
            Ext.Msg.alert("提示","两次密码不可重复");
          }else{
            $.ajax({
              url:"/Handler/AdminHandler.ashx?action=updatepass",
              type:"post",
              dataType:"json",
              async:false,
              data:{
                userPwd:oldPwd,
                newPwd:newPwd,
              }
            }).done(function(result){
              if(result.success){
                Ext.Msg.alert("提示",result.success);
                GLOBAL.pwdBox.hide();
              }
              if(result.error){
                Ext.Msg.alert("提示",result.error);
              }
            });
          }
        }
      },{
        xtype:"button",
        width:100,
        height:34,
        text:"取消",
        margin:"20 10 0 80",
        style:"background:orange;border:none;outline:none;",
        handler:function(){
          GLOBAL.pwdBox.hide();
        }
      }]
    }]
  }).show();
}
//返回消息
function returnInfo(){
  $.ajax({
    url:"/Handler/AdminHandler.ashx?action=returnuserinfo",
    type:"post",
    dataType:"json",
    async:false,
    data:""
  }).done(function(result){
    $("#userName").html(result.turename);
  });
}
//返回登录页面
function returnPage(){
  $.ajax({
    url:"/Handler/AdminHandler.ashx?action=quit",
    type:"get",
    dataType:"json",
    async:false,
    data:""
  }).done(function(result){
    if(result.success){
      window.location="denglu.html";
    }
    if(result.err){
      Ext.Msg.alert("提示",result.err);
    }
  });
}
//点击时根据对应的路径跳到HTML
function loadHashPage(){
  if(window.location.hash){
    var menuHashId=window.location.hash.substring(1);
    if($("#"+menuHashId).length>0){
      //trigger给匹配的元素绑定对应事件
      //当没有给其他绑定事件时，会一直加载当前元素身上
      $("#"+menuHashId).trigger("click")
    }else{
      //没有执行点击事件时所执行的操作
      loadP("index1.html","","")
    }
  }else{
    //没有hash时所执行的操作
    loadP("index1.html","","")
  }
}
//加载iframe页面
function loadP(link,pathName,id){
  //把iframe加载进页面，放进空div中
  var h=$(window).height()-100;
  $("#backContent").html("<iframe id='ifra' width='100%' height='"+h+"' src='"+link+"' frameborder=0></iframe>");
  window.location.hash=id;
  $("#ifra").load(function() {
      $(this).contents().find(".xn_nowpath").html("<a href='#'>首页</a>/"+pathName);
    }
  )
}

