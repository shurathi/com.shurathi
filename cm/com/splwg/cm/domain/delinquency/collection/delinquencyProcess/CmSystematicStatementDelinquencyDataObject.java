/*                                                             
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This Delinquency Monitor Rule algorithm will determine if a customers
 * debt is overdue based on age and amount thresholds (including Amount 
 * and some percent of Unpaid amount with respect to original amount). 
 * It will also check that a customer is not already referred to CDM before 
 * creating a Delinquency process for sending systematic statements to the
 * customer.
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.       
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework                  
 *         
 * **************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess;

import java.math.BigInteger;
import java.util.ArrayList;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.ccb.api.lookup.CustomerManagementOptionLookup;
import com.splwg.ccb.domain.admin.idType.accountIdType.AccountNumberType;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CmCustomerManagementOptionsLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;

public class CmSystematicStatementDelinquencyDataObject {
	private BigInteger age;
	private YesNoOptionLookup unpaidAmtAndPrctRequired;
	private BigDecimal unpaidAmount;
	private BigDecimal unpaidPercentage;
	private CmDelinquencyProcessType delinquencyProcessType;
	private Person_Id personId;
	private AccountNumberType accountNumberIdType;
	private YesNoOptionLookup includeOnAccountPayments;
	private FeatureConfiguration adminstrativeContractTypeFeatureConfig;
	private CustomerManagementOptionLookup adminstrativeContractTypeOptionType;
	private YesNoOptionLookup useCurrentRevenuePeriodBilled;
	private ArrayList<String> accountIdList;
	private String billGroup;
	/**
	 * @return the age
	 */
	public BigInteger getAge() {
		return age;
	}
	/**
	 * @param age the age to set
	 */
	public void setAge(BigInteger age) {
		this.age = age;
	}
	/**
	 * @return the unpaidAmtAndPrctRequired
	 */
	public YesNoOptionLookup getUnpaidAmtAndPrctRequired() {
		return unpaidAmtAndPrctRequired;
	}
	/**
	 * @param unpaidAmtAndPrctRequired the unpaidAmtAndPrctRequired to set
	 */
	public void setUnpaidAmtAndPrctRequired(
			YesNoOptionLookup unpaidAmtAndPrctRequired) {
		this.unpaidAmtAndPrctRequired = unpaidAmtAndPrctRequired;
	}
	/**
	 * @return the unpaidAmount
	 */
	public BigDecimal getUnpaidAmount() {
		return unpaidAmount;
	}
	/**
	 * @param unpaidAmount the unpaidAmount to set
	 */
	public void setUnpaidAmount(BigDecimal unpaidAmount) {
		this.unpaidAmount = unpaidAmount;
	}
	/**
	 * @return the unpaidPercentage
	 */
	public BigDecimal getUnpaidPercentage() {
		return unpaidPercentage;
	}
	/**
	 * @param unpaidPercentage the unpaidPercentage to set
	 */
	public void setUnpaidPercentage(BigDecimal unpaidPercentage) {
		this.unpaidPercentage = unpaidPercentage;
	}
	/**
	 * @return the delinquencyProcessType
	 */
	public CmDelinquencyProcessType getDelinquencyProcessType() {
		return delinquencyProcessType;
	}
	/**
	 * @param delinquencyProcessType the delinquencyProcessType to set
	 */
	public void setDelinquencyProcessType(
			CmDelinquencyProcessType delinquencyProcessType) {
		this.delinquencyProcessType = delinquencyProcessType;
	}
	/**
	 * @return the personId
	 */
	public Person_Id getPersonId() {
		return personId;
	}
	/**
	 * @param personId the personId to set
	 */
	public void setPersonId(Person_Id personId) {
		this.personId = personId;
	}
	/**
	 * @return the accountNumberIdType
	 */
	public AccountNumberType getAccountNumberIdType() {
		return accountNumberIdType;
	}
	/**
	 * @param accountNumberIdType the accountNumberIdType to set
	 */
	public void setAccountNumberIdType(AccountNumberType accountNumberIdType) {
		this.accountNumberIdType = accountNumberIdType;
	}
	/**
	 * @return the includeOnAccountPayments
	 */
	public YesNoOptionLookup getIncludeOnAccountPayments() {
		return includeOnAccountPayments;
	}
	/**
	 * @param includeOnAccountPayments the includeOnAccountPayments to set
	 */
	public void setIncludeOnAccountPayments(
			YesNoOptionLookup includeOnAccountPayments) {
		this.includeOnAccountPayments = includeOnAccountPayments;
	}
	/**
	 * @return the adminstrativeContractTypeFeatureConfig
	 */
	public FeatureConfiguration getAdminstrativeContractTypeFeatureConfig() {
		return adminstrativeContractTypeFeatureConfig;
	}
	/**
	 * @param adminstrativeContractTypeFeatureConfig the adminstrativeContractTypeFeatureConfig to set
	 */
	public void setAdminstrativeContractTypeFeatureConfig(
			FeatureConfiguration adminstrativeContractTypeFeatureConfig) {
		this.adminstrativeContractTypeFeatureConfig = adminstrativeContractTypeFeatureConfig;
	}
	/**
	 * @return the adminstrativeContractTypeOptionType
	 */
	public CustomerManagementOptionLookup getAdminstrativeContractTypeOptionType() {
		return adminstrativeContractTypeOptionType;
	}
	/**
	 * @param adminstrativeContractTypeOptionType the adminstrativeContractTypeOptionType to set
	 */
	public void setAdminstrativeContractTypeOptionType(
			CustomerManagementOptionLookup adminstrativeContractTypeOptionType) {
		this.adminstrativeContractTypeOptionType = adminstrativeContractTypeOptionType;
	}
	/**
	 * @return the useCurrentRevenuePeriodBilled
	 */
	public YesNoOptionLookup getUseCurrentRevenuePeriodBilled() {
		return useCurrentRevenuePeriodBilled;
	}
	/**
	 * @param useCurrentRevenuePeriodBilled the useCurrentRevenuePeriodBilled to set
	 */
	public void setUseCurrentRevenuePeriodBilled(
			YesNoOptionLookup useCurrentRevenuePeriodBilled) {
		this.useCurrentRevenuePeriodBilled = useCurrentRevenuePeriodBilled;
	}
	/**
	 * @return the accountIdList
	 */
	public ArrayList<String> getAccountIdList() {
		return accountIdList;
	}
	/**
	 * @param accountIdList the accountIdList to set
	 */
	public void setAccountIdList(ArrayList<String> accountIdList) {
		this.accountIdList = accountIdList;
	}
	/**
	 * @return the billGroup
	 */
	public String getBillGroup() {
		return billGroup;
	}
	/**
	 * @param billGroup the billGroup to set
	 */
	public void setBillGroup(String billGroup) {
		this.billGroup = billGroup;
	}
}
