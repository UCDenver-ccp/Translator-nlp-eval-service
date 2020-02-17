package edu.ucdenver.ccp.nlp_eval_service.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.evaluation.bossy2013.BoundaryMatchStrategy;
import edu.ucdenver.ccp.nlp.evaluation.bossy2013.SlotErrorRate;

public interface EvalService {

	public SlotErrorRate runEvaluation(File ontologyFile, File referenceDirectory,
			Map<String, TextDocument> sourceIdToTestDocMap, File txtDirectory,
			BoundaryMatchStrategy boundaryMatchStrategy) throws IOException;
}
