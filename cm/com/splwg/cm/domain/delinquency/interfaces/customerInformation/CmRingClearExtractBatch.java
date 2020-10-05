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
 * Ring Clear Extract 
 * 
 * This batch program will extract RingClear Notification Outbound Message 
 * records for a given run number and compile them into a single CSV file.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-08-26   JFerna     CB-267. Initial	
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.interfaces.customerInformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.splwg.base.api.Query;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.batch.WorkUnitResult;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.domain.outboundMessage.outboundMessage.OutboundMessage;
import com.splwg.base.domain.outboundMessage.outboundMessage.OutboundMessage_Id;
import com.splwg.ccb.domain.dataManagement.fileRequest.FileRequestMessageRepository;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileRequestConstants;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;

/**
 * @author JFerna
 *
@BatchJob (modules={}, multiThreaded = true, rerunnable = true,
 *		softParameters = { @BatchJobSoftParameter (name = filePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = fileName, required = true, type = string)
 *            , @BatchJobSoftParameter (name = MAX_ERRORS, type = integer)
 *            , @BatchJobSoftParameter (name = DIST-THD-POOL, type = string)})
 */
public class CmRingClearExtractBatch extends CmRingClearExtractBatch_Gen {
	
	//Constants
	private static final String EMPTY_STRING = "";
	private static final String PERIOD = ".";
	private static final String FILE_PATH_PARM_NAME = "filePath";
	private static final String CSV = ".csv";
	private static final String RINGCLR_EXTRACT="Delinquency Management extract";
	private static final String OPENING_FILE = "opening";
	private static final String WRITING_FILE = "writing";
	private static final String CLOSING_FILE = "closing";
    private static final String INSTALL_DIR = "@INSTALL_DIR";
    private static final String SHARED_DIR  = "@SHARED_DIR";
    private static final String DATE_FORMAT_MM_DD_YY ="MM-dd-yy";
    private static final String SPACE =" ";

	/**
	 * Implemented method that retrieves the records to be processed.
	 * @return Job Work object containing record(s) to process
	 */
	public JobWork getJobWork() {
		Query<OutboundMessage_Id> retrieveOutboundMessageQry = createQuery("FROM OutboundMessage outMsg" +
				" WHERE outMsg.batchControlId = :batchCode" +
				" AND outMsg.batchNumber = :batchNumber",
				"");
		
		retrieveOutboundMessageQry.bindId("batchCode", this.getBatchControlId());
		retrieveOutboundMessageQry.bindBigInteger("batchNumber", this.getBatchNumber());
		
		retrieveOutboundMessageQry.addResult("outboundMessage", "outMsg.id");
		return createJobWorkForEntityIdQuery(retrieveOutboundMessageQry);
	}
	
	/**
	 * Performs initial batch parameter validations.
	 * @param isNewRun parameter validation switch
	 */
	public void validateSoftParameters(boolean isNewRun) {		
		// Retrieve parameters to validate
		String filePath = this.getParameters().getFilePath();
		String fileName = this.getParameters().getFileName().trim();
		
    	if(!filePath.startsWith(INSTALL_DIR) && !filePath.startsWith(SHARED_DIR)){
    		addError(FileRequestMessageRepository.filePathNeitherSharedNorInstalled());
    	}	
    	
    	if(filePath.startsWith(INSTALL_DIR)){
    		filePath = filePath.substring(INSTALL_DIR.length(), filePath.length());
    		filePath = FileRequestConstants.INSTALLED_DIRECTORY + File.separator + filePath;
    	}
    	
    	if(filePath.startsWith(SHARED_DIR)){
    		filePath = filePath.substring(SHARED_DIR.length(), filePath.length());
    		filePath = FileRequestConstants.SHARED_DIRECTORY + File.separator + filePath;
    	}
		
		// Validate if File Path exists
		File folder = new File(filePath);		
		if(!folder.exists()) {
			addError(CmMessageRepository.invalidPath(this.getBatchControlId().getEntity().getParameterByName(FILE_PATH_PARM_NAME).fetchLanguageDescription(),filePath.trim()));
		}
			
		// Validate if File Path is a valid directory
		if(!folder.isDirectory()) {
			addError(CmMessageRepository.invalidDirectory(this.getBatchControlId().getEntity().getParameterByName(FILE_PATH_PARM_NAME).fetchLanguageDescription(),filePath.trim()));
		}
		
		//Validate File Name Format 
		if(fileName.lastIndexOf(PERIOD) > 0 && !fileName.endsWith(CSV)){
			addError(CmMessageRepository.invalidFileFormat(RINGCLR_EXTRACT,CSV));
		}		
	}

	/**
	 * Returns the batch program's inner Thread Worker class.
	 * @return Batch Worker class
	 */
	public Class<CmRingClearExtractBatchWorker> getThreadWorkerClass() {
		return CmRingClearExtractBatchWorker.class;
	}

	/**
	 * RingClear Extract Batch Worker Class
	 */
	public static class CmRingClearExtractBatchWorker extends
			CmRingClearExtractBatchWorker_Gen {		
		//Batch Parameters
		private String filePath = EMPTY_STRING;
		private String fileName = EMPTY_STRING;
		
		//Work Variables
		private BufferedWriter ringClearBufferedWriter;
		private DateFormat dateFormatMMDDYY = new DateFormat(DATE_FORMAT_MM_DD_YY);

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
			
			//Retrieve File Path and File Name
			filePath = this.getParameters().getFilePath();
			fileName = this.getParameters().getFileName().trim();
			
			//Format File Path
	    	if(filePath.startsWith(INSTALL_DIR)){
	    		filePath = filePath.substring(INSTALL_DIR.length(), filePath.length());
	    		filePath = FileRequestConstants.INSTALLED_DIRECTORY + File.separator + filePath;
	    	}
	    	
	    	if(filePath.startsWith(SHARED_DIR)){
	    		filePath = filePath.substring(SHARED_DIR.length(), filePath.length());
	    		filePath = FileRequestConstants.SHARED_DIRECTORY + File.separator + filePath;
	    	}
	    	
			String formattedFilePath = filePath;
			if(!filePath.endsWith(File.separator)) {
				formattedFilePath = filePath + File.separator;
			}
						
			//Format File Name
			String fileDateStamp = this.getProcessDateTime().getDate().toString(dateFormatMMDDYY);
			if(fileName.endsWith(CSV)){
				fileName = fileName.replace(CSV, SPACE + fileDateStamp + CSV);
			}else{
				fileName = fileName + SPACE + fileDateStamp + CSV;
			}
			
			formattedFilePath = formattedFilePath + fileName;
			
			//Create File and Write Header records
			CmRingClearExtractHeaderRecord headerRecord = new CmRingClearExtractHeaderRecord();
			String headerString = headerRecord.cmCreateRecordString();
			
			ringClearBufferedWriter = cmCreateFileAndWriteHeader(fileName, formattedFilePath, headerString);
			
		}

		/**
		 * This method will create the file and write its corresponding header
		 * @param formattedFileName - Formatted File Name
		 * @param fullFilePath - Full File Path
		 * @param headerString - Header record
		 * @return BufferedWriter - Buffered Writer for the file
		 */
		private BufferedWriter cmCreateFileAndWriteHeader(String formattedFileName, String fullFilePath, String headerString) {
			
			BufferedWriter bufferedWriter = cmInitializeFile(formattedFileName, fullFilePath);
			
			cmWriteRecord(headerString, bufferedWriter, fullFilePath);
			
			return bufferedWriter;
		}
		
		/**
		 * This method will initialize the file
		 * @param fileName - File Name
		 * @param fullFilePath - Full File Path
		 * @return BufferedWriter - Buffered Writer for the file
		 */
		private BufferedWriter cmInitializeFile(String fileName, String fullFilePath) {
			
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
				addError(CmMessageRepository.commonMessageIoFileError(OPENING_FILE,fullFilePath,e.toString()));
			}
			
			return bufferedWriter;
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
			
			// Initialize variables
			int recordsProcessed = 0;
			int recordsInError = 0;
			
			OutboundMessage_Id outMsgId = (OutboundMessage_Id) unit.getPrimaryId();
			OutboundMessage outMsg = outMsgId.getEntity();
			
			try{
				recordsProcessed++;
				
				//Convert XML Source to Document
				Document doc = DocumentHelper.parseText(outMsg.getXmlSource());
				Node node = doc.getRootElement();
				
				String customerNbr = node.getDocument().getRootElement().element("customerNbr").getText();
				String phoneNbr = node.getDocument().getRootElement().element("phoneNbr").getText();
				String customerName = node.getDocument().getRootElement().element("customerName").getText();
				
				CmRingClearExtractDetailRecord ringClearRecord = new CmRingClearExtractDetailRecord();
				ringClearRecord.setCustomerNumber(customerNbr);
				ringClearRecord.setPhoneNumber(phoneNbr);
				ringClearRecord.setCustomerName(customerName);
				
				cmWriteRecord(ringClearRecord.cmCreateRecordString(), ringClearBufferedWriter, filePath);
				
			}catch (DocumentException e) {
				recordsInError++;
			}catch (Exception e) {
				recordsInError++;
			}
			
			return constructWorkUnitResult(recordsProcessed, recordsInError);
		}
		
		/**
		 * This method will construct the Work Unit Result
		 * @param recordsProcessed - Records processed
		 * @param recordsInError - Records in error
		 * @return WorkUnitResult - Work Unit Result
		 */
		private WorkUnitResult constructWorkUnitResult(int recordsProcessed, int recordsInError) {
			
			WorkUnitResult workUnitResult = new WorkUnitResult(true);
			workUnitResult.setUnitsProcessed(recordsProcessed);
			workUnitResult.setUnitsInError(recordsInError);
			
			return workUnitResult;
		}
		
		/**
		 * This method will write the record line to the output file
		 * @param recordLine - Record Line to be written to the output file
		 * @param bufferedWriter - Buffered Writer for the output file
		 * @param fullFilePath - Full File Path
		 */
		private void cmWriteRecord(String recordLine, BufferedWriter bufferedWriter, String fullFilePath) {			
			try {
				bufferedWriter.write(recordLine);
				bufferedWriter.newLine();
			} catch(Exception e) {
				addError(CmMessageRepository.commonMessageIoFileError(WRITING_FILE,fileName,e.toString()));
			}			
		}
		
		/**
		 * This method will close the file
		 * @param bufferedWriter - Buffered Writer of the file to be closed
		 * @param fullFilePath - Full File Path
		 */
		private void cmCloseFile(BufferedWriter bufferedWriter, String fullFilePath) {		
			try {
				bufferedWriter.close();
			}catch(Exception e){
				logError(CmMessageRepository.commonMessageIoFileError(CLOSING_FILE,fileName,e.toString()));
			}			
		}
		
		/**
		 * Implemented method that executes work to be performed at the end of the thread run,
		 * after all Work Units for the current thread have been processed.
		 * @throws ThreadAbortedException Thread Aborted Exception
		 * @throws RunAbortedException Run Aborted Exception
		 */
		public void finalizeThreadWork() throws ThreadAbortedException,
				RunAbortedException {
			cmCloseFile(ringClearBufferedWriter, filePath);			
		}
	}
}
