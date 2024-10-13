package com.example.sshd.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.reactor.IOReactorConfig;
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
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public Map<String, String> ipInfoMapping() {
		return Collections.synchronizedMap(new HashMap<>());
	}

	@Bean
	public CloseableHttpAsyncClient asyncClient() {
		final IOReactorConfig ioReactorConfig = IOReactorConfig.custom().build();
		final CloseableHttpAsyncClient client = HttpAsyncClients.custom().setIOReactorConfig(ioReactorConfig).build();
		client.start();
		return client;
	}
}
