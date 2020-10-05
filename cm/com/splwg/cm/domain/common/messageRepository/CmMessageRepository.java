/* 
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
 * This class is for RMB message customization.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-04-13   JFerna     Initial.
 * 2020-04-16	DIvakar    Added ServerMessage method
 * 2020-04-16	DDejes	   CB-37
 * 2020-04-17	KGhuge	   CB-24
 * 2020-04-21   VLaksh     CB-10
 * 2020-04-23	DIvakar	   CB-9
 * 2020-05-05   DIvakar	   CB-9 Removed Method
 * 2020-05-19   JFerna     CB-61
 * 2020-05-27   SPatil     CB-55
 * 2020-06-08	KGhuge	   CB-87
 * 2020-06-11   SPatil     CB-127
 * 2020-06-29   KChan      CB-159
 * 2020-07-23	KGhuge	   CB-231 	
 * 2020-07-28	ShrutikaA  CB-224
 * 2020-07-30	Ishita     CB-145
 * 2020-07-31	KGhuge	   CB-54. Capture Statement Construct during Customer Interface
 * 2020-08-11	ShrutikaA  CB-227
 * 2020-08-12	Shreyas	   CB-302
 * 2020-08-14	Shreyas	   CB-270
 * 2020-08-21   Ishita     CB-274
 * 2020-08-28	KGhuge	   CB-283
 * 2020-09-01	ShrutikaA  CB-264
 * 2020-09-24	ShrutikaA  CB-444
 **************************************************************************
 */
package com.splwg.cm.domain.common.messageRepository;

import java.math.BigInteger;

import com.splwg.base.domain.common.message.MessageCategory_Id;
import com.splwg.base.domain.common.message.MessageParameters;
import com.splwg.base.domain.common.message.ServerMessageFactory;
import com.splwg.shared.common.ServerMessage;

/**
 * This class retrieves the Messages from the Message Repository.
 */
public class CmMessageRepository extends CmMessages {

    /**
     * MessageRepository Instance
     */
    private static CmMessageRepository instance;
	
	 //Start Add - CB-37
    // %1 is not a valid lookup value of %2.
    public static ServerMessage invalidLookupValue(String lookupValue, String lookupName) {
        MessageParameters messageParms = new MessageParameters();
        messageParms.addRawString(lookupValue);
        messageParms.addRawString(lookupName);
        return getInstance().getMessage(CmMessages.INVALID_LOOKUP_VALUE, messageParms);
    }
    // Batch %1: Customer Interface Staging %2 encountered an error %3%4%5
    public static ServerMessage batchProcError(String batchCd, String custStgId, String errorMessage) {
        MessageParameters messageParms = new MessageParameters();
        messageParms.addRawString(batchCd);
        messageParms.addRawString(custStgId);
        messageParms.addRawString(errorMessage);
        return getInstance().getMessage(CmMessages.BATCH_PROC_ERROR, messageParms);
    }
    //End Add - CB-37
	
//Start Add - CB-10
	public static ServerMessage customerEntityCreated(String personId) {
		MessageParameters msgParam = new MessageParameters();
		msgParam.addRawString(personId);
		return getInstance().getMessage(CmMessages.PERSON_ENTITY_CREATED,msgParam);
	}
	
	public static ServerMessage accountEntityCreated(String accountId) {
		MessageParameters msgParam = new MessageParameters();
		msgParam.addRawString(accountId);
		return getInstance().getMessage(CmMessages.ACCOUNT_ENTITY_CREATED,msgParam);
	}
	
	public static ServerMessage invalidNextBOStatus(String boStatus,String boName) {
		MessageParameters msgParam = new MessageParameters();
		msgParam.addRawString(boStatus);
		msgParam.addRawString(boName);
		return getInstance().getMessage(CmMessages.INVALID_BO_NEXT_STATUS,msgParam);
	}
	public static ServerMessage custIntfExtLukupMissingForDiv(String division) {
		MessageParameters msgParam = new MessageParameters();
		msgParam.addRawString(division);
		return getInstance().getMessage(CmMessages.ACCOUNT_ENTITY_CREATED,msgParam);
	}
	
	public static ServerMessage custIntfExtLukupMissingForCustClass(String CustomerClass) {
		MessageParameters msgParam = new MessageParameters();
		msgParam.addRawString(CustomerClass);
		return getInstance().getMessage(CmMessages.ACCOUNT_ENTITY_CREATED,msgParam);
	}
	
	public static ServerMessage mainCustomerNotExistsToAddPersonPersonRelation() {
		MessageParameters msgParam = new MessageParameters();
		return getInstance().getMessage(CmMessages.MAIN_CUST_NOT_EXISTS,msgParam);
	}

	//End Add - CB-10
	
	//Start Add - CB-24
	/*
	*	This Method used to fire an error If The number of records from header (%1) 
	*	does not match to actual number of records (%2) in the xml file
	*
	* 	@Param Number Of Records from Header
	* 	@Param Actual Number Of Records in XML File
	*
	*/
	public static ServerMessage numberOfRecordsDoesNotMatch(String headerRecords,String actualRecords) {
				MessageParameters msgParams = new MessageParameters();
		msgParams.addRawString(headerRecords);
		msgParams.addRawString(actualRecords);
		return getInstance().getMessage(CmMessages.NUMBER_OF_RECORDS_NOT_MATCHED, msgParams);
	}
	//End Add - CB-24
	
	//Start Add - CB-61
	public static ServerMessage accountNotFoundForIdTypeAndValueCombination(String idType,String idValue) {
		MessageParameters msgParam = new MessageParameters();
		msgParam.addRawString(idType);
		msgParam.addRawString(idValue);
		return getInstance().getMessage(CmMessages.ACCOUNT_NOT_EXISTS,msgParam);
	}
	//End Add - CB-61	
	

//Start Add - CB-55

// Start Add - CB-287
		public static ServerMessage entityNotValid(String entityName,String value) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(entityName);
			messageParameters.addRawString(value);
			return getInstance().getMessage(CmMessages.ENTITY_NOT_VALID,messageParameters);
		}
		// End Add - CB-287


	
	
	/**
	 * Throw an error if File Path is invalid
	 *
	 */
	public static ServerMessage invalidPath(String parameterName,String parameterValue) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(parameterName);
		messageParameters.addRawString(parameterValue);
		return getInstance().getMessage(CmMessages.INVALID_FILEPATH, messageParameters);
	}
	
	
	/**
	 * Throw an error if File Path is  invalid Directory
	 *
	 */
	public static ServerMessage invalidDirectory(String parameterName,String parameterValue) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(parameterName);
		messageParameters.addRawString(parameterValue);
		return getInstance().getMessage(CmMessages.INVALID_DIRECTORY,messageParameters);
	}

	/**
	 * Throw an error if FileFormat is not valid
	 *
	 */
	public static ServerMessage invalidFileFormat(String fileName , String extension) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(fileName);
		messageParameters.addRawString(extension);
		return getInstance().getMessage(CmMessages.INVALID_EXTENSION,messageParameters);
	}
	
	/**
	 * Throw an error if Missing Feature Configuration
	 *
	 */
	public static ServerMessage missFeatConfig(String fetchLanguageDescription) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(fetchLanguageDescription);
		return getInstance().getMessage(CmMessages.MISS_FEAT_CONFIG, messageParameters);
		
	}
	
	/**
	 * Throw an error if Missing Feature Configuration Option Type
	 *
	 */
	public static ServerMessage missFeatureOpt(String fetchLanguageDescription,
			String featConfig) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(fetchLanguageDescription);
		messageParameters.addRawString(featConfig);
		
		return getInstance().getMessage(CmMessages.MISS_FEAT_OPT, messageParameters);
	}
	
	/**
	 * Throw an error if File Already Exists
	 *
	 */
	public static ServerMessage fileAlreadyExists(String fileName,
			String fullFilePath) {
		
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(fileName);
		messageParameters.addRawString(fullFilePath);
		return getInstance().getMessage(CmMessages.FILE_ALREADY_EXISTS, messageParameters);
	}
	
	/**
	 * Throw an error if file cannot be open/write/close
	 *
	 */
	 public static ServerMessage commonMessageIoFileError(String action, String filePathfileName, String errorMsg) {
	        MessageParameters parms = new MessageParameters();
	        parms.addRawString(action);
	        parms.addRawString(filePathfileName);
	        parms.addRawString(errorMsg);
	        return getInstance().getMessage(COMMON_MESSAGE_IO_FILE_ERROR, parms);
	    }
	
	   /**
		 * Throw an error if cannot update the the status of Payment Status Flag to Requested for AP Request
		 *
		 */
		public static ServerMessage errorUpdatingStatus(String apRequestId, String exception ) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(apRequestId);
			messageParameters.addRawString(exception);
			return getInstance().getMessage(CmMessages.ERROR_UPDATING_STATUS,messageParameters);
		}
	
	//End Add - CB-55
	
	//Start Add - CB-87
  	/***
  	 * Fires an error if No Active Statement Construct exists for Customer with Id Type %1 and Id Value %2 combination
  	 * @param Customer Identifier Type
  	 * @param Customer Identifier Value
  	 * @return Server Message
  	 * */
  	public static ServerMessage noActiveStatementConstructExists(String idType,String idValue)
  	{
  		MessageParameters msgParams = new MessageParameters();
		msgParams.addRawString(idType);
		msgParams.addRawString(idValue);
		return getInstance().getMessage(CmMessages.NO_ACTIVE_STATEMENT_CONSTRUCT_EXISTS, msgParams);
  	}
  	
  	/**
  	 * This method fire an if error Customer Identifier type is invalid
  	 * @param Customer Identifier Type
  	 * @return Server Message
  	*/
  	public static ServerMessage invalidCustomerIdentifierType(String idType)
  	{
  		MessageParameters msgParams = new MessageParameters();
  		msgParams.addRawString(idType);
  		return getInstance().getMessage(CmMessages.INVALID_CUSTOMER_IDENTIFIER_TYPE,msgParams);
  	}
  	
  	/**
  	 * Fire an error if No open Bills for Statement Construct
  	 * @param Statement Construct Id
  	 * @return Server Message
  	 * */
  	
  	public static ServerMessage noOpenBillsForStatementConstruct(String statementConstructId)
  	{
  		MessageParameters msgParams = new MessageParameters();
  		msgParams.addRawString(statementConstructId);
  		return getInstance().getMessage(CmMessages.NO_OPEN_BILLS_FOR_STATEMENT_CONSTRUCT,msgParams);
  	}
	
	/**
  	 * Returns an ServerMessage If primaryCustomerIdValue is Empty
  	 * @param primaryCustomerIdValue
  	 * @return Server Message
  	 * */
  	public static ServerMessage primaryCustomerIdValueIsEmpty()
  	{
  		ServerMessageFactory serverMessageFactory = ServerMessageFactory.Factory.newInstance();
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString("Primary Customer Id Value");
		MessageCategory_Id messageCategoryId = new MessageCategory_Id(BigInteger.valueOf(11107));
		return serverMessageFactory.createMessage(messageCategoryId, 11509, messageParameters);
  	}
  	//End Add - CB-87
	
	//Start Add - CB-127
	public static ServerMessage invalidAccountRelationshiptype(String relationshipTypeCode) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(relationshipTypeCode);
			return getInstance().getMessage(CmMessages.INVALID_ACCT_REL_TYPE_CD,messageParameters);
		}
	
	//END Add -CB-127
	
	//Start Add - CB-159
	public static ServerMessage missingCustomerClassOnBo(String customerClass,String bo) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(customerClass);
		messageParameters.addRawString(bo);
		return getInstance().getMessage(CmMessages.MISSING_CUSTOMER_CLASS, messageParameters);
	}
	
    public static ServerMessage legacyBillIdNotFound(String legacyBillIdNotFoundMsg) {
        MessageParameters messageParms = new MessageParameters();
        messageParms.addRawString(legacyBillIdNotFoundMsg);
       return getInstance().getMessage(LEGACY_BILL_ID_MISSING, messageParms);
    }

	public static ServerMessage missingCharEntity(String charTypeCd,String entity)
	{
		MessageParameters messageParms = new MessageParameters();
		messageParms.addRawString(charTypeCd);
		messageParms.addRawString(entity);
		return getInstance().getMessage(MISSING_CHAR_ENTITY,messageParms);
	}
	
    public static ServerMessage contractNotIdentifiedForAcctAndPritmComb(String requestId) {
        MessageParameters messageParms = new MessageParameters();
        messageParms.addRawString(requestId);
       return getInstance().getMessage(ACCT_PRICEITEM_COMB_INCORRECT, messageParms);
    }
    
    public static ServerMessage accountNotFound(String accountId) {
        MessageParameters messageParms = new MessageParameters();
        messageParms.addRawString(accountId);
       return getInstance().getMessage(NO_ACCOUNT_EXISTS, messageParms);
    }
	//End Add - CB-159

	//Start Add - CB-231
	public static ServerMessage noSuspenseContractRetrive(String externalSourceId) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(externalSourceId);
		return getInstance().getMessage(CmMessages.NO_SUSPENSE_CONTRACT_FOUND,messageParameters);
	}
	//End Add - CB-231
	//Start Add - CB-224
	
	public static ServerMessage noSegmentValueFound(String string1,String string2,String string3) {
        MessageParameters messageParms = new MessageParameters();
        messageParms.addRawString(string1);
        messageParms.addRawString(string2);
        messageParms.addRawString(string3);
        return getInstance().getMessage(CmMessages.NO_SEGMENT_VALUE_FOUND, messageParms);
    }
   
    public static ServerMessage noValueFound(String string1,String string2,String string3) {
        MessageParameters messageParms = new MessageParameters();
        messageParms.addRawString(string1);
        messageParms.addRawString(string2);
        messageParms.addRawString(string3);
        return getInstance().getMessage(CmMessages.NO_VALUE_FOUND, messageParms);
    }
   
    public static ServerMessage noCharFound(String string1,String string2,String string3,String string4) {
        MessageParameters messageParms = new MessageParameters();
        messageParms.addRawString(string1);
        messageParms.addRawString(string2);
        messageParms.addRawString(string3);
        messageParms.addRawString(string4);
        return getInstance().getMessage(CmMessages.NO_CHAR_FOUND, messageParms);
    }
	
	//End Add - CB-224
	
	//Start CB-302 changes
    public static ServerMessage accountNotFound(String requestPartyType, String requestIdValue) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(requestPartyType);
		messageParameters.addRawString(requestIdValue);
		return getInstance().getMessage(CmMessages.ACCT_NOT_FOUND,messageParameters);
	}
    public static ServerMessage personNotFound(String requestPartyType, String requestIdValue) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(requestPartyType);
		messageParameters.addRawString(requestIdValue);
		return getInstance().getMessage(CmMessages.PERS_NOT_FOUND,messageParameters);
	}
    public static ServerMessage priceListNotFound(String requestPartyType, String requestIdValue) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(requestPartyType);
		messageParameters.addRawString(requestIdValue);
		return getInstance().getMessage(CmMessages.PLST_NOT_FOUND,messageParameters);
	}
    //End CB-302 changes
	
	//Start CB-270 changes
    public static ServerMessage pitmCharEntityNotFound(String characteristicType) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(characteristicType);
		return getInstance().getMessage(CmMessages.PITM_CHAR_ENTITY_NOT_FOUND,messageParameters);
	}
    //End CB-270 changes
	
	// Start Add - CB-145
	public static ServerMessage entityDoesNotExist(String entityId) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(entityId);
		return getInstance().getMessage(CmMessages.ENTITY_DOES_NOT_EXIST,messageParameters);
	}
	// End Add - CB-145

    //Start Add CB-54
	/*
	 * This method returns ServerMessage if required node is not provided
	 * @param Node Name
	 * @return ServerMessage
	 * */
	public static ServerMessage requiredNodeNotPopulated(String node){
  		ServerMessageFactory serverMessageFactory = ServerMessageFactory.Factory.newInstance();
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString("Missing Required Node "+node);
		MessageCategory_Id messageCategoryId = new MessageCategory_Id(BigInteger.valueOf(90000));
		return serverMessageFactory.createMessage(messageCategoryId, 1, messageParameters);
	}
	
	/*
	 * This method returns ServerMessage if Invalid value is given for Node
	 * @param Node value
	 * @param Node Name
	 * @return ServerMessage
	 * */
	public static ServerMessage invalidValueForNode(String value,String node){
		ServerMessageFactory serverMessageFactory = ServerMessageFactory.Factory.newInstance();
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(value+" Is an invalid value for "+node+" Node ");
		MessageCategory_Id messageCategoryId = new MessageCategory_Id(BigInteger.valueOf(90000));
		return serverMessageFactory.createMessage(messageCategoryId, 1, messageParameters);
	}
	
	public static ServerMessage invalidDates(){
  		ServerMessageFactory serverMessageFactory = ServerMessageFactory.Factory.newInstance();
		MessageParameters messageParameters = new MessageParameters();
		MessageCategory_Id messageCategoryId = new MessageCategory_Id(BigInteger.valueOf(17000));
		return serverMessageFactory.createMessage(messageCategoryId, 50, messageParameters);
	}
	//End Add CB-54
	
	//START Add CB-227
	/**
	 * Throw an Error while opening file
	 */
	public static ServerMessage errorToOpenFile(String fileName) {
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(fileName);
		return getInstance().getMessage(CmMessages.OPEN_IO_FILE_ERROR,messageParameters);
	}
	//End Add CB-227
	
	//Start Add CB-276
	public static ServerMessage missingField(String parameter){
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(parameter);
		return getInstance().getMessage(CmMessages.MISSING_FIELD,messageParameters);
	}
	public static ServerMessage invalidValueFromInput(String type,String value){
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(type);
		messageParameters.addRawString(value);
		return getInstance().getMessage(CmMessages.INVALID_ENTITY,messageParameters);
	}
	public static ServerMessage unableToDetermineExcessCreditContractType(String featureConfiguration,String optionType){
		MessageParameters messageParameters = new MessageParameters();
		messageParameters.addRawString(featureConfiguration);
		messageParameters.addRawString(optionType);
		return getInstance().getMessage(CmMessages.UNABLE_TO_DETERMINE_EXCESS_CREDIT_CONTRAT_TYPE,messageParameters);
	}
	//End Add CB-276
	
	//START Add CB-274
		public static ServerMessage todoCreated() {
			return getInstance().getMessage(CmMessages.TO_DO_ENTRY_CREATED);
		}
			
	//End Add CB-274
	
	//Start Add CB-283
		public static ServerMessage addressDoesNotExist(String accountId,String personId){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(accountId);
			messageParameters.addRawString(personId);
			return getInstance().getMessage(CmMessages.PRIMARY_ADDRESS_DOES_NOT_EXIST,messageParameters);
		}
		public static ServerMessage emailAddressNotFound(String personId){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(personId);
			return getInstance().getMessage(CmMessages.EMAIL_DOES_NOT_EXISTS,messageParameters);
		}
		
		public static ServerMessage requiredAttributeNotFound(String missingAttribut,String personId){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(missingAttribut);
			messageParameters.addRawString(personId);
			return getInstance().getMessage(CmMessages.REQUIRED_ATTRIBUTE_NOT_FOUND,messageParameters);
		}
		public static ServerMessage requiredAttributeNotFoundInExtendableLookup(String missingAttribut, String extendableLookup, String personId){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(missingAttribut);
			messageParameters.addRawString(extendableLookup);
			messageParameters.addRawString(personId);
			return getInstance().getMessage(CmMessages.REQUIRED_ATTRIBUTE_NOT_FOUND_IN_EXTENDABLE_LOOKUP,messageParameters);
		}
		
		public static ServerMessage unappliedPaymentsDoNotExists(String accountId){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(accountId);
			return getInstance().getMessage(CmMessages.UNAPPLIED_PAYMENTS_DO_NOT_EXISTS,messageParameters);
		}
		
		public static ServerMessage noOnAccountContractExists(String accountId){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(accountId);
			return getInstance().getMessage(CmMessages.NO_ON_ACCOUNT_CONTRACT_DEFINED,messageParameters);
		}
		
		public static ServerMessage messageSenderContextNotFound(String messageSender){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(messageSender);
			return getInstance().getMessage(CmMessages.MESSAGE_SENDER_CONTEXT_NOT_FOUND,messageParameters);
		}
		
		public static ServerMessage requiredContextTypeIsMissing(String messageSender,String contextType){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(messageSender);
			messageParameters.addRawString(contextType);
			return getInstance().getMessage(CmMessages.REQUIRED_CONTEXT_TYPE_MISSING,messageParameters);
		}
	//End Add CB-283
	
	//START Add CB-264
		/**
		 * Throw an Error while Expected FT Type not Found
		 */
		public static ServerMessage expectedFtTypeNotFound(String ftTypeDesc, String ftId) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString("Adjustment");
			messageParameters.addRawString(ftTypeDesc);
			messageParameters.addRawString(ftId);
			return getInstance().getMessage(CmMessages.EXPECTED_FT_TYPE_NOT_FOUND,messageParameters);
		}
		
		
		/**
		 * Throw an Error while No Characteristic Found in Segment 
		 */
		public static ServerMessage noSegmentCharacteristicFound(String segNum, String ftId) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString("Adjustment");
			messageParameters.addRawString(segNum);
			messageParameters.addRawString(ftId);
			return getInstance().getMessage(CmMessages.NO_SEGMENT_CHARACTERISTICS_FOUND,messageParameters);
		}
		
		//End Add CB-264
		
		//Start Add CB-393
		/**
		* Raise an error while No price item found in bill segment
		*/
		public static ServerMessage priceItemIsNotFoundIInBillSegment(String ftId,String billSegId, String curSeg) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(ftId);
			messageParameters.addRawString(billSegId);
			messageParameters.addRawString(curSeg);
			return getInstance().getMessage(CmMessages.PRICE_ITEM_IS_NOT_FOUND,messageParameters);
		}
		
		/**
		* Raise an error if Characteristic type is not Valid
		*/
		public static ServerMessage charTypeIsNotValidForAdjType(String charType,String adjType,String ftId, String curSeg) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(charType);
			messageParameters.addRawString(adjType);
			messageParameters.addRawString(ftId);
			messageParameters.addRawString(curSeg);
			return getInstance().getMessage(CmMessages.CHAR_TYPE_IS_NOT_VALID_FOR_ADJ_TYPE,messageParameters);
		}
		
		/**
		* Raise an error while No bill segment found
		*/
		public static ServerMessage noBillSegmentFound(String ftId, String adjType, String curSeg) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(ftId);
			messageParameters.addRawString(adjType);
			messageParameters.addRawString(curSeg);
			return getInstance().getMessage(CmMessages.NO_BILL_SEG_FOUND,messageParameters);
		}
		
		/**
		* Raise an error if Invalid FTType is populated
		*/
		public static ServerMessage invalidFtType(String billSeg,String ftType,String curSeg,String ftId){
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(billSeg);
			messageParameters.addRawString(ftType);
			messageParameters.addRawString(curSeg);
			messageParameters.addRawString(ftId);
			return getInstance().getMessage(CmMessages.NO_VALUE_FOUND,messageParameters);
		}
		//End Add CB-393
		
		//Start Add CB-444
		/**
		* Raise an error while Tender source not found on Ft
		*/
		public static ServerMessage cannotFindTndrSrcOfPayment(String seg,String ftId) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(seg);
			messageParameters.addRawString(ftId);
			return getInstance().getMessage(CmMessages.CANNOT_FIND_TNDR_SRC_OF_PAYMENT,messageParameters);
		}
		
		/**
		*Raise an error if Characteristic type is not found on price item 
		*/
		public static ServerMessage glstrInCharacteristicCannotBeFound(String priceItem,String priceItemCd,String seg, String ftId) {
			MessageParameters messageParameters = new MessageParameters();
			messageParameters.addRawString(priceItem);
			messageParameters.addRawString(priceItemCd);
			messageParameters.addRawString(seg);
			messageParameters.addRawString(ftId);
			return getInstance().getMessage(CmMessages.GLSTR_IN_CHARACTERISTIC_CANNOT_BE_FOUND,messageParameters);
		}
    
		 //End Add CB-444
    
	
	
    /**
     * Returns the MessageRepository instance.
     * 
     * @return MessageRepository
     */
    static CmMessageRepository getInstance() {
        if (instance == null) {
            instance = new CmMessageRepository();
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

	
