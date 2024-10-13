package com.example.sshd.core;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EchoSessionListener implements SessionListener {

	private static final Logger logger = LoggerFactory.getLogger(EchoSessionListener.class);
	private static final Logger ipInfoLogger = LoggerFactory.getLogger("ip_info");

	@Autowired
	Map<String, Session> remoteSessionMapping;

	@Autowired
	Map<String, String> ipInfoMapping;

	@Autowired
	CloseableHttpAsyncClient asyncClient;

	@Value("${ssh-server.ip-info-api.url:http://ip-api.com/json/%s}")
	private String ipInfoApiUrl;

	@Value("${ssh-server.ip-info-api.method:GET}")
	private String ipInfoApiMethod;

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
		if (session.getIoSession().getRemoteAddress() instanceof InetSocketAddress && event == Event.KexCompleted) {
			InetSocketAddress remoteAddress = (InetSocketAddress) session.getIoSession().getRemoteAddress();
			String remoteIpAddress = remoteAddress.getAddress().getHostAddress();
			if (!ipInfoMapping.containsKey(remoteIpAddress)) {
				asyncClient.execute(
						SimpleHttpRequest.create(ipInfoApiMethod, String.format(ipInfoApiUrl, remoteIpAddress)),
						new FutureCallback<SimpleHttpResponse>() {

							@Override
							public void completed(SimpleHttpResponse result) {
								logger.info("[{}] asyncClient.execute completed, result: {}, content-type: {}, body: {}",
										remoteIpAddress, result, result.getContentType(), result.getBodyText());
								ipInfoMapping.put(remoteIpAddress, result.getBodyText());
								ipInfoLogger.info("[{}] {}", remoteIpAddress, ipInfoMapping.get(remoteIpAddress));
							}

							@Override
							public void failed(Exception exception) {
								logger.info("[{}] asyncClient.execute failed, exception: {}", remoteIpAddress, exception);
							}

							@Override
							public void cancelled() {
								logger.info("[{}] asyncClient.execute cancelled.", remoteIpAddress);
							}
						});
			} else {
				ipInfoLogger.debug("[{}] {}", remoteIpAddress, ipInfoMapping.get(remoteIpAddress));
			}
		}
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
