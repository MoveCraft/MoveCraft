package com.gmail.hornisyco.movecraft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
    	World world = craft.player.getWorld();
        //int blockId = world.getBlockTypeIdAt(X, Y, Z);

        //if(blockId !=0 && blockId != 4)
            //craft.player.sendMessage("§ceat block !");

       if(Y < 0 || Y > 127 || id < 0 || id > 255){
           return;
       }

       world.getBlockAt(X, Y, Z).setTypeId((int)id);
    }

    public void setBlock(double id, int x, int y, int z, int dx, int dy, int dz, int r) {  
       int X = craft.posX + rotateX(x, z, r) + dx;
       int Y = craft.posY + y + dy;
       int Z = craft.posZ + rotateZ(x, z, r) + dz;

       setBlock(id, X, Y, Z);
    }

	//get world block id with matrix coordinates and rotation
	public short getWorldBlockId(int x, int y, int z, int r){
		World world = craft.player.getWorld();
		short blockId;

		blockId = (short) world.getBlockTypeIdAt(craft.posX + rotateX(x - craft.offX, z - craft.offZ, r),
				craft.posY + y,
				craft.posZ + rotateZ(x - craft.offX, z - craft.offZ, r));
		
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
		if(craft.posY + dy < 0 || craft.posY + craft.sizeY + dy > 128){
			return false;
		}

		//watch out for the head !
		if(craft.isOnCraft(craft.player, false)){

			int px = (int)Math.floor(craft.player.getLocation().getX()) - craft.posX;
			int pz = (int)Math.floor(craft.player.getLocation().getZ()) - craft.posZ;

			int X = craft.posX + rotateX(px + dx, pz + dz, dr);
			int Y = (int)Math.floor(craft.player.getLocation().getY()) + dy;
			int Z = craft.posZ + rotateZ(px + dx, pz + dz, dr);

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

		//rotate d vector back to get the correct local craft.direction
		
		/*
		moveBlocks(rotateX(0, 0, (360 - craft.rotation) % 360),
				0,
				rotateZ(0, 0, (360 - craft.rotation) % 360),
				dr);
				*/
		moveBlocks(0, 0, 0, dr);

		//tp all players in the craft area
		for (Player p : server.getOnlinePlayers()) {
			if(craft.isOnCraft(p, false)){
				double x = p.getLocation().getX() - (craft.posX);
				double z = p.getLocation().getZ() - (craft.posZ);
				float r = p.getLocation().getPitch();

				Location tpTarget = p.getLocation();
				
				tpTarget.setX((double)craft.posX + rotateX(x, z, dr));
				tpTarget.setZ((double)craft.posZ + rotateZ(x, z, dr));
				tpTarget.setYaw(r + dr);
				tpTarget.setPitch(tpTarget.getPitch());
				p.teleportTo(tpTarget);

			}
		}
		
	}

	//move the craft according to a vector d
	//wdx : world delta x
	//wdy : world delta y
	//wdz : world delta z
	//dr : delta rotation (90, -90)
	public void moveBlocks(int dx, int dy, int dz, int dr){
		//something's going all wierd in the 90 degree turns, so I'ma try this to fix it
		//dr += 180;
		//new rotation of the craft
		int newRotation = (craft.rotation + dr + 360) % 360;
		
		//remove all the current blocks
		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){

					short blockId = craft.matrix[x][y][z];

					//craft block
					if(blockId != 255){
						//setBlock(0, x - craft.offX, y, z - craft.offZ, 0, 0, 0, craft.rotation);
						setBlock(0, x, y, z, 0, 0, 0, craft.rotation);
					}

				}
			}
		}
		
		//new matrix...
		short newMatrix[][][] = new short[Math.abs(rotateX(craft.sizeX, craft.sizeZ, dr))]
		                                  [craft.sizeY]
		                                   [Math.abs(rotateZ(craft.sizeX, craft.sizeZ, dr))];

	      for(int x=0; x<newMatrix.length; x++){
	          for(int z=0; z < newMatrix[0].length; z++){
	              for(int y=0; y<newMatrix[0][0].length; y++){
	                  newMatrix[x][z][y] = -1;
	              }
	          }
	       }
		
		//need to rotate all data and complex blocks
	      
	      //because I'm lazy
	      int leftMostX = craft.posX;
	      int bottomMostZ = craft.posZ;
	      
	      int oldOffX = craft.sizeX / 2;
	      int oldOffZ = craft.sizeZ / 2;
			
			int newOffX = newMatrix.length / 2;
			int newOffZ = newMatrix[0][0].length / 2;
			
			int cenXPos = craft.posX + oldOffX;
			int cenZPos = craft.posZ + oldOffZ;
		
		//second pass, the regular blocks
		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){
					//System.out.println("X: " + x + ", Z: " + z);

					short blockId = craft.matrix[x][y][z];

					//craft block
					if(blockId != 255 && blockId != 0 && blockId != -1){
						int xDist = x - oldOffX;
						int zDist = z - oldOffZ;
						int newX = rotateX(xDist, zDist, newRotation);
						int newZ = rotateZ(xDist, zDist, newRotation);
						//System.out.println(xDist + ", " + zDist + " becomes " + newX + ", " + newZ);
						
						//setBlock(blockId, craft.posX + newX, craft.posY + y, craft.posZ + newZ);
						setBlock(blockId, newX + cenXPos, craft.posY + y, newZ + cenZPos);
						
						if((newX + cenXPos) < leftMostX)
							leftMostX = (newX + cenXPos);
						if((newZ + cenZPos) < bottomMostZ)
							bottomMostZ = newZ + cenZPos;
						
					    newMatrix[newX + newOffX][y][newZ + newOffZ] = blockId;
					}

				}
			}
		}
		
		craft.matrix = newMatrix;
		craft.sizeX = newMatrix.length;
		craft.sizeZ = newMatrix[0][0].length;
		craft.posX = leftMostX + 1;
		craft.posZ = bottomMostZ;
	}
	
	public void Diamonds(World world) {
		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){
					if(craft.matrix[x][y][z] == 0 || craft.matrix[x][y][z] == -1 || craft.matrix[x][y][z] == 255)
						continue;
					Block block = world.getBlockAt(craft.posX + x, craft.posY + y, craft.posZ+ z);
					block.setType(Material.DIAMOND_BLOCK);
					//craft.matrix[x][y][z] = 
				}
			}
		}
	}
}
