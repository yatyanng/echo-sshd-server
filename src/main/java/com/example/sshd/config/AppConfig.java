package com.example.sshd.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.sshd.common.Session;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public Map<String, Session> remoteSessionMapping() {
		return Collections.synchronizedMap(new HashMap<>());
	}
}
