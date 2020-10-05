package com.splwg.cm.domain.billing.batch;

import java.io.File;
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
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.splwg.base.api.batch.CommitEveryUnitStrategy;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.businessService.BusinessServiceInstance;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessService.BusinessService_Id;
import com.splwg.cm.domain.billing.service.CmApplyStmtExtrTemplateCondoSafeService;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.environ.ApplicationProperties;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author IshitaGarg
 *
@BatchJob (modules = { "demo"},
 *      softParameters = { @BatchJobSoftParameter (name = extractSourceFilePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = processedExtractFilePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = outputFilePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = templateFilePath, required = true, type = string)
 *            , @BatchJobSoftParameter (name = statementExtractFileName, required = true, type = string)
 *            , @BatchJobSoftParameter (entityName = businessObject, name = extenableLookupBusinessObject, required = true, type = entity)
 *            , @BatchJobSoftParameter (name = customerClass, required = true, type = string)})
 */
public class CmStatementExtractPrintingBatch extends
		CmStatementExtractPrintingBatch_Gen {
	private static Logger logger = LoggerFactory.getLogger(CmStatementExtractPrintingBatch.class);
	static String bussinessService=null;
	public static final String SHARED_VARIABLE="@SHARED_DIR";
	public static final String INSTALLED_VARIABLE="@INSTALL_DIR";
	public static final String SHARED_DIRECTORY = ApplicationProperties
			.getNullableProperty("com.oracle.ouaf.fileupload.shared.directory");
	public static final String INSTALLED_DIRECTORY = System.getenv("SPLEBASE");

	public JobWork getJobWork() {
		File directoryPath = new File(getReportingDirPath(getParameters().getExtractSourceFilePath()));
		//List of all files and directories
	      File filesList[] = directoryPath.listFiles();
	      List<ThreadWorkUnit> threadWorkUnits = new ArrayList<ThreadWorkUnit>();
			ThreadWorkUnit newUnit = null;
			
	      for(File file : filesList)
	      {
	    	  if(file.getName().contains(getParameters().getStatementExtractFileName()))
	    	  {
	    		  newUnit = new ThreadWorkUnit();
	    		  newUnit.addSupplementalData(1, file.getName());
	    		  threadWorkUnits.add(newUnit);
	    	  }
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
		String templateFilePath=param.getTemplateFilePath();
		extractFilePath=getReportingDirPath(extractFilePath);
		processedFilePath=getReportingDirPath(processedFilePath);
		outputFilePath=getReportingDirPath(outputFilePath);
		templateFilePath=getReportingDirPath(templateFilePath);
		if (!new File(extractFilePath).exists())
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DIRECTORY_OR_FILE_NOT_EXIST,extractFilePath));
		if (!new File(processedFilePath).exists())
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DIRECTORY_OR_FILE_NOT_EXIST,processedFilePath));
		if (!new File(outputFilePath).exists())
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DIRECTORY_OR_FILE_NOT_EXIST,outputFilePath));
		if (!new File(templateFilePath).exists())
			addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_DIRECTORY_OR_FILE_NOT_EXIST,templateFilePath));

	}
	public static String getReportingDirPath(String paramString)
	{
		if (paramString.startsWith(SHARED_VARIABLE))
		{
			paramString = paramString.substring(SHARED_VARIABLE.length()+1, paramString.length());
			paramString = SHARED_DIRECTORY + File.separator + paramString;
		}
		else if (paramString.startsWith(INSTALLED_VARIABLE)
				)
		{
			paramString = paramString.substring(INSTALLED_VARIABLE.length()+1, paramString.length());
			paramString = INSTALLED_DIRECTORY + File.separator + paramString;
		}
		return paramString;
	}
	public Class<CmStatementExtractPrintingBatchWorker> getThreadWorkerClass() {
		return CmStatementExtractPrintingBatchWorker.class;
	}

	public static class CmStatementExtractPrintingBatchWorker extends
			CmStatementExtractPrintingBatchWorker_Gen {

		public ThreadExecutionStrategy createExecutionStrategy() {
			// TODO Auto-generated method stub
			return new CommitEveryUnitStrategy(this);
		}

		public boolean executeWorkUnit(ThreadWorkUnit unit)
				throws ThreadAbortedException, RunAbortedException {
			final String XML_TAG="<?xml version=\"1.0\" encoding=\"utf-8\"?>";
			String fileName=(String) unit.getSupplementallData(1);
			
			String extractFilePath = getReportingDirPath(getParameters().getExtractSourceFilePath());
			File file = new File(extractFilePath+File.separator+fileName);  
			StringBuilder data=new StringBuilder();
			try {
				Scanner myReader=null;
				SAXReader reader = new SAXReader();

				myReader = new Scanner(file);
				while (myReader.hasNextLine()) {
					data = data.append(myReader.nextLine());
				}
				  Document extractFile = null;
				try {
					extractFile = reader.read( new StringReader(XML_TAG+data.toString()));
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Element extractXML = extractFile.getRootElement();
				
				Node templateNodeExtractFile = extractXML.selectSingleNode("template").selectSingleNode("templateCode");
				String templateFilePath =getReportingDirPath(getParameters().getTemplateFilePath())+File.separator+templateNodeExtractFile.getText();
				File templateFile;
				if(templateFilePath.contains("."))
				{
					 templateFile = new File(templateFilePath);
				}
				else
				{
					templateFile = new File(templateFilePath+".xlsx");
				}
				BusinessObject extendableLookupBO=getParameters().getExtenableLookupBusinessObject();
				String lookupValue=getParameters().getCustomerClass();
				BusinessObjectInstance boInstance=fetchExtendableLookup(lookupValue,extendableLookupBO);
				Document templateDoc=boInstance.getDocument();
				boInstance.getDocument().asXML();
				List<Node> templateList=templateDoc.selectNodes("CM-TemplateBusSvcMapExtLookup/templateList");
				for(Node node:templateList)
					if(node.selectSingleNode("template").getText().trim().equals(templateNodeExtractFile.getText().trim()))
						bussinessService=node.selectSingleNode("businessService").getText().toString();
				
				if(!isBlankOrNull(bussinessService)){
					logger.info("Calling Business Service");
					BusinessServiceInstance bsInstance=BusinessServiceInstance.create(new BusinessService_Id(bussinessService).getEntity());
					bsInstance.set("extractSourceFilePath", extractFilePath+File.separator+fileName);
					bsInstance.set("outputFilePath",getReportingDirPath(getParameters().getOutputFilePath()));
					bsInstance.set("processedExtractFilePath",getReportingDirPath(getParameters().getProcessedExtractFilePath()));
					bsInstance.set("templateFile",templateFile.getAbsolutePath());
					BusinessServiceDispatcher.execute(bsInstance);
					
					 String processedFilePathStr=getReportingDirPath(getParameters().getProcessedExtractFilePath())+File.separator+file.getName();
			         Path temp = null;
			         myReader.close();
					if(file.exists())
					{
			        	  temp= Files.move(Paths.get(file.getAbsolutePath()), Paths.get(processedFilePathStr),StandardCopyOption.ATOMIC_MOVE);
					}
					if(temp!=null){
						logger.info("----------------------------Moved to processed file location");
						}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		

	}

}
