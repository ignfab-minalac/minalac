/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Map;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class DamBlockDefinition extends IdentifiedBlockDefinition {

	static public final Block DAM_BLOCK = new Block(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.LIGHT_GRAY);
	static public final int MAX_Y_DIFF = 1;
	
	private int topY;//top altitude as declared in data
	private int maxY;//max altitude of the ground on which the dam is

	public DamBlockDefinition(Integer id, Short topY) {
		super(id);
		
		this.topY = topY;
	}

	//compute max z of group of blocks in pre-render
	@Override
	protected void preRenderInit() {
		maxY = 0;
	}
	@Override
	protected void preRender(int x, int z) {
		maxY = Math.max(maxY, altitudes[xz1D(x, z)]);
	}
	@Override
	protected void preRenderEnd() {
		//prevent the dam from being too high compared to surrounding blocks
		topY = Math.min(topY, maxY + MAX_Y_DIFF);
		//or too low
		topY = Math.max(topY, maxY - MAX_Y_DIFF);
	}
	
	@Override
	protected void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		y = Math.min(y, topY);
		for(int curY = y; curY <= topY; curY ++ ) {
			world.setBlock(x, curY, z, DAM_BLOCK);
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(DAM_BLOCK, topY));

	}

	@Override
	protected void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY,
			MineMap.MapItemColors mapItemColors) {
		y = Math.min(y, topY);
		for(int curY = y; curY <= topY; curY ++ ) {
			new BlockMT(x, curY, z, BlockTypeConverter.convert(DAM_BLOCK.getType())).addTo(blockList);
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(DAM_BLOCK, topY));
	}
}
