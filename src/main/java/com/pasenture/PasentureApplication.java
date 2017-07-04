package com.pasenture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableJpaRepositories
@SpringBootApplication
public class PasentureApplication extends SpringBootServletInitializer{

	@Override
	protected SpringApplicationBuilder configure (SpringApplicationBuilder applicationBuilder) {

		return applicationBuilder.sources(PasentureApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(PasentureApplication.class, args);
	}
}
