/* 
 **************************************************************************
 *           	     Confidentiality Information:
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
 *  This algorithm validates the information stored in the Customer 
 *  Interface Inbound Message.It checks if mandatory fields are populated 
 *  in the Inbound Message as well as for validity of populated fields. 
 *  The algorithm will raise appropriate error message if any required 
 *  or invalid values are passed onto the Inbound Message fields.
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-04-18	DSekar     Initial version.
 * 2020-05-08   JFerna     CB-53. Existing Person Account Validation
 * 2020-05-14   JFerna     CB-59. Customer Number Person Identifier
 *                                Type Validation
 * 2020-05-14   JFerna     CB-60. Removed validation check that
 *                                Bill Route Type is required
 * 2020-05-25   JFerna     CB-91. Added validation that account persons
 *                                must have at least one main customer 
 * 2020-05-28 	JFerna	   CB-70. Reverted CB-60.	
 * 2020-06-03 	JFerna     CB-105. Updated defaulting of 
 *                                 Account Relationship Type 
 * 2020-07-07   JFerna     CB-52. Added/Updated validations based on design
 *                                changes  
 * 2020-07-22   JFerna     CB-233. Updated to validate account node only
 *                                 when populated.  
 *                               . Revert CB-53
 * 2020-07-24   JFerna     CB-256. Remove required address validation.   
 * 2020-08-11   JFerna     CB-177. Added additional address validation
 *                                 for update scenario   
 * 2020-08-12	KGhuge	   CB-54   Capture Statement Construct during Customer Interface  
 **************************************************************************
 */

package com.splwg.cm.domain.customer.businessObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;						  
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.BusinessObjectStatusCode;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.BusinessObjectStatusTransitionConditionLookup;
import com.splwg.base.api.lookup.YesNoOptionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.businessObjectStatusReason.BusinessObjectStatusReason_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.country.Country_Id;
import com.splwg.base.domain.common.country.State_Id;
import com.splwg.base.domain.common.lookup.LookupValue;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.api.lookup.EntityFlagLookup;
import com.splwg.ccb.domain.admin.accountRelationshipType.AccountRelationshipType_Id;
import com.splwg.ccb.domain.admin.billRouteType.BillRouteType_Id;
import com.splwg.ccb.domain.admin.cisDivision.CisDivision_Id;
import com.splwg.ccb.domain.admin.customerClass.CustomerClass_Id;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;															   
import com.splwg.ccb.domain.customerinfo.address.Address;
import com.splwg.ccb.domain.customerinfo.address.AddressCharacteristic;
import com.splwg.ccb.domain.customerinfo.address.AddressEntity;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonId;
import com.splwg.cm.api.lookup.AddressTypeFlgLookup;
import com.splwg.cm.api.lookup.BillingAddressSourceLookup;
import com.splwg.ccb.domain.interfaces.inboundMessage.InboundMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.cm.domain.customer.utility.CmCustInterfaceUtilityBussComp;
import com.splwg.cm.domain.customer.utility.CmCustomerInterfaceConstants;
import com.splwg.shared.common.ServerMessage;
import com.splwg.ccb.domain.admin.statementCycle.StatementCycle_Id;
import com.splwg.ccb.domain.admin.statementRoutingType.StatementRoutingType_Id;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstruct_Id;


//Start Change - CB-256
//Start Change - CB-52
///**
// * @author DSekar
// *
//@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = idType, name = custNumPerIdType, required = true, type = entity)
// */
///**
// * @author DSekar
// *
//@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = idType, name = custNumPerIdType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = billToAddressType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressBillToIndicatorCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = billToAccountRelationshipType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = accountOverrideAllowed, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (lookupName = personOrBusiness, name = mainCustomerPersonType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = shipToAddressType, required = true, type = lookup)
// *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressShipToIndicatorCharType, required = true, type = entity)
// *            , @AlgorithmSoftParameter (entityName = accountRelationshipType, name = shipToAccountRelationshipType, required = true, type = entity)})
// */
//End Change - CB-52
/**
 * @author DSekar
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = idType, name = custNumPerIdType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = billToAddressType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressBillToIndicatorCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (lookupName = yesNoOption, name = accountOverrideAllowed, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = personOrBusiness, name = mainCustomerPersonType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (lookupName = addressTypeFlg, name = shipToAddressType, required = true, type = lookup)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressShipToIndicatorCharType, required = true, type = entity)})
 */
//End Change - CB-256

public class CmCustomerInterfaceValidationAlgComp_Impl extends
CmCustomerInterfaceValidationAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot {
	
	@Override
    public boolean getForcePostProcessing() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public BusinessObjectStatusCode getNextStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BusinessObjectStatusTransitionConditionLookup getNextStatusCondition() {
        return transitionConditionLookup;
    }

    @Override
    public BusinessObjectStatusReason_Id getStatusChangeReasonId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getUseDefaultNextStatus() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setAction(BusinessObjectActionLookup arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBusinessObject(BusinessObject arg0) {

    }

    @Override
    public void setBusinessObjectKey(BusinessObjectInstanceKey arg0) {
        newBusinessObjectInstanceKey = arg0;
        // TODO Auto-generated method stub

    }
    
    /**
     * Validate soft parameters
     */
    @Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
    	
    	//Retrieve Algorithm Parameter Descriptions
    	String addressBillToIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(CmCustomerInterfaceConstants.PARM_VAL_IDX_ADDR_BILLTO_IND_CHAR_TYPE).fetchLanguageParameterLabel().trim();
    	String addressShipToIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(CmCustomerInterfaceConstants.PARM_VAL_IDX_ADDR_SHPTO_IND_CHAR_TYPE).fetchLanguageParameterLabel().trim();
    	
    	//Retrieve Soft Parameters
    	addressBillToIndicatorCharType = getAddressBillToIndicatorCharType();
    	addressShipToIndicatorCharType = getAddressShipToIndicatorCharType();
    	
    	//Check that Address Bill To Characteristic Type is valid for Address Characteristic entity
    	validateCharacteristicType(addressBillToIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressBillToIndicatorCharTypeDesc);
    	
    	//Check that Address Ship To Characteristic Type is valid for Address Characteristic entity
    	validateCharacteristicType(addressShipToIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressShipToIndicatorCharTypeDesc);
	}
   
    // Hard Parameters
    private BusinessObjectInstanceKey newBusinessObjectInstanceKey = null;
	private BusinessObjectInstance boInstance = null;
	
	//CB-52 - Start Add
	// Soft Parameters
	private CharacteristicType addressBillToIndicatorCharType;
	private CharacteristicType addressShipToIndicatorCharType;
	//CB-52 - End Add
	
	//Work Variables
    private String transactionId = null;
    private Country_Id ctryId = null;
    private State_Id stateId = null;
    private Integer maxSequence = BigInteger.ZERO.intValue();
    private ArrayList < ServerMessage > errorLogList = new ArrayList < ServerMessage > ();
    private ArrayList<Integer> seqIntAry = new ArrayList< Integer >();
    private BusinessObjectStatusTransitionConditionLookup transitionConditionLookup;
    //CB-53 - Start Add
    private CmCustInterfaceUtilityBussComp custIntfUtility = CmCustInterfaceUtilityBussComp.Factory.newInstance();
    private Person person;
    //CB-233 - Start Revert CB-53
    //private Account account;
    //private StringBuilder retrieveAcctSqlString;
    //private Query<Account> retrieveAcctQry;
    //CB-233 - End Revert CB-53
    private List <SQLResultRow> personSqlResRowList;
    //CB-53 - End Add
	//CB-91 - Start Add
	private int isMainCustomerCount;
	private String mainPersonIdType;
	private String mainPersonIdValue;
    private String isMainCustomer;
    private String accountPersonIdType;
    private String accountPersonIdValue;
    private String yesLookupValue = YesNoOptionLookup.constants.YES.trimmedValue();
    private String noLookupValue = YesNoOptionLookup.constants.NO.trimmedValue();																				 
	//CB-91 - End Add    
    //CB-52 - Start Add
    private COTSInstanceList addressList;
	private COTSInstanceNode addressEntity;
	private COTSInstanceList addressEntityList;
	private COTSInstanceNode addressCharacteristics;  	
	private COTSInstanceList addressCharacteristicList;
    private List<COTSInstanceNode> addressTypeEntityList; 
	private Bool noAddressRecordFound;
	private Bool multipleAddressRecordFound;
	//Start Delete - CB-256
	//private Bool isSameEffectiveDate;
	//End Delete - CB-256
	private int primaryAddressIndicatorCount;
	private int addressTypeCount;
	//Start Delete - CB-256
	//private Date mainCustomerEffectiveDate;
	//private Address effectiveBillToAddress = null;
	//private Address effectiveShipToAddress = null;
	//private Date billToEffectiveDate = null;
	//private Date shipToEffectiveDate = null;
	//End Delete - CB-256
	private Bool isNewPerson;
    //CB-52 - End Add
	
	//CB-256 - Start Add
	private	BillingAddressSourceLookup billAddressSource;
	//CB-256 - End Add
	
	//CB-177 - Start Add
	private StringBuilder stringBuilder;
	private Query<Address> getEffectiveAddressQry;
	private Query<Address> getAddressQry;
	//CB-177 - End Add
	
	//Start Add CB-54
	private Document boDoc = null;
	//End CB-54

    /**
     * Main Processing
     */
	//CB-52 - Start Change
    /*public void invoke() {
    	
    	//Retrieve Inbound Message Details
        boInstance = BusinessObjectDispatcher.read(newBusinessObjectInstanceKey, true);
        
        //Validate Message Header
        validateMessageHeader();
               
        COTSInstanceNode message = boInstance.getGroup(CmCustomerInterfaceConstants.MESSAGE_ELE);
        COTSInstanceNode messageData = message.getGroup(CmCustomerInterfaceConstants.MESSAGEDATA_ELE);
        COTSInstanceNode mainCustomer = messageData.getGroup(CmCustomerInterfaceConstants.MAINCUSTOMER_ELE);
        COTSInstanceNode identifier = mainCustomer.getGroup(CmCustomerInterfaceConstants.IDENTIFIERS_ELE);
        COTSInstanceNode address = mainCustomer.getGroup(CmCustomerInterfaceConstants.ADDRESS_ELE);
        COTSInstanceList idList = identifier.getList(CmCustomerInterfaceConstants.ID_ELE);
        COTSInstanceNode mainCustAcc = mainCustomer.getGroup(CmCustomerInterfaceConstants.ACCOUNTS_ELE);
        
        //CB-53 - Start Change
        COTSInstanceNode persons = mainCustomer.getGroup(CmCustomerInterfaceConstants.PERSONS_ELE);
        
        //Validate Person Identifiers
        //CB-59 - Start Add
        validateIfCustNumIdTypeExists(idList);
        //CB-59 - End Add
        
        //validateIdList(idList, BigInteger.ZERO.intValue(),CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.PRIM_ID_PARAM);
        //CB-91 - Start Change
        //validateIdList(idList, BigInteger.ZERO.intValue(),CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.PRIM_ID_PARAM, Bool.TRUE);
        validateIdList(idList, BigInteger.ZERO.intValue(),CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.PRIM_ID_PARAM, Bool.TRUE, Bool.FALSE);
        //CB-91 - End Change
        
        //Validate Parent Person
        validateParentPerson(persons);
        //CB-53 - End Change   
        
        //Validate CIS Division
        validateCisDivision(mainCustomer.getString(CmCustomerInterfaceConstants.DIVISION_ELE));
        
        //Validate Person Address
        validateAddress(address, ctryId, stateId);
        
        //Validate Accounts
        validateMainCustAccountList(mainCustAcc);
        
        //Populate Error List
        if (!errorLogList.isEmpty()) {
        	COTSInstanceNode errorNode = message.getGroup(CmCustomerInterfaceConstants.ERRORLIST_ELE);
        	COTSInstanceList errorList = errorNode.getList(CmCustomerInterfaceConstants.ERROR_ELE);
        	COTSInstanceListNode errListNode = null;
        	if(!errorList.isEmpty())
        	{
        		maxSequence = fetchMaxSeqNum(errorList);
        	}
        	for (ServerMessage logList: errorLogList) {
        		maxSequence++; 
        		errListNode = errorList.newChild();   		
        		errListNode.setXMLString(CmCustomerInterfaceConstants.SEQNO_ELE,Integer.toString(maxSequence));
        		errListNode.set(CmCustomerInterfaceConstants.MESSAGETEXT_ELE, logList.getMessageText());
        		errListNode.setXMLString(CmCustomerInterfaceConstants.MESSAGECATOGERY_ELE, logList.getMessageId().getMessageCategoryId().getIdValue().toString());
        		errListNode.setXMLString(CmCustomerInterfaceConstants.MESSAGENBR_ELE, logList.getMessageId().getMessageNumber().toString());
        		
        	}
        	BusinessObjectDispatcher.fastUpdate(boInstance.getDocument());
            transitionConditionLookup = BusinessObjectStatusTransitionConditionLookup.constants.EXCEPTION;
        }
        
        
    }*/
    
    public void invoke() {
    	
    	//Retrieve Inbound Message Details
        boInstance = BusinessObjectDispatcher.read(newBusinessObjectInstanceKey, true);
        
		//Start Add CB-54
        boDoc = boInstance.getDocument(); 
     	Node messageNodeRoot = boDoc.getRootElement();
        Node messageNode = messageNodeRoot.selectSingleNode(CmCustomerInterfaceConstants.MESSAGE_ELE);		
		//End Add CB-54
		
        //Validate Message Header and Message Data
        //Start Change CB-54
		//validateMessageHeader();
		validateMessageHeader(messageNode);
		//End CB-54
        
        //Retrieve Message Data
        COTSInstanceNode message = boInstance.getGroup(CmCustomerInterfaceConstants.MESSAGE_ELE);
        COTSInstanceNode messageData = message.getGroup(CmCustomerInterfaceConstants.MESSAGEDATA_ELE);
        COTSInstanceNode mainCustomer = messageData.getGroup(CmCustomerInterfaceConstants.MAINCUSTOMER_ELE);
        COTSInstanceNode identifier = mainCustomer.getGroup(CmCustomerInterfaceConstants.IDENTIFIERS_ELE);
        COTSInstanceNode address = mainCustomer.getGroup(CmCustomerInterfaceConstants.ADDRESSES_ELE);
        COTSInstanceList idList = identifier.getList(CmCustomerInterfaceConstants.ID_ELE);
        //Start Delete - CB-233
        //COTSInstanceNode persons = mainCustomer.getGroup(CmCustomerInterfaceConstants.PERSONS_ELE);
        //COTSInstanceList personList = persons.getList(CmCustomerInterfaceConstants.PERSON_ELE);
        //End Delete - CB-233
        COTSInstanceNode mainCustAcc = mainCustomer.getGroup(CmCustomerInterfaceConstants.ACCOUNTS_ELE);
        COTSInstanceList accountList = mainCustAcc.getList(CmCustomerInterfaceConstants.ACCOUNT_ELE);
        //Start Delete - CB-256
        //mainCustomerEffectiveDate = mainCustomer.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE);
        //End Delete - CB-256
        
        //Retrieve Primary Identifiers and Email
		List<COTSInstanceNode> personIds = idList.getElementsWhere("[isPrimary = '"+YesNoOptionLookup.constants.YES.trimmedValue()+"' ]");
		String email = mainCustomer.getString(CmCustomerInterfaceConstants.EMAIL_ELE);
		
		//If primary identifier is found, retrieve person.
		if(notNull(personIds)&&!personIds.isEmpty()){
			
			//If only one primary identifier is found, retrieve person using Primary Identifier.
			if(personIds.size() == 1){
				COTSInstanceNode primaryIdNode = personIds.get(0);	
				String idType = primaryIdNode.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE);
				String idValue = primaryIdNode.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE);				
				List <SQLResultRow> personsList = findPersonId(idType, idValue);
				
		        if (!personsList.isEmpty()) {
					//If only one person is found, retrieve person.
		        	if (personsList.size() == 1){	
		        		person = isNull(personsList.get(0).getEntity("PER_ID", Person.class))? null 
								: personsList.get(0).getEntity("PER_ID", Person.class);
		        	}	        	
		        }			
			}
			
		//Otherwise, use Email in retrieving the person.
		}else{
			//If email is provided, retrieve person.
			if(!isBlankOrNull(email)){
				person = retrievePersonByEmail(email);
			//Otherwise, log an error.
			}else{
				addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.PRIM_ID_AND_EMAIL_MISSING,transactionId));
			}
		}
		
		//Retrieve and validate CIS Division
        String cisDivision = mainCustomer.getString(CmCustomerInterfaceConstants.DIVISION_ELE); 
        validateCisDivision(cisDivision);
        
        //Retrieve Address List
        addressList = address.getList(CmCustomerInterfaceConstants.ADDRESS_ELE);
    	
        //Retrieve Person Type
        LookupValue personType = mainCustomer.getLookup(CmCustomerInterfaceConstants.PERSON_TYPE_ELE).getLookupValue();
		
		//If Person is not found, set BO Action Flag to ADD and do necessary validations
		if (isNull(person)){
			isNewPerson = Bool.TRUE;
			
			//Set BO Action Flag to ADD
			mainCustomer.set(CmCustomerInterfaceConstants.BO_ACTION_FLG, CmCustomerInterfaceConstants.ADD_ACTION);	
            
			//Start Change - CB-256
        	////If address is not provided, log an error.
        	//if (isNull(addressList) || addressList.isEmpty()){
        	//	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.ADDR_INFO_FOR_MAIN_CUST_MISSING,transactionId));
            ////Otherwise, do necessary validations.
        	//}else{	
            //	validatePersonAddress(billToEffectiveDate);
        	//}
        	if (notNull(addressList) && !addressList.isEmpty()){
            	validateBillToAddress();
        	}
        	//End Change - CB-256
        				
	        //If Person Type is Main Customer, validate that Customer Number Person Identifier is provided
	        if(personType.equals(getMainCustomerPersonType().getLookupValue())){
	            validateIfCustNumIdTypeExists(idList);
	        }
	        
	        //CB-233 - Start Revert CB-53
        	//Validate Id List
        	//validateIdList(idList, BigInteger.ZERO.intValue(),CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.PRIM_ID_PARAM, Bool.TRUE, Bool.FALSE);
	        validateIdList(idList, BigInteger.ZERO.intValue(),CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.PRIM_ID_PARAM, Bool.FALSE);
	        //CB-233 - End Revert CB-53
	        
            //Validate Accounts
        	//Start Add - CB-233
        	if(notNull(accountList) && !accountList.isEmpty()){
        	//End Add - CB-233
        		validateMainCustAccountList(mainCustAcc);
        	//Start Add - CB-233
        	}
        	//End Add - CB-233
            
        	//Start Revert CB-53 - CB-233
            ////If parent person is already existing, validate that it has an existing account
            //validateParentPerson(persons);
        	//End Revert CB-53 - CB-233

		//Otherwise, set BO Action Flag to UPDATE and validate populated values
		}else{
			isNewPerson = Bool.FALSE;
			
			//Set BO Action to UPDATE
			mainCustomer.set(CmCustomerInterfaceConstants.BO_ACTION_FLG, CmCustomerInterfaceConstants.UPDATE_ACTION);
			
			//Set Person Id
			mainCustomer.set(CmCustomerInterfaceConstants.PERSONID_ELE, person.getId().getIdValue());
			
			//Start Change - CB-256
    		/*//Get Existing Effective Bill To Address
			effectiveBillToAddress = getEffectiveAddressEntity(person,this.getBillToAddressType());
			if (notNull(effectiveBillToAddress)){				
				billToEffectiveDate = notNull(effectiveBillToAddress.getEffectiveCharacteristic(
						this.getAddressBillToIndicatorCharType())) ? effectiveBillToAddress.getEffectiveCharacteristic(
						this.getAddressBillToIndicatorCharType()).fetchIdEffectiveDate() : null;
			}
			
      		//Get Existing Effective Ship To Address
    		effectiveShipToAddress = getEffectiveAddressEntity(person,this.getShipToAddressType());
			if (notNull(effectiveShipToAddress)){
				shipToEffectiveDate = notNull(effectiveShipToAddress.getEffectiveCharacteristic(
						this.getAddressShipToIndicatorCharType())) ? effectiveShipToAddress.getEffectiveCharacteristic(
						this.getAddressShipToIndicatorCharType()).fetchIdEffectiveDate() : null;
			}*/
			//End Change - CB-256
			
			//Validate Person Address 
        	if (notNull(addressList) && !addressList.isEmpty()){
        		//Start Change - CB-256
            	//validatePersonAddress(billToEffectiveDate);
        		validateBillToAddress();
        		//End Change - CB-256
        	}
			
        	//If Person Type is Main Customer, validate that Customer Number Person Identifier is provided
	        if((notNull(idList) && !idList.isEmpty()) && personType.equals(getMainCustomerPersonType().getLookupValue())){
	        	validateIfCustNumIdTypeExists(idList);
	        }
	        
            //Validate Accounts 
	        if(notNull(accountList) && !accountList.isEmpty()){
	        	validateMainCustAccountList(mainCustAcc);
	        }
        	
	        //Start Revert CB-53 - CB-233
        	////If parent person is already existing, validate that it has an existing account
        	//if (notNull(personList) && !personList.isEmpty()){
        	//	validateParentPerson(persons);
        	//}
	        //End Revert CB-53 - CB-233
		}
		//Start Add CB-54
   		Node messageDataNode = messageNode.selectSingleNode(CmCustomerInterfaceConstants.MESSAGE_DATA_ELE);
		Node mainCustomerNode = messageDataNode.selectSingleNode(CmCustomerInterfaceConstants.MAIN_CUSTOMER_ELE);
		Node statementNode = mainCustomerNode.selectSingleNode(CmCustomerInterfaceConstants.STATEMENTS_ELE);
		COTSInstanceNode statementNodeGroup = mainCustomer.getGroup(CmCustomerInterfaceConstants.STATEMENTS_ELE);
        if(!isNull(statementNode) && statementNode.hasContent()){
          	validateStatementNode(statementNodeGroup,isNewPerson);
        }
		//End Add CB-54
		
        //Populate Error List
        if (!errorLogList.isEmpty()) {
        	COTSInstanceNode errorNode = message.getGroup(CmCustomerInterfaceConstants.ERRORLIST_ELE);
        	COTSInstanceList errorList = errorNode.getList(CmCustomerInterfaceConstants.ERROR_ELE);
        	COTSInstanceListNode errListNode = null;
        	if(!errorList.isEmpty())
        	{
        		maxSequence = fetchMaxSeqNum(errorList);
        	}
        	for (ServerMessage logList: errorLogList) {
        		maxSequence++; 
        		errListNode = errorList.newChild();   		
        		errListNode.setXMLString(CmCustomerInterfaceConstants.SEQNO_ELE,Integer.toString(maxSequence));
        		errListNode.set(CmCustomerInterfaceConstants.MESSAGETEXT_ELE, logList.getMessageText());
        		errListNode.setXMLString(CmCustomerInterfaceConstants.MESSAGECATOGERY_ELE, logList.getMessageId().getMessageCategoryId().getIdValue().toString());
        		errListNode.setXMLString(CmCustomerInterfaceConstants.MESSAGENBR_ELE, logList.getMessageId().getMessageNumber().toString());
        		
        	}

            transitionConditionLookup = BusinessObjectStatusTransitionConditionLookup.constants.EXCEPTION;
        }
        //Start Add CB-54
		//Document doc = boInstance.getDocument();
        //Node mainNode = doc.selectSingleNode("mainCustomer");
        //Node statementNode = mainNode.selectSingleNode("statements");
        //if(statementNode.hasContent()){
			//validateStatementNode(statementNode);
		//}
		//End Add CB-54
		
        BusinessObjectDispatcher.fastUpdate(boInstance.getDocument());       
    }
    //CB-52 - End Change
    
	
	//Start Add CB-54
	/**
	* This Method will validate Statement Node if it is populated
	*/
		private void validateStatementNode(COTSInstanceNode statementNode,Bool isNew){
				String stmeCycleCd = statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_CYCLE_ELE);
				String stmtRouteType = statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE);
				if(!isBlankOrNull(stmeCycleCd)){
					StatementCycle_Id stmtCycid = new StatementCycle_Id(stmeCycleCd.trim());
					if(isNull(stmtCycid.getEntity())){
						addError(CmMessageRepository.invalidValueForNode(stmeCycleCd,CmCustomerInterfaceConstants.STATEMENT_CYCLE_ELE));
					}
				}else{
					addError(CmMessageRepository.requiredNodeNotPopulated(CmCustomerInterfaceConstants.STATEMENT_CYCLE_ELE));
				}
				if(!isBlankOrNull(stmtRouteType)){
					StatementRoutingType_Id stmtRouteTypId = new StatementRoutingType_Id(stmtRouteType.trim());
					if(isNull(stmtRouteTypId.getEntity())){
						addError(CmMessageRepository.invalidValueForNode(stmtRouteType,CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE));
					}
				}else{
					addError(CmMessageRepository.requiredNodeNotPopulated(CmCustomerInterfaceConstants.STATEMENT_ROUTE_TYPE_ELE));
				}
				if(isNew.isTrue()){
					COTSInstanceNode stmtDtlsGrp = statementNode.getGroup(CmCustomerInterfaceConstants.STATEMENT_CNST_DETAILS_ELE);
					COTSInstanceList stmtDetlsList = stmtDtlsGrp.getList(CmCustomerInterfaceConstants.STATEMENT_CNST_DETAIL_ELE);
					if(!isNull(stmtDetlsList) && !stmtDetlsList.isEmpty()){
						for(COTSInstanceNode listNode : stmtDetlsList){
							if(isNull(listNode.getNumber(CmCustomerInterfaceConstants.PRINT_ORDER_ELE))){
								addError(CmMessageRepository.requiredNodeNotPopulated(CmCustomerInterfaceConstants.PRINT_ORDER_ELE));
							}
							if(!isNull(listNode.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE))){
								if(!isNull(listNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE)) && listNode.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE).isAfter(listNode.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE))){
										addError(CmMessageRepository.invalidDates());
								}
							}else{
								addError(CmMessageRepository.requiredNodeNotPopulated(CmCustomerInterfaceConstants.STARTDATE_ELE));
							}
						}
					}else{
						addError(CmMessageRepository.requiredNodeNotPopulated(CmCustomerInterfaceConstants.STATEMENT_CNST_DETAILS_ELE));
					}
				}else{
					String statementCnstId = statementNode.getString(CmCustomerInterfaceConstants.STATEMENT_CNST_ID_ELE);
					if(!isBlankOrNull(statementCnstId)){
						StatementConstruct_Id stmtCnstId = new StatementConstruct_Id(statementCnstId.trim());
						if(isNull(stmtCnstId.getEntity())){
							addError(CmMessageRepository.invalidValueForNode(statementCnstId,CmCustomerInterfaceConstants.STATEMENT_CNST_ID_ELE));
						}
					}else{
						addError(CmMessageRepository.requiredNodeNotPopulated(CmCustomerInterfaceConstants.STATEMENT_CNST_ID_ELE));
					}
				}
			}
	//End Add CB-54
	/**
	 * This method validates Message Element,
	 * Message header,Message data, External
	 * Transaction Id,Main Customer Nodes have
	 * been provided or not.
	 * 
	 */
	 //Start Change CB-54
     //private void validateMessageHeader() {
	 private void validateMessageHeader(Node messageNode) {
        //Document boDoc = boInstance.getDocument();
        //Node messageNodeRoot = boDoc.getRootElement();
        //Node messageNode = messageNodeRoot.selectSingleNode(CmCustomerInterfaceConstants.MESSAGE_ELE);
	    //End Change CB-54
        if (isNull(messageNode)) {
            addError(InboundMessageRepository.entityNotFound(CmCustomerInterfaceConstants.MESSAGE_ELE));
        }
        Node messageHeader = messageNode.selectSingleNode(CmCustomerInterfaceConstants.MESSAGEHEADER_ELE);
        if (isNull(messageHeader)) {
            addError(InboundMessageRepository.entityNotFound(CmCustomerInterfaceConstants.MESSAGEHEADER_ELE));
        }
        Node transAction = messageHeader.selectSingleNode(CmCustomerInterfaceConstants.EXTERNALTRANSACTIONID_ELE);
        if (isNull(transAction)) {
            addError(InboundMessageRepository.entityNotFound(CmCustomerInterfaceConstants.EXTERNALTRANSACTIONID_ELE));
        } else {
            if (!isBlankOrNull(transAction.getText())) {
                transactionId = transAction.getText().trim();
            } else {
                addError(InboundMessageRepository.entityNotFound(CmCustomerInterfaceConstants.EXTERNALTRANSACTIONID_ELE));
            }
        }
        Node messageData = messageNode.selectSingleNode(CmCustomerInterfaceConstants.MESSAGEDATA_ELE);
        if (isNull(messageData)) {
        	  addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.NOTFOUND_FOR_TRANSNSID,CmCustomerInterfaceConstants.MSG_DATA_PARAM,transactionId)); 
        }
        Node mainCustomerNode = messageData.selectSingleNode(CmCustomerInterfaceConstants.MAINCUSTOMER_ELE);
        if (isNull(mainCustomerNode)) {
        	 addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.NOTFOUND_FOR_TRANSNSID,CmCustomerInterfaceConstants.MAIN_CUST_PARAM,transactionId)); 
        	 }
    }

	/**
	 * This methods validates Account List
	 * @param mainCustAcc
	 */	
	private void validateMainCustAccountList(COTSInstanceNode mainCustAcc) {
        COTSInstanceList mainCustAccList = mainCustAcc.getList(CmCustomerInterfaceConstants.ACCOUNT_ELE);
        if (isNull(mainCustAccList) || mainCustAccList.isEmpty()) {
            addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.NOTFOUND_FOR_TRANSNSID,CmCustomerInterfaceConstants.ACCOUNT_LIST_PARAM,transactionId));
        } else {
            COTSInstanceNode identifiersAcct = null;
            COTSInstanceList idAcctList = null;
            COTSInstanceNode acctPersonsGroup = null;
            COTSInstanceList acctPerGrpList = null;
            COTSInstanceNode acctAutoPayGrp = null;
            CustomerClass_Id custclsType = null;
            COTSInstanceList acctAutoPayList = null;
            AccountRelationshipType_Id acctRelTypeId = null;
            BillRouteType_Id billRoutId = null;
            for (COTSInstanceListNode acctMain: mainCustAccList) {
                if (!isBlankOrNull(acctMain.getString(CmCustomerInterfaceConstants.DIVISION_ELE))) {
                    validateCisDivision(acctMain.getString(CmCustomerInterfaceConstants.DIVISION_ELE).trim());
                }
                if (isBlankOrNull(acctMain.getString(CmCustomerInterfaceConstants.CUSTOMERCLASS_ELE))) {
                	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.CUSTOMER_CL_PARAM,transactionId)); // Customer class not give
                } else {

                    custclsType = new CustomerClass_Id(acctMain.getString(CmCustomerInterfaceConstants.CUSTOMERCLASS_ELE).trim());
                    if (isNull(custclsType.getEntity())) {
                        addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INAVLD_FOR_TRANSNSID,CmCustomerInterfaceConstants.CUSTOMER_CL_PARAM,acctMain.getString(CmCustomerInterfaceConstants.CUSTOMERCLASS_ELE).trim(),transactionId)); // Customer Class is not valid 
                    }
                }
                
                identifiersAcct = acctMain.getGroup(CmCustomerInterfaceConstants.IDENTIFIERS_ELE);
                if (isNull(identifiersAcct)) {
                    addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.NOTFOUND_FOR_TRANSNSID,CmCustomerInterfaceConstants.ACCT_IDENTIFIERS_PARM,transactionId)); // No ID exists for Account level 
                } else {
                    idAcctList = identifiersAcct.getList(CmCustomerInterfaceConstants.ID_ELE);
                    if (!idAcctList.isEmpty()) {
                    	//CB-233 - Start Revert CB-53
                    	//CB-53 - Start Change
                    	//validateIdList(idAcctList, BigInteger.ZERO.intValue(), CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.ACCT_PRIM_ID_PARAM);
                        //CB-91 - Start Change
                    	//validateIdList(idAcctList, BigInteger.ZERO.intValue(), CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.ACCT_PRIM_ID_PARAM, Bool.FALSE);
                        //validateIdList(idAcctList, BigInteger.ZERO.intValue(), CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.ACCT_PRIM_ID_PARAM, Bool.FALSE, Bool.TRUE);
                    	validateIdList(idAcctList, BigInteger.ZERO.intValue(), CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.BLANK_VALUE, CmCustomerInterfaceConstants.ACCT_PRIM_ID_PARAM, Bool.TRUE);
                    	//CB-91 - End Change											
                        //CB-53 - End Change
                      //CB-233 - Start Revert CB-53
                    } else {
                    	 addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.NOTFOUND_FOR_TRANSNSID,CmCustomerInterfaceConstants.ACCT_IDENTIFIERS_PARM,transactionId)); 
                    }
                }
                
                acctPersonsGroup = acctMain.getGroup(CmCustomerInterfaceConstants.ACCOUNTPERSONS_ELE);
                if (!isNull(acctPersonsGroup)) {
                	 acctPerGrpList = acctPersonsGroup.getList(CmCustomerInterfaceConstants.PERSON_ELE);
                	if (isNull(acctPerGrpList) || acctPerGrpList.isEmpty()) {
                    	//CB-52 - Start Add
                    	if (isNewPerson.isTrue()){
                    	//CB-52 - End Add
                    		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.ACCOUNT_PER_LIST_PARAM,transactionId));
                        //CB-52 - Start Add
                    	}
                        //CB-52 - End Add                    		
                    } else {
                        validateAccountPersonsList(acctPerGrpList, acctRelTypeId, billRoutId,null);
                    }
                } else {
                	//CB-52 - Start Add
                	if (isNewPerson.isTrue()){
                	//CB-52 - End Add
                		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.NOTFOUND_FOR_TRANSNSID,CmCustomerInterfaceConstants.ACCOUNT_PER_LIST_PARAM,transactionId));
                    //CB-52 - Start Add
                	}
                    //CB-52 - End Add
                }
                if (!isNull(acctMain.getGroup(CmCustomerInterfaceConstants.ACCOUNTAUTOPAY_ELE))) {
                    acctAutoPayGrp = acctMain.getGroup(CmCustomerInterfaceConstants.ACCOUNTAUTOPAY_ELE);
                    acctAutoPayList = acctAutoPayGrp.getList(CmCustomerInterfaceConstants.AUTOPAY_ELE);
                    if (!acctAutoPayList.isEmpty()) {
                        validateAcctAutoPayList(acctAutoPayList);
                    }

                }
            }
        }
    }


	/**
	 * This method validates AutoPayList
	 * @param acctAutoPayList
	 */
	private void validateAcctAutoPayList(COTSInstanceList acctAutoPayList) {
        for (COTSInstanceListNode autoPayIns: acctAutoPayList) {
            if (isNull(autoPayIns.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE))) {
            	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.APAY_START_DT_PARAM,transactionId)); 
            }
            if (isBlankOrNull(autoPayIns.getString(CmCustomerInterfaceConstants.APAYSOURCECODE_ELE))) {
            	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.APAY_SRC_CD_PARAM,transactionId)); 
            } 
            if (isBlankOrNull(autoPayIns.getString(CmCustomerInterfaceConstants.EXTERNALACCOUNTID_ELE))) {
                addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.EXT_ACCT_ID_PARAM,transactionId)); //externalAccountId is Not Given 
            }
            if (isNull(autoPayIns.getString(CmCustomerInterfaceConstants.EXPMONTH_ELE))) {
            	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.EXP_MONTH_PARAM,transactionId));
            }
            if (isNull(autoPayIns.getString(CmCustomerInterfaceConstants.EXPYEAR_ELE))) {
            	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.EXP_YEAR_PARAM,transactionId));
            }
            if (isNull(autoPayIns.getString(CmCustomerInterfaceConstants.ENTITYNAME_ELE))) {
            	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.ENTITY_NAME_PARAM,transactionId));        
            	}
            if (isNull(autoPayIns.getString(CmCustomerInterfaceConstants.APAYROUTETYPE_ELE))) {
              	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.APAY_RTE_TYPE_PARAM,transactionId));        
            } 
            if ((!isNull(autoPayIns.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE)) && !isNull((autoPayIns.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE))))) {
                if ((autoPayIns.getDate(CmCustomerInterfaceConstants.ENDDATE_ELE).isBefore(autoPayIns.getDate(CmCustomerInterfaceConstants.STARTDATE_ELE)))) {
                	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INAVLD_FOR_TRANSNSID,CmCustomerInterfaceConstants.AUTO_PAY_DATE_COMB_PARM,CmCustomerInterfaceConstants.BLANK_VALUE,transactionId));
                }
            }
            if (!isBlankOrNull(autoPayIns.getString(CmCustomerInterfaceConstants.EXPMONTH_ELE)) && !isBlankOrNull(autoPayIns.getString(CmCustomerInterfaceConstants.EXPYEAR_ELE))) {
                autoPayDateValidation(autoPayIns.getString(CmCustomerInterfaceConstants.EXPMONTH_ELE).trim(), autoPayIns.getString(CmCustomerInterfaceConstants.EXPYEAR_ELE).trim());
            }

        }
    }

	/**
	 * This method validates Expire Month and Year 
	 * of a Autopay list
	 * @param month
	 * @param year
	 */
    private void autoPayDateValidation(String month, String year) {
        try {
            if (Integer.parseInt(year) < getSystemDateTime().getYear()) {
                addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INAVLD_FOR_TRANSNSID,CmCustomerInterfaceConstants.EXP_YEAR_PARAM,CmCustomerInterfaceConstants.BLANK_VALUE,transactionId)); // Exp year is less than current Year
            }
            if (Integer.parseInt(year) == getSystemDateTime().getYear() && Integer.parseInt(month) < getSystemDateTime().getMonth()) {
                addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INAVLD_FOR_TRANSNSID,CmCustomerInterfaceConstants.EXPMONTH_ELE,CmCustomerInterfaceConstants.BLANK_VALUE,transactionId)); // Exp year is less than current Year
             }

        } catch (NumberFormatException e) {
            e.printStackTrace();

        }
    }

	/**
	 * This method validates AccountPersons List 
	 * @param acctPerGrpList
	 * @param acctRelTypeId
	 * @param billRoutId
	 * @param billRoutId
	 * @param acctPerAddrs
	 */
    private void validateAccountPersonsList(COTSInstanceList acctPerGrpList, AccountRelationshipType_Id acctRelTypeId, BillRouteType_Id billRoutId, COTSInstanceNode acctPerAddrs) {
    	//CB-91 - Start Add
        isMainCustomerCount = 0;
        isMainCustomer =  null;
        accountPersonIdType =  null;
        accountPersonIdValue = null;
        
        //Increment counter if account main person is already existing
 		if (!isBlankOrNull(mainPersonIdType) && !isBlankOrNull(mainPersonIdValue)){     				
 			isMainCustomerCount++;
        }			
    	//CB-91 - End Add
		
     	for (COTSInstanceListNode acctPerIns: acctPerGrpList) {    		
    		//CB-91 - Start Add
     		isMainCustomer = acctPerIns.getString(CmCustomerInterfaceConstants.ISMAINCUSTOMER_ELE).trim();
     		accountPersonIdType = acctPerIns.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE).trim();
     		accountPersonIdValue = acctPerIns.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE).trim();
     		
     		//Increment counter if input account person's:
     		//Main Customer Switch  is equal to Y
     		//Id Type and Value is NOT EQUAL to the existing account main person
    		if (isMainCustomer.equals(yesLookupValue)
    				&& (!accountPersonIdType.equals(mainPersonIdType) ||
    				    !accountPersonIdValue.equals(mainPersonIdValue))){
    			isMainCustomerCount++;
    		}
    		
    		//Decrement counter if the input account person's:
    		//Main Customer Switch  is equal to N
    		//Id Type and Value is EQUAL to the existing account main person
    		if (isMainCustomer.equals(noLookupValue)
    				&& accountPersonIdType.equals(mainPersonIdType)
    				&& accountPersonIdValue.equals(mainPersonIdValue)){
    			isMainCustomerCount--;
    		}   	
    		//CB-91 - End Add
			
			//CB-105 - Start Change
            //if (isBlankOrNull(acctPerIns.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE))) {
            //	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.NOTFOUND_FOR_TRANSNSID,CmCustomerInterfaceConstants.ACCOUNT_REL_TYPE_PARAM,transactionId)); // Account Relationship type not given
            //} else {
			if (!isBlankOrNull(acctPerIns.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE))) {
			//CB-105 - End Change
                acctRelTypeId = new AccountRelationshipType_Id(acctPerIns.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE));
                if (isNull(acctRelTypeId.getEntity())) {
                	 addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INAVLD_FOR_TRANSNSID,CmCustomerInterfaceConstants.ACCOUNT_REL_TYPE_PARAM,acctPerIns.getString(CmCustomerInterfaceConstants.ACCOUNTRELATIONSHIPTYPE_ELE),transactionId)); // Exp year is less than current Year
                }
            }

            //CB-70 - Start Change
            //CB-60 - Start Change
            if (isBlankOrNull(acctPerIns.getString(CmCustomerInterfaceConstants.BILLROUTETYPE_ELE))) {
            	 addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.BILL_ROUTE_TYPE_PARAM,transactionId)); 
            		} else {
            //if (!isBlankOrNull(acctPerIns.getString(CmCustomerInterfaceConstants.BILLROUTETYPE_ELE))) {
                billRoutId = new BillRouteType_Id(acctPerIns.getString(CmCustomerInterfaceConstants.BILLROUTETYPE_ELE));

                if (isNull(billRoutId.getEntity())) {
                	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INAVLD_FOR_TRANSNSID,CmCustomerInterfaceConstants.BILL_ROUTE_TYPE_PARAM,acctPerIns.getString(CmCustomerInterfaceConstants.BILLROUTETYPE_ELE),transactionId)); // Exp year is less than current Year
                    }
            }
            //CB-60 - End Change
            //CB-70 - End Change							
                   
            acctPerAddrs = acctPerIns.getGroup(CmCustomerInterfaceConstants.ADDRESS_ELE);
            
            //CB-52 - Start Change
            //if(notNull(acctPerAddrs))
            if(this.getAccountOverrideAllowed().isYes() && notNull(acctPerAddrs))
            //CB-52 - End Change
            {
            	validateAddress(acctPerAddrs, ctryId, stateId);
            }
            
            //CB-256 - Start Change
            //CB-52 - Start Add
            //if(notNull(acctRelTypeId) && acctRelTypeId.getEntity().equals(this.getShipToAccountRelationshipType())){
            //	//Validate Account Address
            //	validateAccountAddress(shipToEffectiveDate);      	
            //}
            //CB-52 - End Add
			billAddressSource =
					notNull(acctPerIns.getLookup(CmCustomerInterfaceConstants.BILLADDRESSSOURCE_ELE))?
							(BillingAddressSourceLookup)acctPerIns.getLookup(CmCustomerInterfaceConstants.BILLADDRESSSOURCE_ELE)
							:null;
			
			if (notNull(billAddressSource) && billAddressSource.equals(BillingAddressSourceLookup.constants.SHIP_TO)
					&& notNull(addressList) && !addressList.isEmpty()){
				validateShipToAddress();
			}
            
          //CB-256 - End Change
    	}
		
    	//CB-91 - Start Add
    	if (isMainCustomerCount != 1){
    		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.ACCOUNT_PERSON_MUST_HAVE_ONE_MAIN_CUST_,transactionId));
    	}
    	//CB-91 - End Add
		
    }

     /**
	 * This method validates Identifiers List
	 * @param idList
	 * @param primaryIdCount
	 * @param idType
	 * @param idValue
	 * @param mainOrAcctIdMissing
	 */ 
    //CB-233 - Start Revert CB-53
    //CB-53 - Start Change
    //private void validateIdList(COTSInstanceList idList, int primaryIdCount, String idType, String idValue, String mainOrAcctIdMissing) {
    //CB-91 - Start Change
    //private void validateIdList(COTSInstanceList idList, int primaryIdCount, String idType, String idValue, String mainOrAcctIdMissing, Bool checkAccount) {
    //private void validateIdList(COTSInstanceList idList, int primaryIdCount, String idType, String idValue, String mainOrAcctIdMissing, Bool checkAccount, Bool getMainCustomer) {
    private void validateIdList(COTSInstanceList idList, int primaryIdCount, String idType, String idValue, String mainOrAcctIdMissing, Bool getMainCustomer) {
    //CB-91 - End Change					
    //CB-53 - End Change 
    //CB-233 - End Revert CB-53
    	for (COTSInstanceListNode personIdenList: idList) {
            if (!isEmptyOrNull(personIdenList.getString(CmCustomerInterfaceConstants.IS_PRIMARY_ELE)) && personIdenList.getString(CmCustomerInterfaceConstants.IS_PRIMARY_ELE).equals(YesNoOptionLookup.constants.YES.trimmedValue())) {
                primaryIdCount++;
                if (primaryIdCount == 1) {
                	//CB-59 - Start Change
                    //if (!isBlankOrNull(personIdenList.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE))) {
                	if (!isBlankOrNull(personIdenList.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE)) && !isBlankOrNull(personIdenList.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE))) {
                    //CB-59 - End Change
                		idType = personIdenList.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE).trim();
                        idValue = personIdenList.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE).trim();
                    } else {
                        if (isBlankOrNull(personIdenList.getString(CmCustomerInterfaceConstants.ID_VALUE_ELE))) {
                       	 addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.PRIM_ID_VAL_PARAM,transactionId)); 
                        }
                    }
                }
            }
        }
    	
        if (primaryIdCount == 0) {
            addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.NOTFOUND_FOR_TRANSNSID,mainOrAcctIdMissing, transactionId));
        } else if (primaryIdCount > 1) {
        	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MULTI_PRIM_TRUE,transactionId));
        } else if (primaryIdCount == 1) {
            validateMultiplePersons(idType, idValue);
            
            //CB-233 - Start Revert CB-53
            //CB-53 - Start Add
            //if (checkAccount.isTrue()){
            //	validateIfPersonAccountExists(idType, idValue);
            //}
            //CB-53 - End Add
          //CB-233 - End Revert CB-53
            
            //CB-91 - Start Add
            if(getMainCustomer.isTrue()){
            	retrieveAccountMainCustomer(idType, idValue);
            }
            //CB-91 - End Add									 
        }
    }  

	/**
	 * This method validates the Division.
	 * @param mainCustCisDiv
	 */
    private void validateCisDivision(String mainCustCisDiv) {
        if (isBlankOrNull(mainCustCisDiv)) {
        	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.DIVISION_PARAM,transactionId));
        } else {
            CisDivision_Id cisDivId = new CisDivision_Id(mainCustCisDiv);
            if (isNull(cisDivId.getEntity())) {
               	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INAVLD_FOR_TRANSNSID,CmCustomerInterfaceConstants.DIVISION_PARAM,mainCustCisDiv,transactionId)); // Exp year is less than current Year
            }
        }
    }

	 /**
	 * This method validates all the address Group data
	 * @param address
	 * @param ctryId
	 * @param stateId
	 */ 
    private void validateAddress(COTSInstanceNode address, Country_Id ctryId, State_Id stateId) {
        
    	if (isBlankOrNull(address.getString(CmCustomerInterfaceConstants.ADDRESS1_ELE))) {
    		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.ADDRESS1_PARAM,transactionId));
    	    }
        if (isBlankOrNull(address.getString(CmCustomerInterfaceConstants.CITY_ELE))) {
        	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.CITY_PARAM,transactionId));
        }
        if (isBlankOrNull(address.getString(CmCustomerInterfaceConstants.STATE_ELE))) {
        	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.STATE_PARAM,transactionId));
        }
        if (isBlankOrNull(address.getString(CmCustomerInterfaceConstants.COUNTRY_ELE))) {
        	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MISSING_FOR_TRANSNSID,CmCustomerInterfaceConstants.COUNTRY_PARAM,transactionId)); 
        	}
        if (!isBlankOrNull(address.getString(CmCustomerInterfaceConstants.COUNTRY_ELE)) && !isBlankOrNull(address.getString(CmCustomerInterfaceConstants.STATE_ELE))) {
            ctryId = new Country_Id(address.getString(CmCustomerInterfaceConstants.COUNTRY_ELE).trim());
            if (isNull(ctryId.getEntity())) {
             	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INAVLD_FOR_TRANSNSID,CmCustomerInterfaceConstants.COUNTRY_PARAM,address.getString(CmCustomerInterfaceConstants.COUNTRY_ELE).trim(),transactionId));  
             	}
            stateId = new State_Id(ctryId, address.getString(CmCustomerInterfaceConstants.STATE_ELE).trim());
            if (isNull(stateId.getEntity())) {
            	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.INVALID_STATE_COUNTRY_COMBINATION,address.getString(CmCustomerInterfaceConstants.STATE_ELE).trim(),address.getString(CmCustomerInterfaceConstants.COUNTRY_ELE).trim(),transactionId));  
            }
        }
    }

    //CB-233 - Start Revert CB-53
    ////CB-53 - Start Add
	///**
	// * This method checks if parent person has an existing account.
	// * @param persons
	// */
	/*private void validateParentPerson(COTSInstanceNode persons){
		
		COTSInstanceList personList = null;
		String primaryIdType;
		String primaryIdValue;
		String personId1Str;
		Person_Id personId1;
		Account account;
   	
		if (notNull(persons)) {
			personList = persons.getList(CmCustomerInterfaceConstants.PERSON_ELE);			
			for (COTSInstanceListNode personListNode: personList) {
				//Initialize
				primaryIdType = CmCustomerInterfaceConstants.BLANK_VALUE;
				primaryIdValue = CmCustomerInterfaceConstants.BLANK_VALUE;
				personId1Str = CmCustomerInterfaceConstants.BLANK_VALUE;
				personId1 = null;
				account = null;
				
				//Primary Id Type				
				if (!isBlankOrNull(personListNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE))){
					primaryIdType = personListNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDTYPE_ELE).trim();
				}
				//Primary Id Value
				if (!isBlankOrNull(personListNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE))){
					primaryIdValue = personListNode.getString(CmCustomerInterfaceConstants.PRIMARYPERSONIDVALUE_ELE).trim();
				}
				//Person Id
				if (!isBlankOrNull(personListNode.getString(CmCustomerInterfaceConstants.PERSONID1_ELE))){
					personId1Str = personListNode.getString(CmCustomerInterfaceConstants.PERSONID1_ELE).trim();
				}
				
				if(isBlankOrNull(personId1Str)){
					//Validate if person account exist using id type and value
					if (!isBlankOrNull(primaryIdType) && !isBlankOrNull(primaryIdValue)){
						validateMultiplePersons(primaryIdType, primaryIdValue);
						validateIfPersonAccountExists(primaryIdType, primaryIdValue);
					}
				}else{
					//Validate if person account exist using parent person id
					personId1 = new Person_Id(personId1Str);					
					if (notNull(personId1.getEntity())){
						account = retrieveAccountOfPersonId(personId1);    		
			    		if (isNull(account)){
			    			addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.ACCOUNT_NOT_FOUND_FOR_PERSONID1,
			    					personId1Str,
			    					transactionId)); 
			    		}						
					}
				}				
			}
		}		
	}*/
	
	///**
	// * This method validates if person has an existing account
	// * @param idType
	// * @param idNbr
	// */
    /*private void validateIfPersonAccountExists(String idType, String idNbr){
    	person = custIntfUtility.fetchPersonById(idType, idNbr, Bool.TRUE);
    	
    	if (notNull(person)){
    		account = retrieveAccountOfPersonId(person.getId());    		
    		if (isNull(account)){
    			addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.ACCOUNT_NOT_FOUND_FOR_ID_TYPE_VALUE,
    					idType,
    					idNbr,
    					transactionId)); 
    		}
    	}   	
    }*/
	
	///**
	// * This method retrieves the Account of the Person Id 
	// * 
	// * @param personId - The Person Id where the Account will be retrieved.
	// * @return Returns the retrieved Account. 
	// *         Returns null if the Person Id has no account
	// */
	/*private Account retrieveAccountOfPersonId(Person_Id personId){
		retrieveAcctSqlString = new StringBuilder();
		retrieveAcctSqlString.append("FROM AccountPerson ap ");
		retrieveAcctSqlString.append("WHERE ap.id.person.id = :personId ");
		
		retrieveAcctQry = createQuery(retrieveAcctSqlString.toString(), "Retrieve account of person");
		retrieveAcctQry.bindId("personId", personId);
		retrieveAcctQry.addResult("account", "ap.id.account");
		
		return retrieveAcctQry.firstRow();
	}*/
	//CB-53 - End Add
    //CB-233 - End Revert CB-53
	
	//CB-59 - Start Add
    /**
     * This method will check if Customer Number Person Identifier Type
     * exists in the person id list
     * @param idList
     */
    private void validateIfCustNumIdTypeExists(COTSInstanceList idList){
    	Bool isCustNumFound = Bool.FALSE;
    	String perIdType;
    	for (COTSInstanceListNode personIdenList: idList) {
    		perIdType = null;
    		
    		if (!isBlankOrNull(personIdenList.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE))){
    			perIdType = personIdenList.getString(CmCustomerInterfaceConstants.ID_TYPE_ELE).trim();
    		}
    		
    		if (!isBlankOrNull(perIdType) && perIdType.equals(getCustNumPerIdType().getId().getIdValue().toString().trim())){
    			isCustNumFound = Bool.TRUE;
    			break;
    		}
    	}
    	
    	if (isCustNumFound.isFalse()){
    		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.CUST_NUM_ID_TYPE_NOT_FOUND,getCustNumPerIdType().getId().getIdValue()));
    	}    	
    }
	//CB-59 - End Add

	/**
	 * This method validates if multiple persons exists
	 * for the provided Id Type.
	 * @param idType
	 * @param idValue
	 */
    private void validateMultiplePersons(String idType, String idValue) {
    	//CB-53 - Start Change
        //List < SQLResultRow > personSqlResRowList = findPersonId(idType, idValue);
        //if (!personSqlResRowList.isEmpty() && (personSqlResRowList.size() > 1) ) {
        personSqlResRowList = findPersonId(idType, idValue);
        if (!personSqlResRowList.isEmpty() && (personSqlResRowList.size() > 1) ) {
        //CB-53 - End Change
        	addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MULTI_PERSONS_ID_TYPE_VALUE,idType, idValue,transactionId));
        }
    }


	 /**
	 * This method returns the Maximum
	 * SeqNo if errorList available 
	 * @param errorList
	 * @return 
	 */
    private Integer fetchMaxSeqNum(COTSInstanceList errorList) {
      	
      	for (COTSInstanceListNode errLst : errorList)
      	{
      		seqIntAry.add(errLst.getNumber(CmCustomerInterfaceConstants.SEQNO_ELE).intValue());
      	}
      	return Collections.max(seqIntAry);
  	}
  

	 /**
	 * This method provides list of person records
	 * for given idType and idNumber
	 * @param idType
	 * @param idNbr
	 * @return 
	 */    
    private List < SQLResultRow > findPersonId(String idType, String idNbr) {
        PreparedStatement personQuery = null;
        List < SQLResultRow > resultRow = null;
        try {
            personQuery = createPreparedStatement(CmCustomerInterfaceConstants.GET_PER_BY_PRIM_ID, "Person Query");
            personQuery.bindBoolean("true", Bool.TRUE);
            personQuery.setAutoclose(false);
            personQuery.bindId("idType", new IdType_Id(idType));
            personQuery.bindStringProperty("personIdNumber", PersonId.properties.personIdNumber, idNbr);
            resultRow = personQuery.list();
        } finally {
            if (personQuery != null) {
                personQuery.close();
                personQuery = null;
            }
        }
        return resultRow;
    }
	
    /**
	 * This method adds serverMessages into the errorLogList
	 * @param serverMessage
	 */  
   private void addErrorLogEntry(ServerMessage serverMessage) {
       errorLogList.add(serverMessage);

   }
   
   //Start Add - CB-91
   private void retrieveAccountMainCustomer(String idType, String idNbr){
	   Account inputAccount = custIntfUtility.fetchAccountById(idType, idNbr, Bool.TRUE);
	   AccountPerson accountPerson;
	   PersonId personId;
	   Person mainPerson = null;
	   mainPersonIdType = null;
	   mainPersonIdValue = null;

	   //Determine main customer account person  
		if(notNull(inputAccount)){
			Iterator<AccountPerson> accountPersonIter = inputAccount.getPersons().iterator();
	        while(accountPersonIter.hasNext()) {
	        	accountPerson = accountPersonIter.next();
	            if(accountPerson.getIsMainCustomer().isTrue()) {
	            	mainPerson = accountPerson.fetchIdPerson();
	                break;
	            }
	        } 
		}
		
		//Determine person id type and value of main customer
		if(notNull(mainPerson)) {
			Iterator<PersonId> personIdIter = mainPerson.getIds().iterator();
	        while(personIdIter.hasNext()) {
	        	personId = personIdIter.next();
	            if(personId.getIsPrimaryId().isTrue()) {
	            	mainPersonIdType = personId.fetchIdIdType().getId().getIdValue().trim();
	                mainPersonIdValue = personId.getPersonIdNumber().trim();
	                break;
	            }
	        } 						
		}
   }
   //End Add - CB-91
   
   //Start Add - CB-52
	/**
	 * This method checks if the Characteristic Type is valid for an Entity.
	 * @param Characteristic Type to Validate
	 * @param Entity to be checked on
	 * @param Description of the Soft Parameter
	 */
	private void validateCharacteristicType(CharacteristicType charType, CharacteristicEntityLookup charEntLkup,
			String parmDesc){
		CharacteristicEntity_Id charEntityId = new CharacteristicEntity_Id(charType, charEntLkup);
		
		if(isNull(charEntityId.getEntity())){			
			addError(CmMessageRepository.getServerMessage(CmMessages.CHAR_TYPE_INVALID_FOR_ENTITY,
					parmDesc,charType.getId().getIdValue(),
					charEntLkup.getLookupValue().fetchLanguageDescription()));
		}
	}
	
	/**
	 * This method retrieves person using email.
	 * @param emailAddress
	 * @return person
	 */
	private Person retrievePersonByEmail(String email){
		//Initialize
		Person person = null;
		PreparedStatement personQuery= null;
		SQLResultRow personIdResultRow = null;
		int personCount = 0;
		
		try {
			personQuery = createPreparedStatement(CmCustomerInterfaceConstants.GET_PER_BY_EMAIL,"Person Query");
			personQuery.setAutoclose(false);
			personQuery.bindStringProperty("email", Person.properties.emailAddress, email);
			
			if(notNull(personQuery.list())){
				personCount = personQuery.list().size();
				
				//If multiple persons found, log an error.
				if(personCount > 1){
					addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MULT_PERSON_FOR_EMAIL_FOUND,email));
				}
				
				//If only one person is found, retrieve first row.
				if(personCount == 1){
					personIdResultRow = personQuery.firstRow();
				}
			}	
			
			//Retrieve Person entity
			if(notNull(personIdResultRow)){
				person =  personIdResultRow.getEntity("PER_ID", Person.class);
			}
		} finally {
			if(notNull(personQuery)){
				personQuery.close();
				personQuery = null;
			}
		}		
		
		return person;
	}
	    
    /**
     * This method validates Person Address
     */
	//public void validatePersonAddress(Date addressEffDate){
	public void validateBillToAddress(){

		//Start Change - CB-256
    	//validateCustomerAddress(this.getBillToAddressType(),this.getAddressBillToIndicatorCharType(),addressEffDate);
		validateCustomerAddress(this.getBillToAddressType(),this.getAddressBillToIndicatorCharType());
		//End Change - CB-256
    	    	
    	//If no Bill To Address Type and Primary Bill To Address Indicator is found, log an error.
   	 	if (noAddressRecordFound.isTrue()){
   	 		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.PRIM_BILL_TO_ADDRESS_MISSING,transactionId));
   	 	}
	 
   	 	//If multiple Bill To Address Type and Primary Bill To Address Indicator is found, log an error
   	 	if (multipleAddressRecordFound.isTrue()){
   	 		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MULT_PRIM_BILL_TO_ADDRESS_FOUND,transactionId));
   	 	}
   	 	
   	 	//Validate Address
   	 	for (COTSInstanceNode addressListNode: addressList){
   	 		validateAddress(addressListNode, ctryId, stateId);
   	 	} 	 	
    }
    
    /**
     * This method validates Account Address
     */
    //public void validateAccountAddress(Date addressEffDate){
	public void validateShipToAddress(){
    	//Start Change - CB-256
    	//validateCustomerAddress(this.getShipToAddressType(),this.getAddressShipToIndicatorCharType(), addressEffDate);
		validateCustomerAddress(this.getShipToAddressType(),this.getAddressShipToIndicatorCharType());
		//End Change - CB-256
    	
    	//If no Ship To Address Type and Primary Ship To Address Indicator is found, log an error.
   	 	if (noAddressRecordFound.isTrue()){
   	 		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.PRIM_SHIP_TO_ADDRESS_MISSING,transactionId));
   	 	}
	 
   	 	//If multiple Ship To Address Type and Primary Ship To Address Indicator is found, log an error
   	 	if (multipleAddressRecordFound.isTrue()){
   	 		addErrorLogEntry(CmMessageRepository.getServerMessage(CmMessages.MULT_PRIM_SHIP_TO_ADDRESS_FOUND,transactionId));
   	 	} 
    }
    
	/**
	 * This method validates customer address.
	 * @param addressType
	 * @param addressIndicatorCharType
	 */
	//Start Change - CB-256
    //private void validateCustomerAddress(AddressTypeFlgLookup addressType, CharacteristicType addressIndicatorCharType, Date addressEffDate) {
	private void validateCustomerAddress(AddressTypeFlgLookup addressType, CharacteristicType addressIndicatorCharType) {
	//End Change - CB-256
    	//Initialize
		//Start Delete - CB-256
    	//Date inputEffectiveDate = null;
		//End Delete - CB-256
    	addressTypeEntityList = null;
    	noAddressRecordFound = Bool.FALSE;
    	multipleAddressRecordFound = Bool.FALSE;
    	//Start Delete - CB-256
    	//isSameEffectiveDate = Bool.FALSE;
    	//End Delete - CB-256
    	primaryAddressIndicatorCount = 0;
    	addressTypeCount = 0;
    	
    	//Start Add - CB-177
    	Address currentAddress;
    	EntityFlagLookup entityFlag;
    	//End Add - CB-177
    	
    	for (COTSInstanceNode addressListNode: addressList){   		
    		//Retrieve Address Entities and Characteristics
    		addressEntity = addressListNode.getGroup(CmCustomerInterfaceConstants.ADDRESS_ENTITIES_ELE);
    		addressEntityList = addressEntity.getList(CmCustomerInterfaceConstants.ADDRESS_ENTITY_ELE);
    		addressCharacteristics = addressListNode.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);  	
    		addressCharacteristicList = addressCharacteristics.getList(CmCustomerInterfaceConstants.CHARACTERISTIC_ELE);
    		
        	//Retrieve Address Type Count
        	addressTypeEntityList = addressEntityList.getElementsWhere("[addressType = '"+ addressType.getLookupValue().fetchIdFieldValue() +"' ]");
        	addressTypeCount = addressTypeCount + addressTypeEntityList.size();
    	
        	//Retrieve Primary Address Indicator Characteristic Count
        	for (COTSInstanceListNode addressCharacteristicListNode: addressCharacteristicList) {
        		if (addressCharacteristicListNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressIndicatorCharType.getId().getIdValue())
        				&& addressCharacteristicListNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE).equals(CmCustomerInterfaceConstants.PRIMARY)){
        			primaryAddressIndicatorCount++;
        		}
        	}
    	}
    	
    	//Start Change - CB-256
      	/*//NOTE: If Address Effective Date is null,this means that person does not have the Bill To/Ship To Address Type yet.
    	if (isNull(addressEffDate)){
        	//If no record found, log an error.
        	if (addressTypeCount == 0 || primaryAddressIndicatorCount == 0){
        		noAddressRecordFound = Bool.TRUE;
        	}
        	
        	//If multiple record found, log an error.
        	//Start Change - CB-256
        	//if (addressTypeCount > 1 || primaryAddressIndicatorCount > 1){
        	if (addressTypeCount > 1 && primaryAddressIndicatorCount > 1){
        	//End Change - CB-256
        		multipleAddressRecordFound = Bool.TRUE;
        	}
    	}else{
        	addressTypeEntityList = null;
        	isSameEffectiveDate = Bool.FALSE;
        	
    		//If Bill To/Ship To is found from input, check that effective date is different from existing Bill To/Ship To Address of person
        	//Start Change - CB-256
        	//if (addressTypeCount > 0 || primaryAddressIndicatorCount > 0){
        	if (addressTypeCount > 0 && primaryAddressIndicatorCount > 0){
        	//End Change - CB-256
        		
            	//Loop thru Address Characteristics and check if effective date as with input
            	for (COTSInstanceNode addressListNode: addressList){
            		addressCharacteristics = addressListNode.getGroup(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);  	
            		addressCharacteristicList = addressCharacteristics.getList(CmCustomerInterfaceConstants.CHARACTERISTIC_ELE);
            		
                	for (COTSInstanceListNode addressCharacteristicListNode: addressCharacteristicList) {         
                		inputEffectiveDate = notNull(addressCharacteristicListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE)) ?
                				addressCharacteristicListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE) : mainCustomerEffectiveDate;              		
                		if (addressCharacteristicListNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressIndicatorCharType.getId().getIdValue())
                				&& addressCharacteristicListNode.getString(CmCustomerInterfaceConstants.CHARACTERISTICVALUE_ELE).equals(CmCustomerInterfaceConstants.PRIMARY)
                				&& inputEffectiveDate.equals(addressEffDate)){
                			isSameEffectiveDate = Bool.TRUE;
                			break;
                		}
                	}
            	}
            	
            	//If existing Bill To/Ship To Address Effective Date is same with the input, set multipleAddressRecordFound to true.
            	if (isSameEffectiveDate.isTrue()){
            		multipleAddressRecordFound = Bool.TRUE;
            	}     		
        	}
    	}*/
    	
    	//If no record found, check if this is a new person
    	if (addressTypeCount == 0 || primaryAddressIndicatorCount == 0){
    		
    		//If person is new, set noAddressRecordFound to true
    		if (isNewPerson.isTrue()){
    			noAddressRecordFound = Bool.TRUE;
    		//Otherwise, check if existing person already has a primary address
    		}else{
    			//Start Change - CB-177
    			//If person has no existing primary address, set noAddressRecordFound to true
    			//if (isPersonHasPrimaryAddress(person,addressType,addressIndicatorCharType).isFalse()){
    			//	noAddressRecordFound = Bool.TRUE;
    			//}
    	    	for (COTSInstanceNode addressListNode: addressList){   		
    	    		//Retrieve Address Entities and Characteristics
    	    		addressEntity = addressListNode.getGroup(CmCustomerInterfaceConstants.ADDRESS_ENTITIES_ELE);
    	    		addressEntityList = addressEntity.getList(CmCustomerInterfaceConstants.ADDRESS_ENTITY_ELE);
        			for (COTSInstanceNode addressEntityListNode : addressEntityList){
        	    		currentAddress = null;
        				entityFlag = notNull(addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ENTITY_FLG_ELE)) ?
        						(EntityFlagLookup) addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ENTITY_FLG_ELE) : EntityFlagLookup.constants.PERSON;

        				//Retrieve existing Address associated with input Effective Date, Address Type, and Person
        	    		currentAddress = getEffectiveAddressEntity(person.getId().getIdValue(), 
        						(AddressTypeFlgLookup) addressEntityListNode.getLookup(CmCustomerInterfaceConstants.ADDRESS_TYPE_ELE), 
        						entityFlag,
        						addressEntityListNode.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE));
        				
        				if (notNull(currentAddress) 
        						&& isPersonHasPrimaryAddress(person,addressType,addressIndicatorCharType,currentAddress).isFalse()){
        					noAddressRecordFound = Bool.TRUE;
        					break;
        				}
        			}  	    		
    	    	}
    	    	//End Change - CB-177
    		}   		
    	}
    	
    	//If multiple record found, log an error.
    	if (addressTypeCount > 1 && primaryAddressIndicatorCount > 1){
    		multipleAddressRecordFound = Bool.TRUE;
    	}
    	//End Change - CB-256
    }
    
	//Start Change - CB-256
	///**
	// * This method retrieves effective address of person
	// * @param person
	// * @return address
	// */
	/*private Address getEffectiveAddressEntity(Person person, AddressTypeFlgLookup addressType){
		Query<Address> getAddressQry = createQuery(
				"FROM Address address, " +
				"     AddressEntity addressEntity, " +
				"WHERE address.id = addressEntity.id.address " +
				"  AND addressEntity.id.collectionEntityId = :entityId " +
				"  AND addressEntity.id.addressTypeFlg = :addressTypeFlag " +
				"  AND addressEntity.id.entityType = :entityType " +
				"  AND addressEntity.id.effectiveDate = ( " +
				"		SELECT MAX(addressEntity2.id.effectiveDate) " +
				"		FROM AddressEntity addressEntity2 " +
				"		WHERE addressEntity2.id.address = addressEntity.id.address " +
				"		AND addressEntity2.id.collectionEntityId = addressEntity.id.collectionEntityId " +
				"		AND addressEntity2.id.effectiveDate <= :processDate " +
				"  ) " , "");
		
		getAddressQry.bindStringProperty("entityId", AddressEntity.properties.collectionEntityId, person.getId().getIdValue());
		getAddressQry.bindLookup("addressTypeFlag", addressType);
		getAddressQry.bindLookup("entityType", EntityFlagLookup.constants.PERSON);
		getAddressQry.bindDate("processDate", this.getProcessDateTime().getDate());
		getAddressQry.addResult("address", "address");
		
		return getAddressQry.firstRow();
	}*/
	
	/**
	 * This method determine if person has existing primary bill to/ship to address
	 * @param person
	 * @return address
	 */
	//Start Change - CB-177
	//private Bool isPersonHasPrimaryAddress(Person person, AddressTypeFlgLookup addressType, CharacteristicType addressCharType){
	private Bool isPersonHasPrimaryAddress(Person person, AddressTypeFlgLookup addressType, CharacteristicType addressCharType, Address address){
		//Query<Address> getAddressQry = createQuery(
		getAddressQry = null;
		getAddressQry = createQuery(
	//End Change - CB-177	
				"FROM AddressEntity addressEntity, " +
				"     AddressCharacteristic addressChar " +
				"WHERE addressEntity.id.address = addressChar.id.address " +
				//Start Add - CB-177
				"  AND addressEntity.id.address <> :address " +				
				//End Add - CB-177
				"  AND addressChar.id.characteristicType = :addressCharType " +
				"  AND addressChar.searchCharacteristicValue= :primary " +
				"  AND addressEntity.id.collectionEntityId = :entityId " +
				"  AND addressEntity.id.addressTypeFlg = :addressTypeFlag " +
				"  AND addressEntity.id.entityType = :entityType ", "");
		
		//Start Add - CB-177
		getAddressQry.bindEntity("address", address);
		//End Add - CB-177
		getAddressQry.bindEntity("addressCharType", addressCharType);
		getAddressQry.bindStringProperty("primary", AddressCharacteristic.properties.searchCharacteristicValue, CmCustomerInterfaceConstants.PRIMARY);
		getAddressQry.bindStringProperty("entityId", AddressEntity.properties.collectionEntityId, person.getId().getIdValue());
		getAddressQry.bindLookup("addressTypeFlag", addressType);
		getAddressQry.bindLookup("entityType", EntityFlagLookup.constants.PERSON);
		getAddressQry.addResult("addressEntity", "addressEntity");
		
		if (notNull(getAddressQry.firstRow())){
			return Bool.TRUE;
		}else{
			return Bool.FALSE;
		}
	}
	//End Change - CB-256
	//End Add - CB-52
	
	//Start Add - CB-177
	/**
	 * This method retrieves effective address of person
	 * @param person
	 * @return address
	 */
	private Address getEffectiveAddressEntity(String entityId, AddressTypeFlgLookup addressType, EntityFlagLookup entityFlag, Date effectiveDate){		
		stringBuilder = null;
		stringBuilder = new StringBuilder();
		stringBuilder.append("FROM");
		stringBuilder.append(" Address address, ");
		stringBuilder.append(" AddressEntity addressEntity ");
		stringBuilder.append("WHERE");
		stringBuilder.append(" address.id = addressEntity.id.address ");
		stringBuilder.append(" AND addressEntity.id.collectionEntityId = :entityId ");
		stringBuilder.append(" AND addressEntity.id.addressTypeFlg = :addressTypeFlag ");
		stringBuilder.append(" AND addressEntity.id.entityType = :entityType ");
		
		if (isNull(effectiveDate)){
			stringBuilder.append(" AND addressEntity.id.effectiveDate = ( ");
			stringBuilder.append("     SELECT MAX(addressEntity2.id.effectiveDate) ");
			stringBuilder.append("     FROM AddressEntity addressEntity2 ");
			stringBuilder.append("     WHERE addressEntity2.id.address = addressEntity.id.address ");
			stringBuilder.append("     AND addressEntity2.id.collectionEntityId = addressEntity.id.collectionEntityId ");
			stringBuilder.append("     AND addressEntity2.id.effectiveDate <= :processDate) ");
		}else{
			stringBuilder.append(" AND addressEntity.id.effectiveDate = :effectiveDate ");
		}	
		
		getEffectiveAddressQry = null;
		getEffectiveAddressQry = createQuery(stringBuilder.toString(), "");			
		getEffectiveAddressQry.bindStringProperty("entityId", AddressEntity.properties.collectionEntityId, entityId);
		getEffectiveAddressQry.bindLookup("addressTypeFlag", addressType);
		getEffectiveAddressQry.bindLookup("entityType", entityFlag);	
		
		if (isNull(effectiveDate)){
			getEffectiveAddressQry.bindDate("processDate", this.getProcessDateTime().getDate());
		}else{
			getEffectiveAddressQry.bindDate("effectiveDate", effectiveDate);
		}
			
		getEffectiveAddressQry.addResult("address", "address");
		
		return getEffectiveAddressQry.firstRow();
	}
	//Start Add - CB-177
	
}