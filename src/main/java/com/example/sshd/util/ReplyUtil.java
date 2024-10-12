package com.example.sshd.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplyUtil {

	private static final Logger logger = LoggerFactory.getLogger(ReplyUtil.class);

	public static boolean replyToCommand(Properties repliesProperties, String command, OutputStream out, String prompt)
			throws IOException {

		String cmdHash = DigestUtils.md5Hex(command.trim()).toUpperCase();

		if (StringUtils.equals(command.trim(), "exit")) {
			logger.info("[{}] Exiting command detected: {}", cmdHash, command.trim());
			out.write(("\r\nExiting...\r\n").getBytes());
			return false;
		} else if (repliesProperties.containsKey(command.trim())) {
			logger.info("[{}] Known command detected: {}", cmdHash, command.trim());
			String reply = repliesProperties.getProperty(command.trim()).replace("\\r", "\r").replace("\\n", "\n")
					.replace("\\t", "\t");
			out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
		} else if (repliesProperties.containsKey(cmdHash)) {
			logger.info("[{}] Known command-hash detected: {}", cmdHash, command.trim());
			String reply = repliesProperties.getProperty(cmdHash).replace("\\r", "\r").replace("\\n", "\n")
					.replace("\\t", "\t");
			out.write(String.format("\r\n%s\r\n%s", reply, prompt).getBytes());
		} else {
			logger.info("[{}] Command not found: {}", cmdHash, command.trim());
			out.write(String.format("\r\nCommand '%s' not found. Try 'exit'.\r\n%s", command.trim(), prompt).getBytes());
		}
		return true;
	}
}
