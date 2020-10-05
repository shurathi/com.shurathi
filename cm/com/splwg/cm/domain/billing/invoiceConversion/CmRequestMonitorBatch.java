/* 
 **************************************************************************
 *           	     Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                 
 **************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This monitor batch is responsible for transmitting request
 * from one state to another when certain criteria is met.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-06-29   KChan      CB-159. Initial	
 * 2020-09-18   SAnart     CB-404. Invoice Conversion Improvements
 **************************************************************************
 */

package com.splwg.cm.domain.billing.invoiceConversion;

import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.lookup.MessageSeverityLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.support.context.FrameworkSession;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.common.LoggedException;

/**
 * @author RIA-Admin
 *
@BatchJob (modules = { "demo"},
 *      softParameters = { @BatchJobSoftParameter (name = maintenanceObject, required = true, type = string)
 *            , @BatchJobSoftParameter (name = isRestrictedByBatchControl, required = true, type = string)
 *            , @BatchJobSoftParameter (name = restrictByRequestType, type = string)
 *            , @BatchJobSoftParameter (name = restrictByBo, type = string)
 *            , @BatchJobSoftParameter (name = restrictByStatus, type = string)
 *            , @BatchJobSoftParameter (name = orderByCharType, type = string)})
 */

public class CmRequestMonitorBatch extends CmRequestMonitorBatch_Gen {

	public JobWork getJobWork() {
		
		List<ThreadWorkUnit> threadWorkUnitList=new ArrayList<ThreadWorkUnit>();
		StringBuilder retrieveAccountIds=new StringBuilder();
		PreparedStatement pstmt=null;
		
		String accountId;
		ThreadWorkUnit threadWorkUnit;
		
		try{
		 
		retrieveAccountIds.append("select distinct PK_VALUE1 as PK_VALUE1");
		retrieveAccountIds.append(" from C1_REQUEST, C1_REQUEST_REL_OBJ");
		retrieveAccountIds.append(" where C1_REQUEST.BUS_OBJ_CD in");
		retrieveAccountIds.append(" ( select BUS_OBJ_CD FROM F1_BUS_OBJ where MAINT_OBJ_CD=rpad(:restrictByMO,12))");
		retrieveAccountIds.append(" and ( :restrictByBO is null or C1_REQUEST.BUS_OBJ_CD=rpad(:restrictByBO,30))");
		retrieveAccountIds.append(" and ( C1_REQUEST.C1_REQ_TYPE_CD=rpad(:restrictByRqType,30) or :restrictByRqType is null )");
		retrieveAccountIds.append(" and ( :restrictByStatus is null or C1_REQUEST.BO_STATUS_CD=rpad(:restrictByStatus,12) )");
		retrieveAccountIds.append(" and C1_REQUEST.C1_REQ_ID=C1_REQUEST_REL_OBJ.C1_REQ_ID ");
		
		if(getParameters().getIsRestrictedByBatchControl().equals("true")){
			retrieveAccountIds.append(" and C1_REQUEST.BO_STATUS_CD in (select BO_STATUS_CD from F1_BUS_OBJ_STATUS where batch_cd=rpad(:batchCd,8) )");
		}
		
		
		pstmt=createPreparedStatement(retrieveAccountIds.toString(),"");
		
		pstmt.bindString("restrictByMO",getParameters().getMaintenanceObject(),"");
		pstmt.bindString("restrictByStatus", getParameters().getRestrictByStatus(),"");
		pstmt.bindString("restrictByRqType", getParameters().getRestrictByRequestType(),"");
		pstmt.bindString("restrictByBO", getParameters().getRestrictByBo(),"");
		
		if(getParameters().getIsRestrictedByBatchControl().equals("true")){
			pstmt.bindString("batchCd", getBatchControlId().getIdValue(),"");
		}
		
		List<SQLResultRow> resultSet=pstmt.list();
		for(SQLResultRow result:resultSet){
			accountId = result.getString("PK_VALUE1");
			threadWorkUnit = new ThreadWorkUnit();
			threadWorkUnit.setPrimaryId(new Account_Id(accountId));
			threadWorkUnitList.add(threadWorkUnit);
			}
		}
		catch(ApplicationError e){
			throw LoggedException.wrap("Application Error:", e);
		}
		finally {
			if(notNull(pstmt))
				pstmt.close();
		}
		return createJobWorkForThreadWorkUnitList(threadWorkUnitList);


	}

	public Class<CmRequestMonitorBatchWorker> getThreadWorkerClass() {
		return CmRequestMonitorBatchWorker.class;
	}

	public static class CmRequestMonitorBatchWorker extends
			CmRequestMonitorBatchWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			
			return new StandardCommitStrategy(this);
		}
		

		StringBuilder retrieveRequests=new StringBuilder();
		
		public void initializeThreadWork(boolean initializationPreviouslySuccessful)
				throws ThreadAbortedException, RunAbortedException {
			
			
			//CB-404 Add- Start
			
			 if(notNull(getParameters().getOrderByCharType())) {
	                retrieveRequests.append("select distinct C1_REQUEST.C1_REQ_ID as C1_REQ_ID, C1_REQUEST_CHAR.ADHOC_CHAR_VAL as CHAR_VAL, C1_REQUEST.BUS_OBJ_CD as BUS_OBJ_CD");
	                retrieveRequests.append(" from C1_REQUEST, C1_REQUEST_REL_OBJ, C1_REQUEST_CHAR");
	            } else {
	                retrieveRequests.append("select distinct C1_REQUEST.C1_REQ_ID as C1_REQ_ID, C1_REQUEST.BUS_OBJ_CD as BUS_OBJ_CD");
	                retrieveRequests.append(" from C1_REQUEST, C1_REQUEST_REL_OBJ");
	            }
	            retrieveRequests.append(" where C1_REQUEST.BUS_OBJ_CD in");
	            retrieveRequests.append(" ( select BUS_OBJ_CD FROM F1_BUS_OBJ where MAINT_OBJ_CD=rpad(:restrictByMO,12) )");
	            retrieveRequests.append(" and ( C1_REQUEST.BUS_OBJ_CD=rpad(:restrictByBO,30) or :restrictByBO is null )");
	            retrieveRequests.append(" and ( C1_REQUEST.C1_REQ_TYPE_CD=rpad(:restrictByRqType,30) or :restrictByRqType is null )");
	            retrieveRequests.append(" and ( C1_REQUEST.BO_STATUS_CD=rpad(:restrictByStatus,12) or :restrictByStatus is null )");            
	            
	            if(getParameters().getIsRestrictedByBatchControl().equals("true")){
	                retrieveRequests.append(" and C1_REQUEST.BO_STATUS_CD in (select BO_STATUS_CD from F1_BUS_OBJ_STATUS where BATCH_CD=rpad(:batchCd,8) )");
	            }
	            
	            retrieveRequests.append(" and C1_REQUEST.C1_REQ_ID=C1_REQUEST_REL_OBJ.C1_REQ_ID ");
	            
	            retrieveRequests.append(" and C1_REQUEST_REL_OBJ.PK_VALUE1=:accountId ");
	            
	            if(notNull(getParameters().getOrderByCharType())) {
	                retrieveRequests.append(" and C1_REQUEST_CHAR.C1_REQ_ID=C1_REQUEST.C1_REQ_ID ");
	                retrieveRequests.append(" and ( C1_REQUEST_CHAR.CHAR_TYPE_CD=rpad(:charTypeCode,8) ) ");
	                retrieveRequests.append(" order by C1_REQUEST_CHAR.ADHOC_CHAR_VAL ");
	            }
			
	          //CB-404 Add- End 
			
		}


		public boolean executeWorkUnit(ThreadWorkUnit unit)
				throws ThreadAbortedException, RunAbortedException {
			
			String requestId;
			String businessObject;
			String nextStatus;
			BusinessObjectInstance bOInstance;
			
			PreparedStatement pstmt=null;
			FrameworkSession newSession = (FrameworkSession) SessionHolder.getSession();
			Account_Id accountId=(Account_Id) unit.getPrimaryId();
			String accountIdVal = accountId.getIdValue();
			newSession.setSavepoint(accountIdVal);
			
			try{
				
				pstmt=createPreparedStatement(retrieveRequests.toString(),"");
				pstmt.bindString("restrictByMO",getParameters().getMaintenanceObject(),"");
				pstmt.bindString("restrictByStatus", getParameters().getRestrictByStatus(),"");
				pstmt.bindString("restrictByRqType", getParameters().getRestrictByRequestType(),"");
				pstmt.bindString("restrictByBO", getParameters().getRestrictByBo(),"");
				if(getParameters().getIsRestrictedByBatchControl().equals("true")){
					pstmt.bindString("batchCd", getBatchControlId().getIdValue(),"");
				}
				//CB-404 Add- Start
                if (notNull(getParameters().getOrderByCharType()))
                {
                    pstmt.bindString("charTypeCode", getParameters().getOrderByCharType(), "");
                }
                //CB-404 Add- End
				pstmt.bindString("accountId", accountIdVal,"");
	
				List<SQLResultRow> resultSet=pstmt.list();
				for(SQLResultRow result:resultSet){
					
					requestId = result.getString("C1_REQ_ID");
					businessObject = result.getString("BUS_OBJ_CD");
					bOInstance = BusinessObjectInstance.create(businessObject);
					bOInstance.set("request", requestId);				
					bOInstance=BusinessObjectDispatcher.read(bOInstance);
					
					nextStatus = getNextBoStatus(bOInstance.getString("boStatus"), businessObject.trim());
					if(!(isBlankOrNull(nextStatus))){
						bOInstance.set("boStatus", nextStatus);
					}
					bOInstance=BusinessObjectDispatcher.update(bOInstance);
					
				}
			}
			catch(ApplicationError e){
				logMessage(e.getServerMessage(),MessageSeverityLookup.constants.ERROR);
				newSession.rollbackToSavepoint(accountIdVal); 
				logError(e.getServerMessage());
				return false;
			}
			finally {
				if(notNull(pstmt))
					pstmt.close();
			}
			return true;
		}

		private String getNextBoStatus(String currStatus, String boName){
			PreparedStatement pstmt = null;
			String nextStatus = null;
			try{
				final StringBuilder fetchNextStatus = new StringBuilder("")
				.append("SELECT BO_NEXT_STATUS_CD as NEXTSTATUS FROM F1_BUS_OBJ_TR_RULE ")
				.append(" where BUS_OBJ_CD=rpad(:bo,30) and BO_STATUS_CD=rpad(:currentStatus,12) and DEFAULT_SW='Y'");
				
				pstmt = createPreparedStatement(fetchNextStatus.toString(),"getNextBoStatus");
				pstmt.bindString("currentStatus", currStatus, "");
				pstmt.bindString("bo", boName, "");
				SQLResultRow res = pstmt.firstRow();
				nextStatus = res.getString("NEXTSTATUS");
			}catch(ApplicationError e){
				throw LoggedException.wrap("Application Error:", e);
			}
			finally{
				if(notNull(pstmt))
					pstmt.close();
			}
	
			return nextStatus;
		}
		
	}

}
