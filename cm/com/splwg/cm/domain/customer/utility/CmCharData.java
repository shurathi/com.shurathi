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
 * Default Characteristic Data Business Component
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-06-30   DDejes      CB-132. Initial Version. 
***********************************************************************
 */
package com.splwg.cm.domain.customer.utility;

import java.util.List;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicValue;
import com.splwg.ccb.api.lookup.BillFormatLookup;

/**
 * @author DDejes
 *
 */
public class CmCharData{
	
	//Work Variables
    private CharacteristicType charType;
    private String charValue;
    private Lookup charEntity;
    


    public CharacteristicType getCharType() {
        return charType;
    }


    public void setCharType(CharacteristicType charType) {
        this.charType = charType;
    }


	public String getCharValue() {
		return charValue;
	}
	

	public void setCharValue(String charValue) {
		this.charValue = charValue;
	}
	
	public Lookup getCharEntity() {
		return charEntity;
	}
	

	public void setCharEntity(Lookup charEntity) {
		this.charEntity = charEntity;
	}
}
