/*
 **********************************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm is responsible for evaluating the delinquent customer for systematic write off or referral to delinquency management unit 
 * for further collections.
 **********************************************************************************************************************************************
 * 
 * 
 * CHANGE HISTORY:
 * 
 * Date:       	by:          Reason:
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **********************************************************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.LookupHelper;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.common.NullHelper;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;
import com.splwg.ccb.api.lookup.MatchEventStatusLookup;
import com.splwg.ccb.domain.admin.generalLedgerDistributionCode.GeneralLedgerDistributionCodeCharacteristic;
import com.splwg.cm.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.api.lookup.CmDelinquencyProcessTypeSystemEventLookup;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyCustomerMonitorRuleConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmCalculateUnpaidOriginalAmountAlgorithmSpot;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessTypeAlgorithm;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.ServerMessage;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = toleranceAmountThreshold, type = decimal)
 *            , @AlgorithmSoftParameter (name = tolerancePercentageThreshold, type = decimal)
 *            , @AlgorithmSoftParameter (name = transitionConditiontoWrittenOffStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = transitionConditiontoDebtReferredStatus, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = fundingArrangements, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = fundingArrangementCharType, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = chargeLineCodeCharType, type = entity) 
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = includeOnAccountPaymentsInThresholdEvaluation, required = true, type = lookup)  
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = adminContractTypesFeatureConfig, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = adminContractTypeOptionType, type = lookup) 
 *            , @AlgorithmSoftParameter (lookupName = businessObjectStatusTransitionCondition, name = transitionConditionToEvaluateDebtNotificationSentStatus, type = lookup)})
 */

public class CmEvaluateDebtAlgComp_Impl extends CmEvaluateDebtAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot {

	private BigDecimal totalOriginalAmt;
	private BigDecimal totalUnpaidAmt;
	private BigDecimal flatThresholdAmt;
	private BigDecimal pctThresholdAmt;
	private BigDecimal amtToCompare;
	private BigDecimal hundred;
	private BusinessObjectStatusTransitionConditionLookup nextStatusCondition;

	private BusinessObjectInstanceKey businessObjectInstKey = null;

	
	private Bool shouldReferDebtSw;
	//private final Logger logger = LoggerFactory.getLogger(CmEvaluateDebtAlgComp_Impl.class);

	private BigDecimal onAccountPayment;
	
	@Override
	public void invoke() {

		totalOriginalAmt = BigDecimal.ZERO;
		totalUnpaidAmt = BigDecimal.ZERO;
		flatThresholdAmt = BigDecimal.ZERO;
		pctThresholdAmt = BigDecimal.ZERO;
		amtToCompare = BigDecimal.ZERO;
		nextStatusCondition = null;
		hundred = new BigDecimal(100);
		
		
		onAccountPayment = BigDecimal.ZERO;
		
		
		CmDelinquencyProcess_Id delinquencyProcId = new CmDelinquencyProcess_Id(businessObjectInstKey.getString("delinquencyProcessId"));
		
		
		shouldReferDebtSw = Bool.FALSE;
		if (!isBlankOrNull(getFundingArrangements())) { 
			shouldReferDebtSw = checkFundingArrangementCharges(delinquencyProcId);
		}
		if (shouldReferDebtSw.isTrue()) {
			nextStatusCondition = LookupHelper.getLookupInstance(BusinessObjectStatusTransitionConditionLookup.class, getTransitionConditiontoDebtReferredStatus());
			return;
		}
		
		CmDelinquencyProcessType delinquencyProcessType = null;
		// Get Delinquency Process Type
		if (notNull(delinquencyProcId.getEntity()))
			delinquencyProcessType = delinquencyProcId.getEntity().getCmDelinquencyProcessType();

		// Fetch all Algorithms on Delinquency Process Type
		ListFilter<CmDelinquencyProcessTypeAlgorithm> algorithmList = delinquencyProcessType.getAlgorithms().createFilter(" where this.id.cmDelinquencyProcessTypeSystemEvent = :systemEvent order by this.id.sequence asc", "CmEvaluateDebtAlgComp_Impl");
		algorithmList.bindLookup("systemEvent", CmDelinquencyProcessTypeSystemEventLookup.constants.CM_CALC_UNPAID_AND_ORIGINAL_AMTS);

		ListFilter<CmDelinquencyProcessRelatedObject> relatedObjs = delinquencyProcId.getEntity().getRelatedObjects().createFilter(" where this.id.maintenanceObject = :maintObj and this.id.cmDelinquencyRelatedObjTypeFlg = :relatedObjTypeFlag ", "CmEvaluateDebtAlgComp_Impl");
		MaintenanceObject_Id maintenanceObjectId = new MaintenanceObject_Id(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL);
		relatedObjs.bindEntity("maintObj", maintenanceObjectId.getEntity());
		relatedObjs.bindLookup("relatedObjTypeFlag", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);

		List<CmDelinquencyProcessRelatedObject> relatedObjectList = relatedObjs.list();
		Iterator<CmDelinquencyProcessRelatedObject> relatedObjectIter = relatedObjectList.iterator();

		if (notNull(algorithmList) && notNull(relatedObjectList)) {
			while (relatedObjectIter.hasNext()) {
				for (Iterator<CmDelinquencyProcessTypeAlgorithm> algoIter = algorithmList.iterate(); algoIter.hasNext();) {

					// Invoke the algorithm passing Delinquency Process ID as input
					
					CmDelinquencyProcessTypeAlgorithm algorithm = algoIter.next();
					CmCalculateUnpaidOriginalAmountAlgorithmSpot algorithmComp = AlgorithmComponentCache.getAlgorithmComponent(algorithm.getAlgorithm().getId(), CmCalculateUnpaidOriginalAmountAlgorithmSpot.class);
					algorithmComp.setDelinquencyProcessId(delinquencyProcId);
					algorithmComp.setDelinquencyProcessRelatedObject(relatedObjectIter.next());
					algorithmComp.invoke();
					totalOriginalAmt = totalOriginalAmt.add(algorithmComp.getOriginalAmount());
					totalUnpaidAmt = totalUnpaidAmt.add(algorithmComp.getUnpaidAmount());
				}
			}
		}

		if (notNull(getToleranceAmountThreshold()))
		{
			flatThresholdAmt = getToleranceAmountThreshold();
		}

		if (notNull(getTolerancePercentageThreshold()))
		{
			try {
				// pctThresholdAmt = totalOriginalAmt multiply by  parameter Tolerance Percentage Threshold divide by 100
				pctThresholdAmt = totalOriginalAmt.multiply(getTolerancePercentageThreshold()).divide(hundred).setScale(18, BigDecimal.ROUND_HALF_DOWN);
			} catch (ArithmeticException ex)
			{
				ex.printStackTrace();
			}
		}

		if (flatThresholdAmt.compareTo(BigDecimal.ZERO) > 0 && pctThresholdAmt.compareTo(BigDecimal.ZERO) > 0)
		{
			if (flatThresholdAmt.compareTo(pctThresholdAmt) < 0)
			{
				amtToCompare = flatThresholdAmt;
			}
			else
			{
				amtToCompare = pctThresholdAmt;
			}
		}
		else
		{
			if (flatThresholdAmt.compareTo(pctThresholdAmt) < 0)
			{
				amtToCompare = pctThresholdAmt;
			}
			else
			{
				amtToCompare = flatThresholdAmt;
			}

		}
		
				
		if (getIncludeOnAccountPaymentsInThresholdEvaluation().isYes()) {
			onAccountPayment = calculateOnAccountPayments(delinquencyProcId);
			totalUnpaidAmt = totalUnpaidAmt.add(onAccountPayment);
		}			
		

		if (totalUnpaidAmt.compareTo(amtToCompare) <= 0)
		{
			
			if (onAccountPayment.compareTo(BigDecimal.ZERO) == 0) {
			
			
				nextStatusCondition = LookupHelper.getLookupInstance(BusinessObjectStatusTransitionConditionLookup.class, getTransitionConditiontoWrittenOffStatus());			
							
			
			} 
			else 
			{
				nextStatusCondition = LookupHelper.getLookupInstance(BusinessObjectStatusTransitionConditionLookup.class, getTransitionConditionToEvaluateDebtNotificationSentStatus());
			}
			
			
		}
		else
		{
			nextStatusCondition = LookupHelper.getLookupInstance(BusinessObjectStatusTransitionConditionLookup.class, getTransitionConditiontoDebtReferredStatus());
		}

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

	
	private Bool checkFundingArrangementCharges(CmDelinquencyProcess_Id delinquencyProcId) {
		// Check if any of the unpaid charges on the bills do not belong to Fully Insured funding arrangements
		String[] fundingArrangements = getFundingArrangements().split(",");
		StringBuilder pstmt = new StringBuilder();
		pstmt.append(" SELECT DST_ID FROM CI_DST_CD_CHAR ");
		pstmt.append(" WHERE CHAR_TYPE_CD = :fundingArrangementCharType ");
		pstmt.append(" AND DST_ID IN (SELECT RPAD(CHAR_VAL_FK1, 10, ' ') ");		
		pstmt.append(" 			FROM C1_FT_EXT_CHAR "); 
		pstmt.append(" 			WHERE CHAR_TYPE_CD = :chargeLineCodeCharType ");  
		pstmt.append(" 			AND FT_ID IN (SELECT FT_ID ");  
		pstmt.append(" 					FROM CI_FT FT, CM_DELIN_PROC_REL_OBJ DPO ");  
		pstmt.append(" 					WHERE DPO.CM_DELIN_PROC_ID = :delinquencyProcess ");
		pstmt.append(" 					AND DPO.MAINT_OBJ_CD = 'BILL' ");
		pstmt.append(" 					AND DPO.CM_DEL_REL_OBJ_TYPE_FLG = :relObjTypeFlg ");
		pstmt.append(" 					AND DPO.PK_VALUE1 = FT.BILL_ID ");
		pstmt.append(" 					AND (FT.MATCH_EVT_ID = ' ' OR ");
		pstmt.append(" 						(FT.MATCH_EVT_ID <> ' ' "); 
		pstmt.append(" 						AND EXISTS (SELECT 1 FROM CI_MATCH_EVT ME ");
		pstmt.append(" 						WHERE ME.MATCH_EVT_ID = FT.MATCH_EVT_ID ");
		pstmt.append(" 						AND ME.MEVT_STATUS_FLG <> :matchEvtStatus))) ");
		pstmt.append(" 				) ");                                                  
		pstmt.append(" 		) "); 
		
		if (getFundingArrangementCharType().getCharacteristicType().isPredefinedValue()) {
			pstmt.append(" 		AND CHAR_VAL NOT IN (:fundArrangement");
		} else {
			pstmt.append(" 		AND ADHOC_CHAR_VAL NOT IN (:fundArrangement0");
		}
				
		for (int ctr = 1; ctr < fundingArrangements.length; ctr++) {
			pstmt.append(" ,:");
			pstmt.append("fundArrangement");
			pstmt.append(ctr);
		}
		pstmt.append(" 		)");
		//logger.info("pstmt:" + pstmt);
		
		PreparedStatement getNonFullyInsuredCharges = createPreparedStatement(pstmt.toString(),"getNonFullyInsuredCharges"); 
		getNonFullyInsuredCharges.bindEntity("fundingArrangementCharType", getFundingArrangementCharType());
		getNonFullyInsuredCharges.bindEntity("chargeLineCodeCharType", getChargeLineCodeCharType());
		getNonFullyInsuredCharges.bindId("delinquencyProcess", delinquencyProcId);
		getNonFullyInsuredCharges.bindLookup("relObjTypeFlg", CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON);
		getNonFullyInsuredCharges.bindLookup("matchEvtStatus", MatchEventStatusLookup.constants.BALANCED);
		
		for (int ctr = 0; ctr < fundingArrangements.length; ctr++) {
			if (getFundingArrangementCharType().getCharacteristicType().isPredefinedValue()) {
				getNonFullyInsuredCharges.bindStringProperty("fundArrangement" + ctr, GeneralLedgerDistributionCodeCharacteristic.properties.characteristicValue, fundingArrangements[0]);
			} else {
				getNonFullyInsuredCharges.bindStringProperty("fundArrangement" + ctr, GeneralLedgerDistributionCodeCharacteristic.properties.adhocCharacteristicValue, fundingArrangements[0]);
			}
		}
		
		if (getNonFullyInsuredCharges.list().size() > 0) {
			return Bool.TRUE;	
		}
		return Bool.FALSE;	
	}
	
	
	/**
	 * Performs soft parameter validation 
	 * 
	 * @param forAlgorithmValidation
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		if(!getTransitionConditiontoWrittenOffStatus().trim().equals(BusinessObjectStatusTransitionConditionLookup.constants.CM_WRITE_OFF.trimmedValue()))
		{
			addError(MessageRepository.invalidStatusTransitionCondition(getTransitionConditiontoWrittenOffStatus()));
		}
		if(!getTransitionConditiontoDebtReferredStatus().trim().equals(BusinessObjectStatusTransitionConditionLookup.constants.CM_REFER_DEBT.trimmedValue()))
		{
			addError(MessageRepository.invalidStatusTransitionCondition(getTransitionConditiontoDebtReferredStatus()));
		}
		
		
		if (!isBlankOrNull(getFundingArrangements())) { 
			
			//Validate fundingArrangementCharType and chargeLineCodeCharType Soft Parameters
			checkNull(getFundingArrangementCharType(), MessageRepository.fundingAgmtCodeCharTypeMissing(getAlgorithmId().getTrimmedValue()));
			validateCharTypeEntity(CharacteristicEntityLookup.constants.DISTRIBUTION_CODE, getFundingArrangementCharType());
			checkNull(getChargeLineCodeCharType(), MessageRepository.chargeLineCodeCharTypeMissing(getAlgorithmId().getTrimmedValue()));
		}
					
		if (getIncludeOnAccountPaymentsInThresholdEvaluation().isYes()) {
			
			// Check that the adminContractTypesFeatureConfig and adminContractTypeOptionType soft parameters are provided
			checkNull(getAdminContractTypesFeatureConfig(), MessageRepository.adminContractTypesFeatureConfigMissing());
			checkNull(getAdminContractTypeOptionType(), MessageRepository.adminContractTypeOptionTypeMissing());
			checkNull(getTransitionConditionToEvaluateDebtNotificationSentStatus(), MessageRepository.transitionConditionToEvaluateDebtNotificationSentStatusMissing());
			
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
	
		
	/**
	 * This method will calculate the On Account Payments 
	 * @param delinquencyProcId
	 */
	private BigDecimal calculateOnAccountPayments(CmDelinquencyProcess_Id delinquencyProcId) {
		
		// Retrieve the open balance of all administrative contracts of the Accounts falling under same customer
		// Administrative contracts are those whose contract types are configured on the 
		// Administrative Contract Types Feature Configuration
		StringBuilder onAccountPaymentQuery = new StringBuilder();		
		onAccountPaymentQuery.append(" FROM FinancialTransaction FT, ServiceAgreement SA, AccountPerson AP, CmDelinquencyProcessRelatedObject DPRO ");
		onAccountPaymentQuery.append(" WHERE DPRO.id.delinquencyProcess.id = :delinquencyProcessId ");
		onAccountPaymentQuery.append(" AND DPRO.id.cmDelinquencyRelatedObjTypeFlg = 'CMDL' ");
		onAccountPaymentQuery.append(" AND ((DPRO.id.maintenanceObject = 'PERSON' AND DPRO.id.primaryKeyValue1 = AP.id.person.id) ");
		onAccountPaymentQuery.append("       OR (DPRO.id.maintenanceObject = 'ACCOUNT' AND DPRO.id.primaryKeyValue1 = AP.id.account.id)) ");
		onAccountPaymentQuery.append(" AND  AP.isMainCustomer = 'Y' ");
		onAccountPaymentQuery.append(" AND  SA.account.id = AP.id.account.id ");
		onAccountPaymentQuery.append(" AND  SA.serviceAgreementType.id.saType IN (SELECT RPAD(FCO.value,8,' ') ");
		onAccountPaymentQuery.append("                                            FROM FeatureConfigurationOption FCO ");
		onAccountPaymentQuery.append("                                            WHERE FCO.id.workforceManagementSystem = :adminContractTypesFeatureConfig ");
		onAccountPaymentQuery.append("                                            AND FCO.id.optionType = :adminContractTypeOptionType) ");
		onAccountPaymentQuery.append(" AND  FT.serviceAgreement.id = SA.id ");
		onAccountPaymentQuery.append(" AND  FT.isFrozen = 'Y' ");		
		
		Query<Money> query = createQuery(onAccountPaymentQuery.toString(), "calculateOnAccountPayments");
		query.bindId("delinquencyProcessId", delinquencyProcId);		
		query.bindEntity("adminContractTypesFeatureConfig", getAdminContractTypesFeatureConfig());
		query.bindLookup("adminContractTypeOptionType", getAdminContractTypeOptionType());		
		query.addResult("overpay", "NVL(SUM(FT.currentAmount),0)");
		
		Money onAcctPymt = query.firstRow();
		BigDecimal onAccountPayment = onAcctPymt.getAmount();
		
		return onAccountPayment;

	}	
	
	@Override
	public BusinessObjectStatusCode getNextStatus() {
		return null;
	}

	@Override
	public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
		return nextStatusCondition;
	}

	@Override
	public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
		return null;
	}

	@Override
	public boolean getUseDefaultNextStatus() {
		return false;
	}

	@Override
	public void setBusinessObject(BusinessObject arg0) {

	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
		this.businessObjectInstKey = arg0;

	}

}

