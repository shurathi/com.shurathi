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
 * This Business Service will apply Invoice Extract Template for CondoSafe 
 **************************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:        			Reason:
 * YYYY-MM-DD   IN         			Reason text.
 * 2020-07-24   ShwethaPatil     	Initial version.                                             
 **************************************************************************
 */

package com.splwg.cm.domain.billing.service;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.splwg.base.api.service.DataElement;
import com.splwg.ccb.domain.billing.billtransform.FOPReportGenerationComponent_Impl;
import com.splwg.cm.domain.billing.batch.CmInvoiceExtractTemplateHelper;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author ShwethaPatil
 *
@QueryPage (program = CMCONDOSAFEPDF, service = CMCONDOSAFEPDF,
 *      body = @DataElement (contents = { @DataField (name = OUTPUTFILEPATH)
 *                  , @DataField (name = FILEPATH)
 *                  , @DataField (name = XSL_FILE_NAME)}),
 *      actions = { "change"},
 *      header = { @DataField (name = OUTPUTFILEPATH)
 *            , @DataField (name = FILEPATH)
 *            , @DataField (name = XSL_FILE_NAME)},
 *      headerFields = { @DataField (name = OUTPUTFILEPATH)
 *            , @DataField (name = FILEPATH)
 *            , @DataField (name = XSL_FILE_NAME)},
 *      modules = { "demo"})
 */
public class CmInvoiceExtractTemplateForCondoSafeService extends
CmInvoiceExtractTemplateForCondoSafeService_Gen {
	static Logger logger = LoggerFactory.getLogger(CmInvoiceExtractTemplateForCondoSafeService.class);
	String outputHttpUrl = null;
	Document document=null;
	CmInvoiceExtractTemplateHelper helper=new CmInvoiceExtractTemplateHelper.Factory().newInstance();
	protected void change(DataElement item) throws ApplicationError {
		logger.info("---------------start change method----------------");
		String outputFilePath=item.get(STRUCTURE.OUTPUTFILEPATH);
		String inputXMLStr=item.get(STRUCTURE.FILEPATH);
		String templateFile=item.get(STRUCTURE.XSL_FILE_NAME);
		File inputFile = new File(inputXMLStr);
		SAXReader reader = new SAXReader();
		try {
			if(inputFile.exists()){

				document = reader.read(inputFile);
				if(notNull(document)){
					String billId=document.selectSingleNode("bill/billId").getText().toString();
					String name=document.selectSingleNode("bill/billToDetail/name1").getText().trim();
					String billDate=document.selectSingleNode("bill/billDate").getText().toString();
					String pdf = outputFilePath + File.separator +name+helper.SPACE+helper.EXTRACT_TYPE+helper.SPACE+billDate + helper.PDF;

					FOPReportGenerationComponent_Impl fopComp = new FOPReportGenerationComponent_Impl();
					fopComp.getFOPPdf(billId,templateFile ,pdf,inputXMLStr ,"" );

					if(inputFile.exists()){
						inputFile.delete();
					}
				}
			}
			logger.info("---------------end change method----------------");
		}
		catch (DocumentException  e) {
			logger.error("------------------------"+e);

		}

	}

}
