package com.sycoprime.movecraft;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Craft_Hyperspace {	
	//Craft-based functions relating specifically to hyperspace
	public static ArrayList<Block> hyperspaceBlocks = new ArrayList<Block>();
	
	public static void enterHyperSpace(Craft craft) {
		//surround the craft with portal blocks
		//use posX, sizeX, etc. to get edges?
		surroundCraft(craft, true);
		craft.inHyperSpace = true;

		craft.player.sendMessage("You have entered hyperspace. Type \"/" + craft.type.name + " hyperspace\" to exit.");
	}
	
	public static void exitHyperSpace(Craft craft) {
		//make the hyperspace bits appear at the destination,
		//then move the craft
		//then remove hyperspace from the origin
		//wait ~10 seconds
		//make the hyperspace disappear at the destination
		
		surroundCraft(craft, false);
		
		CraftMover cm = new CraftMover(craft);
		cm.move(craft.HyperSpaceMoves[0] * 16, craft.HyperSpaceMoves[1] * 16, craft.HyperSpaceMoves[2] * 16);
		craft.HyperSpaceMoves[0] = 0;
		craft.HyperSpaceMoves[1] = 0;
		craft.HyperSpaceMoves[2] = 0;
		//remove the portal block surrounding
		craft.inHyperSpace = false;
		
		craft.player.sendMessage("You exit hyperspace " + 
				(craft.HyperSpaceMoves[0] + craft.HyperSpaceMoves[1]) * 16 + 
				" blocks from where you started.");
	}
	
	public static void hyperSpaceMove(Craft craft, int dx, int dy, int dz) {
		//craft.HyperSpaceMoves[0] += dx * 16;
		//craft.HyperSpaceMoves[1] += dy * 16;
		//craft.HyperSpaceMoves[2] += dz * 16;
		craft.HyperSpaceMoves[0] += dx;
		craft.HyperSpaceMoves[1] += dy;
		craft.HyperSpaceMoves[2] += dz;

		craft.player.sendMessage("You are now " + 
				craft.HyperSpaceMoves[0] * 16 + " X, " + craft.HyperSpaceMoves[1] * 16 + "Y, " + craft.HyperSpaceMoves[2] * 16 + 
				"Z blocks from where you started.");
	}
	
	public static void setBlock(Block block, Boolean fieldOn) {		
		if(fieldOn) {
			hyperspaceBlocks.add(block);
			block.setType(Material.PORTAL);
		}
		else {
			hyperspaceBlocks.remove(block);
			block.setType(Material.AIR);
		}		
		
	}
	
	public static void surroundCraft(Craft craft, Boolean fieldOn) {
		Block fieldBlock;
		int bufferAmount = 2;
		
		//sides
		for(int x = craft.minX - bufferAmount; x < craft.maxX + bufferAmount; x++) {
			for(int y = craft.minY - bufferAmount; y < craft.maxY + bufferAmount; y++) {
				fieldBlock = craft.world.getBlockAt(x, y, craft.minZ - bufferAmount);
				setBlock(fieldBlock, fieldOn);
				fieldBlock = craft.world.getBlockAt(x, y, craft.maxZ + bufferAmount);
				setBlock(fieldBlock, fieldOn);
			}			
		}
		
		//front/back
		for(int z = craft.minZ - bufferAmount; z < craft.maxZ + bufferAmount; z++) {
			for(int y = craft.minY - bufferAmount; y < craft.maxY + bufferAmount; y++) {
				fieldBlock = craft.world.getBlockAt(craft.minX - bufferAmount, y, z);
				setBlock(fieldBlock, fieldOn);								
				fieldBlock = craft.world.getBlockAt(craft.maxX + bufferAmount, y, z);
				setBlock(fieldBlock, fieldOn);
			}			
		}
		
		//top and bottom
		for(int x = craft.minX - bufferAmount; x < craft.maxX + bufferAmount; x++) {
			for(int z = craft.minZ - bufferAmount; z < craft.maxZ + bufferAmount; z++) {
				fieldBlock = craft.world.getBlockAt(x, craft.minY - bufferAmount, z);
				setBlock(fieldBlock, fieldOn);								
				fieldBlock = craft.world.getBlockAt(x, craft.maxY + bufferAmount, z);
				setBlock(fieldBlock, fieldOn);
			}			
		}
	}
}
