/**
 * Created by Administrator on 2016/11/1.
 */
$(function(){
    //配置文件路径
    require.config({
      paths:{
        echarts:"../lib/dist"
      }
    });
    //加载网页中所需要的图表
    require(
      [
        "echarts",
        "echarts/chart/bar",//柱状图
        "echarts/chart/pie",//饼状图
        "echarts/chart/gauge",//仪表盘
        "echarts/theme/macarons"//取到对应的主题
      ],
      function(ec){
        //init初始化数据
        var option1=ec.init(document.getElementById("chart01"),"macarons");
        var option2=ec.init(document.getElementById("chart02"),"macarons");
        var option3=ec.init(document.getElementById("chart03"),"macarons");
        //仪表盘
        var echart={
          title:{
            text:"学习比例人数",//标题
            subText:"",//小标题
            x:"center",//标题显示位置
          },
          tooltip:{
          //tooltip工具提示框   formatter数据占有比例
            formatter:"{a}</br>{b}:{c}%", //显示出表盘中整体的数据比例
          },
          //series表盘显示数据
          series:[
            {
              name:"学习人数比例",//提示框标题内容
              type:"gauge", //表盘类型
              detail:{formatter:"{value}%"},//数据显示
              data:[{value:80,name:"学习人数比例"}]//提示框内容
            }
          ]
        };
        option1.setOption(echart);//设置数据
      //  饼状图
        var echart2={
          title:{
            text:"考试通过率",
            subtext:"",
            x:"center"
          },
          tooltip:{
            formatter:"{a}<br>{b}:{c}({d}%)"
          },
          series:[{
            radius:"50%",
            type:"pie",
            center:["50%","50%"],
            data:[
              {value:400,name:"已通过"},
              {value:200,name:"未通过"}
            ]
          }]
        };
        option2.setOption(echart2);
        //柱状图
        var echart3={
          title:{
            text:"事件、课程、新增课程数",
            subtext:"",
            x:"center"
          },
          tooltip:{
            trigger:"item"
          },
          calculable: true,
          grid: {
            borderWidth: 0,
            y: 80,
            y2: 60
          },
          xAxis: [
            {
              type: 'category',
              show: false,
              data: ['Line', 'Bar', 'Scatter', 'K', 'Pie', 'Radar', 'Chord', 'Force', 'Map', 'Gauge', 'Funnel']
            }
          ],
          yAxis: [
            {
              type: 'value',
              show: false
            }
          ],
          series:[{
            name:'事件、课程、新增课程数',
            type:"bar",
            itemStyle: {
              normal: {
                color: function(params) {
                  // build a color map as your need.
                  var colorList = [
                    '#C1232B','#B5C334','#FCCE10','#E87C25','#27727B',
                    '#FE8463','#9BCA63','#FAD860','#F3A43B','#60C0DD',
                    '#D7504B','#C6E579','#F4E001','#F0805A','#26C0C0'
                  ];
                  return colorList[params.dataIndex]
                },
                label: {
                  show: true,
                  position: 'top',
                  formatter: '{b}\n{c}'
                }
              }
            },
            data: [12,21,10,4,12,5,6,5,25,23,7],
            markPoint: {
              tooltip: {
                trigger: 'item',
                backgroundColor: 'rgba(0,0,0,0)',
                formatter: function(params){
                  return '<img src="'
                    + params.data.symbol.replace('image://', '')
                    + '"/>';
                }
              },
              data: [
                {xAxis:0, y: 350, name:'Line', symbolSize:20, symbol: 'image://../asset/ico/折线图.png'},
                {xAxis:1, y: 350, name:'Bar', symbolSize:20, symbol: 'image://../asset/ico/柱状图.png'},
                {xAxis:2, y: 350, name:'Scatter', symbolSize:20, symbol: 'image://../asset/ico/散点图.png'},
                {xAxis:3, y: 350, name:'K', symbolSize:20, symbol: 'image://../asset/ico/K线图.png'},
                {xAxis:4, y: 350, name:'Pie', symbolSize:20, symbol: 'image://../asset/ico/饼状图.png'},
                {xAxis:5, y: 350, name:'Radar', symbolSize:20, symbol: 'image://../asset/ico/雷达图.png'},
                {xAxis:6, y: 350, name:'Chord', symbolSize:20, symbol: 'image://../asset/ico/和弦图.png'},
                {xAxis:7, y: 350, name:'Force', symbolSize:20, symbol: 'image://../asset/ico/力导向图.png'},
                {xAxis:8, y: 350, name:'Map', symbolSize:20, symbol: 'image://../asset/ico/地图.png'},
                {xAxis:9, y: 350, name:'Gauge', symbolSize:20, symbol: 'image://../asset/ico/仪表盘.png'},
                {xAxis:10, y: 350, name:'Funnel', symbolSize:20, symbol: 'image://../asset/ico/漏斗图.png'},
              ]
            }
          }]
        };
        option3.setOption(echart3);
      }
    );
});

