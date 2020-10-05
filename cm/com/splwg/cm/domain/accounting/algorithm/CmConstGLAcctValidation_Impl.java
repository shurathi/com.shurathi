package com.splwg.cm.domain.accounting.algorithm;

import java.math.BigInteger;

import com.splwg.base.api.datatypes.Date;
import com.splwg.ccb.domain.billing.trialBilling.TrialFinancialTransactionGeneralLedger_Id;
import com.splwg.ccb.domain.financial.financialTransaction.FinancialTransactionGeneralLedger_Id;
import com.splwg.ccb.domain.financial.financialTransaction.GLAccountValidationAlgorithmSpot;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
/**
 * @author IshitaGarg
 *
@AlgorithmComponent (softParameters = { @AlgorithmSoftParameter (name = numberOfSegments, type = integer)
 *            , @AlgorithmSoftParameter (name = segmentDelimiter, type = string)
 *            , @AlgorithmSoftParameter (name = expectedLength, type = integer)})
 */
public class CmConstGLAcctValidation_Impl extends CmConstGLAcctValidation_Gen implements
		GLAccountValidationAlgorithmSpot {

	String glAccountString;
	String validateSw = "Y";
	Date date;
	
	private static final Logger logger = LoggerFactory.getLogger(CmConstGLAcctValidation_Impl.class);
	
	@Override
	public void invoke() {
		
		if(glAccountString.trim().length() != getExpectedLength().intValue())
		{
			validateSw = "N";
			date = getSystemDateTime().getDate();
		}
		else
		{
			//glAccountString=glAccountString.replace('.','-');
			//String[] segmentArray = glAccountString.split(getSegmentDelimiter());
			
			String[] segmentArray;
			 
			 if(isBlankOrNull(getSegmentDelimiter()))
			 {
				 segmentArray = glAccountString.split("\\.");
				 
			 }else
			 {
				 segmentArray = glAccountString.split(getSegmentDelimiter()); 
			 }
			
			 logger.debug(segmentArray);
			 
			 logger.debug(segmentArray.length+" "+getNumberOfSegments().intValue());
			 
			if(segmentArray.length != getNumberOfSegments().intValue())
			{
				 logger.debug("invalid");
				validateSw = "N";
			}
			date = getSystemDateTime().getDate();
		}
		logger.debug(validateSw);
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public String getValidateSw() {
		logger.debug("inside return "+validateSw);
		return validateSw;
	}

	@Override
	public void setDstId(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFTGLId(FinancialTransactionGeneralLedger_Id arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFTId(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGLAccount(String glAccountString) {
		this.glAccountString =  glAccountString;

	}

	@Override
	public void setGLSeqNum(BigInteger arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTFTGLId(TrialFinancialTransactionGeneralLedger_Id arg0) {
		// TODO Auto-generated method stub

	}

}
