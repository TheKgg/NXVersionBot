package NXVersionBot.nxversionbot;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BotUtil {
	private static volatile List<Long[]> guildChannels; /*The command channels that are set by server admins*/
	private static List<Long> botAdmins = new ArrayList<>(Arrays.asList(197085351263731713L, 259767890759254016L, 107299728592515072L)); /*coolios*/
	final static String prefix = ".";

	static synchronized long getCommandChannel(long guild) {
		for(Long[] ids : guildChannels) {
			if(ids[0]==guild) {
				return ids[1];
			}
		}
		return 0L;
	}

	static synchronized long getAnnounceChannel(long guild) {
		for(Long[] ids : guildChannels) {
			if(ids[0]==guild) {
				return ids[2];
			}
		}
		return 0L;
	}

	static synchronized void addCommandChannel(long guild, long channel) {
		boolean addNewOne=true;
		for(Long[] info : guildChannels)
			if(info[0]==guild) {
				info[1]=channel;
				addNewOne=false;
				break;
			}
		if(addNewOne) {
			Long[] info = { guild, channel, 0L };
			guildChannels.add(info);
		}
	}

	static synchronized void addAnnounceChannel(long guild, long channel) {
		boolean addNewOne=true;
		for(Long[] info : guildChannels)
			if(info[0]==guild) {
				info[2]=channel;
				addNewOne=false;
				break;
			}
		if(addNewOne) {
			Long[] info = { guild, 0L, channel };
			guildChannels.add(info);
		}
	}

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

	/*Bot admins are certain people that have privs to do more things with it*/
	static boolean isBotAdmin(long uid) {
		return botAdmins.contains(uid);
	}

	/*Saves the configuration*/
	static synchronized void save() {
		Configuration.saveConfiguration(guildChannels);
	}
	static synchronized void load() {
		guildChannels= Configuration.loadConfiguration();
	}
}
