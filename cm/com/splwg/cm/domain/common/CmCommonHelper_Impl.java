/* 
 **************************************************************************
 *           	     Confidentiality Information:
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
 * This common helper program is used by Invoice Conversion Interface.
 * 
 **************************************************************************
 * 
 * CHANGE HISTORY:
 *
 * Date:        by:        Reason:
 * YYYY-MM-DD   IN         Reason text. 
 * 2020-06-29   KChan      CB-159. Initial	
 **************************************************************************
 */

package com.splwg.cm.domain.common;

import com.splwg.base.api.GenericBusinessComponent;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.lookup.CharacteristicEntityLookup;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.domain.common.businessObject.BusinessObject;
import com.splwg.base.domain.common.characteristicType.CharacteristicEntity_Id;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;
import com.splwg.ccb.domain.admin.customerClass.CustomerClass_Id;
import com.splwg.ccb.domain.customerinfo.account.Account;
import com.splwg.cm.domain.common.CmCommonHelper;
import com.splwg.cm.domain.common.messageRepository.CmMessageRepository;

/**
 * @author RIA-Admin
 *
@BusinessComponent ()
 */
public class CmCommonHelper_Impl extends GenericBusinessComponent implements
		CmCommonHelper {
	
	private String saId = null;

	/**
	 * This method will fetch Contract Id using Extendable Lookup BO
	 * @return String
	 */	
	public String fetchSaIdUsingExtendableLookup(Account personAccountEntity,BusinessObject bo)
	{
		CustomerClass_Id custClId = personAccountEntity.getCustomerClass().getId();
		BusinessObjectInstance extandableBoInstance = BusinessObjectInstance.create(bo);
		extandableBoInstance.set("bo", bo.getId().getTrimmedValue());
		extandableBoInstance.set("lookupValue", custClId.getTrimmedValue());
		extandableBoInstance = BusinessObjectDispatcher.read(extandableBoInstance); 
		if(notNull(extandableBoInstance))
		{
			PreparedStatement fetchSAId = null;
			String acctId = personAccountEntity.getId().getIdValue();
			String saType = extandableBoInstance.getGroup("fileUploadDefaultConfigurations").getString("saType");
			
			final StringBuilder fetchSAIdQuery = new StringBuilder()
			.append("select SA_ID from ci_sa where ACCT_ID = :acctId and SA_TYPE_CD = :saType and SA_STATUS_FLG='20'");
			fetchSAId = createPreparedStatement(fetchSAIdQuery.toString(), "Fetch SA Id");
			fetchSAId.bindString("acctId", acctId, "ACCT_ID");
			fetchSAId.bindString("saType", saType, "SA_TYPE_CD");
			SQLResultRow serviceAgreement = fetchSAId.firstRow();
			fetchSAId.close();
			if(notNull(serviceAgreement))
			{
				saId = serviceAgreement.getString("SA_ID");
			}
		}
		else
		{
			addError(CmMessageRepository.missingCustomerClassOnBo(personAccountEntity.getCustomerClass().entityName(), bo.entityName()));
		}
		return saId;
	}
	
	/**
	 * This method will fetch Contract Id using price item code
	 * @return String
	 */
	
	public String fetchSaIdUsingPriceItemCode(Account personAccountEntity,String priceItemCode)
	{
		PreparedStatement fetchSAId = null;
		String acctId = personAccountEntity.getId().getIdValue();

		final StringBuilder fetchSAIdQuery = new StringBuilder()
		.append("select SA_ID from CI_SA saId,CI_PRICEITEM priceItem where ACCT_ID=:acctId ")
		.append("and saId.SA_TYPE_CD = priceItem.SA_TYPE_CD and priceItem.PRICEITEM_CD=:priceItemCd and SA_STATUS_FLG='20'");
		fetchSAId = createPreparedStatement(fetchSAIdQuery.toString(), "Fetch SA Id");
		fetchSAId.bindString("acctId", acctId, "ACCT_ID");
		fetchSAId.bindString("priceItemCd", priceItemCode, "PRICEITEM_CD");
		SQLResultRow serviceAgreement = fetchSAId.firstRow();
		fetchSAId.close();
		if(notNull(serviceAgreement))
		{
			saId = serviceAgreement.getString("SA_ID");
		}
		return saId;
	}
	

	/**
	 * This method will validate if the char type code against the entity
	 * @return Bool
	 */
	public Bool isCharTypeEntityValid(CharacteristicType charType, CharacteristicEntityLookup charEntLookup) 
	{
        CharacteristicEntity_Id characteristicEntityId = new CharacteristicEntity_Id(charType.getId(), charEntLookup);
        if(isNull(characteristicEntityId.getEntity())) 
        {
            return Bool.FALSE;
        }
        return Bool.TRUE;
    }
}
