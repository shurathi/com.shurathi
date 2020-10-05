/*
 ************************************************************************** *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Customer Monitor Batch
 * 
 * This batch process monitors Customers with collection classes designated 
 * as Delinquency Process. It selects customers based on several criteria 
 * and passes the Person ID to the Delinquency Monitor Rule plug-in(s) 
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
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
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
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyControlMonitorAlgorithmSpot;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmPersonCollection;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmPersonCollection_DTO;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmPersonCollection_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@BatchJob (rerunnable = false,
 *      modules = { "demo"})
 */
public class CmDelinquencyCustomerMonitorBatch extends CmDelinquencyCustomerMonitorBatch_Gen {


	public Class<CmDelinquencyCustomerMonitorBatchWorker> getThreadWorkerClass() {
		return CmDelinquencyCustomerMonitorBatchWorker.class;
	}

	public static class CmDelinquencyCustomerMonitorBatchWorker extends CmDelinquencyCustomerMonitorBatchWorker_Gen {
		public ThreadExecutionStrategy createExecutionStrategy() {
			return new ThreadIterationStrategy(this);
		}
		
		public void initializeThreadWork(boolean initializationPreviouslySuccessful)
				throws ThreadAbortedException, RunAbortedException {
			
			startResultRowQueryIteratorForThread(Person_Id.class);
		}
		
		@Override
		public ThreadWorkUnit getNextWorkUnit(QueryResultRow row)
		{
			ThreadWorkUnit workUnit = new ThreadWorkUnit();
			workUnit.setPrimaryId(row.getId("personId", Person.class));
			return workUnit;
		}

		
		@SuppressWarnings("deprecation")
		protected QueryIterator<Person_Id> getQueryIteratorForThread(@SuppressWarnings("rawtypes") SingleFieldId lowId, @SuppressWarnings("rawtypes") SingleFieldId highId)
		{
			
			Query<Person_Id> query1 = createQuery(CmDelinquencyCustomerMonitorBatchConstants.FETCH_ELIGIBLE_CUSTOMERS1.toString(), "getQueryIteratorForThread");
			query1.bindDate("batchProcessDate", getParameters().getProcessDate());
			query1.bindId("lowId", lowId);
			query1.bindId("highId", highId);
			query1.addResult("personId", "CRS.id.person.id");
			
			Query<Person_Id> query2 = createQuery(CmDelinquencyCustomerMonitorBatchConstants.FETCH_ELIGIBLE_CUSTOMERS2.toString(), "getQueryIteratorForThread");
			query2.bindDate("batchProcessDate", getParameters().getProcessDate());
			query2.bindId("lowId", lowId);
			query2.bindId("highId", highId);
			query2.addResult("personId", "PC.id.person.id");
			
			UnionQuery<Person_Id> unionQuery = query1.unionWith(query2);
			unionQuery.selectDistinct(true);
			unionQuery.orderBy("personId", Query.ASCENDING);

			return unionQuery.iterate();
		}



		public WorkUnitResult executeWorkUnitDetailedResult(ThreadWorkUnit unit) throws ThreadAbortedException, RunAbortedException {
			// TODO Auto-generated method stub
			WorkUnitResult results = new WorkUnitResult(true);
			Person_Id personId = (Person_Id) unit.getPrimaryId();
			CmPersonCollection_Id perCollectionId = new CmPersonCollection_Id(personId);
			CollectionClass_Id collectionClassId = perCollectionId.getEntity().getCollectionClass().getId();

			Query<QueryResultRow> query = createQuery(CmDelinquencyCustomerMonitorBatchConstants.FETCH_ALL_DELINQ_CNTRL_ALGOS_FOR_GIVEN_COLLECT_CLASS.toString(), "CmDelinquencyCustomerMonitorBatch");
			query.bindEntity("collectionClassId", collectionClassId.getEntity());
			query.addResult("sequence", "DCA.id.sequence");
			query.addResult("algorithm", "DCA.algorithm");
			query.orderBy("sequence", Query.ASCENDING);
			List<QueryResultRow> algorithmsList = query.list();
			if (algorithmsList.isEmpty()) {
				addError(MessageRepository.delinquencyAlgorithmsNotConfigured(collectionClassId.getIdValue()));
			}
			for (QueryResultRow row : algorithmsList)
			{
				Algorithm algorithm = row.getEntity("algorithm", Algorithm.class);
				CmDelinquencyControlMonitorAlgorithmSpot algorithmSpot = AlgorithmComponentCache.getAlgorithmComponent(algorithm.getId(), CmDelinquencyControlMonitorAlgorithmSpot.class);
				algorithmSpot.setPersonId(personId);
				algorithmSpot.invoke();
				Bool isProcessingComplete = algorithmSpot.getProcessingCompletedSwitch();
				if (isProcessingComplete.isTrue()) {
					CmPersonCollection_Id perCollId = new CmPersonCollection_Id(personId);
					CmPersonCollection personCollection = perCollId.getEntity();
					CmPersonCollection_DTO perCollDTO = personCollection.getDTO();
					perCollDTO.setLastCreditReviewDate(getProcessDateTime().getDate());
					personCollection.setDTO(perCollDTO);
				}
			}
			return results;
		}

	}

}

