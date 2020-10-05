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
 * Bill Extract
 *
 * This POJO Class has setter and getter methods for the bill extract 
 * extendable lookup configuration 
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-06-18   JFerna               CB-94. Initial Version. 
 ***********************************************************************
 */
package com.splwg.cm.domain.billing.utility;

import java.util.List;

import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.ccb.domain.admin.idType.IdType_Id;
import com.splwg.ccb.domain.admin.serviceQuantityIdentifier.ServiceQuantityIdentifier_Id;
import com.splwg.cm.domain.billing.utility.CmTemplateMappingData;

/**
 * @author JFerna
 *
 */
public class CmBillExtractExtLookupVO {

	//Work Variables
	//Bill Extract Default Extendable Lookup 
	private String customerClass;
	private String fileNamePrefix;
	private String extractDateFormat;
	private String billPeriodDateFormat;
	private String federalTaxIdNumber;
	private String billToCountry;
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
	private String email;
	private List<CmTemplateMappingData> templateMappings;
	
	//Bill Extract Configuration Extendable Lookup 
	private IdType_Id customerNumberIdentifierType;
	private ServiceQuantityIdentifier_Id transactionQuantitySqi;
	private CharacteristicType loanNumberCharacteristicType;
	private CharacteristicType orderNumberCharacteristicType;
	private CharacteristicType borrowerNameCharacteristicType;
	private CharacteristicType propertyAddressCharacteristicType;
	private CharacteristicType propertyCityCharacteristicType;
	private CharacteristicType propertyStateCharacateristicType;
	private CharacteristicType propertyZipCharacteristicType;
	private CharacteristicType paymentTermsCharacteristicType;
	private CharacteristicType chargeCalcLineCharacteristicType;
	private String taxCalcLineCharacteristicValue;
	private String discountCalcLineCharacteristicValue;
	
	//Getters and Setters
	//Bill Extract Default Extendable Lookup
	/**
	 * This method returns the Customer Class
	 * @return Customer Class
	 */
	public String getCustomerClass() {
		return customerClass;
	}
	
	/**
	 * This method sets the Customer Class
	 * @param customerClass
	 */
	public void setCustomerClass(String customerClass) {
		this.customerClass = customerClass;
	}
	
	/**
	 * This method returns the File Name Prefix
	 * @return File Name Prefix
	 */
	public String getFileNamePrefix() {
		return fileNamePrefix;
	}
	
	/**
	 * This method sets the File Name Prefix
	 * @param fileNamePrefix
	 */
	public void setFileNamePrefix(String fileNamePrefix) {
		this.fileNamePrefix = fileNamePrefix;
	}
	
	/**
	 * This method returns the Extract Date Format
	 * @return Extract Date Format
	 */
	public String getExtractDateFormat() {
		return extractDateFormat;
	}
	
	/**
	 * This method sets the Extract Date Format
	 * @param extractDateFormat
	 */
	public void setExtractDateFormat(String extractDateFormat) {
		this.extractDateFormat = extractDateFormat;
	}
	
	/**
	 * This method returns the Bill Period Date Format
	 * @return Bill Period Date Format
	 */
	public String getBillPeriodDateFormat() {
		return billPeriodDateFormat;
	}
	
	/**
	 * This method sets the Bill Period Date Format
	 * @param billPeriodDateFormat
	 */
	public void setBillPeriodDateFormat(String billPeriodDateFormat) {
		this.billPeriodDateFormat = billPeriodDateFormat;
	}
	
	/**
	 * This method returns the Federal Tax Id Number
	 * @return Federal Tax Id Number
	 */
	public String getFederalTaxIdNumber() {
		return federalTaxIdNumber;
	}
	
	/**
	 * This method sets the Federal Tax Id Number
	 * @param federalTaxIdNumber
	 */
	public void setFederalTaxIdNumber(String federalTaxIdNumber) {
		this.federalTaxIdNumber = federalTaxIdNumber;
	}
	
	/**
	 * This method returns the Bill To Country
	 * @return Bill To Country
	 */
	public String getBillToCountry() {
		return billToCountry;
	}
	
	/**
	 * This method sets the Bil To Country
	 * @param billToCountry
	 */
	public void setBillToCountry(String billToCountry) {
		this.billToCountry = billToCountry;
	}
	
	/**
	 * This method returns the Brand Logo
	 * @return Brand Logo
	 */
	public String getBrandLogo() {
		return brandLogo;
	}
	
	/**
	 * This method sets the Brand Logo
	 * @param brandLogo
	 */
	public void setBrandLogo(String brandLogo) {
		this.brandLogo = brandLogo;
	}
	
	/**
	 * This method returns the Remittance Name
	 * @return Remittance Name
	 */
	public String getRemittanceName() {
		return remittanceName;
	}
	
	/**
	 * This method sets the Remittance Name
	 * @param remittanceName
	 */
	public void setRemittanceName(String remittanceName) {
		this.remittanceName = remittanceName;
	}
	
	/**
	 * This method returns the Remittance Address 1
	 * @return Remittance Address 1
	 */
	public String getRemittanceAddress1() {
		return remittanceAddress1;
	}
	/**
	 * This method sets the Remittance Address 1
	 * @param remittanceAddress1
	 */
	public void setRemittanceAddress1(String remittanceAddress1) {
		this.remittanceAddress1 = remittanceAddress1;
	}
	
	/**
	 * This method returns the Remittance Address 2
	 * @return Remittance Address 2
	 */
	public String getRemittanceAddress2() {
		return remittanceAddress2;
	}
	
	/**
	 * This method sets the Remittance Address 2
	 * @param remittanceAddress2
	 */
	public void setRemittanceAddress2(String remittanceAddress2) {
		this.remittanceAddress2 = remittanceAddress2;
	}
	
	/**
	 * This method returns the Remittance Address 3
	 * @return Remittance Address 3
	 */
	public String getRemittanceAddress3() {
		return remittanceAddress3;
	}
	
	/**
	 * This method sets the Remittance Address 3
	 * @param remittanceAddress3
	 */
	public void setRemittanceAddress3(String remittanceAddress3) {
		this.remittanceAddress3 = remittanceAddress3;
	}
	
	/**
	 * This method returns the Remittance Address 4
	 * @return Remittance Address 4
	 */
	public String getRemittanceAddress4() {
		return remittanceAddress4;
	}
	
	/**
	 * This method sets the Remittance Address 4
	 * @param remittanceAddress4
	 */
	public void setRemittanceAddress4(String remittanceAddress4) {
		this.remittanceAddress4 = remittanceAddress4;
	}
	
	/**
	 * This method returns the Remittance City
	 * @return Remittance City
	 */
	public String getRemittanceCity() {
		return remittanceCity;
	}
	
	/**
	 * This method sets the Remittance City
	 * @param remittanceCity
	 */
	public void setRemittanceCity(String remittanceCity) {
		this.remittanceCity = remittanceCity;
	}
	
	/**
	 * This method returns the Remittance Country
	 * @return Remittance Country
	 */
	public String getRemittanceCountry() {
		return remittanceCountry;
	}
	
	/**
	 * This method sets the Remittance Country
	 * @param remittanceCountry
	 */
	public void setRemittanceCountry(String remittanceCountry) {
		this.remittanceCountry = remittanceCountry;
	}
	
	/**
	 * This method returns the Remittance County
	 * @return Remittance County
	 */
	public String getRemittanceCounty() {
		return remittanceCounty;
	}
	
	/**
	 * This method sets the Remittance County
	 * @param remittanceCounty
	 */
	public void setRemittanceCounty(String remittanceCounty) {
		this.remittanceCounty = remittanceCounty;
	}
	
	/**
	 * This method returns the Remittance State
	 * @return Remittance State
	 */
	public String getRemittanceState() {
		return remittanceState;
	}
	
	/**
	 * This method sets the Remittance State
	 * @param remittanceState
	 */
	public void setRemittanceState(String remittanceState) {
		this.remittanceState = remittanceState;
	}
	
	/**
	 * This method returns the Remittance Postal
	 * @return Remittance Postal
	 */
	public String getRemittancePostal() {
		return remittancePostal;
	}
	
	/**
	 * This method sets the Remittance Postal
	 * @param remittancePostal
	 */
	public void setRemittancePostal(String remittancePostal) {
		this.remittancePostal = remittancePostal;
	}
	
	/**
	 * This method returns the Remittance Contact
	 * @return Remittance Contact
	 */	
	public String getRemittanceContact() {
		return remittanceContact;
	}
	
	/**
	 * This method sets the Remittance Contact
	 * @param remittanceContact
	 */
	public void setRemittanceContact(String remittanceContact) {
		this.remittanceContact = remittanceContact;
	}

	/**
	 * This method returns the Bank Name
	 * @return Bank Name
	 */
	public String getBankName() {
		return bankName;
	}
	
	/**
	 * This method sets the Bank Name
	 * @param bankName
	 */
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	/**
	 * This method returns the Bank Account Number
	 * @return Bank Account Number
	 */
	public String getBankAccountNumber() {
		return bankAccountNumber;
	}
	
	/**
	 * This method sets the Bank Account Number
	 * @param bankAccountNumber
	 */
	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	/**
	 * This method returns the Wire Routing Number
	 * @return Wire Routing Number
	 */
	public String getWireRoutingNumber() {
		return wireRoutingNumber;
	}
	
	
	/**
	 * This method sets the Wire Routing Number
	 * @param wireRoutingNumber
	 */
	public void setWireRoutingNumber(String wireRoutingNumber) {
		this.wireRoutingNumber = wireRoutingNumber;
	}
	
	/**
	 * This method returns the ACH Routing Number
	 * @return ACH Routing Number
	 */
	public String getAchRoutingNumber() {
		return achRoutingNumber;
	}
	
	
	/**
	 * This method sets the ACH Routing Number
	 * @param achRoutingNumber
	 */
	public void setAchRoutingNumber(String achRoutingNumber) {
		this.achRoutingNumber = achRoutingNumber;
	}
	
	/**
	 * This method returns the Email
	 * @return Email
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * This method sets the Email
	 * @param email
	 */
	public void setEmail(String email) {
		this.email = email;
	}
		
	/**
	 * This method returns the Template Mappings
	 * @return Template Mappings
	 */
	public List<CmTemplateMappingData> getTemplateMappings() {
		return templateMappings;
	}
	
	/**
	 * This method sets the Template Mappings
	 * @param templateMappings
	 */
	public void setTemplateMappings(List<CmTemplateMappingData> templateMappings) {
		this.templateMappings = templateMappings;
	}
	
	//Bill Extract Configuration Extendable Lookup	
	/**
	 * This method returns the Customer Number Identifier Type
	 * @return Customer Number Identifier Type
	 */
	public IdType_Id getCustomerNumberIdentifierType() {
		return customerNumberIdentifierType;
	}
	
	/**
	 * This method sets the Customer Identifier Type
	 * @param customerNumberIdentifierType
	 */
	public void setCustomerNumberIdentifierType(IdType_Id customerNumberIdentifierType) {
		this.customerNumberIdentifierType = customerNumberIdentifierType;
	}
	
	/**
	 * This method returns the Transaction SQI
	 * @return Transaction SQI
	 */
	public ServiceQuantityIdentifier_Id getTransactionQuantitySqi() {
		return transactionQuantitySqi;
	}
	
	/**
	 * This method sets the Transaction SQI
	 * @param transactionQuantitySqi
	 */
	public void setTransactionQuantitySqi(ServiceQuantityIdentifier_Id transactionQuantitySqi) {
		this.transactionQuantitySqi = transactionQuantitySqi;
	}
	
	/**
	 * This method returns the Loan Number Char Type
	 * @return Loan Number Char Type
	 */
	public CharacteristicType getLoanNumberCharacteristicType() {
		return loanNumberCharacteristicType;
	}
	
	/**
	 * This method sets the Loan Number Char Type
	 * @param loanNumberCharacteristicType
	 */
	public void setLoanNumberCharacteristicType(CharacteristicType loanNumberCharacteristicType) {
		this.loanNumberCharacteristicType = loanNumberCharacteristicType;
	}
	
	/**
	 * This method returns the Order Number Char Type
	 * @return Order Number Char Type
	 */
	public CharacteristicType getOrderNumberCharacteristicType() {
		return orderNumberCharacteristicType;
	}
	
	/**
	 * This method sets the Order Number Char Type
	 * @param orderNumberCharacteristicType
	 */
	public void setOrderNumberCharacteristicType(CharacteristicType orderNumberCharacteristicType) {
		this.orderNumberCharacteristicType = orderNumberCharacteristicType;
	}
	
	/**
	 * This method returns the Borrower Name Char Type
	 * @return Borrower Name Char Type
	 */
	public CharacteristicType getBorrowerNameCharacteristicType() {
		return borrowerNameCharacteristicType;
	}
	
	/**
	 * This method sets the Borrower Name Char Type
	 * @param borrowerNameCharacteristicType
	 */
	public void setBorrowerNameCharacteristicType(CharacteristicType borrowerNameCharacteristicType) {
		this.borrowerNameCharacteristicType = borrowerNameCharacteristicType;
	}
	
	/**
	 * This method returns the Property Address Char Type
	 * @return Property Address Char Type
	 */
	public CharacteristicType getPropertyAddressCharacteristicType() {
		return propertyAddressCharacteristicType;
	}
	
	/**
	 * This method sets the Property Address Char Type
	 * @param propertyAddressCharacteristicType
	 */
	public void setPropertyAddressCharacteristicType(CharacteristicType propertyAddressCharacteristicType) {
		this.propertyAddressCharacteristicType = propertyAddressCharacteristicType;
	}
	
	/**
	 * This method returns the Property City Char Type
	 * @return Property City Char Type
	 */
	public CharacteristicType getPropertyCityCharacteristicType() {
		return propertyCityCharacteristicType;
	}
	
	/**
	 * This method sets the Property City Char Type
	 * @param propertyCityCharacteristicType
	 */
	public void setPropertyCityCharacteristicType(CharacteristicType propertyCityCharacteristicType) {
		this.propertyCityCharacteristicType = propertyCityCharacteristicType;
	}

	/**
	 * This method returns the Property State Char Type
	 * @return Property State Char Type
	 */
	public CharacteristicType getPropertyStateCharacateristicType() {
		return propertyStateCharacateristicType;
	}
	
	/**
	 * This method sets the Property State Char Type
	 * @param propertyStateCharacateristicType
	 */
	public void setPropertyStateCharacateristicType(CharacteristicType propertyStateCharacateristicType) {
		this.propertyStateCharacateristicType = propertyStateCharacateristicType;
	}
	
	/**
	 * This method returns the Property Zip Char Type
	 * @return Property Zip Char Type
	 */	
	public CharacteristicType getPropertyZipCharacteristicType() {
		return propertyZipCharacteristicType;
	}
	
	/**
	 * This method sets the Property Zip Char Type
	 * @param propertyZipCharacteristicType
	 */
	public void setPropertyZipCharacteristicType(CharacteristicType propertyZipCharacteristicType) {
		this.propertyZipCharacteristicType = propertyZipCharacteristicType;
	}

	/**
	 * This method returns the Payment Terms Char Type
	 * @return Payment Terms Char Type
	 */	
	public CharacteristicType getPaymentTermsCharacteristicType() {
		return paymentTermsCharacteristicType;
	}
	
	/**
	 * This method sets the Payment Terms Char Type
	 * @param paymentTermsCharacteristicType
	 */
	public void setPaymentTermsCharacteristicType(CharacteristicType paymentTermsCharacteristicType) {
		this.paymentTermsCharacteristicType = paymentTermsCharacteristicType;
	}

	/**
	 * This method returns the Charge Line Char Type
	 * @return Charge Line Char Type
	 */
	public CharacteristicType getChargeCalcLineCharacteristicType() {
		return chargeCalcLineCharacteristicType;
	}
	
	/**
	 * This method sets the Charge Calc Line Char Type
	 * @param chargeCalcLineCharacteristicType
	 */
	public void setChargeCalcLineCharacteristicType(CharacteristicType chargeCalcLineCharacteristicType) {
		this.chargeCalcLineCharacteristicType = chargeCalcLineCharacteristicType;
	}

	/**
	 * This method returns the Tax Calc Line Char Val
	 * @return Tax Calc Line Char Val
	 */
	public String getTaxCalcLineCharacteristicValue() {
		return taxCalcLineCharacteristicValue;
	}
	
	/**
	 * This method sets the Tax Calc Line Char Value
	 * @param taxCalcLineCharacteristicValue
	 */
	public void setTaxCalcLineCharacteristicValue(String taxCalcLineCharacteristicValue) {
		this.taxCalcLineCharacteristicValue = taxCalcLineCharacteristicValue;
	}

	/**
	 * This method returns the Discount Calc Line Char Val
	 * @return Discount Calc Line Char Val
	 */
	public String getDiscountCalcLineCharacteristicValue() {
		return discountCalcLineCharacteristicValue;
	}
	
	/**
	 * This method sets the Discount Calc Line Char Value
	 * @param discountCalcLineCharacteristicValue
	 */
	public void setDiscountCalcLineCharacteristicValue(String discountCalcLineCharacteristicValue) {
		this.discountCalcLineCharacteristicValue = discountCalcLineCharacteristicValue;
	}
	
}
