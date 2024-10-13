package com.example.sshd.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplyUtil {

	private static final Logger logger = LoggerFactory.getLogger(ReplyUtil.class);
	private static final Logger notFoundLogger = LoggerFactory.getLogger("not_found");

	public static boolean replyToCommand(String command, OutputStream out, String prompt, Properties hashReplies,
			Properties regexMapping) throws IOException {

		String cmdHash = DigestUtils.md5Hex(command.trim()).toUpperCase();

		if (StringUtils.equals(command.trim(), "exit")) {
			logger.info("[{}] Exiting command detected: {}", cmdHash, command.trim());
			out.write(("\r\nExiting...\r\n").getBytes());
			return false;
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
		} else {
			Optional<Pair<String, String>> o = regexMapping.entrySet().stream()
					.filter(e -> command.trim().matches(((String) e.getKey())))
					.map(e -> Pair.of((String) e.getKey(), (String) e.getValue())).findAny();
			if (o.isPresent()) {
				logger.info("[{}] Known pattern detected: {} ({})", cmdHash, command.trim(), o.get());
				String reply = hashReplies.getProperty(o.get().getRight(), "").replace("\\r", "\r").replace("\\n", "\n")
						.replace("\\t", "\t");
				out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
			} else {
				logger.info("[{}] Command not found: {}", cmdHash, command.trim());
				notFoundLogger.info("[{}] Command not found: {}", cmdHash, command.trim());
				out.write(String.format("\r\nCommand '%s' not found. Try 'exit'.\r\n%s", command.trim(), prompt)
						.getBytes());
			}
		}
		return true;
	}
}
