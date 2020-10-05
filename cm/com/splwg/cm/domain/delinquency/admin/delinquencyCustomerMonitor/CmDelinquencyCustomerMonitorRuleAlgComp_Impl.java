/*                                                                
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION                                          
 *                                                                
 * Determine Delinquency > Delinquency Monitor Rule Algorithm
 * 
 * This Algorithm determines if customer or account debt is overdue based on 
 * the credit rating, age and amount thresholds (including Amount and some percent of 
 * Unpaid amount with respect to original amount). This creates new 
 * delinquency process when passed above criteria. If delinquency process 
 * already exists, this adds bills to it.
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   MugdhaP		ANTHM-340 CAB1-9462 Delinquency Framework
 * **************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.datatypes.DateFormatParseException;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.algorithmType.AlgorithmTypeParameter_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.billing.bill.OpenItemBillAmountResults;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.api.lookup.CharacteristicEntityLookup;
import com.splwg.cm.api.lookup.CmDelinquencyLevelLookup;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.collectionClassOverdueRules.CmDetermineOpenBillItemAmounts;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessLog;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject_DTO;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmCustomerReviewSchedule_DTO;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmCustomerReviewSchedule_Id;
import com.splwg.cm.domain.delinquency.common.customBusinessEntity.CmPersonCollection_Id;
import com.splwg.cm.domain.delinquency.utils.CmPolicyHelper;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author MugdhaP
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = creditRatingValue, type = integer)
 *            , @AlgorithmSoftParameter (name = limitingCharacteristicEntity, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = limitingCharacteristicType, type = entity)
 *            , @AlgorithmSoftParameter (name = limitingCharacteristicValue, type = string)
 *            , @AlgorithmSoftParameter (name = limitingCharEffectiveWithinXDays, type = integer)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = postponeDateCharacteristicType, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = cmDelinquencyLevel, name = delinquencyLevel, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (name = determinesAgeDate, required = true, type = string)
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
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = band4UnpaidAmountAndPercentage, type = lookup)
 *            , @AlgorithmSoftParameter (name = band4ProcessBillsWithAge, type = integer)
 *            , @AlgorithmSoftParameter (name = band4ProcessBillsWithUnpaidAmount, type = decimal)
 *            , @AlgorithmSoftParameter (name = band4ProcessBillsWithUnpaidPercentage, type = decimal)
 *            , @AlgorithmSoftParameter (name = band4CustomerStatus, type = string)
 *            , @AlgorithmSoftParameter (entityName = cmDelinquencyProcessType, name = band4DelinquencyProcessType, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = band5UnpaidAmountAndPercentage, type = lookup)
 *            , @AlgorithmSoftParameter (name = band5ProcessBillsWithAge, type = integer)
 *            , @AlgorithmSoftParameter (name = band5ProcessBillsWithUnpaidAmount, type = decimal)
 *            , @AlgorithmSoftParameter (name = band5ProcessBillsWithUnpaidPercentage, type = decimal)
 *            , @AlgorithmSoftParameter (name = band5CustomerStatus, type = string)
 *            , @AlgorithmSoftParameter (entityName = cmDelinquencyProcessType, name = band5DelinquencyProcessType, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = includeOnAccountPayments, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = featureConfiguration, name = adminstrativeContractTypeFeatureConfig, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = customerManagementOption, name = adminstrativeContractTypeOptionType, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = userCurrentRevenuePeriodBilled, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = billIdCharacteristicType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = messageCategory, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = messageNumber, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = custStatusOptionType, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = custStatusOptionValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = businessObject, name = delinquencyProcessSeederBO, required = true, type = entity)})
 */

public class CmDelinquencyCustomerMonitorRuleAlgComp_Impl extends CmDelinquencyCustomerMonitorRuleAlgComp_Gen implements CmDelinquencyControlMonitorAlgorithmSpot {

	Logger logger = LoggerFactory.getLogger(CmDelinquencyCustomerMonitorRuleAlgComp_Impl.class);

	private Person_Id personId = null;
	private Account_Id accountId = null;
	private Bool isProcessingCompleteSwitch;
	private BigInteger creditRatingPoints = BigInteger.ZERO;
	private BigInteger installationCreditRatingThreshold = BigInteger.ZERO;
	@SuppressWarnings("unused")
	private BigInteger installationCashOnlyThreshold = BigInteger.ZERO;
	private String customerStatus = null;

	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
		
		// Determine Age Date Value is B or D
		if (notBlank(getDeterminesAgeDate()) && !(getDeterminesAgeDate().equals(CmDelinquencyCustomerMonitorRuleConstants.CONST_B)
				|| getDeterminesAgeDate().equals(CmDelinquencyCustomerMonitorRuleConstants.CONST_D))) {

			addError(MessageRepository.invalidAlgorithmParmValues(CmDelinquencyCustomerMonitorRuleConstants.DATE_DETERMINE_AGE_DESC, CmDelinquencyCustomerMonitorRuleConstants.DATE_DETERMINE_AGE_DESC_VAL));
		}

		// Validate include on account payments is Y or N
		if (notNull(getIncludeOnAccountPayments()) && !(getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())
				|| getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue()))) {

			addError(MessageRepository
					.invalidUnpaidAmtandPer(
							getAlgorithm().getAlgorithmType().getParameterAt(38).fetchLanguageParameterLabel(),
							getIncludeOnAccountPayments().trimmedValue()));
		}

		// if on account payments is Y validates feature configuration and
		// option type
		if (notNull(getIncludeOnAccountPayments()) && getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {
			if (isNull(getAdminstrativeContractTypeFeatureConfig()))
				reportRequiredParameter("adminstrativeContractTypeFeatureConfig", 39);
			if (isNull(getAdminstrativeContractTypeOptionType()))
				reportRequiredParameter("adminstrativeContractTypeOptionType", 40);

		}

		// Validate Use current Revenue period is Y or N
		if (notNull(getUserCurrentRevenuePeriodBilled()) && !(getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue()))
				|| (getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue()))) {

			addError(MessageRepository
					.invalidUnpaidAmtandPer(
							getAlgorithm().getAlgorithmType().getParameterAt(41).fetchLanguageParameterLabel(),
							getUserCurrentRevenuePeriodBilled().trimmedValue()));
		}

	}

	@Override
	public void invoke() {

		// Validate Limiting characteristics input parameter
		validateLimitingCharParams();
		// Validate Band Parameters
		validateBandParameters();
		// Validate Postpone Date Characteristic Type for the Bill entity
		validateCharTypeForEntity(getPostponeDateCharacteristicType(), CharacteristicEntityLookup.constants.BILL);
		// Validate Bill ID Characteristic Type is valid for the Delinquency
		// Process Logs entity
		validateCharTypeForEntity(getBillIdCharacteristicType(), CharacteristicEntityLookup.constants.CM_DELINQUENCY_PROCESS_LOG);

		// Check Limiting Characteristics
		if (!isBlankOrNull(getLimitingCharacteristicEntity())) {
			if (!limitingCharacteristicFound()) {

				isProcessingCompleteSwitch = Bool.FALSE;
				return;

			}
		}

		// Get Customer Status
		if (isCustomerActive()) {

			customerStatus = CmDelinquencyCustomerMonitorRuleConstants.CONST_A;
			logger.info("Customer Status == " + customerStatus);
		} else {

			customerStatus = CmDelinquencyCustomerMonitorRuleConstants.CONST_C;
			logger.info("Customer Status == " + customerStatus);
		}

		// Check if Band for Retrieved Customer Status Exist or Not
		if (!bandForCustomerStatusExist(customerStatus)) {			
			isProcessingCompleteSwitch = Bool.FALSE;
		     return;
		}

		evaluateCustomerOrAccountForDelinquency(customerStatus);
		isProcessingCompleteSwitch = Bool.TRUE;
	}

	/**
	 * Validate Limiting Characteristics input parameter
	 * 
	 */

	private void validateLimitingCharParams() {

		if (!isNull(getLimitingCharacteristicType())
				|| !isBlankOrNull(getLimitingCharacteristicValue())
				|| !isNull(getLimitingCharEffectiveWithinXDays())) {

			if (isBlankOrNull(getLimitingCharacteristicEntity())) {

				addError(MessageRepository
						.algoParamMissing(CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_ENT_DESC));

			}
		}

		if (!isBlankOrNull(getLimitingCharacteristicEntity())) {

			if (!isValidCharEntity(getLimitingCharacteristicEntity())) {

				addError(MessageRepository
						.algoParamInvalid(CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_ENT_DESC));
			}
		}
		if (!isBlankOrNull(getLimitingCharacteristicEntity())
				|| !isBlankOrNull(getLimitingCharacteristicValue())
				|| !isNull(getLimitingCharEffectiveWithinXDays())) {

			if (isNull(getLimitingCharacteristicType())) {

				addError(MessageRepository
						.algoParamMissing(CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_TYPE_DESC));

			}
		}

		if (!isNull(getLimitingCharacteristicType())) {

			if (!isValidCharType(getLimitingCharacteristicType())) {

				addError(MessageRepository
						.algoParamInvalid(CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_TYPE_DESC));
			}
			if (!isValidCharTypeFlg(getLimitingCharacteristicType())) {

				addError(MessageRepository
						.algoParamInvalid(CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_TYPE_DESC));
			}
		}

		if (!isBlankOrNull(getLimitingCharacteristicEntity())
				|| !isNull(getLimitingCharacteristicType())
				|| !isNull(getLimitingCharEffectiveWithinXDays())) {

			if (isBlankOrNull(getLimitingCharacteristicValue())) {

				addError(MessageRepository
						.algoParamMissing(CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_VAL_DESC));

			}
		}

		if (!isBlankOrNull(getLimitingCharacteristicValue())) {
			if (notNull(getLimitingCharacteristicType())) {

				CharacteristicType_Id characteristicTypeId = getLimitingCharacteristicType().getId();
				if (characteristicTypeId.getEntity().getCharacteristicType().isPredefinedValue()) {
					if (!isValidPredefineCharVal(characteristicTypeId.getEntity(), getLimitingCharacteristicValue())) {

						addError(MessageRepository
								.charValueIsInvalidForCharType(characteristicTypeId, getLimitingCharacteristicValue()));
					}

				}
			}
		}

		if (!isBlankOrNull(getLimitingCharacteristicEntity())
				|| !isNull(getLimitingCharacteristicType())
				|| !isNull(getLimitingCharacteristicValue())) {

			if (isNull(getLimitingCharEffectiveWithinXDays())) {

				addError(MessageRepository
						.algoParamMissing(CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_EFF_WITHIN_XDAYS_DESC));

			}
		}

	}

	/**
	 * Validates that the Characteristic Type is valid for the given entity.
	 * 
	 * @param charType
	 * @param charEntity
	 */
	private void validateCharTypeForEntity(CharacteristicType charType, CharacteristicEntityLookup charEntity) {
		if (notNull(charType)) {
			CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntity);
			if (isNull(charEntityId.getEntity())) {
				addError(MessageRepository.charTypeIsInvalidForEntity(charType.getId(),
						charEntity.getLookupValue().getEffectiveDescription()));
			}
		}

	}

	/**
	 * Validate Characteristic Type Entity A or S or P
	 * @param charTypeCode -  String Characteristic Type entity input parameter
	 * @return - boolean - true or false
	 */
	private Boolean isValidCharEntity(String charEntity) {

		if ((charEntity.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_A) == 0)
				|| (charEntity.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_P) == 0)
				|| (charEntity.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_S) == 0)) {
			return true;
		}
		return false;
	}

	/**
	 * Validate Char Type Code input parameter
	 * @param charTypeCode -  String Characteristic Type code input parameter
	 * @return - boolean - true or false
	 */

	private Boolean isValidCharType(CharacteristicType charTypeCode) {

		CharacteristicType_Id characteristicTypeId = charTypeCode.getId();

		if (isNull(characteristicTypeId.getEntity())) {
			return false;
		}
		return true;
	}

	/**
	 * Validate Characteristic Type Flag id ADV or DFV
	 * @param charTypeCode -  String Characteristic Type Code input parameter
	 * @return - boolean - true or false
	 */
	private Boolean isValidCharTypeFlg(CharacteristicType charTypeCode) {

		CharacteristicType_Id characteristicTypeId = charTypeCode.getId();

		if (characteristicTypeId.getEntity().getCharacteristicType().isAdhocValue() || characteristicTypeId.getEntity().getCharacteristicType().isPredefinedValue()) {

			return true;
		}

		return false;
	}

	/**
	 * This method checks if char value provided is valid for characteristic type
	 * @param Char Type
	 * 		  Char Val  
	 * @return- boolean - true or false
	 */
	private boolean isValidPredefineCharVal(CharacteristicType charType, String inputCharVal) {

		CharacteristicValue_Id charValId = null;
		CharacteristicValue charVal = null;

		charValId = new CharacteristicValue_Id(charType, inputCharVal);
		charVal = charValId.getEntity();

		if (isNull(charVal)) {
			return false;
		}

		return true;

	}

	/**
	 * Validate All Remaining Band Criteria Input Parameter
	 * 
	 */
	private void validateBandParameters() {
		validateBand(getBand1DelinquencyProcessType(),
				getBand1CustomerStatus(), getBand1ProcessBillsWithAge(),
				getBand1ProcessBillsWithUnpaidAmount(),
				getBand1ProcessBillsWithUnpaidPercentage(),
				getBand1UnpaidAmountAndPercentage(),
				CmDelinquencyCustomerMonitorRuleConstants.BAND_1);
		validateBand(getBand2DelinquencyProcessType(),
				getBand2CustomerStatus(), getBand2ProcessBillsWithAge(),
				getBand2ProcessBillsWithUnpaidAmount(),
				getBand2ProcessBillsWithUnpaidPercentage(),
				getBand2UnpaidAmountAndPercentage(),
				CmDelinquencyCustomerMonitorRuleConstants.BAND_2);
		validateBand(getBand3DelinquencyProcessType(),
				getBand3CustomerStatus(), getBand3ProcessBillsWithAge(),
				getBand3ProcessBillsWithUnpaidAmount(),
				getBand3ProcessBillsWithUnpaidPercentage(),
				getBand3UnpaidAmountAndPercentage(),
				CmDelinquencyCustomerMonitorRuleConstants.BAND_3);
		validateBand(getBand4DelinquencyProcessType(),
				getBand4CustomerStatus(), getBand4ProcessBillsWithAge(),
				getBand4ProcessBillsWithUnpaidAmount(),
				getBand4ProcessBillsWithUnpaidPercentage(),
				getBand4UnpaidAmountAndPercentage(),
				CmDelinquencyCustomerMonitorRuleConstants.BAND_4);
		validateBand(getBand5DelinquencyProcessType(),
				getBand5CustomerStatus(), getBand5ProcessBillsWithAge(),
				getBand5ProcessBillsWithUnpaidAmount(),
				getBand5ProcessBillsWithUnpaidPercentage(),
				getBand5UnpaidAmountAndPercentage(),
				CmDelinquencyCustomerMonitorRuleConstants.BAND_5);
	}

	/**
	 * Validate Band - X Criteria input parameter
	 * 
	 */

	private void validateBand(CmDelinquencyProcessType delinquencyProcessType,
			String customerStatus, BigInteger processBillsWithAge,
			BigDecimal processBillsWithUnpaidAmount,
			BigDecimal processBillsWithUnpaidPercentage,
			Lookup unpaidAmountandPercentage, String band) {
		AlgorithmTypeParameter_Id algTypeParam = null;

		if (!isNull(delinquencyProcessType) && (band.compareTo(CmDelinquencyCustomerMonitorRuleConstants.BAND_1) == 0)) {
			validateProcessTypeCdAndDelinquencyLevel(delinquencyProcessType);
			if (!isNull(unpaidAmountandPercentage)) {
				if (!(unpaidAmountandPercentage.trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue()))
						&& !(unpaidAmountandPercentage.trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue()))) {

					addError(MessageRepository
							.invalidUnpaidAmtandPer(
									CmDelinquencyCustomerMonitorRuleConstants.UNPAID_AMT_AND_PER_PARM_DESC,
									unpaidAmountandPercentage.trimmedValue()));
				}
				
				else if(unpaidAmountandPercentage.trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())){
					if(isNull(getBand1ProcessBillsWithUnpaidAmount())){
						reportRequiredParameter("band1ProcessBillsWithUnpaidAmount", 10);
					}
					
					if(isNull(getBand1ProcessBillsWithUnpaidPercentage())){
						reportRequiredParameter("band1ProcessBillsWithUnpaidPercentage", 11);
					}
				}else if(unpaidAmountandPercentage.trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue())){
					if(isNull(getBand1ProcessBillsWithUnpaidAmount()) && isNull(getBand1ProcessBillsWithUnpaidPercentage()) ){
						
						algTypeParam = new AlgorithmTypeParameter_Id(getAlgorithm().getAlgorithmType(), new BigInteger("11"));
						String unpaidAmountLbl = algTypeParam.getEntity().fetchLanguageParameterLabel();
						
						algTypeParam = new AlgorithmTypeParameter_Id(getAlgorithm().getAlgorithmType(), new BigInteger("12"));
						String unpaidPercentLbl = algTypeParam.getEntity().fetchLanguageParameterLabel();
						
						addError(MessageRepository.atleastOneParameterShouldBeProvided(unpaidAmountLbl, unpaidPercentLbl));
					}
					
					
				}
			}
			if (!isNull(processBillsWithUnpaidPercentage)) {
				if (!(processBillsWithUnpaidPercentage.compareTo(BigDecimal.ZERO) >= 0)
						|| !(processBillsWithUnpaidPercentage.compareTo(BigDecimal.TEN.multiply(BigDecimal.TEN)) <= 0)) {

					addError(MessageRepository
							.invalidProcessBillWithUnpaidPer(CmDelinquencyCustomerMonitorRuleConstants.PROC_BIIL_WITH_UNPAID_PER_PARM_DESC));

				}

			}
			if (!isBlankOrNull(customerStatus)) {
				if (!(customerStatus.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_A) == 0)
						&& !(customerStatus.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_C) == 0)) {

					addError(MessageRepository
							.invalidCustomerStatus(
									CmDelinquencyCustomerMonitorRuleConstants.CUSTOMER_STATUS_PARM_DESC,
									customerStatus));
				}
			}

		}
		else {
			if (!isNull(delinquencyProcessType)) {

				
				//if (isBlankOrNull(customerStatus)
				//		|| isNull(processBillsWithAge)
				//		|| isNull(processBillsWithUnpaidAmount)
				//		|| isNull(processBillsWithUnpaidPercentage)
				//		|| isNull(unpaidAmountandPercentage)) {
				if (isBlankOrNull(customerStatus)
						|| isNull(processBillsWithAge)
						|| isNull(unpaidAmountandPercentage)) {
				

					addError(MessageRepository.invalidDelParms(band));
				}

				validateProcessTypeCdAndDelinquencyLevel(delinquencyProcessType);

				if (!isNull(unpaidAmountandPercentage)) {
					
					
					int unpaidAmtSeq = 0;
					int unpaidPrctSeq = 0;
					
					if(band.equalsIgnoreCase(CmDelinquencyCustomerMonitorRuleConstants.BAND_2)){
						unpaidAmtSeq = 16;
						unpaidPrctSeq = 17;
					}else if(band.equalsIgnoreCase(CmDelinquencyCustomerMonitorRuleConstants.BAND_3)){
						unpaidAmtSeq = 22;
						unpaidPrctSeq = 23; 
					}else if(band.equalsIgnoreCase(CmDelinquencyCustomerMonitorRuleConstants.BAND_4)){
						unpaidAmtSeq = 28;
						unpaidPrctSeq = 29;
					}else if(band.equalsIgnoreCase(CmDelinquencyCustomerMonitorRuleConstants.BAND_5)){
						unpaidAmtSeq = 34;
						unpaidPrctSeq = 35;
					}
					
					
					
					if (!(unpaidAmountandPercentage.trimmedValue().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_Y) == 0)
							&& !(unpaidAmountandPercentage.trimmedValue().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_N) == 0)) {

						addError(MessageRepository
								.invalidUnpaidAmtandPer(
										CmDelinquencyCustomerMonitorRuleConstants.UNPAID_AMT_AND_PER_PARM_DESC,
										unpaidAmountandPercentage.trimmedValue()));
					}
					else if(unpaidAmountandPercentage.trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())){
						if(isNull(getBand1ProcessBillsWithUnpaidAmount())){
							reportRequiredParameter("band1ProcessBillsWithUnpaidAmount", unpaidAmtSeq);
						}
						
						if(isNull(getBand1ProcessBillsWithUnpaidPercentage())){
							reportRequiredParameter("band1ProcessBillsWithUnpaidPercentage", unpaidPrctSeq);
						}
					}else if(unpaidAmountandPercentage.trimmedValue().equals(YesNoOptionLookup.constants.NO.trimmedValue())){
						if(isNull(getBand1ProcessBillsWithUnpaidAmount()) && isNull(getBand1ProcessBillsWithUnpaidPercentage()) ){
							
							algTypeParam = new AlgorithmTypeParameter_Id(getAlgorithm().getAlgorithmType(), 
									new BigInteger(String.valueOf(unpaidAmtSeq+1)));
							String unpaidAmountLbl = algTypeParam.getEntity().fetchLanguageParameterLabel();
							
							algTypeParam = new AlgorithmTypeParameter_Id(getAlgorithm().getAlgorithmType(), 
									new BigInteger(String.valueOf(unpaidPrctSeq+1)));
							String unpaidPercentLbl = algTypeParam.getEntity().fetchLanguageParameterLabel();
							
							addError(MessageRepository.atleastOneParameterShouldBeProvided(unpaidAmountLbl, unpaidPercentLbl));
					        }
						
						
					}
					
				}
				if (!isNull(processBillsWithUnpaidPercentage)) {

					if (!(processBillsWithUnpaidPercentage.compareTo(BigDecimal.ZERO) >= 0) || !(processBillsWithUnpaidPercentage.compareTo(BigDecimal.TEN.multiply(BigDecimal.TEN)) <= 0)) {
						addError(MessageRepository
								.invalidProcessBillWithUnpaidPer(CmDelinquencyCustomerMonitorRuleConstants.PROC_BIIL_WITH_UNPAID_PER_PARM_DESC));

					}

				}
				if (!isBlankOrNull(customerStatus)) {
					if (!(customerStatus.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_A) == 0)
							&& !(customerStatus.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_C) == 0)) {

						addError(MessageRepository
								.invalidCustomerStatus(
										CmDelinquencyCustomerMonitorRuleConstants.CUSTOMER_STATUS_PARM_DESC,
										customerStatus));
					}
				}

			} else {

				if (!isBlankOrNull(customerStatus)
						|| !isNull(processBillsWithAge)
						|| !isNull(processBillsWithUnpaidAmount)
						|| !isNull(processBillsWithUnpaidPercentage)
						|| !isNull(unpaidAmountandPercentage)) {

					addError(MessageRepository.invalidDelParms(band));
				}

			}

		}
	}

	/**
	 * Validate Process Type Code input parameter
	 * @param charTypeCode -  String Characteristic Type entity input parameter
	 * @return - boolean - true or false
	 */

	private void validateProcessTypeCdAndDelinquencyLevel(CmDelinquencyProcessType typeCode) {

		if (!typeCode.getCmDelinquencyLevel().equals(getDelinquencyLevel())) {

			addError(MessageRepository.invalidDelLvlParms(typeCode.getCmDelinquencyLevel().value(), getDelinquencyLevel().trimmedValue()));

		}

	}

	/**
	 * Determine Age of Bill
	 * @param dueDate -  Due Date of Bill 
	 * @param billDate - Bill Date of Bill
	 * return billAge
	 */

	private BigInteger determineAgeOfBill(Date dueDate, Date billDate) {

		BigInteger billAge = null;

		if (getDeterminesAgeDate().trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_D) == 0) {

			billAge = new BigInteger(String.valueOf(getProcessDateTime().getDate().difference(dueDate).getTotalDays()));

		} else {

			billAge = new BigInteger(String.valueOf(getProcessDateTime().getDate().difference(billDate).getTotalDays()));

		}
		return billAge;
	}

	private boolean checkCreditRating() {
		boolean creditFlag = false;
		getAccountsOrCustomersCreditRating();
		getCreditRatingThresholdFromInstallationOption();

		if (notNull(creditRatingPoints) && notNull(installationCreditRatingThreshold)) {

			creditRatingPoints = creditRatingPoints
					.add(installationCreditRatingThreshold);
		}
		
			if (notNull(creditRatingPoints) && (creditRatingPoints.compareTo(getCreditRatingValue()) <= 0)) {
			creditFlag = true;
			return creditFlag;
			}
			
		return creditFlag;
	}

	/**
	 * Get Account Credit Rating
	 * 
	 */

	private void getAccountsOrCustomersCreditRating() {

		PreparedStatement pst = null;
		try {
			if (getDelinquencyLevel().equals(CmDelinquencyLevelLookup.constants.CM_CUSTOMER)) {
				pst = createPreparedStatement(CmDelinquencyCustomerMonitorRuleConstants.PER_CRT_RATE_SQL.toString(), "CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
				pst.bindId("personId", personId);
				pst.bindDate("processDate", getProcessDateTime().getDate());
			} else if (getDelinquencyLevel().equals(CmDelinquencyLevelLookup.constants.CM_ACCOUNT)) {
				pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.ACCT_CRT_RATE_SQL.toString(),
						"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
				pst.bindId("accountId", accountId);
				pst.bindDate("processDate", getProcessDateTime().getDate());

			}
			List<SQLResultRow> result = pst.list();
			pst.close();
			if (!isNull(result) && result.size() > 0) {

				SQLResultRow output = result.get(0);
				creditRatingPoints = output.getInteger("CR_RATING_PTS");

			}
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}

	}

	/**
	 * Get Credit rating Threshold From installation option
	 * 
	 */

	private void getCreditRatingThresholdFromInstallationOption() {

		PreparedStatement pst = null;
		try {
			pst = createPreparedStatement(
					CmDelinquencyCustomerMonitorRuleConstants.CRT_RATE_THS_INST_OPTS_SQL
							.toString(), "CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
			List<SQLResultRow> result = pst.list();
			pst.close();
			if (!isNull(result) && result.size() > 0) {

				SQLResultRow output = result.get(0);
				installationCreditRatingThreshold = output
						.getInteger("CR_RAT_THRS");
				installationCashOnlyThreshold = output
						.getInteger("CASH_ONLY_PTS_THRS");
			}
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}

	}

	/**
	 * Get Account List of Customer
	 * @return accountId
	 */

	private List<SQLResultRow> getCustomerAccountList() {

		PreparedStatement pst = null;
		List<SQLResultRow> accountList;
		try {
			pst = createPreparedStatement(
					CmDelinquencyCustomerMonitorRuleConstants.GET_CUST_ACCT.toString(),
					"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");

			pst.bindId("personId", personId);
			accountList = pst.list();

		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}
		return accountList;
	}

	/**
	 * Check if limiting CHaracteristics exist on account or contract or person
	 * 
	 */

	private boolean limitingCharacteristicFound() {

		PreparedStatement pst = null;
		String limitcharEntity = getLimitingCharacteristicEntity();
		Boolean limitCharFlag = false;
		List<SQLResultRow> resultList = null;

		Integer xDays = Integer.parseInt(getLimitingCharEffectiveWithinXDays().toString()) * -1;
		Date effectiveDate = getProcessDateTime().getDate().addDays(xDays);
		try {
			if (limitcharEntity.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_P) == 0) {
				pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_FOR_PER_SQL
								.toString(),
						"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
				pst.bindId("personId", personId);
				pst.bindString("limitingCharValAlgoParm", getLimitingCharacteristicValue(), "");
				pst.bindEntity("limitingCharTypeAlgoParm", getLimitingCharacteristicType());
				pst.bindDate("effectiveDate", effectiveDate);

				resultList = pst.list();
				pst.close();
				if (!isNull(resultList) && resultList.size() > 0) {
					limitCharFlag = true;
					return limitCharFlag;
				}

			} else {
				List<SQLResultRow> customerAccountList = getCustomerAccountList();
				if (!isNull(customerAccountList) && (customerAccountList.size() > 0)) {
					SQLResultRow result = null;
					Iterator<SQLResultRow> acctIterator = customerAccountList.iterator();

					while (acctIterator.hasNext()) {
						result = acctIterator.next();
						Account account = result.getEntity("ACCT_ID", Account.class);
						accountId = account.getId();
						if (limitcharEntity.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_A) == 0) {
							pst = createPreparedStatement(
									CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_FOR_ACCT_SQL
											.toString(),
									"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
							pst.bindId("accountId", accountId);
							pst.bindString("limitingCharValAlgoParm",
									getLimitingCharacteristicValue(), "");
							pst.bindEntity("limitingCharTypeAlgoParm", getLimitingCharacteristicType());
							pst.bindDate("effectiveDate", effectiveDate);

							resultList = pst.list();
							pst.close();
							if (!isNull(resultList) && resultList.size() > 0) {
								limitCharFlag = true;
								return limitCharFlag;
							}
						} else {
							pst = createPreparedStatement(
									CmDelinquencyCustomerMonitorRuleConstants.LMT_CHAR_FOR_SA_SQL
											.toString(),
									"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
							pst.bindId("accountId", accountId);
							pst.bindString("limitingCharValAlgoParm",
									getLimitingCharacteristicValue(), "");
							pst.bindEntity("limitingCharTypeAlgoParm", getLimitingCharacteristicType());
							pst.bindDate("effectiveDate", effectiveDate);

							resultList = pst.list();
							pst.close();
							if (!isNull(resultList) && resultList.size() > 0) {
								limitCharFlag = true;
								return limitCharFlag;
							}
						}
					}
				}
			}
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}
		return limitCharFlag;
	}

	/**
	 * Determine Postpone Credit Review Date
	 * @param billId -  String Bill Id
	 */

	private boolean isPostponeCreditReviewDateFound(String billId) {

		Date postponeCrtRvwDate = null;
		PreparedStatement pst = null;
		List<SQLResultRow> resultList = null;

		if (notNull(getPostponeDateCharacteristicType())) {

			postponeCrtRvwDate = getPostponeCreditReviewDate(billId);
		}
		try {
			if (!isNull(postponeCrtRvwDate)
					&& postponeCrtRvwDate.isAfter(getProcessDateTime().getDate())) {

				pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.EXIST_CUST_CRED_REVW_SCHDULE
								.toString(),
						"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");

				pst.bindId("personId", personId);
				pst.bindDate("retrievedAdhocCharVal", postponeCrtRvwDate);
				resultList = pst.list();

				if (!isNull(resultList) && resultList.size() > 0) {

					SQLResultRow output = resultList.get(0);

					CmPersonCollection_Id perCollId = new CmPersonCollection_Id(personId);

					CmCustomerReviewSchedule_Id customerReviewScheduleId = new CmCustomerReviewSchedule_Id(perCollId.getEntity(), postponeCrtRvwDate);

					if (!isNull(output)) {

						CmCustomerReviewSchedule_DTO customerReviewScheduleDTO = new CmCustomerReviewSchedule_DTO();
						customerReviewScheduleDTO.setId(customerReviewScheduleId);
						customerReviewScheduleDTO.newEntity();
					}
				}
				return true;
			}

		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}

		return false;

	}

	/**
	 * Get postponed credit date for bill
	 * @param billId -  String Bill Id
	 * @return - Date - Postpone Credit Review Date
	 */

	private Date getPostponeCreditReviewDate(String billId) {

		PreparedStatement pst = null;
		String crtRvwDate = "";
		Date pptCrtRvwDate = null;
		try {

			pst = createPreparedStatement(
					CmDelinquencyCustomerMonitorRuleConstants.PP_CRT_REW_DT_SQL.toString(),
					"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
			pst.bindString("billId", billId, "");
			pst.bindEntity("postPoneDateCharType", getPostponeDateCharacteristicType());
			List<SQLResultRow> result = pst.list();
			pst.close();
			if (!isNull(result) && result.size() > 0) {

				SQLResultRow output = result.get(0);
				crtRvwDate = output.getString("ADHOC_CHAR_VAL");
				try {
					pptCrtRvwDate = Date.fromString(crtRvwDate, new DateFormat(
							CmDelinquencyCustomerMonitorRuleConstants.DT_FORMAT));
				} catch (DateFormatParseException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}
		return pptCrtRvwDate;

	}

	/**
	 * Get Completed Bills Not Linked to Active Delinquency Process That is Not Fully Balanced
	 * @return - List<SQLResultRow> - Bill list 
	 */

	private List<SQLResultRow> getCompletedBillsNotLinkToActiveDelinquencyProcess() {

		PreparedStatement pst = null;
		List<SQLResultRow> billList = null;
		try {
			if (getDelinquencyLevel().equals(CmDelinquencyLevelLookup.constants.CM_CUSTOMER)
					|| getDelinquencyLevel().equals(CmDelinquencyCustomerMonitorRuleConstants.CONST_B)) {

				pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.GET_COMPLT_BILL_CUST_SQL
								.toString(),
						"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
				pst.bindId("personId", personId);
			} else {
				pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.GET_COMPLT_BILL_ACCT_SQL
								.toString(),
						"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
				pst.bindId("accountId", accountId);
			}
			billList = pst.list();
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}
		return billList;

	}

	/**
	 * Add Bill to Delinquency Process
	 * @param billId -  Bill Id
	 */

	private void addBilltoDelinquencyProcess(String billId, CmDelinquencyProcess_Id delinqProcessId) {

		CmDelinquencyProcessRelatedObject_DTO relObjDTO = createDTO(CmDelinquencyProcessRelatedObject.class);

		MaintenanceObject_Id maintenanceObjectId = new MaintenanceObject_Id(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL);
		delinqProcessId.getEntity().getRelatedObjects().add(relObjDTO, maintenanceObjectId, CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON, billId);

		logger.info("Bill ==" + billId + " added to Existing Delinquency Process==" + delinqProcessId.getIdValue());

		MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<
				CmDelinquencyProcess, CmDelinquencyProcessLog>(
						delinqProcessId.getEntity().getBusinessObject().getMaintenanceObject(), delinqProcessId.getEntity());
		logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM,
				MessageRepository.billIdMessageDel(billId), null, getBillIdCharacteristicType(), new
				Bill_Id(billId).getEntity());

		logger.info("Bill ==" + billId +
				" added to Delinquency Process Log for Delinquency Process ==" +
				delinqProcessId.getIdValue());

	}

	/**
	 * Create Delinquency Process
	 * @param bandProcessType -  Delinquency Process Type Code
	 * @param delinquentBills - List of delinquent bill
	 */

	private void createDelinquencyProcess(CmDelinquencyProcessType bandDelProcessType, List<String> billIdList) {

		CmDelinquencyProcess_Id delinquencyProcessId = null;
		CmDelinquencyProcess cmDelinquencyProcess = null;

		CmDelinquencyProcessType_Id delProcTypeId = bandDelProcessType.getId();
		BusinessObjectInstance delProcBOInstance = BusinessObjectInstance.create(getDelinquencyProcessSeederBO());

		delProcBOInstance.set("cmDelinquencyProcessType", delProcTypeId.getEntity());
		delProcBOInstance.set("businessObject", getDelinquencyProcessSeederBO());
		delProcBOInstance.set("status", delProcTypeId.getEntity().getRelatedTransactionBO().getInitialStatus().getStatusString());
		delProcBOInstance.set("creationDateTime", getProcessDateTime());
		delProcBOInstance.set("statusDateTime", getProcessDateTime());
			
		COTSInstanceList cotsInstanceList = delProcBOInstance.getList(CmDelinquencyCustomerMonitorRuleConstants.DEL_REL_OBJ_LIST_NODE);
		
		if(getDelinquencyLevel().trimmedValue().equals(CmDelinquencyLevelLookup.constants.CM_CUSTOMER.trimmedValue())){
			
			COTSInstanceListNode cotsInstanceListNode = cotsInstanceList.newChild();
			cotsInstanceListNode.set("maintenanceObject", CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_PERSON);
			cotsInstanceListNode.set("cmDelinquencyRelatedObjTypeFlg", CmDelinquencyCustomerMonitorRuleConstants.DEL_PROC_RELATED_OBJECT_TYPE_FLAG);
			cotsInstanceListNode.set("primaryKeyValue1", personId.getIdValue());
		}
		else if(getDelinquencyLevel().trimmedValue().equals(CmDelinquencyLevelLookup.constants.CM_ACCOUNT.trimmedValue())){
			
			COTSInstanceListNode cotsInstanceListNode = cotsInstanceList.newChild();
			cotsInstanceListNode.set("maintenanceObject", CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_ACCOUNT);
			cotsInstanceListNode.set("cmDelinquencyRelatedObjTypeFlg", CmDelinquencyCustomerMonitorRuleConstants.DEL_PROC_RELATED_OBJECT_TYPE_FLAG);
			cotsInstanceListNode.set("primaryKeyValue1", accountId.getIdValue());		
		}
	
		Iterator<String> bill = billIdList.iterator();
		while (bill.hasNext()) {
			
			COTSInstanceListNode cotsInstanceListNode = cotsInstanceList.newChild();
			cotsInstanceListNode.set("maintenanceObject", CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL);
			cotsInstanceListNode.set("cmDelinquencyRelatedObjTypeFlg", CmDelinquencyCustomerMonitorRuleConstants.RELATED_OBJECT_TYPE_FLAG);
			cotsInstanceListNode.set("primaryKeyValue1", bill.next());		
		}
			
		delProcBOInstance = BusinessObjectDispatcher.execute(delProcBOInstance, BusinessObjectActionLookup.constants.FAST_ADD);
		
		String delProcId = delProcBOInstance.getElement().selectSingleNode(CmDelinquencyCustomerMonitorRuleConstants.DEL_PROC_ID).getText();
		if (notNull(delProcId)) {
			delinquencyProcessId = new CmDelinquencyProcess_Id(delProcId);
			cmDelinquencyProcess = delinquencyProcessId.getEntity();
		}

		Iterator<String> delinquentBill = billIdList.iterator();
		while (delinquentBill.hasNext()) {

			String billId = delinquentBill.next();

			MaintenanceObjectLogHelper<CmDelinquencyProcess, CmDelinquencyProcessLog> logHelper = new MaintenanceObjectLogHelper<
					CmDelinquencyProcess, CmDelinquencyProcessLog>(
							cmDelinquencyProcess.getBusinessObject().getMaintenanceObject(), cmDelinquencyProcess);
			logHelper.addLogEntry(LogEntryTypeLookup.constants.SYSTEM,
					MessageRepository.billIdMessageDel(billId), null, getBillIdCharacteristicType(), new
					Bill_Id(billId).getEntity());
		}

	}

	/**
	 * Check if delinquency process exists for current customer and current band�s Delinquency Process Type.
	 * 
	 */
	private CmDelinquencyProcess_Id delinquencyProcessExists(CmDelinquencyProcessType delinquencyProcessType) {

		PreparedStatement pst = null;
		CmDelinquencyProcess_Id delinquencyProcessId = null;
		try {
			if (getDelinquencyLevel().equals(CmDelinquencyLevelLookup.constants.CM_CUSTOMER)) {
				pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.EXISTS_DELINQ_PROC_PER_LVL.toString(),
						"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
				pst.bindId("personId", personId);
				pst.bindEntity("delinProcTypeCd", delinquencyProcessType);

			} else if (getDelinquencyLevel().equals(CmDelinquencyLevelLookup.constants.CM_ACCOUNT)) {

				pst = createPreparedStatement(
						CmDelinquencyCustomerMonitorRuleConstants.EXISTS_DELINQ_PROC_ACCT_LVL.toString(),
						"CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
				pst.bindId("accountId", accountId);
				pst.bindEntity("delinProcTypeCd", delinquencyProcessType);
			}
			List<SQLResultRow> result = pst.list();
			pst.close();
			if (!isNull(result) && result.size() > 0) {

				SQLResultRow output = result.get(0);
				delinquencyProcessId = new CmDelinquencyProcess_Id(output.getString("CM_DELIN_PROC_ID"));
			}
		} finally {
			if (!isNull(pst)) {
				pst.close();
				pst = null;
			}
		}
		return delinquencyProcessId;
	}

	/**
	 * Get Customer Status from Policy helper business component
	 */

	private boolean isCustomerActive() {

		Boolean status = false;
		CmPolicyHelper policyHelper = CmPolicyHelper.Factory.newInstance();

		status = policyHelper.isActiveCustomer(personId, getProcessDateTime().getDate(), getCustStatusOptionType(), getCustStatusOptionValue());
		return status;

	}

	/**
	 * Check if Customer Status exists for any band 1...5 
	 * 
	 */

	private boolean bandForCustomerStatusExist(String customerStatus) {

		if (!isBlankOrNull(getBand1CustomerStatus())
				&& (getBand1CustomerStatus().trim().compareTo(customerStatus.trim()) == 0)) {

			return true;
		} else if (!isBlankOrNull(getBand2CustomerStatus())
				&& (getBand2CustomerStatus().trim().compareTo(customerStatus.trim()) == 0)) {

			return true;
		} else if (!isBlankOrNull(getBand3CustomerStatus())
				&& (getBand3CustomerStatus().trim().compareTo(customerStatus.trim()) == 0)) {

			return true;
		} else if (!isBlankOrNull(getBand4CustomerStatus())
				&& (getBand4CustomerStatus().trim().compareTo(customerStatus.trim()) == 0)) {

			return true;
		} else if (!isBlankOrNull(getBand5CustomerStatus())
				&& (getBand5CustomerStatus().trim().compareTo(customerStatus.trim()) == 0)) {

			return true;
		}
		return false;
	}

	/**
	 * Main Processing 
	 * 
	 */
	private void evaluateCustomerOrAccountForDelinquency(String customerStatus) {
		if (getDelinquencyLevel().equals(CmDelinquencyLevelLookup.constants.CM_CUSTOMER)) {

			if(notNull(getCreditRatingValue())){			
				
				if (!checkCreditRating()) {
					isProcessingCompleteSwitch = Bool.FALSE;
					return;
				}
			}

			if (isMoreThanOneCustomerStatusExist(customerStatus)) {
				addError(MessageRepository.validateMultiCustStatus(customerStatus));
			}

			if (!isBlankOrNull(getBand1CustomerStatus())
					&& (getBand1CustomerStatus().trim().compareTo(customerStatus) == 0)) {

				processBandCriteriaForCustomer(getBand1DelinquencyProcessType(),
						getBand1ProcessBillsWithAge(),
						getBand1ProcessBillsWithUnpaidAmount(),
						getBand1ProcessBillsWithUnpaidPercentage(),
						getBand1UnpaidAmountAndPercentage(), customerStatus);
				return;
			}
			if (!isBlankOrNull(getBand2CustomerStatus())
					&& (getBand2CustomerStatus().trim().compareTo(customerStatus) == 0)) {
				processBandCriteriaForCustomer(getBand2DelinquencyProcessType(),
						getBand2ProcessBillsWithAge(),
						getBand2ProcessBillsWithUnpaidAmount(),
						getBand2ProcessBillsWithUnpaidPercentage(),
						getBand2UnpaidAmountAndPercentage(), customerStatus);
				return;
			}
			if (!isBlankOrNull(getBand3CustomerStatus())
					&& (getBand3CustomerStatus().trim().compareTo(customerStatus) == 0)) {
				processBandCriteriaForCustomer(getBand3DelinquencyProcessType(),
						getBand3ProcessBillsWithAge(),
						getBand3ProcessBillsWithUnpaidAmount(),
						getBand3ProcessBillsWithUnpaidPercentage(),
						getBand3UnpaidAmountAndPercentage(), customerStatus);
				return;
			}
			if (!isBlankOrNull(getBand4CustomerStatus())
					&& (getBand4CustomerStatus().trim().compareTo(customerStatus) == 0)) {
				processBandCriteriaForCustomer(getBand4DelinquencyProcessType(),
						getBand4ProcessBillsWithAge(),
						getBand4ProcessBillsWithUnpaidAmount(),
						getBand4ProcessBillsWithUnpaidPercentage(),
						getBand4UnpaidAmountAndPercentage(), customerStatus);
				return;
			}
			if (!isBlankOrNull(getBand5CustomerStatus())
					&& (getBand5CustomerStatus().trim().compareTo(customerStatus) == 0)) {
				processBandCriteriaForCustomer(getBand5DelinquencyProcessType(),
						getBand5ProcessBillsWithAge(),
						getBand5ProcessBillsWithUnpaidAmount(),
						getBand5ProcessBillsWithUnpaidPercentage(),
						getBand5UnpaidAmountAndPercentage(), customerStatus);
				return;
			}

		} else if (getDelinquencyLevel().equals(CmDelinquencyLevelLookup.constants.CM_ACCOUNT)) {

			List<SQLResultRow> customerAccountList = getCustomerAccountList();
			if (!isNull(customerAccountList) && customerAccountList.size() > 0) {
				SQLResultRow result = null;
				Iterator<SQLResultRow> acctIterator = customerAccountList.iterator();
				while (acctIterator.hasNext()) {

					result = acctIterator.next();
					Account account = result.getEntity("ACCT_ID", Account.class);
					accountId = account.getId();

					if (notNull(getCreditRatingValue())) {

						if (!checkCreditRating()) {
							isProcessingCompleteSwitch = Bool.FALSE;
							return;
						}
					}

					if (isMoreThanOneCustomerStatusExist(customerStatus)) {
						addError(MessageRepository
								.validateMultiCustStatus(customerStatus));
					}

					if (!isBlankOrNull(getBand1CustomerStatus())
							&& (getBand1CustomerStatus().trim().compareTo(customerStatus) == 0)) {

						processBandCriteriaForAccount(getBand1DelinquencyProcessType(),
								getBand1ProcessBillsWithAge(),
								getBand1ProcessBillsWithUnpaidAmount(),
								getBand1ProcessBillsWithUnpaidPercentage(),
								getBand1UnpaidAmountAndPercentage(), accountId.getIdValue(), customerStatus);

						if (!acctIterator.hasNext()) {

							return;
						}

					}

					if (!isBlankOrNull(getBand2CustomerStatus())
							&& (getBand2CustomerStatus().trim().compareTo(customerStatus) == 0)) {
						processBandCriteriaForAccount(getBand2DelinquencyProcessType(),
								getBand2ProcessBillsWithAge(),
								getBand2ProcessBillsWithUnpaidAmount(),
								getBand2ProcessBillsWithUnpaidPercentage(),
								getBand2UnpaidAmountAndPercentage(), accountId.getIdValue(), customerStatus);
						if (!acctIterator.hasNext()) {

							return;
						}

					}

					if (!isBlankOrNull(getBand3CustomerStatus())
							&& (getBand3CustomerStatus().trim().compareTo(customerStatus) == 0)) {
						processBandCriteriaForAccount(getBand3DelinquencyProcessType(),
								getBand3ProcessBillsWithAge(),
								getBand3ProcessBillsWithUnpaidAmount(),
								getBand3ProcessBillsWithUnpaidPercentage(),
								getBand3UnpaidAmountAndPercentage(), accountId.getIdValue(), customerStatus);
						if (!acctIterator.hasNext()) {

							return;
						}

					}

					if (!isBlankOrNull(getBand4CustomerStatus())
							&& (getBand4CustomerStatus().trim().compareTo(customerStatus) == 0)) {
						processBandCriteriaForAccount(getBand4DelinquencyProcessType(),
								getBand4ProcessBillsWithAge(),
								getBand4ProcessBillsWithUnpaidAmount(),
								getBand4ProcessBillsWithUnpaidPercentage(),
								getBand4UnpaidAmountAndPercentage(), accountId.getIdValue(), customerStatus);
						if (!acctIterator.hasNext()) {

							return;
						}

					}
					if (!isBlankOrNull(getBand5CustomerStatus())
							&& (getBand5CustomerStatus().trim().compareTo(customerStatus) == 0)) {
						processBandCriteriaForAccount(getBand5DelinquencyProcessType(),
								getBand5ProcessBillsWithAge(),
								getBand5ProcessBillsWithUnpaidAmount(),
								getBand5ProcessBillsWithUnpaidPercentage(),
								getBand5UnpaidAmountAndPercentage(), accountId.getIdValue(), customerStatus);
						if (!acctIterator.hasNext()) {

							return;
						}

					}

				}

			}

		} else {

			if (!isBlankOrNull(getBand1CustomerStatus())
					&& (getBand1CustomerStatus().trim().compareTo(customerStatus) == 0)) {

				processBandCriteriaForCustomerBills(getBand1DelinquencyProcessType(),
						getBand1ProcessBillsWithAge(),
						getBand1ProcessBillsWithUnpaidAmount(),
						getBand1ProcessBillsWithUnpaidPercentage(),
						getBand1UnpaidAmountAndPercentage());
				return;
			}
			if (!isBlankOrNull(getBand2CustomerStatus())
					&& (getBand2CustomerStatus().trim().compareTo(customerStatus) == 0)) {
				processBandCriteriaForCustomerBills(getBand2DelinquencyProcessType(),
						getBand2ProcessBillsWithAge(),
						getBand2ProcessBillsWithUnpaidAmount(),
						getBand2ProcessBillsWithUnpaidPercentage(),
						getBand2UnpaidAmountAndPercentage());
				return;
			}
			if (!isBlankOrNull(getBand3CustomerStatus())
					&& (getBand3CustomerStatus().trim().compareTo(customerStatus) == 0)) {
				processBandCriteriaForCustomerBills(getBand3DelinquencyProcessType(),
						getBand3ProcessBillsWithAge(),
						getBand3ProcessBillsWithUnpaidAmount(),
						getBand3ProcessBillsWithUnpaidPercentage(),
						getBand3UnpaidAmountAndPercentage());
				return;
			}
			if (!isBlankOrNull(getBand4CustomerStatus())
					&& (getBand4CustomerStatus().trim().compareTo(customerStatus) == 0)) {
				processBandCriteriaForCustomerBills(getBand4DelinquencyProcessType(),
						getBand4ProcessBillsWithAge(),
						getBand4ProcessBillsWithUnpaidAmount(),
						getBand4ProcessBillsWithUnpaidPercentage(),
						getBand4UnpaidAmountAndPercentage());
				return;
			}
			if (!isBlankOrNull(getBand5CustomerStatus())
					&& (getBand5CustomerStatus().trim().compareTo(customerStatus) == 0)) {
				processBandCriteriaForCustomerBills(getBand5DelinquencyProcessType(),
						getBand5ProcessBillsWithAge(),
						getBand5ProcessBillsWithUnpaidAmount(),
						getBand5ProcessBillsWithUnpaidPercentage(),
						getBand5UnpaidAmountAndPercentage());
				return;
			}
		}
	}

	/**
	 * Check if multiple Customer Status exists if object level = A 
	 * 
	 */

	private boolean isMoreThanOneCustomerStatusExist(String customerStatus) {

		boolean foundStatus = false;
		if (!isBlankOrNull(getBand1CustomerStatus())
				&& (getBand1CustomerStatus().trim().compareTo(customerStatus) == 0)) {

			foundStatus = true;
		}
		if (!isBlankOrNull(getBand2CustomerStatus())
				&& (getBand2CustomerStatus().trim().compareTo(customerStatus) == 0)) {
			if (foundStatus) {

				return true;
			}
			foundStatus = true;
		}
		if (!isBlankOrNull(getBand3CustomerStatus())
				&& (getBand3CustomerStatus().trim().compareTo(customerStatus) == 0)) {
			if (foundStatus) {

				return true;
			}
			foundStatus = true;
		}
		if (!isBlankOrNull(getBand4CustomerStatus())
				&& (getBand4CustomerStatus().trim().compareTo(customerStatus) == 0)) {
			if (foundStatus) {

				return true;
			}
			foundStatus = true;
		}
		if (!isBlankOrNull(getBand5CustomerStatus())
				&& (getBand5CustomerStatus().trim().compareTo(customerStatus) == 0)) {
			if (foundStatus) {

				return true;
			}
			foundStatus = true;
		}
		return false;
	}

	/**
	 * Process Band criteria for Band - X for Customer
	 * 
	 */

	private void processBandCriteriaForCustomer(CmDelinquencyProcessType delinquencyProcessType,
			BigInteger billWithAge, BigDecimal billWithUnpaidAmt,
			BigDecimal billWithUnpaidPer, Lookup unpaidAmtandPer, String custStatus) {

		logger.info("Processing band criteria for customer ===>");
		SQLResultRow result = null;
		String billId = "";
		Date dueDate = null;
		Date billDate = null;

		// Get active delinquency process for customer
		CmDelinquencyProcess_Id delinquencyProcessId = delinquencyProcessExists(delinquencyProcessType);

		// Get list of completed bills not fully paid that are not linked to an
		// active delinquency
		List<SQLResultRow> billList = getCompletedBillsNotLinkToActiveDelinquencyProcess();

		if (!isNull(billList) && billList.size() > 0) {

			// Set totals to zero
			BigInteger ageOfBill = BigInteger.ZERO;
			BigDecimal billUnpaidAmount = BigDecimal.ZERO;
			BigDecimal billOriginalAmount = BigDecimal.ZERO;
			BigDecimal totalCustomerOriginalAmount = BigDecimal.ZERO;
			BigDecimal totalCustomerUnpaidAmount = BigDecimal.ZERO;
			BigDecimal onAccountPayments = BigDecimal.ZERO;
			BigDecimal totalCustomerUnpaidPercentage = BigDecimal.ZERO;
			BigDecimal hundred = new BigDecimal(100.00);

			// Define list to hold bills of the Customer
			List<String> delinquencyProcessBills = new ArrayList<String>();

			Iterator<SQLResultRow> iterator = billList.iterator();

			// Initialize latest due date before iterating bill list
			Date latestDueDate = null;

			// Process each bill
			while (iterator.hasNext()) {

				result = iterator.next();

				// Get bill details
				billId = result.getString("BILL_ID");
				dueDate = result.getDate("DUE_DT");
				billDate = result.getDate("BILL_DT");

				logger.info("Bill Id == " + billId);
				logger.info("Due Date == " + dueDate.toString());
				logger.info("Bill Date == " + billDate.toString());

				// Process bill only if it is not marked with a postpone credit
				// review date
				if (!isPostponeCreditReviewDateFound(billId)) {

					ageOfBill = determineAgeOfBill(dueDate, billDate);
					logger.info("Age of Bill == " + ageOfBill.toString());

					CmDetermineOpenBillItemAmounts openBillItemAmt = CmDetermineOpenBillItemAmounts.Factory.newInstance();
					OpenItemBillAmountResults output = openBillItemAmt.getBillAmounts(new Bill_Id(billId).getEntity(), null);

					billUnpaidAmount = output.getUnpaidBillAmount().getAmount();
					billOriginalAmount = output.getOriginalBillAmount().getAmount();

					if (notNull(delinquencyProcessId)) {

						if (ageOfBill.compareTo(billWithAge) >= 0) {

							addBilltoDelinquencyProcess(billId, delinquencyProcessId);
						} else {
							if (billUnpaidAmount.compareTo(BigDecimal.ZERO) < 0) {
								addBilltoDelinquencyProcess(billId, delinquencyProcessId);
							}
						}
					} else {
						
						if (ageOfBill.compareTo(billWithAge) >= 0) {
							delinquencyProcessBills.add(billId);
							
							totalCustomerOriginalAmount = totalCustomerOriginalAmount.add(billOriginalAmount);
							totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.add(billUnpaidAmount);
							
							Bill bill = new Bill_Id(billId).getEntity();
							if (isNull(latestDueDate) || latestDueDate.isBefore(bill.getDueDate())) {
								latestDueDate = bill.getDueDate();
							}
						} else {
							if (billUnpaidAmount.compareTo(BigDecimal.ZERO) < 0) {
								delinquencyProcessBills.add(billId);
								
								totalCustomerOriginalAmount = totalCustomerOriginalAmount.add(billOriginalAmount);
								totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.add(billUnpaidAmount);
							}

						}
					}
				}
			}
			if (isNull(delinquencyProcessId) && !isNull(delinquencyProcessBills) && (delinquencyProcessBills.size() > 0)) {

				if (notNull(getIncludeOnAccountPayments()) && (getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue()))) {

					Query<Money> query = createQuery(CmDelinquencyCustomerMonitorRuleConstants.ON_ACCOUNT_PAYMENTS_QUERY_CUSTOMER_LVL.toString(), "CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
					query.bindId("personId", personId);
					query.bindEntity("adminContrFeatureConfig", getAdminstrativeContractTypeFeatureConfig());
					query.bindLookup("adminContrOptionType", getAdminstrativeContractTypeOptionType());
					query.addResult("amount", "SUM(FT.currentAmount)");
					onAccountPayments = query.firstRow().getAmount();

					totalCustomerUnpaidAmount = totalCustomerUnpaidAmount.add(onAccountPayments);
				}
				// If Use Current Revenue Period Billed For Latest Due Date In
				// Threshold Evaluation
				if (notNull(getUserCurrentRevenuePeriodBilled()) && (getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) && notNull(latestDueDate) && (custStatus.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_A.trim()) == 0)) {

					Query<Money> query = createQuery(CmDelinquencyCustomerMonitorRuleConstants.TOT_BILLED_AMOUNT_CUSTOMER_LVL.toString(), "CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
					query.bindId("personId", personId);
					query.bindDate("latestDueDate", latestDueDate);
					query.addResult("amount", "SUM(FT.currentAmount)");

					totalCustomerOriginalAmount = query.firstRow().getAmount();
				}
				if (totalCustomerOriginalAmount.compareTo(BigDecimal.ZERO) == 1) {

					try {
						totalCustomerUnpaidPercentage = totalCustomerUnpaidAmount.divide(totalCustomerOriginalAmount).multiply(hundred);
						logger.info("totalCustomerUnpaidPercentage ==" + totalCustomerUnpaidPercentage);
					} catch (ArithmeticException ae) {

						addError(MessageRepository.arithmeticExpressionError(ae.getLocalizedMessage()));
					}

				}

				boolean addDelinquency = false;

				if (unpaidAmtandPer.trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {

					if (totalCustomerUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0 && totalCustomerUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0) {

						addDelinquency = true;
					}
				} else {


					//if (totalCustomerUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0 || totalCustomerUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0) {
					if(notNull(billWithUnpaidAmt) && totalCustomerUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0){

						addDelinquency = true;
					}


					if(notNull(billWithUnpaidPer) && totalCustomerUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0){
						addDelinquency = true;
					}			
				}

				// Add bills to delinquency if conditions are met
				if (addDelinquency) {

					createDelinquencyProcess(delinquencyProcessType, delinquencyProcessBills);

				}

			}
		}

	}

	/**
	 * Process Band criteria for Band - X for Account
	 * 
	 */
	private void processBandCriteriaForAccount(CmDelinquencyProcessType delinquencyProcessType,
			BigInteger billWithAge, BigDecimal billWithUnpaidAmt,
			BigDecimal billWithUnpaidPer, Lookup unpaidAmtandPer, String idValue, String custStatus) {

		logger.info("Processing band criteria for account ===>");
		SQLResultRow result = null;
		String billId = "";
		Date dueDate = null;
		Date billDate = null;

		CmDelinquencyProcess_Id delinquencyProcessId = delinquencyProcessExists(delinquencyProcessType);

		// Get list of completed bills not fully paid that are not linked to an
		// active Delinquency

		List<SQLResultRow> billList = getCompletedBillsNotLinkToActiveDelinquencyProcess();

		if (!isNull(billList) && billList.size() > 0) {

			// Set account totals to zero
			BigInteger ageOfBill = BigInteger.ZERO;
			BigDecimal billUnpaidAmount = BigDecimal.ZERO;
			BigDecimal billOriginalAmount = BigDecimal.ZERO;
			BigDecimal totalAccountOriginalAmount = BigDecimal.ZERO;
			BigDecimal totalAccountUnpaidAmount = BigDecimal.ZERO;
			BigDecimal onAccountPayments = BigDecimal.ZERO;
			BigDecimal hundred = new BigDecimal(100.00);

			// Define list to hold Delinquency bills of the account
			List<String> delinquencyProcessBills = new ArrayList<String>();

			Iterator<SQLResultRow> iterator = billList.iterator();

			Date latestDueDate = null;

			// Process each bill
			while (iterator.hasNext()) {

				result = iterator.next();

				// Get bill details
				billId = result.getString("BILL_ID");
				dueDate = result.getDate("DUE_DT");
				billDate = result.getDate("BILL_DT");

				logger.info("Bill Id == " + billId);
				logger.info("Due Date == " + dueDate.toString());
				logger.info("Bill Date == " + billDate.toString());
				// Process bill only if it is not marked with a postpone credit
				// review date

				if (!isPostponeCreditReviewDateFound(billId)) {

					ageOfBill = determineAgeOfBill(dueDate, billDate);
					logger.info("Age of Bill == " + ageOfBill.toString());

					CmDetermineOpenBillItemAmounts openBillItemAmt = CmDetermineOpenBillItemAmounts.Factory.newInstance();
					OpenItemBillAmountResults output = openBillItemAmt.getBillAmounts(new Bill_Id(billId).getEntity(), null);

					billUnpaidAmount = output.getUnpaidBillAmount().getAmount();
					billOriginalAmount = output.getOriginalBillAmount().getAmount();

					if (notNull(delinquencyProcessId)) {

						if (ageOfBill.compareTo(billWithAge) >= 0) {

							addBilltoDelinquencyProcess(billId, delinquencyProcessId);
						}

						else {
							if (billUnpaidAmount.compareTo(BigDecimal.ZERO) < 0) {
								addBilltoDelinquencyProcess(billId, delinquencyProcessId);
							}
						}
					} else {

						if (ageOfBill.compareTo(billWithAge) >= 0) {

							delinquencyProcessBills.add(billId);
							
							totalAccountOriginalAmount = totalAccountOriginalAmount.add(billOriginalAmount);
							totalAccountUnpaidAmount = totalAccountUnpaidAmount.add(billUnpaidAmount);

							// If Latest Due Date is null or Latest Due Date <
							// Bill Due Date
							Bill bill = new Bill_Id(billId).getEntity();
							if (isNull(latestDueDate) || latestDueDate.isBefore(bill.getDueDate())) {
								latestDueDate = bill.getDueDate();
							}

						}

						else {

							if (billUnpaidAmount.compareTo(BigDecimal.ZERO) < 0) {

								delinquencyProcessBills.add(billId);
								
								totalAccountOriginalAmount = totalAccountOriginalAmount.add(billOriginalAmount);
								totalAccountUnpaidAmount = totalAccountUnpaidAmount.add(billUnpaidAmount);
								
							}

						}

					}
				}
			}
			if (isNull(delinquencyProcessId) && !isNull(delinquencyProcessBills) && (delinquencyProcessBills.size() > 0)) {

				// If Include On Account Payments In Threshold Evaluation
				// (algorithm parameter) = Y
				if (notNull(getIncludeOnAccountPayments()) && (getIncludeOnAccountPayments().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue()))) {

					Query<Money> query = createQuery(CmDelinquencyCustomerMonitorRuleConstants.ON_ACCOUNT_PAYMENTS_QUERY_ACCOUNT_LVL.toString(), "CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
					query.bindId("account", accountId);
					query.bindEntity("adminContrFeatureConfig", getAdminstrativeContractTypeFeatureConfig());
					query.bindLookup("adminContrOptionType", getAdminstrativeContractTypeOptionType());
					query.addResult("amount", "SUM(FT.currentAmount)");

					onAccountPayments = query.firstRow().getAmount();
					totalAccountUnpaidAmount = totalAccountUnpaidAmount.add(onAccountPayments);
				}
				// If Use Current Revenue Period Billed For Latest Delinquency
				// Due Date In Threshold Evaluation
				if (notNull(getUserCurrentRevenuePeriodBilled()) && (getUserCurrentRevenuePeriodBilled().trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) && notNull(latestDueDate) && (custStatus.trim().compareTo(CmDelinquencyCustomerMonitorRuleConstants.CONST_A.trim()) == 0)) {
					Query<Money> query = createQuery(CmDelinquencyCustomerMonitorRuleConstants.TOT_BILLED_AMOUNT_ACCOUNT_LVL.toString(), "CmDelinquencyCustomerMonitorRuleAlgComp_Impl");
					query.bindId("account", accountId);
					query.bindDate("latestDueDate", latestDueDate);
					query.addResult("amount", "SUM(FT.currentAmount)");

					totalAccountOriginalAmount = query.firstRow().getAmount();
				}
				BigDecimal totalAccountUnpaidPercentage = BigDecimal.ZERO;

				if (totalAccountOriginalAmount.compareTo(BigDecimal.ZERO) == 1) {
					try {

						totalAccountUnpaidPercentage = totalAccountUnpaidAmount.divide(totalAccountOriginalAmount).multiply(hundred);
						logger.info("totalAccountUnpaidPercentage ==" + totalAccountUnpaidPercentage);

					} catch (ArithmeticException ae) {

						addError(MessageRepository.arithmeticExpressionError(ae.getLocalizedMessage()));
					}

				}

				// Determine if unpaid amount and/or percentage conditions are
				// met

				boolean addDelinquency = false;

				if (unpaidAmtandPer.trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {

					if (totalAccountUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0 && totalAccountUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0) {

						addDelinquency = true;
					}
				} else {

					//if (totalAccountUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0 || totalAccountUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0) {
					if(notNull(billWithUnpaidAmt) && totalAccountUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0){	
						addDelinquency = true;
					}

					if(notNull(billWithUnpaidPer) && totalAccountUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0){
						addDelinquency = true;
					}
					
				}

				// Add bills to Delinquency if conditions are met
				if (addDelinquency) {

					createDelinquencyProcess(delinquencyProcessType, delinquencyProcessBills);

				}
			}
		}
	}

	/**
	 * 
	 * Process all bills of person not linked to active delinquency process
	 * 
	 */
	private void processBandCriteriaForCustomerBills(CmDelinquencyProcessType delinquencyProcessType,
			BigInteger billWithAge, BigDecimal billWithUnpaidAmt,
			BigDecimal billWithUnpaidPer, Lookup unpaidAmtandPer) {

		logger.info("Processing customer bills");
		SQLResultRow result = null;
		String billId = "";
		Date dueDate = null;
		Date billDate = null;

		// Get list of completed bills not fully paid that are not linked to an
		// active Delinquency
		List<SQLResultRow> billList = getCompletedBillsNotLinkToActiveDelinquencyProcess();

		if (!isNull(billList) && billList.size() > 0) {

			BigDecimal billUnpaidPercentage = BigDecimal.ZERO;
			BigDecimal hundread = new BigDecimal(100.00);
			BigDecimal billUnpaidAmount = BigDecimal.ZERO;
			BigDecimal billOriginalAmount = BigDecimal.ZERO;

			List<String> delinquencyProcessBills = new ArrayList<String>();

			Iterator<SQLResultRow> iterator = billList.iterator();

			// Process each bill
			while (iterator.hasNext()) {

				// Set account totals to zero
				BigInteger ageOfBill = BigInteger.ZERO;
				result = iterator.next();

				// Get bill details
				billId = result.getString("BILL_ID");
				dueDate = result.getDate("DUE_DT");
				billDate = result.getDate("BILL_DT");

				logger.info("Bill Id == " + billId);
				logger.info("Due Date == " + dueDate.toString());
				logger.info("Bill Date == " + billDate.toString());
				// Process bill only if it is not marked with a postpone credit
				// review date

				if (!isPostponeCreditReviewDateFound(billId)) {

					ageOfBill = determineAgeOfBill(dueDate, billDate);
					logger.info("Age of Bill == " + ageOfBill.toString());

					CmDetermineOpenBillItemAmounts openBillItemAmt = CmDetermineOpenBillItemAmounts.Factory.newInstance();
					OpenItemBillAmountResults output = openBillItemAmt.getBillAmounts(new Bill_Id(billId).getEntity(), null);

					billUnpaidAmount = output.getUnpaidBillAmount().getAmount();
					billOriginalAmount = output.getOriginalBillAmount().getAmount();

					if (billOriginalAmount.compareTo(BigDecimal.ZERO) > 0) {

						billUnpaidPercentage = billUnpaidAmount
								.divide(billOriginalAmount).multiply(hundread);

						logger.info("billUnpaidPercentage=="
								+ billUnpaidPercentage);
						logger.info("Bill Unpaid Amount == "
								+ billOriginalAmount);
						logger.info("Bill Original Amount == "
								+ billUnpaidAmount);

					}

					if ((billUnpaidAmount.compareTo(BigDecimal.ZERO) > 0) && (ageOfBill.compareTo(billWithAge) >= 0)) {

						boolean addDelinquency = false;

						if (unpaidAmtandPer.trimmedValue().equals(YesNoOptionLookup.constants.YES.trimmedValue())) {

							if (billUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0 && billUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0) {

								addDelinquency = true;
							}
						} else {

							//if (billUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0 || billUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0) {
							if(notNull(billWithUnpaidAmt) && billUnpaidAmount.compareTo(billWithUnpaidAmt) >= 0){
								//bug 9734 - end update	
								addDelinquency = true;
							}

							if(notNull(billWithUnpaidPer) && billUnpaidPercentage.compareTo(billWithUnpaidPer) >= 0){
								addDelinquency = true;
							}						
						}

						if (addDelinquency) {

							createDelinquencyProcess(delinquencyProcessType, delinquencyProcessBills);

						}
					}

				}
			}
		}
	}

	/**
	 * Sets Person_Id
	 * 
	 * @param personId  Person_Id to set
	 */

	@Override
	public void setPersonId(Person_Id personId) {
		this.personId = personId;
	}

	@Override
	public Bool getProcessingCompletedSwitch() {
		return isProcessingCompleteSwitch;
	}

	@Override
	public void setAccountId(Account_Id accountId) {
		this.accountId = accountId;
		// TODO Auto-generated method stub
		
	}

}

