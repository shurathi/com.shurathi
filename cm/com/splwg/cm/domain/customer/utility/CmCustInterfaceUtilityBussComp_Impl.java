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
 * Process Customer Interface
 *
 * This Business Component will retrieve and retruns all required configuration objects to create
 *   - Person
 *   - Account
 *   - Contract
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-04-13   VLaksh              CB-10. Initial Version. 
 ***********************************************************************
 */
package com.splwg.cm.domain.customer.utility;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.Query;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.maintenanceObject.MaintenanceObjectLogHelper;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.StandardMessages;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.lookup.LookupField_Id;
import com.splwg.base.domain.common.lookup.LookupValue;
import com.splwg.base.domain.common.lookup.LookupValue_Id;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject;
import com.splwg.base.support.schema.BusinessObjectInfo;
import com.splwg.base.support.schema.BusinessObjectInfoCache;
import com.splwg.base.support.schema.ExtendedLookupValueInfo;
import com.splwg.ccb.api.lookup.BillFormatLookup;
import com.splwg.ccb.api.lookup.BillingAddressSourceLookup;
import com.splwg.ccb.api.lookup.NameTypeLookup;
import com.splwg.ccb.api.lookup.PersonOrBusinessLookup;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonId;
import com.splwg.ccb.domain.interfaces.inboundMessage.InboundMessage;
import com.splwg.ccb.domain.interfaces.inboundMessage.InboundMessageLog_DTO;
import com.splwg.ccb.domain.interfaces.inboundMessage.InboundMessageLog_Id;
import com.splwg.shared.common.ServerMessage;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author venka
 *
@BusinessComponent (customizationCallable = true)
 */
public class CmCustInterfaceUtilityBussComp_Impl extends GenericBusinessComponent
		implements CmCustInterfaceUtilityBussComp {
	
	private static final Logger logger = LoggerFactory.getLogger(CmCustInterfaceUtilityBussComp_Impl.class);
	
	
	public Document fetchActiveExtendedLookupDocument(String boName,String lookupName){
		Document extLookupDocument = null;
		 BusinessObjectInfo boInfo = BusinessObjectInfoCache.getRequiredBusinessObjectInfo(boName);
		 if(!isBlankOrNull(lookupName)){
			 ExtendedLookupValueInfo extendedLookupValueInfo = boInfo.getExtendedLookupValue(lookupName);
			 if(notNull(extendedLookupValueInfo)&&extendedLookupValueInfo.getLookupUsage().isActive()){
				 extLookupDocument =  extendedLookupValueInfo.getXMLRepresentation();
			 }
		 }else{
			 for(ExtendedLookupValueInfo extendedLookupValueInfo: boInfo.getActiveExtendedLookupValues()){
				 extLookupDocument = extendedLookupValueInfo.getXMLRepresentation();
				 if(notNull(extLookupDocument))
					 break;
			}
		 }
		 //extLookupDocument.asXML();
		return extLookupDocument;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes", "null" })
	public void addLogEntries(ServerMessage exceptionMessage,InboundMessage inboundMessage,LogEntryTypeLookup lookup,MaintenanceObject maintenanceObject) {
		   if (maintenanceObject == null) {
		      return;
		   }
		   MaintenanceObjectLogHelper logHelper = new MaintenanceObjectLogHelper(maintenanceObject, inboundMessage);
		   logHelper.addLogEntry(lookup, exceptionMessage, null);
		}
		
		@SuppressWarnings({ "null", "unchecked", "rawtypes" })
		public void addLogEntries(ServerMessage message,InboundMessage inbMsg,LogEntryTypeLookup lookup,MaintenanceObject mo,CharacteristicType charType,String charValue) {
		   if (mo == null) {
		      addError(StandardMessages.recordNotFoundError(mo.getId()));
		   }
		   MaintenanceObjectLogHelper logHelper = new MaintenanceObjectLogHelper(mo, inbMsg);
		   logHelper.addLogEntry(lookup, message, null,charType,charValue);
		}
		
		
		public void addFkCharLogEntries(ServerMessage message,InboundMessage inbMsg,LogEntryTypeLookup lookup,MaintenanceObject mo,CharacteristicType charType,String charValue) {
		   	BigInteger seq=getMaxSeq(inbMsg);
		   	InboundMessageLog_DTO inboundLogDTO= new InboundMessageLog_DTO();
		   	InboundMessageLog_Id inboundLogID = new InboundMessageLog_Id(inbMsg, seq.add(BigInteger.ONE));
			inboundLogDTO.setCharacteristicTypeId(charType.getId());
			inboundLogDTO.setCharacteristicValueForeignKey1(charValue);
			inboundLogDTO.setLogDateTime(getProcessDateTime());
			inboundLogDTO.setLogEntryType(LogEntryTypeLookup.constants.SYSTEM);
			inboundLogDTO.setMessageId(message.getMessageId());
			inboundLogDTO.setId(inboundLogID);
			inboundLogDTO.newEntity();
			
		}
		
		
		private BigInteger getMaxSeq(InboundMessage inbMsg) {
			BigInteger seq=BigInteger.ZERO;
			Query<BigInteger>query = createQuery(" from InboundMessageLog lg where lg.id.inboundMessage=:reqId ","");
			query.bindId("reqId",inbMsg.getId());
			query.addResult("seq", "max(lg.id.sequence)");
			if(notNull(query.firstRow())){
				seq = query.firstRow();
			}
			return seq;
		}
		
		
		public Person fetchPersonById(String idType,String idValue,Bool isPrimaryId){
			Person person = null;
			PreparedStatement personQuery= null;
			try {
				if(isPrimaryId.isTrue()){
					personQuery = createPreparedStatement(CmCustomerInterfaceConstants.GET_PER_BY_PRIM_ID,"Person Query");
					personQuery.bindBoolean("true", Bool.TRUE);
				}else{
					personQuery = createPreparedStatement(CmCustomerInterfaceConstants.GET_PER_BY_ID,"Person Query");
				}
				personQuery.setAutoclose(false);
				personQuery.bindId("idType", new IdType_Id(idType));
				personQuery.bindStringProperty("personIdNumber", PersonId.properties.personIdNumber, idValue);
				
				SQLResultRow personIdResultRow = personQuery.firstRow();
				
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
		
		
		public Account fetchAccountById(String idType,String idValue,Bool isPrimaryId){
			Account account = null;
			/*String GET_ACCT_BY_PRIM_ID =" from AccountNumber a where a.id.accountIdentifierType=:idType "
					+ " and a.accountNumber=:accountNumber and a.isPrimaryId='Y' ";
			
			String GET_ACCT_BY_ID =" from AccountNumber a where a.id.accountIdentifierType=:idType "
					+ " and a.accountNumber=:accountNumber  ";
			
			Query<QueryResultRow> accountQuery= null;
			if(isPrimaryId.isTrue()){
				accountQuery = createQuery(GET_ACCT_BY_PRIM_ID,"Account Query");
			}else{
				accountQuery = createQuery(GET_ACCT_BY_ID,"Account Query");
			}
			accountQuery.bindId("idType", new AccountNumberType_Id(idType));
			accountQuery.bindStringProperty("accountNumber", AccountNumber.properties.accountNumber, idValue);
			accountQuery.addResult("account", "a.id.account");
			
			QueryResultRow accountResultRow = accountQuery.firstRow();
			
			if(notNull(accountResultRow)){
				account =  accountResultRow.getEntity("PER_ID", Account.class);
			}*/
			
	
					
			PreparedStatement accountQuery= null;
			try {
				if(isPrimaryId.isTrue()){
					accountQuery = createPreparedStatement(CmCustomerInterfaceConstants.GET_ACCT_BY_PRIM_ID,"account Query");
					accountQuery.bindBoolean("true", Bool.TRUE);
				}else{
					accountQuery = createPreparedStatement(CmCustomerInterfaceConstants.GET_ACCT_BY_ID,"account Query");
				}
				accountQuery.setAutoclose(false);
				accountQuery.bindId("idType", new IdType_Id(idType));
				accountQuery.bindStringProperty("accountNumber", AccountNumber.properties.accountNumber, idValue);
				
				SQLResultRow accountResultRow = accountQuery.firstRow();
				
				if(notNull(accountResultRow)){
					account =  accountResultRow.getEntity("ACCT_ID", Account.class);
				}
			} finally {
				if(notNull(accountQuery)){
					accountQuery.close();
					accountQuery = null;
				}
			}
			
			return account;
		}

}
