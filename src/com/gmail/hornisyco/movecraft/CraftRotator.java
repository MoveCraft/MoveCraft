package com.gmail.hornisyco.movecraft;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.gmail.hornisyco.movecraft.Craft.DataBlock;

public class CraftRotator {
	public static MoveCraft plugin;
	public Craft craft;

	public CraftRotator(Craft c, MoveCraft movecraft) {
		plugin = movecraft;
		craft = c;
	}

	//if the craft can go through this block id
	public boolean canGoThrough(int blockId){

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

	//get the corresponding world z coordinate
	public static double rotateZ(double x, double z, int r){		
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

	//get the corresponding world x coordinate
	public static int rotateX(int x, int z, int r){
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

	//get the corresponding world z coordinate
	public static int rotateZ(int x, int z, int r){
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


	//get world block id with matrix coordinates and rotation
	public short getWorldBlockId(int x, int y, int z, int r){
		World world = craft.player.getWorld();
		short blockId;

		blockId = (short) world.getBlockTypeIdAt(craft.minX + rotateX(x - craft.offX, z - craft.offZ, r),
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
		World world = craft.player.getWorld();

		//new rotation of the craft
		int newRotation = (craft.rotation + dr + 360) % 360;
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
		Server server = plugin.getServer();

		if(dr < 0)
			dr = 360 - Math.abs(dr);
		while(dr > 359)
			dr = dr - 360;

		ArrayList<Double> xDists = new ArrayList<Double>();
		ArrayList<Double> zDists = new ArrayList<Double>();
		
		for (Player p : server.getOnlinePlayers()) {
			if(craft.isOnCraft(p, false)){
				Location pLoc = p.getLocation();
			
				double xDist = pLoc.getX() - (craft.minX + craft.offX);
				double zDist = pLoc.getZ() - (craft.minZ + craft.offZ);
			
				xDists.add(xDist);
				zDists.add(zDist);
			}
		}
		
		moveBlocks(0, 0, 0, dr);

		//tp all players in the craft area
		for (Player p : server.getOnlinePlayers()) {
			if(craft.isOnCraft(p, false)){
				Location pLoc = p.getLocation();
			
				double xDist = xDists.get(0);
				double zDist = zDists.get(0);
				xDists.remove(0);
				zDists.remove(0);
				
				double x = (craft.minX + craft.offX) + rotateX(xDist, zDist, dr);
				double z = (craft.minZ + craft.offZ) + rotateZ(xDist, zDist, dr);

				pLoc.setX(x);
				pLoc.setZ(z);
				pLoc.setYaw(pLoc.getYaw() + dr);
				//tpTarget.setPitch(tpTarget.getPitch());
				
				p.teleportTo(pLoc);

			}
		}

	}

	//move the craft according to a vector d
	//wdx : world delta x
	//wdy : world delta y
	//wdz : world delta z
	//dr : delta rotation (90, -90)
	public void moveBlocks(int dx, int dy, int dz, int dr){

		dr = dr % 360;

		//craft.player.sendMessage("rotation");
		//int newRotation = (craft.rotation + dr + 360) % 360;

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
		craft.storeDataBlocks();
		craft.storeComplexBlocks();
		
		System.out.println("PosX is " + craft.posX + ", posZ is " + craft.posZ);
		System.out.println("minx is " + craft.minX + ", minz is " + craft.minZ);
		System.out.println("Started with " + craft.dataBlocks.size() + " data blocks and " + 
				craft.complexBlocks.size() + " complex blocks.");
		
		//ArrayList<DataBlock> unMovedDataBlocks = craft.dataBlocks;
		//ArrayList<DataBlock> unMovedComplexBlocks = craft.complexBlocks;
		ArrayList<DataBlock> unMovedDataBlocks = new ArrayList<DataBlock>();
		ArrayList<DataBlock> unMovedComplexBlocks = new ArrayList<DataBlock>();
		
		for(int i = 0; i < craft.dataBlocks.size(); i ++ ) {
			unMovedDataBlocks.add(craft.dataBlocks.get(i));
			craft.dataBlocks.remove(i);
		}
		for(int i = 0; i < craft.complexBlocks.size(); i ++ ) {
			unMovedDataBlocks.add(craft.complexBlocks.get(i));
			craft.complexBlocks.remove(i);
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
						DataBlock dataBlock = unMovedDataBlocks.get(i);
						if(dataBlock.locationMatches(newX, y, newZ)) {
							dataBlock.x = x;
							dataBlock.z = z;

							craft.dataBlocks.add(dataBlock);
							unMovedDataBlocks.remove(i);
						}
					}
					for(int i = 0; i < unMovedComplexBlocks.size(); i ++ ) {
						DataBlock dataBlock = unMovedComplexBlocks.get(i);
						if(dataBlock.locationMatches(newX, y, newZ)) {
							dataBlock.x = x;
							dataBlock.z = z;

							craft.complexBlocks.add(dataBlock);
							unMovedComplexBlocks.remove(i);
						}
					}
					/*
					for(int i = 0; i < unMovedDataBlocks.size(); i ++ ) {
						DataBlock dataBlock = unMovedDataBlocks.get(i);
						if(dataBlock.locationMatches(x, y, z)) {
							dataBlock.x = newX;
							dataBlock.z = newZ;

							craft.dataBlocks.add(dataBlock);
							unMovedDataBlocks.remove(i);
						}
					}
					for(int i = 0; i < unMovedComplexBlocks.size(); i ++ ) {
						DataBlock dataBlock = unMovedComplexBlocks.get(i);
						if(dataBlock.locationMatches(x, y, z)) {
							dataBlock.x = newX;
							dataBlock.z = newZ;

							craft.complexBlocks.add(dataBlock);
							unMovedComplexBlocks.remove(i);
						}
					}
					*/
				}
			}
		}

		System.out.println("Ended with " + craft.dataBlocks.size() + " data blocks and " + 
				craft.complexBlocks.size() + " complex blocks.");

		//COLLISION DETECTION GOES HERE

		//remove all the current blocks
		for(int x=0;x<craft.sizeX;x++){
			for(int y=0;y<craft.sizeY;y++){
				for(int z=0;z<craft.sizeZ;z++){
					if(craft.matrix[x][y][z] != -1){
						setBlock(0, craft.minX + x,
								craft.minY + y,
								craft.minZ + z);
					}
				}
			}
		}

		craft.matrix = newMatrix;
		craft.sizeX = newSizeX;
		craft.sizeZ = newSizeZ;

		//craft pivot
		int posX = craft.minX + craft.offX;
		int posZ = craft.minZ + craft.offZ;

		//rotate offset
		int newOffX = rotateX(craft.offX, craft.offZ, -dr % 360);
		int newOffZ = rotateZ(craft.offX, craft.offZ, -dr % 360);

		if(newOffX < 0)
			newOffX = newSizeX - 1 - newOffX;
		if(newOffZ < 0)
			newOffZ = newSizeZ - 1 - newOffZ;

		craft.offX = newOffX;
		craft.offZ = newOffZ;

		//update min/max
		craft.minX = posX - craft.offX;
		craft.minZ = posZ - craft.offZ;
		craft.maxX = craft.minX + craft.sizeX -1;
		craft.maxZ = craft.minZ + craft.sizeZ -1;

		//put craft back
		for(int x=0;x<craft.sizeX;x++){
			for(int y=0;y<craft.sizeY;y++){
				for(int z=0;z<craft.sizeZ;z++){
					short blockId = newMatrix[x][y][z];
					//BlocksInfo.is
					if(blockId != -1){
						setBlock(blockId, craft.minX + x,
								craft.minY + y,
								craft.minZ + z);
					}
				}
			}
		}
		
		System.out.println("PosX is " + craft.posX + ", posZ is " + craft.posZ);
		System.out.println("minx is " + craft.minX + ", minz is " + craft.minZ);
		
		craft.restoreDataBlocks(0, 0, 0);
		craft.restoreComplexBlocks(0, 0, 0);
	}

	public void Diamonds(World world) {
		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){
					if(craft.matrix[x][y][z] == 0 || craft.matrix[x][y][z] == -1 || craft.matrix[x][y][z] == 255)
						continue;
					Block block = world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ+ z);
					block.setType(Material.DIAMOND_BLOCK);
					//craft.matrix[x][y][z] = 
				}
			}
		}
	}
}
