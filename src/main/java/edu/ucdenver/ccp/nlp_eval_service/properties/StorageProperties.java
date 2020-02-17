package edu.ucdenver.ccp.nlp_eval_service.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * This code was modified from
 * https://github.com/callicoder/spring-boot-file-upload-download-rest-api-example
 *
 */
@ConfigurationProperties(prefix = "file")
public class StorageProperties {
	@Getter
	@Setter
	private String craftDir;

}
