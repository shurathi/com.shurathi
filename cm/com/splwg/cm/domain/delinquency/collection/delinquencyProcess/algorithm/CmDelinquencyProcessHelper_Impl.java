/*                                                                
 ****************************************************************************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Delinquency Process Helper Business components
 * 
 * This helper class contains common methods used for Delinquency Process across various
 * modules.                                                  
 ****************************************************************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 ****************************************************************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.BusinessEntity;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.Query;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.StandardMessages;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusOption;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.base.support.schema.BusinessObjectInfo;
import com.splwg.base.support.schema.BusinessObjectInfoCache;
import com.splwg.ccb.api.lookup.BillableChargeStatusLookup;
import com.splwg.ccb.api.lookup.CustomerManagementOptionLookup;
import com.splwg.ccb.domain.admin.cisDivision.CisDivision_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.BusinessObjectStatusOptionTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyProcessCalculateTriggerDateConstants;
import com.splwg.cm.domain.delinquency.admin.overdueProcessTemplate.CmCancelCriteriaConstants;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.shared.common.ServerMessage;
import com.splwg.shared.common.StringUtilities;

/**
 * @author MugdhaP
 *
@BusinessComponent (customizationCallable = true, customizationReplaceable = true)
 */
public class CmDelinquencyProcessHelper_Impl extends GenericBusinessComponent implements
		CmDelinquencyProcessHelper {
	@SuppressWarnings({ "rawtypes", "unused" })
	public void addDelinquencyMOLog(ServerMessage message, List charValues, LogEntryTypeLookup logEntryType, CharacteristicType charType, BusinessObject bo, CmDelinquencyProcess delinquencyProcess, String longDescription)
	{
		BusinessEntity fkCharEntity = null;
		String charvalue = null;
		BusinessObjectInfo boinfo = BusinessObjectInfoCache.getBusinessObjectInfo(bo.getId().getIdValue());
		MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog>(bo.getMaintenanceObject(), delinquencyProcess);
		if (charType.getCharacteristicType().isForeignKeyValue()) {
			fkCharEntity = getFKEntity(charType, charValues);
			if (notNull(longDescription)) {
				logHelper.addLogEntry(logEntryType, message, longDescription, charType, fkCharEntity);
			}
			else {
				logHelper.addLogEntry(logEntryType, message, null, charType, fkCharEntity);
			}
		}
		else if (charType.getCharacteristicType().isAdhocValue() || charType.getCharacteristicType().isPredefinedValue()) {
			String charValue = null;
			Iterator i = charValues.iterator();
			do
			{
				if (!i.hasNext())
					break;
				int idx = 1;
				String id = (String) i.next();
				if (StringUtilities.isBlankOrNull(id)) {
					if (charType.getCharacteristicType().isAdhocValue())
						addError(StandardMessages.fieldMissing((new StringBuilder()).append("ADHOC_CHAR_VAL").append(idx).toString()));

					if (charType.getCharacteristicType().isPredefinedValue())
						addError(StandardMessages.fieldMissing((new StringBuilder()).append("CHAR_VAL").append(idx).toString()));

				}
				charValue = id;
				if (notNull(longDescription)) {
					logHelper.addLogEntry(logEntryType, message, longDescription, charType, charValue);
				}
				else {
					logHelper.addLogEntry(logEntryType, message, null, charType, charValue);
				}

			} while (true);

		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BusinessEntity getFKEntity(CharacteristicType charType, List charValIds)
	{
		BusinessEntity entity = null;
		EntityId entityId = null;
		if (isNull(charValIds))
			addError(StandardMessages.fieldMissing("CHAR_VAL_FK1"));
		Iterator i = charValIds.iterator();
		do
		{
			if (!i.hasNext())
				break;
			int idx = 1;
			String id = (String) i.next();
			if (StringUtilities.isBlankOrNull(id))
				addError(StandardMessages.fieldMissing((new StringBuilder()).append("CHAR_VAL_FK").append(idx).toString()));
		} while (true);
		if (notNull(charType.fetchForeignKeyReference())) {
			entityId = charType.fetchForeignKeyReference().getEntityId(charValIds);
			entity = entityId.getEntity();
		}
		if (entity == null)
			addError(StandardMessages.recordNotFoundError(charType.getId()));
		return entity;
	}


	/**
	 * This method retrieves Person from delinquency process
	 * @param delinquencyProcess
	 * @return Person
	 */
	public Person fetchPersonOfDelinquencyProcess(CmDelinquencyProcess delinquencyProcess) {
		Query<String> query = createQuery(CmDelinquencyProcessConstant.DELINQUENCY_REL_OBJ_PERSON_QUERY.toString(), "CmDelinquencyProcessHelper_Impl");
		query.bindEntity("delinquencyProcess", delinquencyProcess);
		query.addResult("pkValue1", "DELRO.id.primaryKeyValue1");
		if (notBlank(query.firstRow())) {
			return new Person_Id(query.firstRow()).getEntity();
		}
		else {
			return null;
		}

	}

	/**
	 * This method retrieves Account from delinquency process
	 * @param delinquencyProcess
	 * @return Account
	 */
	public Account fetchAccountOfDelinquencyProcess(CmDelinquencyProcess delinquencyProcess) {
		Query<String> query = createQuery(CmDelinquencyProcessConstant.DELINQUENCY_REL_OBJ_ACCOUNT_QUERY.toString(), "CmDelinquencyProcessHelper_Impl");
		query.bindEntity("delinquencyProcess", delinquencyProcess);
		query.addResult("pkValue1", "DELRO.id.primaryKeyValue1");
		if (notBlank(query.firstRow())) {
			return new Account_Id(query.firstRow()).getEntity();
		}
		else {
			return null;
		}
	}

	/**
	 * This method retrieves on account payments for contracts configured in feature configuration
	 * @param Person
	 * @param adminstrativeContractTypeFeatureConfig
	 * @param adminstrativeContractTypeOptionType
	 * @return on account payments in BugDecimal format
	 */
	public BigDecimal fetchOnAccountPaymentsForPerson(Person person, FeatureConfiguration adminstrativeContractTypeFeatureConfig,
			CustomerManagementOptionLookup adminstrativeContractTypeOptionType) {

		BigDecimal onAcctPayment = BigDecimal.ZERO;
		
		Query<Money> query = createQuery(CmDelinquencyProcessConstant.CHECK_ON_ACCOUNT_PAYMENT_FOR_PERSON.toString(), "CmDelinquencyProcessHelper_Impl");

		query.bindEntity("person", person);
		query.bindEntity("featureConfig", adminstrativeContractTypeFeatureConfig);
		query.bindLookup("featureConfigOptionType", adminstrativeContractTypeOptionType);
		query.bindBoolean("isFrozen", Bool.TRUE);
		query.addResult("amount", "NVL(SUM(FT.currentAmount),0)");
		
		Money row = query.firstRow();
		if (row.compareTo(Money.ZERO) != 0) {
			onAcctPayment = row.negate().getAmount();
		}
		
		return onAcctPayment;
	}

	/**
	 * This method retrieves on account payments for contracts configured in feature configuration
	 * @param account
	 * @param adminstrativeContractTypeFeatureConfig
	 * @param adminstrativeContractTypeOptionType
	 * @return on account payments in BugDecimal format
	 */
	public BigDecimal checkOnAcctPaymentForAccount(Account account, FeatureConfiguration adminstrativeContractTypeFeatureConfig,
			CustomerManagementOptionLookup adminstrativeContractTypeOptionType) {

		Query<Money> query = createQuery(CmCancelCriteriaConstants.CHECK_ON_ACCOUNT_PAYMENT.toString(),
				"CmDelinquencyProcessHelper_Impl");

		query.bindId("overdueAccountId", account.getId());
		query.bindEntity("featureConfig", adminstrativeContractTypeFeatureConfig);
		query.bindLookup("featureConfigOptionType", adminstrativeContractTypeOptionType);
		query.bindBoolean("isFrozen", Bool.TRUE);
		query.bindBoolean("isNotInArrears", Bool.FALSE);

		query.addResult("amount", "SUM(FT.currentAmount)");
		Money row = query.firstRow();
		return row.getAmount();
	}

	/**
	 * This method retrieves all accounts of person
	 * @param person
	 * @return list of account
	 */
	public List<Account> fetchAccountListForPerson(Person person) {

		Query<Account> query = createQuery(CmDelinquencyProcessConstant.ACCOUNT_PERSONS_FOR_PERSON.toString(),
				"CmDelinquencyProcessHelper_Impl");

		query.bindEntity("person", person);
		query.addResult("account", "AP.id.account");
		return query.list();

	}


	/**
	 * This method fetches state of issues for customer
	 * @param personId
	 * 		  latestDueDate
	 * @return List<CisDivision_Id> stateOfIssueList
	 */
	public List<CisDivision_Id> fetchStateOIssueForMainCustomer(Person_Id personId, Date latestDueDate, CharacteristicType policyPlanCharacteristicType,
			BusinessObjectStatusOptionTypeLookup policyBusinessObjectStatusOptionType, CharacteristicType stateOfIssueCharacteristicType,
			String activePolicyBusinessObjectOptionTypeValue, String terminatedPolicyBusinessObjectOptionTypeValue) {
		List<CisDivision_Id> stateOfIssueList = null;

		PreparedStatement ps = null;

		try {
			ps = createPreparedStatement(CmDelinquencyProcessCalculateTriggerDateConstants.STATE_OF_ISSUE_CUSTOMER_LVL.toString(), "CmDelinquencyProcessHelper_Impl");
			ps.bindId("personId", personId);
			ps.bindDate("latestBillDueDate", latestDueDate);
			ps.bindEntity("policyPlanCharType", policyPlanCharacteristicType);
			ps.bindLookup("polStatOptionType", policyBusinessObjectStatusOptionType);
			ps.bindStringProperty("polStatActOptionVal", BusinessObjectStatusOption.properties.value, activePolicyBusinessObjectOptionTypeValue);
			ps.bindStringProperty("polStatTermOptionVal", BusinessObjectStatusOption.properties.value, terminatedPolicyBusinessObjectOptionTypeValue);
			ps.bindDate("processDate", getProcessDateTime().getDate());
			ps.bindEntity("stateOfIssueCharType", stateOfIssueCharacteristicType);
			ps.bindLookup("billable", BillableChargeStatusLookup.constants.BILLABLE);

			List<SQLResultRow> resultList = ps.list();
			CisDivision_Id stateOfIssueId = null;

			if (notNull(resultList) && !resultList.isEmpty()) {

				stateOfIssueList = new ArrayList<CisDivision_Id>();

				// Take distinct value if there is repeataion of state of issue
				for (SQLResultRow resultRow : resultList) {
					String stateOfIssueStr = resultRow.getString("STATE_OF_ISSUE");
					if (notBlank(stateOfIssueStr)) {
						stateOfIssueId = new CisDivision_Id(stateOfIssueStr);
						if (notNull(stateOfIssueId.getEntity())) {
							stateOfIssueList.add(stateOfIssueId);
						}
					}
				}
			}

		} finally {
			if (notNull(ps)) {
				ps.close();
				ps = null;
			}
		}

		return stateOfIssueList;
	}

	public BigDecimal fetchTotalBilledAmtForCurrRevPeriodForCustomer(Person_Id personId, Date latestDueDate) {
		Query<Money> query = createQuery(CmDelinquencyProcessConstant.TOT_BILLED_AMOUNT_CUSTOMER_LVL.toString(), "CmDelinquencyProcessHelper_Impl");
		query.bindId("personId", personId);
		query.bindDate("latestDueDate", latestDueDate);
		query.addResult("amount", "SUM(FT.currentAmount)");

		BigDecimal totalCustomerOriginalAmount = query.firstRow().getAmount();

		return totalCustomerOriginalAmount;

	}

}

