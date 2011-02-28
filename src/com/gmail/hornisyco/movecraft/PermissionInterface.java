package com.gmail.hornisyco.movecraft;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Permissions support file to interface Nijikokun's Permissions plugin to MoveCraft
*/

public class PermissionInterface {
	public static PermissionHandler Permissions = null;

	public static void setupPermissions() {
		Plugin test = MoveCraft.instance.getServer().getPluginManager().getPlugin("Permissions");

		if(test != null) {
			Permissions = ((Permissions)test).getHandler();
		}
	}

	public static boolean CheckGroupPermission(Player player, String group) {
		if(Permissions == null) {
			System.out.println("Movecraft: WARNING! A command attempted to check against a group, " + 
				"but no group handling plugin was found!");
			//return true;
		}
		else if(group.equalsIgnoreCase(Permissions.getGroup(player.getName())))
			return true;
		if(group.equalsIgnoreCase(player.getName()))
			return true;

		player.sendMessage("Only users in group " + group + " may use that.");
		return false;
	}
	
	public static boolean CheckPermission(Player player, String command) {		
		command = command.replace(" ", ".");
		
		if (Permissions != null) {
		    if(Permissions.has(player, command) || player.isOp())
		    	return true;
		    else
				player.sendMessage("You do not have permission to preform " + command);
		}
		else {
			if(MoveCraft.instance.ConfigSetting("RequireOp").equalsIgnoreCase("true") && !player.isOp())
				return false;
		}
		
		return true;
	}
}
