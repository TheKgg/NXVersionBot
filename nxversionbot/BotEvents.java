package NXVersionBot.nxversionbot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class BotEvents {
	private static EmbedObject join;

	@EventSubscriber public void onMessageReceived(MessageReceivedEvent e) {
		if (!Main.connected())
			return;
		/*Check if it's a DM*/
		if(e.getGuild()==null) {
			EmbedObject o = new EmbedBuilder()
					.withTitle("Invite Me")
					.withDesc("Direct messaging is unavailable. Add me to a server instead.")
					.withUrl("https://thekgg.xyz/tiny/nxbot")
					.build();
			RequestBuffer.request(() -> {
				try {
					e.getClient().getOrCreatePMChannel(e.getAuthor()).sendMessage(o);
				} catch (Exception e1) {
					/*Sending a message might create an error loop*/
					e1.printStackTrace();
				}
			});
			return;
		}

		/*Check if the message is even a command*/
		if(!e.getMessage().getContent().startsWith(BotUtil.prefix) || e.getMessage().getContent().length()<5)
			return;

		/*Check user permissions*/
		boolean admin = e.getAuthor().getPermissionsForGuild(e.getGuild()).contains(Permissions.ADMINISTRATOR);
		boolean isCommandChannel = e.getChannel().getLongID()== BotUtil.getCommandChannel(e.getGuild().getLongID());
		boolean botAdmin = BotUtil.isBotAdmin(e.getAuthor().getLongID());

		/*Allow admins to run commands outside of command channels*/
		if(admin || isCommandChannel || botAdmin)
			runCommand(e.getMessage().toString(), e.getChannel().getLongID(), e.getGuild().getLongID(), admin, botAdmin);
	}

	@EventSubscriber public void onInviteReceived(GuildCreateEvent e) {
		if (!Main.connected())
			return;
		Log.log("Joined a new server " + e.getGuild().getName() + "\n" + e.getGuild().getStringID());
		IChannel c = e.getGuild().getSystemChannel();
		Main.sendMessage("Thanks for inviting me! Here are some commands to help get you started.", c.getLongID());
		Main.sendMessage(buildJoinMessage(), c.getLongID());
	}


	/*Build the embed object or return a previously built one*/
	private static EmbedObject buildJoinMessage() {
		if (join == null) {
			EmbedBuilder eb = new EmbedBuilder()
					.appendField(BotUtil.prefix+"setchannel <commands/announcements> <channel>", "Sets the channel for your server.", false)
					.appendField(BotUtil.prefix+"nxver <version>", "Gets the version changes.", false)
					.appendField(BotUtil.prefix+"website", "Returns this bot's website.", false)
					.appendField(BotUtil.prefix+"settings", "Shows this server's settings.", false)
					.withColor(32, 89, 163);
			join = eb.build();
		}
		return join;
	}

	/*Runs commands*/
	private static void runCommand(String message, long channel, long guild, boolean admin, boolean isBotAdmin) {
		String[] args = message.split(" ");
		if(args[0].length()<2)
			return;
		args[0]=args[0].replaceFirst(BotUtil.prefix, "");
		switch(args[0]) {

		/*Server management*/
		case "setchannel":
			if(adminCheck(admin, channel))
				break;
			if(args.length<2) {
				Main.sendMessage("Please specify which channel you're setting!", channel);
				Main.sendMessage(new EmbedBuilder().withTitle(BotUtil.prefix+"setchannel <commands/announcements> <channel>").build(), channel);
			} else if(args[1].equals("commands") || args[1].equals("announcements")) {
				long channelId;
				if(args.length<3) { /*If they don't specify a specific channel, go with the one they sent it in*/
					channelId = channel;
				} else {
					try {
						channelId = Long.parseLong(args[2].replace("<#", "").replace(">", "").replace(" ", ""));
					} catch(Exception e) {
						Main.sendMessage("Please specify which channel you're setting!", channel);
						return;
					}
				}
				if(args[1].equals("commands")) {
					BotUtil.addCommandChannel(guild, channelId);
					Main.sendMessage("Added <#" + channelId + "> as this server's command channel.", channel);
				} else {
					BotUtil.addAnnounceChannel(guild, channelId);
					Main.sendMessage("Added <#" + channelId + "> as this server's announcement channel.", channel);
				}

			} else {
				Main.sendMessage("Please specify which channel you're setting!", channel);
				Main.sendMessage(new EmbedBuilder().withTitle(BotUtil.prefix+"setchannel <commands/announcements> <channel>").build(), channel);
			}
			break;
		/*Any user if they are in the right channel*/
		case "nxver":
			if(args.length<2) {
				Main.sendMessage("Please include the version in your message.",channel);
				return;
			}
			String info = UpdateUtils.getUpdateInformation(args[1]);
			if(info==null) {
				Main.sendMessage("Looks like that version doesn't exist. Double check that, then try again.", channel);
			} else {
				if(info.length()>1500)
					info="This version's changelog is too long, please click the link to get more information.";
				String changes = info.split("^url")[0];
				EmbedBuilder eb = new EmbedBuilder()
						.withTitle("Changes in version "+ UpdateUtils.formatUpdateVersion(args[1]))
						.withUrl(UpdateUtils.getLink(args[1]))
						.withDesc(info);
				if(changes.contains("^url")) {
					String[] urls = info.split("^url")[1].split("\n");
					for(int i = 0; i<urls.length; i++) {
						eb.appendField("Link "+i,"",false).withUrl(urls[i]);
					}
				}

				Main.sendMessage(eb.build(), channel);
			}
			break;
		case "website":
			Main.sendMessage(new EmbedBuilder().withTitle("Website").withUrl("https://thekgg.xyz/nxversionbot").withDesc("This is the bot's website.").build(), channel);
			break;
		case "settings":
			String cmdChannel = Long.toString(BotUtil.getCommandChannel(guild));
			String announceChannel = Long.toString(BotUtil.getAnnounceChannel(guild));
			if(cmdChannel.length()<5)
				cmdChannel="Channel is not set up currently.";
			else
				cmdChannel="<#"+cmdChannel+">";
			if(announceChannel.length()<5)
				announceChannel="Channel is not set up currently.";
			else
				announceChannel="<#"+announceChannel+">";
			EmbedObject o = new EmbedBuilder().withTitle("This Server's NXVersionBot Settings:")
					.appendField("Command Channel", cmdChannel, false)
					.appendField("Announcement Channel", announceChannel, false)
					.withDesc("Type\n.setchannel <commands/announcements> <channel>\nto change these settings.")
					.build();
			Main.sendMessage(o, channel);
			break;
		case "help":
			Main.sendMessage(buildJoinMessage(), channel);
			break;
		/*Bot admins only*/
		case "fixitpls":
			if(!isBotAdmin)
				break;
			UpdateUtils.fillVersionMap();
			Main.sendMessage("Attempted to refill the Nintendo Switch version map.", channel);
			break;
		case "logoutpls":
			if(!isBotAdmin)
				break;
			Main.sendMessage("Logging out...", channel);
			Main.logout();
			break;
		case "announcepls":
			if(!isBotAdmin)
				break;
			Main.announceNewVersion(UpdateUtils.getLatestVersion());
			break;
		}
	}

	/*Makes invalid permissions messaging more elegant*/
	private static boolean adminCheck(boolean isAdmin, long channel) {
		if(isAdmin)
			return false;
		Main.sendMessage("Sorry, you have insufficient permissions.", channel);
		return true;
	}
}
