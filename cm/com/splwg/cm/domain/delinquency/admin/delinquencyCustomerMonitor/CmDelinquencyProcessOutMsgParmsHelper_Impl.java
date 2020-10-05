/*
 **********************************************************************************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * Delinquency Process Refer to CDM Helper Class to get and set outbound message parameters
 **********************************************************************************************************************************************
 * 
 * 
 * CHANGE HISTORY:
 * 
 * Date:        by:         Reason:
 * 2020-05-06	MugdhaP		ANTHM-340 CAB1-9462 Delinquency Framework 
 **********************************************************************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;

/**
 * @author MugdhaP
 *
@BusinessComponent (customizationCallable = true)
 */
public class CmDelinquencyProcessOutMsgParmsHelper_Impl extends
		GenericBusinessComponent implements CmDelinquencyProcessOutMsgParmsHelper {
	//Class Variables
	private CmDelinquencyProcess_Id delinquencyProcessId;
	private String policyId;
	private String policyNumber;
	private String customerId;
	private String customerName;
	private String address1;
	private String address2;
	private String city;
	private String state;
	private String postal;
	private String billGroupContact;
	private String phone;
	private String salesOffice;
	private String customerType;
	private String policyCancelReason;
	private Date policyCancelDate;
	private Money amount;
	private String collectionLoc;
	private String sharedArrangementIndicator;
	private String obligorId;
	public CmDelinquencyProcess_Id getDelinquencyProcessId() {
		return delinquencyProcessId;
	}
	public void setDelinquencyProcessId(CmDelinquencyProcess_Id delinquencyProcessId) {
		this.delinquencyProcessId = delinquencyProcessId;
	}
	public String getPolicyId() {
		return policyId;
	}
	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}
	public String getPolicyNumber() {
		return policyNumber;
	}
	public void setPolicyNumber(String policyNumber) {
		this.policyNumber = policyNumber;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getAddress2() {
		return address2;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getPostal() {
		return postal;
	}
	public void setPostal(String postal) {
		this.postal = postal;
	}
	public String getBillGroupContact() {
		return billGroupContact;
	}
	public void setBillGroupContact(String billGroupContact) {
		this.billGroupContact = billGroupContact;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getSalesOffice() {
		return salesOffice;
	}
	public void setSalesOffice(String salesOffice) {
		this.salesOffice = salesOffice;
	}
	public String getCustomerType() {
		return customerType;
	}
	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}
	public String getPolicyCancelReason() {
		return policyCancelReason;
	}
	public void setPolicyCancelReason(String policyCancelReason) {
		this.policyCancelReason = policyCancelReason;
	}
	public Date getPolicyCancelDate() {
		return policyCancelDate;
	}
	public void setPolicyCancelDate(Date policyCancelDate) {
		this.policyCancelDate = policyCancelDate;
	}
	public Money getAmount() {
		return amount;
	}
	public void setAmount(Money amount) {
		this.amount = amount;
	}
	public String getCollectionLoc() {
		return collectionLoc;
	}
	public void setCollectionLoc(String collectionLoc) {
		this.collectionLoc = collectionLoc;
	}
	public String getSharedArrangementIndicator() {
		return sharedArrangementIndicator;
	}
	public void setSharedArrangementIndicator(String sharedArrangementIndicator) {
		this.sharedArrangementIndicator = sharedArrangementIndicator;
	}
	public String getObligorId() {
		return obligorId;
	}
	public void setObligorId(String obligorId) {
		this.obligorId = obligorId;
	}
}
