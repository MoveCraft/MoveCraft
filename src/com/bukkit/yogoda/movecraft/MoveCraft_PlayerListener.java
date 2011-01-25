package com.bukkit.yogoda.movecraft;

import javax.imageio.IIOException;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
//import org.bukkit.ItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import org.bukkit.event.player.PlayerChatEvent;

import org.bukkit.event.player.*;

//import com.bukkit.authorblues.GroupUsers.GroupUsers;

public class MoveCraft_PlayerListener extends PlayerListener {
	private final MoveCraft plugin;

	public MoveCraft_PlayerListener(MoveCraft instance) {
		plugin = instance;
	}

	@Override
	public void onPlayerQuit(PlayerEvent event) {
		Player player = event.getPlayer();

		Craft craft = Craft.getCraft(player);

		if (craft != null) {
			Craft.removeCraft(craft);
		}
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		// World world = player.getWorld();

		Craft craft = Craft.getCraft(player);

		if (craft != null) {
			craft.setSpeed(1);

			if (craft.isOnBoard && !craft.isOnCraft(player, false)) {
				player.sendMessage("§eYou get off the " + craft.name);
				player.sendMessage("§7type /" + craft.name
						+ " remote for remote control");
				craft.isOnBoard = false;
				craft.haveControl = false;
			} else if (!craft.isOnBoard && craft.isOnCraft(player, false)) {
				player.sendMessage("§eWelcome on board");
				craft.isOnBoard = true;
				craft.haveControl = true;
			}
		}
	}

	@Override
	public void onPlayerItem(PlayerItemEvent event) {
		Player player = event.getPlayer();
		World world = player.getWorld();

		Craft craft = Craft.getCraft(player);

		if (craft != null) {

			if (craft.blockPlaced) {
				craft.blockPlaced = false;
				return;
			}

			if (craft.blockCount <= 0) {

				plugin.releaseCraft(player, craft);
				return;
			}

			ItemStack pItem = player.getItemInHand();
			int item = pItem.getTypeId();

			if (plugin.DebugMode)
				System.out.println(player.getName() + " used item "
						+ Integer.toString(item));

			// the craft won't budge if you have any tool in the hand
			if (!craft.haveControl ||
			/*
			 * item == 256 || item == 257 || item == 258 ||
			 * 
			 * item == 269 || item == 270 || item == 271 ||
			 * 
			 * item == 273 || item == 274 || item == 275 ||
			 * 
			 * item == 277 || item == 278 || item == 279 ||
			 * 
			 * item == 284 || item == 285 || item == 286 || (item >= 290 && item
			 * <= 294) ||
			 */
			item == 336 // the brick, compatibility with PushBlocks
			)
				return;

			// minimum time between 2 swings
			if (System.currentTimeMillis() - craft.lastMove < 0.2 * 1000)
				return;

			// speed decrease with time
			craft.setSpeed(craft.speed
					- (int) ((System.currentTimeMillis() - craft.lastMove) / 500));

			if (craft.speed <= 0)
				craft.speed = 1;

			float rotation = (float) Math.PI * player.getLocation().getYaw()
					/ 180f;

			// Not really sure what the N stands for...
			float nx = -(float) Math.sin(rotation);
			float nz = (float) Math.cos(rotation);

			int dx = (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
			int dz = (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);

			int dy = 0;

			// we are on a flying object, handle height change
			if (craft.type.canFly || craft.type.canDive) {

				float p = player.getLocation().getPitch();

				dy = -(Math.abs(player.getLocation().getPitch()) >= 25 ? 1 : 0)
						* (int) Math.signum(p);

				// move straight up or straight down
				if (Math.abs(player.getLocation().getPitch()) >= 75) {
					dx = 0;
					dz = 0;
				}
			}

			/*
			 * //check the craft can move there. If not, reduce the speed and
			 * try again until speed = 1 if(!craft.canMove(dx, dy, dz)){
			 * player.sendMessage("§cthe " + craft.name +
			 * " won't go any further"); return; }
			 */

			// System.out.println("waterLevel " + craft.waterLevel);

			// prevent submarines from getting out of water
			if (craft.type.canDive && !craft.type.canFly
					&& craft.waterLevel <= 0 && dy > 0)
				dy = 0;

			// check the craft can move there. If not, reduce the speed and try
			// again until speed = 1
			while (!craft.canMove(world, dx, dy, dz)) {

				// player.sendMessage("can't move !");

				if (craft.speed == 1) {

					// try to remove horizontal displacement, and just go up
					if (craft.type.canFly && dy >= 0) {
						dx = 0;
						dz = 0;
						dy = 1;
						if (craft.canMove(world, dx, dy, dz))
							break;
					}

					player.sendMessage("§cthe " + craft.name
							+ " won't go any further");
					return;
				}

				craft.setSpeed(craft.speed - 1);

			}

			craft.move(world, dx, dy, dz);

			// the craft goes faster every clic
			craft.setSpeed(craft.speed + 1);
		}
	}

	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		Player player = event.getPlayer();
		String[] split = event.getMessage().split(" ");

		/*
		 * //Removed, players can use reloadplugin or bukkit equivalent for the
		 * time being if(split[0].equalsIgnoreCase("/reload") &&
		 * player.canUseCommand("/reload")){ loadProperties(); return false;
		 * //continues default processing } else
		 */
		if(split[0].equalsIgnoreCase("/electrify")){
			Location pLoc = player.getLocation();
			Block block = event.getPlayer().getWorld().getBlockAt(pLoc.getBlockX(), pLoc.getBlockY(), pLoc.getBlockZ());
			lightup.electrify(event.getPlayer().getWorld(), block, 15);
		}else
			if(split[0].equalsIgnoreCase("/woosh")){
				try
				{
					System.out.println("Attempting to apply vector.");
					CraftPlayer craftPlayer = (CraftPlayer) player;
					Vector juan = new Vector();
					juan.setX(500);
					juan.setY(500);
					juan.setZ(500);
					craftPlayer.setVelocity(juan);//new Vector(500, 500, 500));
				}
				catch (ClassCastException e)
				{
					System.out.println("Player -> CraftPlayer not happening.");
				}
			} else
		if (split[0].equalsIgnoreCase("/movecraft")) {
			if (split.length >= 2) {
				if (split[1].equalsIgnoreCase("types")) {

					for (CraftType craftType : CraftType.craftTypes) {

						player.sendMessage("§e " + craftType.name + " :§f "
								+ craftType.minBlocks + "-"
								+ craftType.maxBlocks + " blocks" + " speed : "
								+ craftType.maxSpeed);
					}
				} else if (split[1].equalsIgnoreCase("list")) {
					// list all craft currently controlled by a player

					if (Craft.craftList.isEmpty()) {
						player.sendMessage("§7no player controlled craft");
						// return true;
					}

					for (Craft craft : Craft.craftList) {

						player.sendMessage("§e" + craft.name
								+ " controlled by " + craft.thePlayer.getName()
								+ " : " + craft.blockCount + " blocks");
					}
				} else if (split[1].equalsIgnoreCase("reload")) {

					plugin.loadProperties();
					player.sendMessage("§econfiguration reloaded");
				} else if (split[1].equalsIgnoreCase("debug")) {
					plugin.ToggleDebug();
				}
			}

			player.sendMessage("§aMoveCraft v" + MoveCraft.version
					+ " commands :");
			player.sendMessage("§e/movecraft types "
					+ " : §flist the types of craft available");
			player.sendMessage("§e/movecraft list : §flist the current player controled craft");
			player.sendMessage("§e/movecraft reload : §freload config files");
			player.sendMessage("§e/[craft type] "
					+ " : §fcommands specific to the craft type");
		} else if (split[0].equalsIgnoreCase("release")) {
			plugin.releaseCraft(player, Craft.getCraft(player));
		} else {
			// try to detect a craft command

			String craftName = split[0].substring(1);

			/*
			 * Plugin gu =
			 * plugin.getServer().getPluginManager().getPlugin("GroupUsers"); if
			 * (gu != null) { GroupUsers groupUsers = (GroupUsers) gu; if
			 * (!groupUsers.playerCanUseCommand(player, split[0])) return; }
			 */

			// player.sendMessage(craftName);

			CraftType craftType = CraftType.getCraftType(craftName);

			if (craftType != null) {
				processCommand(craftType, player, split);
			}
		}

		// return false;
	}

	public boolean processCommand(CraftType craftType, Player player,
			String[] split) {

		Craft craft = Craft.getCraft(player);

		if (split.length >= 2) {

			if (split[1].equalsIgnoreCase(craftType.driveCommand)) {

				// try to detect and create the craft
				// use the block the player is standing on
				plugin.createCraft(player, craftType,
						(int) Math.floor(player.getLocation().getX()),
						(int) Math.floor(player.getLocation().getY() - 1),
						(int) Math.floor(player.getLocation().getZ()), null);

				return true;
			} else if (split[1].equalsIgnoreCase("setspeed")) {

				if (craft == null) {
					player.sendMessage("§eYou don't have any "
							+ craftType.name);
					return true;
				}

				int speed = Math.abs(Integer.parseInt(split[2]));

				if (speed < 1 || speed > craftType.maxSpeed) {
					player.sendMessage("§cAllowed speed between 1 and "
							+ craftType.maxSpeed);
					return true;
				}

				craft.setSpeed(speed);
				player.sendMessage("§e" + craft.name + "'s speed set to "
						+ craft.speed);

				return true;
			}
			if (split[1].equalsIgnoreCase("setname")) {

				if (craft == null) {
					player.sendMessage("§eYou don't have any "
							+ craftType.name);
					return true;
				}

				craft.name = split[2];
				player.sendMessage("§e" + craft.name + "'s name set to "
						+ craft.name);
				return true;
			} else if (split[1].equalsIgnoreCase("size")) {

				if (craft == null) {
					player.sendMessage("§eYou don't have any "
							+ craftType.name);
					return true;
				}

				player.sendMessage("§eThe " + craft.name + " is built with "
						+ craft.blockCount + " blocks");
				return true;
			} else if (split[1].equalsIgnoreCase("remote")) {

				if (craft == null) {
					player.sendMessage("§eYou don't have any "
							+ craftType.name);
					return true;
				}

				if (craft.isOnCraft(player, true)) {
					player.sendMessage("§eYou are on the " + craftType.name
							+ ", remote control not possible");
				} else {
					if (craft.haveControl) {
						player.sendMessage("§eYou switch off the remote controller");
					} else {
						player.sendMessage("§eYou switch on the remote controller");
					}

					craft.haveControl = !craft.haveControl;
				}

				return true;
			} else if (split[1].equalsIgnoreCase("release")) {

				plugin.releaseCraft(player, craft);
				return true;

			} else if (split[1].equalsIgnoreCase("info")) {

				player.sendMessage("§a" + craftType.name);
				player.sendMessage("§e" + craftType.minBlocks + "/" + craftType.maxBlocks + " blocks.");
				player.sendMessage("§eMax speed: " + craftType.maxSpeed);

				if (plugin.DebugMode)
					player.sendMessage("§e" + craft.dataBlocks.size() + " data Blocks, " + 
							craft.complexBlocks.size() + " complex Blocks.");
				
				String canDo = "§e" + craftType.name + "s can ";

				if (craftType.canFly)
					canDo += "fly, ";

				if (craftType.canDive)
					canDo += "dive, ";
				
				if(craftType.canDig)
					canDo += "dig, ";

				if (craftType.canNavigate)
					canDo += " navigate on both water and lava, ";
				
				player.sendMessage(canDo);

				if (craftType.flyBlockType != 0) {
					player.sendMessage("§cFlight requirement: "
							+ craftType.flyBlockPercent + "%" + " of "
							+ craftType.flyBlockName);
				}

				return true;

			}
		}

		player.sendMessage("§aMoveCraft v" + MoveCraft.version + " commands :");
		player.sendMessage("§e/" + craftType.name + " "
				+ craftType.driveCommand + " : §f" + " "
				+ craftType.driveCommand + " the " + craftType.name);
		player.sendMessage("§e/" + craftType.name + " "
				+ "release : §frelease the " + craftType.name);
		player.sendMessage("§e/" + craftType.name + " "
				+ "remote : §fremote control of the " + craftType.name);
		player.sendMessage("§e/" + craftType.name + " "
				+ "size : §fthe size of the " + craftType.name + " in block");
		player.sendMessage("§e/" + craftType.name + " "
				+ "setname : §fset the " + craftType.name + "'s name");
		player.sendMessage("§e/" + craftType.name + " "
				+ "info : §fdisplays informations about the " + craftType.name);

		return true;
	}
}
