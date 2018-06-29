package NXVersionBot.nxversionbot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Pattern;

class UpdateUtils {
	private final static String nintendoUpdateLog = "https://en-americas-support.nintendo.com/app/answers/detail/a_id/22525/p/897/"; /*The official Switch changelog.*/
	private final static String unofficialUpdateLog = "http://switchbrew.org/index.php?title="; /*The unofficial Switch changelog.*/
	private static HashMap<String, String> nxMap = new HashMap<>();

	/*Grabs update information from the server*/
	static String getUpdateInformation(String update) {
		update = formatUpdateVersion(update);
		if (update == null) {
			return null;
		}
		return nxMap.getOrDefault(update, null);
	}

	/*Gets the unofficial changelog link*/
	static String getLink(String update) {
		update = formatUpdateVersion(update);
		if (update == null) {
			return null;
		}
		if(update.equals("4.2.0"))
			return "http://tiny.cc/klg6sy";
		return unofficialUpdateLog + update;
	}

	/*
	Format the version correctly.
	Does feel overcomplicated, but makes it more resilient to typos.
	Assuming they never do something like 10.10.0, this will continue to work.
	*/
	static String formatUpdateVersion(String update) {
		if (update.isEmpty())
			return null;

		StringBuilder formattedUpdate = new StringBuilder();
		update = update.replace(" ", "");
		update = update.replace(".", "");

		/*Checking if it's a real number, we don't actually need the value of the parse.*/
		try {
			Integer.parseInt(update);
		} catch (Exception e) {
			return null;
		}

		/*Make it at least three numbers long. 5 -> 500*/
		if (update.length() < 3) {
			formattedUpdate.append(update);
			while (formattedUpdate.length() < 3)
				formattedUpdate.append("0");
			update = formattedUpdate.toString();
		}

		/*
		Put back the dots. Since the major update is the one that will go above 9 (like 10.0.0), we reverse it, split 3 times,
		put periods in between the splits, and reverse it again.
		*/
		String[] vars = new StringBuilder(update).reverse().toString().split("", 3);
		update = vars[0]+"."+vars[1]+"."+vars[2];
		return new StringBuilder(update).reverse().toString();
	}

	/*Returns formatted site*/
	private static String getFormattedNintendoSite() {
		String info = null;
		Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

		/*meme*/
		nxMap.put("4.2.0", "Users can now select Snoop Dogg as an icon for their user\nGeneral system stability improvements to enhance the user's experience");

		/*Downloads web page into a string to be parsed*/
		URLConnection c=null;
		try {
			c = new URL(nintendoUpdateLog).openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(c==null)
			return null;

		try (InputStream is=c.getInputStream(); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			byte[] b = new byte[2048];
			int length;
			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
			info = os.toString("UTF-8");
		} catch (Exception e) {
			Log.error(e, "Error getting version changes.");
		}

		if(info!=null) {
			if(info.replace(" ","").isEmpty())
				return null;
			/*Fix HTML tags*/
			info = info.replace("</a>", " (hyperlink, see official site)");
			info = REMOVE_TAGS.matcher(info).replaceAll("");
			info=info.replace("&gt;",">").replace("&lt;","<");
			info=info.replace("&nbsp;*","");
		}
		return info;
	}

	/*Fills the hash map with version changes*/
	static void fillVersionMap() {
		String info = getFormattedNintendoSite();
		if(info==null) {
			Log.warn("Please run \".fixitpls\"");
			return;
		}

		/*Do the parsing!*/
		String[] updates = info.split("Improvements Included in Version ");
		for(String improvements : updates) {
			/*Get version and improvements*/
			String update = improvements.substring(0, improvements.indexOf(" ")); /*Uses index instead of a set number in case 10.0.0 becomes a thing.*/
			improvements=improvements.substring(improvements.indexOf(")\n")+2);

			/*Sometimes there are a ton of new lines before the next piece of the web page*/
			if(improvements.contains("\n\n\n\n\n"))
				improvements=improvements.substring(0,improvements.indexOf("\n\n\n\n\n"));

			/*Makes the word wrapping in embed objects less ugly.*/
			improvements=improvements.replace("\n","\n\n");
			while(improvements.contains("\n\n\n"))
				improvements=improvements.replace("\n\n\n", "\n\n");

			nxMap.put(update, improvements);
		}
	}

	/*Gets the latest version from Nintendo's site.*/
	static String getLatestVersion() {
		String info = getFormattedNintendoSite();
		/*It didn't download correctly*/
		if(info==null)
			return null;
		/*Find the latest version*/
		info=info.substring(info.indexOf("Latest version")); /*Go to the latest version*/
		info=info.replace("Latest version: ", ""); /*Get rid of the pieces before the version number*/
		info=info.substring(0, info.indexOf(" (")); /*Get rid of the pieces after the version number*/
		return info;
	}
}
