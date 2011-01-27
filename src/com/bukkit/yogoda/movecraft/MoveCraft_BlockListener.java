package com.bukkit.yogoda.movecraft;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.entity.Player;

import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.plugin.Plugin;

public class MoveCraft_BlockListener extends BlockListener {
	private final MoveCraft plugin;

	public MoveCraft_BlockListener(MoveCraft instance) {
		plugin = instance;
	}

	//@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		// World world = player.getWorld();
		Block blockPlaced = event.getBlockPlaced();

		plugin.DebugMessage(player.getName() + " placed a block.");

		Craft playerCraft = Craft.getCraft(player);

		if (blockPlaced != null) {

			// try to find a craft at this location
			Craft craft = Craft.getCraft(blockPlaced.getX(),
					blockPlaced.getY(), blockPlaced.getZ());

			// if there is a craft, add the block to it
			if (craft != null) {

				if (blockPlaced.getTypeId() == 321 || // picture
						blockPlaced.getTypeId() == 323 || // sign
						blockPlaced.getTypeId() == 324 || // door
						blockPlaced.getTypeId() == 330) { // door

					player.sendMessage(ChatColor.YELLOW + "please release the "
							+ craft.type.name + " to add this item");
					return;
				}

				craft.addBlock(blockPlaced);
			}

			if (playerCraft != null)
				playerCraft.blockPlaced = true;
		}

		/*
		 * int data = etc.getServer().getBlockData(blockClicked.getX(),
		 * blockClicked.getY(), blockClicked.getZ());
		 * player.sendMessage("clicked " + data);
		 * 
		 * data = etc.getServer().getBlockData(blockPlaced.getX(),
		 * blockPlaced.getY(), blockPlaced.getZ()); player.sendMessage("placed "
		 * + data);
		 */
		
		if (blockPlaced.getState() instanceof Sign) {

			if (playerCraft == null) {
				Sign sign = (Sign) blockPlaced.getState();
				
				// if the first line of the sign is a craft type, get the
				// matching craft type.
				CraftType craftType = CraftType.getCraftType(sign.getLine(0).trim());

				// it is a registered craft type !
				if (craftType != null) {

					int x = blockPlaced.getX();
					int y = blockPlaced.getY();
					int z = blockPlaced.getZ();

					int direction = blockPlaced.getData();

					// get the block the sign is attached to (not rly needed
					// lol)
					x = x + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
					z = z + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

					String name = sign.getLine(1);
					if (name.trim().equals(""))
						name = null;

					plugin.createCraft(player, craftType, x, y, z, name);
				}
			} else {

				plugin.releaseCraft(player, playerCraft);
			}
		}
	}

	/*
	 * @Override public void onBlockInteract (BlockInteractEvent event) { Player
	 * player = null; if(event.isPlayer()) player = (Player) event.getEntity();
	 * 
	 * if(player != null) player.sendMessage("Interact event."); }
	 */

	//@Override
	public void onBlockRightClick(BlockRightClickEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Craft playerCraft = Craft.getCraft(player);


		if (block.getState() instanceof Sign) {
			if(playerCraft == null){
				Sign sign = (Sign) block.getState();

				if(sign.getLine(0).trim().equals("")) return;

				String craftTypeName = sign.getLine(0).trim().toLowerCase();;

				//remove colors
				craftTypeName = craftTypeName.replaceAll("§.", "");

				//remove brackets
				if(craftTypeName.startsWith("["))
					craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);

				//if the first line of the sign is a craft type, get the matching craft type.
				CraftType craftType = CraftType.getCraftType(craftTypeName);

				//it is a registred craft type !
				if(craftType != null){

                       if(!craftType.canUse(player)){
                            player.sendMessage(ChatColor.RED + "You are not allowed to use this type of craft");
                            return;
                       }

					String name = sign.getLine(1).replaceAll("§.", "");

					if(name.trim().equals(""))
						name = null;

					/*
                        String[] groups = (sign.getLine(2) + " " + sign.getLine(3)).replace(",", "").replace(";", "").split("[ ]");

                        if(!craftType.canUse(player) && !checkPermission(player, groups)){                        
                            player.sendMessage(ChatColor.RED + "You are not allowed to take control of that " + craftType.name + " !");
                            return true;
                        }
					 */

					int x = block.getX();
					int y = block.getY();
					int z = block.getZ();

					int direction = block.getData();

					//get the block the sign is attached to (not rly needed lol)
					x = x + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
					z = z + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

					plugin.createCraft(player, craftType, x, y, z, name);

					return;                        
				} else {                    
					return;
				}
			} else {

				plugin.releaseCraft(player, playerCraft);
			}
		}
	}

	//@Override
	public void onBlockRedstoneChange(BlockFromToEvent event) {
		Block toBlock = event.getToBlock();
		Craft craft = Craft.getCraft(toBlock.getX(), toBlock.getY(),
				toBlock.getZ());
		
		if(toBlock.getType() == Material.REDSTONE_WIRE)
		{
			//plugin.DebugMessage(toBlock.getData());
			Block block = event.getBlock().getWorld().getBlockAt(toBlock.getX(), toBlock.getY() + 1, toBlock.getZ());
			
			if(block.getType() == Material.FURNACE && toBlock.getData() != (byte) 0 ){
				int dx = 0;
				int dy = 0;
				
				plugin.DebugMessage("You are lighting up a furnace with data " + block.getData());
				
				if(block.getData() == 2)
					dy = -1;			
				if(block.getData() == 3)
					dx = -1;
				if(block.getData() == 4)
					dy = 1;	
				if(block.getData() == 5)
					dx = 1;
				
				//new ReminderBeep(10);
			}
		}

		/*
		craft.setSpeed(1);
		int dx = 0;
		int dy = 0;
		craft.move(event.getBlock().getWorld(), dx, dy, 0);
		*/

		// the craft goes faster every clic
		//craft.setSpeed(craft.speed
		//		- (int) ((System.currentTimeMillis() - craft.lastMove) / 500));
		//craft.setSpeed(craft.speed + 1);
	}
}
