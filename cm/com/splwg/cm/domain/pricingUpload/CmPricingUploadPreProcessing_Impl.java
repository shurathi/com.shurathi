/*	
 **************************************************************************                                                                
 *                            	     Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                                    
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This is a custom file request type pre-processing algorithm 
 * to perform below operations. 
 * 1. Identify Party Type Flag (ACCT, PERS, PLST) and pass this node to Service Script
 * 2. If Acct, Pers or PLST is not present based on the PartyIdValue input throw an error
 * 
 * ************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-08-11   SSapre		Initial Version
 * 2020-09-16   SAnart		CB-387-ORMB238 - Pricing Upload: Repricing/Pricing Update
 **************************************************************************
 */

package com.splwg.cm.domain.pricingUpload;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Id;
import com.splwg.base.api.datatypes.StringId;
import com.splwg.base.api.datatypes.TimeInterval;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.StandardMessages;
import com.splwg.ccb.api.lookup.PaOwnerTypeFlagLookup;
import com.splwg.ccb.api.lookup.PriceStatusFlagLookup;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.dataManagement.fileRequest.CompositeKeyBean_Per;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileRequestPreProcessingAlgorithmSpot;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileTransformationRecord;
import com.splwg.ccb.domain.pricing.priceassign.PriceAsgn_DTO;
import com.splwg.ccb.domain.pricing.priceassign.PriceAsgn_Id;
import com.splwg.ccb.domain.pricing.pricelist.PriceList_Id;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;


/**
 * @author ShreyasSapre
 *
@AlgorithmComponent ()
 */
public class CmPricingUploadPreProcessing_Impl extends
		CmPricingUploadPreProcessing_Gen implements
		FileRequestPreProcessingAlgorithmSpot {

	private String recordPayLoad;
	private String servicePayload;
	private static final String ACCT_PARTY = "ACCT";
	private static final String PERS_PARTY = "PERS";
	private static final String PLST_PARTY = "PLST";
	private static final String BLANK = "";
	private static final String ID_VAL_OP_NODE = "<idValue>";
	private static final String ID_VAL_CL_NODE = "</idValue>";
	private static final String PARTY_TYPE_OP_NODE = "<partyType>";
	private static final String PARTY_TYPE_CL_NODE = "</partyType>";
	private static final String CM_CreatePA_NODE = "CM-CreatePA";
	private static final String PRICE_ITEM_CD_NODE = "priceItemCode";
	private static final String START_DATE_NODE = "startDate";
	private static final String PRICE_ASGN_ID_NODE = "priceAsgnId";
	private static final String OVERIDE_SW_NODE = "overideSw";
	private static final String PA_OWNER_TYPE_FLAG_NODE = "paOwnerTypeFlag";
	private static final String OVERIDE_SW_VAL="Y";
	
	
	@Override
	public void invoke() 
	{
		String requestIdValue = StringUtils.substringBetween(recordPayLoad, ID_VAL_OP_NODE, ID_VAL_CL_NODE);
		String requestPartyType = StringUtils.substringBetween(recordPayLoad, PARTY_TYPE_OP_NODE, PARTY_TYPE_CL_NODE);
		
		try
		{
			//If Party Type is ACCT--> then 
			//find Account Id using Account Number --> if present map to Price Assignment BS Account Id else throw error
			if(requestPartyType.trim().equals(ACCT_PARTY))
			{
				String accountId = getAccountIdFromAcctNbr(requestIdValue);
				if(!isBlankOrNull(accountId))
				{
					//Add this node to Service Script CM-CreatePA root level
					Document servicePayloadDoc = DocumentHelper.parseText(servicePayload.trim());
					servicePayloadDoc.getRootElement().addElement("accountId").setText(accountId);
					
					//CB-387-ADD-START
					
					 Account_Id accid=new Account_Id(accountId.trim());
					 servicePayload= createServicePayload( servicePayloadDoc, accid,ACCT_PARTY);
					
					 //CB-387-ADD-END 
					 
				}
				else
					addError(CmMessageRepository.accountNotFound(requestPartyType, requestIdValue));
			}
			else if(requestPartyType.trim().equals(PERS_PARTY))
			{
				String personId = getPersonIdFromPersonIdNbr(requestIdValue);
				if(!isBlankOrNull(personId))
				{
					//Add this node to Service Script CM-CreatePA root level
					Document servicePayloadDoc = DocumentHelper.parseText(servicePayload);
					servicePayloadDoc.getRootElement().addElement("personId").setText(personId);
					
					//CB-387-ADD-START
					
					 Person_Id perId=new Person_Id(personId.trim());					 
					 servicePayload= createServicePayload( servicePayloadDoc, perId, PERS_PARTY);
					
					//CB-387-ADD-END 
					 
				}
				else
					addError(CmMessageRepository.personNotFound(requestPartyType, requestIdValue));
			}
			else if(requestPartyType.trim().equals(PLST_PARTY))
			{
				String priceListId = getPriceListIdFromPriceListDescr(requestIdValue);
				if(!isBlankOrNull(priceListId))
				{
					//Add this node to Service Script CM-CreatePA root level
					Document servicePayloadDoc = DocumentHelper.parseText(servicePayload);
					servicePayloadDoc.getRootElement().addElement("priceListId").setText(priceListId);
					
					//CB-387-ADD-START
					
					 PriceList_Id plistId=new PriceList_Id(priceListId);
					 servicePayload= createServicePayload( servicePayloadDoc, plistId, PLST_PARTY);
				
					//CB-387-ADD-END
				}
				else
					addError(CmMessageRepository.priceListNotFound(requestPartyType, requestIdValue));
			}
		}
		catch (DocumentException e) 
		{
			addError(StandardMessages.systemError(e));
		}
	}

	/**
	 * This method will be used to get Price List Id from Price List description
	 * @param priceListDescr
	 * @return priceListId
	 */
	private String getPriceListIdFromPriceListDescr(String priceListDescr) 
	{
		String priceListId = BLANK;
		
		PreparedStatement getPriceListIdFromPriceListDescrPrepStmt = null;
		StringBuilder getPriceListIdFromPriceListDescrQuery = new StringBuilder()
		.append("SELECT PRICELIST_ID FROM CI_PRICELIST_L WHERE DESCR=:priceListDescr");
		
		try
		{
			getPriceListIdFromPriceListDescrPrepStmt =  createPreparedStatement(getPriceListIdFromPriceListDescrQuery.toString(), 
			"fetch Person Id from Person Id Number");
			getPriceListIdFromPriceListDescrPrepStmt.setAutoclose(false);
			
			getPriceListIdFromPriceListDescrPrepStmt.bindString("priceListDescr", priceListDescr, "DESCR");
			
			SQLResultRow getPriceListIdFromPriceListDescrResultRow = getPriceListIdFromPriceListDescrPrepStmt.firstRow();
			if(notNull(getPriceListIdFromPriceListDescrResultRow))
				priceListId = getPriceListIdFromPriceListDescrResultRow.getString("PRICELIST_ID");
		}
		catch(ApplicationError ae)
		{
			addError(ae.getServerMessage());
		}
		finally
		{
			if(notNull(getPriceListIdFromPriceListDescrPrepStmt))
			{
				getPriceListIdFromPriceListDescrPrepStmt.close();
				getPriceListIdFromPriceListDescrPrepStmt = null;
			}
		}
		return priceListId;
	}

	/**
	 * This method will be used to fetch Person Id from Person Id Number
	 * @param personIdNumber
	 * @return personId
	 */
	private String getPersonIdFromPersonIdNbr(String personIdNumber) 
	{
		String personId = BLANK;
		
		PreparedStatement getPersonIdFromPersonIdNbrPrepStmt = null;
		StringBuilder getPersonIdFromPersonIdNbrQuery = new StringBuilder()
		.append("SELECT PER_ID FROM CI_PER_ID WHERE PER_ID_NBR=:personIdNumber");
		
		try
		{
			getPersonIdFromPersonIdNbrPrepStmt =  createPreparedStatement(getPersonIdFromPersonIdNbrQuery.toString(), "fetch Person Id from Person Id Number");
			getPersonIdFromPersonIdNbrPrepStmt.setAutoclose(false);
			
			getPersonIdFromPersonIdNbrPrepStmt.bindString("personIdNumber", personIdNumber, "PER_ID_NBR");
			
			SQLResultRow getPersonIdFromPersonIdNbrResultRow = getPersonIdFromPersonIdNbrPrepStmt.firstRow();
			if(notNull(getPersonIdFromPersonIdNbrResultRow))
				personId = getPersonIdFromPersonIdNbrResultRow.getString("PER_ID");
		}
		catch(ApplicationError ae)
		{
			addError(ae.getServerMessage());
		}
		finally
		{
			if(notNull(getPersonIdFromPersonIdNbrPrepStmt))
			{
				getPersonIdFromPersonIdNbrPrepStmt.close();
				getPersonIdFromPersonIdNbrPrepStmt = null;
			}
		}
		return personId;
	}

	/**
	 * This method will be used to fetch Account Id from Account Number
	 * @param accountNumber
	 * @return accountId
	 */
	private String getAccountIdFromAcctNbr(String accountNumber) 
	{
		String accountId = BLANK;
		
		PreparedStatement getAccountIdFromAcctNbrPrepStmt = null;
		StringBuilder getAccountIdFromAcctNbrQuery = new StringBuilder()
		.append("SELECT ACCT_ID FROM CI_ACCT_NBR WHERE ACCT_NBR=:accountNumber");
		
		try
		{
			getAccountIdFromAcctNbrPrepStmt =  createPreparedStatement(getAccountIdFromAcctNbrQuery.toString(), "fetch Account Id from Account Number");
			getAccountIdFromAcctNbrPrepStmt.setAutoclose(false);
			
			getAccountIdFromAcctNbrPrepStmt.bindString("accountNumber", accountNumber, "ACCT_NBR");
			
			SQLResultRow getAccountIdFromAcctNbrResultRow = getAccountIdFromAcctNbrPrepStmt.firstRow();
			if(notNull(getAccountIdFromAcctNbrResultRow))
				accountId = getAccountIdFromAcctNbrResultRow.getString("ACCT_ID");
		}
		catch(ApplicationError ae)
		{
			addError(ae.getServerMessage());
		}
		finally
		{
			if(notNull(getAccountIdFromAcctNbrPrepStmt))
			{
				getAccountIdFromAcctNbrPrepStmt.close();
				getAccountIdFromAcctNbrPrepStmt = null;
			}
		}
		return accountId;
	}

	//CB-387-ADD-START
	
	/**
	 * This Method will create ServicePayload with new Nodes
	 * @return servicePayload
	 */
	
	private String createServicePayload(Document servicePayloadDoc,StringId id,String inputType)
	{
		Node createPA = servicePayloadDoc.selectSingleNode(CM_CreatePA_NODE);
		 
		Node priceItemCodeNode = createPA.selectSingleNode(PRICE_ITEM_CD_NODE); 
		String priceItemCode =priceItemCodeNode.getText();
			
		Node startDateNode = createPA.selectSingleNode(START_DATE_NODE); 
		String startDate =startDateNode.getText();
		
		Date dt = null;
		
		if(startDate.contains("-"))
		{
			String [] str=startDate.split("-");
			dt=new Date(Integer.parseInt(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2]));
		
		}
		if(startDate.contains("/"))
		{
			String [] str=startDate.split("/");
			dt=new Date(Integer.parseInt(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2]));
		
		}
		
		String priceAssignId = null;
		Date startDateNw = null;
		Date endDateNw = null;
		SQLResultRow row = null;
		
		
		if (inputType.equals( ACCT_PARTY))
		{
	
			row =cmRetrieveRecords(priceItemCode,id,ACCT_PARTY) ;
		
		}
		if (inputType.equals(PERS_PARTY))
		{
			row =cmRetrieveRecords(priceItemCode,id,PERS_PARTY) ;
		}
		if (inputType.equals(PLST_PARTY))
		{
			row =cmRetrieveRecords(priceItemCode,id,PLST_PARTY) ;
		}
	  
			if (notNull(row)) 
			{
	        	priceAssignId = row.getString("PRICE_ASGN_ID");
	        	startDateNw = row.getDate("START_DT");
	        	endDateNw = row.getDate("END_DT");
	        }
		 if(notNull(priceAssignId))
		 { 
			 if(startDateNw.equals(dt))
			 {	 
				 if (inputType.equals( ACCT_PARTY) || inputType.equals(PERS_PARTY))
					{
				
					servicePayloadDoc.getRootElement().addElement(PRICE_ASGN_ID_NODE).setText(priceAssignId);
					 servicePayloadDoc.getRootElement().addElement(OVERIDE_SW_NODE).setText(OVERIDE_SW_VAL);
					servicePayloadDoc.getRootElement().addElement(PA_OWNER_TYPE_FLAG_NODE).setText(PaOwnerTypeFlagLookup.constants.PARTY.getLookupValue().fetchIdFieldValue());
			 
					}
				 if (inputType.equals(PLST_PARTY))
					{
					 servicePayloadDoc.getRootElement().addElement(PRICE_ASGN_ID_NODE).setText(priceAssignId);
					 servicePayloadDoc.getRootElement().addElement(OVERIDE_SW_NODE).setText(OVERIDE_SW_VAL);
					servicePayloadDoc.getRootElement().addElement(PA_OWNER_TYPE_FLAG_NODE).setText(PaOwnerTypeFlagLookup.constants.PRICE_LIST.getLookupValue().fetchIdFieldValue());
					} 
			 }
			 else
			 {
				 if(!(notNull(endDateNw)))
				 {
					 
					 TimeInterval ti=new TimeInterval(0, 1, 0, 0, 0);
					 Date updatedDate=dt.subtract(ti);
					 
					 PriceAsgn_Id paId=new PriceAsgn_Id(priceAssignId);
					 
					 PriceAsgn_DTO paDTO=paId.getEntity().getDTO();
					 paDTO.setEndDate(updatedDate);
					 paId.getEntity().setDTO(paDTO);	
				 }
			 }
			 
		 }	
		servicePayload = servicePayloadDoc.asXML();
		return servicePayload;
		
	}
	
	//CB-387-ADD-END
	//CB-387-ADD-START
	
	/**
	 * This Method will Fetch start date , price assignment id and end date
	 * @return sqlResultRow
	 */
	private SQLResultRow cmRetrieveRecords(String priceItemCode, StringId id, String inputType) 
	{
		StringBuilder sb = new StringBuilder();
		PreparedStatement preparedStatement=null;
		
		if (inputType.equals( ACCT_PARTY) || inputType.equals(PERS_PARTY))
		{
	
        sb.append("SELECT pa.PRICE_ASGN_ID ,pa.START_DT ,pa.END_DT ");
        sb.append("FROM CI_PRICEASGN pa, CI_PARTY p ");
        sb.append("WHERE trim(pa.priceitem_cd)=trim(:pricItmCd) ");
        sb.append("AND pa.price_status_flag=:flag ");
        sb.append("AND pa.owner_id=p.party_uid ");
        sb.append("AND p.party_id=trim(:accId) ");
        sb.append("order by pa.start_dt desc ");
			
		preparedStatement = createPreparedStatement(sb.toString(),"");
         
		 preparedStatement.bindString("pricItmCd", priceItemCode.trim() , "PRICEITEM_CD");
		 preparedStatement.bindId("accId", id);
		 preparedStatement.bindLookup("flag",PriceStatusFlagLookup.constants.ACTIVE);
	
	     SQLResultRow row = preparedStatement.firstRow();
	     
	     if (preparedStatement != null) {
             preparedStatement.close();
         }	
		return row;
		}
		 if (inputType.equals(PLST_PARTY))
		{
	            sb.append("SELECT pa.PRICE_ASGN_ID ,pa.START_DT ,pa.END_DT ");
	            sb.append("FROM CI_PRICEASGN pa ");
	            sb.append("WHERE trim(pa.priceitem_cd)=trim(:pricItmCd) ");
	            sb.append("AND pa.price_status_flag=:flag ");
	            sb.append("AND pa.owner_id=trim(:accId) ");
	            sb.append("order by pa.start_dt desc ");
	            
				preparedStatement = createPreparedStatement(sb.toString(),"");
	             
				 preparedStatement.bindString("pricItmCd", priceItemCode.trim() , "PRICEITEM_CD");
				 preparedStatement.bindId("accId", id);
				 preparedStatement.bindLookup("flag",PriceStatusFlagLookup.constants.ACTIVE);
			
			     SQLResultRow row = preparedStatement.firstRow();
			     if (preparedStatement != null) {
		             preparedStatement.close();
		         }
			 
			 return row;
		}
		return null;
	}
	
	//CB-387-ADD-END	
	
	@Override
	public CompositeKeyBean_Per getCompositeKeyBean_Per() {
		return null;
	}

	@Override
	public String getDataReTransformRequired() {
		return null;
	}

	@Override
	public String getFileName() {
		return null;
	}

	@Override
	public FileTransformationRecord getInputFieldValueMap() {
		return null;
	}

	@Override
	public FileTransformationRecord getInputGroupRecordMap() {
		return null;
	}

	@Override
	public FileTransformationRecord getMapFieldXpathMap() {
		return null;
	}

	@Override
	public String getMessageCategory() {
		return null;
	}

	@Override
	public String getMessageNumber() {
		return null;
	}

	@Override
	public String getMessageParam1() {
		return null;
	}

	@Override
	public String getMessageParam2() {
		return null;
	}

	@Override
	public String getMessageParam3() {
		return null;
	}

	@Override
	public String getMessageParam4() {
		return null;
	}

	@Override
	public String getMessageParam5() {
		return null;
	}

	@Override
	public String getOperation() {
		return null;
	}

	@Override
	public String getRefId() {
		return null;
	}

	@Override
	public String getRequestPayload() {
		return this.recordPayLoad;
	}

	@Override
	public String getRequestType() {
		return null;
	}

	@Override
	public String getServicePayload() {
		return this.servicePayload;
	}

	@Override
	public String getSkipReason() {
		return null;
	}

	@Override
	public String getStatus() {
		return null;
	}

	@Override
	public boolean isSkipServiceExecution() {
		return false;
	}

	@Override
	public void setCompositeKeyBean_Per(CompositeKeyBean_Per arg0) {
	}

	@Override
	public void setDataReTransformRequired(String arg0) {
	}

	@Override
	public void setFileName(String arg0) {
	}

	@Override
	public void setInputFieldValueMap(FileTransformationRecord arg0) {
	}

	@Override
	public void setInputGroupRecordMap(FileTransformationRecord arg0) {
	}

	@Override
	public void setMapFieldXpathMap(FileTransformationRecord arg0) {
	}

	@Override
	public void setMessageCategory(String arg0) {
	}

	@Override
	public void setMessageNumber(String arg0) {
	}

	@Override
	public void setMessageParam1(String arg0) {
	}

	@Override
	public void setMessageParam2(String arg0) {
	}

	@Override
	public void setMessageParam3(String arg0) {
	}

	@Override
	public void setMessageParam4(String arg0) {
	}

	@Override
	public void setOperation(String arg0) {
	}

	@Override
	public void setRefId(String arg0) {
	}

	@Override
	public void setRequestPayload(String recordPayLoad) {
		this.recordPayLoad = recordPayLoad;
	}

	@Override
	public void setRequestType(String arg0) {
	}

	@Override
	public void setServiceName(String arg0) {
	}

	@Override
	public void setServicePayload(String servicePayLoad) {
		this.servicePayload = servicePayLoad;
	}

	@Override
	public void setSkipReason(String arg0) {
	}

	@Override
	public void setSkipServiceExecution(boolean arg0) {
	}

	@Override
	public void setStatus(String arg0) {
	}
}
