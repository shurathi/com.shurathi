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
 * This batch process will retrieve Customers and/or Accounts with
 * Active Statement Constructs having an open Statement Cycle or 
 * those associated to the provided batch parameters. Once selected, 
 * the batch will identify whether the Customer / Account have Open 
 * Bills, unmatched Credit Memos and/or unmatched Payments on On 
 * Account Contract. If it does, the information will be written 
 * into the extract file.
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-07-06	DDejes		CB-157. Initial Version.
 * 2020-09-07	KGhuge		CB-368 	CUSTNUM Change - Statement
 * 2020-09-10	KGhuge		CB-408	Invalid File Path Parameter for CMCRESTX Batch
 ***********************************************************************
 */
package com.splwg.cm.domain.billing.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryIterator;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.batch.WorkUnitResult;
import com.splwg.base.api.businessObject.COTSInstanceList;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.businessObject.DataAreaInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.lookup.EffectiveStatusLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.support.context.FrameworkSession;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.ccb.api.lookup.MatchEventStatusLookup;
import com.splwg.ccb.api.lookup.StatementProcessStatusLookup;
import com.splwg.ccb.domain.admin.adjustmentType.AdjustmentType_Id;
import com.splwg.ccb.domain.admin.cisDivision.CisDivision_Id;
import com.splwg.ccb.domain.admin.customerClass.CustomerClass;
import com.splwg.ccb.domain.admin.customerClass.CustomerClass_Id;
import com.splwg.ccb.domain.admin.idType.IdType;
import com.splwg.ccb.domain.admin.idType.accountIdType.AccountNumberType;
import com.splwg.ccb.domain.admin.serviceAgreementType.ServiceAgreementType_Id;
import com.splwg.ccb.domain.admin.statementCycle.StatementCycleSchedule;
import com.splwg.ccb.domain.admin.statementCycle.StatementCycleSchedule_DTO;
import com.splwg.ccb.domain.billing.billableCharge.BillableCharge_Id;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber;
import com.splwg.ccb.domain.customerinfo.account.Account_Id;
import com.splwg.ccb.domain.customerinfo.address.Address;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.person.PersonId;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstruct;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstructDetail;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstructDetail_Id;
import com.splwg.ccb.domain.customerinfo.statementConstruct.StatementConstruct_Id;
import com.splwg.ccb.domain.payment.payment.Payment_Id;
import com.splwg.cm.domain.billing.utility.CmStatementExtractConstants;
import com.splwg.cm.domain.billing.utility.CmStatementExtractExtLookupCache;
import com.splwg.cm.domain.billing.utility.CmStatementExtractExtLookupVO;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileRequestConstants;
import com.splwg.shared.common.ApplicationError;

/**
 * @author Denise De Jesus
 *
@BatchJob (multiThreaded = true, rerunnable = true, modules={},
 * softParameters = { @BatchJobSoftParameter (name = filePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = fileName, required = true, type = string)
 *            , @BatchJobSoftParameter (entityName = idType, name = customerNumberIdType, required = true, type = entity)
 *            , @BatchJobSoftParameter (name = billableChargeCharList, required = true, type = string)
 *            , @BatchJobSoftParameter (entityName = businessObject, name = statementExtractExtLookup, required = true, type = entity)
 *            , @BatchJobSoftParameter (name = contractTypeList, required = true, type = string)
 *            , @BatchJobSoftParameter (name = adjustmentTypeList, required = true, type = string)
 *            , @BatchJobSoftParameter (entityName = idType, name = adhocCustomerIdType, type = entity)
 *            , @BatchJobSoftParameter (name = adhocCustomerIdValueList, type = string)
 *            , @BatchJobSoftParameter (entityName = accountNumberType, name = adhocAccountIdType, type = entity)
 *            , @BatchJobSoftParameter (name = adhocAccountIdValueList, type = string)
 *            , @BatchJobSoftParameter (name = maxErrors, type = string)
 *            , @BatchJobSoftParameter (name = DIST-THD-POOL, type = string)})
 */
public class CmCreateStatementExtractBatch extends
		CmCreateStatementExtractBatch_Gen {	
	//Parameters
	private Bool isAdhoc; 
	
	//Work Variables
	private String fileName;
	private String billableChargeCharacteristics;
	private String contracType;
	private String adjustmentType;
	private String adhocCustomerIdValueList;
	private CisDivision_Id cisDivId;
	private IdType adhocCustIdType;
	private AccountNumberType adhocAcctIdType;
	private String adhocAccountIdValueList;
	
	public void validateSoftParameters(boolean isNewRun) {
		isAdhoc = Bool.FALSE;
		cmValidateFileNameAndExtension();
		
		//Validate Billable Charge Characteristics List
		billableChargeCharacteristics = getParameters().getBillableChargeCharList().trim();
		
		for(String bcChar : billableChargeCharacteristics.split(CmStatementExtractConstants.COMMA)){
			if(isNull(new CharacteristicType_Id(bcChar).getEntity())){
				//throw error
				addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_CHAR_TYPE, bcChar));
			}
			
		}
				
		//Validate Contract Type List
		cisDivId = new CisDivision_Id(CmStatementExtractConstants.DIVISION);
		contracType = getParameters().getContractTypeList().trim();
		for(String contType : contracType.split(CmStatementExtractConstants.COMMA)){
			if(isNull(new ServiceAgreementType_Id(cisDivId, contType).getEntity())){
				//addError();92000 40004
				addError(CmMessageRepository.getServerMessage(CmMessages.ENTITY_NOT_VALID, contType, CmStatementExtractConstants.CONTRACT_TYPE));
			}
		}
		
		//Validate Adjustment Type List
		adjustmentType = getParameters().getAdjustmentTypeList().trim();
		for(String adjType : adjustmentType.split(CmStatementExtractConstants.COMMA)){
			if(isNull(new AdjustmentType_Id(adjType).getEntity())){
				//addError();92000 40004
				addError(CmMessageRepository.getServerMessage(CmMessages.ENTITY_NOT_VALID, adjType, CmStatementExtractConstants.ADJUSTMENT_TYPE));
			}
		}
		
		//Validate if adhocCustomerIdValueList is populated, then adhocCustomerIdType should have a value
		adhocCustIdType = getParameters().getAdhocCustomerIdType();
		adhocCustomerIdValueList = getParameters().getAdhocCustomerIdValueList();
		if(!isBlankOrNull(adhocCustomerIdValueList)){
			if(isNull(adhocCustIdType)){
				//Adderror 92000, 30102
				addError(CmMessageRepository.getServerMessage(CmMessages.ENTITY_NOT_VALID, CmStatementExtractConstants.BLANK
						, CmStatementExtractConstants.CUSTOMER_ID_TYPE));
			}else{
				isAdhoc = Bool.TRUE;
			}
		}
		
		//Validate if adhocAccountIdTypeValueList is populated, then adhocAccountIdType should have a value
		adhocAcctIdType = getParameters().getAdhocAccountIdType();
		adhocAccountIdValueList = getParameters().getAdhocAccountIdValueList();
		if(!isBlankOrNull(adhocAccountIdValueList)){
			if(isNull(adhocAcctIdType)){
				//Adderror 92000 40004
				addError(CmMessageRepository.getServerMessage(CmMessages.ENTITY_NOT_VALID, CmStatementExtractConstants.BLANK
						, CmStatementExtractConstants.ACCOUNT_ID_TYPE));
			}else{
				isAdhoc = Bool.TRUE;
			}
		}
	}
	
	/**
	 * This method will check for extension of fileName. 
    */
	public void cmValidateFileNameAndExtension() {
		fileName = getParameters().getFileName().trim();
		int extensionIndex1 = fileName.lastIndexOf(CmStatementExtractConstants.FILEEXTENSION); 

		if(extensionIndex1 > 0){
			if(!fileName.contains(CmStatementExtractConstants.XMLEXTENSION)){
				addError(CmMessageRepository.getServerMessage(CmMessages.FILE_EXT_NOT_XML));		
			}
		}else{
			fileName = fileName.concat(CmStatementExtractConstants.XMLEXTENSION).trim();
		}
	}
	
	public JobWork getJobWork() {
		ArrayList<ThreadWorkUnit> workUnits = new ArrayList<ThreadWorkUnit>();
		ThreadWorkUnit workUnit = null;
		String tempAdhocCustomerIdValueList = CmStatementExtractConstants.EMPTY_STRING;
		String tempAdhocAccountIdValueList = CmStatementExtractConstants.EMPTY_STRING; 
		QueryIterator<StatementConstructDetail_Id> queryIteratorForAdhocCust = null;
		QueryIterator<QueryResultRow> queryIteratorForAdhocAcct = null;
		//Start Change CB-368
		//QueryIterator<StatementConstruct_Id> queryIteratorForNonAdhoc = null;
		QueryIterator<StatementConstructDetail_Id> queryIteratorForNonAdhoc = null;
//		StatementConstruct_Id statementConstId = null;
		//End Change CB-368
		StatementConstructDetail_Id stConstDetId = null;

			initialize();
			//For Non Adhoc
			if(isAdhoc.isFalse()){
				queryIteratorForNonAdhoc = retrieveStatementConstructForNonAdhoc();

				while (queryIteratorForNonAdhoc.hasNext()){
					//Start Change CB-368
					//statementConstId =  queryIteratorForNonAdhoc.next();
					//End Change CB-368					
					workUnit = new ThreadWorkUnit();
					//Start Change CB-368
					//workUnit.setPrimaryId(statementConstId);
					workUnit.setPrimaryId(queryIteratorForNonAdhoc.next());
					//workUnit.addSupplementalData(CmStatementExtractConstants.BATCH_ACTION, CmStatementExtractConstants.NON_ADHOC);
					//End Change CB-368
					workUnits.add(workUnit);
				}
			}else{
				//For Adhoc Person
				if(!isBlankOrNull(adhocCustomerIdValueList) && !isNull(adhocCustIdType)){
					queryIteratorForAdhocCust = retrieveStatementConstructForAdhocCustomer();
					
					if(queryIteratorForAdhocCust.hasNext()){
						tempAdhocCustomerIdValueList = adhocCustomerIdValueList;
					}else{
						workUnit = new ThreadWorkUnit();
						//Start Change CB-368
						//workUnit.addSupplementalData(CmStatementExtractConstants.BATCH_ACTION,CmStatementExtractConstants.ADHOC_PER);
						//workUnit.addSupplementalData(CmStatementExtractConstants.WARNING,CmStatementExtractConstants.WARNING);
						workUnit.addSupplementalData(CmStatementExtractConstants.WARNING,CmStatementExtractConstants.PER_WARNING);
						//End Change CB-368
						workUnits.add(workUnit);
					}
				}
				
				//For Adhoc Account
				if(!isBlankOrNull(adhocAccountIdValueList) && !isNull(adhocAcctIdType)){
					queryIteratorForAdhocAcct = retrieveStatementConstructForAdhocAccount();
					if(queryIteratorForAdhocAcct.hasNext()){
						tempAdhocAccountIdValueList = adhocAccountIdValueList;
					}else{
						workUnit = new ThreadWorkUnit();
						//Start Change CB-368
						//workUnit.addSupplementalData(CmStatementExtractConstants.BATCH_ACTION,CmStatementExtractConstants.ADHOC_ACCT);
						//workUnit.addSupplementalData(CmStatementExtractConstants.WARNING,CmStatementExtractConstants.WARNING);
						workUnit.addSupplementalData(CmStatementExtractConstants.WARNING,CmStatementExtractConstants.ACCT_WARNING);
						//End Change CB-368
						workUnits.add(workUnit);
					}
				}
							
					if(!isBlankOrNull(tempAdhocCustomerIdValueList)){
						while (queryIteratorForAdhocCust.hasNext()){
							//Start Change CB-368
							//statementConstId =  queryIteratorForAdhocCust.next();
							//End Change CB-368
							workUnit = new ThreadWorkUnit();
							//Start Change CB-368
							//workUnit.setPrimaryId(statementConstId);
							workUnit.setPrimaryId(queryIteratorForAdhocCust.next());
							//workUnit.addSupplementalData(CmStatementExtractConstants.BATCH_ACTION,CmStatementExtractConstants.ADHOC_PER);
							//End Change CB-368
							workUnits.add(workUnit);
						}
					}
					if(!isBlankOrNull(tempAdhocAccountIdValueList)){
						while (queryIteratorForAdhocAcct.hasNext()){
							QueryResultRow iterCust =  queryIteratorForAdhocAcct.next();
							stConstDetId = (StatementConstructDetail_Id)iterCust.getId("statementConstDetailId", StatementConstructDetail.class);
							workUnit = new ThreadWorkUnit();
							workUnit.setPrimaryId(stConstDetId);
							//Start Change CB-368
							//workUnit.addSupplementalData(CmStatementExtractConstants.BATCH_ACTION,CmStatementExtractConstants.ADHOC_ACCT);
							//End Change CB-368
							workUnits.add(workUnit);
						}
					}
			}
		return createJobWorkForThreadWorkUnitList(workUnits);
	}
	/**
	 * Set Statement Cycle to In Progress
	 */
	private void initialize(){
		if(isAdhoc.isFalse()){
			StatementCycleSchedule_DTO stmtCycleSchDto = null;
			if(!retrieveStatementCycleSched().isEmpty()){
				for(StatementCycleSchedule statementCycleSched : retrieveStatementCycleSched()){
						if(statementCycleSched.getStatementProcessStatus().isPending()){
							stmtCycleSchDto = statementCycleSched.getDTO();		
							stmtCycleSchDto.setStatementProcessStatus(StatementProcessStatusLookup.constants.IN_PROGRESS);
							statementCycleSched.setDTO(stmtCycleSchDto);
						}
					}
				}
			}
	}
	
	/**
	 * Retrieve Active Statement Construct for Non Adhoc
	 * @return Statement Construct Id
	 */
	//Start Change CB-368
//	private QueryIterator<StatementConstruct_Id> retrieveStatementConstructForNonAdhoc(){
	private QueryIterator<StatementConstructDetail_Id> retrieveStatementConstructForNonAdhoc(){	
//		Query <StatementConstruct_Id> retrieveStmtConstructQuery = createQuery("FROM StatementCycle stmtCycle"
//				+ ", StatementCycleSchedule stmtCycleSched "
//				+ " , StatementConstruct stmtConst "
//                + "WHERE stmtCycleSched.statementProcessStatus = :inProg "
//                + "AND stmtCycle.id = stmtCycleSched.id.statementCycle "
//                + "AND stmtConst.statementCycle = stmtCycle.id "
//                + "AND stmtConst.effectiveStatus = :effectiveStatus ",  "Retrieve_Statement_Construct");
//        
//		retrieveStmtConstructQuery.bindLookup("inProg", StatementProcessStatusLookup.constants.IN_PROGRESS);
//		retrieveStmtConstructQuery.bindLookup("effectiveStatus", EffectiveStatusLookup.constants.ACTIVE);
//		
//		retrieveStmtConstructQuery.addResult("statementConstructId", "stmtConst.id");
//		retrieveStmtConstructQuery.orderBy("statementConstructId",Query.ASCENDING);
//		retrieveStmtConstructQuery.selectDistinct(true);
//        QueryIterator<StatementConstruct_Id> queryIterator = retrieveStmtConstructQuery.iterate();
		Query <StatementConstructDetail_Id> retrieveStmtConstructQuery = createQuery("FROM StatementCycle stmtCycle"
				+ ", StatementCycleSchedule stmtCycleSched "
				+ " , StatementConstruct stmtConst ,StatementConstructDetail stmtConstDtls "
                + "WHERE stmtCycleSched.statementProcessStatus = :inProg "
                + "AND stmtCycle.id = stmtCycleSched.id.statementCycle "
                + "AND stmtConst.statementCycle = stmtCycle.id "
                + "AND stmtConstDtls.statementConstruct.id = stmtConst.id " 
                + "AND stmtConst.effectiveStatus = :effectiveStatus ",  "Retrieve_Statement_Construct_Detail");
        
		retrieveStmtConstructQuery.bindLookup("inProg", StatementProcessStatusLookup.constants.IN_PROGRESS);
		retrieveStmtConstructQuery.bindLookup("effectiveStatus", EffectiveStatusLookup.constants.ACTIVE);
		
		//retrieveStmtConstructQuery.addResult("statementConstructId", "stmtConst.id");
		retrieveStmtConstructQuery.addResult("statementConstDetailId", "stmtConstDtls.id");
		retrieveStmtConstructQuery.orderBy("statementConstDetailId",Query.ASCENDING);
		retrieveStmtConstructQuery.selectDistinct(true);
		//End Change CB-368
        QueryIterator<StatementConstructDetail_Id> queryIterator = retrieveStmtConstructQuery.iterate();
      return queryIterator;
	}
	
	/**
	 * Retrieve Active Statement Construct for Adhoc Customer
	 * @return Statement Construct Id
	 */
	//Start Change CB-368
	//private QueryIterator<StatementConstruct_Id> retrieveStatementConstructForAdhocCustomer(){
	private QueryIterator<StatementConstructDetail_Id> retrieveStatementConstructForAdhocCustomer(){
	//End Change CB-368
		String[] list = adhocCustomerIdValueList.split(CmStatementExtractConstants.COMMA);
		StringBuilder stringBuilder = new StringBuilder();
		//Start Change CB-368
		//stringBuilder.append("FROM StatementConstruct stmtConst ");
		stringBuilder.append("FROM StatementConstruct stmtConst , StatementConstructDetail stmtConstDtls ");
		//End Change CB-368
		stringBuilder.append("WHERE stmtConst.effectiveStatus = :effectiveStatus ");
		//Start Add CB-368
		stringBuilder.append(" AND stmtConst.id = stmtConstDtls.statementConstruct.id ");
		//End Add CB-368
		stringBuilder.append("AND stmtConst.person.id IN (SELECT perId.id.person.id FROM PersonId perId ");
									stringBuilder.append("WHERE perId.id.idType.id = :idType ");
									stringBuilder.append("	AND perId.personIdNumber IN ( ");
                							for(int i=0; i<list.length; i++){
                								if(i==0){
                									stringBuilder.append(":idNumber"+i);
                								}else{
                									stringBuilder.append(", :idNumber"+i);
                								}
                							}
                					stringBuilder.append("))");
        //Start Change CB-368
		//Query<StatementConstruct_Id> retrieveStmtConstructQuery = createQuery(stringBuilder.toString(), "Retrieve_Statement_Construct");
        Query<StatementConstructDetail_Id> retrieveStmtConstructQuery = createQuery(stringBuilder.toString(), "Retrieve_Statement_Construct_Detail");
        //End Change CB-368				
		retrieveStmtConstructQuery.bindLookup("effectiveStatus", EffectiveStatusLookup.constants.ACTIVE);
		retrieveStmtConstructQuery.bindId("idType", adhocCustIdType.getId());
		for(int i=0; i<list.length; i++){
			retrieveStmtConstructQuery.bindStringProperty("idNumber"+i, PersonId.properties.personIdNumber, list[i].trim());
		}
		//Start Change CB-368
		//retrieveStmtConstructQuery.addResult("statementConstructId", "stmtConst.id");
		retrieveStmtConstructQuery.addResult("statementConstDetailId", "stmtConstDtls.id");
		//End Change CB-368
		
		retrieveStmtConstructQuery.orderBy("statementConstDetailId",Query.ASCENDING);

		return retrieveStmtConstructQuery.iterate();
	}
	

	/**
	 * Retrieve Active Statement Construct for Adhoc Account
	 * @return Statement Construct Detail Id
	 */
	private QueryIterator<QueryResultRow> retrieveStatementConstructForAdhocAccount(){
		String[] list = adhocAccountIdValueList.split(CmStatementExtractConstants.COMMA);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("FROM StatementConstruct stmtConst, StatementConstructDetail stmtConstDtl ");
		stringBuilder.append("WHERE stmtConst.effectiveStatus = :effectiveStatus ");
		stringBuilder.append("AND stmtConstDtl.statementConstruct.id = stmtConst.id ");
		stringBuilder.append("AND stmtConstDtl.accountId IN (SELECT acctNumber.id.account.id FROM AccountNumber acctNumber ");
										stringBuilder.append(" WHERE acctNumber.id.accountIdentifierType.id = :acctIdType ");
										stringBuilder.append(" AND acctNumber.accountNumber IN ( ");
										for(int i=0; i<list.length; i++){
            								if(i==0){
            									stringBuilder.append(":idNumber"+i);
            								}else{
            									stringBuilder.append(", :idNumber"+i);
            								}
            							}
            					stringBuilder.append("))");
        Query<QueryResultRow> retrieveStmtConstructQuery = createQuery(stringBuilder.toString(), "Retrieve_Statement_Construct");
		retrieveStmtConstructQuery.bindLookup("effectiveStatus", EffectiveStatusLookup.constants.ACTIVE);
		retrieveStmtConstructQuery.bindId("acctIdType", adhocAcctIdType.getId());
		for(int i=0; i<list.length; i++){
			retrieveStmtConstructQuery.bindStringProperty("idNumber"+i, AccountNumber.properties.accountNumber , list[i].trim());
		}
		retrieveStmtConstructQuery.addResult("statementConstDetailId", "stmtConstDtl.id");
		retrieveStmtConstructQuery.addResult("statementConstructId", "stmtConst.id");
	
		retrieveStmtConstructQuery.orderBy("statementConstructId",Query.ASCENDING);

		return retrieveStmtConstructQuery.iterate();
	}
	
	/**
	 * Retrieve Statement Cycle Schedule
	 * @return Statement Cycle Schedule List
	 */
	private List<StatementCycleSchedule> retrieveStatementCycleSched(){
		StringBuilder queryString = new StringBuilder();
		queryString.append(" FROM StatementCycleSchedule stmtCycleSched ");
		queryString.append(" WHERE stmtCycleSched.id.processDate <= :processDate ");
		
		Query<StatementCycleSchedule> query = createQuery(queryString.toString(), "Retrieve_StatementCycle_Schedule");	
		query.bindDate("processDate", getProcessDateTime().getDate());
		query.addResult("statementCycle", "stmtCycleSched");

		return query.list();
	}

	public Class<CmCreateStatementExtractBatchWorker> getThreadWorkerClass() {
		return CmCreateStatementExtractBatchWorker.class;
	}

	public static class CmCreateStatementExtractBatchWorker extends
			CmCreateStatementExtractBatchWorker_Gen {

    	// Work Parameters
		private static String batchAction;		
		private BufferedWriter bufferedWriter;
    	private int recordsInError;
    	private int recordsProcessed;
    	private Bool errorEncountered;
    	private FrameworkSession session;
    	private CmStatementExtractExtLookupVO statementExtLookup;
    	private String remitToName;
    	private String remitToBusinessUnit;
		private String logo;
		private String remitToAddress1;
		private String remitToAddress2;
		private String remitToAddress3;
		private String remitToAddress4;
		private String remitToCity;
		private String remitToState;
		private String remitToCounty;
		private String remitToPostal;
		private String remitToCountry;
		private String remitToPhone;
		private String remitToEmail;
		private List<String> templateList;
		private StatementConstruct_Id statementConstId;
		private StatementConstruct statementConst;
		private StatementConstructDetail_Id stConstDetId;
		private StatementConstructDetail stConstDet;	
    	private DataAreaInstance stmtExtractRecInst;
    	private COTSInstanceNode statementsGroup;
    	private COTSInstanceList statementList;   	
    	private Date transactionDate;
    	private String consolidatedInvoiceNumber;
    	private String orderNumber;
    	private String description;
    	private BigDecimal remainingBalance;
    	private String loanNumber;
    	private String borrower;
    	private String address;
    	private String state;
    	private String zip;
    	private BigDecimal subTotal;
    	private BigDecimal grandTotal;
    	private BillableCharge_Id billChargeId;
    	private CharacteristicType_Id charTypeId;
    	private List<Account_Id> acctList = new ArrayList<Account_Id>();
    	
    	//Batch Parameter
    	private String absoluteFilePath;
    	private String fileName;
    	private String filePath;
    	private String billableChargeCharacteristics;
    	private String contracTypeList;
    	private String adjustmentTypeList;
    	private String adhocCustomerIdValueList;
    	private IdType adhocCustIdType;
    	private IdType custNumberIdType;
    	private AccountNumberType adhocAcctIdType;
    	private String adhocAccountIdValueList;
    	
    	
    	public void initializeThreadWork(boolean initializationPreviouslySuccessful) throws ThreadAbortedException,
    	RunAbortedException {
    		initializeExtLookupVariables();
    		
    		//Retrieve the Batch Parameters
    		absoluteFilePath = getParameters().getFilePath().trim();
    		
    		fileName = getParameters().getFileName().trim();
    		int extensionIndex1 = fileName.lastIndexOf(CmStatementExtractConstants.FILEEXTENSION);  
    		
    		//If fileName has ?? then replace with thread number
    		if(fileName.contains(CmStatementExtractConstants.QUESTIONMARKS)){
    			fileName = fileName.replace(CmStatementExtractConstants.QUESTIONMARKS, getBatchThreadNumber().toString());
    		}
    		//If fileName has no file extension, append it with .xml
    		if(extensionIndex1 < 0){
    			if(!fileName.contains(CmStatementExtractConstants.XMLEXTENSION)){
    				fileName = fileName.concat(CmStatementExtractConstants.XMLEXTENSION).trim();
    			}
    		}
    		
    		//Validate file path if it has '\' ,if not Replace '/' with '\' and Append '\' at the end 
    		if(absoluteFilePath.contains(CmStatementExtractConstants.FRONTSLASH)&&
    				!absoluteFilePath.endsWith(CmStatementExtractConstants.FRONTSLASH)){
    			  filePath = absoluteFilePath + CmStatementExtractConstants.FRONTSLASH;	  
    		}
    		else if(absoluteFilePath.contains(CmStatementExtractConstants.BACKSLASH)&& !absoluteFilePath.endsWith(CmStatementExtractConstants.BACKSLASH)){
    			filePath = absoluteFilePath + CmStatementExtractConstants.BACKSLASH;
    		} else {
    			filePath = absoluteFilePath;	
    		}
    		
    		//Create File
    		bufferedWriter = createFile();
    		
    		//Batch Parameter
    		billableChargeCharacteristics = getParameters().getBillableChargeCharList().trim();
    		custNumberIdType = getParameters().getCustomerNumberIdType();
    		contracTypeList = getParameters().getContractTypeList();
    		adjustmentTypeList = getParameters().getAdjustmentTypeList();
    		adhocCustIdType = getParameters().getAdhocCustomerIdType();
    		adhocCustomerIdValueList = getParameters().getAdhocCustomerIdValueList();
    		adhocAcctIdType = getParameters().getAdhocAccountIdType();
    		adhocAccountIdValueList = getParameters().getAdhocAccountIdValueList();
    		
    		//Initialize Data Area
    		stmtExtractRecInst = DataAreaInstance.create(CmStatementExtractConstants.STATEMENT_DA);
			statementsGroup = stmtExtractRecInst.getGroup(CmStatementExtractConstants.STATEMENTS_GROUP);
			statementList = statementsGroup.getList(CmStatementExtractConstants.STATEMENT_LIST);

    	}
    	
		public ThreadExecutionStrategy createExecutionStrategy() {
			return new StandardCommitStrategy(this);
		}

		public WorkUnitResult executeWorkUnitDetailedResult(ThreadWorkUnit unit) throws ThreadAbortedException,
		RunAbortedException {
			WorkUnitResult workUnitResult = new WorkUnitResult(true);

			recordsProcessed = 0;
			recordsInError = 0;
			errorEncountered= Bool.FALSE;
			statementConstId = null;
			statementConst = null;
			stConstDet = null;
			acctList = new ArrayList<Account_Id>();
			CustomerClass_Id custClassId = null;
			CustomerClass custClass = null;
			
			//Retrieve Work Units
			try {
				//Start Change CB-368
				//batchAction = unit.getSupplementallData(CmStatementExtractConstants.BATCH_ACTION).toString();
				
				//For Non Adhoc and Adhoc Person
//				if(batchAction.equalsIgnoreCase(CmStatementExtractConstants.NON_ADHOC) 
//						|| batchAction.equalsIgnoreCase(CmStatementExtractConstants.ADHOC_PER)){
				//End Change CB-368
					if(notNull(unit.getSupplementallData(CmStatementExtractConstants.WARNING))){
						String entity = unit.getSupplementallData(CmStatementExtractConstants.WARNING).toString();
						if(entity.equalsIgnoreCase(CmStatementExtractConstants.PER_WARNING)){
							logError(CmMessageRepository.getServerMessage(CmMessages.NO_ACTIVE_STATEMENT_CONSTRUCT_EXISTS, adhocCustIdType.getId().getIdValue(),
									adhocCustomerIdValueList));
						}else if(entity.equalsIgnoreCase(CmStatementExtractConstants.ACCT_WARNING)){
							logError(CmMessageRepository.getServerMessage(CmMessages.NO_ACTIVE_STATEMENT_CONSTRUCT_EXISTS, adhocAcctIdType.getId().getIdValue(),
									adhocAccountIdValueList));
						}
						errorEncountered = Bool.TRUE;
					}else{
						//Start Change CB-368
						//statementConstId = (StatementConstruct_Id) unit.getPrimaryId();
						//if(notNull(statementConstId.getEntity())){
						//	statementConst = statementConstId.getEntity();
						//}
						stConstDetId = (StatementConstructDetail_Id) unit.getPrimaryId();
						if(notNull(stConstDetId.getEntity())){
							stConstDet = stConstDetId.getEntity();
							statementConst = stConstDetId.getEntity().getStatementConstruct();
							statementConstId = statementConst.getId();
						}
					}
					
				//For Adhoc Account
//				}else if(batchAction.equalsIgnoreCase(CmStatementExtractConstants.ADHOC_ACCT)){
//					if(notNull(unit.getSupplementallData(CmStatementExtractConstants.WARNING))){
//						logError(CmMessageRepository.getServerMessage(CmMessages.NO_ACTIVE_STATEMENT_CONSTRUCT_EXISTS, adhocAcctIdType.getId().getIdValue(),
//								adhocAccountIdValueList));
//					}else{
//						stConstDetId = (StatementConstructDetail_Id) unit.getPrimaryId();
//						if(notNull(stConstDetId.getEntity())){
//							stConstDet = stConstDetId.getEntity();
//							statementConst = stConstDetId.getEntity().getStatementConstruct();
//							statementConstId = statementConst.getId();
//						}
//					}
//				}
				//End Change CB-368
					
				session = (FrameworkSession) SessionHolder.getSession();
				session.setSavepoint(CmStatementExtractConstants.SAVE_POINT);
			} catch (Exception e) {
				if (notNull(e.getLocalizedMessage())) {
					addError(CmMessageRepository.batchProcError(getBatchControlId().getIdValue(),
							statementConstId.getIdValue(), e.getLocalizedMessage()));
				} else {
					addError(CmMessageRepository.batchProcError(getBatchControlId().getIdValue(),
							statementConstId.getIdValue(), e.getLocalizedMessage()));
				}
			}
			
			try{
				if(notNull(statementConst)){
					//Start Change CB-368
					//For Non Adhoc and Adhoc Person
//					if(batchAction.equalsIgnoreCase(CmStatementExtractConstants.NON_ADHOC) 
//							|| batchAction.equalsIgnoreCase(CmStatementExtractConstants.ADHOC_PER)){
//						//Loop through Statement Construct Detail from Statement Construct
//						for(StatementConstructDetail stConstDetLoop: statementConst.getDetails()){
//							//Retrieve Account List for Customer/NonAdhoc Level
//							if(notNull(stConstDetLoop.getAccountId())){
//								acctList.add(stConstDetLoop.getAccountId());
//								custClassId = stConstDetLoop.getAccountId().getEntity().getCustomerClass().getId();
//								if(notNull(custClassId.getEntity())){
//									//Retrieve Customer Class 
//									custClass = custClassId.getEntity();
//								}
//							}else{
//								logError(CmMessageRepository.getServerMessage(CmMessages.NO_STATEMENT_CONST_DET, statementConst.getId().getIdValue()));
//								errorEncountered = Bool.TRUE;
//								break;
//								
//							}
//						}
//					}else{
						custClass = stConstDet.fetchAccount().getCustomerClass();
//					}
					//End Change CB-368
					if(notNull(custClass)){
						//Retrieve Extendable Lookup Data		
						retrieveExtLookupData(custClass.getId().getIdValue());

						//Template
						COTSInstanceList templateListInstance = statementsGroup.getList(CmStatementExtractConstants.TEMPLATE_LIST);		
						COTSInstanceNode template;
						if(notNull(templateList) && !templateList.isEmpty()){
							for(String templateList : templateList){
								template = templateListInstance.newChild();
								template.set(CmStatementExtractConstants.TEMPLATE_CD, (!isBlankOrNull(templateList) ? templateList : CmStatementExtractConstants.EMPTY_STRING));
							}
						}
						//Map to Statement Extract Data Area
						//Start Change CB-368
						//populateStatementExtract(statementList);
						populateStatementExtract(statementList,stConstDetId.getEntity().fetchAccount().getId());
						//End Change CB-368
					}
				}
			}catch(ApplicationError e){
    			errorEncountered = Bool.TRUE;

    			session.rollbackToSavepoint(CmStatementExtractConstants.SAVE_POINT);         
    		}
			
			if (notNull(statementConst)) {
				recordsProcessed++;
				if (errorEncountered.isTrue()) {                   
					recordsInError++;
				}

				saveChanges();
			}

			workUnitResult.setUnitsInError(recordsInError);
			workUnitResult.setUnitsProcessed(recordsProcessed);

			return workUnitResult;
		}
		public void finalizeThreadWork() throws ThreadAbortedException,
		RunAbortedException {	

			//Start Change CB-368
			//if(notNull(batchAction)){
			//End Change CB-368
				statementsGroup.set(CmStatementExtractConstants.GRANDTOTAL, grandTotal);
				//Write XML to a File
				writeFile(stmtExtractRecInst.getDocument().asXML().replace(CmStatementExtractConstants.XML_CHAR, "").trim());

				// Close the extract files
				closeFile(bufferedWriter);

				//Update Statement Cycle to Complete
				//Start Change CB-368
				//if(batchAction.equalsIgnoreCase(CmStatementExtractConstants.NON_ADHOC)){
				if(isBlankOrNull(adhocCustomerIdValueList) && isBlankOrNull(adhocAccountIdValueList) && isNull(adhocAcctIdType) && isNull(adhocCustIdType)){
				//End Change CB-368 
					StatementCycleSchedule_DTO stmtCycleSchDto = null;
					if(!retrieveStatementCycleSchedule().isEmpty()){
						for(StatementCycleSchedule statementCycleSched : retrieveStatementCycleSchedule()){
							if(statementCycleSched.getStatementProcessStatus().isInProgress()){
								stmtCycleSchDto = statementCycleSched.getDTO();		
								stmtCycleSchDto.setStatementProcessStatus(StatementProcessStatusLookup.constants.COMPLETED);
								statementCycleSched.setDTO(stmtCycleSchDto);
							}
						}
					}
				}
			//}
		}
		//Start Change CB-368
		//private void populateStatementExtract(COTSInstanceList statementList){
		private void populateStatementExtract(COTSInstanceList statementList,Account_Id accountId){
			//End Change CB-368
			String customerNumber = CmStatementExtractConstants.EMPTY_STRING;
			String customerName = CmStatementExtractConstants.EMPTY_STRING;
			Address statementConstAddress = null;
			if(notNull(statementConst.getPerson())){
				customerName = getPersonName(statementConst.getPerson());
				//Start Change CB-368
				//customerNumber = getCustNumber(statementConst.getPerson());
				customerNumber = getCustNumber(statementConst.getPerson(),accountId);
				//End Change CB-368
			}
			if(notNull(statementConst.fetchAddress())){
				statementConstAddress = statementConst.fetchAddress();
			}
			COTSInstanceNode statementListNode = statementList.newChild();

			//Logo
			statementListNode.set(CmStatementExtractConstants.LOGO, logo);

			//Statement Date
			statementListNode.set(CmStatementExtractConstants.ST_DATE, getProcessDateTime().getDate());

			//Customer Name
			statementListNode.set(CmStatementExtractConstants.CUST_NAME, customerName);
			//Customer Number
			statementListNode.set(CmStatementExtractConstants.CUST_NUM, customerNumber);


			//Bill To Fields
			COTSInstanceNode billToGroup = statementListNode.getGroup(CmStatementExtractConstants.BILL_TO_GROUP);

			billToGroup.set(CmStatementExtractConstants.CUST_NAME, customerName);

			if(isNull(statementConstAddress)){
				billToGroup.set(CmStatementExtractConstants.ADDRESS1, CmStatementExtractConstants.EMPTY_STRING);
				billToGroup.set(CmStatementExtractConstants.ADDRESS2, CmStatementExtractConstants.EMPTY_STRING);
				billToGroup.set(CmStatementExtractConstants.ADDRESS3, CmStatementExtractConstants.EMPTY_STRING);
				billToGroup.set(CmStatementExtractConstants.ADDRESS4, CmStatementExtractConstants.EMPTY_STRING);
				billToGroup.set(CmStatementExtractConstants.CITY, CmStatementExtractConstants.EMPTY_STRING);
				billToGroup.set(CmStatementExtractConstants.STATE, CmStatementExtractConstants.EMPTY_STRING);
				billToGroup.set(CmStatementExtractConstants.COUNTY, CmStatementExtractConstants.EMPTY_STRING);
				billToGroup.set(CmStatementExtractConstants.POSTAL, CmStatementExtractConstants.EMPTY_STRING);
				billToGroup.set(CmStatementExtractConstants.COUNTRY, CmStatementExtractConstants.EMPTY_STRING);										
			}else{
				billToGroup.set(CmStatementExtractConstants.ADDRESS1, statementConstAddress.getAddress1());						
				billToGroup.set(CmStatementExtractConstants.ADDRESS2, statementConstAddress.getAddress2()); 						
				billToGroup.set(CmStatementExtractConstants.ADDRESS3, statementConstAddress.getAddress3()); 						
				billToGroup.set(CmStatementExtractConstants.ADDRESS4, statementConstAddress.getAddress4()); 						
				billToGroup.set(CmStatementExtractConstants.CITY, statementConstAddress.getCity()); 						
				billToGroup.set(CmStatementExtractConstants.STATE, statementConstAddress.getState()); 						
				billToGroup.set(CmStatementExtractConstants.COUNTY, statementConstAddress.getCounty()); 
				billToGroup.set(CmStatementExtractConstants.POSTAL,  statementConstAddress.getPostal()); 
				billToGroup.set(CmStatementExtractConstants.COUNTRY, statementConstAddress.getCountry()); 
			}
			billToGroup.set(CmStatementExtractConstants.EMAILID, isNull(statementConst.getPerson())
					? CmStatementExtractConstants.EMPTY_STRING : statementConst.getPerson().getEmailAddress());

			//Remit To Fields
			COTSInstanceNode remitToGroup = statementListNode.getGroup(CmStatementExtractConstants.REMIT_TO_GROUP);

			remitToGroup.set(CmStatementExtractConstants.NAME, remitToName);
			remitToGroup.set(CmStatementExtractConstants.BUS_UNIT, remitToBusinessUnit);
			remitToGroup.set(CmStatementExtractConstants.ADDRESS1, remitToAddress1);
			remitToGroup.set(CmStatementExtractConstants.ADDRESS2, remitToAddress2);
			remitToGroup.set(CmStatementExtractConstants.ADDRESS3, remitToAddress3);
			remitToGroup.set(CmStatementExtractConstants.ADDRESS4, remitToAddress4);
			remitToGroup.set(CmStatementExtractConstants.CITY, remitToCity);
			remitToGroup.set(CmStatementExtractConstants.STATE, remitToState);
			remitToGroup.set(CmStatementExtractConstants.COUNTY, remitToCounty);
			remitToGroup.set(CmStatementExtractConstants.POSTAL, remitToPostal);
			remitToGroup.set(CmStatementExtractConstants.COUNTRY, remitToCountry);
			remitToGroup.set(CmStatementExtractConstants.PHONE,remitToPhone);
			remitToGroup.set(CmStatementExtractConstants.EMAILID, remitToEmail);

			//Statement Details Fields
			COTSInstanceNode statementDetailsGroup = statementListNode.getGroup(CmStatementExtractConstants.ST_DETAILS_GROUP);
			COTSInstanceList statementDetailsList = statementDetailsGroup.getList(CmStatementExtractConstants.ST_DETAIL_LIST);
			subTotal = BigDecimal.ZERO;
			
			//Start Change CB-368
			//For Non Adhoc and Adhoc Person
//			if(batchAction.equalsIgnoreCase(CmStatementExtractConstants.NON_ADHOC) 
//					|| batchAction.equalsIgnoreCase(CmStatementExtractConstants.ADHOC_PER)){
//				if(!acctList.isEmpty()){
//					for(Account_Id acctId : acctList){
//						
//						//Financial Transactions
//						financialTransactions(acctId, statementDetailsList);
//
//					}
//				}
//			}
			//End Change CB-368

			//Start Change CB-368
			//For Adhoc Account
//			if(batchAction.equalsIgnoreCase(CmStatementExtractConstants.ADHOC_ACCT)){
			//End Change CB-368
				Account_Id acctId = stConstDet.getAccountId();
				//Financial Transactions
				financialTransactions(acctId, statementDetailsList);
//			}

			//Grand Total
			grandTotal = grandTotal.add(subTotal).setScale(CmStatementExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);

			if(subTotal.abs().equals(BigDecimal.ZERO)){
				logError(CmMessageRepository.getServerMessage(CmMessages.NO_UNMATCHED_ITEMS, customerNumber));
				statementListNode.set(CmStatementExtractConstants.SUBTOTAL, BigDecimal.ZERO);
			}else{
				//Sub Total
				statementListNode.set(CmStatementExtractConstants.SUBTOTAL, subTotal);
			}

		}
		
		/**
		 * Financial Transactions Setting per Account
		 * @param acctId
		 * @param statementDetailsList
		 */
		private void financialTransactions(Account_Id acctId, COTSInstanceList statementDetailsList){
			
			for(SQLResultRow fts: retrieveFinancialTransactions(acctId)){
				initializeStatementDetailVariables();

				transactionDate = fts.getDate(CmStatementExtractConstants.TRANSACTION_DATE);					
				consolidatedInvoiceNumber = fts.getString(CmStatementExtractConstants.TRANSACTION_ID);
				description = fts.getString(CmStatementExtractConstants.BS_DESC);
				billChargeId = new BillableCharge_Id(fts.getString(CmStatementExtractConstants.BILLABLE_CHG_ID));
				remainingBalance = fts.getBigDecimal(CmStatementExtractConstants.CUR_BAL).setScale(CmStatementExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
				if(fts.getString(CmStatementExtractConstants.FT_TYPE).equalsIgnoreCase(CmStatementExtractConstants.PAY)){
					Payment_Id payId = new Payment_Id(consolidatedInvoiceNumber);
					consolidatedInvoiceNumber = retrievePayTenderChar(payId);
				}

				//Retrieve Billable Charge Characteristic for every Billable Charge ID
				for(String bcChar : billableChargeCharacteristics.split(CmStatementExtractConstants.COMMA)){
					charTypeId = new CharacteristicType_Id(bcChar);
					if(charTypeId.getIdValue().equalsIgnoreCase(CmStatementExtractConstants.ORDER_NUMBER_CHAR)){
						orderNumber = retrieveBillableChargeChar(billChargeId, charTypeId);
					}
					if(charTypeId.getIdValue().equalsIgnoreCase(CmStatementExtractConstants.LOAN_NUMBER_CHAR)){
						loanNumber = retrieveBillableChargeChar(billChargeId, charTypeId);
					}
					if(charTypeId.getIdValue().equalsIgnoreCase(CmStatementExtractConstants.BORROWER_CHAR)){
						borrower = retrieveBillableChargeChar(billChargeId, charTypeId);
					}
					if(charTypeId.getIdValue().equalsIgnoreCase(CmStatementExtractConstants.ADDRESS_CHAR)){
						address = retrieveBillableChargeChar(billChargeId, charTypeId);
					}
					if(charTypeId.getIdValue().equalsIgnoreCase(CmStatementExtractConstants.STATE_CHAR)){
						state = retrieveBillableChargeChar(billChargeId, charTypeId);
					}
					if(charTypeId.getIdValue().equalsIgnoreCase(CmStatementExtractConstants.ZIP_CHAR)){
						zip = retrieveBillableChargeChar(billChargeId, charTypeId);
					}
				}

				subTotal = subTotal.add(remainingBalance).setScale(CmStatementExtractConstants.TWO, BigDecimal.ROUND_HALF_UP);
				//For every financial transactions
				mapStatementDetails(statementDetailsList, transactionDate, consolidatedInvoiceNumber, description, remainingBalance, 
						orderNumber, loanNumber, borrower, address, state, zip);
			}
		}
		
		/**
		 * Map Statement Details
		 * @param statementDetailsList
		 * @param transactionDate
		 * @param consolidatedInvoiceNumber
		 * @param description
		 * @param remainingBalance
		 * @param orderNumber
		 * @param loanNumber
		 * @param borrower
		 * @param address
		 * @param state
		 * @param zip
		 */		
		private void mapStatementDetails(COTSInstanceList statementDetailsList, Date transactionDate, String consolidatedInvoiceNumber, String description, BigDecimal remainingBalance, 
				String orderNumber, String loanNumber, String borrower, String address, String state, String zip){
			
			COTSInstanceNode statementDetListFields= statementDetailsList.newChild();
			statementDetListFields.set(CmStatementExtractConstants.DATE, transactionDate);
			statementDetListFields.set(CmStatementExtractConstants.CONSOLIDATED_INV_NUM, consolidatedInvoiceNumber);
			statementDetListFields.set(CmStatementExtractConstants.ORDER_NUM, orderNumber);
			statementDetListFields.set(CmStatementExtractConstants.DESCR, description);
			statementDetListFields.set(CmStatementExtractConstants.REM_BAL, remainingBalance);
			statementDetListFields.set(CmStatementExtractConstants.LOAN_NUM, loanNumber);
			statementDetListFields.set(CmStatementExtractConstants.BORROWER, borrower);
			statementDetListFields.set(CmStatementExtractConstants.ADDRESS, address);
			statementDetListFields.set(CmStatementExtractConstants.STATE, state);
			statementDetListFields.set(CmStatementExtractConstants.POSTAL, state);

		}
		
		/**
		 * Retrieve Financial Transactions
		 * @param acctId
		 * @return list of Financial Transactions
		 */
		private List<SQLResultRow> retrieveFinancialTransactions(Account_Id acctId){
			String listOfAdjustmentTypesSingleQuote = generateSingleQuoteCommaSeperatedString(adjustmentTypeList);
			String listOfContractTypesSingleQuote = generateSingleQuoteCommaSeperatedString(contracTypeList);
	
			StringBuilder stringBuilder = new StringBuilder();
			
			stringBuilder.append("WITH MAIN_QRY AS ( ");
			stringBuilder.append("SELECT SUM(FT.CUR_AMT) AS TOT_CUR_BAL, BSEGCALC.END_DT AS TXN_DT, BSEG.BILL_ID AS TXN_ID ");
			stringBuilder.append(", 'BSG' AS FT_TYPE ");
			stringBuilder.append(", BSEGCALC.BILLABLE_CHG_ID AS BILL_CHG_ID ");
			stringBuilder.append(", BSEGCALC.DESCR_ON_BILL AS BS_CALC_DESC ");
			stringBuilder.append("FROM CI_FT FT, CI_SA SA, CI_BSEG BSEG, CI_BSEG_CALC BSEGCALC ");
			stringBuilder.append("WHERE FT.SIBLING_ID = BSEG.BSEG_ID ");
			stringBuilder.append(" AND BSEG.SA_ID = SA.SA_ID ");
			stringBuilder.append("AND SA.ACCT_ID = :acctId ");
			stringBuilder.append("AND BSEGCALC.BSEG_ID = BSEG.BSEG_ID ");
			stringBuilder.append("AND FT.FREEZE_SW = :isFrozen ");
			stringBuilder.append("AND (FT.MATCH_EVT_ID = ' ' ");
			stringBuilder.append("OR EXISTS (SELECT 'X' FROM CI_FT FT2, CI_MATCH_EVT ME ");
			stringBuilder.append("WHERE FT.MATCH_EVT_ID = FT2.MATCH_EVT_ID ");
			stringBuilder.append("AND FT2.MATCH_EVT_ID = ME.MATCH_EVT_ID ");
			stringBuilder.append("AND ME.MEVT_STATUS_FLG <> :balanced ");
			stringBuilder.append("HAVING SUM(FT2.CUR_AMT) <> '0' ");
			stringBuilder.append("GROUP BY FT2.FT_ID)) ");
			stringBuilder.append("GROUP BY BSEGCALC.END_DT, BSEGCALC.BILLABLE_CHG_ID, BSEGCALC.DESCR_ON_BILL, BSEG.BILL_ID ");
			stringBuilder.append("UNION ");
			stringBuilder.append("SELECT SUM(FT.CUR_AMT) AS TOT_CUR_BAL, ADJ.CRE_DT AS TXN_DT, ADJ.ADJ_ID AS TXN_ID ");
			stringBuilder.append(", 'ADJ' AS FT_TYPE , ' ' AS BILL_CHG_ID, ' ' AS BS_CALC_DESC ");
			stringBuilder.append("FROM CI_FT FT, CI_SA SA, CI_ADJ ADJ ");
	    	stringBuilder.append("WHERE TRIM(FT.SA_ID) = TRIM(SA.SA_ID) ");
	    	stringBuilder.append("AND TRIM(FT.SIBLING_ID) = TRIM(ADJ.ADJ_ID) ");
			stringBuilder.append("AND TRIM(FT.PARENT_ID) IN (" + listOfAdjustmentTypesSingleQuote + ") ");
	    	stringBuilder.append("AND TRIM(SA.ACCT_ID) = :acctId ");
	    	stringBuilder.append("AND FT.FREEZE_SW = :isFrozen ");
	    	stringBuilder.append("AND (FT.MATCH_EVT_ID = ' ' ");
	    	stringBuilder.append("OR EXISTS (SELECT 'X' FROM CI_FT FT2, CI_MATCH_EVT ME ");
	    	stringBuilder.append("WHERE FT.MATCH_EVT_ID = FT2.MATCH_EVT_ID ");
	    	stringBuilder.append("AND FT2.MATCH_EVT_ID = ME.MATCH_EVT_ID ");
	    	stringBuilder.append("AND ME.MEVT_STATUS_FLG <> :balanced ");
	    	stringBuilder.append("HAVING SUM(FT2.CUR_AMT) <> '0' ");
	    	stringBuilder.append("GROUP BY FT2.FT_ID)) ");
	    	stringBuilder.append("GROUP BY ADJ.CRE_DT, ADJ.ADJ_ID ");
	    	stringBuilder.append("UNION ");
	    	stringBuilder.append("SELECT FT.CUR_AMT AS TOT_CUR_BAL,PE.PAY_DT AS TXN_DT, FT.PARENT_ID AS TXN_ID ");
	    	stringBuilder.append(", 'PAY' AS FT_TYPE , ' ' AS BILL_CHG_ID, ' ' AS BS_CALC_DESC ");
	    	stringBuilder.append("FROM CI_FT FT, CI_SA SA, CI_PAY PAY, CI_PAY_EVENT PE ");
	    	stringBuilder.append("WHERE FT.SA_ID = SA.SA_ID ");
	    	stringBuilder.append("AND SA.ACCT_ID = :acctId ");
	    	stringBuilder.append("AND SA.SA_TYPE_CD IN (" + listOfContractTypesSingleQuote + ") ");
	    	stringBuilder.append("AND FT.PARENT_ID = PAY.PAY_ID ");
	    	stringBuilder.append("AND PE.PAY_EVENT_ID = PAY.PAY_EVENT_ID ");
	    	stringBuilder.append("AND FT.FREEZE_SW = :isFrozen ");
	    	stringBuilder.append("AND FT.MATCH_EVT_ID = ' ' ");
	    	stringBuilder.append(") ");
	    	stringBuilder.append("SELECT * FROM MAIN_QRY ");
	    	stringBuilder.append("ORDER BY TXN_DT DESC ");

			PreparedStatement retFT = createPreparedStatement(stringBuilder.toString(), "Retrieve_Financial_Transactions");
			retFT.setAutoclose(false);
			retFT.bindId("acctId", acctId);
			retFT.bindBoolean("isFrozen", Bool.TRUE);
			retFT.bindLookup("balanced", MatchEventStatusLookup.constants.BALANCED);

			List<SQLResultRow> resRowList = retFT.list();
			retFT.close();
			return resRowList;	    	
	    	
		}
		
		/**
		 * Retrieve Billable Charge Characteristics Value
		 * @param billChargeId
		 * @param charTypeId
		 * @return char value
		 */
		private String retrieveBillableChargeChar(BillableCharge_Id billChargeId, CharacteristicType_Id charTypeId){
			CharacteristicTypeLookup charTypeLookup = charTypeId.getEntity().getCharacteristicType();
			
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(" FROM BilllableChargeCharacteristic bc ");
			stringBuilder.append(" WHERE bc.id.billableCharge.id = :billChargeId ");
			stringBuilder.append(" AND bc.id.characteristicType = :charType ");
					
			stringBuilder.append(" AND bc.id.effectiveDate = (SELECT MAX(bc2.id.effectiveDate) FROM BilllableChargeCharacteristic bc2 ");
			stringBuilder.append(" WHERE bc2.id.billableCharge.id = bc.id.billableCharge.id ");
			stringBuilder.append(" AND bc2.id.characteristicType = bc.id.characteristicType ");
			stringBuilder.append(" AND bc2.id.effectiveDate <= :procDate) ");
			
			Query<String> retrieveBillChar = createQuery(stringBuilder.toString(), "RETRIEVE_BILLABLE_CHG_CHAR");
			retrieveBillChar.bindId("billChargeId", billChargeId);
			retrieveBillChar.bindId("charType", charTypeId);
			retrieveBillChar.bindDate("procDate", getProcessDateTime().getDate());
			
			if (charTypeLookup.isPredefinedValue()){
				retrieveBillChar.addResult("charVal", "bc.characteristicValue");
			}
			else if (charTypeLookup.isForeignKeyValue()){
				retrieveBillChar.addResult("charVal", "bc.characteristicValueForeignKey1");
			}
			else {
				retrieveBillChar.addResult("charVal", "bc.adhocCharacteristicValue");
			}	
			
			return retrieveBillChar.firstRow();

		}																	
	    
		/**
		 * Retrieve Payment Tender Characteristic
		 * @param payId
		 * @return char val
		 */
	    private String retrievePayTenderChar(Payment_Id payId){
	    	StringBuilder stringBuilder = new StringBuilder();
	    	CharacteristicType_Id charType = new CharacteristicType_Id(CmStatementExtractConstants.PAY_TENDER_CHAR);
	    	stringBuilder.append(" FROM PaymentTender payTender, PaymentTenderCharacteristic payTenderChar, Payment pay ");
	    	stringBuilder.append(" WHERE payTender.id = payTenderChar.id.paymentTender ");
	    	stringBuilder.append(" AND payTender.paymentEvent.id = pay.paymentEvent.id ");
	    	stringBuilder.append(" AND pay.id = :payId ");
	    	stringBuilder.append(" AND payTenderChar.id.characteristicType = :charType ");
	    	
	    	Query<String> query = createQuery(stringBuilder.toString(), "RETRIEVE_PAY_CHAR");
	    	query.bindId("payId", payId);
	    	query.bindId("charType", charType);
	    	query.addResult("characteristicVal", "payTenderChar.adhocCharacteristicValue");
	    	return query.firstRow();
	    	
	    }
	    
	    /**
	     * Retrieve Person Name
	     * @param per
	     * @return Person Name
	     */
		private String getPersonName(Person per){
			
			StringBuilder stringBuilder= new StringBuilder();
			stringBuilder.append("FROM PersonName perName ");
			stringBuilder.append("WHERE perName.id.person = :perId ");
			stringBuilder.append("AND perName.isPrimaryName = :isPrimarySw ");
			
			Query<String> getPerNameQry = createQuery(stringBuilder.toString(), "Retrieve_Person_Name");
			getPerNameQry.bindId("perId", per.getId());
			getPerNameQry.bindBoolean("isPrimarySw", Bool.TRUE);
			getPerNameQry.addResult("personName", "perName.entityName");
			String perNameString = getPerNameQry.firstRow();
			if(isBlankOrNull(perNameString)){
				return CmStatementExtractConstants.EMPTY_STRING;
			}
			return perNameString;
		}
		
		/**
		 * Retrieve Customer Number
		 * @param per
		 * @return
		 */
		//Start Change CB-368
		public String getCustNumber(Person per,Account_Id accountId){
		//End Change CB-368
			StringBuilder stringBuilder= new StringBuilder();
			//Start Change CB-368
//			stringBuilder.append("FROM PersonId per ");
//			stringBuilder.append("WHERE per.id.person = :perId ");
//			stringBuilder.append("AND per.id.idType = :idTypeCd ");
//
//			Query<String> getCustNum = createQuery(stringBuilder.toString(), "Retrieve_Customer_Number");
//			getCustNum.bindId("perId", per.getId());	
//			getCustNum.bindId("idTypeCd", custNumberIdType.getId());
//
//			getCustNum.addResult("custNum", "per.personIdNumber");
			stringBuilder.append(" FROM AccountNumber AcctNbr, AccountPerson AcctPer ");
			stringBuilder.append(" WHERE AcctPer.id.account = AcctNbr.id.account ");
			stringBuilder.append(" AND AcctPer.id.person=:perId AND AcctNbr.id.account=:accountId ");
			stringBuilder.append(" And AcctNbr.id.accountIdentifierType=:acctIdTypeCd ");
			Query<String> getCustNum = createQuery(stringBuilder.toString(), "Retrieve_Account_Number");
			getCustNum.bindId("perId", per.getId());
			getCustNum.bindId("accountId", accountId);
			getCustNum.bindId("acctIdTypeCd", custNumberIdType.getId());
			getCustNum.addResult("custNum", "AcctNbr.accountNumber");
			//End Change CB-368
			String custNumStr = getCustNum.firstRow();
			
			if(isBlankOrNull(custNumStr)){
				return CmStatementExtractConstants.EMPTY_STRING;
			}
			return custNumStr;
		}
		
		/**
		 * Retrieve Statement Cycle Schedule
		 * @return List Statement Cycle Schedule
		 */
		private List<StatementCycleSchedule> retrieveStatementCycleSchedule(){
			List<StatementCycleSchedule> statementCycleSched = null;
			StringBuilder queryString = new StringBuilder();
			queryString.append(" FROM StatementCycleSchedule sch ");
			queryString.append(" WHERE sch.id.processDate <= :processDate ");
			
			Query<StatementCycleSchedule> query = createQuery(queryString.toString(), "Retrieve_StatementCycle_Schedule");	
			query.bindDate("processDate", getProcessDateTime().getDate());
			query.addResult("sch", "sch");
			if(query.listSize() > 0){	
				statementCycleSched = query.list();
			}
			return statementCycleSched;
		}
		
		/**
		 * Retrieve Extendable Lookup Values
		 * @param customerClass
		 */
		private void retrieveExtLookupData(String customerClass){
			statementExtLookup = CmStatementExtractExtLookupCache
					.getStatementExtractConfigLookupByCustClass(getParameters().getStatementExtractExtLookup(), customerClass);
		
			remitToName = statementExtLookup.getName();
			remitToBusinessUnit = statementExtLookup.getBusinessUnit();
			logo = statementExtLookup.getLogo();
			remitToAddress1 = statementExtLookup.getAddressLine1();
			remitToAddress2 = statementExtLookup.getAddressLine2();
			remitToAddress3 = statementExtLookup.getAddressLine3();
			remitToAddress4 = statementExtLookup.getAddressLine4();
			remitToCity = statementExtLookup.getCity();
			remitToState = statementExtLookup.getState();
			remitToPostal = statementExtLookup.getPostal();
			remitToCountry = statementExtLookup.getCountry();
			remitToCounty = statementExtLookup.getCounty();
			remitToPhone = statementExtLookup.getPhoneNbr();
			remitToEmail = statementExtLookup.getEmailId();
			templateList= statementExtLookup.getTemplate();
			
		}
		
		/**
		 * Initialize Extendable Lookup Variables
		 */
		private void initializeExtLookupVariables(){
			remitToName = CmStatementExtractConstants.EMPTY_STRING;
	    	remitToBusinessUnit = CmStatementExtractConstants.EMPTY_STRING;
			logo = CmStatementExtractConstants.EMPTY_STRING;
			remitToAddress1 = CmStatementExtractConstants.EMPTY_STRING;
			remitToAddress2 = CmStatementExtractConstants.EMPTY_STRING;
			remitToAddress3 = CmStatementExtractConstants.EMPTY_STRING;
			remitToAddress4 = CmStatementExtractConstants.EMPTY_STRING;
			remitToCity = CmStatementExtractConstants.EMPTY_STRING;
			remitToState = CmStatementExtractConstants.EMPTY_STRING;
			remitToCounty = CmStatementExtractConstants.EMPTY_STRING;
			remitToPostal = CmStatementExtractConstants.EMPTY_STRING;
			remitToCountry = CmStatementExtractConstants.EMPTY_STRING;
			remitToPhone = CmStatementExtractConstants.EMPTY_STRING;
			remitToEmail = CmStatementExtractConstants.EMPTY_STRING;
			templateList = null;
			grandTotal = BigDecimal.ZERO;
		}
		
		/**
		 * Initialize Statement Detail Variables
		 */
		private void initializeStatementDetailVariables(){
	
			transactionDate = null;
			consolidatedInvoiceNumber = CmStatementExtractConstants.EMPTY_STRING;
			orderNumber = CmStatementExtractConstants.EMPTY_STRING;
			description = CmStatementExtractConstants.EMPTY_STRING;
			remainingBalance = BigDecimal.ZERO;
			loanNumber = CmStatementExtractConstants.EMPTY_STRING;
			borrower = CmStatementExtractConstants.EMPTY_STRING;
			address = CmStatementExtractConstants.EMPTY_STRING;
			state = CmStatementExtractConstants.EMPTY_STRING;
			zip = CmStatementExtractConstants.EMPTY_STRING;
			billChargeId = null;
			charTypeId = null;
		}
		
		/**
		 * Generate Single Quote Separated String
		 * @param commaSeparatedString
		 */
		private String generateSingleQuoteCommaSeperatedString(String commaSeparatedString) {
			
			StringBuilder generatedString = new StringBuilder();
			
			String commaSeparatedStringList[] = commaSeparatedString.split(CmStatementExtractConstants.COMMA);
			
			for(int i = 0; i < commaSeparatedStringList.length; i++) {
				if(i == commaSeparatedStringList.length - 1) {
					generatedString.append(CmStatementExtractConstants.SINGLE_QUOTE + commaSeparatedStringList[i] + CmStatementExtractConstants.SINGLE_QUOTE);
				}
				else {
					generatedString.append(CmStatementExtractConstants.SINGLE_QUOTE + commaSeparatedStringList[i] + CmStatementExtractConstants.SINGLE_QUOTE 
							+ CmStatementExtractConstants.COMMA);
				}
				
			}
			
			
			return generatedString.toString();
		}
		
		/**
		 * Create File
		 * @return bufferedWriter
		 */
		private BufferedWriter createFile() {
		
			bufferedWriter = null;
			//Start Change CB-408
			//File extractFile = new File(filePath, fileName);
			File extractFile = new File(getReportingDirPath(filePath), fileName);
			//End Change CB-408
			
			//Check if the file already exists
			if(extractFile.exists()) {	
				addError(CmMessageRepository.getServerMessage(CmMessages.FILE_ALREADY_EXISTS, fileName, absoluteFilePath));
			}

			//Initialize the file
			try {
				bufferedWriter = new BufferedWriter(new FileWriter(extractFile));					
			} catch (Exception e) {	
				addError(CmMessageRepository.commonMessageIoFileError(CmStatementExtractConstants.OPENING_FILE,fileName,e.toString()));
			}
			
			return bufferedWriter;
		}
		
		/**
		 * Write on File
		 * @param recordLine
		 * @return Buffered Writer
		 */
		private BufferedWriter writeFile(String recordLine) {
			
			try{
				bufferedWriter.write(recordLine);
				bufferedWriter.newLine();
			}catch(Exception e){
				 addError(CmMessageRepository.commonMessageIoFileError(CmStatementExtractConstants.WRITING_FILE, fileName, e.toString()));
			}	
			return bufferedWriter;
		}		
		
		/**
		 * Close File
		 * @param bufferedWriter
		 */
		private void closeFile(BufferedWriter bufferedWriter) {
			
			// Close the extract file
			try{
				bufferedWriter.flush();
				bufferedWriter.close();
			}catch(Exception e){
				 addError(CmMessageRepository.commonMessageIoFileError(CmStatementExtractConstants.CLOSING_FILE,fileName,e.toString()));
			}
				
		}

		//Start Add CB-408
		/*
		 * This method will returns the reporting directory path
		 * @param filePath
		 * @return filePath 
		 */
		public static String getReportingDirPath(String filePath){
			if (filePath.startsWith(FileRequestConstants.SHARED_VARIABLE)){
				filePath = filePath.substring(FileRequestConstants.SHARED_VARIABLE.length()+1, filePath.length());
				filePath = FileRequestConstants.SHARED_DIRECTORY + File.separator + filePath;
			}else if (filePath.startsWith(FileRequestConstants.INSTALLED_VARIABLE)){
				filePath = filePath.substring(FileRequestConstants.INSTALLED_VARIABLE.length()+1, filePath.length());
				filePath = FileRequestConstants.INSTALLED_DIRECTORY + File.separator + filePath;
			}
			return filePath;
		}
		//End Add CB-408	
	}

}
