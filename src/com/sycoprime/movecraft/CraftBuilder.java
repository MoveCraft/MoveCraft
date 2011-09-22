package com.sycoprime.movecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;

/**
 * MoveCraft craft builder
 * Detects and "assembles" a craft from a series of blocks, from a specified origin block
 * @author Joel (Yogoda)
 */
public class CraftBuilder {

	private static Craft craft;

	public static boolean waitStopMakingThatCraft = false;
	private static Short nullBlock = -1;

	//used for the detection of blocks
	private static Stack<BlockLoc> blocksStack;
	//private static HashMap<BlockLoc, BlockLoc> blocksList = null;

	private static HashMap<Integer,HashMap<Integer,HashMap<Integer,Short>>> dmatrix;

	private static boolean isFree(int x, int y, int z){

		if(x < 0 || x >= craft.sizeX ||
				y < 0 || y >= craft.sizeY ||
				z < 0 || z >= craft.sizeZ)
			return true;

		int blockId = craft.matrix[x][y][z];

		if(blockId == 0 || blockId == -1)
			return true;

		return false;
	}

	private static Short get(int x, int y, int z){

		HashMap<Integer,HashMap<Integer,Short>> xRow;
		HashMap<Integer,Short> yRow;

		xRow = dmatrix.get(new Integer(x));
		if(xRow!=null){

			yRow = xRow.get(new Integer(y));

			if(yRow!=null){
				return yRow.get(new Integer(z));
			}
		}

		return null;
	}

	private static void set(short blockType, int x, int y, int z) {

		HashMap<Integer,HashMap<Integer,Short>> xRow;

		//create an x row if it does not exists
		xRow = dmatrix.get(new Integer(x));
		if(xRow==null){
			xRow = new HashMap<Integer,HashMap<Integer,Short>>();
			dmatrix.put(new Integer(x), xRow);
		}

		HashMap<Integer,Short> yRow;

		//get the y row, create it if it does not exists
		yRow = xRow.get(new Integer(y));

		if(yRow==null){
			yRow = new HashMap<Integer,Short>();
			xRow.put(new Integer(y), yRow);
		}

		Short type = yRow.get(new Integer(z));

		if(type==null){
			yRow.put(new Integer(z), new Short(blockType));
		}
	}

	private static void detectWater(int x, int y, int z){
		if(craft.isCraftBlock(x, y, z)) return;
		//craft block
		//if(x >= 0 && x < craft.sizeX && y >= 0 && y < craft.sizeY && z >= 0 && z < craft.sizeZ &&
				//craft.matrix[x][y][z] != -1) return;

		//Block theBlock = craft.world.getBlockAt(craft.posX + x, craft.posY + y, craft.posZ + z);
		Block theBlock = craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z);
		int blockId = theBlock.getTypeId();

		//found water, record water level and water type
		if((blockId == 8 || blockId == 9) && theBlock.getData() == 0){ //water
			if(y > craft.waterLevel) craft.waterLevel = y;
			craft.waterType = 8;
			return;
		}

		//found lava, record lava level and water type
		if((blockId == 10 || blockId == 11) && theBlock.getData() == 0){ //lava
			if(y > craft.waterLevel) craft.waterLevel = y;
			craft.waterType = 10;
			return;
		}
	}

	//remove water blocks that have been incorrectly added
	private static void removeWater(){

		boolean updated;

		do{

			updated = false;

			for(int x=0;x<craft.sizeX;x++){
				for(int z=0;z<craft.sizeZ;z++){
					for(int y=0;y<craft.sizeY;y++){

						if(craft.matrix[x][y][z] >= 8 && craft.matrix[x][y][z] <= 11 && y <= craft.waterLevel){

							if(isFree(x + 1, y, z) ||
									isFree(x - 1, y, z) ||
									isFree(x, y, z + 1) ||
									isFree(x, y, z - 1) ||
									isFree(x, y - 1, z)){

								craft.matrix[x][y][z] = -1;
								updated = true;

								//craft.thePlayer.sendMessage("water removed");
							}
						}
					}
				}
			}
		} while(updated);
	}

	private static void removeAir(){

		BlockLoc block = blocksStack.pop();

		//this block is out of the craft, return
		if(block.x < 0 || block.x > craft.maxX - craft.minX ||
				block.y < 0 || block.y > craft.maxY - craft.minY ||
				block.z < 0 || block.z > craft.maxZ - craft.minZ){
			return;
		}

		//no air, what are we doing here ?
		if(craft.matrix[block.x][block.y][block.z] != 0)
			return;

		craft.matrix[block.x][block.y][block.z] = -1;

		//explore all 6 directions
		blocksStack.push(new BlockLoc(block.x + 1, block.y, block.z));
		blocksStack.push(new BlockLoc(block.x - 1, block.y, block.z));
		blocksStack.push(new BlockLoc(block.x, block.y + 1, block.z));
		blocksStack.push(new BlockLoc(block.x, block.y - 1, block.z));
		blocksStack.push(new BlockLoc(block.x, block.y, block.z + 1));
		blocksStack.push(new BlockLoc(block.x, block.y, block.z - 1));

		return;
	}

	//detect and create an air bubble surrounding the player
	private static boolean createAirBubble(){
		MoveCraft.instance.DebugMessage("Adding an air bubble.", 4);

		BlockLoc block = blocksStack.pop();

		//out of the craft, there is a hole
		if(block.x < 0 || block.x > craft.maxX - craft.minX ||
				block.y < 0 || block.y > craft.maxY - craft.minY ||
				block.z < 0 || block.z > craft.maxZ - craft.minZ){

			return false;
		}

		//already visited
		if(craft.matrix[block.x][block.y][block.z] == 0)
			return true;

		if(craft.matrix[block.x][block.y][block.z] == -1){

			//add air
			craft.matrix[block.x][block.y][block.z] = 0;

		} else {
			return true;
		}

		//explore all 6 directions
		blocksStack.push(new BlockLoc(block.x + 1, block.y, block.z));
		blocksStack.push(new BlockLoc(block.x - 1, block.y, block.z));
		blocksStack.push(new BlockLoc(block.x, block.y + 1, block.z));
		blocksStack.push(new BlockLoc(block.x, block.y - 1, block.z));
		blocksStack.push(new BlockLoc(block.x, block.y, block.z + 1));
		blocksStack.push(new BlockLoc(block.x, block.y, block.z - 1));

		return true;

	}

	//second pass detection, we have the craft blocks, now we go from bottom to top,
	//add all missing blocks, detect water level
	private static boolean secondPassDetection(){
		//boolean needWaterDetection = false;

		for(int x=0; x<craft.sizeX; x++){
			for(int z=0; z<craft.sizeZ; z++){

				boolean floor = false; //if we have reached the craft floor

				for(int y=0; y<craft.sizeY; y++){

					//we reached the floor of the craft
					if(!floor && craft.matrix[x][y][z] != -1){                       
						floor = true;
						continue;
					} else if(floor && craft.matrix[x][y][z] == -1) {	//free space, check there is no block here

						Block block = craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z);
						int blockId = block.getTypeId();

						craft.matrix[x][y][z] = (short)blockId; // record this block as part of the craft, also fill with air

						if(BlocksInfo.isDataBlock(blockId)){
							addDataBlock(blockId, craft.minX + x, craft.minY + y, craft.minZ + z);
						}

						if(BlocksInfo.isComplexBlock(blockId)){
							addComplexBlock(blockId, craft.minX + x, craft.minY + y, craft.minZ + z);
							craft.findFuel(block);
						}
						
						if(craft.type.engineBlockId != 0 && blockId == craft.type.engineBlockId) {
							addEngineBlock(blockId, craft.minX + x, craft.minY + y, craft.minZ + z);
						}

						//there is a problem with ice that spawn a source block, we can't have ice
						if(blockId==79){
							craft.player.sendMessage(ChatColor.RED + "Sorry, you can't have ice in the " + craft.name);
							return false;
						}

					}

					//water detected, we do the detection of the water level
					if(craft.waterType != 0 && craft.matrix[x][y][z] != -1) {
						detectWater(x + 1, y, z);
						detectWater(x - 1, y, z);
						detectWater(x, y, z + 1);
						detectWater(x, y, z - 1);
					}
				}
			}
		}

		//remove water blocks that can flow out of the craft
		if(craft.waterLevel != -1)
			removeWater();

		//if the craft can dive, we need to create an air bubble surrounding the player
		//if it touch the bounding box walls, then the submarine has a hole !
		if(craft.type.canDive){

			//remove air
			for(int x=0;x<craft.sizeX;x++){
				for(int z=0;z<craft.sizeZ;z++){
					for(int y=0;y<craft.sizeY;y++){
						if(craft.matrix[x][y][z]== 0)
							craft.matrix[x][y][z] = -1;
					}
				}
			}

			blocksStack = new Stack<BlockLoc>();

			//start with the player's head
			blocksStack.push(new BlockLoc((int)Math.floor(craft.player.getLocation().getX()) - craft.minX,
					(int)Math.floor(craft.player.getLocation().getY() + 1 - craft.minY),
					(int)Math.floor(craft.player.getLocation().getZ()) - craft.minZ));

			//detect all connected empty blocks
			do{
				if(!createAirBubble() && MoveCraft.instance.ConfigSetting("allowHoles").equalsIgnoreCase("false")){
					craft.player.sendMessage(ChatColor.YELLOW + "This " + craft.type.name + " have holes, it needs to be waterproof");
					return false;
				}
			}
			while(!blocksStack.isEmpty());

			blocksStack = null;

			//fill with air
			for(int x=0;x<craft.sizeX;x++){
				for(int z=0;z<craft.sizeZ;z++){
					for(int y=0;y<craft.sizeY;y++){
						if(craft.matrix[x][y][z]== -1)
							craft.matrix[x][y][z] = 0;
					}
				}
			}

			//if there is air touching a border, remove it
			for(int x=0;x<craft.sizeX;x++){
				for(int z=0;z<craft.sizeZ;z++){
					for(int y=0;y<craft.sizeY;y++){
						if(craft.matrix[x][y][z] == 0 &&
								(x == 0 ||
										y == 0 ||
										z == 0 ||
										x == craft.sizeX - 1 ||
										y == craft.sizeY - 1 ||
										z == craft.sizeZ - 1)){

							blocksStack = new Stack<BlockLoc>();
							blocksStack.push(new BlockLoc(x, y, z));

							do{
								removeAir();
							}
							while(!blocksStack.isEmpty());

							blocksStack = null;
						}
					}
				}
			}

			blocksStack = null;

		} else {

			//there is water detected
			if(craft.waterLevel != -1){

				//remove air above the water level (so the part under water have still air)
				for(int x=0;x<craft.sizeX;x++){
					for(int z=0;z<craft.sizeZ;z++){
						for(int y=craft.waterLevel + 1;y<craft.sizeY;y++){
							if(craft.matrix[x][y][z]==0)
								craft.matrix[x][y][z] = -1;
						}
					}
				}
				//no water, remove ALL air
			} else {
				for(int x=0;x<craft.sizeX;x++){
					for(int z=0;z<craft.sizeZ;z++){
						for(int y=0;y<craft.sizeY;y++){
							if(craft.matrix[x][y][z]==0)
								craft.matrix[x][y][z] = -1;
						}
					}
				}
			}
		}

		return true;

	}

	private static void addDataBlock(int id, int x, int y, int z){
		craft.dataBlocks.add(new DataBlock(id, x - craft.minX, y - craft.minY, z - craft.minZ,
				craft.world.getBlockAt(x, y, z).getData()));
	}

	private static void addComplexBlock(int id, int x, int y, int z){
		craft.complexBlocks.add(new DataBlock(id, x - craft.minX, y - craft.minY, z - craft.minZ,
				craft.world.getBlockAt(x, y, z).getData()));
		//craft.complexBlocks.add(world.getBlockAt(x - craft.posX, y - craft.posY, z - craft.posZ));
	}

	private static void addEngineBlock(int id, int x, int y, int z){
		craft.engineBlocks.add(new DataBlock(id, x - craft.minX, y - craft.minY, z - craft.minZ,
				craft.world.getBlockAt(x, y, z).getData()));
	}

	//put all data in a standard matrix to be more efficient
	private static void createMatrix(){

		craft.matrix = new short[craft.sizeX][craft.sizeY][craft.sizeZ];
		craft.displacedBlocks = new short[craft.matrix[0].length + 1][craft.matrix[1].length + 1][craft.matrix[2].length + 1];
		craft.dataBlocks = new ArrayList<DataBlock>();
		craft.complexBlocks = new ArrayList<DataBlock>();

		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){
					craft.matrix[x][y][z] = -1;
				}
			}
		}

		for(Integer x:dmatrix.keySet()){
			HashMap<Integer,HashMap<Integer,Short>> xRow = dmatrix.get(x);
			for(Integer y:xRow.keySet()){
				HashMap<Integer,Short> yRow = xRow.get(y);
				for(Integer z:yRow.keySet()){

					short blockId = yRow.get(z);

					if(blockId == -1)
						continue;

					craft.matrix[x - craft.minX][y - craft.minY][z - craft.minZ] = blockId;

	                   if(BlocksInfo.isDataBlock(blockId)){
	                        addDataBlock(blockId, x, y, z);
	                   }
	                   if(BlocksInfo.isComplexBlock(blockId)){
	                        addComplexBlock(blockId, x, y, z);
	                   }
				}
			}
		}

		dmatrix = null; //release the dynamic matrix now we don't need it anymore
	}

	private static void detectBlock(int x, int y, int z, int dir){

		Short blockType = get(x, y, z);

		//location have already been visited
		if(blockType != null) return;

		blockType = new Short((short) craft.world.getBlockAt(x, y, z).getTypeId());
		int BlockData = craft.world.getBlockAt(x, y, z).getData();

		//check for fobidden blocks
		if(craft.type.forbiddenBlocks != null && craft.type.forbiddenBlocks.length > 0 && waitStopMakingThatCraft == false) {
			for(int i = 0; i < craft.type.forbiddenBlocks.length; i++) {
				if(blockType == craft.type.forbiddenBlocks[i]) {
					craft.player.sendMessage("Forbidden block of type " + Material.getMaterial(blockType) + " found. " + 
					"Remove all blocks of that type in order to pilot this craft.");
					waitStopMakingThatCraft = true;
					return;
				}
			}
		}

		//found water, record water level and water type
		if((blockType == 8 || blockType == 9) && BlockData == 0){ //water
			if(y > craft.waterLevel) craft.waterLevel = y;
			craft.waterType = 8;
			set(nullBlock, x, y, z);
			return;
		}

		//found lava, record lava level and water type
		if((blockType == 10 || blockType == 11) && BlockData == 0){ //lava
			if(y > craft.waterLevel) craft.waterLevel = y;
			craft.waterType = 10;
			set(nullBlock, x, y, z);
			return;
		}

		//found air
		if(blockType == 0){ //air
			set(nullBlock, x, y, z);
			return;
		}

		//special blocks
		if(blockType == 55){ //redstone wires
			if(dir!=1){
				set(nullBlock, x, y, z);
				return;
			}
		} else

			//return when the block type is not part of the craft structure
			//default block list
			if(craft.type.structureBlocks == null){

				//only block types supported to make the base of the craft
				if(!(blockType == 4 ||
						blockType == 5 ||
						blockType == 17 ||
						blockType == 19 ||
						blockType == 20 ||
						blockType == 35 ||
						(blockType >= 41 && blockType <= 50) ||
						blockType == 53 ||
						blockType == 55 ||
						blockType == 57 ||
						blockType == 65 ||
						blockType == 67 ||
						blockType == 68 ||
						blockType == 69 ||
						blockType == 75 ||
						blockType == 76 ||
						blockType == 77 ||
						blockType == 85 ||
						blockType == 87 ||
						blockType == 88 ||
						blockType == 89)
				){
					set(nullBlock, x, y, z);
					return;
				}

				//custom block list defined in the config file
			} else {

				boolean found = false;
				for(short blockId : craft.type.structureBlocks){
					if(blockType == blockId) {
						found = true;
						break;
					}
				}
				if(!found){
					set(nullBlock, x, y, z);
					return;
				}
			}  

		//record block type at this location
		set(blockType, x, y, z);
		craft.blockCount++;
		
		if(craft.blockCount > craft.type.maxBlocks){
			return;
		}

		if(blockType == craft.type.flyBlockType){
			craft.flyBlockCount ++;
		}

		if(blockType == craft.type.digBlockId){
			craft.digBlockCount ++;
		}

		if(x < craft.minX) craft.minX = x;
		if(x > craft.maxX) craft.maxX = x;
		if(y < craft.minY) craft.minY = y;
		if(y > craft.maxY) craft.maxY = y;
		if(z < craft.minZ) craft.minZ = z;
		if(z > craft.maxZ) craft.maxZ = z;

		//don't propagate through items that need a support
		if(BlocksInfo.needsSupport(blockType)) return;

		blocksStack.push(new BlockLoc(x, y, z));
	}

	//detect the craft you are in
	private static void detectBlock(BlockLoc block){

		//explore all directions

		//face-face connection
		detectBlock(block.x + 1, block.y, block.z, 1);
		detectBlock(block.x - 1, block.y, block.z, 2);
		detectBlock(block.x, block.y + 1, block.z, 1);
		detectBlock(block.x, block.y - 1, block.z, 6);
		detectBlock(block.x, block.y, block.z + 1, 3);
		detectBlock(block.x, block.y, block.z - 1, 4);

		//edge-edge horizontal connection
		detectBlock(block.x + 1, block.y - 1, block.z, -1);
		detectBlock(block.x - 1, block.y - 1, block.z, -1);
		detectBlock(block.x, block.y - 1, block.z + 1, -1);
		detectBlock(block.x, block.y - 1, block.z - 1, -1);
		detectBlock(block.x + 1, block.y + 1, block.z, -1);
		detectBlock(block.x - 1, block.y + 1, block.z, -1);
		detectBlock(block.x, block.y + 1, block.z + 1, -1);
		detectBlock(block.x, block.y + 1, block.z - 1, -1);

	}

	public static boolean detect(Craft craft, int X, int Y, int Z){
		waitStopMakingThatCraft = false;
		CraftBuilder.craft = craft;

		//if(craft.type.canDig)
		//craft.waterType = 1;

		dmatrix = new HashMap<Integer,HashMap<Integer,HashMap<Integer,Short>>>();

		craft.blockCount = 0;

		craft.minX = craft.maxX = X;
		craft.minY = craft.maxY = Y;
		craft.minZ = craft.maxZ = Z;

		blocksStack = new Stack<BlockLoc>();
		blocksStack.push(new BlockLoc(X, Y, Z));

		//detect all connected blocks
		do{
			detectBlock(blocksStack.pop());
		}
		while(!blocksStack.isEmpty());

		blocksStack = null;
		
		if(waitStopMakingThatCraft == true)
			return false;

		//max block count have been reached, craft can't be detected !
		if(craft.blockCount > craft.type.maxBlocks){
			craft.player.sendMessage(ChatColor.RED + "Unable to detect the " + craft.name + ", be sure it is not connected");
			craft.player.sendMessage(ChatColor.RED + " to the ground, or maybe it is too big for this type of craft");
			craft.player.sendMessage(ChatColor.RED + "The maximum size is " + craft.type.maxBlocks + " blocks");
			return false;
		}
		else
			if(craft.blockCount <  craft.type.minBlocks)
			{
				if(craft.blockCount==0){
					craft.player.sendMessage(ChatColor.RED + "There is no " + craft.name + " here");
					craft.player.sendMessage(ChatColor.RED + "Be sure you are standing on a block");
				}
				else{
					craft.player.sendMessage(ChatColor.RED + "This " + craft.name + " is too small !");
					craft.player.sendMessage(ChatColor.RED + "You need to add " + (craft.type.minBlocks - craft.blockCount) + " blocks");
				}

				return false;
			}
		//the recursive algorithm returned before the max block count have been reached, we have a craft !
			else{

				//check the craft is not already in controlled by someone
				for(Craft c: Craft.craftList){
					if(c != craft && c.isOnBoard){
						//check for intersection between 2 cubes
						if( !((c.minX < craft.minX && c.maxX < craft.minX) || (craft.minX < c.minX && craft.maxX < c.minX )))
							if( !((c.minY < craft.minY && c.maxY < craft.minY) || (craft.minY < c.minY && craft.maxY < c.minY )))
								if( !((c.minZ < craft.minZ && c.maxZ < craft.minZ) || (craft.minZ < c.minZ && craft.maxZ < c.minZ ))){
									craft.player.sendMessage(ChatColor.RED + "" + c.player.getName() + " is already controling this " + craft.name);
									return false;
								}
					}
				}

				//create the craft matrix
				craft.sizeX = (craft.maxX - craft.minX) + 1;
				craft.sizeY = (craft.maxY - craft.minY) + 1;
				craft.sizeZ = (craft.maxZ - craft.minZ) + 1;


				if(craft.waterLevel != -1)
					craft.waterLevel = craft.waterLevel - craft.minY;

				/*
           //player offset from the craft origin
           offX = (float)(player.getX() - posX);
           offY = (float)(player.getY() - posY);
           offZ = (float)(player.getZ() - posZ);
				 */

				createMatrix();

				if(!secondPassDetection()) //second pass, add some blocks, check for water problems
					return false;

				//the ship is not on water
				//if(craft.type.canNavigate && !craft.type.canFly && craft.waterType == 0){
				if(craft.type.canNavigate && !craft.type.canFly && craft.waterType == 0 && !craft.type.canDig && !craft.type.isTerrestrial){
					craft.player.sendMessage(ChatColor.RED + "This " + craft.name + " is not on water...");
					return false;
				} else
					//the submarine is not into water
					//if(craft.type.canDive && !craft.type.canFly && craft.waterType == 0){
					if(craft.type.canDive && !craft.type.canFly && craft.waterType == 0 && !craft.type.canDig){
						craft.player.sendMessage(ChatColor.RED + "This " + craft.name + " is not into water...");
						return false;
					} else
						//the airplane / airship is into water
						if(craft.type.canFly && !craft.type.canNavigate && !craft.type.canDive && craft.waterLevel > -1){
							craft.player.sendMessage(ChatColor.RED + "This " + craft.name + " is into water...");
							return false;
						}

				//an airship needs to have x percent of flystone to be able to move
				//if(craft.type.canFly && craft.type.flyBlockType != 0){
				if(craft.type.flyBlockType != 0 && craft.type.flyBlockPercent > 0) {
					//int flyBlocksNeeded = (int)Math.floor((blockCount - flyBlockCount) * ((float)type.flyBlockPercent * 0.01));

					//let's hope it is correct :P
					int flyBlocksNeeded = (int)Math.floor((craft.blockCount - craft.flyBlockCount) * ((float)craft.type.flyBlockPercent * 0.01) / (1 - ((float)craft.type.flyBlockPercent * 0.01)));

					if(flyBlocksNeeded < 1)
						flyBlocksNeeded = 1;

					if(craft.flyBlockCount < flyBlocksNeeded){
						craft.player.sendMessage(ChatColor.RED + "Not enough " + BlocksInfo.getName(craft.type.flyBlockType) + " to make this " + craft.name + " move");
						craft.player.sendMessage(ChatColor.RED + "You need to add " + (flyBlocksNeeded - craft.flyBlockCount) + " more" );
						return false;
					}
				}

				//drill needs to have a diamond block
				if(craft.type.canDig && craft.type.digBlockId != 0){
					//plugin.DebugMessage("Drill flyblock is " + Integer.toString(craft.type.flyBlockType));

					int digBlocksNeeded = (int)Math.floor((craft.blockCount - craft.digBlockCount) * ((float)craft.type.digBlockPercent * 0.01) / (1 - ((float)craft.type.digBlockPercent * 0.01)));
					if(digBlocksNeeded < 1)
						digBlocksNeeded = 1;

					if(craft.digBlockCount < digBlocksNeeded){
						craft.player.sendMessage(ChatColor.RED + "Not enough " + BlocksInfo.getName(craft.type.digBlockId) + " to make this " + craft.name + " move");
						craft.player.sendMessage(ChatColor.RED + "You need to add " + (digBlocksNeeded - craft.digBlockCount) + " more" );
						return false;
					}
				}

				if(craft.customName == null)
					craft.player.sendMessage(ChatColor.YELLOW +  craft.type.sayOnControl);
				else
					craft.player.sendMessage(ChatColor.YELLOW + "Welcome on the " + ChatColor.WHITE + craft.customName + ChatColor.YELLOW + " !");
			}

		if(craft.type.requiresRails) {
			int xMid = craft.matrix.length / 2;
			int zMid = craft.matrix[0][0].length / 2;

			Block belowBlock = craft.world.getBlockAt(craft.minX + xMid, craft.minY - 1, craft.minZ + zMid);

			if(belowBlock.getType() == Material.RAILS) {
				craft.railBlock = belowBlock;
			}
		}

		return true;

	}

}