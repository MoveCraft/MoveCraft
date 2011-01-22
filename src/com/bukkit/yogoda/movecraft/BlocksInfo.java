package com.bukkit.yogoda.movecraft;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Joel
 */
public class BlocksInfo {

	public static BlockInfo[] blocks = new BlockInfo[255];

	public static void loadBlocksInfo() {

		// name, isDataBlock, needsSupport, isComplexBlock
		blocks[0] = new BlockInfo("air", false, false, false);
		blocks[1] = new BlockInfo("smoothstone", false, false, false);
		blocks[2] = new BlockInfo("grass", false, false, false);
		blocks[3] = new BlockInfo("dirt", false, false, false);
		blocks[4] = new BlockInfo("cobblestone", false, false, false);
		blocks[5] = new BlockInfo("wood", false, false, false);
		blocks[6] = new BlockInfo("sapling", false, false, false);
		blocks[7] = new BlockInfo("adminium", false, false, false);
		blocks[8] = new BlockInfo("water", true, false, false);
		blocks[9] = new BlockInfo("water", true, false, false);
		blocks[10] = new BlockInfo("lava", true, false, false);
		blocks[11] = new BlockInfo("lava", true, false, false);
		blocks[12] = new BlockInfo("sand", false, false, false);
		blocks[13] = new BlockInfo("gravel", false, false, false);
		blocks[14] = new BlockInfo("gold ore", false, false, false);
		blocks[15] = new BlockInfo("iron ore", false, false, false);
		blocks[16] = new BlockInfo("charcoal", false, false, false);
		blocks[17] = new BlockInfo("trunc", false, false, false);
		blocks[18] = new BlockInfo("foliage", false, false, false);
		blocks[19] = new BlockInfo("sponge", false, false, false);
		blocks[20] = new BlockInfo("glass", false, false, false);
		blocks[35] = new BlockInfo("wool", false, false, false);
		blocks[37] = new BlockInfo("yellow flower", false, true, false);
		blocks[38] = new BlockInfo("red flower", false, true, false);
		blocks[39] = new BlockInfo("brown mushroom", false, true, false);
		blocks[40] = new BlockInfo("red mushroom", false, true, false);
		blocks[41] = new BlockInfo("gold block", false, false, false);
		blocks[42] = new BlockInfo("iron block", false, false, false);
		blocks[43] = new BlockInfo("double steps", false, false, false);
		blocks[44] = new BlockInfo("step", false, false, false);
		blocks[45] = new BlockInfo("brick", false, false, false);
		blocks[46] = new BlockInfo("TNT", false, false, false);
		blocks[47] = new BlockInfo("library", false, false, false);
		blocks[48] = new BlockInfo("mossy cobblestone", false, false, false);
		blocks[49] = new BlockInfo("obsidian", false, false, false);
		blocks[50] = new BlockInfo("torch", true, true, false);
		blocks[51] = new BlockInfo("fire", true, true, false);
		blocks[52] = new BlockInfo("spawner", true, false, false);
		blocks[53] = new BlockInfo("wooden stair", true, false, false);
		blocks[54] = new BlockInfo("chest", true, false, true);
		blocks[55] = new BlockInfo("redstone dust", true, true, false);
		blocks[56] = new BlockInfo("diamond", false, false, false);
		blocks[57] = new BlockInfo("diamond block", false, false, false);
		blocks[58] = new BlockInfo("workbench", false, false, false);
		blocks[59] = new BlockInfo("seed", true, true, false);
		blocks[60] = new BlockInfo("field", true, false, false);
		blocks[61] = new BlockInfo("furnace", true, false, false);
		blocks[62] = new BlockInfo("furnace", true, false, false);
		blocks[63] = new BlockInfo("sign", true, true, true);
		blocks[64] = new BlockInfo("wooden door", true, true, false);
		blocks[65] = new BlockInfo("ladder", true, true, false);
		blocks[66] = new BlockInfo("rail", true, true, false);
		blocks[67] = new BlockInfo("cobblestone stair", true, false, false);
		blocks[68] = new BlockInfo("sign", true, true, true);
		blocks[69] = new BlockInfo("lever", true, true, false);
		blocks[70] = new BlockInfo("pressure plate", true, true, false);
		blocks[71] = new BlockInfo("steel door", true, true, false);
		blocks[72] = new BlockInfo("wooden pressure plate", true, true, false);
		blocks[73] = new BlockInfo("redstone ore", false, false, false);
		blocks[74] = new BlockInfo("redstone ore", false, false, false);
		blocks[75] = new BlockInfo("redstone torch", true, true, false);
		blocks[76] = new BlockInfo("redstone torch", true, true, false);
		blocks[77] = new BlockInfo("stone button", true, true, false);
		blocks[78] = new BlockInfo("snow", false, true, false);
		blocks[79] = new BlockInfo("ice", false, false, false);
		blocks[80] = new BlockInfo("snow block", false, false, false);
		blocks[81] = new BlockInfo("cacti", false, true, false);
		blocks[82] = new BlockInfo("clay", false, false, false);
		blocks[83] = new BlockInfo("reed", true, true, false);
		blocks[84] = new BlockInfo("jukebox", true, false, false);
		blocks[85] = new BlockInfo("fence", true, false, false);
		blocks[86] = new BlockInfo("pumpkin", true, false, false);
		blocks[87] = new BlockInfo("hellstone", false, false, false);
		blocks[88] = new BlockInfo("mud", false, false, false);
		blocks[89] = new BlockInfo("lightstone", false, false, false);
		blocks[90] = new BlockInfo("portal", true, true, false);
		blocks[91] = new BlockInfo("pumpkin", true, false, false);
	}

	public static String getName(int blockId) {

		return blocks[blockId].name;
	}

	public static boolean isDataBlock(int blockId) {
		return blockId != -1 && blocks[blockId].isDataBlock;
	}

	public static boolean isComplexBlock(int blockId) {
		// So far just a sign or a chest
		return blockId != -1 && blocks[blockId].isComplexBlock;
	}

	public static boolean needsSupport(int blockId) {

		return blockId != -1 && blocks[blockId].needSupport;
	}

	private static class BlockInfo {

		String name;
		boolean isDataBlock;
		boolean needSupport;
		boolean isComplexBlock;

		public BlockInfo(String name, boolean isDataBlock, boolean needSupport,
				boolean isComplexBlock) {

			this.name = name;
			this.isDataBlock = isDataBlock;
			this.needSupport = needSupport;
			this.isComplexBlock = isComplexBlock;
		}

	}
}