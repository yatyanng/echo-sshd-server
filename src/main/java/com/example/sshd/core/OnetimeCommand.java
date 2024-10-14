package com.example.sshd.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.example.sshd.util.ReplyUtil;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OnetimeCommand implements Command, SessionAware {
	
	@Autowired
	ReplyUtil replyUtil;

	private InputStream in;
	private OutputStream out;
	private OutputStream err;
	private ExitCallback callback;
	private Environment environment;
	private String command;
	private ServerSession session;

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
		replyUtil.replyToCommand(command, out, "", session);
		out.flush();
		callback.onExit(0);
	}

	@Override
	public void destroy() {
	}

	public ExitCallback getCallback() {
		return callback;
	}

	@Override
	public void setSession(ServerSession session) {
		this.session = session;
	}
}
