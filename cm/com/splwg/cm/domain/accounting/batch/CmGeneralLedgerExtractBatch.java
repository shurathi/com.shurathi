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
 * GL Extract
 * 
 * This batch program will be created to extract GL Records from ORMB. 
 * GL records will be extracted to an CSV file 
 * and will be processed to write in EBS Integration tables.
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-08-10  SAnart         CB-227.Initial Version.
 */

package com.splwg.cm.domain.accounting.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

//import javafx.scene.control.Separator;



import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.batch.WorkUnitResult;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateFormat; 
import com.splwg.base.api.datatypes.DateFormatParseException;
import com.splwg.base.api.datatypes.DateTime;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.domain.common.currency.Currency;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.ccb.domain.adjustment.adjustment.Adjustment_Id;
import com.splwg.ccb.domain.admin.accountingCalendar.AccountingCalendarCalendarPeriods;
import com.splwg.ccb.domain.admin.accountingCalendar.AccountingCalendar_Id;
import com.splwg.ccb.domain.admin.accountingCalendar.CalendarPeriod;
import com.splwg.ccb.domain.admin.adjustmentType.AdjustmentType_Id;
import com.splwg.ccb.domain.admin.data.RecordCICBSCLCFragment.BillSegmentCalcLine;
import com.splwg.ccb.domain.admin.generalLedgerDivision.GeneralLedgerDivision;
import com.splwg.ccb.domain.billing.billSegment.BillSegment;
import com.splwg.ccb.domain.billing.billSegment.BillSegmentCalculationHeader_Id;
import com.splwg.ccb.domain.billing.billSegment.BillSegment_Id;
import com.splwg.ccb.domain.common.featureConfiguration.FeatureConfigurationOptionsRetriever;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransaction;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransactionGeneralLedger;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransactionGeneralLedger_Id;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransaction_Id;
import com.splwg.ccb.domain.payment.payment.Payment_Id;
import com.splwg.cm.api.lookup.XgleOptTypFlgLookup;
import com.splwg.ccb.domain.common.featureConfiguration.FeatureConfigurationOptionData;
import com.splwg.ccb.domain.dataManagement.fileRequest.FileRequestMessageRepository;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileRequestConstants;
import com.splwg.cm.api.lookup.ExternalSystemTypeLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author SAnarthe 
 *
@BatchJob (modules = { "demo"},
 *      softParameters = { @BatchJobSoftParameter (name = MAX_ERRORS, type = string)
 *            , @BatchJobSoftParameter (name = DIST-THD-POOL, type = string)
 *            , @BatchJobSoftParameter (entityName = featureConfiguration, name = glExtractFeatureConfig, type = entity)
 *            , @BatchJobSoftParameter (entityName = characteristicType, name = receiptNumberCharType, required = true, type = entity)
 *            , @BatchJobSoftParameter (entityName = characteristicType, name = orderNumberCharacteristicType, required = true, type = entity)
 *            , @BatchJobSoftParameter (name = fileExtension, required = true, type = string)
 *            , @BatchJobSoftParameter (name = fileNamePrefix, required = true, type = string)
 *            , @BatchJobSoftParameter (name = filePath, required = true, type = string)})
 */
public class CmGeneralLedgerExtractBatch extends
		CmGeneralLedgerExtractBatch_Gen 
{
       
	private static final String EMPTY_STRING = "";
	private static final String FILEEXTENSION = ".";
	private static final String FRONTSLASH="/";
	private static final String BACKSLASH="\\" ;
	private static final String CSVEXTENSION=".csv";
	private static final String DELIMITER=",";
    private static final String FILE_PATH_PARM_NAME="filePath";
    private static final String INSTALL_DIR = "@INSTALL_DIR";
    private static final String SHARED_DIR  = "@SHARED_DIR";
    
	static Logger LOGGER = LoggerFactory.getLogger(CmGeneralLedgerExtractBatch.class);
   
    /**
	* Implemented method that retrieves the records to be processed.
	* @return Job Work object containing record(s) to process
	*/
    
	public JobWork getJobWork() 
	{
        Query<QueryResultRow> ccQuery = cmRetrieveRecords();	
		return createJobWorkForQueryIterator(ccQuery.iterate(), this);	
	}
	   
	   /**
		 * This method will create the work unit given a query result row
		 * @param queryResultRow - Query Result Row
		 * @return ThreadWorkUnit - Thread Work Unit containing details for the record to be used by the batch
		 */
	public ThreadWorkUnit createWorkUnit(QueryResultRow queryResultRow) 
	{
			FinancialTransaction_Id financialTransactionId=(FinancialTransaction_Id) queryResultRow.get("FT_ID");
			BigInteger glSequence=queryResultRow.getInteger("GL_SEQ_NBR");
			
			FinancialTransactionGeneralLedger_Id idddd = new FinancialTransactionGeneralLedger_Id(financialTransactionId, glSequence);
	
			ThreadWorkUnit threadWorkUnit = new ThreadWorkUnit();
			threadWorkUnit.setPrimaryId(idddd);
			return threadWorkUnit;
	}

	public Class<CmGeneralLedgerExtractBatchWorker> getThreadWorkerClass()
	{
		return CmGeneralLedgerExtractBatchWorker.class;
	}

	/**
	 * GL Extract Extract Batch Worker Class
	 */
	public static class CmGeneralLedgerExtractBatchWorker extends
			CmGeneralLedgerExtractBatchWorker_Gen 
	{
		
			private static BufferedWriter bufferedWriterRec;
			private static BufferedWriter bufferedWriterCtr;  
			private String fileName=EMPTY_STRING;
			private String extractFileName=EMPTY_STRING;
			private String ctrlFileName=EMPTY_STRING;
			private DateTime currentDateTime;
			private String seperator=EMPTY_STRING;
			private String fileExtention=EMPTY_STRING;
			FinancialTransaction_Id financialTransactionId;
			FinancialTransaction ft;
			FinancialTransactionGeneralLedger_Id ftGlId;
			FinancialTransactionGeneralLedger ftGL;
			
			private int totalCount =0;
			private Money  finaltotalDebit=Money.ZERO;
		    private Money  finaltotalCredit=Money.ZERO;
	
		    private String absoluteFilePath=EMPTY_STRING;
		    
			/**
			* Implemented method that defines the Execution Strategy
			* to be used by the batch process.
			* @return Execution Strategy
			*/
			public ThreadExecutionStrategy createExecutionStrategy() 
			{
				return new StandardCommitStrategy(this);
			}
			
		    /**
			 * Implemented method that performs main processing logic for each record retrieved by the batch process.
			 * @param unit Work Unit/Record returned by the getJobWork() method
			 * @return Work Unit Result object indicating the number of records processed and records in error
			 * @throws ThreadAbortedException Thread Aborted Exception
			 * @throws RunAbortedException Run Aborted Exception
			*/
			public WorkUnitResult executeWorkUnitDetailedResult(ThreadWorkUnit unit)
					throws ThreadAbortedException, RunAbortedException 
			{
				totalCount++;
				
				String ledgerId=EMPTY_STRING;
				String status=EMPTY_STRING;
				String setOfBooksId=EMPTY_STRING;
				String journalSourceName=EMPTY_STRING;
				String journalCategoryName=EMPTY_STRING;
				String accountingDate=EMPTY_STRING;
				String currencyCode=EMPTY_STRING;
				String dateCreated=EMPTY_STRING;
				String createdBy=EMPTY_STRING;
				String glAccount=EMPTY_STRING;
				String segment1=EMPTY_STRING;
				String segment2=EMPTY_STRING;
				String segment3=EMPTY_STRING;
				String segment4=EMPTY_STRING;
				String segment5=EMPTY_STRING;
				String segment6=EMPTY_STRING;
				String segment7=EMPTY_STRING;
				String segment8=EMPTY_STRING;
				String actualFlag=EMPTY_STRING;
				String enteredDebit=EMPTY_STRING;
				String enteredCredit=EMPTY_STRING;
				String accountedDebit=EMPTY_STRING;
				String accountedCredit=EMPTY_STRING;
				String periodName=EMPTY_STRING;
				String description=EMPTY_STRING;
				
				String adjustmentType="Adjustment Type";
				
				
				CmGLExtractRecord_Impl glExtRecord;
				glExtRecord = new CmGLExtractRecord_Impl();
					
				ftGlId=(FinancialTransactionGeneralLedger_Id)unit.getPrimaryId();
				ftGL=ftGlId.getEntity();
				ft = ftGL.getId().getFinancialTransaction();
				
				ledgerId=retrieveAndvalidateFeatureConfigOptions(XgleOptTypFlgLookup.constants.LEDGER_ID).trim();
				status=retrieveAndvalidateFeatureConfigOptions(XgleOptTypFlgLookup.constants.STATUS_FLAG).trim();
				setOfBooksId=retrieveAndvalidateFeatureConfigOptions(XgleOptTypFlgLookup.constants.LEDGER_ID).trim();
				journalSourceName=retrieveAndvalidateFeatureConfigOptions(XgleOptTypFlgLookup.constants.SOURCE_NAME).trim();
				actualFlag=retrieveAndvalidateFeatureConfigOptions(XgleOptTypFlgLookup.constants.ACTUAL_FLAG).trim();
				
				//journalCategoryName
				if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BS") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BX"))
				{
					journalCategoryName=retrieveAndvalidateFeatureConfigOptions(XgleOptTypFlgLookup.constants.SALES_INVOICE_DESC).trim();
				}
				if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("PS") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("PX"))
				{
					journalCategoryName=retrieveAndvalidateFeatureConfigOptions(XgleOptTypFlgLookup.constants.RECEIPT_DESC).trim();
				}
				if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("AD") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("AX"))
				{
					String adjId=ft.getParentId();
					AdjustmentType_Id id=new AdjustmentType_Id(adjId);
					PreparedStatement preparedStatement=null;	
			        preparedStatement = createPreparedStatement("Select A.DESCR from ci_adj_type_l A where A.ADJ_type_cd=:adjCd","");
			        preparedStatement.bindId("adjCd",id);
			        preparedStatement.setAutoclose(false);
			        SQLResultRow row = preparedStatement.firstRow();
			        if (notNull(row)) {
			        	journalCategoryName = row.getString("DESCR");
			        }
			        if (preparedStatement != null) {
			                preparedStatement.close();
			        }	
					//journalCategoryName=adjustmentType;
				}
				  /*DateFormat df1 = new DateFormat("dd-MMM-yyyy");
				  Date d=ft.getAccountingDate();	  
				  LOGGER.info("d"+d); 
				  String inputTxt=d.toString();
				//accountingDate= formatter.format(ft.getAccountingDate());
				try {
					accountingDate= df1.parseDate(inputTxt).toString();
				} catch (DateFormatParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				//AccountingDate
				Date d=ft.getAccountingDate();
				String inputTxt=d.toString();
				String[] dateArray= inputTxt.split("-");
				int monthNumber=Integer.parseInt(dateArray[1]);
				String month = new DateFormatSymbols().getShortMonths()[monthNumber - 1];
				accountingDate=dateArray[2]+"-"+month+"-"+dateArray[0];
			
				//currencyCode	
				currencyCode=ft.getCurrency().getId().getIdValue();
				
				
				//datedCreated and createdBy
				DateTime d1=ft.getFreezeDateTime();
				String inputTxt1=d1.getDate().toString();
				String[] dateArray2= inputTxt1.split("-");
				int monthNumber2=Integer.parseInt(dateArray2[1]);
				String month2 = new DateFormatSymbols().getShortMonths()[monthNumber2 - 1];
				dateCreated=dateArray2[2]+"-"+month2+"-"+dateArray2[0];
				createdBy=ft.getFrozenByUserId().getIdValue();
				
				//segment
				glAccount=ftGL.getGlAccount();
				String[] segmentArray= glAccount.split("\\.");
				if(segmentArray.length == 8)
				{	
					segment1=segmentArray[0];
					segment2=segmentArray[1];
					segment3=segmentArray[2];
					segment4=segmentArray[3];
					segment5=segmentArray[4];
					segment6=segmentArray[5];
					segment7=segmentArray[6];
					segment8=segmentArray[7];
				}
				
				//Entered debit and total debit
				Money enteredDebit1;
				enteredDebit1=ftGL.getOriginalPaymentAmount();
				Money zero = Money.ZERO;
				Money amount=ftGL.getAmount();
				Money totalDebit = Money.ZERO;
				if(enteredDebit1.isEqualTo(zero))
				{
					enteredDebit1=amount;
					if(enteredDebit1.isGreaterThan(zero))
					{
						totalDebit=enteredDebit1;
						enteredDebit=enteredDebit1.toPlainString();
					}
					if(enteredDebit1.isEqualTo(zero))
					{
						enteredDebit=zero.toPlainString();
						totalDebit=zero;
					}
				}
				if(enteredDebit1.isGreaterThan(zero))
				{
					enteredDebit=enteredDebit1.toPlainString();
					totalDebit=enteredDebit1;
				}
				finaltotalDebit=finaltotalDebit.add(totalDebit);
				
				//Entered credit and total credit 
				Money enteredCredit1;
				Money totalCredit = Money.ZERO;
				enteredCredit1=ftGL.getOriginalPaymentAmount();
				if(enteredCredit1.isEqualTo(zero))
				{
					enteredCredit1=amount;
					if(enteredCredit1.isLessThan(zero))
					{
						totalCredit=new Money(enteredCredit1.getAmount().abs());
						enteredCredit=totalCredit.toPlainString();
					}
					if(enteredCredit1.isEqualTo(zero))
					{
						totalCredit=zero;
						enteredDebit=zero.toPlainString();
					}
				}
				if(enteredCredit1.isLessThan(zero))
				{
					totalCredit=new Money(enteredCredit1.getAmount().abs());
					enteredCredit=totalCredit.toPlainString();
				}
				finaltotalCredit=finaltotalCredit.add(totalCredit);
				
				//Accounted debit
				if(amount.isGreaterThanOrEqual(zero))
				{
					accountedDebit=amount.toPlainString();
				}
				
				//Accounted credit
				if(amount.isLessThanOrEqual(zero))
				{
					amount=new Money(amount.getAmount().abs());
					accountedCredit=amount.toPlainString();
				}
				
				//period Name 
				GeneralLedgerDivision DIV=ft.getGlDivision();
				AccountingCalendar_Id calId=DIV.getCalendar().getId();
				Date AccountingDate=ft.getAccountingDate();
				AccountingCalendarCalendarPeriods accCalPer = calId.getEntity().getCalendarPeriods();
				 
				for(CalendarPeriod per : accCalPer)
				{
					//if(AccountingDate.isAfter(per.getBeginDate()) && AccountingDate.isBefore(per.getEndDate()))
					//{
					if(AccountingDate.isSameOrAfter(per.getBeginDate()) && AccountingDate.isSameOrBefore(per.getEndDate()))
				    {
					
						String pdate = per.getEndDate().toString();
						String[] dateArray3= pdate.split("-");
						int monthNumber3=Integer.parseInt(dateArray3[1]);
						String month3 = new DateFormatSymbols().getShortMonths()[monthNumber3 - 1];
						periodName=month3+"-"+dateArray3[0].substring(0, 2);
						break;
					}
				}
				
				//description 
				if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BS") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("BX"))
				{
					BillSegment_Id id=new BillSegment_Id(ft.getSiblingId());
					BillSegment seg=id.getEntity();
					PreparedStatement preparedStatement=null;
				    preparedStatement = createPreparedStatement("SELECT A.ADHOC_CHAR_VAL FROM ci_bill_chg_char A,ci_bseg_calc B"
			            		+ " WHERE B.BSEG_ID=:billSegId" + 
                                  " AND A.BILLABLE_CHG_ID = B.BILLABLE_CHG_ID" +
                                  " AND A.CHAR_TYPE_CD=:charTypeCd ORDER BY A.EFFDT DESC","");
			        preparedStatement.bindId("billSegId",id);
			        preparedStatement.bindId("charTypeCd",(getParameters().getOrderNumberCharacteristicType().getId()));
			        preparedStatement.setAutoclose(false);
			        SQLResultRow row = preparedStatement.firstRow();
			        if (notNull(row)) 
			        {
			            	description = row.getString("ADHOC_CHAR_VAL");
			         }
			         if (preparedStatement != null) 
			         {
			                preparedStatement.close();
			         }	
				}
				if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("PS") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("PX"))
				{
					Payment_Id id=new Payment_Id(ft.getParentId());
					PreparedStatement preparedStatement=null;	
			        preparedStatement = createPreparedStatement("SELECT C.ADHOC_CHAR_VAL FROM CI_PAY A,ci_pay_tndr B,ci_pay_tndr_char C WHERE"
			            		+ " A.PAY_ID =:payId" + 
                                  " AND A.PAY_EVENT_ID = B.PAY_EVENT_ID" +
                                  " AND B.PAY_TENDER_ID = C.PAY_TENDER_ID" +
                                  " AND C.CHAR_TYPE_CD=:charTypeCd","");
			        preparedStatement.bindId("payId",id);
			        preparedStatement.bindId("charTypeCd",(getParameters().getReceiptNumberCharType().getId()));
			        preparedStatement.setAutoclose(false);
			        SQLResultRow row = preparedStatement.firstRow();
			        if (notNull(row)) {
			            	description = row.getString("ADHOC_CHAR_VAL");
			        }
			        if (preparedStatement != null) {
			                preparedStatement.close();
			        }		
				}
				if(ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("AD") || ft.getFinancialTransactionType().getLookupValue().getId().getFieldValue().trim().equals("AX"))
				{
					description =ft.getSiblingId();
				}
				
				glExtRecord.setLedgerId(ledgerId);
				glExtRecord.setStatus(status);
				glExtRecord.setSetOfBookId(setOfBooksId);
				glExtRecord.setUserJESourceName(journalSourceName);
				glExtRecord.setUserJECategoryName(journalCategoryName);
				glExtRecord.setAccountingDate(accountingDate);
				glExtRecord.setCurrencyCode(currencyCode);
				glExtRecord.setDateCreated(dateCreated);
				glExtRecord.setCreatedBy(createdBy);
				glExtRecord.setActualFlag(actualFlag);
				glExtRecord.setSegment1(segment1);
				glExtRecord.setSegment2(segment2);
				glExtRecord.setSegment3(segment3);
				glExtRecord.setSegment4(segment4);
				glExtRecord.setSegment5(segment5);
				glExtRecord.setSegment6(segment6);
				glExtRecord.setSegment7(segment7);
				glExtRecord.setSegment8(segment8);
				glExtRecord.setEnteredDebit(enteredDebit);
				glExtRecord.setEnteredCredit(enteredCredit);
				glExtRecord.setAccountedDebit(accountedDebit);
				glExtRecord.setAccountedCredit(accountedCredit);
				glExtRecord.setPeriodName(periodName);
				glExtRecord.setDescription(description);

				
				cmWriteRecord(glExtRecord.cmCreateRecordString(), bufferedWriterRec, absoluteFilePath);
				WorkUnitResult workUnitResult;
				workUnitResult = new WorkUnitResult(true);
				return workUnitResult;	
				
			}
			
			/**
			 * Implemented method that executes work to be performed at the start of the thread run.
			 * @param initializationPreviouslySuccessful thread initialization boolean
			 * @throws ThreadAbortedException Thread Aborted Exception
			 * @throws RunAbortedException Run Aborted Exception
			 */
			public void initializeThreadWork(
					boolean initializationPreviouslySuccessful)
					throws ThreadAbortedException, RunAbortedException 
			{
				totalCount =0;
			    finaltotalDebit=Money.ZERO;
			    finaltotalCredit=Money.ZERO;
			    
			    CmGeneralLedgerExtractBatch_Gen.JobParameters jobParameters = getParameters();
		    	absoluteFilePath=jobParameters.getFilePath();
		    	
		    	if(!absoluteFilePath.startsWith(INSTALL_DIR) && !absoluteFilePath.startsWith(SHARED_DIR)){
		    		addError(FileRequestMessageRepository.filePathNeitherSharedNorInstalled());
		    	}	
		    	
		    	if(absoluteFilePath.startsWith(INSTALL_DIR))
		    	{
		    		
		    		
		    		absoluteFilePath = absoluteFilePath.substring(INSTALL_DIR.length()+1, absoluteFilePath.length());
		    		absoluteFilePath = FileRequestConstants.INSTALLED_DIRECTORY + File.separator + absoluteFilePath;
		    	}
		    	if(absoluteFilePath.startsWith(SHARED_DIR))
		    	{
		    		
		    		
		    		absoluteFilePath = absoluteFilePath.substring(SHARED_DIR.length()+1, absoluteFilePath.length());
		    		absoluteFilePath = FileRequestConstants.SHARED_DIRECTORY + File.separator + absoluteFilePath;
		    	}			
			  	
		    	
		    	LOGGER.info("File PATH "+absoluteFilePath);
		    	
		    	
				extractFileName=cmCreateFileName(absoluteFilePath);	
				ctrlFileName=cmCreateCtrlFileName(absoluteFilePath);
				
				CmGLExtractHeaderRecord_Impl headerRecord = new CmGLExtractHeaderRecord_Impl();
				CmGLExtractControlHeaderRecord_Impl ControlHeaderRecord = new CmGLExtractControlHeaderRecord_Impl();
				
				String headerString = headerRecord.cmCreateRecordString();
				String controlHeaderString = ControlHeaderRecord.cmCreateRecordString();		
				
				try {
					bufferedWriterRec = cmCreateFileAndWriteHeader(extractFileName, absoluteFilePath, headerString);
					bufferedWriterCtr  = cmCreateFileAndWriteHeader(ctrlFileName, absoluteFilePath, controlHeaderString);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			/**
			 * This method will initialize the file
			 * @param fileName - File Name
			 * @param filePath - File Path
			 * @return BufferedWriter - Buffered Writer for the file
			 */
			private BufferedWriter cmInitializeFile(String fileName, String filePath) 
			{	
				BufferedWriter bufferedWriter = null;
				File extractFile = new File(fileName);
				// Check if the file already exists
				if(extractFile.exists()) {	
					addError(CmMessageRepository.fileAlreadyExists(fileName, filePath));
				}
				// Initialize the file
				
				try 
				{
					bufferedWriter = new BufferedWriter(new FileWriter(extractFile));					
				} catch (Exception e) {	
					
					//addError(CmMessageRepository.commonMessageIoFileError("openfile",fileName,e.toString()));
					addError(CmMessageRepository.errorToOpenFile(fileName));
				}
				return bufferedWriter;
			}
			
			/**
			 * This method will create the file and write its corresponding header
			 * @param fileName - Formatted File Name
			 * @param filePath -File Path
			 * @param headerString - Header record
			 * @return BufferedWriter - Buffered Writer for the file
			 */
			private BufferedWriter cmCreateFileAndWriteHeader(String fileName, String fullFilePath, String headerString) throws IOException 
			{	
				BufferedWriter bufferedWriter = cmInitializeFile(fileName, fullFilePath);
				cmWriteRecord(headerString, bufferedWriter, fullFilePath);
				return bufferedWriter;
			}
			
			/**
			 * This method will write the record line to the output file
			 * @param recordLine - Record Line to be written to the output file
			 * @param bufferedWriter - Buffered Writer for the output file
			 * @param filePath -  File Path
			 */
			private void cmWriteRecord(String recordLine, BufferedWriter bufferedWriter, String filePath) 
			{	
				try
				{
					bufferedWriter.write(recordLine);
					bufferedWriter.newLine();
				}
				catch(Exception e)
				{
					
					LOGGER.info("in write File Error "+fileName);
					 addError(CmMessageRepository.errorToOpenFile(fileName));
					 
				}	
			}
			public String cmCreateFileName(String filePath) 
			{
				seperator=File.separator;
				currentDateTime=getProcessDateTime();
				fileExtention=getParameters().getFileExtension();
				fileName=getParameters().getFileNamePrefix();
				
				String formattedDateTime = null;
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
				java.util.Date tempDate = new java.util.Date();
				try {
					tempDate = dateFormat.parse(currentDateTime.toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dateFormat = new SimpleDateFormat("ddMMyyyy HHmmss");
				formattedDateTime = dateFormat.format(tempDate).toUpperCase();
				formattedDateTime=formattedDateTime.replace(' ', '_');

				String fileName1=filePath+seperator+fileName+getBatchNumber()+"_"+getBatchThreadNumber()+
						               "_"+"BATCH"+"_"+formattedDateTime+"."+fileExtention;
				return fileName1;
				
			
				//String fileName1=filePath+seperator+fileName+""+getBatchNumber()+""+getBatchThreadNumber()+
						              // ""+"BATCH"+""+currentDateTime+"."+fileExtention;
				//return fileName1;
			}
			
			/**
			*  This Method and Return File Name
			*/
			public String cmCreateCtrlFileName(String filePath) 
			{
				seperator=File.separator;
				currentDateTime=getProcessDateTime();
				fileExtention=getParameters().getFileExtension();
				fileName=getParameters().getFileNamePrefix();
				
				String formattedDateTime = null;
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
				java.util.Date tempDate = new java.util.Date();
				try {
					tempDate = dateFormat.parse(currentDateTime.toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dateFormat = new SimpleDateFormat("ddMMyyyy HHmmss");
				formattedDateTime = dateFormat.format(tempDate).toUpperCase();
				formattedDateTime=formattedDateTime.replace(' ', '_');
				
				String fileName1=filePath+seperator+fileName+getBatchNumber()+"_"+getBatchThreadNumber()+
						               "_"+"CONTROL"+"_"+formattedDateTime+"."+fileExtention;
				return fileName1;
			}
			
			/**
			*  This Method will validate the Feature Configuration Option Type and Retrieve its values
			*  If no Option Type then raise an error
			*/
		    	 
			public String retrieveAndvalidateFeatureConfigOptions(XgleOptTypFlgLookup optionTypeFlgLookup)
			{

				FeatureConfiguration featConfig = getParameters().getGlExtractFeatureConfig();    
				FeatureConfigurationOptionsRetriever featureConfigOptRetriever = FeatureConfigurationOptionsRetriever.Factory.newInstance();
				List<FeatureConfigurationOptionData> featureConfigOptionData = 
				featureConfigOptRetriever.getFeatureConfigurationOptions(featConfig,optionTypeFlgLookup.getLookupValue().fetchIdFieldValue(),ExternalSystemTypeLookup.constants.EBS_G_L_EXTRACT);

				if(featureConfigOptionData.size()==0)
				{
					addError(CmMessageRepository.missFeatureOpt(optionTypeFlgLookup.getLookupValue().fetchLanguageDescription(),featConfig.getId().getIdValue()));    
				}  
				return featureConfigOptionData.get(0).getOptionValue().trim();
			}

			/**
			 * This method will close the file
			 * @param bufferedWriter - Buffered Writer of the file to be closed
			 * @param filePath -  File Path
			 */
			private void cmCloseFile(BufferedWriter bufferedWriter, String fullFilePath) 
			{
				try 
				{
					bufferedWriter.close();
				}
				catch(Exception e)
				{
					logError(CmMessageRepository.commonMessageIoFileError("closing", fullFilePath, e.getMessage()));
				}
				
			}

			/**
			 * Implemented method that executes work to be performed at the end of the thread run,
			 * and also created Control File
			 * after all Work Units for the current thread have been processed.
			 * @throws ThreadAbortedException Thread Aborted Exception
			 * @throws RunAbortedException Run Aborted Exception
			 */
			public void finalizeThreadWork() throws ThreadAbortedException,
			RunAbortedException
			{
				CmGLExtractControlRecord_Impl glExtCtrl;
				glExtCtrl = new CmGLExtractControlRecord_Impl();
				String currentDateTimeStr=currentDateTime.toString();
				String formattedDateTime = null;
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				java.util.Date tempDate = new java.util.Date();
				try {
					tempDate = dateFormat.parse(currentDateTime.getDate().toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				formattedDateTime = dateFormat.format(tempDate).toUpperCase();
				
				glExtCtrl.setTotalCredits(finaltotalCredit.toPlainString());
				glExtCtrl.setTotalDebits(finaltotalDebit.toPlainString());
				glExtCtrl.setTotalCount(Integer.toString(totalCount));
				glExtCtrl.setExtractedDate(formattedDateTime);
				glExtCtrl.setBatchId(getBatchNumber().toString());
				cmWriteRecord(glExtCtrl.cmCreateCtrRecordString(), bufferedWriterCtr, absoluteFilePath);
				
				cmCloseFile(bufferedWriterRec, absoluteFilePath);
				cmCloseFile(bufferedWriterCtr, absoluteFilePath);
			}
	}
	/**
	 * This Method will Fetch the GLExtract Records
	 * @return sqlResultList
	 */
	private Query<QueryResultRow> cmRetrieveRecords() 
	{
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("FROM FinancialTransactionProcess A, FinancialTransactionGeneralLedger B, FinancialTransaction C  ");
		queryBuilder.append("WHERE A.id.financialTransaction.id=C.id ");
		queryBuilder.append("AND C.id=B.id.financialTransaction.id ");
		queryBuilder.append("AND A.batchControlId = :batchCode ");
		queryBuilder.append("AND A.batchNumber = :batchNumber ");

		Query<QueryResultRow> ccQuery = createQuery(queryBuilder.toString(), EMPTY_STRING);
		ccQuery.bindId("batchCode", this.getBatchControlId());
		ccQuery.bindBigInteger("batchNumber", this.getBatchNumber());
		ccQuery.addResult("FT_ID", "C.id");
		ccQuery.addResult("GL_SEQ_NBR", "B.id.glSequence");
		
		return ccQuery;
	}

}
