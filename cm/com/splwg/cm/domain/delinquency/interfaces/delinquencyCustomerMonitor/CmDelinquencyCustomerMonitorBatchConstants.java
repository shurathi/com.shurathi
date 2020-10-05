/*
 ************************************************************************************************************
 *                                                                
 * Copyright (c) 2000, 2012, Oracle. All rights reserved.        
 *                                                                
 ************************************************************************************************************                                                              
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Constant File for Delinquency Customer Monitor Batch
 *                                                             
 ************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * **********************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.interfaces.delinquencyCustomerMonitor;

/**
 * @author MugdhaP
 *
 */
public class CmDelinquencyCustomerMonitorBatchConstants {
	public final static StringBuilder FETCH_ELIGIBLE_CUSTOMERS1 = new StringBuilder()
			.append(" from CmCustomerReviewSchedule CRS, CmPersonCollection PC, CollectionClass CL ")
			.append(" WHERE (CRS.id.nextCreditReviewDate <= :batchProcessDate ")
			.append(" OR CRS.id.nextCreditReviewDate IS NULL) ")
			.append(" AND (PC.postponeCreditReviewUntil IS NULL OR PC.postponeCreditReviewUntil <= :batchProcessDate) ")
			.append(" AND CRS.id.person.id = PC.id.person.id ")
			.append(" AND PC.collectionClass = CL.id ")
			.append(" AND CL.collectionMethod <> 'NALW' AND PC.id.person.id BETWEEN :lowId AND :highId ");

	public final static StringBuilder FETCH_ELIGIBLE_CUSTOMERS2 = new StringBuilder()
			.append(" from CmPersonCollection PC, CmDelinquencyControl DC, CollectionClass CL ")
			.append(" where DC.id.collectionClass = PC.collectionClass ")
			.append(" AND (PC.postponeCreditReviewUntil IS NULL OR PC.postponeCreditReviewUntil <= :batchProcessDate) ")
			.append(" AND ((PC.lastCreditReviewDate + DC.minCreditReviewFreq) IS NULL OR (PC.lastCreditReviewDate + DC.minCreditReviewFreq) <= :batchProcessDate) ")
			.append(" AND PC.collectionClass = CL.id ")
			.append(" AND CL.collectionMethod <> 'NALW' AND PC.id.person.id BETWEEN :lowId AND :highId ");
	
	public final static StringBuilder FETCH_ALL_DELINQ_CNTRL_ALGOS_FOR_GIVEN_COLLECT_CLASS = new StringBuilder()
			.append(" FROM CmDelinquencyControlAlgorithm DCA ")
			.append(" WHERE DCA.id.collectionClass = :collectionClassId ");

	public final static StringBuilder FETCH_CUSTOMERS_NOT_MONITORED = new StringBuilder()
			.append(" SELECT PER_ID, COLL_CL_CD FROM ( SELECT DISTINCT PC.PER_ID PER_ID, (PC.CR_REVIEW_DT + DC.CM_MIN_CR_RVW_FREQ) CALC_DATE, PC.COLL_CL_CD COLL_CL_CD ")
			.append(" FROM CM_PER_COLL PC, CM_DELIN_CNTL DC WHERE DC.COLL_CL_CD = PC.COLL_CL_CD AND (PC.POSTPONE_CR_RVW_DT IS NULL OR PC.POSTPONE_CR_RVW_DT <= :batchProcessDate) ")
			.append(" AND PC.CR_REVIEW_DT IS NOT NULL) WHERE CALC_DATE <= :batchProcessDate ");

}

