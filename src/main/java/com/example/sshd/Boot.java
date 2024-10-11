package com.example.sshd;

import org.apache.sshd.SshServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Boot {

	private static final Logger logger = LoggerFactory.getLogger(Boot.class);

	@Autowired
	protected SshServer sshd;

	public static void main(String[] args) throws Exception {
		String configDirectory = "conf";
		if (args.length > 0) {
			configDirectory = args[0];
		}
		logger.info("config directory: {}", configDirectory);

		if (new java.io.File(configDirectory).exists() && new java.io.File(configDirectory).isDirectory()) {
			System.setProperty("spring.config.location", configDirectory + "/springboot.yml");
			System.setProperty("logging.config", configDirectory + "/log4j2.xml");
		}
		SpringApplication.run(Boot.class, args);
	} 
}
