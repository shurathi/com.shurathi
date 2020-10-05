/* 
 **************************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                 
 **************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This class is for RMB Delinquency message customization.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-07-17   KChan      CB-150. Delinquency Framework.
 * 2020-08-20   SAnart     CB-286 Transition to Previous Status Algorithm
 * 2020-08-21	KGhuge	   CB-277 Delinquency - Hold Criteria - Account Balance above Threshold Algorithm
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.messageRepository;

import java.math.BigInteger;

import com.splwg.base.domain.common.message.MessageCategory_Id;
import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.base.domain.common.message.ServerMessageFactory;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.shared.common.ServerMessage;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration_Id;


/**
 * This class retrieves the Messages from the Message Repository.
 */
public class MessageRepository extends Messages {

    /**
     * MessageRepository Instance
     */
    private static MessageRepository instance;
	
	// CB-150 Delinquency Framework - Start add
	   /**
		 * Check required parameter is missing depending on input parameter
		 *
		 * @param parameterName
		 *            - Parameter Name
		 * @return - ServerMessage
		 */
		public static ServerMessage algoParamMissing(String parameterName) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterName);
			return repo.getMessage(OMR_ALGO_PARM_MISSING, messageParameters);
		}
			/**
		 * Validate Characteristic Type Code
		 *
		 * @param parameterName
		 *            - Parameter Name
		 * @return - ServerMessage
		 */
		public static ServerMessage invalidCharTypeCd(String parameterName) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterName);
			return repo.getMessage(OMR_INVALID_CHAR_TYPE_CD, messageParameters);
		}
			/**
		 * Invalid Unpaid Amount and Percentage Algorithm Parameter
		 *
		 * @param parameterName
		 *            - Parameter Name
		 * @param parameterValue
		 *            - Parameter Value
		 * @return - ServerMessage
		 */
		public static ServerMessage invalidUnpaidAmtandPer(String parameterName,
				String parameterValue) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterName);
			messageParameters.addRawString(parameterValue);
			return repo.getMessage(OMR_VAL_UNP_AMT_AND_PER_PARM, messageParameters);
		}
	/**
		 * Invalid Process Bill with an Unpaid Percentage Algo Parameter
		 *
		 * @param parameterName
		 *            - Parameter Name
		 * @return - ServerMessage
		 */
		public static ServerMessage invalidProcessBillWithUnpaidPer(
				String parameterName) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterName);
			return repo
					.getMessage(OMR_VAL_PRO_BILL_WITH_UNP_PER, messageParameters);
		}
	/**
		 * Check For valid CustomerConatct and OverdueProcess Char
		 *
		 * @param parameterName
		 *            - Parameter Name
		 * @param parameterValue
		 *            - Parameter Value
		 * @return - ServerMessage
		 */
		public static ServerMessage checkForValidCustomercontactandOverdueProcessChar(
				String parameterName, String parameterValue) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterName);
			messageParameters.addRawString(parameterValue);
			return repo
					.getMessage(OMR_VAL_PRO_BILL_WITH_UNP_PER, messageParameters);
		}
			/**
		 * Invalid Customer Status
		 *
		 * @param parameterName
		 *            - Parameter Name
		 * @param parameterValue
		 *            - Parameter Value
		 * @return - ServerMessage
		 */
		public static ServerMessage invalidCustomerStatus(String parameterName,
				String parameterValue) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterName);
			messageParameters.addRawString(parameterValue);
			return repo
					.getMessage(OMR_VAL_PRO_BILL_WITH_UNP_PER, messageParameters);
		}
			/**
		 * Validate Multiple Customer Status for Object Level ="A"
		 *
		 * @param customerStatus
		 *            - customerStatus
		 * @return - ServerMessage
		 */
		public static ServerMessage validateMultiCustStatus(String customerStatus) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(customerStatus);
			return repo.getMessage(OMR_CUST_STATUS_INVALID, messageParameters);
		}
			public static ServerMessage optionValueIsInvalidCustomerClass(
				FeatureConfiguration_Id featConfigId, Lookup optionType,
				String value) {

			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addEntityIdValues(featConfigId);
			messageParameters.addLookup(optionType);
			messageParameters.addRawString(value);

			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(OPTION_VALUE_IS_INVALID_CUSTOMER_CLASS,
					messageParameters);
		}
		//  10594 - Start Add
		public static ServerMessage invalidSystematicStatmentDay(int lowerLimit,
				int upperLimit) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(String.valueOf(lowerLimit));
			messageParameters.addRawString(String.valueOf(upperLimit));
			return repo.getMessage(INVALID_SYS_STMT_DAY, messageParameters);
		}
		//  10594 - End Add
		
		public static ServerMessage missingFeatureConfig(String parameterOne) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterOne);
			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(MISSING_FEATURE_CONFIG, messageParameters);
		}
		public static ServerMessage missingFeatureConfigOptType(
				String parameterOne, String parameterTwo) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterOne);
			messageParameters.addRawString(parameterTwo);
			MessageRepository repo = MessageRepository.getInstance();
			return repo
					.getMessage(MISSING_FEATURE_CONFIG_OPT_TY, messageParameters);
		}
		public static ServerMessage multipleFeatureConfigOptType(
				String parameterOne, String parameterTwo) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterOne);
			messageParameters.addRawString(parameterTwo);
			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(MULTIPLE_FEATURE_CONFIG_OPT_TY,
					messageParameters);
		}
		
		public static ServerMessage unableToDetermineTerminationDateRule(
				String parameterOne) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterOne);
			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(UNABLE_TO_DET_TERM_DT_RULE, messageParameters);
		}
		
		public static ServerMessage missingEOGCharType(String parameterOne) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterOne);
			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(MISSING_EOG_CHAR_TYPE, messageParameters);
		}
		/**
		 * Error if the Characteristic Value is invalid for a Characteristic Type.
		 *
		 * @param charTypeId
		 * @param charValue
		 * @return ServerMessage
		 */
		public static ServerMessage charValueIsInvalidForCharType(
				CharacteristicType_Id charTypeId, String charValue) {

			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addEntityIdValues(charTypeId);
			messageParameters.addRawString(charValue);

			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(CHAR_VALUE_IS_INVALID_FOR_CHAR_TYPE,
					messageParameters);
		}
		/**
		 * Error if the Characteristic Type is invalid for an entity.
		 *
		 * @param charTypeId
		 * @param entityName
		 * @return ServerMessage
		 */
		public static ServerMessage charTypeIsInvalidForEntity(
				CharacteristicType_Id charTypeId, String entityName) {

			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addEntityIdValues(charTypeId);
			messageParameters.addRawString(entityName);

			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(CHAR_TYPE_IS_INVALID_FOR_ENTITY,
					messageParameters);
		}
		
		public static ServerMessage atleastOneParameterShouldBeProvided(
				String parameter, String parameterOne) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameter);
			messageParameters.addRawString(parameterOne);
			return repo.getMessage(ATLEAST_ONE_PARAMETER_SHOULD_BE_PROVIDED,
					messageParameters);
		}
		public static ServerMessage invalidBusinessObjectStatus(String BOStatus,
				BusinessObject_Id BusinessObject) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(BOStatus);
			messageParameters.addObject(BusinessObject);
			return repo.getMessage(INVALID_STATUS_FOR_POLICY_BUSINESS_OBJECT,
					messageParameters);
		}
		/**
		 * Check parameter if within valid range
		 *
		 * @param parameterName
		 *            - Algorithm Id
		 * @param parameterName
		 *            - Parameter Name
		 * @param parameterValue
		 *            - Tolerance Amount
		 * @return - ServerMessage
		 */
		public static ServerMessage invalidUnpaidAmtAndPer(String algorithmId,
				String parameterName, String toleranceAmount) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(algorithmId);
			messageParameters.addRawString(parameterName);
			messageParameters.addRawString(toleranceAmount);
			return repo
					.getMessage(CAN_CRIT_INV_UNPAID_AMT_PERCT, messageParameters);
		}
		
		public static ServerMessage calcUnpaidAndOriginalAmtAlgoIsRequired(
				String CalcUnpaidAlgorithmDescription) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(CalcUnpaidAlgorithmDescription);
			return repo.getMessage(
					CALCULATE_UNPAID_ORIGINAL_AMT_ALGORITHM_IS_REQUIRED,
					messageParms);
		}
		
		public static ServerMessage cancelCriteriaAlgoIsRequired(
				String CancelCriteriaAlgorithmDescription) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(CancelCriteriaAlgorithmDescription);
			return repo.getMessage(CANCEL_CRITERIA_ALGORITHM_IS_REQUIRED,
					messageParms);
		}
		public static ServerMessage zeroOrMultipleRecordsInRelatedObjTable(
				String delinquencyLevel, String maintenanceObject) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(delinquencyLevel);
			messageParms.addRawString(maintenanceObject);
			return repo.getMessage(ZERO_OR_MULTIPLE_RECORDS_IN_REL_OBJ,
					messageParms);
		}
		
		public static ServerMessage maintenanceObjectMismatch(String maintObj,
				String collectingOnObj) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(maintObj);
			messageParms.addRawString(collectingOnObj);
			return repo.getMessage(MAINT_OBJ_MISMATCH, messageParms);
		}
		
		
		public static ServerMessage invalidCustomerContact(String algorithmName,
				String parameter, String customerContact, String CustomerClass) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(algorithmName);
			messageParms.addRawString(parameter);
			messageParms.addRawString(customerContact);
			messageParms.addRawString(CustomerClass);
			return repo.getMessage(INVALID_CC, messageParms);
		}

		public static ServerMessage invalidMessageCategoryMessageNumberCombination(
				String algorithmName, String parameter, String messsageCategory,
				String messageNumber) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(algorithmName);
			messageParms.addRawString(parameter);
			messageParms.addRawString(messsageCategory);
			messageParms.addRawString(messageNumber);
			return repo.getMessage(INVALID_MESSAGE_NUMBER_CATEGORY_COMBINATION,
					messageParms);
		}
		
			public static ServerMessage invalidNotifyGroupCustomerOrAccountsValue(
				String C, String A) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(C);
			messageParms.addRawString(A);
			return repo.getMessage(INVALID_C_A_VALUE, messageParms);
		}
		
		
		public static ServerMessage accoutAndPersonNotFound() {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			return repo.getMessage(ACCOUNT_AND_PERSON_NOT_FOUND, messageParms);
		}
		
		public static ServerMessage multipleDeliquencyProcessTypeAlgorithmsFound(
				String deliquencyProcessType) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(deliquencyProcessType);
			return repo.getMessage(MULTIPLE_DELIQUENCY_PROCESS_TYPE_ALG_FOUND,
					messageParameters);
		}
		
		public static ServerMessage invalidStatusTransitionCondition(
				String statusTransitionCondition) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(statusTransitionCondition);
			return repo.getMessage(INVALID_STATUS_TRANSITION_CONDITION,
					messageParms);
		}

		public static ServerMessage cannotDetermineStateOfIssueForPolicyOfPerson(
				Person_Id mainPerson, String fetchLanguageDescription) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addEntityIdValues(mainPerson);
			messageParameters.addRawString(fetchLanguageDescription);
			return repo.getMessage(STATE_OF_ISSUE_NOT_FOUND_ON_POLICY,
					messageParameters);
		}

		
				public static ServerMessage delinquencyNotLinkedToCustomer(CmDelinquencyProcess_Id delProcId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addEntityIdValues(delProcId);
					return repo.getMessage(DELINQUENCY_NOT_LINKED_TO_CUSTOMER,messageParameters);
				}
				
		public static ServerMessage invalidAlgorithmParameterValue(
				Algorithm algorithm, String parameterName, String parameterValue) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addEntityIdValues(algorithm.getId());
			messageParameters.addRawString(parameterName);
			messageParameters.addRawString(parameterValue);
			return repo.getMessage(INVALID_PERCENTAGE_VALUE, messageParameters);
		}
		
		public static ServerMessage delinquencyProcessRequired() {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			return repo.getMessage(DELINQUENCY_PROCESS_REQUIRED, messageParameters);
		}
		
		public static ServerMessage errorDuringComputation() {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			return repo.getMessage(ERROR_DURING_COMPUTATION, messageParameters);
		}
		
		public static ServerMessage noPersonPhoneFound(String customerNumber) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(customerNumber);
			return repo.getMessage(NO_PERSON_PHONE_FOUND, messageParameters);
		}
		public static ServerMessage invalidAlgorithmParmValues(
				String parameterName, String validValues) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterName);
			messageParameters.addRawString(validValues);
			return repo.getMessage(ALGO_PARM_VALID_VALUES, messageParameters);
		}
		
		public static ServerMessage delProcIdRequiredForPlugInSpot() {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			return repo.getMessage(REQ_VALUES_PLUG_IN_SPOT, messageParameters);
		}

		public static ServerMessage delProcRelObjRequiredForPlugInSpot() {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			return repo.getMessage(REL_OBJ_REQ_VALUES_PLUG_IN_SPOT,
					messageParameters);
		}
		
		public static ServerMessage invalidDelParms(String parameter) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameter);
			return repo.getMessage(DEL_PRO_TYP_PARM, messageParameters);
		}
		
		public static ServerMessage invalidDelLvlParms(String procTyplvl,
				String parameter) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(procTyplvl);
			messageParameters.addRawString(parameter);
			return repo.getMessage(DEL_LVL_PARM, messageParameters);
		}
		public static ServerMessage billIdRequired() {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			return repo.getMessage(BILL_ID_REQUIRED_FOR_ALGORITHM,
					messageParameters);
		}
				public static ServerMessage bilNotBelongsToDelProcPer(Bill_Id billId, Person_Id perId,
						CmDelinquencyProcess_Id delProcId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addEntityIdValues(billId);
					messageParameters.addEntityIdValues(perId);
					messageParameters.addEntityIdValues(delProcId);
					return repo.getMessage(BILL_NOT_BELONG_TO_DEL_PROC_FOR_PER, messageParameters);
				}
			public static ServerMessage bilNotBelongsToDelProcAcct(Bill_Id billId, Account_Id acctId,
						CmDelinquencyProcess_Id delProcId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addEntityIdValues(billId);
					messageParameters.addEntityIdValues(acctId);
					messageParameters.addEntityIdValues(delProcId);
					return repo.getMessage(BILL_NOT_BELONG_TO_DEL_PROC_FOR_ACCT, messageParameters);
				}

		
				public static ServerMessage deliquencyProcessCMDLReqd(CmDelinquencyProcess_Id delinquencyProcessId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addEntityIdValues(delinquencyProcessId);
					return repo.getMessage(DLC_CMDL_REQD, messageParameters);
				}
				
				public static ServerMessage adminContractTypesFeatureConfigMissing() {
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(ADMIN_CONTRACT_TYPES_FEATURE_CONFIG_MISSING);
				}
				public static ServerMessage adminContractTypeOptionTypeMissing() {
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(ADMIN_CONTRACT_TYPE_OPTION_TYPE_MISSING);
				}
				public static ServerMessage transitionConditionToEvaluateDebtNotificationSentStatusMissing() {
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(TRAN_COND_EVALUATE_DEBT_NOTIFICATION_SENT_STATUS_MISSING);
				}

		
				public static ServerMessage noPersonFoundForDelinquency(
						String delinquencyProcess) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(delinquencyProcess);
					return repo.getMessage(NO_PERSON_FOUND_FOR_DEL, messageParameters);
				}

			public static ServerMessage billIdMessageDel(String billId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(billId);
					return repo.getMessage(DEL_BILL_ID_MSG, messageParameters);
				}
			public static ServerMessage outMsgCreatedDelinquency(
						String outboundMessageId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(outboundMessageId);
					return repo.getMessage(DQCDM_OUTMSG_CREATED, messageParameters);
				}
			public static ServerMessage unableToCreateOutboundMessageCDM() {
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(DQCDM_OUTMSG_NOT_CREATED);
				}
				public static ServerMessage customerIdentifierTypeNotFound(
						String sourceSystem) {
					MessageParameters msgParms = new MessageParameters();
					msgParms.addRawString(sourceSystem);
					return getInstance().getMessage(CUST_ID_TYPE_NOT_FOUND, msgParms);
				}
				public static ServerMessage customerNumberFromSourceSystemNotFound(
						String custNumber, String sourceSystem) {
					MessageParameters msgParms = new MessageParameters();
					msgParms.addRawString(custNumber);
					msgParms.addRawString(sourceSystem);
					return getInstance().getMessage(CUST_NBR_FRM_SRC_SYS_NOT_FOUND,
							msgParms);
				}
					public static ServerMessage missingPolicyCollectionLocation(String policyId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(policyId);
					return repo.getMessage(MISSING_POLICY_COLLECTION_LOCATION,
							messageParameters);
				}
			public static ServerMessage noUnpaidAmountFound(String delinquencyProcess) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(delinquencyProcess);
					return repo.getMessage(NO_UNPAID_AMOUNT_FOUND, messageParameters);
				}
			public static ServerMessage noUnpaidAmountCalcAlgFound(String processType) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(processType);
					return repo.getMessage(NO_UNPAID_AMOUNT_CALC_ALG_FOUND,
							messageParameters);
				}
			public static ServerMessage nobillsFoundFromDelinquency(
						String delinquencyProcess) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(delinquencyProcess);
					return repo.getMessage(NO_BILLS_FOUND_DQ, messageParameters);
				}
				public static ServerMessage nopolicyFoundFromDelinquency(
						String delinquencyProcess) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(delinquencyProcess);
					return repo.getMessage(NO_POLICY_FOUND_DQ, messageParameters);
				}
				public static ServerMessage missingOfficeOfRegistration(String policyId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(policyId);
					return repo.getMessage(MISSING_OFFICE_OF_REGISTRATION,
							messageParameters);
				}
				public static ServerMessage missingFrozenMarketSegment(String policyId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(policyId);
					return repo
							.getMessage(MISSING_FROZEN_MARKET_SEGMENT, messageParameters);
				}
				public static ServerMessage exception() {
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(EXCEPTION);
				}
				public static ServerMessage invalidOutboundMessageProfile(
						String outboundMessageType, String externalSystem) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(outboundMessageType);
					messageParameters.addRawString(externalSystem);
					return repo.getMessage(INVALID_OUTBOUND_MESSAGE_PROFILE,
							messageParameters);
				}
				public static ServerMessage invalidUnpaidAmountCalcValue() {
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(INVALID_UNPAID_AMT_CALC_VAL);
				}
				public static ServerMessage arithmeticExpressionError(String exception) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(exception);
					return repo.getMessage(ARITHMETIC_EXPERSSION_HAS_INVALID_OUTPUT,
							messageParameters);
				}
				public static ServerMessage addDelProcLogForCharVal(String charVal) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParms = new MessageParameters();
					messageParms.addRawString(charVal);
					return repo.getMessage(DEL_PROC_LOG_ON_HOLD, messageParms);
				}
				public static ServerMessage failedToReadPaymentFromClob() {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					return repo.getMessage(PAYMENTS_FROM_REQUEST_CLOB_READ_ERROR,
							messageParameters);
				}
				public static ServerMessage invalidEntity(String entityValue, String entity) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(entityValue);
					messageParameters.addRawString(entity);
					return repo.getMessage(INVALID_ENTITY, messageParameters);
				}
				public static ServerMessage cannotDetermineStateOfIssue(
						String parameterOne, String parameterTwo) {
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(parameterOne);
					messageParameters.addRawString(parameterTwo);
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(CANNOT_DET_STATE_OF_ISSUE, messageParameters);
				}
					public static ServerMessage multipleFeatureConfig(String parameterOne) {
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(parameterOne);
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(MULTIPLE_FEATURE_CONFIG, messageParameters);
				}
				public static ServerMessage businessObjectMismatch(
						String businessObjOnTriggerEvt, String businessObjOnDelinProc) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParms = new MessageParameters();
					messageParms.addRawString(businessObjOnTriggerEvt);
					messageParms.addRawString(businessObjOnDelinProc);
					return repo.getMessage(BUSINESS_OBJECT_MISMATCH, messageParms);
				}
				public static ServerMessage inValidRoleForTodoType(String todoRole,
						String toDoType) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParms = new MessageParameters();
					messageParms.addRawString(todoRole);
					messageParms.addRawString(toDoType);
					return repo.getMessage(INVALID_ROLE_FOR_TODO_TYPE, messageParms);
				}
				public static ServerMessage delLevelRequired() {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					return repo.getMessage(CUST_OR_ACCT_LEVEL_DEL_REQUIRED,
							messageParameters);
				}
				public static ServerMessage toDoMessage(String customerName, String status) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(customerName);
					messageParameters.addRawString(status);
					return repo.getMessage(TO_DO_MESSAGE, messageParameters);
				}
				public static ServerMessage noStateOfIssueListFound(String parameterOne) {
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(parameterOne);
					MessageRepository repo = MessageRepository.getInstance();
					return repo
							.getMessage(STATE_OF_ISSUE_LIST_NOT_FOUND, messageParameters);
				}
				public static ServerMessage charTypeNumericValue(String charType,
						String division) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(charType);
					messageParameters.addRawString(division);
					return repo.getMessage(CHAR_TYPE_NUMERIC_VALUE, messageParameters);
				}
				public static ServerMessage invalidCharTypeToEntity(String parameterName,
						String parameterEntity) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(parameterName);
					messageParameters.addRawString(parameterEntity);
					return repo.getMessage(INVALID_ENTITY_TO_CHAR_TYPE, messageParameters);
				}
				public static ServerMessage billCycleWindowStartDtNotAvlbl(String text,
						String period) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(text);
					messageParameters.addRawString(period);
					return repo.getMessage(BILL_CYC_NT_AVL, messageParameters);
				}
				public static ServerMessage accountInfoInconsistent(Person_Id personId,
						String text) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addEntityIdValues(personId);
					messageParameters.addRawString(text);
					return repo.getMessage(CUST_ACCT_INCONSISTANCY, messageParameters);
				}
				/**
				 * Check For Invalid Value of customerConatctType
				 *
				 * @param parameterName
				 *            - Parameter Name
				 * @param parameterValue
				 *            - Parameter Value
				 * @return - ServerMessage
				 */
				public static ServerMessage msgNumberandMsgCategoryCombinationInvalid(
						String parameterName, String parameterValue) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(parameterName);
					messageParameters.addRawString(parameterValue);
					return repo.getMessage(MSG_NBR_AND_MSG_ACTEGORY_COMBINATION_INVALID,
							messageParameters);
				}
			public static ServerMessage noPersonFoundForDelinquencyProcess(
						String delinquencyProcess) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParms = new MessageParameters();
					messageParms.addRawString(delinquencyProcess);
					return repo.getMessage(NO_PERSON_FOUND_FOR_DELINQUENCY_PROCESS,
							messageParms);
				}
			public static ServerMessage delWriteOffInitiated() {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					return repo.getMessage(DEL_LOG_WRITE_OFF_INITIATED, messageParameters);
				}

				public static ServerMessage delWriteOffCreated() {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					return repo.getMessage(WRITE_OFF_REQUEST_CREATED, messageParameters);
				}
			public static ServerMessage terminationEffectiveDateRuleNotRetrieved() {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					return repo.getMessage(TERM_EFFECTIVE_DATE_RULE_NOT_RETRIEVED,
							messageParameters);
				}

				public static ServerMessage noUnpaidBillRetrieved(String parameterOne) {
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(parameterOne);
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(NO_UNPAID_BILL_RETRIEVED, messageParameters);
				}
			public static ServerMessage invalidDelProcCharType(String parameterOne) {
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(parameterOne);
					MessageRepository repo = MessageRepository.getInstance();
					return repo.getMessage(INVALID_CHAR_TYPE, messageParameters);
				}

				public static ServerMessage unableToRetrieveTriggerDate() {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					return repo.getMessage(TRIGGER_DATE_NOT_RETRIEVED, messageParameters);
				}
			public static ServerMessage unableToDetermineTerminationDateRule() {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					return repo.getMessage(UNABLE_TO_DETERMINE_TERMINATION_RULE,
							messageParameters);
				}
			public static ServerMessage chargeLineCodeCharTypeMissing(String algoCode) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(algoCode);
					return repo
							.getMessage(CHARGE_LINE_CHAR_TYPE_MISSING, messageParameters);
				}

				public static ServerMessage fundingAgmtCodeCharTypeMissing(String algoCode) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(algoCode);
					return repo.getMessage(FUNDING_AGMT_CHAR_TYPE_MISSING,
							messageParameters);
				}
			public static ServerMessage unpaidAmountAndPercentageMissing() {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					return repo.getMessage(UNPAID_AMOUNT_AND_PERCENTAGE_MISSING,
							messageParameters);
				}
			public static ServerMessage brokerIdNotFound(String strCustomerNbr) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(strCustomerNbr);
					return repo.getMessage(BROKER_ID_NOT_FOUND, messageParameters);
				}

				public static ServerMessage outboundMessageForSystematicStmtProcess(
						String strBrokerId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(strBrokerId);
					return repo.getMessage(
							OUTBOUND_MESSAGE_WAS_CREATED_FOR_SYSTEMATIC_STMT_PROCESS,
							messageParameters);
				}

				public static ServerMessage customerContactForSystematicStmtProcess(
						String strBillGroupId) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(strBillGroupId);
					return repo.getMessage(
							CUST_CONTACT_CREATED_FOR_SYSTEMATIC_STMT_PROCESS,
							messageParameters);
				}
			public static ServerMessage cannotDetermineterminationDateOnDelinProc(
						CharacteristicType_Id terminationDate) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addEntityIdValues(terminationDate);
					return repo.getMessage(CANNOT_DETERMINE_TERMINATION_DATE_ON_DELIN_PROC,
							messageParameters);
				}

				public static ServerMessage logMessage(String status, String date,
						int messageNumber) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParms = new MessageParameters();
					messageParms.addRawString(status);
					messageParms.addRawString(date);
					return repo.getMessage(messageNumber, messageParms);
				}
			public static ServerMessage logCreatedMessage(String status,
						int messageNumber) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParms = new MessageParameters();
					messageParms.addRawString(status);
					return repo.getMessage(messageNumber, messageParms);
				}
			public static ServerMessage delinquencyMonitorRuleAlgoIsRequired(
						String monitorRuleAlgorithm) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParms = new MessageParameters();
					messageParms.addRawString(monitorRuleAlgorithm);
					return repo
							.getMessage(MONITOR_RULE_ALGORITHM_IS_REQUIRED, messageParms);
				}
			public static ServerMessage multipleDeliquencyMonitorRuleAlgorithmsFound(
						String deliquencyControl) {
					MessageRepository repo = MessageRepository.getInstance();
					MessageParameters messageParameters = new MessageParameters();
					messageParameters.addRawString(deliquencyControl);
					return repo.getMessage(MULTIPLE_DELIQUENCY_MONITOR_RULE_ALG_FOUND,
							messageParameters);
				}
			public static ServerMessage delinquencyAlgorithmsNotConfigured(
					String collectionClass) {
				MessageRepository repo = MessageRepository.getInstance();
				MessageParameters messageParameters = new MessageParameters();
				messageParameters.addRawString(collectionClass);
				return repo.getMessage(DELINQUENCY_CONTROL_ALGORITHMS_NOT_CONFIGURED,
						messageParameters);
			}
			public static ServerMessage invalidCollectionClass(String personId) 
			{
				MessageParameters messageParameters = new MessageParameters();
				MessageRepository repo = MessageRepository.getInstance();
				messageParameters.addRawString(personId);
				return repo.getMessage(INVALID_COLLECTION_CLASS, messageParameters);
			}

			public static ServerMessage invalidGroupPerson(String delinquencyProcessId) 
			{
				MessageParameters messageParameters = new MessageParameters();
				MessageRepository repo = MessageRepository.getInstance();
				messageParameters.addRawString(delinquencyProcessId);
				return repo.getMessage(INVALID_GROUP_PERSON, messageParameters);
			}
		public static ServerMessage createMessageForAlert(String processCount, int messageNumber) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParms = new MessageParameters();
			messageParms.addRawString(processCount);
			return repo.getMessage(messageNumber, messageParms);
		}
		
		public static ServerMessage divisionMissing() {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			return repo.getMessage(DIVISION_MISSING, messageParameters);
		}
		
		//Start add CB-281
		/**
		 * Collection Class Account is not valid for the entity.
		 * @param AccountId
		 * @return - ServerMessage
		 */
		public static ServerMessage invalidCollectionClassForAccount(String accId) {
			MessageParameters messageParameters = new MessageParameters();
			MessageRepository repo = MessageRepository.getInstance();
			messageParameters.addRawString(accId);
			return repo.getMessage(INVALID_COLLECTION_CLASS_ACCOUNT, messageParameters);
		}
		//End CB-281
		//Start add CB-281
		/**
		 * Delinquency Process Id is not valid for the entity.
		 * @param delinquencyProcessId
		 * @return - ServerMessage
		 */
		public static ServerMessage invalidAccountId(String delinquencyProcessId) 
			{
				MessageParameters messageParameters = new MessageParameters();
				MessageRepository repo = MessageRepository.getInstance();
				messageParameters.addRawString(delinquencyProcessId);
				return repo.getMessage(ACCOUNT_ID_INVALID, messageParameters);
			}
		//End CB-281
		
		/**
		 * Validate value pass in input parameter
		 *
		 * @param parameterName
		 *            - Parameter Name
		 * @return - ServerMessage
		 */
		public static ServerMessage algoParamInvalid(String parameterName) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(parameterName);
			return repo.getMessage(OMR_ALGO_PARM_INVALID, messageParameters);
		}
		
		public static ServerMessage optionValueMissingForFeatureConfiguration(
				FeatureConfiguration_Id featConfigId, Lookup optionType) {

			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addEntityIdValues(featConfigId);
			messageParameters.addLookup(optionType);

			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(OPTION_VALUE_MISSING_FOR_FEATURE_CONFIGURATION,
					messageParameters);
		}
		
		/**
		 * Characteristic Type is not valid for the entity.
		 *
		 * @param charType
		 * @param charEntity
		 * @return - ServerMessage
		 */
		public static ServerMessage invalidCharTypeForEntity(String charType,
				String charEntity) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(charType);
			messageParameters.addRawString(charEntity);
			return repo.getMessage(INVALID_CHAR_TYPE_FOR_ENTITY, messageParameters);
		}
		
		/**
		 * Error if char value is invalid for char type.
		 *
		 * @param batchParameterName
		 * @param charValue
		 * @param charType
		 * @return ServerMessage
		 */
		public static ServerMessage charValueInvalidForCharType(
				String batchParameterName, String charValue, String charType) {

			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(batchParameterName);
			messageParameters.addRawString(charValue);
			messageParameters.addRawString(charType);

			MessageRepository repo = MessageRepository.getInstance();
			return repo.getMessage(CHAR_VALUE_INVALID_FOR_CHAR_TYPE,
					messageParameters);
		}
		
		public static ServerMessage invalidCharValue(String charValue,
				String charType) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(charValue);
			messageParameters.addRawString(charType);
			return repo.getMessage(CHAR_VALUE_INVALID, messageParameters);
		}
		
	// CB-150 Delinquency Framework - Stop add
	//CB-286 Transition to Previous Status Algorithm- Start add
		public static ServerMessage unableToDetPrevStatus(String status,String dpId ) {
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(status);
			messageParameters.addRawString(dpId);
			return repo.getMessage(UNABLE_RETURN_TO_PREV_STATUS, messageParameters);
		}
	//CB-286 Transition to Previous Status Algorithm- Stop add
	
	//Start Add CB-277
		public static ServerMessage overDueAccountBalance(String desc){
			MessageRepository repo = MessageRepository.getInstance();
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(desc);
			return repo.getMessage(OVER_DUE_AMOUNT, messageParameters);			
		}
		//End Add CB-277

    /**
     * Returns the MessageRepository instance.
     * 
     * @return MessageRepository
     */
    static MessageRepository getInstance() {
        if (instance == null) {
            instance = new MessageRepository();
        }
        return instance;
    }	
	
    //Retrieve Server Message, here you can pass N number of arguments to Message 
  	/**
  	 * Retrieve Server Message
  		   @param msgNbr error Message Number
  		   @param args n number of arguments
  		   @return ServerMessage
  	 */
  	public static ServerMessage getServerMessage(int msgNbr,
  			String... args) {
  		MessageCategory_Id messageCategoryId = new MessageCategory_Id(
  				BigInteger.valueOf(MESSAGE_CATEGORY));
  		MessageParameters messageParams = new MessageParameters();
  		if (args.length > 0) {
  			for (String arg : args) {
  				messageParams.addRawString(arg);
  			}
  		}
  		ServerMessageFactory serverMessageFactory = ServerMessageFactory.Factory
  				.newInstance();
  		return serverMessageFactory.createMessage(
  				messageCategoryId, msgNbr, messageParams);
  	}
	
}

	
