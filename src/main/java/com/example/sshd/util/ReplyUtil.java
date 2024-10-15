package com.example.sshd.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sshd.server.session.ServerSession;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReplyUtil {

	private static final Logger logger = LoggerFactory.getLogger(ReplyUtil.class);
	private static final Logger notFoundLogger = LoggerFactory.getLogger("not_found");

	@Autowired
	Properties hashReplies;

	@Autowired
	Properties regexMapping;

	@Autowired
	Map<String, String> ipInfoMapping;

	public boolean replyToCommand(String command, OutputStream out, String prompt, ServerSession session)
			throws IOException {
		String remoteIpAddress = "";
		String cmdHash = DigestUtils.md5Hex(command.trim()).toUpperCase();
		if (session.getIoSession().getRemoteAddress() instanceof InetSocketAddress) {
			InetSocketAddress remoteAddress = (InetSocketAddress) session.getIoSession().getRemoteAddress();
			remoteIpAddress = remoteAddress.getAddress().getHostAddress();
		} else {
			remoteIpAddress = session.getIoSession().getRemoteAddress().toString();
		}

		if (StringUtils.equals(command.trim(), "about")) {
			logger.info("[{}] {} About command detected: {}", remoteIpAddress, cmdHash, command.trim());
			out.write(String.format("\r\n%s\r\n%s", ipInfoMapping.get(remoteIpAddress), prompt).getBytes());
		} else if (StringUtils.equals(command.trim(), "exit")) {
			logger.info("[{}] {} Exiting command detected: {}", remoteIpAddress, cmdHash, command.trim());
			out.write(String.format("\r\nExiting...\r\n%s", prompt).getBytes());
			return false;
		} else if (hashReplies.containsKey(command.trim())) {
			logger.info("[{}] {} Known command detected: {}", remoteIpAddress, cmdHash, command.trim());
			String reply = hashReplies.getProperty(command.trim()).replace("\\r", "\r").replace("\\n", "\n")
					.replace("\\t", "\t");
			out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
		} else if (hashReplies.containsKey(cmdHash)) {
			logger.info("[{}] {} Known command-hash detected: {}", remoteIpAddress, cmdHash, command.trim());
			String reply = hashReplies.getProperty(cmdHash).replace("\\r", "\r").replace("\\n", "\n").replace("\\t",
					"\t");
			out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
		} else if (hashReplies.containsKey(String.format("base64(%s)", cmdHash))) {
			logger.info("[{}] {} Known base64-hash detected: {}", remoteIpAddress, cmdHash, command.trim());
			String reply = hashReplies.getProperty(String.format("base64(%s)", cmdHash));
			reply = new String(Base64.decode(reply));
			out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
		} else {
			Optional<Pair<String, String>> o = regexMapping.entrySet().stream()
					.filter(e -> command.trim().matches(((String) e.getKey())))
					.map(e -> Pair.of((String) e.getKey(), (String) e.getValue())).findAny();
			if (o.isPresent()) {
				logger.info("[{}] {} Known pattern detected: {} ({})", remoteIpAddress, cmdHash, command.trim(),
						o.get());
				String reply = hashReplies.getProperty(o.get().getRight(), "").replace("\\r", "\r").replace("\\n", "\n")
						.replace("\\t", "\t");
				out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
			} else {
				logger.info("[{}] {} Command not found: {}", remoteIpAddress, cmdHash, command.trim());
				notFoundLogger.info("[{}] {} Command not found: {}", remoteIpAddress, cmdHash, command.trim());
				out.write(String.format("\r\nCommand '%s' not found. Try 'exit'.\r\n%s", command.trim(), prompt)
						.getBytes());
			}
		}
		return true;
	}
}
