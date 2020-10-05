/*                                                                
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This business component will be responsible for creating a Write Off Request
 *                                         
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework        
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.customerClass;

import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.lookup.BusinessObjectActionLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.currency.Currency;
import com.splwg.ccb.api.lookup.FinancialTransactionTypeLookup;
import com.splwg.ccb.api.lookup.RefundWoEntityFlagLookup;
import com.splwg.ccb.domain.admin.adjustmentType.AdjustmentType;
import com.splwg.ccb.domain.admin.refundWORequestType.RefundWORequestType;
import com.splwg.ccb.domain.billing.bill.Bill;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement;
import com.splwg.ccb.domain.financial.refundWORequest.RefundWORequest;
import com.splwg.ccb.domain.financial.refundWORequest.RefundWORequestDetails;
import com.splwg.ccb.domain.financial.refundWORequest.RefundWORequestDetails_DTO;
import com.splwg.ccb.domain.financial.refundWORequest.RefundWORequest_DTO;
import com.splwg.ccb.domain.financial.refundWORequest.RefundWORequest_Id;

/**
 * @author MugdhaP
 *
@BusinessComponent (customizationCallable = true)
 */
public class CmProcessWriteOffRequestHelper_Impl
		extends GenericBusinessComponent
		implements CmProcessWriteOffRequestHelper {

	/**
	 * This method creates write off request.
	 * @param refundWORequestTypeBO
	 * @param refundWORequestType
	 * @param account
	 * @param billList
	 */
	public RefundWORequest createWriteOffRequest(BusinessObject refundWORequestTypeBO, RefundWORequestType refundWORequestType, Account account, List<Bill> billList) {
		AdjustmentType adjustmentType = null;

		BusinessObjectInstance businessObjectInstance = BusinessObjectInstance.create(refundWORequestTypeBO);
		businessObjectInstance.set(CmCashPostingToleranceWriteOffConstants.REQUEST_TYPE, refundWORequestType.getId().getIdValue());
		businessObjectInstance = BusinessObjectDispatcher.read(businessObjectInstance);
		COTSInstanceNode parameters = businessObjectInstance.getGroup(CmCashPostingToleranceWriteOffConstants.PARAMETERS_INFO_GROUP_ELEMENT);
		if (notNull(parameters)) {
			adjustmentType = parameters.getEntity(CmCashPostingToleranceWriteOffConstants.ADJUSTMENT_TYPE, AdjustmentType.class);
		}
		BusinessObject refundWORequestBO = refundWORequestType.getRelatedTransactionBO();
		RefundWORequest refundWORequest = processWriteOffRequest(refundWORequestBO, adjustmentType, refundWORequestType, account, billList);

		return refundWORequest;
	}

	/**
	 * This method process write off request.
	 * @param refundWORequestBO
	 * @param adjustmentType
	 * @param refundWORequestType
	 * @param account
	 * @param billList
	 */
	public RefundWORequest processWriteOffRequest(BusinessObject refundWORequestBO, AdjustmentType adjustmentType, RefundWORequestType refundWORequestType, Account account, List<Bill> billList) {
		BigDecimal totalWriteOffAmount = BigDecimal.ZERO;
		List<SQLResultRow> result = null;
		RefundWORequest refundWORequest = null;

		BusinessObjectInstance businessObjectInstance = BusinessObjectInstance.create(refundWORequestBO);

		businessObjectInstance.set(CmCashPostingToleranceWriteOffConstants.BO_ELEMENT_NAME, refundWORequestBO);
		businessObjectInstance.set(CmCashPostingToleranceWriteOffConstants.REQUEST_TYPE, refundWORequestType);
		businessObjectInstance.set(CmCashPostingToleranceWriteOffConstants.ACCOUNT, account);
		businessObjectInstance.set(CmCashPostingToleranceWriteOffConstants.CREATE_DATE_TIME, getProcessDateTime());

		businessObjectInstance = BusinessObjectDispatcher.execute(businessObjectInstance, BusinessObjectActionLookup.constants.FAST_ADD);

		String writeOffRequestId = businessObjectInstance.getElement().selectSingleNode(CmCashPostingToleranceWriteOffConstants.REQUEST).getText();
		if (!isBlankOrNull(writeOffRequestId)) {
			refundWORequest = new RefundWORequest_Id(writeOffRequestId).getEntity();
		}
		for (Bill bill : billList) {
			PreparedStatement preparedStatement = null;
			try {
				preparedStatement = createPreparedStatement(CmCashPostingToleranceWriteOffConstants.RETRIEVE_OPEN_FT_FROM_BILL.toString(), "CmProcessWriteOffRequestHelper_Impl");
				preparedStatement.bindId("billID", bill.getId());
				result = preparedStatement.list();
				for (SQLResultRow sqlResultRow : result) {
					String siblingId = sqlResultRow.getString("SIBLING_ID");
					ServiceAgreement serviceAgreement = sqlResultRow.getEntity("SA_ID", ServiceAgreement.class);
					FinancialTransactionTypeLookup financialTransactionTypeLookup = sqlResultRow.getLookup("FT_TYPE_FLG", FinancialTransactionTypeLookup.class);
					Currency currency = sqlResultRow.getEntity("CURRENCY_CD", Currency.class);
					BigDecimal entityAmount = sqlResultRow.getBigDecimal("ENTITYAMOUNT");
					BigDecimal writeOffAmount = sqlResultRow.getBigDecimal("WRITEOFFAMOUNT");

					RefundWORequestDetails_DTO refundWORequestDetails_DTO = createDTO(RefundWORequestDetails.class);
					refundWORequestDetails_DTO.setRefundWORequestId(refundWORequest.getId());
				
					// refundWORequestDetails_DTO.setRefundWOAmount(writeOffAmount);
					refundWORequestDetails_DTO.setRefundWOAmount(writeOffAmount.negate());
					
					refundWORequestDetails_DTO.setEntityAmount(entityAmount);
					
					// Process did not process systematic write off
					refundWORequestDetails_DTO.setBillId(bill.getId().getIdValue());
					
					// Process did not process systematic write off
					refundWORequestDetails_DTO.setCurrencyId(currency.getId());
					if (financialTransactionTypeLookup.isBillSegment() || financialTransactionTypeLookup.isBillCancellation()) {
						refundWORequestDetails_DTO.setEntityType(RefundWoEntityFlagLookup.constants.BILL_SEGMENT.toString());
					}
					if (financialTransactionTypeLookup.isAdjustment()) {
						refundWORequestDetails_DTO.setEntityType(RefundWoEntityFlagLookup.constants.ADJUSTMENT.toString());
					}
					refundWORequestDetails_DTO.setCollectionEntityId(siblingId.trim());
					refundWORequestDetails_DTO.setServiceAgreement(serviceAgreement.getId().getIdValue().trim());
					refundWORequestDetails_DTO.setAdjustmentTypeId(adjustmentType.getId());
					refundWORequestDetails_DTO.newEntity();
					totalWriteOffAmount = totalWriteOffAmount.add(writeOffAmount);
				}
			} finally {
				if (notNull(preparedStatement)) {
					preparedStatement.close();
				}
			}
		}
		if (notNull(refundWORequest)) {
			RefundWORequest_DTO refundWORequest_DTO = refundWORequest.getDTO();
			refundWORequest_DTO.setTotalRefundWoAmount(totalWriteOffAmount);
			refundWORequest.setDTO(refundWORequest_DTO);
		}
		return refundWORequest;
	}

}
