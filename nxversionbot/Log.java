package NXVersionBot.nxversionbot;

import java.io.PrintWriter;
import java.io.StringWriter;

class Log {
	private final static long myUserID = 197085351263731713L; /*My user ID for the bot to ping me when something goes wrong.*/
	private final static long logChannel = 430535207649673227L; /*The log channel on the bot's official server. Not for general use.*/

	/*Logs to me*/
	static void log(String s) {
		System.out.println(s);
		if(s.length()<2000)
			Main.sendMessage(s, logChannel);
	}

	/*Logs a warning to me*/
	static void warn(String s) {
		System.out.println(s);
		Main.sendMessage("<@"+myUserID+">\n"+s, logChannel);
	}

	/*Logs an error to me*/
	static void error(Exception e, String comments) {
		String s = exToString(e);
		Log.log("<@"+myUserID+"> "+comments);
		Log.log("```"+s+"```");
	}

	/* Converts an exception to a string.*/
	private static String exToString(Exception e) {
		StringWriter s = new StringWriter();
		e.printStackTrace(new PrintWriter(s));
		return s.toString();
	}
}
