package com.example.sshd.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OnetimeCommand implements Command {

	private static final Logger logger = LoggerFactory.getLogger(OnetimeCommand.class);

	@Autowired
	Properties repliesProperties;

	private InputStream in;
	private OutputStream out;
	private OutputStream err;
	private ExitCallback callback;
	private Environment environment;
	private String command;

	public OnetimeCommand(String cmd) {
		command = cmd;
	}

	public InputStream getIn() {
		return in;
	}

	public OutputStream getOut() {
		return out;
	}

	public OutputStream getErr() {
		return err;
	}

	public Environment getEnvironment() {
		return environment;
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		environment = env;
		String cmdHash = DigestUtils.md5Hex(command).toUpperCase();
		logger.info("command = {}, cmdHash = {}", command, cmdHash);

		if (StringUtils.equals(command, "exit")) {
			logger.info("Exiting command detected: {}", command);
			out.write(("\r\nExiting...\r\n").getBytes());
		} else if (repliesProperties.containsKey(command)) {
			logger.info("Known command detected: {}", command);
			String reply = repliesProperties.getProperty(command).replace("\\r", "\r").replace("\\n", "\n")
					.replace("\\t", "\t");
			out.write(("\r\n" + reply + "\r\n").getBytes());
		} else if (repliesProperties.containsKey(cmdHash)) {
			logger.info("Known command-hash detected: {}", cmdHash);
			String reply = repliesProperties.getProperty(cmdHash).replace("\\r", "\r").replace("\\n", "\n")
					.replace("\\t", "\t");
			out.write(("\r\n" + reply + "\r\n").getBytes());
		} else {
			logger.info("Command not found: {}", command);
			out.write(("\r\nCommand '" + command + "' not found. Try 'exit'.\r\n").getBytes());
		}
		out.flush();
		callback.onExit(0);
	}

	@Override
	public void destroy() {
	}

	public ExitCallback getCallback() {
		return callback;
	}
}
