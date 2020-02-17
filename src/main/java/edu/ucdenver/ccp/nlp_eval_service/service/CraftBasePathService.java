package edu.ucdenver.ccp.nlp_eval_service.service;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ucdenver.ccp.nlp_eval_service.properties.StorageProperties;
import lombok.Getter;

/**
 * Allows configuration of the base path to the CRAFT distribution that will be
 * used by the eval service
 *
 */
@Service
public class CraftBasePathService {

	/**
	 * The base file path for the CRAFT distribution. This will be used to
	 * obtain the appropriate ontologies during evaluation. It is also assumed
	 * that the bionlp formatted gold standard concept annotation files have
	 * been generated and that they live in a directory called 'bionlp' that is
	 * next to its corresponding 'knowtator' directory.
	 */
	@Getter
	private final Path craftLocation;

	@Autowired
	public CraftBasePathService(StorageProperties storageProperties) {
		this.craftLocation = Paths.get(storageProperties.getCraftDir()).toAbsolutePath().normalize();
	}

}