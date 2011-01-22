package com.bukkit.yogoda.movecraft;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;
import java.io.File;
//import java.util.HashMap;
import java.util.Properties;

import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

//import com.bukkit.authorblues.GroupUsers.GroupUsers;

/*
 * MoveCraft plugin for Hey0 mod (hMod) by Yogoda
 * Ported to Bukkit by SycoPrime
 *
 * You are free to modify it for your own server
 * or use part of the code for your own plugins.
 * You don't need to credit me if you do, but I would appreciate it :)
 *
 * You are not allowed to distribute alternative versions of MoveCraft without my consent.
 * If you do cool modifications, please tell me so I can integrate it :)
 */

public class MoveCraft extends JavaPlugin {

	// PropertiesFile properties;
	Properties properties;

	static final String pluginName = "MoveCraft";
	static final String version = "0.6.3";
	static final String updatrUrl = "http://dl.dropbox.com/u/4422249/Minecraft/Plugins/MoveCraft.updatr";
	static final String updatrFileUrl = "http://dl.dropbox.com/u/4422249/Minecraft/Plugins/MoveCraft.jar";
	static final String updatrNotes = "";

	static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public static Logger logger = Logger.getLogger("Minecraft");

	boolean DebugMode = false;

	// private Server myServer = this.getServer();
	// private World myWorld = myServer.getWorlds()[0];

	private final MoveCraft_PlayerListener playerListener = new MoveCraft_PlayerListener(
			this);
	private final MoveCraft_BlockListener blockListener = new MoveCraft_BlockListener(
			this);

	public static void consoleSay(String msg) {
		System.out.println(getDateTime() + " [INFO] " + pluginName + " " + msg);
	}

	public void createUpdatrFile() {

		try {

			File updatrDir = new File("Updatr");

			if (updatrDir.exists()) {

				File updatrFile = new File("Updatr" + File.separator
						+ pluginName + ".updatr");

				// Updatr file does not exist, create it
				if (!updatrFile.exists()) {

					updatrFile.createNewFile();

					BufferedWriter writer = new BufferedWriter(new FileWriter(
							updatrFile));

					writer.write("name = " + pluginName);
					writer.newLine();
					writer.write("version = " + version);
					writer.newLine();
					writer.write("url = " + updatrUrl);
					writer.newLine();
					writer.write("file = " + updatrFileUrl);
					writer.newLine();
					writer.write("notes = " + updatrNotes);
					writer.newLine();

					writer.close();

				}
			}
		} catch (IOException e) {
			MoveCraft.logger.log(Level.SEVERE, null, e);
		}
	}

	public void loadProperties() {

		// directory where the craft types are stored
		File dir = new File("movecraft");
		if (!dir.exists())
			dir.mkdir();

		// properties = new PropertiesFile("movecraft" + File.separator +
		// pluginName.toLowerCase() + ".properties");

		// load craft types and properties
		CraftType.loadTypes(dir);
		CraftType.saveTypes(dir);

		// properties.save();
	}

	public void onEnable() {
		// TODO: Place any custom enable code here including the registration of
		// any events

		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Monitor, this);

		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
		//pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);

		loadProperties();
		// createUpdatrFile();

		consoleSay(version + " plugin enabled");

		// PluginDescriptionFile pdfFile = this.getDescription();
		// System.out.println( pdfFile.getName() + " version " +
		// pdfFile.getVersion() + " is enabled!" );
	}

	public void onDisable() {
		consoleSay(version + " plugin disabled");
	}

	public MoveCraft(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

		BlocksInfo.loadBlocksInfo();
	}

	public void releaseCraft(Player player, Craft craft) {

		if (craft != null) {

			player.sendMessage("§e" + craft.type.sayOnRelease);
			Craft.removeCraft(craft);

		} else
			player.sendMessage("§eYou don't have anything to release");
	}

	public void ToggleDebug() {
		this.DebugMode = !this.DebugMode;
		System.out.println("Debug mode set to " + this.DebugMode);
	}

	public void createCraft(Player player, CraftType craftType, int x, int y,
			int z, String name) {
		if (DebugMode == true)
			player.sendMessage("Attempting to create " + craftType.name
					+ "at coordinates " + Integer.toString(x) + ", "
					+ Integer.toString(y) + ", " + Integer.toString(z));

		Craft craft = Craft.getCraft(player);

		// release any old craft the player had
		if (craft != null) {
			releaseCraft(player, craft);
		}

		craft = new Craft(this, craftType, player, name);

		if (DebugMode == true)
			player.sendMessage("Craft created.");

		// auto-detect and create the craft
		if (!CraftBuilder.detect(player.getWorld(), craft, x, y, z)) {
			return;
		}

		Craft.addCraft(craft);

		player.sendMessage("§7Right-click in the direction you want to go.");
	}

	public static String getDateTime() {
		return dateFormat.format(new Date());
	}

	public boolean checkPermission(Player player, String[] rights) {
		/*
		Plugin gu = this.getServer().getPluginManager().getPlugin("GroupUsers");
		if (gu != null) {
			GroupUsers groupUsers = (GroupUsers) gu;
			*/
			for (String right : rights) {
				if (right.trim().equalsIgnoreCase("")) {
					continue;
				}
				if (right.equalsIgnoreCase("public")) {
					return true;
				}

				if (player.equals(this.getServer().matchPlayer(right))) {
					return true;
				}

				if (!right.startsWith("g:"))
					continue;
				//if (groupUsers.isInGroup(player, right.substring(2))) {
				//	return true;
				//}
			//}
		}
		return false;
	}
}