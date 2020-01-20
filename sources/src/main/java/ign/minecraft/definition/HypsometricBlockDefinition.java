/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Map;
import java.util.logging.Logger;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class HypsometricBlockDefinition extends BlockDefinition {
	protected static final Logger LOGGER = Logger.getLogger("HypsometricBlockDefinition");
	protected Block block;

	public HypsometricBlockDefinition(Block block) {
		this.block = block;
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		for (int curY = 0 ; curY <= y; curY ++) {
			world.setBlock(x, curY, z, this.block);
		}
	}

	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize,
			MineMap.MapItemColors mapItemColors) {
		for (int curY = y-(UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2)-1; curY <= y; curY ++) {
			new BlockMT(x, curY, z, BlockTypeConverter.convert(this.block)).addTo(blockList);
		}
		new BlockMT(x, y-(UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2), z, "default:cloud").addTo(blockList);  // Layer of Minetest bedrock
	}
}
