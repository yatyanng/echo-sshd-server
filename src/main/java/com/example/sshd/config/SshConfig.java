package com.example.sshd.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.example.sshd.core.EchoSessionListener;
import com.example.sshd.core.EchoShellFactory;
import com.example.sshd.core.OnetimeCommand;

@Configuration
public class SshConfig {

	private static final Logger logger = LoggerFactory.getLogger(SshConfig.class);

	@Value("${ssh-server.port}")
	private int port;

	@Value("${ssh-server.private-key.location}")
	private String pkLocation;

	@Value("${ssh-server.login.usernames:root}")
	private String[] usernames;

	@Value("${ssh-server.hash-replies.location}")
	private String hashReplies;

	@Value("${ssh-server.regex-mapping.location}")
	private String regexMapping;

	@Autowired
	ApplicationContext applicationContext;

	@Bean
	public SshServer sshd() throws IOException, NoSuchAlgorithmException {
		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(pkLocation).getPath(), "RSA", 2048));

		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(final String username, final String password, final ServerSession session) {
				logger.info("Login Attempt: username = {}, password = {}", username, password);
				return Arrays.asList(usernames).contains(username);
			}
		});
		sshd.setShellFactory(applicationContext.getBean(EchoShellFactory.class));
		sshd.setCommandFactory(command -> applicationContext.getBean(OnetimeCommand.class, command));

		sshd.start();
		sshd.getSessionFactory().addListener(applicationContext.getBean(EchoSessionListener.class));
		return sshd;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public Properties hashReplies() throws IOException {
		Properties prop = new Properties();
		File configFile = new File(hashReplies);
		FileInputStream stream = new FileInputStream(configFile);
		prop.load(stream);
		return prop;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public Properties regexMapping() throws IOException {
		Properties prop = new Properties();
		File configFile = new File(regexMapping);
		FileInputStream stream = new FileInputStream(configFile);
		prop.load(stream);
		return prop;
	}
}
