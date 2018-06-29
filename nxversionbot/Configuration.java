package NXVersionBot.nxversionbot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class Configuration {

	/*
	Saves the list to a configuration file
	Format: guildId,cmdChannelId,announcementChannelId
	*/
	static synchronized void saveConfiguration(List<Long[]> guildChannels) {
		if(guildChannels.isEmpty())
			return;
		File f = new File(Main.folder+"channels.txt");
		if(!f.exists())
			try {
				if (!f.createNewFile()) {
					Log.error(new Exception(), "Failed to save config. (File doesn't exist and returned false upon creation)");
					return;
				}
			} catch(Exception e) {
				Log.error(e, "Failed to save config. (File doesn't exist and could not be created)");
				return;
			}

		/*All has been checked, ready to write*/
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for(Long[] channels : guildChannels) {
				for(long channel : channels) {
					bw.write(Long.toString(channel));
					bw.write(",");
				}
				bw.newLine();
			}
		} catch(Exception e) {
			Log.error(e, "Failed to save config. (Error during writing)");
		}
	}

	/*Reads the configuration file to a list*/
	static List<Long[]> loadConfiguration() {
		File f = new File(Main.folder+"channels.txt");
		List<Long[]> guildChannels = new ArrayList<>();
		if(!f.exists())
			try {
				if (!f.createNewFile()) {
					Log.error(new Exception(), "Failed to load config. (File doesn't exist and returned false upon creation)");
				}
				return guildChannels;
			} catch(Exception e) {
				Log.error(e, "Failed to load config. (File doesn't exist and could not be created)");
				return guildChannels;
			}

		/*Commence the reading!*/
		try(BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while((line = br.readLine())!=null) {
				String[] channels = line.split(",");

				/*It shouldn't be less than 4*/
				if(channels.length<4) {
					Log.warn("Guild "+channels[0]+"'s configuration got messed up.");
					Thread.sleep(500);
					continue;
				}
				Long[] channelsLong = {Long.parseLong(channels[0]), Long.parseLong(channels[1]), Long.parseLong(channels[2]), Long.parseLong(channels[3])};
				guildChannels.add(channelsLong);
			}
		} catch(Exception e) {
			Log.error(e, "Error loading configuration. (Error during reading)");
		}
		return guildChannels;
	}
}
