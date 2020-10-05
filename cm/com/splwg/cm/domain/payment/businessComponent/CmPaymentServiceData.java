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
 * Payment Service Data POJO
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-06-02   DDejes      Initial Version. 
 * 2020-07-30	IGarg			   Updated Version for CB-145
 ***********************************************************************
 */
package com.splwg.cm.domain.payment.businessComponent;

import com.splwg.base.api.datatypes.Money;


public class CmPaymentServiceData {

	private String matchType;
	private String matchValue;
	private String matchEntityType;
	private Money paymentAmount;
	private String entityFlag;
	private String matchEntityId;
	private String fkValue;
	private String accountId; // Added By Ishita - CB 145
	
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getMatchType(){
		return matchType;
	}
	
	public void setMatchType(String matchType){
		this.matchType = matchType;
	}
	
	public String getMatchValue(){
		return matchValue;
	}
	
	public void setMatchValue(String matchValue){
		this.matchValue = matchValue;
	}
	
	public String getMatchEntityType(){
		return matchEntityType;
	}
	
	public void setMatchEntityType(String matchEntityType){
		this.matchEntityType = matchEntityType;
	}
	
	public Money getPaymentAmount(){
		return paymentAmount;
	}
	
	public void setPaymentAmount(Money paymentAmount){
		this.paymentAmount = paymentAmount;
	}
	
	public String getEntityFlag(){
		return entityFlag;
	}
	
	public void setEntityFlag(String entityFlag){
		this.entityFlag = entityFlag;
	}
	
	public String getMatchEntityId(){
		return matchEntityId;
	}
	
	public void setMatchEntityId(String matchEntityId){
		this.matchEntityId = matchEntityId;
	}
	
	public String getFkValue(){
		return fkValue;
	}
	
	public void setFkValue(String fkValue){
		this.fkValue = fkValue;
	}
	
}
