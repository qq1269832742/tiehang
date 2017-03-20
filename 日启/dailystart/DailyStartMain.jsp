<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>日启操作</title>

<script type="text/javascript" src="script/ext-3.2.1/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="script/ext-3.2.1/ext-all.js"></script>
<link type="text/css" rel="stylesheet" href="script/ext-3.2.1/resources/css/ext-all.css" />

<script type="text/javascript">
var middlePosition = document.documentElement.clientWidth;
    
var pTabs = parent.contentPanel;//框架的tabs
var framePageId = pTabs.getActiveTab().getId();
var currentActivePageId = '';        //页面第一次打开,注入一个PageId
	
var tabs = new Ext.TabPanel({ 
	id:'mainTab',
	activeTab:0,
	frame:true,
	height:800,
	defaults: {autoScroll:true},
	items:[{
		id:'mainFlow',
		title:'日启主流程',
		closable:false,
		html:'<iframe id="mainFrame" name="mainFrame" src="app/jrfw/dailystart/DailyStart.jsp" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}]
});

var miniWindow = new Ext.Window({
	el:'miniWindow',
    width:160,
    height:260,
    x: middlePosition - 250,
    y:35,
    title: '颜色-状态对照信息',
    minimizable:true,
    constrain: false,
    resizable:false,
    closable:true,
    closeAction:'hide',
    html:'<iframe id="miniWindow" name="miniWindow" src="app/jrfw/dailystart/DailyStartMiniWindow.jsp" frameborder="0" height="100%" width="100%" style="overflow:hidden;"></iframe>'
});

miniWindow.on('minimize',function(){
	miniWindow.toggleCollapse();
});

function addTabById(tabId){
	tabs.add({
		id:tabId,
		title:'TA - ' + tabId,
		closable:true,
		html:'<iframe id="TAFrame' + tabId + '" name="TAFrame' + tabId + '" src="DailyStartTA.jsp" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();

	showWaiting('TAFrame' + tabId);
	document.getElementById('TAFrame' + tabId).src = "<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartTAQuery&marketCode=" + tabId;
	currentActivePageId = tabId;
}

function addTabById1(tabId){
	tabs.add({
		id:tabId,
		title:'清算TA列表界面',
		closable:true,
		html:'<iframe id="TAListPage" name="TAListPage" src="app/jrfw/dailystart/DailyStartList.jsp" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	
	showWaiting('TAListPage');
	document.getElementById('TAListPage').src = "<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartTAList";
	currentActivePageId = tabId;
}

function addTabForLog(tabId,marketCode,taskCode,log){
	tabs.add({
		title:'Log-' + marketCode + "-" + taskCode,
		closable:true,
		id:tabId,
		html:'<pre>'+log+'</pre>'
	}).show();
}

function addTabForDetail(tabId,marketCode,taskCode,url){
	(function(){
		pTabs.add({
			title:marketCode + "-" + taskCode,
			closable:true,
			id:tabId,
			html:'<iframe id="detail' + marketCode + taskCode + '" name="detail' + marketCode + taskCode + '" src="' + url + '" frameborder="0" height="100%" width="100%" style="overflow:hidden;"></iframe>'
		}).show();
	}).defer(1);      //延迟打开新的TAB页面
}

function addTabForNodeList(tabId,marketCode,taskCode){
	tabs.add({
		id:tabId + '_' + marketCode,
		title:'节点[' + taskCode + ']分销售商运行列表',
		closable:true,
		html:'<iframe id="BankListPage' + tabId + marketCode + '" name="BankListPage" src="app/jrfw/dailystart/DailyStartBankNodeList.jsp" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	
	showWaiting('BankListPage' + tabId + marketCode);
	document.getElementById('BankListPage' + tabId + marketCode).src = "<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartBankList&marketCode=" + marketCode + "&taskCode=" + taskCode;
	currentActivePageId = tabId + '_' + marketCode;
}

function addTabForNodeList1(tabId,marketCode,taskCode){
	tabs.add({
		id:tabId + '_' + marketCode,
		title:'节点[' + taskCode + ']分销售商运行列表',
		closable:true,
		html:'<iframe id="BankListPage1' + tabId + marketCode + '" name="BankListPage1" src="app/jrfw/dailystart/DailyStartBankNodeList.jsp" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	
	showWaiting('BankListPage1' + tabId + marketCode);
	document.getElementById('BankListPage1' + tabId + marketCode).src = "<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartBankList&marketCode=" + marketCode + "&taskCode=" + taskCode;
	currentActivePageId = tabId + '_' + marketCode;
}

function showWaiting(frameId){
	var leftOffset = middlePosition/3;
    var maskHtml = "<html><head><meta http-equiv='npower' context='no-cache'></head>";
    maskHtml += "<body><div style='POSITION: absolute; HEIGHT: 60px;WIDTH:300px;LEFT:" + leftOffset + "px; TOP: 160px;'>";
    maskHtml += "<table width=100% border=0 cellspacing=0 cellpadding=0 ";
    maskHtml += "style='background-color: #CCCCFF; border-style: solid; border-color: #808080;HEIGHT: 60;font-size: 9pt'>";
    maskHtml += "<tr><td><div align=center>请稍候,页面载入中......</div></td></tr></table></div></body></html>";
    document.getElementById(frameId).contentWindow.document.write(maskHtml);
}

Ext.onReady(function(){
	var viewport = new Ext.Viewport({
        layout: 'fit',
        items: 
        [
            {items:tabs,layout:'fit'}  
        ]
    });

	showWaiting('mainFrame');
	document.getElementById('mainFrame').src = "<%=request.getContextPath()%>/ifmCQsglDailyStart.app?subTranCode=dailyStartQuery";
	currentActivePageId = 'mainFlow';
	try {
		miniWindow.show();
	} catch (e) {
		alert('错误信息:日启页面未能完全加载!');
	}
});
</script>
</head>
<body>
	<div id="miniWindow"></div>
</body>
</html>