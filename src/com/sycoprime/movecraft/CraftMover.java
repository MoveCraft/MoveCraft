package com.sycoprime.movecraft;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import com.sycoprime.movecraft.events.MoveCraftMoveEvent;

public class CraftMover {
	public Craft craft;

	public CraftMover(Craft c) {
		craft = c;
	}

	public void setBlock(int id, Block block) {		
		// if(y < 0 || y > 127 || id < 0 || id > 255){
		if (id < 0 || id > 255) {
			// + " x=" + x + " y=" + y + " z=" + z);
			System.out.println("Invalid block type ID. Begin panic.");
			return;
		}
		
		/*
		if(id == 65 && MoveCraft.instance.DebugMode) {
			System.out.println("I expectificated this.");
			Exception ex = new Exception();
			ex.printStackTrace();
		}
		*/
		
		if(block.getTypeId() == id) {
			MoveCraft.instance.DebugMessage("Tried to change a " + id + " to itself.", 5);
			return;
		}
		
		MoveCraft.instance.DebugMessage("Attempting to set block at " + block.getX() + ", "
				 + block.getY() + ", " + block.getZ() + " to " + id, 5);
		
		if (block.setTypeId(id) == false) {
			if(craft.world.getBlockAt(block.getLocation()).setTypeId(id) == false)
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
	
	public Block getWorldBlock(int x, int y, int z) {
		//return world.getBlockAt(posX + x, posY + y, posZ + z);
		return craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z);
	}
	
	public void storeDataBlocks() {
		for (DataBlock dataBlock : craft.dataBlocks) {
			dataBlock.data = getWorldBlock(dataBlock.x, dataBlock.y, dataBlock.z).getData();
		}
	}
	
	public void storeComplexBlocks() {	// store the data of all complex blocks, or die trying
		Block currentBlock;
		
		for (DataBlock complexBlock : craft.complexBlocks) {
			currentBlock = getWorldBlock(complexBlock.x, complexBlock.y, complexBlock.z);
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
				MoveCraft.instance.DebugMessage("Inventory is " + inventory.getSize(), 4);
				for(int slot = 0; slot < inventory.getSize(); slot++) {
					if(inventory.getItem(slot).getTypeId() != 0 && inventory.getItem(slot) != null) {
						//complexBlock.setItem(slot, inventory.getItem(slot).getTypeId(), inventory.getItem(slot).getAmount());
						complexBlock.setItem(slot, inventory.getItem(slot));
						//inventory.setItem(slot, new ItemStack(0));

						MoveCraft.instance.DebugMessage("Inventory has " + inventory.getItem(slot).getAmount() + 
								" inventory item of craft.type " + inventory.getItem(slot).getTypeId() + 
								" in slot " + slot, 4);
						
						inventory.setItem(slot, null);
					}
				}
			}
		}
	}
	
	public void restoreDataBlocks(int dx, int dy, int dz) {
		Block block;
		
		for (DataBlock dataBlock : craft.dataBlocks) {			
			// this is a pop item, the block needs to be created
			if (BlocksInfo.needsSupport(craft.matrix[dataBlock.x][dataBlock.y][dataBlock.z])) {
				block = getWorldBlock(dx + dataBlock.x, dy + dataBlock.y, dz + dataBlock.z);

				setBlock(craft.matrix[dataBlock.x][dataBlock.y][dataBlock.z], block);
				//block.setcraft.typeId(matrix[dataBlock.x][dataBlock.y][dataBlock.z]);
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
		Block theBlock;
		Inventory inventory;
		
		for (DataBlock complexBlock : craft.complexBlocks) {
			theBlock = getWorldBlock(dx + complexBlock.x,
					dy + complexBlock.y,
					dz + complexBlock.z);
			
			theBlock.setData((byte) complexBlock.data);
			
			inventory = null;

			if (complexBlock.id == 63 || complexBlock.id == 68) {
				MoveCraft.instance.DebugMessage("Restoring a sign.", 2);
				setBlock(complexBlock.id, theBlock);
				//theBlock.setcraft.typeId(complexBlock.id);
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
								" inventory item of craft.type " + complexBlock.items[slot].getTypeId() + 
								" in slot " + slot, 4);
					}
				}			
			}
			
			if(theBlock.getTypeId() == 54)
				((Chest)theBlock.getState()).update();
		}		
	}
	


	public void calculatedMove(int dx, int dy, int dz) {
		MoveCraft.instance.DebugMessage("DXYZ is (" + dx + ", " + dy + ", " + dz + ")", 4);
		//instead of forcing the craft to move, check some things beforehand

		if(craft.inHyperSpace) {
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
			
			Craft_Hyperspace.hyperSpaceMove(craft, dx, dy, dz);
			return;
		}			

		if(craft.type.obeysGravity && craft.canMove(dx, dy - 1, dz) && (craft.engineBlocks.size() == 0)) {
			dy -= 1;
		}

		// craft.speed decrease with time
		craft.setSpeed(craft.speed - (int) ((System.currentTimeMillis() - craft.lastMove) / 500));

		if (craft.speed <= 0)
			craft.speed = 1;

		// prevent submarines from getting out of water
		if ( (craft.type.canDive || craft.type.canDig) && !craft.type.canFly && craft.waterLevel <= 0 && dy > 0)
			dy = 0;

		// check the craft can move there. If not, reduce the craft.speed and try
		// again until craft.speed = 1
		while (!craft.canMove(dx, dy, dz)) {

			// player.sendMessage("can't move !");
			//if (craft.speed == 1 && type.obeysGravity) {	//vehicles which obey gravity can go over certain terrain
			if (craft.speed == 1 && craft.type.isTerrestrial) {	//vehicles which are terrestrial (ground-dwelling) can go over certain terrain
				if(craft.canMove(dx, dy + 1, dz)) {
					dy += 1;
					break;
				}
			}

			if (craft.speed == 1) {

				// try to remove horizontal displacement, and just go up
				if (craft.type.canFly && dy >= 0) {
					dx = 0;
					dz = 0;
					dy = 1;
					if (craft.canMove(dx, dy, dz))
						break;
				}

				craft.player.sendMessage(ChatColor.RED + "the " + craft.name + " won't go any further");
				return;
			}

			craft.setSpeed(craft.speed - 1);

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
		craft.setSpeed(craft.speed + 1);
	}

	// move the craft according to a vector d
	public void move(int dx, int dy, int dz) {
		//if(craft.type.canDig) {
		MoveCraft.instance.DebugMessage("craft.waterLevel is " + craft.waterLevel, 4);
		MoveCraft.instance.DebugMessage("craft.waterType is " + craft.waterType, 4);
		MoveCraft.instance.DebugMessage("newcraft.waterLevel is " + craft.newWaterLevel, 4);
		//}

		dx = craft.speed * dx;
		dz = craft.speed * dz;

		MoveCraftMoveEvent event = new MoveCraftMoveEvent(craft, dx, dy, dz);
		MoveCraft.instance.getServer().getPluginManager().callEvent(event);   

		if (event.isCancelled()) {
			return;
		}
		dx = (int) event.getMovement().getX();
		dy = (int) event.getMovement().getY();
		dz = (int) event.getMovement().getZ();

		if(craft.type.canDig)
			craft.waterLevel = craft.newWaterLevel;

		ArrayList<Entity> checkEntities;

		if (Math.abs(craft.speed * dy) > 1) {
			dy = craft.speed * dy / 2;
			if (Math.abs(dy) == 0)
				dy = (int) Math.signum(dy);
		}

		structureUpdate();
		
		storeDataBlocks();
		
		storeComplexBlocks();
		
		checkEntities = craft.getCraftEntities();

		// first pass, remove all items that need a support
		removeSupportBlocks();

		short blockId;
		Block block;
		Block innerBlock;
		
		// second pass, the regular blocks				
		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = 0; y < craft.sizeY; y++) {
					//for (int y = craft.sizeY - 1; y > -1; y--) {

					blockId = craft.matrix[x][y][z];

					block = getWorldBlock(x, y, z);

					if (blockId == -1)
						continue;

						// old block position (remove)
						if (x - dx >= 0 && y - dy >= 0 && z - dz >= 0
								&& x - dx < craft.sizeX && y - dy < craft.sizeY
								&& z - dz < craft.sizeZ) {
							
							// after moving, this location is not a craft block anymore
							if (craft.matrix[x - dx][y - dy][z - dz] == -1
									|| BlocksInfo.needsSupport(craft.matrix[x - dx][y - dy][z - dz])) {
								if (y > craft.waterLevel || !(craft.type.canNavigate || craft.type.canDive)) {
									//|| craft.matrix [ x - dx ] [ y - dy ] [ z - dz ] == 0)
									setBlock(0, block);
								}
								else
									setBlock(craft.waterType, block);
							}
						} else { // the back of the craft, remove
							if (y > craft.waterLevel ||
									!(craft.type.canNavigate || craft.type.canDive) ||
									craft.type.canDig)
								setBlock(0, block);		//the promised land!!!
							else
								setBlock(craft.waterType, block);
						}

						// new block position (place)
						if (!BlocksInfo.needsSupport(blockId)) {

							//Block innerBlock = world.getBlockAt(posX + dx + x,posY + dy + y, posZ + dz + z);
							innerBlock = getWorldBlock(dx + x, dy + y, dz + z);

							//drop the item corresponding to the block if it is not a craft block
							if(!craft.isCraftBlock(dx + x,dy + y, dz + z)) {
								MoveCraft.instance.dropItem(innerBlock);
							}
							
							if(craft.type.digBlockDurability > 0) { //break drill bits
								int blockDurability = block.getType().getMaxDurability();
								int num = ( (new Random()).nextInt( Math.abs( blockDurability - 0 ) + 1 ) ) + 0;
								
								if(num == 1) {
									MoveCraft.instance.DebugMessage("Random = 1", 1);
									continue;
								}
								else
									MoveCraft.instance.DebugMessage("Random number = " + Integer.toString(num), 1);
							}

							// inside the craft, the block is different
							if (x + dx >= 0 && y + dy >= 0 && z + dz >= 0
									&& x + dx < craft.sizeX && y + dy < craft.sizeY
									&& z + dz < craft.sizeZ) {
								if (craft.matrix[x][y][z] != craft.matrix[x + dx][y + dy][z + dz]) {
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

		restoreDataBlocks(dx, dy, dz);
		restoreComplexBlocks(dx, dy, dz);
		
		restoreSupportBlocks(dx, dy, dz);
		
		for(Entity e : checkEntities) {
			if(craft.isOnCraft(e, false)) {
				if(MoveCraft.instance.ConfigSetting("TryNudge").equalsIgnoreCase("true") &&
						(craft.type.listenMovement == false || e != craft.player) ) {
					movePlayer(e, dx, dy, dz);
				} else {
					teleportPlayer(e, dx, dy, dz);
				}
			}
		}

		craft.minX += dx;
		craft.minY += dy;
		craft.minZ += dz;
		craft.maxX = craft.minX + craft.sizeX - 1;
		craft.maxY = craft.minY + craft.sizeY - 1;
		craft.maxZ = craft.minZ + craft.sizeZ - 1;

		// adjust water level
		// if(craft.waterLevel <= -1)

		if (craft.waterLevel == craft.sizeY - 1 && craft.newWaterLevel < craft.waterLevel) {
			craft.waterLevel = craft.newWaterLevel;
		} else if (craft.waterLevel <= -1 && craft.newWaterLevel > craft.waterLevel) {
			craft.waterLevel = craft.newWaterLevel;
		} else if (craft.waterLevel >= 0 && craft.waterLevel < craft.sizeY - 1) {
			craft.waterLevel -= dy;
		}

		craft.lastMove = System.currentTimeMillis();

		if(craft.type.requiresRails) {
			int xMid = craft.matrix.length / 2;
			int zMid = craft.matrix[0][0].length / 2;

			//Block belowBlock = world.getBlockAt(posX + xMid, posY - 1, posZ + zMid);
			Block belowBlock = getWorldBlock(xMid, -1, zMid);
			craft.railBlock = belowBlock;

			if(belowBlock.getType() == Material.RAILS) {
				railMove();
			}
		}

	}

	// scan to know if any of the craft blocks are now missing (blocks removed, TNT damage, creeper ?)
	// and update the structure
	public void structureUpdate() {
		short craftBlockId;
		int blockId;
		
		for (int x = 0; x < craft.sizeX; x++) {
			for (int y = 0; y < craft.sizeY; y++) {
				for (int z = 0; z < craft.sizeZ; z++) {
					craftBlockId = craft.matrix[x][y][z];

					// remove blocks from the structure if it is not there anymore
					if (craftBlockId != -1 && craftBlockId != 0
							&& !(craftBlockId >= 8 && craftBlockId <= 11)) {

						//int blockId = world.getBlockAt(posX + x, posY + y, posZ + z).getTypeId();
						blockId = craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z).getTypeId();

						 // regenerate TNT on a bomber
						if (craftBlockId == 46 && craft.type.bomber)
							continue;

						// block is not here anymore, remove it
						if (blockId == 0 || blockId >= 8 && blockId <= 11) {
							// air, water, or lava
							if (craft.waterType != 0 && y <= craft.waterLevel)
								craft.matrix[x][y][z] = 0;
							else
								craft.matrix[x][y][z] = -1; // make a hole in the craft

							craft.blockCount--;
							MoveCraft.instance.DebugMessage("Removing a block of craft.type " + craftBlockId + 
									" because of craft.type " + blockId, 4);
						}
					}
				}
			}
		}
	}
	
	public void removeSupportBlocks() {
		short blockId;
		Block block;
		
		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = craft.sizeY - 1; y > -1; y--) {
				//for (int y = 0; y < craft.sizeY; y++) {

					blockId = craft.matrix[x][y][z];

					// craft block, replace by air
					if (BlocksInfo.needsSupport(blockId)) {

						//Block block = world.getBlockAt(posX + x, posY + y, posZ + z);
						block = getWorldBlock(x, y, z);

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
	}

	// restore items that need a support but are not data blocks
	public void restoreSupportBlocks(int dx, int dy, int dz) {
		short blockId;

		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = 0; y < craft.sizeY; y++) {

					blockId = craft.matrix[x][y][z];

					if (BlocksInfo.needsSupport(blockId)
							&& !BlocksInfo.isDataBlock(blockId)
							&& !BlocksInfo.isComplexBlock(blockId)) {
						//setBlock(blockId, world.getBlockAt(posX + dx + x, posY + dy + y, posZ + dz + z));
						setBlock(blockId, getWorldBlock(dx + x, dy + y, dz + z));						
					}
				}
			}
		}
	}
	
	public void teleportPlayer(Entity p, int dx, int dy, int dz) {
		MoveCraft.instance.DebugMessage("Teleporting entity " + p.getEntityId(), 4);
		Location pLoc = p.getLocation();
		pLoc.setX(pLoc.getX() + dx);
		pLoc.setY(pLoc.getY() + dy);
		pLoc.setZ(pLoc.getZ() + dz);
		p.teleport(pLoc);
	}
	
	public void movePlayer(Entity p, int dx, int dy, int dz) {
		MoveCraft.instance.DebugMessage("Moving player", 4);
		int mccraftspeed = craft.speed;
		if(mccraftspeed > 2)
			mccraftspeed = 2;
		
		//double emm = Double.parseDouble(MoveCraft.instance.ConfigSetting("ExperimentalMovementMultiplier"));
		Vector pVel = p.getVelocity();
		//pVel = pVel.add(new Vector(dx * craft.speed, dy * craft.speed, dz * craft.speed));
		//MoveCraft.instance.DebugMessage("Moving player X by " + dx + " * " + mccraft.speed + " * " + emm);
		//MoveCraft.instance.DebugMessage("Moving player Z by " + dz + " * " + mccraft.speed + " * " + emm);
		if(dx > 0) dx = craft.speed;
		else dx = craft.speed * -1;
		if(dy > 0) dy = craft.speed;
		else dy = craft.speed * -1;
		if(dz > 0) dz = craft.speed;
		else dz = craft.speed * -1;
		pVel = pVel.add(new Vector(dx, dy, dz));
		//pVel = new Vector(dx * mccraft.speed, dy * mccraft.speed, dz * mccraft.speed);
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
	
	public boolean AsyncMove(int dx, int dy, int dz) {
		if(MoveCraft.instance.getServer().getScheduler().isCurrentlyRunning(craft.asyncTaskId))
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
		
		craft.asyncTaskId = MoveCraft.instance.getServer().getScheduler().scheduleAsyncDelayedTask(MoveCraft.instance, r);
		return true;
	}
	
	public void railMove() {
		Byte deets = craft.railBlock.getData();

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
}
