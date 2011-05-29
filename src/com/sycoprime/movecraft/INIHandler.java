package com.sycoprime.movecraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class INIHandler {
	
	/*
	File dir = MoveCraft.instance.getDataFolder();
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
	*/
	
	public void load(File MCConfig) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(MCConfig));

			String line;
			while( (line=in.readLine() ) != null ) {
				line = line.trim();

				if( line.startsWith( "#" ) )
					continue;

				String[] split = line.split("=");

				MoveCraft.instance.configFile.ConfigSettings.put(split[0], split[1]);
			}
			in.close();
		}
		catch (IOException e) {
		}		
	}
	
	public void save(File MCConfig) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(MCConfig));

			for(Object configLine : MoveCraft.instance.configFile.ConfigSettings.keySet().toArray()) {
				String configKey = (String) configLine;
				bw.write(configKey + "=" + MoveCraft.instance.configFile.ConfigSettings.get(configKey) + System.getProperty("line.separator"));
			}
			bw.close();
		}
		catch (IOException ex) {
		}		
	}
}
