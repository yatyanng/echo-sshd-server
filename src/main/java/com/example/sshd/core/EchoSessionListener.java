package com.example.sshd.core;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.sshd.common.Session;
import org.apache.sshd.common.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EchoSessionListener implements SessionListener {

	private static final Logger logger = LoggerFactory.getLogger(EchoSessionListener.class);

	@Autowired
	Map<String, Session> remoteSessionMapping;

	@Override
	public void sessionCreated(Session session) {
		logger.info("sessionCreated: {}", session);
		if (session.getIoSession().getRemoteAddress() instanceof InetSocketAddress) {
			InetSocketAddress remoteAddress = (InetSocketAddress) session.getIoSession().getRemoteAddress();
			String remoteIpAddress = remoteAddress.getAddress().getHostAddress();
			if (remoteSessionMapping.containsKey(remoteIpAddress)) {
				logger.info("kill old session: {} -> {}", remoteIpAddress, remoteSessionMapping.get(remoteIpAddress));
				remoteSessionMapping.get(remoteIpAddress).close(false);
			}
			logger.info("new session: {} -> {}", remoteIpAddress, session);
			remoteSessionMapping.put(remoteIpAddress, session);
		}
	}

	@Override
	public void sessionEvent(Session session, Event event) {
		logger.info("sessionEvent: {}, event: {}", session, event);
	}

	@Override
	public void sessionClosed(Session session) {
		logger.info("sessionClosed: {}", session);
		if (session.getIoSession().getRemoteAddress() instanceof InetSocketAddress) {
			InetSocketAddress remoteAddress = (InetSocketAddress) session.getIoSession().getRemoteAddress();
			String remoteIpAddress = remoteAddress.getAddress().getHostAddress();
			logger.info("removing session: {} -> {}", remoteIpAddress, remoteSessionMapping.get(remoteIpAddress));
			remoteSessionMapping.remove(remoteIpAddress);
		}
	}

}
