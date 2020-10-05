/*
 **************************************************************************
 *                    Confidentiality Information:
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
 * This batch will process all invoice extract files stored in the source 
 * directory and convert to its appropriate template. The batch will invoke 
 * the Business Service associated to the extract files template by 
 * retrieving it from the Extendable Lookup in the batch parameter.
 **************************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:        			Reason:
 * YYYY-MM-DD   IN         			Reason text.
 * 2020-07-24   ShwethaPatil     	Initial version.                                             
 **************************************************************************
 */

package com.splwg.cm.domain.billing.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessService.BusinessService_Id;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.common.ApplicationException;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author ShwethaPatil
 *
@BatchJob (modules = { "demo"},
 *      softParameters = { @BatchJobSoftParameter (name = extractSourceFilePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = processedExtractFilePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = outputFilePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = templateFilePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = invoiceExtractFileName, required = true, type = string)
 *            , @BatchJobSoftParameter (entityName = businessObject, name = extenableLookupBusinessObject, required = true, type = entity)
 *            , @BatchJobSoftParameter (name = extenableLookupValue, required = true, type = string)})
 */
public class CmInvoiceExtractTemplateBatch extends
CmInvoiceExtractTemplateBatch_Gen {
	static Logger logger = LoggerFactory.getLogger(CmInvoiceExtractTemplateBatch.class);
	CmInvoiceExtractTemplateHelper helper=new CmInvoiceExtractTemplateHelper.Factory().newInstance();
	@Override
	public JobWork getJobWork() {
		logger.debug("-------------------Inside GetJobWork-------------------------------------");
		Scanner myReader=null;
		StringBuilder data=new StringBuilder();
		String extractFilePath=helper.getReportingDirPath(getParameters().getExtractSourceFilePath()) ;
		String fileName=getParameters().getInvoiceExtractFileName();
		List<ThreadWorkUnit> threadWorkUnits = new ArrayList<ThreadWorkUnit>();
		ThreadWorkUnit newUnit = null;
		try {
			if(!fileName.contains(helper.XML))
				fileName=fileName+helper.XML;

			File inputFile = new File(extractFilePath+File.separator+fileName);

			SAXReader reader = new SAXReader();

			myReader = new Scanner(inputFile);
			while (myReader.hasNextLine()) {
				data = data.append(myReader.nextLine());
			}

			Document document = reader.read( new StringReader(helper.XML_TAG+helper.BILL_START_TAG+data.toString()+helper.BILL_END_TAG));
			if(notNull(document)){
				List<Node> nodes = document.selectNodes("billDetails/bill");

				for (Node node : nodes) {
					newUnit = new ThreadWorkUnit();
					String billDetails=node.asXML();
					newUnit.addSupplementalData(1, billDetails);
					threadWorkUnits.add(newUnit);
					
				}
			}
		}
		catch (DocumentException e) {
			logger.error("---------DocumentException--------------"+e);

		}catch(ApplicationError e){
			logger.error("----------ApplicationError-------------"+e);
			throw e;
		} catch (FileNotFoundException e) {
			logger.error("---------FileNotFoundException--------------"+e);
		}
		finally{
			if(myReader!=null)
				myReader.close();
		}
		return createJobWorkForThreadWorkUnitList(threadWorkUnits);
	}

	@Override
	public void validateSoftParameters(boolean isNewRun) {
		super.validateSoftParameters(isNewRun);
		JobParameters param = getParameters();
		String extractFilePath=param.getExtractSourceFilePath();
		String processedFilePath=param.getProcessedExtractFilePath();
		String outputFilePath=param.getOutputFilePath();
		String fileName=param.getInvoiceExtractFileName();
		String templateFilePath=param.getTemplateFilePath();
		extractFilePath=helper.getReportingDirPath(extractFilePath);
		processedFilePath=helper.getReportingDirPath(processedFilePath);
		outputFilePath=helper.getReportingDirPath(outputFilePath);
		templateFilePath=helper.getReportingDirPath(templateFilePath);
		if(!fileName.contains(helper.XML))
			fileName=fileName+helper.XML;
		if (!new File(extractFilePath+File.separator+fileName).exists())
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_FILE_NAME,fileName,extractFilePath));
		if (!new File(processedFilePath).exists())
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DIRECTORY_OR_FILE_NOT_EXIST,processedFilePath));
		if (!new File(outputFilePath).exists())
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DIRECTORY_OR_FILE_NOT_EXIST,outputFilePath));
		if (!new File(templateFilePath).exists())
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DIRECTORY_OR_FILE_NOT_EXIST,templateFilePath));

	}

	public Class<CmInvoiceExtractTemplateBatchWorker> getThreadWorkerClass() {
		return CmInvoiceExtractTemplateBatchWorker.class;
	}

	public static class CmInvoiceExtractTemplateBatchWorker extends
	CmInvoiceExtractTemplateBatchWorker_Gen {
		String bussinessServicePDF=null;
		String bussinessServiceXL=null;
		String extractFilePath=null;
		String processedFilePath=null;
		String outputFilePath=null;
		String templateFilePath=null;
		String fileName=null;
		Bool output=Bool.FALSE;
		CmInvoiceExtractTemplateHelper helper=null;

		public ThreadExecutionStrategy createExecutionStrategy() {
			// TODO Auto-generated method stub
			return new StandardCommitStrategy(this);
		}



		@Override
		public void initializeThreadWork(
				boolean initializationPreviouslySuccessful)
						throws ThreadAbortedException, RunAbortedException {
			logger.info("-------------------Inside initializeThreadWork-------------------------------------");

			helper=new CmInvoiceExtractTemplateHelper.Factory().newInstance();

			extractFilePath=helper.getReportingDirPath(getParameters().getExtractSourceFilePath());
			processedFilePath=helper.getReportingDirPath(getParameters().getProcessedExtractFilePath());
			outputFilePath=helper.getReportingDirPath(getParameters().getOutputFilePath());
			templateFilePath=helper.getReportingDirPath(getParameters().getTemplateFilePath());

			fileName=getParameters().getInvoiceExtractFileName();
			if(!fileName.contains(helper.XML))
				fileName=fileName+helper.XML;
			BusinessObject extendableLookupBO=getParameters().getExtenableLookupBusinessObject();
			String lookupValue=getParameters().getExtenableLookupValue();
			BusinessObjectInstance boInstance=fetchExtendableLookup(lookupValue,extendableLookupBO);
			System.out.println();
			Document templateDoc=boInstance.getDocument();
			boInstance.getDocument().asXML();
			List<Node> templateList=templateDoc.selectNodes("CM-TemplateBusSvcMapExtLookup/templateList");

			for(Node node:templateList)
				if(node.selectSingleNode("template").getText().trim().equals(helper.TEMPLATE_CONDO_SAFE_PDF))
					bussinessServicePDF=node.selectSingleNode("businessService").getText().toString();
				else if (node.selectSingleNode("template").getText().trim().equals(helper.TEMPLATE_CONDO_SAFE_XLSX))
					bussinessServiceXL=node.selectSingleNode("businessService").getText().toString();
		}





		public boolean executeWorkUnit(ThreadWorkUnit unit)
				throws ThreadAbortedException, RunAbortedException {
			String billDetails=(String) unit.getSupplementallData(1);
			String inputXML=extractFilePath+File.separator+helper.INPUT_XML+helper.XML;
			SAXReader reader = new SAXReader();
			Document document=null;
			try{
				document = reader.read(new  StringReader(helper.XML_TAG+billDetails));
				List<Node> nodes = document.selectNodes("bill/templateDetail/templates");
				for (Node node : nodes) {
					String templatePath=null;
					String template=node.selectSingleNode("templateCode").getText().trim();
					if(template.equalsIgnoreCase(helper.TEMPLATE_CONDO_SAFE_PDF)){
						if(!templateFilePath.contains(helper.XSL))
							templatePath=templateFilePath+File.separator+helper.TEMPLATE_CONDO_SAFE_PDF+helper.XSL;
						callBusinessService( inputXML, billDetails,bussinessServicePDF,templatePath);

					}
					else if (template.equalsIgnoreCase(helper.TEMPLATE_CONDO_SAFE_XLSX)) {
						if(!templateFilePath.contains(helper.XLSX))
							templatePath=templateFilePath+File.separator+helper.TEMPLATE_CONDO_SAFE_XLSX+helper.XLSX;
						callBusinessService( inputXML, billDetails,bussinessServiceXL,templatePath);
					}

				}	
			}catch(ApplicationException e){
				logger.info("---------------------------e");
				throw e;
			} catch (DocumentException e) {
				logger.info("---------------------------e");
			}
			return true;
		}
		/**
		 * Fetch extendable lookup instance
		 * @param lookupValue
		 * @param extendableLookupBo
		 * @return
		 */
		private BusinessObjectInstance fetchExtendableLookup(String lookupValue, BusinessObject extendableLookupBo) {
			BusinessObjectInstance boInstance = BusinessObjectInstance.create(extendableLookupBo.getId().getTrimmedValue());
			boInstance.set("bo", extendableLookupBo.getId().getTrimmedValue());
			boInstance.set("lookupValue", lookupValue);
			boInstance = BusinessObjectDispatcher.read(boInstance);
			return boInstance;
		}
		/**
		 * This method invoke's Business Service
		 */
		private void callBusinessService(String inputXML,String billDetails,String bussinessService,String templateFilePath) {
			FileWriter writer;
			try {
				writer = new FileWriter(new File(inputXML));
				writer.write(helper.XML_TAG+billDetails); 
				writer.flush();
				writer.close();
				if(!isBlankOrNull(bussinessService)){
					BusinessServiceInstance bsInstance=BusinessServiceInstance.create(new BusinessService_Id(bussinessService).getEntity());
					bsInstance.set("outputFilePath", outputFilePath);
					bsInstance.set("inputXmlFile", inputXML);
					bsInstance.set("xslFileName", templateFilePath);
					BusinessServiceDispatcher.execute(bsInstance);
					output=Bool.TRUE;	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


		@Override
		public void finalizeJobWork() throws Exception {
			if(output.equals(Bool.TRUE)){
				String extractFilePathStr=extractFilePath+File.separator + fileName;
				String processedFilePathStr=processedFilePath+File.separator+fileName;

				if(new File(extractFilePathStr).exists()){
					Path temp= Files.move(Paths.get(extractFilePathStr), Paths.get(processedFilePathStr),StandardCopyOption.ATOMIC_MOVE);
					if(temp!=null){
						logger.info("----------------------------Moved to processed file location");
					}

				}
			}
		}

	}

}
