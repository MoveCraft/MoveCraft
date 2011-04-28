package com.sycoprime.movecraft;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Joel
 */

// a simple class to store a block location
class BlockLoc {

	int x;
	int y;
	int z;

	public BlockLoc(int x, int y, int z) {

		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof BlockLoc))
			return false;

		BlockLoc block = (BlockLoc) object;
		return x == block.x && y == block.y && z == block.z;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(x).hashCode() >> 13
				^ Integer.valueOf(y).hashCode() >> 7
				^ Integer.valueOf(z).hashCode();
	}
}
