package com.sycoprime.movecraft;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class ConfigFile {
	public String filename = "movecraft.xml";
	public HashMap<String, String> ConfigSettings = new HashMap<String, String>();
	public HashMap<String, String> ConfigComments = new HashMap<String, String>();

	public ConfigFile() {
		ConfigSettings.put("CraftReleaseDelay", "15");
		ConfigSettings.put("UniversalRemoteId", "294");
		//ConfigSettings.put("WriteDefaultCraft", "true");
		ConfigSettings.put("RequireOp", "true");
		ConfigSettings.put("StructureBlocks",
				"4,5,17,19,20,35,41,42,43,44,45,46,47,48,49,50,53,57,65,67,68,69,75,76,77,85,87,88,89");
		ConfigSettings.put("allowHoles", "false");
		ConfigSettings.put("EnableAsyncMovement", "false");
		ConfigSettings.put("ExperimentalMovementMultiplier", "1.0");
		ConfigSettings.put("TryNudge", "false");
		ConfigSettings.put("LogLevel", "1");
		ConfigSettings.put("RequireRemote", "false");
		ConfigSettings.put("EngineBlockId", "61");
		ConfigSettings.put("HungryHungryDrill", "true");
		ConfigSettings.put("WriteDefaultCraft", "true");
		ConfigSettings.put("ForbiddenBlocks", "null");		
		
		ConfigComments.put("CraftReleaseDelay", "<Number:15> The amount of time between when a user exists a craft and when" +
				" the craft automatically releases.");
		ConfigComments.put("UniversalRemoteId", "<Number:294> The item ID of the remote control that works on all vehicles.");
		ConfigComments.put("RequireOp", "<TRUE/false> Only users with Bukkit-given 'op' can use craft.");
		ConfigComments.put("StructureBlocks", "The blocks that define the structure of the craft. " +
				"It is recommended not to use blocks like stone, dirt, and grass.");
		ConfigComments.put("allowHoles", "<true/FALSE> Are holes allowed in craft (for submarines, drills, etc.)");
		ConfigComments.put("EnableAsyncMovement", "<true/FALSE> Puts craft movement in asyncronous threading." +
				" This is experimental, and might not work. There could be a preformance increase from it if it does, though.");
		ConfigComments.put("TryNudge", "<true/FALSE> 'Nudge' the player rather than moving them. Currently broken.");
		ConfigComments.put("LogLevel", "<Number:1> The amount of output to display to the console. " +
				"1 means nothing beyond what Bukkit normally does, 2 means suspected errors, " +
				"3 means errors and notifications, and 4 means suspected errors, notifications, and status messages.");
		ConfigComments.put("RequireRemote", "<true/FALSE> The vehicle only moves if the remote item is in the player's hand.");
		ConfigComments.put("EngineBlockId", "<block ID:61> The ID of the block to use as engines for craft types which do not " +
				" explicitly define their own individual engine type in their craft type file.");
		ConfigComments.put("HungryHungryDrill", "<true/FALSE> Any craft types which can drill will eat blocks rather than " +
				"creating items.");
		ConfigComments.put("WriteDefaultCraft", "Whether or not to create the default craft type files on plugin enable.");
		ConfigComments.put("ForbiddenBlocks", "Blocks that prevent craft from being created if they are anywhere in the craft" + 
				" leave 'null' for none.");
				
		MoveCraft.instance.configFile = this;
		
		XMLHandler.load();
		
		XMLHandler.save();
	}
	
	public void ListSettings(Player player) {
		if (player != null) {
			player.sendMessage("Movecraft config settings:");
			for(Object configLine : ConfigSettings.keySet().toArray()) {
				String configKey = (String) configLine;
				player.sendMessage(configKey + "=" + ConfigSettings.get(configKey));
			}
		}
		else {
			System.out.println("Movecraft config settings:");
			for(Object configLine : ConfigSettings.keySet().toArray()) {
				String configKey = (String) configLine;
				System.out.println(configKey + "=" + ConfigSettings.get(configKey));
			}			
		}
	}
	
	public String GetSetting(String setting) {
		return ConfigSettings.get(setting);
	}
	
	public void ChangeSetting(String settingName, String settingValue) {
		//Change the value, and update that which is dependant on it
	}
	
	public void SaveSetting(String settingName) {
		//save the setting currently in the hashmap to the file
	}
	
	public void CheckSetting(String settingName, String defaultValue) {
		//Checks to see if a setting exists in the config file, and sets it if it isn't
	}
}
