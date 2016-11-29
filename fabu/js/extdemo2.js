/**
 * Created by Administrator on 2016/11/3.
 */
var GLOBAL=GLOBAL||{};
Ext.onReady(function(){
  //当改变窗口时自动刷新表格
  Ext.EventManager.onWindowResize(function(){
    GLOBAL.studentGrid.getView().refresh()
  })
//  搜索框
  GLOBAL.studentSearch=Ext.create("Ext.form.Panel",{
    renderTo:"searchBox",
    layout:"hbox",
    margin:10,
    items:[{
      xtype:"panel",
      layout:"hbox",
      items:[
        new Ext.custom.textfield({
          itemId:"userName",
          emptyText:"用户名",
        }),
        new Ext.custom.textfield({
          itemId:"email",
          emptyText:"邮箱"
        }),
        new Ext.custom.textfield({
          itemId:"phone",
          emptyText:"手机号"
        })
      ]
    },{
      xtype:"button",
      text:"查询",
      width:100,
      height:34,
      style:"background:green",
      handler:function(){

      }
    }]
  })
  //表格
  //创建数据仓库
  GLOBAL.studentStore=Ext.create("Ext.data.Store",{
    fields:["userName","email","phone","createAt","isstate","tokenId"]
  });
  ////分页栏
  GLOBAL.studentBar=Ext.create("PagingToolBar",{
    id:"pageId",
    onChange:function(num,pageFirst,pageSize){
      var pageStart=pageFirst/pageSize+1;
      studentList(pageStart,pageFirst);
    }
  });
  //创建
  GLOBAL.studentGrid=Ext.create("Ext.grid.Panel",{
    bbar:GLOBAL.studentBar,
    store:GLOBAL.studentStore,
    minHeight:BPR.gridMinHeight,
    id:"studentId",
    columns:[
      {"header":"用户名",width:150,dataIndex:"userName"},
      {"header":"邮箱",width:150,dataIndex:"email",flex:1},
      {"header":"手机",width:150,dataIndex:"phone"},
      {"header":"注册日期",width:150,dataIndex:"createAt"},
      {"header":"状态",width:150,dataIndex:"isstate",
        renderer:function(value,cellmeat,record){
          //value作为单元格的值
          if(value){
            return '<a href="#" class="xn_tablea" style="color: red;">已冻结</a>'
          }else{
            return '正常'
          }
        }},
      {"header":"操作",width:200,dataIndex:"tokenId",
        renderer:function(value,cellmeta,record){
          //record获取整行记录
          var lock=record.data.isstate?'解冻':'冻结';
          return '<a href="#" class="xn_tablea" onclick="userD('+value+')">'+lock+'</a>'
        }
      }
    ],
    renderTo:"studentList"
  });
  studentList();
})
function studentList(pageStart,pageFirst){
  //pageStart第几页   pageFirst当前页第一条数据的下标
  GLOBAL.msgTip=new Ext.LoadMask(Ext.getCmp("studentId"),{
    msg:"数据加载中",
    removeMask:true
  }).show();
  $.ajax({
    url:"/Handler/AdminHandler.ashx?action=usershow",
    type:"post",
    dataType:"json",
    async:false,
    data:{
      userName:GLOBAL.studentSearch.down("#userName").getValue(),
      email:GLOBAL.studentSearch.down("#email").getValue(),
      phone:GLOBAL.studentSearch.down("#phone").getValue(),
      pageStart:pageStart?pageStart:1
    }
  }).done(function(result){
    GLOBAL.studentBar.setPageSize(result.data.pageSize);
    GLOBAL.studentBar.loadPage(pageFirst?pageFirst:0,result.data.count);
    GLOBAL.studentStore.loadData(result.data.list);
  })
  GLOBAL.msgTip.hide();
}
function userD(id){
  $.ajax({
    url:"/Handler/AdminHandler.ashx?action=lockuser",
    type:"get",
    dataType:"json",
    async:false,
    data:{
      tokenId:id
    }
  }).done(function(result){
    Ext.Msg.alert("提示",result.success)
    studentList();
  })
}
