package edu.ucdenver.ccp.nlp_eval_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import edu.ucdenver.ccp.nlp_eval_service.properties.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({ StorageProperties.class })
public class NlpEvalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NlpEvalServiceApplication.class, args);
	}

}
