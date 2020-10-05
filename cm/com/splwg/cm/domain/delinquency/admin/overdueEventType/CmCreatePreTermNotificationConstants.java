/*                                                               
 ********************************************************************************************           
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *
 *Constants file for broker information Pre termination and Termination Algorithm 
 *                                                      
 *********************************************************************************************
 * CHANGE HISTORY:                                                
 *                                                                                                 
 * YYYY-MM-DD  	IN     		Reason text.                                
 * 2020-05-06   VINODW		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework          
 ***********************************************************************************************
 */
package com.splwg.cm.domain.delinquency.admin.overdueEventType;

public class CmCreatePreTermNotificationConstants {

	public final static String NOTIFICATION_EXTERNAL_ID = "notificationExternalId";
	public final static String OUT_BOUND_MSG_TYPE = "outBoundMessageType";
	public final static String OUT_BOUND_MSG_ID = "outboundMessageId";
	public final static String PROCESSING_METHOD = "processingMethod";
	public final static String BATCH_CNTRL = "batchControl";
	public final static String BATCH_NUMBER = "batchNumber";
	public final static String XML_SOURCE = "xmlSource";
	public final static String OVER_DUE_PROCESS_ID = "overdueProcessId";
	public final static String CREATE_DATE = "creationDateTime";
	public final static String CUSTOMER_ID = "customerId";
	public final static String POLICY_ID = "policyId";
	public final static String BROKER_ID = "brokerId";
	public final static String TERMINATE_DATE = "termDate";
	public final static String TERMINATED_POLICY = "TERMINATED";
	public final static String PENDING_TERMINATE_POLICY = "PENDTERMINTE";
	public final static String ACCOUNT = "Account";
	public final static String PERSON = "Person";
	public final static String MAIN_CUSTOMER = "Main Customer of Account";
	public final static String SOURCE_CUSTOMER = "Source Customer Id";
	public final static String POLICY_BO_END_DATE = "endDate";

	public final static String DELINQUENCY_PROCESS_ID = "delinquencyProcessId";

	public final static String CUSTOMER_ID_TYPE = "cusIdType";

	
	public static final StringBuilder BASE_POLICY_QUERY = new StringBuilder("FROM PolicyPerson PP WHERE PP.id.person=:mainCustomer ")
			.append(" AND PP.isMainCustomer='Y' AND EXISTS (FROM PolicyCharacteristic PC ")
			.append(" WHERE PC.id.characteristicType=:basePolicyCharType AND PC.searchCharacteristicValue='Y' AND PC.id.policy=PP.id.policy ")
			.append(" AND PC.id.effectiveDate=(SELECT MAX(PC2.id.effectiveDate) FROM PolicyCharacteristic PC2 ")
			.append(" WHERE PC2.id.policy=PC.id.policy AND PC2.id.characteristicType=:basePolicyCharType )) ");
	public final static StringBuilder BROKER_INFO_LIST = new StringBuilder(" FROM CmBrokerInformation brokerInfo WHERE brokerInfo.id.cmSourceCustomerId=:sourceCustomerId ");
	public final static StringBuilder ACTIVE_OVER_DUE_PROC = new StringBuilder("FROM OverdueProcess overDue WHERE overDue.overdueProcessStatus='10'")
			.append(" AND overDue.account=:account ");

	public final static StringBuilder EXISTING_OUT_MSG_QUERY = new StringBuilder(" FROM OutboundMessage outmsg WHERE outmsg.batchControlId =:batchControl AND outmsg.batchNumber = :batchNumber ")
			.append(" AND EXISTS ( FROM CmOutboundMessageCharacteristic outmsgChar WHERE outmsgChar.id.outboundMessage = outmsg.id ")
			.append(" AND outmsgChar.id.characteristicType =:brokerCharType AND outmsgChar.searchCharacteristicValue =:brokerId) ")
			.append(" AND EXISTS (FROM CmOutboundMessageCharacteristic outmsgChar2 WHERE outmsgChar2.id.outboundMessage = outmsg.id ")
			.append(" AND outmsgChar2.id.characteristicType =:customerCharType AND outmsgChar2.searchCharacteristicValue =:customerId)");

}

