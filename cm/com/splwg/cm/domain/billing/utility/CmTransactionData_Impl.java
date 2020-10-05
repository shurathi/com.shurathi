/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * Transaction Data Business Component
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-06-11   JFerna      CB-94. Initial Version. 
***********************************************************************
 */
package com.splwg.cm.domain.billing.utility;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.Query;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.ccb.domain.billing.billableCharge.BillableCharge;
import com.splwg.ccb.domain.billing.billableCharge.BilllableChargeCharacteristic;

/**
 * @author JFerna
 *
@BusinessComponent ()
 */
public class CmTransactionData_Impl extends GenericBusinessComponent implements CmTransactionData {
	
	//Work Variables
    private String productDescription;
    private String orderNumber;
    private String transactionDate;
    private String loanNumber;
    private String borrowerName;
    private String propertyAddress;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal tax;
    private BigDecimal amount;
    
	/**
	 * This method returns the Product Description
	 * @return Product Description
	 */
    public String getProductDescription() {
        return productDescription;
    }

	/**
	 * This method sets the Product Description
	 * @param productDescription
	 */
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

	/**
	 * This method returns the Order Number
	 * @return Order Number
	 */
    public String getOrderNumber() {
        return orderNumber;
    }

	/**
	 * This method sets the Order Number
	 * @param billableCharge
	 * @param orderNumberCharType
	 * @param effectiveDate
	 */
    public void setOrderNumber(BillableCharge billableCharge, CharacteristicType orderNumberCharType, Date effectiveDate) {
        this.orderNumber = retrieveBillableChargeCharacteristic(billableCharge,orderNumberCharType,effectiveDate);
    }
    
	/**
	 * This method returns the Transaction Date
	 * @return Transaction Date
	 */
    public String getTransactionDate() {
        return transactionDate;
    }

	/**
	 * This method sets the Transaction Date
	 * @param transactionDate
	 */
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
    
	/**
	 * This method returns the Loan Number
	 * @return Loan Number
	 */
    public String getLoanNumber() {
        return loanNumber;
    }

	/**
	 * This method sets the Loan Number
	 * @param billableCharge
	 * @param loanNumberCharType
	 * @param effectiveDate
	 */
    public void setLoanNumber(BillableCharge billableCharge, CharacteristicType loanNumberCharType, Date effectiveDate) {
        this.loanNumber = retrieveBillableChargeCharacteristic(billableCharge,loanNumberCharType,effectiveDate);
    }
    
	/**
	 * This method returns the Borrower Name
	 * @return Borrower Name
	 */
    public String getBorrowerName() {
        return borrowerName;
    }

	/**
	 * This method sets the Borrower Name
	 * @param billableCharge
	 * @param borrowerNameCharType
	 * @param effectiveDate
	 */
    public void setBorrowerName(BillableCharge billableCharge, CharacteristicType borrowerNameCharType, Date effectiveDate) {
        this.borrowerName = retrieveBillableChargeCharacteristic(billableCharge,borrowerNameCharType,effectiveDate);
    }
    
	/**
	 * This method returns the Property Address
	 * @return Property Address
	 */
    public String getPropertyAddress() {
        return propertyAddress;
    }

	/**
	 * This method sets the Property Address
	 * @param billableCharge
	 * @param propertyAddressCharType
	 * @param effectiveDate
	 */
    public void setPropertyAddress(BillableCharge billableCharge, CharacteristicType propertyAddressCharType, Date effectiveDate) {
        this.propertyAddress = retrieveBillableChargeCharacteristic(billableCharge,propertyAddressCharType,effectiveDate);;
    }
    
	/**
	 * This method returns the Quantity
	 * @return Quantity
	 */
    public BigDecimal getQuantity() {
        return quantity;
    }

	/**
	 * This method sets the Quantity
	 * @param quantity
	 */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
	/**
	 * This method returns the Rate
	 * @return Rate
	 */
    public BigDecimal getRate() {
        return rate;
    }

	/**
	 * This method sets the Rate
	 * @param rate
	 */
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
    
	/**
	 * This method returns the Tax
	 * @return Tax
	 */
    public BigDecimal getTax() {
        return tax;
    }

	/**
	 * This method sets the Tax
	 * @param tax
	 */
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }
    
	/**
	 * This method returns the Amount
	 * @return Amount
	 */
    public BigDecimal getAmount() {
        return amount;
    }

	/**
	 * This method sets the Amount
	 * @param amount
	 */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    /**
     * This method retrieves effective billable charge characteristic
     * @param billableCharge
     * @param charType
     * @param effectiveDate
     * @return billableChargeCharVal
     */
	private String retrieveBillableChargeCharacteristic(BillableCharge billableCharge, CharacteristicType charType, Date effectiveDate){
		String billableChargeCharVal = null;
		CharacteristicTypeLookup charTypeLookup = charType.getCharacteristicType();
		
		//Retrieve from Bill Charge Characteristics
		Query<BilllableChargeCharacteristic> getBillableChargeCharacteristicQry = createQuery(
				"FROM BilllableChargeCharacteristic billChargeChar " +
				"WHERE billChargeChar.id.billableCharge = :billableCharge " +
				"  AND billChargeChar.id.characteristicType = :charType " +
				"  AND billChargeChar.id.effectiveDate = ( " +
				"		SELECT MAX(billChargeChar2.id.effectiveDate) " +
				"		FROM BilllableChargeCharacteristic billChargeChar2 " +
				"		WHERE billChargeChar2.id.billableCharge = billChargeChar.id.billableCharge " +
				"		AND billChargeChar2.id.characteristicType = billChargeChar.id.characteristicType " +
				"		AND billChargeChar2.id.effectiveDate <= :effectiveDate " +
				"  ) " , "");
		
		getBillableChargeCharacteristicQry.bindEntity("billableCharge", billableCharge);
		getBillableChargeCharacteristicQry.bindEntity("charType", charType);
		getBillableChargeCharacteristicQry.bindDate("effectiveDate", effectiveDate);
		
		BilllableChargeCharacteristic billableChargeChar = getBillableChargeCharacteristicQry.firstRow();
		if(notNull(billableChargeChar)){
			if (charTypeLookup.isPredefinedValue()){
				billableChargeCharVal = billableChargeChar.getCharacteristicValue();
			}
			else if (charTypeLookup.isForeignKeyValue()){
				billableChargeCharVal = billableChargeChar.getCharacteristicValueForeignKey1();
			}
			else {
				billableChargeCharVal = billableChargeChar.getAdhocCharacteristicValue();
			}			
		}
		
		return billableChargeCharVal;
	}
}
