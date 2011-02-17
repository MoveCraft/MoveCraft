package com.bukkit.yogoda.movecraft;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionInterface {
	public static PermissionHandler Permissions = null;
	public static MoveCraft plugin = null;

	public static void setupPermissions(MoveCraft movecraft) {
		plugin = movecraft;
		Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");

		if(Permissions == null) {
			if(test != null) {
				Permissions = ((Permissions)test).getHandler();
			} else {
				//plugin.log.info(Messaging.bracketize(name) + " Permission system not enabled. Disabling plugin.");
				//plugin.getServer().getPluginManager().disablePlugin(this);
			}
		}
	}
    
	public static boolean CheckPermission(Player player, String command) {		
		if (Permissions != null) {
			plugin.DebugMessage("Permissions is not null.");
		    if(Permissions.has(player, "movecraft." + command))
		    	return true;
		}
		
		if(player.isOp())
			return true;
	    
		player.sendMessage("You do not have permission to preform movecraft." + command);
	    return false;
	}
    
	public static boolean old_CheckPermission(Player player, String command) {
		if (Permissions == null) {
			if(plugin.configFile.ConfigSettings.get("RequireOp") == "true")
				return player.isOp();
			else
				return true;
		}
	    if(Permissions.has(player, "movecraft." + command))
	    	return true;
	    
		player.sendMessage("You do not have permission to preform movecraft." + command);
	    return false;
	}
}
