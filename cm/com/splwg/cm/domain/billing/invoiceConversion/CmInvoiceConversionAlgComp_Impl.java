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
 * Invoice Conversion Algorithm
 * 
 * This algorithm is responsible for creating billable charges and bills for
 * legacy invoices.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-06-29   KChan      CB-159. Initial	
 * 2020-07-07   AShrutika  CB-167. Fixed issue where only the first Service
 *                         Quantity is included in Billable Charge of the Bill
 * 2020-08-11   SPatil     CB-236 (Add change)
 **************************************************************************
 */

package com.splwg.cm.domain.billing.invoiceConversion;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.BusinessObjectInstanceKey;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.lookup.LogEntryTypeLookup;
import com.splwg.base.api.lookup.MessageSeverityLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectEnterStatusAlgorithmSpot;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.message.MessageCategory_Id;
import com.splwg.base.domain.common.message.Message_Id;
import com.splwg.ccb.api.lookup.BillActionLookup;
import com.splwg.ccb.api.lookup.BillStatusLookup;
import com.splwg.ccb.api.lookup.BillableChargeStatusLookup;
import com.splwg.ccb.api.lookup.CharacteristicEntityLookup;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.BillAPI;
import com.splwg.ccb.domain.billing.bill.BillAPIInputData;
import com.splwg.ccb.domain.billing.bill.BillCharacteristic_DTO;
import com.splwg.ccb.domain.billing.bill.BillCharacteristic_Id;
import com.splwg.ccb.domain.billing.bill.BillCompletionInputData;
import com.splwg.ccb.domain.billing.bill.Bill_DTO;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.common.c1Request.C1RequestLogParameter_DTO;
import com.splwg.ccb.domain.common.c1Request.C1RequestLogParameter_Id;
import com.splwg.ccb.domain.common.c1Request.C1RequestLog_DTO;
import com.splwg.ccb.domain.common.c1Request.C1RequestLog_Id;
import com.splwg.ccb.domain.common.c1Request.C1Request_Id;
import com.splwg.ccb.domain.common.installation.CcbInstallationHelper;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.cm.domain.common.CmCommonHelper_Impl;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.common.ApplicationError;

/**
 * @author RIA-Admin
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = billableChargeBo, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = legacyBillIdCharacteristic, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = billCharType, required = true, type = entity)})
 */
public class CmInvoiceConversionAlgComp_Impl extends CmInvoiceConversionAlgComp_Gen implements BusinessObjectEnterStatusAlgorithmSpot 
{
	private BusinessObjectInstanceKey boInstanceKey;
	private BusinessObjectInstance requestBoInstance;
	private C1Request_Id requestId;
	private Account_Id acctId;
	private Date billDate;
	private String legacyBillId;
	StringBuilder sb = new StringBuilder();
	StringBuilder sb1 = new StringBuilder();
	String priceItemCode=null;
	private static final String DELIMITER = ",";
	private static final String BLANK_STRING = "";
	// CB-236 Start ADD
	String priceGroupId=BLANK_STRING;
	// CB-236 End ADD
	@Override
	public void invoke() 
	{
		BusinessObjectInstance billableChargeBoInstance;
		
		try
		{
			//Fetch Business Object for this request
			requestBoInstance = BusinessObjectDispatcher.read(boInstanceKey, true);

			//Fetch Request Id
			requestId = new C1Request_Id(requestBoInstance.getString("request").trim());	

			//Fetch Account Id
			acctId = new Account_Id(requestBoInstance.getString("account"));
			
			//Fetch Legacy Bill Id
			legacyBillId = requestBoInstance.getString("legacyBillId").trim();
			
			if (!isBlankOrNull(legacyBillId))
			{
				//Fetch Bill Date
				billDate = requestBoInstance.getDate("billDate");
				
				//Fetch Billable Charges Group
				COTSInstanceNode bchgNode = requestBoInstance.getGroup("billableCharges");
				
				//Fetch Billable Charge List
				COTSInstanceList bchgList = bchgNode.getList("billableCharge");
				
				// CB-236 Start ADD
				//Featch price price Group Id
				for(COTSInstanceListNode priceParamList: bchgList)
				{
					priceItemCode = priceParamList.getString("priceItemCode").trim();
					determinePriceParameterGroup(priceParamList.getList("priceParameter"));

				}
				// CB-236 End ADD
				//create Billable charge and add Ids to a List
				List<String> billableChargeIdList = new ArrayList<String>();
				String billableChargeId;
				
				for(COTSInstanceListNode billableCharge:bchgList)
				{
					
					billableChargeBoInstance = createBillableCharge(billableCharge);
					billableChargeId = billableChargeBoInstance.getString("billableChargeId").trim();
					if(notNull(billableChargeId))
					{
						billableChargeIdList.add(billableChargeId);
					}
				}	
				
				//create bill from Billable charge Id list
				createBill(billableChargeIdList);
				
			}
			else
			{
				addError(CmMessageRepository.legacyBillIdNotFound(requestId.getTrimmedValue()));
			}
		}
		catch(ApplicationError e)
		{
			addError(e.getServerMessage(), MessageSeverityLookup.constants.ERROR);
		}	
	}
	// CB-236 Start ADD
	private void determinePriceParameterGroup(COTSInstanceList list) 
	{
		String code=BLANK_STRING;
		String value=BLANK_STRING; 
		String parameterCodeString =BLANK_STRING;
		String parameterCodeValue= BLANK_STRING;
		
		if(!list.isEmpty())
		{
		for(COTSInstanceListNode priceListElement:list)
		{
			code= priceListElement.getString("parameterCode").trim();
			sb.append(code + DELIMITER);
			value=priceListElement.getString("parameterValue").trim();
			sb1.append(value + DELIMITER); 
		}
		if(notNull(sb))
		{
			parameterCodeString = sb.substring(0, sb.length()-1).trim();
		}	
		if(notNull(sb1))
		{
			parameterCodeValue = sb1.substring(0, sb1.length()-1).trim();
		}
		BusinessServiceInstance parmServiceInstance = BusinessServiceInstance.create("C1_PriceItemParmGroupService");
		parmServiceInstance.set("flag","Y");
	    parmServiceInstance.set("priceItem",priceItemCode);
	    parmServiceInstance.set("priceItemParmCode", parameterCodeString);
	    parmServiceInstance.set("priceItemParmValue", parameterCodeValue);
	    BusinessServiceInstance responseInstance = BusinessServiceDispatcher.execute(parmServiceInstance);
	    priceGroupId=responseInstance.getElement().selectSingleNode("groupId").getStringValue().trim();
		}
	}
	// CB-236 End ADD



	/**
	 * This method creates billable charges from the billable charge list node 
	 * of the request bo instance
	 * @return BusinessObjectInstance
	 */
	private BusinessObjectInstance createBillableCharge(COTSInstanceListNode billableCharge) 
	{
		CmCommonHelper_Impl helper = new CmCommonHelper_Impl();//To determine whether SA Id exists or not
		
		BusinessObjectInstance bchgBoInstance = BusinessObjectInstance.create(getBillableChargeBo()); //Create Billable Charge BO Instance
		
		priceItemCode = billableCharge.getString("priceItemCode").trim();
		//Fetch SA Id using priceitem code and Account Id
		Account personAccount = acctId.getEntity();
		if(notNull(personAccount))
		{
			String saId = helper.fetchSaIdUsingPriceItemCode(personAccount, priceItemCode);
			if(notNull(saId))
			{
				//Set the basic parameter values
				bchgBoInstance.set("serviceAgreement", saId);
				bchgBoInstance.set("startDate", billableCharge.getDate("startDate"));
				bchgBoInstance.set("endDate", billableCharge.getDate("endDate"));
				bchgBoInstance.set("descriptionOnBill", billableCharge.getString("descriptionOnBill"));
				bchgBoInstance.set("billableChargeStatus", BillableChargeStatusLookup.constants.BILLABLE);
				bchgBoInstance.set("priceItemCode", priceItemCode);
				bchgBoInstance.set("grpRefVal", legacyBillId);
				// CB-236 Start ADD
				if(!isEmptyOrNull(priceGroupId))
    			{
				bchgBoInstance.set("priceItemParmGroupId", new BigDecimal(priceGroupId));
    			}
				// CB-236 End ADD
				//Set all the characteristics in the Billable Charge BO
				COTSInstanceList bchgCharList = bchgBoInstance.getList("billlableChargeCharacteristic");
				COTSInstanceList bchgCharsList = billableCharge.getList("characteristics");
				
				COTSInstanceListNode bchgNewCharNode;
				String billableChargeCharTypeStr;
				CharacteristicType billableChargeCharType;
				
				for(COTSInstanceNode billableCharges:bchgCharsList)
				{
					bchgNewCharNode = bchgCharList.newChild();
					billableChargeCharTypeStr = billableCharges.getString("characteristicType").trim();
					billableChargeCharType = new CharacteristicType_Id(billableChargeCharTypeStr).getEntity();
					if(helper.isCharTypeEntityValid(billableChargeCharType, 
							CharacteristicEntityLookup.constants.BILLABLE_CHARGE_CHARACTERISTICS) == Bool.TRUE)
					{
						bchgNewCharNode.set("characteristicType", billableChargeCharTypeStr);
						bchgNewCharNode.set("effectiveDate", billableCharge.getDate("startDate"));
						bchgNewCharNode.set("characteristicValue", billableCharges.getString("characteristicValue"));		
						bchgNewCharNode.set("adhocCharacteristicValue", billableCharges.getString("adhocCharacteristicValue"));		
						bchgNewCharNode.set("characteristicValueForeignKey1", billableCharges.getString("characteristicValueFK1"));
					}
					else
					{
						addError(CmMessageRepository.missingCharEntity(billableChargeCharTypeStr, 
								CharacteristicEntityLookup.constants.BILLABLE_CHARGE_CHARACTERISTICS
								.getLookupValue().fetchLanguageDescription()));
					}
				}
				
				//Set all the SQIs in the Billable Charge BO
				COTSInstanceListNode bchgNewSqiNode;
				
				COTSInstanceList bchgSqiList = bchgBoInstance.getList("billableChargeServiceQuantity");
				COTSInstanceList bchgSqisList = billableCharge.getList("sq");
				for(COTSInstanceNode billableChargeSqi:bchgSqisList)
				{
					bchgNewSqiNode = bchgSqiList.newChild();
					bchgNewSqiNode.set("sequence", billableChargeSqi.getNumber("seqNbr"));
					System.out.println();
					bchgNewSqiNode.set("serviceQuantityIdentifier", billableChargeSqi.getString("sqi"));		
					bchgNewSqiNode.set("dailyServiceQuantity", billableChargeSqi.getNumber("sq"));	
					//CB-167 Start - Add
					//return BusinessObjectDispatcher.add(bchgBoInstance);
					//CB-167 End - Add
				}
				//CB-167 Start - Add
				return BusinessObjectDispatcher.add(bchgBoInstance);
				//CB-167 End - Add
			}
			else
			{
				addError(CmMessageRepository.contractNotIdentifiedForAcctAndPritmComb(requestId.getTrimmedValue()));
			}			
		}
		else
		{
			addError(CmMessageRepository.accountNotFound(acctId.getTrimmedValue()));
		}
		
		return null;
		
		
	}
	
	/**
	 * This method creates bill from the list of billable charges obtained
	 * @return BusinessObjectInstance
	 */
	private void createBill(List<String> billableChargeIdList)
	{
		BillAPI billAPI;
		BillAPIInputData billAPIInputData;
		Account personAccount;
		
		Bill_DTO bill_DTO = createDTO(Bill.class);
        bill_DTO.setAccountId(acctId);
        bill_DTO.setBillStatus(BillStatusLookup.constants.PENDING);
        bill_DTO.setIsReopenAllowed(Bool.TRUE);        
        
        bill_DTO.setGrpRefVal(legacyBillId);
        bill_DTO.setIsAdhocBill(Bool.TRUE);        
        
        Bill newBill = bill_DTO.newEntity();
        Bill_Id billId = newBill.getId();

        CharacteristicType legacyBillIdCharType = getLegacyBillIdCharacteristic();
        // set order number as bill char
        CharacteristicType_Id characteristicType_Id = legacyBillIdCharType.getId();

        BillCharacteristic_Id billChar = new BillCharacteristic_Id(characteristicType_Id, billId, BigInteger.ONE);
        BillCharacteristic_DTO billCharDTO = new BillCharacteristic_DTO();
        billCharDTO.setId(billChar);
        billCharDTO.setAdhocCharacteristicValue(legacyBillId);
        billCharDTO.newEntity();

        for (String billableCharge : billableChargeIdList) 
        {
            // Generating bill segments
            billAPIInputData = BillAPIInputData.Factory.newInstance();
            personAccount = acctId.getEntity();
            if(notNull(personAccount))
            {
            	billAPIInputData.setAccount(personAccount);
                billAPIInputData.setBill(billId.getEntity());
                billAPIInputData.setBillAction(BillActionLookup.constants.GENERATE_BILL_CHG_SEGMENT);
                billAPIInputData.setBchgId(billableCharge);
                billAPIInputData.setOffCycleBillSwitch(Bool.TRUE);
                billAPIInputData.setCutoffDate(billDate);
                billAPI = BillAPI.Factory.newInstance();
                billAPI.performBillAction(billAPIInputData);
            }
            else
			{
				addError(CmMessageRepository.accountNotFound(acctId.getTrimmedValue()));
			}            
        }
        
        if(CcbInstallationHelper.getCcbInstallation().getBillSegmentFreezeOption().isFreezeAtWill())
        {
            newBill.freeze(billDate);
        }
        
        // completing the bill
        BillCompletionInputData billCompletionInputData = BillCompletionInputData.Factory.newInstance();
        billCompletionInputData.setAccountingDate(billDate);
        billCompletionInputData.setBillDate(billDate);

        newBill.complete(billCompletionInputData);
        
        //Add log entry
		addLogRequestEntry(CmMessages.BILL_GEN_LOG, getBillCharType(), billId.getTrimmedValue());
	}
	
	/***Add log on Request.
	 * 
	 * @param msgId
	 * @param relatedObjCharType
	 * @param relatedObjId
	 */
	private void addLogRequestEntry(int msgId, CharacteristicType relatedObjCharType, String relatedObjId)
	{		
		MessageCategory_Id msgCatId = new MessageCategory_Id(new BigInteger(String.valueOf(CmMessages.MESSAGE_CATEGORY)));

		C1RequestLog_DTO logDto = new C1RequestLog_DTO();
		C1RequestLog_Id logId   = new C1RequestLog_Id(requestId, fetchNextLogSequence(requestId));
		logDto.setId(logId);
		logDto.setLogDateTime(getProcessDateTime());
		logDto.setLogEntryType(LogEntryTypeLookup.constants.SYSTEM);	
		logDto.setMessageId(new Message_Id(msgCatId, new BigInteger(String.valueOf(msgId))));
		logDto.setCharacteristicTypeId(relatedObjCharType.getId());
		logDto.setCharacteristicValueForeignKey1(relatedObjId);
		logDto.setSearchCharacteristicValue(relatedObjId);
		logDto.newEntity();

		C1RequestLogParameter_DTO logParmDto = new C1RequestLogParameter_DTO();
		logParmDto.setId(new C1RequestLogParameter_Id(logId, BigInteger.ONE));
		logParmDto.setMessageParameter(relatedObjId);
		logParmDto.newEntity();
	}
	
	/***Fetch next Request Log Sequence.
	 * 
	 * @param requestId
	 * @return BigInteger
	 */
	private BigInteger fetchNextLogSequence(C1Request_Id requestId)
	{
		PreparedStatement statement = null;
		
		BigInteger seq = BigInteger.ZERO;
		SQLResultRow res = null;
		statement = createPreparedStatement("select SEQNO from C1_REQUEST_LOG where C1_REQ_ID=:reqId ORDER BY SEQNO DESC", "");
		statement.bindId("reqId", requestId);
		res = statement.firstRow();
		statement.close();
		if(notNull(res))
		{
			seq = new BigInteger(res.getString("SEQNO"));
		}
		return seq.add(BigInteger.ONE);
	}

	@Override
	public boolean getForcePostProcessing() {
		return false;
	}

	@Override
	public void setAction(BusinessObjectActionLookup var1) {
		
	}

	@Override
	public void setBusinessObject(BusinessObject var1) {
		
	}

	@Override
	public void setBusinessObjectKey(BusinessObjectInstanceKey var1) 
	{
		this.boInstanceKey = var1;
	}	
}
