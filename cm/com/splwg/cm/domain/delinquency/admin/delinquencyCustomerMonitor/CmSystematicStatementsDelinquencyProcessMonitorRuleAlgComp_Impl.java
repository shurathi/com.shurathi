/*                                                              
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This Delinquency Monitor Rule algorithm will determine if a customers
 * debt is overdue based on age and amount thresholds (including  Amount 
 * and some percentage of Unpaid amount with respect to original amount). 
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
 * 2020-05-06   MugdhaP		ANTHM-340 CAB1-9462 Delinquency Framework                      
 *          
 * **************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.CmSystematicStatementDelinquencyDataObject;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.CmSystematicStatementDelinquencyHelper;
import com.splwg.cm.domain.delinquency.utils.CmPolicyHelper;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = limitingCharacteristicEntity, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = limitingCharacteristicType, type = entity)
 *            , @AlgorithmSoftParameter (name = limitingCharacteristicValue, type = string)
 *            , @AlgorithmSoftParameter (name = limitingCharEffectiveWithinXDays, type = integer)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = band1UnpaidAmountAndPercentage, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (name = band1ProcessBillsWithAge, required = true, type = integer)
 *            , @AlgorithmSoftParameter (name = band1ProcessBillsWithUnpaidAmount, type = decimal)
 *            , @AlgorithmSoftParameter (name = band1ProcessBillsWithUnpaidPercentage, type = decimal)
 *            , @AlgorithmSoftParameter (name = band1CustomerStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = cmDelinquencyProcessType, name = band1DelinquencyProcessType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = band2UnpaidAmountAndPercentage, type = lookup)
 *            , @AlgorithmSoftParameter (name = band2ProcessBillsWithAge, type = integer)
 *            , @AlgorithmSoftParameter (name = band2ProcessBillsWithUnpaidAmount, type = decimal)
 *            , @AlgorithmSoftParameter (name = band2ProcessBillsWithUnpaidPercentage, type = decimal)
 *            , @AlgorithmSoftParameter (name = band2CustomerStatus, type = string)
 *            , @AlgorithmSoftParameter (entityName = cmDelinquencyProcessType, name = band2DelinquencyProcessType, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = band3UnpaidAmountAndPercentage, type = lookup)
 *            , @AlgorithmSoftParameter (name = band3ProcessBillsWithAge, type = integer)
 *            , @AlgorithmSoftParameter (name = band3ProcessBillsWithUnpaidAmount, type = decimal)
 *            , @AlgorithmSoftParameter (name = band3ProcessBillsWithUnpaidPercentage, type = decimal)
 *            , @AlgorithmSoftParameter (name = band3CustomerStatus, type = string)
 *            , @AlgorithmSoftParameter (entityName = cmDelinquencyProcessType, name = band3DelinquencyProcessType, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = includeOnAccountPayments, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = adminstrativeContractTypeFeatureConfig, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = adminstrativeContractTypeOptionType, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = useCurrentRevenuePeriodBilled, type = lookup)
 *            , @AlgorithmSoftParameter (name = custStatusOptionType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = custStatusOptionValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = billGroupCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = cdmReferralCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = cdmReferralCharacteristicValueList, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = accountNumberType, name = accountNumberType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = systematicStatementDay, required = true, type = integer)})
 */
public class CmSystematicStatementsDelinquencyProcessMonitorRuleAlgComp_Impl
		extends CmSystematicStatementsDelinquencyProcessMonitorRuleAlgComp_Gen
		implements CmDelinquencyControlMonitorAlgorithmSpot {
	
	private Person_Id personId;
	private Bool isProcessingCompleted;
	
	@Override
	public void setPersonId(Person_Id personId) {
		this.personId = personId;
	}

	@Override
	public Bool getProcessingCompletedSwitch() {
		return isProcessingCompleted;
	}
	

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// Validate include on account payments is Y or N
		if (notNull(getIncludeOnAccountPayments()) && !(getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())
				|| getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue()))) {

			addError(MessageRepository.invalidUnpaidAmtandPer(
							getAlgorithm().getAlgorithmType().getParameterAt(22).fetchLanguageParameterLabel(),
							getIncludeOnAccountPayments().trimmedValue()));
		}

		// if on account payments is Y validates feature configuration and
		// option type
		if (notNull(getIncludeOnAccountPayments()) && getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {
			if (isNull(getAdminstrativeContractTypeFeatureConfig()))
				reportRequiredParameter("adminstrativeContractTypeFeatureConfig", 23);
			if (isNull(getAdminstrativeContractTypeOptionType()))
				reportRequiredParameter("adminstrativeContractTypeOptionType", 24);

		}

		// Validate Use current Revenue period is Y or N
		if (notNull(getUseCurrentRevenuePeriodBilled()) && !(getUseCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())
				|| getUseCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue()))) {
			addError(MessageRepository.invalidUnpaidAmtandPer(
							getAlgorithm().getAlgorithmType().getParameterAt(25).fetchLanguageParameterLabel(),
							getUseCurrentRevenuePeriodBilled().trimmedValue()));
		}
		
		// Validate if Systematic Statement Day is a number between 1 and 28
		if (getSystematicStatementDay().intValue() < CmDelinquencyCustomerMonitorRuleConstants.SYSTEMATIC_STMT_DAY_LOWER_LIMIT 
				|| getSystematicStatementDay().intValue() > CmDelinquencyCustomerMonitorRuleConstants.SYSTEMATIC_STMT_DAY_UPPER_LIMIT) {
			addError(MessageRepository.invalidSystematicStatmentDay(CmDelinquencyCustomerMonitorRuleConstants.SYSTEMATIC_STMT_DAY_LOWER_LIMIT
					, CmDelinquencyCustomerMonitorRuleConstants.SYSTEMATIC_STMT_DAY_UPPER_LIMIT));
		}
	}

	@Override
	public void invoke() {
		
		Person person = personId.getEntity();

		// If process day is not same as systematic statement day.
		if(getProcessDateTime().getDate().getDay() != getSystematicStatementDay().intValue()) {
			isProcessingCompleted = Bool.FALSE;
			return;
		}
		
		// If limiting characteristic entity soft parameter is populated and effective limiting characteristic doesn't exist on person
		if(!isBlankOrNull(getLimitingCharacteristicEntity()) && !this.doesLimitingCharExist()) {
			this.isProcessingCompleted = Bool.FALSE;
			return;
		}
		
		// Retrieving effective dated CDM Referral Characteristic for the customer.
		String cdmReferredCharValue = this.retrievePersonCharacteristicValue(person, getCdmReferralCharacteristicType(), getProcessDateTime().getDate());
	
		List<String> cdmCharReferralValueList = Arrays.asList(getCdmReferralCharacteristicValueList().split(","));
		
		// If char value is in CDM Referral Characteristic Value List soft parameter
		if(!isBlankOrNull(cdmReferredCharValue) && cdmCharReferralValueList.contains(cdmReferredCharValue.trim())) {
			this.isProcessingCompleted = Bool.FALSE;
			return;
		}
		
		
		// Check if customer is active
		boolean isActive = CmPolicyHelper.Factory.newInstance().isActiveCustomer(personId, getProcessDateTime().getDate(), getCustStatusOptionType()
				, getCustStatusOptionValue());
		
		String customerStatus = "";
		
		// If customer is active
		if(isActive) {
			customerStatus = CmDelinquencyCustomerMonitorRuleConstants.CONST_A;
		}
		else {
			customerStatus = CmDelinquencyCustomerMonitorRuleConstants.CONST_C;
		}
		
		
		int matchesFound = 0;
		
		// If customer status is equal to band 1 customer status
		if (customerStatus.equals(getBand1CustomerStatus().trim())) {
			matchesFound++;
		}
		// If band 2 customer status exists and customer status is equal to band 2 customer status
		if (!isBlankOrNull(getBand2CustomerStatus()) && customerStatus.equals(getBand2CustomerStatus().trim())) {
			matchesFound++;
		}
		// If band 3 customer status exists and customer status is equal to band 3 customer status
		if (!isBlankOrNull(getBand3CustomerStatus()) && customerStatus.equals(getBand3CustomerStatus().trim())) {
			matchesFound++;
		}
		
		// if there is no band matched with retrieved customer status
		if (matchesFound == 0) {
			this.isProcessingCompleted = Bool.FALSE;
			return;
		}
		
		// If multiple bands match with the retrieved customer status
		if (matchesFound > 1) {
			// Throw error  There should only be 1 band criteria for the Customer Status %1
			MessageRepository.validateMultiCustStatus(customerStatus);
		}
		
		// If retrieved customer status is same as band 1 customer status
		if(customerStatus.equalsIgnoreCase(getBand1CustomerStatus())) {
			this.evaluateCustomerForDelinquency(getBand1ProcessBillsWithAge(), getBand1UnpaidAmountAndPercentage()
					, getBand1ProcessBillsWithUnpaidAmount(), getBand1ProcessBillsWithUnpaidPercentage(), getBand1DelinquencyProcessType());
		}
		
		// If retrieved customer status is same as band 2 customer status
		if(!isBlankOrNull(getBand2CustomerStatus()) && customerStatus.equalsIgnoreCase(getBand2CustomerStatus())) {
			this.evaluateCustomerForDelinquency(getBand2ProcessBillsWithAge(), getBand2UnpaidAmountAndPercentage()
					, getBand2ProcessBillsWithUnpaidAmount(), getBand2ProcessBillsWithUnpaidPercentage(), getBand2DelinquencyProcessType());
		}
		
		// If retrieved customer status is same as band 3 customer status
		if(!isBlankOrNull(getBand3CustomerStatus()) && customerStatus.equalsIgnoreCase(getBand3CustomerStatus())) {
			this.evaluateCustomerForDelinquency(getBand3ProcessBillsWithAge(), getBand3UnpaidAmountAndPercentage()
					, getBand3ProcessBillsWithUnpaidAmount(), getBand3ProcessBillsWithUnpaidPercentage(), getBand3DelinquencyProcessType());
		}
	}

	/**
	 * This method is to check if the person has effective limiting characteristic.
	 * 
	 * @author IlaM
	 * 
	 * @return If limiting char exists on person 
	 * */
	private boolean doesLimitingCharExist() {
		
		
		// If limiting characteristic entity soft parameter value is set to P
		if(getLimitingCharacteristicEntity().trim().equals(CmDelinquencyCustomerMonitorRuleConstants.CONST_P)) {
			
			// Set Limiting Char Effective Date = Process Date (subtract) parameter Limiting Characteristic Effective within Last X Days
			Date limitingCharEffectiveDate = getProcessDateTime().getDate().addDays(getLimitingCharEffectiveWithinXDays().negate().intValue());
			
			// Creating query string
			StringBuilder queryString = new StringBuilder().append(CmDelinquencyCustomerMonitorRuleConstants.CHECK_FOR_LIMITING_CHAR);
			
			// If characteristic type is of type predefined value
			if (getLimitingCharacteristicType().getCharacteristicType().isPredefinedValue()) {
				queryString.append(" and personChar.characteristicValue = :limitingCharVal ");
			} 
			// If characteristic type is of type foreign key value
			else if (getLimitingCharacteristicType().getCharacteristicType().isForeignKeyValue()) {
				queryString.append(" and personChar.characteristicValueForeignKey1 = :limitingCharVal ");
			// If characteristic type is of type adhoc value	
			} else if (getLimitingCharacteristicType().getCharacteristicType().isAdhocValue()) {
				queryString.append(" and personChar.adhocCharacteristicValue = :limitingCharVal ");
			}
			
			// Creating query
			Query<Person> query = createQuery(queryString.toString(), 
					"CmSystematicStatementsDelinquencyProcessMonitorRuleAlgComp_Impl.doesLimitingCharExist()");
			
			// Binding reference variables
			query.bindId("personId", personId);
			query.bindEntity("limitingCharType", getLimitingCharacteristicType());
			query.bindDate("limitingCharEffDate", limitingCharEffectiveDate);
			query.bindDate("processDate", getProcessDateTime().getDate());
			
			// If characteristic type is of type predefined value
			if (getLimitingCharacteristicType().getCharacteristicType().isPredefinedValue()) {
				query.bindStringProperty("limitingCharVal", PersonCharacteristic.properties.characteristicValue, getLimitingCharacteristicValue());
			} 
			// If characteristic type is of type foreign key value
			else if (getLimitingCharacteristicType().getCharacteristicType().isForeignKeyValue()) {
				query.bindStringProperty("limitingCharVal", PersonCharacteristic.properties.characteristicValueForeignKey1, getLimitingCharacteristicValue());
			}
			// If characteristic type is of type adhoc value	
			else if (getLimitingCharacteristicType().getCharacteristicType().isAdhocValue()) {
				query.bindStringProperty("limitingCharVal", PersonCharacteristic.properties.adhocCharacteristicValue, getLimitingCharacteristicValue());
			}
			
			// Adding result
			query.addResult("person", "personChar.id.person");
			
			// If limiting char found
			if (notNull(query.firstRow())) {
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * This Method Retrieves Person Characteristic value.
	 * 
	 * @author IlaM
	 * 
	 * @param person
	 * @param characteristicType
	 * @param effectiveDate
	 * 
	 * @return person characteristic value
	 * 
	 */
	private String retrievePersonCharacteristicValue(Person person, CharacteristicType characteristicType, Date effectiveDate) {
		String charvalue = "";
		ListFilter<PersonCharacteristic> personCharacteristicFilter = null;
		
		
		personCharacteristicFilter = person.getCharacteristics().createFilter(" where this.id.characteristicType = :characteristicType and  this.id.effectiveDate <= :effectiveDate order by this.id.effectiveDate desc "
					, "CmSystematicStatementsDelinquencyProcessMonitorRuleAlgComp_Impl");
		
		// Binding char type and effective date
		personCharacteristicFilter.bindId("characteristicType", characteristicType.getId());
		personCharacteristicFilter.bindDate("effectiveDate", effectiveDate);
			
		PersonCharacteristic personCharacteristic = personCharacteristicFilter.firstRow();

		// conditions to handle characteristic type and null check
		if (isNull(personCharacteristic)) {
			return charvalue;
		}
		else if (characteristicType.getCharacteristicType().isForeignKeyValue()) {
			charvalue = personCharacteristic.getCharacteristicValueForeignKey1();
		}
		else if (characteristicType.getCharacteristicType().isAdhocValue()) {
			charvalue = personCharacteristic.getAdhocCharacteristicValue();
		}
		else {
			charvalue = personCharacteristic.getCharacteristicValue();
		}

		return charvalue;
	}
	
	/**
	 * This method is to evaluate the customer for delinquency on the basis of given band parameters.
	 * 
	 * @author IlaM
	 * 
	 * @param bandAge
	 * @param bandUnpaidAmtAndPrctRequired
	 * @param bandUnpaidAmount
	 * @param bandUnpaidPercentage
	 * @param bandDelinquencyProcessType
	 * 
	 * */
	private void evaluateCustomerForDelinquency(BigInteger bandAge, YesNoOptionLookup bandUnpaidAmtAndPrctRequired, 
			BigDecimal bandUnpaidAmount, BigDecimal bandUnpaidPercentage, CmDelinquencyProcessType bandDelinquencyProcessType) {
		
		CmSystematicStatementDelinquencyHelper systematicStatementDelinquencyHelper = CmSystematicStatementDelinquencyHelper.Factory.newInstance();
		
		Map<String, ArrayList<String>> billGroupAccountIdMap = systematicStatementDelinquencyHelper.getBillGroupRelatedAccounts(personId
				, getAccountNumberType().getId());
		
		// Populate Systematic Statement delinquency data object
		CmSystematicStatementDelinquencyDataObject systematicStatementDelinquencyDO = new CmSystematicStatementDelinquencyDataObject();
		systematicStatementDelinquencyDO.setAge(bandAge);
		systematicStatementDelinquencyDO.setUnpaidAmount(bandUnpaidAmount);
		systematicStatementDelinquencyDO.setUnpaidAmtAndPrctRequired(bandUnpaidAmtAndPrctRequired);
		systematicStatementDelinquencyDO.setUnpaidPercentage(bandUnpaidPercentage);
		systematicStatementDelinquencyDO.setDelinquencyProcessType(bandDelinquencyProcessType);
		systematicStatementDelinquencyDO.setPersonId(personId);
		systematicStatementDelinquencyDO.setAccountNumberIdType(getAccountNumberType());
		systematicStatementDelinquencyDO.setIncludeOnAccountPayments(getIncludeOnAccountPayments());
		systematicStatementDelinquencyDO.setAdminstrativeContractTypeFeatureConfig(getAdminstrativeContractTypeFeatureConfig());
		systematicStatementDelinquencyDO.setAdminstrativeContractTypeOptionType(getAdminstrativeContractTypeOptionType());
		systematicStatementDelinquencyDO.setUseCurrentRevenuePeriodBilled(getUseCurrentRevenuePeriodBilled());
		
		
		// For each bill group in bill group account id map 
		for (String billGroup : billGroupAccountIdMap.keySet()) {
			
			// Retrieve the account id list for current bill group 
			ArrayList<String> accountIdList = billGroupAccountIdMap.get(billGroup);
			systematicStatementDelinquencyDO.setAccountIdList(accountIdList);
			
			// If customer is eligible for delinquency and delinquency process doesnt already  exist for the person.
			if (systematicStatementDelinquencyHelper.isCustomerEligibleForDelinquency(systematicStatementDelinquencyDO) 
					&& !this.doesDelinquencyProcessExist(bandDelinquencyProcessType)) {
				
				// Create delinquency process for bill group
				this.createDelinquencyProcess(bandDelinquencyProcessType, billGroup);
				isProcessingCompleted = Bool.TRUE;
				return;
			}
			
		}
		
		isProcessingCompleted = Bool.FALSE;
		
	}
	
	/**
	 * This method is to create Delinquency Process
	 *   
	 * @author IlaM
	 * 
	 * @param bandDelProcessType
	 * @param billGroup
	 * 
	 */

	private void createDelinquencyProcess(CmDelinquencyProcessType bandDelProcessType, String billGroup) {
		
		CmDelinquencyProcessType_Id delProcTypeId = bandDelProcessType.getId();
		BusinessObjectInstance delProcBOInstance = BusinessObjectInstance.create(bandDelProcessType.getRelatedTransactionBO());

		delProcBOInstance.set("delinquencyProcessType", delProcTypeId.getEntity());
		delProcBOInstance.set("bo", bandDelProcessType.getRelatedTransactionBO());
		delProcBOInstance.set("boStatus", bandDelProcessType.getRelatedTransactionBO().getInitialStatus().getStatusString());
		delProcBOInstance.set("creationDateTime", getProcessDateTime());
		delProcBOInstance.set("updateStatusDateTime", getProcessDateTime());
		delProcBOInstance.set("personId", personId.getIdValue());

		BusinessObjectDispatcher.execute(delProcBOInstance, BusinessObjectActionLookup.constants.FAST_ADD).getEntity("delinquencyProcessId", CmDelinquencyProcess.class);
	}
	
	/**
	 * This method is to check if delinquency process already exists
	 * in non final status 
	 * for the person for the given delinquency type.
	 * 
	 * @author IlaM
	 * 
	 * @param delinquencyProcessType
	 * 
	 * @return Does delinquency process exist
	 * 
	 * */
	private boolean doesDelinquencyProcessExist(CmDelinquencyProcessType delinquencyProcessType) {

		// Creating prepared statement
		PreparedStatement preparedStatement = createPreparedStatement(
				CmDelinquencyCustomerMonitorRuleConstants.EXISTS_DELINQ_PROC_PER_LVL.toString(),
				"CmSystematicStatementsDelinquencyProcessMonitorRuleAlgComp_Impl");
		
		// Binding reference variables
		preparedStatement.bindId("personId", personId);
		preparedStatement.bindEntity("delinProcTypeCd", delinquencyProcessType);

		// Retrieving list from prepared statement
		List<SQLResultRow> resultRowList = preparedStatement.list();

		// If result row list is not null and empty
		if (notNull(resultRowList) && !resultRowList.isEmpty()) {

			return true;
		}
		
		return false;
	}

	@Override
	public void setAccountId(Account_Id accountId) {
		// TODO Auto-generated method stub
		
	}
}
