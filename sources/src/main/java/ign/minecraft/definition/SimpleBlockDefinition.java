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
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.importer.AltiImporter;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class SimpleBlockDefinition extends BlockDefinition {
	
	protected Block block;

	public SimpleBlockDefinition(Block block) {
		this.block = block;
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		world.setBlock(x, y, z, this.block);
		if(this.block.getType() != BlockType.TALLGRASS && MineGenerator.getSnowMode() == MineGenerator.MODE_SNOW) {
			int numberOfHeightSlices = (MineGenerator.MINECRAFTMAP_SNOWHEIGHTMAX - MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN) + 1;
			int minAndMaxAltiDifference = (int)(AltiImporter.MAX_ALTI - AltiImporter.MIN_ALTI);
			int eachSliceSize = minAndMaxAltiDifference / numberOfHeightSlices;
			int snowHeight = MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN + (int)Math.floor(((y + 1)-AltiImporter.MIN_ALTI)/eachSliceSize);

			for(int snowY = y + 1; snowY <= y + snowHeight; snowY++) {
				world.setBlock(x, snowY, z, new Block(BlockType.SNOW));//snow
			}
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(this.block, y));
	}

	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize,
			MineMap.MapItemColors mapItemColors) {
		new BlockMT(x, y, z, BlockTypeConverter.convert(this.block)).addTo(blockList);
		if(this.block.getType() != BlockType.TALLGRASS && MineGenerator.getSnowMode() == MineGenerator.MODE_SNOW) {
			int numberOfHeightSlices = (MineGenerator.MINECRAFTMAP_SNOWHEIGHTMAX - MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN) + 1;
			int minAndMaxAltiDifference = (int)(AltiImporter.MAX_ALTI - AltiImporter.MIN_ALTI);
			int eachSliceSize = minAndMaxAltiDifference / numberOfHeightSlices;
			int snowHeight = MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN + (int)Math.floor(((y + 1)-AltiImporter.MIN_ALTI)/eachSliceSize);

			for(int snowY = y + 1; snowY <= y + snowHeight; snowY++) {
				new BlockMT(x, snowY, z, BlockTypeConverter.convert(BlockType.SNOW)).addTo(blockList);//snow
			}
		}
		//blockList.add(new BlockMT(x, y, z, BlockTypeConverter.convert(this.block.getType())));
		mapItemColors.setColor(x, z, MapItemColor.getColor(this.block, y));
	}
}
