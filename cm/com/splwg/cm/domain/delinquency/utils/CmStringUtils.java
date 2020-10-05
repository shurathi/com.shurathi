/*                                                               
 **************************************************************************                                                    
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *           
 * String Utility class for String related operations
 *                                    
 *                                                             
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework    
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.utils;

import com.ibm.icu.math.BigDecimal;
import com.splwg.shared.common.StringUtilities;

public class CmStringUtils {

	/**
	 * Right pad input string with spaces
	 * 
	 * @param inputString
	 * 	  columnSize
	 * @return String
	 */
	public static String rightPadWithSpaces(String inputString, int columnSize) {

		int strLen = inputString.length();

		for (int i = strLen; i < columnSize; i++) {
			inputString = inputString.concat(" ");
		}

		return inputString;

	}

	/**
	 * Check Input is numeric or not
	 * @param value -  String parameter
	 * @return - boolean - true or false
	 */
	public static Boolean isNumericValue(String value) {

		try {

			Integer.parseInt(value);
		} catch (Exception exp) {
			return false;
		}
		return true;
	}

	/**
	 * Check Input is numeric or not
	 * @param inputStr -  String parameter
	 * 		 length - allowed legth
	 * @return - boolean - true or false
	 */
	public static Boolean checkValidLength(String inputStr, int length) {

		if (inputStr.length() == length) {
			return true;
		}

		return false;
	}


	public static String truncateString(String str, int length) {
		if (!StringUtilities.isBlankOrNull(str)) {
			if (str.trim().length() > length) {
				return str.substring(0, length);
			}
		}
		return str;

	}

	/**
	 * Left pad input string with zeros
	 * 
	 * @param inputString
	 * 	  columnSize
	 * @return String
	 */
	public static String leftPadWithZeros(String inputString, int columnSize) {
		BigDecimal ZERO = BigDecimal.ZERO;
		int strLen = inputString.length();

		for (int i = strLen; i < columnSize; i++) {
			inputString = ZERO.toString().concat(inputString);
		}

		return inputString;

	}

}

