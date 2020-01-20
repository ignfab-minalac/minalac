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

public class VariableHeightBlockDefinition extends BlockDefinition {
	
	protected final Block[] baseBlocks;
	protected final Block[] repeatedBlocks;
	protected final int height;

	public VariableHeightBlockDefinition(Block[] baseBlocks, Block[] repeatedBlocks, int height) {
		this.baseBlocks = baseBlocks;
		this.repeatedBlocks = repeatedBlocks;
		assert height > 0;
		this.height = height;//height is considered over base blocks, not including them
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		int i;
		for (i = 0; i < baseBlocks.length; i++) {
			world.setBlock(x, y+i, z, baseBlocks[i]);
		}
		int curY = y + baseBlocks.length;
		int maxY = y + baseBlocks.length + height;
		while (curY < maxY) {
			for (i = 0; i < repeatedBlocks.length && curY < maxY; i++) {
				world.setBlock(x, curY+i, z, repeatedBlocks[i]);
				curY ++;
			}
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(repeatedBlocks[(height - 1) % repeatedBlocks.length], maxY - 1));
	}

	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize,
			MineMap.MapItemColors mapItemColors) {
		int i;
		for (i = 0; i < baseBlocks.length; i++) {
			new BlockMT(x, y+i, z, BlockTypeConverter.convert(baseBlocks[i].getType())).addTo(blockList);
			//blockList.add(new BlockMT(x, y+i, z, BlockTypeConverter.convert(baseBlocks[i].getType())));
		}
		int curY = y + baseBlocks.length;
		int maxY = y + baseBlocks.length + height;
		while (curY < maxY) {
			for (i = 0; i < repeatedBlocks.length && curY < maxY; i++) {
				new BlockMT(x, curY+i, z, BlockTypeConverter.convert(repeatedBlocks[i].getType())).addTo(blockList);
				//blockList.add(new BlockMT(x, curY+i, z, BlockTypeConverter.convert(repeatedBlocks[i].getType())));
				curY ++;
			}
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(repeatedBlocks[(height - 1) % repeatedBlocks.length], maxY - 1));
	}
}
