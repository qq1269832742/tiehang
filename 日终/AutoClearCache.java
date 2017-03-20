/********************************************
 * 文件名称: AutoClearCache.java
 * 系统名称: 综合理财管理平台V3.0
 * 模块名称:
 * 软件版权: 恒生电子股份有限公司
 * 功能说明: 
 * 系统版本: 3.0.0.3
 * 开发人员: 
 * 开发时间: 2010-10-11 下午07:32:02
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期    修改人员    修改说明
 * V3.0.0.2  20131212   yuanhb      分业务清算    M201312170010
 * V3.0.0.3  20140321   yuanhb      分业务清算缺陷修复  M201403210001
 *********************************************/
package com.levelappro.gbism.app.jrfw.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.levelappro.gbism.app.jrfw.bean.IConstant;
import com.levelappro.gbism.app.jrfw.bean.IErrMsg;
import com.levelappro.gbism.app.jrfw.bean.TaskPool;
import com.levelappro.gbism.app.jrfw.dataset.IDataset;
import com.levelappro.gbism.app.jrfw.db.DataLayer;
import com.levelappro.gbism.util.LpException;



public class AutoClearCache {
	public static final String HUNDSUN_VERSION="@system 综合理财平台 @version 3.0.0.3 @lastModiDate 2014-03-21 @describe ";
	private List<TaskPool> cacheList = new ArrayList<TaskPool>();
	private Map<String, String> map = new HashMap<String, String>();
	private static AutoClearCache instance = null;
	
	private AutoClearCache() throws LpException{
		init();
	}
	
	/**
	 * 初始化
	 * @throws LpException
	 */
	private void init() throws LpException{
		DataLayer session = DataLayer.getDataLayer();
		
		try {
			cacheList = session.getObjectList(
					"select * from tbtaskpool where task_page = '1' order by market_code,task_code", 
					TaskPool.class);
			
			IDataset ds = session.getDataSet("select market_code, prd_type from vmarketinfo");
			ds.beforeFirst();
			for(int i=0; i<ds.getRowCount(); i++){
				ds.next();
				map.put(ds.getString("market_code"), ds.getString("prd_type"));
			}
			
		} catch (LpException e) {
			e.printStackTrace();
			throw new LpException(IErrMsg.ERR_DEFAULT,
					"初始化TaskPoolCache失败");
		} 
	}
	
	/**
	 * 获取缓存类
	 * @return
	 * @throws LpException
	 */
	synchronized public static AutoClearCache getInstance() throws LpException{
		if(instance==null)
		{
			instance=new AutoClearCache();
		}
		return instance;
	}
	
	/**
	 * 刷新缓存
	 */
	public void refresh() throws LpException{
		synchronized (cacheList) {
			cacheList.clear();
			init();
		}
	}
	
	/**
	 * 获得某条配置信息
	 * @param marketCode
	 * @param taskCode
	 * @param parentTaskPool
	 * @return
	 */
	public TaskPool getTaskPool(String marketCode, String taskCode, String parentTaskCode){
		TaskPool taskPool = null;
		for(int i=0;i<cacheList.size();i++)
		{
			taskPool = cacheList.get(i);
			if(marketCode.equals(taskPool.getMarketCode())
					&& taskCode.equals(taskPool.getTaskCode())
					&& parentTaskCode.equals(taskPool.getParentTaskCode()))
			{
				return taskPool;
			}
		}
		return null;
	}
	
	/**
	 * 获得某条配置信息
	 * @param marketCode
	 * @param taskCode
	 * @param parentTaskPool
	 * @param bankNo
	 * @return
	 */
	public TaskPool getTaskPool(String marketCode, String taskCode, String parentTaskCode,String bankNo){
		TaskPool taskPool = null;
		for(int i=0;i<cacheList.size();i++)
		{
			taskPool = cacheList.get(i);
			if(marketCode.equals(taskPool.getMarketCode())
					&& taskCode.equals(taskPool.getTaskCode())
					&& parentTaskCode.equals(taskPool.getParentTaskCode())
					&& bankNo.equals(taskPool.getBankNo()))
			{
				return taskPool;
			}
		}
		return null;
	}
	
	/**
	 * 获得某个市场代码下的配置信息链表
	 * @param marketCode
	 * @return
	 */
	public List getTaskPoolListByMarketCode(String marketCode){
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(marketCode.equals(taskPool.getMarketCode()))
			{
				list.add(taskPool);
			}
		}
		return list;
	}
	
	/**
	 * 根据父作业代码获得父作业所在的市场代码列表
	 * 一个作业被多个TA清算所拥有
	 * @param parentTaskCode
	 * @return
	 */
	public List getMarketCodeListByParentTaskCode(String parentTaskCode){
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++)
		{
			String taskCode = cacheList.get(i).getTaskCode();
			if(taskCode.equals(parentTaskCode))
			{
				if(!list.contains(taskCode))
				{
					list.add(cacheList.get(i).getMarketCode());
				}
			}
		}
		return list;
	}
	
	/**
	 * 判断一个作业的父作业是否在TA清算中
	 * true:  在
	 * false: 不在
	 * @param parentTaskCode
	 * @return
	 */
	public boolean checkIsParentInMarket(String parentTaskCode){
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(taskPool.getTaskCode().equals(parentTaskCode))
			{
				String marketCode = taskPool.getMarketCode();
				if(!IConstant.DEFAULT_CODE.equals(marketCode))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 获得TaskPool表中所有配置的TA清算代码
	 * @return
	 */
	public List getMarketCodeList() {
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++){
			String marketCode = cacheList.get(i).getMarketCode();
			if(!IConstant.DEFAULT_CODE.equals(marketCode)
					&& !list.contains(marketCode))
			{
				list.add(marketCode);
			}
		}
		return list;
	}
	
	/**
	 * 获得TaskPool中所有非TA清算中的作业代码
	 * @return
	 * @throws Exception
	 */
	public List getTaskCodeList() throws LpException {
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(IConstant.DEFAULT_CODE.equals(taskPool.getMarketCode())){
				String taskCode = taskPool.getTaskCode();
				if(!list.contains(taskCode)){
					list.add(taskCode);
				}
			}
		}
		return list;
	}
	
	/**
	 * 判断某个父作业码是否在表中有配置
	 * @param parentTaskCode
	 * @return
	 * @throws Exception
	 */
	public boolean isParentTaskCodeInTable(String parentTaskCode) throws LpException{
		for(int i=0;i<cacheList.size();i++){
			if(cacheList.get(i).getTaskCode().equals(parentTaskCode))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获得一个节点的父节点串
	 * 以"|"分隔
	 * @param marketCode
	 * @param taskCode
	 * @return
	 * @throws Exception
	 */
	public String getParentCodeString(String marketCode, String taskCode) throws LpException{
		String parentCodeString = "";
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(marketCode.equals(taskPool.getMarketCode())
					&& taskCode.equals(taskPool.getTaskCode()))
			{
				if(parentCodeString.indexOf(taskPool.getParentTaskCode())>=0)
				{
					continue;
				}
				parentCodeString += taskPool.getParentTaskCode() + "|";
			}
		}
		parentCodeString = parentCodeString.substring(0, parentCodeString.length()-1);
		return parentCodeString;
	}
	
	/**
	 * 判断节点是否区分银行
	 * @param marketCode
	 * @param taskCode
	 * @return
	 * @throws LpException
	 */
	public boolean getMultiFlag(String marketCode, String taskCode, String defaultBankNo)throws LpException{
		boolean multiFlag = false;
		List list = getTaskPoolListByMarketCode(marketCode);
		
		for(int i=0;i<list.size();i++){
			TaskPool tPool = (TaskPool)list.get(i);
			if(!tPool.getTaskCode().equals(taskCode))
			{
				continue;
			}
			if(!defaultBankNo.equals(tPool.getBankNo())){
				multiFlag = true;
				break;
			}
		}
		return multiFlag;
	}
	
	/**
	 * 取区分销售商的节点配置的链表
	 * @param marketCode
	 * @param taskCode
	 * @return
	 * @throws LpException
	 */
	public List getTaskPoolListForBank(String marketCode, String taskCode)throws LpException{
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(!taskPool.getMarketCode().equals(marketCode)){
				continue;
			}else{
				if(!taskPool.getTaskCode().equals(taskCode)){
					continue;
				}else{
					list.add(taskPool);
				}
			}
		}
		return list;
	}
	

	/**
	 * 获得taskpool中配置的业务类型
	 * @return
	 * @throws Exception
	 */
	public List getPrdTypesList() throws LpException{
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(!IConstant.DEFAULT_CODE.equals(taskPool.getMarketCode())){
				if(taskPool.getMarketCode().startsWith(IConstant.MARKET_CODE_PREFIX)){
					if(!list.contains(taskPool.getMarketCode().substring(1,2))) {
						list.add(taskPool.getMarketCode().substring(1,2));
					}
				}else{
					if(null!=map.get(taskPool.getMarketCode())){
						String prdType = (String)map.get(taskPool.getMarketCode());
						if(!list.contains(prdType)){
							list.add(prdType);
						}
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * 获得taskpool中配置的业务类型(仅仅是含有公共节点的业务列表)
	 * @return
	 * @throws Exception
	 */
	public List getPrdTypesListOnly() throws LpException{
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(taskPool.getMarketCode().startsWith(IConstant.MARKET_CODE_PREFIX)){
				if(!list.contains(taskPool.getMarketCode().substring(1,2))) {
					list.add(taskPool.getMarketCode().substring(1,2));
				}
			}
		}
		return list;
	}
	
	/**
	 * 判断一个作业的父作业是否在TA清算中
	 * true:  在
	 * false: 不在
	 * @param parentTaskCode
	 * @return
	 */
	public boolean checkIsParentInMarket(String parentTaskCode, String prdType){
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(taskPool.getTaskCode().equals(parentTaskCode))
			{
				String marketCode = taskPool.getMarketCode();
				/*modified by yuanhb 20140321 去掉多余的标点 beg*/
				//if(!IConstant.DEFAULT_CODE.equals(marketCode) && !marketCode.startsWith(IConstant.MARKET_CODE_PREFIX + prdType));
				if(!IConstant.DEFAULT_CODE.equals(marketCode) && !marketCode.startsWith(IConstant.MARKET_CODE_PREFIX + prdType))
				/*modified by yuanhb 20140321 去掉多余的标点 end*/
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断一个作业的父作业是系统公共节点
	 * true:  在
	 * false: 不在
	 * @param parentTaskCode
	 * @return
	 */
	public boolean checkIsParentInSystem(String parentTaskCode){
		for(int i=0;i<cacheList.size();i++)
		{
			TaskPool taskPool = cacheList.get(i);
			if(taskPool.getTaskCode().equals(parentTaskCode))
			{
				String marketCode = taskPool.getMarketCode();
				if(IConstant.DEFAULT_CODE.equals(marketCode))
				{   
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 获得TaskPool表中某个业务所配置的TA清算代码
	 * @return
	 */
	public List getMarketCodeList(String prdType) {
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++){
			String marketCode = cacheList.get(i).getMarketCode();
			String pType = (String)map.get(marketCode);
			if(null==pType || "".equals(pType.trim())){
				continue;
			}else{
				if(pType.equals(prdType)){
					if(!IConstant.DEFAULT_CODE.equals(marketCode)
							&& !marketCode.startsWith(IConstant.MARKET_CODE_PREFIX + prdType)
							&& !list.contains(marketCode))
					{
						list.add(marketCode);
					}
				}
			}
		}
		return list;
	}

}
