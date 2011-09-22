package com.sycoprime.movecraft;

import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.entity.Item;

/**
 * MoveCraft for Bukkit by Yogoda and SycoPrime
 *
 * You are free to modify it for your own server
 * or use part of the code for your own MoveCraft.
 * You don't need to credit me if you do, but I would appreciate it :)
 *
 * You are not allowed to distribute alternative versions of MoveCraft without my consent.
 * If you do cool modifications, please tell me so I can integrate it :)
 */
public class Craft {

	// list of craft
	public static ArrayList<Craft> craftList = new ArrayList<Craft>();

	public CraftType type;
	public String name; // name, a different name can be set

	short matrix[][][];
	ArrayList<DataBlock> dataBlocks;
	//convert to LinkedList for preformance boost?
	ArrayList<DataBlock> complexBlocks = new ArrayList<DataBlock>();
	//ArrayList<ArrayList<String>> signLines = new ArrayList<ArrayList<String>>();
	
	short displacedBlocks[][][];

	// size of the craft
	int sizeX, sizeZ, sizeY = 0;

	// position of the craft on the map
	World world;
	//int posX, posY, posZ;
	int centerX, centerZ = -1;
	
	int blockCount = 0;
	int flyBlockCount, digBlockCount = 0;

	int maxBlocks;

	int waterLevel = -1;
	int newWaterLevel = -1; // new detected waterlevel when moving

	short waterType = 0; // water or lava

	int minX, maxX, minY, maxY, minZ, maxZ = 0;

	public Player player;

	int speed = 1;

	long lastMove = System.currentTimeMillis(); // record time of the last arm
	// swing
	boolean haveControl = true; // if the player have the control of the craft
	boolean isOnBoard = true; // if the player is on board
	String customName = null;

	boolean blockPlaced = false;
	
	/* Rotation */
	int rotation = 0;
	int offX, offZ = 0;
	/* End Rotation */

	public MoveCraft_Timer timer = null;
	boolean isPublic = false;
	public boolean inHyperSpace = false;
	public int HyperSpaceMoves[] = new int[3];
	public ArrayList<Location> WayPoints = new ArrayList<Location>();
	public int currentWayPoint = 0;
	public boolean StopRequested = false;
	public Block railBlock;
	int remainingFuel = 0;
	int asyncTaskId = 0;

	// Added engine block to test having blocks that propel the craft
	ArrayList<DataBlock> engineBlocks = new ArrayList<DataBlock>();

	Craft(CraftType type, Player player, String customName, float Rotation) {
		if(Rotation > 45 && Rotation < 135)
			Rotation = 90;
		else if(Rotation > 135 && Rotation < 225)
			Rotation = 180;
		else if (Rotation > 225 && Rotation < 315)
			Rotation = 270;
		else
			Rotation = 0;
		
		this.type = type;
		this.name = type.name;
		this.customName = customName;
		this.player = player;
		this.world = player.getWorld();
		this.rotation = (int) Rotation;
	}

	public static Craft getCraft(Player player) {

		if (craftList.isEmpty())
			return null;

		for (Craft craft : craftList) {
			if (craft.player.getName().equalsIgnoreCase(player.getName())) {
				return craft;
			}
		}

		return null;
	}

	// return the craft the block is belonging to
	public static Craft getCraft(int x, int y, int z) {

		if (craftList.isEmpty())
			return null;

		for (Craft craft : craftList) {
			if (craft.isIn(x, y, z)) {
				return craft;
			}
		}
		return null;
	}

	// add a block to the craft, if it is connected to a craft block
	public void addBlock(Block block) {
		MoveCraft.instance.DebugMessage("Adding a block...", 4);

		// to craft coordinates
		int x = block.getX() - minX;
		int y = block.getY() - minY;
		int z = block.getZ() - minZ;

		// the block can be attached to a bloc of the craft
		if (x < sizeX - 1 && !isFree(matrix[x + 1][y][z]) || x > 0
				&& !isFree(matrix[x - 1][y][z]) || y < sizeY - 1
				&& !isFree(matrix[x][y + 1][z]) || y > 0
				&& !isFree(matrix[x][y - 1][z]) || z < sizeZ - 1
				&& !isFree(matrix[x][y][z + 1]) || z > 0
				&& !isFree(matrix[x][y][z - 1])) {

			short blockId = (short) block.getTypeId();

			// some items need to be converted into blocks
			if (blockId == 331) // redstone wire
				blockId = 55;
			else if (blockId == 323) // sign
				blockId = 68;
			else if (blockId == 324) { // door
				blockId = 64;
				matrix[x][y + 1][z] = blockId;
				dataBlocks.add(new DataBlock(blockId, x, y + 1, z, block.getData() + 8));
				blockCount++;
			} else if (blockId == 330) { // door
				blockId = 71;
				matrix[x][y + 1][z] = blockId;
				dataBlocks.add(new DataBlock(blockId, x, y + 1, z, block.getData() + 8));
				blockCount++;
			} else if (blockId == 338) { // reed
				blockId = 83;
			} else if (blockId >= 256) {
				return;
			}

			matrix[x][y][z] = blockId;

			// add block data
			if (BlocksInfo.isDataBlock(blockId)) {
				dataBlocks.add(new DataBlock(blockId, x, y, z, block.getData()));
			}
			if (BlocksInfo.isComplexBlock(blockId)) {
				complexBlocks.add(new DataBlock(blockId, x, y, z, block.getData()));
			}

			blockCount++;
		}
	}

	// return if the point is in the craft box
	public boolean isIn(int x, int y, int z) {
		return x >= minX && x <= maxX && y >= minY && y <= maxY
		&& z >= minZ && z <= maxZ;
	}

	static void addCraft(Craft craft) {
		craftList.add(craft);
	}

	static void removeCraft(Craft craft) {
		if(craft.timer != null)
			craft.timer.Destroy();
		craftList.remove(craft);
	}

	// if the craft can go through this block id
	private boolean canGoThrough(int craftBlockId, int blockId, int data) {

		// all craft types can move through air and flowing water/lava
		if ( blockId == 0 ||
				(blockId >= 8 && blockId <= 11 && data != 0) ||
				//blockId == 78 || 
				BlocksInfo.coversGrass(blockId)) //snow cover
			return true;

		// we can't go through adminium
		if(blockId == 7)
			return false;

		if (!type.canNavigate && !type.canDive){
			return false;
		}

		if(craftBlockId == 0) {
			if( (blockId >= 8 && blockId <= 11) // air can go through liquid,
					|| blockId == 0) //or other air
				return true;
			else								//but nothing else
				return false;
		}

		// drill can move through all block types...for now.
		if (type.canDig && craftBlockId == type.digBlockId && blockId != 0)
			return true;

		// ship on water
		if (blockId == 8 || blockId == 9)
			if (waterType == 8)
				return true;

		// ship on lava
		if (blockId == 10 || blockId == 11)
			if (waterType == 10)
				return true;

		if(blockId == waterType)
			return true;

		// iceBreaker can go through ice :)
		if (blockId == 79 && type.iceBreaker)
			if (waterType == 8)
				return true;
		return false;
	}

	private static boolean isFree(int blockId) {
		if (blockId == 0 || blockId == -1)
			return true;
		return false;
	}

	@SuppressWarnings("unused")
	private static boolean isAirOrWater(int blockId) {
		if (blockId == 0 || (blockId >= 8 && blockId <= 11))
			return true;
		return false;
	}

	public boolean isOnCraft(Player player, boolean precise) {

		int x = (int) Math.floor(player.getLocation().getX());
		int y = (int) Math.floor(player.getLocation().getY());
		int z = (int) Math.floor(player.getLocation().getZ());

		if (isIn(x, y - 1, z)) {

			if (!precise)
				return true;

			// the block the player is standing on is part of the craft
			if (matrix[x - minX][y - minY - 1][z - minZ] != -1) {
				return true;
			}
		}

		return false;
	}

	public boolean isOnCraft(Entity player, boolean precise) {

		int x = (int) Math.floor(player.getLocation().getX());
		int y = (int) Math.floor(player.getLocation().getY());
		int z = (int) Math.floor(player.getLocation().getZ());

		if (isIn(x, y - 1, z)) {

			if (!precise)
				return true;

			// the block the player is standing on is part of the craft
			if (matrix[x - minZ][y - minY - 1][z - minZ] != -1) {
				return true;
			}
		}

		return false;
	}

	public boolean isCraftBlock(int x, int y, int z) {

		if (x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ) {

			return !(matrix[x][y][z] == -1);
		} else {
			return false;
		}
	}

	// check there is no blocks in the way
	public boolean canMove(int dx, int dy, int dz) {

		/*
		 * MoveCraft.instance.DebugMessage("move dx : " + dx + " dy : " + dy + " dZ : " + dz);
		 * MoveCraft.instance.DebugMessage("move speed : " + speed);
		 * MoveCraft.instance.DebugMessage("move sizeX : " + sizeX + " sizeY : " + sizeY + " sizeZ : " + sizeZ);
		 */
		ArrayList<Chunk> checkChunks = new ArrayList<Chunk>();
		dx = speed * dx;
		dz = speed * dz;

		if (Math.abs(speed * dy) > 1) {
			dy = speed * dy / 2;
			if (Math.abs(dy) == 0)
				dy = (int) Math.signum(dy);
		}

		// vertical limit
		if (minY + dy < 0 || maxY + dy > 128) {
			MoveCraft.instance.DebugMessage("Craft prevented from moving due to vertical limit.", 4);
			return false;
		}

		// watch out of the head !
		if (isOnCraft(player, true)) {

			int X = (int) Math.floor(player.getLocation().getX()) + dx;
			int Y = (int) Math.floor(player.getLocation().getY()) + dy;
			int Z = (int) Math.floor(player.getLocation().getZ()) + dz;

			Block targetBlock1 = world.getBlockAt(X, Y, Z);
			Block targetBlock2 = world.getBlockAt(X, Y + 1, Z);
			if (!isCraftBlock(X - minX, Y - minY, Z - minZ)
					&& !canGoThrough(0, targetBlock1.getTypeId(), 0)
					|| !isCraftBlock(X - minX, Y + 1 - minY, Z - minZ)
					&& !canGoThrough(0, targetBlock2.getTypeId(), 0)) {
				MoveCraft.instance.DebugMessage("Craft prevented from because...can't go through?", 4);
				return false;
			}
		}

		// check all blocks can move
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {

					// all blocks new positions needs to have a free space
					// before
					if (!isFree(matrix[x][y][z]) && // before move : craft block
							!isCraftBlock(x + dx, y + dy, z + dz)) { // after

						Block theBlock = world.getBlockAt(minX + x + dx, minY
								+ y + dy, minZ + z + dz);
						int blockId = theBlock.getTypeId();
						// int blockId = world.getBlockAt(posX + x + dx, posY +
						// y + dy, posZ + z + dz);
						int blockData = theBlock.getData();
						
						if(!checkChunks.contains(theBlock.getChunk()))
							checkChunks.add(theBlock.getChunk());

						// go into water
						//if (dy < 0 && blockId >= 8 && blockId <= 11) {
						if (blockId >= 8 && blockId <= 11) {

							// MoveCraft.instance.DebugMessage("found water at " + y);
							if (y > newWaterLevel)
								newWaterLevel = y;
						} else if (dy > 0 && blockId == 0) { // get out of water, into air

								// MoveCraft.instance.DebugMessage("found air at " + y);
								if (y - 1 < newWaterLevel)
									newWaterLevel = y - 1;
							}
						
						if (!canGoThrough(matrix[x][y][z], blockId, blockData) ) {
							MoveCraft.instance.DebugMessage("Craft prevented from moving because can't go through.", 4);
							return false;
						}
						
						/*
						if(type.requiresRails) {
							if (!canGoThrough(matrix[x][y][z], theBlock, blockData, (y==0)))
								return false;
						} else {
							if (!canGoThrough(matrix[x][y][z], blockId, blockData) )
								return false;
						}
						*/
					}
				}
			}
		}
		
		for (Chunk checkChunk : checkChunks) {
			if(!world.isChunkLoaded(checkChunk)) {
				try {
					world.loadChunk(checkChunk);
				}
				catch (Exception ex) {
					MoveCraft.instance.DebugMessage("Craft prevented from moving because destination chunk is not loaded.", 3);
					return false;
				}
			}
		}
		return true;
	}
	
	/*
	public Block getWorldBlock(int x, int y, int z){
		if(rotation == 0)
			return world.getBlockAt(minX + CraftRotator.rotateX(x, z, rotation),
					minY + y,
					minZ + CraftRotator.rotateZ(x, z, rotation));
		
		else
			return world.getBlockAt(posX + CraftRotator.rotateX(x - offX, z - offZ, rotation),
				posY + y,
				posZ + CraftRotator.rotateZ(x - offX, z - offZ, rotation));
	}
	*/
	
	public ArrayList<Entity> getCraftEntities() {
		ArrayList<Entity> checkEntities = new ArrayList<Entity>();

		Chunk firstChunk = world.getChunkAt(new Location(world, minX, minY, minZ));
		Chunk lastChunk = world.getChunkAt(new Location(world, minX + sizeX, minY + sizeY, minZ + sizeZ));
		
		int targetX = 0;
		int targetZ = 0;
		Chunk addChunk;
		Entity[] ents;

		for(int x = 0; Math.abs(firstChunk.getX() - lastChunk.getX()) >= x; x++) {
			targetX = 0;
			if(firstChunk.getX() < lastChunk.getX()) {
				targetX = firstChunk.getX() + x;
			} else {
				targetX = firstChunk.getX() - x;
			}
			for(int z = 0; Math.abs(firstChunk.getZ() - lastChunk.getZ()) >= z; z++) {
				targetZ = 0;
				if(firstChunk.getZ() < lastChunk.getZ()) {
					targetZ = firstChunk.getZ() + z;
				} else {
					targetZ = firstChunk.getZ() - z;
				}

				addChunk = world.getChunkAt(targetX, targetZ);

				try {
					ents = addChunk.getEntities();
					for(Entity e : ents) {
						if(!(e instanceof Item) && this.isOnCraft(e, false)) {
							checkEntities.add(e);
						}
					}
				}
				catch (Exception ex) {

				}
			}
		}
		return checkEntities;
	}

	public void setSpeed(int speed) {
		if (speed < 1)
			this.speed = speed;
		else if (speed > type.maxSpeed)
			this.speed = type.maxSpeed;
		else
			this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}
	
	public void turn(int dr) {
		CraftRotator cr = new CraftRotator(this);
		cr.turn(dr);
	}

	public void engineTick() {
		//CraftMover cm = new CraftMover(this);
		int dx = 0;
		int dy = 0;
		int dz = 0;
		int[] returnVals = new int[3]; 
		
		if (type.obeysGravity)
			dy -= 1;
		
		//later these will be config options
		//returnVals = enginesByEngineFace(cm);
		returnVals = enginesByPlayerFacing(player, engineBlocks.size());
		dx = returnVals[0];
		dy = returnVals[1];
		dz = returnVals[2];
		
		if(dx != 0 || dy != 0 || dz != 0) {
			//cm.calculatedMove(dx, dy, dz);
		}
	}
	
	public int[] enginesByEngineFace(CraftMover cm) {
		int dx = 0, dy = 0, dz = 0;
		
		for (DataBlock edb : engineBlocks) {
			//Block engineBlock = world.getBlockAt(this.minX + edb.x, this.minY + edb.y, this.minZ + edb.z);
			Block engineBlock = cm.getWorldBlock(edb.x, edb.y, edb.z);
			Block underBlock = world.getBlockAt(engineBlock.getX(), engineBlock.getY() - 1, engineBlock.getZ());			
			//Sign sign = (Sign) engineBlock.getState();
			
			if(engineBlock.getBlockPower() != 0) {
				//System.out.println("Powered engine.");
			} else {
				//System.out.println("Unpowered engine.");
			}
			
			//0,1,2,3
			//north, east, west, south
			
			//north is dx - 1
			//south is dx + 1
			//east is dz - 1
			//west is dz + 1
			
			int engineDirection = BlocksInfo.getCardinalDirectionFromData(engineBlock.getTypeId(), engineBlock.getData());
			switch(engineDirection) {
			case 0:
				dx -= 1;
				break;
			case 1:
				dz -= 1;
				break;
			case 2:
				dz += 1;
				break;
			case 3:
				dx += 1;
				break;
			}
			
			if(underBlock.getType() == Material.REDSTONE_WIRE && underBlock.getData() != 0) {
			//if(engineBlock.isBlockPowered()) {
				/*
				sign.setLine(0, ChatColor.YELLOW + "OOOO");
				sign.setLine(1, ChatColor.YELLOW + "OO" + ChatColor.RED + "OO" + ChatColor.YELLOW + "OO");
				sign.setLine(2, ChatColor.YELLOW + "OO" + ChatColor.RED + "OO" + ChatColor.YELLOW + "OO");
				sign.setLine(3, ChatColor.YELLOW + "OOOO");
				*/
			}
			else
			{
				/*
				sign.setLine(0, "OOOO");
				sign.setLine(1, "OOOOOO");
				sign.setLine(2, "OOOOOO");
				sign.setLine(3, "OOOO");
				*/
			}
		}
		
		return new int[] {dx, dy, dz};
	}
	
	public int[] enginesByPlayerFacing(Player player, int engineCount) {
		float rotation = (float) Math.PI * player.getLocation().getYaw() / 180f;

		float nx = -(float) Math.sin(rotation);
		float nz = (float) Math.cos(rotation);
		
		int[] returnVals = new int[3];
		
		returnVals[0] = engineCount * (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
		returnVals[1] = engineCount * (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);
		returnVals[2] = 0;
		
		return returnVals;
	}
	
	public boolean addWayPoint(Location loc) {
		/*
			if(WayPoints.size() != 0) {
				Location lastWP = WayPoints.get(WayPoints.size() - 1);
				int matches = 0;
				
				if(lastWP.getX() == loc.getX())
					matches += 1;
				if(lastWP.getY() == loc.getY())
					matches += 1;
				if(lastWP.getZ() == loc.getZ())
					matches += 1;
				
				if(matches != 2)
					return false;
			}
			*/
			WayPoints.add(loc);
		return true;
	}
	
	public void removeWayPoint(Location loc) {
		WayPoints.remove(loc);		
	}
	
	public void WayPointTravel(boolean forward) {
		Location nextWaypoint;
		if(forward == true)
			nextWaypoint = WayPoints.get(currentWayPoint + 1);
		else
			nextWaypoint = WayPoints.get(currentWayPoint - 1);
		
		currentWayPoint++;
		if (forward == true && WayPoints.size() >= currentWayPoint)
			forward = false;
		if (forward == false && currentWayPoint == 0)
			forward = true;
		
		Vector deviation = new Vector();
		deviation.add(getLocation().toVector());
		deviation.add(nextWaypoint.toVector());
		
		player.sendMessage(deviation.toString());
	}
	
	public void WarpToWorld(World targetWorld) {
		World oldWorld = this.world;
		CraftMover cm = new CraftMover(this);
		
		//assemble the craft in the new world
		this.world = targetWorld;
		
		cm.move(0, 0, 0);
		
		this.world = oldWorld;

		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {
					Block theBlock = cm.getWorldBlock(x, y, z);
					theBlock.setTypeId(0);
				}
			}
		}

		this.world = targetWorld;
	}
	
	public void SelfDestruct(boolean justTheTip) {
		//figure out what part of the craft is touching the world, or its direction...

		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {
					Block theBlock = world.getBlockAt(minX + x, minY + y, minZ + z);
					theBlock.setType(Material.TNT);
					//TNT tnt = (TNT) theBlock.getState();
				}
			}
		}
	}
	
	public Location getLocation() {
		return new Location(this.world, this.minX, this.minY, this.minZ);
	}
	
	public void Destroy() {
		matrix = null;;
		player = null;
	}
	
	public void findFuel(Block block) {
		if (block.getState() instanceof Chest) {
			Chest chest = (Chest) block.getState();
			Inventory inventory = chest.getInventory();

			for(int slot = 0; slot < inventory.getSize(); slot++) {
				if(inventory.getItem(slot).getTypeId() != 0) {
					if(inventory.getItem(slot).getTypeId() == type.fuelItemId) {
						remainingFuel += inventory.getItem(slot).getAmount(); 
					}
				}
			}
		}		
	}
}