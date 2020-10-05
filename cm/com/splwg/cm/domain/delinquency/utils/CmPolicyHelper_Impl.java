/*                                                               
 *******************************************************************************************************                                                               
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 * Policy Helper Business components
 * 
 * This helper class contains common methods used for policy across various
 * modules like check if account has active policy or determing policy status                                                                                             
 *******************************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:    	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2019-06-18   NIMYAKAL	Initial Version.    
 * *********************************************************************************************************
 * */
package com.splwg.cm.domain.delinquency.utils;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import com.splwg.base.api.BusinessEntity;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.datatypes.DateFormatParseException;
import com.splwg.base.api.datatypes.DateTime;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.api.service.PageBody;
import com.splwg.base.api.service.PageHeader;
import com.splwg.base.api.service.ServiceDispatcher;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.StandardMessages;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectStatusOption;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReasonCharacteristic;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.base.support.schema.BusinessObjectInfo;
import com.splwg.base.support.schema.BusinessObjectInfoCache;
import com.splwg.ccb.api.lookup.BillSegmentStatusLookup;
import com.splwg.ccb.api.lookup.BillableChargeStatusLookup;
import com.splwg.ccb.domain.adjustment.adjustment.Adjustment;
import com.splwg.ccb.domain.adjustment.adjustment.AdjustmentCharacteristic;
import com.splwg.ccb.domain.adjustment.adjustment.AdjustmentCharacteristic_DTO;
import com.splwg.ccb.domain.admin.billCancelReason.BillCancelReason;
import com.splwg.ccb.domain.admin.statementRoutingType.StatementRoutingType;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.BillCharacteristic;
import com.splwg.ccb.domain.billing.bill.BillMaintenanceService;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.billing.billSegment.BillSegment;
import com.splwg.ccb.domain.billing.billSegment.BillSegment_Id;
import com.splwg.ccb.domain.billing.billableCharge.BillableCharge;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeCharacteristics;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeLine;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeLine_DTO;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeLine_Id;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeLines;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeServiceQuantities;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeServiceQuantity;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeServiceQuantity_DTO;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeServiceQuantity_Id;
import com.splwg.ccb.domain.billing.billableCharge.BillableCharge_DTO;
import com.splwg.ccb.domain.billing.billableCharge.BillableCharge_Id;
import com.splwg.ccb.domain.billing.billableCharge.BilllableChargeCharacteristic;
import com.splwg.ccb.domain.billing.billableCharge.BilllableChargeCharacteristic_DTO;
import com.splwg.ccb.domain.billing.billableCharge.BilllableChargeCharacteristic_Id;
import com.splwg.ccb.domain.common.c1Request.C1Request;
import com.splwg.ccb.domain.common.c1Request.C1RequestLog;
import com.splwg.ccb.domain.common.c1Request.C1Request_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristics;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.ccb.domain.insurance.membership.Membership;
import com.splwg.ccb.domain.insurance.membership.Membership_Id;
import com.splwg.ccb.domain.insurance.policy.Policy;
import com.splwg.ccb.domain.insurance.policy.PolicyLog;
import com.splwg.ccb.domain.insurance.policy.PolicyPerson;
import com.splwg.ccb.domain.insurance.policy.Policy_Id;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.shared.common.ServerMessage;
import com.splwg.shared.common.StringUtilities;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author ni3myakal
 *
@BusinessComponent (customizationCallable = true, customizationReplaceable = true)
 */
public class CmPolicyHelper_Impl extends GenericBusinessComponent implements
		CmPolicyHelper {

	Logger logger = LoggerFactory.getLogger(CmPolicyHelper_Impl.class);

	/**
	 * This method checks if person has any active policies
	 * @param personId - person Id
	 * 		  processDate - process date
	 * 		  optionType - option type on policy business object
	 * 		  optionValue - value of policy bo's option type
	 * @return policyIdList
	 */
	public boolean isActiveCustomer(Person_Id personId, Date processDate, String optionType, String optionValue) {
		PreparedStatement ps = null;
		boolean isActiveCustomer = false;

		try {
			ps = createPreparedStatement(CmPolicyConstants.CHECK_ACTIVE_POLICY_LIST.toString(), "CmPolicyHelper_Impl");
			ps.bindId("personId", personId);
			ps.bindDate("processDate", processDate);
			ps.bindString("optionType", optionType, "BO_OPT_FLG");
			ps.bindStringProperty("activeOptionVal", BusinessObjectStatusOption.properties.value, optionValue);
			ps.bindStringProperty("terminatedOptionVal",BusinessObjectStatusOption.properties.value,CmPolicyConstants.TEMINATED_STATUS);
			SQLResultRow sqlResultRow = ps.firstRow();
			if (notNull(sqlResultRow)) {
				isActiveCustomer = true;
			}

		} finally {
			if (notNull(ps)) {
				ps.close();
				ps = null;
			}
		}
		return isActiveCustomer;
	}

	

	@SuppressWarnings({ "unused", "rawtypes" })
	public void addPolicyMOLog(ServerMessage message, List charValues, LogEntryTypeLookup logEntryType, CharacteristicType charType, BusinessObjectInstanceKey boInstanceKey, Policy policy)
	{
		BusinessObjectInfo boinfo = BusinessObjectInfoCache.getBusinessObjectInfo(boInstanceKey.getBusinessObject().getId().getIdValue());
		MaintenanceObjectLogHelper<Policy, PolicyLog> logHelper = new MaintenanceObjectLogHelper<Policy, PolicyLog>(boInstanceKey.getBusinessObject().getMaintenanceObject(), policy);
		BusinessEntity fkCharEntity = getFKEntity(charType, charValues);
		logHelper.addLogEntry(logEntryType, message, null, charType, fkCharEntity);
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public void addPolicyMOLog(ServerMessage message, List charValues, LogEntryTypeLookup logEntryType, CharacteristicType charType, BusinessObject bo, Policy policy, String longDescription)
	{
		BusinessEntity fkCharEntity = null;
		String charvalue = null;
		BusinessObjectInfo boinfo = BusinessObjectInfoCache.getBusinessObjectInfo(bo.getId().getIdValue());
		MaintenanceObjectLogHelper<Policy, PolicyLog> logHelper = new MaintenanceObjectLogHelper<Policy, PolicyLog>(bo.getMaintenanceObject(), policy);
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
	
}


