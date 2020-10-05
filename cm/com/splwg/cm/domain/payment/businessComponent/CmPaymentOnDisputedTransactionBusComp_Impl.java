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
 * This Business Component will create To Do Entry
 * 
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-08-11   KGhuge      Initial Version. 
 ***********************************************************************
 */
package com.splwg.cm.domain.payment.businessComponent;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.lookup.SendToLookup;

/**
 * @author KGhuge
 *
@BusinessComponent ()
 */
public class CmPaymentOnDisputedTransactionBusComp_Impl extends
		GenericBusinessComponent implements
		CmPaymentOnDisputedTransactionBusComp {
	public void createToDoEntry(String drillKey, String sortKey, String toDoType, String toDoRole){
		BusinessServiceInstance businessService = BusinessServiceInstance.create("F1-AddToDoEntry");
		businessService.set("toDoType",toDoType);
		businessService.set("drillKey1", drillKey);
		businessService.set("sortKey1",sortKey);
		businessService.set("sendTo",SendToLookup.constants.ROLE);
		businessService.set("toDoRole", toDoRole);
		businessService.set("subject","Raise Todo when a payment is being applied on a disputed transaction");
		businessService.set("messageParm1",drillKey);
		BusinessServiceDispatcher.execute(businessService);
	}
}
