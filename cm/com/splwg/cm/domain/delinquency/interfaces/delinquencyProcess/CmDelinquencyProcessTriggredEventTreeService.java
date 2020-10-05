/*
 **************************************************************************
 *                                                                
 * PROGRAM DESCRIPTION:                                           
 *                                                                
 * Input: Trigger Date and Status Update Date time
 * Output: Number of days difference and post string
 * This Business Service Calculates the Date difference in Trigger Date and Process Date Depends on Status update date time.  
 *                                                          
 **************************************************************************
 *                                                                
 * CHANGE HISTORY:                                                
 *                                                                
 * Date:       	by:    		Reason:                                     
 * YYYY-MM-DD  	IN     		Reason text.                                
 *           
 * 2020-05-06   MugdhaP		Initial version : ANTHM-340 CAB1-9462 Delinquency Framework
 **************************************************************************
 */
package com.splwg.cm.domain.delinquency.interfaces.delinquencyProcess;

import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateTime;
import com.splwg.base.api.service.DataElement;
import com.splwg.cm.domain.delinquency.collection.delinquencyProcess.algorithm.CmDelinquencyProcessConstant;
import com.splwg.shared.common.ApplicationError;

/**
 * @author MugdhaP
 *
 *
@QueryPage (program = CMGETTRIEVENTTREE, service = CMGETTRIEVENTTREE,
 *      body = @DataElement (contents = { @DataField (name = STATUS_UPD_DTTM)
 *                  , @DataField (name = TRIGGER_DT)
 *                  , @DataField (name = F1_INFORMATION)}),
 *      actions = { "change"},
 *      modules = { "demo"})
 */
public class CmDelinquencyProcessTriggredEventTreeService extends CmDelinquencyProcessTriggredEventTreeService_Gen {

	@Override
	protected void change(DataElement item) throws ApplicationError {

		DateTime statusUpdateDateTime;
		Date triggredDate;
		Long ageOfTriggredEvent;
		String triggredEventTree;
	
		statusUpdateDateTime = item.get(STRUCTURE.STATUS_UPD_DTTM);
		triggredDate = item.get(STRUCTURE.TRIGGER_DT);

		/*
		* Start Add - XCaraig HPB230 2020-03-12
		*/
		Date processDate;
		processDate = getProcessDateTime().getDate();
		triggredEventTree = CmDelinquencyProcessConstant.EMPTY;
		/*
		* End Add - XCaraig HPB230 2020-03-12
		*/

		if (notNull(statusUpdateDateTime))
		{
	
			/*
			* Start Add - XCaraig HPB230 2020-03-12
			*/	
			Date statusUpdateDate;
			statusUpdateDate = statusUpdateDateTime.getDate();	
			
			ageOfTriggredEvent = Math.abs(processDate.difference(statusUpdateDate).getTotalDays());
			
			if(processDate.isBefore(statusUpdateDate))
			{
				triggredEventTree = ageOfTriggredEvent.toString()	+ CmDelinquencyProcessConstant.DAYS_FROM_TODAY;
			}
			else
			{
				triggredEventTree = ageOfTriggredEvent.toString() + CmDelinquencyProcessConstant.DAYS_AGO;
			}
			/*
			* End Add - XCaraig HPB230 2020-03-12
			*/		
			
			/*
			* Start Delete - XCaraig HPB230 2020-03-12
			*/
			/*
			ageOfTriggredEvent = getProcessDateTime().getDate().difference(statusUpdateDateTime).getTotalDays();
			
			if(ageOfTriggredEvent < 0)
			{
				ageOfTriggredEvent=Math.abs(ageOfTriggredEvent);
			}
			triggredEventTree = ageOfTriggredEvent.toString() + CmDelinquencyProcessConstant.DAYS_AGO;
			*/
			/*
			* End Delete - XCaraig HPB230 2020-03-12
			*/					
		}
		/*
		* Start Add - XCaraig HPB230 2020-03-12
		*/		
		else if (notNull(triggredDate))
		{
			/*
			* Start Add - XCaraig HPB230 2020-03-12
			*/		
			ageOfTriggredEvent = Math.abs(processDate.difference(triggredDate).getTotalDays());
			
			if(processDate.isBefore(triggredDate))
			{
				triggredEventTree = ageOfTriggredEvent.toString()	+ CmDelinquencyProcessConstant.DAYS_FROM_TODAY;
			}
			else
			{
				triggredEventTree = ageOfTriggredEvent.toString() + CmDelinquencyProcessConstant.DAYS_AGO;
			}
			/*
			* End Add - XCaraig HPB230 2020-03-12
			*/				
			
		}
		/*
		* End Add - XCaraig HPB230 2020-03-12
		*/		
	
	
		/*
		* Start Delete - XCaraig HPB230 2020-03-12
		*/	
		/*
		else
		{
			if (triggredDate.isAfter(getProcessDateTime().getDate()))
			{
				ageOfTriggredEvent = triggredDate.difference(getProcessDateTime().getDate()).getTotalDays();
				if(ageOfTriggredEvent < 0)
				{
					ageOfTriggredEvent=Math.abs(ageOfTriggredEvent);
					triggredEventTree = ageOfTriggredEvent.toString() + CmDelinquencyProcessConstant.DAYS_AGO;
				}
				else
				{
					triggredEventTree = ageOfTriggredEvent.toString() + CmDelinquencyProcessConstant.DAYS_FROM_TODAY;
				}
				

			}
			else
			{
				ageOfTriggredEvent = getProcessDateTime().getDate().difference(triggredDate).getTotalDays();
								

				triggredEventTree = ageOfTriggredEvent.toString() + CmDelinquencyProcessConstant.DAYS_AGO;
			}
		}
		
		*/
		/*
		* End Delete - XCaraig HPB230 2020-03-12
		*/		
		
		item.put(CmDelinquencyProcessTriggredEventTreeService_Gen.STRUCTURE.F1_INFORMATION, triggredEventTree);
		setOverrideResultForChange(item);
	}
}
