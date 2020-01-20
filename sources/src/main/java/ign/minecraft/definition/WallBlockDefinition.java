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

public class WallBlockDefinition extends IdentifiedBlockDefinition {

	static final Block WALL_BLOCK = new Block(BlockType.COBBLESTONE);
	static public final int SIMPLE_WALL_HEIGHT = 3;
	static public final int RUIN_WALL_HEIGHT = 2;
	
	final private int height;

	public WallBlockDefinition(Integer id, Integer height) {
		super(id);
		
		this.height = height;
	}

	//computing min or max z of wall could be done in pre-render if needed
	
	@Override
	protected void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		for(int curY = y; curY < y + height; curY ++ ) {
			world.setBlock(x, curY, z, WALL_BLOCK);
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(WALL_BLOCK, y + height));
	}
	
	@Override
	public void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		for(int curY = y; curY < y + height; curY ++ ) {
			new BlockMT(x, curY, z, "default:cobble").addTo(blockList);
		}
		mapItemColors.setColor(x, z, MapItemColor.getColor(WALL_BLOCK, y + height));
	}
	
	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		switch (ImporterName) {
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
