package edu.ucdenver.ccp.nlp_eval_service.payload;

import java.math.BigDecimal;

import edu.ucdenver.ccp.nlp.evaluation.bossy2013.SlotErrorRate;
import edu.ucdenver.ccp.nlp_eval_service.controller.EvalController;
import lombok.Data;

/**
 * Response for the {@link EvalController} to store/return relevant metrics of
 * the Slot Error Rate
 */
@Data
public class EvalResponse {

	private BigDecimal matches;
	private int insertions;
	private int deletions;
	private int predictedCount;
	private int referenceCount;
	private BigDecimal slotErrorRate;
	private BigDecimal precision;
	private BigDecimal recall;
	private BigDecimal fScore;

	public EvalResponse(SlotErrorRate ser) {
		super();
		matches = ser.getMatches();
		insertions = ser.getInsertions();
		deletions = ser.getDeletions();
		predictedCount = ser.getPredictedCount();
		referenceCount = ser.getReferenceCount();
		slotErrorRate = ser.getSER();
		precision = ser.getPrecision();
		recall = ser.getRecall();
		fScore = ser.getFScore();
	}

}
