package com.sycoprime.movecraft;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChatColor;
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
import org.bukkit.event.Event;

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

	CraftType type;
	String name; // name, a different name can be set

	short matrix[][][];
	ArrayList<DataBlock> dataBlocks;
	//convert to LinkedList for preformance boost?
	ArrayList<DataBlock> complexBlocks = new ArrayList<DataBlock>();
	//ArrayList<ArrayList<String>> signLines = new ArrayList<ArrayList<String>>();

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

	Craft(CraftType type, Player player, String customName) {

		this.type = type;
		this.name = type.name;
		this.customName = customName;
		this.player = player;
		this.world = player.getWorld();
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
		MoveCraft.instance.DebugMessage("Adding a block...");

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

	// SAFE ! setblock
	public void setBlock(int id, Block block) {		
		// if(y < 0 || y > 127 || id < 0 || id > 255){
		if (id < 0 || id > 255) {
			// + " x=" + x + " y=" + y + " z=" + z);
			System.out.println("Invalid block type ID. Begin panic.");
			return;
		}
		
		if(block.getTypeId() == id) {
			MoveCraft.instance.DebugMessage("Tried to change a " + id + " to itself.");
			return;
		}
		
		MoveCraft.instance.DebugMessage("Attempting to set block at " + block.getX() + ", "
				 + block.getY() + ", " + block.getZ() + " to " + id);
		
		if (block.setTypeId(id) == false) {
			if(world.getBlockAt(block.getLocation()).setTypeId(id) == false)
				System.out.println("Could not set block of type " + block.getTypeId() + 
						" to type " + id + ". I tried to fix it, but I couldn't.");
			else
				System.out.println("I hope to whatever God you believe in that this fix worked.");
		}
		/*
		 call an onblockflow event, or otherwise somehow handle worldguard's sponge fix
		 BlockFromToEvent blockFlow = new BlockFromToEvent(Type.BLOCK_FLOW, source, blockFace);
		  getServer().getMoveCraft.instanceManager().callEvent(blockFlow);
		 */
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
			MoveCraft.instance.DebugMessage("Craft prevented from moving due to vertical limit.");
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
				MoveCraft.instance.DebugMessage("Craft prevented from because...can't go through?");
				return false;
			}
		}

		if(type.canDig)
			newWaterLevel = -1;
		else
			newWaterLevel = waterLevel;

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
							MoveCraft.instance.DebugMessage("Craft prevented from moving because can't go through.");
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
					MoveCraft.instance.DebugMessage("Craft prevented from moving because destination chunk is not loaded.");
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
	public Block getWorldBlock(int x, int y, int z) {
		//return world.getBlockAt(posX + x, posY + y, posZ + z);
		return world.getBlockAt(minX + x, minY + y, minZ + z);
	}
	
	public void storeDataBlocks() {
		for (DataBlock dataBlock : dataBlocks) {
			dataBlock.data = getWorldBlock(dataBlock.x, dataBlock.y, dataBlock.z).getData();
		}
	}
	
	public void storeComplexBlocks() {
		// store the data of all complex blocks, or die trying
		for (DataBlock complexBlock : complexBlocks) {
			Block currentBlock = getWorldBlock(complexBlock.x, complexBlock.y, complexBlock.z);
			complexBlock.id = currentBlock.getTypeId();
			complexBlock.data = currentBlock.getData();
			
			Inventory inventory = null;
			
			if (currentBlock.getState() instanceof Sign) {
				Sign sign = (Sign) currentBlock.getState();
				
				complexBlock.signLines = sign.getLines();
				
				/*
				ArrayList<String> myLines = new ArrayList<String>();
				
				if(sign.getLine(0) != null) {
					myLines.add(sign.getLine(0));
					myLines.add(sign.getLine(1));
					myLines.add(sign.getLine(2));
					myLines.add(sign.getLine(3));
					//signLines.add(myLines);
				}
				*/
				
			} else if (currentBlock.getTypeId() == 54) {
				Chest chest = ((Chest)currentBlock.getState());
				inventory = chest.getInventory();
			} else if (currentBlock.getTypeId() == 23) {
				Dispenser dispenser = (Dispenser) currentBlock.getState();
				inventory = dispenser.getInventory();
			} else if (currentBlock.getTypeId() == 61) {
				Furnace furnace = (Furnace) currentBlock.getState();
				inventory = furnace.getInventory();				
			}
			
			if(inventory != null) {
				MoveCraft.instance.DebugMessage("Inventory is " + inventory.getSize());
				for(int slot = 0; slot < inventory.getSize(); slot++) {
					if(inventory.getItem(slot).getTypeId() != 0 && inventory.getItem(slot) != null) {
						//complexBlock.setItem(slot, inventory.getItem(slot).getTypeId(), inventory.getItem(slot).getAmount());
						complexBlock.setItem(slot, inventory.getItem(slot));
						//inventory.setItem(slot, new ItemStack(0));

						MoveCraft.instance.DebugMessage("Inventory has " + inventory.getItem(slot).getAmount() + 
								" inventory item of type " + inventory.getItem(slot).getTypeId() + 
								" in slot " + slot);
						
						inventory.setItem(slot, null);
					}
				}
			}
		}
	}
	
	public void restoreDataBlocks(int dx, int dy, int dz) {
		Block block;
		
		for (DataBlock dataBlock : dataBlocks) {
			// this is a pop item, the block needs to be created
			if (BlocksInfo.needsSupport(matrix[dataBlock.x][dataBlock.y][dataBlock.z])) {
				block = getWorldBlock(dx + dataBlock.x, dy + dataBlock.y, dz + dataBlock.z);

				block.setTypeId(matrix[dataBlock.x][dataBlock.y][dataBlock.z]);
				block.setData((byte) dataBlock.data);
			} else { //the block is already there, just set the data
				getWorldBlock(dx + dataBlock.x,
						dy + dataBlock.y,
						dz + dataBlock.z)
						.setData((byte)dataBlock.data);
			}
		}
	}
	
	public void restoreComplexBlocks(int dx, int dy, int dz) {
		for (DataBlock complexBlock : complexBlocks) {
			Block theBlock = getWorldBlock(dx + complexBlock.x,
					dy + complexBlock.y,
					dz + complexBlock.z);
			
			theBlock.setData((byte) complexBlock.data);
			
			Inventory inventory = null;

			if (complexBlock.id == 63 || complexBlock.id == 68) {
				MoveCraft.instance.DebugMessage("Restoring a sign.");
				theBlock.setTypeId(complexBlock.id);
				theBlock.setData((byte) complexBlock.data);
				Sign sign = (Sign) theBlock.getState();
				
				sign.setLine(0, complexBlock.signLines[0]);
				sign.setLine(1, complexBlock.signLines[1]);
				sign.setLine(2, complexBlock.signLines[2]);
				sign.setLine(3, complexBlock.signLines[3]);

				sign.update();
			}  else if (theBlock.getTypeId() == 54) {
				Chest chest = ((Chest)theBlock.getState());
				inventory = chest.getInventory();
			} else if (theBlock.getTypeId() == 23) {
				Dispenser dispenser = (Dispenser) theBlock.getState();
				inventory = dispenser.getInventory();
			} else if (theBlock.getTypeId() == 61) {
				Furnace furnace = (Furnace) theBlock.getState();
				inventory = furnace.getInventory();				
			}

			//https://github.com/Afforess/MinecartMania/blob/master/src/com/afforess/minecartmaniacore/MinecartManiaChest.java
			//restore the block's inventory
			if (inventory != null) {
				for(int slot = 0; slot < inventory.getSize(); slot++) {
					if(complexBlock.items[slot] != null && complexBlock.items[slot].getTypeId() != 0) {
						inventory.setItem(slot, complexBlock.items[slot]);
						MoveCraft.instance.DebugMessage("Moving " + complexBlock.items[slot].getAmount() + 
								" inventory item of type " + complexBlock.items[slot].getTypeId() + 
								" in slot " + slot);
					}
				}			
			}
			
			if(theBlock.getTypeId() == 54)
				((Chest)theBlock.getState()).update();
		}		
	}

	// move the craft according to a vector d
	public void move(int dx, int dy, int dz) {
		//if(type.canDig) {
			MoveCraft.instance.DebugMessage("Waterlevel is " + waterLevel);
			MoveCraft.instance.DebugMessage("Watertype is " + waterType);
			MoveCraft.instance.DebugMessage("newWaterlevel is " + newWaterLevel);
		//}
		
		MoveCraftMoveEvent event = new MoveCraftMoveEvent(this, dx, dy, dz);	
		MoveCraft.instance.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
		    return;
		}
		
		dx = (int) event.getMovement().getX();
		dy = (int) event.getMovement().getY();
		dz = (int) event.getMovement().getZ();
		
			if(type.canDig)
				waterLevel = newWaterLevel;
		
		//Server server = MoveCraft.instance.getServer();

		dx = speed * dx;
		dz = speed * dz;
		
		ArrayList<Entity> checkEntities;

		if (Math.abs(speed * dy) > 1) {
			dy = speed * dy / 2;
			if (Math.abs(dy) == 0)
				dy = (int) Math.signum(dy);
		}

		// scan to know if any of the craft blocks are now missing (blocks removed, TNT damage, creeper ?)
		// and update the structure
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				for (int z = 0; z < sizeZ; z++) {
					int craftBlockId = matrix[x][y][z];

					// remove blocks from the structure if it is not there anymore
					if (craftBlockId != -1 && craftBlockId != 0
							&& !(craftBlockId >= 8 && craftBlockId <= 11)) {

						//int blockId = world.getBlockAt(posX + x, posY + y, posZ + z).getTypeId();
						int blockId = world.getBlockAt(minX + x, minY + y, minZ + z).getTypeId();

						 // regenerate TNT on a bomber
						if (craftBlockId == 46 && type.bomber)
							continue;

						// block is not here anymore, remove it
						if (blockId == 0 || blockId >= 8 && blockId <= 11) {
							// air, water, or lava
							if (waterType != 0 && y <= waterLevel)
								matrix[x][y][z] = 0;
							else
								matrix[x][y][z] = -1; // make a hole in the craft

							blockCount--;
							MoveCraft.instance.DebugMessage("Removing a block of type " + craftBlockId + 
									" because of type " + blockId);
						}
					}
				}
			}
		}
		
		storeDataBlocks();
		
		storeComplexBlocks();
		
		checkEntities = getCraftEntities();

		// first pass, remove all items that need a support
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = sizeY - 1; y >0; y--) {
				//for (int y = 0; y < sizeY; y++) {

					short blockId = matrix[x][y][z];

					// craft block, replace by air
					if (BlocksInfo.needsSupport(blockId)) {

						//Block block = world.getBlockAt(posX + x, posY + y, posZ + z);
						Block block = getWorldBlock(x, y, z);

						// special case for doors
						// we need to remove the lower part of the door only, or the door will pop
						// lower part have data 0 - 7, upper part have data 8 - 15
						if (blockId == 64 || blockId == 71) { // wooden door and steel door
							if (block.getData() >= 8)
								continue;
						}
						
						if(blockId == 26) { //bed
							if(block.getData() > 4)
								continue;
						}

						setBlock(0, block);
					}
				}
			}
		}

		// second pass, the regular blocks
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				//for (int y = sizeY - 1; y > -1; y--) {
				for (int y = 0; y < sizeY; y++) {

					short blockId = matrix[x][y][z];

					// if(blockId==8)
					// System.out.println("water !");

					//Block block = world.getBlockAt(posX + x, posY + y, posZ + z);
					Block block = getWorldBlock(x, y, z);

					// craft block
					if (blockId != -1) {

						// old block postion (remove)
						if (x - dx >= 0 && y - dy >= 0 && z - dz >= 0
								&& x - dx < sizeX && y - dy < sizeY
								&& z - dz < sizeZ) {
							// after moving, this location is not a craft block anymore
							if (matrix[x - dx][y - dy][z - dz] == -1
									|| BlocksInfo.needsSupport(matrix[x - dx][y - dy][z - dz])) {
								if (y > waterLevel || !(type.canNavigate || type.canDive)) {
									//|| matrix [ x - dx ] [ y - dy ] [ z - dz ] == 0)
									setBlock(0, block);
								}
								else
									setBlock(waterType, block);
							}
							// the back of the craft, remove
						} else {
							if (y > waterLevel ||
									!(type.canNavigate || type.canDive) ||
									type.canDig)
								setBlock(0, block);
							else
								setBlock(waterType, block);
						}

						// new block position (place)
						if (!BlocksInfo.needsSupport(blockId)) {

							//Block innerBlock = world.getBlockAt(posX + dx + x,posY + dy + y, posZ + dz + z);
							Block innerBlock = getWorldBlock(dx + x, dy + y, dz + z);

							//drop the item corresponding to the block if it is not a craft block
							if(!isCraftBlock(dx + x,dy + y, dz + z)){
								MoveCraft.instance.dropItem(innerBlock);
							}
							
							//A BREAKA THE DRILL BLOCKA
							if(type.digBlockDurability > 0) {
								int blockDurability = block.getType().getMaxDurability();
								int num = ( (new Random()).nextInt( Math.abs( blockDurability - 0 ) + 1 ) ) + 0;
								
								if(num == 1) {
									MoveCraft.instance.DebugMessage("Random = 1");
									continue;
								}
								else
									MoveCraft.instance.DebugMessage("Random number = " + Integer.toString(num));
							}

							// inside the craft, the block is different
							if (x + dx >= 0 && y + dy >= 0 && z + dz >= 0
									&& x + dx < sizeX && y + dy < sizeY
									&& z + dz < sizeZ) {
								if (matrix[x][y][z] != matrix[x + dx][y + dy][z + dz]) {
									// setBlock(world, blockId, posX + dx + x,
									// posY + dy + y, posZ + dz + z);
									setBlock(blockId, innerBlock);
								}
							}
							// outside of the previous bounding box
							else {
								setBlock(blockId, innerBlock);
							}
						}
					}
				}
			}
		}

		restoreDataBlocks(dx, dy, dz);
		restoreComplexBlocks(dx, dy, dz);

		// restore items that need a support but are not data blocks
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {

					short blockId = matrix[x][y][z];

					if (BlocksInfo.needsSupport(blockId)
							&& !BlocksInfo.isDataBlock(blockId)
							&& !BlocksInfo.isComplexBlock(blockId)) {
						//setBlock(blockId, world.getBlockAt(posX + dx + x, posY + dy + y, posZ + dz + z));
						setBlock(blockId, getWorldBlock(dx + x, dy + y, dz + z));						
					}
				}
			}
		}
		
		for(Entity e : checkEntities) {
			if(isOnCraft(e, false)) {
				if(MoveCraft.instance.ConfigSetting("TryNudge").equalsIgnoreCase("true") &&
						(type.listenMovement == false || e != player) ) {
					movePlayer(e, dx, dy, dz);
				} else {
					teleportPlayer(e, dx, dy, dz);
				}
			}
		}

		minX += dx;
		minY += dy;
		minZ += dz;
		maxX = minX + sizeX - 1;
		maxY = minY + sizeY - 1;
		maxZ = minZ + sizeZ - 1;

		// adjust water level
		// if(waterLevel <= -1)

		if (waterLevel == sizeY - 1 && newWaterLevel < waterLevel) {
			waterLevel = newWaterLevel;
		} else if (waterLevel <= -1 && newWaterLevel > waterLevel) {
			waterLevel = newWaterLevel;
		} else if (waterLevel >= 0 && waterLevel < sizeY - 1) {
			waterLevel -= dy;
		}

		lastMove = System.currentTimeMillis();

		if(type.requiresRails) {
			int xMid = matrix.length / 2;
			int zMid = matrix[0][0].length / 2;

			//Block belowBlock = world.getBlockAt(posX + xMid, posY - 1, posZ + zMid);
			Block belowBlock = getWorldBlock(xMid, -1, zMid);
			railBlock = belowBlock;

			if(belowBlock.getType() == Material.RAILS) {
				railMove();
			}
		}

	}
	
	public void teleportPlayer(Entity p, int dx, int dy, int dz) {
		MoveCraft.instance.DebugMessage("Teleporting entity " + p.getEntityId());
		Location pLoc = p.getLocation();
		pLoc.setX(pLoc.getX() + dx);
		pLoc.setY(pLoc.getY() + dy);
		pLoc.setZ(pLoc.getZ() + dz);
		p.teleport(pLoc);
	}
	
	public void movePlayer(Entity p, int dx, int dy, int dz) {
		MoveCraft.instance.DebugMessage("Moving player");
		int mcSpeed = speed;
		if(mcSpeed > 2)
			mcSpeed = 2;
		
		//double emm = Double.parseDouble(MoveCraft.instance.ConfigSetting("ExperimentalMovementMultiplier"));
		Vector pVel = p.getVelocity();
		//pVel = pVel.add(new Vector(dx * speed, dy * speed, dz * speed));
		//MoveCraft.instance.DebugMessage("Moving player X by " + dx + " * " + mcSpeed + " * " + emm);
		//MoveCraft.instance.DebugMessage("Moving player Z by " + dz + " * " + mcSpeed + " * " + emm);
		if(dx > 0) dx = speed;
		else dx = speed * -1;
		if(dy > 0) dy = speed;
		else dy = speed * -1;
		if(dz > 0) dz = speed;
		else dz = speed * -1;
		pVel = pVel.add(new Vector(dx, dy, dz));
		//pVel = new Vector(dx * mcSpeed, dy * mcSpeed, dz * mcSpeed);
		//pVel.setY(pVel.getY() / 2);
		
		if(pVel.getX() > 10 || pVel.getZ() > 10 || pVel.getY() > 10) {
			//p.sendMessage("I have to teleport you.");
			System.out.println("Velocity is too high, have to teleport " + p.getEntityId());
			Location pLoc = p.getLocation();
			pLoc.setX(pLoc.getX() + pVel.getX());
			pLoc.setY(pLoc.getY() + pVel.getY());
			pLoc.setZ(pLoc.getZ() + pVel.getZ());
			p.teleport(pLoc);						
		} else {
			p.setVelocity(pVel);
		}		
	}
	
	public ArrayList<Entity> getCraftEntities() {
		ArrayList<Entity> checkEntities = new ArrayList<Entity>();

		Chunk firstChunk = world.getChunkAt(new Location(world, minX, minY, minZ));
		Chunk lastChunk = world.getChunkAt(new Location(world, minX + sizeX, minY + sizeY, minZ + sizeZ));

		for(int x = 0; Math.abs(firstChunk.getX() - lastChunk.getX()) >= x; x++) {
			int targetX = 0;
			if(firstChunk.getX() < lastChunk.getX()) {
				targetX = firstChunk.getX() + x;
			} else {
				targetX = firstChunk.getX() - x;
			}
			for(int z = 0; Math.abs(firstChunk.getZ() - lastChunk.getZ()) >= z; z++) {
				int targetZ = 0;
				if(firstChunk.getZ() < lastChunk.getZ()) {
					targetZ = firstChunk.getZ() + z;
				} else {
					targetZ = firstChunk.getZ() - z;
				}

				Chunk addChunk = world.getChunkAt(targetX, targetZ);

				try {
					Entity[] ents = addChunk.getEntities();
					for(Entity e : ents) {
						if(!(e instanceof Item))
							checkEntities.add(e);
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
	
	public boolean AsyncMove(int dx, int dy, int dz) {
		if(MoveCraft.instance.getServer().getScheduler().isCurrentlyRunning(asyncTaskId))
			return false;
		
		final int changeX = dx; 
		final int changeY = dy;
		final int changeZ = dz;
		Runnable r = new Runnable() {
			public void run() {
				//calculatedMove(changeX, changeY, changeZ);
				move(changeX, changeY, changeZ);
			}
		};
		
		asyncTaskId = MoveCraft.instance.getServer().getScheduler().scheduleAsyncDelayedTask(MoveCraft.instance, r);
		return true;
	}

	public void calculatedMove(int dx, int dy, int dz) {
		MoveCraft.instance.DebugMessage("DXYZ is (" + dx + ", " + dy + ", " + dz + ")" );
		//instead of forcing the craft to move, check some things beforehand

		if(this.inHyperSpace) {
			if(dx > 0)
				dx = 1;
			else if (dx < 0)
				dx = -1;
			if(dy > 0)
				dy = 1;
			else if (dy < 0)
				dy = -1;
			if(dz > 0)
				dz = 1;
			else if (dz < 0)
				dz = -1;
			
			Craft_Hyperspace.hyperSpaceMove(this, dx, dy, dz);
			return;
		}			

		if(type.obeysGravity && canMove(dx, dy - 1, dz) && (engineBlocks.size() == 0)) {
			dy -= 1;
		}

		// speed decrease with time
		setSpeed(speed - (int) ((System.currentTimeMillis() - lastMove) / 500));

		if (speed <= 0)
			speed = 1;

		// prevent submarines from getting out of water
		if ( (type.canDive || type.canDig) && !type.canFly && waterLevel <= 0 && dy > 0)
			dy = 0;

		// check the craft can move there. If not, reduce the speed and try
		// again until speed = 1
		while (!canMove(dx, dy, dz)) {

			// player.sendMessage("can't move !");
			//if (speed == 1 && type.obeysGravity) {	//vehicles which obey gravity can go over certain terrain
			if (speed == 1 && type.isTerrestrial) {	//vehicles which are terrestrial (ground-dwelling) can go over certain terrain
				if(canMove(dx, dy + 1, dz)) {
					dy += 1;
					break;
				}
			}

			if (speed == 1) {

				// try to remove horizontal displacement, and just go up
				if (type.canFly && dy >= 0) {
					dx = 0;
					dz = 0;
					dy = 1;
					if (canMove(dx, dy, dz))
						break;
				}

				player.sendMessage(ChatColor.RED + "the " + name + " won't go any further");
				return;
			}

			setSpeed(speed - 1);

		}
		
		if(!(dx == 0 && dy == 0 && dz == 0)) {
			//if async movement is not configured, or the craft can't asyncmove
			//if(!MoveCraft.instance.configFile.ConfigSettings.get("EnableAsyncMovement").equalsIgnoreCase("true") ||
			//		!AsyncMove(dx, dy, dz))
			
			//If the MoveCraft.instance is configured for async movement, it will be used
			//However, using this, if the craft hasn't finished moving before, it won't continue to move...
			if(!MoveCraft.instance.ConfigSetting("EnableAsyncMovement").equalsIgnoreCase("true"))
				move(dx, dy, dz);
			else
				AsyncMove(dx, dy, dz);
		}

		// the craft goes faster every click
		setSpeed(speed + 1);
	}
	
	public void turn(int dr) {
		CraftRotator cr = new CraftRotator(this);
		cr.turn(dr);
	}

	public void engineTick() {
		int dx = 0;
		int dy = 0;
		int dz = 0;
		
		if (type.obeysGravity)
			dy -= 1;
		
		for (DataBlock edb : engineBlocks) {
			Block engineBlock = world.getBlockAt(this.minX + edb.x, this.minY + edb.y, this.minZ + edb.z);
			Block underBlock = world.getBlockAt(engineBlock.getX(), engineBlock.getY() - 1, engineBlock.getZ());			
			Sign sign = (Sign) engineBlock.getState();
			
			if(underBlock.getType() == Material.REDSTONE_WIRE && underBlock.getData() != 0) {
			//if(engineBlock.isBlockPowered()) {

				switch(engineBlock.getData()) {
				case 4:
					System.out.println("NORTH FACING ENGINE");
					dx += 1;
					break;
				case 5:
					System.out.println("SOUTH FACING ENGINE");
					dx -= 1;
					break;
				case 2:
					System.out.println("EAST FACING ENGINE");
					dz += 1;
					break;
				case 3:
					System.out.println("WEST FACING ENGINE");
					dz -= 1;
					break;
				}
				sign.setLine(0, ChatColor.YELLOW + "OOOO");
				sign.setLine(1, ChatColor.YELLOW + "OO" + ChatColor.RED + "OO" + ChatColor.YELLOW + "OO");
				sign.setLine(2, ChatColor.YELLOW + "OO" + ChatColor.RED + "OO" + ChatColor.YELLOW + "OO");
				sign.setLine(3, ChatColor.YELLOW + "OOOO");
			}
			else
			{
				sign.setLine(0, "OOOO");
				sign.setLine(1, "OOOOOO");
				sign.setLine(2, "OOOOOO");
				sign.setLine(3, "OOOO");
			}
		}
		if(dx != 0 || dy != 0 || dz != 0) {			
			calculatedMove(dx, dy, dz);
		}
	}
	
	public void railMove() {
		Byte deets = railBlock.getData();

		if(deets == 1 || deets == 2 || deets == 3) {
			//player.sendMessage("HEADIN NORTH! Or south. Depends on what da orders say.");
			//norf is X
			calculatedMove(1, 0, 0);
		} else
		if(deets == 0 || deets == 4 || deets == 5) {
			calculatedMove(0, 0, 1);			
		}
		//6-9 are turns

		//get the next block
		//check if its material is rails
		//if so, prep another move?
	}
	
	public boolean addWayPoint(Location loc) {
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
