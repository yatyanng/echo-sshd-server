package com.example.sshd.test;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class SplitTest {

	@Test
	public void test() {
		String command = "echo hello | lscpu && uname ; whoami";
		String[] splited = StringUtils.split(command,";|&");
		System.out.println(Arrays.asList(splited));
		Assert.assertTrue(splited.length == 4);
	}

}
