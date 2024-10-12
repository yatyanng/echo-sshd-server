package com.example.sshd.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.example.sshd.util.ReplyUtil;

@Component
public class EchoShellFactory implements Factory<Command> {

	private static final Logger logger = LoggerFactory.getLogger(EchoShellFactory.class);

	@Autowired
	Properties repliesProperties;

	@Autowired
	ApplicationContext applicationContext;

	@Override
	public Command create() {
		return new EchoShell(repliesProperties);
	}

	public static class EchoShell implements Command, Runnable {

		private InputStream in;
		private OutputStream out;
		private OutputStream err;
		private ExitCallback callback;
		private Environment environment;
		private Thread thread;
		private Properties repliesProperties;

		public EchoShell(Properties repliesProperties) {
			this.repliesProperties = repliesProperties;
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
			thread = new Thread(this, UUID.randomUUID().toString());
			thread.start();
		}

		@Override
		public void destroy() {
			thread.interrupt();
		}

		@Override
		public void run() {
			String prompt = repliesProperties.getProperty("prompt","$ ");
			try {
				out.write(prompt.getBytes());
				out.flush();

				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				String command = "";

				while (!Thread.currentThread().isInterrupted()) {
					int s = r.read();
					if (s == 13 || s == 10) {
						if (!ReplyUtil.replyToCommand(repliesProperties, command, out, prompt)) {
							return;
						}
						command = "";
					} else {
						out.write(s);
						command += (char) s;
					}
					out.flush();
				}
			} catch (Exception e) {
				logger.error("run error!", e);
			} finally {
				callback.onExit(0);
			}
		}
	}
}