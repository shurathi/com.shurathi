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
 *
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 *The AP Request Extract batch process will retrieve all records 
 *in the AP Request table stamped as Not Selected for Payment. 
 *Once the record is properly processed for extract, 
 *the batch will update the AP Request record as Requested for Payment.
 *  
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * YYYY-MM-DD  	IN     		Reason text.  
 * 2020-05-27   SPatil	    Initial Version.  
 * 2020-06-11   SPatil      CB-127
***********************************************************************
 */
package com.splwg.cm.domain.accounting.batch;
 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.Query;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.batch.WorkUnitResult;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.featureConfiguration.FeatureConfiguration;
import com.splwg.ccb.api.lookup.AdjustmentStatusLookup;
import com.splwg.ccb.api.lookup.PaymentSelectionStatusLookup;
import com.splwg.ccb.domain.adjustment.adjustment.ApCheckRequest;
import com.splwg.ccb.domain.adjustment.adjustment.ApCheckRequest_DTO;
import com.splwg.ccb.domain.adjustment.adjustment.ApCheckRequest_Id;
import com.splwg.ccb.domain.admin.accountRelationshipType.AccountRelationshipType_Id;
import com.splwg.ccb.domain.common.featureConfiguration.FeatureConfigurationOptionData;
import com.splwg.ccb.domain.common.featureConfiguration.FeatureConfigurationOptionsRetriever;
import com.splwg.cm.api.lookup.ExternalSystemTypeLookup;
import com.splwg.cm.api.lookup.XapeOptTypFlgLookup;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;

/**
 * @author SPatil
 *
@BatchJob (modules={},multiThreaded = false, rerunnable = false,
 *      softParameters = { @BatchJobSoftParameter (name = MAX_ERRORS, type = string)
 *            , @BatchJobSoftParameter (name = DIST-THD-POOL, type = string)
 *            , @BatchJobSoftParameter (entityName = idType, name = customerIdentifierType, required = true, type = entity)
 *            , @BatchJobSoftParameter (name = accountRelationshipTypeList, type = string)
 *            , @BatchJobSoftParameter (entityName = featureConfiguration, name = featureConfigForApExtract, required = true, type = entity)
 *            , @BatchJobSoftParameter (name = filePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = fileName, required = true, type = string)})
 */

public class CmApRequestExtractBatch extends CmApRequestExtractBatch_Gen {
	
	//Constants
	private static final String FILEEXTENSION = ".";
	private static final String EMPTY_STRING=" ";
	private static final String FRONTSLASH="/";
	private static final String BACKSLASH="\\" ;
	private static final String CSVEXTENSION=".csv";
	private static final String DELIMITER=",";
	private static final String AP_EXTRACT="AP extract";
    private static final String FILE_PATH_PARM_NAME="filePath";
	
	//Work Variables
	private String fileName =EMPTY_STRING;
	private String absoluteFilePath = EMPTY_STRING;
	
	//Start Add - CB-127
	private String accountRelationshipTypeList;
	private AccountRelationshipType_Id accRelId;
	//End Add - CB-127
	
	/**
	 * Performs initial batch parameter validations.
	 * @param isNewRun parameter validation switch
	 */
    @Override
	public void validateSoftParameters(boolean isNewRun) { 	
	
	//Start Add - CB-127
	if(notNull(getParameters().getAccountRelationshipTypeList())){
    cmValidateAccountRelationshipType();
	}
	//End Add - CB-127
	
    cmValidateFileNameAndExtension();
    cmValidateFeatureConfiguration(); 
 
	// Validate if File Path exists
    absoluteFilePath=getParameters().getFilePath().trim();
	File folder = new File(absoluteFilePath);
	if(!folder.exists()) {
		addError(CmMessageRepository.invalidPath(this.getBatchControlId().getEntity().getParameterByName(FILE_PATH_PARM_NAME).fetchLanguageDescription(),absoluteFilePath));
	}
		
	// Validate if File Path is a valid directory
	if(!folder.isDirectory()) {
		addError(CmMessageRepository.invalidDirectory(this.getBatchControlId().getEntity().getParameterByName(FILE_PATH_PARM_NAME).fetchLanguageDescription(),absoluteFilePath));
	}
    }
    
	/**
	* Implemented method that retrieves the records to be processed.
	* @return Job Work object containing record(s) to process
	*/
    public JobWork getJobWork() {
    	
	List<ThreadWorkUnit> workUnits = new ArrayList<ThreadWorkUnit>();
	ThreadWorkUnit workUnit = new ThreadWorkUnit();

	workUnits.add(workUnit);
	return createJobWorkForThreadWorkUnitList(workUnits);
    }
       
	/**
	* Returns the batch program's inner Thread Worker class.
	* @return Batch Worker class
	*/
	public Class<CmApRequestExtractBatchWorker> getThreadWorkerClass() {
		
		return CmApRequestExtractBatchWorker.class;
	}
		
	/**
	 * This method will check for extension of fileName . 
    */
	public void cmValidateFileNameAndExtension()
	{
	fileName=getParameters().getFileName().trim();
	int extensionIndex1 = fileName.lastIndexOf(FILEEXTENSION);  	
	if(extensionIndex1>0)
	{
		if(!fileName.contains(CSVEXTENSION))
		{
			addError(CmMessageRepository.invalidFileFormat(AP_EXTRACT,CSVEXTENSION));
		}
	}
	else
	{
		fileName=fileName.concat(CSVEXTENSION).trim();
	}
	}
	
	 /**
      * This method will validate Feature Configuration of option type "XAPE" 
      */
	public void cmValidateFeatureConfiguration()
    { 
	StringBuilder queryBuilder = new StringBuilder();
	queryBuilder.append("FROM FeatureConfiguration featConfig ");
	queryBuilder.append("WHERE featConfig.featureType =:featureType AND featConfig.id =:featureName " );
    Query<FeatureConfiguration> featConfigQuery = createQuery(queryBuilder.toString(), "");
	featConfigQuery.bindLookup("featureType", ExternalSystemTypeLookup.constants.EBS_AP_REQUEST_EXTRACT);
	featConfigQuery.bindEntity("featureName", getParameters().getFeatureConfigForApExtract());
	FeatureConfiguration featureConfiguration = featConfigQuery.firstRow();
	if(isNull(featureConfiguration))
    {
		 addError(CmMessageRepository.missFeatConfig(ExternalSystemTypeLookup.constants.EBS_AP_REQUEST_EXTRACT.getLookupValue().fetchLanguageDescription()));	   
    }
	}
	  
	 //Start Add - CB-127  
	public void cmValidateAccountRelationshipType()
	{
	 accountRelationshipTypeList=getParameters().getAccountRelationshipTypeList().trim();
	    for(String acctRel : accountRelationshipTypeList.split(DELIMITER))
	    { 
	       accRelId = new AccountRelationshipType_Id(acctRel);
	       if(isNull(accRelId.getEntity()))
	         {
		       addError(CmMessageRepository.invalidAccountRelationshiptype(acctRel));	
	         }  
	    }
	}
	//End Add - CB-127
	
	/**
	 * Ap Request Extract Batch Worker Class
	 */
	public static class CmApRequestExtractBatchWorker extends
								CmApRequestExtractBatchWorker_Gen {
	//WorkVariables
	private static BufferedWriter bufferedWriter;
	private String featCongigOptCoatVal=EMPTY_STRING;
	private String featConfigOptInccVal=EMPTY_STRING;
	private String featConfigOptSrcVal=EMPTY_STRING;
	private String featConfigOptPaytVal=EMPTY_STRING;
	private String featConfigOptPmlcVal=EMPTY_STRING;
	private String featConfigOptEisVal=EMPTY_STRING;
	private String filePath=EMPTY_STRING;   
	private String fileName=EMPTY_STRING;
	private String absoluteFilePath=EMPTY_STRING; 
	private static final String  OPENING_FILE="opening";
	private static final String  CLOSING_FILE="closing";
	private static final String  WRITING_FILE="writing";
	//Start Add - CB-127
	private String accountRelationshipTypeList;
	private String[] list;
	//End Add - CB-127
	     
	/**
	* Implemented method that defines the Execution Strategy
	* to be used by the batch process.
	* @return Execution Strategy
	*/

	public ThreadExecutionStrategy createExecutionStrategy() {
		
		return new StandardCommitStrategy(this);
	}		

	/**
	 * Implemented method that executes work to be performed at the start of the thread run.
	 * @param initializationPreviouslySuccessful thread initialization boolean
	 * @throws ThreadAbortedException Thread Aborted Exception
	 * @throws RunAbortedException Run Aborted Exception
	 */
	public void initializeThreadWork(
			boolean initializationPreviouslySuccessful)
			throws ThreadAbortedException, RunAbortedException {

	//Retrieve the Batch Parameters
	absoluteFilePath=getParameters().getFilePath().trim();
	fileName=getParameters().getFileName().trim();
	
	//Start Add - CB-127
	if(notNull(getParameters().getAccountRelationshipTypeList()))
	{
		accountRelationshipTypeList = getParameters().getAccountRelationshipTypeList().toString().trim();
	}
	//End Add - CB-127
	
	//Validating for path if it has '\' ,if not Replace '/' with '\' and Append '\' at end 
	if(absoluteFilePath.contains(FRONTSLASH)& !absoluteFilePath.endsWith(FRONTSLASH))
	{
		  filePath = absoluteFilePath+FRONTSLASH;	  
	}
	else if(absoluteFilePath.contains(BACKSLASH)& !absoluteFilePath.endsWith(BACKSLASH))
	{
		filePath = absoluteFilePath+BACKSLASH;
	}
	else
	{
		filePath=absoluteFilePath;	
	}
			
	//Validating if fileName has ".csv " extension if not then concatenate the fileName with .csv extension.
	if(!fileName.contains(CSVEXTENSION))
	{
		fileName=fileName.concat(CSVEXTENSION).trim();
	}
	
	//Creating File and Write Header
	CmApReqExtractHeaderRecord_Impl headerRecord = new CmApReqExtractHeaderRecord_Impl();
	String headerString = headerRecord.cmCreateRecordString();					
	bufferedWriter = cmCreateFileAndWriteHeader(fileName, filePath, headerString);					
	}		
		
    /**
	 * Implemented method that performs main processing logic for each record retrieved by the batch process.
	 * @param unit Work Unit/Record returned by the getJobWork() method
	 * @return Work Unit Result object indicating the number of records processed and records in error
	 * @throws ThreadAbortedException Thread Aborted Exception
	 * @throws RunAbortedException Run Aborted Exception
	*/
	public WorkUnitResult executeWorkUnitDetailedResult(ThreadWorkUnit unit)
			throws ThreadAbortedException, RunAbortedException {
	
	//Retrieve and validate the Feature Configuration Option Type
	featCongigOptCoatVal = retrieveAndvalidateFeatureConfigOptions(XapeOptTypFlgLookup.constants.CONTROL_AMOUNT).trim();
	featConfigOptInccVal = retrieveAndvalidateFeatureConfigOptions(XapeOptTypFlgLookup.constants.INVOICE_CURRENCY_CODE).trim();
	featConfigOptSrcVal = retrieveAndvalidateFeatureConfigOptions(XapeOptTypFlgLookup.constants.SOURCE).trim();
	featConfigOptPaytVal = retrieveAndvalidateFeatureConfigOptions(XapeOptTypFlgLookup.constants.PAYMENT_TERMS).trim();
	featConfigOptPmlcVal = retrieveAndvalidateFeatureConfigOptions(XapeOptTypFlgLookup.constants.PAYMENT_METHOD_LOOKUP_CODE).trim();
	featConfigOptEisVal = retrieveAndvalidateFeatureConfigOptions(XapeOptTypFlgLookup.constants.EPAYABLES_INVOICE_STATUS).trim();
	
	//Setting FeatureConfiguration Option Value 
	CmApReqExtractRecord_Impl apreqRecord;
	apreqRecord = new CmApReqExtractRecord_Impl();	
	apreqRecord.setSource(featConfigOptSrcVal);
	apreqRecord.setPayMethodLookupCd(featConfigOptPmlcVal);	
	apreqRecord.setPaymentTerms(featConfigOptPaytVal);
	List<SQLResultRow> apRequests = fetchApRequests();	
	
	//Initializing the Variables 
	String customerNumVal=EMPTY_STRING;
	String address1Value=EMPTY_STRING;
	String address2Value=EMPTY_STRING;
	String address3Val=EMPTY_STRING;
	String address4Val=EMPTY_STRING;
	String cityVal=EMPTY_STRING;
	String stateVal=EMPTY_STRING;
	String countryVal=EMPTY_STRING;
	String zipVal=EMPTY_STRING;
	String invoiceNumVal=EMPTY_STRING;
	String invoiceDateVal=EMPTY_STRING;
	String invoiceAmtVal=EMPTY_STRING;
	String invoiceCurrencyCodeVal=EMPTY_STRING;
	String distCodeConcatenatedVal=EMPTY_STRING;
	String receiptNumberVal=EMPTY_STRING;
	String apReqIdVal=EMPTY_STRING;
	String overMailName1Val=EMPTY_STRING;
	String overMailName2Val=EMPTY_STRING;
	String overMailName3Val=EMPTY_STRING;
	String entityNameVal=EMPTY_STRING;
	String invoiceCurrCode=EMPTY_STRING;
	String customerName =EMPTY_STRING;
	
          
	for(SQLResultRow req :apRequests)
	{
	  customerNumVal=req.getString("CUSTOMER_NUM").trim();
	  address1Value= req.getString("ADDRESS1").trim();
	  address2Value=req.getString("ADDRESS2").trim();
	  address3Val=req.getString("ADDRESS3").trim();
	  address4Val=req.getString("ADDRESS4").trim();
	  cityVal=req.getString("CITY").trim();
	  stateVal=req.getString("STATE").trim();
	  countryVal=req.getString("COUNTRY").trim();
	  zipVal=req.getString("ZIP").trim();
	  invoiceNumVal=req.getString("INVOICE_NUM").trim();
	  invoiceDateVal=req.getString("INVOICE_DATE").trim();
	  invoiceAmtVal=req.getString("INVOICE_AMT").trim();
	  invoiceCurrencyCodeVal=req.getString("INVOICE_CURRENCY_CODE").trim();
	  distCodeConcatenatedVal=req.getString("DIST_CODE_CONCATENATED").trim();
	  receiptNumberVal=req.getString("RECEIPT_NUMBER").trim();
	  apReqIdVal=req.getString("AP_REQUEST_ID").trim();
	  overMailName1Val = req.getString("OVERRIDE_MAILNAME1").trim();
	  overMailName2Val = req.getString("OVERRIDE_MAILNAME2").trim();
	  overMailName3Val = req.getString("OVERRIDE_MAILNAME3").trim();
	  entityNameVal = req.getString("ENTITY_NAME").trim();
			  
	//Checking for Invoice Currency Code value ,if null then provide the default value from FeatureConfiguaration
	if(isBlankOrNull(invoiceCurrencyCodeVal))
	{
	 invoiceCurrCode=featConfigOptInccVal;
	}
	else
	{
	 invoiceCurrCode=invoiceCurrencyCodeVal;
	}
	  
	//Retrieve Customer Name 	
	if(!isBlankOrNull(overMailName1Val))
	{
		customerName=overMailName1Val;	
	}
	else if(!isBlankOrNull(overMailName2Val))
	{
		customerName=overMailName2Val;	
	}
	else if(!isBlankOrNull(overMailName3Val))
	{
		customerName=overMailName3Val;	
	}
	else
	{
		  customerName=entityNameVal;
		  customerName = customerName.replace(DELIMITER,EMPTY_STRING).trim();
	}
					
	//Setting the ApRequest Records	
	apreqRecord.setCustomerNum(customerNumVal);
	apreqRecord.setCustomerName(customerName);
	apreqRecord.setAddress1(address1Value);
	apreqRecord.setAddress2(address2Value);
	apreqRecord.setAddress3(address3Val);
	apreqRecord.setAddress4(address4Val);
	apreqRecord.setCity(cityVal);
	apreqRecord.setState(stateVal);
	apreqRecord.setCountry(countryVal);
	apreqRecord.setZip(zipVal);
	apreqRecord.setInvoiceNum(invoiceNumVal);
	apreqRecord.setInvoiceDate(invoiceDateVal);
	apreqRecord.setInvoiceAmt(invoiceAmtVal);
	apreqRecord.setControlAmt(featCongigOptCoatVal);
	apreqRecord.setInvoiceCurrCode(invoiceCurrCode);				
	apreqRecord.setDistCodeConcat(distCodeConcatenatedVal);
	apreqRecord.setReceiptNumber(receiptNumberVal);
		
	//Write the ApReq Records details
	 cmWriteRecord(apreqRecord.cmCreateRecordString(), bufferedWriter, filePath);

   //update AP Payment Status Flag to requested for payment
	 cmUpdateApCheckReqFlag(apReqIdVal);	
	}
         
	WorkUnitResult workUnitResult;
	workUnitResult = new WorkUnitResult(true);
	workUnitResult.setUnitsProcessed(apRequests.size());
	return workUnitResult;	
	}
	
	/**
	 * Implemented method that executes work to be performed at the end of the thread run,
	 * after all Work Units for the current thread have been processed.
	 * @throws ThreadAbortedException Thread Aborted Exception
	 * @throws RunAbortedException Run Aborted Exception
	 */
	public void finalizeThreadWork() throws ThreadAbortedException,
			RunAbortedException {
		
	 // Close the extract files
		cmCloseFile(bufferedWriter, filePath);
	}
	
	/**
	*  This Method will validate the Feature Configuration Option Type and Retrieve its values
	*  If no Option Type then raise an error
	*/
    	 
	public String retrieveAndvalidateFeatureConfigOptions(XapeOptTypFlgLookup optionTypeFlgLookup){

	FeatureConfiguration featConfig = getParameters().getFeatureConfigForApExtract();    
	FeatureConfigurationOptionsRetriever featureConfigOptRetriever = FeatureConfigurationOptionsRetriever.Factory.newInstance();
	List<FeatureConfigurationOptionData> featureConfigOptionData = 
	featureConfigOptRetriever.getFeatureConfigurationOptions(featConfig,optionTypeFlgLookup.getLookupValue().fetchIdFieldValue(),ExternalSystemTypeLookup.constants.EBS_AP_REQUEST_EXTRACT);

	if(featureConfigOptionData.size()==0){
		addError(CmMessageRepository.missFeatureOpt(optionTypeFlgLookup.getLookupValue().fetchLanguageDescription(),featConfig.getId().getIdValue()));    
	}  
	return featureConfigOptionData.get(0).getOptionValue().trim();
	}
		
	/**
	 * This method will create the file and write its corresponding header
	 * @param fileName - Formatted File Name
	 * @param filePath -File Path
	 * @param headerString - Header record
	 * @return BufferedWriter - Buffered Writer for the file
	 */
	private BufferedWriter cmCreateFileAndWriteHeader(String fileName, String fullFilePath, String headerString) {
			
	BufferedWriter bufferedWriter = cmInitializeFile(fileName, filePath);
	cmWriteRecord(headerString, bufferedWriter, filePath);
	return bufferedWriter;
	}						
									
	/**
	 * This method will initialize the file
	 * @param fileName - File Name
	 * @param filePath - File Path
	 * @return BufferedWriter - Buffered Writer for the file
	 */
	private BufferedWriter cmInitializeFile(String fileName, String filePath) {
			
	BufferedWriter bufferedWriter = null;
	File extractFile = new File(filePath, fileName);
	
	// Check if the file already exists
	if(extractFile.exists()) {	
		addError(CmMessageRepository.fileAlreadyExists(fileName, filePath));
	}

	// Initialize the file
	try {
		bufferedWriter = new BufferedWriter(new FileWriter(extractFile));					
	} catch (Exception e) {	
		addError(CmMessageRepository.commonMessageIoFileError(OPENING_FILE,fileName,e.toString()));
	}
	
	return bufferedWriter;
		}
		
	/**
	 * This Method will update AP Check Request Payment Status Flag to requested for payment
	 **/		
	private void cmUpdateApCheckReqFlag(String apReqid)
	{
	    try{
			ApCheckRequest apreq = new ApCheckRequest_Id(apReqid).getEntity();
			ApCheckRequest_DTO apaydto =  apreq.getDTO();
			apaydto.setPaymentSelectionStatus(PaymentSelectionStatusLookup.constants.REQUESTED_FOR_PAYMENT);
			apreq.setDTO(apaydto);
		}
		catch(Exception e){
			addError(CmMessageRepository.errorUpdatingStatus(apReqid,e.toString()));
		}
	}	
	
	/**
	 * This Method will Fetch the ApRequest Records
	 * @return sqlResultList
	 */
    private  List<SQLResultRow> fetchApRequests(){
	       
	StringBuilder queryBuilder = new StringBuilder();
	if(notNull(getParameters().getAccountRelationshipTypeList()))
	{
	 list = accountRelationshipTypeList.split(DELIMITER);
	}
	queryBuilder.append("SELECT ");
	queryBuilder.append("PID.PER_ID_NBR  CUSTOMER_NUM , ");
	queryBuilder.append("PNM.ENTITY_NAME CUSTOMER_NAME , ");
	queryBuilder.append("APR.ADDRESS1  ADDRESS1 , ");
	queryBuilder.append("APR.ADDRESS2  ADDRESS2 , ");
	queryBuilder.append("APR.ADDRESS3  ADDRESS3 , ");
	queryBuilder.append("APR.ADDRESS4  ADDRESS4 , ");
	queryBuilder.append("APR.CITY  CITY , ");
	queryBuilder.append("APR.STATE  STATE , ");
	queryBuilder.append("APR.COUNTRY  COUNTRY , ");
	queryBuilder.append("APR.POSTAL  ZIP , ");
	queryBuilder.append("CA.ADJ_ID || '-' || APR.AP_REQ_ID  INVOICE_NUM , ");
	queryBuilder.append("CA.CRE_DT  INVOICE_DATE , ");
	queryBuilder.append("CA.ADJ_AMT  INVOICE_AMT , ");
	queryBuilder.append("CA.CURRENCY_CD  INVOICE_CURRENCY_CODE , ");
	queryBuilder.append("DCE.GL_ACCT  DIST_CODE_CONCATENATED , ");
	queryBuilder.append("CA.ADJ_ID  RECEIPT_NUMBER , ");
	queryBuilder.append("APR.AP_REQ_ID  AP_REQUEST_ID , ");
	queryBuilder.append("PER.OVRD_MAIL_NAME1  OVERRIDE_MAILNAME1 , ");
	queryBuilder.append("PER.OVRD_MAIL_NAME2 OVERRIDE_MAILNAME2 , ");
	queryBuilder.append("PER.OVRD_MAIL_NAME3 OVERRIDE_MAILNAME3 , ");
	queryBuilder.append("PNM.ENTITY_NAME ENTITY_NAME  ");
	queryBuilder.append("FROM ");
	queryBuilder.append("CI_ADJ_APREQ APR , ");
	queryBuilder.append("CI_ADJ CA , ");
	queryBuilder.append("CI_SA SA , ");
	queryBuilder.append("CI_ACCT_PER AP  , ");
	queryBuilder.append("CI_PER_NAME PNM , ");
	queryBuilder.append("CI_PER PER , ");
	queryBuilder.append("CI_PER_ID PID , ");
	queryBuilder.append("CI_ADJ_TYPE ATP , ");
	queryBuilder.append("CI_DST_CODE_EFF DCE  ");
	queryBuilder.append("WHERE ");
	queryBuilder.append("APR.ADJ_ID = CA.ADJ_ID  AND ");
	queryBuilder.append("CA.SA_ID =SA.SA_ID AND ");
	queryBuilder.append("SA.ACCT_ID= AP.ACCT_ID AND ");
	queryBuilder.append("AP.PER_ID =PNM.PER_ID AND ");
	queryBuilder.append("PNM.PER_ID = PER.PER_ID AND ");
	queryBuilder.append("PNM.PER_ID=PID.PER_ID AND ");
	queryBuilder.append("PER.PER_ID=PID.PER_ID AND ");
	queryBuilder.append("ATP.ADJ_TYPE_CD=CA.ADJ_TYPE_CD AND ");
	queryBuilder.append("DCE.DST_ID=ATP.DST_ID AND ");
	queryBuilder.append("PID.PER_ID=AP.PER_ID AND ");
	queryBuilder.append("PID.ID_TYPE_CD=:customerIdentifierType AND ");
	queryBuilder.append("APR.BATCH_CD=:batchCode AND ");
	queryBuilder.append("APR.BATCH_NBR=:batchNumber AND ");
	queryBuilder.append("APR.PYMNT_SEL_STAT_FLG=:paymentSelectionStatus AND ");
	queryBuilder.append("CA.ADJ_STATUS_FLG =:adjustmentStatus AND ");
	queryBuilder.append("AP.MAIN_CUST_SW =:mainCustomer AND ");
	queryBuilder.append("PNM.PRIM_NAME_SW=:primaryName ");
		    
	//Start Add - CB-127
	if (!isBlankOrNull(accountRelationshipTypeList))
	{
		 queryBuilder.append(" AND AP.ACCT_REL_TYPE_CD IN (" );
		 for(int i=0; i<list.length; i++){
			if(i==0){
				queryBuilder.append(":accountRelationshipType"+i);
			}else{
				queryBuilder.append(", :accountRelationshipType"+i);
			}
		}
		 queryBuilder.append(")");
	}
    //End Add - CB-127
	
	PreparedStatement ps =createPreparedStatement(queryBuilder.toString(), EMPTY_STRING);
	ps.bindId("batchCode", getBatchControlId());
	ps.bindBigInteger("batchNumber", getBatchNumber());
	//Start Add - CB-127
	if (!isBlankOrNull(accountRelationshipTypeList))
	{
	    for(int i=0; i<list.length; i++)
		{
		   ps.bindId("accountRelationshipType"+i, new AccountRelationshipType_Id(list[i]));
	    }
	}
	//End Add - CB-127
	
	ps.bindEntity("customerIdentifierType", getParameters().getCustomerIdentifierType().getId().getEntity());
	ps.bindLookup("adjustmentStatus", AdjustmentStatusLookup.constants.FROZEN);
	ps.bindLookup("paymentSelectionStatus", PaymentSelectionStatusLookup.constants.NOT_SELECTEDFOR_PAYMENT);
	ps.bindBoolean("mainCustomer",Bool.TRUE );
	ps.bindBoolean("primaryName", Bool.TRUE);
	ps.execute();
	List<SQLResultRow> sqlResultList = ps.list();
	ps.close();
	ps.setAutoclose(false);
	return sqlResultList;       
    }					
				
	/**
	 * This method will write the record line to the output file
	 * @param recordLine - Record Line to be written to the output file
	 * @param bufferedWriter - Buffered Writer for the output file
	 * @param filePath -  File Path
	 */
	private void cmWriteRecord(String recordLine, BufferedWriter bufferedWriter, String filePath) {
		
		try
		{
			bufferedWriter.write(recordLine);
			bufferedWriter.newLine();
		}
		catch(Exception e)
		{
			 addError(CmMessageRepository.commonMessageIoFileError(WRITING_FILE,fileName,e.toString()));
		}
		
	}						
		
	/**
	 * This method will close the file
	 * @param bufferedWriter - Buffered Writer of the file to be closed
	 * @param filePath -  File Path
	 */
	private void cmCloseFile(BufferedWriter bufferedWriter, String filePath) {
	
		// Close the extract file
		try 
		{
			bufferedWriter.flush();
			bufferedWriter.close();
		}
		catch(Exception e)
		{
			 addError(CmMessageRepository.commonMessageIoFileError(CLOSING_FILE,fileName,e.toString()));
		}
			
	}	
		
	}	
	    
}