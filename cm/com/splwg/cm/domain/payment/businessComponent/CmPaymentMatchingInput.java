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
 * Payment Matching Input POJO
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-06-12   DDejes      Initial Version. 
 * 2020-07-30	IGarg	    Updated Version for CB-145
 ***********************************************************************
 */
package com.splwg.cm.domain.payment.businessComponent;

import java.util.ArrayList;
import java.util.List;

public class CmPaymentMatchingInput {

	/*
	 * Code Added By Ishita on 24-07-2020 start
	 * Commenting the below line as per the design changes suggested in CB-145 and adding new one
	 * Making billId to billIdList
	 */
    //private String billId;
	//private String orderNumber;
	//private String loanNumber;
	private List<String> billIdList = new ArrayList<String>();
	private List<String> orderNumList = new ArrayList<String>();
	private List<String> loanNumList = new ArrayList<String>();
	/*
	 * Code Added By Ishita on 24-07-2020 end
	 */
	
   
    private List<String> accountIdentifier = new ArrayList<String>();
    private String customerName;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String city;
    private String county;
    private String state;
    private String postal;
    private String country;

	
  
    /*
	 * Code Added By Ishita on 24-07-2020 start
	 * Commenting the below Code as per the design changes suggested in CB-145 and adding new ones
	 * Making billId to billIdList
	 */
	/*public String getBillId(){
		return billId;
	}
	
    public void setBillId(String billId) {
        this.billId = billId;
    }
    public String getOrderNumber(){
		return orderNumber;
	}
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    public String getLoanNumber(){
		return loanNumber;
	}
    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }
    */
    
    public List<String> getBillIdList(){
		return billIdList;
	}
    public void setBillIdList(List<String> billIdList) {
        this.billIdList = billIdList;
    }
    public List<String> getOrderNumList(){
		return orderNumList;
	}
    public void setOrderNumList(List<String> orderNumList) {
        this.orderNumList = orderNumList;
    }
    public List<String> getLoanNumList(){
		return loanNumList;
	}
    public void setLoanNumList(List<String> loanNumList) {
        this.loanNumList = loanNumList;
    }
    /*
	 * Code Added By Ishita on 24-07-2020 end
	 */
	
	public List<String> getAccountIdentifier(){
		return accountIdentifier;
	}
    public void setAccountIdentifier(List<String> accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }
	
	public String getCustomerName(){
		return customerName;
	}
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
	public String getAddress1(){
		return address1;
	}
    public void setAddress1(String address1) {
        this.address1 = address1;
    }
	public String getAddress2(){
		return address2;
	}
    public void setAddress2(String address2) {
        this.address2 = address2;
    }
	public String getAddress3(){
		return address3;
	}
    public void setAddress3(String address3) {
        this.address3 = address3;
    }
	public String getAddress4(){
		return address4;
	}
    public void setAddress4(String address4) {
        this.address4 = address4;
    }
	public String getCity(){
		return city;
	}
    public void setCity(String city) {
        this.city = city;
    }
	public String getCounty(){
		return county;
	}
    public void setCounty(String county) {
        this.county = county;
    }
	public String getState(){
		return state;
	}
    public void setState(String state) {
        this.state = state;
    }
	public String getPostal(){
		return postal;
	}
    public void setPostal(String postal) {
        this.postal = postal;
    }
	public String getCountry(){
		return country;
	}
    public void setCountry(String country) {
        this.country = country;
    }
}
