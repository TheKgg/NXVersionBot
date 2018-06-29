package NXVersionBot.nxversionbot;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BotUtil {
	private static volatile List<Long[]> guildSettings; /*The settings per Discord guild*/
	private static List<Long> botAdmins = new ArrayList<>(Arrays.asList(197085351263731713L, 259767890759254016L, 107299728592515072L)); /*coolios*/
	final static String prefix = ".";

	private static synchronized long getId(long guild, int id) {
		for(Long[] ids : guildSettings) {
			if(ids[0]==guild) {
				return ids[id];
			}
		}
		return 0L;
	}

	private static synchronized void setId(long guild, int id, long value) {
		boolean addNewOne=true;
		for(Long[] info : guildSettings)
			if(info[0]==guild) {
				info[id]=value;
				addNewOne=false;
				break;
			}
		if(addNewOne) {
			Long[] info = { guild, 0L, 0L, 0L };
			info[id] = value;
			guildSettings.add(info);
		}
	}

	static synchronized long getCommandChannel(long guild) { return getId(guild, 1); }

	static synchronized long getAnnounceChannel(long guild) { return getId(guild, 2); }

	static synchronized long getAnnounceRole(long guild) { return getId(guild, 3); }

	static synchronized void setCommandChannel(long guild, long channel) { setId(guild, 1, channel); }

	static synchronized void setAnnounceChannel(long guild, long channel) { setId(guild, 2, channel); }

	static synchronized void setAnnounceRole(long guild, long id) { setId(guild, 3, id); }

	/*Gets the bot token that is stored in an external file. */
	static String getToken() {
		File tokenFile = new File(Main.folder + "token.txt");
		String token = "";
		try {
			token = Files.readAllLines(tokenFile.toPath(), StandardCharsets.UTF_8).get(0).replace("\uFEFF", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}

	/*Bot admins are certain people that have privileges to do more things with it*/
	static boolean isBotAdmin(long uid) {
		return botAdmins.contains(uid);
	}

	/*Saves the configuration*/
	static synchronized void save() {
		Configuration.saveConfiguration(guildSettings);
	}
	static synchronized void load() {
		guildSettings = Configuration.loadConfiguration();
	}
}
