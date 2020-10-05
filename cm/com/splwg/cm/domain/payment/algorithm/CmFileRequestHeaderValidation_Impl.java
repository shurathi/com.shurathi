/* 
 **************************************************************************
 *                Confidentiality Information:
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
 * This class is for File Request Header Validation.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-04-16   KGhuge     Initial.
 * 2020-09-21   SAnart     File Upload Dashboard Status for Invoice Conversion Request.
 **************************************************************************
 */
package com.splwg.cm.domain.payment.algorithm;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileTransformationRecord;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileValidationAlgorithmSpot;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;

/**
 * @author KGhuge
 *
@AlgorithmComponent ()
 */
public class CmFileRequestHeaderValidation_Impl extends CmFileRequestHeaderValidation_Gen implements FileValidationAlgorithmSpot {
	//Hard Parameters
	private String header;
	private BigInteger numberOfRecords;
	private String filePayload;
	private FileTransformationRecord fileHeader;
	private String requestType;
	private FileTransformationRecord mapFieldHeaderRecord;
	private String fileName;
	private String filePath;
	private String businessDate;
	private FileTransformationRecord firstRecord;
	private String firstRecordString;
	//Constants
	private static final Pattern FOOTER_NUMREC_REGEX = Pattern.compile("<numOfRecords>(.+?)</numOfRecords>",32);
	
	@Override
	public void invoke() {	
		int numberOfRec = numberOfRecords.intValue();
		//If number of records does not match, raise an error.
		if(Integer.parseInt(getNumberOfRecordsFromXmlHeader(header))!=numberOfRec)
		{
			addError(CmMessageRepository.numberOfRecordsDoesNotMatch(getNumberOfRecordsFromXmlHeader(header),String.valueOf(numberOfRec)));	
		}
	}

	/**
	*	This Method retrieves Number of records from header
	*	@Param header
	*	@return numberOfRecordsFromHeader
	*/
	private static String getNumberOfRecordsFromXmlHeader(String header) {
		String numberOfRecordsFromHeader = "0";
	    Matcher matcher = FOOTER_NUMREC_REGEX.matcher(header);
	    if (matcher.find())
	    {
	    	numberOfRecordsFromHeader = matcher.group(1).trim();
	    }	
	    return numberOfRecordsFromHeader;
	}
	
	@Override
	public String getDefaultValue1() {
		return null;
	}

	@Override
	public String getDefaultValue2() {
		return null;
	}

	@Override
	public String getDefaultValue3() {
		return null;
	}

	@Override
	public String getDefaultValue4() {
		return null;
	}

	@Override
	public String getDefaultValue5() {
		return null;
	}

	@Override
	public String getFileBusinessDate() {
		return this.businessDate;
	}

	@Override
	public String getFileName() {
		return this.fileName;
	}

	@Override
	public String getFilePath() {
		return this.filePath;
	}

	@Override
	public long getFileSizeInBytes() {
		return 0;
	}

	@Override
	public FileTransformationRecord getFirstRecord() {
		return this.firstRecord;
	}

	@Override
	public String getFirstRecordString() {
		return this.firstRecordString;
	}

	@Override
	public FileTransformationRecord getFooter() {
		return null;
	}

	@Override
	public FileTransformationRecord getHeader() {
		return fileHeader;
	}

	@Override
	public FileTransformationRecord getMapFieldFooterRecord() {
		return null;
	}

	@Override
	public FileTransformationRecord getMapFieldHeaderRecord() {
		return this.mapFieldHeaderRecord;	
	}

	@Override
	public String getMessageCategory() {
		return "0";		
	}

	@Override
	public String getMessageNumber() {
		return "0";
	}

	@Override
	public String getMessageParam2() {
		return null;
	}

	@Override
	public String getMessageParam3() {
		return null;
	}

	@Override
	public String getMessageParam4() {
		return null;
	}

	@Override
	public String getMessageParam5() {
		return null;
	}

	@Override
	public int getNumOfFilesAtGivenFilePath() {
		return 0;
	}

	@Override
	public String getRefFieldName() {
		return null;
	}

	@Override
	public String getRefId() {
		return null;
	}

	@Override
	public String getRefXPath() {
		return null;
	}

	@Override
	public String getRequestType() {
		return this.requestType;
	}

	@Override
	public String getStatus() {
		return null;
	}

	@Override
	public boolean isValidateFlag() {
		
		//CB-391-ADD-START
		return true;
		//CB-391-ADD-END
	}

	@Override
	public void setChecksum(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDefaultValue1(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDefaultValue2(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDefaultValue3(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDefaultValue4(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDefaultValue5(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFileBusinessDate(String businessDate) {
		this.businessDate = businessDate;
	}

	@Override
	public void setFileName(String fileName) {
     this.fileName = fileName;
	}

	@Override
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public void setFilePayload(String filePayload) {
		this.filePayload = filePayload;
	}

	@Override
	public void setFileSizeInBytes(long arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFirstRecord(FileTransformationRecord arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFirstRecordString(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFooter(FileTransformationRecord arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setHeader(FileTransformationRecord header) {
		this.fileHeader = header;
	}

	@Override
	public void setMapFieldFooterRecord(FileTransformationRecord arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMapFieldHeaderRecord(FileTransformationRecord mapFieldHeaderRecord) {
		 this.mapFieldHeaderRecord = mapFieldHeaderRecord;
	}

	@Override
	public void setMessageCategory(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMessageNumber(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMessageParam2(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMessageParam3(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMessageParam4(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMessageParam5(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setNumOfFilesAtGivenFilePath(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setNumOfRecords(BigInteger numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	@Override
	public void setRefFieldName(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setRefId(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setRefXPath(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	@Override
	public void setStatus(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setStringFooter(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setStringHeader(String header) {
		this.header = header;
	}

	@Override
	public void setValidateFlag(boolean arg0) {
		// TODO Auto-generated method stub
	}
}
