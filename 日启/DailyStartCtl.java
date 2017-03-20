/********************************************
 * 文件名称: DailyStartHSAdapter.java
 * 系统名称: 综合理财管理平台V3.0
 * 模块名称:
 * 软件版权: 恒生电子股份有限公司
 * 功能说明: 
 * 系统版本: 3.0.0.1
 * 开发人员: yuanhb
 * 开发时间: 2010-7-15 下午03:17:26
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期    修改人员    修改说明
 *          20120213     yhb        日启界面优化
 * V4.0.0.1  20140912-01 yanjl  版本调整4.0.0.1
 * V4.0.0.2  20141028-01 huangjl 优化清算颜色显示问题   M201410280065
 * 		     20141029-02 huangjl 优化清算颜色显示问题   M201410280065
 *********************************************/

package com.levelappro.gbism.app.jrfw.action;

import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import com.levelappro.gbism.app.jrfw.bean.AnswerDataset;
import com.levelappro.gbism.app.jrfw.bean.DailyStartRequest;
import com.levelappro.gbism.app.jrfw.bean.IConstant;
import com.levelappro.gbism.app.jrfw.bean.IDict;
import com.levelappro.gbism.app.jrfw.bean.IErrMsg;
import com.levelappro.gbism.app.jrfw.bean.TaInfoCache;
import com.levelappro.gbism.app.jrfw.bean.TaskPool;
import com.levelappro.gbism.app.jrfw.channel.FileClient;
import com.levelappro.gbism.app.jrfw.dataset.DatasetColumnType;
import com.levelappro.gbism.app.jrfw.dataset.DatasetService;
import com.levelappro.gbism.app.jrfw.dataset.IDataset;
import com.levelappro.gbism.app.jrfw.db.DataLayer;
import com.levelappro.gbism.app.jrfw.db.HsData;
import com.levelappro.gbism.app.jrfw.db.HsDate;
import com.levelappro.gbism.app.jrfw.util.ServletUtil;
import com.levelappro.gbism.log.Log;
import com.levelappro.gbism.systemx.ext.util.impt.AppCtl;
import com.levelappro.gbism.app.jrfw.util.SendGapsService;
import com.levelappro.gbism.util.LpException;
import com.levelappro.gbism.util.ajax.AjaxTools;
import com.levelappro.gbism.util.xml.JdomXml;

public class DailyStartCtl extends AppCtl {
	
	//默认银行编号,放到缓存中
	private static String DEFAULT_BANK_NO =  "999";
	
	
	@Override
	public void doControl() throws LpException {

		if ("dailyStartQuery".equals(subTranCode)) {
			//query(context,transCode,subTransCode);
			query1(tranCode,subTranCode);
		}
		// 请求TA的流程图
		else if ("dailyStartTAQuery".equals(subTranCode)) {
			queryTa(tranCode,subTranCode);
		}
		// 接收管理台的动作
		else if ("dailyStartAction".equals(subTranCode)) {
			callRemoteService();
		}
		// 查询状态
		else if ("dailyStartStatus".equals(subTranCode)) {
			//monitorStatus();
			monitorStatus1();
		}
		//查询节点Tip
		else if("dailyStartTip".equals(subTranCode)){
			getTipsForNode();
		}
		//校验节点是否可以进行当前操作
		else if("dailyStartCheck".equals(subTranCode)){
			checkStatus();
		}
		//获得节点详细信息
		else if("dailyStartDetail".equals(subTranCode)){
			getDetailsForNode();
		}
		//下载服务端日志
		else if("dailyStartDown".equals(subTranCode)){
			downLog();
		}else if("dailyStartTAList".equals(subTranCode)){
			queryTaList(tranCode,subTranCode);
		}else if("dailyStartBankList".equals(subTranCode)){
			queryBankList(tranCode,subTranCode);
		}
		
	}
	


	
	/**
	 * 获取指定TA节点的显示状态
	 * TA下所有作业未激活则显示作业未激活
	 * TA下所有作业完成则显示作业完成
	 * 否则显示作业处理中
	 * @param taCode
	 * @return
	 * @throws Exception
	 */
	protected String getTaStatus(String taCode) throws Exception {
		String status = "";
		String lastStatus = "";
		
		DataLayer session = DataLayer.getDataLayer();
		IDataset ds = session.getDataSet(
				"select * from tbtaskpool where market_code=? and task_page = '0'", taCode);

		//map用于存放每个节点当前状态
		Map map = null;
		map = new HashMap(ds.getRowCount());
		
		ds.beforeFirst();
		for (int i = 0; i < ds.getRowCount(); i++) {
			ds.next();
			//modified by huangjl 20141028-01 优化清算颜色显示问题  beg
			/*status = ds.getString("deal_status");
			if (!"0".equals(status) && !"3".equals(ds.getString("deal_status")))  // 如果存在非"未激活",或者非"作业完成"状态,认为该TA正在处理中
			{
				status = "Z";
				break;
			} 
			else { // 如果是"0"或者"3"的状态
				if ("".equals(lastStatus))       // 第一次进入,为lastStatus赋值
				{ 
					lastStatus = status;
					continue;
				} 
				else {
					if (status.equals(lastStatus)) 
					{
						continue;
					} 
					else {         //如果存在两条记录的deal_status不一致,认为正在处理
						status = "Z";
						break;
					}
				}
			}*/
			if(map.size() <1 || !map.containsKey(ds.getString("task_code"))) {
				map.put(ds.getString("task_code"), ds.getString("deal_status"));
				status = ds.getString("deal_status");
			}else {
				if(map.get(ds.getString("task_code")).equals(ds.getString("deal_status"))) {
					continue;
				}else if(ds.getString("deal_status").equals("4")) {
					map.put(ds.getString("task_code"), "4");
					status = "4";
					continue;
				}else if(!ds.getString("task_code").equals("4")) {
					map.put(ds.getString("task_code"), "Z");
					status = "Z";
					continue;
				}
			}
			//modified by huangjl 20141028-01 优化清算颜色显示问题  end
		}
		return status;
	}
	
	/**
	 * 获取指定TA节点的显示状态
	 * TA下所有作业未激活则显示作业未激活
	 * TA下所有作业完成则显示作业完成
	 * 如果TA下有其中一个节点的状态为2-作业暂停,4-作业失败,5-作业中断,则显示TA节点状态为对应的状态
	 * 其他情况下,显示TA节点状态为Z-正在处理
	 * @param taCode
	 * @return
	 * @throws Exception
	 */
	protected String getTaStatus1(String taCode) throws LpException {
		String statusSql    = "select distinct deal_status from tbtaskpool where market_code=? and task_page = '0'";
		DataLayer session = DataLayer.getDataLayer();
		String taStatus = IDict.K_PCLZT.PCLZT_NO_DEAL;
		
		boolean existActiveNode = false;
		boolean existFinishedNode = false;
		
		//查询处理状态不是未激活、已完成状态的节点
		IDataset dataset = session.getDataSet(statusSql, taCode);
		if(dataset.getRowCount()<=0){
			throw new LpException(IErrMsg.ERR_DEFAULT,"查询TA[" + taCode + "]的当前状态失败");
		}
		if(dataset.getRowCount()==1 && dataset.getString("deal_status").equals(IDict.K_PCLZT.PCLZT_NO_DEAL)){
			return IDict.K_PCLZT.PCLZT_NO_DEAL;
		}
		if(dataset.getRowCount()==1 && dataset.getString("deal_status").equals(IDict.K_PCLZT.PCLZT_SUCC)){
			return IDict.K_PCLZT.PCLZT_SUCC;
		}
		if(dataset.getRowCount()>1){
			dataset.beforeFirst();
			while(dataset.hasNext()){
				dataset.next();
				String dealStatus = dataset.getString("deal_status");
				//modified by huangjl 20141029-01  优化清算节点颜色显示   beg
				/*if(dealStatus.equals(IDict.K_PCLZT.PCLZT_PAUSE)
						|| dealStatus.equals(IDict.K_PCLZT.PCLZT_FAIL)
						|| dealStatus.equals(IDict.K_PCLZT.PCLZT_BREAK)){
					return dealStatus;
				}*/
				if(dealStatus.equals(IDict.K_PCLZT.PCLZT_FAIL)) {
					return dealStatus;
				}
				/*if(dealStatus.equals(IDict.K_PCLZT.PCLZT_ACTIVE)){
					existActiveNode = true;
				}
				if(dealStatus.equals(IDict.K_PCLZT.PCLZT_SUCC)){
					existFinishedNode = true;
				}*/
				//modified by huangjl 20141029-01  优化清算节点颜色显示   end
			}
			if(existActiveNode && (!existFinishedNode)){
				return IDict.K_PCLZT.PCLZT_ACTIVE;
			}
			return IDict.K_PCLZT.PCLZT_DOING;
		}
		return taStatus;
	}
	
	/**
	 * 获得每个TA的当前清算状态,返回map:key-TA代码, value-当前状态
	 * @return
	 * @throws Exception
	 */
	protected Map getTaStatus() throws LpException {
		Map statusMap = new HashMap();
		List list = DailyStartCache.getInstance().getMarketCodeList();
		for(int i=0;i<list.size();i++){
			String taCode = (String)list.get(i);
			String dealStatus = getTaStatus1(taCode);
			statusMap.put(taCode, dealStatus);
		}
		
		return statusMap;
	}
	
	/**
	 * 获得主流程界面上TA主节点的状态
	 * 1.如果所有TA下的节点的状态都是未激活,则显示为未激活
	 * 2.如果所有TA下的节点的状态都是已完成,则显示为已完成
	 * 3.如果所有TA下的节点,有其中一个节点的状态为2-作业暂停,4-作业失败,5-作业中断,则显示主TA节点状态为作业中断
	 * 4.其他情况下,显示主节点状态为Z-正在处理
	 * @return
	 * @throws Exception
	 */
	protected String getMainTaStatus() throws LpException {
		String notActiveSql = "select * from tbtaskpool where market_code<>? and deal_status<>? and task_page = '0'";
		String finishedSql  = "select * from tbtaskpool where market_code<>? and deal_status<>? and task_page = '0'";
		String processSql   = "select * from tbtaskpool where market_code<>? and deal_status in (?,?,?) and task_page = '0'";
		String breakSql  = "select * from tbtaskpool where market_code<>? and deal_status=? and task_page = '0'";
		
		String mainTaStatus = IDict.K_PCLZT.PCLZT_NO_DEAL;
		int count = 0;
		
		DataLayer session = DataLayer.getDataLayer();
		//IDBSession session = DBSessionFactory.getSession();
		
		count = session.account(notActiveSql, IConstant.DEFAULT_CODE,IDict.K_PCLZT.PCLZT_NO_DEAL);
		if(count<=0){
			return mainTaStatus;
		}
		
		count = session.account(finishedSql, IConstant.DEFAULT_CODE,IDict.K_PCLZT.PCLZT_SUCC);
		if(count<=0){
			mainTaStatus = IDict.K_PCLZT.PCLZT_SUCC;
			return mainTaStatus;
		}
		
		//modified by huangjl 20141029-01 优化清算节点颜色   beg
		/*count = session.account(processSql, IConstant.DEFAULT_CODE,IDict.K_PCLZT.PCLZT_PAUSE,
				IDict.K_PCLZT.PCLZT_FAIL,IDict.K_PCLZT.PCLZT_BREAK);
		if(count>0){
			mainTaStatus = IDict.K_PCLZT.PCLZT_BREAK;
			return mainTaStatus;
		}*/
		count = session.account(breakSql, IConstant.DEFAULT_CODE,IDict.K_PCLZT.PCLZT_FAIL);
		if(count>0){
			mainTaStatus = IDict.K_PCLZT.PCLZT_FAIL;
			return mainTaStatus;
		}
		//modified by huangjl 20141029-01 优化清算节点颜色   end
		
		mainTaStatus = IDict.K_PCLZT.PCLZT_DOING;
		return mainTaStatus;
	}

	/**
	 * 获得父节点是TA清算中的节点、且自身的market_code为"000000"的节点状态
	 * 只有每条配置的状态都一样,才认为这个节点的状态是deal_status,否则认为是处理中
	 * @param marketCode
	 * @param taskCode
	 * @return
	 * @throws Exception
	 */
	private String getNodeStatus(String marketCode, String taskCode)
			throws LpException {
		String status = "";
		String lastStatus = "";
		DataLayer session = DataLayer.getDataLayer();
		//IDBSession session = DBSessionFactory.getSession();
		IDataset ds = session.getDataSet(
				"select * from tbtaskpool where market_code=? and task_code=? and task_page='0'",
				marketCode, taskCode);

		ds.beforeFirst();
		for (int i = 0; i < ds.getRowCount(); i++) {
			ds.next();
			if ("".equals(lastStatus)) {
				status = lastStatus = ds.getString("deal_status");
				continue;
			} else {
				if (ds.getString("deal_status").equals(lastStatus)) {
					continue;
				} else {
					status = "Z";
					i = ds.getRowCount();
				}
			}
		}
		return status;
	}
	
	/**
	 * 根据两种状态,判断节点应该处于哪种状态,对一个节点多个配置使用
	 * @param status1
	 * @param status2
	 * @return
	 */
	private String getStatus(String status1, String status2){
		String status = IDict.K_PCLZT.PCLZT_DOING;
		if(status1.equals(status2)){
			status = status1;
		}else{
			if(status1.equals(IDict.K_PCLZT.PCLZT_FAIL) || 
					status2.equals(IDict.K_PCLZT.PCLZT_FAIL))
			{
				status = IDict.K_PCLZT.PCLZT_FAIL;
			}
			//modified by huangjl 20141028-01 优化清算颜色显示问题  beg
			/*if(status1.equals(IDict.K_PCLZT.PCLZT_BREAK) || 
					status2.equals(IDict.K_PCLZT.PCLZT_BREAK))
			{
				status = IDict.K_PCLZT.PCLZT_BREAK;
			}*/
			else {
				status = IDict.K_PCLZT.PCLZT_DOING;
			}
			//modified by huangjl 20141028-01 优化清算颜色显示问题  end
		}
		return status;
	}
	
	/**
	 * 向Dataset中添加新的字段
	 * @param answerDataset
	 * @param answerDsRowIndex
	 * @param ds
	 */
	private void setMoreBackInformation(AnswerDataset answerDataset,int answerDsRowIndex,IDataset ds) throws LpException{
		System.out.println(ds.getString("market_code"));
		answerDataset.setValue(answerDsRowIndex, "marketCode",
				ds.getString("market_code"));
		answerDataset.setValue(answerDsRowIndex, "parentTaskCode",
				DailyStartCache.getInstance().getParentCodeString(
						ds.getString("market_code"), 
						ds.getString("task_code")));
		answerDataset.setValue(answerDsRowIndex, "taskType",
				ds.getString("task_type"));
		answerDataset.setValue(answerDsRowIndex, "redoFlag",
				ds.getString("redo_flag"));
		answerDataset.setValue(answerDsRowIndex, "delayFlag",
				ds.getString("delay_flag"));
		answerDataset.setValue(answerDsRowIndex, "delayTime",
				ds.getString("delay_time"));
		answerDataset.setValue(answerDsRowIndex, "exePercnet",
				ds.getString("excute_percentage"));
		answerDataset.setValue(answerDsRowIndex, "errorCode",
				ds.getString("err_code"));
		answerDataset.setValue(answerDsRowIndex, "errorMsg",
				ds.getString("err_msg"));
		
		String warnMsg = ds.getString("warn_msg");
		if(warnMsg.trim().length()>0){
			String[] array = {"[","]","{","}"};
			warnMsg = regularReplace(warnMsg, array, "");
			warnMsg = regularReplace(warnMsg, "\n", "<br>");
		}
		
		answerDataset.setValue(answerDsRowIndex, "warnMsg",
				"".equals(warnMsg.trim()) ? "" : warnMsg);
		answerDataset.setValue(answerDsRowIndex, "beginTime",
				HsDate.timeTo8(ds.getString("begin_time")));
		answerDataset.setValue(answerDsRowIndex, "endTime",
				HsDate.timeTo8(ds.getString("end_time")));
		answerDataset.setValue(answerDsRowIndex, "lastTransDate",
				HsDate.dateTo10(ds.getString("last_trans_date")));
		answerDataset.setValue(answerDsRowIndex, "holdTime", 
				ds.getString("hold_time"));
		answerDataset.setValue(answerDsRowIndex, "detailUrl",
				ds.getString("detail_url"));
	}
	
	/**
	 * 复制一个Dataset中内容到另一个Dataset
	 * @param sourceDataset
	 * @param targetDataset
	 * @param resultDsRowIndex
	 */
	private void moveDataset(IDataset sourceDataset,AnswerDataset targetDataset,int resultDsRowIndex) {
		targetDataset.setValue(resultDsRowIndex, "marketCode", sourceDataset
				.getString("marketCode"));
		targetDataset.setValue(resultDsRowIndex, "parentTaskCode", sourceDataset
				.getString("parentTaskCode"));
		targetDataset.setValue(resultDsRowIndex, "taskType", sourceDataset
				.getString("taskType"));
		targetDataset.setValue(resultDsRowIndex, "redoFlag", sourceDataset
				.getString("redoFlag"));
		targetDataset.setValue(resultDsRowIndex, "delayFlag", sourceDataset
				.getString("delayFlag"));
		targetDataset.setValue(resultDsRowIndex, "delayTime", sourceDataset
				.getString("delayTime"));
		targetDataset.setValue(resultDsRowIndex, "exePercnet", sourceDataset
				.getString("exePercnet"));
		targetDataset.setValue(resultDsRowIndex, "errorCode", sourceDataset
				.getString("errorCode"));
		targetDataset.setValue(resultDsRowIndex, "errorMsg", sourceDataset
				.getString("errorMsg"));
		targetDataset.setValue(resultDsRowIndex, "warnMsg", sourceDataset
				.getString("warnMsg"));
		targetDataset.setValue(resultDsRowIndex, "beginTime", sourceDataset
				.getString("beginTime"));
		targetDataset.setValue(resultDsRowIndex, "endTime", sourceDataset
				.getString("endTime"));
		targetDataset.setValue(resultDsRowIndex, "holdTime", sourceDataset
				.getString("holdTime"));
		targetDataset.setValue(resultDsRowIndex, "lastTransDate", sourceDataset
				.getString("lastTransDate"));
		targetDataset.setValue(resultDsRowIndex, "detailUrl", sourceDataset
				.getString("detailUrl"));
	}

	/**
	 * 查询日启主流程界面
	 * @param context
	 * @throws Exception
	 */
	protected void query(String transCode,String subTransCode) throws Exception {
		DataLayer session = DataLayer.getDataLayer();
		AnswerDataset answerDataset = new AnswerDataset();
		int answerDsRowIndex = 1;
		
		//判断配置表中是否有TA,如果配置了TA信息,则将每个TA作为一个虚拟节点压入应答包
		if(null!=DailyStartCache.getInstance().getMarketCodeList() &&
				DailyStartCache.getInstance().getMarketCodeList().size()>0)
		{
			List marketCodeList = DailyStartCache.getInstance().getMarketCodeList();
			List taskCodeList   = DailyStartCache.getInstance().getTaskCodeList();
			
			// 将每个TA作为一个节点压入应答包,每个TA中有很多子节点,需要找到第一个子节点的父节点,作为这个大的TA节点的父节点
			for (int i = 0; i < marketCodeList.size(); i++) 
			{
				String marketCode = (String) marketCodeList.get(i);
				List taskPoolList = DailyStartCache.getInstance().getTaskPoolListByMarketCode(marketCode);
				
				//找到taskPoolList中父作业代码位于taskCodeList中的记录
				for (int j = 0; j < taskPoolList.size(); j++) 
				{
					TaskPool taskPool = (TaskPool)taskPoolList.get(j);
					if(taskCodeList.contains(taskPool.getParentTaskCode()))
					{
						answerDataset.setValue(answerDsRowIndex, "id",
								marketCode);
						answerDataset.setValue(answerDsRowIndex, "pid",
								taskPool.getParentTaskCode());
						answerDataset.setValue(answerDsRowIndex, "describe", //从内存中根据marketCode获得TA名称
								TaInfoCache.getInstance().getTaName(marketCode));
						answerDataset.setValue(answerDsRowIndex, "status",
								getTaStatus(marketCode));
						answerDataset.setValue(answerDsRowIndex, "flag",     //用于设别节点类型:0-普通节点,1-交易节点,2-TA节点
 								"2");
						answerDsRowIndex++;
						break;
					}
				}
			}
		}
		
		// 将清算配置表中不是TA清算中的交易节点压入应答包
		IDataset ds = session.getDataSet(
				"select * from tbtaskpool where market_code=? and task_page='0'", IConstant.DEFAULT_CODE);  
		ds.beforeFirst();
		for (int i = 0; i < ds.getRowCount(); i++) 
		{
			ds.next();
			String taskCode = ds.getString("task_code");
			String parentTaskCode = ds.getString("parent_task_code");

			if (null == parentTaskCode || "".equals(parentTaskCode.trim()))  // 如果parent_task_code没配置,则是第一个节点,将其父节点设置为"0",即开始节点
			{
				answerDataset.setValue(answerDsRowIndex, "id", taskCode);
				answerDataset.setValue(answerDsRowIndex, "pid", "0");
				answerDataset.setValue(answerDsRowIndex, "describe", 
						ds.getString("task_name")); 
				answerDataset.setValue(answerDsRowIndex, "status", 
						ds.getString("deal_status"));
				answerDataset.setValue(answerDsRowIndex, "flag", "1");
				//压入更多信息,数据字典需要翻译
				setMoreBackInformation(answerDataset, answerDsRowIndex, ds);
				answerDsRowIndex++;
			} 
			else {
				if (!DailyStartCache.getInstance().checkIsParentInMarket(parentTaskCode))    // 如果父节点不是TA清算中的交易节点,直接压入应答包
				{ 
					answerDataset.setValue(answerDsRowIndex, "id", taskCode);
					answerDataset.setValue(answerDsRowIndex, "pid",parentTaskCode);
					answerDataset.setValue(answerDsRowIndex, "describe",
							ds.getString("task_name")); 
					answerDataset.setValue(answerDsRowIndex, "status", 
							ds.getString("deal_status"));
					answerDataset.setValue(answerDsRowIndex, "flag", "1");
					setMoreBackInformation(answerDataset, answerDsRowIndex, ds);
					answerDsRowIndex++;
				} 
				else {                  // 如果父节点是TA清算中的节点,根据父节点,找到该节点所属的TA列表
					List list = null;
					list = DailyStartCache.getInstance().getMarketCodeListByParentTaskCode(parentTaskCode);
					String status = "";
					status = getNodeStatus(IConstant.DEFAULT_CODE, taskCode);

					for (int j = 0; j < list.size(); j++) {
						answerDataset.setValue(answerDsRowIndex, "id", taskCode);
						answerDataset.setValue(answerDsRowIndex, "pid",(String) list.get(j));
						answerDataset.setValue(answerDsRowIndex, "describe",
								ds.getString("task_name")); 
						answerDataset.setValue(answerDsRowIndex, "status",status);
						answerDataset.setValue(answerDsRowIndex, "flag", "1");
						setMoreBackInformation(answerDataset, answerDsRowIndex, ds);
						answerDsRowIndex++;
					}
				}
			}
		}
		
		// 对压好的应答包进行排序
		IDataset sourceDs = answerDataset.getAnswerDataset();
		sourceDs.beforeFirst();
		
		for(int i=0;i<sourceDs.getRowCount();i++){  // 处理某条记录的父作业码在表中不存在的情况
			sourceDs.next();
			String pTaskCode = sourceDs.getString("parentTaskCode");
			
			if(!"".equals(pTaskCode.trim()) &&                  // 如果不存在修改其pid为0
					!DailyStartCache.getInstance().isParentTaskCodeInTable(pTaskCode))
			{
				sourceDs.updateString("pid", "0");
			}
		}
		
		AnswerDataset resultDs = new AnswerDataset();
		int resultDsRowIndex = 1;
		List allDealedNodes = new ArrayList(sourceDs.getRowCount());      //记录排序算法中已经处理过的节点
		int allDealedNodeCount = 0; 
		
		//先完成一次操作
		do {
			allDealedNodeCount = allDealedNodes.size();                 //记录本次while循环开始前的已处理列表长度,便于下一次循环条件中的比较
			sourceDs.beforeFirst();
			for (int i = 0; i < sourceDs.getRowCount(); i++) {
				sourceDs.next();
				// 如果已经处理过该节点
				if (allDealedNodes.contains(sourceDs.getString("id"))) {
					// 因为已经处理过该节点,所以resultDs中已经压入过一次,在sourceDs中再遇到该节点,需要比较两条记录的pid是否相同
					String taskNode = sourceDs.getString("id");
					String parentNode = sourceDs.getString("pid");
					IDataset checkDs = resultDs.getAnswerDataset();
					boolean checkFlag = true;

					checkDs.beforeFirst();
					for (int j = 0; j < checkDs.getRowCount(); j++) 
					{
						checkDs.next();
						if (taskNode.equals(checkDs.getString("id"))
								&& parentNode.equals(checkDs.getString("pid"))) 
						{
							checkFlag = false;
							break;
						}
					}
					
					// 如果虽然已压入该节点，但是父节点不同
					if (checkFlag) 
					{
						if(!allDealedNodes.contains(parentNode))     //如果父节点还没有被处理过,则跳出继续下一次循环
						{
							continue;
						}
						resultDs.setValue(resultDsRowIndex, "id", sourceDs
								.getString("id"));
						resultDs.setValue(resultDsRowIndex, "pid", sourceDs
								.getString("pid"));
						resultDs.setValue(resultDsRowIndex, "describe",
								sourceDs.getString("describe"));
						resultDs.setValue(resultDsRowIndex, "status", sourceDs
								.getString("status"));
						resultDs.setValue(resultDsRowIndex, "flag", sourceDs
								.getString("flag"));
						
						if("1".equals(sourceDs.getString("flag"))){
							moveDataset(sourceDs, resultDs, resultDsRowIndex);
						}
						resultDsRowIndex++;

						allDealedNodes.add(sourceDs.getString("id"));
					}
				} 
				else {
					if ("0".equals(sourceDs.getString("pid"))) 
					{
						resultDs.setValue(resultDsRowIndex, "id", 
								sourceDs.getString("id"));
						resultDs.setValue(resultDsRowIndex, "pid",
								sourceDs.getString("pid"));
						resultDs.setValue(resultDsRowIndex, "describe",
								sourceDs.getString("describe"));
						resultDs.setValue(resultDsRowIndex, "status", 
								sourceDs.getString("status"));
						resultDs.setValue(resultDsRowIndex, "flag", 
								sourceDs.getString("flag"));
						
						if("1".equals(sourceDs.getString("flag")))
						{
							moveDataset(sourceDs, resultDs, resultDsRowIndex);
						}
						resultDsRowIndex++;

						allDealedNodes.add(sourceDs.getString("id"));
						continue;
					} 
					else {
						if (allDealedNodes.contains(sourceDs.getString("pid"))) 
						{
							resultDs.setValue(resultDsRowIndex, "id", 
									sourceDs.getString("id"));
							resultDs.setValue(resultDsRowIndex, "pid", 
									sourceDs.getString("pid"));
							resultDs.setValue(resultDsRowIndex, "describe",
									sourceDs.getString("describe"));
							resultDs.setValue(resultDsRowIndex, "status",
									sourceDs.getString("status"));
							resultDs.setValue(resultDsRowIndex, "flag", 
									sourceDs.getString("flag"));
							
							if("1".equals(sourceDs.getString("flag")))
							{
								moveDataset(sourceDs, resultDs, resultDsRowIndex);
							}
							resultDsRowIndex++;

							allDealedNodes.add(sourceDs.getString("id"));
							continue;
						} 
						else {
							continue;
						}
					}

				}
			}
		} while (allDealedNodes.size() < sourceDs.getRowCount()
					&& sourceDs.findColumn("id")>0                  //AnswerDataset中默认会有一行,因此不能用getRowCount来判断是否含有记录
					&& allDealedNodeCount<allDealedNodes.size());   //判断上一次记录的已处理列表长度,和现在的已处理列表长度,
																	//如果相等,表示刚刚的那次循环没有做任何事,表示进入死循环,
																	//如果小于当前的已处理列表,表示刚刚的循环有处理,则可以继续进行
		
		//HttpServletRequest request = context.getEventAttribute("$_REQUEST");
		request.setAttribute("dataset", resultDs.getAnswerDataset());
		//this.actionForward(context, transCode,subTransCode);
	}
	
	protected void query1(String transCode,String subTransCode) throws LpException {
		DataLayer session = DataLayer.getDataLayer();
		AnswerDataset answerDataset = new AnswerDataset();
		int answerDsRowIndex = 1;
		
		//判断配置表中是否有TA
		if(null!=DailyStartCache.getInstance().getMarketCodeList()
				&& DailyStartCache.getInstance().getMarketCodeList().size()>0)
		{
			//所有TA节点编号List
			List marketCodeList = DailyStartCache.getInstance().getMarketCodeList();
			//所有公共节点List
			List taskCodeList   = DailyStartCache.getInstance().getTaskCodeList();
			
			String marketCode = (String) marketCodeList.get(0);  //只要判断一个TA节点的父节点,即可
			List taskPoolList = DailyStartCache.getInstance().getTaskPoolListByMarketCode(marketCode);
			
			for (int j = 0; j < taskPoolList.size(); j++) 
			{
				TaskPool taskPool = (TaskPool)taskPoolList.get(j);
				if(taskCodeList.contains(taskPool.getParentTaskCode()))
				{
					answerDataset.setValue(answerDsRowIndex, "id",
							"TANODE");
					answerDataset.setValue(answerDsRowIndex, "pid",
							taskPool.getParentTaskCode());
					answerDataset.setValue(answerDsRowIndex, "describe",             //从内存中根据marketCode获得TA名称
							"TA清算综合节点");
					answerDataset.setValue(answerDsRowIndex, "status",
							getMainTaStatus());
					answerDataset.setValue(answerDsRowIndex, "flag",     //用于设别节点类型:0-普通节点,1-交易节点,2-TA节点
							"2");
					answerDsRowIndex++;
					break;
				}
			}
		}
		
		// 将清算配置表中不是TA清算中的交易节点压入应答包
		IDataset ds = session.getDataSet(
				"select * from tbtaskpool where market_code=? and task_page='0'", 
				IConstant.DEFAULT_CODE);  
		ds.beforeFirst();
		for (int i = 0; i < ds.getRowCount(); i++) 
		{
			ds.next();
			String taskCode = ds.getString("task_code");
			String parentTaskCode = ds.getString("parent_task_code");

			// 如果parent_task_code没配置,则是第一个节点,将其父节点设置为"0",即开始节点
			if (null == parentTaskCode || "".equals(parentTaskCode.trim())) 
			{
				answerDataset.setValue(answerDsRowIndex, "id", taskCode);
				answerDataset.setValue(answerDsRowIndex, "pid", "0");
				answerDataset.setValue(answerDsRowIndex, "describe",
						ds.getString("task_name")); 
				answerDataset.setValue(answerDsRowIndex, "status", 
						ds.getString("deal_status"));
				answerDataset.setValue(answerDsRowIndex, "flag", "1");
				answerDataset.setValue(answerDsRowIndex, "bankNo", DEFAULT_BANK_NO);
				answerDataset.setValue(answerDsRowIndex, "multiFlag", 
						DailyStartCache.getInstance().getMultiFlag(IConstant.DEFAULT_CODE, taskCode, DEFAULT_BANK_NO) ? "1" : "0");
				//压入更多信息,数据字典需要翻译
				setMoreBackInformation(answerDataset, answerDsRowIndex, ds);
				answerDsRowIndex++;
			} 
			else {
				if (!DailyStartCache.getInstance().checkIsParentInMarket(parentTaskCode))  // 如果父节点不是TA清算中的交易节点,直接压入应答包
				{ 
					answerDataset.setValue(answerDsRowIndex, "id", taskCode);
					answerDataset.setValue(answerDsRowIndex, "pid",parentTaskCode);
					answerDataset.setValue(answerDsRowIndex, "describe",
							ds.getString("task_name")); 
					answerDataset.setValue(answerDsRowIndex, "status", 
							ds.getString("deal_status"));
					answerDataset.setValue(answerDsRowIndex, "flag", "1");
					answerDataset.setValue(answerDsRowIndex, "bankNo", DEFAULT_BANK_NO);
					answerDataset.setValue(answerDsRowIndex, "multiFlag", 
							DailyStartCache.getInstance().getMultiFlag(IConstant.DEFAULT_CODE, taskCode, DEFAULT_BANK_NO) ? "1" : "0");
					setMoreBackInformation(answerDataset, answerDsRowIndex, ds);
					answerDsRowIndex++;
				} 
				else {               // 如果父节点是TA清算中的节点,主界面上目前只有一个TA节点,因此只需要处理一条记录即可
					String status = "";
					status = getNodeStatus(IConstant.DEFAULT_CODE, taskCode);

					answerDataset.setValue(answerDsRowIndex, "id", taskCode);
					answerDataset.setValue(answerDsRowIndex, "pid", "TANODE");
					answerDataset.setValue(answerDsRowIndex, "describe",
							ds.getString("task_name")); 
					answerDataset.setValue(answerDsRowIndex, "status",status);
					answerDataset.setValue(answerDsRowIndex, "flag", "1");
					answerDataset.setValue(answerDsRowIndex, "bankNo", DEFAULT_BANK_NO);
					answerDataset.setValue(answerDsRowIndex, "multiFlag", 
							DailyStartCache.getInstance().getMultiFlag(IConstant.DEFAULT_CODE, taskCode, DEFAULT_BANK_NO) ? "1" : "0");
					setMoreBackInformation(answerDataset, answerDsRowIndex, ds);
					answerDsRowIndex++;
				}
			}
		}
		
		// 对压好的应答包进行排序
		IDataset sourceDs = answerDataset.getAnswerDataset();
		// 处理某条记录的父作业码在表中不存在的情况
		sourceDs.beforeFirst();
		for(int i=0;i<sourceDs.getRowCount();i++)     // 处理某条记录的父作业码在表中不存在的情况
		{  
			sourceDs.next();
			String pTaskCode = sourceDs.getString("parentTaskCode");
			
			if(!"".equals(pTaskCode.trim()) &&                  // 如果不存在修改其pid为0
					pTaskCode.indexOf("|")<0 &&
					!DailyStartCache.getInstance().isParentTaskCodeInTable(pTaskCode))
			{
				sourceDs.updateString("pid", "0");
			}
			
			if(!"".equals(pTaskCode.trim()) &&        //对父节点配置为两条以上的情况:xxxxxx|xxxxxx     
					pTaskCode.indexOf("|")>0){
				String[] arr = pTaskCode.split("\\|");
				for(int j=0;j<arr.length;j++){
					if(!DailyStartCache.getInstance().isParentTaskCodeInTable(arr[j])){
						throw new LpException("交易[" + arr[j] + "]在taskpool表中没有配置！");
					}
				}
			}
		}
		
		AnswerDataset resultDs = new AnswerDataset();
		int resultDsRowIndex = 1;
		List allDealedNodes = new ArrayList(sourceDs.getRowCount());
		int allDealedNodeCount = 0; 
		
		do {
			allDealedNodeCount = allDealedNodes.size();
			sourceDs.beforeFirst();
			for (int i = 0; i < sourceDs.getRowCount(); i++) 
			{
				sourceDs.next();
				// 如果已经处理过该节点
				if (allDealedNodes.contains(sourceDs.getString("id"))) {
					// 因为已经处理过该节点,所有resultDs中已经压入过一次,在sourceDs中再遇到该节点,需要比较两条记录的pid是否相同
					String taskNode = sourceDs.getString("id");
					String parentNode = sourceDs.getString("pid");
					IDataset checkDs = resultDs.getAnswerDataset();
					boolean checkFlag = true;

					checkDs.beforeFirst();
					for (int j = 0; j < checkDs.getRowCount(); j++) 
					{
						checkDs.next();
						if (taskNode.equals(checkDs.getString("id"))
								&& parentNode.equals(checkDs.getString("pid"))) 
						{
							checkFlag = false;
							break;
						}
					}
					
					if (checkFlag) // 如果虽然已压入该节点，但是父节点不同
					{
						if(!allDealedNodes.contains(parentNode))
						{
							continue;
						}
						resultDs.setValue(resultDsRowIndex, "id", sourceDs
								.getString("id"));
						resultDs.setValue(resultDsRowIndex, "pid", sourceDs
								.getString("pid"));
						resultDs.setValue(resultDsRowIndex, "describe",sourceDs
								.getString("describe"));
						resultDs.setValue(resultDsRowIndex, "status", sourceDs
								.getString("status"));
						resultDs.setValue(resultDsRowIndex, "flag", sourceDs
								.getString("flag"));
						resultDs.setValue(resultDsRowIndex, "bankNo", sourceDs
								.getString("bankNo"));
						resultDs.setValue(resultDsRowIndex, "multiFlag", sourceDs
								.getString("multiFlag"));
						
						if("1".equals(sourceDs.getString("flag")))
						{
							moveDataset(sourceDs, resultDs, resultDsRowIndex);
						}
						resultDsRowIndex++;

						allDealedNodes.add(sourceDs.getString("id"));
					}
				} 
				else {
					if ("0".equals(sourceDs.getString("pid"))) 
					{
						resultDs.setValue(resultDsRowIndex, "id", sourceDs
								.getString("id"));
						resultDs.setValue(resultDsRowIndex, "pid", sourceDs
								.getString("pid"));
						resultDs.setValue(resultDsRowIndex, "describe",sourceDs
								.getString("describe"));
						resultDs.setValue(resultDsRowIndex, "status", sourceDs
								.getString("status"));
						resultDs.setValue(resultDsRowIndex, "flag", sourceDs
								.getString("flag"));
						resultDs.setValue(resultDsRowIndex, "bankNo", sourceDs
								.getString("bankNo"));
						resultDs.setValue(resultDsRowIndex, "multiFlag", sourceDs
								.getString("multiFlag"));
						
						if("1".equals(sourceDs.getString("flag")))
						{
							moveDataset(sourceDs, resultDs, resultDsRowIndex);
						}
						resultDsRowIndex++;

						allDealedNodes.add(sourceDs.getString("id"));
						continue;
					} 
					else {
						if (allDealedNodes.contains(sourceDs.getString("pid"))) 
						{
							resultDs.setValue(resultDsRowIndex, "id", sourceDs
									.getString("id"));
							resultDs.setValue(resultDsRowIndex, "pid", sourceDs
									.getString("pid"));
							resultDs.setValue(resultDsRowIndex, "describe", sourceDs
									.getString("describe"));
							resultDs.setValue(resultDsRowIndex, "status", sourceDs
									.getString("status"));
							resultDs.setValue(resultDsRowIndex, "flag", sourceDs
									.getString("flag"));
							resultDs.setValue(resultDsRowIndex, "bankNo", sourceDs
									.getString("bankNo"));
							resultDs.setValue(resultDsRowIndex, "multiFlag", sourceDs
									.getString("multiFlag"));
							
							if("1".equals(sourceDs.getString("flag")))
							{
								moveDataset(sourceDs, resultDs, resultDsRowIndex);
							}
							resultDsRowIndex++;

							allDealedNodes.add(sourceDs.getString("id"));
							continue;
						} else {
							continue;
						}
					}

				}
			}
		} while (allDealedNodes.size()<sourceDs.getRowCount()
					&& sourceDs.findColumn("id")>0
					&& allDealedNodeCount<allDealedNodes.size());

		//HttpServletRequest request = context.getEventAttribute("$_REQUEST");
		request.setAttribute("dataset", resultDs.getAnswerDataset());
		
		//this.actionForward(context,transCode,subTransCode);
	}
	
	/**
	 * 查询TA节点列表
	 * @param context
	 * @throws Exception
	 */
	protected void queryTaList(String transCode,String subTransCode) throws LpException {
		DataLayer session = DataLayer.getDataLayer();

		AnswerDataset answerDataset = new AnswerDataset();
		int answerDsRowIndex = 1;
		
		//判断配置表中是否有TA
		if(null!=DailyStartCache.getInstance().getMarketCodeList()
				&& DailyStartCache.getInstance().getMarketCodeList().size()>0)
		{
			//所有TA节点编号List
			List marketCodeList = DailyStartCache.getInstance().getMarketCodeList();
			
			for (int j = 0; j < marketCodeList.size(); j++) 
			{
				String taCode = (String)marketCodeList.get(j);
				answerDataset.setValue(answerDsRowIndex, "id",
						taCode);
				answerDataset.setValue(answerDsRowIndex, "pid",
						"-1");
				answerDataset.setValue(answerDsRowIndex, "describe",             //从内存中根据marketCode获得TA名称
						TaInfoCache.getInstance().getTaName(taCode));
				answerDataset.setValue(answerDsRowIndex, "status",
						getTaStatus1(taCode)); 
				answerDataset.setValue(answerDsRowIndex, "flag",     //用于设别节点类型:0-普通节点,1-交易节点,2-TA节点
						"2");
				answerDsRowIndex++;
			}
		}else{
			throw new LpException(IErrMsg.ERR_DEFAULT,"清算配置表中不存在TA相关流程配置");
		}
		
		//HttpServletRequest request = context.getEventAttribute("$_REQUEST");
		request.setAttribute("dataset", answerDataset.getAnswerDataset());
		
		//this.actionForward(context,transCode,subTransCode);
	}
	
	/**
	 * 查询销售商清算节点列表
	 * @param context
	 * @throws Exception
	 */
	protected void queryBankList(String transCode,String subTransCode) throws LpException {
		AnswerDataset answerDataset = new AnswerDataset();
		int answerDsRowIndex = 1;
		
		//IDataset requestDataset = context.getRequestDataset();
		String marketCode = $("marketCode");
		String taskCode = $("taskCode");
		
		List list = DailyStartCache.getInstance().getTaskPoolListForBank(marketCode, taskCode);
		
		if(list.size()>0)
		{
			for (int j = 0; j < list.size(); j++) 
			{
				TaskPool tPool = (TaskPool)list.get(j);
				answerDataset.setValue(answerDsRowIndex, "id",
						tPool.getBankNo() + tPool.getTaskCode());
				answerDataset.setValue(answerDsRowIndex, "pid",
						"-1");
				answerDataset.setValue(answerDsRowIndex, "describe",          
						tPool.getTaskName());
				answerDataset.setValue(answerDsRowIndex, "status",
						tPool.getDealStatus()); 
				answerDataset.setValue(answerDsRowIndex, "flag", 
						"1");
				answerDataset.setValue(answerDsRowIndex, "bankNo", 
						tPool.getBankNo());
				//add by wuxj 20120511 begin
				answerDataset.setValue(answerDsRowIndex, "parentTaskCode", 
						tPool.getParentTaskCode());
				answerDataset.setValue(answerDsRowIndex, "marketCode", 
						tPool.getMarketCode());
				//add by wuxj 20120511 end
				answerDsRowIndex++;
			}
		}else{
			throw new LpException(IErrMsg.ERR_DEFAULT,"节点[" + taskCode + "]缺少分销售商运行的配置记录，请检查！");
		}
		
		//HttpServletRequest request = context.getEventAttribute("$_REQUEST");
		request.setAttribute("dataset", answerDataset.getAnswerDataset());
		request.setAttribute("marketCode",marketCode);
		request.setAttribute("taskCode",taskCode);
		
		//this.actionForward(context,transCode,subTransCode);
	}

	/**
	 * 监控状态
	 * 
	 * @param context
	 * @throws Exception
	 */
	protected void monitorStatus() throws LpException {
		//IDataset requestDataset = context.getRequestDataset();

		// 定义一个字段，表示查询的是主流程还是TA流程
		String flowType = $("flowType");
		DataLayer session = DataLayer.getDataLayer();

		if ("0".equals(flowType))       // 查询主流程:TA节点只有未激活、正在处理、作业完成
		{ 
			AnswerDataset answerDataset = new AnswerDataset();
			int answerDatasetIndex = 1;

			if(null!=DailyStartCache.getInstance().getMarketCodeList()
					&& DailyStartCache.getInstance().getMarketCodeList().size()>0)
			{
				List marketCodeList = DailyStartCache.getInstance().getMarketCodeList();
	
				for (int i = 0; i < marketCodeList.size(); i++) 
				{
					String status = "";
					String lastStatus = "";
					
					IDataset marketDs = session.getDataSet(
							"select * from tbtaskpool where market_code = ? and task_page='0'",
							marketCodeList.get(i));
					
					//map用于存放每个节点当前状态
					Map map = null;
					map = new HashMap(marketDs.getRowCount());
					
					marketDs.beforeFirst();
					
					for (int j = 0; j < marketDs.getRowCount(); j++) 
					{
						marketDs.next();
						//modified by huangjl 20141028-01 优化清算颜色显示问题  beg
						/*String dealStatus = marketDs.getString("deal_status");
						if (!"0".equals(dealStatus)        // 如果存在非"未激活",或者非"作业完成"状态,认为该TA正在处理中
								&& !"3".equals(dealStatus)) 
						{
							status = "Z";
							j = marketDs.getRowCount();    // 跳出循环
						} 
						else {                            // 如果是"0"或者"3"的状态
							if ("".equals(lastStatus))    // 第一次进入,为lastStatus赋值
							{ 
								status =lastStatus = dealStatus;
								continue;
							} 
							else {
								if (dealStatus.equals(
										lastStatus)) 
								{
									continue;
								} 
								else {
									status = "Z";
									j = marketDs.getRowCount();
								}
							}
						}*/
						if(map.size() <1 || !map.containsKey(marketDs.getString("task_code"))) {
							map.put(marketDs.getString("task_code"), marketDs.getString("deal_status"));
							status = marketDs.getString("deal_status");
						}else {
							if(map.get(marketDs.getString("task_code")).equals(marketDs.getString("deal_status"))) {
								continue;
							}else if(marketDs.getString("deal_status").equals("4")) {
								map.put(marketDs.getString("task_code"), "4");
								status = "4";
								continue;
							}else if(!marketDs.getString("task_code").equals("4")) {
								map.put(marketDs.getString("task_code"), "Z");
								status = "Z";
								continue;
							}
						}
						//modified by huangjl 20141028-01 优化清算颜色显示问题  end
					}
	
					// 将每个TA的状态压入应答包
					answerDataset.setValue(answerDatasetIndex, "id",
							(String) marketCodeList.get(i));
					answerDataset.setValue(answerDatasetIndex, "status", status);
					setMoreBackInformation(answerDataset, answerDatasetIndex, marketDs);
					answerDataset.setValue(answerDatasetIndex, "warnMsg", "");
					answerDatasetIndex++;
				}
			}
			
			// 对每个非TA清算节点进行处理
			IDataset taskCodeDs = session.getDataSet(
					"select * from tbtaskpool where market_code = ? and task_page='0'", 
					IConstant.DEFAULT_CODE);
			
			Map map = null;        //map用于存放每个节点当前状态
			map = new HashMap(taskCodeDs.getRowCount());

			taskCodeDs.beforeFirst();
			for (int i = 0; i < taskCodeDs.getRowCount(); i++) 
			{
				taskCodeDs.next();
				//如果map为空或者map中不包含当前处理的节点
				if (map.size() < 1 || !map.containsKey(taskCodeDs.getString("task_code"))) 
				{
					map.put(taskCodeDs.getString("task_code"), 
							taskCodeDs.getString("deal_status"));
					answerDataset.setValue(answerDatasetIndex, "id",
							taskCodeDs.getString("task_code"));
					answerDataset.setValue(answerDatasetIndex, "status", 
							taskCodeDs.getString("deal_status"));
					setMoreBackInformation(answerDataset, answerDatasetIndex, taskCodeDs);
					answerDatasetIndex++;
					continue;
				}
				else {        // 如果一个节点有两条以上配置,则需要比较这两条的状态
					String mapStatus = (String) map.get(taskCodeDs.getString("task_code"));
					if (mapStatus.equals(taskCodeDs.getString("deal_status"))) 
					{
						continue;
					} 
					//modified by huangjl 20141028-01   优化颜色显示功能   beg
					/*else { // 两条记录状态不同,认为正在处理
						map.put(taskCodeDs.getString("task_code"), "Z");
						answerDataset.setValue(answerDatasetIndex, "id",
								taskCodeDs.getString("task_code"));
						answerDataset.setValue(answerDatasetIndex, "status", "Z");
						setMoreBackInformation(answerDataset, answerDatasetIndex, taskCodeDs);
						answerDatasetIndex++;
					}*/
					else if(taskCodeDs.getString("deal_status").equals("4")) {   //如果有一条记录为处理失败，则都为处理失败
						map.put(taskCodeDs.getString("task_code"), "4");					
						answerDataset.setValue(answerDatasetIndex, "id",
								taskCodeDs.getString("task_code"));
						answerDataset.setValue(answerDatasetIndex, "status", 
								"4");
						setMoreBackInformation(answerDataset, answerDatasetIndex, taskCodeDs);
						answerDatasetIndex++;
					}else if(!mapStatus.equals("4")){
						map.put(taskCodeDs.getString("task_code"), "Z");					
						answerDataset.setValue(answerDatasetIndex, "id",
								taskCodeDs.getString("task_code"));
						answerDataset.setValue(answerDatasetIndex, "status", 
								"Z");
						setMoreBackInformation(answerDataset, answerDatasetIndex, taskCodeDs);
						answerDatasetIndex++;
					}
				}
				//modified by huangjl 20141028-01   优化颜色显示功能   end
			}
			
			//按照map中的节点状态,逐条更新answerDataset中的status
			IDataset returnDs = answerDataset.getAnswerDataset();
			if(returnDs.findColumn("id")>0){
				returnDs.beforeFirst();
				for(int i=0;i<returnDs.getRowCount();i++)
				{
					returnDs.next();
					String mapId = returnDs.getString("id");
					if(map.containsKey(mapId) && !returnDs.getString("status").equals(map.get(mapId)))
					{
						returnDs.updateString("status", (String)map.get(mapId));
					}
				}
				
			}
			setResponseDataset( returnDs);
		} 
		else if ("1".equals(flowType)) { // 更新TA流程
			String taCode = $("taCode");
			AnswerDataset answerDataset = new AnswerDataset();
			int answerDatasetIndex = 1;

			IDataset marketDs = session.getDataSet(
					"select * from tbtaskpool where market_code =? and task_page='0'", 
					taCode);
			marketDs.beforeFirst();
			for (int i = 0; i < marketDs.getRowCount(); i++) 
			{
				marketDs.next();
				answerDataset.setValue(answerDatasetIndex, "id", marketDs
						.getString("task_code"));
				answerDataset.setValue(answerDatasetIndex, "status", marketDs
						.getString("deal_status"));
				setMoreBackInformation(answerDataset, answerDatasetIndex, marketDs);
				answerDatasetIndex++;
			}
			
			setResponseDataset(answerDataset.getAnswerDataset());
		}
	}
	
	protected void monitorStatus1() throws LpException {
		//IDataset requestDataset = context.getRequestDataset();

		// 定义一个字段,表示查询的是主流程页面(0),TA列表页面(1),TA流程页面(2)
		String flowType = $("flowType");
		DataLayer session = DataLayer.getDataLayer();

		if ("0".equals(flowType))     // 查询主流程
		{ 
			AnswerDataset answerDataset = new AnswerDataset();
			int answerDatasetIndex = 1;

			if(null!=DailyStartCache.getInstance().getMarketCodeList() 
					&& DailyStartCache.getInstance().getMarketCodeList().size()>0)
			{
				answerDataset.setValue(answerDatasetIndex, "id", "TANODE");
				answerDataset.setValue(answerDatasetIndex, "status", getMainTaStatus());
				//setMoreBackInformation(answerDataset, answerDatasetIndex, marketDs);
				answerDataset.setValue(answerDatasetIndex, "warnMsg", "");
				answerDatasetIndex++;
			}
			
			// 对每个非TA清算节点进行处理
			IDataset taskCodeDs = session.getDataSet(
					"select * from tbtaskpool where market_code = ? and task_page='0' order by task_code,bank_no", IConstant.DEFAULT_CODE);
			
			//map用于存放每个节点当前状态
			Map map = null;
			Map errMsgMap = null;
			map = new HashMap(taskCodeDs.getRowCount());
			errMsgMap = new HashMap();
			
			//改变写法,对一个交易码多条记录的情况(比如父节点是两个TA中不同的节点,或者是saas版的分销售商情况)
			taskCodeDs.beforeFirst();
			for (int i = 0; i < taskCodeDs.getRowCount(); i++) 
			{
				taskCodeDs.next();
				String tmpTaskCode = taskCodeDs.getString("task_code");
				if(map.containsKey(tmpTaskCode)){
					map.put(tmpTaskCode, 
							getStatus(taskCodeDs.getString("deal_status"),
									(String) map.get(tmpTaskCode)));
				}else{
					map.put(tmpTaskCode, taskCodeDs.getString("deal_status"));
				}
				
				String warnMsg = taskCodeDs.getString("warn_msg");
				if(warnMsg.trim().length()>0){
					String[] array = {"[","]","{","}"};
					warnMsg = regularReplace(warnMsg, array, "");
					warnMsg = regularReplace(warnMsg, "\n", "<br>");
					errMsgMap.put(tmpTaskCode, warnMsg);
				}
			}
			
			Set keySet = map.keySet();
			Iterator it = keySet.iterator();
			while(it.hasNext()){
				String key = (String)it.next();
				
				answerDataset.setValue(answerDatasetIndex, "id", key);
				answerDataset.setValue(answerDatasetIndex, "status", (String)map.get(key));
				answerDataset.setValue(answerDatasetIndex, "warnMsg", errMsgMap.containsKey(key)?(String)errMsgMap.get(key):"");
				answerDatasetIndex++;
			}
			
			setResponseDataset(answerDataset.getAnswerDataset());
		} 
		else if ("1".equals(flowType)) { // 更新TA流程
			String taCode = $("taCode");
			AnswerDataset answerDataset = new AnswerDataset();
			int answerDatasetIndex = 1;

			IDataset marketDs = session.getDataSet(
					"select * from tbtaskpool where market_code =? and task_page='0'", 
					taCode);

			/*
			marketDs.beforeFirst();
			for (int i = 0; i < marketDs.getRowCount(); i++) 
			{
				marketDs.next();
				answerDataset.setValue(answerDatasetIndex, "id", marketDs
						.getString("task_code"));
				answerDataset.setValue(answerDatasetIndex, "status", marketDs
						.getString("deal_status"));
				setMoreBackInformation(answerDataset, answerDatasetIndex, marketDs);
				answerDatasetIndex++;
			}
			*/
			//map用于存放每个节点当前状态
			Map map = null;
			Map errMsgMap = null;
			map = new HashMap(marketDs.getRowCount());
			errMsgMap = new HashMap();
			
			//改变写法,对一个交易码多条记录的情况(saas版的分销售商情况)
			marketDs.beforeFirst();
			for (int i = 0; i < marketDs.getRowCount(); i++) 
			{
				marketDs.next();
				String tmpTaskCode = marketDs.getString("task_code");
				if(map.containsKey(tmpTaskCode)){
					map.put(tmpTaskCode, 
							getStatus(marketDs.getString("deal_status"),
									(String) map.get(tmpTaskCode)));
				}else{
					map.put(tmpTaskCode, marketDs.getString("deal_status"));
				}
				
				String warnMsg = marketDs.getString("warn_msg");
				if(warnMsg.trim().length()>0){
					String[] array = {"[","]","{","}"};
					warnMsg = regularReplace(warnMsg, array, "");
					warnMsg = regularReplace(warnMsg, "\n", "<br>");
					errMsgMap.put(tmpTaskCode, warnMsg);
				}
			}
			
			Set keySet = map.keySet();
			Iterator it = keySet.iterator();
			while(it.hasNext()){
				String key = (String)it.next();
				
				answerDataset.setValue(answerDatasetIndex, "id", key);
				answerDataset.setValue(answerDatasetIndex, "status", (String)map.get(key));
				answerDataset.setValue(answerDatasetIndex, "warnMsg", errMsgMap.containsKey(key)?(String)errMsgMap.get(key):"");
				answerDatasetIndex++;
			}
			
			setResponseDataset(answerDataset.getAnswerDataset());
		}
		else if ("2".equals(flowType)){   //更新TA列表界面
			AnswerDataset answerDataset = new AnswerDataset();
			int answerDatasetIndex = 1;
			
			Map map = getTaStatus();
			Iterator iter = map.keySet().iterator();
			for(;iter.hasNext();){
				String key = (String)iter.next();
				answerDataset.setValue(answerDatasetIndex, "id", key);
				answerDataset.setValue(answerDatasetIndex, "status", (String)map.get(key));
				//setMoreBackInformation(answerDataset, answerDatasetIndex, marketDs);
				answerDatasetIndex++;
			}
			
			setResponseDataset( answerDataset.getAnswerDataset());
		}
		else if("3".equals(flowType)){   //更新银行列表界面
			String marketCode = $("marketCode");
			String taskCode   = $("taskCode");
			
			AnswerDataset answerDataset = new AnswerDataset();
			int answerDatasetIndex = 1;

			IDataset ds = session.getDataSet(
					"select * from tbtaskpool where market_code =? and task_code=? and task_page='0'", 
					marketCode,taskCode);

			ds.beforeFirst();
			for (int i = 0; i < ds.getRowCount(); i++) 
			{
				ds.next();
				answerDataset.setValue(answerDatasetIndex, "id", 
						ds.getString("bank_no") + ds.getString("task_code"));
				answerDataset.setValue(answerDatasetIndex, "status", 
						ds.getString("deal_status"));
				setMoreBackInformation(answerDataset, answerDatasetIndex, ds);
				answerDatasetIndex++;
			}
			
			setResponseDataset(answerDataset.getAnswerDataset());
		}
	}

	/**
	 * 查询TA界面
	 * @param context
	 * @throws Exception
	 */
	protected void queryTa(String transCode,String subTransCode) throws LpException {
		//IDataset requestDataset = context.getRequestDataset();
		String marketCode = $("marketCode");

		AnswerDataset answerDataset = new AnswerDataset();
		int answerDsRowIndex = 1;
		DataLayer session = DataLayer.getDataLayer();
		
		List notTaTaskCodeList =DailyStartCache.getInstance().getTaskCodeList();     // 获得非TA节点列表
		List allDealedTaskNodes = null;          // 所有已处理的节点
		int allDealedTaskNodeCount = 0;
		Map map = null;
		IDataset marketCodeDs = session.getDataSet("select * from tbtaskpool where market_code=? and task_page='0'", 
				marketCode);

		if (marketCodeDs.getRowCount() > 0) 
		{
			allDealedTaskNodes = new ArrayList(marketCodeDs.getRowCount());
			map = new HashMap();
			do {
				allDealedTaskNodeCount = allDealedTaskNodes.size();
				marketCodeDs.beforeFirst();
				for (int i = 0; i < marketCodeDs.getRowCount(); i++) 
				{
					marketCodeDs.next();
					// 压入第一个节点,该节点的父作业节点在非TA节点链表中
					if (notTaTaskCodeList.contains(marketCodeDs.getString("parent_task_code")) 
							&& !map.containsKey(marketCodeDs.getString("task_code"))) 
					{
						answerDataset.setValue(answerDsRowIndex, "id",
								marketCodeDs.getString("task_code"));
						answerDataset.setValue(answerDsRowIndex, "pid",
								"-1");
						answerDataset.setValue(answerDsRowIndex,"describe", 
								marketCodeDs.getString("task_name"));
						answerDataset.setValue(answerDsRowIndex, "status",
								marketCodeDs.getString("deal_status"));
						answerDataset.setValue(answerDsRowIndex, "flag", 
								"1");
						answerDataset.setValue(answerDsRowIndex, "bankNo", DEFAULT_BANK_NO);
						answerDataset.setValue(answerDsRowIndex, "multiFlag", 
								DailyStartCache.getInstance().getMultiFlag(
										marketCode, 
										marketCodeDs.getString("task_code"), 
										DEFAULT_BANK_NO) ? "1" : "0");
						setMoreBackInformation(answerDataset, answerDsRowIndex, marketCodeDs);
						
						allDealedTaskNodes.add(marketCodeDs.getString("task_code"));
						answerDsRowIndex++;
						
						List list = new ArrayList();
						list.add("-1");
						map.put(marketCodeDs.getString("task_code"), list);
						
						continue;
					} 
					else {
						if (allDealedTaskNodes.contains(marketCodeDs.getString("parent_task_code"))) 
						{
							String task_code = marketCodeDs.getString("task_code");
							if(map.containsKey(task_code)                         //如果是已经处理过该节点,并且父作业码也相同,则不再处理
									&& ((List)map.get(task_code)).contains(marketCodeDs.getString("parent_task_code")))
							{
								continue;
							}
							else{
								answerDataset.setValue(answerDsRowIndex, "id",
										marketCodeDs.getString("task_code"));
								answerDataset.setValue(answerDsRowIndex, "pid",
										marketCodeDs.getString("parent_task_code"));
								answerDataset.setValue(answerDsRowIndex,"describe", 
										marketCodeDs.getString("task_name"));
								answerDataset.setValue(answerDsRowIndex,"status", 
										getNodeStatus(marketCodeDs.getString("market_code"), 
													  marketCodeDs.getString("task_code")));
								answerDataset.setValue(answerDsRowIndex, "flag", "1");
								answerDataset.setValue(answerDsRowIndex, "bankNo", DEFAULT_BANK_NO);
								answerDataset.setValue(answerDsRowIndex, "multiFlag", 
										DailyStartCache.getInstance().getMultiFlag(
												marketCode, 
												marketCodeDs.getString("task_code"), 
												DEFAULT_BANK_NO) ? "1" : "0");
								setMoreBackInformation(answerDataset, answerDsRowIndex, marketCodeDs);

								allDealedTaskNodes.add(marketCodeDs.getString("task_code"));
								answerDsRowIndex++;
								
								List list = new ArrayList();
								list.add(marketCodeDs.getString("parent_task_code"));
								map.put(marketCodeDs.getString("task_code"), list);
								
								continue;
							}
						}
					}
				}
			} while (answerDsRowIndex<=marketCodeDs.getRowCount() 
						&& allDealedTaskNodeCount<allDealedTaskNodes.size());
		} 
		else {
			throw new LpException(IErrMsg.ERR_DBNOSETTING, "存在未配置的交易!");
		}
		//HttpServletRequest request = context.getEventAttribute("$_REQUEST");
		request.setAttribute("dataset", answerDataset.getAnswerDataset());
		request.setAttribute("taCode", marketCode);
		//this.actionForward(context, transCode,subTransCode);
	}
	
	
	/**
	 * 获得节点提示信息
	 * @param context
	 * @throws Exception
	 */
	private void getTipsForNode() throws LpException {
		//IDataset requestDataset = context.getRequestDataset();
		String taskCode = $("taskCode");
		String marketCode = $("marketCode");
		String parentTaskCode = $("parentTaskCode"); //这里的parentTaskCode是以"|"分隔的字符串,一个节点的父作业码可能不止一个
		
		String parentTaskCodeForQuery = "";
		if(parentTaskCode.indexOf("|")<0)
		{
			parentTaskCodeForQuery = parentTaskCode;
		}else{
			parentTaskCodeForQuery = parentTaskCode.substring(0, parentTaskCode.indexOf("|"));
		}
		
		AnswerDataset answerDataset = new AnswerDataset();
		String transName = ((TaskPool)DailyStartCache.getInstance().
							 getTaskPool(marketCode, taskCode, parentTaskCodeForQuery)).getTaskName();
		answerDataset.setValue("describe", transName);
		answerDataset.setValue("marketCode", marketCode);
		answerDataset.setValue("parentTaskCode", parentTaskCode);
		
		setResponseDataset( answerDataset.getAnswerDataset());
	}
	
	/**
	 * 获得节点的详细信息
	 * @param context
	 * @throws Exception
	 */
	private void getDetailsForNode() throws LpException {
		//IDataset requestDataset = context.getRequestDataset();
		String taskCode = $("taskCode");
		String marketCode = $("marketCode");
		String parentTaskCode = $("parentTaskCode");
		//add  by wuxj 20120509 begin
		String bankNo = $("bankNo");
		 //String taskCode1=requestDataset.getString("taskCode").substring(3, 9);
		//add  by wuxj 20120509 end
		AnswerDataset answerDataset = new AnswerDataset();
		int answerDsRowIndex = 1;
		DataLayer session = DataLayer.getDataLayer();
		
		if(parentTaskCode.indexOf("|")<0)           //只有一个父作业节点
		{	
			//update  by wuxj 20120509 begin
			IDataset ds ;
			if(taskCode.length()>6){
				String taskCode1 =taskCode.substring(3, 9);
				 ds = session.getDataSet(
						"select * from tbtaskpool where market_code=? and task_code=? and parent_task_code=? and task_page='0' and bank_no =?",
						marketCode,taskCode1,parentTaskCode,bankNo);
			}else{
				 ds = session.getDataSet(
						"select * from tbtaskpool where market_code=? and task_code=? and parent_task_code=? and task_page='0'",
						marketCode,taskCode,parentTaskCode);
			}
			//update  by wuxj 20120509
			/*IDataset ds = session.getDataSet(
					"select * from tbtaskpool where market_code=? and task_code=? and parent_task_code=? and task_page='0'",
					marketCode,taskCode,parentTaskCode);*/
			if(null!=ds && ds.getRowCount()>0)
			{
				answerDataset.setValue(answerDsRowIndex, "taskCode",
						taskCode);
				answerDataset.setValue(answerDsRowIndex, "describe",
						ds.getString("task_name"));
				setMoreBackInformation(answerDataset, answerDsRowIndex, ds);
				answerDsRowIndex++;
			}
			else{
				throw new LpException(IConstant.DEFAULT_CODE,"节点任务:" + taskCode + "的配置不存在!");
			}
		}else{                                   //有多个父作业节点
			String[] parentTaskCodeArray = parentTaskCode.split("\\|");
			String condition = "";
			//added by yhb for saas 多条配置拥有相同父节点的情况
			List list = new ArrayList();
			//added by yhb for saas 多条配置拥有相同父节点的情况
			for(int i=0;i<parentTaskCodeArray.length;i++)
			{
				if(!list.contains(parentTaskCodeArray[i]))
				{
					list.add(parentTaskCodeArray[i]);
					condition += "'" + parentTaskCodeArray[i] + "',";
				}
			}
			condition = condition.substring(0, condition.length()-1);
			
			IDataset ds = session.getDataSet(
					"select * from tbtaskpool where market_code=? and task_code=? and task_page='0' and parent_task_code in("
					+ condition + ")",marketCode,taskCode);
			if(null!=ds && ds.getRowCount()>0)
			{
				answerDataset.setValue(answerDsRowIndex, "taskCode",
						taskCode);
				answerDataset.setValue(answerDsRowIndex, "describe",
						ds.getString("task_name"));
				setMoreBackInformation(answerDataset, answerDsRowIndex, ds);
				answerDataset.setValue(answerDsRowIndex,"parentTaskCode",ds.getString("parent_task_code"));
				answerDsRowIndex++;
			}
			else{
				throw new LpException(IConstant.DEFAULT_CODE,"节点任务:" + taskCode + "的配置不存在!");
			}
			
		}

		setResponseDataset( answerDataset.getAnswerDataset());
	}
	
	/**
	 * 状态校验
	 * @param context
	 * @throws Exception
	 */
	private void checkStatus() throws LpException{
		//IDataset requestDataset = context.getRequestDataset();
		String taskCode = $("taskCode");
		String taCode = $("taCode");
		String bankNo = $("bankNo");
		
		//只需要校验是否已经完成,如果完成提示是否继续进行
		AnswerDataset answerDataset = new AnswerDataset();
		DataLayer session = DataLayer.getDataLayer();
		IDataset ds = session.getDataSet(
				"select * from tbtaskpool where market_code=? and task_code=?  and bank_no=? and task_page = '0'",
				taCode,taskCode,bankNo);
		
		String status = "";
		if(ds.getRowCount()>0)
		{
			ds.beforeFirst();
			for(int i=0;i<ds.getRowCount();i++)
			{
				ds.next();
				status = ds.getString("deal_status");
				if(!"3".equals(status))
				{
					status = "";                //如果发现有不是已完成状态的配置,直接返回空
					break;
				}
			}
		}
		
		answerDataset.setValue(1, "status", status);
		setResponseDataset( answerDataset.getAnswerDataset());
	}
	
	/**
	 * 日志下载
	 * @param context
	 * @throws Exception
	 */
	private void downLog() throws LpException {
		//IDataset requestDataset = context.getRequestDataset();
		String taCode = $("marketCode");
		String functionId = $("taskCode");
		
		//获得配置文件中的批量日志存放路径
		String logPath = "";
		/*try {
			logPath = ConsoleConfig.getConsoleConfig().getValue(IConstant.BTACHLOGPATH);
		} catch (Exception e) {
			throw new Exception("日志文件存放路径未配置!");
		}*/
		
	    if(HsData.isNullStr(logPath)){
	    	File directory = new File("logs"); 
            if(!directory.exists()){
            	directory.mkdir();
            }
	    	logPath=directory.getAbsolutePath().replace('\\', '/');
	    }
	    
	    String filePath = logPath+"/" + taCode + "/";
		String fileName = functionId + HsDate.getDate() + ".log";   //默认获得当天的日志
		
		String remoteIp = "";
		int port = 0;
		/*try {
			//批量处理主机地址
			remoteIp = ConsoleConfig.getConsoleConfig().getValue("REMOTEIP");
			//批量处理主机端口
			port = Integer.valueOf(ConsoleConfig.getConsoleConfig().getValue("REMOTEPORT"));
		} catch (Exception e) {
			throw new Exception("批量处理的主机IP或端口未配置!");
		}*/
		
		FileClient fileClient = new FileClient(remoteIp, port);
		
		byte[] logByte;
		try {
			logByte = fileClient.downloadFileByteArray(fileName, filePath);
		} catch (Exception e) {
			e.printStackTrace();
			logByte = (functionId + "在" + HsDate.dateTo10(HsDate.getDate()) + "的日志不存在").getBytes();
		} 
		
		String logString = new String(logByte);
		
		IDataset returnDataset = DatasetService.getDefaultInstance().getDataset();
		returnDataset.addColumn("marketCode");
		returnDataset.addColumn("taskCode");
		returnDataset.addColumn("log");
		returnDataset.appendRow();
		returnDataset.updateString("marketCode", taCode);
		returnDataset.updateString("taskCode", functionId);
		returnDataset.updateString("log", "".equals(logString.trim())?"日志内容为空!":logString);
		setResponseDataset( returnDataset);
	}

	/**
	 * 调用远程服务
	 * @param contesxt
	 * @throws Exception
	 */
	protected void callRemoteService() throws LpException {
		String SEND_ID = "999999";
		String nodeId = $("nodeId");
		String taCode = $("taCode");
		String exceuteMode = $("ExceuteMode");
		String operFlag = $("operFlag");
		String bankNo = $("bankNo");
		if(HsData.isNullStr(bankNo)){
			bankNo = DEFAULT_BANK_NO;
		}
		
		
		DailyStartRequest request = new DailyStartRequest(bankNo, nodeId, taCode, exceuteMode, operFlag);
		String packXml = request.pkgRquest();
		Log.getInstance().debug(logid, "请求报文："+packXml);
		SendGapsService sendGapsService = new SendGapsService();
		
		if("1".equals(exceuteMode))   //单步,只用send模式向后台发交易
		{
			sendGapsService.sendGaps(packXml, SEND_ID);
			//ServiceClient.send(dailyStartReqDataset.getRequestDataset());
			IDataset returnDataset = DatasetService.getDefaultInstance().getDataset();
			returnDataset.addColumn("ReturnCode", DatasetColumnType.DS_STRING);
			returnDataset.addColumn("ErrorNo", DatasetColumnType.DS_STRING);
			returnDataset.addColumn("ErrorInfo", DatasetColumnType.DS_STRING);
			returnDataset.addColumn("taskCode", DatasetColumnType.DS_STRING);
			returnDataset.addColumn("bankNo", DatasetColumnType.DS_STRING);
			returnDataset.appendRow();
			returnDataset.updateString("ReturnCode", "0");
			returnDataset.updateString("ErrorNo", "0000");
			returnDataset.updateString("ErrorInfo", "单步操作触发成功");
			returnDataset.updateString("taskCode", nodeId);
			returnDataset.updateString("bankNo", bankNo);
			setResponseDataset( returnDataset);
		}
		else if("2".equals(exceuteMode))        //激活的操作,采用sendAndReceive模式
		{
			String respXml = sendGapsService.sendGaps(packXml, SEND_ID);
			
			JdomXml jdom = new JdomXml();
			jdom.build(respXml);
			Element	root = jdom.getRoot();
			
			IDataset returnDataset = DatasetService.getDefaultInstance().getDataset();
			returnDataset.addColumn("ReturnCode", DatasetColumnType.DS_STRING);
			returnDataset.addColumn("ErrorNo", DatasetColumnType.DS_STRING);
			returnDataset.addColumn("ErrorInfo", DatasetColumnType.DS_STRING);
			returnDataset.addColumn("taskCode", DatasetColumnType.DS_STRING);
			returnDataset.addColumn("bankNo", DatasetColumnType.DS_STRING);
			returnDataset.appendRow();
			returnDataset.updateString("ReturnCode", "0");
			String errorNo = root.getChildText("respcode");
			if("919998".equals(errorNo)){
				returnDataset.updateString("ErrorNo", "0000");
				returnDataset.updateString("ErrorInfo", "运行成功");
			}else{
				returnDataset.updateString("ErrorNo", root.getChildText("respcode"));
				returnDataset.updateString("ErrorInfo", root.getChildText("respmsg"));
			}			
			returnDataset.updateString("taskCode", nodeId);
			returnDataset.updateString("bankNo", bankNo);
	
			setResponseDataset(returnDataset);
		}
	}
	
	/**
	 * 去除字符串中特殊字符,如中括号,花括号等
	 * @param src          源字符串
	 * @param regex        需要去除的字符
	 * @param replacement  替代的字符
	 * @return
	 * @throws Exception
	 */
	private String regularReplace(String src, String regex, String replacement) throws LpException{
		if(replacement.indexOf(regex)>0){
			throw new LpException("Replacement is not Specification !");
		}
		
		String result = src;
		while(result.indexOf(regex)!=-1){
			int index = result.indexOf(regex);
			result = result.substring(0, index) + replacement + result.substring(index + 1);
		}
		return result;
	}
	
	/**
	 * 去除字符串中特殊字符,如中括号,花括号等
	 * @param src
	 * @param regexArray
	 * @param replacement
	 * @return
	 * @throws Exception
	 */
	private String regularReplace(String src, String[] regexArray, String replacement) throws LpException{
		String result = src;
		for(int i=0;i<regexArray.length;i++){
			String regex = regexArray[i];
			if(replacement.indexOf(regex)>0){
				throw new LpException("Replacement is not Specification !");
			}
			
			while(result.indexOf(regex)!=-1){
				int index = result.indexOf(regex);
				result = result.substring(0, index) + replacement + result.substring(index + 1);
			}
		}
		return result;
	}

	public void setResponseDataset(IDataset result) throws LpException{
		 String dataType = $("_type");
		 String _mapping = $("mapping");
		 String rootId = $("_rootId");
	      if (!StringUtils.isNotBlank(rootId)) {
	        rootId = $("node");
	      }
	      boolean isMap = $("isMap") != null ? Boolean.valueOf($("isMap")).booleanValue() : false;
	      boolean hasLeafAttribute = $("hasLeafAttribute") != null ? Boolean.valueOf($("hasLeafAttribute")).booleanValue() : false;
	      
		 StringBuffer tempBuffer = new StringBuffer();
	     tempBuffer.append("{ dataSetResult : [");
		 ServletUtil servletUtil = new ServletUtil();
		 if ((dataType != null) && (dataType.equals("treeOutput"))) {
           servletUtil.result2Tree(result, dataType, tempBuffer, _mapping, rootId, hasLeafAttribute);
         } else {
            servletUtil.result2List(result, dataType, tempBuffer, isMap);
         }
		 tempBuffer.append("],");
		    tempBuffer.append("returnCode : ");
		    tempBuffer.append(0);
		    tempBuffer.append(",");
		    tempBuffer.append("errorNo : ");
		    tempBuffer.append(escapingString("0"));
		    tempBuffer.append(",");
		    tempBuffer.append("errorInfo : ");
		    tempBuffer.append(escapingString("null"));
		    tempBuffer.append("}");
		 String json = tempBuffer.toString();
		// System.out.println("tempBuffer.toString()=="+json);
		AjaxTools.exAjax(json, response,logid);
	}
	
	  private String escapingString(String obj)
	  {
	    if (obj == null) {
	      return null;
	    }
	    CharacterIterator it = new StringCharacterIterator(obj.trim());
	    StringBuilder build = new StringBuilder();
	    build.append("\"");
	    for (char c = it.first(); c != 65535; c = it.next()) {
	      if (c == '"') {
	        build.append("\\\"");
	      } else if (c == '\\') {
	        build.append("\\\\");
	      } else if (c == '/') {
	        build.append("\\/");
	      } else if (c == '\b') {
	        build.append("\\b");
	      } else if (c == '\f') {
	        build.append("\\f");
	      } else if (c == '\n') {
	        build.append("\\n");
	      } else if (c == '\r') {
	        build.append("\\r");
	      } else if (c == '\t') {
	        build.append("\\t");
	      } else {
	        build.append(c);
	      }
	    }
	    build.append("\"");
	    return build.toString();
	  }


}
