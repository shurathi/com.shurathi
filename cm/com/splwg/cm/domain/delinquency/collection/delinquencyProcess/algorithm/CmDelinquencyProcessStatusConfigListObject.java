/*
 ***********************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory LLC; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory LLC.
 ***********************************************************************
 *
 * PROGRAM DESCRIPTION:
 * 
 * Java Class
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:                  Reason:
 * 2020-08-19  SAnart         CB-273.Initial Version.
 */

package com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author SAnarthe
 *
 */
public class CmDelinquencyProcessStatusConfigListObject
{
	
	BigInteger sequence;
    String businessObjectStatus;
    String businessObjectStatusDescr;
    String days;


    public BigInteger getSequence() {
        return sequence;
    }

    public void setSequence(BigInteger sequence) {
        this.sequence = sequence;
    }

    public String getBusinessObjectStatus() {
        return businessObjectStatus;
    }

    public void setBusinessObjectStatus(String businessObjectStatus) {
        this.businessObjectStatus = businessObjectStatus;
    }

    public String getBusinessObjectStatusDescr() {
        return businessObjectStatusDescr;
    }

    public void setBusinessObjectStatusDescr(String businessObjectStatusDescr) {
        this.businessObjectStatusDescr = businessObjectStatusDescr;
    }

    public String getDays(){
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }	

}
