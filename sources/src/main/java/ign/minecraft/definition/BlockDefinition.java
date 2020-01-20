/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Map;

import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;

public abstract class BlockDefinition {
	/**
	 * called just before writing the minecraft map data to set blocks
	 * 
	 * @param world
	 * @param x x in the world
	 * @param y y in the world
	 * @param z z in the world
	 * @param bufferX x in 2D zero based coordinate (like the data stored in MinecraftMap)
	 * @param bufferY y in 2D zero based coordinate (like the data stored in MinecraftMap)
	 * @param mapItemColors 
	 */
	public abstract void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors);
	public abstract void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors);
	
	/**
	 * check if it's possible to replace this block when importing some new data
	 * 
	 */
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		return true;
	}

	/**
	 * check if it's possible to put an overlay layer over this block
	 * 
	 */
	public boolean canPutOverlayLayer(int bufferX, int bufferY, int bufferSize) {
		return true;
	}
	
	/**
	 * check if it's possible to put an overlay block (from the overlay layer) over this block
	 * 
	 */
	public boolean canPutOverlayBlock(int bufferX, int bufferY, int bufferSize) {
		return true;
	}
	
	/**
	 * get the level at which the overlay block should be put
	 * 
	 */
	public int getOverlayLevel(int bufferX, int bufferY, int y, int bufferSize) {
		return y + 1;
	}
	
	/**
	 * equal method, useful for nested block definitions
	 */
	public boolean isSameDefinition(int bufferX, int bufferY, int bufferSize, BlockDefinition other) {
		return other == this;
	}
}