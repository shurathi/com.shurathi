package com.splwg.cm.domain.conversion.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.support.context.FrameworkSession;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.ccb.domain.dataManagement.fileRequest.FileRequestMessageRepository;
import com.splwg.ccb.domain.dataManagement.fileRequest.batch.FileRequestConstants;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.JSONException;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.JSONObject;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.XML;
import com.splwg.shared.common.ApplicationException;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;

/**
 * @author Lenovo
 *
@BatchJob (modules={}, 
 *			softParameters = { @BatchJobSoftParameter (name = targetFilePath, type = string)
 *            , @BatchJobSoftParameter (name = fileName, type = string)
 *            , @BatchJobSoftParameter (name = sourceFilePath, type = string)})
 */
public class CmTransformFileFormat extends CmTransformFileFormat_Gen {
	
	//Constants
	private static final String INSTALL_DIR = "@INSTALL_DIR";
	private static final String SHARED_DIR  = "@SHARED_DIR";
		
	public static final Logger logger = LoggerFactory.getLogger(CmTransformFileFormat.class);
	
	// Soft Parameter
    private String sourceFilePath;
    private String fileName;
    private static String targetFilePath;
    
	/**
     * Validate batch parameters
     */
    @Override
    public void validateSoftParameters(boolean isNewRun) {
    	CmTransformFileFormat_Gen.JobParameters jobParameters = getParameters();
    	sourceFilePath = jobParameters.getSourceFilePath().trim();
    	targetFilePath = jobParameters.getTargetFilePath().trim();
    	fileName	   = jobParameters.getFileName();
    	
    	logger.debug("Source file path->"+sourceFilePath);
    	logger.debug("Target file path->"+targetFilePath);
    	
    	
    	if(!sourceFilePath.startsWith(INSTALL_DIR) && !sourceFilePath.startsWith(SHARED_DIR)){
    		addError(FileRequestMessageRepository.filePathNeitherSharedNorInstalled());
    	}
    	
    	if(!targetFilePath.startsWith(INSTALL_DIR) && !targetFilePath.startsWith(SHARED_DIR)){
    		addError(FileRequestMessageRepository.filePathNeitherSharedNorInstalled());
    	}
    	
    	if(sourceFilePath.startsWith(INSTALL_DIR)){
    		sourceFilePath = sourceFilePath.substring(INSTALL_DIR.length(), sourceFilePath.length());
    		sourceFilePath = FileRequestConstants.INSTALLED_DIRECTORY + File.separator + sourceFilePath;
    	}
    	
    	if(targetFilePath.startsWith(INSTALL_DIR)){
    		targetFilePath = targetFilePath.substring(INSTALL_DIR.length(), targetFilePath.length());
    		targetFilePath = FileRequestConstants.INSTALLED_DIRECTORY + File.separator + targetFilePath;
    	}
    	
    	if(sourceFilePath.startsWith(SHARED_DIR)){
    		sourceFilePath = sourceFilePath.substring(SHARED_DIR.length(), sourceFilePath.length());
    		sourceFilePath = FileRequestConstants.SHARED_DIRECTORY + File.separator + sourceFilePath;
    	}
    	
    	if(targetFilePath.startsWith(SHARED_DIR)){
    		targetFilePath = targetFilePath.substring(SHARED_DIR.length(), targetFilePath.length());
    		targetFilePath = FileRequestConstants.SHARED_DIRECTORY + File.separator + targetFilePath;
    	}
    }

	public JobWork getJobWork() {
		logger.debug("GetJobWork start..");
		
		File sourceFile = new File(sourceFilePath);
	    if (!sourceFile.exists()) {
	      logger.error("Source filepath doesn't exists.");
	      addError(FileRequestMessageRepository.wrongFilepath());
	    }
	    File[] arrayOfSourceFiles = new File[1];
	    
	    if(notNull(fileName)){
	    	for(File file: sourceFile.listFiles()){
	    		if(file.getName().equalsIgnoreCase(fileName)){
	    			arrayOfSourceFiles[0] = file;
	    			break;
	    		}
	    	}
	    }
	    else{
	    	arrayOfSourceFiles = sourceFile.listFiles();
	    }
	    
	    if(arrayOfSourceFiles.length == 0){
	    	addError(FileRequestMessageRepository.noFileExists());
	    }
	    ThreadWorkUnit threadWorkUnit;
	    List<ThreadWorkUnit> threadWorkUnitList = new ArrayList<ThreadWorkUnit>();
	    for (File file : arrayOfSourceFiles) {
	    	threadWorkUnit = new ThreadWorkUnit();
	    	threadWorkUnit.addSupplementalData("FileName", file);
	    	threadWorkUnitList.add(threadWorkUnit);
		}
	    
	    return createJobWorkForThreadWorkUnitList(threadWorkUnitList);
	}
	
	public Class<CmTransformFileFormatWorker> getThreadWorkerClass() {
		return CmTransformFileFormatWorker.class;
	}

	public static class CmTransformFileFormatWorker extends
			CmTransformFileFormatWorker_Gen {
		
		private FrameworkSession session;
		private static final String SAVE_POINT = "SAVE_POINT";
		
		public ThreadExecutionStrategy createExecutionStrategy() {
			return new StandardCommitStrategy(this);
		}

		public boolean executeWorkUnit(ThreadWorkUnit unit)
				throws ThreadAbortedException, RunAbortedException {
			File sourceFile = null;
			String xml = null;
			try{
				sourceFile = (File) unit.getSupplementalData().get("FileName");
				session = (FrameworkSession) SessionHolder.getSession();
    			session.setSavepoint(SAVE_POINT);
			} catch (Exception e){
				e.printStackTrace();
				return false;
			}
			
			try{
				JSONObject json;
				Scanner sc = null;
				try {
    				//Convert json to XML
					sc = new Scanner(sourceFile);
					StringBuilder sourceFileContent = new StringBuilder();
					while (sc.hasNextLine()) {
						sourceFileContent.append(sc.nextLine());
					}
					
					logger.debug("sourceFileContent in JSON->"+sourceFileContent);
					
    				json = new JSONObject(sourceFileContent.toString());
    				xml = "<root>"+XML.toString(json).trim()+"</root>";
    				try{
    					 Source xmlInput = new StreamSource(new StringReader(xml));
    				     StringWriter stringWriter = new StringWriter();
    				     StreamResult xmlOutput = new StreamResult(stringWriter);
    				     TransformerFactory transformerFactory = new TransformerFactoryImpl();
    				     transformerFactory.setAttribute("indent-number", 2);
    				     Transformer transformer = transformerFactory.newTransformer();
    				     transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    				     transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    				     transformer.transform(xmlInput, xmlOutput);
    				     xml=xmlOutput.getWriter().toString();
    					 logger.debug("sourceFileContent in xml->"+xml);
    				}catch(Exception e){
    					e.printStackTrace();
    				}
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					return false;
				}
				finally{
					if(notNull(sc)){
						sc.close();
					}
				}
			}catch(ApplicationException e){
				e.printStackTrace();
				return false;
			}
			 
			
			 File targetFile = new File(targetFilePath);
			    if (!targetFile.exists()) {
			      logger.error("Target filepath doesn't exists.");
			      addError(FileRequestMessageRepository.wrongFilepath());
			    }
			targetFile = new File(targetFilePath + File.separator + sourceFile.getName().replace(".json", ".xml"));
			try {
				boolean result = targetFile.createNewFile();
				if(!result)
				{
					addError(FileRequestMessageRepository.fileAlreadyProcessed(targetFile.getName()));
				}
				
				logger.debug("Target file name->"+targetFilePath + File.separator + targetFile.getName());
				FileWriter xmlWriter = new FileWriter(targetFilePath + File.separator + targetFile.getName());
				xmlWriter.write(xml);
				xmlWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

	}

}
