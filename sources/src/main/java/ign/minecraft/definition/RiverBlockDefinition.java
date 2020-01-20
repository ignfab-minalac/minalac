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
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class RiverBlockDefinition extends SimpleBlockDefinition {

	static final Block RIVER_BLOCK = new Block(BlockType.FLOWING_WATER);
	static final Block RIVERBED_BLOCK = new Block(BlockType.SAND);

	public RiverBlockDefinition() {
		super(RIVER_BLOCK);
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		//set water blocks down one block
		world.setBlock(x, y - 1, z, RIVER_BLOCK);
		//set water bed under
		world.setBlock(x, y - 2, z, RIVERBED_BLOCK);
		mapItemColors.setColor(x, z, MapItemColor.getColor(RIVER_BLOCK, y - 1));
	}
	
	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		//set water blocks down one block
		new BlockMT(x, y - 1, z, BlockTypeConverter.convert(RIVER_BLOCK.getType())).addTo(blockList);
		//set water bed under
		new BlockMT(x, y - 2, z, BlockTypeConverter.convert(RIVERBED_BLOCK.getType())).addTo(blockList);
		mapItemColors.setColor(x, z, MapItemColor.getColor(RIVER_BLOCK, y - 1));
	}
	
	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		switch (ImporterName) {
		case "BuildingsImporter":
		case "LinearConstructionsImporter":
		case "RoadsImporter":
			return true;
		default:
			return false;
		}
	}
	@Override
	public boolean canPutOverlayLayer(int bufferX, int bufferY, int bufferSize) {
		return false;
	}

}
