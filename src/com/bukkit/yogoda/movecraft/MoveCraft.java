package com.bukkit.yogoda.movecraft;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;
import java.io.File;
import java.util.Properties;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

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
	static String version = "";

	static final DateFormat dateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	public static Logger logger = Logger.getLogger("Minecraft");
	boolean DebugMode = false;

	//public HashMap<String, String> ConfigSettings = new HashMap<String, String>();
	public ConfigFile configFile;

	// private Server myServer = this.getServer();
	// private World myWorld = myServer.getWorlds()[0];

	private final MoveCraft_PlayerListener playerListener = new MoveCraft_PlayerListener(this);
	private final MoveCraft_BlockListener blockListener = new MoveCraft_BlockListener(this);

	public static void consoleSay(String msg) {
		System.out.println(getDateTime() + " [INFO] " + pluginName + " " + msg);
	}

	public void loadProperties() {
		configFile = new ConfigFile(this);

		File dir = getDataFolder();
		if (!dir.exists())
			dir.mkdir();

		CraftType.loadTypes(dir);
		if(configFile.ConfigSettings.get("WriteDefaultCraft") == "true")
			CraftType.saveTypes(dir);		
	}

	public void onEnable() {
		CraftType.plugin = this;

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);

		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
		//pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
		//pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Normal, this);

		loadProperties();
		PermissionInterface.setupPermissions(this);

		PluginDescriptionFile pdfFile = this.getDescription();
		consoleSay(pdfFile.getVersion() + " plugin enabled");
		version = pdfFile.getVersion();
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
			player.sendMessage(ChatColor.YELLOW + craft.type.sayOnRelease);
			Craft.removeCraft(craft);
			if(DebugMode)
				craft.Destroy();
		} else
			player.sendMessage(ChatColor.YELLOW + "You don't have anything to release");
	}

	public void ToggleDebug() {
		this.DebugMode = !this.DebugMode;
		System.out.println("Debug mode set to " + this.DebugMode);
	}

	public boolean DebugMessage(String message) {
		if(this.DebugMode == true)
			System.out.println(message);
		return this.DebugMode;
	}

	public void createCraft(Player player, CraftType craftType, int x, int y, int z, String name) {
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

		/* Rotation code */
		float sin45 = (float)Math.sin((float)Math.PI * 45.0 / 180f);
		//get player current orientation
		float rotation = (float)Math.PI * player.getLocation().getPitch() / 180f;

		float nx = - (float)Math.sin(rotation);
		float nz = (float)Math.cos(rotation);

		//current direction of the craft
		int dirX = (Math.abs(nx) > sin45 ? 1 : 0) * (int)Math.signum(nx);
		int dirZ = (Math.abs(nz) >= sin45 ? 1 : 0) * (int)Math.signum(nz);
		
		craft.dirX = dirX;
		craft.dirZ = dirZ;

		//auto-detect and create the craft
		//if(!craft.createCraft(x, y, z, dirX, dirZ, name)){
		//   craft = null;
		//   return;
		// }
		/* End Rotation Code */

		// auto-detect and create the craft
		if (!CraftBuilder.detect(craft, x, y, z)) {
			return;
		}

		Craft.addCraft(craft);

		if(craft.engineBlocks.size() > 0)
			craft.timer = new MoveCraft_Timer(0, craft, "engineCheck", false);
		else {
			if(craft.type.requiresRails) {
				craft.railMove();
			}
		}

		player.sendMessage(ChatColor.GRAY + "Right-click in the direction you want to go.");
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

	public void dropItem(Block block){

		int itemToDrop = BlocksInfo.getDropItem(block.getTypeId());
		int quantity = BlocksInfo.getDropQuantity(block.getTypeId());

		if(itemToDrop != -1 && quantity != 0){

			for(int i=0; i<quantity; i++){
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(itemToDrop, 1));
			}
		}
	}
}