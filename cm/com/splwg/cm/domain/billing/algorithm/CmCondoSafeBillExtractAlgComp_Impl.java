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
 * This algorithm will be responsible for extracting the bills in ORMB and 
 * create an xml file.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-06-08   JFerna     CB-94. Initial	
 * 2020-06-18	JFerna	   CB-142. Fix for parsing of string to decimal	
 *                                 Added rounding of decimals	
 * 2020-06-24   JFerna     CB-148. Added logic to set scale of transaction
 *                                 quantity			
 * 2020-06-24   JFerna     CB-149. Added logic to retrieve bill to address
 *                                 details from person address entity
 *                                 when bill routing method is Email
 * 2020-08-13   JFerna     CB-304. Added Remittance Email and moved position
 *                                 of Subtotal Amount and Tax under the
 *                                 Transactions Summary group
 * 2020-09-03   JFerna     CB-361. Updated logic that retrieves customer
 *                                 number and payment terms
 *                                 Updated logic that retrieves tax                               
 **************************************************************************
 */

package com.splwg.cm.domain.billing.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.ListFilter;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessObject.DataAreaInstance;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.datatypes.DateFormatParseException;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.domain.batch.batchControl.BatchControl;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue_Id;
import com.splwg.base.domain.report.reportDefinition.ReportDefinition;
import com.splwg.base.support.schema.DataAreaInfo;
import com.splwg.base.support.schema.DataAreaInfoCache;
import com.splwg.ccb.api.lookup.AddressTypeFlgLookup;
import com.splwg.ccb.api.lookup.BillRoutingMethodLookup;
import com.splwg.ccb.api.lookup.EntityFlagLookup;
import com.splwg.ccb.domain.admin.billRouteType.BillRouteTypeBillExtractAlgorithmSpot;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.admin.serviceQuantityIdentifier.ServiceQuantityIdentifier_Id;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.BillCharacteristic;
import com.splwg.ccb.domain.billing.bill.BillRouting;
import com.splwg.ccb.domain.billing.bill.BillRouting_Id;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.billing.billSegment.BillCalculationLine;
import com.splwg.ccb.domain.billing.billSegment.BillCalculationLineCharacteristic;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentCalculationHeader;
import com.splwg.ccb.domain.billing.billableCharge.BillableCharge;
import com.splwg.ccb.domain.billing.billableCharge.BillableChargeServiceQuantity;
import com.splwg.ccb.domain.billing.trialBilling.TrialBill;
import com.splwg.ccb.domain.common.fileHelper.FileHelper;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountCharacteristic;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber;
import com.splwg.ccb.domain.customerinfo.address.Address;
import com.splwg.ccb.domain.customerinfo.address.AddressEntity;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.pricing.pricecomp.PriceComp;
import com.splwg.ccb.domain.pricing.pricecomp.PriceComp_Id;
import com.splwg.cm.domain.billing.utility.CmBillExtractConstants;
import com.splwg.cm.domain.billing.utility.CmBillExtractExtLookupCache;
import com.splwg.cm.domain.billing.utility.CmBillExtractExtLookupVO;
import com.splwg.cm.domain.billing.utility.CmTemplateMappingData;
import com.splwg.cm.domain.billing.utility.CmTransactionData;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.common.Dom4JHelper;

/**
 * @author JFerna
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = businessObject, name = billExtractDefaultExtLookup, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = businessObject, name = billExtractConfigExtLookup, required = true, type = entity)})
 */
public class CmCondoSafeBillExtractAlgComp_Impl extends CmCondoSafeBillExtractAlgComp_Gen
		implements BillRouteTypeBillExtractAlgorithmSpot {
	
	//Hard Parameters
	private Bill paramBill;
	private BigInteger paramSequenceNumber;
	private String paramExtractFileName;

	//Work Variables
	private Bill_Id billId;
	private List<CmTemplateMappingData> templateMappings;
	private String fileNamePrefix;
	private String extractDateFormatStr;
	private String billPeriodDateFormatStr;
	private DateFormat extractDateFormat;
	private DateFormat billPeriodDateFormat;
	private String federalTaxIdNumber;
	private String brandLogo;
	private String remittanceName;
	private String remittanceAddress1;
	private String remittanceAddress2;
	private String remittanceAddress3;
	private String remittanceAddress4;
	private String remittanceCity;
	private String remittanceCountry;
	private String remittanceCounty;
	private String remittanceState;
	private String remittancePostal;
	private String remittanceContact;
	private String bankName;
	private String bankAccountNumber;
	private String wireRoutingNumber;
	private String achRoutingNumber;
	//CB-304 - Start Add
	private String remittanceEmail;
	//CB-304 - End Add
	private IdType_Id customerNumberIdType;
	private ServiceQuantityIdentifier_Id transactionSqi;
	private CharacteristicType loanNumberCharType;
	private CharacteristicType orderNumberCharType;
	private CharacteristicType borrowerNameCharType;
	private CharacteristicType propertyAddressCharType;
	private CharacteristicType paymentTermsCharType;
	private CharacteristicType chargeCalcLineCharType;
	private String taxCalcLineCharVal;
	private String discountCalcLineCharVal;
	//CB-361 - Start Change
	//private Person_Id primaryPersonId;
	private Account account;
	//CB-361 - End Change
	private String billDate;
	private String billDueDate;
	private Date billStartDate;
	private Date billEndDate;
	private BillRouting billRouting;
	//CB-142 - Start Add
	private String currentChargeStr;
	private String netAmountDueStr;
	//CB-142 - End Add
	private BigDecimal currentCharge;
	private BigDecimal netAmountDue;
	private String currentBatchNumber;
	private String completeFileName;
	private Element elem;
	private Element elemForEmptyNode;
	//CB-148 - Start Add
	private int quantityScale;
	//CB-148 - End Add
	//CB-149 - Start Add
	private Address addressEntity;
	private String billToAddress1;
	private String billToAddress2;
	private String billToAddress3;
	private String billToAddress4;
	private String billToCity;
	private String billToCountry;
	private String billToCounty;
	private String billToState;
	private String billToPostal;
	//CB-149 - End Add
	//CB-361 - Start Add
	private String customerNbr;
	private Query<BillCalculationLine> bsCalcLinequery;
	private StringBuilder taxCalcLineStringBuilder;
	private CharacteristicTypeLookup taxLineCharTypeLkp;
	//CB-361 - End Add
	
	/**
	 * Main Processing
	 */
	@Override
	public void invoke() {
		
		initializeWorkVariables();
		
		//Fetch Bill Extract Extendable Lookups
		billId = paramBill.getId();
		//CB-361 - Start Change
		//Account account = paramBill.getAccount();
		account = paramBill.getAccount();
		//CB-361 - End Change
		fetchExtLookupData(account.getCustomerClass().getId().getIdValue());
		
		//Fetch Date Formats
		if (isValidDelimitedDateFormat(extractDateFormatStr)){
			extractDateFormat = new DateFormat(extractDateFormatStr);
		}else{
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DATE_FORMAT,
					CmBillExtractConstants.EXTRACT_DATE_FORMAT,
					extractDateFormatStr));
		}
		
		if (isValidDelimitedDateFormat(billPeriodDateFormatStr)){
			billPeriodDateFormat = new DateFormat(billPeriodDateFormatStr);	
		}else{
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DATE_FORMAT,
					CmBillExtractConstants.BILL_PERIOD_DATE_FORMAT,
					billPeriodDateFormatStr));
		}
		
		//CB-361 - Start Add
		//Retrieve Account Customer Number
		if(notNull(account)){			
			ListFilter<AccountNumber> accountNbrListFilter = account.getAccountNumber().createFilter(" where this.id.accountIdentifierType = :idType", "CmCondoSafeBillExtractAlgComp_Impl");
			accountNbrListFilter.bindId("idType", customerNumberIdType);
			AccountNumber accountNbr = accountNbrListFilter.firstRow();
			customerNbr = notNull(accountNbr) ? accountNbr.getAccountNumber() : CmBillExtractConstants.EMPTY_STRING;
		}
		//CB-361 - End Add
		
		//Fetch Data in Base Business Service
		//CB-361 - Start Delete
		//Retrieve Primary Person of Account
		//primaryPersonId = retrievePrimaryPerson(account);
		//CB-361 - End Delete
		
		//Invoke base business service C1-BnkBillHeaderExtract			
		BusinessServiceInstance bankBillHeaderExtract = invokeBankBillHeaderExtract(extractDateFormatStr,billPeriodDateFormatStr);
		try {
			billStartDate = billPeriodDateFormat.parseDate(bankBillHeaderExtract.getString("billStartDt"));
			billEndDate = billPeriodDateFormat.parseDate(bankBillHeaderExtract.getString("billEndDt"));
		} catch (DateFormatParseException e) {
			addError(CmMessageRepository.getServerMessage(CmMessages.ERROR_PARSING_DATE,
					CmBillExtractConstants.BILL_PERIOD_DATE,
					e.getMessage()));			
		}

		//Invoke base business service C1-InvoiceSummaryExtract
		BusinessServiceInstance invoiceSummaryExtract = invokeInvoiceSummaryExtract();
		//CB-304 - Start Change
		//CB-142 - Start Change
		//currentCharge = BigDecimal.valueOf(Float.parseFloat(invoiceSummaryExtract.getString("currChrg"))).setScale(CmBillExtractConstants.TWO);
		//netAmountDue = BigDecimal.valueOf(Float.parseFloat(invoiceSummaryExtract.getString("netAmtDue"))).setScale(CmBillExtractConstants.TWO);
		//currentChargeStr = invoiceSummaryExtract.getString("currChrg").replace(CmBillExtractConstants.COMMA, CmBillExtractConstants.EMPTY_STRING);			
		//netAmountDueStr = invoiceSummaryExtract.getString("netAmtDue").replace(CmBillExtractConstants.COMMA, CmBillExtractConstants.EMPTY_STRING);
		//currentCharge = BigDecimal.valueOf(Float.parseFloat(currentChargeStr)).setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
		//netAmountDue = BigDecimal.valueOf(Float.parseFloat(netAmountDueStr)).setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
		//CB-142 - End Change
		if (notNull(invoiceSummaryExtract)){
			currentChargeStr = !isBlankOrNull(invoiceSummaryExtract.getString("currChrg")) ?
					invoiceSummaryExtract.getString("currChrg").replace(CmBillExtractConstants.COMMA, CmBillExtractConstants.EMPTY_STRING) :
						CmBillExtractConstants.ZERO;			
			netAmountDueStr = !isBlankOrNull(invoiceSummaryExtract.getString("netAmtDue")) ?
					invoiceSummaryExtract.getString("netAmtDue").replace(CmBillExtractConstants.COMMA, CmBillExtractConstants.EMPTY_STRING) :
						CmBillExtractConstants.ZERO;
			currentCharge = BigDecimal.valueOf(Float.parseFloat(currentChargeStr)).setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
			netAmountDue = BigDecimal.valueOf(Float.parseFloat(netAmountDueStr)).setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
		}else{
			currentCharge = BigDecimal.ZERO;
			netAmountDue = BigDecimal.ZERO;
		}
		//CB-304 - End Change
	
		//Populate Bill Extract Record Data Area
		String xml = populateBillExtractRecord();
		
		//Setup File Path and File
		setupFilePathAndFile();
		
		//Write XML to a File
		writeToFile(xml);
				
	}
	
	/**
	 * This method initializes work variables
	 */
	private void initializeWorkVariables(){
		templateMappings = null;
		fileNamePrefix = CmBillExtractConstants.EMPTY_STRING;
		extractDateFormatStr = CmBillExtractConstants.EMPTY_STRING;
		billPeriodDateFormatStr = CmBillExtractConstants.EMPTY_STRING;
		federalTaxIdNumber = CmBillExtractConstants.EMPTY_STRING;
		brandLogo = CmBillExtractConstants.EMPTY_STRING;
		remittanceName = CmBillExtractConstants.EMPTY_STRING;
		remittanceAddress1 = CmBillExtractConstants.EMPTY_STRING;
		remittanceAddress2 = CmBillExtractConstants.EMPTY_STRING;
		remittanceAddress3 = CmBillExtractConstants.EMPTY_STRING;
		remittanceAddress4 = CmBillExtractConstants.EMPTY_STRING;
		remittanceCity = CmBillExtractConstants.EMPTY_STRING;
		remittanceCountry = CmBillExtractConstants.EMPTY_STRING;
		remittanceCounty = CmBillExtractConstants.EMPTY_STRING;
		remittanceState = CmBillExtractConstants.EMPTY_STRING;
		remittancePostal = CmBillExtractConstants.EMPTY_STRING;
		remittanceContact = CmBillExtractConstants.EMPTY_STRING;
		bankName = CmBillExtractConstants.EMPTY_STRING;
		bankAccountNumber = CmBillExtractConstants.EMPTY_STRING;
		wireRoutingNumber = CmBillExtractConstants.EMPTY_STRING;
		achRoutingNumber = CmBillExtractConstants.EMPTY_STRING;
		//CB-304 - Start Add
		remittanceEmail = CmBillExtractConstants.EMPTY_STRING;
		//CB-304 - End Add
		customerNumberIdType = null;
		transactionSqi = null;
		loanNumberCharType = null;
		orderNumberCharType = null;
		borrowerNameCharType = null;
		propertyAddressCharType = null;
		paymentTermsCharType = null;
		chargeCalcLineCharType = null;
		taxCalcLineCharVal = CmBillExtractConstants.EMPTY_STRING;
		discountCalcLineCharVal = CmBillExtractConstants.EMPTY_STRING;
		billDate = null;
		billDueDate = null;
		billStartDate = null;
		billEndDate = null;
		currentCharge = BigDecimal.ZERO;
		netAmountDue = BigDecimal.ZERO;
		completeFileName = CmBillExtractConstants.EMPTY_STRING;	
		//CB-148 - Start Add
		quantityScale = CmBillExtractConstants.INT_ZERO;
		//CB-148 - End Add
		//CB-149 - Start Add
		addressEntity = null;
		billToAddress1 = CmBillExtractConstants.EMPTY_STRING;
		billToAddress2 = CmBillExtractConstants.EMPTY_STRING;
		billToAddress3 = CmBillExtractConstants.EMPTY_STRING;
		billToAddress4 = CmBillExtractConstants.EMPTY_STRING;
		billToCity = CmBillExtractConstants.EMPTY_STRING;
		billToCountry = CmBillExtractConstants.EMPTY_STRING;
		billToCounty = CmBillExtractConstants.EMPTY_STRING;
		billToState = CmBillExtractConstants.EMPTY_STRING;
		billToPostal = CmBillExtractConstants.EMPTY_STRING;
		//CB-149 - End Add
		//CB-361 - Start Add
		customerNbr = CmBillExtractConstants.EMPTY_STRING;
		//CB-361 - End Add
	}
	
	/**
	 * This method fetch extendable lookup data
	 * @param customerClass
	 */
	private void fetchExtLookupData(String customerClass){
		//Initialize lookups
		CmBillExtractExtLookupVO billExtractDefaultExtLookup = null;
		CmBillExtractExtLookupVO billExtractConfigExtLookup = null;
		String billExtractExtLookup = this.getBillExtractDefaultExtLookup().getId().getIdValue();
		
		//Bill Extract Default Extendable Lookup
		billExtractDefaultExtLookup = CmBillExtractExtLookupCache.getBillExtractDefaultConfigLookup(this.getBillExtractDefaultExtLookup(), customerClass);
		if(isNull(billExtractDefaultExtLookup)){
			addError(CmMessageRepository.getServerMessage(CmMessages.NO_EXT_LOOKUP_CONFIG_FOR_CUST_CL,
					billExtractExtLookup,
					customerClass));
		}
		
		templateMappings = billExtractDefaultExtLookup.getTemplateMappings();
		fileNamePrefix = billExtractDefaultExtLookup.getFileNamePrefix();
		extractDateFormatStr = billExtractDefaultExtLookup.getExtractDateFormat();
		billPeriodDateFormatStr = billExtractDefaultExtLookup.getBillPeriodDateFormat();		
		federalTaxIdNumber = billExtractDefaultExtLookup.getFederalTaxIdNumber();
		brandLogo = billExtractDefaultExtLookup.getBrandLogo();
		remittanceName = billExtractDefaultExtLookup.getRemittanceName();
		remittanceAddress1 = billExtractDefaultExtLookup.getRemittanceAddress1();
		remittanceAddress2 = billExtractDefaultExtLookup.getRemittanceAddress2();
		remittanceAddress3 = billExtractDefaultExtLookup.getRemittanceAddress3();
		remittanceAddress4 = billExtractDefaultExtLookup.getRemittanceAddress4();
		remittanceCity = billExtractDefaultExtLookup.getRemittanceCity();
		remittanceCountry = billExtractDefaultExtLookup.getRemittanceCountry();
		remittanceCounty = billExtractDefaultExtLookup.getRemittanceCounty();
		remittanceState = billExtractDefaultExtLookup.getRemittanceState();
		remittancePostal = billExtractDefaultExtLookup.getRemittancePostal();
		remittanceContact = billExtractDefaultExtLookup.getRemittanceContact();
		bankName = billExtractDefaultExtLookup.getBankName();
		bankAccountNumber = billExtractDefaultExtLookup.getBankAccountNumber();
		wireRoutingNumber = billExtractDefaultExtLookup.getWireRoutingNumber();
		achRoutingNumber = billExtractDefaultExtLookup.getAchRoutingNumber();
		//CB-304 - Start Add
		remittanceEmail = billExtractDefaultExtLookup.getEmail();
		//CB-304 - End Add
				
		//Bill Extract Configuration Extendable Lookup
		billExtractConfigExtLookup = CmBillExtractExtLookupCache.getBillExtractConfigLookup(this.getBillExtractConfigExtLookup(), customerClass);
		if(isNull(billExtractConfigExtLookup)){
			addError(CmMessageRepository.getServerMessage(CmMessages.NO_EXT_LOOKUP_CONFIG_FOR_CUST_CL,
					billExtractExtLookup,
					customerClass));
		}
		
		customerNumberIdType = billExtractConfigExtLookup.getCustomerNumberIdentifierType();
		transactionSqi = billExtractConfigExtLookup.getTransactionQuantitySqi();
		
		//CB-148 - Start Add
		if (notNull(transactionSqi.getEntity())){
			quantityScale = transactionSqi.getEntity().getDecimalPositions().intValue();
		}
		//CB-148 - End Add
		
		loanNumberCharType = billExtractConfigExtLookup.getLoanNumberCharacteristicType();
		orderNumberCharType = billExtractConfigExtLookup.getOrderNumberCharacteristicType();
		borrowerNameCharType = billExtractConfigExtLookup.getBorrowerNameCharacteristicType();
		propertyAddressCharType = billExtractConfigExtLookup.getPropertyAddressCharacteristicType();
		paymentTermsCharType = billExtractConfigExtLookup.getPaymentTermsCharacteristicType();		
		chargeCalcLineCharType = billExtractConfigExtLookup.getChargeCalcLineCharacteristicType();
		taxCalcLineCharVal = billExtractConfigExtLookup.getTaxCalcLineCharacteristicValue();
		discountCalcLineCharVal = billExtractConfigExtLookup.getDiscountCalcLineCharacteristicValue();
		
		//Check if Tax and Discount are valid values for Charge Calc Line Characteristic Type
		validateCharacteristicValueForType(chargeCalcLineCharType,taxCalcLineCharVal);
		validateCharacteristicValueForType(chargeCalcLineCharType,discountCalcLineCharVal);
	}
	
	/**
	 * This method validates char val for a char type
	 * @param characteristicType
	 * @param strCharacteristicValue
	 */
	private void validateCharacteristicValueForType(CharacteristicType characteristicType, String strCharacteristicValue) {
		CharacteristicType_Id charTypeId = characteristicType.getId();
		CharacteristicValue_Id characteristicValueId = new CharacteristicValue_Id(charTypeId, strCharacteristicValue);
		if(isNull(characteristicValueId.getEntity())){
			addError(CmMessageRepository.getServerMessage(CmMessages.CHAR_VAL_INVALID_FOR_CHAR_TYPE,
					strCharacteristicValue,
					charTypeId.getIdValue()));
		}
	}
	
	/**
	 * This method validates delimited date formats
	 * @param dateString
	 * @return true if date format is valid, otherwise, false
	 */
	private static boolean isValidDelimitedDateFormat(String dateString) {
		return (Pattern.matches("y{1,4}+[\\W&&\\S]M{1,3}+[\\W&&\\S]d{1,2}+",
				dateString)
				|| Pattern.matches("y{1,4}+[\\W&&\\S]d{1,2}+[\\W&&\\S]M{1,3}+",
						dateString)
				|| Pattern.matches("M{1,3}+[\\W&&\\S]d{1,2}+[\\W&&\\S]y{1,4}+",
						dateString) || Pattern.matches(
				"d{1,2}+[\\W&&\\S]M{1,3}+[\\W&&\\S]y{1,4}+", dateString))
				&& dateString.lastIndexOf(121) - dateString.indexOf(121) + 1 != 3;
	}
	
	//CB-361 - Start Delete
	///**
	// * This method retrieves primary person of account
	// * @param account
	// * @return primaryPersonId
	// */
	//private Person_Id retrievePrimaryPerson(Account account){
	//	Iterator<AccountPerson> accountPersonIter = account.getPersons().iterator();
	//	AccountPerson accountPerson;
	//	Person_Id primaryPersonId = null;
	//	while(accountPersonIter.hasNext()) {
	//		accountPerson = accountPersonIter.next();
	//	    if(accountPerson.getIsMainCustomer().isTrue()) {
	//	    	primaryPersonId = accountPerson.fetchIdPerson().getId();
	//	        break;
	//	    }
	//	} 		
	//	return primaryPersonId;
	//}
	//CB-361 - End Delete
	
	/**
	 * This method invokes base BS C1-BnkBillHeaderExtract
	 * @param extractDateFormat
	 * @param billPeriodDateFormat
	 * @return bankBillHeaderExtract
	 */
	private BusinessServiceInstance invokeBankBillHeaderExtract(String extractDateFormat, String billPeriodDateFormat){	
		BusinessServiceInstance bankBillHeaderExtract = BusinessServiceInstance.create(CmBillExtractConstants.BANK_BILL_HEADER_EXTRACT_BS);
		bankBillHeaderExtract.set("billId", billId.getIdValue());
		bankBillHeaderExtract.set("processDateFormat", extractDateFormat);
		bankBillHeaderExtract.set("billDatesFormat", billPeriodDateFormat);		
		bankBillHeaderExtract.set("isTrialBill", CmBillExtractConstants.FALSE);
		
		COTSInstanceList personDtlsListNode = bankBillHeaderExtract.getList("personDtls");
		COTSInstanceNode personDtlsNode = personDtlsListNode.newChild();
		personDtlsNode.set("idType", customerNumberIdType.getIdValue());
		
		try{
			bankBillHeaderExtract = BusinessServiceDispatcher.execute(bankBillHeaderExtract);
		}catch (ApplicationError e){
			addError(CmMessageRepository.getServerMessage(CmMessages.ERROR_INVOKING_BS,
					CmBillExtractConstants.BANK_BILL_HEADER_EXTRACT_BS,
					e.getMessage()));
		}
		
		return bankBillHeaderExtract;
	}
	
	/**
	 * This method invokes base BS C1-InvoiceSummaryExtract
	 * @return invoiceSummaryExtract
	 */
	private BusinessServiceInstance invokeInvoiceSummaryExtract(){
		BusinessServiceInstance invoiceSummaryExtract = BusinessServiceInstance.create(CmBillExtractConstants.INVOICE_SUMMARY_EXTRACT_BS);
		invoiceSummaryExtract.set("billId", billId.getIdValue());
		invoiceSummaryExtract.set("chrgCharType", chargeCalcLineCharType.getId().getIdValue());
		invoiceSummaryExtract.set("taxCalcVal", taxCalcLineCharVal);
		invoiceSummaryExtract.set("disCalcVal", discountCalcLineCharVal);
		invoiceSummaryExtract.set("isIga", CmBillExtractConstants.NO);
		invoiceSummaryExtract.set("langCd", this.getActiveContextLanguage().getId().getIdValue());
		invoiceSummaryExtract.set("isTrialBill", CmBillExtractConstants.FALSE);
		
		try{
			invoiceSummaryExtract = BusinessServiceDispatcher.execute(invoiceSummaryExtract);
		}catch (ApplicationError e){
			addError(CmMessageRepository.getServerMessage(CmMessages.ERROR_INVOKING_BS,
					CmBillExtractConstants.INVOICE_SUMMARY_EXTRACT_BS,
					e.getMessage()));
		}
			
		return invoiceSummaryExtract;
	}
	
	/**
	 * This method populates the DA CM-CondoSafeBillExtractRecord
	 * @return xml
	 */
	private String populateBillExtractRecord(){
		DataAreaInfo dataAreaInfo = DataAreaInfoCache.getRequiredDataAreaInfo(CmBillExtractConstants.CONDOSAFE_BILL_EXTRACT_RECORD_DA);
		Document groupTemplate=getEmptyXMLDocument(dataAreaInfo.getSchemaMD().toExpandedXMLDocument(true));
		DataAreaInstance groupHeaderDAInstance = new DataAreaInstance(dataAreaInfo, groupTemplate);
		
		//Retrieve Bill Routing and Bill Dates
		billRouting = new BillRouting_Id(billId,paramSequenceNumber).getEntity();
		billDate = paramBill.getBillDate().toString(extractDateFormat).toUpperCase();
		billDueDate = paramBill.getDueDate().toString(extractDateFormat).toUpperCase();
		
		//Populate Bill Details
		COTSInstanceNode templateGroup = groupHeaderDAInstance.getGroup(CmBillExtractConstants.TEMPLATE_DTL_GRP);
		COTSInstanceList templatesList = templateGroup.getList(CmBillExtractConstants.TEMPLATE_LIST);		
		COTSInstanceNode template;
		Bool isBlankTemplateNodeRemoved = Bool.FALSE;
		for (CmTemplateMappingData templateMappingData : templateMappings){
			if(templateMappingData.getBillFormat().equals(billRouting.getBillFormat())){
				
				//Remove blank template node
				if (isBlankTemplateNodeRemoved.isFalse()){
					for (COTSInstanceNode existingTempalateNode : templatesList){				
						if(isBlankOrNull((existingTempalateNode.getString(CmBillExtractConstants.TEMPLATE_CD_ELEM)))){
							removeEmptyNodes(existingTempalateNode.getElement());
							isBlankTemplateNodeRemoved = Bool.TRUE;
						}
					}
				}

				//Create new template node
				for (String templateCode : templateMappingData.getTemplateCodes()){
					template = templatesList.newChild();
					template.set(CmBillExtractConstants.TEMPLATE_CD_ELEM, templateCode);
				}
			}
		}
		
		groupHeaderDAInstance.set(CmBillExtractConstants.BILL_ROUTING_METHOD_ELEM, billRouting.fetchBillRouteType().getBillRoutingMethod());
		groupHeaderDAInstance.set(CmBillExtractConstants.BILL_ID_ELEM, billId.getIdValue());
		groupHeaderDAInstance.set(CmBillExtractConstants.BILL_DT_ELEM, billDate);
		//CB-361 - Start Change
		//groupHeaderDAInstance.set(CmBillExtractConstants.MAIN_CUST_ELEM, retrieveMainCustomerNumber(primaryPersonId));
		//groupHeaderDAInstance.set(CmBillExtractConstants.PAY_TERMS_ELEM, retrievePaymentTerms(primaryPersonId.getEntity()));
		groupHeaderDAInstance.set(CmBillExtractConstants.MAIN_CUST_ELEM, customerNbr);
		groupHeaderDAInstance.set(CmBillExtractConstants.PAY_TERMS_ELEM, retrievePaymentTerms());
		//CB-361 - End Change
		groupHeaderDAInstance.set(CmBillExtractConstants.BILL_DUE_DT_ELEM, billDueDate);
		groupHeaderDAInstance.set(CmBillExtractConstants.FEDERAL_TAX_ID_NBR_ELEM, federalTaxIdNumber);
		groupHeaderDAInstance.set(CmBillExtractConstants.INVOICE_MONTH_ELEM, paramBill.getBillDate().getMonthValue().toString());
		
		//Populate Remittance Details
		COTSInstanceNode remittanceGroup = groupHeaderDAInstance.getGroup(CmBillExtractConstants.REMITTANCE_DTL_GRP);
		remittanceGroup.set(CmBillExtractConstants.BRAND_LOGO_ELEM, brandLogo);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_NAME_ELEM, remittanceName);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_ADDR1_ELEM, remittanceAddress1);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_ADDR2_ELEM, remittanceAddress2);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_ADDR3_ELEM, remittanceAddress3);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_ADDR4_ELEM, remittanceAddress4);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_CITY_ELEM, remittanceCity);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_COUNTRY_ELEM, remittanceCountry);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_COUNTY_ELEM, remittanceCounty);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_STATE_ELEM, remittanceState);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_POSTAL_ELEM, remittancePostal);
		remittanceGroup.set(CmBillExtractConstants.REMITTANCE_CONTACT_ELEM, remittanceContact);
		remittanceGroup.set(CmBillExtractConstants.BANK_NAME_ELEM, bankName);
		remittanceGroup.set(CmBillExtractConstants.BANK_ACCT_NBR_ELEM, bankAccountNumber);
		remittanceGroup.set(CmBillExtractConstants.WIRE_ROUTING_NBR_ELEM, wireRoutingNumber);
		remittanceGroup.set(CmBillExtractConstants.ACH_ROUTING_NBR_ELEM, achRoutingNumber);
		//CB-304 - Start Add
		remittanceGroup.set(CmBillExtractConstants.EMAIL_ELEM, remittanceEmail);
		//CB-304 - End Add
		
		//Populate Bill To Details
		COTSInstanceNode billToGroup = groupHeaderDAInstance.getGroup(CmBillExtractConstants.BILL_TO_DTL_GRP);
		billToGroup.set(CmBillExtractConstants.NAME1_ELEM, billRouting.getEntityName1());
		billToGroup.set(CmBillExtractConstants.NAME2_ELEM, billRouting.getEntityName2());
		billToGroup.set(CmBillExtractConstants.NAME3_ELEM, billRouting.getEntityName3());
		
		//CB-149 - Start Change
		//billToGroup.set(CmBillExtractConstants.ADDR1_ELEM, billRouting.getAddress1());
		//billToGroup.set(CmBillExtractConstants.ADDR2_ELEM, billRouting.getAddress2());
		//billToGroup.set(CmBillExtractConstants.ADDR3_ELEM, billRouting.getAddress3());
		//billToGroup.set(CmBillExtractConstants.ADDR4_ELEM, billRouting.getAddress4());
		//billToGroup.set(CmBillExtractConstants.CITY_ELEM, billRouting.getCity());
		//billToGroup.set(CmBillExtractConstants.COUNTRY_ELEM, billRouting.getCountry());
		//billToGroup.set(CmBillExtractConstants.COUNTY_ELEM, billRouting.getCounty());
		//billToGroup.set(CmBillExtractConstants.STATE_ELEM, billRouting.getState());
		//billToGroup.set(CmBillExtractConstants.POSTAL_ELEM, billRouting.getPostal());
		
		//If Bill Routing Method is Email, retrieve Bill TO Address details from Person Address Entity.
		//Otherwise, retrieve from Bill Routing Information
		if (billRouting.fetchBillRouteType().getBillRoutingMethod().equals(BillRoutingMethodLookup.constants.EMAIL)){
			addressEntity = getEffectiveAddressEntity(billRouting.getPerson());
			if(notNull(addressEntity)){
				billToAddress1 = addressEntity.getAddress1();
				billToAddress2 = addressEntity.getAddress2();
				billToAddress3 = addressEntity.getAddress3();
				billToAddress4 = addressEntity.getAddress4();
				billToCity = addressEntity.getCity();
				billToCountry = addressEntity.getCountry();
				billToCounty = addressEntity.getCounty();
				billToState = addressEntity.getState();
				billToPostal = addressEntity.getPostal();			
			}
		}else {	
			billToAddress1 = billRouting.getAddress1();
			billToAddress2 = billRouting.getAddress2();
			billToAddress3 = billRouting.getAddress3();
			billToAddress4 = billRouting.getAddress4();
			billToCity = billRouting.getCity();
			billToCountry = billRouting.getCountry();
			billToCounty = billRouting.getCounty();
			billToState = billRouting.getState();
			billToPostal = billRouting.getPostal();
		}	
		
		billToGroup.set(CmBillExtractConstants.ADDR1_ELEM, billToAddress1);
		billToGroup.set(CmBillExtractConstants.ADDR2_ELEM, billToAddress2);
		billToGroup.set(CmBillExtractConstants.ADDR3_ELEM, billToAddress3);
		billToGroup.set(CmBillExtractConstants.ADDR4_ELEM, billToAddress4);
		billToGroup.set(CmBillExtractConstants.CITY_ELEM, billToCity);
		billToGroup.set(CmBillExtractConstants.COUNTRY_ELEM, billToCountry);
		billToGroup.set(CmBillExtractConstants.COUNTY_ELEM, billToCounty);
		billToGroup.set(CmBillExtractConstants.STATE_ELEM, billToState);
		billToGroup.set(CmBillExtractConstants.POSTAL_ELEM, billToPostal);
		//CB-149 - End Change
		
		billToGroup.set(CmBillExtractConstants.EMAIL_ELEM, billRouting.getPerson().getEmailAddress());
				
		//Populate Transaction Details
		COTSInstanceNode transactionDetailGroup = groupHeaderDAInstance.getGroup(CmBillExtractConstants.TRANS_DTL_GRP);
		transactionDetailGroup.set(CmBillExtractConstants.BILL_START_DT_ELEM, billStartDate.toString(billPeriodDateFormat));
		transactionDetailGroup.set(CmBillExtractConstants.BILL_END_DT_ELEM, billEndDate.toString(billPeriodDateFormat));
		
		//Retrieve Transactions
		List<CmTransactionData> transactionDataList = retrieveTransactions();
		
		//Populate Transaction List
		COTSInstanceList transactionsList = transactionDetailGroup.getList(CmBillExtractConstants.TRANS_LIST);
		COTSInstanceNode transactionsNode;
		CmTransactionData transactionData;	
		Bool isBlankTransactionNodeRemoved = Bool.FALSE;
		BigDecimal subTotalAmount = BigDecimal.ZERO;
		BigDecimal subTotalTax = BigDecimal.ZERO;
		for (Iterator<CmTransactionData> transactionDataIter = transactionDataList.iterator(); transactionDataIter.hasNext();){
			transactionData = transactionDataIter.next();
			
			if (isBlankTransactionNodeRemoved.isFalse()){
				//Loop thru the existing list and remove empty nodes
				for (COTSInstanceNode existingTransactionNode : transactionsList){				
					if(isBlankOrNull((existingTransactionNode.getString(CmBillExtractConstants.PRODUCT_DESC_ELEM)))){
						removeEmptyNodes(existingTransactionNode.getElement());
						isBlankTransactionNodeRemoved = Bool.TRUE;
					}
				}
			}
			
			//Create new transaction node
			transactionsNode = transactionsList.newChild();
			transactionsNode.set(CmBillExtractConstants.PRODUCT_DESC_ELEM, transactionData.getProductDescription());
			transactionsNode.set(CmBillExtractConstants.ORDER_NBR_ELEM, transactionData.getOrderNumber());
			transactionsNode.set(CmBillExtractConstants.TRANS_DT_ELEM, transactionData.getTransactionDate());
			transactionsNode.set(CmBillExtractConstants.LOAN_NBR_ELEM, transactionData.getLoanNumber());
			transactionsNode.set(CmBillExtractConstants.BRW_NAME_ELEM, transactionData.getBorrowerName());
			transactionsNode.set(CmBillExtractConstants.PROPERTY_ADDR_ELEM, transactionData.getPropertyAddress());
			transactionsNode.set(CmBillExtractConstants.TAX_ELEM, transactionData.getTax());
			transactionsNode.set(CmBillExtractConstants.AMOUNT_ELEM, transactionData.getAmount());
			
			//Calculate Totals
			//CB-142 - Start Change
			//subTotalAmount = subTotalAmount.add(transactionData.getAmount()).setScale(CmBillExtractConstants.TWO);
			//subTotalTax = subTotalTax.add(transactionData.getTax()).setScale(CmBillExtractConstants.TWO);
			subTotalAmount = subTotalAmount.add(transactionData.getAmount()).setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
			subTotalTax = subTotalTax.add(transactionData.getTax()).setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
			//CB-142 - End Change
		
		}

		//Populate Product List
		COTSInstanceList productsList = transactionDetailGroup.getList(CmBillExtractConstants.PRODUCT_LIST);
		COTSInstanceNode productNode;
		CmTransactionData productData;
		Bool isProductExisting;		
		for (Iterator<CmTransactionData> productsDataIter = transactionDataList.iterator(); productsDataIter.hasNext();){
			productData = productsDataIter.next();	
			isProductExisting = Bool.FALSE;
			
			//Loop thru the existing list
			for (COTSInstanceNode existingProductNode : productsList){		
				//Remove empty nodes
				if(isBlankOrNull((existingProductNode.getString(CmBillExtractConstants.PRODUCT_DESC_ELEM)))){
					removeEmptyNodes(existingProductNode.getElement());
				}else{
					//If product description already exist in the list, add sub total tax and sub total amount
					 if (existingProductNode.getString(CmBillExtractConstants.PRODUCT_DESC_ELEM).equals(productData.getProductDescription())){
							existingProductNode.set(CmBillExtractConstants.SUBTOTAL_TAX_ELEM, existingProductNode.getNumber(CmBillExtractConstants.SUBTOTAL_TAX_ELEM).add(productData.getTax()));
							existingProductNode.set(CmBillExtractConstants.SUBTOTAL_AMOUNT_ELEM, existingProductNode.getNumber(CmBillExtractConstants.SUBTOTAL_AMOUNT_ELEM).add(productData.getAmount()));
							isProductExisting = Bool.TRUE;
							break;
					 }
				}
			}
			
			//If product description does not exist yet in the list, add new node
			if(isProductExisting.isFalse()){
				productNode = productsList.newChild();
				productNode.set(CmBillExtractConstants.PRODUCT_DESC_ELEM, productData.getProductDescription());
				productNode.set(CmBillExtractConstants.SUBTOTAL_TAX_ELEM, productData.getTax());
				productNode.set(CmBillExtractConstants.SUBTOTAL_AMOUNT_ELEM, productData.getAmount());
			}			
		}
		
		//Populate Transaction Summary
		COTSInstanceNode transactionSummaryGroup = groupHeaderDAInstance.getGroup(CmBillExtractConstants.TRANS_SUMMARY_GRP);
		//CB-304 - Start Delete
		//transactionSummaryGroup.set(CmBillExtractConstants.SUBTOTAL_AMOUNT_ELEM, subTotalAmount);
		//transactionSummaryGroup.set(CmBillExtractConstants.TAX_ELEM, subTotalTax);
		//CB-304 - End Delete
		
		COTSInstanceList transactionSummaryTransactionsList = transactionSummaryGroup.getList(CmBillExtractConstants.TRANS_LIST);
		COTSInstanceNode transactionSummaryTransactionsNode;
		CmTransactionData summaryData;
		Bool isSummaryExisting;	
		for (Iterator<CmTransactionData> summaryDataIter = transactionDataList.iterator(); summaryDataIter.hasNext();){
			summaryData = summaryDataIter.next();	
			isSummaryExisting = Bool.FALSE;
			
			//Loop thru the existing list
			for (COTSInstanceNode existingSummaryNode : transactionSummaryTransactionsList){	
				//Remove empty nodes
				if(isBlankOrNull((existingSummaryNode.getString(CmBillExtractConstants.PRODUCT_DESC_ELEM)))){
					removeEmptyNodes(existingSummaryNode.getElement());
				}else{
					
					//If combination of product description and rate already exist in the list, add quantity and amount
					 if (existingSummaryNode.getString(CmBillExtractConstants.PRODUCT_DESC_ELEM).equals(summaryData.getProductDescription())
							 && existingSummaryNode.getNumber(CmBillExtractConstants.RATE_ELEM).equals(summaryData.getRate())){
						 	existingSummaryNode.set(CmBillExtractConstants.QTY_ELEM, existingSummaryNode.getNumber(CmBillExtractConstants.QTY_ELEM).add(summaryData.getQuantity()));
						 	existingSummaryNode.set(CmBillExtractConstants.AMOUNT_ELEM, existingSummaryNode.getNumber(CmBillExtractConstants.AMOUNT_ELEM).add(summaryData.getAmount()));
						 	isSummaryExisting = Bool.TRUE;
							break;
					 }
				}
			}
			
			//If combination of product description and rate does not exist yet in the list, add new node
			if(isSummaryExisting.isFalse()){
				transactionSummaryTransactionsNode = transactionSummaryTransactionsList.newChild();
				transactionSummaryTransactionsNode.set(CmBillExtractConstants.PRODUCT_DESC_ELEM, summaryData.getProductDescription());
				transactionSummaryTransactionsNode.set(CmBillExtractConstants.QTY_ELEM, summaryData.getQuantity());
				transactionSummaryTransactionsNode.set(CmBillExtractConstants.RATE_ELEM, summaryData.getRate());
				transactionSummaryTransactionsNode.set(CmBillExtractConstants.AMOUNT_ELEM, summaryData.getAmount());
			}			
		}
		
		//CB-304 - Start Add
		removeEmptyNodes(transactionSummaryGroup.getElement());
		transactionSummaryGroup.set(CmBillExtractConstants.SUBTOTAL_AMOUNT_ELEM, subTotalAmount);
		transactionSummaryGroup.set(CmBillExtractConstants.TAX_ELEM, subTotalTax);
		//CB-304 - End Add
		
		//Populate Totals Summary
		COTSInstanceNode totalsSummaryGroup = groupHeaderDAInstance.getGroup(CmBillExtractConstants.TOTALS_SUMMARY_GRP);	
		totalsSummaryGroup.set(CmBillExtractConstants.CURRENT_CHARGES_ELEM, currentCharge);
		totalsSummaryGroup.set(CmBillExtractConstants.NET_DUE_AMOUNT_ELEM, netAmountDue);
		
		//Transform to XML
		Document daDoc = groupHeaderDAInstance.getDocument();
		daDoc.getRootElement().setName(CmBillExtractConstants.BILL_GRP);
		Element rootElement = daDoc.getRootElement();		
		return reformat(rootElement).trim();
	}
	
	/**
	 * This method transforms the schema into a clean xml document
	 * @param doc
	 * @return xml
	 */
	private Document getEmptyXMLDocument(Document doc) {
		Document cleanDocument = doc;
		removeAttributes(cleanDocument.getRootElement());
		return cleanDocument;
	}
	
	/**
	 * This method removes attributes of the xml
	 * @param element
	 */
	private void removeAttributes(Element element){
		elem = null;
		for (Iterator elementIter = element.elementIterator(); elementIter.hasNext();) {
			elem = (Element)elementIter.next();			
			removeAttributes(elem);
		}
		element.attributes().clear();
	}
	
	/**
	 * This method removes empty nodes of the xml
	 * @param element
	 */
	private void removeEmptyNodes(Element element) {
		elemForEmptyNode = null;
		for (Iterator elementIter = element.elementIterator(); elementIter.hasNext();) {
			elemForEmptyNode = (Element)elementIter.next();
			removeEmptyNodes(elemForEmptyNode);
		}

		if ((isBlankOrNull(element.getStringValue())) && (!isNull(element.getParent()))) {
			element.getParent().remove(element);
		}
	}
	
	//CB-361 - Start Delete
	///**
	//* This method retrieves main customer number
	// * @param personId
	// * @return personIdNbr
	// */
	//private String retrieveMainCustomerNumber(Person_Id personId)	{
	//	String personIdNbr = CmBillExtractConstants.EMPTY_STRING;
	//	StringBuilder queryString = new StringBuilder();
	//	queryString.append("FROM PersonId personId ");
	//	queryString.append("WHERE personId.id.idType.id = :perIdType ");
	//	queryString.append("AND personId.id.person.id = :perId");
	//
	//	Query<PersonId> retrievePersonIdQuery = createQuery(queryString.toString(), "Retrieve Main Customer");
	//	retrievePersonIdQuery.bindId("perIdType", customerNumberIdType);
	//	retrievePersonIdQuery.bindId("perId", personId);
	//	retrievePersonIdQuery.addResult("personIdNumber", "personId");
	//
	//	if(notNull(retrievePersonIdQuery.firstRow())) {
	//		personIdNbr = retrievePersonIdQuery.firstRow().getPersonIdNumber().trim();
	//	}
	//	
	//	return personIdNbr;
	//}
	//CB-361 - End Delete
	
	/**
	 * This method retrieves payment terms
	 * @param person
	 * @return paymentTerms
	 */
	//CB-361 - Start Change
	//private String retrievePaymentTerms(Person person){
	private String retrievePaymentTerms(){
		String paymentTerms = null;
		CharacteristicTypeLookup paymentTermsCharTypeLookup = paymentTermsCharType.getCharacteristicType();
		
		//Retrieve from Bill Characteristics
		Query<BillCharacteristic> getBillCharacteristicQry = createQuery(
				"FROM BillCharacteristic billChar " +
				"WHERE billChar.id.bill = :bill " +
				"  AND billChar.id.characteristicType = :charType " +
				"  AND billChar.id.sequence = ( " +
				"		SELECT MAX(billChar2.id.sequence) " +
				"		FROM BillCharacteristic billChar2 " +
				"		WHERE billChar2.id.bill = billChar.id.bill " +
				"		AND billChar2.id.characteristicType = billChar.id.characteristicType " +
				"  ) " , "");
		
		getBillCharacteristicQry.bindEntity("bill", paramBill);
		getBillCharacteristicQry.bindEntity("charType", paymentTermsCharType);
		
		BillCharacteristic billChar = getBillCharacteristicQry.firstRow();
		if(notNull(billChar)){
			if (paymentTermsCharTypeLookup.isPredefinedValue()){
				paymentTerms = billChar.getCharacteristicValue();
			}
			else if (paymentTermsCharTypeLookup.isForeignKeyValue()){
				paymentTerms = billChar.getCharacteristicValueForeignKey1();
			}
			else {
				paymentTerms = billChar.getAdhocCharacteristicValue();
			}			
		}
		
		//CB-361 - Start Change
		////If payment terms is not found from Bill Characteristics, retrieve from Person Characteristics
		//if(isBlankOrNull(paymentTerms)){
		//	PersonCharacteristic personChar = person.getEffectiveCharacteristic(paymentTermsCharType);
		//	if (notNull(personChar)){
		//		if (paymentTermsCharTypeLookup.isPredefinedValue()){
		//			paymentTerms = personChar.getCharacteristicValue();
		//		}
		//		else if (paymentTermsCharTypeLookup.isForeignKeyValue()){
		//			paymentTerms = personChar.getCharacteristicValueForeignKey1();
		//		}
		//		else {
		//			paymentTerms = personChar.getAdhocCharacteristicValue();
		//		}
		//	}
		//
		//}		
		//If payment terms is not found from Bill Characteristics, retrieve from Account Characteristics
		if(isBlankOrNull(paymentTerms)){
			AccountCharacteristic accountChar = account.getEffectiveCharacteristic(paymentTermsCharType);
			if (notNull(accountChar)){
				if (paymentTermsCharTypeLookup.isPredefinedValue()){
					paymentTerms = accountChar.getCharacteristicValue();
				}
				else if (paymentTermsCharTypeLookup.isForeignKeyValue()){
					paymentTerms = accountChar.getCharacteristicValueForeignKey1();
				}
				else {
					paymentTerms = accountChar.getAdhocCharacteristicValue();
				}
			}
		}		
		//CB-361 - End Change
		
		return paymentTerms;
	}
	
	/**
	 * This method retrieves all transactions of the bill. Transactions are sorted by product description and rate
	 * @return sortedTransactionDataList
	 */
	private List<CmTransactionData> retrieveTransactions(){
        
		List<CmTransactionData> unsortedTransactionDataList = new ArrayList<CmTransactionData>();
		CmTransactionData transactionData;
		BillSegmentCalculationHeader bsCalcHeader;
		BillableCharge billableCharge;
		Date headerEndDate;
		QueryResultRow bsCalcLinesRow;
		BillCalculationLine bsCalcLine;
		BigDecimal quantity;
		ListFilter<BillableChargeServiceQuantity> quantityFilter;
		BigDecimal rate;
		PriceComp priceComp;
		PriceComp_Id priceCompId;
		BigDecimal tax;
		BigDecimal amount;
		//CB-361 - Start Add
		ListFilter<BillCalculationLineCharacteristic> bsCalcLineCharListFilter;
		BillCalculationLineCharacteristic taxCalcLineChar;
		Bool isTaxLine;
		//CB-361 - End Add
		
		//Retrieve Bill Segment Calculation Lines
		List<QueryResultRow> bsCalcLines = getBillSegmentCalculationLines();
		if(notNull(bsCalcLines) && !bsCalcLines.isEmpty()){
			for (Iterator<QueryResultRow> bsCalcLinesIterator = bsCalcLines.iterator(); bsCalcLinesIterator.hasNext();){
				//Initialize
				bsCalcLine = null;
				quantity = null;
				rate = BigDecimal.ZERO;
				priceComp = null;
				priceCompId = null;
				tax = BigDecimal.ZERO;
				amount = BigDecimal.ZERO;
				bsCalcLinesRow = bsCalcLinesIterator.next();
				//CB-361 - Start Add
				taxCalcLineChar = null;
				isTaxLine = Bool.FALSE;
				//CB-361 - End Add
				
				if(notNull(bsCalcLinesRow.getEntity("BCL", BillCalculationLine.class))){
					bsCalcLine = bsCalcLinesRow.getEntity("BCL", BillCalculationLine.class);
					bsCalcHeader = bsCalcLine.fetchIdBillSegmentCalculationHeader();
					billableCharge = bsCalcHeader.fetchBillableCharge();
					headerEndDate = bsCalcHeader.getEndDate();
					
					//CB-361 - Start Add
					//Determine if Bill Segment Calculation Line is Tax. If yes, skip.
					bsCalcLineCharListFilter = bsCalcLine.getCharacteristics().
							createFilter("where this.id.characteristicType = :chargeCharType ", "CmCondoSafeBillExtractAlgComp_Impl");
					bsCalcLineCharListFilter.bindEntity("chargeCharType", chargeCalcLineCharType);
					taxCalcLineChar = bsCalcLineCharListFilter.firstRow();

					if (notNull(taxCalcLineChar)){
						if (!isBlankOrNull(taxCalcLineChar.getAdhocCharacteristicValue()) && 
								taxCalcLineChar.getAdhocCharacteristicValue().trim().equals(taxCalcLineCharVal)){
							isTaxLine = Bool.TRUE;
						}else if (!isBlankOrNull(taxCalcLineChar.getCharacteristicValue()) && 
								taxCalcLineChar.getCharacteristicValue().trim().equalsIgnoreCase(taxCalcLineCharVal)){
							isTaxLine = Bool.TRUE;
						}else if (!isBlankOrNull(taxCalcLineChar.getCharacteristicValueForeignKey1()) && 
								taxCalcLineChar.getCharacteristicValue().trim().equals(taxCalcLineCharVal)) {
							isTaxLine = Bool.TRUE;
						}				
					}
					//CB-361 - End Add
					
					//Determine Quantity - Retrieve from calc line. If none, get from billable charge sq
					//CB-148 - Start Change
					//quantity = bsCalcLine.getBillableServiceQuantity();	
					//CB-361 - Start Add
					if (isTaxLine.isFalse()){
					//CB-361 - End Add
						quantity = bsCalcLine.getBillableServiceQuantity().setScale(quantityScale, BigDecimal.ROUND_HALF_UP);
						//CB-148 - End Change
						if (isNull(quantity) || quantity.compareTo(BigDecimal.ZERO) == 0){
							//CB-361 - Start Change
							//quantityFilter = billableCharge.getServiceQuantities().
							//		createFilter("WHERE this.serviceQuantityIdentifierId = :txnSqi ORDER BY this.id.sequence DESC", 
							//		"Retrieve Billable Charge Service Quantity");
							quantityFilter = billableCharge.getServiceQuantities().
									createFilter("WHERE this.serviceQuantityIdentifierId = :txnSqi ORDER BY this.id.sequence DESC", 
									"CmCondoSafeBillExtractAlgComp_Impl");
							//CB-361 - End Change
							quantityFilter.bindId("txnSqi", transactionSqi);
							
							if(notNull(quantityFilter.firstRow())){
								//CB-148 - Start Change
								//quantity = quantityFilter.firstRow().getDailyServiceQuantity();
								quantity = quantityFilter.firstRow().getDailyServiceQuantity().setScale(quantityScale, BigDecimal.ROUND_HALF_UP);
								//CB-148 - End Change
							}						
						}
						
						//Determine Rate
						//Get Calculation Line Price Amount
						//CB-142 - Start Change
						//rate = bsCalcLine.getPricamt().setScale(CmBillExtractConstants.TWO);
						rate = bsCalcLine.getPricamt().setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
						//CB-142 - End Change
						if (rate.compareTo(BigDecimal.ZERO) == 0){
							//If no rate found, get from value amount of calculation line price component
							priceCompId = new PriceComp_Id(bsCalcLine.getPriceCompId());
							if(notNull(priceCompId.getEntity())){
								priceComp = priceCompId.getEntity();
								//CB-142 - Start Change
								//rate = priceComp.getValueAmt().setScale(CmBillExtractConstants.TWO);
								rate = priceComp.getValueAmt().setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
								//CB-142 - End Change
							}
						}

						//Determine Amount
						//CB-142 - Start Change
						//amount = bsCalcLine.getCalculatedAmount().setScale(CmBillExtractConstants.TWO);
						amount = bsCalcLine.getCalculatedAmount().setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
						//CB-142 - End Change
						
						//CB-361 - Start Add
						//Determine Tax
						tax = retrieveTax(bsCalcHeader).setScale(CmBillExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
						//CB-361 - End Add
										
						//Create Transaction Data
						transactionData = CmTransactionData.Factory.newInstance();
						transactionData.setProductDescription(billableCharge.getDescriptionOnBill());
						transactionData.setOrderNumber(billableCharge,orderNumberCharType,headerEndDate);
						transactionData.setTransactionDate(billableCharge.getEndDate().toString(extractDateFormat).toUpperCase());
						transactionData.setLoanNumber(billableCharge,loanNumberCharType,headerEndDate);
						transactionData.setBorrowerName(billableCharge,borrowerNameCharType,headerEndDate);
						transactionData.setPropertyAddress(billableCharge,propertyAddressCharType,headerEndDate);
						transactionData.setQuantity(quantity);
						transactionData.setRate(rate);
						transactionData.setTax(tax);
						transactionData.setAmount(amount);

						//Add to Transaction Data List
						unsortedTransactionDataList.add(transactionData);	
					//CB-361 - Start Add
					}
					//CB-361 - End Add					
				}	
			}				
		}
		
		List<CmTransactionData> sortedTransactionDataList = sortTransactions(unsortedTransactionDataList);
		
		return sortedTransactionDataList;

	}
	
	/**
	 * This method gets the Bill Segments associated to the Bill.
	 * @param billId
	 */
	public List<QueryResultRow> getBillSegmentCalculationLines(){
		Query<QueryResultRow> query = createQuery("FROM BillSegment BS, BillSegmentCalculationHeader BCH, BillCalculationLine BCL "
				+ "where BS.billId = :billId "
				+ "and BS.id = BCH.id.billSegment "
				+ "and BCH.id.billSegment = BCL.id.billSegmentCalculationHeader.id.billSegment ", "Retrieve Calculation Lines");	
		
		query.bindId("billId", billId);	
		
		query.addResult("bseg", "BCL.id.billSegmentCalculationHeader.id.billSegment");
		query.addResult("headerSequence", "BCL.id.billSegmentCalculationHeader.id.headerSequence");
		query.addResult("sequence", "BCL.id.sequence");
		query.addResult("BCL", "BCL");
		
		query.orderBy("bseg",Query.ASCENDING);
		query.orderBy("headerSequence",Query.ASCENDING);
		query.orderBy("sequence",Query.ASCENDING);
					
		return query.list();
	}
	
	/**
	 * This method sorts the transaction list by product description and rate
	 * @param transactionList
	 * @return
	 */
	private List<CmTransactionData> sortTransactions(List<CmTransactionData> transactionList){
     		
		//Comparators
		Comparator<CmTransactionData> sortbyProductDescription = new Comparator<CmTransactionData>() {    
            public int compare(CmTransactionData f1, CmTransactionData f2){        
                return f1.getProductDescription().compareTo(f2.getProductDescription());   
                } };
         
		Comparator<CmTransactionData> sortbyRate = new Comparator<CmTransactionData>() {    
            public int compare(CmTransactionData f1, CmTransactionData f2){        
                return f1.getRate().compareTo(f2.getRate());   
                } };
                
        //Sort by Product Description and Rate
        List<CmTransactionData> sortedTransactionDataList = transactionList.stream().
        		sorted(sortbyProductDescription.thenComparing(sortbyRate)).collect(Collectors.toList()); 
        
		return sortedTransactionDataList;

	}
	
	/**
	 * This method formats the the bill extract record into an xml
	 * @param rootElement
	 * @return
	 */
	private String reformat(Element rootElement) {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setSuppressDeclaration(true);
		format.setNewLineAfterDeclaration(false);
		format.setIndentSize(3);
		format.setExpandEmptyElements(true);
		String str = null;
		str = Dom4JHelper.print(rootElement.getDocument(), format);
		return str;
	}
	
	/**
	 * This method setups the file path and file
	 * @return completeFileName
	 */
	private String setupFilePathAndFile(){
		currentBatchNumber = billRouting.getBatchNumber().toString();
		String fileSeparator = File.separator;
		String tempFilePath = CmBillExtractConstants.EMPTY_STRING;
		String formattedFileName = fileNamePrefix + currentBatchNumber + CmBillExtractConstants.XML_FILE_EXTENSION;
		
		//If extract file name parameter is not provided, replace the default file name to blank	
		//Note: When file name parameter is not provided, POSTROUT is using a default file name XTRACT01.DAT
		if (paramExtractFileName.endsWith(CmBillExtractConstants.DEFAULT_EXTRACT_FILE_NAME)){
			tempFilePath = paramExtractFileName.replace(CmBillExtractConstants.DEFAULT_EXTRACT_FILE_NAME, CmBillExtractConstants.EMPTY_STRING);
			
		//Else, retrieve substring before last instance of file separator of the extract file name
		//Note: When file name parameter is provided, remove this from the file path. This is because file name to be used must always
		//      be the Prefix File Name configured in the Extendable Lookup
		}else{
			tempFilePath = paramExtractFileName.substring(0, paramExtractFileName.lastIndexOf(fileSeparator, paramExtractFileName.length()));
		}
		
		//If File Path is blank, set complete file name to:
		//File Name Prefix + Current Batch Number + File Extension
		//Note: This happens when Output Directory Path is not provided. If this blank, file will be
		//saved in the default path sploutput
		if(isBlankOrNull(tempFilePath)){
			completeFileName = formattedFileName;
			
		//Else, set complete file name equal to:
		//Temp File Path + File Separator + File Name Prefix + Current Batch Number + File Extension
		}else{
			completeFileName = tempFilePath + fileSeparator + formattedFileName;
		}
			
		return completeFileName;					
	}
		
	/**
	 * This method writes xml to an extract file
	 * @param xml
	 */
	private void writeToFile(String xml){
		try {
			File file = new File(completeFileName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,true));
            bufferedWriter.write(xml);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } 
		catch (IOException e) {        	
			addError(CmMessageRepository.commonMessageIoFileError(CmBillExtractConstants.WRITING_FILE,
					fileNamePrefix + currentBatchNumber,
					e.getMessage()));
        }
	}
	
	//CB-149 - Start Add
	/**
	 * This method retrieves effective address of person
	 * @param person
	 * @return address
	 */
	private Address getEffectiveAddressEntity(Person person){
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
		getAddressQry.bindLookup("addressTypeFlag", AddressTypeFlgLookup.constants.PERSON_MAIN);
		getAddressQry.bindLookup("entityType", EntityFlagLookup.constants.PERSON);
		getAddressQry.bindDate("processDate", this.getProcessDateTime().getDate());
		getAddressQry.addResult("address", "address");
		
		return getAddressQry.firstRow();
	}
	//CB-149 - End Add
	
	//CB-361 - Start Add
	/**
	 * This method retrieves tax amount
	 * @param bsCalcHeader
	 * @return tax
	 */
	private BigDecimal retrieveTax(BillSegmentCalculationHeader bsCalcHeader){
		bsCalcLinequery = null;	
		taxCalcLineStringBuilder = null;
		taxLineCharTypeLkp = null;
		
		taxCalcLineStringBuilder = new StringBuilder();
		taxCalcLineStringBuilder.append("FROM BillSegmentCalculationHeader BCH, BillCalculationLine BCL, BillCalculationLineCharacteristic BCLC ");
		taxCalcLineStringBuilder.append("where BCH.id.billSegment = :bseg ");
		taxCalcLineStringBuilder.append("and BCH.id.headerSequence = :headerSequence ");
		taxCalcLineStringBuilder.append("and BCH.id.billSegment = BCL.id.billSegmentCalculationHeader.id.billSegment ");
		taxCalcLineStringBuilder.append("and BCL.id.billSegmentCalculationHeader.id.billSegment = BCLC.id.billCalculationLine.id.billSegmentCalculationHeader.id.billSegment ");
		taxCalcLineStringBuilder.append("and BCL.id.billSegmentCalculationHeader.id.headerSequence = BCLC.id.billCalculationLine.id.billSegmentCalculationHeader.id.headerSequence ");
		taxCalcLineStringBuilder.append("and BCL.id.sequence = BCLC.id.billCalculationLine.id.sequence ");
		taxCalcLineStringBuilder.append("and BCLC.id.characteristicType = :taxCharType ");
		
		taxLineCharTypeLkp = chargeCalcLineCharType.getCharacteristicType();
		if (taxLineCharTypeLkp.isAdhocValue()){
			taxCalcLineStringBuilder.append("and BCLC.adhocCharacteristicValue = :taxCharVal ");
		}else if (taxLineCharTypeLkp.isPredefinedValue()){
			taxCalcLineStringBuilder.append("and BCLC.characteristicValue = :taxCharVal ");
		}else{
			taxCalcLineStringBuilder.append("and BCLC.characteristicValueForeignKey1 = :taxCharVal ");
		}
				
		bsCalcLinequery = createQuery(taxCalcLineStringBuilder.toString(),"Retrieve Tax Calculation Line");		
		bsCalcLinequery.bindEntity("bseg", bsCalcHeader.fetchIdBillSegment());
		bsCalcLinequery.bindBigInteger("headerSequence", bsCalcHeader.fetchIdHeaderSequence());
		bsCalcLinequery.bindEntity("taxCharType", chargeCalcLineCharType);
		
		if (taxLineCharTypeLkp.isAdhocValue()){
			bsCalcLinequery.bindStringProperty("taxCharVal", BillCalculationLineCharacteristic.properties.adhocCharacteristicValue, taxCalcLineCharVal);
		}else if (taxLineCharTypeLkp.isPredefinedValue()){
			bsCalcLinequery.bindStringProperty("taxCharVal", BillCalculationLineCharacteristic.properties.characteristicValue, taxCalcLineCharVal);
		}else{
			bsCalcLinequery.bindStringProperty("taxCharVal", BillCalculationLineCharacteristic.properties.characteristicValueForeignKey1, taxCalcLineCharVal);
		}
		
		bsCalcLinequery.addResult("BCL", "BCL");

		if (notNull(bsCalcLinequery.firstRow())){
			return bsCalcLinequery.firstRow().getCalculatedAmount();
		}else{
			return  BigDecimal.ZERO;
		}		
	}
	//CB-361 - End Add
			
	/**
	 * This method sets Bill
	 * @param paramBill
	 */
	public void setBill(Bill paramBill) {
		this.paramBill = paramBill;
	}
	
	/**
	 * This method sets Sequence Number
	 * @param paramSequenceNumber
	 */
	public void setSequenceNumber(BigInteger paramSequenceNumber) {
		this.paramSequenceNumber = paramSequenceNumber;
	}

	/**
	 * This method sets Extract File Name
	 * @param paramExtractFileName
	 */
	public void setExtractFileName(String paramExtractFileName) {
		this.paramExtractFileName = paramExtractFileName;
	}
	
	/**
	 * This method sets Batch Number
	 * @param paramBatchNumber
	 */
	public void setBatchNumber(BigInteger paramBatchNumber) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method sets Batch Control
	 * @param paramBatchControl
	 */
	public void setBatchControl(BatchControl paramBatchControl) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method sets Extract Algorithm
	 * @param paramExtractAlgorithm
	 */
	public void setExtractAlgorithm(Algorithm paramExtractAlgorithm) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method sets Set File Helper
	 * @param paramSetFileHelper
	 */
	public void setFileHelper(FileHelper paramSetFileHelper) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method sets Is Trial Bill
	 * @param paramIsTrialBill
	 */
	public void setIsTrialBill(Bool paramIsTrialBill) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method sets Number of Copies
	 * @param paramNumberOfCopies
	 */
	public void setNumberOfCopies(BigInteger paramNumberOfCopies) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method sets To Bill
	 * @param paramToBill
	 */
	public void setToBill(Bill paramToBill) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method sets To Trial Bill
	 * @param paramToTrialBill
	 */
	public void setToTrialBill(TrialBill paramToTrialBill) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method sets Trial Bill
	 * @param paramTrialBill
	 */
	public void setTrialBill(TrialBill paramTrialBill) {
		// TODO Auto-generated method stub		
	}

	/**
	 * This method gets Is Bill Skipped
	 * @return Is Bill Skipped
	 */
	public Bool getIsBillSkipped() {
		return null;
	}

	/**
	 * This method gets Report Definition
	 * @return Report Definition
	 */
	public ReportDefinition getReportDefinition() {
		return null;
	}

	/**
	 * This method gets Return Code
	 * @return Return Code
	 */
	public BigInteger getReturnCode() {
		return null;
	}
}
