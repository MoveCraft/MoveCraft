package com.sycoprime.movecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.sycoprime.movecraft.events.MoveCraftTurnEvent;

public class CraftRotator {
	public Craft craft;
	
    //offset between the craft origin and the pivot for rotation

	public CraftRotator(Craft c) {
		craft = c;
		
		if(craft.offX == 0 || craft.offZ == 0) {
			craft.offX = Math.round(craft.sizeX / 2);
			craft.offZ = Math.round(craft.sizeZ / 2);
		}
	}

	public boolean canGoThrough(int blockId){
		/** if the craft can go through this block id */

		//all craft types can move through air
		if(blockId == 0) return true;

		if(!craft.type.canNavigate && !craft.type.canDive)
			return false;

		//ship on water
		if(blockId == 8 || blockId == 9)
			if(craft.waterType == 8) return true;

		//ship on lava
		if(blockId == 10 || blockId == 11)
			if(craft.waterType == 10) return true;

		//iceBreaker can go through ice :)
		if(blockId == 79 && craft.type.iceBreaker)
			if(craft.waterType == 8) return true;

		return false;
	}

	public static double rotateX(double x, double z, int r){		
		if(r==0)
			return x;
		else if(r==90)
			return -z;
		else if(r==180)
			return -x;
		else if(r==270)
			return z;
		else return x;
	}

	public static double rotateZ(double x, double z, int r){	
		/** get the corresponding world z coordinate */	
		if(r==0)
			return z;
		else if(r == 90)
			return x;
		else if(r==180)
			return -z;
		else if(r==270)
			return -x;
		else
			return z;
	}

	public static int rotateX(int x, int z, int r){
		/** get the corresponding world x coordinate */
		
		MoveCraft.instance.DebugMessage("r is " + r +
				", x is " + x +  
				", z is " + z, 4);
		
		if(r==0)
			return x;
		else if(r==90)
			return -z;
		else if(r==180)
			return -x;
		else if(r==270)
			return z;
		else return x;
	}

	public static int rotateZ(int x, int z, int r){
		/** get the corresponding world z coordinate */
		if(r==0)
			return z;
		else if(r==90)
			return x;
		else if(r==180)
			return -z;
		else if(r==270)
			return -x;
		else
			return z;
	}

	//setblock, SAFE !
	public void setBlock(double id, int X, int Y, int Z) {
		if(Y < 0 || Y > 127 || id < 0 || id > 255){
			return;
		}

		if((id == 64 || id == 63) && MoveCraft.instance.DebugMode) {
			System.out.println("This stack trace is totally expected.");
			//Thread.currentThread().getStackTrace();
			//new Throwable().getStackTrace();
			new Throwable().printStackTrace();
			//Exception ex = new Exception();
			//ex.printStackTrace();
		}

		craft.world.getBlockAt(X, Y, Z).setTypeId((int)id);
	}

	public void setBlock(double id, int x, int y, int z, int dx, int dy, int dz, int r) {  
		int X = craft.minX + rotateX(x, z, r) + dx;
		int Y = craft.minY + y + dy;
		int Z = craft.minZ + rotateZ(x, z, r) + dz;

		setBlock(id, X, Y, Z);
	}
	
	public void setDataBlock(short id, byte data, int X, int Y, int Z) {
		if(Y < 0 || Y > 127 || id < 0 || id > 255){
			return;
		}

		craft.world.getBlockAt(X, Y, Z).setTypeId(id);
		craft.world.getBlockAt(X, Y, Z).setData(data);
	}

	public short getWorldBlockId(int x, int y, int z, int r){
		/** get world block id with matrix coordinates and rotation */
		short blockId;

		blockId = (short) craft.world.getBlockTypeIdAt(craft.minX + rotateX(x - craft.offX, z - craft.offZ, r),
				craft.minY + y,
				craft.minZ + rotateZ(x - craft.offX, z - craft.offZ, r));

		return blockId;
	}

	public short getCraftBlockId(int x, int y, int z, int r){

		int nx = rotateX(x - craft.offX, z - craft.offZ , r) + craft.offX;
		int ny = y;
		int nz = rotateZ(x - craft.offX, z - craft.offZ, r) + craft.offZ;

		if(!(nx >= 0 && nx < craft.sizeX &&
				ny >= 0 && ny < craft.sizeY &&
				nz >= 0 && nz < craft.sizeZ))
			return 255;

		return craft.matrix[nx][ny][nz];
	}

	public boolean canMoveBlocks(int dx, int dy, int dz, int dr){
		// Do not like the following :(
		World world = craft.world;

		//new rotation of the craft
		//int newRotation = (craft.rotation + dr + 360) % 360;
		int newRotation = (dr + 360) % 360;
		//int backRotation = (360 - dr) % 360;

		//vertical limit
		if(craft.minY + dy < 0 || craft.minY + craft.sizeY + dy > 128){
			return false;
		}

		//watch out for the head !
		if(craft.isOnCraft(craft.player, false)){

			int px = (int)Math.floor(craft.player.getLocation().getX()) - craft.minX;
			int pz = (int)Math.floor(craft.player.getLocation().getZ()) - craft.minZ;

			int X = craft.minX + rotateX(px + dx, pz + dz, dr);
			int Y = (int)Math.floor(craft.player.getLocation().getY()) + dy;
			int Z = craft.minZ + rotateZ(px + dx, pz + dz, dr);

			if(world.getBlockTypeIdAt(X, Y, Z) != 0 && world.getBlockTypeIdAt(X, Y + 1, Z) != 0){
				craft.player.sendMessage("head check !");
				return false;
			}
		}

		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){
					//all blocks new craft.positions needs to have a free space before
					if(craft.matrix[x][y][z]!=255){ //before move : craft block

						if(getCraftBlockId(x + dx, y + dy, z + dz, dr) == 255){
							if(!canGoThrough(getWorldBlockId(x + dx, y + dy, z + dz, newRotation))){
								return false;
							}
						}
					}
				}
			}
		}

		return true;
	}	

	public void turn(int dr){
		if(dr < 0)
			dr = 360 - Math.abs(dr);
		while(dr > 359)
			dr = dr - 360;
		
		 MoveCraftTurnEvent event = new MoveCraftTurnEvent(craft, dr);
		 MoveCraft.instance.getServer().getPluginManager().callEvent(event);
		 if (event.isCancelled()) {
			 return;
		 }
		 dr = event.getDegrees();
		
		/*
		if(MoveCraft.instance.DebugMode) {
			int newoffX = rotateX(craft.craft.offX, craft.craft.offZ, dr);
			int newoffZ = rotateZ(craft.craft.offX, craft.craft.offZ, dr);

			MoveCraft.instance.DebugMessage("New off is " + newoffX + ", " + newoffZ);
			return;
		}
		*/

		//ArrayList<Double> xDists = new ArrayList<Double>();
		//ArrayList<Double> zDists = new ArrayList<Double>();
		HashMap<Entity, Double> xDists = new HashMap<Entity, Double>(); 
		HashMap<Entity, Double> zDists = new HashMap<Entity, Double>();
		
		ArrayList<Entity> craftEntities = craft.getCraftEntities();
		
		//for (Player p : server.getOnlinePlayers()) {
		for (Entity e : craftEntities) {
				Location pLoc = e.getLocation();
			
				//double xDist = pLoc.getX() - (craft.minX + craft.craft.offX);
				//double zDist = pLoc.getZ() - (craft.minZ + craft.craft.offZ);
				double xDist = pLoc.getX() - craft.minX;
				double zDist = pLoc.getZ() - craft.minZ;
				
				//MoveCraft.instance.DebugMessage(p.getName() + " is at " + pLoc.getX() + "," + pLoc.getZ());
				//MoveCraft.instance.DebugMessage(p.getName() + " is " + xDist + "," + zDist + " from center.");
			
				//xDists.add(xDist);
				//zDists.add(zDist);
				xDists.put(e, xDist);
				zDists.put(e, zDist);
		}
		
		moveBlocks(0, 0, 0, dr);

		//tp all players in the craft area
		for (Entity e : craftEntities) {
			//if(craft.isOnCraft(p, false)) {
				Location pLoc = e.getLocation();
			
				//double xDist = xDists.get(0);
				//double zDist = zDists.get(0);
				//xDists.remove(0);
				//zDists.remove(0);
				double xDist = xDists.get(e);
				double zDist = zDists.get(e);
				
				//double x = craft.minX + (xDist * Math.cos(dr) - zDist * Math.sin(dr));
		        //double z = craft.minZ + (xDist * Math.sin(dr) + zDist * Math.cos(dr));
				
				MoveCraft.instance.DebugMessage("Dists are " + xDist + ", " + zDist, 1);
				MoveCraft.instance.DebugMessage("Rotated dists are " + rotateX(xDist, zDist, dr) + 
						", " + rotateZ(xDist, zDist, dr), 1);
				
				//player is always displaced from the craft minimum by positive numbers
				double x = craft.minX + Math.abs(rotateX(xDist, zDist, dr));
				double z = craft.minZ + Math.abs(rotateZ(xDist, zDist, dr));
				
				//MoveCraft.instance.DebugMessage("Putting " + p.getName() + " " + rotateX(xDist, zDist, dr) + 
						//"," + rotateZ(xDist, zDist, dr) + " from center.");
				//MoveCraft.instance.DebugMessage("Putting " + e.getName() + " at " + x +  "," + z);

				pLoc.setX(x);
				pLoc.setZ(z);
				pLoc.setYaw(pLoc.getYaw() + dr);
				//tpTarget.setPitch(tpTarget.getPitch());
				
				e.teleport(pLoc);
		}

	}

	public void moveBlocks(int dx, int dy, int dz, int dr){
		/** move the craft according to a vector d
		wdx : world delta x
		wdy : world delta y
		wdz : world delta z
		dr : delta rotation (90, -90) */
		
		dr = dr % 360;
		
		CraftMover cm = new CraftMover(craft);
		
		//rotate dimensions
		int newSizeX = craft.sizeX;
		int newSizeZ = craft.sizeZ;

		if(dr == 90 ||dr == 270){
			newSizeX = craft.sizeZ;
			newSizeZ = craft.sizeX;
		}

		//new matrix
		short newMatrix[][][] = new short[newSizeX]
		                                  [craft.sizeY]
		                                   [newSizeZ];
		
		//store data blocks
		cm.storeDataBlocks();
		cm.storeComplexBlocks();
		//ArrayList<DataBlock> unMovedDataBlocks = craft.dataBlocks;
		//ArrayList<DataBlock> unMovedComplexBlocks = craft.complexBlocks;
		ArrayList<DataBlock> unMovedDataBlocks = new ArrayList<DataBlock>();
		ArrayList<DataBlock> unMovedComplexBlocks = new ArrayList<DataBlock>();
		
		//for(int i = 0; i < craft.dataBlocks.size(); i ++ ) {
		while(craft.dataBlocks.size() > 0) {
			unMovedDataBlocks.add(craft.dataBlocks.get(0));
			craft.dataBlocks.remove(0);
		}
		//for(int i = 0; i < craft.complexBlocks.size(); i ++ ) {
		while(craft.complexBlocks.size() > 0) {
			unMovedComplexBlocks.add(craft.complexBlocks.get(0));
			craft.complexBlocks.remove(0);
		}
		
		//craft.dataBlocks = new ArrayList<DataBlock>();
		//craft.complexBlocks = new ArrayList<DataBlock>();
		
		//rotate matrix
		for(int x=0; x<newSizeX; x++){
			for(int y=0; y < craft.sizeY; y++){
				for(int z=0; z < newSizeZ; z++){
					int newX = 0;
					int newZ = 0;
					if(dr == 90) {
						newX = z;
						newZ = newSizeX - 1 - x;
					} else if(dr == 270){
						newX = newSizeZ - 1 - z;
						newZ = x;
					} else {
						newX = newSizeX - 1 - x;
						newZ = newSizeZ - 1 - z;						
					}

					newMatrix[x][y][z] = craft.matrix[newX][y][newZ];

					for(int i = 0; i < unMovedDataBlocks.size(); i ++ ) {
					//while(unMovedDataBlocks.size() > 0) {
						DataBlock dataBlock = unMovedDataBlocks.get(i);
						if(dataBlock.locationMatches(newX, y, newZ)) {
							dataBlock.x = x;
							dataBlock.z = z;

							craft.dataBlocks.add(dataBlock);
							unMovedDataBlocks.remove(i);
							break;
						}
					}
					for(int i = 0; i < unMovedComplexBlocks.size(); i ++ ) {
					//while(unMovedComplexBlocks.size() > 0) {
						DataBlock dataBlock = unMovedComplexBlocks.get(i);
						if(dataBlock.locationMatches(newX, y, newZ)) {
							dataBlock.x = x;
							dataBlock.z = z;

							craft.complexBlocks.add(dataBlock);
							unMovedComplexBlocks.remove(i);
							break;
						}
					}
				}
			}
		}
		
		//remove blocks that need support first
		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){
					if(craft.matrix[x][y][z] != -1){
						int blockId = craft.matrix[x][y][z];	
						Block block = craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z);
						
						if(BlocksInfo.needsSupport(blockId)) {
							
							if (blockId == 64 || blockId == 71) { // wooden door and steel door						
								if (block.getData() >= 8) {
								//if(belowBlock.getTypeId() == 64 || belowBlock.getTypeId() == 71) {
									continue;
								}
							}
							
							if(blockId == 26) { //bed
								if(block.getData() > 4)
									continue;
							}
							
							setBlock(0, craft.minX + x, craft.minY + y, craft.minZ + z);
						}
					}
				}
			}
		}

		//remove all the current blocks
		for(int x=0;x<craft.sizeX;x++){
			for(int y=0;y<craft.sizeY;y++){
				for(int z=0;z<craft.sizeZ;z++){
					//int blockId = craft.matrix[x][y][z];
					
					/**
						Added to attempt to resolve water issues...
					 */
					
					/*
					// old block postion (remove)
					if (x - dx >= 0 && y - dy >= 0 && z - dz >= 0
							&& x - dx < craft.sizeX && y - dy < craft.sizeY
							&& z - dz < craft.sizeZ) {
						// after moving, this location is not a craft block
						// anymore
						if (craft.matrix[x - dx][y - dy][z - dz] == -1
								|| BlocksInfo.needsSupport(craft.matrix[x - dx][y - dy][z - dz])) {
							if (y > craft.waterLevel || !(craft.type.canNavigate || craft.type.canDive)) {
								//|| matrix [ x - dx ] [ y - dy ] [ z - dz ] == 0)
								setBlock(0, x, y, z);
							}
							else
								setBlock(craft.waterType, x, y, z);
						}
						// the back of the craft, remove
					} else {
					*/
						if (y > craft.waterLevel
								|| !(craft.type.canNavigate || craft.type.canDive))
							setBlock(0, craft.minX + x, craft.minY + y, craft.minZ + z);
						else
							setBlock(craft.waterType, craft.minX + x, craft.minY + y, craft.minZ + z);
					//}

						/*
					if(blockId != -1 && !BlocksInfo.needsSupport(blockId))
						setBlock(0, craft.minX + x, craft.minY + y, craft.minZ + z);
					//if(craft.matrix[x][y][z] != -1){
					//}
					 */
				}
			}
		}

		craft.matrix = newMatrix;
		craft.sizeX = newSizeX;
		craft.sizeZ = newSizeZ;

		//craft pivot
		int posX = craft.minX + craft.offX;
		int posZ = craft.minZ + craft.offZ;
		
		MoveCraft.instance.DebugMessage("Min vals start " + craft.minX + ", " + craft.minZ, 2);
		
		MoveCraft.instance.DebugMessage("Off was " + craft.offX + ", " + craft.offZ, 2);

		//rotate offset
		//int newoffX = rotateX(craft.craft.offX, craft.craft.offZ, -dr % 360);
		//int newoffZ = rotateZ(craft.craft.offX, craft.craft.offZ, -dr % 360);
		int newoffX = rotateX(craft.offX, craft.offZ, dr);
		int newoffZ = rotateZ(craft.offX, craft.offZ, dr);
		
		MoveCraft.instance.DebugMessage("New off is " + newoffX + ", " + newoffZ, 2);

		if(newoffX < 0)
			newoffX = newSizeX - 1 - Math.abs(newoffX);
		if(newoffZ < 0)
			newoffZ = newSizeZ - 1 - Math.abs(newoffZ);

		craft.offX = newoffX;
		craft.offZ = newoffZ;
		
		MoveCraft.instance.DebugMessage("Off is " + craft.offX + ", " + craft.offZ, 2);

		//update min/max
		craft.minX = posX - craft.offX;
		craft.minZ = posZ - craft.offZ;
		craft.maxX = craft.minX + craft.sizeX -1;
		craft.maxZ = craft.minZ + craft.sizeZ -1;
		
		MoveCraft.instance.DebugMessage("Min vals end " + craft.minX + ", " + craft.minZ, 2);
		
		rotateCardinals(craft.dataBlocks, dr);
		rotateCardinals(craft.complexBlocks, dr);

		//put craft back
		for(int x=0;x<craft.sizeX;x++){
			for(int y=0;y<craft.sizeY;y++){
				for(int z=0;z<craft.sizeZ;z++){
					short blockId = newMatrix[x][y][z];

					if(blockId != -1 
							&& !BlocksInfo.needsSupport(blockId))
						setBlock(blockId, craft.minX + x, craft.minY + y, craft.minZ + z);
				}
			}
		}

		//blocks that need support, but are not data blocks
		for(int x=0;x<craft.sizeX;x++){
			for(int y=0;y<craft.sizeY;y++){
				for(int z=0;z<craft.sizeZ;z++){
					short blockId = newMatrix[x][y][z];

					if (BlocksInfo.needsSupport(blockId)
							&& !BlocksInfo.isDataBlock(blockId)) {
						setBlock(blockId, craft.minX + x, craft.minY + y, craft.minZ + z);
					}
				}
			}
		}
		
		cm.restoreDataBlocks(0, 0, 0);
		cm.restoreComplexBlocks(0, 0, 0);
	}
	
	public void rotateCardinals(ArrayList<DataBlock> blocksToRotate, int dr) {
		//http://www.minecraftwiki.net/wiki/Data_values
		//and beds
		
		byte[] cardinals;
		int blockId;
		
		for(DataBlock dataBlock: blocksToRotate) {
			//Block theBlock = craft.getWorldBlock(dataBlock.x, dataBlock.y, dataBlock.z);
			blockId = dataBlock.id;
			
			//torches, skip 'em if they're centered on the tile on the ground
			if(blockId == 50 || blockId == 75 || blockId == 76) {
				if(dataBlock.data == 5)
					continue;				
			}
			
			cardinals = Arrays.copyOf(BlocksInfo.getCardinals(blockId), 4);
			
			if(blockId == 63) {	//sign post
				dataBlock.data = (dataBlock.data + 4) % 16;
				//dataBlock.data = dataBlock.data + 4;
				//if(dataBlock.data > 14) dataBlock.data -= 16;
				continue;
			}
			
			if(blockId == 26) {	//bed
				if(dataBlock.data > 8) {
					for(int c = 0; c < 4; c++)
						cardinals[c] += 8;
				}
			}
			
			if(blockId == 64 || blockId == 71	//wooden or steel door
					|| blockId == 93 || blockId == 94) {	//repeater
				
				if(dataBlock.data > 11) {	//if the door is an open top 
					for(int c = 0; c < 4; c++)
						cardinals[c] += 11;
				} else if (dataBlock.data > 8) {		//if the door is a top
					for(int c = 0; c < 4; c++)
						cardinals[c] += 8; 
				} else if (dataBlock.data > 4) {		//not a top, but open
					for(int c = 0; c < 4; c++)
						cardinals[c] += 4;
				}
			}
			
			if (blockId == 66 ) { // rails
				if(dataBlock.data == 0) {
					dataBlock.data = 1;
					continue;
				}
				if(dataBlock.data == 1) {
					dataBlock.data = 0;
					continue;
				}
			}
			
			if(blockId == 69) {	//lever
				
				if(dataBlock.data == 5 || dataBlock.data == 6 ||	//if it's on the floor
						dataBlock.data == 13 || dataBlock.data == 14) {
					cardinals = new byte[]{6, 5, 14, 13};
				}
				else if(dataBlock.data > 4) {	//switched on
					for(int c = 0; c < 4; c++) {
						cardinals[c] += 8;
					}					
				}
			}
			
			if(blockId == 93 || blockId == 94) {	//repeater
				if(dataBlock.data > 11) {
					for(int c = 0; c < 4; c++)
						cardinals[c] += 12;
				}
				else if(dataBlock.data > 7) {
					for(int c = 0; c < 4; c++)
						cardinals[c] += 8;
				}
				else if(dataBlock.data > 3) {
					for(int c = 0; c < 4; c++)
						cardinals[c] += 4;
				}
			}
						
			if(cardinals != null) {
				MoveCraft.instance.DebugMessage(Material.getMaterial(blockId) + 
						" Cardinals are "
						+ cardinals[0] + ", "
						+ cardinals[1] + ", "
						+ cardinals[2] + ", "
						+ cardinals[3], 2);
				
				int i = 0;
				for(i = 0; i < 3; i++)
					if(dataBlock.data == cardinals[i])
						break;

				MoveCraft.instance.DebugMessage("i starts as " + i + " which is " + cardinals[i], 2);

				i += (dr / 90);
				
				if(i > 3)
					i = i - 4;

				MoveCraft.instance.DebugMessage("i ends as " + i + ", which is " + cardinals[i], 2);
				
				dataBlock.data = cardinals[i];
			}
		}
	}
}
