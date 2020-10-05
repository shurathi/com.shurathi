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
 * This POJO Class has setter and getter methods for the customer extended lookup configuration 
 *   - Person
 *   - Account 
 *   - Contract
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-04-13   VLaksh/JFerna        CB-10. Initial Version. 
 * 2020-05-20	DDejes				 CB-75. Added should receive 
 * 									 notification.
 * 2020-05-28 	JFerna	             CB-70. Remove Bill Route Type 
 *                                   defaulting during Customer 
 *                                   Interface
 * 2020-06-30	DDejes			     CB-132. Added Default Characteristics                                  
 * 2020-07-31	KGhuge				 CB-54. Capture Statement Construct during Customer Interface	                                  
 ***********************************************************************
 */
package com.splwg.cm.domain.customer.utility;

import java.util.List;

import com.splwg.ccb.api.lookup.AutoPayMethodLookup;
import com.splwg.ccb.api.lookup.AutoPayTypeFlgLookup;
import com.splwg.ccb.api.lookup.BillFormatLookup;
import com.splwg.ccb.api.lookup.BillingAddressSourceLookup;
import com.splwg.ccb.api.lookup.NameTypeLookup;
import com.splwg.ccb.api.lookup.PersonOrBusinessLookup;
import com.splwg.ccb.api.lookup.StatementLinkTypeLookup;
import com.splwg.ccb.api.lookup.StatementFormatLookup;


public class CmCustInterfaceExtLookupVO {

	private String customerClass;
	private String division;
	private String accessGroup;
	private String phoneType;
	private PersonOrBusinessLookup personTypeLookup;
	private NameTypeLookup nameTypeLookup;
	private List<String> saTypeList;
	private String idType;
	private String personPersonRelationshipType;
	private String financialRelationshipSwitch;
	private String currency;
	private String shouldReceiveCopyOfBill;
	private BillingAddressSourceLookup billAddressSource;
	private BillFormatLookup billFormat;
	//CB-70 - Start Delete
	//private String billRouteType;
	//CB-70 - End Delete
	private String accountIdType;
	private String accountRelationshipType;
	private String apaySourceCode;
	private String apayRouteType;
	private String billCycle;
	private String collectionClass;
	private AutoPayTypeFlgLookup autopayType;
	private AutoPayMethodLookup autopayMethod;
	//Start Add-CB-75
	private String shouldReceiveNotification;	
	//End Add-CB-75
	//Start Add - CB-132
	private List<CmCharData> charDefault;
	//End Add - CB-132
		
	//Start Add - CB-54
	private Number nbrOfCopies;
	private String statementDescription;
	private StatementFormatLookup statementFormat;
	private StatementLinkTypeLookup statementDetailType;
	private String statementDetailDescription;
	//End Add CB-54
	
	public PersonOrBusinessLookup getPersonTypeLookup() {
		return personTypeLookup;
	}
	public void setPersonTypeLookup(PersonOrBusinessLookup personTypeLookup) {
		this.personTypeLookup = personTypeLookup;
	}
	public NameTypeLookup getNameTypeLookup() {
		return nameTypeLookup;
	}
	public void setNameTypeLookup(NameTypeLookup nameTypeLookup) {
		this.nameTypeLookup = nameTypeLookup;
	}
	public String getCustomerClass() {
		return customerClass;
	}
	public void setCustomerClass(String customerClass) {
		this.customerClass = customerClass;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getAccessGroup() {
		return accessGroup;
	}
	public void setAccessGroup(String accessGroup) {
		this.accessGroup = accessGroup;
	}
	public String getPhoneType() {
		return phoneType;
	}
	public void setPhoneType(String phoneType) {
		this.phoneType = phoneType;
	}
	public List<String> getSaTypeList() {
		return saTypeList;
	}
	public void setSaTypeList(List<String> saTypeList) {
		this.saTypeList = saTypeList;
	}
	public String getIdType() {
		return idType;
	}
	public void setIdType(String idType) {
		this.idType = idType;
	}
	public String getPersonPersonRelationshipType() {
		return personPersonRelationshipType;
	}
	public void setPersonPersonRelationshipType(String personPersonRelationshipType) {
		this.personPersonRelationshipType = personPersonRelationshipType;
	}
	public String getFinancialRelationshipSwitch() {
		return financialRelationshipSwitch;
	}
	public void setFinancialRelationshipSwitch(String financialRelationshipSwitch) {
		this.financialRelationshipSwitch = financialRelationshipSwitch;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getShouldReceiveCopyOfBill() {
		return shouldReceiveCopyOfBill;
	}
	public void setShouldReceiveCopyOfBill(String shouldReceiveCopyOfBill) {
		this.shouldReceiveCopyOfBill = shouldReceiveCopyOfBill;
	}
	
	//Start Add-CB-75
	public String getShouldReceiveNotification() {
		return shouldReceiveNotification;
	}
	public void setShouldReceiveNotification(String shouldReceiveNotification) {
		this.shouldReceiveNotification = shouldReceiveNotification;
	}
	//End Add-CB-75
	
	public BillingAddressSourceLookup getBillAddressSource() {
		return billAddressSource;
	}
	public void setBillAddressSource(BillingAddressSourceLookup billAddressSource) {
		this.billAddressSource = billAddressSource;
	}
	public BillFormatLookup getBillFormat() {
		return billFormat;
	}
	public void setBillFormat(BillFormatLookup billFormat) {
		this.billFormat = billFormat;
	}
	//CB-70 - Start Delete				   
	/*public String getBillRouteType() {
		return billRouteType;
	}
	public void setBillRouteType(String billRouteType) {
		this.billRouteType = billRouteType;
	}*/
	//CB-70 - End Delete				 
	public String getAccountIdType() {
		return accountIdType;
	}
	public void setAccountIdType(String accountIdType) {
		this.accountIdType = accountIdType;
	}
	public String getAccountRelationshipType() {
		return accountRelationshipType;
	}
	public void setAccountRelationshipType(String accountRelationshipType) {
		this.accountRelationshipType = accountRelationshipType;
	}
	public String getApaySourceCode() {
		return apaySourceCode;
	}
	public void setApaySourceCode(String apaySourceCode) {
		this.apaySourceCode = apaySourceCode;
	}
	public String getApayRouteType() {
		return apayRouteType;
	}
	public void setApayRouteType(String apayRouteType) {
		this.apayRouteType = apayRouteType;
	}
	
	public String getBillCycle() {
		return billCycle;
	}
	public void setBillCycle(String billCycle) {
		this.billCycle = billCycle;
	}
	public String getCollectionClass() {
		return collectionClass;
	}
	public void setCollectionClass(String collectionClass) {
		this.collectionClass = collectionClass;
	}
	public AutoPayTypeFlgLookup getAutopayType() {
		return autopayType;
	}
	public void setAutopayType(AutoPayTypeFlgLookup autopayType) {
		this.autopayType = autopayType;
	}
	public AutoPayMethodLookup getAutopayMethod() {
		return autopayMethod;
	}
	public void setAutopayMethod(AutoPayMethodLookup autopayMethod) {
		this.autopayMethod = autopayMethod;
	}
	//Start Add - CB-132
	public List<CmCharData> getCharDefault() {
		return charDefault;
	}
	public void setCharDefault(List<CmCharData> charDefault) {
		this.charDefault = charDefault;
	}
	//End Add - CB-132
	
	//Start Add - CB-54
	public void setNbrOfCopies(Number nbrOfCopies){
		this.nbrOfCopies = nbrOfCopies;
	}
	public void setStatementDescription(String statementDescription){
		this.statementDescription = statementDescription;
	}
	public void setStatementFormat(StatementFormatLookup statementFormat){
		this.statementFormat = statementFormat;
	}
	public void setStatementDetailType(StatementLinkTypeLookup statementDetailType){
		this.statementDetailType = statementDetailType;
	}
	public void setStatementDetailDescription(String statementDetailDescription){
		this.statementDetailDescription = statementDetailDescription;
	}
	public Number getNbrOfCopies(){
		return this.nbrOfCopies;
	}
	public String getStatementDescription(){
		return this.statementDescription;
	}
	public StatementFormatLookup getStatementFormat(){
		return this.statementFormat;
	}
	public StatementLinkTypeLookup getStatementDetailType(){
		return this.statementDetailType;
	}
	public String getStatementDetailDescription(){
		return this.statementDetailDescription;
	}
	//End Add - CB-54

}
