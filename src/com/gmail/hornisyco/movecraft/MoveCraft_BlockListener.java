package com.gmail.hornisyco.movecraft;

import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.entity.Player;

import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRightClickEvent;

public class MoveCraft_BlockListener extends BlockListener {
	private final MoveCraft plugin;

	public MoveCraft_BlockListener(MoveCraft instance) {
		plugin = instance;
	}

	//@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block blockPlaced = event.getBlockPlaced();

		plugin.DebugMessage(player.getName() + " placed a block of type " + blockPlaced.getTypeId());

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

					player.sendMessage(ChatColor.YELLOW + "please release the " + craft.type.name + " to add this item");
					return;
				}

				craft.addBlock(blockPlaced);
			}

			if (playerCraft != null)
				playerCraft.blockPlaced = true;
		}

		if (blockPlaced.getState() instanceof Sign) {
			player.sendMessage("This is a sign.");
			/*

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
			*/
		}
	}

	//@Override
	public void onBlockRightClick(BlockRightClickEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Craft playerCraft = Craft.getCraft(player);


		if (block.getState() instanceof Sign) {
			Sign sign = (Sign) block.getState();

			if(sign.getLine(0).trim().equals("")) return;

			String craftTypeName = sign.getLine(0).trim().toLowerCase();;

			//remove colors
			craftTypeName = craftTypeName.replaceAll(ChatColor.BLUE.toString(), "");

			//remove brackets
			if(craftTypeName.startsWith("["))
				craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);

			//if the first line of the sign is a craft type, get the matching craft type.
			CraftType craftType = CraftType.getCraftType(craftTypeName);

			//it is a registred craft type !
			if(craftType != null){

				if(playerCraft != null) {
					if(playerCraft.type == craftType) {
						plugin.releaseCraft(player, playerCraft);
						return;
					}
				}

				//All players can use signs...
				/*
				if(!craftType.canUse(player)){
					player.sendMessage(ChatColor.RED + "You are not allowed to use this type of craft");
					return;
				}
				*/

				String restriction = sign.getLine(1).trim();
				if(!restriction.equals("") && restriction != null) {
					if(restriction != "public" && restriction != player.getName()) {
						if(!PermissionInterface.CheckGroupPermission(player, restriction))
							return;
					}
				}

				String name = sign.getLine(2).replaceAll("§.", "");

				if(name.trim().equals(""))
					name = null;

				int x = block.getX();
				int y = block.getY();
				int z = block.getZ();

				int direction = block.getData();

				//get the block the sign is attached to (not rly needed lol)
				x = x + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
				z = z + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

				plugin.createCraft(player, craftType, x, y, z, name);

				return;                        
			} else if(craftTypeName.equalsIgnoreCase("engage") && sign.getLine(1).equalsIgnoreCase("hyperdrive")) {
				if(playerCraft == null) {
					player.kickPlayer("I am TIRED of these MOTHERFUCKING noobs on this MOTHERFUCKING server.");
					return;
				}
				Craft_Hyperspace.enterHyperSpace(playerCraft);
				sign.setLine(0, "Disengage Hyperdrive");
			} else if(craftTypeName.equalsIgnoreCase("disengage") && sign.getLine(1).equalsIgnoreCase("hyperdrive")) {
				if(playerCraft == null) {
					player.kickPlayer("I am TIRED of these MOTHER____ING noobs on this MOTHER____ING server.");
					return;
				}
				Craft_Hyperspace.exitHyperSpace(playerCraft);
				sign.setLine(0, "Engage Hyperdrive");
			}
		}
	}
}
