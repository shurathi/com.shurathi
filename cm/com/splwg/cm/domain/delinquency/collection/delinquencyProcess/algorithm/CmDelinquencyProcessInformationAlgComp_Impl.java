/*
 **********************************************************************************************************************************************
 * This Algorithms Returns the Information String of Delinquency Process
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:          Reason:
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **********************************************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateInterval;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectStatusConditionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.businessObject.BusinessObjectInfoAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatus_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyCustomerMonitorRuleConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyProcessCalculateTriggerDateConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmCalculateUnpaidOriginalAmountAlgorithmSpot;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObjects;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = field1, type = string)
 *            , @AlgorithmSoftParameter (name = field2, type = string)
 *            , @AlgorithmSoftParameter (name = field3, type = string)
 *            , @AlgorithmSoftParameter (name = field4, type = string)
 *            , @AlgorithmSoftParameter (name = field5, type = string)
 *            , @AlgorithmSoftParameter (name = field6, type = string)
 *            , @AlgorithmSoftParameter (name = field7, type = string)
 *            , @AlgorithmSoftParameter (name = field8, type = string)
 *            , @AlgorithmSoftParameter (name = field9, type = string)
 *            , @AlgorithmSoftParameter (name = field10, type = string)
 *            , @AlgorithmSoftParameter (name = field11, type = string)
 *            , @AlgorithmSoftParameter (name = field12, type = string)
 *            , @AlgorithmSoftParameter (name = field13, type = string)
 *            , @AlgorithmSoftParameter (name = field14, type = string)
 *            , @AlgorithmSoftParameter (name = field15, type = string)
 *            , @AlgorithmSoftParameter (name = field16, type = string)
 *            , @AlgorithmSoftParameter (name = field17, type = string)
 *            , @AlgorithmSoftParameter (name = field18, type = string)
 *            , @AlgorithmSoftParameter (name = field19, type = string)
 *            , @AlgorithmSoftParameter (name = field20, type = string)
 *            , @AlgorithmSoftParameter (name = field21, type = string)
 *            , @AlgorithmSoftParameter (name = field22, type = string)
 *            , @AlgorithmSoftParameter (name = field23, type = string)
 *            , @AlgorithmSoftParameter (name = field24, type = string)
 *            , @AlgorithmSoftParameter (name = field25, type = string)
 *            , @AlgorithmSoftParameter (name = field26, type = string)
 *            , @AlgorithmSoftParameter (name = field27, type = string)
 *            , @AlgorithmSoftParameter (name = field28, type = string)
 *            , @AlgorithmSoftParameter (name = field29, type = string)
 *            , @AlgorithmSoftParameter (name = field30, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = unpaidAmountDueCharacteristicType, required = true, type = entity)})
 */

public class CmDelinquencyProcessInformationAlgComp_Impl extends
		CmDelinquencyProcessInformationAlgComp_Gen implements
		BusinessObjectInfoAlgorithmSpot {

	private static final Logger logger = LoggerFactory
			.getLogger(CmDelinquencyProcessInformationAlgComp_Impl.class);
	private CmDelinquencyProcess delinquencyProcess;
	private boolean isDelimiter = false;
	private String lastValueAdded = CmDelinquencyProcessConstant.EMPTY;
	private String cmDelinquencyProcessInfo = CmDelinquencyProcessConstant.EMPTY;;
	private List<String> fieldList;
	private String lastEntry = CmDelinquencyProcessConstant.EMPTY;
	private List<Integer> exclusionList = new ArrayList<Integer>();

	@Override
	public void invoke() {
		try {
			if (isNull(delinquencyProcess)) {
				return;
			}

			if (!isValidAlgorithmParameters()) {
				logger.debug("Algorith Parameters are invalid");
				return;
			}
			captureLastEntry();
			populateDelimiterExemptionList();
			createDelinquencyProcessInformation();

		} catch (Exception e) {
			// Information Routines do not issue errors
			logger.debug(e.getMessage());
			return;
		}

	}

	@Override
	public String getInfo() {
		return cmDelinquencyProcessInfo;
	}

	@Override
	public void setBusinessObject(BusinessObjectInstance businessObjectInstance) {
		delinquencyProcess = businessObjectInstance.getEntity("delinquencyProcessId", CmDelinquencyProcess.class);

	}
	
	private boolean isValidAlgorithmParameters() {
		fieldList = new ArrayList<String>();

		addField(this.getField1());
		addField(this.getField2());
		addField(this.getField3());
		addField(this.getField4());
		addField(this.getField5());
		addField(this.getField6());
		addField(this.getField7());
		addField(this.getField8());
		addField(this.getField9());
		addField(this.getField10());
		addField(this.getField11());
		addField(this.getField12());
		addField(this.getField13());
		addField(this.getField14());
		addField(this.getField15());
		addField(this.getField16());
		addField(this.getField17());
		addField(this.getField18());
		addField(this.getField19());
		addField(this.getField20());
		addField(this.getField21());
		addField(this.getField22());
		addField(this.getField23());
		addField(this.getField24());
		addField(this.getField25());
		addField(this.getField26());
		addField(this.getField27());
		addField(this.getField28());
		addField(this.getField29());
		addField(this.getField30());

		// Verify that the list is not empty, at least one Field must be
		// specified
		return !fieldList.isEmpty();
		// CSON: ExecutableStatementCountCheck
	}

	private void addField(String field) {
		// If soft Parameter is not populated, return
		if (isNull(field))
			return;
		fieldList.add(field);
	}

	private void captureLastEntry() {

		lastEntry = fieldList.get(fieldList.size() - 1);

		if (isDelimiter(lastEntry)) {
			fieldList.remove(fieldList.size() - 1);
		}
	}

	private void checkFirstEntry() {
		String firstEntry = fieldList.get(0);

		// If the first parameter is a delimiter MUST BE CONCATENATED without
		// any restriction
		if (isDelimiter(firstEntry)) {
			exclusionList.add(0);
		}
	}

	private boolean isDelimiter(String val) {
		return val.startsWith(CmDelinquencyProcessConstant.QUOTE)
				&& val.endsWith(CmDelinquencyProcessConstant.QUOTE);
	}

	private void populateDelimiterExemptionList() {
		checkFirstEntry();
		// If at least two continuous parameters = delimiters are defined
		// the first delimiter is restricted by the rules
		// the second and the following contiguous ones
		// MUST BE CONCATENATED without any restriction.
		for (int i = 0; i <= fieldList.size() - 2; i++) {
			if (isDelimiter(fieldList.get(i))
					&& isDelimiter(fieldList.get(i + 1))) {
				exclusionList.add(i + 1);
			}
		}

		int i = 0;
		// If delimiters before last entry has a valid field before it place
		// it in exemption list to avoid being removed in formatToDoInfo (Fix
		// for second error in VIMB-44)
		while (isDelimiter(fieldList.get(fieldList.size() - 1))) {
			i++;

			if (!isDelimiter(fieldList.get(fieldList.size() - (i + 1)))) {
				String nonDelimiterField = getExpandedParameter(fieldList
						.get(fieldList.size() - (i + 1)));
				if (!nonDelimiterField.equals(CmDelinquencyProcessConstant.EMPTY)) {
					exclusionList.add(fieldList.size() - i);
				}
				break;
			}
		}
	}

	/*
	 * This Method compares the soft parameter from algorithm and get the
	 * information of that.
	 */
	private String getExpandedParameter(String val) {

		if (val.equals(CmDelinquencyProcessConstant.PARAM_DELINQUECNY_PROCESS_ID)) {

			return delinquencyProcess.getId().getIdValue().toString();

		} else if (val.equals(CmDelinquencyProcessConstant.PARAM_DELINQUECNY_PROCESS_TYPE_DESC)) {

			return delinquencyProcess.getCmDelinquencyProcessType().fetchLanguageDescription();

		} else if (val.equals(CmDelinquencyProcessConstant.PARAM_AGE)) {

			String ageString = calCulateAge();
			return ageString;

		} else if (val.equals(CmDelinquencyProcessConstant.PARAM_BO_STATUS_DESC)) {

			String boStatusCdDescr = getBoStatusDescr();

			return boStatusCdDescr;

		} else if (val.equals(CmDelinquencyProcessConstant.UNPAIDAMOUNT)) {

			String unpaidAmt = "";
			if (delinquencyProcess.getBusinessObject().getStatuses().equals(CmDelinquencyProcessConstant.FINAL))
			{
				unpaidAmt = CmDelinquencyProcessConstant.EMPTY;
			}
			else
			{
				// If the Delinquency Process Types Collecting On Object Type value is blank
				if (isNull(delinquencyProcess.getCmDelinquencyProcessType().getCmCollectingOnObjectTypeId())) {	
					
				// Retrieve Unpaid Amount Due characteristic on the Delinquency Process
					unpaidAmt = this.getUnpaidAmountFromDelinProcCharacteristic();
				} else {
					unpaidAmt = getUnpaidAmount();
				}
			}

			return unpaidAmt;
		}
		else if (isDelimiter(val)) {
			// For delimiters
			isDelimiter = true;
			return val.substring(1, val.length() - 1);
		}
		else
		{

			return CmDelinquencyProcessConstant.EMPTY;
		}
	}

	/*
	 * This method create the Information Algorithm get the all string and
	 * delimiter and concatenate as per sequence.
	 */
	private void createDelinquencyProcessInformation() {
		StringBuilder delinquencyProcessInformationBuffer = new StringBuilder();
		boolean lastSubstitutionValueWasEmpty = true;
		boolean passedExceptionRule = false;

		int index = 0;

		for (String parameterField : fieldList) {
			boolean delimiter = isDelimiter(parameterField);

			passedExceptionRule = isDelimiterExempted(index);

			index++;
			String val = getExpandedParameter(parameterField);
			
			// If unpaid amount is not retrieved from either collecting on object type or delinquency process characteristic
			if(parameterField.equals(CmDelinquencyProcessConstant.UNPAIDAMOUNT) && isBlankOrNull(val)) {
				break;
			}
			lastSubstitutionValueWasEmpty = conditionallyAppendInfo(
					delinquencyProcessInformationBuffer, lastSubstitutionValueWasEmpty,
					delimiter, passedExceptionRule, val);

		}

		cmDelinquencyProcessInfo = formatMaxToDoInfoLength(
				delinquencyProcessInformationBuffer.toString(), passedExceptionRule);
	}

	/*
	 * This method get the business status description from the delinquency
	 * process id.
	 */

	public String getBoStatusDescr()
	{
		String boStatusCdDescr = "";

		StringBuffer bostsDescr = new StringBuffer(" ");
		bostsDescr.append(" SELECT DESCR FROM F1_BUS_OBJ_STATUS_L BOSTS, CM_DELIN_PROC DP ");
		bostsDescr.append(" WHERE DP.CM_DELIN_PROC_ID= :delinquencyProcessId ");
		bostsDescr.append(" AND BOSTS.BUS_OBJ_CD=DP.BUS_OBJ_CD ");
		bostsDescr.append(" AND BOSTS.BO_STATUS_CD=DP.BO_STATUS_CD ");
		SQLResultRow outBoStatusDescr = null;
		PreparedStatement query = null;

		try
		{
			query = createPreparedStatement(bostsDescr.toString(), "Delinquency Process Business Object Status Description");
			query.bindString("delinquencyProcessId", delinquencyProcess.getId().getIdValue().toString(), "CM_DELIN_PROC_ID");
			outBoStatusDescr = query.firstRow();

			if (notNull(outBoStatusDescr)) {
				boStatusCdDescr = outBoStatusDescr.getString("DESCR");
			}
		}

		finally {
			if (notNull(query)) {
				query.close();
				query = null;
			}

		}

		return boStatusCdDescr;
	}

	/*
	 * This method calculates the number of days for completed for the status.
	 * if BO status condition is final then it takes the difference of status
	 * update date time with process date. else takes the difference of create
	 * date time with process date.
	 */
	public String calCulateAge()
	{
		Date finalizeDate = null;
		String boStatusCd = delinquencyProcess.getStatus();
		long age;
		String postString = null;
		BusinessObjectStatus businessObjectStatus = new BusinessObjectStatus_Id(delinquencyProcess.getBusinessObject(), boStatusCd).getEntity();
		if (businessObjectStatus.getCondition().equals(BusinessObjectStatusConditionLookup.constants.FINAL))
		{
			finalizeDate = delinquencyProcess.getStatusDateTime().getDate();
			age = determineAge(finalizeDate);
			if (finalizeDate.isBefore(getProcessDateTime().getDate()))
			{
				postString = " " + age + CmDelinquencyProcessConstant.DAYS_AGO;
			}
			else
			{
				postString = " " + age + CmDelinquencyProcessConstant.DAYS_FROM_TODAY;
			}
		}
		else
		{
			finalizeDate = delinquencyProcess.getCreationDateTime().getDate();
			age = determineAge(finalizeDate);
			if (finalizeDate.isBefore(getProcessDateTime().getDate()))
			{
				postString = " " + age + CmDelinquencyProcessConstant.DAYS_AGO;
			}
			else
			{
				postString = " " + age + CmDelinquencyProcessConstant.DAYS_FROM_TODAY;
			}
		}
		return postString;
	}

	public long determineAge(Date finalizeDate)
	{
		long days;
		DateInterval dateDifference = null;
		if (notNull(finalizeDate))
			dateDifference = getProcessDateTime().getDate().difference(finalizeDate);

		days = Math.abs(dateDifference.getTotalDays());

		return days;
	}

	private String formatMaxToDoInfoLength(String val,
			boolean passedExceptionRule) {
		String value = val;

		// If Last entry added is a delimiter, remove it, this scenario is
		// caused by the last field processed being EMPTY
		if (isDelimiter && !passedExceptionRule) {
			value = value.substring(0,
					value.length() - (lastValueAdded.length()));
		}

		// Add the intentional last delimiter
		if (isDelimiter(lastEntry)) {
			value = value + lastEntry.substring(1, lastEntry.length() - 1);
		}

		// Cut the to do Information to the maximum length
		if (value.length() > CmDelinquencyProcessConstant.MAX_TO_DO_INFORMATION_LENGTH) {
			value = value.substring(0,
					CmDelinquencyProcessConstant.MAX_TO_DO_INFORMATION_LENGTH - 1);
		}
		return value;
	}

	private boolean isDelimiterExempted(int currentIndex) {
		for (Iterator<Integer> iterInt = exclusionList.iterator(); iterInt
				.hasNext();) {
			int currentIndexofExceptionList = iterInt.next();

			if (currentIndex == currentIndexofExceptionList) {
				return true;
			}

		}
		return false;
	}

	private boolean conditionallyAppendInfo(
			StringBuilder policyInformationBuffer,
			boolean previousSubstitutionValueWasEmpty, boolean delimiter,
			boolean passedExceptionRule, String val) {
		boolean lastSubstitutionValueWasEmpty = previousSubstitutionValueWasEmpty;

		// Add only if value is not a delimiter or if it is a delimiter but
		// previous entry was not null
		if (!delimiter) {
			lastSubstitutionValueWasEmpty = false;

			policyInformationBuffer.append(val);
			if (!val.equals(CmDelinquencyProcessConstant.EMPTY)) {
				isDelimiter = false;
			} else {
				lastSubstitutionValueWasEmpty = true;
			}

		} else if (delimiter && !lastSubstitutionValueWasEmpty) {
			policyInformationBuffer.append(val);
			lastValueAdded = val;

		} else if (delimiter && passedExceptionRule) {
			policyInformationBuffer.append(val);
			lastValueAdded = val;
		}

		return lastSubstitutionValueWasEmpty;
	}

	/*
	 * This method calculates the unpaid amount from bilId.
	 */
	public String getUnpaidAmount()
	{
		BigDecimal totalUnpaidAmt = BigDecimal.ZERO;
		BigDecimal billUnpaidAmount = BigDecimal.ZERO;
		Bill bill = null;
		Money totalUnpaidAmount = null;

		String deliproId = delinquencyProcess.getId().getIdValue().toString();
		String billId = "";
		ListFilter<CmDelinquencyProcessRelatedObjects> listFilter = delinquencyProcess.getRelatedObjects().createFilter(" where this.id.maintenanceObject = :maintenceObjectId "
				+ " and this.id.cmDelinquencyRelatedObjTypeFlg = :deliRelObjTypFlg  and this.id.delinquencyProcess.id = :delinquencyProcessId ", "CmDelinquencyProcessInformationAlgComp_Impl");

		listFilter.bindId("maintenceObjectId", new MaintenanceObject_Id(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL));
		listFilter.bindId("delinquencyProcessId", delinquencyProcess.getId());
		listFilter.bindLookup("deliRelObjTypFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);

		List<CmDelinquencyProcessRelatedObjects> relatedObject = listFilter.list();

		try {

			for (Iterator<CmDelinquencyProcessRelatedObjects> iterator = relatedObject.iterator(); iterator.hasNext();) {
				CmDelinquencyProcessRelatedObject cmDelinquencyProcessRelatedObject = (CmDelinquencyProcessRelatedObject) iterator.next();
				billId = cmDelinquencyProcessRelatedObject.getId().getPrimaryKeyValue1();
				bill = new Bill_Id(billId).getEntity();

				CmDelinquencyProcess_Id delinquencyProcessId = new CmDelinquencyProcess_Id(deliproId);

				if (notNull(delinquencyProcessId.getEntity())) {

					CmDelinquencyProcessType_Id delinqProcessTypeId = delinquencyProcessId
							.getEntity().getCmDelinquencyProcessType().getId();
					Query<QueryResultRow> query = createQuery(
							CmDelinquencyCustomerMonitorRuleConstants.FETCH_DELINQ_PROC_TYP_ALG
									.toString(), "CmDelinquencyProcessInformationAlgComp_Impl");
					query.bindId("delinquencyProcessType", delinqProcessTypeId);
					query.addResult("algorithm", "DPTA.algorithm");

					Algorithm algorithm = (Algorithm) query.firstRow();

					if (notNull(algorithm)) {
						CmCalculateUnpaidOriginalAmountAlgorithmSpot algorithmSpot = AlgorithmComponentCache
								.getAlgorithmComponent(algorithm.getId(), CmCalculateUnpaidOriginalAmountAlgorithmSpot.class);

						algorithmSpot.setDelinquencyProcessId(delinquencyProcessId);

						MaintenanceObject_Id maintenanceObjectId = new MaintenanceObject_Id(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL);
						CmDelinquencyProcessRelatedObject_Id cmDelProcRelObjId = new CmDelinquencyProcessRelatedObject_Id(delinquencyProcessId, maintenanceObjectId, CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON, billId);

						if (notNull(cmDelProcRelObjId.getEntity())) {

							algorithmSpot.setDelinquencyProcessRelatedObject(cmDelProcRelObjId.getEntity());
							algorithmSpot.invoke();

							billUnpaidAmount = algorithmSpot.getUnpaidAmount();

							totalUnpaidAmt = totalUnpaidAmt.add(billUnpaidAmount);

						}
					}
				}
			}
			bill = new Bill_Id(billId).getEntity();

			totalUnpaidAmount = new Money(totalUnpaidAmt, bill.getAccount().getCurrency().getId());

		} finally {
			if (!isNull(relatedObject)) {

				relatedObject = null;
			}
		}
		return totalUnpaidAmount.toLocalizedString(bill.getAccount().getCurrency());
	}
	
	/**
	 * This method retrieves the unpaid amount from delinquency process Characteristic.
	 * 
	 * @author vjrane
	 * 
	 * */
	private String getUnpaidAmountFromDelinProcCharacteristic() {
		
				// Fetch Level and entity for delinquency process
				QueryResultRow resultRow = fetchLevelAndEntityForDelinquecnyProcess();

				MaintenanceObject maintenanceObject = resultRow.getEntity("maintenanceObject", MaintenanceObject.class);
				Person_Id personId = null;
				Account_Id accountId = null;

				// If maintenance object is of person
				if (maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_PERSON)) {
					personId = new Person_Id(resultRow.getString("primaryKeyValue1"));

					// Creating query string
					StringBuilder queryString = new StringBuilder()
						.append(" from AccountPerson accountPerson where accountPerson.id.person.id = :personId ")
						.append(" and accountPerson.isMainCustomer = :yes ");

					//	Creating query
					Query<Account_Id> query = createQuery(queryString.toString(),
								"CmDelinquencyProcessInformationAlgComp_Impl.getUnpaidAmountFromDelinProcCharacteristic()");
								
					// Binding reference variables
					query.bindId("personId", personId);
					query.bindBoolean("yes", Bool.TRUE);
					
					// Adding result
					query.addResult("accountId", "accountPerson.id.account.id");

					accountId = query.firstRow();

				} 
				// If maintenance object is of account
				else if (maintenanceObject.getId().getTrimmedValue().equalsIgnoreCase(CmDelinquencyProcessCalculateTriggerDateConstants.MAINTENANCE_OBJ_ACCOUNT)) {
					
					accountId = new Account_Id(resultRow.getString("primaryKeyValue1"));
					
				}
				BigDecimal unpaidAmt = BigDecimal.ZERO;
				if (notNull(delinquencyProcess.getEffectiveCharacteristic(getUnpaidAmountDueCharacteristicType()))) {
					unpaidAmt =	new BigDecimal(delinquencyProcess.getEffectiveCharacteristic(getUnpaidAmountDueCharacteristicType())
						.getSearchCharacteristicValue());
				
					return new Money(unpaidAmt, accountId.getEntity().getCurrency().getId()).toLocalizedString(accountId.getEntity().getCurrency());	
				} 
				else {
					return "";
				}
	}
	
	

	/**
	 * This method fetches level and maintenance object for delinquencyProcessId
	 * 
	 * @author vjrane
	 * 
	 * @return resultRow
	 */
	private QueryResultRow fetchLevelAndEntityForDelinquecnyProcess() {

		Query<QueryResultRow> query = createQuery(CmDelinquencyProcessCalculateTriggerDateConstants.FETCH_DELIQ_LVL_ID.toString(), "CmDelinquencyProcessCancelCriteriaAlgComp_Impl");
		query.bindId("delinProcId", delinquencyProcess.getId());
		query.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
		query.addResult("maintenanceObject", "dpo.id.maintenanceObject");
		query.addResult("primaryKeyValue1", "dpo.id.primaryKeyValue1");

		QueryResultRow resultRow = query.firstRow();

		return resultRow;

	}
}

