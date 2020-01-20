/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ignfab.minetest;

import java.util.Map;
import java.util.Objects;

import ign.minecraft.MinetestMap;

public class BlockMT {
	public int x,y,z; //protected
	public String type; //protected
	
	public BlockMT(int x, int z, int y, String type) {
		// Y is Z in the Minecraft world (thus the constructor is XZY)
		this.x = x;
		this.y = -y;
		this.z = z;
		this.type = type;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void addTo(Map<BlockMT,Object> blockList) {
		blockList.remove(this,MinetestMap.PRESENT);
		if(!this.type.equals("air")) {
			blockList.put(this,MinetestMap.PRESENT);
		}
	}

	public static boolean containsBlock(Map<BlockMT, Object> blockList, int x, int y, int z) {
		return blockList.containsKey(new BlockMT(x,y,z,""));
	}

	public static BlockMT getBlock(Map<BlockMT, Object> blockList, int x, int y, int z) {
		return blockList.entrySet()
		        .stream()
		        .filter(e -> Objects.equals(e.getKey(), new BlockMT(x,y,z,"")))
		        .map(Map.Entry::getKey).findFirst().get();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockMT other = (BlockMT) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
	
}
