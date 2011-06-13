package com.sycoprime.movecraft.plugins;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sycoprime.movecraft.MoveCraft;

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
	
	public static boolean inGroup(Player player, String group) {
		if(Permissions == null) {
			System.out.println("Movecraft: WARNING! A command attempted to check against a group, " + 
				"but no group handling plugin was found!");
			//return true;
		}

		player.sendMessage("Only users in group " + group + " may use that.");
		return false;
	}

	@SuppressWarnings("deprecation")
	public static boolean CheckGroupPermission(String world, Player player, String group) {
		MoveCraft.instance.DebugMessage("Checking if " + player.getName() + " is in group " + group, 4);
		
		if(Permissions == null) {
			System.out.println("Movecraft: WARNING! A command attempted to check against a group, " + 
				"but no group handling plugin was found!");
			//return true;
		}
		//else if(group.equalsIgnoreCase(Permissions.getGroup(player.getName())))
		else if(group.equalsIgnoreCase(Permissions.getGroup(world, player.getName())))
			return true;
		if(group.equalsIgnoreCase(player.getName()))
			return true;

		player.sendMessage("Your group does not have permission for that.");
		return false;
	}
	
	public static boolean CheckPermission(Player player, String command) {		
		command = command.replace(" ", ".");
		MoveCraft.instance.DebugMessage("Checking if " + player.getName() + " can " + command, 3);
		
		if (Permissions != null) {			
		    if(Permissions.has(player, command) || player.isOp()) {
		    	MoveCraft.instance.DebugMessage("Player has permissions: " + 
		    			Permissions.has(player, command), 3);
		    	MoveCraft.instance.DebugMessage("Player isop: " + 
		    			player.isOp(), 3);
		    	return true;
		    }
		    else {
				player.sendMessage("You do not have permission to preform " + command);
				return false;
		    }
		}
		else {
			if(MoveCraft.instance.ConfigSetting("RequireOp").equalsIgnoreCase("true") && !player.isOp()) {
				MoveCraft.instance.DebugMessage("Op is required, and " + player.getDisplayName() + " doesn't have it.", 4);
				return false;
			}
		}
		
		return true;
	}
}
