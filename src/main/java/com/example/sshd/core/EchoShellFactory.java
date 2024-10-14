package com.example.sshd.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.sshd.util.ReplyUtil;

@Component
public class EchoShellFactory implements Factory<Command> {

	private static final Logger logger = LoggerFactory.getLogger(EchoShellFactory.class);

	@Autowired
	ReplyUtil replyUtil;

	@Autowired
	Properties hashReplies;

	@Autowired
	Map<String, String> ipInfoMapping;

	@Override
	public Command create() {
		return new EchoShell();
	}

	public class EchoShell implements Command, Runnable, SessionAware {

		protected InputStream in;
		protected OutputStream out;
		protected OutputStream err;
		protected ExitCallback callback;
		protected Environment environment;
		protected Thread thread;
		protected ServerSession session;

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
			
			if (session.getIoSession().getRemoteAddress() instanceof InetSocketAddress) {
				InetSocketAddress remoteAddress = (InetSocketAddress) session.getIoSession().getRemoteAddress();
				String remoteIpAddress = remoteAddress.getAddress().getHostAddress();
				thread = new Thread(this, remoteIpAddress);
			} else {
				thread = new Thread(this, session.getIoSession().getRemoteAddress().toString());
			}
			
			logger.info("environment: {}, thread-name: {}", environment.getEnv(), thread.getName());
			thread.start();
		}

		@Override
		public void destroy() {
			thread.interrupt();
		}

		@Override
		public void run() {
			String prompt = hashReplies.getProperty("prompt", "$ ");
			try {
				out.write(prompt.getBytes());
				out.flush();

				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				String command = "";

				while (!Thread.currentThread().isInterrupted()) {
					int s = r.read();
					if (s == 13 || s == 10) {
						if (!replyUtil.replyToCommand(command, out, prompt, session)) {
							out.flush();
							return;
						}
						command = "";
					} else {
						logger.trace("input character: {}", s);
						if (s == 127) {
							if (command.length() > 0) {
								command = command.substring(0, command.length() - 1);
								out.write(s);
							}
						} else if (s >= 32 && s < 127) {
							command += (char) s;
							out.write(s);
						}
					}
					out.flush();
				}
			} catch (Exception e) {
				logger.error("run error!", e);
			} finally {
				callback.onExit(0);
			}
		}

		@Override
		public void setSession(ServerSession session) {
			this.session = session;
		}
	}
}