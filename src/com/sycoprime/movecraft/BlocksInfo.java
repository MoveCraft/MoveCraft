package com.sycoprime.movecraft;

import org.bukkit.Material;

/**
 * BlocksInfo file
 * Defines information on all known blocks that MoveCraft uses for various purposes.
 * @author Joel (Yogoda)
 */
public class BlocksInfo {

	public static BlockInfo[] blocks = new BlockInfo[255];

	public static void loadBlocksInfo() {

		// name, isDataBlock, needsSupport, isComplexBlock, item dropped, amount dropped, is grass cover, cardinal directions
		blocks[0] = new BlockInfo(0,"air", false, false, false, -1, 0, false);
		blocks[1] = new BlockInfo(1,"smoothstone", false, false, false, 4, 1, false);
		blocks[2] = new BlockInfo(2,"grass", false, false, false, 3, 1, false);
		blocks[3] = new BlockInfo(3,"dirt", false, false, false, 3, 1, false);
		blocks[4] = new BlockInfo(4,"cobblestone", false, false, false, false);
		blocks[5] = new BlockInfo(5,"wood", true, false, false, false);
		blocks[6] = new BlockInfo(6,"sapling", true, false, false, false);
		blocks[7] = new BlockInfo(7,"adminium", false, false, false, false);
		blocks[8] = new BlockInfo(8,"water", true, false, false, -1, 0, false);
		blocks[9] = new BlockInfo(9,"water", true, false, false, -1, 0, false);
		blocks[10] = new BlockInfo(10,"lava", true, false, false, -1, 0, false);
		blocks[11] = new BlockInfo(11,"lava", true, false, false, -1, 0, false);
		blocks[12] = new BlockInfo(12,"sand", false, false, false, false);
		blocks[13] = new BlockInfo(13,"gravel", false, false, false, false);
		blocks[14] = new BlockInfo(14,"gold ore", false, false, false, false);
		blocks[15] = new BlockInfo(15,"iron ore", false, false, false, false);
		blocks[16] = new BlockInfo(16,"charcoal", false, false, false, 263, 1, false);
		blocks[17] = new BlockInfo(17,"trunk", true, false, false, false);
		blocks[18] = new BlockInfo(18,"leaves", true, false, false, -1, 0, false);
		blocks[19] = new BlockInfo(19,"sponge", false, false, false, false);
		blocks[20] = new BlockInfo(20,"glass", false, false, false, -1, 0, false);
		blocks[23] = new BlockInfo(23,"dispenser", true, false, true, new byte[] {4, 2, 5, 3});
		blocks[25] = new BlockInfo(25,"note", true, false, false, false);
		blocks[26] = new BlockInfo(26,"bed", true, true, false, 355, 1, false);
		blocks[27] = new BlockInfo(27,"power rail", true, true, false, false);
		blocks[28] = new BlockInfo(28,"detector rail", true, true, false, false);
		blocks[29] = new BlockInfo(29,"sticky piston", true, false, false, false);
		blocks[30] = new BlockInfo(30,"web", false, false, false, false);
		blocks[31] = new BlockInfo(31,"tall grass", true, true, false, false);
		blocks[33] = new BlockInfo(33,"piston", true, false, false, false);
		blocks[34] = new BlockInfo(34,"piston head", true, false, false, false);
		blocks[35] = new BlockInfo(35,"wool", true, false, false, 35, 1, false);
		blocks[37] = new BlockInfo(37,"yellow flower", false, true, false, true);
		blocks[38] = new BlockInfo(38,"red flower", false, true, false, true);
		blocks[39] = new BlockInfo(39,"brown mushroom", false, true, false, true);
		blocks[40] = new BlockInfo(40,"red mushroom", false, true, false, true);
		blocks[41] = new BlockInfo(41,"gold block", false, false, false, 266, 9, false);
		blocks[42] = new BlockInfo(42,"iron block", false, false, false, 265, 9, false);
		blocks[43] = new BlockInfo(43,"double steps", true, false, false, 44, 2, false);
		blocks[44] = new BlockInfo(44,"step", true, false, false, false);
		blocks[45] = new BlockInfo(45,"brick", false, false, false, 336, 4, false);
		blocks[46] = new BlockInfo(46,"TNT", false, false, false, false);
		blocks[47] = new BlockInfo(47,"library", false, false, false, false);
		blocks[48] = new BlockInfo(48,"mossy cobblestone", false, false, false, false);
		blocks[49] = new BlockInfo(49,"obsidian", false, false, false, false);
		blocks[50] = new BlockInfo(50,"torch", true, true, false, false);
		blocks[51] = new BlockInfo(51,"fire", true, true, false, -1, 0, false);
		blocks[52] = new BlockInfo(52,"spawner", true, false, false, false);
		blocks[53] = new BlockInfo(53,"wooden stair", true, false, false, false);
		blocks[54] = new BlockInfo(54,"chest", true, false, true, false);
		blocks[55] = new BlockInfo(55,"redstone dust", true, true, false, 331, 1, false);
		blocks[56] = new BlockInfo(56,"diamond", false, false, false, 264, 1, false);
		blocks[57] = new BlockInfo(57,"diamond block", false, false, false, 264, 9, false);
		blocks[58] = new BlockInfo(58,"workbench", false, false, false, false);
		blocks[59] = new BlockInfo(59,"seed", true, true, false, 295, 1, false);
		blocks[60] = new BlockInfo(60,"field", true, false, false, 3, 1, false);
		blocks[61] = new BlockInfo(61,"furnace", false, true, 4, 8, new byte[] {4, 2, 5, 3}); /* Might need support...*/
		blocks[62] = new BlockInfo(62,"furnace", false, true, 4, 8, new byte[] {4, 2, 5, 3});
		blocks[63] = new BlockInfo(63,"sign", false, true, true, 323, 1, false);
		blocks[64] = new BlockInfo(64,"wooden door", true, true, false, 5, 3, false);
		blocks[65] = new BlockInfo(65,"ladder", true, true, false, false);
		blocks[66] = new BlockInfo(66,"rail", true, true, false, false);
		blocks[67] = new BlockInfo(67,"cobblestone stair", true, false, false, false);
		blocks[68] = new BlockInfo(68,"sign", false, true, true, 323, 1, false);
		blocks[69] = new BlockInfo(69,"lever", true, true, false, false);
		blocks[70] = new BlockInfo(70,"pressure plate", true, true, false, false);
		blocks[71] = new BlockInfo(71,"steel door", true, true, false, 265, 3, false);
		blocks[72] = new BlockInfo(72,"wooden pressure plate", true, true, false, false);
		blocks[73] = new BlockInfo(73,"redstone ore", false, false, false, 331, 4, false);
		blocks[74] = new BlockInfo(74,"redstone ore", false, false, false, 331, 4, false);
		blocks[75] = new BlockInfo(75,"redstone torch", true, true, false, false);
		blocks[76] = new BlockInfo(76,"redstone torch", true, true, false, false);
		blocks[77] = new BlockInfo(77,"stone button", true, true, false, false);
		blocks[78] = new BlockInfo(78,"snow", false, true, false, 332, 1, true);
		blocks[79] = new BlockInfo(79,"ice", false, false, false, false);
		blocks[80] = new BlockInfo(80,"snow block", false, false, false, false);
		blocks[81] = new BlockInfo(81,"cacti", true, true, false, false);
		blocks[82] = new BlockInfo(82,"clay", false, false, false, false);
		blocks[83] = new BlockInfo(83,"reed", true, true, false, 338, 1, false);
		blocks[84] = new BlockInfo(84,"jukebox", true, false, false, false);
		blocks[85] = new BlockInfo(85,"fence", true, false, false, false);
		blocks[86] = new BlockInfo(86,"pumpkin", true, false, false, new byte[] {3, 0, 1, 2});
		blocks[87] = new BlockInfo(87,"hellstone", false, false, false, false);
		blocks[88] = new BlockInfo(88,"mud", false, false, false, false);
		blocks[89] = new BlockInfo(89,"lightstone", false, false, false, false);
		blocks[90] = new BlockInfo(90,"portal", true, true, false, false);
		blocks[92] = new BlockInfo(92,"cake", true, false, false, false);
		blocks[91] = new BlockInfo(91,"pumpkin", true, false, false, new byte[] {3, 0, 1, 2});
		blocks[93] = new BlockInfo(93,"repeater", true, true, false, new byte[] {3, 0, 2, 1});
		blocks[94] = new BlockInfo(94,"repeater", true, true, false, new byte[] {3, 0, 2, 1});
		blocks[96] = new BlockInfo(96,"trapdoor", true, true, false, false);
		blocks[98] = new BlockInfo(98,"stone brick", false, false, false, false);
		//may need to set up some directional info for this
		blocks[101] = new BlockInfo(101,"iron bars", true, false, false, false);
		blocks[102] = new BlockInfo(102,"glass pane", true, false, false, false);
		blocks[103] = new BlockInfo(103, "melon", true, false, false, new byte[] {3, 0, 1, 2});
		//definitely going to need directional info for this
		blocks[107] = new BlockInfo(107,"fence gate", true, false, false, false);

		//bed
		blocks[26].cardinalDirections = new byte[] {1, 2, 3, 0};
		//torch
		blocks[50].cardinalDirections = new byte[] {2, 4, 1, 3};
		//wooden stair
		blocks[53].cardinalDirections = new byte[] {1, 3, 0, 2};
		//sign
		//blocks[63].cardinalDirections = new byte[] {4, 2, 5, 3};
		blocks[63].cardinalDirections = new byte[] {5, 3, 4, 2};
		//wooden door
		blocks[64].cardinalDirections = new byte[] {0, 1, 2, 3};
		//ladder
		//blocks[65].cardinalDirections = new byte[] {4, 2, 5, 3};
		blocks[65].cardinalDirections = new byte[] {5, 3, 4, 2};
		//cobblestone stairs
		blocks[67].cardinalDirections = new byte[] {1, 3, 0, 2};
		//wall sign
		//blocks[68].cardinalDirections = new byte[] {4, 2, 5, 3};
		blocks[68].cardinalDirections = new byte[] {5, 3, 4, 2};
		//lever
		//blocks[69].cardinalDirections = new byte[] {2, 4, 1, 3};
		blocks[69].cardinalDirections = new byte[] {1, 3, 2, 4};
		//steel door
		blocks[71].cardinalDirections = new byte[] {0, 1, 2, 3};
		//restone torch on
		blocks[75].cardinalDirections = new byte[] {2, 4, 1, 3};
		//restone torch off
		blocks[76].cardinalDirections = new byte[] {2, 4, 1, 3};	
		//button		
		//blocks[77].cardinalDirections = new byte[] {4, 1, 3, 2};
		blocks[77].cardinalDirections = new byte[] {3, 1, 4, 2};
		//repeater		
		blocks[93].cardinalDirections = new byte[] {2, 3, 0, 1};
		//repeater (on?)		
		blocks[94].cardinalDirections = new byte[] {2, 3, 0, 1};
		//trapdoor
		blocks[96].cardinalDirections = new byte[] {3, 1, 2, 0};
	}

	public static String getName(int blockId) {
		return Material.getMaterial(blockId).name();
	}

	public static boolean isDataBlock(int blockId) {
		if(blockId != -1 && blocks[blockId] == null) {
			MoveCraft.instance.DebugMessage("blocks(" + blockId + " is null!", 0);
			return false;
		}

		return blockId != -1 && blocks[blockId].isDataBlock;
	}

	public static boolean isComplexBlock(int blockId) {
		if(blockId != -1 && blocks[blockId] == null)
			return false;

		// So far just a sign or a chest
		return blockId != -1 && blocks[blockId].isComplexBlock;
	}

	public static boolean needsSupport(int blockId) {		
		if(blockId == -1)
			return false;
		
		if(blocks[blockId] == null)
			return false;

		return blocks[blockId].needSupport;
	}

	public static boolean coversGrass(int blockId) {
		if(blockId != -1 && blocks[blockId] == null)
			return false;

		return blockId != -1 && blocks[blockId].isGrassCover;
	}

	public static int getDropItem(int blockId){
		if(blockId != -1 && blocks[blockId] == null)
			return -1;

		return blocks[blockId].dropItem;
	}

	public static int getDropQuantity(int blockId){
		if(blockId != -1 && blocks[blockId] == null)
			return 0;

		return blocks[blockId].dropQuantity;
	}
	
	public static int getCardinalDirectionFromData(int BlockId, short BlockData) {
		if(blocks[BlockId].cardinalDirections == null) {
			System.out.println("Tried to get cardinals for " + BlockId + ", which has no cardinals.");
			return -1;
		}
		
		for (int i = 0; i < blocks[BlockId].cardinalDirections.length; i++) {
			if(BlockData == blocks[BlockId].cardinalDirections[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public static String getCardinalDirection(int BlockId, short BlockData) {
		if(blocks[BlockId].cardinalDirections == null)
			return "Woops";
			
		switch(getCardinalDirectionFromData(BlockId, BlockData)) {
		case 0:
			return "North";
		case 1:
			return "East";
		case 2:
			return "West";
		case 3:
			return "South";
		}
		
		return "";
	}
	
	public static byte[] getCardinals(int BlockId) {
		if(blocks[BlockId] == null) {
			System.out.println("NO BLOCK INFO FOUND FOR " + BlockId + "! PANIC!");
			return null;
		}
		
		byte[] returnVal = blocks[BlockId].cardinalDirections; 
		
		if (blocks[BlockId].cardinalDirections == null)
			return null;
		else
			return returnVal;
	}

	public static class BlockInfo {
		int id;
		boolean isDataBlock;
		boolean needSupport;
		boolean isComplexBlock;
		int     dropItem = -1;
		int     dropQuantity = 0;
		boolean isGrassCover; 
		private byte[] cardinalDirections = null;
		//by default, cardinals are usually 4,2,5,3 -> North,East,South,West

		/* Given grasscover */
		private BlockInfo(int id, String name, boolean isDataBlock, boolean needSupport, boolean isComplexBlock, boolean isGrassCover) {
			this(id, name, isDataBlock, needSupport, isComplexBlock, id, 1, isGrassCover);
		}

		/* Given cardinals */
		private BlockInfo(int id, String name, boolean isDataBlock, boolean needSupport, boolean isComplexBlock, byte[] cardinals) {
			this(id, name, isDataBlock, needSupport, isComplexBlock, id, 1, false);
			this.cardinalDirections = cardinals;
			//blocks[id].cardinalDirections = cardinals;
		}

		/* Given cardinals and dropItems */
		//cardinals are North, East, West, South
		private BlockInfo(int id, String name, boolean isDataBlock, boolean isComplexBlock, int dropItem, int dropQuantity, byte[] cardinals) {
			//this(id, name, isDataBlock, false, isComplexBlock, id, 1, false);
			this.id = id;
			this.isDataBlock = isDataBlock;
			this.isComplexBlock = isComplexBlock;
			this.dropItem = dropItem;
			this.dropQuantity = dropQuantity;
			this.cardinalDirections = cardinals;
			//blocks[id].cardinalDirections = cardinals;
		}

		/* Given dropitems */
		private BlockInfo(int id, String name, boolean isDataBlock, boolean needSupport,
				boolean isComplexBlock, int dropItem, int dropQuantity, boolean isGrassCover) {

			this.id = id;
			//this.name = name;
			this.isDataBlock = isDataBlock;
			this.needSupport = needSupport;
			this.isComplexBlock = isComplexBlock;
			this.dropItem = dropItem;
			this.dropQuantity = dropQuantity;
			this.isGrassCover = isGrassCover;
		}

	}
}