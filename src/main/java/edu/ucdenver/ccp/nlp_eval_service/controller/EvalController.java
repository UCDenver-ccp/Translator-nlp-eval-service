package edu.ucdenver.ccp.nlp_eval_service.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.file.conversion.bionlp.BioNLPDocumentReader;
import edu.ucdenver.ccp.nlp.evaluation.bossy2013.BoundaryMatchStrategy;
import edu.ucdenver.ccp.nlp.evaluation.bossy2013.SlotErrorRate;
import edu.ucdenver.ccp.nlp.uima.collections.craft.CraftOntology;
import edu.ucdenver.ccp.nlp_eval_service.payload.EvalResponse;
import edu.ucdenver.ccp.nlp_eval_service.service.ConceptAnnotationEvalService;
import edu.ucdenver.ccp.nlp_eval_service.service.CraftBasePathService;

/**
 * Note that this is designed to work with the CRAFT distribution directory
 * structure (versions >=3.1). See https://github.com/UCDenver-ccp/CRAFT.
 * 
 * The evaluation also assumes that the bionlp format files for all gold
 * standard concept annotations have been created and placed in a directory
 * called "bionlp" next to the respective "knowtator" directories, e.g.
 * CRAFT-4.0.1/concept-annotation/CL/CL+extensions/bionlp
 *
 */
@RestController
public class EvalController {

	public static final String EVAL_POST_ENTRY = "/eval";

	public static final String ONTOLOGY_KEY_PARAM = "ont";

	public static final String BOUNDARY_MATCH_STRATEGY_PARAM = "bms";

	public static final String FILES_TO_EVAL_PARAM = "files";

	private static final Logger logger = Logger.getLogger(EvalController.class.getName());

	@Autowired
	private ConceptAnnotationEvalService evalService;

	@Autowired
	private CraftBasePathService craftBasePathService;

	@PostMapping(EVAL_POST_ENTRY)
	public EvalResponse uploadMultipleFiles(@RequestParam(FILES_TO_EVAL_PARAM) MultipartFile[] files,
			@RequestParam(value = BOUNDARY_MATCH_STRATEGY_PARAM, defaultValue = "JACCARD") BoundaryMatchStrategy boundaryMatchStrategy,
			@RequestParam(value = ONTOLOGY_KEY_PARAM) CraftOntology ontologyKey) {

		Map<String, TextDocument> sourceIdToTestDocMap = new HashMap<String, TextDocument>();
		for (MultipartFile file : files) {
			try {
				importBioNlpDocument(file, sourceIdToTestDocMap);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error while processing BioNLP formatted file: " + file.getOriginalFilename(),
						e);
			}
		}

		return run_eval(ontologyKey, boundaryMatchStrategy, sourceIdToTestDocMap);
	}

	/**
	 * Parse the input BioNLP formatted file and add it to the
	 * sourceId-to-testDocument map
	 * 
	 * @param file
	 * @param sourceIdToTestDocMap
	 * @throws IOException
	 */
	private void importBioNlpDocument(MultipartFile file, Map<String, TextDocument> sourceIdToTestDocMap)
			throws IOException {
		String originalFilename = file.getOriginalFilename();
		String sourceId = originalFilename.substring(0, originalFilename.lastIndexOf("."));
		InputStream txtFileStream = getTextFileStream(sourceId);
		BioNLPDocumentReader docReader = new BioNLPDocumentReader();
		TextDocument testDocument = docReader.readDocument(sourceId, "unknown", file.getInputStream(), txtFileStream,
				CharacterEncoding.UTF_8);
		sourceIdToTestDocMap.put(sourceId, testDocument);
	}

	/**
	 * @return the path to the CRAFT text file directory
	 */
	private Path getTextFileDirectory() {
		return Paths.get(craftBasePathService.getCraftLocation().toString(), "articles", "txt");
	}

	/**
	 * @param sourceId
	 * @return InputStream for the text document corresponding to the input
	 *         source ID
	 * @throws FileNotFoundException
	 */
	private InputStream getTextFileStream(String sourceId) throws FileNotFoundException {
		File txtFile = new File(getTextFileDirectory().toFile(), sourceId + ".txt");
		return new FileInputStream(txtFile);
	}

	/**
	 * Run the evaluation
	 * 
	 * @param ontologyKey
	 * @param boundaryMatchStrategy
	 * @param sourceIdToTestDocMap
	 * @return
	 */
	private EvalResponse run_eval(CraftOntology ontologyKey, BoundaryMatchStrategy boundaryMatchStrategy,
			Map<String, TextDocument> sourceIdToTestDocMap) {
		/* get the name of the ontology file to use for the evaluation */
		String ontFileName = ontologyKey.oboFilePath().substring(ontologyKey.oboFilePath().lastIndexOf("/"));
		/*
		 * replace .gz with .zip b/c that's how the ontologies are distributed
		 * in the CRAFT distribution
		 */
		ontFileName = ontFileName.replace(".gz", ".zip");

		/*
		 * use the ontology abbreviation (key) to compose the directory where
		 * the ontology file and the gold standard files can be found
		 */
		String ontKey = ontologyKey.name();
		/* convert NCBI_TAXON to NCBITaxon if applicable */
		if (ontKey.contains("NCBI_TAXON")) {
			ontKey = ontKey.replace("NCBI_TAXON", "NCBITaxon");
		}
		/*
		 * the base directory level is the key without _EXT in all cases - this
		 * is according to how the concept annotations are stored in the CRAFT
		 * distribution, e.g.
		 * CRAFT-4.0.1/concept-annotation/GO_BP/GO_BP+extensions
		 */
		String baseOntKey = (ontKey.endsWith("_EXT") ? StringUtils.removeSuffix(ontKey, "_EXT") : ontKey);
		String secondLevelOntKey = (ontKey.endsWith("_EXT") ? StringUtils.removeSuffix(ontKey, "_EXT") + "+extensions"
				: ontKey);
		Path conceptPath = Paths.get(craftBasePathService.getCraftLocation().toString(), "concept-annotation",
				baseOntKey, secondLevelOntKey);

		Path ontologyPath = Paths.get(conceptPath.toString(), ontFileName);
		Path refStorageLocation = Paths.get(conceptPath.toString(), "bionlp");

		/*
		 * In the CRAFT distribution, the plain text for the articles are stored
		 * in the articles/txt directory, e.g. CRAFT-4.0.1/articles/txt
		 */
		Path txtStorageLocation = getTextFileDirectory();

		SlotErrorRate totalSER = null;
		try {
			totalSER = evalService.runEvaluation(ontologyPath.toFile(), refStorageLocation.toFile(),
					sourceIdToTestDocMap, txtStorageLocation.toFile(), boundaryMatchStrategy);
		} catch (IOException e) {
			throw new RuntimeException("Error during evaluation calculations.", e);
		}
		
		logger.log(Level.INFO, "===EVAL\t" + ontKey + "\t" + totalSER.toString() +"\t" + sourceIdToTestDocMap.keySet());
		
		return new EvalResponse(totalSER);

	}

}
