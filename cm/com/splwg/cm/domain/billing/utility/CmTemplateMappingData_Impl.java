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
 * Template Mapping Data Business Component
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-06-09   JFerna      CB-94. Initial Version. 
***********************************************************************
 */
package com.splwg.cm.domain.billing.utility;

import java.util.List;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.ccb.api.lookup.BillFormatLookup;

/**
 * @author JFerna
 *
@BusinessComponent ()
 */
public class CmTemplateMappingData_Impl extends GenericBusinessComponent implements CmTemplateMappingData {
	
	//Work Variables
    private BillFormatLookup billFormat;
    private List<String> temaplateCodes;

	/**
	 * This method returns the Bill Format Flag
	 * @return Bill Format Flag
	 */
    public BillFormatLookup getBillFormat() {
        return billFormat;
    }

	/**
	 * This method sets the Bill Format Flag
	 * @param billFormat
	 */
    public void setBillFormat(BillFormatLookup billFormat) {
        this.billFormat = billFormat;
    }

	/**
	 * This method returns the Template Codes
	 * @return Template Codes
	 */
	public List<String> getTemplateCodes() {
		return temaplateCodes;
	}
	
	/**
	 * This method sets the Template Codes
	 * @param temaplateCodes
	 */
	public void setTemplateCodes(List<String> temaplateCodes) {
		this.temaplateCodes = temaplateCodes;
	}
}
