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

/**
 * this class implement a block definition that is not for one block but for a group of blocks
 * the grouping being done by an id
 * 
 * @author David Frémont
 * @see IdentifiedBlockFactory
 *
 */
public abstract class IdentifiedBlockDefinition extends BlockDefinition {
	
	//static common storage of block definitions and altitudes
	// to avoid having one full array per instance although there is no overlap
	protected static int mapSize = 0;//gives a max value for x and z values
	protected static IdentifiedBlockDefinition[] blocks;
	protected static int[] altitudes;
	
	public static void setMapSize(int mapSize) {
		IdentifiedBlockDefinition.mapSize = mapSize;
		IdentifiedBlockDefinition.blocks = new IdentifiedBlockDefinition[mapSize * mapSize];
		IdentifiedBlockDefinition.altitudes = new int[mapSize * mapSize];
	}

	protected static int xz1D(int x, int z) {
		return x + z * mapSize;
	}
	
	protected final int id;
	protected boolean preRenderInitialized = false;
	protected boolean preRenderEnded = false;

	public IdentifiedBlockDefinition(Integer id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void addBlock(int x, int y, int z) {
		blocks[ xz1D(x,z) ] = this;
		altitudes[ xz1D(x,z) ] = y;
	}
	
	public static void launchPreRender() {
		//browse all blocks for init and prerender of each block in its group
		for(int x = 0; x < mapSize; x++) {
			for(int z = 0; z < mapSize; z++) {
				if (blocks[xz1D(x,z)] != null) {
					if (!blocks[xz1D(x,z)].preRenderInitialized) {
						blocks[xz1D(x,z)].preRenderInit();
						blocks[xz1D(x,z)].preRenderInitialized = true;
					}
					blocks[xz1D(x,z)].preRender(x, z);
				}
			}
		}
		//browse again for ending the pre render process
		for (int index = 0; index < blocks.length ; index ++) {
			if (blocks[index] != null) {
				if (!blocks[index].preRenderEnded) {
					blocks[index].preRenderEnd();
					blocks[index].preRenderEnded = true;
				}
			}
		}
	}
	
	protected void preRenderInit() {
	}
	protected void preRender(int x, int z) {
	}
	protected void preRenderEnd() {
	}
	
	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		assert bufferSize == mapSize;
		assert (blocks[ xz1D(bufferX, bufferY) ] == this)
			|| ( (OverlayIdentifiedBlockDefinition.class.isAssignableFrom(blocks[ xz1D(bufferX, bufferY) ].getClass()))
					&& ( OverlayIdentifiedBlockDefinition.baseBlocks[xz1D(bufferX, bufferY)] == this ) );
		assert altitudes[ xz1D(bufferX, bufferY) ] == y;
		renderOneBlock(world, x, y, z, bufferX, bufferY, mapItemColors);
	}
	
	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		assert bufferSize == mapSize;
		assert (blocks[ xz1D(bufferX, bufferY) ] == this)
			|| ( (OverlayIdentifiedBlockDefinition.class.isAssignableFrom(blocks[ xz1D(bufferX, bufferY) ].getClass()))
					&& ( OverlayIdentifiedBlockDefinition.baseBlocks[xz1D(bufferX, bufferY)] == this ) );
		assert altitudes[ xz1D(bufferX, bufferY) ] == y;
		renderOneBlock(blockList, x, y, z, bufferX, bufferY, mapItemColors);
	}
	
	protected abstract void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors);
	protected abstract void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors);
}
