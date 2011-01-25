package com.bukkit.yogoda.movecraft;

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

		if (plugin.DebugMode)
			System.out.println(player.getName() + " placed a block.");

		Craft playerCraft = Craft.getCraft(player);

		if (blockPlaced != null) {

			// try to find a craft at this location
			Craft craft = Craft.getCraft(blockPlaced.getX(),
					blockPlaced.getY(), blockPlaced.getZ());

			// if there is a craft, add the block to it
			if (craft != null) {

				// System.out.println("" + blockPlaced.getType());

				if (blockPlaced.getTypeId() == 321 || // picture
						blockPlaced.getTypeId() == 323 || // sign
						blockPlaced.getTypeId() == 324 || // door
						blockPlaced.getTypeId() == 330) { // door

					player.sendMessage("Â§eplease release the "
							+ craft.type.name + " to add this item");
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

		// right-click a sign
		// block...Clicked?
		// if(blockPlaced.getTypeID() == 68){
		if (blockPlaced.getState() instanceof Sign) {

			if (playerCraft == null) {
				Sign sign = (Sign) blockPlaced.getState();

				// if the first line of the sign is a craft type, get the
				// matching craft type.
				CraftType craftType = CraftType.getCraftType(sign.getLine(0)
						.trim());

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

		if (block.getState() instanceof Sign) {
			Sign sign = (Sign) block.getState();
			String craftTypeName = sign.getLine(0).trim().toLowerCase();

			if (plugin.DebugMode)
				player.sendMessage("Crafttypename is " + craftTypeName);

			if (craftTypeName.startsWith("[")) {
				craftTypeName = craftTypeName.substring(1,
						craftTypeName.length() - 1);
			}

			CraftType craftType = CraftType.getCraftType(craftTypeName);

			if (craftType != null) {
				sign.setLine(0, "[§1" + craftType.name + "§0]");

				String name = sign.getLine(1);

				if (name.length() > 0) {
					sign.setLine(1, "§e" + name);
				}
				sign.update();

				// just seeing if this works...need permissions, and if it's
				// already controlled, etc.
				plugin.createCraft(player, craftType,
						(int) Math.floor(player.getLocation().getX()),
						(int) Math.floor(player.getLocation().getY() - 1),
						(int) Math.floor(player.getLocation().getZ()), null);
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
			System.out.println(toBlock.getData());
			Block block = event.getBlock().getWorld().getBlockAt(toBlock.getX(), toBlock.getY() + 1, toBlock.getZ());
			
			if(block.getType() == Material.FURNACE && toBlock.getData() != (byte) 0 ){
				int dx = 0;
				int dy = 0;
				
				System.out.println("You are lighting up a furnace with data " + block.getData());
				
				if(block.getData() == 2)
					dy = -1;			
				if(block.getData() == 3)
					dx = -1;
				if(block.getData() == 4)
					dy = 1;	
				if(block.getData() == 5)
					dx = 1;
				
				new ReminderBeep(10);
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

/*
	public void onBlockFlow(BlockFromToEvent event)
	{
		Plugin wg = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");

		if(wg != null)
		{
			WorldGuardPlugin worldguard = (WorldGuardPlugin) wg;
			World world = event.getBlock().getWorld();
			Block blockFrom = event.getBlock();
			Block blockTo = event.getToBlock();

			boolean isWater = (blockFrom.getTypeId() == 8) || (blockFrom.getTypeId() == 9);
			boolean isLava = (blockFrom.getTypeId() == 10) || (blockFrom.getTypeId() == 11);

			if ((worldguard.simulateSponge) && (isWater)) {
				int ox = blockTo.getX();
				int oy = blockTo.getY();
				int oz = blockTo.getZ();

				for (int cx = -this.plugin.spongeRadius; cx <= this.plugin.spongeRadius; cx++) {
					for (int cy = -this.plugin.spongeRadius; cy <= this.plugin.spongeRadius; cy++) {
						for (int cz = -this.plugin.spongeRadius; cz <= this.plugin.spongeRadius; cz++) {
							if (world.getBlockTypeIdAt(ox + cx, oy + cy, oz + cz) == 19) {
								event.setCancelled(true);
								return;
							}
						}
					}
				}
			}
		}
	}
	*/
}
