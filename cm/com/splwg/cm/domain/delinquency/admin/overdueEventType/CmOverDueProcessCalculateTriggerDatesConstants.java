/*                                                              
 ************************************************************************************                                                                
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This is constant file for algorithm created for overdue process event 
 * activation. 
 *                                                          
 ************************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 ************************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.overdueEventType;

public class CmOverDueProcessCalculateTriggerDatesConstants{

	public final static String ENITITY_FILED_ACCT = "ACCT";
	
	public final static String ENITITY_FILED_OVERDUE_PROCESS_LOG = "ODLG";
	
	public final static String YES = "Y";
	
	public final static String DEPENDENT_ON_STATE = "STAT";
	
	public final static String ALWAYS_ELIGIBLE = "ALEL";
	
	public final static String DAYS_USAGE_AFTR= "AFTR";
	
	public final static String DAYS_USAGE_BFR = "BEFR";
		
	//fetch latest bill date
	public final static StringBuilder FETCH_LATEST_BILL_DT = new StringBuilder("From Bill bl, OverdueProcessObjectsBeingCollectedOn od ")
	        .append(" where bl.id = od.characteristicValueForeignKey1 and od.id.overdueProcess =:overDueProcessId ");
	
	//fetch list of events for current process
	public final static StringBuilder FETCH_EVENT_TYPE_SEQ = new StringBuilder("from OverdueProcessEvent ope where ope.overdueEventStatus = '10' ")
		.append(" and ope.id.overdueProcess = :overDueProcessId and ope.id.eventSequence > :currentEventSeqNum ");
	
	//fetch broker person id of overdue process account
	public final static StringBuilder FETCH_BROKER_PERSON = new StringBuilder("From AccountPerson ap where ap.id.account = :accountId ")
		.append(" AND ap.accountRelationshipType = :brokerAcctRelTypeAlgParm ");
	
	//fetch days after previous event
	public final static StringBuilder FETCH_DAYS_AFTER_PREV_EVENT = new StringBuilder("from OverdueProcessTemplateResponse odr, OverdueProcessEvent oe, OverdueProcess op ")
		.append("where op.id = :odProcId and oe.id.overdueProcess = op.id and oe.id.eventSequence = :odEvtSeq ")
		.append("and odr.id.overdueProcessTemplate = op.overdueProcessTemplate and odr.id.eventSequence = oe.id.eventSequence ");
	
	//fetch next bill date
	public final static StringBuilder FETCH_NEXT_BILL_DATE = new StringBuilder("from BillCycleSchedule bcs where bcs.id.billCycle = :accountBillCycle ")
		.append("and bcs.id.windowStartDate > :processDate ");
	
	//delete overdue process event
	public static final StringBuilder DELETE_OVERDUE_PROCESS = new StringBuilder("from OverdueProcessEvent ope where ope.id.overdueProcess = :overDueProcessId ")
		.append("and ope.id.eventSequence = :currentEventSeqNum");

	// public static final StringBuilder(" FROM BillableCharge bc,BilllableChargeCharacteristic bcc,ServiceAgreement sa,Membership mbr,PolicyPlan pp,Policy po,BusinessObjectStatusOption opt,PolicyPerson pper,AccountPerson aper,PolicyCharacteristic pch ")
	public static final StringBuilder STATE_OF_ISSUE_LIST = new StringBuilder(" FROM BillableCharge bc,ServiceAgreement sa,ServiceAgreementCharacteristic sac,PolicyPlan pp,Policy po,BusinessObjectStatusOption opt,PolicyPerson pper,AccountPerson aper,PolicyCharacteristic pch ")

			.append(" WHERE sa.account = :account AND bc.serviceAgreement = sa.id AND bc.billableChargeStatus = :active ")

			// .append(" AND bc.startDate <= :latestBillDueDate AND bc.endDate >= :latestBillDueDate AND bcc.id.billableCharge=bc.id ")
			// .append(" AND bcc.id.characteristicType = :memberShipCharType AND mbr.id = bcc.searchCharacteristicValue AND pp.id=mbr.planId ")

			.append(" AND bc.startDate <= :latestBillDueDate AND bc.endDate >= :latestBillDueDate ")
			.append(" AND sac.id.serviceAgreement = sa.id ")
			.append(" AND sac.id.characteristicType = :policyPlanCharType ")
			.append(" AND sac.searchCharacteristicValue = pp.id ")

			.append(" AND pp.policy = po.id  AND opt.id.businessObjectStatus.id.businessObject = po.businessObject AND opt.id.businessObjectStatus.id.status = po.status ")
			.append(" AND opt.id.optionType = :policyStatusOptionType AND (opt.id.value = :policyStatusActiveOptionVal OR(opt.id.value = :policyStatusTerminatedOptionVal AND po.endDate > :processDate)) ")
			.append(" AND opt.id.sequence = (SELECT MAX(opt2.id.sequence) FROM BusinessObjectStatusOption opt2 ")
			.append(" WHERE opt2.id.businessObjectStatus.id.businessObject = opt.id.businessObjectStatus.id.businessObject AND opt2.id.businessObjectStatus.id.status = opt.id.businessObjectStatus.id.status ")
			.append(" AND opt2.id.optionType = opt.id.optionType ) AND pper.id.policy = po.id AND pper.isMainCustomer = 'Y' ")
			.append(" AND aper.id.person = pper.id.person AND aper.isMainCustomer = 'Y' AND aper.id.account = sa.account ")
			.append(" AND pch.id.policy = po.id AND pch.id.characteristicType = :stateOfIssueCharType AND pch.id.effectiveDate = (SELECT MAX(pch2.id.effectiveDate) FROM PolicyCharacteristic pch2 ")
			.append(" WHERE pch2.id.policy = po.id AND pch2.id.characteristicType = pch.id.characteristicType AND pch2.id.effectiveDate <= :latestBillDueDate) ");
	
	public static final StringBuilder LENIENT_GRACE_DAY_QUERY = new StringBuilder(" FROM CisDivisionCharacteristic divc WHERE  ")
			.append(" divc.id.characteristicType = :graceDaysCharType AND divc.id.effectiveDate = (SELECT max(divc2.id.effectiveDate) FROM CisDivisionCharacteristic divc2 ")
			.append(" WHERE divc2.id.division=divc.id.division and divc2.id.characteristicType=divc.id.characteristicType and divc2.id.effectiveDate <= :latestBillDueDate )");
			
			//.append(" AND divc.id.division in ( '");
	
}

