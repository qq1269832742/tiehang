<%@page language="java" contentType="text/html; charset=UTF-8" 
   pageEncoding="UTF-8"%>
<%@page import="com.levelappro.gbism.app.jrfw.dataset.IDataset"%>
<%
  String basePath = request.getScheme()+"://"
				  + request.getServerName()+":"
				  + request.getServerPort()
				  + request.getContextPath()+"/";
	
  IDataset ds = (IDataset)request.getAttribute("dataset"); 
  String parentIdOfEnd = "";
  String color = null;
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>TA列表界面</title>
<!--[if IE]><script type="text/javascript" src="app/jrfw/dailystart/js/excanvas.js"></script><![endif]-->
<script type="text/javascript" src="app/jrfw/dailystart/js/ECOTree.js"></script>
<link type="text/css" rel="stylesheet" href="app/jrfw/dailystart/css/ECOTree.css" />


<script type="text/javascript" src="script/ext-3.2.1/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="script/ext-3.2.1/ext-all.js"></script>
<script type="text/javascript" src="script/ext-3.2.1/ext-lang-zh_CN.js"></script>
<link type="text/css" rel="stylesheet" href="script/ext-3.2.1/resources/css/ext-all.css" />

<script type="text/javascript">
var canSend = true;
var clickedNodeId = "";

var statusUrl = '<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartStatus';

var parentPage = (navigator.appName=="Microsoft Internet Explorer"?
				  document.parentWindow.parent:
				  document.defaultView.parent);
//页面ID,唯一定位这个页面
var pageId = parentPage.currentActivePageId;

//ADDED BY YHB 20110623 兼容不同浏览器中的事件 BEGIN
var browserUserSgent = navigator.userAgent;
var browserType = '0';  //默认是IE浏览器
if(browserUserSgent.indexOf("Chrome")!=-1){
	browserType = '1';  //google浏览器
}
if(browserUserSgent.indexOf("Firefox")!=-1){
	browserType = '2';  //Firefox浏览器
}
//获得鼠标位置
var getMousePosition = function(event){
	try {
		event = event || window.event;
		if(event.pageX || event.pageY){
			return [event.pageX,event.pageY];
		}
		return [event.clientX + document.body.scrollLeft - document.body.clientLeft,event.clientY + document.body.scrollTop  - document.body.clientTop];
		
	} catch (e) {
		alert(e);
	}
}
//ADDED BY YHB 20110623 兼容不同浏览器中的事件 END

function CreateTree() {
	var myTree = null;
	myTree = new ECOTree('myTree','myTreeContainer');
	myTree.config.topXAdjustment = parentPage.middlePosition/3;
	myTree.config.topYAdjustment = 20;

	<%if(null!=ds && ds.findColumn("id")>0){
			ds.beforeFirst();
			for(int i=0;i<ds.getRowCount();i++){
				ds.next();
				if(i==ds.getRowCount()-1){
					parentIdOfEnd = ds.getString("id");
				}
				char status = ds.getString("status").charAt(0);
				switch(status){
					case '0': color = "#D9D6C3";break;
					case '1': color = "#2A5CAA";break;
					case '2': color = "#FF00FF";break;
					case '3': color = "#00FF00";break;
					case '4': color = "#FF0000";break;
					case '5': color = "#8552A1";break;
					case 'Z': color = "#FFFF00";break;
					default: color = "#D9D6C3";
				}
	%>
        myTree.add('<%=(String)ds.getString("id")%>','<%=(String)ds.getString("pid")%>',
                   '<%=(String)ds.getString("describe")+":"+(String)ds.getString("id")%>',
                   '','','<%=color%>','','','',
                   '<%=(String)ds.getString("flag")%>',
                   '<%=(String)ds.getString("id")%>',
                   '<%=(String)ds.getString("marketCode")%>',
                   '<%=(String)ds.getString("parentTaskCode")%>',
                   '<%=(String)ds.getString("detailUrl")%>');
	<%}}%>

	myTree._positionTree1(6);
	var canvas = document.getElementById("canvas");
	myTree.canvasoffsetLeft = canvas.offsetLeft;
	myTree.canvasoffsetTop = canvas.offsetTop;
	myTree.setCtx(canvas.getContext('2d'));
	myTree._drawTree1();
	
	var div = document.getElementById("myTreeContainer");
	div.innerHTML = myTree.drawDiv();
}

function updateStatus(json){
	canSend = true;
	var warnMsg = '';
	var activePageId = parentPage.tabs.getActiveTab().getId();    //当前显示的Tab页的ID
	var currentFrameActivePageId = parentPage.pTabs.getActiveTab().getId();
	
	try {
		if(null!=json.dataSetResult[0] && null!=json.dataSetResult[0].data && json.dataSetResult[0].data.length>0){
			for(var i=0;i<json.dataSetResult[0].data.length;i++){
				var node = document.getElementById(json.dataSetResult[0].data[i].id);
				if(null==node || 'undefined' == node){
					alert("无法找到节点:" + json.dataSetResult[0].data[i].id + ",请检查清算配置表中的配置!");
					return;
				}
				
				var color = "";
				if(json.dataSetResult[0].data[i].status =="4"){
					color = "red";
				}
				else{
					color = "black";
				}
					
				if("0" == json.dataSetResult[0].data[i].status){
					node.style.backgroundColor= "#D9D6C3";
				}
				if("1" == json.dataSetResult[0].data[i].status){
					node.style.backgroundColor= "#2A5CAA";
				}
				if("2" == json.dataSetResult[0].data[i].status){
					node.style.backgroundColor= "#FF00FF";
				}
				if("3" == json.dataSetResult[0].data[i].status){
					node.style.backgroundColor= "#00FF00";
				}
				if("4" == json.dataSetResult[0].data[i].status){
					node.style.backgroundColor= "#FF0000";
				}
				if("5" == json.dataSetResult[0].data[i].status){
					node.style.backgroundColor= "#8552A1";
				}
				if("Z" == json.dataSetResult[0].data[i].status){
					node.style.backgroundColor= "#FFFF00";
				}
			}
		}
	} catch (e) {
		alert(e);
	}
}

//params是json格式,修改超时时间为10分钟
function sendAndCall(requestUrl,params){
	Ext.Ajax.request({
		url:requestUrl,
		params:params,
		method:'post',
		timeout:600000,
		callback : function(options, success, response) {   
        	if(success) {   
        		var executeMode = params.ExceuteMode;
            	var jsonData = eval('('+ response.responseText + ')'); 
				
            	if("T"==executeMode){             //定时查询
            		updateStatus(jsonData);
            	}
        	}else {   
        		Ext.MessageBox.alert('提示','通信失败!');
        	}   
    	}  	
	});
}

//TA节点的单击
function taLeftClick(obj){
	var tabId = obj.id;
	var tabs = parentPage.tabs;
	var tab_length = tabs.items.length;
	if(tab_length>6){
		Ext.Msg.alert("提示","打开TAB页个数过多，请关闭不用的TAB页提高访问速度");
    	return;
    }
	var tab = tabs.getComponent(tabId);
	
	if(tab){
	    tabs.setActiveTab(tab);
	}else{
		parentPage.addTabById(tabId);
	}
}

function timingSendUpdate(){
	<%if(null!=ds && ds.findColumn("id")>0){%>
		if(canSend){
			canSend = false;
			var param={flowType:"2",ExceuteMode:"T"};
			try {
				sendAndCall(statusUrl,param);
			} catch (e) {
				alert(e);
			}
		}
	<%}%>
}

</script>
</head>
<body onload="CreateTree()" oncontextmenu="return false;">
<form name="containerForm">
  <div id="myTreeContainer"></div>
  <canvas id="canvas" width=100 height=100></canvas>
</form>
</body>
<script language="JavaScript">
    setInterval(timingSendUpdate,5*1000);
</script>
</html>