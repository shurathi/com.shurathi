/*                                                                
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION                                         
 *                                                                
 * Determine Termination Effective Date Helper Business components
 * 
 * This helper class contains common methods used to determine Termination                      
 * Date components
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 * **************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.api.datatypes.LookupHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOptionInfo;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfigurationOptionsCache;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration_Id;
import com.splwg.ccb.domain.admin.cisDivision.CisDivisionCharacteristic;
import com.splwg.ccb.domain.admin.cisDivision.CisDivision_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.BusinessObjectStatusOptionTypeLookup;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.api.lookup.DetEffTermDateRuleForOverdueLookup;
import com.splwg.cm.api.lookup.ExternalSystemTypeLookup;
import com.splwg.cm.domain.delinquency.admin.overdueEventType.CmOverdueEventTypeHelper;
import com.splwg.cm.domain.delinquency.admin.overdueEventType.CmTerminationData;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;

/**
 * @author MugdhaP
 *
@BusinessComponent (customizationCallable = true, customizationReplaceable = true)
 */
public class CmDetermineTerminationEffectiveDateRuleHelper_Impl extends GenericBusinessComponent implements CmDetermineTerminationEffectiveDateRuleHelper {
	private String policyStatusOptionType;
	private String policyStatusOptionValue;
	private String policyTerminatedStatusOptVal;
	private String stateOfIssue;
	private String terminationDateRule;
	private String priorityOneTermDateRule;
	private String priorityTwoTermDateRule;
	private String priorityThreeTermDateRule;
	private String priorityFourTermDateRule;
	private String priorityFiveTermDateRule;
	private String retroPaidTerminationDateRule;
	private String dateOfProcesingTerminationDateRule;
	private String retroPaidPercentage;
	private String dateOfProcessingNumberOfDaysPrior;
	private String policyPlan;
	private CharacteristicType stateOFIssueCharType;
	private CharacteristicType terminationDateRuleCharType;
	private CharacteristicType retroPaidPercentageCharType;
	private CharacteristicType dateOfProcessingNumberOfDaysPriorCharType;
	private CharacteristicType policyPlanCharType;

	public CmTerminationData fetchTerminationEffectiveDateRule(CmDelinquencyProcess_Id delinquencyProcessId) {
		Person_Id personId = null;
		Account_Id accountId = null;
		CmTerminationData terminationData = new CmTerminationData();
		// validate soft parameters
		validateSoftParameters();

		CmDelinquencyProcess delinquencyProcess = delinquencyProcessId.getEntity();

		BusinessObjectInstance delProcBOInstance = BusinessObjectInstance.create(delinquencyProcess.getBusinessObject());
		delProcBOInstance.set(CmDetermineTerminationDateRuleConstants.DELINQUENCY_PROCESS_ID, delinquencyProcessId.getIdValue());
		delProcBOInstance = BusinessObjectDispatcher.read(delProcBOInstance);
		// Person or Account will be retrieved from bo instance based on
		// Delinquency Level
		String perId = delProcBOInstance.getString("personId");
		String acctId = delProcBOInstance.getString("accountId");

		if (isNull(perId) && isNull(acctId)) {

			addError(MessageRepository.delinquencyNotLinkedToCustomer(delinquencyProcessId));
		}
		else if (notNull(perId)) {

			personId = new Person_Id(perId);

		} else if (notNull(acctId)) {

			accountId = new Account_Id(acctId);
			personId = getPrimaryCustomer(accountId);
		}
		// Retrieve Latest Due Date
		Date latestDueDate = fetchLatestBillDueDate(delinquencyProcessId);
		List<CisDivision_Id> stateOfIssueList = new ArrayList<CisDivision_Id>();
		BusinessObjectStatusOptionTypeLookup businessObjectStatusOptionTypeLookup = LookupHelper.getLookupInstance(BusinessObjectStatusOptionTypeLookup.class, policyStatusOptionType);
		if (notNull(personId)) {
			// Retrieve State of Issue List for Person
			CmDelinquencyProcessHelper cmDelinquencyProcessHelper = CmDelinquencyProcessHelper.Factory.newInstance();
			stateOfIssueList = cmDelinquencyProcessHelper.fetchStateOIssueForMainCustomer(personId, latestDueDate, policyPlanCharType, businessObjectStatusOptionTypeLookup, stateOFIssueCharType, policyStatusOptionValue, policyTerminatedStatusOptVal);

		} else if (notNull(accountId)) {
			// Retrieve State of Issue List for Account
			CmOverdueEventTypeHelper cmOverdueEventTypeHelper = CmOverdueEventTypeHelper.Factory.newInstance();
			stateOfIssueList = cmOverdueEventTypeHelper.fetchPolicyStateOfIssue(accountId.getEntity(), latestDueDate, stateOFIssueCharType, businessObjectStatusOptionTypeLookup, policyStatusOptionValue, policyTerminatedStatusOptVal, policyPlanCharType);

		}

		//if (isNull(stateOfIssueList) && stateOfIssueList.isEmpty())
		if (isNull(stateOfIssueList) || stateOfIssueList.isEmpty())
		{
			addError(MessageRepository.noStateOfIssueListFound(delinquencyProcess.getId().getIdValue()));

		} else {
			Bool priorityTermDateRuleFoundSwitch = Bool.FALSE;
			// Determine Priority Termination Date Rule
			priorityTermDateRuleFoundSwitch = determinePriorityTermDateRule(stateOfIssueList, priorityOneTermDateRule, latestDueDate, terminationDateRuleCharType);
			if (priorityTermDateRuleFoundSwitch.isTrue()) {

				terminationData.setTerminationEffectiveRule(priorityOneTermDateRule);

			}
			if (priorityTermDateRuleFoundSwitch.isFalse()) {

				priorityTermDateRuleFoundSwitch = determinePriorityTermDateRule(stateOfIssueList, priorityTwoTermDateRule, latestDueDate, terminationDateRuleCharType);
				if (priorityTermDateRuleFoundSwitch.isTrue()) {

					terminationData.setTerminationEffectiveRule(priorityTwoTermDateRule);
				}
			}
			if (priorityTermDateRuleFoundSwitch.isFalse()) {

				priorityTermDateRuleFoundSwitch = determinePriorityTermDateRule(stateOfIssueList, priorityThreeTermDateRule, latestDueDate, terminationDateRuleCharType);
				if (priorityTermDateRuleFoundSwitch.isTrue()) {

					terminationData.setTerminationEffectiveRule(priorityThreeTermDateRule);
				}
			}
			if (priorityTermDateRuleFoundSwitch.isFalse()) {

				priorityTermDateRuleFoundSwitch = determinePriorityTermDateRule(stateOfIssueList, priorityFourTermDateRule, latestDueDate, terminationDateRuleCharType);
				if (priorityTermDateRuleFoundSwitch.isTrue()) {

					terminationData.setTerminationEffectiveRule(priorityFourTermDateRule);
				}
			}
			if (priorityTermDateRuleFoundSwitch.isFalse()) {

				priorityTermDateRuleFoundSwitch = determinePriorityTermDateRule(stateOfIssueList, priorityFiveTermDateRule, latestDueDate, terminationDateRuleCharType);
				if (priorityTermDateRuleFoundSwitch.isTrue()) {

					terminationData.setTerminationEffectiveRule(priorityFiveTermDateRule);
				}
			}
			if (priorityTermDateRuleFoundSwitch.isFalse()) {

				addError(MessageRepository.unableToDetermineTerminationDateRule());
			}
			else {
				// process if termination effective rule is retro paid termination date rule
				if (terminationData.getTerminationEffectiveRule().trim().compareTo(retroPaidTerminationDateRule.trim()) == 0) {
					BigDecimal retroPaidthreshold = BigDecimal.ZERO;
					Query<String> retroPaidTerminationDateCisDivCharQuery = fetchCisDivCharacteristics(stateOfIssueList, latestDueDate, retroPaidPercentageCharType);
					if (retroPaidPercentageCharType.getCharacteristicType().isAdhocValue()) {
						retroPaidTerminationDateCisDivCharQuery.addResult("charVal", "divc.adhocCharacteristicValue");
					}
					else if (retroPaidPercentageCharType.getCharacteristicType().isForeignKeyValue()) {
						retroPaidTerminationDateCisDivCharQuery.addResult("charVal", "divc.characteristicValueForeignKey1");
					}
					else {
						retroPaidTerminationDateCisDivCharQuery.addResult("charVal", "divc.characteristicValue");
					}

					List<String> queryList = retroPaidTerminationDateCisDivCharQuery.list();
					for (String queryRowCharVal : queryList) {

						BigDecimal rowValue = new BigDecimal(queryRowCharVal.trim());
						retroPaidthreshold = (rowValue.compareTo(retroPaidthreshold) > 0) ? rowValue : retroPaidthreshold;

					}
					terminationData.setRetroPaidPercentageThreshold(retroPaidthreshold);
				}
				// process if termination effective rule is date of processing termination date rule
				else if (terminationData.getTerminationEffectiveRule().trim().compareTo(dateOfProcesingTerminationDateRule.trim()) == 0) {

					BigInteger dateofProcessingNoOfDaysPrior = null;
					Query<String> retroPaidTerminationDatecisdivCharQuery = fetchCisDivCharacteristics(stateOfIssueList, latestDueDate, dateOfProcessingNumberOfDaysPriorCharType);
					if (dateOfProcessingNumberOfDaysPriorCharType.getCharacteristicType().isAdhocValue()) {
						retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.adhocCharacteristicValue");
					}
					else if (dateOfProcessingNumberOfDaysPriorCharType.getCharacteristicType().isForeignKeyValue()) {
						retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.characteristicValueForeignKey1");
					}
					else {
						retroPaidTerminationDatecisdivCharQuery.addResult("charVal", "divc.characteristicValue");
					}
					// determine date of processing no of days prior
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
					if (isNull(dateofProcessingNoOfDaysPrior)) {
						dateofProcessingNoOfDaysPrior = BigInteger.ZERO;
					}
					terminationData.setDateofProcessingNumberofDaysPrior(dateofProcessingNoOfDaysPrior);
				}

			}

		}

		return terminationData;
	}

	/**
	 * This method determines if the Termination Date Rule is valid for the account based on priority.
	 * @param stateOfIssueList
	 * @param priorityTerminationDateRuleCharVal
	 * @param latestBillDueDate
	 * @param termDateRuleCharType
	 */
	public Bool determinePriorityTermDateRule(List<CisDivision_Id> stateOfIssueList, String priorityTerminationDateRuleCharVal, Date latestBillDueDate, CharacteristicType termDateRuleCharType)
	{
		Bool priorityTermDateRuleFoundSwitch = Bool.FALSE;
		StringBuilder priorityTermDateRule = new StringBuilder().append(CmDetermineTerminationDateRuleConstants.DET_PRIORITY_TERM_DT_RULE);

		for (int index = 0; index < stateOfIssueList.size(); index++)
		{
			if (index != 0)
			{
				priorityTermDateRule.append(",");
			}
			priorityTermDateRule.append("'").append(stateOfIssueList.get(index).getIdValue().trim()).append("'");

		}
		priorityTermDateRule.append(")");

		Query<Long> query = createQuery(priorityTermDateRule.toString(), "CmDetermineTerminationEffectiveDateRuleHelper_Impl");
		query.bindId("terminationDateRuleCharType", termDateRuleCharType.getId());
		query.bindDate("latestBillDueDate", latestBillDueDate);
		query.bindStringProperty("priorityTerminationDateRuleCharVal", CisDivisionCharacteristic.properties.characteristicValue, priorityTerminationDateRuleCharVal);
		query.addResult("division", "divc.id.division");

		if (notNull(query.firstRow())) {
			priorityTermDateRuleFoundSwitch = Bool.TRUE;
		}

		return priorityTermDateRuleFoundSwitch;

	}
	// fetch division characteristics
	public Query<String> fetchCisDivCharacteristics(List<CisDivision_Id> stateOfIssueList, Date latestBillDueDate, CharacteristicType cisDivisionCharType) {
		StringBuilder priorityTermDateRule = new StringBuilder().append(CmDetermineTerminationDateRuleConstants.CIS_DIVISION_CHAR_QUERY);

		for (int index = 0; index < stateOfIssueList.size(); index++)
		{
			if (index != 0)
			{
				priorityTermDateRule.append(",");
			}
			priorityTermDateRule.append("'").append(stateOfIssueList.get(index).getIdValue().trim()).append("'");

		}
		priorityTermDateRule.append(")");

		Query<String> query = createQuery(priorityTermDateRule.toString(), "CmDetermineTerminationEffectiveDateRuleHelper_Impl");
		query.bindId("terminationDateRuleCharType", cisDivisionCharType.getId());
		query.bindDate("latestBillDueDate", latestBillDueDate);

		return query;
	}

	public void validateSoftParameters() {

		PreparedStatement preparedStatement = null;
		SQLResultRow sqlResultRow = null;
		String featureConfigName = "";
		BigInteger count = BigInteger.ZERO;

		try {
			// Retrieve soft parameter values from Feature Configuration
			preparedStatement = createPreparedStatement(CmDetermineTerminationDateRuleConstants.RETRIVE_FEATURE_CONFIG_VAL.toString(), "CmDetermineTerminationEffectiveDateRuleHelper_Impl");

			preparedStatement.bindString("extSysType", CmDetermineTerminationDateRuleConstants.EXTERNAL_SYSTEM_TYPE_FLG, "EXT_SYS_TYP_FLG");

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

		if (!count.equals(BigInteger.ONE)) {
			if (count.equals(0))
				addError(MessageRepository.missingFeatureConfig(featureConfigName));
			else
				addError(MessageRepository.multipleFeatureConfig(featureConfigName));
		}

		FeatureConfiguration tdFeatureConfiguration = new FeatureConfiguration_Id(featureConfigName).getEntity();
		featureConfigName = tdFeatureConfiguration.fetchLanguageDescription();
		// Get the Option value policy status option type.
		policyStatusOptionType = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.POLICY_OPTION_TYPE);
		// Get the Option value policy status option value.
		policyStatusOptionValue = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.POLICY_ACTIVE_OPTION_VAL);
		// Get the policy terminated status option value.
		policyTerminatedStatusOptVal = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.POLICY_TERMINATED_OPTION_VAL);
		// Get the Option value state of issue char type.
		stateOfIssue = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.STATE_OF_ISSUE_CHAR_TYPE);
		// Get the Option value termination date rule char type.
		terminationDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.TERM_DATE_RULE_CHAR_TYPE);
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
		// Get the Option value retro paid percentage CharType .
		retroPaidPercentage = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.RETRO_PAID_PERCENT_THRESHOLD_CHAR_TYPE);
		// Get the Option value date of processing termination Date Rule.
		dateOfProcesingTerminationDateRule = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.DATE_OF_PROCESSING_TERM_DATE_RULE);
		// Get the Option value date Of Processing Number Of Days Prior CharType
		dateOfProcessingNumberOfDaysPrior = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.DATE_OF_PROCESSING_NUMBER_OF_DAYS_PRIOR_CHAR_TYPE);
		// Get the Option value policy plan characteristic type.
		policyPlan = fetchFeatureConfigValues(tdFeatureConfiguration, featureConfigName, DetEffTermDateRuleForOverdueLookup.constants.POLICY_PLAN_CHAR_TYPE);

		// Validate Option Values for Characteristic Type
		stateOFIssueCharType = validateAndFetchCharType(stateOfIssue);

		terminationDateRuleCharType = validateAndFetchCharType(terminationDateRule);

		retroPaidPercentageCharType = validateAndFetchCharType(retroPaidPercentage);

		dateOfProcessingNumberOfDaysPriorCharType = validateAndFetchCharType(dateOfProcessingNumberOfDaysPrior);

		policyPlanCharType = validateAndFetchCharType(policyPlan);

		// Validate Characteristics Type for Entity
		validateCharTypeForEntity(stateOFIssueCharType, CharacteristicEntityLookup.constants.POLICY);

		validateCharTypeForEntity(policyPlanCharType, CharacteristicEntityLookup.constants.SERVICE_AGREEMENT);

		validateCharTypeForEntity(terminationDateRuleCharType, CharacteristicEntityLookup.constants.CIS_DIVISION);

		validateCharTypeForEntity(retroPaidPercentageCharType, CharacteristicEntityLookup.constants.CIS_DIVISION);

		validateCharTypeForEntity(dateOfProcessingNumberOfDaysPriorCharType, CharacteristicEntityLookup.constants.CIS_DIVISION);

	}

	public String fetchFeatureConfigValues(FeatureConfiguration tdFeatureConfiguration, String featureConfigName, Lookup processLookup)
	{
		FeatureConfigurationOptionInfo featureConfigInfo = null;
		String featureConfigOptValue = "";
		featureConfigInfo = FeatureConfigurationOptionsCache.getHighestSequenceOptionFor(ExternalSystemTypeLookup.constants.DET_EFF_TERM_DATE_RULE_FOR_OVERDUE, processLookup);

		if (isNull(featureConfigInfo)) {
			addError(MessageRepository.missingFeatureConfigOptType(featureConfigName, processLookup.toLocalizedString()));
		}
		else
		{
			// Get the option value.
			featureConfigOptValue = featureConfigInfo.getValue();
		}
		return featureConfigOptValue;
	}

	/**
	 * Validates that the Characteristic Value is valid for the given Characteristic Type.
	 * 
	 * @param charType
	 * @param charValue
	 */
	private CharacteristicType validateAndFetchCharType(String charTypeVal) {

		CharacteristicType_Id charTypeId = new CharacteristicType_Id(charTypeVal);

		if (isNull(charTypeId) && isNull(charTypeId.getEntity())) {

			addError(MessageRepository.invalidDelProcCharType(charTypeVal));

		}
		CharacteristicType charType = charTypeId.getEntity();

		return charType;
	}

	/**
	 * Validates that the Characteristic Type is valid for the given entity.
	 * 
	 * @param charType
	 * @param charEntity
	 */
	private void validateCharTypeForEntity(CharacteristicType charType, CharacteristicEntityLookup charEntity) {

		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntity);
		if (isNull(charEntityId.getEntity())) {
			addError(MessageRepository.charTypeIsInvalidForEntity(charType.getId(),
					charEntity.getLookupValue().getEffectiveDescription()));
		}
	}

	/**
	 * Get Primary Customer of Account
	 * @return personId
	 */

	private Person_Id getPrimaryCustomer(Account_Id acctId) {

		PreparedStatement pst = null;
		Person_Id personId = null;
		try {
			pst = createPreparedStatement(
					CmDetermineTerminationDateRuleConstants.GET_PRIM_CUST.toString(),
					"CmDetermineTerminationEffectiveDateRuleHelper_Impl");
			pst.bindId("accountId", acctId);
			List<SQLResultRow> result = pst.list();
			pst.close();
			if (!isNull(result) && result.size() > 0) {
				SQLResultRow output = result.get(0);
				personId = new Person_Id(output.getString("PER_ID"));
			}
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}
		return personId;

	}

	/**
	 * Get Lates Bill for Delinquency Process
	 * @return billId
	 */

	private Date fetchLatestBillDueDate(CmDelinquencyProcess_Id delProcId) {

		Date latestDueDate = null;

		Query<Date> query = createQuery(CmDetermineTerminationDateRuleConstants.RET_LATEST_BILL_DUE_DT.toString(), "CmDetermineTerminationEffectiveDateRuleHelper_Impl");
		query.bindId("delinquencyProcId", delProcId);
		query.bindLookup("collectingObjTypeFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);
		query.addResult("billDate", "BI.dueDate");
		query.orderBy("billDate", Query.DESCENDING);

		if (notNull(query.firstRow())) {

			latestDueDate = query.firstRow();
		}

		return latestDueDate;
	}

}

