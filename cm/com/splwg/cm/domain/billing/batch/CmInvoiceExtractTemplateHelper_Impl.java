package com.splwg.cm.domain.billing.batch;

import java.io.File;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.shared.environ.ApplicationProperties;

/**
 * @author ShwethaPatil
 *
@BusinessComponent ()
 */
public class CmInvoiceExtractTemplateHelper_Impl extends
GenericBusinessComponent implements CmInvoiceExtractTemplateHelper {

	public static final String TEMPLATE_CONDO_SAFE_PDF = "CondoSafe-PD";
	public static final String TEMPLATE_CONDO_SAFE_XLSX = "CondoSafe-XL";
	public static final String INPUT_XML="inputXML";
	public static final String XML=".xml";
	public static final String XSL=".xsl";
	public static final String PDF=".pdf";
	public static final String XML_TAG="<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	public static final String BILL_START_TAG="<billDetails>";
	public static final String BILL_END_TAG="</billDetails>";
	public static final String SHARED_VARIABLE="@SHARED_DIR";
	public static final String INSTALLED_VARIABLE="@INSTALL_DIR";
	public static final String SHARED_DIRECTORY = ApplicationProperties
			.getNullableProperty("com.oracle.ouaf.fileupload.shared.directory");
	public static final String INSTALLED_DIRECTORY = System.getenv("SPLEBASE");
	public static final String EXTRACT_TYPE="CONDOSAFE INVOICES";
	public static final String SPACE=" ";
	public static final String XLSX=".xlsx";
	public static final String DOLLAR_SYMBOL="$";
	public static final String NEXT_LINE="\n";
	public static final String HYPHEN="-";
	
	public String getReportingDirPath(String paramString)
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
}
