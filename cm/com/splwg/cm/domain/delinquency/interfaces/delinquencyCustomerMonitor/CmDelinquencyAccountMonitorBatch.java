/*
 ************************************************************************** *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Account Monitor Batch
 * 
 * This batch process monitors Accounts with collection classes designated 
 * as Delinquency Process. It selects accounts based on several criteria 
 * and passes the Account ID to the Delinquency Monitor Rule plug-in(s) 
 * that are plugged-in on the Delinquency Control. 
 * 
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-08-17   Ishita Garg		Initial version
 * 2020-08-20	Ishita Garg	    Updated Version
 * **************************************************************************
 */

package com.splwg.cm.domain.delinquency.interfaces.delinquencyCustomerMonitor;

import java.util.List;

import com.splwg.base.api.Query;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.UnionQuery;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadIterationStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.batch.WorkUnitResult;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.SingleFieldId;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.ccb.domain.admin.collectionClass.CollectionClass_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_DTO;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CollectionMethodLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyControlMonitorAlgorithmSpot;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmDelinquencyControl;

/**
 * @author IshitaGarg
 *
@BatchJob (rerunnable = false,
 *      modules = { "demo"})
 */
public class CmDelinquencyAccountMonitorBatch extends
		CmDelinquencyAccountMonitorBatch_Gen {

	public Class<CmDelinquencyAccountMonitorBatchWorker> getThreadWorkerClass() {
		return CmDelinquencyAccountMonitorBatchWorker.class;
	}

	public static class CmDelinquencyAccountMonitorBatchWorker extends
			CmDelinquencyAccountMonitorBatchWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			// TODO Auto-generated method stub
			return new ThreadIterationStrategy(this);
		}
		
		public void initializeThreadWork(boolean initializationPreviouslySuccessful)
				throws ThreadAbortedException, RunAbortedException {
			
			startResultRowQueryIteratorForThread(Account_Id.class);
		}
		
		@Override
		public ThreadWorkUnit getNextWorkUnit(QueryResultRow row)
		{
			ThreadWorkUnit workUnit = new ThreadWorkUnit();
			workUnit.setPrimaryId(row.getId("accountId", Account.class));
			return workUnit;
		}
		
		@SuppressWarnings("deprecation")
		protected QueryIterator<Account_Id> getQueryIteratorForThread(@SuppressWarnings("rawtypes") SingleFieldId lowId, @SuppressWarnings("rawtypes") SingleFieldId highId)
		{
			StringBuilder fetchAccounts = new StringBuilder();
			fetchAccounts.append(" from Account ACCT, CollectionClass CL, CmDelinquencyControl DC  ");
			fetchAccounts.append(" WHERE CL.id <= ACCT.collectionClassId ");
			fetchAccounts.append(" AND CL.collectionMethod = :collectionMethod ");
			fetchAccounts.append(" AND (ACCT.postponeCreditReviewUntil IS NULL "
					+ "OR (ACCT.postponeCreditReviewUntil IS NOT NULL "
					+ "AND ACCT.postponeCreditReviewUntil <= :processDate)"
					+ "	) ");
			fetchAccounts.append(" AND ACCT.collectionClassId = DC.id.collectionClass.id ");
			fetchAccounts.append(" AND ((EXISTS (SELECT SCH.id.account.id "
												+ "FROM CreditReviewSchedule SCH "
												+ "WHERE SCH.id.account.id = ACCT.id "
												+ "AND SCH.id.nextCreditReviewDate <= :processDate "
												+ ") "
										+ ") ");
				fetchAccounts.append("OR ( ACCT.lastCreditReviewDate IS NULL OR (ACCT.lastCreditReviewDate + DC.minCreditReviewFreq) <= :processDate) ");
				fetchAccounts.append(")");
				fetchAccounts.append(" AND ACCT.id BETWEEN :lowId AND :highId ");
			Query<Account_Id> query1 = createQuery(fetchAccounts.toString(), "getQueryIteratorForThread");
			query1.bindLookup("collectionMethod",CollectionMethodLookup.constants.CM_DELINQUENCY_PROCESS);
			query1.bindDate("processDate", getParameters().getProcessDate());
			query1.bindId("lowId", lowId);
			query1.bindId("highId", highId);
			query1.addResult("accountId", "ACCT.id");
			query1.orderBy("accountId", Query.ASCENDING);

			return query1.iterate();
		}
		
		public WorkUnitResult executeWorkUnitDetailedResult(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			// TODO Auto-generated method stub
			WorkUnitResult results = new WorkUnitResult(true);
			Account_Id accountId = (Account_Id) unit.getPrimaryId();
			CollectionClass_Id collectionClassId = accountId.getEntity().getCollectionClassId();

			Query<QueryResultRow> query = createQuery(CmDelinquencyCustomerMonitorBatchConstants.FETCH_ALL_DELINQ_CNTRL_ALGOS_FOR_GIVEN_COLLECT_CLASS.toString(), "CmDelinquencyCustomerMonitorBatch");
			query.bindEntity("collectionClassId", collectionClassId.getEntity());
			query.addResult("sequence", "DCA.id.sequence");
			query.addResult("algorithm", "DCA.algorithm");
			query.orderBy("sequence", Query.ASCENDING);
			List<QueryResultRow> algorithmsList = query.list();
			/*if (algorithmsList.isEmpty()) {
				addError(MessageRepository.delinquencyAlgorithmsNotConfigured(collectionClassId.getIdValue()));
			}*/
			for (QueryResultRow row : algorithmsList)
			{
				Algorithm algorithm = row.getEntity("algorithm", Algorithm.class);
				CmDelinquencyControlMonitorAlgorithmSpot algorithmSpot = AlgorithmComponentCache.getAlgorithmComponent(algorithm.getId(), CmDelinquencyControlMonitorAlgorithmSpot.class);
				algorithmSpot.setAccountId(accountId);
				algorithmSpot.invoke();
				Bool isProcessingComplete = algorithmSpot.getProcessingCompletedSwitch();
				if (isProcessingComplete.isTrue()) {
					Account_DTO accountDTO = accountId.getEntity().getDTO();
					accountDTO.setLastCreditReviewDate(getProcessDateTime().getDate());
					accountId.getEntity().setDTO(accountDTO);
				}
			}
			return results;
		}

	}

}
