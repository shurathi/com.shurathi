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
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 *
 * This will be used as json formatter
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-04-16	DDejes		CB-37
 ***********************************************************************
 */
package com.splwg.cm.domain.common.customBusinessComponent;

import java.util.regex.Pattern;

import com.splwg.base.api.GenericBusinessComponent;

/**
 * @author Denise De Jesus
 *
@BusinessComponent ()
 */
public class CmCommonAPI_Impl extends GenericBusinessComponent implements
		CmCommonAPI {
	private static final Pattern LTRIM = Pattern.compile("^\\s+");
	private static final String COMMA = ",";
	/**
	 * Format Json
	 * @param intinalJsonString
	 * @param custIntStg
	 * @return string
	 */
	public String jsonFormatter(String intinalJsonString) {
		String formattedJson=" ";
		try {
			String[] colonSplitArry=intinalJsonString.split("\":");
			String[] commaSplitArry;
			String commaSplitFirstStr;
			String commaSplitSecondStr;
			int i=0;

			for(String colonSplitStr:colonSplitArry){
				colonSplitStr=ltrim(colonSplitStr).trim();
				if(colonSplitStr.contains(COMMA)){
					commaSplitArry = colonSplitStr.split(COMMA);
					commaSplitFirstStr=ltrim(commaSplitArry[0]).trim();
					commaSplitSecondStr=ltrim(commaSplitArry[1]).trim();
					if(!commaSplitFirstStr.startsWith("\"") && !commaSplitFirstStr.contains("}")){
						commaSplitFirstStr="\"".concat(commaSplitFirstStr).concat("\"").trim();
					}
					if(commaSplitSecondStr.contains("\"")){
						colonSplitStr=commaSplitFirstStr+COMMA+commaSplitSecondStr;
					}
				}else if(colonSplitStr.contains("}") && !colonSplitStr.startsWith("\"")){

					colonSplitStr="\""+colonSplitStr.replaceFirst("}", "\"}");
				}
				if(++i==1){
					formattedJson=colonSplitStr;
				}else if(++i>1){
					formattedJson=formattedJson.concat("\":").concat(colonSplitStr).trim();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return formattedJson;
	}
	public String ltrim(String string) {
		return LTRIM.matcher(string).replaceAll("");
	}

}
