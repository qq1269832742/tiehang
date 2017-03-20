<%--  
    @system  综合理财平台
    @version 4.0.0.2
    @lastModiDate 2014-11-11
    @describe   
    v3.0.0.2 20130225-01 liangshuang  增加tab页切换监听，切换时触发轮询状态事件  M201302150003	
    v3.0.0.3 20130820-01 niqq   点击“日志下载”时js 报对象为空或不是对象 M201308300003
    V3.0.0.4 20131212-01 yuanhb       分业务清算     M201312170010
    V4.0.0.1 20140912-01 yanjl  版本调整4.0.0.1
    v4.0.0.2 20141111-01 huangjl 修复未定义方法   M201411110010
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="com.levelappro.gbism.app.jrfw.action.DictCache"%>
<%@page import="java.util.List"%>
<%@page import="com.levelappro.gbism.app.jrfw.bean.Dict"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>自动清算</title>

<script type="text/javascript" src="script/ext-3.2.1/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="script/ext-3.2.1/ext-all.js"></script>
<link type="text/css" rel="stylesheet" href="script/ext-3.2.1/resources/css/ext-all.css" />


<script type="text/javascript">
var middlePosition = document.documentElement.clientWidth;
var pTabs = parent.contentPanel;      //框架的tabs
var framePageId = pTabs.getActiveTab().getId();
var currentActivePageId = '';        //页面第一次打开,注入一个PageId

var tabs = new Ext.TabPanel({ 
	id:'mainTab',
	activeTab:0,
	frame:true,
	height:800,
	defaults: {autoScroll:true},
	//v3.0.0.2 20130225-01 liangshuang  增加tab页切换监听，切换时触发轮询状态事件  beg
	listeners:{
		tabchange: function(tabPanel, newCard, oldCard){
			//v3.0.0.3 20130820-01 niqq   点击“日志下载”时js 报对象为空或不是对象 beg
			if(newCard.getEl().query('iframe')!='undefiend'&&newCard.getEl().query('iframe')!=''){
				var contentWnd=newCard.getEl().query('iframe')[0].contentWindow;
				if(typeof contentWnd.Ext!='undefined'){
					if(typeof contentWnd.timingSendUpdate == 'function'){
						contentWnd.timingSendUpdate();
					}
				}
			}
			//v3.0.0.3 20130820-01 niqq   点击“日志下载”时js 报对象为空或不是对象 end
		}
	},
	//v3.0.0.2 20130225-01 liangshuang  增加tab页切换监听，切换时触发轮询状态事件  end
	items:[{
		id:'mainFlow',
		title:'清算主流程',
		closable:false,
		html:'<iframe id="mainFrame" name="mainFrame" src="" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
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
    html:'<iframe id="miniWindow" name="miniWindow" src="app/jrfw/autoclear/MiniWindow.jsp" frameborder="0" height="100%" width="100%" style="overflow:hidden;"></iframe>'
});

miniWindow.on('minimize',function(){
	miniWindow.toggleCollapse();
});

function addTabById(tabId){
	tabs.add({
		id:tabId,
		title:'TA - ' + tabId,
		closable:true,
		html:'<iframe id="TAFrame' + tabId + '" name="TAFrame' + tabId + '" src="" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	
	showWaiting('TAFrame' + tabId);
	document.getElementById('TAFrame' + tabId).src = "<%=request.getContextPath()%>/ifmCQsglAutoClear.app?subTranCode=autoClearTAQuery&marketCode=" + tabId;
	currentActivePageId = tabId;
}

function addTabById1(tabId){
	var pType = tabId.substr(tabId.length-1);
	var name = "";
	<%List list = DictCache.getInstance().getDictList("K_CPLX");%>
	<%for(int m=0;m<list.size();m++){%>
	    <%Dict dict = (Dict)list.get(m);%>
        if(pType=='<%=dict.getVal()%>'){
        	name = '<%=dict.getPrompt()%>';
        }
	<%}%>
	tabs.add({
		id:tabId,
		title:'[' + name + ']业务清算界面',
		closable:true,
		html:'<iframe id="TAListPage' + tabId.substr(tabId.length-1) + '" name="TAListPage' + tabId.substr(tabId.length-1) + '" src="" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	showWaiting('TAListPage' + tabId.substr(tabId.length-1));
	document.getElementById('TAListPage' + tabId.substr(tabId.length-1)).src = "<%=request.getContextPath()%>/ifmCQsglAutoClear.app?subTranCode=autoClearTAList&PrdType=" + tabId;
	currentActivePageId = tabId;
}

//从分业务清算打开的TA列表界面
function addTabById2(tabId){
	var pType = tabId.substr(tabId.length-1);
	var name = "";
	<%List list1 = DictCache.getInstance().getDictList("K_CPLX");%>
	<%for(int m=0;m<list1.size();m++){%>
	    <%Dict dict = (Dict)list1.get(m);%>
        if(pType=='<%=dict.getVal()%>'){
        	name = '<%=dict.getPrompt()%>';
        }
	<%}%>
	tabs.add({
		id:tabId,
		title:'[' + name + ']业务TA列表界面',
		closable:true,
		html:'<iframe id="TAListPagePrdType' + tabId.substr(tabId.length-1) + '" name="TAListPagePrdType' + tabId.substr(tabId.length-1) + '" src="" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	showWaiting('TAListPagePrdType' + tabId.substr(tabId.length-1));
	document.getElementById('TAListPagePrdType' + tabId.substr(tabId.length-1)).src = "<%=request.getContextPath()%>/ifmCQsglAutoClear.app?subTranCode=autoClearTAList&PrdType=" + tabId + "&JustTaList=1";
	currentActivePageId = tabId;
}

function addTabById3(tabId){
	tabs.add({
		id:tabId,
		title:'清算TA列表界面',
		closable:true,
		html:'<iframe id="TAListPage" name="TAListPage" src="" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	
	showWaiting('TAListPage');
	document.getElementById('TAListPage').src = "<%=request.getContextPath()%>/ifmCQsglAutoClear.app?subTranCode=autoClearTAList";
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
		tabs.add({
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
		html:'<iframe id="BankListPage' + tabId + marketCode + '" name="BankListPage" src="app/jrfw/autoclear/BankNodeList.jsp" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	
	showWaiting('BankListPage' + tabId + marketCode);
	document.getElementById('BankListPage' + tabId + marketCode).src = "<%=request.getContextPath()%>/ifmCQsglAutoClear.app?subTranCode=autoClearBankList&marketCode=" + marketCode + "&taskCode=" + taskCode;
	currentActivePageId = tabId + '_' + marketCode;
}

function addTabForNodeList1(tabId,marketCode,taskCode){
	tabs.add({
		id:tabId + '_' + marketCode,
		title:'节点[' + taskCode + ']分销售商运行列表',
		closable:true,
		html:'<iframe id="BankListPage1' + tabId + marketCode + '" name="BankListPage1" src="app/jrfw/autoclear/BankNodeList.jsp" frameborder="0" height="100%" width="100%" style="overflow-x:hidden;overflow-y:scroll;"></iframe>'
	}).show();
	
	showWaiting('BankListPage1' + tabId + marketCode);
	document.getElementById('BankListPage1' + tabId + marketCode).src = "<%=request.getContextPath()%>/ifmCQsglAutoClear.app?subTranCode=autoClearBankList&marketCode=" + marketCode + "&taskCode=" + taskCode;
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
	document.getElementById('mainFrame').src = "<%=request.getContextPath()%>/ifmCQsglAutoClear.app?subTranCode=autoClearQuery";
	currentActivePageId = 'mainFlow';
	try {
		miniWindow.show();
	} catch (e) {
		alert('错误信息:自动清算页面未能完全加载!');
	}
});
</script>
</head>
<body>
	<div id="miniWindow"></div>
</body>
</html>