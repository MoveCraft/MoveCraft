package com.gmail.hornisyco.movecraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.entity.Player;

public class ConfigFile {
	public HashMap<String, String> ConfigSettings = new HashMap<String, String>();

	public ConfigFile(MoveCraft plugin) {
		String filename = "movecraft.config";

		ConfigSettings.put("CraftReleaseDelay", "15");
		ConfigSettings.put("UniversalRemoteId", "294");
		ConfigSettings.put("WriteDefaultCraft", "true");
		ConfigSettings.put("RequireOp", "true");
		ConfigSettings.put("StructureBlocks",
				"4,5,17,19,20,35,41,42,43,44,45,46,47,48,49,50,53,57,65,67,68,69,75,76,77,85,87,88,89");
		ConfigSettings.put("allowHoles", "false");
		
		File dir = plugin.getDataFolder();
		if (!dir.exists())
			dir.mkdir();

		File MCConfig = new File(dir, filename);
		if(MCConfig.exists()) {
			LoadFile(MCConfig);
		} else {
			try {
				MCConfig.createNewFile();
			} catch (IOException e) {
			}
		}
		//save the file whether it was just loaded or not, thus injecting out of date config files with new config settings
		SaveFile(MCConfig);
		
			/*
			try {
				HashMap<String, String> FileSettings = new HashMap<String, String>();
				BufferedReader in = new BufferedReader(new FileReader(MCConfig));

				String line;
				while( (line=in.readLine() ) != null ) {
					line = line.trim();

					if( line.startsWith( "#" ) )
						continue;

					String[] split = line.split("=");

					FileSettings.put(split[0], split[1]);
				}
				in.close();
				
				for(String configSetting: ConfigSettings.keySet()) {
					if(!FileSettings.containsKey(configSetting)) {
						//add the setting and write to file...
					}
				}
			}
			catch (IOException e) {
			}
		}
		else {
			try {
				MCConfig.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(MCConfig));

				for(Object configLine : ConfigSettings.keySet().toArray()) {
					String configKey = (String) configLine;
					bw.write(configKey + "=" + ConfigSettings.get(configKey) + System.getProperty("line.separator"));
				}
				bw.close();
			}
			catch (IOException ex) {
			}
		}
		*/

		//CraftType.loadTypes(dir);
		//if(ConfigSettings.get("WriteDefaultCraft") == "true")
			//CraftType.saveTypes(dir);
	}
	
	public void LoadFile(File MCConfig) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(MCConfig));

			String line;
			while( (line=in.readLine() ) != null ) {
				line = line.trim();

				if( line.startsWith( "#" ) )
					continue;

				String[] split = line.split("=");

				ConfigSettings.put(split[0], split[1]);
			}
			in.close();
		}
		catch (IOException e) {
		}		
	}
	
	public void SaveFile(File MCConfig) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(MCConfig));

			for(Object configLine : ConfigSettings.keySet().toArray()) {
				String configKey = (String) configLine;
				bw.write(configKey + "=" + ConfigSettings.get(configKey) + System.getProperty("line.separator"));
			}
			bw.close();
		}
		catch (IOException ex) {
		}		
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
