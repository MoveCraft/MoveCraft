package com.bukkit.yogoda.movecraft;

public class Craft_Hyperspace {	
	//Craft-based functions relating specifically to hyperspace
	
	public static void enterHyperSpace(Craft craft) {
		//surround the craft with portal blocks
		//use posX, sizeX, etc. to get edges?
		craft.inHyperSpace = true;
		
		//movement needs to be changed...
		craft.player.sendMessage("You enter hyperspace.");
	}
	
	public static void exitHyperSpace(Craft craft) {
		craft.move(craft.HyperSpaceMoves[0] * 16, craft.HyperSpaceMoves[1] * 16, craft.HyperSpaceMoves[2] * 16);
		craft.HyperSpaceMoves[0] = 0;
		craft.HyperSpaceMoves[1] = 0;
		craft.HyperSpaceMoves[2] = 0;
		//remove the portal block surrounding
		craft.inHyperSpace = false;
		
		craft.player.sendMessage("You exit hyperspace.");
	}
	
	public static void hyperSpaceMove(Craft craft, int dx, int dy, int dz) {
		//craft.HyperSpaceMoves[0] += dx * 16;
		//craft.HyperSpaceMoves[1] += dy * 16;
		//craft.HyperSpaceMoves[2] += dz * 16;
		craft.HyperSpaceMoves[0] += dx;
		craft.HyperSpaceMoves[1] += dy;
		craft.HyperSpaceMoves[2] += dz;
	}
}
