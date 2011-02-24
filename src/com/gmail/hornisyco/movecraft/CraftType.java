package com.gmail.hornisyco.movecraft;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.*;

import org.bukkit.entity.Player;

/*
 * MoveCraft plugin for Hey0 mod (hMod) by Yogoda
 *
 * You are free to modify it for your own server
 * or use part of the code for your own plugins.
 * You don't need to credit me if you do, but I would appreciate it :)
 *
 * You are not allowed to distribute alternative versions of MoveCraft without my consent.
 * If you do cool modifications, please tell me so I can integrate it :)
 */
public class CraftType {
	
	String name = "";
	String driveCommand = "pilot";

	int minBlocks = 9;
	int maxBlocks = 500;
	int maxSpeed = 4;

	// type of stone needed to make the vehicle fly
	int flyBlockType = 0;
	// the type of block needed to make the vehicle able to drill through
	// terrain
	int digBlockId = 0;
	// percent of flystone needed to make it fly
	double flyBlockPercent = 0;
	double digBlockPercent = 0;
	int digBlockDurability = 0;
	int engineBlockId = 0;
	int fuelItemId = 0;
	int fuelConsumptionMultiplier = 1;
	
	int remoteControllerItem = 0;

	boolean canFly = false;
	boolean canNavigate = false;
	boolean canDive = false;
	boolean iceBreaker = false;
	boolean bomber = false;
	boolean canDig = false;
	boolean obeysGravity = false;
	boolean isTerrestrial = false;
	boolean requiresRails = false;

	String sayOnControl = "You control the craft";
	String sayOnRelease = "You release the craft";

	short[] structureBlocks = null; // blocks that can make the structure of the craft

	public static ArrayList<CraftType> craftTypes = new ArrayList<CraftType>();

	public CraftType(String name) {
		this.name = name;
		
		String[] bob = MoveCraft.instance.ConfigSetting("StructureBlocks").split(",");
		short[] juan = new short[bob.length + 1];
		for(int i = 0; i < bob.length; i++)
			juan[i] = Short.parseShort(bob[i]);
		structureBlocks = juan;
		//structureBlocks = plugin.configFile.ConfigSettings.get("");
	}

	public static CraftType getCraftType(String name) {

		for (CraftType type : craftTypes) {
			if (type.name.equalsIgnoreCase(name))
				return type;
		}

		return null;
	}

	public String getCommand() {
		return "/" + name.toLowerCase();
	}

	public Boolean canUse(Player player){
		if(PermissionInterface.CheckPermission(player, "movecraft." + name.toLowerCase() + "." + driveCommand))
			return true;
		else
			return false;
	}

	private static void loadDefaultCraftTypes() {
		// if the default craft types are not loaded (first execution), then
		// load them
		if (CraftType.getCraftType("boat") == null)
			craftTypes.add(CraftType.getDefaultCraftType("boat"));
		if (CraftType.getCraftType("ship") == null)
			craftTypes.add(CraftType.getDefaultCraftType("ship"));
		// if(CraftType.getCraftType("icebreaker")==null)
		// craftTypes.add(CraftType.getDefaultCraftType("icebreaker"));
		if (CraftType.getCraftType("bomber") == null)
			craftTypes.add(CraftType.getDefaultCraftType("bomber"));
		if (CraftType.getCraftType("aircraft") == null)
			craftTypes.add(CraftType.getDefaultCraftType("aircraft"));
		if (CraftType.getCraftType("airship") == null)
			craftTypes.add(CraftType.getDefaultCraftType("airship"));
		if (CraftType.getCraftType("UFO") == null)
			craftTypes.add(CraftType.getDefaultCraftType("UFO"));
		// if(CraftType.getCraftType("USO")==null)
		// craftTypes.add(CraftType.getDefaultCraftType("USO"));
		if (CraftType.getCraftType("submarine") == null)
			craftTypes.add(CraftType.getDefaultCraftType("submarine"));
		if (CraftType.getCraftType("drill") == null)
			craftTypes.add(CraftType.getDefaultCraftType("drill"));
		if (CraftType.getCraftType("car") == null)
			craftTypes.add(CraftType.getDefaultCraftType("car"));
		if (CraftType.getCraftType("train") == null)
			craftTypes.add(CraftType.getDefaultCraftType("train"));
	}

	private static CraftType getDefaultCraftType(String name) {

		CraftType craftType = new CraftType(name);

		if (name.equalsIgnoreCase("template")) {

			setAttribute(
					craftType,
					"structureBlocks",
					"4,5,17,19,20,35,41,42,43,44,45,46,47,48,49,50,53,57,65,67,68,69,75,76,77,85,87,88,89");
			
		} else if (name.equalsIgnoreCase("boat")) {
			craftType.driveCommand = "sail";
			craftType.canNavigate = true;
			craftType.minBlocks = 9;
			craftType.maxBlocks = 500;
			craftType.maxSpeed = 4;
			craftType.sayOnControl = "You're on a boat !";
			craftType.sayOnRelease = "You release the helm";
			
		} else if (name.equalsIgnoreCase("ship")) {
			craftType.driveCommand = "sail";
			craftType.canNavigate = true;
			craftType.minBlocks = 50;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 6;
			craftType.sayOnControl = "You're on a ship !";
			craftType.sayOnRelease = "You release the helm";
			
		} else if (name.equalsIgnoreCase("icebreaker")) {
			craftType.driveCommand = "sail";
			craftType.canNavigate = true;
			craftType.minBlocks = 50;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 4;
			craftType.iceBreaker = true;
			craftType.sayOnControl = "Let's break some ice !";
			craftType.sayOnRelease = "You release the helm";
			
		} else if (name.equalsIgnoreCase("drill")) {
			craftType.driveCommand = "drive";
			craftType.canNavigate = true;
			craftType.minBlocks = 20;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 1;
			craftType.canDig = true;
			craftType.canDive = true;
			craftType.digBlockId = 57;
			// craftType.flyBlockPercent = 1;
			craftType.sayOnControl = "Armageddon, but down.";
			craftType.sayOnRelease = name + " controls released.";
			
		} else if (name.equalsIgnoreCase("aircraft")) {
			craftType.driveCommand = "pilot";
			craftType.canFly = true;
			craftType.minBlocks = 9;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 6;
			craftType.sayOnControl = "You're on an aircraft !";
			craftType.sayOnRelease = "You release the joystick";
			
		} else if (name.equalsIgnoreCase("bomber")) {
			craftType.driveCommand = "pilot";
			craftType.canFly = true;
			craftType.minBlocks = 20;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 4;
			craftType.bomber = true;
			craftType.sayOnControl = "You're on a bomber !";
			craftType.sayOnRelease = "You release the joystick";
			
		} else if (name.equalsIgnoreCase("airship")) {
			craftType.driveCommand = "pilot";
			craftType.canFly = true;
			craftType.minBlocks = 9;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 6;
			craftType.flyBlockType = 35;
			craftType.flyBlockPercent = 60;
			craftType.sayOnControl = "You're on an airship !";
			craftType.sayOnRelease = "You release the control panel";
			
		} else if (name.equalsIgnoreCase("UFO")) {
			craftType.driveCommand = "pilot";
			craftType.canFly = true;
			craftType.minBlocks = 9;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 9;
			craftType.flyBlockType = 89;
			craftType.flyBlockPercent = 4;
			craftType.sayOnControl = "You're on a UFO !";
			craftType.sayOnRelease = "You release the control panel";
			
		} else if (name.equalsIgnoreCase("USO")) {
			craftType.driveCommand = "pilot";
			craftType.canFly = true;
			craftType.canDive = true;
			craftType.minBlocks = 9;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 9;
			craftType.flyBlockType = 89;
			craftType.flyBlockPercent = 4;
			craftType.sayOnControl = "You're on a USO !";
			craftType.sayOnRelease = "You release the control panel";
			
		} else if (name.equalsIgnoreCase("submarine")) {
			craftType.driveCommand = "dive";
			craftType.canDive = true;
			craftType.minBlocks = 10;
			craftType.maxBlocks = 1000;
			craftType.maxSpeed = 3;
			craftType.sayOnControl = "You're into a submarine !";
			craftType.sayOnRelease = "You release the helm";
			
		} else if (name.equalsIgnoreCase("car")) {
		craftType.driveCommand = "drive";
		craftType.canNavigate = true;
		craftType.isTerrestrial = true;
		craftType.obeysGravity = true;
		craftType.minBlocks = 10;
		craftType.maxBlocks = 1000;
		craftType.maxSpeed = 3;
		craftType.sayOnControl = "You blew a .07! You're good to go!";
		craftType.sayOnRelease = "Remember where you parked!";
		
	} else if (name.equalsIgnoreCase("train")) {
		craftType.driveCommand = "conduct";
		craftType.canNavigate = true;
		craftType.isTerrestrial = true;
		craftType.requiresRails = true;
		craftType.obeysGravity = true;
		craftType.minBlocks = 10;
		craftType.maxBlocks = 1000;
		craftType.maxSpeed = 3;
		craftType.sayOnControl = "All aboard! Ha ha ha ha ha ha haaaa!";
		craftType.sayOnRelease = "Last stop.";
	}

		return craftType;
	}

	// set the attributes of the craft type
	private static void setAttribute(CraftType craftType, String attribute,
			String value) {

		if (attribute.equalsIgnoreCase("driveCommand"))
			craftType.driveCommand = value;
		else if (attribute.equalsIgnoreCase("minBlocks"))
			craftType.minBlocks = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("maxBlocks"))
			craftType.maxBlocks = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("maxSpeed"))
			craftType.maxSpeed = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("flyBlockType"))
			craftType.flyBlockType = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("flyBlockPercent"))
			craftType.flyBlockPercent = Double.parseDouble(value);
		else if (attribute.equalsIgnoreCase("digBlockId"))
			craftType.digBlockId = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("digBlockDurability"))
			craftType.digBlockDurability = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("fuelItemId"))
			craftType.fuelItemId = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("fuelConsumptionMultiplier"))
			craftType.fuelConsumptionMultiplier = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("canNavigate"))
			craftType.canNavigate = Boolean.parseBoolean(value);
		else if (attribute.equalsIgnoreCase("isTerrestrial"))
			craftType.isTerrestrial = Boolean.parseBoolean(value);
		else if (attribute.equalsIgnoreCase("requiresRails"))
			craftType.requiresRails = Boolean.parseBoolean(value);
		else if (attribute.equalsIgnoreCase("canFly"))
			craftType.canFly = Boolean.parseBoolean(value);
		else if (attribute.equalsIgnoreCase("canDive"))
			craftType.canDive = Boolean.parseBoolean(value);
		else if (attribute.equalsIgnoreCase("canDig"))
			craftType.canDig = Boolean.parseBoolean(value);
		else if (attribute.equalsIgnoreCase("obeysGravity"))
			craftType.obeysGravity = Boolean.parseBoolean(value);
		// else if(attribute.equalsIgnoreCase("iceBreaker"))
		// craftType.iceBreaker = Boolean.parseBoolean(value);
		else if (attribute.equalsIgnoreCase("bomber"))
			craftType.bomber = Boolean.parseBoolean(value);
		else if (attribute.equalsIgnoreCase("sayOnControl"))
			craftType.sayOnControl = value;
		else if (attribute.equalsIgnoreCase("sayOnRelease"))
			craftType.sayOnRelease = value;
		else if (attribute.equalsIgnoreCase("remoteControllerItem"))
			craftType.remoteControllerItem = Integer.parseInt(value);
		else if (attribute.equalsIgnoreCase("structureBlocks")) {
			String[] split = value.split(",");
			craftType.structureBlocks = new short[split.length];
			int i = 0;
			for (String blockId : split) {
				craftType.structureBlocks[i] = Short.parseShort(blockId);
				i++;
			}
		}
	}

	public static void saveType(File dir, CraftType craftType, boolean force) {		
		File craftFile = new File(dir + File.separator
				+ craftType.name + ".txt");

		if (!craftFile.exists()) {
			try {
				craftFile.createNewFile();
			} catch (IOException ex) {
				MoveCraft.logger.log(Level.SEVERE, null, ex);
				return;
			}
		} else
			// we don't overwrite existing files
			return;

		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(craftFile));

			writeAttribute(writer, "driveCommand", craftType.driveCommand,
					force);
			writeAttribute(writer, "minBlocks", craftType.minBlocks, true);
			writeAttribute(writer, "maxBlocks", craftType.maxBlocks, force);

			// list of blocks that make the structure of the craft
			if (craftType.structureBlocks != null) {
				String line = "structureBlocks=";
				for (short blockId : craftType.structureBlocks) {

					line += blockId + ",";
				}

				writer.write(line.substring(0, line.length() - 1));
				writer.newLine();
			}
			
			writeAttribute(writer, "maxSpeed", craftType.maxSpeed, force);
			writeAttribute(writer, "flyBlockType", craftType.flyBlockType, force);
			writeAttribute(writer, "flyBlockPercent", craftType.flyBlockPercent, force);
			writeAttribute(writer, "digBlockId", craftType.digBlockId, force);
			writeAttribute(writer, "digBlockDurability", craftType.digBlockDurability, force);
			writeAttribute(writer, "fuelItemId", craftType.fuelItemId, force);
			writeAttribute(writer, "fuelConsumptionMultiplier", craftType.fuelConsumptionMultiplier, force);
			writeAttribute(writer, "canNavigate", craftType.canNavigate, force);
			writeAttribute(writer, "isTerrestrial", craftType.isTerrestrial, force);
			writeAttribute(writer, "requiresRails", craftType.requiresRails, force);
			writeAttribute(writer, "canFly", craftType.canFly, force);
			writeAttribute(writer, "canDive", craftType.canDive, force);
			writeAttribute(writer, "canDig", craftType.canDig, force);
			writeAttribute(writer, "obeysGravity", craftType.obeysGravity, force);
			// writeAttribute(writer, "iceBreaker", craftType.iceBreaker);
			writeAttribute(writer, "bomber", craftType.bomber, force);
			writeAttribute(writer, "sayOnControl", craftType.sayOnControl, force);
			writeAttribute(writer, "sayOnRelease", craftType.sayOnRelease, force);

			writer.close();

		} catch (IOException ex) {
			MoveCraft.logger.log(Level.SEVERE, null, ex);
		}
	}

	public static void saveTypes(File dir) {		
		for (CraftType craftType : craftTypes) {
			saveType(dir, craftType, false);
		}

		// the template is just a file that shows all parameters
		saveType(dir, getDefaultCraftType("template"), true);

	}

	private static void writeAttribute(BufferedWriter writer, String attribute,
			String value, boolean force) throws IOException {
		if ((value == null || value.trim().equals("")) && !force)
			return;
		writer.write(attribute + "=" + value);
		writer.newLine();
	}

	private static void writeAttribute(BufferedWriter writer, String attribute,
			int value, boolean force) throws IOException {
		if (value == 0 && !force)
			return;
		writer.write(attribute + "=" + value);
		writer.newLine();
	}

	private static void writeAttribute(BufferedWriter writer, String attribute,
			double value, boolean force) throws IOException {
		if (value == 0 && !force)
			return;
		writer.write(attribute + "=" + value);
		writer.newLine();		
	}

	private static void writeAttribute(BufferedWriter writer, String attribute,
			boolean value, boolean force) throws IOException {
		if (!value && !force)
			return;
		writer.write(attribute + "=" + value);
		writer.newLine();
	}

	public static void loadTypes(File dir) {
		File[] craftTypesList = dir.listFiles();
		craftTypes.clear();

		for (File craftFile : craftTypesList) {

			if (craftFile.isFile() && craftFile.getName().endsWith(".txt")) {

				String craftName = craftFile.getName().split("\\.")[0];

				// skip the template file
				if (craftName.equalsIgnoreCase("template"))
					continue;

				CraftType craftType = new CraftType(craftName);

				try {
					BufferedReader reader = new BufferedReader(new FileReader(
							craftFile));

					String line;
					while ((line = reader.readLine()) != null) {

						String[] split;
						split = line.split("=");

						if (split.length >= 2)
							setAttribute(craftType, split[0], split[1]);
					}

					reader.close();

				} catch (IOException ex) {
					MoveCraft.logger.log(Level.SEVERE, null, ex);
				}

				craftTypes.add(craftType);
			}
		}

		loadDefaultCraftTypes();
	}
}
