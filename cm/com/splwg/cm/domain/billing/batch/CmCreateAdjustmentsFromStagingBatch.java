/* 
 **************************************************************************
 *                Confidentiality Information:
 *
 * This module is the confidential and proprietary information of
 * RIA Advisory; it is not to be copied, reproduced, or
 * transmitted in any form, by any means, in whole or in part,
 * nor is it to be used for any purpose other than that for which
 * it is expressly provided without the written permission of
 * RIA Advisory.
 *                 
 **************************************************************************
 * 
 * PROGRAM DESCRIPTION:
 * 
 * This batch process will retrieve PENDING and/or ERROR Adjustment 
 * Interface Staging records, depending on the batch parameter provided for,
 * and create the equivalent Adjustment record for each. Once the equivalent 
 * Adjustment has been successfully created, the batch will update the 
 * status of the Adjustment Interface Staging record to PROCESSED.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:               Reason:
 * YYYY-MM-DD   IN                Reason text. 
 * 2020-04-16   JFerna/JRaymu     CB-33. Initial.
 **************************************************************************
 */
package com.splwg.cm.domain.billing.batch;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.splwg.base.api.Query;
import com.splwg.base.api.batch.JobWork;
import com.splwg.base.api.batch.RunAbortedException;
import com.splwg.base.api.batch.StandardCommitStrategy;
import com.splwg.base.api.batch.ThreadAbortedException;
import com.splwg.base.api.batch.ThreadExecutionStrategy;
import com.splwg.base.api.batch.ThreadWorkUnit;
import com.splwg.base.api.batch.WorkUnitResult;
import com.splwg.base.api.businessObject.batch.MessageRepository;
import com.splwg.base.api.businessService.BusinessServiceDispatcher;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateFormat;
import com.splwg.base.api.datatypes.DateFormatParseException;
import com.splwg.base.api.lookup.CharacteristicTypeLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.base.domain.common.characteristicType.CharacteristicType_Id;
import com.splwg.base.support.context.FrameworkSession;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.ccb.api.lookup.ServiceAgreementStatusLookup;
import com.splwg.ccb.domain.adjustment.adjustment.Adjustment_Id;
import com.splwg.ccb.domain.admin.adjustmentType.AdjustmentType_Id;
import com.splwg.ccb.domain.admin.idType.accountIdType.AccountNumberType_Id;
import com.splwg.ccb.domain.admin.serviceAgreementType.ServiceAgreementType;
import com.splwg.ccb.domain.customerinfo.account.AccountNumber;
import com.splwg.ccb.domain.customerinfo.serviceAgreement.ServiceAgreement_Id;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.JSONException;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.JSONObject;
import com.splwg.ccb.domain.dataManagement.fileRequest.json.XML;
import com.splwg.cm.api.lookup.CmStagingStatusFlagLookupLookup;
import com.splwg.cm.domain.billing.customBusinessEntity.CmAdjIntStg;
import com.splwg.cm.domain.billing.customBusinessEntity.CmAdjIntStg_DTO;
import com.splwg.cm.domain.billing.customBusinessEntity.CmAdjIntStg_Id;
import com.splwg.cm.domain.common.customBusinessComponent.CmCommonAPI;
import com.splwg.cm.domain.common.customBusinessComponent.CmCommonAPI_Impl;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;
import com.splwg.cm.domain.common.messageRepository.CmMessages;
import com.splwg.shared.common.ApplicationError;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * @author JFerna/JRaymu
 *
@BatchJob (modules = {}, multiThreaded = true, rerunnable = true,
 *         softParameters = { @BatchJobSoftParameter (name = statusList, required = true, type = string)
 *            , @BatchJobSoftParameter (entityName = adjustmentType, name = adjustmentTypeCode, type = entity)
 *            , @BatchJobSoftParameter (name = MAX_ERRORS, type = integer)
 *            , @BatchJobSoftParameter (name = DIST-THD-POOL, type = string)})
 */
public class CmCreateAdjustmentsFromStagingBatch extends CmCreateAdjustmentsFromStagingBatch_Gen {
    // Constants
    private static final String CM_STG_STATUS_FLG = "CM_STG_STATUS_FLG";
    private static final String COMMA = ",";

    // Soft Parameter
    private String statusList;

    /**
     * This method validates the Batch Soft Parameters.
     * @param TRUE if it's a new run, FALSE otherwise.
     */
    @Override
    public void validateSoftParameters(boolean isNewRun) {
        statusList = getParameters().getStatusList().trim();
        for (String status : statusList.split(COMMA)) {
            if (!(CmStagingStatusFlagLookupLookup.constants.CM_PENDING.toString().equalsIgnoreCase(status.trim()) || CmStagingStatusFlagLookupLookup.constants.CM_ERROR
                    .toString().equalsIgnoreCase(status.trim()))) {
                addError(CmMessageRepository.getServerMessage(CmMessages.INVALID_LOOKUP_VALUE, status.trim(),
                        CM_STG_STATUS_FLG));
            }
        }
    }

    /**
     * This method retrieves adjustment interface staging records 
     * to process using status and adjustment type
     * 
     * @return JobWork
     */
    @Override
    public JobWork getJobWork() {
        ArrayList<ThreadWorkUnit> workUnits = new ArrayList<>();
        List<SQLResultRow> preparedStatementList = null;
        ThreadWorkUnit workUnit = null;
        CmAdjIntStg_Id adjIntStgId = null;

        String[] list = statusList.split(COMMA);
        AdjustmentType_Id adjTypeCd = null;
        if (notNull(getParameters().getAdjustmentTypeCode())) {
            adjTypeCd = getParameters().getAdjustmentTypeCode().getId();
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT CM_ADJ_INT_STG_ID ");
        stringBuilder.append("FROM CM_ADJ_INT_STG ");
        stringBuilder.append("WHERE CM_STG_STATUS_FLG IN (");

        for (int i = 0; i < list.length; i++) {
            if (i == 0) {
                stringBuilder.append(":status" + i);
            } else {
                stringBuilder.append(", :status" + i);
            }
        }

        stringBuilder.append(")");

        if (notNull(adjTypeCd)) {
            stringBuilder.append(" AND ADJ_TYPE_CD = :adjTypeCd ");
        }

        PreparedStatement getAdjIntStg = createPreparedStatement(stringBuilder.toString(),
                "Retrieve Adjustment Interface Staging");

        for (int i = 0; i < list.length; i++) {
            getAdjIntStg.bindString("status" + i, list[i].trim(), CM_STG_STATUS_FLG);
        }

        if (notNull(adjTypeCd)) {
            getAdjIntStg.bindId("adjTypeCd", adjTypeCd);
        }

        preparedStatementList = getAdjIntStg.list();
        getAdjIntStg.close();

        for (SQLResultRow result : preparedStatementList) {
            adjIntStgId = new CmAdjIntStg_Id(result.getString("CM_ADJ_INT_STG_ID"));
            workUnit = new ThreadWorkUnit();
            workUnit.setPrimaryId(adjIntStgId);
            workUnits.add(workUnit);
        }

        return createJobWorkForThreadWorkUnitList(workUnits);
    }

    /**
     * Returns the batch program's inner Thread Worker class.
     * @return Batch Thread Worker Class
     */
    public Class<CmCreateAdjustmentsFromStagingBatchWorker> getThreadWorkerClass() {
        return CmCreateAdjustmentsFromStagingBatchWorker.class;
    }

    /**
     * Process Adjustment Interface Staging Batch Worker class
     *
     */
    public static class CmCreateAdjustmentsFromStagingBatchWorker extends CmCreateAdjustmentsFromStagingBatchWorker_Gen {
        Logger logger = LoggerFactory.getLogger(CmCreateAdjustmentsFromStagingBatchWorker.class);
        // Work Parameters
        private int recordsInError;
        private int recordsProcessed;
        private Adjustment_Id adjId;
        private AdjustmentType_Id adjTypeCode;

        // Constants
        private static final String SAVE_POINT = "SAVE_POINT";
        private static final String BLANK_STRING = "";
        private static final String BS = "CM-CreateFrozenAdjustment";
        private static final DateFormat dateFormat = new DateFormat("yyyy-MM-dd");

        /**
         * Implemented method defining the Thread Execution Strategy
         * to be used by the batch process.
         * @return Thread Execution Strategy
         */
        public ThreadExecutionStrategy createExecutionStrategy() {
            return new StandardCommitStrategy(this);
        }

        /**
         * Main Processing Logic.
         * @param unit Adjustment Interface Staging record to be processed
         * @return workUnitResult
         */
        @Override
        public WorkUnitResult executeWorkUnitDetailedResult(ThreadWorkUnit unit) throws ThreadAbortedException,
                RunAbortedException {
            recordsProcessed = 0;
            recordsInError = 0;

            WorkUnitResult workUnitResult = new WorkUnitResult(true);
            Bool errorEncountered = Bool.FALSE;
            adjId = null;
            CmAdjIntStg_Id adjIntStgId = null;
            CmAdjIntStg adjIntStg = null;
            CmCommonAPI commonAPI = new CmCommonAPI_Impl();
            // Get Adjustment Interface Staging details
            adjIntStgId = (CmAdjIntStg_Id) unit.getPrimaryId();
            adjIntStg = adjIntStgId.getEntity();
            FrameworkSession session = (FrameworkSession) SessionHolder.getSession();
            session.setSavepoint(SAVE_POINT);

            // Convert JSON to XML and create frozen adjustment
            JSONObject json;
            try {
                // Convert JSON to XML
                String boDataFormatted = commonAPI.jsonFormatter(adjIntStg.getBusinessObjectDataArea());

                json = new JSONObject("{value:".concat(boDataFormatted).concat("}"));
                String xml = XML.toString(json).trim();

                // Validate Values and Create Frozen Adjustment
                validateCreateFrozenAdjustment(xml);

            } catch (JSONException e) {
                errorEncountered = Bool.TRUE;
                session.rollbackToSavepoint(SAVE_POINT);
                // Log error message in the batch run tree
                logError(CmMessageRepository.getServerMessage(CmMessages.BATCH_RUN_TREE_ERR, adjIntStg.getId().getIdValue(),
                        CmMessageRepository.getServerMessage(CmMessages.JSON_FORMAT_ERROR, BLANK_STRING).getMessageText()));
                setAdjIntStgStatus(adjIntStg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, CmMessageRepository
                        .getServerMessage(CmMessages.JSON_FORMAT_ERROR, BLANK_STRING).getMessageText());
            } catch (ApplicationError e) {
                errorEncountered = Bool.TRUE;
                session.rollbackToSavepoint(SAVE_POINT);
                // Log error message in the batch run tree
                logError(CmMessageRepository.getServerMessage(CmMessages.BATCH_RUN_TREE_ERR, adjIntStg.getId().getIdValue(),
                        e.getServerMessage().getMessageText()));
                setAdjIntStgStatus(adjIntStg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getServerMessage()
                        .getMessageText());
            } catch (Exception e) {
                errorEncountered = Bool.TRUE;
                session.rollbackToSavepoint(SAVE_POINT);
                // Log error message in the batch run tree
                logError(CmMessageRepository.getServerMessage(CmMessages.BATCH_RUN_TREE_ERR, adjIntStg.getId().getIdValue(),
                        e.getMessage()));
                setAdjIntStgStatus(adjIntStg, null, CmStagingStatusFlagLookupLookup.constants.CM_ERROR, e.getMessage());
            }
            if (notNull(adjIntStg)) {
                recordsProcessed++;
                if (errorEncountered.isTrue()) {
                    recordsInError++;
                } else {
                    if (notNull(adjId)) {
                        setAdjIntStgStatus(adjIntStg, adjId, CmStagingStatusFlagLookupLookup.constants.CM_PROCESSED, "");
                    }
                }
                saveChanges();
            }

            workUnitResult.setUnitsInError(recordsInError);
            workUnitResult.setUnitsProcessed(recordsProcessed);

            return workUnitResult;
        }

        /**
         * This method creates frozen adjustment.
         * @param adjIntStg
         * @param xml
         */
        private void validateCreateFrozenAdjustment(String xml) {

            Document boDataAreaXml = null;
            String adjTypeCodeString = null;
            String amount = null;
            String adjDtString = null;
            Date adjDt = null;
            try {
                boDataAreaXml = DocumentHelper.parseText(xml);
            } catch (DocumentException e1) {
                addError(MessageRepository.fieldInvalid("BO Data Area XML"));
            }

            // Retrieve Adjustment Type Code
            adjTypeCodeString = retrieveAndRaiseErrorIfMissing(boDataAreaXml.selectSingleNode("value/adjTypeCode"),
                    "Adjustment Type Code");
            adjTypeCode = new AdjustmentType_Id(adjTypeCodeString);
            if (isNull(adjTypeCode.getEntity())) {
                addError(MessageRepository.fieldInvalid("Adjustment Type Code"));
            }

            // Retrieve Amount
            amount = retrieveAndRaiseErrorIfMissing(boDataAreaXml.selectSingleNode("value/amount"), "Amount");

            // Retrieve Adjustment Date
            adjDtString = retrieveAndRaiseErrorIfMissing(boDataAreaXml.selectSingleNode("value/adjDt"), "Adjustment Date");
            try {
                adjDt = dateFormat.parseDate(adjDtString);
            } catch (DateFormatParseException e) {
                addError(MessageRepository.fieldInvalid("Adjustment Date"));
            }
            // Retrieve Identifier Type
            String identifierType = retrieveAndRaiseErrorIfMissing(
                    boDataAreaXml.selectSingleNode("value/contractDetails/identifierType"), "Account Identifier Type");
            AccountNumberType_Id acctNbrTypeId = new AccountNumberType_Id(identifierType);
            if (isNull(acctNbrTypeId.getEntity())) {
                addError(MessageRepository.fieldInvalid("Account Identifier Type"));
            }

            // Retrieve Identifier Value
            String identifierValue = retrieveAndRaiseErrorIfMissing(
                    boDataAreaXml.selectSingleNode("value/contractDetails/identifierValue"), "Account Identifier Value");

            // Retrieve Contract Type
            String contractType = isNull(boDataAreaXml.selectSingleNode("value/contractDetails/contractType")) ? BLANK_STRING
                    : boDataAreaXml.selectSingleNode("value/contractDetails/contractType").getText().trim();
            /*
             * Retrieve billable Contract associated to the Account having Identifier Type/Value from CI_ACCT_NBR
             */
            ServiceAgreement_Id saId = retrieveContractGivenAcctNbr(acctNbrTypeId, identifierValue, contractType);
            // Create and Freeze Adjustment
            adjId = createFreezeAdjustment(boDataAreaXml, saId, adjTypeCode, adjDt, amount);
        }

        private String retrieveAndRaiseErrorIfMissing(Node node, String nodeName) {
            String value = isNull(node) ? BLANK_STRING : node.getText().trim();

            if (isBlankOrNull(value)) {
                addError(MessageRepository.fieldMissing(nodeName));
            }
            return value;
        }

        /**
         * Create and Freeze Adjustment
         * @param saId
         * @param adjTypeId
         * @param adjustmentDate
         * @param amount
         * @return
         */
        private Adjustment_Id createFreezeAdjustment(Document boDataAreaXml, ServiceAgreement_Id saId,
                AdjustmentType_Id adjTypeId, Date adjustmentDate, String amount) {
            Document request = null;
            Document output = null;

            request = DocumentHelper.createDocument();
            Element bsElement = request.addElement(BS);
            Element inputGroup = bsElement.addElement("input");
            inputGroup.addElement("serviceAgreement").addText(saId.getIdValue());
            inputGroup.addElement("adjustmentType").addText(adjTypeId.getIdValue());
            inputGroup.addElement("adjustmentDate").addText(adjustmentDate.toString());
            inputGroup.addElement("adjustmentAmount").addText(amount);
            List<Node> characteristics = boDataAreaXml.selectNodes("value/characteristics");
            int index = 1;
            for (Node characteristic : characteristics) {
                Element chars = inputGroup.addElement("characteristics1");
                chars.addElement("seq").setText(String.valueOf(index));

                String charTypeString = retrieveAndRaiseErrorIfMissing(
                        characteristic.selectSingleNode("characteristicType"), "Characteristic Type");
                CharacteristicType charType = new CharacteristicType_Id(charTypeString).getEntity();
                if (isNull(charType)) {
                    addError(MessageRepository.fieldInvalid("Characteristic Type"));
                }
                CharacteristicTypeLookup charTypeLookup = charType.getCharacteristicType();
                String charVal = retrieveAndRaiseErrorIfMissing(characteristic.selectSingleNode("characteristicValue"),
                        "Characteristic Value");
                if (charTypeLookup.isForeignKeyValue()) {
                    chars.addElement("foreignKeyVal1").setText(charVal);
                } else if (charTypeLookup.isPredefinedValue()) {
                    chars.addElement("charVal").setText(charVal);
                } else {
                    chars.addElement("adhocCharVal").setText(charVal);
                }
                chars.addElement("charTypeCd").setText(charTypeString);

                index++;
            }
            logger.info("request " + request.asXML());
            output = BusinessServiceDispatcher.execute(request, BS);
            String adjustmentIdString = output.selectSingleNode(BS + "/output/adjustmentId").getStringValue().trim();
            return new Adjustment_Id(adjustmentIdString);
        }

        /**
         * Set Status
         * @param custIntStg
         * @param statusFlg
         * @param message
         */
        private void setAdjIntStgStatus(CmAdjIntStg adjStg, Adjustment_Id adjId, CmStagingStatusFlagLookupLookup statusFlg,
                String message) {
            CmAdjIntStg_DTO adjStgDto = adjStg.getDTO();

            if (notNull(adjId)) {
                adjStgDto.setAdjustmentId(adjId);
                adjStgDto.setAdjustmentType(adjTypeCode.getIdValue());
            }
            adjStgDto.setCmStatus(statusFlg.trimmedValue());
            adjStgDto.setMessageText(isBlankOrNull(message) ? BLANK_STRING : message);
            adjStgDto.setUpdateDateTime(getProcessDateTime());
            adjStgDto.setUser(getActiveContextUser().getId().getIdValue());
            adjStg.setDTO(adjStgDto);

        }

        /**
         * Retrieve Contract given the account number details
         * @param acctNbrType 
         * @param acctNbr
         * @param saTypeCd
         * @return Service Agreement ID
         */
        private ServiceAgreement_Id retrieveContractGivenAcctNbr(AccountNumberType_Id acctNbrType, String acctNbr,
                String saTypeCd) {
            StringBuilder queryString = new StringBuilder();
            queryString.append("from ServiceAgreement SA                                ");
            queryString.append("where exists (select ACCNBR.id.account.id from AccountNumber ACCNBR      ");
            queryString.append("where ACCNBR.id.accountIdentifierType.id = :acctNbrType ");
            queryString.append("and ACCNBR.accountNumber = :acctNbr                     ");
            queryString.append("and ACCNBR.id.account.id = SA.account.id)               ");
            if (!isBlankOrNull(saTypeCd)) {
                queryString.append("and SA.serviceAgreementType.id.saType = :saTypeCd          ");
            }
            queryString.append("and SA.status IN (:active, :pendingStop, :stopped) ");
            Query<ServiceAgreement_Id> query = createQuery(queryString.toString(), "retrieveContractGivenAcctNbr");
            query.bindId("acctNbrType", acctNbrType);
            query.bindStringProperty("acctNbr", AccountNumber.properties.accountNumber, acctNbr);
            query.bindLookup("active", ServiceAgreementStatusLookup.constants.ACTIVE);
            query.bindLookup("pendingStop", ServiceAgreementStatusLookup.constants.PENDING_STOP);
            query.bindLookup("stopped", ServiceAgreementStatusLookup.constants.STOPPED);
            if (!isBlankOrNull(saTypeCd)) {
                query.bindStringProperty("saTypeCd", ServiceAgreementType.properties.saType, saTypeCd);
            }
            query.addResult("SA.id", "SA.id");

            int queryListSize = query.list().size();

            if (queryListSize == 0) {
                // If Contract not found for Account Identifier/Value %1 %2, raise error
                addError(CmMessageRepository.getServerMessage(CmMessages.CONT_NOT_FOUND, acctNbrType.getIdValue(), acctNbr));
            } else if (queryListSize > 1) {
                // If multiple Contracts is found, raise error
                // Multiple Contracts in Active, Pending Stop or Stopped status exist for Account Identifier/Value %1 %2
                // combination
                addError(CmMessageRepository
                        .getServerMessage(CmMessages.MULT_CONT_ACTIVE, acctNbrType.getIdValue(), acctNbr));
            }
            return query.firstRow();
        }

    }

}
