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
  String defaultBankNo = "999";
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>日启操作</title>
<!--[if IE]><script type="text/javascript" src="app/jrfw/dailystart/js/excanvas.js"></script><![endif]-->
<script type="text/javascript" src="app/jrfw/dailystart/js/ECOTree.js"></script>
<link type="text/css" rel="stylesheet" href="app/jrfw/dailystart/css/ECOTree.css" />

<script type="text/javascript" src="script/ext-3.2.1/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="script/ext-3.2.1/ext-all.js"></script>
<script type="text/javascript" src="app/jrfw/dailystart/js/MsgWindow.js"></script>
<link type="text/css" rel="stylesheet" href="script/ext-3.2.1//resources/css/ext-all.css" />

<script type="text/javascript">
var canSend = true;
var clickedNodeId = "";

var actionUrl = '<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartAction';
var statusUrl = '<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartStatus';
var tipUrl    = '<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartTip';
var checkUrl  = '<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartCheck';
var detailUrl = '<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartDetail';
var downUrl   = '<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartDown';

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

//错误消息框
var msgWindow  = new MsgWindow({
	id : Ext.id(),
    width:160,
    height:200,
    x:parentPage.middlePosition - 250,
    y:235 
});
//处理鼠标滚轮事件
if(navigator.appName=="Microsoft Internet Explorer"){     //IE浏览器
	window.onscroll = function(){
    	var scroll = Ext.getDoc().getScroll();
    	if(msgWindow.isVisible())
        {
            if(scroll.top + 300 < canvasHeight){
    			msgWindow.getEl().moveTo(scroll.left + parentPage.middlePosition - 250, scroll.top + 235,true);
    		}
    	}
    }
}else{
	 Ext.getDoc().on('scroll',function(e){
	     var scroll = Ext.getDoc().getScroll();
	     if(this.msgWindow.isVisible())
	     {
	    	 if(scroll.top + 300 < canvasHeight){
	    			this.msgWindow.getEl().moveTo(scroll.left + parentPage.middlePosition - 250, scroll.top + 235,true);
	    	 }
	     }
	 });
}

function CreateTree() {
	var myTree = null;
	myTree = new ECOTree('myTree','myTreeContainer');
	myTree.config.topXAdjustment = parentPage.middlePosition/3;
	myTree.config.topYAdjustment = 20;
					
	myTree.add('0','-1','START','','','#CDE6C7','','','','0','','');

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
                   '<%=(String)ds.getString("detailUrl")%>',
                   '<%=(String)ds.getString("bankNo")%>',
                   '<%=(String)ds.getString("multiFlag")%>');
	<%}}%>

	myTree.add('end','<%=parentIdOfEnd.equals("")?"0":parentIdOfEnd%>','END','','','#CDE6C7','','','','0','','');
	myTree._positionTree();
	var canvas = document.getElementById("canvas");
	myTree.canvasoffsetLeft = canvas.offsetLeft;
	myTree.canvasoffsetTop = canvas.offsetTop;
	myTree.setCtx(canvas.getContext('2d'));
	myTree._drawTree();
	
	var div = document.getElementById("myTreeContainer");
	div.innerHTML = myTree.drawDiv();
}

var contextmenu = new Ext.menu.Menu({
	id:'contextMenu',
	enableScrolling: false,
	items:[new Ext.menu.Item({
			id:'activeMenu',
			text:'运行',
			icon:'app/jrfw/dailystart/images/run.png',
			handler:onActiveClick
		}),
		new Ext.menu.Item({
			id:'stepMenu',
			text:'单步',
			icon:'app/jrfw/dailystart/images/step1.png',
			handler:onStepClick
		}),
		new Ext.menu.Item({
			id:'detailMenu',
			text:'查看详细',
			icon:'app/jrfw/dailystart/images/detail.png',
			handler:onDetailClick
		})
		//new Ext.menu.Item({
		//	id:'downMenu',
		//	text:'日志下载',
		//	icon:'app/jrfw/dailystart/images/download4.png',
		//	handler:onDownClick
		//})
	]
});

var detailWin = new Ext.Window({
	el:'detail-win',
	title:'详细信息',
	layout:'fit',
	width:370,
    height:250,
    closeAction:'hide',
    autoScroll:true,
    plain: true,
    resizable:true,
    items:[new Ext.Panel({
	    	id:'detailPanel',
			autoScroll:true,
		    html: '<div id="detail_div"></div>'
    	})]
});

function showMessage(func,url,param){
	Ext.MessageBox.show({
        title:'确认',
        msg: '是否开始执行本步骤?',
        buttons: Ext.MessageBox.YESNO,
        fn: function(btn) {
        	if("yes"==btn){
        		Ext.Ajax.request({
        			url:url,
        			params:param,
        			method:'post',
        			timeout:600000,
        			callback : function(options, success, response) {   
        	        	if(success) { 
        	            	var jsonData = eval('('+ response.responseText + ')'); 
        	            	var status = jsonData.dataSetResult[0].data[0].status;
        	            	if('3'==status){
            	            	Ext.MessageBox.show({
                	            	title:'确认',
                    	            msg:'该交易已完成,是否强制执行?',
                    	            buttons: Ext.MessageBox.YESNO,
                    	            fn: function(btn) {
                    	            	if("yes"==btn){
                        	            	func("1");//强制发起
                    	            	}
                    	            }
                	            });
        	            	}
        	            	else{
        	            		func("0"); //正常发起
        	            	}
        	        	}else {   
        	        		Ext.MessageBox.alert('提示','校验失败!'); 
        	        	}   
        	    	}  	
        		});
        	}
        },
        animEl: 'dialog'
    });
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

				//处理warnMsg
				if(null!=json.dataSetResult[0].data[i].warnMsg && json.dataSetResult[0].data[i].warnMsg.length>0){     //存在错误信息
					warnMsg += "{msg:'" + "<font size=2 color=red>节点" + json.dataSetResult[0].data[i].id + " : " + json.dataSetResult[0].data[i].warnMsg + "</font>'},";
				}
			}

			if(currentFrameActivePageId == parentPage.framePageId && pageId.length>0 && pageId == activePageId){     //表示当前页可见
				if(warnMsg.length>0){
					warnMsg = warnMsg.substring(0,warnMsg.length-1);
					var jsonMsgStr = "{dataSetResult:[{data:[" + warnMsg + "]}]}";
					var jsonData = Ext.decode(jsonMsgStr);

					if(!msgWindow.isVisible()){
						msgWindow.show();
					}
					msgWindow.loadMsg(jsonData);
				}else{
					msgWindow.clearMsg();
					msgWindow.hide(); 
				}
			}else{
				msgWindow.clearMsg();
				msgWindow.hide(); 
			}
		}
	} catch (e) {
		alert(e);
	}
}

function activeBack(json){
	try {
		var errorNo = json.dataSetResult[0].data[0].ErrorNo;
		var errorInfo = json.dataSetResult[0].data[0].ErrorInfo;
		var node = document.getElementById(json.dataSetResult[0].data[0].taskCode);
		
		if("0000"==errorNo){
			node.style.backgroundColor= "#2A5CAA";
			Ext.MessageBox.alert('提示','运行成功!');
		}else{
			Ext.MessageBox.alert('提示',errorNo + ':' + errorInfo);
		}
	} catch (e) {
		alert(e);
	}
}

function stepBack(json){
	try {
		var returnCode = json.dataSetResult[0].data[0].ReturnCode;
		var errorNo    = json.dataSetResult[0].data[0].ErrorNo;
		var errorInfo  = json.dataSetResult[0].data[0].ErrorInfo;
		var node = document.getElementById(json.dataSetResult[0].data[0].taskCode);
		
		if("0000"==errorNo){
			node.style.backgroundColor= "#D9D6C3";
			Ext.MessageBox.alert('提示','单步操作触发成功!');
		}else{
			Ext.MessageBox.alert('提示','单步操作触发失败!');
		}
	} catch (e) {
		alert(e);
	}
}

function updateDetail(json){
	try {
		var html = "";
		if(null!=json.dataSetResult[0] && null!=json.dataSetResult[0].data && json.dataSetResult[0].data.length>0)
		{
			html = "<font size=2>"
			     + "作业名称:"+ json.dataSetResult[0].data[0].describe + "<br>"
			     + "市场代码:"+ json.dataSetResult[0].data[0].marketCode + "<br>" ;
			//如果一个节点有两条配置信息,则这里需要分别显式
			for(var i=0;i<json.dataSetResult[0].data.length;i++)
			{
				if(i>0)     //第二条配置中间需要加一行
				{         
					html += "  " + "<br>";
				}
				html += "父作业码:"       + json.dataSetResult[0].data[0].parentTaskCode + "<br>"
	            	 + "归属模块:"  		 + json.dataSetResult[0].data[0].taskType       + "<br>"
			     	 + "是否允许重做:" 	 + json.dataSetResult[0].data[0].redoFlag       + "<br>"
			     	 + "延时处理标识:" 	 + json.dataSetResult[0].data[0].delayFlag      + "<br>"
			     	 + "延时时间:"		 + json.dataSetResult[0].data[0].delayTime      + "<br>"
			     	 + "执行百分比例:"	 + json.dataSetResult[0].data[0].exePercnet     + "<br>"
			    	 + "执行结果:"		 + json.dataSetResult[0].data[0].errorCode      + "<br>"
			   	     + "执行结果信息:"	 + json.dataSetResult[0].data[0].errorMsg       + "<br>"
			         + "提示信息:"		 + json.dataSetResult[0].data[0].warnMsg        + "<br>"
			         + "执行开始时间:"    + json.dataSetResult[0].data[0].beginTime      + "<br>"
			         + "执行结束时间:"    + json.dataSetResult[0].data[0].endTime        + "<br>"
			         + "持续时间(ms):"     + json.dataSetResult[0].data[0].holdTime      + "<br>"
			         + "最近执行日期:"    + json.dataSetResult[0].data[0].lastTransDate  + "<br>" ;
			}
		         html += "</font>";
		}
		detailWin.show();
		Ext.DomHelper.overwrite(document.getElementById("detail_div"),html);
	} catch (e) {
		alert(e);
	}
}

function showLog(json){
	try {
		var marketCode = json.dataSetResult[0].data[0].marketCode;
		var taskCode = json.dataSetResult[0].data[0].taskCode;
		var log = json.dataSetResult[0].data[0].log;
		
		var tabId = marketCode + taskCode;
		var tabs = parentPage.tabs;
		var tab_length = tabs.items.length;
		if(tab_length>6){
			Ext.Msg.alert("提示","打开TAB页个数过多，请关闭不用的TAB页提高访问速度");
	    	return;
	    }
		if(contextmenu){
	    	contextmenu.hide();
	    }
	    if(msgWindow){
	    	msgWindow.hide();
	    }
		var tab = tabs.getComponent(tabId);
		if(tab){
		    tabs.setActiveTab(tab);
		}else{
			parentPage.addTabForLog(tabId,marketCode,taskCode,log);
		}
	} catch (e) {
		alert(e);
	}
}

function getTips(json,target){
	try {
		var html = "";
		if(null!=json.dataSetResult[0] && null!=json.dataSetResult[0].data && json.dataSetResult[0].data.length>0){
			html = "<font size=2>"
			     + "作业名称:"+ json.dataSetResult[0].data[0].describe + "<br>"
			     + "市场代码:"+ json.dataSetResult[0].data[0].marketCode + "<br>"
		         + "父作业码:"+ json.dataSetResult[0].data[0].parentTaskCode + "<br>"
			     + "</font>";
		}
		Ext.DomHelper.overwrite(document.getElementById("div_" + target),html);
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
            	else if("P"==executeMode){             //获得tips
                	getTips(jsonData,params.taskCode);
            	}
            	else if("X"==executeMode){             //获得详细信息
                	updateDetail(jsonData);
            	}
            	else if("D"==executeMode){             //显示日志
                	showLog(jsonData);
            	}
        	}else {   
        		Ext.MessageBox.alert('提示','通信失败!');
        	}   
    	}  	
	});
}

//发后台服务,单步和运行用这个函数
function sendAndToHost(url, params){
	Ext.Ajax.request({
		url:url,
		params:params,
		method:'post',
		timeout:600000,
		callback : function(options, success, response) {   
        	if(success) {   
        		var executeMode = params.ExceuteMode;
            	var result = eval('('+ response.responseText + ')'); 
				
            	if("1"==executeMode){                  //单步交易返回
                	stepBack(result);
            	}
            	else if("2"==executeMode){             //激活交易返回
                	activeBack(result);
            	}
        	}else {   
        		Ext.MessageBox.alert('提示','通信失败!');
        	}   
    	}  	
	});
}

//运行:ExceuteMode送2
function onActiveClick(){
	var func = function(operFlag){
		var param={nodeId:clickedNodeId,taCode:"000000",ExceuteMode:"2",operFlag:operFlag,bankNo:"<%=defaultBankNo%>"};
		try {
			sendAndToHost(actionUrl,param);
		} catch (e) {
			alert(e);
		}
	};
	
	var checkParam = {taskCode:clickedNodeId,taCode:"000000",bankNo:"<%=defaultBankNo%>"};
	showMessage(func,checkUrl,checkParam);
}

//单步:ExecuteMode送1
function onStepClick(){
	var func = function(operFlag){
		var param={nodeId:clickedNodeId,taCode:"000000",ExceuteMode:"1",operFlag:operFlag,bankNo:"<%=defaultBankNo%>"};
		try {
			sendAndToHost(actionUrl,param);
		} catch (e) {
			alert(e);
		}
	};
	
	var checkParam = {taskCode:clickedNodeId,taCode:"000000",bankNo:"<%=defaultBankNo%>"};
	showMessage(func,checkUrl,checkParam);
}

//详细事件
function onDetailClick(){
	var taskCode = document.getElementById(clickedNodeId).getAttribute("taskNode");
	var marketCode = document.getElementById(clickedNodeId).getAttribute("marketNode");
	var parentTaskCode = document.getElementById(clickedNodeId).getAttribute("parentTaskNode");
	var bankNo = document.getElementById(clickedNodeId).getAttribute("bankNo");  //分销售商节点可以查看详细信息
	
	var param={ExceuteMode:"X",taskCode:taskCode,marketCode:marketCode,parentTaskCode:parentTaskCode,bankNo:bankNo};
	try {
		sendAndCall(detailUrl,param);
	} catch (e) {
		alert(e);
	}
}

//下载事件
function onDownClick(){
	var taskCode = document.getElementById(clickedNodeId).getAttribute("taskNode");
	var marketCode = document.getElementById(clickedNodeId).getAttribute("marketNode");
	var bankNo = document.getElementById(clickedNodeId).getAttribute("bankNo");  //分销售商节点可以下载日志
	
	var param={ExceuteMode:"D",taskCode:taskCode,marketCode:marketCode,bankNo:bankNo};
	try {
		sendAndCall(downUrl,param);
	} catch (e) {
		alert(e);
	}
}

function onUrlClick(obj,marketCode,taskCode,url){
	try {
		var tabId = obj.id;
		var tabs = parentPage.tabs;
		var tab_length = tabs.items.length;
		if(tab_length>6){
			Ext.Msg.alert("提示","打开TAB页个数过多，请关闭不用的TAB页提高访问速度");
	    	return;
	    }
		if(contextmenu){
	    	contextmenu.hide();
	    }
	    if(msgWindow){
	    	msgWindow.hide();
	    }
	    tabId = tabId + 'log';
		var tab = tabs.getComponent(tabId);
		if(tab){
		    tabs.setActiveTab(tab);
		}else{
			parentPage.addTabForDetail(tabId,marketCode,taskCode,'<%=basePath%>' + url);
		}
	} catch (e) {
		alert(e);
	}
}

//added for saas
function onNodeListClick(obj,marketCode,taskCode){
	var tabId = obj.id;
	var tabs = parentPage.tabs;
	var tab_length = tabs.items.length;
	if(tab_length>6){
		Ext.Msg.alert("提示","打开TAB页个数过多，请关闭不用的TAB页提高访问速度");
    	return;
    }
	if(contextmenu){
    	contextmenu.hide();
    }
    if(msgWindow){
    	msgWindow.hide();
    }
    
	var tab = tabs.getComponent(tabId + '_' + marketCode);
	if(tab){
	    tabs.setActiveTab(tab);
	}else{
		parentPage.addTabForNodeList(tabId,marketCode,taskCode);
	}
}

//交易节点的单击
function leftClick(obj){
	//判断是否分银行,如果区分,则不允许继续
	var multiFlag = document.getElementById(obj.id).getAttribute("multiFlag");
	var bankNo = document.getElementById(obj.id).getAttribute("bankNo");
	if('1'==multiFlag){  //区分销售商节点,单击即进入列表
		var tabId = obj.id;
		var tabs = parentPage.tabs;
		var tab_length = tabs.items.length;
		if(tab_length>6){
			Ext.Msg.alert("提示","打开TAB页个数过多，请关闭不用的TAB页提高访问速度");
	    	return;
	    }
		if(contextmenu){
	    	contextmenu.hide();
	    }
	    if(msgWindow){
	    	msgWindow.hide();
	    }
		var tab = tabs.getComponent(tabId + '_' + '000000');
		if(tab){
		    tabs.setActiveTab(tab);
		}else{
			parentPage.addTabForNodeList(tabId,"000000",obj.id);
		}
		return;
	}
	
	var taCode = "000000";
	clickedNodeId = obj.id;
	
	//检查节点状态后的回调函数
	var func = function(operFlag){
		var param={nodeId:obj.id,taCode:taCode,ExceuteMode:"2",operFlag:operFlag,bankNo:"<%=defaultBankNo%>"};
		try {
			sendAndToHost(actionUrl,param);
		} catch (e) {
			alert(e);
		}
	};
	
	var checkParam = {taskCode:obj.id,taCode:"000000",bankNo:bankNo};
	showMessage(func,checkUrl,checkParam);
}

//TA节点的单击,如果该TA的tab页已经存在,则激活,否则添加一个tab页
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
		if(contextmenu){
	    	contextmenu.hide();
	    }
	    if(msgWindow){
	    	msgWindow.hide();
	    }
	    tabs.setActiveTab(tab);
	}else{
		if(contextmenu){
	    	contextmenu.hide();
	    }
	    if(msgWindow){
	    	msgWindow.hide();
	    }
		parentPage.addTabById1(tabId);
	}
}

//交易节点的右击事件
function rightClick(obj,event){
	//先取得这个节点的detailUrl
	var url = document.getElementById(obj.id).getAttribute("detailUrl");
	var marketCode = document.getElementById(obj.id).getAttribute("marketNode");
	var taskCode = document.getElementById(obj.id).getAttribute("taskNode");
	var multiFlag = document.getElementById(obj.id).getAttribute("multiFlag");
	url = url.replace(/[ ]/g,"");
	
	var cMenu = Ext.getCmp('contextMenu');
	
	if(null!=Ext.getCmp('urlMenu'))           //先移除
	{
		cMenu.remove(Ext.getCmp('urlMenu'));
	}
	if(null!=Ext.getCmp('bankListMenu'))      //先移除
	{
		cMenu.remove(Ext.getCmp('bankListMenu'));
	}

	var items = cMenu.items;
	
	if('1'==multiFlag){
		for(var i=0;i<items.length;i++){
			var itemId = items.get(i).getItemId();
			if(itemId=='detailMenu'){
				
			}else{
				items.get(i).hide();
			}
		}
		cMenu.insert(0,
			new Ext.menu.Item({
				id:'bankListMenu',
				text:'进入销售商清算列表',
				icon:'app/jrfw/dailystart/images/url_page.png',
				handler:function(){
					onNodeListClick(obj,marketCode,taskCode);
				}
			})
		);
	}
	else{
		for(var i=0;i<items.length;i++){
			items.get(i).show();
		}
		if(url.length>0){
			cMenu.addItem(
				new Ext.menu.Item({
					id:'urlMenu',
					text:'明细界面 ',
					icon:'app/jrfw/dailystart/images/url_page.png',
					handler:function(){
						onUrlClick(obj,marketCode,taskCode,url);
					}
				})
			);
		}
	}
	
	//var el = Ext.get(obj.id);
	//el.on("contextmenu",function(e){
	//	e.preventDefault();
	//	contextmenu.showAt(e.getXY());
	//});
	
	var position = [0,0];
	position = getMousePosition(event);
	contextmenu.showAt(position);

	if('0'==browserType){
		event.cancelBubble=true;
		event.returnValue=false;
	}else if('2'==browserType){
		event.stopPropagation();
		event.preventDefault();
	}else{
		event.stopPropagation();
		event.preventDefault();
	}
	
	clickedNodeId = obj.id;
}

//鼠标移动到某个交易节点,触发获取这个节点信息的事件
function showTip(obj){
	if(null==Ext.get('tip_' + obj.id)){
		new Ext.ToolTip({   
			id:'tip_' + obj.id,
			title:'--提示信息--',
		    target: obj.id,   
		    renderTo:'toolTip',
		    trackMouse:true,
		    width: 200,
		    html:'<div id="div_' + obj.id +'" style="height:50"></div>'
		});
	}
	var taskCode = document.getElementById(obj.id).getAttribute("taskNode");
	var marketCode = document.getElementById(obj.id).getAttribute("marketNode");
	var parentTaskCode = document.getElementById(obj.id).getAttribute("parentTaskNode");
	
	var param={ExceuteMode:"P",taskCode:taskCode,marketCode:marketCode,parentTaskCode:parentTaskCode};
	try {
		sendAndCall(tipUrl,param);
	} catch (e) {
		alert(e);
	}
}

//定时更新状态,时间间隔为5秒
function timingSendUpdate(){
	<%if(null!=ds && ds.findColumn("id")>0){%>
		if(canSend){
			canSend = false;
			var param={flowType:"0",ExceuteMode:"T"};
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
  <div id="dialog"></div>
  <div id="toolTip"></div>
  <div id="detail-win"></div>
</form>
</body>
<script language="JavaScript">
    setInterval(timingSendUpdate,5*1000);
</script>
</html>