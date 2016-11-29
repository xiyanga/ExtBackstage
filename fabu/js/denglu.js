/**
 * Created by Administrator on 2016/11/1.
 */
$(function(){
  $(document).keydown(function(ev){
    var ev=event||ev;

    if(ev.keyCode==13){
      login();
    }
  })
  function login(){
    var userName=$("#user").val()
    var password=$("#password").val();
    $.ajax({
      //url请求路径
      // type传输方式
      // dataType传输文件类型,dataType:json值为json是json是一个轻量级的而且方便传输
      // async同步异步
      url:"/Handler/AdminLoginAndRegHandler.ashx?action=login",
      type:"post",
      dataType:"json",
      async:false,
      data:{
        userName:userName,
        password:password,
      },
      //成功时执行函数
      //success:function(result){
      //  console.log(result);
      //},
      ////报错执行函数
      //err:function(result){
      //  console.log(result);
      //}
    }).done(function(result){
      if(result.success){
        window.location="header.html";
      }
      if(result.err){
        console.log(result.err);
      }
    });
  }
  $("#login").click(function(){
    login();
  });
});
