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
 * This Business Service will apply Invoice Extract Template for CondoSafe-XLSX  
 **************************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:        			Reason:
 * YYYY-MM-DD   IN         			Reason text.
 * 2020-08-10   ShwethaPatil     	Initial version.                                             
 **************************************************************************
 */
package com.splwg.cm.domain.billing.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.splwg.base.api.service.DataElement;
import com.splwg.cm.domain.billing.batch.CmInvoiceExtractTemplateHelper;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
/**
 * @author ShwethaPatil
 *
@QueryPage (program = CMCONDOSAFEXLSL, service = CMCONDOSAFEXLSL,
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
public class CmInvoiceExtractTemplateForCondoSafeXLSXService extends
		CmInvoiceExtractTemplateForCondoSafeXLSXService_Gen {
	static Logger logger = LoggerFactory.getLogger(CmInvoiceExtractTemplateForCondoSafeXLSXService.class);
	String outputHttpUrl = null;
	Document document=null;
	CmInvoiceExtractTemplateHelper helper=new CmInvoiceExtractTemplateHelper.Factory().newInstance();
	String prevProdDesp=null;
	String mainCustomerNumber=null;
	String billDate=null;
	String billId=null;
	CellStyle style=null;
	CellStyle body=null;
	CellStyle txnSubTtl =null;
	CellStyle threeSideBorder=null;
	CellStyle twoSideBold =null;
	CellStyle fourSideBold =null;
	CellStyle normalStyle =null;
	
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
					String name=document.selectSingleNode("bill/billToDetail/name1").getText().trim();
					String billDate=document.selectSingleNode("bill/billDate").getText().toString();
					String xlsx = outputFilePath + File.separator +name+helper.SPACE+helper.EXTRACT_TYPE+helper.SPACE+billDate +helper.XLSX;
					excelWrite(templateFile,  document, xlsx) ;
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
		catch(ApplicationError er){
			logger.error("ApplicationError------------------------"+er);

		}
	}
	public  void excelWrite(String template, Document document,String excelPath)  {
		FileInputStream fis;
		try {
			fis = new FileInputStream(new File(template));
		
		Workbook workbook = WorkbookFactory.create(fis);
		cellStyleDefination(workbook);

		if((document!=null)){
			List<Node> nodes = document.selectNodes("bill");
			Sheet sheet = workbook.getSheet("Table 1");
			for (Node node : nodes) {

				billDetails( node, sheet);
				
				int rownxt=16;
				
				rownxt=txnSummaryTransaction( node,  sheet,  rownxt) ;
				
				invoiceDetails( rownxt,  sheet, node);
		
				rownxt=rownxt+20;
				
				rownxt=txnDtlsTxnsList( node, sheet, rownxt);
				
				rownxt=txnDtlsSubTotol( rownxt, node, sheet);

			}

		}

		FileOutputStream fileOutput = new FileOutputStream(new File(excelPath));
		workbook.write(fileOutput);
		workbook.close();
		fileOutput.close();
		
		} catch (IOException  e) {
			logger.error("------------------------"+e);
		}  


	}
	private  void cellStyleDefination(Workbook workbook) {
		short i=11;
		
		short j=7;
		Font font = workbook.createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints(i);

		Font txnSubTtlFont = workbook.createFont();
		txnSubTtlFont.setFontName("Arial");
		txnSubTtlFont.setFontHeightInPoints(j);
		txnSubTtlFont.setBold(true);

		Font boldFont = workbook.createFont();
		boldFont.setFontName("Arial");
		boldFont.setFontHeightInPoints(i);
		boldFont.setBold(true);

		style = workbook.createCellStyle();
		style.setFont(font);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);

		body = workbook.createCellStyle();
		body.setFont(font);
		body.setBorderRight(BorderStyle.THIN);
		body.setBorderLeft(BorderStyle.THIN);
		body.setBorderBottom(BorderStyle.THIN);
		body.setBorderTop(BorderStyle.THIN);
		body.setAlignment(HorizontalAlignment.JUSTIFY);

		txnSubTtl = workbook.createCellStyle();
		txnSubTtl.setFont(txnSubTtlFont);
		txnSubTtl.setBorderRight(BorderStyle.THIN);
		txnSubTtl.setBorderLeft(BorderStyle.THIN);
		txnSubTtl.setBorderBottom(BorderStyle.THIN);
		txnSubTtl.setBorderTop(BorderStyle.THIN);
		txnSubTtl.setAlignment(HorizontalAlignment.RIGHT);


		threeSideBorder = workbook.createCellStyle();
		threeSideBorder.setFont(boldFont);
		threeSideBorder.setBorderRight(BorderStyle.THIN);
		threeSideBorder.setBorderLeft(BorderStyle.THIN);
		threeSideBorder.setBorderTop(BorderStyle.THIN);
		threeSideBorder.setAlignment(HorizontalAlignment.JUSTIFY);

		twoSideBold = workbook.createCellStyle();
		twoSideBold.setFont(boldFont);
		twoSideBold.setBorderRight(BorderStyle.THIN);
		twoSideBold.setBorderLeft(BorderStyle.THIN);
		twoSideBold.setAlignment(HorizontalAlignment.JUSTIFY);

		fourSideBold = workbook.createCellStyle();
		fourSideBold.setFont(boldFont);
		fourSideBold.setBorderRight(BorderStyle.THIN);
		fourSideBold.setBorderLeft(BorderStyle.THIN);
		fourSideBold.setBorderBottom(BorderStyle.THIN);
		fourSideBold.setBorderTop(BorderStyle.THIN);
		fourSideBold.setAlignment(HorizontalAlignment.JUSTIFY);

		normalStyle = workbook.createCellStyle();
		normalStyle.setFont(font);
		normalStyle.setAlignment(HorizontalAlignment.JUSTIFY);


	}
	private  void invoiceDetails(int rownxt, Sheet sheet,Node node) {
		sheet.getRow(rownxt).setHeightInPoints(15);
		Cell subTotal=sheet.getRow(rownxt).createCell(10);
		subTotal.setCellValue(helper.DOLLAR_SYMBOL+node.selectSingleNode("transactionSummary/subtotalAmount").getText());
		subTotal.setCellStyle(threeSideBorder);

		sheet.getRow(rownxt+1).setHeightInPoints(15);
		Cell tax=sheet.getRow(rownxt+1).createCell(10);
		tax.setCellValue(helper.DOLLAR_SYMBOL+node.selectSingleNode("transactionSummary/tax").getText());
		tax.setCellStyle(twoSideBold);

		Cell total=sheet.getRow(rownxt+2).createCell(10);
		total.setCellValue(helper.DOLLAR_SYMBOL+node.selectSingleNode("totalsSummary/currentCharges").getText());
		total.setCellStyle(fourSideBold);


		Cell customer=sheet.getRow(rownxt+5).createCell(0);
		customer.setCellValue("Customer Name:"+node.selectSingleNode("billToDetail/name1").getText());
		customer.setCellStyle(normalStyle);

		Cell account=sheet.getRow(rownxt+6).createCell(0);
		account.setCellValue("Account No:"+mainCustomerNumber);
		account.setCellStyle(normalStyle);

		String invoiceDetail="Invoice No:"+billId+helper.NEXT_LINE+
				"Invoice Date:"+billDate+helper.NEXT_LINE+
				"Invoice for the month of "+node.selectSingleNode("invoiceMonth").getText();
		Cell invoiceDetailCell=sheet.getRow(rownxt+7).createCell(0);
		invoiceDetailCell.setCellValue(invoiceDetail);
		invoiceDetailCell.setCellStyle(normalStyle);

		Cell invoiceTotal=sheet.getRow(rownxt+16).createCell(0);
		invoiceTotal.setCellValue(helper.DOLLAR_SYMBOL+node.selectSingleNode("totalsSummary/currentCharges").getText());
		invoiceTotal.setCellStyle(body);

		Cell amtDue=sheet.getRow(rownxt+16).createCell(1);
		amtDue.setCellValue(helper.DOLLAR_SYMBOL+node.selectSingleNode("totalsSummary/netAmountDue").getText());
		amtDue.setCellStyle(body);

		sheet.addMergedRegion(new CellRangeAddress(rownxt+18,rownxt+18,1,7));  
		Cell dateRange=sheet.getRow(rownxt+18).createCell(1);
		dateRange.setCellValue(node.selectSingleNode("transactionDetail/billStartDate").getText()+helper.HYPHEN+node.selectSingleNode("transactionDetail/billEndDate").getText());
		dateRange.setCellStyle(normalStyle);


	}
	private  void billDetails(Node node,Sheet sheet) {
		 mainCustomerNumber=node.selectSingleNode("mainCustomerNumber").getText();
		 billDate=node.selectSingleNode("billDate").getText();
		 billId=node.selectSingleNode("billId").getText();

		String billToDetail=node.selectSingleNode("billToDetail/name1").getText()+helper.NEXT_LINE +
				node.selectSingleNode("billToDetail/email").getText()+helper.NEXT_LINE+
				node.selectSingleNode("billToDetail/address1").getText()+helper.NEXT_LINE+
				node.selectSingleNode("billToDetail/city").getText()+helper.NEXT_LINE+
				node.selectSingleNode("billToDetail/county").getText()+helper.NEXT_LINE+
				node.selectSingleNode("billToDetail/state").getText()+helper.NEXT_LINE+
				node.selectSingleNode("billToDetail/postal").getText()+helper.NEXT_LINE+
				node.selectSingleNode("billToDetail/country").getText();

		sheet.getRow(2).setHeightInPoints(115);
		Cell address=sheet.getRow(2).createCell(0);
		address.setCellValue(billToDetail);
		address.setCellStyle(body);

		Cell a00 =  sheet.getRow(10).createCell(0);
		a00.setCellValue("Bill Number:" +billId);
		a00.setCellStyle(body);

		Cell b00 =  sheet.getRow(11).createCell(0);
		b00.setCellValue("Date:"+billDate);
		b00.setCellStyle(body);

		Cell c00 =  sheet.getRow(12).createCell(0);
		c00.setCellValue("Customer Number:"+mainCustomerNumber);
		c00.setCellStyle(body);


		Row row14=sheet.getRow(14);
		row14.setHeightInPoints(15);
		Cell j14=row14.createCell(9);
		j14.setCellValue(node.selectSingleNode("paymentTerms").getText());
		j14.setCellStyle(body);

		Cell k14=row14.createCell(10);
		k14.setCellValue(node.selectSingleNode("billDueDate").getText());
		k14.setCellStyle(body);

	}
	
	private  int txnSummaryTransaction(Node node, Sheet sheet, int rownxt) {
		int m=0;
		List<Node> transactions  = node.selectNodes("transactionSummary/transactions");
		for(Node nd:transactions) {
			if(m>=1) {
				sheet.shiftRows(rownxt, sheet.getLastRowNum(), 1,true,false);
				sheet.createRow(rownxt);
				sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,0,5));  
				sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,6,8)); 
				sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,10,11)); 

			}
			Row row=sheet.getRow(rownxt);

			Cell productDescription=row.createCell(0);
			productDescription.setCellValue(nd.selectSingleNode("productDescription").getText());
			productDescription.setCellStyle(style);

			Cell quantity=row.createCell(6);
			quantity.setCellValue(nd.selectSingleNode("quantity").getText());
			quantity.setCellStyle(style);

			Cell rate=row.createCell(9);
			rate.setCellValue(nd.selectSingleNode("rate").getText());
			rate.setCellStyle(style);

			Cell amount=row.createCell(10);
			amount.setCellValue(nd.selectSingleNode("amount").getText());
			amount.setCellStyle(style);

			row.createCell(11).setCellStyle(style);;
			m=m+1;
			rownxt=rownxt+1;

		}
		return rownxt;
	}
	private  int txnDtlsTxnsList(Node node,Sheet sheet,int rownxt) {
		int m=0;
		short rowheight=15;
		List<Node> transactionDetail  = node.selectNodes("transactionDetail/transactions");
		String prstProdDesp="";
		for(Node nd:transactionDetail) {

			if(m>=1) {
				sheet.shiftRows(rownxt, sheet.getLastRowNum(), 1,true,false);
				sheet.createRow(rownxt);
				sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,0,1));
				sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,3,4));
				sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,5,6));
				sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,7,8));
				sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,11,12));

			}
			Row rw=sheet.getRow(rownxt);
			rw.setHeightInPoints(rowheight);
			prstProdDesp=nd.selectSingleNode("productDescription").getText();
			if(prevProdDesp!=null) {
				if(!prevProdDesp.equals(prstProdDesp) ) {
					rownxt=txnDtlsSubTotol( rownxt, node, sheet);
				}
			}
			Cell productDescription=rw.createCell(0);
			productDescription.setCellValue(prstProdDesp);
			productDescription.setCellStyle(body);

			rw.createCell(1).setCellStyle(body);

			Cell orderNumber=rw.createCell(2);
			orderNumber.setCellValue(nd.selectSingleNode("orderNumber").getText());
			orderNumber.setCellStyle(body);

			Cell transactionDate=rw.createCell(3);
			transactionDate.setCellValue(nd.selectSingleNode("transactionDate").getText());
			transactionDate.setCellStyle(body);

			rw.createCell(4).setCellStyle(body);

			Cell loanNumber=rw.createCell(5);
			loanNumber.setCellValue(nd.selectSingleNode("loanNumber").getText());
			loanNumber.setCellStyle(body);

			rw.createCell(6).setCellStyle(body);

			Cell borrowerName=rw.createCell(7);
			borrowerName.setCellValue(nd.selectSingleNode("borrowerName").getText());
			borrowerName.setCellStyle(body);

			rw.createCell(8).setCellStyle(body);

			Cell propertyAddress=rw.createCell(9);
			propertyAddress.setCellValue(nd.selectSingleNode("propertyAddress").getText());
			propertyAddress.setCellStyle(body);

			Cell tax2=rw.createCell(10);
			tax2.setCellValue(nd.selectSingleNode("tax").getText());
			tax2.setCellStyle(body);

			Cell amount=rw.createCell(11);
			amount.setCellValue(nd.selectSingleNode("amount").getText());
			amount.setCellStyle(body);

			rw.createCell(12).setCellStyle(body);

			prevProdDesp=prstProdDesp;
			rownxt=rownxt+1;
			m=m+1;
		}
		return rownxt;
	}
  
	public  int txnDtlsSubTotol(int rownxt,Node node,Sheet sheet ) {
		short i=9;
		List<Node> products  = node.selectNodes("transactionDetail/products");
		for (Node prod:products) {
			String prdDesp=prod.selectSingleNode("productDescription").getText();
			if(prdDesp.equals(prevProdDesp)) {

				if(sheet.getLastRowNum()!=rownxt) {
					sheet.shiftRows(rownxt,  sheet.getLastRowNum(), 1,true,false);
					sheet.createRow(rownxt);
					sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,0,9));
					sheet.addMergedRegion(new CellRangeAddress(rownxt,rownxt,11,12));
					
				}else {
					sheet.getRow(rownxt).createCell(1).setCellStyle(txnSubTtl);
					sheet.getRow(rownxt).createCell(2).setCellStyle(txnSubTtl);
					sheet.getRow(rownxt).createCell(3).setCellStyle(txnSubTtl);
					sheet.getRow(rownxt).createCell(4).setCellStyle(txnSubTtl);
					sheet.getRow(rownxt).createCell(5).setCellStyle(txnSubTtl);
					sheet.getRow(rownxt).createCell(6).setCellStyle(txnSubTtl);
					sheet.getRow(rownxt).createCell(7).setCellStyle(txnSubTtl);
					sheet.getRow(rownxt).createCell(8).setCellStyle(txnSubTtl);
					sheet.getRow(rownxt).createCell(9).setCellStyle(txnSubTtl);

				}

				sheet.getRow(rownxt).setHeightInPoints(i);

				Cell subtotal=sheet.getRow(rownxt).createCell(0);
				subtotal.setCellValue("Subtotal:");
				subtotal.setCellStyle(txnSubTtl);

				Cell subtotalTax=sheet.getRow(rownxt).createCell(10);
				subtotalTax.setCellValue(helper.DOLLAR_SYMBOL+prod.selectSingleNode("subtotalTax").getText());
				subtotalTax.setCellStyle(txnSubTtl);

				Cell subtotalAmount=sheet.getRow(rownxt).createCell(11);
				subtotalAmount.setCellValue(helper.DOLLAR_SYMBOL+prod.selectSingleNode("subtotalAmount").getText());
				subtotalAmount.setCellStyle(txnSubTtl);
				sheet.getRow(rownxt).createCell(12).setCellStyle(txnSubTtl);

				rownxt=rownxt+1;
				
				break;
			}

		}
		
		return rownxt;

	}

}
