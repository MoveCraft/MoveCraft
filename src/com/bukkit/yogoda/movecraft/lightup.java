package com.bukkit.yogoda.movecraft;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class lightup {
	public static void electrify(World world, Block block, int amount){
		if(block.getType() == Material.REDSTONE_WIRE ||
				block.getType() == Material.LEVER ||
				block.getType() == Material.STONE_BUTTON ||
				block.getType() == Material.STONE_PLATE ||
				block.getType() == Material.WOOD_PLATE)
		{
			block.setData((byte) amount);
			//explore all directions

			//face-face connection
			electrify(world, world.getBlockAt(block.getX() + 1, block.getY(), block.getZ()), amount - 1);
			electrify(world, world.getBlockAt(block.getX() - 1, block.getY(), block.getZ()), amount - 1);
			electrify(world, world.getBlockAt(block.getX(), block.getY() + 1, block.getZ()), amount - 1);
			electrify(world, world.getBlockAt(block.getX(), block.getY() - 1, block.getZ()), amount - 1);
			electrify(world, world.getBlockAt(block.getX(), block.getY(), block.getZ() + 1), amount - 1);
			electrify(world, world.getBlockAt(block.getX(), block.getY(), block.getZ() - 1), amount - 1);

			//edge-edge horizontal connection
			electrify(world, world.getBlockAt(block.getX() + 1, block.getY() - 1, block.getZ()), amount - 1);
			electrify(world, world.getBlockAt(block.getX() - 1, block.getY() - 1, block.getZ()), amount - 1);
			electrify(world, world.getBlockAt(block.getX(), block.getY() - 1, block.getZ() + 1), amount - 1);
			electrify(world, world.getBlockAt(block.getX(), block.getY() - 1, block.getZ() - 1), amount - 1);
			electrify(world, world.getBlockAt(block.getX() + 1, block.getY() + 1, block.getZ()), amount - 1);
			electrify(world, world.getBlockAt(block.getX() - 1, block.getY() + 1, block.getZ()), amount - 1);
			electrify(world, world.getBlockAt(block.getX(), block.getY() + 1, block.getZ() + 1), amount - 1);
			electrify(world, world.getBlockAt(block.getX(), block.getY() + 1, block.getZ() - 1), amount - 1);
		}
	}
}
