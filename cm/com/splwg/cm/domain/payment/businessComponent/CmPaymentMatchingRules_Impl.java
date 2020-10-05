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
 * Payment Matching Rules Business Component
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                Reason:
 * 2020-06-12   DDejes/JRaymu      Initial Version. 
 * 2020-07-30	IGarg			   Updated Version for CB-145
 ***********************************************************************
 */
package com.splwg.cm.domain.payment.businessComponent;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.country.Country;
import com.splwg.ccb.api.lookup.BillSegmentStatusLookup;
import com.splwg.ccb.api.lookup.MatchEventStatusLookup;
import com.splwg.ccb.domain.admin.matchType.MatchType;
import com.splwg.ccb.domain.admin.tenderSource.TenderSource;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.ccb.domain.billing.billSegment.BillSegment_Id;
import com.splwg.ccb.domain.billing.billableCharge.BilllableChargeCharacteristic;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonName;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author Denise De Jesus
 *
@BusinessComponent ()
 */
public class CmPaymentMatchingRules_Impl extends GenericBusinessComponent implements CmPaymentMatchingRules {

    //Work Variables
    private CmPaymentMatchingInput paymentMatchingInput = new CmPaymentMatchingInput();
    Logger logger = LoggerFactory.getLogger(CmPaymentMatchingRules_Impl.class);
    /*
     * Code changed By Ishita on 24-07-2020 start
     * CB-145 Design changes
     */
   // private String billId;
    //private String orderNumber;
    //private String loanNumber;
    private List<String> billIdList = new ArrayList<String>();
    private List<String> orderNumList = new ArrayList<String>();
    private List<String> loanNumList = new ArrayList<String>();
    List<Account_Id> accountList = new ArrayList<Account_Id>();
    private Account_Id noBillAccountId = null;
   // Code changed By Ishita on 24-07-2020 end
    
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
    private Money tenderAmount;
    private Date accountingDate;
    private String externalSourceId;
    private String payorPrimaryAccountIdentifier;
    private String payorAccountId;
    private List<CmPaymentServiceData> matchDetails = new ArrayList<CmPaymentServiceData>();
    private String payorAccount;
    private Account_Id suspenseAccount; // Added By Ishita - CB-145
    private String suspenseCustomerClass; // Added By Ishita - CB-145
    private String matchRuleFlag; // Added By Ishita CB-145

    private MatchType matchByBillMatchType;
    private MatchType matchByBillSegmentMatchType;
    private MatchType matchByAccountMatchType;
    private MatchType applyToGeneralSuspenseMatchType;
    private CharacteristicType orderNumberCharacteristicType;
    private CharacteristicType loanNumberCharacteristicType;
    private Country defaultCountry;
    
    public void setPaymentMatchingInput(CmPaymentMatchingInput paymentMatchingInput){
    	this.paymentMatchingInput = paymentMatchingInput;
    }
    
    public void setTenderAmount(Money tenderAmount) {
        this.tenderAmount = tenderAmount;
    }

    public void setAccountingDate(Date accountingDate) {
        this.accountingDate = accountingDate;
    }

    public void setExternalSourceId(String externalSourceId) {
        this.externalSourceId = externalSourceId;
    }

    public void setPayorPrimaryAccountIdentifier(String payorPrimaryAccountIdentifier) {
        this.payorPrimaryAccountIdentifier = payorPrimaryAccountIdentifier;
    }

    public String getPayorAccountId() {
    	logger.info("get payor accout id :"+payorAccountId);
        return payorAccountId;
    }

    public List<CmPaymentServiceData> getMatchDetails() {
        return matchDetails;
    }

    /**
     * Determine Matching Rule
     */
    public void determineMatchingRule() {
        retrievePaymentMatchingInput();
        initializeMasterConfigValues();
        validateInputParameters();
        retrievePayorAcctId();
        determineSuspenceAccount();
        suspenseCustomerClass = suspenseAccount.getEntity().getCustomerClass().getId().getIdValue().trim();
        logger.info("suspenseCustomerClass "+suspenseCustomerClass);

        /*
         * Changes done by Ishita on 24-07-2020 start
         * Replacing the bill Id with Bill Id List (CB-145)
         */
        //if (isBlankOrNull(billId) && isBlankOrNull(orderNumber) && accountIdentifier.isEmpty() && isBlankOrNull(loanNumber)) {
        if (billIdList.isEmpty() && orderNumList.isEmpty() && accountIdentifier.isEmpty() && loanNumList.isEmpty()) {
        /*
         * Changes done by Ishita on 24-07-2020 end
         */
            if (!isBlankOrNull(customerName) && !isBlankOrNull(address1) && !isBlankOrNull(city) && !isBlankOrNull(state)
                    && !isBlankOrNull(postal)) {
            	logger.info("Inside first If for Customer name address");
                customerNameAddrMatchRule();
            } else {
                generalSuspenseMatchRule();
            }
        }
        
        if (matchDetails.isEmpty()) {
            billMatchRule();
        }
        if (matchDetails.isEmpty()) {
            orderNumberMatchRule();
        }
        if (matchDetails.isEmpty()) {
            accountIdentifierMatchRule();
        }
        if (matchDetails.isEmpty()) {
           customerNameAddrMatchRule();
        }
        if (matchDetails.isEmpty()) {
            loanNumberMatchRule();
        }
        if (matchDetails.isEmpty()) {
            generalSuspenseMatchRule();
        }
    }
    
    /**
     * Validations
     */
    private void validateInputParameters() {
       
        if (isNull(accountingDate)) {
            addError(CmMessageRepository.getServerMessage(CmMessages.MATCH_RULE_PARM_REQUIRED, "Accounting Date"));
        }
        if (isNull(tenderAmount)) {
            addError(CmMessageRepository.getServerMessage(CmMessages.MATCH_RULE_PARM_REQUIRED, "Tender Amount"));
        }
    }

    /**
     * Retrieve Payor Account Id
     */
    private void retrievePayorAcctId() {
    	payorAccount = null;
        if (!isBlankOrNull(payorPrimaryAccountIdentifier)) {
        	Account_Id acctId = retrieveAcctId(payorPrimaryAccountIdentifier);
        	logger.info("Payor Account Id "+acctId);
        	System.out.println("Payor Account Id "+acctId);
            if (notNull(acctId)) {
                payorAccount = acctId.getIdValue();
            } 
        }
    }

    
    
  /*
   *   Made changed for CB-145 start 
   *   Commenting below method
   *   *//**
     * Bill Match Rule
     *//*
    private void billMatchRule() {
        Money totalBillAmount = Money.ZERO;
        Bill_Id billIdEntity;
        if (!isBlankOrNull(billId)) {
            billIdEntity = new Bill_Id(billId);
            if (notNull(billIdEntity.getEntity())) {
                Bill billEntity = billIdEntity.getEntity();
                totalBillAmount = determineBillAmount(billIdEntity);
               
                if (totalBillAmount.isLessThanOrEqual(tenderAmount)) {
                    setOutputMatchDetails(matchByBillMatchType.getId().getIdValue(), billId, tenderAmount);
                } else {
                  
                    setOutputMatchDetails(matchByAccountMatchType.getId().getIdValue(), billEntity.getAccount().getId()
                            .getIdValue(), tenderAmount);
                }          

                if (!isBlankOrNull(payorAccount)) {
                    payorAccountId = payorAccount;
                } else {
                    payorAccountId = billEntity.getAccount().getId().getIdValue();
                }
            }
        }
    }*/
    
    // CB-145 changes end
    
    /*
     * Code Added By Ishita Start
     * CB-145
     */
    private void billMatchRule() {
    	 Money totalBillAmount = Money.ZERO;
         Bill_Id billIdEntity;
         if (!billIdList.isEmpty()) 
         {
        	 List<Bill_Id> billList = new ArrayList<Bill_Id>();
        	 for(String billId : billIdList)
     		 {
        		 logger.info("Inside For new Bill Match Rule :"+billId);
        		 billIdEntity = new Bill_Id(billId);
        		 if (notNull(billIdEntity.getEntity())) 
        		 {
        			 billList.add(new Bill_Id(billId));
        			 totalBillAmount = totalBillAmount.add(determineBillAmount(billIdEntity));
        		 }
        		 else
        		 {
        			 addError(CmMessageRepository.entityDoesNotExist(billId));
        		 }
         	}
        	 Collections.sort(billList, new BillSorting());
        	 matchToOpenBills(null,billList, totalBillAmount);
         }
         else
         {
        	 return;
         }
    }
    // CB-145 changes end
    
   /* 
    * Changes for CB-145 start
    * Commenting the below method
    * *//**
     * Match to Open Bills
     * @param firstAcctId
     * @param billList
     * @param totalBillAmount
     *//*
    private void matchToOpenBills(Account_Id firstAcctId, List<Bill_Id> billList, Money totalBillAmount) {
        Money remainingBillAmount = Money.ZERO;
        Bill_Id billId;
        Money currentBillAmount = Money.ZERO;
        int ctr = 1;
      
        if (totalBillAmount.isLessThanOrEqual(tenderAmount)) {
            remainingBillAmount = tenderAmount;
            for (Bill_Id billListNode : billList) {
                billId = billListNode;
                currentBillAmount = determineBillAmount(billId);

                if (ctr == billList.size()) {                   
                    setOutputMatchDetails(matchByBillMatchType.getId().getIdValue(), billId.getIdValue(),
                            remainingBillAmount);
                } else {
                    setOutputMatchDetails(matchByBillMatchType.getId().getIdValue(), billId.getIdValue(), currentBillAmount);
                }
                remainingBillAmount = remainingBillAmount.subtract(currentBillAmount);

                ctr++;
            }
        } else {
            setOutputMatchDetails(matchByAccountMatchType.getId().getIdValue(), firstAcctId.getIdValue(), tenderAmount);
        }
        if (notNull(payorAccount)) {
            payorAccountId = payorAccount;
        } else {
            payorAccountId = firstAcctId.getIdValue();
        }

    }*/
    // Changes for CB-145 end
    
   /* 
    * Match to Open Bills
    * @param acctList
    * @param billList
    * @param totalBillAmount
    */
    // Changes for CB-145 start
   private void matchToOpenBills(List<Account_Id> acctList,List<Bill_Id> billList,Money totalBillAmount)
   {
	   logger.info("inside match to open bills");
	   Money remainingBillAmount = Money.ZERO;
      // Bill_Id billId;
       Money currentBillAmount = Money.ZERO;
       int ctr = 1;
       Money excessAmount = Money.ZERO;
       Money payAmt = Money.ZERO;
       boolean hasExcessApplied=false;
       Account_Id acctFromLastBill = null;
       List<Bill_Id> otherBillList = new ArrayList<Bill_Id>();
       Money billAmount = Money.ZERO;
     
       if (totalBillAmount.isLessThanOrEqual(tenderAmount)) {
    	   excessAmount = tenderAmount.subtract(totalBillAmount);
    	   if(excessAmount.isZero())
    	   {
    		   hasExcessApplied = true;
    	   }
    	   for(Bill_Id billId : billList)
    	   {
    		   currentBillAmount = determineBillAmount(billId);
    		   Account_Id acctId = billId.getEntity().getAccount().getId();
    		   if(isNull(noBillAccountId))
    		   {
    			   logger.info("acct is cust class :"+acctId.getEntity().getCustomerClass().getId().getIdValue());
    			   if(suspenseCustomerClass.equals(acctId.getEntity().getCustomerClass().getId().getIdValue().trim()) && !hasExcessApplied)
    			   {
    					   payAmt = excessAmount.add(currentBillAmount);
    					   hasExcessApplied = true;
    			   }
    			   else if (ctr == billList.size() && hasExcessApplied == false) {      
    				   payAmt = excessAmount.add(currentBillAmount);
    			   }
    			   else
    			   {
    				   payAmt = currentBillAmount;
    			   }
    		   }
    		   else
			   {
				   payAmt = currentBillAmount;
			   }
    		   acctFromLastBill = acctId;
    		   setOutputMatchDetails(matchByBillMatchType.getId().getIdValue(), billId.getIdValue(),payAmt,acctId.getIdValue());
    		   ctr++;
    	   }
    	   if(!isNull(noBillAccountId))
    	   {
    		   payAmt = excessAmount;
    		   setOutputMatchDetails(matchByAccountMatchType.getId().getIdValue(),noBillAccountId.getIdValue(),payAmt,noBillAccountId.getIdValue());
    	   }
    	   if(isNull(payorAccount))
    	   {
    		   if(!isNull(acctFromLastBill))
    		   {  
    			   payorAccountId = acctFromLastBill.getIdValue();
    		   }
    	   }
    	   else
    	   {
    		   payorAccountId = payorAccount;
    	   }
    	   
       }
       else if("accountIden".equals(matchRuleFlag) || "custNameAdd".equals(matchRuleFlag))
       {
    	   Account_Id lastAcctId = null;
    	   if(!isNull(acctList))
    	   {
    		   int count = 1;
    		   for(Account_Id acctId : acctList)
    		   {
    			   if(acctId.getEntity().getCustomerClass().getId().getIdValue().trim().equals(suspenseCustomerClass) || count == acctList.size())
    			   {
    				   lastAcctId = acctId;
    				   setOutputMatchDetails(matchByAccountMatchType.getId().getIdValue(), acctId.getIdValue(), tenderAmount, acctId.getIdValue());
    				   break;
    			   }
    			   count++;
    		   }
    		   if(!isNull(payorAccount))
    		   {
    			   payorAccountId = payorAccount;
    		   }
    		   else
    		   {
    			   payorAccountId = lastAcctId.getIdValue();
    		   }
    	   }
       }
       else
       {
    	   remainingBillAmount = tenderAmount;
    	   String lastAcctId = null;
    	   for(Bill_Id bill : billList)
    	   {
    		   if(suspenseCustomerClass.equals(bill.getEntity().getAccount().getCustomerClass().getId().getIdValue().trim()))
    		   {
    			 billAmount = determineBillAmount(bill);
    			 if(remainingBillAmount.isGreaterThanOrEqual(billAmount))
    			 {
    				 payAmt = billAmount;
    				 remainingBillAmount = remainingBillAmount.subtract(billAmount);
    				 setOutputMatchDetails(matchByBillMatchType.getId().getIdValue(), bill.getIdValue(), payAmt, bill.getEntity().getAccount().getId().getIdValue());
    				 if(remainingBillAmount.isZero())
    				 {
    					 lastAcctId = bill.getEntity().getAccount().getId().getIdValue();
    					 break;
    				 }
    			 }
    			 else
    			 {
    				 payAmt = remainingBillAmount;
    				 setOutputMatchDetails(matchByBillMatchType.getId().getIdValue(), bill.getIdValue(), payAmt, bill.getEntity().getAccount().getId().getIdValue());
    			 }
				 
    		   }
    		   else
    		   {
    			   otherBillList.add(bill);
    		   }
    		   lastAcctId = bill.getEntity().getAccount().getId().getIdValue();
    	   }
    	   if(remainingBillAmount.isGreaterThan(Money.ZERO) && !otherBillList.isEmpty())
    	   {
    		   for(Bill_Id billId : otherBillList)
    		   {
    			  billAmount = determineBillAmount(billId);
    			  if(remainingBillAmount.isGreaterThanOrEqual(billAmount))
    			  {
    				  payAmt = billAmount;
    				  remainingBillAmount = remainingBillAmount.subtract(billAmount);
    				  setOutputMatchDetails(matchByBillMatchType.getId().getIdValue(), billId.getIdValue(), payAmt, billId.getEntity().getAccount().getId().getIdValue());
    				  if(remainingBillAmount.isZero())
    				  {
    					  break;
    				  }
    			  }
    			  else
    			  {
    				  payAmt = remainingBillAmount;
    				  setOutputMatchDetails(matchByBillMatchType.getId().getIdValue(), billId.getIdValue(), payAmt, billId.getEntity().getAccount().getId().getIdValue());
    				  break;
    			  }
    			 // lastAcctId = billId.getEntity().getAccount().getId().getIdValue();
    		   }
    	   }
    	   if(!isNull(payorAccount))
		   {
			   payorAccountId = payorAccount;
		   }
		   else
		   {
			   payorAccountId = lastAcctId;
		   }
       }
       logger.info("Payor Account Id from match rule :"+payorAccountId);
   }
   // Changes for CB-145 end
   
 /* 
  * Changes done for CB-145 start
  * Commenting the below method
  *  *//**
    * Order Number Match Rule
    *//*
   private void orderNumberMatchRule() {
       BillSegment_Id bsegIdEntity = null;
       bsegIdEntity = retrieveBseg(orderNumberCharacteristicType, orderNumber);
       if (notNull(bsegIdEntity)) {
               matchToBillSegment(bsegIdEntity);
       }
   }*/
//Changes done for CB-145 end
   
   /**
    * Order Number Match Rule
    * CB-145 start
    */
   private void orderNumberMatchRule() {
       BillSegment_Id bsegIdEntity = null;
       List<BillSegment_Id> bsegIdList = new ArrayList<BillSegment_Id>();
       BigDecimal totalBillSegAmt =BigDecimal.ZERO;
       /*
        * Code Added By Ishita on 24-07-2020 start
        * For design changes CB-145
        */
       if(!orderNumList.isEmpty())
       {
    	   for(String orderNumber : orderNumList)
    	   {
    		   bsegIdEntity = retrieveBseg(orderNumberCharacteristicType, orderNumber);
    		   if (notNull(bsegIdEntity)) 
    		   {
    			   bsegIdList.add(bsegIdEntity);
    			   totalBillSegAmt = totalBillSegAmt.add(determineBsegAmount(bsegIdEntity).getAmount());
    		   }
    		   else
    			   return;
	   		}
    	   matchToBillSegment(bsegIdList,totalBillSegAmt);
       }
	  
   }
   //  Code Added By Ishita for CB-145 end
   
 /* 
  * * Changes done for CB-145 start
  * Commenting the below method 
  * *//**
    * Account Identifier Match Rule
    *//*
   private void accountIdentifierMatchRule() {
   
       Account_Id accountId = null;
       Account_Id firstAccountId = null;
       Money totalBillAmount = Money.ZERO;
       List<Bill_Id> billList = new ArrayList<Bill_Id>();
       if (!accountIdentifier.isEmpty()) {
           for (int ctr = 0; ctr < accountIdentifier.size(); ctr++) {
           	accountId =retrieveAcctId(accountIdentifier.get(ctr));
               if (notNull(accountId)) {
                   if (ctr == 0) {
                       firstAccountId = accountId;
                   }
                   for (Bill_Id openBillNode : determineOpenBill(accountId)) {
                       billList.add(openBillNode);
                       totalBillAmount = totalBillAmount.add(determineBillAmount(openBillNode));
                   }
               } else {
                   return;
               }
           }
           matchToOpenBills(firstAccountId, billList, totalBillAmount);
       }
   }*/
   
   // CB-145 end

    /*
     * Code Added By Ishita for CB-145 start
     */
    /**
     * Account Identifier Match Rule
     */
    private void accountIdentifierMatchRule() {
    	matchRuleFlag = "accountIden";
        Account_Id accountId = null;
        Money totalBillAmount = Money.ZERO;
        
        accountList = new ArrayList<Account_Id>();
        List<Bill_Id> billList = new ArrayList<Bill_Id>();
        if (!accountIdentifier.isEmpty()) {
            for (int ctr = 0; ctr < accountIdentifier.size(); ctr++) {
            	boolean flag = false;
            	accountId =retrieveAcctId(accountIdentifier.get(ctr));
            	accountList.add(accountId);
                if (notNull(accountId)) {
                    /*if (ctr == 0) {
                        firstAccountId = accountId;
                    }*/
                    for (QueryResultRow openBillNode : determineOpenBill(accountId)) {
                    	flag = true;
                        billList.add((Bill_Id) openBillNode.get("billId"));
                        totalBillAmount = totalBillAmount.add(determineBillAmount((Bill_Id)openBillNode.get("billId")));
                    }
                    if(flag == false && isNull(noBillAccountId))
                    {
                    	noBillAccountId = accountId;
                    }
                } 
                else {
                    return;
                }
            }
            matchToOpenBills(accountList,billList,totalBillAmount);
        }
    }
    /*
     * Code Added By Ishita for CB-145 end
     */
    
/* 
 * * Changes done for CB-145 start
  * Commenting the below method
 *    *//**
     * Customer Name and Address Match Rule
     *//*
    private void customerNameAddrMatchRule() {
        if (!isBlankOrNull(customerName) && !isBlankOrNull(address1) && !isBlankOrNull(city) && !isBlankOrNull(state)
                && !isBlankOrNull(postal)) {
            List<Account_Id> accountList = retrieveAcctsCusInfo(customerName);
            // Proceed to the next level if no account found
            if (!accountList.isEmpty()) {

            	Money totalBillAmount = Money.ZERO;
            	List<Bill_Id> billList = new ArrayList<>();
            	Account_Id firstAccountId = null;
            	for (Account_Id acctId : accountList) {
            		if (isNull(firstAccountId)) {
            			firstAccountId = acctId;
            		}
            		billList = determineOpenBill(acctId);
            		for (Bill_Id openBillId : billList) {
            			totalBillAmount = totalBillAmount.add(determineBillAmount(openBillId));
            		}
            	}
            	matchToOpenBills(firstAccountId, billList, totalBillAmount);
            }
        }
    }*/
    // Changes done for CB-145 end

/*
 * Code Added By Ishita for CB-145 start
 */
    /**
     * Customer Name and Address Match Rule
     */
    private void customerNameAddrMatchRule() {
    	matchRuleFlag = "custNameAdd";
    	if (!isBlankOrNull(customerName) && !isBlankOrNull(address1) && !isBlankOrNull(city) && !isBlankOrNull(state)
                && !isBlankOrNull(postal)) {
    		logger.info("Inside another If inside Customer Name Address Match Rule");
            accountList = retrieveAcctsCusInfo(customerName);
            logger.info("Size of Account Id list retrieved from Customer Name and Address "+accountList.size());
            // Proceed to the next level if no account found
            if (!accountList.isEmpty()) {

            	Money totalBillAmount = Money.ZERO;
            	List<QueryResultRow> billList = new ArrayList<QueryResultRow>();
            	List<Bill_Id> billIdList = new ArrayList<Bill_Id>();
            	for (Account_Id acctId : accountList) {
            		logger.info("Processing for Account Id : "+acctId);
            		billList = determineOpenBill(acctId);
            		logger.info("Size of Open Bill List for account Id : "+acctId+" is :"+billIdList.size());
            		if(billList.size() == 0 && isNull(noBillAccountId))
            		{
            			noBillAccountId = acctId;
            		}
//            		logger.info("billList QueryReult 1 :"+billList.iterator().next().get("billId"));
					for (QueryResultRow openBillId : billList) 
					{
            			totalBillAmount = totalBillAmount.add(determineBillAmount((Bill_Id) openBillId.get("billId")));
            			billIdList.add((Bill_Id) openBillId.get("billId"));
            		}
            	}
            	matchToOpenBills(accountList,billIdList,totalBillAmount);
            }
        }
    }
    /*
     * Code Added By Ishita for CB-145 end
     */
    /**
     * Retrieve Accounts from customer info
     * @param customerName
     * @return List<Account_Id>
     */
    private List<Account_Id> retrieveAcctsCusInfo(String customerName) {
      
        StringBuilder queryString = new StringBuilder();
        queryString.append("from AccountPerson ACCTPER                                          ");
        queryString.append("where EXISTS (select PERNAME.id.person.id from PersonName PERNAME   ");
        queryString.append("where PERNAME.id.person.id = ACCTPER.id.person.id                   ");
        queryString.append("and PERNAME.isPrimaryName = :true                                   ");
        queryString.append("and trim(PERNAME.uppercaseEntityName) = trim(:customerName))                    ");
        queryString.append("and (exists (select PERADOVR.address1                                   ");
        queryString.append("FROM PersonAddressOverride PERADOVR                                 ");
        queryString.append("where PERADOVR.id.accountPerson.id.person.id = ACCTPER.id.person.id ");
        queryString.append("and PERADOVR.id.accountPerson.id.account.id = ACCTPER.id.account.id ");
        queryString.append("and PERADOVR.address1 = :address1                                   ");
        if (!isBlankOrNull(address2)) {
            queryString.append("and PERADOVR.address2 = :address2                               ");
        }
        if (!isBlankOrNull(address3)) {
            queryString.append("and PERADOVR.address3 = :address3                               ");
        }

        if (!isBlankOrNull(address4)) {
            queryString.append("and PERADOVR.address4 = :address4                               ");
        }
        queryString.append("and trim(PERADOVR.city) = trim(:city)                                           ");
        queryString.append("and trim(PERADOVR.postal) = trim(:postal)                                       ");
        queryString.append("and trim(PERADOVR.state) = trim(:states)                                        ");
        if (!isBlankOrNull(county)) {
            queryString.append("and PERADOVR.county = :county                                       ");
        }
        queryString.append("and trim(PERADOVR.country) = trim(:country))                                        ");
        queryString.append("OR exists (select PERS.address1                                     ");
        queryString.append("FROM Person PERS                                                    ");
        queryString.append("where PERS.id = ACCTPER.id.person.id                                ");
        queryString.append("and trim(PERS.address1) = trim(:address1)                                       ");
        if (!isBlankOrNull(address2)) {
            queryString.append("and PERS.address2 = :address2                                   ");
        }

        if (!isBlankOrNull(address3)) {
            queryString.append("and PERS.address3 = :address3                                   ");
        }

        if (!isBlankOrNull(address4)) {
            queryString.append("and PERS.address4 = :address4                                   ");
        }
        queryString.append("and trim(PERS.city) = trim(:city)                                               ");
        queryString.append("and trim(PERS.postal) = trim(:postal)                                           ");
        queryString.append("and trim(PERS.state) = trim(:states)                                            ");
        if (!isBlankOrNull(county)) {
            queryString.append("and PERS.county = :county                                       ");
        }
        queryString.append("and trim(PERS.country) = trim(:country)))                                       ");
        Query<Account_Id> query = createQuery(queryString.toString(), "retrieveAcctsCusInfo");
        query.bindBoolean("true", Bool.TRUE);
        query.bindStringProperty("customerName", PersonName.properties.uppercaseEntityName, customerName.toUpperCase());
        query.bindStringProperty("address1", Person.properties.address1, address1);
        query.bindStringProperty("city", Person.properties.city, city);
        query.bindStringProperty("postal", Person.properties.postal, postal);
        query.bindStringProperty("states", Person.properties.state, state);
        logger.info(customerName.toUpperCase()+" "+address1+" "+city+" "+postal+" "+state);
        if (!isBlankOrNull(address2)) {
            query.bindStringProperty("address2", Person.properties.address2, address2);
        }

        if (!isBlankOrNull(address3)) {
            query.bindStringProperty("address3", Person.properties.address3, address3);
        }

        if (!isBlankOrNull(address4)) {
            query.bindStringProperty("address4", Person.properties.address4, address4);
        }

        if (!isBlankOrNull(county)) {
            query.bindStringProperty("county", Person.properties.address2, county);
        }
        query.bindStringProperty("country", Person.properties.country, (isBlankOrNull(country) ? defaultCountry.getId()
                .getIdValue() : country));
        logger.info("Country "+defaultCountry.getId().getIdValue());
        query.addResult("ACCTPER.id.account.id", "ACCTPER.id.account.id");
        logger.info(queryString);
        logger.info(query.list());
        return query.list();
    }
    
   /* 
    * * Changes done for CB-145 start
  * Commenting the below method
    * *//**
     * Loan Number Match Rule
     *//*
    private void loanNumberMatchRule(){
        BillSegment_Id bsegIdEntity = null;
        bsegIdEntity = retrieveBseg(loanNumberCharacteristicType, loanNumber);
        if (notNull(bsegIdEntity)) {
                matchToBillSegment(bsegIdEntity);
        }
    }*/
    //Changes done for CB-145 end
    
    /*
     * Code Added By Ishita for CB-145 start
     * CB-145
     */
    private void loanNumberMatchRule(){
    	BillSegment_Id bsegId = null;
    	Money totalBsegAmt = Money.ZERO;
    	List<BillSegment_Id> bsegIdList = new ArrayList<BillSegment_Id>();
    	if(!loanNumList.isEmpty())
    	{
    		for(String loanNum : loanNumList)
    		{
    			bsegId = retrieveBseg(loanNumberCharacteristicType, loanNum);
    			if(!isNull(bsegId))
    			{
    				bsegIdList.add(bsegId);
    				totalBsegAmt = totalBsegAmt.add(determineBsegAmount(bsegId));
    			}
    			else
    			{
    				return;
    			}
    		}
    		matchToBillSegment(bsegIdList, totalBsegAmt.getAmount());
    	}
    }
    
    /*
     * Code Added By Ishita for CB-145 end
     */
    
    /**
     * Code changes for CB-145 start
     * Commenting the below method
     * General Suspense Match Rule
     *//*
     private void generalSuspenseMatchRule() {

         if (isBlankOrNull(externalSourceId)) {
             addError(CmMessageRepository.getServerMessage(CmMessages.MANDATORY_NODE_MISSING, ""));
         }
         // Retrieve Suspense Account using the external source id
         ServiceAgreement_Id suspenseContractId = retrieveSuspenseAccount(externalSourceId);
         //Create Match Details Output
         setOutputMatchDetails(applyToGeneralSuspenseMatchType.getId().getIdValue()
        		 , suspenseContractId.getEntity().getAccount().getId().getIdValue() , tenderAmount);
         if (isBlankOrNull(payorAccountId)) {
             payorAccountId = suspenseContractId.getEntity().getAccount().getId().getIdValue();
         }
        
     }*/
    // Code changes for CB-145 end
    
    

     /*
      * Code Added By Ishita for CB-145 start
      */
     /**
      * General Suspense Match Rule
      */
      private void generalSuspenseMatchRule() {
          //Create Match Details Output
          setOutputMatchDetails(applyToGeneralSuspenseMatchType.getId().getIdValue(),suspenseAccount.getIdValue(),tenderAmount,suspenseAccount.getIdValue());
          if (isBlankOrNull(payorAccount)) {
              payorAccountId = suspenseAccount.getIdValue();
          }
          else
          {
        	  payorAccountId = payorAccount;
          }
         
      }
     /*
      * Code Added By Ishita for CB-145 end
      */
     /**
      * This method is used to retrieve suspense account
      * @param externalSourceId External Source ID
      * @return Service Agreement ID
      */
     private ServiceAgreement_Id retrieveSuspenseAccount(String externalSourceId) {
         StringBuilder queryString = new StringBuilder();
         queryString.append("from TenderSource TSOUR                          ");
         queryString.append("where TSOUR.externalSourceId = :externalSourceId ");
         Query<ServiceAgreement_Id> query = createQuery(queryString.toString(), "retrieveSuspenseAccount");
         query.bindStringProperty("externalSourceId", TenderSource.properties.externalSourceId, externalSourceId);
         query.addResult("TSOUR.serviceAgreementId", "TSOUR.serviceAgreementId");
         ServiceAgreement_Id saId = query.firstRow();
         if (isNull(saId)) {
             addError(CmMessageRepository.getServerMessage(CmMessages.NO_SUSPENSE_CONTRACT_FOUND, externalSourceId));
         }
         return saId;
     }
     
    /**
     * Retrieve Account Id 
     * @param acctNumber
     * @return Account Id
     */
    private Account_Id retrieveAcctId(String acctNumber) {

        StringBuilder queryString = new StringBuilder();
        queryString.append("FROM AccountNumber acctNumber       ");
        queryString.append("WHERE acctNumber.accountNumber = :acctNumber  ");
        queryString.append("AND acctNumber.isPrimaryId = :true  ");

        Query<Account_Id> query = createQuery(queryString.toString(), "retrieveAccountId");
        query.bindStringProperty("acctNumber", AccountNumber.properties.accountNumber, acctNumber);
        query.bindBoolean("true", Bool.TRUE);
        query.addResult("acctNumber.id.account.id", "acctNumber.id.account.id");

        List<Account_Id> acctIdList = query.list();
      
        if (acctIdList.size() == 1) {
            return query.firstRow();
        } else {
            return null;
        }
    }
  /* 
   * Changes done for CB-145 start
   * Commenting the method below 
   * *//**
     * Match to Bill Segment
     * @param bsegId
     *//*
    private void matchToBillSegment(BillSegment_Id bsegId) {
        Money bsegAmt = Money.ZERO;

        String acctId = bsegId.getEntity().getServiceAgreement().getAccount().getId().getIdValue();

        bsegAmt = determineBsegAmount(bsegId);
        if (bsegAmt.isLessThanOrEqual(tenderAmount)) {
            setOutputMatchDetails(matchByBillSegmentMatchType.getId().getIdValue(), bsegId.getIdValue(), tenderAmount);

        } else {
            setOutputMatchDetails(matchByAccountMatchType.getId().getIdValue(), acctId, tenderAmount);
        }
        if (notNull(payorAccount)) {
            payorAccountId = payorAccount;
        } else {
            payorAccountId = acctId;
        }
    }*/
    
    // Changes for CB-145 end

    /*
     * Code Added By Ishita for CB-145 start
     */
    private void matchToBillSegment(List<BillSegment_Id> bsegList,BigDecimal totalBillSegAmt) {
    	Collections.sort(bsegList, new BillSegmentSorting());
    	Money totBillSegAmt = new Money(totalBillSegAmt);
    	Money excessAmt = Money.ZERO;
    	Money payAmt = Money.ZERO;
    	boolean hasExcessApplied = false;
    	if(totBillSegAmt.isLessThanOrEqual(tenderAmount))
    	{
    		excessAmt = tenderAmount.subtract(totBillSegAmt);
    		if(excessAmt.isZero())
    		{
    			hasExcessApplied = true;
    		}
    		int counter = 1;
    		Account_Id lastAcctId = null;
    		for(BillSegment_Id bsegId : bsegList)
    		{
    			Bill_Id curBillId = bsegId.getEntity().getBillId();
    			Account curAcct = curBillId.getEntity().getAccount();
    			String custClass = curAcct.getCustomerClass().getId().getIdValue().trim();
    			if(suspenseCustomerClass.equals(custClass) && !hasExcessApplied)
    			{
    				Money bsegAmt = determineBsegAmount(bsegId);
    					payAmt = excessAmt.add(bsegAmt);
    					hasExcessApplied = true;
    				setOutputMatchDetails(matchByBillSegmentMatchType.getId().getIdValue(), bsegId.getIdValue(), payAmt, curAcct.getId().getIdValue());
    			}
    			else if(counter == bsegList.size() && !hasExcessApplied)
    			{
    				 Money bsegAmt = determineBsegAmount(bsegId);
    				 payAmt = excessAmt.add(bsegAmt);
    				 setOutputMatchDetails(matchByBillSegmentMatchType.getId().getIdValue(), bsegId.getIdValue(), payAmt, curAcct.getId().getIdValue());
    			}
    			else
    			{
    				Money bsegAmt = determineBsegAmount(bsegId);
    				setOutputMatchDetails(matchByBillSegmentMatchType.getId().getIdValue(), bsegId.getIdValue(),bsegAmt, curAcct.getId().getIdValue());
    			}
    			if(counter == bsegList.size())
    			{
    				lastAcctId = curAcct.getId();
    			}
    			counter++;
    		}
    		if(!isNull(payorAccount))
    			payorAccountId = payorAccount;
    		else
    			payorAccountId = lastAcctId.getIdValue();
    	}
    	else
    	{
    		List<BillSegment_Id> otherBsegList = new ArrayList<BillSegment_Id>();
    		Money remainingAmt = tenderAmount;
    		int counter = 1;
    		String lastAcctId = null;
    		for(BillSegment_Id bsegId : bsegList)
    		{
    			Bill_Id curBillId = bsegId.getEntity().getBillId();
    			Account curAcct = curBillId.getEntity().getAccount();
    			String custClass = curAcct.getCustomerClass().getId().getIdValue().trim();
    			if(suspenseCustomerClass.equals(custClass))
    			{
    				 Money billSegmentAmt = determineBsegAmount(bsegId);
    				 if(remainingAmt.isGreaterThanOrEqual(billSegmentAmt))
    				 {
    					 	payAmt = billSegmentAmt;
    					 	setOutputMatchDetails(matchByBillSegmentMatchType.getId().getIdValue(), bsegId.getIdValue(), payAmt, curAcct.getId().getIdValue());
    					 	remainingAmt = remainingAmt.subtract(billSegmentAmt);
    					 	if(remainingAmt.isZero())
    					 	{
    					 		lastAcctId = curAcct.getId().getIdValue();
    					 		break;
    					 	}
    				 }
    				 else
    				 {
    					 payAmt = remainingAmt;
    					 setOutputMatchDetails(matchByBillSegmentMatchType.getId().getIdValue(), bsegId.getIdValue(), payAmt,curAcct.getId().getIdValue());
    					 lastAcctId = curAcct.getId().getIdValue();
    					 break;
    				 }
    			}
    			else
    			{
    				otherBsegList.add(bsegId);
    			}
    			if(counter == bsegList.size())
    			{
    				lastAcctId = curAcct.getId().getIdValue();
    			}
    			counter++;
    		}
    		if(remainingAmt.isGreaterThan(Money.ZERO) && !otherBsegList.isEmpty())
    		{
    			for(BillSegment_Id bsegId : otherBsegList)
    			{
    				Money billSegAmt = determineBsegAmount(bsegId);
    				Account_Id acctId = bsegId.getEntity().getBillId().getEntity().getAccount().getId();
    				 if(remainingAmt.isGreaterThanOrEqual(billSegAmt))
    				 {
    					 	payAmt = billSegAmt;
    					 	setOutputMatchDetails(matchByBillSegmentMatchType.getId().getIdValue(), bsegId.getIdValue(), payAmt,acctId.getIdValue());
    					 	remainingAmt = remainingAmt.subtract(billSegAmt);
    					 	if(remainingAmt.isZero())
    					 	{
    					 		break;
    					 	}
    				 }
    				 else
    				 {
    					 payAmt = remainingAmt;
    					 setOutputMatchDetails(matchByBillSegmentMatchType.getId().getIdValue(), bsegId.getIdValue(), payAmt,acctId.getIdValue());
    					 break;
    				 }
    			}
    		}
    		if(!isNull(payorAccount))
    		{
    			payorAccountId = payorAccount;
    		}
    		else
    		{
    			payorAccountId = lastAcctId;
    		}
    	}
    }
    /*
     * Code Added By Ishita on for CB-145 end
     */
    
    /**
     * Retrieve Bill Segment
     * @param charType
     * @param charVal
     * @return Bill Segment Id
     */
    private BillSegment_Id retrieveBseg(CharacteristicType charType, String charVal) {
        StringBuilder query = new StringBuilder();
        
        CharacteristicTypeLookup charTypeLookup = charType.getCharacteristicType();
        
        query.append(" FROM BillSegment bs, BillSegmentCalculationHeader bsc, BilllableChargeCharacteristic bc ");
        query.append(" WHERE bsc.billableChargeId = bc.id.billableCharge.id ");
        query.append(" AND bs.id = bsc.id.billSegment ");
        query.append(" AND bs.billSegmentStatus = :frozen ");
        query.append(" AND bc.id.characteristicType = :charType ");
        if (charTypeLookup.isPredefinedValue()){
            query.append(" AND bc.characteristicValue = :charVal ");
		}
		else if (charTypeLookup.isForeignKeyValue()){
	        query.append(" AND bc.characteristicValueForeignKey1 = :charVal ");
		}
		else {
	        query.append(" AND bc.adhocCharacteristicValue = :charVal ");
		}			
        query.append(" AND bc.id.effectiveDate = (SELECT MAX(bc2.id.effectiveDate) FROM BilllableChargeCharacteristic bc2 ");
        query.append(" WHERE bc2.id.billableCharge.id = bc.id.billableCharge.id ");
        query.append(" AND bc2.id.characteristicType = bc.id.characteristicType ");
        query.append(" AND bc2.id.effectiveDate <= :accountingDate) ");

        Query<BillSegment_Id> retrieveBseg = createQuery(query.toString(), "RETRIEVE_BILL_SEGMENT");
        retrieveBseg.bindLookup("frozen", BillSegmentStatusLookup.constants.FROZEN);
        retrieveBseg.bindEntity("charType", charType);
        retrieveBseg.bindStringProperty("charVal", BilllableChargeCharacteristic.properties.adhocCharacteristicValue,
                charVal);
        retrieveBseg.bindDate("accountingDate", accountingDate);
        retrieveBseg.addResult("bsegId", "bs.id");
       
        if (retrieveBseg.listSize() == 1) {
            return retrieveBseg.firstRow();
        } else {
            return null;
        }
    }
    
    /**
     * Determine Open Bill
     * @param acctId
     * @return List Bill Id
     */
    private List<QueryResultRow> determineOpenBill(Account_Id acctId) {

        StringBuilder query = new StringBuilder();
        query.append(" FROM Bill bl WHERE bl.account.id = :acctId ");
        query.append(" AND (EXISTS ( select ft.id FROM FinancialTransaction ft ");
        query.append(" WHERE ft.billId = bl.id AND ft.matchEventId = ' ' ) ");
        query.append(" OR EXISTS ( select ft.id FROM FinancialTransaction ft,  MatchEvent me ");
        query.append(" WHERE bl.id = ft.billId AND ft.matchEventId = me.id ");
        query.append(" AND me.matchEventStatus <> :balanced )) ");

        Query<QueryResultRow> retrieveOpenBill = createQuery(query.toString(), "RETRIEVE_OPEN_BILL");
        retrieveOpenBill.bindId("acctId", acctId);
        retrieveOpenBill.bindLookup("balanced", MatchEventStatusLookup.constants.BALANCED);
        retrieveOpenBill.addResult("billId", "bl.id");
        retrieveOpenBill.addResult("dueDate", "bl.dueDate"); // Added for CB-145
        retrieveOpenBill.orderBy("dueDate",Query.ASCENDING); // Added for CB-145

        return retrieveOpenBill.list();
    }

    /**
     * Determine Open Bill Amount
     * @param billId
     * @return Amount
     */
    private Money determineBillAmount(Bill_Id billId) {
        Money billAmount = Money.ZERO;
        StringBuilder query = new StringBuilder();
        query.append("FROM FinancialTransaction ft ");
        query.append("WHERE ft.billId = :billId ");
        query.append("AND ft.isFrozen = :isFrozen ");
        query.append("AND (ft.matchEventId = ' ' ");
        query.append("OR EXISTS ( select ft2.id FROM FinancialTransaction ft2,  MatchEvent me ");
        			query.append("WHERE ft2.billId = ft.billId ");
        			query.append("AND ft2.matchEventId = me.id ");
        			query.append("AND me.matchEventStatus <> :balanced )) ");
        			
        Query<Money> retrieveBillTotAmt = createQuery(query.toString(), "RETRIEVE_BILL_TOT_AMT");
        retrieveBillTotAmt.bindId("billId", billId);
        retrieveBillTotAmt.bindBoolean("isFrozen", Bool.TRUE);
        retrieveBillTotAmt.bindLookup("balanced", MatchEventStatusLookup.constants.BALANCED);
        retrieveBillTotAmt.addResult("totalAmount", "NVL(SUM(ft.payoffAmount),0)");

        billAmount = retrieveBillTotAmt.firstRow();

        return billAmount;
    }
    
    /**
     * Determine Open Bill Segment Amount
     * @param bsegId
     * @return Amount
     */
    private Money determineBsegAmount(BillSegment_Id bsegId) {
        Money bsegAmount = Money.ZERO;
        StringBuilder query = new StringBuilder();
        query.append("FROM FinancialTransaction ft ");
        query.append("WHERE ft.siblingId = :bsegId ");
        query.append("AND ft.isFrozen = :isFrozen ");
        query.append("AND (ft.matchEventId = ' ' ");
        query.append("OR EXISTS ( select ft2.id FROM FinancialTransaction ft2,  MatchEvent me ");
        			query.append("WHERE ft2.siblingId = ft.siblingId ");
        			query.append("AND ft2.matchEventId = me.id ");
        			query.append("AND me.matchEventStatus <> :balanced )) ");

        Query<Money> retrieveBsegTotAmt = createQuery(query.toString(), "RETRIEVE_BSEG_TOT_AMT");
        retrieveBsegTotAmt.bindId("bsegId", bsegId);
        retrieveBsegTotAmt.bindBoolean("isFrozen", Bool.TRUE);
        retrieveBsegTotAmt.bindLookup("balanced", MatchEventStatusLookup.constants.BALANCED);
        retrieveBsegTotAmt.addResult("totalAmount", "NVL(SUM(ft.payoffAmount),0)");

        bsegAmount = retrieveBsegTotAmt.firstRow();
        
        return bsegAmount;
    }

    /*
     * Code Added By Ishita Start
     * CB-145
     */
    
   /* 
    * Changes for done for CB-145 start
    * Commenting the below method
    * *//**
     * Set Output Match Details
     * @param matchType
     * @param matchValue
     * @param paymentAmount
     *//*
    private void setOutputMatchDetails(String matchType, String matchValue, Money paymentAmount) {
        CmPaymentServiceData payServiceData = new CmPaymentServiceData();
        payServiceData.setMatchType(matchType);
        payServiceData.setMatchValue(matchValue);
        payServiceData.setPaymentAmount(paymentAmount);
        matchDetails.add(payServiceData);
    }*/
    // Changes done for CB-145 end
    
    
    // Changes for CB-145 start
    /**
     * Set Output Match Details
     * @param matchType
     * @param matchValue
     * @param paymentAmount
     * @param accountId
     */
    private void setOutputMatchDetails(String matchType, String matchValue, Money paymentAmount,String accountId) {
    	logger.info("Inside set Match Output start");
    	logger.info("Payment Amount :"+paymentAmount);
        CmPaymentServiceData payServiceData = new CmPaymentServiceData();
        payServiceData.setMatchType(matchType);
        payServiceData.setMatchValue(matchValue);
        payServiceData.setPaymentAmount(paymentAmount);
        payServiceData.setAccountId(accountId);
        matchDetails.add(payServiceData);
        logger.info("Inside set Match Output end");
    }
    
    /*
     * Code Added By Ishita for CB-145 End
     */

    /**
     * Initialize Master Config Values
     */
    private void initializeMasterConfigValues() {
        CmPaymentMatchingRuleMstConfHelper paymentMatchingConfigHelper = CmPaymentMatchingRuleMstConfHelper.Factory
                .newInstance();
        paymentMatchingConfigHelper.invoke();
        matchByBillMatchType = paymentMatchingConfigHelper.getMatchByBill();
        matchByBillSegmentMatchType = paymentMatchingConfigHelper.getMatchByBS();
        matchByAccountMatchType = paymentMatchingConfigHelper.getMatchByAcct();
        applyToGeneralSuspenseMatchType = paymentMatchingConfigHelper.getApplyToGenSus();
        orderNumberCharacteristicType = paymentMatchingConfigHelper.getOrderNumCharType();
        loanNumberCharacteristicType = paymentMatchingConfigHelper.getLoanNumCharType();
        defaultCountry = paymentMatchingConfigHelper.getDefaultCountry();
    }
    
    /**
     * Retrieve Payment Matching Input
     */
    private void retrievePaymentMatchingInput(){
    	/*
    	 * Code Changed By Ishita on 24-07-2020 start
    	 * Changes done as per the design suggested in CB-145
    	 */
    	//billId = paymentMatchingInput.getBillId();
    	//orderNumber = paymentMatchingInput.getOrderNumber();
    	//loanNumber  = paymentMatchingInput.getLoanNumber();
    	billIdList = paymentMatchingInput.getBillIdList();
    	orderNumList = paymentMatchingInput.getOrderNumList();
    	loanNumList = paymentMatchingInput.getLoanNumList();
    	//Code Changed By Ishita on 24-07-2020 end
    	
    	
    	accountIdentifier = paymentMatchingInput.getAccountIdentifier();
    	
    	customerName  = paymentMatchingInput.getCustomerName();
    	address1  = paymentMatchingInput.getAddress1();
    	address2  = paymentMatchingInput.getAddress2();
    	logger.info("Value of address2:"+address2+"end");
    	address3  = paymentMatchingInput.getAddress3();
    	logger.info("Value of address3:"+address3+"end");
    	address4  = paymentMatchingInput.getAddress4();
    	logger.info("Value of address4:"+address4+"end");
    	city  = paymentMatchingInput.getCity();
    	county = paymentMatchingInput.getCounty();
    	state  = paymentMatchingInput.getState();
    	postal  = paymentMatchingInput.getPostal();
    	country  = paymentMatchingInput.getCountry();
    }
    /*
     * Code Added By Ishita - CB-145 start
     */
    private void determineSuspenceAccount()
    {
    	ServiceAgreement_Id suspenseContractId = retrieveSuspenseAccount(externalSourceId);
    	suspenseAccount = suspenseContractId.getEntity().getAccount().getId();
    }
    
}
class BillSegmentSorting implements Comparator
{
	/*
	 * Compares two objects based on date
	 */
	public int compare(Object arg0,Object arg1)
	{
		BillSegment_Id date1 = null;
		BillSegment_Id date2 = null;
		
		date1 = (BillSegment_Id) arg0;
		date2 = (BillSegment_Id) arg1;
		
		return date1.getEntity().getBillId().getEntity().getDueDate().compareTo(date2.getEntity().getBillId().getEntity().getDueDate());
	}
}
class BillSorting implements Comparator
{
	/*
	 * Compares two objects based on date
	 */
	public int compare(Object arg0,Object arg1)
	{
		Bill_Id date1 = null;
		Bill_Id date2 = null;
		
		date1 = (Bill_Id) arg0;
		date2 = (Bill_Id) arg1;
		
		return date1.getEntity().getDueDate().compareTo(date2.getEntity().getDueDate());
	}
	/*
     * Code Added By Ishita - CB-145 end
     */
}
