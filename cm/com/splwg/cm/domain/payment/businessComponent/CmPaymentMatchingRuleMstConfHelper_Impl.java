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
 * Payment Matching Rule Master Config Helper
 *
 *
 ***********************************************************************
 *
 * CHANGE HISTORY:
 *
 * Date:        by:         Reason:
 * 2020-06-12   JRaymu      Initial Version. 
 ***********************************************************************
 */
package com.splwg.cm.domain.payment.businessComponent;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.country.Country;
import com.splwg.ccb.domain.admin.matchType.MatchType;

/**
 * @author Jerrick Raymundo
 *
@BusinessComponent ()
 */
public class CmPaymentMatchingRuleMstConfHelper_Impl extends GenericBusinessComponent implements
        CmPaymentMatchingRuleMstConfHelper {
    // Getters
    private MatchType matchByBill;
    private MatchType matchByBS;
    private MatchType matchByAcct;
    private MatchType applyToGenSus;
    private CharacteristicType orderNumCharType;
    private CharacteristicType loanNumCharType;
    private Country defaultCountry;

    public MatchType getMatchByBill() {
        return matchByBill;
    }

    public MatchType getMatchByBS() {
        return matchByBS;
    }

    public MatchType getMatchByAcct() {
        return matchByAcct;
    }

    public MatchType getApplyToGenSus() {
        return applyToGenSus;
    }

    public CharacteristicType getOrderNumCharType() {
        return orderNumCharType;
    }

    public CharacteristicType getLoanNumCharType() {
        return loanNumCharType;
    }

    public Country getDefaultCountry() {
        return defaultCountry;
    }

    // Constants
    private static final String PAYMENT_MATCHING_RULE_BO = "CM-PaymentMatchingRule";
    private static final String MAIN_CONF_GRP = "mainConfig";
    private static final String MATCH_BY_BILL = "matchByBillMatchType";
    private static final String MATCH_BY_BS = "matchByBillSegmentMatchType";
    private static final String MATCH_BY_ACCT = "matchByAccountMatchType";
    private static final String APPLY_TO_GEN_SUS = "applyToGeneralSuspenseMatchType";
    private static final String ORDER_NUM_CHAR_TYP = "orderNumberCharacteristicType";
    private static final String LOAN_NUM_CHAR_TYPE = "loanNumberCharacteristicType";
    private static final String DEFAULT_COUNTRY = "defaultCountry";

    public void invoke() {
        // Load BO Details
        BusinessObjectInstance boInstance = BusinessObjectInstance.create(PAYMENT_MATCHING_RULE_BO);
        boInstance.set("bo", PAYMENT_MATCHING_RULE_BO);

        boInstance = BusinessObjectDispatcher.read(boInstance, true);
        if ((boInstance) == null) {
            // Payment Matching Rule Master Configuration is not configured
            System.out.println("Payment Matching Rule Master Configuration is not configured");
        }
        initializeGetters(boInstance);

    }

    private void initializeGetters(BusinessObjectInstance boInstance) {
        COTSInstanceNode mainConfig = boInstance.getGroup(MAIN_CONF_GRP);
        matchByBill = mainConfig.getEntity(MATCH_BY_BILL, MatchType.class);
        matchByBS = mainConfig.getEntity(MATCH_BY_BS, MatchType.class);
        matchByAcct = mainConfig.getEntity(MATCH_BY_ACCT, MatchType.class);
        applyToGenSus = mainConfig.getEntity(APPLY_TO_GEN_SUS, MatchType.class);
        orderNumCharType = mainConfig.getEntity(ORDER_NUM_CHAR_TYP, CharacteristicType.class);
        loanNumCharType = mainConfig.getEntity(LOAN_NUM_CHAR_TYPE, CharacteristicType.class);
        defaultCountry = mainConfig.getEntity(DEFAULT_COUNTRY, Country.class);

    }
}
