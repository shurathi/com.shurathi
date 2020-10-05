/*                                                             
 ********************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This business component returns most lenient termination effective date 
 * rule associated with the overdue process.  
 * 
 * The priority based on most lenient state cancellation date is:
 * Date of processing 
 * End of Grace
 * Retro Paid
 *                                         
 ********************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    	 Reason:                                     
 * YYYY-MM-DD  	IN     	 Reason text.                                
 * 
 * 2020-05-06   VINODW	Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 ********************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.overdueEventType;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.Query;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.api.datatypes.LookupHelper;
import com.splwg.base.api.lookup.BusinessObjectStatusOptionTypeLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusOption;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOptionInfo;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOptionsCache;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration_Id;
import com.splwg.ccb.api.lookup.BillableChargeStatusLookup;
import com.splwg.ccb.domain.admin.cisDivision.CisDivisionCharacteristic;
import com.splwg.ccb.domain.admin.cisDivision.CisDivision_Id;
import com.splwg.ccb.domain.creditcollections.overdueProcess.OverdueProcess;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.cm.api.lookup.DetEffTermDateRuleForOverdueLookup;
import com.splwg.cm.api.lookup.ExternalSystemTypeLookup;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author VINODW
 *
@BusinessComponent (customizationCallable = true)
 */
public class CmOverdueEventTypeHelper_Impl extends GenericBusinessComponent implements CmOverdueEventTypeHelper {
	private String billCharType;
	private String policyStatusOptionType;
	private String policyStatusOptionValue;
	private String policyTerminatedStatusOptVal;
	private String stateOfIssue;
	private String terminationDateRuleCharType;
	private String priorityOneTermDateRule;
	private String priorityTwoTermDateRule;
	private String priorityThreeTermDateRule;

	private String priorityFourTermDateRule;
	private String priorityFiveTermDateRule;
	private String retroPaidTerminationDateRule;
	private String dateOfProcesingTerminationDateRule;
	private CharacteristicType retroPaidPercentageCharType;
	private CharacteristicType dateOfProcessingNumberOfDaysPriorCharType;

	private String policyPlanCharType;

	/**
	 * This method determines termination date rule.
	 * @param overdueProcess
	 */
	public CmTerminationData determineTermintaionDateRule(OverdueProcess overdueProcess)
	{
	
		CmTerminationData terminationData = new CmTerminationData();


		Long count;
		// Validate inputs.
		validateParameters();
		// Get the Account of the Overdue Process.
		Account account = overdueProcess.getAccount();
		CharacteristicType billCharacteristicType = new CharacteristicType_Id(billCharType).getEntity();
		// Get the latest bill due date associated with the bills on the overdue
		// process. The latest bill due date is used as the reference date to
		// retrieve policies billed under the account.
		Date latestBillDueDate = getLatestBillDueDate(overdueProcess, billCharacteristicType);
		
		// CharacteristicType membershipCharacteristicType = new CharacteristicType_Id(membershipCharType).getEntity();

		CharacteristicType policyPlanCharactristicType = new CharacteristicType_Id(policyPlanCharType).getEntity();
		// Bug 8745 End Add
		CharacteristicType stateOfIssueCharType = new CharacteristicType_Id(stateOfIssue).getEntity();
		BusinessObjectStatusOptionTypeLookup businessObjectStatusOptionTypeLookup = LookupHelper.getLookupInstance(BusinessObjectStatusOptionTypeLookup.class, policyStatusOptionType);
		// Populate Account State of Issue list. Get distinct state of issues
		// associated with the accounts policies and store in list.

        // List<CisDivision_Id> stateOfIssueList = fetchPolicyStateOfIssue(account, latestBillDueDate, membershipCharacteristicType, stateOfIssueCharType, businessObjectStatusOptionTypeLookup, policyStatusOptionValue, policyTerminatedStatusOptVal);
		List<CisDivision_Id> stateOfIssueList = fetchPolicyStateOfIssue(account, latestBillDueDate, stateOfIssueCharType, businessObjectStatusOptionTypeLookup, policyStatusOptionValue, policyTerminatedStatusOptVal, policyPlanCharactristicType);

		// Check If no State of Issue is associated with the Account, ie
		// Account State of Issue list is empty
		if (stateOfIssueList.isEmpty() && stateOfIssueList.size() == 0)
		{
			// Throw an error
			addError(MessageRepository.cannotDetermineStateOfIssue(account.getId().getIdValue(), latestBillDueDate.toString()));
		}
		CharacteristicType termDateRuleCharType = new CharacteristicType_Id(terminationDateRuleCharType).getEntity();
		// Determine if the Priority 1 Termination Date Rule is valid for the
		// account
		count = determinePriorityTermDateRule(stateOfIssueList, priorityOneTermDateRule, latestBillDueDate, termDateRuleCharType);
		// Check if count is greater than zero.
		if (count > 0)
		{

			// Return Termination Effective Date Rule = Priority 1 Termination
			// Date Rule
			// return priorityOneTermDateRule;

			// Set Termination Effective Date Rule = Priority 1 Termination Date
			// Rule
			terminationData.setTerminationEffectiveRule(priorityOneTermDateRule);

		}

		if (count == 0) {

			// Determine if the Priority 2 Termination Date Rule is valid for
			// the account.
			count = determinePriorityTermDateRule(stateOfIssueList, priorityTwoTermDateRule, latestBillDueDate, termDateRuleCharType);
			// Check if count is greater than zero.

			if (count > 0)
			{

				// Return Termination Effective Date Rule = Priority 2
				// Termination Date Rule.
				// return priorityTwoTermDateRule;
				// Set Termination Effective Date Rule = Priority 2 Termination
				// Date Rule
				terminationData.setTerminationEffectiveRule(priorityTwoTermDateRule);

			}

		}
		if (count == 0) {

			// Determine if the Priority 3 Termination Date Rule is valid for
			// the account
			count = determinePriorityTermDateRule(stateOfIssueList, priorityThreeTermDateRule, latestBillDueDate, termDateRuleCharType);
			// Check if count is greater than zero.

			if (count > 0)
			{
				// Return Termination Effective Date Rule = Priority 3
				// Termination Date Rule
				// return priorityThreeTermDateRule;
				// Set Termination Effective Date Rule = Priority 3 Termination Date Rule
				terminationData.setTerminationEffectiveRule(priorityThreeTermDateRule);
			}
		}

		if (count == 0) {
			// Determine if the Priority 4 Termination Date Rule is valid
			count = determinePriorityTermDateRule(stateOfIssueList, priorityFourTermDateRule, latestBillDueDate, termDateRuleCharType);
			// Check if count is greater than zero
			if (count > 0) {
				// Set Termination Effective Date Rule = Priority 4 Termination Date Rule
				terminationData.setTerminationEffectiveRule(priorityFourTermDateRule);
			}
		}
		if (count == 0) {
			// Determine if the Priority 5 Termination Date Rule is valid
			count
			= determinePriorityTermDateRule(stateOfIssueList, priorityFiveTermDateRule, latestBillDueDate, termDateRuleCharType);
			// Check if count is greater than zero.
			if (count > 0) {
				// Set Termination Effective Date Rule = Priority 5 Termination
				// Date Rule
				terminationData.setTerminationEffectiveRule(priorityFiveTermDateRule);
			}
		}
		if (isBlankOrNull(terminationData.getTerminationEffectiveRule())) {
			// If Priority 1, 2 or 3 Termination Date Rule is not valid for the
			// account, throw an error.
			addError(MessageRepository.unableToDetermineTerminationDateRule(account.getId().getIdValue()));
		}

		if (terminationData.getTerminationEffectiveRule().equals(retroPaidTerminationDateRule)) {
			BigDecimal retroPaidthreshold = BigDecimal.ZERO;
			// Retrieve the maximum threshold stored on the state division using
			// the Retro Paid Percentage Threshold Characteristic Type
			Query<String> retroPaidTerminationDatecisdivCharQuery = fetchCisDivCharacteristicQuery(stateOfIssueList, latestBillDueDate, retroPaidPercentageCharType);
			if (retroPaidPercentageCharType.getCharacteristicType().isAdhocValue()) {
				retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.adhocCharacteristicValue");
			}
			else if (retroPaidPercentageCharType.getCharacteristicType().isForeignKeyValue()) {
				retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.characteristicValueForeignKey1");
			}
			else {
				retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.characteristicValue");
			}

			List<String> queryList = retroPaidTerminationDatecisdivCharQuery.list();
			for (String queryRowCharVal : queryList) {
				BigDecimal rowValue = new BigDecimal(queryRowCharVal.trim());

				retroPaidthreshold = (rowValue.compareTo(retroPaidthreshold) > 0) ? rowValue : retroPaidthreshold;

			}
			terminationData.setRetroPaidPercentageThreshold(retroPaidthreshold);
		}
		else if (terminationData.getTerminationEffectiveRule().equals(dateOfProcesingTerminationDateRule)) {
			BigInteger dateofProcessingNoOfDaysPrior=null;
			// Retrieve the minimum number of days prior stored on the state
			// division using the Date of Processing Number of Days Prior
			// Characteristic Type
			Query<String> retroPaidTerminationDatecisdivCharQuery = fetchCisDivCharacteristicQuery(stateOfIssueList, latestBillDueDate, dateOfProcessingNumberOfDaysPriorCharType);
			// Check for Characteristic type
			if (dateOfProcessingNumberOfDaysPriorCharType.getCharacteristicType().isAdhocValue()) {
				retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.adhocCharacteristicValue");
			}
			else if (dateOfProcessingNumberOfDaysPriorCharType.getCharacteristicType().isForeignKeyValue()) {
				retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.characteristicValueForeignKey1");
			}
			else {
				retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.characteristicValue");
			}

			List<String> queryList = retroPaidTerminationDatecisdivCharQuery.list();
			for (String queryRowCharVal : queryList) {
				BigInteger rowValue = new BigInteger(queryRowCharVal.trim());
				if (isNull(dateofProcessingNoOfDaysPrior)) {
					dateofProcessingNoOfDaysPrior = rowValue;
				}
				else {
					dateofProcessingNoOfDaysPrior = (rowValue.compareTo(dateofProcessingNoOfDaysPrior) < 0) ? rowValue : dateofProcessingNoOfDaysPrior;
				}

			}
			if(isNull(dateofProcessingNoOfDaysPrior)){
				dateofProcessingNoOfDaysPrior=BigInteger.ZERO;
			}
			terminationData.setDateofProcessingNumberofDaysPrior(dateofProcessingNoOfDaysPrior);

		}

		// return null;
		return terminationData;

	}

	/**
	 * @param retroPaidPercentageCharType2
	 * @return
	 */
	private Query<String> fetchCisDivCharacteristicQuery(List<CisDivision_Id> stateOfIssueList, Date latestBillDueDate,CharacteristicType cisDivisionCharType) {
		StringBuilder priorityTermDateRule = new StringBuilder().append(CmOverdueProcessConstants.CIS_DIVISION_CHAR_QUERY);

		for (int index = 0; index < stateOfIssueList.size(); index++)
		{
			if (index != 0)
			{
				priorityTermDateRule.append(",");
			}
			priorityTermDateRule.append("'").append(stateOfIssueList.get(index).getIdValue().trim()).append("'");

		}
		priorityTermDateRule.append(")");

		Query<String> query = createQuery(priorityTermDateRule.toString(), "CmOverdueEventTypeHelper_Impl");
		query.bindId("terminationDateRuleCharType", cisDivisionCharType.getId());
		query.bindDate("latestBillDueDate", latestBillDueDate);
		
		return query;
	}
 
	/**
	 * This method validates soft parameters.
	 */
	public void validateParameters()
	{
		PreparedStatement preparedStatement = null;
		SQLResultRow sqlResultRow = null;
		String featureConfigName = "";
		BigInteger count = BigInteger.ZERO;

		try {
			// Retrieve soft parameter values from Feature Configuration where
			// Feature Type is Determine Termination Effective Date Rule for
			// Overdue
			preparedStatement = createPreparedStatement(CmOverdueProcessConstants.RETRIVE_FEATURE_CONFIG_VAL.toString(), "CmOverdueEventTypeHelper_Impl");

			preparedStatement.bindString("extSysType", CmOverdueProcessConstants.EXTERNAL_SYSTEM_TYPE_FLG, "EXT_SYS_TYP_FLG");

			if (notNull(preparedStatement)) {
				sqlResultRow = preparedStatement.firstRow();
				featureConfigName = sqlResultRow.getString("WFM_NAME");
				count = sqlResultRow.getInteger("COUNT");
			}
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}

		FeatureConfiguration tdFeatureConfiguration = new FeatureConfiguration_Id(featureConfigName).getEntity();
		featureConfigName = tdFeatureConfiguration.fetchLanguageDescription();

		if (!count.equals(BigInteger.ONE)) {
			// If no Feature Configuration record found,throw an error
			if (count.equals(0))
				addError(MessageRepository.missingFeatureConfig(featureConfigName));
			// If more than one Feature Configuration record found,throw an
			// error
			else
				addError(MessageRepository.multipleFeatureConfig(featureConfigName));
		}
		// Get the Option value bill id characteristic type.
		billCharType = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.BILL_CHAR_TYPE);

		// Get the Option value membership characteristic type.
		// membershipCharType = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.MEMBERSHIP_CHAR_TYPE);
		// Get the Option value policy plan characteristic type.
		policyPlanCharType = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.POLICY_PLAN_CHAR_TYPE);

		// Get the Option value policy status option type.
		policyStatusOptionType = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.POLICY_OPTION_TYPE);
		// Get the Option value policy status option value.
		policyStatusOptionValue = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.POLICY_ACTIVE_OPTION_VAL);
		//Get the policy terminated status option value.
		policyTerminatedStatusOptVal = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.POLICY_TERMINATED_OPTION_VAL);
		// Get the Option value state of issue char type.
		stateOfIssue = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.STATE_OF_ISSUE_CHAR_TYPE);
		// Get the Option value termination date rule char type.
		terminationDateRuleCharType = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.TERM_DATE_RULE_CHAR_TYPE);
		// Get the Option value priority one termination Date Rule.
		priorityOneTermDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.PRIORITY_1_TERM_DATE_RULE_CHAR_VAL);
		// Get the Option value priority two termination Date Rule.
		priorityTwoTermDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.PRIORITY_2_TERM_DATE_RULE_CHAR_VAL);
		// Get the Option value priority three termination Date Rule.
		priorityThreeTermDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.PRIORITY_3_TERM_DATE_RULE_CHAR_VAL);
		

		// Get the Option value priority four termination Date Rule.
		priorityFourTermDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.PRIORITY_4_TERM_DATE_RULE_CHAR_VAL);
		// Get the Option value priority four termination Date Rule.
		priorityFiveTermDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.PRIORITY_5_TERM_DATE_RULE_CHAR_VAL);
		// Get the Option value retro paid Date Rule.
		retroPaidTerminationDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.RETRO_PAID_TERM_DATE_RULE);
		// Get the Option value date of processing termination Date Rule.
		dateOfProcesingTerminationDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.DATE_OF_PROCESSING_TERM_DATE_RULE);
		// Get the Option value date Of Processing Number Of Days Prior CharType .
		dateOfProcessingNumberOfDaysPriorCharType = new CharacteristicType_Id(fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.DATE_OF_PROCESSING_NUMBER_OF_DAYS_PRIOR_CHAR_TYPE)).getEntity();
		// Get the Option value retro paid percentage CharType .
		retroPaidPercentageCharType = new CharacteristicType_Id(fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.RETRO_PAID_PERCENT_THRESHOLD_CHAR_TYPE)).getEntity();

	}

	/**
	 * This method fetches feature configuration values.
	 * @param tdFeatureConfiguration
	 * @param featureConfigName
	 * @param overdueProcessLookup
	 */
	public String fetchFeatureConfigValues(FeatureConfiguration tdFeatureConfiguration, String featureConfigName, Lookup overdueProcessLookup)
	{
		FeatureConfigurationOptionInfo featureConfigInfo = null;
		String featureConfigOptValue = "";
		featureConfigInfo = FeatureConfigurationOptionsCache.getHighestSequenceOptionFor(ExternalSystemTypeLookup.constants.DET_EFF_TERM_DATE_RULE_FOR_OVERDUE, overdueProcessLookup);

		if (isNull(featureConfigInfo)) {
			addError(MessageRepository.missingFeatureConfigOptType(featureConfigName, overdueProcessLookup.toLocalizedString()));
		}
		else
		{
			// Get the option value.
			featureConfigOptValue = featureConfigInfo.getValue();
		}
		return featureConfigOptValue;
	}

	/**
	 * This method gets the latest bill due date associated with the bills on the overdue process.  
	 * The latest bill due date is used as the reference date to retrieve policies billed under the account.
	 * @param overdueProcess
	 * @param billCharacteristicType
	 */
	public Date getLatestBillDueDate(OverdueProcess overdueProcess, CharacteristicType billCharacteristicType)
	{
		Date dueDate = null;
		Query<Date> query = createQuery(CmOverdueProcessConstants.FETCH_LATEST_BILL_DUE_DT.toString(), "CmOverdueEventTypeHelper_Impl");
		query.bindEntity("overdueProcess", overdueProcess);
		query.bindEntity("billCharType", billCharacteristicType);
		query.addResult("billDueDate", "MAX(bl.dueDate)");
		dueDate = query.firstRow();
		return dueDate;
	}

	/**
	 * This method gets distinct state of issues associated with the account�s policies.
	 * @param account
	 * @param latestBillDueDate
	 * @param stateCharType
	 * @param optionType
	 * @param optionVal
	 * @param policyPlanCharType
	 */
	public List<CisDivision_Id> fetchPolicyStateOfIssue(Account account, Date latestBillDueDate, CharacteristicType stateCharType, Lookup optionType, String optionVal, String terminatedStatusOptVal, CharacteristicType policyPlanCharType)
	{

		List<CisDivision_Id> stateOfIssue = new ArrayList<CisDivision_Id>();

		Query<String> stateOfIssueQuery = createQuery(CmOverDueProcessCalculateTriggerDatesConstants.STATE_OF_ISSUE_LIST.toString(), "CmOverdueEventTypeHelper_Impl");
		stateOfIssueQuery.bindEntity("account", account);
		stateOfIssueQuery.bindLookup("active", BillableChargeStatusLookup.constants.BILLABLE);
		stateOfIssueQuery.bindDate("latestBillDueDate", latestBillDueDate);
 
		// stateOfIssueQuery.bindId("memberShipCharType",
		// membershipCharType.getId());
		// Bug 8745 End Delete 
		stateOfIssueQuery.bindId("stateOfIssueCharType", stateCharType.getId());
		stateOfIssueQuery.bindLookup("policyStatusOptionType", optionType);
		stateOfIssueQuery.bindStringProperty("policyStatusActiveOptionVal", BusinessObjectStatusOption.properties.value, optionVal);
		stateOfIssueQuery.bindStringProperty("policyStatusTerminatedOptionVal", BusinessObjectStatusOption.properties.value, terminatedStatusOptVal);
 
		stateOfIssueQuery.bindId("policyPlanCharType", policyPlanCharType.getId());
 
		stateOfIssueQuery.bindDate("processDate", getProcessDateTime().getDate());
		stateOfIssueQuery.addResult("searchCharacteristicValue", "pch.searchCharacteristicValue");
		// Verify Return result is null or not
		if (stateOfIssueQuery.listSize() > 0) {
			// Take distinct value if there is repeataion of state of issue
			stateOfIssueQuery.selectDistinct(true);
			// Iterate through result and add it to state of issue list
			Iterator<String> stateOfissueItr = stateOfIssueQuery.iterate();
			while (stateOfissueItr.hasNext()) {
				String stateOfIssueStr = stateOfissueItr.next();
				if (notNull(stateOfIssueStr)) {
					CisDivision_Id stateOfIssueId = null;
					stateOfIssueId = new CisDivision_Id(stateOfIssueStr);
					if (notNull(stateOfIssueId) && notNull(stateOfIssueId.getEntity())) {
						stateOfIssue.add(stateOfIssueId);
					}
				}
			}
		}
		return stateOfIssue;
	}



	/**
	 * This method determines if the Termination Date Rule is valid for the account based on priority.
	 * @param stateOfIssueList
	 * @param priorityTerminationDateRuleCharVal
	 * @param latestBillDueDate
	 * @param termDateRuleCharType
	 */
	public Long determinePriorityTermDateRule(List<CisDivision_Id> stateOfIssueList, String priorityTerminationDateRuleCharVal, Date latestBillDueDate, CharacteristicType termDateRuleCharType)
	{
		StringBuilder priorityTermDateRule = new StringBuilder().append(CmOverdueProcessConstants.DET_PRIORITY_TERM_DT_RULE);

		for (int index = 0; index < stateOfIssueList.size(); index++)
		{
			if (index != 0)
			{
				priorityTermDateRule.append(",");
			}
			priorityTermDateRule.append("'").append(stateOfIssueList.get(index).getIdValue().trim()).append("'");

		}
		priorityTermDateRule.append(")");

		Query<Long> query = createQuery(priorityTermDateRule.toString(), "CmOverdueEventTypeHelper_Impl");
		query.bindId("terminationDateRuleCharType", termDateRuleCharType.getId());
		query.bindDate("latestBillDueDate", latestBillDueDate);
		query.bindStringProperty("priorityTerminationDateRuleCharVal", CisDivisionCharacteristic.properties.characteristicValue, priorityTerminationDateRuleCharVal);
		query.addResult("count", "COUNT(divc.id.division)");
		Long count = query.firstRow();
		return count;

	}

	/**
	 * This method fetch the most lenient grace days
	 * @param stateOfIssueList
	 * @param latestBillDueDate
	 * @param graceDayCharType
	 * @return
	 */
	public BigInteger retrieveMostLenientGraceDays(List<CisDivision_Id> stateOfIssueList, Date latestBillDueDate, CharacteristicType graceDayCharType) {
		BigInteger mostLenientGraceDays = null;
		StringBuilder lenientGraceDayQuery = new StringBuilder();
		lenientGraceDayQuery.append(CmOverDueProcessCalculateTriggerDatesConstants.LENIENT_GRACE_DAY_QUERY);
	
		//for (int i = 0; i < stateOfIssueList.size(); i++) {
			//lenientGraceDayQuery.append(stateOfIssueList.get(i).getIdValue().trim());
			//if ((i != 0 && stateOfIssueList.size() == 1) || i != stateOfIssueList.size() - 1) {
				//lenientGraceDayQuery.append("','");
			//}
			//else if (i != 0 || stateOfIssueList.size() == 1) {
				//lenientGraceDayQuery.append("' ) ");
			//}

		//}
		if(!stateOfIssueList.isEmpty())
		{
			lenientGraceDayQuery.append(" AND divc.id.division in ( ");
			for (int index = 0; index < stateOfIssueList.size(); index++)
			{
				if (index != 0)
				{
					lenientGraceDayQuery.append(",");
				}
				lenientGraceDayQuery.append("'").append(stateOfIssueList.get(index).getIdValue().trim()).append("'");

			}
			lenientGraceDayQuery.append(")");
		}
		
		Query<String> graceDaysQuery = createQuery(lenientGraceDayQuery.toString(), "CmOverdueEventTypeHelper_Impl");
		graceDaysQuery.bindId("graceDaysCharType", graceDayCharType.getId());
		graceDaysQuery.bindDate("latestBillDueDate", latestBillDueDate);
		graceDaysQuery.addResult("adhocCharacteristicValue", "max(divc.adhocCharacteristicValue)");

		// if (notBlank(graceDaysQuery.firstRow()) && graceDaysQuery.firstRow().matches("-?\\d+(\\d+)?")) {
		if (notBlank(graceDaysQuery.firstRow())) {

			mostLenientGraceDays = new BigInteger(graceDaysQuery.firstRow().trim());

		}
		return mostLenientGraceDays;
	}

}

