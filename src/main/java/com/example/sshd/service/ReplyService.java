package com.example.sshd.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sshd.server.session.ServerSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReplyService {

	private static final Logger logger = LoggerFactory.getLogger(ReplyService.class);
	private static final Logger notFoundLogger = LoggerFactory.getLogger("not_found");

	@Autowired
	Properties hashReplies;

	@Autowired
	Properties regexMapping;

	@Autowired
	Map<String, String> ipInfoMapping;

	public boolean replyToCommand(String command, OutputStream out, String prompt, ServerSession session)
			throws IOException {

		String cmdHash = DigestUtils.md5Hex(command.trim()).toUpperCase();
		if (StringUtils.equals(command.trim(), "about")) {
			logger.info("[{}] About command detected: {}", cmdHash, command.trim());
			out.write(String.format("\r\n%s\r\n%s", ipInfoMapping.get(Thread.currentThread().getName()), prompt)
					.getBytes());
		} else if (StringUtils.equals(command.trim(), "exit")) {
			logger.info("[{}] Exiting command detected: {}", cmdHash, command.trim());
			out.write(String.format("\r\nExiting...\r\n%s", prompt).getBytes());
			return true;
		} else if (hashReplies.containsKey(command.trim())) {
			logger.info("[{}] Known command detected: {}", cmdHash, command.trim());
			String reply = hashReplies.getProperty(command.trim()).replace("\\r", "\r").replace("\\n", "\n")
					.replace("\\t", "\t");
			out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
		} else if (hashReplies.containsKey(cmdHash)) {
			logger.info("[{}] Known command-hash detected: {}", cmdHash, command.trim());
			String reply = hashReplies.getProperty(cmdHash).replace("\\r", "\r").replace("\\n", "\n").replace("\\t",
					"\t");
			out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
		} else if (hashReplies.containsKey(String.format("base64(%s)", cmdHash))) {
			logger.info("[{}] Known base64-hash detected: {}", cmdHash, command.trim());
			String reply = hashReplies.getProperty(String.format("base64(%s)", cmdHash));
			reply = new String(Base64.decodeBase64(reply));
			out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
		} else {
			Optional<Pair<String, String>> o = regexMapping.entrySet().stream()
					.filter(e -> command.trim().matches(((String) e.getKey())))
					.map(e -> Pair.of((String) e.getKey(), (String) e.getValue())).findAny();
			if (o.isPresent()) {
				String reply = hashReplies.getProperty(o.get().getRight(), "").replace("\\r", "\r").replace("\\n", "\n")
						.replace("\\t", "\t");
				if (reply.isEmpty()) {
					logger.info("[{}] Execute cmd for real: {} ({})", cmdHash, command.trim(), o.get());
					CommandLine cmdLine = CommandLine.parse(command.trim());
					DefaultExecutor executor = DefaultExecutor.builder().get();
					ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
					PumpStreamHandler streamHandler = new PumpStreamHandler(tempOut);
					executor.setStreamHandler(streamHandler);
					int exitValue = executor.execute(cmdLine);
					logger.info("[{}] Result: {} ({})", cmdHash, command.trim(), exitValue);
					reply = new String(tempOut.toByteArray()).replace("\n", "\r\n");
					out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
				} else {
					logger.info("[{}] Known pattern detected: {} ({})", cmdHash, command.trim(), o.get());
					out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
				}
			} else {
				logger.info("[{}] Command not found: {}", cmdHash, command.trim());
				notFoundLogger.info("[{}] Command not found: {}", cmdHash, command.trim());
				out.write(String.format("\r\nCommand '%s' not found. Try 'exit'.\r\n%s", command.trim(), prompt)
						.getBytes());
			}
		}
		return false;
	}
}
