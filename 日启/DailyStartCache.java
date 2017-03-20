/********************************************
 * 文件名称: DailyStartCache.java
 * 系统名称: 综合理财管理平台V3.0
 * 模块名称:
 * 软件版权: 恒生电子股份有限公司
 * 功能说明: 
 * 系统版本: 3.0.0.1
 * 开发人员: yuanhb
 * 开发时间: 2010-10-11 下午07:32:02
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期    修改人员    修改说明
 *********************************************/
package com.levelappro.gbism.app.jrfw.action;

import java.util.ArrayList;
import java.util.List;

import com.levelappro.gbism.app.jrfw.bean.IConstant;
import com.levelappro.gbism.app.jrfw.bean.IErrMsg;
import com.levelappro.gbism.app.jrfw.bean.TaskPool;
import com.levelappro.gbism.app.jrfw.db.DataLayer;
import com.levelappro.gbism.util.LpException;

public class DailyStartCache {
	public static final String HUNDSUN_VERSION="@system 综合理财平台 @version 4.0.0.1  @lastModiDate 2012-04-05 @describe " +
			"@system 综合理财平台 @version 4.0.0.1  @lastModiDate 2012-05-15 @add by wuxj  解决无法在根节点建立多个分支问题";
	/**
	 * 保存表结构对应Bean的链表
	 */
	private List<TaskPool> cacheList = new ArrayList<TaskPool>();
	


	/**
	 * 私有静态实例
	 */
	private static DailyStartCache instance = null;
	
	/**
	 * 私有构造方法
	 * @throws LpException
	 */
	private DailyStartCache() throws LpException{
		init();
	}
	
	/**
	 * 初始化,只得到日启的配置信息
	 * @throws LpException
	 */
	private void init() throws LpException{
		DataLayer db = DataLayer.getDataLayer();
		try {
			cacheList = db.getObjectList(
					"select * from tbtaskpool where task_page='0' order by market_code,task_code", 
					TaskPool.class);
			
		} catch (LpException e) {
			e.printStackTrace();
			throw new LpException(IErrMsg.ERR_DEFAULT,"初始化DailyStartCache失败");
		} finally{
			try {
				db.freeConnection();
				
			} catch (LpException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取内存中的静态成员实例
	 * @return
	 * @throws LpException
	 */
	synchronized public static DailyStartCache getInstance() throws LpException{
		if(instance==null){
			instance=new DailyStartCache();
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
	 * 获得某条配置信息对应的Bean
	 * @param marketCode 市场代码
	 * @param taskCode 作业代码
	 * @param parentTaskPool 父作业码
	 * @return
	 */
	public TaskPool getTaskPool(String marketCode, String taskCode, String parentTaskCode){
		TaskPool taskPool = null;
		for(int i=0;i<cacheList.size();i++){
			taskPool = cacheList.get(i);
			
			if(marketCode.equals(taskPool.getMarketCode())
					&& taskCode.equals(taskPool.getTaskCode())
					&& parentTaskCode.equals(taskPool.getParentTaskCode())){
				return taskPool;
			}
		}
		return null;
	}
	
	/**
	 * 根据市场代码获得该市场下的配置信息链表
	 * @param marketCode
	 * @return
	 */
	public List getTaskPoolListByMarketCode(String marketCode){
		List list = new ArrayList();
		for(int i=0;i<cacheList.size();i++){
			TaskPool taskPool = cacheList.get(i);
			if(marketCode.equals(taskPool.getMarketCode())){
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
		for(int i=0;i<cacheList.size();i++){
			String taskCode = cacheList.get(i).getTaskCode();
			if(taskCode.equals(parentTaskCode)){
				String marketCode = cacheList.get(i).getMarketCode();
				if(!list.contains(marketCode)){
					list.add(marketCode);
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
		for(int i=0;i<cacheList.size();i++){
			if(cacheList.get(i).getTaskCode().equals(parentTaskCode)){
				String marketCode = cacheList.get(i).getMarketCode();
				if(!IConstant.DEFAULT_CODE.equals(marketCode)){
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
			if(!IConstant.DEFAULT_CODE.equals(marketCode)){
				if(!list.contains(marketCode)){
					list.add(marketCode);
				}
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
		for(int i=0;i<cacheList.size();i++){
			if(IConstant.DEFAULT_CODE.equals(cacheList.get(i).getMarketCode())){
				String taskCode = cacheList.get(i).getTaskCode();
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
			if(cacheList.get(i).getTaskCode().equals(parentTaskCode)){
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
		//add by wuxj 20120515 begin
		if(parentCodeString.length()>1){
			parentCodeString = parentCodeString.substring(0, parentCodeString.length()-1);
		}else{
			parentCodeString=" ";
		}
		//add by wuxj 20120515 end 
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
	
	
}
