package edu.ucdenver.ccp.nlp_eval_service.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.springframework.stereotype.Service;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.file.conversion.bionlp.BioNLPDocumentReader;
import edu.ucdenver.ccp.nlp.evaluation.bossy2013.BossyMetric;
import edu.ucdenver.ccp.nlp.evaluation.bossy2013.BoundaryMatchStrategy;
import edu.ucdenver.ccp.nlp.evaluation.bossy2013.SlotErrorRate;

/**
 * A concept annotation evaluation service implemented using the Bossy et al.,
 * 2013 metric.
 *
 */
@Service
public class ConceptAnnotationEvalService implements EvalService {

	private static final Logger logger = Logger.getLogger(ConceptAnnotationEvalService.class.getName());
	private static final String BIONLP_SUFFIX = ".bionlp";

	@Override
	public SlotErrorRate runEvaluation(File ontologyFile, File referenceDirectory,
			Map<String, TextDocument> sourceIdToTestDocMap, File txtDirectory,
			BoundaryMatchStrategy boundaryMatchStrategy) throws IOException {

		BossyMetric bm = new BossyMetric(getInputStream(ontologyFile));

		SlotErrorRate totalSer = new SlotErrorRate(BigDecimal.valueOf(0), 0, 0, 0, 0);

		for (Entry<String, TextDocument> entry : sourceIdToTestDocMap.entrySet()) {
			String sourceId = entry.getKey();
			TextDocument testDocument = entry.getValue();
			File refAnnotFile = new File(referenceDirectory, sourceId + BIONLP_SUFFIX);
			File txtFile = new File(txtDirectory, sourceId + ".txt");

			/* all files must exist in order to properly evaluate */
			if (txtFile.exists() && refAnnotFile.exists()) {
				BioNLPDocumentReader docReader = new BioNLPDocumentReader();
				TextDocument refDocument = docReader.readDocument(sourceId, "unknown", refAnnotFile, txtFile,
						CharacterEncoding.UTF_8);

				SlotErrorRate ser = bm.evaluate(refDocument.getAnnotations(), testDocument.getAnnotations(),
						boundaryMatchStrategy);

				/* updated the cumulative SER with the SER for this document */
				totalSer.update(ser);
			} else {
				logger.log(Level.WARNING, "Unable to evaluate files for source id (" + sourceId
						+ ") as one or more files (text or reference annotation) are not present.");
			}
		}

		return totalSer;
	}

	/**
	 * Utility method for retrieving an {@link InputStream} from a possibly
	 * compressed file
	 * 
	 * @param inputFile
	 * @return an {@link InputStream} opened according to the suffix of the
	 *         input file, e.g. '.gz' will be a GZIPInputStream.
	 * @throws IOException
	 */
	private static InputStream getInputStream(File inputFile) throws IOException {
		if (inputFile.getName().endsWith(".obo")) {
			return new FileInputStream(inputFile);
		}
		if (inputFile.getName().endsWith(".gz")) {
			return new GZIPInputStream(new FileInputStream(inputFile));
		}
		if (inputFile.getName().endsWith(".zip")) {
			ZipFile zipFile = new ZipFile(inputFile);
			ZipEntry zipEntry = zipFile.entries().nextElement();
			String fileName = zipEntry.getName();
			Files.copy(zipFile.getInputStream(zipEntry), new File(inputFile.getParentFile(), fileName).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			zipFile.close();
			return new FileInputStream(new File(inputFile.getParentFile(), fileName));
		}
		throw new IllegalArgumentException("Unable to return input stream for file: " + inputFile.getAbsolutePath());
	}

}
