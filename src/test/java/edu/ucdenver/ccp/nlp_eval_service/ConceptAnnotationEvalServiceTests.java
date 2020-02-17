package edu.ucdenver.ccp.nlp_eval_service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.nlp_eval_service.controller.EvalController;
import edu.ucdenver.ccp.nlp_eval_service.service.CraftBasePathService;

@ComponentScan({ "edu.ucdenver.ccp.nlp_eval_service.service" })
@RunWith(SpringRunner.class)
@WebMvcTest(value = EvalController.class)
public class ConceptAnnotationEvalServiceTests {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File baseFilePath;

	private File craftBasePath;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CraftBasePathService craftBasePathService;

	@Before
	public void setup() throws IOException {
		baseFilePath = folder.newFolder();
		craftBasePath = folder.newFolder();
		File conceptBasePath = FileUtil.appendPathElementsToDirectory(craftBasePath, "concept-annotation", "CL", "CL");
		File refPath = new File(conceptBasePath, "bionlp");
		File txtPath = FileUtil.appendPathElementsToDirectory(craftBasePath, "articles", "txt");

		conceptBasePath.mkdirs();
		refPath.mkdirs();
		txtPath.mkdirs();

		/*
		 * copy sample ontology, txt, and annotation files from the classpath to
		 * the directories expected by the evaluation service
		 */

		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("CL.obo.zip"),
				new File(conceptBasePath, "CL.obo.zip"));

		for (String prefix : new String[] { "11319941", "11604102" }) {
			FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream(prefix + ".txt"),
					new File(txtPath, prefix + ".txt"));
			FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("ref/" + prefix + ".bionlp"),
					new File(refPath, prefix + ".bionlp"));
			FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("test/" + prefix + ".bionlp"),
					new File(baseFilePath, prefix + ".bionlp"));
		}

	}

	/**
	 * Test the ConceptAnnotationEvaluationService using 2 sample documents and
	 * accompanying annotations
	 * 
	 * @throws Exception
	 */
	@Test
	public void testComputeSER() throws Exception {
		Mockito.when(craftBasePathService.getCraftLocation()).thenReturn(craftBasePath.toPath());

		MockMultipartFile file_11319941 = new MockMultipartFile(EvalController.FILES_TO_EVAL_PARAM, "11319941.bionlp",
				"text/plain", new FileInputStream(new File(baseFilePath, "11319941.bionlp")));

		MockMultipartFile file_11604102 = new MockMultipartFile(EvalController.FILES_TO_EVAL_PARAM, "11604102.bionlp",
				"text/plain", new FileInputStream(new File(baseFilePath, "11604102.bionlp")));

		MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(EvalController.EVAL_POST_ENTRY)
				.file(file_11319941).file(file_11604102).param(EvalController.ONTOLOGY_KEY_PARAM, "CL")
				.param(EvalController.BOUNDARY_MATCH_STRATEGY_PARAM, "JACCARD");

		MvcResult result = mockMvc.perform(builder).andReturn();

		String expected = "{\"matches\":109.68553565269845,\"insertions\":6,\"deletions\":3,\"predictedCount\":131,\"referenceCount\":128,\"slotErrorRate\":0.1899567527,\"precision\":0.8372941653,\"recall\":0.8569182473,\"fscore\":0.8469925533}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

}
