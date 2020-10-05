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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlCursor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.splwg.base.api.service.DataElement;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author IshitaGarg
 *
@QueryPage (program = CMASETCS, secured = false, service = CMASETCS,
 *      body = @DataElement (contents = { @DataField (name = CM_EXTRACT_FILE)
 *                  , @DataField (name = OUTPUTFILEPATH)
 *                  , @DataField (name = CM_PROC_EXTR_FILEPATH)
 *                  , @DataField (name = OVRD_TMPL_SRC_FILE)}),
 *      actions = { "change"
 *            , "add"
 *            , "read"},
 *      header = { @DataField (name = OUTPUTFILEPATH)
 *            , @DataField (name = OVRD_TMPL_SRC_FILE)
 *            , @DataField (name = CM_PROC_EXTR_FILEPATH)
 *            , @DataField (name = CM_EXTRACT_FILE)},
 *      headerFields = { @DataField (name = OUTPUTFILEPATH)
 *            , @DataField (name = OVRD_TMPL_SRC_FILE)
 *            , @DataField (name = CM_PROC_EXTR_FILEPATH)
 *            , @DataField (name = CM_EXTRACT_FILE)},
 *      modules = { "demo"})
 */
public class CmApplyStmtExtrTemplateCondoSafeService extends
CmApplyStmtExtrTemplateCondoSafeService_Gen {
	static Logger logger = LoggerFactory.getLogger(CmApplyStmtExtrTemplateCondoSafeService.class);
	String outputHttpUrl = null;
	Document document=null;
	//CmInvoiceExtractTemplateHelper helper=new CmInvoiceExtractTemplateHelper.Factory().newInstance();
	protected void change(DataElement item) throws ApplicationError {
		logger.info("---------------start change method----------------");
		try {
			String inputXML=item.get(STRUCTURE.CM_EXTRACT_FILE);
			File file = new File(inputXML);  
			//an instance of factory that gives a document builder  
			
			String outputFilePath=item.get(STRUCTURE.OUTPUTFILEPATH);
			FileInputStream srcFile = new FileInputStream(new File(item.get(STRUCTURE.OVRD_TMPL_SRC_FILE)));
			File outputFile = new File(outputFilePath+"CondoSafe_Billing_Statement.xlsx");
		      if (outputFile.createNewFile()) {
		        logger.info("File created: " + outputFile.getName());
		      } else {
		       logger.info("File already exists.");
		      }
			XSSFWorkbook outputFileWb = new XSSFWorkbook ();
			
			XSSFWorkbook templateFile = new XSSFWorkbook(srcFile); 
			
			outputFileWb = templateFile;
			
			XSSFFont font = outputFileWb.createFont();
			CellStyle style = outputFileWb.createCellStyle();
			XSSFSheet outputSheet = outputFileWb.getSheetAt(0);
			
			int rowNumber = 0;
			
			// Reading XML
			final String XML_TAG="<?xml version=\"1.0\" encoding=\"utf-8\"?>";
			StringBuilder data=new StringBuilder();
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
			List<Node> statementList = extractXML.selectNodes("statement");
			
			XSSFSheet sheet = templateFile.getSheetAt(0);
			Iterator ite = sheet.rowIterator();
			int counter =0 ;
			 XSSFSheet dublicateSheet = outputFileWb.cloneSheet(0);
			 outputSheet.removeRow(outputSheet.getRow(18));
			for(Node statement : statementList)
			{
				if(rowNumber != 0)
				{
					 int i=rowNumber+2;
					 rowNumber = rowNumber +3;
					 int countRows = 1;
					for(Row row : dublicateSheet)
					{
						if(countRows > 17)
						{
							break;
						}
						Row rw=outputSheet.createRow(i+row.getRowNum()+1);
						CellRangeAddress mg= outputSheet.getMergedRegion(row.getRowNum());
			            outputSheet.addMergedRegion(new CellRangeAddress(mg.getFirstRow()+i+1,mg.getLastRow()+i+1,mg.getFirstColumn(),mg.getLastColumn()));
			            rw.setHeightInPoints(row.getHeightInPoints());
			            rw.setRowStyle(row.getRowStyle());
			            rw.setHeight(row.getHeight());
			            for (Cell mycell : row) {
			                CellType typ=    mycell.getCellType();
			                Cell cell=    rw.createCell(mycell.getColumnIndex());
			                if(typ .equals(CellType.BLANK))
			                    cell.setCellValue(mycell.getStringCellValue());
			                else if (typ.equals(CellType.BOOLEAN))
			                    cell.setCellValue(mycell.getBooleanCellValue());
			                else if (typ .equals(CellType.ERROR))
			                    cell.setCellValue(mycell.getErrorCellValue());
			                else if (typ .equals(CellType.FORMULA))
			                    cell.setCellValue(mycell.getCellFormula());
			                else if (typ.equals(CellType.NUMERIC))
			                    cell.setCellValue(mycell.getNumericCellValue());
			                else if (typ .equals(CellType.STRING))
			                    cell.setCellValue(mycell.getStringCellValue());

			                cell.setCellStyle(mycell.getCellStyle());
			                
			            }
			            countRows++;
					}
				}
				String addressLine = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.ADDRESS_LINE_1).getText();
				String addressLine2 = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.ADDRESS_LINE_2).getText();
				String addressLine3 = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.ADDRESS_LINE_3).getText();
				String addressLine4 = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.ADDRESS_LINE_4).getText();
				
				if(addressLine2.length()>1)
				{
					addressLine = addressLine.concat(","+addressLine2);
				}
				if(addressLine3.length()>1)
				{
					addressLine = addressLine.concat(","+addressLine3);
				}
				if(addressLine4.length()>1)
				{
					addressLine = addressLine.concat(","+addressLine4);
				}
				
				String city = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.CITY).getText();
				String state = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.STATE).getText();
				String postal = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.POSTAL).getText();
				String country = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.COUNTRY).getText();
				
				String statementDate = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.STATEMENT_DATE).getText();
				
				String logo = statement.selectSingleNode("logo").getText();
				
				if(rowNumber == 0)
				{
					deleteImage(outputSheet);
				}
				if(!isBlankOrNull(logo))
				{
					addLogo(outputFileWb,outputSheet,rowNumber,0,logo);
				}
				setCellData(rowNumber+2, 3, outputSheet, readCellData(rowNumber+2, 3, sheet)+statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.STATEMENT_DATE).getText(),false);
				setStatementDateAlignStyle(outputFileWb,rowNumber+2,3,outputSheet);
				setCellData(rowNumber+5, 1, outputSheet, statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.CUSTOMER_NAME).getText(),false);
				setCellData(rowNumber+6, 1, outputSheet, statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.CUSTOMER_NUMBER).getText(),false);
				
				// Buliding Bill To Information
				setCellData(rowNumber+8, 0, outputSheet, statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.BILL_TO).selectSingleNode(CmApplyStatementExtractTemplateConstants.CUSTOMER_NAME).getText(),false);
				if(rowNumber==0)
				{
					setBillToCusNameStyle(outputFileWb,rowNumber+8,0,outputSheet,true);
				}
				else
				{
					outputSheet.addMergedRegion(new CellRangeAddress(rowNumber+8,rowNumber+8,0,2));
					setBillToCusNameStyle(outputFileWb,rowNumber+8,0,outputSheet,false);
				}
				setCellData(rowNumber+9, 0, outputSheet, addressLine,false);
				if(rowNumber != 0)
				{
					outputSheet.addMergedRegion(new CellRangeAddress(rowNumber+9,rowNumber+9,0,2));
				}
				setCellData(rowNumber+10, 0, outputSheet,city+","+state+","+postal+","+country,false);
				if(rowNumber==0)
				{
					setBillToCityStyle(outputFileWb,rowNumber+10,0,outputSheet,true);
				}
				else
				{
					setBillToCityStyle(outputFileWb,rowNumber+10,0,outputSheet,false);
					outputSheet.addMergedRegion(new CellRangeAddress(rowNumber+10,rowNumber+10,0,2));
				}
				
				rowNumber = rowNumber+10+5;
				
				// Buliding Statement Detail
				List<Node> statementDetailList = statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.STATEMENT_DETAILS).selectNodes(CmApplyStatementExtractTemplateConstants.STATEMENT_DETAIL);
				for(Node statementDetail :  statementDetailList)
				{
					double remainingBalance = Double.parseDouble(statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.REMAINING_BALANCE).getText());
					setCellData(rowNumber, 0, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.DATE).getText(),true);
					setCellData(rowNumber, 1, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.CONSOLIDATED_INVOICE_NUMBER).getText(),false);
					setCellData(rowNumber, 2, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.ORDER_NUMBER).getText(),false);
					setCellData(rowNumber, 3, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.DESCRIPTION).getText(),false);
					if(remainingBalance<0)
					{
						setCellData(rowNumber, 4, outputSheet,"("+Math.abs(remainingBalance)+")",false);
						setCSSStyle(font,style,rowNumber,4,outputSheet);
					}
					else
					{
						setCellData(rowNumber, 4, outputSheet,remainingBalance+"",false);
					}
					setCellData(rowNumber, 5, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.LOAN_NUMBER).getText(),false);
					setCellData(rowNumber, 6, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.BORROWER).getText(),false);
					setCellData(rowNumber, 7, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.ADDRESS).getText(),false);
					setCellData(rowNumber, 8, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.STATE).getText(),false);
					setCellData(rowNumber, 9, outputSheet, statementDetail.selectSingleNode(CmApplyStatementExtractTemplateConstants.POSTAL).getText(),false);
					rowNumber++;
				}
				rowNumber = rowNumber + 2;
				setCellData(rowNumber, 3, outputSheet,"Sub Total",true);	
				setCellData(rowNumber, 4, outputSheet, statement.selectSingleNode(CmApplyStatementExtractTemplateConstants.SUBTOTAL).getText(),false);	
				rowNumber = rowNumber+2;
			}
			
			setCellData(rowNumber+2, 3, outputSheet, "Grand Total",true);
			setCellData(rowNumber+2, 4, outputSheet, extractXML.selectSingleNode(CmApplyStatementExtractTemplateConstants.GRAND_TOTAL).getText(),false);
			outputFileWb.removeSheetAt(1);
			FileOutputStream fileOut = new FileOutputStream(outputFile);
			outputFileWb.write(fileOut);
			outputFileWb.close();
	        fileOut.close();
	        myReader.close();
	        
	       
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
	/**
	 * Reading Cell Data
	 * @param vRow
	 * @param vColumn
	 * @param sheet
	 * @return 
	 */
	public String readCellData(int vRow, int vColumn,XSSFSheet sheet)  
	{  
		String value=null;          //variable for storing the cell value     
		Row row=sheet.getRow(vRow); //returns the logical row  
		Cell cell=row.getCell(vColumn); //getting the cell representing the given column  
		value=cell.getStringCellValue();    //getting cell value  
		return value;               //returns the cell value  
	}  
	
	/**
	 * Setting Cell Data
	 * @param vRow
	 * @param vColumn
	 * @param sheet
	 * @param value
	 * @param isCreateRow
	 */
	public static void setCellData(int vRow, int vColumn,XSSFSheet sheet,String value,boolean isCreateRow)  
	{   
		XSSFRow row;
		if(isCreateRow)
		{
			row=sheet.createRow(vRow); //returns the logical row
		}
		else
		{
			row=sheet.getRow(vRow); //returns the logical row
		}
		XSSFCell cell=row.createCell(vColumn); //getting the cell representing the given column  
		cell.setCellValue(value);    //getting cell value   
	}  
	/**
	 * Setting CSS Style
	 * @param font
	 * @param style
	 * @param rowNum
	 * @param columnNum
	 * @param sheet
	 */
	public static void setCSSStyle(XSSFFont font,CellStyle style,int rowNum,int columnNum,XSSFSheet sheet)
	{
		font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        XSSFRow row=sheet.getRow(rowNum); //returns the logical row  
		XSSFCell cell=row.getCell(columnNum); //getting the cell representing the given column  
        cell.setCellStyle(style);
	}
	/**
	 * Setting Style for Customer Name
	 * @param wb
	 * @param rowNum
	 * @param columnNum
	 * @param sheet
	 */
	public static void setBillToCusNameStyle(XSSFWorkbook wb,int rowNum,int columnNum,XSSFSheet sheet,boolean isBorderRight)
	{
		 CellStyle cellStyle = wb.createCellStyle();  
		 cellStyle.setBorderTop(BorderStyle.THIN);
		 if(isBorderRight)
		 cellStyle.setBorderRight(BorderStyle.THIN);
		 XSSFRow row=sheet.getRow(rowNum); //returns the logical row  
			XSSFCell cell=row.getCell(columnNum); //getting the cell representing the given column  
			cell.setCellStyle(cellStyle);
	}
	/**
	 * Setting Style for City
	 * @param wb
	 * @param rowNum
	 * @param columnNum
	 * @param sheet
	 */
	public static void setBillToCityStyle(XSSFWorkbook wb,int rowNum,int columnNum,XSSFSheet sheet,boolean isBorderRight)
	{
		 CellStyle cellStyle = wb.createCellStyle();  
		 cellStyle.setBorderBottom(BorderStyle.THIN);
		 if(isBorderRight)
		 cellStyle.setBorderRight(BorderStyle.THIN);
		 XSSFRow row=sheet.getRow(rowNum); //returns the logical row  
			XSSFCell cell=row.getCell(columnNum); //getting the cell representing the given column  
			cell.setCellStyle(cellStyle);
	}
	public static void setStatementDateAlignStyle(XSSFWorkbook wb,int rowNum,int columnNum,XSSFSheet sheet)
	{
		CellStyle cellStyle = wb.createCellStyle();  
		 cellStyle.setAlignment(HorizontalAlignment.CENTER);
		 XSSFRow row=sheet.getRow(rowNum); //returns the logical row  
			XSSFCell cell=row.getCell(columnNum); //getting the cell representing the given column  
			cell.setCellStyle(cellStyle);
	}

	/**
	 * Add logo
	 * @param wb
	 * @param sheet
	 * @param rowNum
	 * @param colNum
	 * @param logo
	 */
	public static void addLogo(XSSFWorkbook wb,XSSFSheet sheet,int rowNum,int colNum,String logo)
	{     
		 /* Read input PNG / JPG Image into FileInputStream Object*/
		 InputStream my_banner_image;
		try {
			my_banner_image = new FileInputStream(logo);
			/* Convert picture to be added into a byte array */
			 byte[] bytes = IOUtils.toByteArray(my_banner_image);
			 /* Add Picture to Workbook, Specify picture type as PNG and Get an Index */
			 int my_picture_id = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
			 /* Close the InputStream. We are ready to attach the image to workbook now */
			 my_banner_image.close();                
			 /* Create the drawing container */
			 XSSFDrawing drawing = sheet.createDrawingPatriarch();
			 /* Create an anchor point */
			 XSSFClientAnchor my_anchor = new XSSFClientAnchor();
			 /* Define top left corner, and we can resize picture suitable from there */
			 my_anchor.setCol1(colNum);
			 my_anchor.setRow1(rowNum);           
			 /* Invoke createPicture and pass the anchor point and ID */
			 XSSFPicture  my_picture = drawing.createPicture(my_anchor, my_picture_id);
			 /* Call resize method, which resizes the image */
			 my_picture.resize(); 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	/**
	 * @param sheet
	 */
	public static void deleteImage(XSSFSheet sheet)
	{
		XSSFDrawing drawing = sheet.getDrawingPatriarch();

		  XSSFPicture xssfPictureToDelete = null;
		  if (drawing instanceof XSSFDrawing) 
		  {
			for (XSSFShape shape : (drawing).getShapes()) 
			{
				if (shape instanceof XSSFPicture) 
				{
					XSSFPicture xssfPicture = (XSSFPicture)shape;
					String shapename = xssfPicture.getShapeName();
				int row = xssfPicture.getClientAnchor().getRow1();
					int col = xssfPicture.getClientAnchor().getCol1();
				//System.out.println("Picture " + "" + " with Shapename: " + shapename + " is located row: " + row + ", col: " + col);

		    xssfPictureToDelete = xssfPicture;
		    if  (xssfPictureToDelete != null) deleteEmbeddedXSSFPicture(xssfPictureToDelete);
			  if  (xssfPictureToDelete != null) deleteCTAnchor(xssfPictureToDelete);

		    }
		   }
		  }
		 
	}
	
	/**
	 * @param xssfPicture
	 */
	public static void deleteEmbeddedXSSFPicture(XSSFPicture xssfPicture) {
		  if (xssfPicture.getCTPicture().getBlipFill() != null) {
		   if (xssfPicture.getCTPicture().getBlipFill().getBlip() != null) {
		    if (xssfPicture.getCTPicture().getBlipFill().getBlip().getEmbed() != null) {
		     String rId = xssfPicture.getCTPicture().getBlipFill().getBlip().getEmbed();
		     if(!(rId.equals("") || rId.equals(null)))
		     {
		     XSSFDrawing drawing = xssfPicture.getDrawing();
		     drawing.getPackagePart().removeRelationship(rId);
		     drawing.getPackagePart().getPackage().deletePartRecursive(drawing.getRelationById(rId).getPackagePart().getPartName());
		     }
		    // System.out.println("Picture " + xssfPicture + " was deleted.");
		    }
		   }
		  }
		 }
	/**
	 * @param xssfPicture
	 */
	public static void deleteCTAnchor(XSSFPicture xssfPicture) {
		  XSSFDrawing drawing = xssfPicture.getDrawing();
		  XmlCursor cursor = xssfPicture.getCTPicture().newCursor();
		  cursor.toParent();
		  if (cursor.getObject() instanceof org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor) {
		   for (int i = 0; i < drawing.getCTDrawing().getTwoCellAnchorList().size(); i++) {
		    if (cursor.getObject().equals(drawing.getCTDrawing().getTwoCellAnchorArray(i))) {
		     drawing.getCTDrawing().removeTwoCellAnchor(i);
		    // System.out.println("TwoCellAnchor for picture " + xssfPicture + " was deleted.");
		    }
		   }
		  } else if (cursor.getObject() instanceof org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor) {
		   for (int i = 0; i < drawing.getCTDrawing().getOneCellAnchorList().size(); i++) {
		    if (cursor.getObject().equals(drawing.getCTDrawing().getOneCellAnchorArray(i))) {
		     drawing.getCTDrawing().removeOneCellAnchor(i);
		   //  System.out.println("OneCellAnchor for picture " + xssfPicture + " was deleted.");
		    }
		   }
		  } else if (cursor.getObject() instanceof org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTAbsoluteAnchor) {
		   for (int i = 0; i < drawing.getCTDrawing().getAbsoluteAnchorList().size(); i++) {
		    if (cursor.getObject().equals(drawing.getCTDrawing().getAbsoluteAnchorArray(i))) {
		     drawing.getCTDrawing().removeAbsoluteAnchor(i);
		  //   System.out.println("AbsoluteAnchor for picture " + xssfPicture + " was deleted.");
		    }
		   }
		  }
		 }
	}

