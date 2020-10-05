/*
 ********************************************************************************************************************************************** 
 * PROGRAM DESCRIPTION:
 * 
 * This algorithm highlights Active Delinquency Processes associated with the Person in context.
 **********************************************************************************************************************************************
 * CHANGE HISTORY:
 * 
 * Date:       by:          Reason:
 * 2020-06-01  MugdhaP      Initial Version ANTHM-404 Installation Option-Delinquency Control Central Alert algorithm 
 **********************************************************************************************************************************************
 */

package com.splwg.cm.domain.delinquency.common.installation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.lookup.BusinessObjectSystemEventLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.algorithm.AlgorithmComponentCache;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.businessObject.BusinessObjectAlgorithm;
import com.splwg.base.domain.common.businessObject.BusinessObjectAlgorithms;
import com.splwg.base.domain.common.businessObject.BusinessObjectInfoAlgorithmSpot;
import com.splwg.base.domain.common.message.Message_Id;
import com.splwg.base.domain.common.navigationOption.NavigationOption;
import com.splwg.base.domain.database.field.Field;
import com.splwg.ccb.domain.common.installation.ControlCentralAlert;
import com.splwg.ccb.domain.common.installation.InstallationControlCentralAlertAlgorithmSpot;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.ccb.domain.customerinfo.person.Person;
import com.splwg.ccb.domain.customerinfo.premise.Premise;
import com.splwg.cm.domain.delinquency.collection.CmDelinquencyProcess_Id;
import com.splwg.cm.domain.delinquency.messageRepository.MessageRepository;
import com.splwg.shared.common.ServerMessage;

/**
 * @author MugdhaP
 *
 @AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (entityName = navigationOption, name = delinquencyProcesPageNavigationOpt, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = field, name = tooltipLabelForGoToProcess, type = entity)
 *            , @AlgorithmSoftParameter (entityName = navigationOption, name = delinquencyProcessSearchPageNavOpt, required = true, type = entity)
 *            , @AlgorithmSoftParameter (entityName = field, name = toolTipLabelForGoToSearch, type = entity)
 *            , @AlgorithmSoftParameter (entityName = messageCategory, name = messageCategory, required = true, type = entity)
 *            , @AlgorithmSoftParameter (name = messageNumber, required = true, type = integer)})
 */
public class CmHighlightActiveDelinquencyProcessAlgComp_Impl 
extends CmHighlightActiveDelinquencyProcessAlgComp_Gen 
implements InstallationControlCentralAlertAlgorithmSpot {

	private Person person;
	List<ControlCentralAlert> controlCentralAlerts = new ArrayList<ControlCentralAlert>();

	@Override
	public void invoke() {

		int counter = 0;
		String delinquencyProcessIdString;
		CmDelinquencyProcess_Id delinquencyProcId = null;
		int delinquencyCnt;
		String alertMessage;

		// Get Delinquency Processes on Person
		List<SQLResultRow> delinquencyProcesses = getDelinquencyProcess();

		// Get total number of delinquency Process on Person
		delinquencyCnt = delinquencyProcesses.size();

		if (notNull(delinquencyProcesses))
		{
			// For each Delinquency Process
			Iterator<SQLResultRow> delinquencyProcessIterator = delinquencyProcesses.iterator();
			while (delinquencyProcessIterator.hasNext()) {
				SQLResultRow delinquencyProcess = (SQLResultRow) delinquencyProcessIterator.next();

				if (counter < 5)
				{
					counter++;
					delinquencyProcessIdString = delinquencyProcess.getString("CM_DELIN_PROC_ID");
					delinquencyProcId = new CmDelinquencyProcess_Id(delinquencyProcessIdString);

					BusinessObject businessObject = delinquencyProcId.getEntity().getBusinessObject();
					BusinessObjectInstance businessObjectInstance = BusinessObjectInstance.create(businessObject);
					businessObjectInstance.set("delinquencyProcessId", delinquencyProcId.getIdValue());
					businessObjectInstance = BusinessObjectDispatcher.read(businessObjectInstance);

					// Get the Delinquency Process' BO's Information algorithm
					BusinessObjectAlgorithms algorithms = businessObject.getAlgorithms();
					Iterator<BusinessObjectAlgorithm> iterator = algorithms.iterator();
					while (iterator.hasNext()) {
						BusinessObjectAlgorithm businessObjectAlgorithm = (BusinessObjectAlgorithm) iterator.next();
						if (businessObjectAlgorithm.fetchIdEvent().equals(BusinessObjectSystemEventLookup.constants.INFO)) {
							BusinessObjectInfoAlgorithmSpot algorithmComp = AlgorithmComponentCache.getAlgorithmComponent(businessObjectAlgorithm.getId().getAlgorithm().getId(), BusinessObjectInfoAlgorithmSpot.class);
							algorithmComp.setBusinessObject(businessObjectInstance);
							algorithmComp.invoke();
							alertMessage = algorithmComp.getInfo();
							createControlCentralAlert(alertMessage, delinquencyProcId,getTooltipLabelForGoToProcess(),getDelinquencyProcesPageNavigationOpt());
						}
						else
						{
							break;
						}
					}
				}
				else{
					break;
				}
			}

			if (delinquencyCnt > 5)
			{
				ServerMessage message = MessageRepository.createMessageForAlert(String.valueOf(delinquencyCnt - 5), getMessageNumber().intValue());
				alertMessage = message.getMessageText();
				createControlCentralAlert(alertMessage, null,getToolTipLabelForGoToSearch(),getDelinquencyProcessSearchPageNavOpt());
			}
		}

	}

	/**
	 * @param alertMessage
	 * @param delinquencyProcess_I
	 * This method control creates alert
	 */
	private void createControlCentralAlert(String alertMessage, CmDelinquencyProcess_Id delinquencyProcId, Field toolTipLabel, NavigationOption navigationOpt) {

		ControlCentralAlert delProcAlert = ControlCentralAlert.Factory.newInstance();
		delProcAlert.setAlertText(alertMessage);
		
		// Set navigation Option from soft parameters
		delProcAlert.setNavigationOption(navigationOpt);
		if (notNull(toolTipLabel))
		{
			delProcAlert.setToolTip(toolTipLabel);
		}
		
		// Set alert key
		if(notNull(delinquencyProcId))
		delProcAlert.addAlertKey("CM_DELIN_PROC_ID", delinquencyProcId.getIdValue());
		
		// Add Alert to Control Central Alert's List - Hard Parameter.
		controlCentralAlerts.add(delProcAlert);

	}

	/**
	 * This method returns all the Delinquency Process currently on the Person.
	 */
	protected List<SQLResultRow> getDelinquencyProcess()
	{
		PreparedStatement preparedStatement = null;
		StringBuilder RETRIEVE_DELINQUENCY_PROCESS = null;
		List<SQLResultRow> sqlResultRowList = null;

		RETRIEVE_DELINQUENCY_PROCESS = new StringBuilder()
				.append(" SELECT DISTINCT (CM_DELIN_PROC_ID), CRE_DTTM FROM  ")
				.append(" ((SELECT DPO.CM_DELIN_PROC_ID, DP.CRE_DTTM  ")
				.append(" FROM CM_DELIN_PROC_REL_OBJ DPO, CM_DELIN_PROC DP,  ")
				.append(" F1_BUS_OBJ_STATUS BOS ")
				.append(" WHERE DPO.CM_DEL_REL_OBJ_TYPE_FLG = 'CMDL'  ")
				.append(" AND DPO.MAINT_OBJ_CD = 'PERSON' ")
				.append(" AND DPO.PK_VALUE1 = :person ")
				.append(" AND DP.CM_DELIN_PROC_ID = DPO.CM_DELIN_PROC_ID ")
				.append(" AND DP.BUS_OBJ_CD = BOS.BUS_OBJ_CD ")
				.append(" AND BOS.BO_STATUS_COND_FLG <> 'F1FL' ")
				.append(" AND DP.BO_STATUS_CD = BOS.BO_STATUS_CD) ")
				.append(" UNION ")
				.append(" (SELECT DPO.CM_DELIN_PROC_ID, DP.CRE_DTTM  ")
				.append(" FROM CM_DELIN_PROC_REL_OBJ DPO, CI_ACCT_PER AP, CM_DELIN_PROC DP,  ")
				.append(" F1_BUS_OBJ_STATUS BOS ")
				.append(" WHERE DPO.CM_DEL_REL_OBJ_TYPE_FLG = 'CMDL'  ")
				.append(" AND DPO.MAINT_OBJ_CD = 'ACCOUNT' ")
				.append(" AND DPO.PK_VALUE1 = AP.ACCT_ID ")
				.append(" AND AP.PER_ID = :person  ")
				.append(" AND AP.MAIN_CUST_SW = 'Y' ")
				.append(" AND DP.BUS_OBJ_CD = BOS.BUS_OBJ_CD ")
				.append(" AND DP.BO_STATUS_CD = BOS.BO_STATUS_CD ")
				.append(" AND DP.CM_DELIN_PROC_ID = DPO.CM_DELIN_PROC_ID ")
				.append(" AND BOS.BO_STATUS_COND_FLG <> 'F1FL')) ")
				.append(" ORDER BY CRE_DTTM DESC ");

		preparedStatement = createPreparedStatement(RETRIEVE_DELINQUENCY_PROCESS.toString(), "CmHighlightActiveDelinquencyProcessAlgComp_Impl");

		preparedStatement.bindId("person", person.getId());

		if (notNull(preparedStatement))
			sqlResultRowList = preparedStatement.list();

		preparedStatement.close();
		return sqlResultRowList;

	}

	/**
	 * Performs parameter validation.
	 * 
	 * @param forAlgorithmValidation
	 */
	protected void extraSoftParameterValidations(boolean forAlgorithmValidation) {

		// Check that the Message Category parameter value and Message Number
		// parameter value combination is valid
		Message_Id messageId = new Message_Id(getMessageCategory(), getMessageNumber());
		if (isNull(messageId.getEntity()))
		{
			addError(MessageRepository.invalidMessageCategoryMessageNumberCombination(getAlgorithm().fetchLanguageDescription(), getAlgorithm().getAlgorithmType().getParameterAt(2).fetchLanguageParameterLabel(), getMessageCategory().fetchLanguageDescription(), String.valueOf(getMessageNumber())));
		}
	}

	@Override
	public List<ControlCentralAlert> getAlerts() {
		return controlCentralAlerts;
	}

	@Override
	public void setAccount(Account arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxSize(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPerson(Person arg0) {
		this.person = arg0;

	}

	@Override
	public void setPremise(Premise arg0) {
		// TODO Auto-generated method stub

	}

}