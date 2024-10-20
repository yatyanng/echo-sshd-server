package com.example.sshd.core;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class EchoShellFactory implements Factory<Command> {

	@Autowired
	ApplicationContext applicationContext;

	@Override
	public Command create() {
		return (Command) applicationContext.getBean("echoShell");
	}

}