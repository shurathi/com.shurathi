/*
 **********************************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm creates an Outbound Message to refer the delinquent customer to CDM for further collections 
 * The Outbound Message schema will store the Customer and Delinquency Process related attributes that are required within the extract file
 * These attributes will be processed by the CDM Referred Cases Extract Batch Control while creating the extract file
 **********************************************************************************************************************************************
 * 
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:          Reason:
 * 2020-05-06   MugdhaP		ANTHM-340 CAB1-9462 Delinquency Framework
 **********************************************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.lookup.OutboundMessageProcessingMethodLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.ccb.api.lookup.MatchEventStatusLookup;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic_DTO;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristic_Id;
import com.splwg.ccb.domain.customerinfo.person.PersonCharacteristics;
import com.splwg.ccb.domain.customerinfo.person.PersonId;
import com.splwg.ccb.domain.customerinfo.person.PersonIds;
import com.splwg.ccb.domain.customerinfo.person.PersonPhone;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.insurance.policy.Policy;
import com.splwg.ccb.domain.insurance.policy.PolicyCharacteristic;
import com.splwg.ccb.domain.insurance.policy.Policy_Id;
import com.splwg.ccb.domain.insurance.policyPlan.PolicyPlan;
import com.splwg.ccb.domain.insurance.policyPlan.PolicyPlanCharacteristic;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.api.lookup.CmDelinquencyProcessTypeSystemEventLookup;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmCalculateUnpaidOriginalAmountAlgorithmSpot;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessTypeAlgorithm;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessTypeAlgorithms;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessCharacteristic;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessConstant;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessHelper;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.ServerMessage;
import com.splwg.base.common.NullHelper;
import com.splwg.base.domain.batch.batchControl.BatchControl;
import com.splwg.base.domain.batch.batchControl.BatchControl_Id;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.currency.Currency_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;
import com.splwg.base.domain.outboundMessage.outboundMessage.OutboundMessage_Id;
import com.splwg.base.domain.outboundMessage.outboundMessageType.OutboundMessageType;
import com.splwg.base.domain.workflow.notificationExternalId.NotificationExternalId;
import com.splwg.base.domain.workflow.notificationExternalId.OutboundMessageProfile;
import com.splwg.base.support.schema.BusinessObjectInfo;
import com.splwg.base.support.schema.BusinessObjectInfoCache;
import com.splwg.base.support.schema.ExtendedLookupValueInfo;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = sourceSysExtLookup, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = notificationExternalId, name = externalSystem, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = outboundMessageType, name = outboundMessageType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = basisUnpaidAmtCalculation, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = unpaidAmtDueCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = officeRegistrationCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = frozenMktSegCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = phoneType, name = billToPhoneType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = phoneType, name = businessPhoneType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = sharedAgmtIndicatorCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = sharedAgmtIndicatorDefaultValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = obligorIdCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = obligatorIdDefaultValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = referralTypeCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = referralFlagCharacteristicValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = outboundMessageIdCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = formatPhoneNumber, required = true, type = lookup)})
 */

public class CmDelinquencyProcReferToCdmAlgComp_Impl extends
		CmDelinquencyProcReferToCdmAlgComp_Gen implements
		BusinessObjectEnterStatusAlgorithmSpot {

	//Hard Parameters
	private BusinessObjectInstanceKey businessObjectInstKey;

	private static final String BLANK = " ";

	
	@Override
	public void invoke() {
			
		CmDelinquencyProcessOutMsgParmsHelper outMsgParms = CmDelinquencyProcessOutMsgParmsHelper.Factory.newInstance();
		//Get Delinquency Id
		CmDelinquencyProcess_Id delinquencyProcessId = new CmDelinquencyProcess_Id(this.businessObjectInstKey.getString("delinquencyProcessId"));
		outMsgParms.setDelinquencyProcessId(delinquencyProcessId);
		
		//Fetch Delinquency Customer
		Person_Id delinquencyCust = fetchDelinquencyCustomer(delinquencyProcessId);
		checkNull(delinquencyCust,MessageRepository.noPersonFoundForDelinquency(delinquencyProcessId.getTrimmedValue()));
		 
		//Fetch Customer Details
		outMsgParms = fetchCustomerDetails(delinquencyCust, outMsgParms);
			
		//Determine Policy and Unpaid Amount
		outMsgParms = fetchPolicyDetails(delinquencyProcessId, delinquencyCust, outMsgParms);
			
		//Determine Collection Location and Customer Id
		outMsgParms = fetchCustomerLocAndCustomerId(outMsgParms, delinquencyCust);
			
		//Create Out-bound Message
		OutboundMessage_Id outMsgId = createOutbound(outMsgParms);
			
		//Create Delinquency Process Logs
		List<String> charValues = new ArrayList<String>();
		charValues.add(outMsgId.getTrimmedValue());
		CmDelinquencyProcessHelper dqProcessHelper = CmDelinquencyProcessHelper.Factory.newInstance();
		dqProcessHelper.addDelinquencyMOLog(MessageRepository.outMsgCreatedDelinquency(outMsgId.getTrimmedValue()), charValues, LogEntryTypeLookup.constants.CREATED
				, getOutboundMessageIdCharacteristicType(), delinquencyProcessId.getEntity().getBusinessObject()
				, delinquencyProcessId.getEntity(), "");
		
		//Check Customer Referral Characteristic 
		checkCustomerReferralChar(delinquencyCust);
	}

	/**
	 * This method will check customer referral char
	 * @param delinquencyCust
	 */
	private void checkCustomerReferralChar(Person_Id delinquencyCust) {
		
		String effectiveReferralFlag = null;
		Date effectiveReferralDate = null;		
		PersonCharacteristics personChars = delinquencyCust.getEntity().getCharacteristics();
		ListFilter<PersonCharacteristic> personCharacteristicFilter = personChars.createFilter("where this.id.characteristicType = :characteristicType order by this.id.effectiveDate desc "
				, this.getClass().getSimpleName()+"_checkCustomerReferralChar");
		personCharacteristicFilter.bindId("characteristicType", getReferralTypeCharacteristicType().getId());
		PersonCharacteristic referralChar = personCharacteristicFilter.firstRow();
		if(notNull(referralChar)){
			if(referralChar.fetchIdCharacteristicType().getCharacteristicType().isPredefinedValue()){
				effectiveReferralFlag = referralChar.getCharacteristicValue().trim();
			}else if(referralChar.fetchIdCharacteristicType().getCharacteristicType().isAdhocValue()){
				effectiveReferralFlag = referralChar.getAdhocCharacteristicValue().trim();
			}
			
			effectiveReferralDate = referralChar.fetchIdEffectiveDate();
		}
		
		if(isBlankOrNull(effectiveReferralFlag)){
			addReferralChar(delinquencyCust);
		}else if("N".equalsIgnoreCase(effectiveReferralFlag)){
			if(getProcessDateTime().getDate().equals(effectiveReferralDate)){
				//Update Referral Characteristic
				PersonCharacteristic_DTO refChardto = referralChar.getDTO();
				if(referralChar.fetchIdCharacteristicType().getCharacteristicType().isPredefinedValue()){
					refChardto.setCharacteristicValue(getReferralFlagCharacteristicValue());
				}else if(referralChar.fetchIdCharacteristicType().getCharacteristicType().isAdhocValue()){
					refChardto.setAdhocCharacteristicValue(getReferralFlagCharacteristicValue());
				}
				referralChar.setDTO(refChardto);
			}else{
				addReferralChar(delinquencyCust);
			}
		}
		
	}

	/**
	 * This method will add referral char on customer
	 * @param delinquencyCust
	 */
	private void addReferralChar(Person_Id delinquencyCust) {
		
		PersonCharacteristic_Id referralCharId = new PersonCharacteristic_Id(delinquencyCust, getReferralTypeCharacteristicType()
				.getId(), getProcessDateTime().getDate());
		
		if(isNull(referralCharId.getEntity())){
			PersonCharacteristic_DTO newPersonChar_DTO = new PersonCharacteristic_DTO();
			newPersonChar_DTO.setId(referralCharId);
			if(getReferralTypeCharacteristicType().getCharacteristicType().isAdhocValue()){
				newPersonChar_DTO.setAdhocCharacteristicValue(getReferralFlagCharacteristicValue());
			}else if(getReferralTypeCharacteristicType().getCharacteristicType().isPredefinedValue()){
				newPersonChar_DTO.setCharacteristicValue(getReferralFlagCharacteristicValue().toUpperCase());
			}
			newPersonChar_DTO.newEntity();
		}
		
	}

	/**
	 * This method will create out-bound message.
	 * @param outMsgParms
	 * @return
	 */
	private OutboundMessage_Id createOutbound(CmDelinquencyProcessOutMsgParmsHelper outMsgParms) {
		
		OutboundMessageProfile outMsgProfile = getExternalSystem().getProfileForType(getOutboundMessageType());
		BatchControl_Id batchCtrlId = outMsgProfile.getBatchControlId();
		BatchControl batchCtrl = batchCtrlId.getEntity();
		String batchCode = batchCtrlId.getTrimmedValue();
		BusinessObjectInstance outboundBoInstance = BusinessObjectInstance.create(getOutboundMessageType().getBusinessObject());
		outboundBoInstance.set("externalSystem", getExternalSystem());
		outboundBoInstance.set("outboundMessageType", getOutboundMessageType());
		outboundBoInstance.set("processingMethod",OutboundMessageProcessingMethodLookup.constants.BATCH);
		outboundBoInstance.set("batchControl", batchCode);
		outboundBoInstance.set("batchNumber", BigDecimal.valueOf(batchCtrl.getNextBatchNumber().longValue()));
		outboundBoInstance.set("creationDateTime", getProcessDateTime());
		
		COTSInstanceNode xmlSourceGroup = outboundBoInstance.getGroup("xmlSource");
		xmlSourceGroup.set("delinquencyProcessId", outMsgParms.getDelinquencyProcessId().getTrimmedValue());
		xmlSourceGroup.set("policyId", outMsgParms.getPolicyId());
		xmlSourceGroup.set("policyNumber", outMsgParms.getPolicyNumber());
		xmlSourceGroup.set("customerId", outMsgParms.getCustomerId());
		xmlSourceGroup.set("customerName", outMsgParms.getCustomerName());
		xmlSourceGroup.set("address1", outMsgParms.getAddress1());
		xmlSourceGroup.set("address2", outMsgParms.getAddress2());
		xmlSourceGroup.set("city", outMsgParms.getCity());
		xmlSourceGroup.set("state", outMsgParms.getState());
		xmlSourceGroup.set("postalCode", outMsgParms.getPostal());
		xmlSourceGroup.set("billGroupContact", outMsgParms.getBillGroupContact());
		xmlSourceGroup.set("phone", outMsgParms.getPhone());
		xmlSourceGroup.set("salesOffice", outMsgParms.getSalesOffice());
		xmlSourceGroup.set("customerType", outMsgParms.getCustomerType());
		xmlSourceGroup.set("policyCancelReason", outMsgParms.getPolicyCancelReason());
		xmlSourceGroup.set("policyCancelDate", outMsgParms.getPolicyCancelDate());
		xmlSourceGroup.set("collectionDate", getProcessDateTime().getDate());
		xmlSourceGroup.set("paymentAmount", outMsgParms.getAmount());
		xmlSourceGroup.set("collectionLocation", outMsgParms.getCollectionLoc());
		xmlSourceGroup.set("sharedArrangementIndicator", outMsgParms.getSharedArrangementIndicator());
		xmlSourceGroup.set("obligorId", outMsgParms.getObligorId());
		outboundBoInstance = BusinessObjectDispatcher.add(outboundBoInstance);
		OutboundMessage_Id outboundMessage = new OutboundMessage_Id(outboundBoInstance.getString("outboundMsgId"));
		checkNull(outboundMessage, MessageRepository.unableToCreateOutboundMessageCDM());
		
		return outboundMessage;
	}

	/**
	 * This method will fetch customer location and customer identifier value
	 * @param outMsgParms
	 * @param delinquencyCust
	 * @return
	 */
	private CmDelinquencyProcessOutMsgParmsHelper fetchCustomerLocAndCustomerId(CmDelinquencyProcessOutMsgParmsHelper outMsgParms, Person_Id delinquencyCust) {
		
		Policy_Id policyId = new Policy_Id(outMsgParms.getPolicyId());
		String sourceSystem = policyId.getEntity().getSourceSystem();
		Document document = getExtendedLookupDocument(getSourceSysExtLookup().getId().getTrimmedValue(),sourceSystem);
		if(notNull(document)){
			Node collLocNode = document.getRootElement().selectSingleNode("collectionLocation");
			checkNull(collLocNode, MessageRepository.missingPolicyCollectionLocation(policyId.getTrimmedValue()));
			outMsgParms.setCollectionLoc(collLocNode.getText());
			
			Node personIdType = document.getRootElement().selectSingleNode("externalSystemIDType");
			checkNull(personIdType, MessageRepository.customerIdentifierTypeNotFound(sourceSystem));

			String personIdValue = fetchPersonIdentifierValue(delinquencyCust, personIdType.getText().trim());
			checkNull(personIdValue, MessageRepository.customerNumberFromSourceSystemNotFound(delinquencyCust.getTrimmedValue(),sourceSystem));
			outMsgParms.setCustomerId(personIdValue);
		}
		return outMsgParms;
	}
	
	/**
	 * This method will get person identifier value for given identifier type
	 * @param delinquencyCust
	 * @param personIdType
	 * @return
	 */
	private String fetchPersonIdentifierValue(Person_Id delinquencyCust,
			String personIdType) {
		String idValue = null;
		PersonIds personIds = delinquencyCust.getEntity().getIds();
		ListFilter<PersonId> personIdsFilter = personIds.createFilter("WHERE this.id.person = :personId and this.id.idType = :idType"
				, this.getClass().getSimpleName()+"_fetchPersonIdentifierValue");
		personIdsFilter.bindId("personId", delinquencyCust);
		personIdsFilter.bindId("idType", new IdType_Id(personIdType));
		if(notNull(personIdsFilter.firstRow())){
			PersonId personId = personIdsFilter.firstRow();
			idValue = personId.getPersonIdNumber();
		}
		return idValue;
	}

	/**
	 * This method will get document object from extended lookup
	 * @param boName
	 * @param lookupName
	 * @return
	 */
	public Document getExtendedLookupDocument(String boName,String lookupName){
		Document document = null;
		BusinessObjectInfo boInfo = BusinessObjectInfoCache.getRequiredBusinessObjectInfo(boName);
		for(ExtendedLookupValueInfo extendedLookupValueInfo: boInfo.getActiveExtendedLookupValues()){
			String lookupValue = extendedLookupValueInfo.getId().getValue();
			if(isBlankOrNull(lookupName)){
				document = extendedLookupValueInfo.getXMLRepresentation();
				break;
			}else if(lookupValue.trim().equals(lookupName.trim())){
				document = extendedLookupValueInfo.getXMLRepresentation();
				break;
			}
		}
		return document;
	}

	/**
	 * This method will retrieve policy details required for out-bound message
	 * @param delinquencyProcessId
	 * @param delinquencyCust 
	 * @param outMsgParms
	 * @throws DocumentException 
	 */
	private CmDelinquencyProcessOutMsgParmsHelper fetchPolicyDetails(CmDelinquencyProcess_Id delinquencyProcessId,
			Person_Id delinquencyCust, CmDelinquencyProcessOutMsgParmsHelper outMsgParms){
		String policy = null;
		String unpaidAmount = null;
		outMsgParms.setSharedArrangementIndicator(getSharedAgmtIndicatorDefaultValue());
		outMsgParms.setObligorId(getObligatorIdDefaultValue());
		//Get currency from customer
		Currency_Id currencyId = getCurrency(delinquencyCust);
		
		//If criteria is D get data from delinquency
		if(CmDelinquencyProcessConstant.BASIS_UNPAID_AMT_D.equalsIgnoreCase(getBasisUnpaidAmtCalculation())){
			CmDelinquencyProcessCharacteristic amountChar = delinquencyProcessId.getEntity().
					getEffectiveCharacteristic(getUnpaidAmtDueCharacteristicType());
			if(notNull(amountChar)){
				unpaidAmount = amountChar.getAdhocCharacteristicValue();
			}else{
				addError(MessageRepository.noUnpaidAmountFound(delinquencyProcessId.getTrimmedValue()));
			}
			
			BusinessObjectInstance boInstance = BusinessObjectDispatcher.read(this.businessObjectInstKey, true);
			if(notNull(boInstance.getElement().selectSingleNode("policyId"))){
				policy = boInstance.getElement().selectSingleNode("policyId").getText();
				outMsgParms.setPolicyId(policy);
			}
		}
		//If criteria is C calculate data using algorithm
		else if(CmDelinquencyProcessConstant.BASIS_UNPAID_AMT_C.equalsIgnoreCase(getBasisUnpaidAmtCalculation())){
			BigDecimal amount = BigDecimal.ZERO;
			CmDelinquencyProcessType processType = delinquencyProcessId.getEntity().getCmDelinquencyProcessType();
			CmDelinquencyProcessTypeAlgorithms algorithms =  processType.getAlgorithms();
			ListFilter<CmDelinquencyProcessTypeAlgorithm> algorithmFilter = algorithms
					.createFilter("WHERE this.id.cmDelinquencyProcessType = :processType AND this.id.cmDelinquencyProcessTypeSystemEvent = :eventFlag "
							+ "ORDER BY this.id.sequence DESC", this.getClass().getSimpleName()+"_fetchPolicyDetails");
			algorithmFilter.bindId("processType", processType.getId());
			algorithmFilter.bindLookup("eventFlag", CmDelinquencyProcessTypeSystemEventLookup.constants.CM_CALC_UNPAID_AND_ORIGINAL_AMTS);
			
			CmDelinquencyProcessTypeAlgorithm processTypeAlgorithm = algorithmFilter.firstRow();
			checkNull(processTypeAlgorithm, MessageRepository.noUnpaidAmountCalcAlgFound(processType.fetchLanguageDescription()));

			Algorithm algorithm = processTypeAlgorithm.getAlgorithm();
			List<SQLResultRow> billIds = fetchBills(delinquencyProcessId);
			checkNull(billIds,MessageRepository.nobillsFoundFromDelinquency(delinquencyProcessId.getTrimmedValue()));

			for(SQLResultRow row: billIds){
				String billId = row.getString("PK_VALUE1");
				BigDecimal amountFromAlg = callAlgorithm(delinquencyProcessId, algorithm, billId);
				amount = amount.add(amountFromAlg);
				if(isNull(policy))
					policy = getPolicy(billId);
			}
			unpaidAmount = amount.toString();
			outMsgParms.setPolicyId(policy);
		}
		Money unpaidMoney = new Money(unpaidAmount, currencyId);
		outMsgParms.setAmount(unpaidMoney);
		checkNull(policy, MessageRepository.nopolicyFoundFromDelinquency(delinquencyProcessId.getTrimmedValue()));
		
		Policy_Id policyId = new Policy_Id(policy);
		Policy policyObj = policyId.getEntity();
		outMsgParms.setPolicyNumber(policyObj.getPolicyNumber());
		outMsgParms.setPolicyCancelDate(policyObj.getEndDate());
		outMsgParms.setPolicyCancelReason(notNull(policyObj.getStatusReasonId())?policyObj.getStatusReasonId().getTrimmedValue():"");
		PolicyCharacteristic policyChar = policyObj.getEffectiveCharacteristic(getOfficeRegistrationCharacteristicType());
		checkNull(policyChar, MessageRepository.missingOfficeOfRegistration(policy));
		outMsgParms.setSalesOffice(fetchPolicyCharValue(policyChar));
		policyChar = policyObj.getEffectiveCharacteristic(getFrozenMktSegCharacteristicType());
		checkNull(policyChar, MessageRepository.missingFrozenMarketSegment(policy));
		outMsgParms.setCustomerType(fetchPolicyCharValue(policyChar));

		// Retrieve Shared Arrangement from Policy
		policyChar = policyObj.getEffectiveCharacteristic(getSharedAgmtIndicatorCharacteristicType());
		if (notNull(policyChar)) {
			outMsgParms.setSharedArrangementIndicator(fetchPolicyCharValue(policyChar));
		}
		
		Query<PolicyPlan> policyPlanQuery = createQuery("from PolicyPlan pp where pp.policy = :policyId", this.getClass().getSimpleName()+"_fetchPolicyDetails");
		policyPlanQuery.bindId("policyId", policyId);
		
		if(notNull(policyPlanQuery.firstRow())){
			
			PolicyPlan pp = policyPlanQuery.firstRow();
			PolicyPlanCharacteristic ppChar = pp.getEffectiveCharacteristic(getSharedAgmtIndicatorCharacteristicType());
			if(notNull(ppChar))
				outMsgParms.setSharedArrangementIndicator(fetchPolicyPlanCharValue(ppChar));
			ppChar = pp.getEffectiveCharacteristic(getObligorIdCharacteristicType());
			if(notNull(ppChar))
				outMsgParms.setObligorId(fetchPolicyPlanCharValue(ppChar));
		}
		
		return outMsgParms;
	}

	/**
	 * This method will fetch currency id from customer.
	 * @param delinquencyCust
	 * @return
	 */
	private Currency_Id getCurrency(Person_Id delinquencyCust) {
		Currency_Id currencyId = null;
		StringBuilder queryString = new StringBuilder()
			.append(" from AccountPerson accountPerson where accountPerson.id.person.id = :personId ")
			.append(" and accountPerson.isMainCustomer = :yes ");

		Query<Account_Id> query = createQuery(queryString.toString(),
				this.getClass().getSimpleName()+"_getCurrency");

		query.bindId("personId", delinquencyCust);
		query.bindBoolean("yes", Bool.TRUE);

		query.addResult("accountId", "accountPerson.id.account.id");

		Account_Id accountId = query.firstRow();
		currencyId = accountId.getEntity().getCurrency().getId();
		return currencyId;
	}

	/**
	 * This method will fetch policy plan char value
	 * @param ppChar
	 * @return
	 */
	private String fetchPolicyPlanCharValue(PolicyPlanCharacteristic ppChar) {
		String value = null;
		if(ppChar.fetchIdCharacteristicType().getCharacteristicType().isAdhocValue()){
			value = ppChar.getAdhocCharacteristicValue();
		}else if(ppChar.fetchIdCharacteristicType().getCharacteristicType().isPredefinedValue()){
			value = ppChar.getCharacteristicValue();
		}
		return value;
	}

	/**
	 * This method will fetch policy char value
	 * @param policyChar
	 * @return
	 */
	private String fetchPolicyCharValue(PolicyCharacteristic policyChar) {
		String value = null;
		if(policyChar.fetchIdCharacteristicType().getCharacteristicType().isAdhocValue()){
			value = policyChar.getAdhocCharacteristicValue();
		}else if(policyChar.fetchIdCharacteristicType().getCharacteristicType().isPredefinedValue()){
			value = policyChar.getCharacteristicValue();
		}
		return value;
	}

	/**
	 * This method will get policy id using bill id.
	 * @param billId
	 * @return
	 */
	private String getPolicy(String billId) {
		String policyId = null;
		PreparedStatement pst  = null;
		StringBuilder queryString = new StringBuilder();
		//Query to fetch all the bills from CM_DELIN_PROC_REL_OBJ
		queryString.append("SELECT DISTINCT(FE.POLICY_ID) AS POLICY_ID FROM C1_FT_EXT FE WHERE FE.POLICY_ID <> ' ' ");
		queryString.append("AND FE.FT_ID IN (SELECT FT1.FT_ID FROM CI_FT FT1 WHERE FT1.BILL_ID = :billId ");
		queryString.append("AND FT1.MATCH_EVT_ID = ' ' UNION SELECT FT2.FT_ID FROM CI_FT FT2 WHERE FT2.BILL_ID = :billId ");
		queryString.append("AND FT2.MATCH_EVT_ID <> ' ' AND FT2.MATCH_EVT_ID IN (SELECT ME.MATCH_EVT_ID ");
		queryString.append("FROM CI_MATCH_EVT ME WHERE ME.MEVT_STATUS_FLG <> :meStatFlg))");
		try{
			pst = createPreparedStatement(queryString.toString(),this.getClass().getSimpleName()+"_fetchDelinquencyCustomer");
			pst.setAutoclose(false);
			pst.bindId("billId", new Bill_Id(billId));
			pst.bindLookup("meStatFlg", MatchEventStatusLookup.constants.BALANCED);
			SQLResultRow row = pst.firstRow();
			if(notNull(row)){
				policyId = row.getString("POLICY_ID");
			}
		}catch(Exception e){
			addError(MessageRepository.exception());
		}finally{
			queryString.setLength(0);
			queryString = null;
			if(notNull(pst)){
				pst.close();
				pst = null;
			}
		}
		return policyId;
	}

	/**
	 * This method will call Calculate Unpaid and Original Amount algorithm
	 * @param delinquencyProcessId 
	 * @param algorithm
	 * @param billId
	 * @return Amount
	 */
	private BigDecimal callAlgorithm(CmDelinquencyProcess_Id delinquencyProcessId, Algorithm algorithm, String billId) {
		BigDecimal amount = BigDecimal.ZERO;
		CmCalculateUnpaidOriginalAmountAlgorithmSpot algoSpot = AlgorithmComponentCache.getAlgorithmComponent(algorithm.getId(),CmCalculateUnpaidOriginalAmountAlgorithmSpot.class);
		algoSpot.setDelinquencyProcessId(delinquencyProcessId);
		CmDelinquencyProcessRelatedObject_Id delProcRelObjectId = new CmDelinquencyProcessRelatedObject_Id(delinquencyProcessId,
				new MaintenanceObject_Id(CmDelinquencyProcessConstant.MAINTENANCE_OBJ_BILL),
				CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON,
				billId);
		algoSpot.setDelinquencyProcessRelatedObject(delProcRelObjectId.getEntity());
		algoSpot.invoke();
		amount = algoSpot.getUnpaidAmount();
		return amount;
	}

	/**
	 * This method will fetch all the bills from CM_DELIN_PROC_REL_OBJ
	 * @param delinquencyProcessId
	 * @return Bill Id's
	 */
	private List<SQLResultRow> fetchBills(CmDelinquencyProcess_Id delinquencyProcessId) {
		List<SQLResultRow> resultList = null;
		PreparedStatement pst  = null;
		StringBuilder queryString = new StringBuilder();
		//Query to fetch all the bills from CM_DELIN_PROC_REL_OBJ
		queryString.append("SELECT PK_VALUE1 FROM CM_DELIN_PROC_REL_OBJ WHERE CM_DELIN_PROC_ID = :processId ");
		queryString.append("AND CM_DEL_REL_OBJ_TYPE_FLG = :objTypeFlg AND MAINT_OBJ_CD = :mo");
		try{
			pst = createPreparedStatement(queryString.toString(),this.getClass().getSimpleName()+"_fetchDelinquencyCustomer");
			pst.setAutoclose(false);
			pst.bindId("processId", delinquencyProcessId);
			pst.bindLookup("objTypeFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);
			pst.bindId("mo", new MaintenanceObject_Id(CmDelinquencyProcessConstant.MAINTENANCE_OBJ_BILL));
			resultList = pst.list();
		}catch(Exception e){
			addError(MessageRepository.exception());
		}finally{
			queryString.setLength(0);
			queryString = null;
			if(notNull(pst)){
				pst.close();
				pst = null;
			}
		}	
		return resultList;
	}

	/**
	 * This method will get all the customer details required for out-bound message
	 * @param delinquencyCust
	 * @param outMsgParms
	 */
	private CmDelinquencyProcessOutMsgParmsHelper fetchCustomerDetails(Person_Id delinquencyCust,
			CmDelinquencyProcessOutMsgParmsHelper outMsgParms) {
		
		Person customer = delinquencyCust.getEntity();
		
		//Set Customer Name
		outMsgParms.setCustomerName(customer.getPersonPrimaryName());
		
		//Set Address City State & Postal
		outMsgParms.setAddress1(customer.getAddress1());
		outMsgParms.setAddress2(customer.getAddress2());
		outMsgParms.setCity(customer.getCity());
		outMsgParms.setState(customer.getState());
		outMsgParms.setPostal(customer.getPostal());
		
		//Set Bill Group Contact
		outMsgParms.setBillGroupContact(customer.getAddress4());
		
		//Set Phone Number
		ListFilter<PersonPhone> personPhoneFilter = customer.getPhones()
				.createFilter("WHERE this.id.person = :customerId AND this.phoneType = :phoneType order by this.id.sequence DESC"
						, this.getClass().getSimpleName()+"_fetchCustomerDetails");
		personPhoneFilter.bindId("customerId",delinquencyCust);
		personPhoneFilter.bindId("phoneType",getBillToPhoneType().getId());
		if(notNull(personPhoneFilter.firstRow())){
			PersonPhone personPhone = personPhoneFilter.firstRow();
			outMsgParms.setPhone(formatPhone(personPhone));
			
		}else{
			personPhoneFilter.bindId("phoneType",getBusinessPhoneType().getId());
			PersonPhone personPhone = personPhoneFilter.firstRow();
			if(notNull(personPhone)){
				outMsgParms.setPhone(formatPhone(personPhone));	
			}else{
				addError(MessageRepository.noPersonPhoneFound(delinquencyCust.getTrimmedValue()));
			}
		}
		return outMsgParms;
		
	}

	/**
	 * This Method will fetch the delinquency customer using delinquency process Id
	 * @param delinquencyProcessId 
	 * @return Delinquency Customer
	 */
	private Person_Id fetchDelinquencyCustomer(CmDelinquencyProcess_Id delinquencyProcessId) {
		Person_Id delinquencyCust = null;
		PreparedStatement pst  = null;
		StringBuilder queryString = new StringBuilder();
		//Query to fetch delinquency entity from CM_DELIN_PROC_REL_OBJ
		queryString.append("SELECT MAINT_OBJ_CD, PK_VALUE1 FROM CM_DELIN_PROC_REL_OBJ ");
		queryString.append("WHERE CM_DELIN_PROC_ID = :processId AND CM_DEL_REL_OBJ_TYPE_FLG = :objTypeFlg");
		try{
			pst = createPreparedStatement(queryString.toString(),this.getClass().getSimpleName()+"_fetchDelinquencyCustomer");
			pst.setAutoclose(false);
			pst.bindId("processId", delinquencyProcessId);
			pst.bindLookup("objTypeFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_DELINQUENCY_LEVEL);
			SQLResultRow resultRow = pst.firstRow();
			if(notNull(resultRow)){
				String maintObjCode = resultRow.getString("MAINT_OBJ_CD");
				String pkValue = resultRow.getString("PK_VALUE1");
				if(notNull(maintObjCode) && notNull(pkValue) && "PERSON".equalsIgnoreCase(maintObjCode.trim())){
					delinquencyCust = new Person_Id(pkValue);
				}else if(notNull(maintObjCode) && notNull(pkValue) && "ACCOUNT".equalsIgnoreCase(maintObjCode.trim())){
					Account_Id accountId = new Account_Id(pkValue);
					delinquencyCust = retrieveCustUsingAcct(accountId);
				}
			}
		}catch(Exception e){
			addError(MessageRepository.exception());
		}finally{
			queryString.setLength(0);
			queryString = null;
			if(notNull(pst)){
				pst.close();
				pst = null;
			}
		}
		return delinquencyCust;
	}

	/**
	 * Retrieve Primary Customer Using Account Number
	 * @param accountId
	 * @return Person Id
	 */
	private Person_Id retrieveCustUsingAcct(Account_Id accountId) {
		Person_Id delinquencyCust = null;
		PreparedStatement pst  = null;
		StringBuilder queryString = new StringBuilder();
		//Query to fetch primary customer
		queryString.append("SELECT PER_ID FROM CI_ACCT_PER WHERE MAIN_CUST_SW = :mainCustSw AND ACCT_ID = :accountId");
		try{
			pst = createPreparedStatement(queryString.toString(),this.getClass().getSimpleName()+"_fetchDelinquencyCustomer");
			pst.setAutoclose(false);
			pst.bindId("accountId", accountId);
			pst.bindString("mainCustSw", "Y", "MAIN_CUST_SW");
			SQLResultRow resultRow = pst.firstRow();
			if(notNull(resultRow)){
				delinquencyCust = (Person_Id)resultRow.getId("PER_ID", Person.class);
			}
		}catch(Exception e){
			addError(MessageRepository.exception());
		}finally{
			queryString.setLength(0);
			queryString = null;
			if(notNull(pst)){
				pst.close();
				pst = null;
			}
		}
		return delinquencyCust;
	}

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
		//Validate Out-bound Message Profile
		OutboundMessageType outMsgType = getOutboundMessageType();
		NotificationExternalId externalSys = getExternalSystem();
		if(isNull(externalSys.getProfileForType(outMsgType))){
			addError(MessageRepository.invalidOutboundMessageProfile(outMsgType.getId().getTrimmedValue(), externalSys.getId().getTrimmedValue()));
		}
		
		//Validate Basis for Unpaid Amount Calculation
		Set<String> unpaidAmtCalcSet = new HashSet<>();
		unpaidAmtCalcSet.add(CmDelinquencyProcessConstant.BASIS_UNPAID_AMT_C);
		unpaidAmtCalcSet.add(CmDelinquencyProcessConstant.BASIS_UNPAID_AMT_D);
		if(unpaidAmtCalcSet.add(getBasisUnpaidAmtCalculation().trim())){
			addError(MessageRepository.invalidUnpaidAmountCalcValue());
		}
		
		//Validate Characteristic Type Entity
		validateCharTypeEntity(CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS, getUnpaidAmtDueCharacteristicType());
		validateCharTypeEntity(CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG, getOutboundMessageIdCharacteristicType());
		validateCharTypeEntity(CharacteristicEntityLookup.constants.POLICY, getOfficeRegistrationCharacteristicType());
		validateCharTypeEntity(CharacteristicEntityLookup.constants.POLICY, getFrozenMktSegCharacteristicType());
		validateCharTypeEntity(CharacteristicEntityLookup.constants.POLICY_PLAN, getSharedAgmtIndicatorCharacteristicType());
		validateCharTypeEntity(CharacteristicEntityLookup.constants.POLICY_PLAN, getObligorIdCharacteristicType());
		validateCharTypeEntity(CharacteristicEntityLookup.constants.PERSON, getReferralTypeCharacteristicType());

	}
	
	/**
	 * This method validates the Entity of a Characteristic Type Algorithm parameter.
	 * Throws an error message if input Characteristic Type does not belong to the input Characteristic Entity.
	 * @param charEntityLookup - Represents the Characteristic Entity Lookup that will be used to validate the 
	 *                           input Characteristic Type.
	 * @param characteristicType = Represents the input Characteristic Type that will be validated if it contains
	 *                             the input Characteristic Entity (Lookup).
	 */
	public void validateCharTypeEntity (CharacteristicEntityLookup charEntityLookup, 
			CharacteristicType characteristicType) {

		CharacteristicEntity_Id characteristicEntityId = new CharacteristicEntity_Id(characteristicType, charEntityLookup);
		CharacteristicEntity characteristicEntity = characteristicEntityId.getEntity();

		if (isNull(characteristicEntity)) {
			addError(MessageRepository.invalidCharTypeForEntity(characteristicType.getId().getTrimmedValue()
					, charEntityLookup.getLookupValue().fetchLanguageDescription()));
		}
	}
	
	/**
	 * This method will check Null for input object
	 * @param object
	 * @param serverMessage
	 */
	private void checkNull(Object object, ServerMessage serverMessage) {
		if(NullHelper.isNull(object)){
			addError(serverMessage);
		}
	}

	private String formatPhone(PersonPhone personPhone) {
		String personPhoneNbr = BLANK;
		if (getFormatPhoneNumber().isYes()) {
			String[] perPhoneParts = personPhone.getPhone().split(BLANK);
			if (perPhoneParts.length >= 2) {
				if (perPhoneParts[1].length() > 10) {
					personPhoneNbr = perPhoneParts[1].substring(0, 10);	
				} else {  
					personPhoneNbr = perPhoneParts[1];
				}
			}
		} else if (getFormatPhoneNumber().isNo()) {
			personPhoneNbr = personPhone.getPhone();
		}
		return personPhoneNbr;
	}	
	
	@Override
	public boolean getForcePostProcessing() {
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup arg0) {
	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		businessObjectInstKey = arg0;
	}

	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return null;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		return false;
	}

}
