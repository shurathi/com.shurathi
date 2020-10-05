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
 *	This algorithm creates a  newOrder outbound message to Chase. If the response 	
 *	is successful, the freezable payment will be frozen. But if not, the error 
 *  message will be captured and logged in the Payment Exception table.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        		Reason:
 * YYYY-MM-DD   IN         		Reason text. 
 * 2020-09-23   AMusal 	 CB-39. Initial		   
 **************************************************************************
 */
package com.splwg.cm.domain.admin.serviceAgreementType.algorithm.payFreeze;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Node;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessObject.DataAreaInstance;
import com.splwg.base.api.lookup.OutboundMessageProcessingMethodLookup;
import com.splwg.base.api.lookup.OutboundMessageStatusLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.outboundMessage.outboundMessageType.OutboundMessageType;
import com.splwg.base.domain.workflow.notificationExternalId.NotificationExternalId;
import com.splwg.ccb.domain.admin.serviceAgreementType.SaTypePaymentFreezeAlgorithmSpot;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountPerson;
import com.splwg.ccb.domain.customerinfo.account.AccountPersons;
import com.splwg.ccb.domain.customerinfo.person.Person_Id;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.ccb.domain.payment.payment.Payment;
import com.splwg.ccb.domain.payment.payment.PaymentCharacteristic;
import com.splwg.ccb.domain.payment.payment.PaymentCharacteristic_DTO;
import com.splwg.ccb.domain.payment.payment.PaymentCharacteristic_Id;
import com.splwg.ccb.domain.payment.payment.PaymentSegment;
import com.splwg.ccb.domain.payment.payment.Payment_Id;
import com.splwg.ccb.domain.payment.paymentEvent.AutopayClearingStaging_DTO;
import com.splwg.ccb.domain.payment.paymentEvent.AutopayClearingStaging_Id;
import com.splwg.ccb.domain.payment.paymentEvent.PaymentTender_Id;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;


/**
 * @author RIA-IN-L-005
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = chaseDefaultValuesExtLookup, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = outboundMessageType, name = outboundMessageType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = notificationExternalId, name = externalSystem, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = transactionReferenceNumber, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = ctiLevel3Indicator, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = primaryShipToCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = characteristicType, name = primaryBillToCharType, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = primaryAddressIndicatorValue, required = true, type = string)
 *            , @AlgorithmSoftParameter (name = primaryShipToAddrType, required = true, type = string) 
 *            , @AlgorithmSoftParameter (name = primaryBillToAddrType, required = true, type = string)})
 */
public class CmCreateOutboundMessageToChase_Impl extends
		CmCreateOutboundMessageToChase_Gen implements
		SaTypePaymentFreezeAlgorithmSpot {
	//Soft Parameters

	private CharacteristicType transactionReferenceNumber=null;
	private CharacteristicType ctiLevel3Indicator=null;
	private BusinessObject chaseDefaultValuesExtLookup=null;
	private NotificationExternalId externalSystem=null;
	private OutboundMessageType outboundMessageType=null;
	String primaryShipToAddrType=null;
	String primaryAddressIndicatorValue=null;
	String primaryBillToAddrType=null;
	CharacteristicType primaryShipToCharType=null;
	CharacteristicType primaryBillToCharType=null;
	
	DataAreaInstance newRequestIntanace=null;
	String cardBrand=null;
	// Constant 
	public static final String Discover="DI";
	public static final String Amex="AX";
	public static final String Visa="VI";
	public static final String Mastercard="MC";
	public static final String EC="EC";
	public static final String DST_ID="A/R";       
	public static final String DST_ID_FOR_TAX="TAX";   
	// Hard Parameter 
	private Payment payment=null;
	
	@Override
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) 
	{
		
		chaseDefaultValuesExtLookup=getChaseDefaultValuesExtLookup();
		transactionReferenceNumber=getTransactionReferenceNumber();
		ctiLevel3Indicator=getCtiLevel3Indicator();
		externalSystem=getExternalSystem();
		outboundMessageType=getOutboundMessageType();
		primaryShipToAddrType=getPrimaryShipToAddrType();
		primaryAddressIndicatorValue=getPrimaryAddressIndicatorValue();
		primaryBillToAddrType=getPrimaryBillToAddrType();
		primaryShipToCharType=getPrimaryShipToCharType();
		primaryBillToCharType=getPrimaryBillToCharType();
		cardBrand=null;
	}
	
	@Override
	public void invoke() 
	{
		//Variable Initialization
		PreparedStatement query = null,query1=null,query3=null,query4=null,sqlQuery1=null;
		String payTenderId=null;
		String autopayClearingStagingId=null;
		String acctApayId=null;
		String Value="Y";
		Bill_Id billId =null;
		COTSInstanceNode newOrderGroup=null;
		
		//Fetch Payment Id 
		Payment_Id paymentId = payment.getId();
		
		//Fetch PAY EVENT ID
		String payEventId = paymentId.getEntity().getPaymentEvent().getId().getTrimmedValue();

		//Fetch Account Id 
		Account account = payment.getAccount();
		
		//Fetch Person Id  
		  Person_Id IdPerson = null;
		  AccountPersons acctPers =account.getPersons();
		  Iterator<AccountPerson> acctPer = acctPers.iterator();
          while(acctPer.hasNext())
          {
              AccountPerson personId= acctPer.next();
              IdPerson = personId.fetchIdPerson().getId();
          }
		
		 
          	//Fetch Payment Tender Id 
			StringBuilder getEventId = new StringBuilder()
			.append("SELECT PT.PAY_TENDER_ID from  CI_PAY_TNDR PT INNER JOIN CI_PAY CP ON PT.PAY_EVENT_ID ")
			.append("= CP.PAY_EVENT_ID AND CP.PAY_EVENT_ID=:eventId");
			query=createPreparedStatement(getEventId.toString(), "");
			query.bindId("eventId", paymentId.getEntity().getPaymentEvent().getId());
			SQLResultRow resultRow=query.firstRow(); 
			
		if(!isNull(resultRow))
		{
			payTenderId = resultRow.getString("PAY_TENDER_ID");
			query.close();
			
		//Check if Pay Tender Id Present in CI_APAY_CLR_STG table  	
			PaymentTender_Id paymentTnderId=new PaymentTender_Id(payTenderId);
			StringBuilder checkAutopayClearingStaging  = new StringBuilder()
			.append("select ACCT_APAY_ID,APAY_CLR_ID,ENTITY_NAME from CI_APAY_CLR_STG  where PAY_TENDER_ID=:paymentTnderId");
			query1=createPreparedStatement(checkAutopayClearingStaging.toString(), "");
			query1.bindId("paymentTnderId", paymentTnderId);
			SQLResultRow resultRow1=query1.firstRow(); 
			
			//If resultRow1Is Not Null 
			if(!isNull(resultRow1))
			{
				
				autopayClearingStagingId=resultRow1.getString("APAY_CLR_ID");
				acctApayId=resultRow1.getString("ACCT_APAY_ID");
				String entityName=resultRow1.getString("ENTITY_NAME");
				query.close();
				
				AutopayClearingStaging_Id  autoClearStagId= new AutopayClearingStaging_Id(autopayClearingStagingId);
				AutopayClearingStaging_DTO autoClearStagDto = autoClearStagId.getEntity().getDTO();
				String extAcctId = autoClearStagDto.getExternalAccountId();
				billId = autoClearStagDto.getBillId();
				String billAmount = payment.getPaymentAmount().getAmount().toString();
				  
				  //Outbound Message Bo Call 
			BusinessObjectInstance chaseOutBounMess=BusinessObjectInstance.create("CM-ChaseApayOutboundMsg");
			chaseOutBounMess.set("notificationExternalId",externalSystem.getId().getTrimmedValue());
			chaseOutBounMess.set("outboundMessageType",outboundMessageType.getId().getTrimmedValue());
			chaseOutBounMess.set("processingMethod",OutboundMessageProcessingMethodLookup.constants.REAL_TIME);
			chaseOutBounMess.set("status",OutboundMessageStatusLookup.constants.PENDING);
			COTSInstanceNode requestMessage = chaseOutBounMess.getGroup("requestMessage");
					COTSInstanceNode messageData = requestMessage.getGroup("messageData");
						COTSInstanceNode newOrder = messageData.getGroup("NewOrder");
							COTSInstanceNode rootGroup=newOrder.getGroup("newOrderRequest");
						
						
				//Fetch card brand 
				cardBrand=fetchCardBrand(acctApayId);

				
			
							//Fetch Schema of ExtendableLookUp
				 BusinessObjectInstance extendableBusinessObject = fetchExtendableLookUp(account.getCustomerClass().getId().getTrimmedValue(),chaseDefaultValuesExtLookup);
				 newOrderGroup=extendableBusinessObject.getGroup("newOrder");
				
				 if(isNull(newOrderGroup.getString("orbitalConnectionUsername")))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,newOrderGroup.getString("orbitalConnectionUsername"),autopayClearingStagingId));
				 }
				 else
				 {
					 rootGroup.set("orbitalConnectionUsername",newOrderGroup.getString("orbitalConnectionUsername"));
				 }
			
				 if(isNull(newOrderGroup.getString("orbitalConnectionPassword")))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,newOrderGroup.getString("orbitalConnectionPassword"),autopayClearingStagingId));
				 }
				 else
				 {
						rootGroup.set("orbitalConnectionPassword",newOrderGroup.getString("orbitalConnectionPassword"));
				 }

				 if(isNull(newOrderGroup.getString("industryType")))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,newOrderGroup.getString("industryType"),autopayClearingStagingId));
				 }
				 else
				 {
						rootGroup.set("industryType",newOrderGroup.getString("industryType"));
				 }

				 if(isNull(newOrderGroup.getString("transType")))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,newOrderGroup.getString("transType"),autopayClearingStagingId));
				 }
				 else
				 {
					 rootGroup.set("transType",newOrderGroup.getString("transType"));
				 }

				 
				 if(isNull(newOrderGroup.getString("bin")))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,newOrderGroup.getString("bin"),autopayClearingStagingId));
				 }
				 else
				 {
					 rootGroup.set("bin",newOrderGroup.getString("bin"));
				 }

				 
				 if(isNull(newOrderGroup.getString("merchantID")))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,newOrderGroup.getString("merchantID"),autopayClearingStagingId));
				 }
				 else
				 {
						rootGroup.set("merchantID",newOrderGroup.getString("merchantID"));
				 }

				 
				 if(isNull(newOrderGroup.getString("terminalID")))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,newOrderGroup.getString("terminalID"),autopayClearingStagingId));
				 }
				 else
				 {
					 rootGroup.set("terminalID",newOrderGroup.getString("terminalID"));
				 }

				 
				 if(isNull(extAcctId))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,extAcctId,autopayClearingStagingId));
				 }
				 else
				 {
						rootGroup.set("customerRefNum",extAcctId);
				 }
			
		
				 if(isNull(payEventId))
				 {
					 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,payEventId,autopayClearingStagingId));
				 }
				 else
				 {
						rootGroup.set("orderID",payEventId);
				 }
			
		
			
			rootGroup.set("cardBrand",cardBrand);
		
			 if(isNull(billAmount))
			 {
				 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,billAmount,autopayClearingStagingId));
			 }
			 else
			 {
				 Double intElement6=Double.parseDouble(billAmount);  
					billAmount = String.format("%.0f",intElement6);
					rootGroup.set("amount",billAmount);
			 }
			
			
			//Address Data parameter Initialization
			String address=null;
			String postal=null;
			String state=null;
			String city=null;
			
			
			int tempCard3LineItemCount =0,pCard3DtlIndex =0;
			
			//If cardBrand is eligible for L2/L3 according to mapping
			String chatType=ctiLevel3Indicator.getId().getTrimmedValue();
			String primaryShipToCharType1=primaryShipToCharType.getId().getTrimmedValue();
			String PersonId=IdPerson.getTrimmedValue();
			String primaryBillToCharType1=primaryBillToCharType.getId().getTrimmedValue();
		
		if(chatType.equals("CMCTILV3") && Value.equals("Y"))
			{ 
				StringBuilder addressInfo1=null,addressInfo=null;
				SQLResultRow addressData=null,addressData1=null;
				  //Fetch Address Data parameter Value from C1_ADDRESSS 
				   addressInfo  = new StringBuilder()
				  .append("SELECT ADDRESS1,CITY,POSTAL,STATE  FROM C1_ADDRESS WHERE ADDRESS_ID IN")
				  .append(" (SELECT ADT.ADDRESS_ID FROM C1_ADDRESS_ENTITY ADT ,C1_ADDRESS_CHAR ADC WHERE ADC.CHAR_TYPE_CD=:primaryShipToCharType1 ")
				  .append("AND ADC.SRCH_CHAR_VAL=:primaryAddressIndicatorValue AND ")
				  .append("ADT.ADDRESS_TYPE_FLG=:primaryShipToAddrType AND ADT.ADDRESS_ID = ADC.ADDRESS_ID  AND ADT.ENTITY_ID=:PersonId)");
				  query3=createPreparedStatement(addressInfo.toString(), "");
				  query3.bindString("primaryShipToCharType1", primaryShipToCharType1,"CHAR_TYPE_CD");
				  query3.bindString("primaryAddressIndicatorValue", primaryAddressIndicatorValue, "SRCH_CHAR_VAL");
				  query3.bindString("primaryShipToAddrType", primaryShipToAddrType, "ADDRESS_TYPE_FLG");
				  query3.bindString("PersonId",PersonId,"ENTITY_ID");
				  addressData=query3.firstRow(); 
				  
				  
				  if(isNull(addressData))
				  { //Fetch Value Address Data parameter from C1_ADDRESSS 
					   addressInfo1  = new StringBuilder()
					  .append("SELECT ADDRESS1,CITY,POSTAL,STATE  FROM C1_ADDRESS WHERE ADDRESS_ID IN ")
					  .append(" (SELECT ADT.ADDRESS_ID FROM C1_ADDRESS_ENTITY ADT ,C1_ADDRESS_CHAR ADC WHERE ADC.CHAR_TYPE_CD=:primaryBillToCharType1 ")
					  .append("AND ADC.SRCH_CHAR_VAL=:primaryAddressIndicatorValue AND ")
					  .append("ADT.ADDRESS_TYPE_FLG=:primaryBillToAddrType AND ADT.ADDRESS_ID = ADC.ADDRESS_ID  AND ADT.ENTITY_ID=:PersonId)");
					  query4=createPreparedStatement(addressInfo1.toString(), "");
					  query4.bindString("primaryBillToCharType1", primaryBillToCharType1,"CHAR_TYPE_CD");
					  query4.bindString("primaryAddressIndicatorValue", primaryAddressIndicatorValue, "SRCH_CHAR_VAL");
					  query4.bindString("primaryBillToAddrType", primaryBillToAddrType, "ADDRESS_TYPE_FLG");
					  query4.bindString("PersonId",PersonId,"ENTITY_ID");
					  addressData1=query4.firstRow(); 
					  address=addressData1.getString("ADDRESS1");
					  postal=addressData1.getString("POSTAL");
					  state=addressData1.getString("STATE");
					  city=addressData1.getString("CITY");
				  }
				  else
				  {
				   address=addressData.getString("ADDRESS1");
				   postal=addressData.getString("POSTAL");
				   state=addressData.getString("STATE");
				   city=addressData.getString("CITY");
				  }
				 
					BigInteger taxAmount=null;
					int tempAmount=0;
				
					// Only for Amex Card Brand 
					if(cardBrand.equals(Amex))
					{
						rootGroup.set("pCardDestName",entityName);
						if(address.length()>30)
						{
							String address2=null;
							address2 = address.substring(28, 56);
							rootGroup.set("pCardDestAddress",address);
							rootGroup.set("pCardDestAddress2",address2);
						}
						rootGroup.set("pCardDestAddress",address);
						rootGroup.set("pCardDestCity",city);
						rootGroup.set("pCardDestStateCd",state);
					}
					
					//only for Mastercard and Visa card Level 3 strat from here 
					if(cardBrand.equals(Mastercard)|| cardBrand.equals( Visa))
					{
						rootGroup.set("pCard3FreightAmt",newOrderGroup.getString("pCard3FreightAmt"));
						rootGroup.set("pCard3DutyAmt",newOrderGroup.getString("pCard3DutyAmt"));
						rootGroup.set("pCard3DestCountryCd",newOrderGroup.getString("pCard3DestCountryCd"));
						rootGroup.set("pCard3ShipFromZip",newOrderGroup.getString("pCard3ShipFromZip"));
					}
					
					//Only for VIsa Card 
					if(cardBrand.equals( Visa))
					{
						rootGroup.set("pCard3DiscAmt",newOrderGroup.getString("pCard3DiscAmt"));
						rootGroup.set("pCard3VATtaxAmt",newOrderGroup.getString("pCard3VATtaxAmt"));
						rootGroup.set("pCard3VATtaxRate",newOrderGroup.getString("pCard3VATtaxRate"));
					}	
					
					//Only for Mastercard  Card 
					if(cardBrand.equals(Mastercard))
					{
						rootGroup.set("pCard3AltTaxInd",newOrderGroup.getString("pCard3AltTaxInd"));
						rootGroup.set("pCard3AltTaxAmt",newOrderGroup.getString("pCard3AltTaxAmt"));
					}
					
					//This is for getList from Cheas BO
					COTSInstanceList lineItems= rootGroup.getList("pCard3LineItems");
			
					//This is for getList from ExtendableLookUp
					COTSInstanceList levelDetailBo= newOrderGroup.getList("pCard3LineDtlDflts");	 
					
					
					
					//Fetch Id From Bill Segment Table  
					PreparedStatement totalRow=null;
					StringBuilder totalRowFromCI_BSEG  = new StringBuilder()
					.append("SELECT BSEG_ID FROM  CI_BSEG WHERE BILL_ID=:billId");
					totalRow=createPreparedStatement(totalRowFromCI_BSEG.toString(), "");
					totalRow.bindId("billId", billId);
					
					List<SQLResultRow> totalLine = totalRow.list();
					for(SQLResultRow row:totalLine)
					{
						String pCard3Dtllinetot=null;
						String pCard3DtlUnitCost=null;
						pCard3DtlIndex++;
						
					
						//Data area for create child node
						COTSInstanceNode lineItemsNode=lineItems.newChild();
						
						COTSInstanceNode childNode = lineItemsNode.getGroup("pC3LineItem");
						
						// for set value data area 
						childNode.set("pCard3DtlIndex",Integer.toString(pCard3DtlIndex))	;
						tempCard3LineItemCount++;
						String bsid=row.getString("BSEG_ID");
						
						PreparedStatement sqlQuery10=null;
						
						StringBuilder fetchBillDescFrombsid  = new StringBuilder()
						.append("SELECT DESCR_ON_BILL,BILLABLE_CHG_ID FROM CI_BILL_CHG WHERE BILLABLE_CHG_ID IN ")
						.append("(SELECT BILLABLE_CHG_ID FROM CI_BSEG_CALC WHERE bseg_id=:bsid)");
						sqlQuery10=createPreparedStatement(fetchBillDescFrombsid.toString(), "");
						sqlQuery10.bindString("bsid", bsid, "BSEG_ID");
						SQLResultRow resultRow10 = sqlQuery10.firstRow();
						
						
						String billableChargeId=resultRow10.getString("BILLABLE_CHG_ID");  
					
						//Fetch Value when  CALC_AMT.CI_BSEG_CALC_LN having CI_BSEG_CL_CHAR where CHAR_TYPE_CD = TAXCHAR
						sqlQuery1= null;
						StringBuilder fetchDatafromBSEGAndChar  = new StringBuilder()
						.append("SELECT BS.CALC_AMT,BS.VALUE_AMT,BS.DST_ID FROM CI_BSEG_CALC_LN BS ,CI_BSEG_CL_CHAR BSC  WHERE ")
						.append("BSC.CHAR_TYPE_CD='TAXCHAR' AND BSC.BSEG_ID = BS.BSEG_ID AND BSC.BSEG_ID =:bsid");
						sqlQuery1=createPreparedStatement(fetchDatafromBSEGAndChar.toString(), "");
						sqlQuery1.bindString("bsid", bsid, "BSEG_ID");
					
						List<SQLResultRow> totalamountCalAndVal = sqlQuery1.list();
						
						int tempCal=0,temp=0,tempVal=0;
						for(SQLResultRow row1:totalamountCalAndVal)
						{
							Double Dtest1=null,Dtest=null,DtaxAmount=null;
							  temp=0;
							String Dist_id=row1.getString("DST_ID");
							
							if(Dist_id.trim().equals(DST_ID_FOR_TAX))
							{
						//Fetch value when CALC_AMT.CI_BSEG_CALC_LN NOT having CI_BSEG_CL_CHAR where CHAR_TYPE_CD = TAXCHAR
								pCard3Dtllinetot=row1.getString("CALC_AMT");
								pCard3DtlUnitCost=row1.getString("VALUE_AMT");
								DtaxAmount=Double.parseDouble(pCard3Dtllinetot);
								String DTaxInString = String.format("%.0f",DtaxAmount);
								taxAmount=new BigInteger(DTaxInString);
								tempAmount=taxAmount.intValue();
							}
							
							temp=0;
							   String pCard3DtlTaxAmt1 = row1.getString("CALC_AMT");
							    Dtest=Double.parseDouble(pCard3DtlTaxAmt1);  
							   pCard3DtlTaxAmt1 = String.format("%.0f",Dtest);
							   
							   temp=Integer.parseInt(pCard3DtlTaxAmt1);
							   tempCal=tempCal+temp;
							  
							   
							   temp=0;
							   
							   String pCard3DtlTaxRate1 = row1.getString("VALUE_AMT");
							    Dtest1=Double.parseDouble(pCard3DtlTaxRate1);  
							   pCard3DtlTaxRate1 = String.format("%.0f",Dtest1);
							   
							   temp=Integer.parseInt(pCard3DtlTaxRate1);
							   tempVal=tempVal+temp;
							   
						}
						String pCard3DtlTaxAmt=Integer.toString(tempCal);
						String pCard3DtlTaxRate=Integer.toString(tempVal);
						
						//Fetch PRICECOMP ID
						PreparedStatement sqlQuery15=null;
						StringBuilder fetchPRICECOMP_ID  = new StringBuilder()
						.append("select PRICECOMP_ID from CI_BSEG_CALC_LN where BSEG_ID=:bsid ORDER BY PRICECOMP_ID desc");
						sqlQuery15=createPreparedStatement(fetchPRICECOMP_ID.toString(), "");
						sqlQuery15.bindString("bsid", bsid, "BSEG_ID");
						SQLResultRow resultRow15 = sqlQuery15.firstRow();
						String priceCompId=resultRow15.getString("PRICECOMP_ID");
					
						
						// Fetch Data From CI_PRICEASGN
						PreparedStatement sqlQuery2= null;
						StringBuilder fetchDataFromCI_PRICEASGN  = new StringBuilder()
						.append("select PRICEITEM_CD from CI_PRICEASGN where PRICE_ASGN_ID IN ")
						.append("(select PRICE_ASGN_ID from CI_PRICECOMP where PRICECOMP_ID=:priceCompId)");
						sqlQuery2=createPreparedStatement(fetchDataFromCI_PRICEASGN.toString(), "");
						sqlQuery2.bindString("priceCompId", priceCompId, "PRICECOMP_ID");
						SQLResultRow resultRow4 = sqlQuery2.firstRow();
						String pCard3DtlProdCd=resultRow4.getString("PRICEITEM_CD");
						
						// Fetch SVC_QTY from CI_BCHG_SQ table
						PreparedStatement sqlQuery3= null;
						StringBuilder fetchDataFromCI_BCHG_SQ  = new StringBuilder()
						.append("select SVC_QTY from CI_BCHG_SQ WHERE SQI_CD='QTY' AND BILLABLE_CHG_ID=:billableChargeId");
						sqlQuery3=createPreparedStatement(fetchDataFromCI_BCHG_SQ.toString(), "");
						sqlQuery3.bindString("billableChargeId", billableChargeId, "BILLABLE_CHG_ID");
						SQLResultRow resultRow5 = sqlQuery3.firstRow();
						String pCard3DtlQty=resultRow5.getString("SVC_QTY");
			
						
						
						for( COTSInstanceNode  boSetvalue:levelDetailBo )
						{
						
						String pCard3DtlDesc= resultRow10.getString("DESCR_ON_BILL");
						
						
						// Only for Mastercard Visa Discover
						
							if(cardBrand.equals(Mastercard) || cardBrand.equals(Visa) || cardBrand.equals(Discover))
							{
								if(isNull(pCard3DtlDesc))
								 {
									 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,pCard3DtlDesc,autopayClearingStagingId));
								 }
								 else
								 {
									 childNode.set("pCard3DtlDesc", pCard3DtlDesc);
								 }
								
								
								if(isNull(pCard3DtlProdCd))
								 {
									 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,pCard3DtlProdCd,autopayClearingStagingId));
								 }
								 else
								 {
										childNode.set("pCard3DtlProdCd", pCard3DtlProdCd);
								 }
							
								
								Double intElement=Double.parseDouble(pCard3DtlQty);  
								pCard3DtlQty = String.format("%.0f",intElement);
								
								
								if(isNull(pCard3DtlQty))
								 {
									 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,pCard3DtlQty,autopayClearingStagingId));
								 }
								 else
								 {
										childNode.set("pCard3DtlQty", pCard3DtlQty);
								 }
								
								if(isNull(boSetvalue.getString("pCard3DtlUOM")))
								 {
									 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,boSetvalue.getString("pCard3DtlUOM"),autopayClearingStagingId));
								 }
								 else
								 {
										childNode.set("pCard3DtlUOM", boSetvalue.getString("pCard3DtlUOM"));
								 }
							
								
							
								
								Double intElement2=Double.parseDouble(pCard3Dtllinetot);  
								 pCard3Dtllinetot = String.format("%.0f",intElement2);
								childNode.set("pCard3Dtllinetot", pCard3Dtllinetot); 
								
								
								Double intElement3=Double.parseDouble(pCard3DtlUnitCost);  
								pCard3DtlUnitCost = String.format("%.0f",intElement3);
								childNode.set("pCard3DtlUnitCost", pCard3DtlUnitCost);
							}
						
						
						// Mandatory Visa and Discover. 
							if(cardBrand.equals(Visa) || cardBrand.equals(Discover)	)
							{
								childNode.set("pCard3DtlCommCd", boSetvalue.getString("pCard3DtlCommCd"));
							
								
							}
					
						// Mandatory for Visa and Mastercard
							if(cardBrand.equals(Visa) || cardBrand.equals(Mastercard)	)	
							{
								Double intElement4=Double.parseDouble(pCard3DtlTaxAmt);  
								 pCard3DtlTaxAmt = String.format("%.0f",intElement4);
								
								childNode.set("pCard3DtlTaxAmt", pCard3DtlTaxAmt);
					
								//done
								childNode.set("pCard3DtlDisc", boSetvalue.getString("pCard3DtlDisc"));
							
								
								Double intElement5=Double.parseDouble(pCard3DtlTaxRate);  
								pCard3DtlTaxRate = String.format("%.0f",intElement5);
								
								childNode.set("pCard3DtlTaxRate", pCard3DtlTaxRate);
							
								
								String pCard3DtlGrossNet=null;
								if(taxAmount.intValue()>0)
								{
									pCard3DtlGrossNet="Y";
									childNode.set("pCard3DtlGrossNet", pCard3DtlGrossNet);
									
								}
								else
								{
									pCard3DtlGrossNet="N";
									childNode.set("pCard3DtlGrossNet", pCard3DtlGrossNet);
									
								}
							
								if(pCard3DtlGrossNet.equals("Y"))
								{
									childNode.set("pCard3DtlTaxType", "SALE");
									
								}
								
								// Mandatory for  Mastercard
								if(cardBrand.equals(Mastercard))
								{
									if(taxAmount.intValue()==0)
									{
										childNode.set("pCard3DtlDiscountRate", boSetvalue.getString("pCard3DtlDiscountRate"));
										
									}
								}
								
							}
						
						
							// Mandatory for Mastercard	
							if( cardBrand.equals(Mastercard))
							{
								childNode.set("pCard3DtlDiscInd", boSetvalue.getString("pCard3DtlDiscInd"));
								
							}
					
							// Mandatory for Discover	
							if( cardBrand.equals(Discover))
							{
								childNode.set("pCard3DtlDiscountRate", boSetvalue.getString("pCard3DtlDiscountRate"));
								
							}
						
						}
						
					
					}
					
					if(isNull(Integer.toString(tempCard3LineItemCount)))
					 {
						 addError(CmMessageRepository.getServerMessage(CmMessages.NEW_ORDER_REQUEST_ERROR ,Integer.toString(tempCard3LineItemCount),autopayClearingStagingId));
					 }
					 else
					 {
							rootGroup.set("pCard3LineItemCount", Integer.toString(tempCard3LineItemCount));

					 }
					
					//only for EC
					
					if( cardBrand.equals(EC))
					{
					
					
						rootGroup.set("customerName", entityName);
	
						rootGroup.set("ecpDelvMethod", "B");
	
						rootGroup.set("ecpAuthMethod", "I");
					}
					
					if(cardBrand.equals(Mastercard) ||cardBrand.equals(Visa) ||cardBrand.equals(Discover))
					{
						
						rootGroup.set("taxAmount",Integer.toString(tempAmount));
						
						int calAmount=taxAmount.intValue();  
						if(calAmount>0)
						{
							
							rootGroup.set("taxInd","1");
						}
						else if (calAmount==0)
						{
							rootGroup.set("taxInd","0");
						}
								
						rootGroup.set("pCardOrderID",payEventId);
						
						rootGroup.set("pCardDestZip",postal);
						
					}
					
				}
		
					// New BO chase BO
		Document chaseOutBounMessDOc = BusinessObjectDispatcher.fastAdd(chaseOutBounMess.getDocument());
		Node node =chaseOutBounMessDOc.getRootElement().selectSingleNode("outboundMessageId");
		String OutboundId=null;
		if(notNull(node))
		{
			OutboundId=node.getText();
			
		}
		
		if(!isBlankOrNull(OutboundId))
		{
			// fetch Data from BO
			PreparedStatement fetchDatafromc= null;
			StringBuilder fetchDataFromboBOSchema  = new StringBuilder()
			.append("select extractValue(xmlType(XML_RESPONSE),'/NewOrderResponse/return/procStatusMessage' )as ")
			.append("procStatusMessage,extractValue(xmlType(XML_RESPONSE),'/NewOrderResponse/return/txRefNum' )as txRefNum , ")
			.append("extractValue(xmlType(XML_RESPONSE),'/NewOrderResponse/return/procStatus' ) as procStatus ")
			.append("from F1_OUTMSG where OUTMSG_ID=:OutboundId"); 
			
			fetchDatafromc=createPreparedStatement(fetchDataFromboBOSchema.toString(), "");
			fetchDatafromc.bindString("OutboundId", OutboundId, "OUTMSG_ID");
			SQLResultRow resultRow40 = fetchDatafromc.firstRow();
			String txRefNum=resultRow40.getString("TXREFNUM");
			String procStatus=resultRow40.getString("PROCSTATUS");
			String procStatusMessage=resultRow40.getString("PROCSTATUSMESSAGE");
			 COTSInstanceNode newOrderResponse = newOrderGroup.getGroup("newOrderResponse");
			 String processStatusExBo = newOrderResponse.getString("processStatus");
			 
			 if(!processStatusExBo.equals(procStatus))
			 {
				 addError(CmMessageRepository.getServerMessage(CmMessages.PROCESS_STATUS_ERROR ,autopayClearingStagingId,procStatus,procStatusMessage));
			 }
			 else
			 { 
				 // fetch Value SEQ_NUM
				 PreparedStatement selectSEQ_NUM= null;
					StringBuilder fetchDataselectSEQ_NUM = new StringBuilder()
					.append("SELECT MAX(SEQ_NUM) as SEQ_NUM FROM CI_paY_CHAR ");
					selectSEQ_NUM=createPreparedStatement(fetchDataselectSEQ_NUM.toString(), "");
					SQLResultRow resultSEQ_NUM = selectSEQ_NUM.firstRow();
					 BigInteger SS = resultSEQ_NUM.getInteger("SEQ_NUM");
				 PaymentCharacteristic_DTO payCharDTO = createDTO(PaymentCharacteristic.class);
				 
					
				      //Set Characteristic Type
					 payCharDTO.setId(new PaymentCharacteristic_Id(paymentId.getEntity(), transactionReferenceNumber,SS.add(BigInteger.ONE)));
				        //Set Characteristic Value
				        if(transactionReferenceNumber.getCharacteristicType().isPredefinedValue()){
				        	payCharDTO.setCharacteristicValue(txRefNum);
				        }else if(transactionReferenceNumber.getCharacteristicType().isAdhocValue()){
				        	payCharDTO.setAdhocCharacteristicValue(txRefNum);
				        }else if(transactionReferenceNumber.getCharacteristicType().isForeignKeyValue()){
				        	payCharDTO.setCharacteristicValueForeignKey1(txRefNum);
				        }
				        payCharDTO.setSearchCharacteristicValue(txRefNum);
				     
				        payCharDTO.newEntity();
			 }
		
			}
				
			}	
			}
		
	}



	private String fetchCardBrand(String acctApayId) 
	{
		PreparedStatement sqlQuery=null;
		
		StringBuilder fetchCardBrand  = new StringBuilder()
		.append("SELECT SRCH_CHAR_VAL FROM CI_ACCT_APAY_CHAR WHERE  CHAR_TYPE_CD='CMAPYCRD' AND ACCT_APAY_ID=:acctApayId");
		sqlQuery=createPreparedStatement(fetchCardBrand.toString(), "");
		sqlQuery.bindString("acctApayId", acctApayId, "ACCT_APAY_ID");
		SQLResultRow resultRow2=sqlQuery.firstRow();
		cardBrand=resultRow2.getString("SRCH_CHAR_VAL");
		return cardBrand;
	}

	

	private BusinessObjectInstance fetchExtendableLookUp(String trimmedValue,BusinessObject chaseDefaultValuesExtLookup) 
	{
		BusinessObjectInstance extenBoInstance=BusinessObjectInstance.create(chaseDefaultValuesExtLookup.getId().getTrimmedValue());
		extenBoInstance.set("bo",chaseDefaultValuesExtLookup.getId().getTrimmedValue());
		extenBoInstance.set("lookupValue", trimmedValue);
		extenBoInstance= BusinessObjectDispatcher.read(extenBoInstance);
		return extenBoInstance;
	}

	@Override
	public void setFinancialTransaction(FinancialTransaction arg0) {


	}

	@Override
	public void setPayment(Payment arg0) {
	
		this.payment=arg0;
	}

	@Override
	public void setPaymentSegment(PaymentSegment arg0) {

	}

	@Override
	public void setServiceAgreement(ServiceAgreement arg0) {

	}
	
}
