/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory LLC; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory LLC.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * Validate Address Characteristics
 *
 * This audit algorithm validates Address Characteristics during Add, 
 * Update and Delete
 *  
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:           Reason:
 * 2020-07-17   JFerna        CB-177. Initial
 ***********************************************************************
 */

package com.splwg.cm.domain.customer.businessObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.splwg.base.api.Query;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessObject.SchemaInstanceChanges;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.EffectiveStatusLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.AuditBusinessObjectAlgorithmSpot;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.cm.api.lookup.AddressTypeFlgLookup;
import com.splwg.cm.api.lookup.BillingAddressSourceLookup;
import com.splwg.cm.api.lookup.StatementAddressSourceLookup;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.api.lookup.EntityFlagLookup;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.account.AccountPersonRouting;
import com.splwg.ccb.domain.customerinfo.account.AccountPersonRouting_DTO;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson_Id;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.address.Address;
import com.splwg.ccb.domain.customerinfo.address.AddressCharacteristic;
import com.splwg.ccb.domain.customerinfo.address.AddressEntity;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstruct;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstruct_DTO;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.cm.domain.customer.utility.CmCustomerInterfaceConstants;
import com.splwg.shared.common.ServerMessage;

/**
 * @author JFerna
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = characteristicType, name = addressBillToIndicatorCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = addressBillToIndicatorCharVal, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressShipToIndicatorCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = addressShipToIndicatorCharVal, required = true, type = string)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = addressStatementIndicatorCharType, type = entity)
 *            , @AlgorithmSoftParameter (name = addressStatementIndicatorCharVal, type = string)})
 */
public class CmValidateAddressCharacteristicsAlgComp_Impl extends
		CmValidateAddressCharacteristicsAlgComp_Gen implements
		AuditBusinessObjectAlgorithmSpot {

	//Hard Parameters
	private BusinessObjectActionLookup boAction;
	private BusinessObjectInstance newBo;
	private BusinessObjectInstance oldBo;
	
	//Soft Parameters
	private CharacteristicType addressBillToIndicatorCharType;
	private String addressBillToIndicatorCharVal;
	private CharacteristicType addressShipToIndicatorCharType;
	private String addressShipToIndicatorCharVal;
	private CharacteristicType addressStatementIndicatorCharType;
	private String addressStatementIndicatorCharVal;
		
	//Work Parameters
	private String addressBillToIndicatorCharTypeStr;
	private String addressShipToIndicatorCharTypeStr;
	private String addressStatementIndicatorCharTypeStr;
	private COTSInstanceList addressEntitiesList;
	private COTSInstanceList origAddressCharList;
	private COTSInstanceList newAddressCharList;
	private String perIdStr = CmCustomerInterfaceConstants.BLANK_VALUE;	
	private Person_Id personId = null;
	private Person person = null;
	private Address currentAddress = null;
	private AddressTypeFlgLookup currentAddressType = null;
	
	/**
	 * Validate Soft Parameters
	 * @param forAlgorithmValidation Boolean
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {
    	//Retrieve Algorithm Parameter Descriptions
    	String addressBillToIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(CmCustomerInterfaceConstants.PARM_AUD_IDX_ADDR_BILLTO_CHAR_TYPE).fetchLanguageParameterLabel().trim();
    	String addressShipToIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(CmCustomerInterfaceConstants.PARM_AUD_IDX_ADDR_SHIPTO_CHAR_TYPE).fetchLanguageParameterLabel().trim();
    	String addressStatementIndicatorCharTypeDesc = getAlgorithm().getAlgorithmType().getParameterAt(CmCustomerInterfaceConstants.PARM_AUD_IDX_ADDR_STM_CHAR_TYPE).fetchLanguageParameterLabel().trim();  
    	String addressStatementIndicatorCharValDesc = getAlgorithm().getAlgorithmType().getParameterAt(CmCustomerInterfaceConstants.PARM_AUD_IDX_ADDR_STM_CHAR_VAL).fetchLanguageParameterLabel().trim();
    	
    	//Retrieve Soft Parameters
    	addressBillToIndicatorCharType = this.getAddressBillToIndicatorCharType();
    	addressBillToIndicatorCharVal = this.getAddressBillToIndicatorCharVal();
    	addressShipToIndicatorCharType = this.getAddressShipToIndicatorCharType();
    	addressShipToIndicatorCharVal = this.getAddressShipToIndicatorCharVal();
    	addressStatementIndicatorCharType = this.getAddressStatementIndicatorCharType();
    	addressStatementIndicatorCharVal = this.getAddressStatementIndicatorCharVal();
    	
    	//Validate Bill To Address Characteristic Type and Value
    	validateCharacteristicType(addressBillToIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressBillToIndicatorCharTypeDesc);
    	validateCharValForCharType(addressBillToIndicatorCharType,addressBillToIndicatorCharVal);
    	addressBillToIndicatorCharTypeStr = addressBillToIndicatorCharType.getId().getIdValue();
    			
    	//Validate Ship To Address Characteristic Type and Value
    	validateCharacteristicType(addressShipToIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressShipToIndicatorCharTypeDesc);
    	validateCharValForCharType(addressShipToIndicatorCharType,addressShipToIndicatorCharVal);
    	addressShipToIndicatorCharTypeStr = addressShipToIndicatorCharType.getId().getIdValue();
    	
    	//Validate Statement Indicator Address Characteristic Type and Value
    	if (notNull(addressStatementIndicatorCharType) && isBlankOrNull(addressStatementIndicatorCharVal)){
			addError(CmMessageRepository.getServerMessage(CmMessages.BOTH_PARM_MUST_BE_PROVIDED,
					addressStatementIndicatorCharTypeDesc,
					addressStatementIndicatorCharValDesc));
    	}
    	
    	if (isNull(addressStatementIndicatorCharType) && !isBlankOrNull(addressStatementIndicatorCharVal)){
			addError(CmMessageRepository.getServerMessage(CmMessages.BOTH_PARM_MUST_BE_PROVIDED,
					addressStatementIndicatorCharTypeDesc,
					addressStatementIndicatorCharValDesc));
    	}
    	
    	if (notNull(addressStatementIndicatorCharType) && !isBlankOrNull(addressStatementIndicatorCharVal)){
        	validateCharacteristicType(addressStatementIndicatorCharType,CharacteristicEntityLookup.constants.ADDRESS_CHARACTERISTIC,addressStatementIndicatorCharTypeDesc);    
        	validateCharValForCharType(addressStatementIndicatorCharType,addressStatementIndicatorCharVal);
        	addressStatementIndicatorCharTypeStr = addressStatementIndicatorCharType.getId().getIdValue();
    	}
	}
	
	/**
	 * Main Processing
	 */
	public void invoke() {
		
		if (boAction.isAdd() || boAction.isFastAdd() || boAction.isUpdate() || boAction.isFastUpdate()){
			//Retrieve Address
			currentAddress = notNull(newBo.getEntity(CmCustomerInterfaceConstants.REQUEST_BO_ELE,Address.class)) ?
					newBo.getEntity(CmCustomerInterfaceConstants.REQUEST_BO_ELE,Address.class) : null;
					
			//Retrieve Address Entities
			COTSInstanceNode addressEntitiesGroup = newBo.getGroupFromPath(CmCustomerInterfaceConstants.ADDRESS_ENTITIES_BO_ELE);
			addressEntitiesList = addressEntitiesGroup.getList(CmCustomerInterfaceConstants.ADDRESS_ENTITY_BO_ELE);
			
			//Retrieve Entity Id
			List<COTSInstanceNode> addressEntityList = addressEntitiesList.getElementsWhere("[entityType = '"+ EntityFlagLookup.constants.PERSON.getLookupValue().fetchIdFieldValue() +"' ]");;
			COTSInstanceNode addressEntityNode = null;
			
			if (notNull(addressEntityList) && !addressEntityList.isEmpty()){
				addressEntityNode = addressEntityList.get(0);
				perIdStr = addressEntityNode.getString(CmCustomerInterfaceConstants.COLL_ENTITY_ID_BO_ELE);			
				personId = !isBlankOrNull(perIdStr) ? new Person_Id(perIdStr) : null;
				person = notNull(personId) ? personId.getEntity() : null;				
				currentAddressType = (AddressTypeFlgLookup) addressEntityNode.getLookup(CmCustomerInterfaceConstants.ADDRESS_TYPE_CD_BO_ELE);
			}
			
			//If no person found, stop processing.
			if(isNull(person)){
				return;
			}
			
			//Retrieve New Address Characteristics
			COTSInstanceNode newAddressCharGroup = newBo.getGroupFromPath(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);
			newAddressCharList = newAddressCharGroup.getList(CmCustomerInterfaceConstants.ADDRESS_CHARS_BO_ELE);
			
			//Validate Address Characteristics during ADD
			if(boAction.isAdd() || boAction.isFastAdd()){
				Bool isPrimaryBillToAddress = Bool.FALSE;
				Bool isPrimaryShipToAddress = Bool.FALSE;
				Bool isStatementIndicatorAddress = Bool.FALSE;
				
				for (COTSInstanceNode addressChar : newAddressCharList){
					//Bill To
					if(addressChar.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressBillToIndicatorCharTypeStr)
						&& addressChar.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressBillToIndicatorCharVal)){
						isPrimaryBillToAddress = Bool.TRUE;					
					}
										
					//Ship To
					if(addressChar.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressShipToIndicatorCharTypeStr)
						&& addressChar.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressShipToIndicatorCharVal)){
						isPrimaryShipToAddress = Bool.TRUE;
					}
								
					//Statement Indicator
					if(notNull(addressStatementIndicatorCharType) && !isBlankOrNull(addressStatementIndicatorCharVal)){
						if (addressChar.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressStatementIndicatorCharTypeStr)
								&& addressChar.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressStatementIndicatorCharVal)){
							isStatementIndicatorAddress = Bool.TRUE;						
						}
					}									
				}
				
				if(isPrimaryBillToAddress.isTrue()){
					Address primaryBillToAddress = getPrimaryPersonAddress(person, addressBillToIndicatorCharType, addressBillToIndicatorCharVal);	
					if(notNull(primaryBillToAddress)){
						addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_BILL_TO_ADDRESS_ERROR,primaryBillToAddress.getId().getIdValue(),perIdStr));
					}
					
					//Retrieve Bill To Account Persons of Entity Id
					List<AccountPerson> accountPersons = getPersonAccounts(person, BillingAddressSourceLookup.constants.BILL_TO);
					Iterator<AccountPersonRouting> apRoutingIter = null;
					AccountPersonRouting_DTO apRoutingDto = null;
					AccountPersonRouting apRouting = null;
					
					//Loop thru account persons bill routing and update address id
					for (AccountPerson accountPerson : accountPersons){
						if (!accountPerson.getAccountPersonRouting().isEmpty()){
							apRoutingIter = accountPerson.getAccountPersonRouting().iterator();
							while (apRoutingIter.hasNext()) {
								apRouting = apRoutingIter.next();
								apRoutingDto = apRouting.getDTO();
								if (apRoutingDto.getBillAddressSource().equals(BillingAddressSourceLookup.constants.BILL_TO)){
									apRoutingDto.setAddressId(currentAddress.getId().getIdValue());
								}
								apRouting.setDTO(apRoutingDto);
							}
						}							
					}
					
					//Retrieve Bill To Statement Construct of Person
					StatementConstruct stmCons = retrieveStatementConstruct(person);
		   		 
			    	//Update Bill To Address Id of the Statement Construct
			    	if (notNull(stmCons)){
			    		StatementConstruct_DTO stmConsDto = stmCons.getDTO();
			    		if (stmConsDto.getStatementAddressSource().equals(StatementAddressSourceLookup.constants.BILL_TO)){
				    		stmConsDto.setAddressId(currentAddress.getId());
			    		}
			    		stmCons.setDTO(stmConsDto);
			    	}
				}
				
				if(isPrimaryShipToAddress.isTrue()){
					Address primaryShipToAddress = getPrimaryPersonAddress(person, addressShipToIndicatorCharType, addressShipToIndicatorCharVal);	
					if(notNull(primaryShipToAddress)){
						addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_SHIP_TO_ADDRESS_ERROR,primaryShipToAddress.getId().getIdValue(),perIdStr));
					}
					
					//Retrieve Ship To Account Persons of Entity Id
					List<AccountPerson> accountPersons = getPersonAccounts(person, BillingAddressSourceLookup.constants.SHIP_TO);
					Iterator<AccountPersonRouting> apRoutingIter = null;
					AccountPersonRouting_DTO apRoutingDto = null;
					AccountPersonRouting apRouting = null;
					
					//Loop thru account persons bill routing and update address id
					for (AccountPerson accountPerson : accountPersons){
						if (!accountPerson.getAccountPersonRouting().isEmpty()){
							apRoutingIter = accountPerson.getAccountPersonRouting().iterator();
							while (apRoutingIter.hasNext()) {
								apRouting = apRoutingIter.next();
								apRoutingDto = apRouting.getDTO();
								if (apRoutingDto.getBillAddressSource().equals(BillingAddressSourceLookup.constants.SHIP_TO)){
									apRoutingDto.setAddressId(currentAddress.getId().getIdValue());
								}
								apRouting.setDTO(apRoutingDto);
							}
						}							
					}					
				}
				
				if(isStatementIndicatorAddress.isTrue()){
					Address statementIndicatorAddress = getPrimaryPersonAddress(person, addressStatementIndicatorCharType, addressStatementIndicatorCharVal);	
					if(notNull(statementIndicatorAddress)){
						addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_STM_IND_ADDRESS_ERROR,statementIndicatorAddress.getId().getIdValue(),perIdStr));
					}
					
					//Retrieve Statement Construct associated to Entity Id
					StatementConstruct stmConsForUpdatedAddress = retrieveStatementConstruct(person);
		   		 
			    	//Update Address Id of the Statement Construct
			    	if (notNull(stmConsForUpdatedAddress)){
			    		StatementConstruct_DTO stmConsForUpdatedAddressDto = stmConsForUpdatedAddress.getDTO();
			    		if (currentAddressType.equals(AddressTypeFlgLookup.constants.BILL_TO)){
			    			stmConsForUpdatedAddressDto.setStatementAddressSource(StatementAddressSourceLookup.constants.BILL_TO);
			    		}else if(currentAddressType.equals(AddressTypeFlgLookup.constants.SHIP_TO)){
			    			stmConsForUpdatedAddressDto.setStatementAddressSource(StatementAddressSourceLookup.constants.SHIP_TO);
			    		}else{
			    			stmConsForUpdatedAddressDto.setStatementAddressSource(StatementAddressSourceLookup.constants.PERSON);
			    		}	    		
			    		stmConsForUpdatedAddressDto.setAddressId(currentAddress.getId());
			    		stmConsForUpdatedAddress.setDTO(stmConsForUpdatedAddressDto);
			    	}				
				}			
			}
			
			//Validate Address Characteristics during Update
			if(boAction.isUpdate() || boAction.isFastUpdate()){
				
				//Retrieve Original Address Characteristics
				COTSInstanceNode origAddressCharGroup = oldBo.getGroupFromPath(CmCustomerInterfaceConstants.CHARACTERISTICS_ELE);
				origAddressCharList = origAddressCharGroup.getList(CmCustomerInterfaceConstants.ADDRESS_CHARS_BO_ELE);
						
				//Check if there are changes between original and new address characteristics			
				Bool isNoUpdate = Bool.FALSE;
				Bool isCharUpdateFound = Bool.FALSE;
				for (COTSInstanceNode origAddressChar : origAddressCharList){
					isNoUpdate = Bool.FALSE;
					for (COTSInstanceNode newAddressChar : newAddressCharList){
						if (newAddressChar.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE).equals(origAddressChar.getDate(CmCustomerInterfaceConstants.EFFECTIVE_DATE_ELE))
								&& newAddressChar.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE)
								&& newAddressChar.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(origAddressChar.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE))){
							isNoUpdate = Bool.TRUE;
							break;
						}
					}
					
					if (isNoUpdate.isFalse()){
						isCharUpdateFound = Bool.TRUE;
						break;
					}
				}
				
				//If there are changes between original and new address characteristics, 
				//validate deleted and updated values.
				if (isCharUpdateFound.isTrue()){				
					validateDeletedAddressChars();				
					validateUpdatedAddressChars();										
				}
			}
		}				
	}
	
	/**
	 * This method validates if Bill To, Ship To, and Statement Indicator
	 * address characteristics are deleted.
	 */
	private void validateDeletedAddressChars(){
		//Initialize
		Bool origBillToExist = Bool.FALSE;
		Bool origShipToExist = Bool.FALSE;
		Bool origStatementIndicatorExist = Bool.FALSE;
		Bool newBillToExist = Bool.FALSE;
		Bool newShipToExist = Bool.FALSE;
		Bool newStatementIndicatorExist = Bool.FALSE;
		
		//Loop thru original address characteristics and check if Bill To, Ship To, 
		//and Statement Indicator Address Characteristics exist
		for (COTSInstanceNode origAddressChar2 : origAddressCharList){						
			//Bill To
    		if (origAddressChar2.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressBillToIndicatorCharTypeStr)
    				&& origAddressChar2.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressBillToIndicatorCharVal)){
    			origBillToExist = Bool.TRUE;        			
    		}
    		
    		//Ship To
    		if (origAddressChar2.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressShipToIndicatorCharTypeStr)
    				&& origAddressChar2.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressShipToIndicatorCharVal)){
    			origShipToExist = Bool.TRUE;        			
    		}
    		
    		//Statement Indicator
    		if (notNull(addressStatementIndicatorCharType) && !isBlankOrNull(addressStatementIndicatorCharVal) 
    				&& origAddressChar2.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressStatementIndicatorCharTypeStr)
    				&& origAddressChar2.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressStatementIndicatorCharVal)){
	    			origStatementIndicatorExist = Bool.TRUE;        							    		
    		}
		}
		
		//Loop thru new address characteristics and check if Bill To, Ship To, 
		//and Statement Indicator Address Characteristics exist
		for (COTSInstanceNode newAddressChar2 : newAddressCharList){						
			//Bill To
    		if (newAddressChar2.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressBillToIndicatorCharTypeStr)
    				&& newAddressChar2.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressBillToIndicatorCharVal)){
    			newBillToExist = Bool.TRUE;        			
    		}
    		
    		//Ship To
    		if (newAddressChar2.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressShipToIndicatorCharTypeStr)
    				&& newAddressChar2.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressShipToIndicatorCharVal)){
    			newShipToExist = Bool.TRUE;        			
    		}
    		
    		//Statement Indicator
    		if (notNull(addressStatementIndicatorCharType) && !isBlankOrNull(addressStatementIndicatorCharVal) 
    				&& newAddressChar2.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressStatementIndicatorCharTypeStr)
    				&& newAddressChar2.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressStatementIndicatorCharVal)){
	    			newStatementIndicatorExist = Bool.TRUE;        							    		
    		}
		}
		
		//Raise Warnings if Primary Bill To, Ship To, and/or Statement Indicator is deleted
		//If Bill To Characteristic exist from the original bo and deleted from the new bo, raise a warning.
		if (origBillToExist.isTrue() && newBillToExist.isFalse()){
			if(isOnlineConnection() && areWarningsEnabled()){
				addWarning(CmMessageRepository.getServerMessage(CmMessages.NO_PRIM_BILL_TO_ADDRESS_WARNING,perIdStr));
			}
		}
				
		//If Ship To Characteristic exist from the original bo and deleted from the new bo, raise a warning.
		if (origShipToExist.isTrue() && newShipToExist.isFalse()){
			if(isOnlineConnection() && areWarningsEnabled()){
				addWarning(CmMessageRepository.getServerMessage(CmMessages.NO_PRIM_SHIP_TO_ADDRESS_WARNING,perIdStr));
			}
		}
		
		//If Statement Indicator Char Type and Value are provided, check if Statement Indicator exist from the original bo
		//and deleted from the new bo. If yes, raise a warning.
		if (notNull(addressStatementIndicatorCharType) && !isBlankOrNull(addressStatementIndicatorCharVal)
				&& origStatementIndicatorExist.isTrue() && newStatementIndicatorExist.isFalse()){
			
			//If session is online and warnings are enabled, raise a warning
			if(isOnlineConnection()){
				
				if (areWarningsEnabled()){
					addWarning(CmMessageRepository.getServerMessage(CmMessages.NO_STATEMENT_INDICATOR_ADDRESS_WARNING,perIdStr));
				}
									
				//If OK, retrieve Primary Bill To Address associated to Entity Id
				List<ServerMessage> warningList = SessionHolder.getSession().getWarnings();
				if (isNull(warningList) || warningList.isEmpty()) {						
					Address billToAddress = getPrimaryPersonAddress(person,addressBillToIndicatorCharType,addressBillToIndicatorCharVal);
					
					//If no Primary Bill To Address is found, raise an error.
					if(isNull(billToAddress)){
				    	addError(CmMessageRepository.getServerMessage(CmMessages.NO_PRIM_BILL_TO_ADDRESS_ERROR,perIdStr));
				    //Otherwise, retrieve Statement Construct associated with Entity Id
					}else{
						StatementConstruct stmCons = retrieveStatementConstruct(person);
				    		 
				    	//Update Address Id of the Statement Construct using Primary Bill To Address Id
				    	if (notNull(stmCons)){
				    		StatementConstruct_DTO stmConsDto = stmCons.getDTO();
				    		stmConsDto.setStatementAddressSource(StatementAddressSourceLookup.constants.BILL_TO);
				    		stmConsDto.setAddressId(billToAddress.getId());
				    		stmCons.setDTO(stmConsDto);
				    	}
				    }
				}						
			}
		}
	}
	
	/**
	 * This method validates if Bill To, Ship To, and Statement Indicator
	 * address characteristics are updated.
	 */
	private void validateUpdatedAddressChars(){
		//Initialize Variables
		Bool updatedBillToAddressCharFound = Bool.FALSE;
		Bool updatedShipToAddressCharFound = Bool.FALSE;
		Bool updatedStmIndAddressCharFound = Bool.FALSE;
		Bool newBillToAddressCharFound = Bool.FALSE;
		Bool newShipToAddressCharFound = Bool.FALSE;
		Bool newStmIndAddressCharFound = Bool.FALSE;
		Address billToAddress;
		Address shipToAddress;
		Address stmIndToAddress;
		
		//Check if new Bill To Characteristic is being added
		List<COTSInstanceNode> origBillToAddressCharList = origAddressCharList.getElementsWhere("[characteristicType = '"+ addressBillToIndicatorCharTypeStr +"' ]");
		List<COTSInstanceNode> newBillToAddressCharList = newAddressCharList.getElementsWhere("[characteristicType = '"+ addressBillToIndicatorCharTypeStr +"' ]");
		if (origBillToAddressCharList.size() == 0 && newBillToAddressCharList.size() > 0){
			newBillToAddressCharFound = Bool.TRUE;
		}

		//Check if new Ship To Characteristic is being added
		List<COTSInstanceNode> origShipToAddressCharList = origAddressCharList.getElementsWhere("[characteristicType = '"+ addressShipToIndicatorCharTypeStr +"' ]");
		List<COTSInstanceNode> newShipToAddressCharList = newAddressCharList.getElementsWhere("[characteristicType = '"+ addressShipToIndicatorCharTypeStr +"' ]");
		if (origShipToAddressCharList.size() == 0 && newShipToAddressCharList.size() > 0){
			newShipToAddressCharFound = Bool.TRUE;
		}
		
		//Check if new Statement Indicator Characteristic is being added
		List<COTSInstanceNode> origStatementIndicatorAddressCharList = origAddressCharList.getElementsWhere("[characteristicType = '"+ addressStatementIndicatorCharTypeStr +"' ]");
		List<COTSInstanceNode> newStatementIndicatorAddressCharList = newAddressCharList.getElementsWhere("[characteristicType = '"+ addressStatementIndicatorCharTypeStr +"' ]");
		if (origStatementIndicatorAddressCharList.size() == 0 && newStatementIndicatorAddressCharList.size() > 0){
			newStmIndAddressCharFound = Bool.TRUE;
		}
		
		//Loop thru old and new address characteristics and check if Bill To, Ship To, 
		//and Statement Indicator Address Characteristics are updated
		for (COTSInstanceNode origAddressChar3 : origAddressCharList){

			updatedBillToAddressCharFound = Bool.FALSE;
			updatedShipToAddressCharFound = Bool.FALSE;
			updatedStmIndAddressCharFound = Bool.FALSE;
			
			for (COTSInstanceNode newAddressChar3 : newAddressCharList){
				
				if (origAddressChar3.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(newAddressChar3.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE))
						&& !origAddressChar3.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(newAddressChar3.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE))){
					
					//Bill To
					if(newAddressChar3.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressBillToIndicatorCharTypeStr)
						&& newAddressChar3.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressBillToIndicatorCharVal)){
						updatedBillToAddressCharFound = Bool.TRUE;
						newBillToAddressCharFound = Bool.FALSE;
					}
										
					//Ship To
					if(newAddressChar3.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressShipToIndicatorCharTypeStr)
						&& newAddressChar3.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressShipToIndicatorCharVal)){
						updatedShipToAddressCharFound = Bool.TRUE;
						newShipToAddressCharFound = Bool.FALSE;
					}
								
					//Statement Indicator
					if(notNull(addressStatementIndicatorCharType) && !isBlankOrNull(addressStatementIndicatorCharVal)){
						if (newAddressChar3.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressStatementIndicatorCharTypeStr)
								&& newAddressChar3.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressStatementIndicatorCharVal)){
							updatedStmIndAddressCharFound = Bool.TRUE;	
							newStmIndAddressCharFound = Bool.FALSE;
						}
					}					
				}
			}
		}
				
		//If a Bill To Address Characteristic is being added or updated as a primary,
		//check if there is already an existing Primary Bill To Address Characteristic
		if(newBillToAddressCharFound.isTrue() || updatedBillToAddressCharFound.isTrue()){
			//Retrieve existing Primary Bill To Address
			billToAddress = getPrimaryPersonAddress(person, this.getAddressBillToIndicatorCharType(), this.getAddressBillToIndicatorCharVal());
			
			//FOR UPDATE: If existing Primary Bill To Address Characteristic is found, raise an error.
			if (updatedBillToAddressCharFound.isTrue() && notNull(billToAddress)){
				addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_BILL_TO_ADDRESS_ERROR,billToAddress.getId().getIdValue(),perIdStr));
			}

			//FOR ADD: If existing Primary Bill To Address Characteristic is found, raise an error.
			if (newBillToAddressCharFound.isTrue()){
				for (COTSInstanceNode newAddressChar4 : newAddressCharList){
					if(newAddressChar4.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressBillToIndicatorCharTypeStr)
						&& newAddressChar4.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressBillToIndicatorCharVal)
						&& notNull(billToAddress)){
						//Raise an error
						addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_BILL_TO_ADDRESS_ERROR,billToAddress.getId().getIdValue(),perIdStr));					
					}
				}
			}
			
			//Retrieve Account Persons of Person
			List<AccountPerson> accountPersons = getPersonAccounts(person, BillingAddressSourceLookup.constants.BILL_TO);
			Iterator<AccountPersonRouting> apRoutingIter = null;
			AccountPersonRouting_DTO apRoutingDto = null;
			AccountPersonRouting apRouting = null;
			
			//Loop thru account persons and bill routing and update address id
			for (AccountPerson accountPerson : accountPersons){
				if (!accountPerson.getAccountPersonRouting().isEmpty()){
					apRoutingIter = accountPerson.getAccountPersonRouting().iterator();
					while (apRoutingIter.hasNext()) {
						apRouting = apRoutingIter.next();
						apRoutingDto = apRouting.getDTO();
						if (apRoutingDto.getBillAddressSource().equals(BillingAddressSourceLookup.constants.BILL_TO)){
							apRoutingDto.setAddressId(currentAddress.getId().getIdValue());
						}
						apRouting.setDTO(apRoutingDto);
					}
				}							
			}
			
			//Retrieve Bill To Statement Construct of Person
			StatementConstruct stmCons = retrieveStatementConstruct(person);
   		 
	    	//Update Bill To Address Id of the Statement Construct
	    	if (notNull(stmCons)){
	    		StatementConstruct_DTO stmConsDto = stmCons.getDTO();
	    		if (stmConsDto.getStatementAddressSource().equals(StatementAddressSourceLookup.constants.BILL_TO)){
		    		stmConsDto.setAddressId(currentAddress.getId());
	    		}
	    		stmCons.setDTO(stmConsDto);
	    	}
		}

		//If a Ship To Address Characteristic is being added or updated as a primary,
		//check if there is already an existing Primary Ship To Address Characteristic
		if(newShipToAddressCharFound.isTrue() || updatedShipToAddressCharFound.isTrue()){
			//Retrieve existing Primary Ship To Address
			shipToAddress = getPrimaryPersonAddress(person, this.getAddressShipToIndicatorCharType(), this.getAddressShipToIndicatorCharVal());				
			
			//FOR UPDATE: If existing Primary Ship To Address Characteristic is found, raise an error.
			if (updatedShipToAddressCharFound.isTrue() && notNull(shipToAddress)){
				addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_SHIP_TO_ADDRESS_ERROR,shipToAddress.getId().getIdValue(),perIdStr));
			}
			
			//FOR Add: If existing Primary Ship To Address Characteristic is found, raise an error.
			if (newBillToAddressCharFound.isTrue()){
				for (COTSInstanceNode newAddressChar5 : newAddressCharList){
					if(newAddressChar5.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressShipToIndicatorCharTypeStr)
						&& newAddressChar5.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressShipToIndicatorCharVal)
						&& notNull(shipToAddress)){
						//Raise an error
						addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_SHIP_TO_ADDRESS_ERROR,shipToAddress.getId().getIdValue(),perIdStr));					
					}
				}
			}
			
			//Retrieve Account Persons of Person
			List<AccountPerson> accountPersons = getPersonAccounts(person, BillingAddressSourceLookup.constants.SHIP_TO);
			Iterator<AccountPersonRouting> apRoutingIter = null;
			AccountPersonRouting_DTO apRoutingDto = null;
			AccountPersonRouting apRouting = null;
			
			//Loop thru account persons and update address id
			for (AccountPerson accountPerson : accountPersons){
				if (!accountPerson.getAccountPersonRouting().isEmpty()){
					apRoutingIter = accountPerson.getAccountPersonRouting().iterator();
					while (apRoutingIter.hasNext()) {
						apRouting = apRoutingIter.next();
						apRoutingDto = apRouting.getDTO();
						if (apRoutingDto.getBillAddressSource().equals(BillingAddressSourceLookup.constants.SHIP_TO)){
							apRoutingDto.setAddressId(currentAddress.getId().getIdValue());
						}
						apRouting.setDTO(apRoutingDto);
					}
				}							
			}					
		}
		
		//If a Statement Indicator Address Characteristic is being added or updated as a primary,
		//check if there is already an existing Statement Indicator Address Characteristic		
		if(newStmIndAddressCharFound.isTrue() || updatedStmIndAddressCharFound.isTrue()){		
			//Retrieve existing Primary Ship To Address
			stmIndToAddress = getPrimaryPersonAddress(person, this.getAddressStatementIndicatorCharType(), this.getAddressStatementIndicatorCharVal());				
			
			//FOR UPDATE: If existing Statement Indicator Address Characteristic is found, raise an error.
			if (updatedStmIndAddressCharFound.isTrue() && notNull(stmIndToAddress)){
				addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_STM_IND_ADDRESS_ERROR,stmIndToAddress.getId().getIdValue(),perIdStr));
			}
			
			//FOR ADD: If existing Statement Indicator Address Characteristic is found, raise an error.
			if (newStmIndAddressCharFound.isTrue()){
				for (COTSInstanceNode newAddressChar6 : newAddressCharList){
					if(newAddressChar6.getString(CmCustomerInterfaceConstants.CHARACTERISTICTYPE_ELE).equals(addressStatementIndicatorCharTypeStr)
						&& newAddressChar6.getString(CmCustomerInterfaceConstants.SRCH_CHAR_VAL_BO_ELE).equals(addressStatementIndicatorCharVal)
						&& notNull(stmIndToAddress)){
						//Raise an error
						addError(CmMessageRepository.getServerMessage(CmMessages.EXISTING_PRIM_STM_IND_ADDRESS_ERROR,stmIndToAddress.getId().getIdValue(),perIdStr));					
					}
				}
			}
			
			//Retrieve Statement Construct associated to Entity Id
			StatementConstruct stmConsForUpdatedAddress = retrieveStatementConstruct(person);
   		 
	    	//Update Address Id of the Statement Construct
	    	if (notNull(stmConsForUpdatedAddress)){
	    		StatementConstruct_DTO stmConsForUpdatedAddressDto = stmConsForUpdatedAddress.getDTO();
	    		if (currentAddressType.equals(AddressTypeFlgLookup.constants.BILL_TO)){
	    			stmConsForUpdatedAddressDto.setStatementAddressSource(StatementAddressSourceLookup.constants.BILL_TO);
	    		}else if(currentAddressType.equals(AddressTypeFlgLookup.constants.SHIP_TO)){
	    			stmConsForUpdatedAddressDto.setStatementAddressSource(StatementAddressSourceLookup.constants.SHIP_TO);
	    		}else{
	    			stmConsForUpdatedAddressDto.setStatementAddressSource(StatementAddressSourceLookup.constants.PERSON);
	    		}	    		
	    		stmConsForUpdatedAddressDto.setAddressId(currentAddress.getId());
	    		stmConsForUpdatedAddress.setDTO(stmConsForUpdatedAddressDto);
	    	}
		}
	}
		
	/**
	 * This method retrieves list of account person
	 * @param person
	 * @param billAddressSOurce
	 * @return List of Account Person
	 */
	private List<AccountPerson> getPersonAccounts(Person person, BillingAddressSourceLookup billAddressSOurce){
	     String accountIdStr;
	     String personIdStr;
	     Account_Id accountId = null;
	     Person_Id personId = null;
	     AccountPerson_Id accountPersonId = null;
	     List<AccountPerson> accountPersonList = new ArrayList<AccountPerson>();
		 List<SQLResultRow> resultRowList = new ArrayList<SQLResultRow>();
		 
		 StringBuilder strBuilder = new StringBuilder();
	     strBuilder.append(" SELECT AP.ACCT_ID, AP.PER_ID ");
	     strBuilder.append(" FROM CI_ACCT_PER AP ");
	     strBuilder.append(" WHERE AP.PER_ID = :perId ");
	     strBuilder.append("   AND AP.BILL_ADDR_SRCE_FLG = :addressSource ");
	     
	     PreparedStatement acctPerQuery = createPreparedStatement(strBuilder.toString(), "");
	     acctPerQuery.bindId("perId", person.getId());
	     acctPerQuery.bindLookup("addressSource", billAddressSOurce);
	     
	     resultRowList = acctPerQuery.list();
	     acctPerQuery.close();
	     
	     for (SQLResultRow resultRow: resultRowList){
	    	 accountIdStr = resultRow.getString("ACCT_ID");
	    	 accountId = new Account_Id(accountIdStr);
	    	 
	    	 personIdStr = resultRow.getString("PER_ID");
	    	 personId = new Person_Id(personIdStr);
	    	
	    	 accountPersonId = new AccountPerson_Id(personId,accountId);
	    	 if (notNull(accountPersonId.getEntity())){
	    		 accountPersonList.add(accountPersonId.getEntity());
	    	 }	    	 
	     }
	     
	     return accountPersonList;	     		
	}
	
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
	 * This method checks if Characteristic Value if valid for Characteristic Type
	 * @param Characteristic Type to be checked on
	 * @param Characteristic Value to validate
	 */
	private void validateCharValForCharType(CharacteristicType charType, String charVal){
		CharacteristicValue_Id charValueId = new CharacteristicValue_Id(charType, charVal);
		if(isNull(charValueId.getEntity())) {			
			addError(CmMessageRepository.getServerMessage(CmMessages.CHAR_VAL_INVALID_FOR_CHAR_TYPE,
					charVal,
					charType.getId().getIdValue()));
		}
	}
	
	/**
	 * This method retrieves active Statement Construct of Person
	 * @param person
	 * @return Statement Construct 
	 */
	private StatementConstruct retrieveStatementConstruct(Person person){
		Query<StatementConstruct> getStatementConstructQry = createQuery(
				"FROM StatementConstruct stmCons, " +
				"WHERE stmCons.person = :person " +
				"AND stmCons.effectiveStatus = :effectiveStatus ","");
		
		getStatementConstructQry.bindEntity("person", person);
		getStatementConstructQry.bindLookup("effectiveStatus", EffectiveStatusLookup.constants.ACTIVE);
		getStatementConstructQry.addResult("stmCons", "stmCons");
		
		return getStatementConstructQry.firstRow();
	}
	
	/**
	 * This method retrieves Primary Bill To/Ship To/Statement Indicator Person Address
	 * @param person
	 * @param addressCharType
	 * @param addressCharVal
	 * @return Address
	 */
	private Address getPrimaryPersonAddress(Person person, CharacteristicType addressCharType, String addressCharVal){
		Query<Address> getAddressQry = createQuery(
				"FROM Address address, " +
				"     AddressEntity addressEntity, " +
				"     AddressCharacteristic addressChar " +
				"WHERE address.id <> :addressId " +
				"  AND address.id = addressEntity.id.address " +
				"  AND addressEntity.id.address = addressChar.id.address " +
				"  AND addressChar.id.characteristicType = :addressCharType " +
				"  AND addressChar.searchCharacteristicValue= :addressCharVal " +
				"  AND addressEntity.id.collectionEntityId = :entityId " +
				"  AND addressEntity.id.entityType = :entityType ", "");
		
		getAddressQry.bindId("addressId", currentAddress.getId());
		getAddressQry.bindEntity("addressCharType", addressCharType);
		getAddressQry.bindStringProperty("addressCharVal", AddressCharacteristic.properties.searchCharacteristicValue, addressCharVal);
		getAddressQry.bindStringProperty("entityId", AddressEntity.properties.collectionEntityId, person.getId().getIdValue());
		getAddressQry.bindLookup("entityType", EntityFlagLookup.constants.PERSON);
		getAddressQry.addResult("address", "address");
		
		return getAddressQry.firstRow();
	}

	/**
	 * This method sets BO Action Flag
	 * @param boAction
	 */
	public void setAction(BusinessObjectActionLookup boAction) {
		this.boAction = boAction;
	}
	
	/**
	 * This method sets New Business Object
	 * @param boRequest
	 */
	public void setNewBusinessObject(BusinessObjectInstance boRequest) {
		this.newBo = boRequest;
	}

	/**
	 * This method sets Original Business Object
	 * @param boRequest
	 */
	public void setOriginalBusinessObject(BusinessObjectInstance boRequest) {
		this.oldBo = boRequest;
	}

	/**
	 * This method sets Business Object
	 * @param bo
	 */
	public void setBusinessObject(BusinessObject bo) {

	}

	/**
	 * This method sets Business Object Key
	 * @param boKey
	 */
	public void setBusinessObjectKey(BusinessObjectInstanceKey boKey) {

	}

	/**
	 * This method sets Schema Instance Changes
	 * @param changes
	 */
	public void setChangedValues(SchemaInstanceChanges changes) {

	}

	/**
	 * This method sets Entity Id
	 * @param id
	 */
	@SuppressWarnings("rawtypes")
	public void setEntityId(EntityId id) {
		
	}

	/**
	 * This method sets Maintenance Object
	 * @param mo
	 */
	public void setMaintenanceObject(MaintenanceObject mo) {

	}
}
