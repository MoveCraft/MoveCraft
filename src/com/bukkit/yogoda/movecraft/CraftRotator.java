package com.bukkit.yogoda.movecraft;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CraftRotator {
	public static MoveCraft plugin;
	public Craft craft;

	public CraftRotator(Craft c, MoveCraft movecraft) {
		plugin = movecraft;
		craft = c;
	}

	//if the craft can go through this block id
	private boolean canGoThrough(int blockId){

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

	private static double rotateX(double x, double z, int r){
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
	private static double rotateZ(double x, double z, int r){
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
	private static int rotateX(int x, int z, int r){
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
	private static int rotateZ(int x, int z, int r){
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
    public void setBlock(int id, int X, int Y, int Z) {
    	World world = craft.player.getWorld();
        //int blockId = world.getBlockTypeIdAt(X, Y, Z);

        //if(blockId !=0 && blockId != 4)
            //craft.player.sendMessage("§ceat block !");

       if(Y < 0 || Y > 127 || id < 0 || id > 255){
           return;
       }

       world.getBlockAt(X, Y, Z).setTypeId(id);
    }

    public void setBlock(int id, int x, int y, int z, int dx, int dy, int dz, int r) {  
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

	private short getCraftBlockId(int x, int y, int z, int r){

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

	public void move(int dx, int dy, int dz, int dr){
		World world = craft.player.getWorld();
		Server server = plugin.getServer();
		dx = craft.speed * dx;
		dz = craft.speed * dz;

		//reduce vertical craft.speed
		if(Math.abs(craft.speed * dy) > 1){
			dy = craft.speed * dy / 2;
			if(Math.abs(dy)==0) dy = (int)Math.signum(dy);
		}

		//player.sendMessage("dr " + dr);

		craft.wdx = dx;
		craft.wdy = dy;
		craft.wdz = dz;

		//rotate d vector back to get the correct local craft.direction
		moveBlocks(rotateX(dx, dz, (360 - craft.rotation) % 360),
				dy,
				rotateZ(dx, dz, (360 - craft.rotation) % 360),
				dr);

		//tp all players in the craft area
		for (Player p : server.getOnlinePlayers()) {
			if(craft.isOnCraft(p, false)){
				double x = p.getLocation().getX() - (craft.posX + 0.5);
				double z = p.getLocation().getZ() - (craft.posZ + 0.5);

				float r = p.getLocation().getPitch();

				//all players but the driver turn with the craft
				if(!p.getName().equalsIgnoreCase(craft.player.getName())){
					r = r + dr;
				}

				Location tpTarget = new Location(world,
						(double)craft.posX + 0.5d + rotateX(x, z, dr) + (double)dx,
						p.getLocation().getY() + (double)dy,
						(double)craft.posZ + 0.5d + rotateZ(x, z, dr) + (double)dz);
				tpTarget.setYaw(r);
				tpTarget.setPitch(p.getLocation().getPitch());
				p.teleportTo(tpTarget);

			}
		}

		craft.rotation = (craft.rotation + dr + 360) % 360;

		craft.posX += dx;
		craft.posY += dy;
		craft.posZ += dz;

		//update bounding box of the craft
		//we transform the 2 points (0,0,0) and (sizeX - 1, sizeY - 1, sizeZ - 1)

		int ax = rotateX(-craft.offX, -craft.offZ, craft.rotation);
		int az = rotateZ(-craft.offX, -craft.offZ, craft.rotation);

		int bx = rotateX(craft.sizeX - craft.offX, craft.sizeZ - craft.offZ, craft.rotation);
		int bz = rotateZ(craft.sizeX - craft.offX, craft.sizeZ - craft.offZ, craft.rotation);

		craft.minX = craft.posX + (ax < bx ? ax : bx);
		craft.minY = craft.posY;
		craft.minZ = craft.posZ + (az < bz ? az : bz);

		craft.maxX = craft.posX + (ax >= bx ? ax : bx);
		craft.maxY = craft.posY + craft.sizeY - 1;
		craft.maxZ = craft.posZ + (az >= bz ? az : bz);

		//update craft.direction of the craft
		dx = rotateX(craft.dirX, craft.dirZ, dr);
		dz = rotateZ(craft.dirX, craft.dirZ, dr);

		craft.dirX = dx;
		craft.dirZ = dz;

		craft.lastMove = System.currentTimeMillis();
	}

	//move the craft according to a vector d
	//wdx : world delta x
	//wdy : world delta y
	//wdz : world delta z
	//dr : delta rotation (90, -90)
	public void moveBlocks(int dx, int dy, int dz, int dr){
		//new rotation of the craft
		int newRotation = (craft.rotation + dr + 360) % 360;
		//remove all the current blocks
		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){

					short blockId = craft.matrix[x][y][z];

					//craft block
					if(blockId != 255){
						setBlock(0, x - craft.offX, y, z - craft.offZ, 0, 0, 0, craft.rotation);
					}

				}
			}
		}

		//second pass, the regular blocks
		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){

					short blockId = craft.matrix[x][y][z];

					//craft block
					if(blockId != 255){
						setBlock(blockId, x - craft.offX, y, z - craft.offZ, craft.wdx, craft.wdy, craft.wdz, newRotation);
					}

				}
			}
		}
	}
}
