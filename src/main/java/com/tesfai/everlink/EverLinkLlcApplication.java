package com.tesfai.everlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collections;

@SpringBootApplication
public class EverLinkLlcApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(EverLinkLlcApplication.class);
		// Let Heroku set the port
		String port = System.getenv("PORT");
		if (port != null) {
			app.setDefaultProperties(Collections.singletonMap("server.port", port));
		}
		app.run(args);
	}
}
