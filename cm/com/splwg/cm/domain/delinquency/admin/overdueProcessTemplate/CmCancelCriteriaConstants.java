/*                                                               
 **************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Cancel Criteria  Algorithm Constants.
 *                                                             
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    	   Reason:                                     
 * YYYY-MM-DD  	IN     	   Reason text.                                
 * 
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework    
 * **************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.overdueProcessTemplate;

public class CmCancelCriteriaConstants {
	
    public final static String CUSTOMER_TEMPLATE_STATUS = "Customer Template Status";

    public final static String TOLERANCE_PERCENTAGE = "Tolerance Percentage";
 
    public final static String TOLERANCE_AMOUNT = "Tolerance Amount";

    public final static String TOLERANCE_AMT_PERCT_REQ = "Tolerance Amount and Percentage Required";

    public final static String POLICY_STATE_BO_STATUS_OPTION_TYPE = "Policy State Business Object Status Option Type";

    public final static String ACTIVE_POLICY_STATE_OPTION_TYPE_VAL = "Active Policy Option Type Value";
	
	public final static String CONST_Y = "Y";

    public final static String CONST_N = "N";
	
	public final static String ACTIVE_STATUS = "ACTIVE";

	public final static String OPT_TYP_PARM = "CMSS";
	
	public final static String COLLECTING_ON_BILL_CHAR_TYPE = "C1_OVDBL";
	
	public final static String BO_EXT_LOOKUP = "CM-ODEventTypeExtLookup";

	public final static StringBuilder FETCH_CHAR_FROM_OD_LOGS=new StringBuilder()
	.append(" from OverdueProcessLog opl, CharacteristicType ct ")
	.append(" where opl.id.overdueProcess.id=:overdueProcessId ")
	.append(" AND opl.characteristicTypeId=:characteristicType ")
	.append(" AND opl.id.logSequenceNumber= ")
	.append(" (select max(opl2.id.logSequenceNumber) ")
	.append(" from OverdueProcessLog opl2 ")
	.append(" where opl2.id.overdueProcess.id=opl.id.overdueProcess.id ")
	.append(" AND opl2.characteristicTypeId=opl.characteristicTypeId) ")
	.append(" AND ct.id=opl.characteristicTypeId ");
	
	
    public final static String HOLD_ON_ACCT_BAL_EXISTS = "Hold On Account Balance Exists";
    public final static StringBuilder CHECK_ON_ACCOUNT_PAYMENT=new StringBuilder()
	.append(" from ServiceAgreement SA, FinancialTransaction FT WHERE SA.account.id = :overdueAccountId AND SA.serviceAgreementType.id.saType IN ")
	.append(" (SELECT RPAD(WFMOPT.value,8,' ') FROM FeatureConfigurationOption WFMOPT WHERE WFMOPT.id.workforceManagementSystem = :featureConfig ")
	.append(" AND WFMOPT.id.optionType = :featureConfigOptionType) AND SA.id = FT.serviceAgreement.id AND FT.isFrozen = :isFrozen AND FT.isNotInArrears = :isNotInArrears ");


	public final static StringBuilder ON_ACCOUNT_PAYMENTS_QUERY = new StringBuilder()
			.append(" FROM FinancialTransaction FT, ServiceAgreement SA WHERE SA.account = :account AND FT.serviceAgreement = SA.id ")
			.append("  AND FT.isFrozen = 'Y' AND EXISTS (SELECT WO.id FROM FeatureConfigurationOption WO WHERE WO.id.workforceManagementSystem = :adminContrFeatureConfig  ")
			.append(" AND WO.id.optionType = :adminContrOptionType AND SA.serviceAgreementType.id.saType = RPAD(WO.value,8)  ) ");
	public final static StringBuilder TOTAL_BILLED_AMT_QUERY = new StringBuilder()
			
			// .append(" FROM FinancialTransaction FT, Bill BL, CmFinancialTransactionExtension CMFT WHERE BL.account = :account AND BL.billStatus = 'C' ")
			// .append(" AND BL.dueDate = :latestDueDate AND FT.billId = BL.id AND FT.isFrozen = 'Y' AND FT.shouldShowOnBill = 'Y' AND FT.financialTransactionType IN ('AD', 'AX', 'BS', 'BX') ")
			// .append(" AND CMFT.id = FT.id AND CMFT.startDate = BL.dueDate ");
			
			//.append(" FROM FinancialTransaction FT, Bill BL, CmFinancialTransactionExtension CMFT WHERE BL.account = :account AND BL.billStatus = 'C' ")
			.append(" FROM FinancialTransaction FT, Bill BL, FinancialTransactionExtension CMFT WHERE BL.account = :account AND BL.billStatus = 'C' ")
			
			.append(" AND BL.dueDate = :latestDueDate AND FT.billId = BL.id AND CMFT.id = FT.id AND CMFT.startDate = :latestDueDate ");

}

