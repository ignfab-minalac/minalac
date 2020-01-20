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
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class VerticalBlockDefinition extends BlockDefinition {
	
	protected Block[] blocks;

	public VerticalBlockDefinition(Block[] blocks) {
		this.blocks = blocks;
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		for (int i = 0; i < blocks.length; i++) {
			world.setBlock(x, y+i, z, blocks[i]);
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(blocks[blocks.length - 1], y + blocks.length - 1));
	}
	
	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		for (int i = 0; i < blocks.length; i++) {
			new BlockMT(x, y+i, z, BlockTypeConverter.convert(blocks[i].getType())).addTo(blockList);
			//blockList.add(new BlockMT(x, y+i, z, BlockTypeConverter.convert(blocks[i].getType())));
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(blocks[blocks.length - 1], y + blocks.length - 1));
	}
}
