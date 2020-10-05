/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * This Algorithm will Raise ToDo when a payment is being applied on a disputed transaction
 * 
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-08-11   KGhuge      Initial Version. 
 ***********************************************************************
 */

package com.splwg.cm.domain.payment.algorithm;

import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.ccb.domain.admin.customerClass.CustomerClassPayFreezeAlgorithmSpot;
import com.splwg.ccb.domain.admin.matchType.MatchType_Id;
import com.splwg.ccb.domain.payment.payment.Payment;
import com.splwg.cm.domain.payment.businessComponent.CmPaymentOnDisputedTransactionBusComp;

/**
 * @author KGhuge
 *
 *@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = matchType, name = billMatchType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = matchType, name = billSegMatchType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = requestCompletedStatusLst, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = toDoType, name = toDoType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = toDoRole, required = true, type = string)})
 */
public class CmPaymentOnDisputedTransactionAlgComp_Impl extends
		CmPaymentOnDisputedTransactionAlgComp_Gen implements
		CustomerClassPayFreezeAlgorithmSpot {
	//Hard Parameters
	Payment paymentObject = null;
	@Override
	public void invoke() {
		MatchType_Id matchTypeId = paymentObject.getMatchTypeId();
		String matchValue = paymentObject.getMatchValue().trim();
		StringBuilder checkMatchValQuery = new StringBuilder();
		checkMatchValQuery.append("SELECT ENTITY_ID FROM C1_DSPT_REQ_DTLS WHERE ENTITY_ID=:entityId ");
		PreparedStatement psmt = createPreparedStatement(checkMatchValQuery.toString(),"");
		psmt.bindString("entityId",matchValue,"ENTITY_ID");
		SQLResultRow firstRow = psmt.firstRow();
		if(notNull(firstRow)){
			CmPaymentOnDisputedTransactionBusComp createToDo = CmPaymentOnDisputedTransactionBusComp.Factory.newInstance();
			if((matchTypeId.equals(this.getBillMatchType().getId()) && checkEntryExists(matchValue,"BLID")) ||(matchTypeId.equals(this.getBillSegMatchType().getId()) && checkEntryExists(matchValue,"BSID"))){	
					createToDo.createToDoEntry(paymentObject.getId().getTrimmedValue(), paymentObject.getAccount().getId().getTrimmedValue(), this.getToDoType().getId().getTrimmedValue(), this.getToDoRole());
			}
		}
	}
	
	/*
	 * This method Check if the Match Value has an entry in the Dispute Request Details table or not
	 * @param matchValue
	 * @param entityType
	 * @return boolean Value
	 * */
	private boolean checkEntryExists(String matchValue,String entityType){
		StringBuilder checkEntryQuery = new StringBuilder();
		PreparedStatement checkEntryStatement = null;
		//BSID
		String []statusList = this.getRequestCompletedStatusLst().split(",");
		checkEntryQuery.append(" SELECT A.DSPT_DTL_ID FROM C1_DSPT_REQ_DTLS A, C1_DSPT_REQ B WHERE ");
		checkEntryQuery.append(" A.DSPT_REQ_ID = B.DSPT_REQ_ID AND A.ENTITY_TYPE=:entityType AND A.ENTITY_ID=:entityId ");
		checkEntryQuery.append(" AND B.BO_STATUS_CD NOT IN( ");
		for(int i=0;i<statusList.length;i++){
			if(i==0){
				checkEntryQuery.append(":status"+i);
			}else{
				checkEntryQuery.append(",:status"+i);
			}
		}
		checkEntryQuery.append(" )");
		checkEntryStatement = createPreparedStatement(checkEntryQuery.toString(),"Check Match Type has Entry in Dispute Request Details Table");
		checkEntryStatement.bindString("entityId",matchValue,"ENTITY_ID");
		checkEntryStatement.bindString("entityType", entityType, "ENTITY_TYPE");
		for(int i=0;i<statusList.length;i++){
			checkEntryStatement.bindString("status"+i,statusList[i] ,"BO_STATUS_CD");
		}
		SQLResultRow firstRow = checkEntryStatement.firstRow();
		checkEntryStatement.close();
		if(isNull(firstRow)){
			return false;
		}else{
			return true;
		}
	}

	@Override
	public void setPayment(Payment paymentObject) {
		this.paymentObject = paymentObject;
	}
}
