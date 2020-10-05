/*
 **************************************************************************                                                      
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Monitor Rule - Delinquency control plug-in spot 
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
package com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor;

import com.splwg.base.api.algorithms.AlgorithmSpot;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;

/**
 * @author MugdhaP
 *
@AlgorithmSpot (algorithmEntityValues = { "cmDelinquencyMonitorRule"})
 */
public interface CmDelinquencyControlMonitorAlgorithmSpot extends AlgorithmSpot {

	public abstract void setAccountId(Account_Id accountId);
	
	public abstract void setPersonId(Person_Id personId);

	public abstract Bool getProcessingCompletedSwitch();

}
