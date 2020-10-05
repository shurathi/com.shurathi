/*                                                                
 ************************************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 * The Cancel Criteria algorithm will cancel the Overdue Process for an Account
 * if the Bill or bills is or are paid within the tolerance limit or the Customers status is 
 * changed from one state to another
 * This business component helper class will contain routines for cancel criteria logic                                   
 *                                     
 ************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 ************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.overdueProcessTemplate;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.Query;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.ccb.api.lookup.CustomerManagementOptionLookup;
import com.splwg.ccb.domain.customerinfo.account.Account;

/**
 * @author VINODW
 *
@BusinessComponent (customizationCallable = true)
 */
public class CmOverdueProcessHelper_Impl extends GenericBusinessComponent implements CmOverdueProcessHelper {
	/**
	 * This method get the total billed amount for the current revenue period
	 * @param latestDueDate
	 * @param account
	 * @return amount in BigDecimal format
	 */
	public BigDecimal fetchTotalBilledAmountForCurrentRevenuePeriod(Date latestDueDate, Account account) {

		// Query<Money> query = createQuery(CmCancelCriteriaConstants.TOTAL_BILLED_AMT_QUERY.toString(), "CmOverdueProcessHelper_Impl");
		Query<Money> query = createQuery(CmCancelCriteriaConstants.TOTAL_BILLED_AMT_QUERY.toString(), "CmOverdueProcessHelper_Impl");

		query.bindEntity("account", account);
		query.bindDate("latestDueDate", latestDueDate);
		query.addResult("amount", "SUM(FT.currentAmount)");
		return query.firstRow().getAmount();
	}
	
	/**
	 * This method retrieves on account payments for contracts configured in feature configuration
	 * @param account
	 * @param adminstrativeContractTypeFeatureConfig
	 * @param adminstrativeContractTypeOptionType
	 * @return on account payments in BugDecimal format
	 */
	public BigDecimal fetchOnAccountpayments(Account account, FeatureConfiguration adminstrativeContractTypeFeatureConfig,
			CustomerManagementOptionLookup adminstrativeContractTypeOptionType) {

		// Query<Money> query = createQuery(CmCancelCriteriaConstants.ON_ACCOUNT_PAYMENTS_QUERY.toString(), "CmOverdueProcessHelper_Impl");
		Query<Money> query = createQuery(CmCancelCriteriaConstants.ON_ACCOUNT_PAYMENTS_QUERY.toString(), "CmOverdueProcessHelper_Impl");

		query.bindEntity("account", account);
		query.bindEntity("adminContrFeatureConfig", adminstrativeContractTypeFeatureConfig);
		query.bindLookup("adminContrOptionType", adminstrativeContractTypeOptionType);
		query.addResult("amount", "SUM(FT.currentAmount)");
		return query.firstRow().getAmount();
	}

	
}

