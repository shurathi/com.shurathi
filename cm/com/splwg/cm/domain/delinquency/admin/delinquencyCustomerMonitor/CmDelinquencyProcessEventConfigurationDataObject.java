/*
 **************************************************************************

 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * This is data object for lockbox upload module Pay tender data set
 *                                                           
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   MugdhaP		ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************
 */

package com.splwg.cm.domain.delinquency.admin.delinquencyCustomerMonitor;

import java.math.BigInteger;

import com.splwg.base.api.datatypes.Date;
import com.splwg.base.domain.common.characteristicType.CharacteristicType;

public class CmDelinquencyProcessEventConfigurationDataObject {

	private String businessObjectStatus;
	private String daysToWaitSource;
	private String stateOfIssue;
	private int daysToWaitForNotification;
	private CharacteristicType daysToWaitForNotificationCharacteristicType;
	private String EOGDaysUsage;
	private boolean recalculateTriggerDate;
	private int triggerDateCushionDays;
	private boolean requiresBroker;
	private BigInteger sequence;
	private Date triggerDate;

	public CmDelinquencyProcessEventConfigurationDataObject() {
		daysToWaitForNotification = 0;
		triggerDateCushionDays = 0;
		sequence = BigInteger.ZERO;
		requiresBroker = false;
		recalculateTriggerDate = false;
	}

	/**
	 * @return the businessObjectStatus
	 */
	public String getBusinessObjectStatus() {
		return businessObjectStatus;
	}

	/**
	 * @param businessObjectStatus the businessObjectStatus to set
	 */
	public void setBusinessObjectStatus(String businessObjectStatus) {
		this.businessObjectStatus = businessObjectStatus;
	}

	/**
	 * @return the daysToWaitSource
	 */
	public String getDaysToWaitSource() {
		return daysToWaitSource;
	}

	/**
	 * @param daysToWaitSource the daysToWaitSource to set
	 */
	public void setDaysToWaitSource(String daysToWaitSource) {
		this.daysToWaitSource = daysToWaitSource;
	}

	/**
	 * @return the stateOfIssue
	 */
	public String getStateOfIssue() {
		return stateOfIssue;
	}

	/**
	 * @param stateOfIssue the stateOfIssue to set
	 */
	public void setStateOfIssue(String stateOfIssue) {
		this.stateOfIssue = stateOfIssue;
	}

	/**
	 * @return the daysToWaitForNotification
	 */
	public int getDaysToWaitForNotification() {
		return daysToWaitForNotification;
	}

	/**
	 * @param daysToWaitForNotification the daysToWaitForNotification to set
	 */
	public void setDaysToWaitForNotification(int daysToWaitForNotification) {
		this.daysToWaitForNotification = daysToWaitForNotification;
	}

	/**
	 * @return the daysToWaitForNotificationCharacteristicType
	 */
	public CharacteristicType getDaysToWaitForNotificationCharacteristicType() {
		return daysToWaitForNotificationCharacteristicType;
	}

	/**
	 * @param daysToWaitForNotificationCharacteristicType the daysToWaitForNotificationCharacteristicType to set
	 */
	public void setDaysToWaitForNotificationCharacteristicType(CharacteristicType daysToWaitForNotificationCharacteristicType) {
		this.daysToWaitForNotificationCharacteristicType = daysToWaitForNotificationCharacteristicType;
	}

	/**
	 * @return the recalculateTriggerDate
	 */
	public boolean isRecalculateTriggerDate() {
		return recalculateTriggerDate;
	}

	/**
	 * @param recalculateTriggerDate the recalculateTriggerDate to set
	 */
	public void setRecalculateTriggerDate(boolean recalculateTriggerDate) {
		this.recalculateTriggerDate = recalculateTriggerDate;
	}

	/**
	 * @return the triggerDateCushionDays
	 */
	public int getTriggerDateCushionDays() {
		return triggerDateCushionDays;
	}

	/**
	 * @param triggerDateCushionDays the triggerDateCushionDays to set
	 */
	public void setTriggerDateCushionDays(int triggerDateCushionDays) {
		this.triggerDateCushionDays = triggerDateCushionDays;
	}

	/**
	 * @return the requiresBroker
	 */
	public boolean isRequiresBroker() {
		return requiresBroker;
	}

	/**
	 * @param requiresBroker the requiresBroker to set
	 */
	public void setRequiresBroker(boolean requiresBroker) {
		this.requiresBroker = requiresBroker;
	}

	/**
	 * @return the sequence
	 */
	public BigInteger getSequence() {
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(BigInteger sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the eOGDaysUsage
	 */
	public String getEOGDaysUsage() {
		return EOGDaysUsage;
	}

	/**
	 * @param eOGDaysUsage the eOGDaysUsage to set
	 */
	public void setEOGDaysUsage(String eOGDaysUsage) {
		EOGDaysUsage = eOGDaysUsage;
	}

	/**
	 * @return the triggerDate
	 */
	public Date getTriggerDate() {
		return triggerDate;
	}

	/**
	 * @param triggerDate the triggerDate to set
	 */
	public void setTriggerDate(Date triggerDate) {
		this.triggerDate = triggerDate;
	}

}
