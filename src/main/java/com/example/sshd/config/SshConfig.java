package com.example.sshd.config;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SshConfig {

	@Value("${ssh-server.port}")
	private int port;

	@Value("${ssh-server.private-key.location}")
	private String pkLocation;

	@Value("${ssh-server.root.username:root}")
	private String rootUsername;

	@Bean
	public SshServer sshd() throws IOException, NoSuchAlgorithmException {
		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(pkLocation).getPath(), "RSA", 2048));

		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(final String username, final String password, final ServerSession session) {
				return StringUtils.equals(username, rootUsername);
			}
		});
		sshd.setShellFactory(new EchoShellFactory());
		sshd.start();
		return sshd;
	}
}
