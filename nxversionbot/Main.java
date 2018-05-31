package NXVersionBot.nxversionbot;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import javax.swing.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
	final static String folder = new JFileChooser().getFileSystemView().getDefaultDirectory().toString().replace("\\", "/") + "/NXVersionBot/"; /*Bot's directory*/
	private static IDiscordClient client;
	private static boolean clientCompletelyReady=false;
	private static Thread mainThread;

	public static void main(String[] args) { new Main(); }

	private Main() {
		client = new ClientBuilder().withToken(BotUtil.getToken()).build();
		if (client == null)
			return;

		mainThread=Thread.currentThread();

		client.getDispatcher().registerListener(new BotEvents());
		client.login();

		while (!client.isReady()) /*probably not the best way to do this*/
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		Log.log("Connected to Discord, but not ready yet.");

		/*Do loading here while Discord does things*/
		UpdateUtils.fillVersionMap();
		String currentVersion = UpdateUtils.getLatestVersion();
		BotUtil.load();

		/*Wait a few seconds before sending welcome messages, since GuildCreationEvents are activated when Guilds are being loaded. (Probably a better way to do that)*/
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.log("Ready.");
		clientCompletelyReady=true;

		DateFormat df = new SimpleDateFormat("mm");
		Date d;

		/*main loop for announcements*/
		while (connected()) {
			try { Thread.sleep(60000); } catch (Exception e) { Log.warn("Logging out, sleeping got interrupted."); break; }

			/*Check and save every 5 minutes*/
			d=new Date();
			if(Integer.parseInt(df.format(d))%5!=0)
				continue;

			BotUtil.save();
			String newCurrentVersion = UpdateUtils.getLatestVersion();


			Log.log(newCurrentVersion+">"+currentVersion+"?");

			/*If the newest version is not the same as the version that was gotten before, announce it!*/
			if(newCurrentVersion!=null && currentVersion!=null && !newCurrentVersion.equals(currentVersion)) {
				announceNewVersion(currentVersion);
			}
			currentVersion=newCurrentVersion;
		}
		client.logout();
	}

	/*Sends a message*/
	static void sendMessage(String msg, long channel) {
		if (msg.length() > 2000 || !connected())
			return;
		IChannel c = client.getChannelByID(channel);
		RequestBuffer.request(() -> {
			try {
				c.sendMessage(msg);
			} catch (Exception e) {
				/*Sending a message might create an error loop*/
				e.printStackTrace();
			}
		});
	}

	/*Sends an embed object*/
	static void sendMessage(EmbedObject msg, long channel) {
		IChannel c = client.getChannelByID(channel);
		RequestBuffer.request(() -> {
			try {
				c.sendMessage(msg);
			} catch (Exception e) {
				/*Sending a message might create an error loop*/
				e.printStackTrace();
			}
		});
	}

	/*Guild creation events are called when the bot is just turning on, having a 20 second delay fixes it*/
	static boolean connected() {
		return client.isReady() && clientCompletelyReady;
	}

	/*Goes thru all of the announcement channels and send the message*/
	static void announceNewVersion(String version) {
		/*Build embed object*/
		EmbedBuilder eb = new EmbedBuilder()
				.withTitle("There is a new Nintendo Switch version! (" + version + ")")
				.withUrl(UpdateUtils.getLink(version))
				.withDesc(UpdateUtils.getUpdateInformation(version));
		EmbedObject msg = eb.build();

		/*Run through all of the servers*/
		for(IGuild guild : client.getGuilds()) {
			long channel = BotUtil.getAnnounceChannel(guild.getLongID());
			if(channel!=0L) {
				Main.sendMessage(msg, channel);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*Logs out the bot and shuts down the program*/
	static void logout() {
		mainThread.interrupt();
	}


}
