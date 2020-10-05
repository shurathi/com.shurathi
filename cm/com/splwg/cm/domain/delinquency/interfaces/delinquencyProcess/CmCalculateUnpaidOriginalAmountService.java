/*
 **************************************************************************
 *                                                               
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Input: Delinquency Process ID and Bill Id
 * Output:Original Amount and Unpaid Amount
 * This Business Service Calculates the Original and Amount for the BillId
 *                                                          
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.interfaces.delinquencyProcess;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.Query;
import com.splwg.base.api.QueryResultRow;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.service.DataElement;
import com.splwg.base.domain.common.algorithm.Algorithm;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.maintenanceObject.MaintenanceObject_Id;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.billing.bill.Bill_Id;
import com.splwg.cm.api.lookup.CmDelinquencyRelatedObjTypeLookup;
import com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor.CmDelinquencyCustomerMonitorRuleConstants;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmCalculateUnpaidOriginalAmountAlgorithmSpot;
import com.splwg.cm.domain.delinquency.admin.delinquencyProcessType.CmDelinquencyProcessType_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcessRelatedObject_Id;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.shared.common.ApplicationError;

/**
 * @author VINODW
 *
@QueryPage (program = CMORIUNPDIAMT, service = CMORIUNPDIAMT,
 *      body = @DataElement (contents = { @DataField (name = BILL_ID)
 *                  , @DataField (name = ORIGINAL_AMT)
 *                  , @DataField (name = CM_DELIN_PROC_ID)
 *                  , @DataField (name = UNPAID_AMT)}),
 *      actions = { "change"},
 *      modules = { "demo"})
 */
public class CmCalculateUnpaidOriginalAmountService extends
		CmCalculateUnpaidOriginalAmountService_Gen {
	private BigDecimal billUnpaidAmount = BigDecimal.ZERO;
	private BigDecimal billOriginalAmount = BigDecimal.ZERO;

	private Money originalAmount;
	private Money unpaidAmount;

	@SuppressWarnings("deprecation")
	@Override
	protected void change(DataElement item) throws ApplicationError {

		String delProcId = item.get(STRUCTURE.CM_DELIN_PROC_ID);
		String billId = item.get(STRUCTURE.BILL_ID);
		Bill bill = new Bill_Id(billId).getEntity();
		
		CmDelinquencyProcess_Id delinquencyProcessId = new CmDelinquencyProcess_Id(delProcId);
		if (notNull(delinquencyProcessId.getEntity())) {

			CmDelinquencyProcessType_Id delinqProcessTypeId = delinquencyProcessId
						.getEntity().getCmDelinquencyProcessType().getId();
				Query<QueryResultRow> query = createQuery(
						CmDelinquencyCustomerMonitorRuleConstants.FETCH_DELINQ_PROC_TYP_ALG
								.toString(), "CmCalculateUnpaidOriginalAmountService");
				query.bindId("delinquencyProcessType", delinqProcessTypeId);
				query.addResult("algorithm", "DPTA.algorithm");

				Algorithm algorithm = (Algorithm) query.firstRow();

				if (notNull(algorithm)) {
					CmCalculateUnpaidOriginalAmountAlgorithmSpot algorithmSpot = AlgorithmComponentCache
							.getAlgorithmComponent(algorithm.getId(), CmCalculateUnpaidOriginalAmountAlgorithmSpot.class);

					algorithmSpot.setDelinquencyProcessId(delinquencyProcessId);

					MaintenanceObject_Id maintenanceObjectId = new MaintenanceObject_Id(CmDelinquencyCustomerMonitorRuleConstants.MAINTENANCE_OBJ_BILL);
					CmDelinquencyProcessRelatedObject_Id cmDelProcRelObjId = new CmDelinquencyProcessRelatedObject_Id(delinquencyProcessId, maintenanceObjectId, CmDelinquencyRelatedObjTypeLookup.constants.CM_COLLECTING_ON, billId);

					if (notNull(cmDelProcRelObjId.getEntity())) {

						algorithmSpot.setDelinquencyProcessRelatedObject(cmDelProcRelObjId.getEntity());
						algorithmSpot.invoke();

						billOriginalAmount = algorithmSpot.getOriginalAmount();
						billUnpaidAmount = algorithmSpot.getUnpaidAmount();

						originalAmount = new Money(billOriginalAmount,bill.getAccount().getCurrency().getId());
						unpaidAmount = new Money(billUnpaidAmount,bill.getAccount().getCurrency().getId());

					}
				}

				item.put(CmCalculateUnpaidOriginalAmountService_Gen.STRUCTURE.ORIGINAL_AMT,
						originalAmount);
				item.put(CmCalculateUnpaidOriginalAmountService_Gen.STRUCTURE.UNPAID_AMT,
						unpaidAmount);

				setOverrideResultForChange(item);			

		}

	}

}

