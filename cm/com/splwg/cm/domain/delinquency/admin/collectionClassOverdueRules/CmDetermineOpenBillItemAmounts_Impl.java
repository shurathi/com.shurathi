/*
 **************************************************************************
 *                                                               
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Business Component to get Bill parameter.
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    	Reason:                                     
 * YYYY-MM-DD  	IN     	Reason text.                                
 *           
 * 2020-05-10   vjrane	Initial Version : ANTHM-340 CAB1-9462 Delinquency Framework
 * **************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.collectionClassOverdueRules;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.support.cobol.CobolArray;
import com.splwg.ccb.cobol.bi.CobolCopybookCICBAZZX;
import com.splwg.ccb.cobol.bi.CobolCopybookCICBAZZX.INPUT_WRK;
import com.splwg.ccb.cobol.bi.CobolCopybookCICBAZZX.OUTPUT_WRK;
import com.splwg.ccb.cobol.bi.CobolCopybookCICBAZZX.OUTPUT_WRK.UNPAID_SA_ME_Row;
import com.splwg.ccb.cobol.bi.CobolProgramCIPBAZZN;
import com.splwg.ccb.cobol.bi.CobolProgramCIPBAZZN_Impl;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.OpenItemBillAmountResults;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.UnpaidSaMatchEventData;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.UnpaidSaNoMatchEventData;
import com.splwg.ccb.domain.financial.matchEvent.MatchEvent_Id;

/**
 * 
 * @version $Revision: #1 $
 * @BusinessComponent (customizationReplaceable = true, customizationCallable = true)
 */
public class CmDetermineOpenBillItemAmounts_Impl
        extends GenericBusinessComponent
        implements CmDetermineOpenBillItemAmounts {

    //~ Instance fields --------------------------------------------------------------------------------------

    private CobolProgramCIPBAZZN cobolProgramCIPBAZZN;
    private CobolCopybookCICBAZZX oibc;

    //~ Methods ----------------------------------------------------------------------------------------------

    public OpenItemBillAmountResults getBillAmounts(Bill bill, ServiceAgreement sa) {

       // logger.info("DetermineOpenBillItemAmounts - start getBillAmounts !!!! bill" + bill + " sa " + sa);
        mapInputDataToCobol(bill, sa);
        //logger.info("COBOL Program CIPBAZZN started");
        cobolProgramCIPBAZZN.callCobol();
        //logger.info("COBOL Program CIPBAZZN ended");

        return mapResultDataFromCobol();
    }

    private void mapInputDataToCobol(Bill bill, ServiceAgreement sa) {
        cobolProgramCIPBAZZN = new CobolProgramCIPBAZZN_Impl();
        oibc = cobolProgramCIPBAZZN.getCobolCopybookCICBAZZX();

        INPUT_WRK input = oibc.get_INPUT_WRK();
        if (notNull(bill)) {
            input.set_BILL_ID(bill.getId().getIdValue());
        }

        if (notNull(sa)) {
            input.set_SA_ID(sa.getId().getIdValue());
        }
    }

    private OpenItemBillAmountResults mapResultDataFromCobol() {

        OUTPUT_WRK output = oibc.get_OUTPUT_WRK();
        CobolArray meArray = output.get_UNPAID_SA_ME(); // Match Event Collection

        OpenItemBillAmountResults billAmountsResults = OpenItemBillAmountResults.Factory.newInstance();

        billAmountsResults.setIsCurrentAmountNotEqualToTotal(output.get_CUR_NE_TOT_SW());
        billAmountsResults.setIsUnpaidAmountIndeterminate(output.get_UNP_AMT_INDT_SW());
        billAmountsResults.setOriginalBillAmount(new Money(output.get_ORIG_BILL_AMT(), null));
        billAmountsResults.setUnpaidBillAmount(new Money(output.get_UNPAID_BILL_AMT(), null));
        billAmountsResults.setUnpaidBillFtCount(new BigDecimal(output.get_UNPAID_BILL_FT_CNT()));
        billAmountsResults.setWoBillFtCount(new BigDecimal(output.get_WO_BILL_FT_CNT()));
        billAmountsResults.setWoBillAmount(new Money(output.get_WO_BILL_AMT(), null));

        if (isNull(output.get_UNPAID_SA_ME_CNT())) return billAmountsResults;

        List<UnpaidSaMatchEventData> meData = new ArrayList<UnpaidSaMatchEventData>();
        for (int i = 0; i < output.get_UNPAID_SA_ME_CNT().intValue(); i++) {
            UNPAID_SA_ME_Row meRow = (UNPAID_SA_ME_Row) meArray.getArrayElement(i);

            UnpaidSaMatchEventData unpaidSaMeDataRow = UnpaidSaMatchEventData.Factory.newInstance();
            unpaidSaMeDataRow.setMatchEvent(new MatchEvent_Id(meRow.get_MATCH_EVT_ID()).getEntity());
            unpaidSaMeDataRow.setOriginalSaMatchEventAmount(new Money(meRow.get_ORIG_SA_ME_AMT(), null));
            unpaidSaMeDataRow.setUnpaidSaMatchEventAmount(new Money(meRow.get_UNPAID_SA_ME_AMT(), null));

            meData.add(unpaidSaMeDataRow);
        }

        billAmountsResults.setUnpaidSaMatchEventDataCollection(meData);

        UnpaidSaNoMatchEventData unpaidSaNoMatchEventData = UnpaidSaNoMatchEventData.Factory.newInstance();
        unpaidSaNoMatchEventData.setUnpaidSaAmount(new Money(output.get_UNPAID_SA_NO_ME().get_UNPAID_SA_AMT(), null));
        unpaidSaNoMatchEventData
                .setUnpaidSaFtCount(new BigDecimal(output.get_UNPAID_SA_NO_ME().get_UNPAID_SA_FT_CNT()));

        billAmountsResults.setUnpaidSaNoMatchEventData(unpaidSaNoMatchEventData);

        return billAmountsResults;
    }

}

