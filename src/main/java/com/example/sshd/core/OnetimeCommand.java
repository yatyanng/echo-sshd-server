package com.example.sshd.core;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OnetimeCommand extends EchoShell {

	private static final Logger logger = LoggerFactory.getLogger(OnetimeCommand.class);

	private String command;

	public OnetimeCommand(String cmd) {
		command = cmd;
	}

	@Override
	public void run() {
		try {
			Arrays.asList(command.split(";")).stream().forEach(cmd -> {
				try {
					replyUtil.replyToCommand(cmd.trim(), out, "", session);
					out.flush();
				} catch (Exception e) {
					logger.error("run error!", e);
				}
			});
		} catch (Exception e) {
			logger.error("run error!", e);
		} finally {
			callback.onExit(0);
		}
	}
}
