package com.seaway.game.common.utils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ScriptHelper {

	public static String execScript(String... args) throws Exception {

		ProcessBuilder pb = new ProcessBuilder(args);

		Process proc = pb.start();

		StringBuilder sb = new StringBuilder();
		StringBuilder error = new StringBuilder();

		try (BufferedReader is = new BufferedReader(new InputStreamReader(
				proc.getInputStream()))) {
			String line;
			while ((line = is.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		}

		try (BufferedReader is = new BufferedReader(new InputStreamReader(
				proc.getErrorStream()))) {
			String line;
			while ((line = is.readLine()) != null) {
				error.append(line);
				error.append("\n");
			}
		}

		proc.destroy();

		proc.waitFor();

		int exitValue = proc.exitValue();

		if (exitValue != 0) {
			String msg = sb.toString();
			if (error.length() > 0) {
				msg += "," + error.toString();
			}
			throw new RuntimeException(msg);
		}

		IOUtils.closeQuietly(proc.getOutputStream());
		IOUtils.closeQuietly(proc.getInputStream());
		IOUtils.closeQuietly(proc.getErrorStream());

		return sb.toString();
	}

	public static final String getScriptPath(String scriptName) {
		File f = new File(Constants.SCRIPT_FOLDER, scriptName);

		return f.getAbsolutePath();
	}

}
