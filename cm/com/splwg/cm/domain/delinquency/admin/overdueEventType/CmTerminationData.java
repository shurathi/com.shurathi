/*                                                               
 ***************************************************************************************************                                                             
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Define Data file to determine termination date 
 *                                                             
 ***************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 ***************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.overdueEventType;

import java.math.BigInteger;

import com.ibm.icu.math.BigDecimal;

public class CmTerminationData {
	// initializing variables
	private String terminationEffectiveRule;
	private BigDecimal retroPaidPercentageThreshold;
	private BigInteger dateofProcessingNumberofDaysPrior;

	/**
	 * This method fetches stored Effective termination rule
	 * @return
	 */
	public String getTerminationEffectiveRule() {
		return terminationEffectiveRule;
	}

	/**
	 * This method set value for effective termination rule 
	 * @param terminationEffectiveRule
	 */
	public void setTerminationEffectiveRule(String terminationEffectiveRule) {
		this.terminationEffectiveRule = terminationEffectiveRule;
	}

	/**
	 * This Method retrieves stored retro paid percentage threshold
	 * @return
	 */
	public BigDecimal getRetroPaidPercentageThreshold() {
		return retroPaidPercentageThreshold;
	}

	/**
	 * This method set retro paid percentage threshold
	 * @param retroPaidPercentageThreshold
	 */
	public void setRetroPaidPercentageThreshold(BigDecimal retroPaidPercentageThreshold) {
		this.retroPaidPercentageThreshold = retroPaidPercentageThreshold;
	}

	/**
	 * This method retrieve dateofProcessingNumberofDaysPrior
	 * @return
	 */
	public BigInteger getDateofProcessingNumberofDaysPrior() {
		return dateofProcessingNumberofDaysPrior;
	}

	/**
	 * This method sets value for dateofProcessingNumberofDaysPrior
	 * @param dateofProcessingNumberofDaysPrior
	 */
	public void setDateofProcessingNumberofDaysPrior(BigInteger dateofProcessingNumberofDaysPrior) {
		this.dateofProcessingNumberofDaysPrior = dateofProcessingNumberofDaysPrior;
	}

}

