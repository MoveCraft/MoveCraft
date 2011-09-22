package com.sycoprime.movecraft;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.entity.Player;

import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.sycoprime.movecraft.plugins.PermissionInterface;


public class MoveCraft_BlockListener extends BlockListener {
	public static Craft updatedCraft = null;

	public MoveCraft_BlockListener() {
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if(updatedCraft != null) {
			System.out.println("Updated craft is " + updatedCraft.name + " of type " + updatedCraft.type.name);
			updatedCraft.addBlock(event.getBlock());
			updatedCraft = null;
		}
		/*
		Block blockPlaced = event.getBlock();		

		Craft craft = Craft.getCraft(blockPlaced.getX(),
				blockPlaced.getY(), blockPlaced.getZ());

		if(craft != null) {

		}
		 */
	}

	public static void ClickedASign(Player player, Block block) {
		String world = block.getWorld().getName();
		Craft playerCraft = Craft.getCraft(player);

		Sign sign = (Sign) block.getState();

		if(sign.getLine(0) == null || sign.getLine(0).trim().equals("")) return;

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

			if(playerCraft != null && playerCraft.type == craftType) {
				MoveCraft.instance.releaseCraft(player, playerCraft);
				return;
			}

			//All players can use signs...
			/*
				if(!craftType.canUse(player)){
					player.sendMessage(ChatColor.RED + "You are not allowed to use this type of craft");
					return;
				}
			 */

			//need to update this so that partial player names work
			//will do so when Bukkit permissions are implemented
			String restriction = sign.getLine(2).trim();
			if(!restriction.equals("") && restriction != null) {
				if(restriction != "public" && restriction != player.getName()) {
					if(!PermissionInterface.CheckGroupPermission(world, player, restriction))
						return;
				}
			}

			String name = sign.getLine(1);//.replaceAll("§.", "");

			if(name.trim().equals(""))
				name = null;

			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();

			int direction = block.getData();

			//get the block the sign is attached to
			x = x + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
			z = z + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

			@SuppressWarnings("unused")
			Craft tehCraft = MoveCraft.instance.createCraft(player, craftType, x, y, z, name);

			if(sign.getLine(3).equalsIgnoreCase("center")) {
				//tehCraft.offX = x;
				//tehCraft.offZ = z;
			}

			return;                        
		} else if(craftTypeName.equalsIgnoreCase("engage") && sign.getLine(1).equalsIgnoreCase("hyperdrive")) {
			if(playerCraft == null) {
				player.kickPlayer("Don't.");
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
	
	public static Player matchPlayerName(String subName) {
		Player[] uL = MoveCraft.instance.getServer().getOnlinePlayers();
		ArrayList<Player> userList = new ArrayList<Player>();
		
		for (Player p : uL) {
			if(!p.getName().contains(subName)) {
				userList.add(p);
			}
		}
		
		if(userList.size() == 1) {
			return userList.get(0);
		}
		else {
			System.out.println("Attempted to find player matching " + subName + " but failed.");
			return null;
		}
	}

	public void onSignChange(SignChangeEvent event) {
		MoveCraft.instance.DebugMessage("A SIGN CHANGED!", 3);

		Player player = event.getPlayer();
		String craftTypeName = event.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

		//remove brackets
		if(craftTypeName.startsWith("["))
			craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);

		//if the first line of the sign is a craft type, get the matching craft type.
		CraftType craftType = CraftType.getCraftType(craftTypeName);

		if (craftType != null &&
				!PermissionInterface.CheckPermission(player, "movecraft." + craftTypeName + "." + craftType.driveCommand)) {
			player.sendMessage("You don't have permission to do that!");
			event.setCancelled(true);
		}
	}

	@Override
	public void onBlockPhysics(final BlockPhysicsEvent event)
	{
		if ( !event.isCancelled())
		{
			final Block block = event.getBlock();
			//if (StargateManager.isBlockInGate(block) && (block.getTypeId() != 55))
			//if(Craft.getCraft(block.getX(), block.getY(), block.getZ()) != null)
			if(Craft_Hyperspace.hyperspaceBlocks.contains(block))
			{
				event.setCancelled(true);
			}
		}
	}
	
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		int blockId = event.getBlock().getTypeId();
		Location loc = event.getBlock().getLocation();
		//System.out.println(blockId);
		
		if(blockId == 29 || blockId == 33) {	//piston / sticky piston (base)
			Craft craft = Craft.getCraft(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			
			if (craft != null) {
				craft.player.sendMessage("You just did something with a piston, didn't you?");
			}
		}
	}
}
