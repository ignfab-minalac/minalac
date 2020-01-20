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
import ignfab.minetest.*;

public class SeaBlockDefinition extends SimpleBlockDefinition {

	static final Block SEA_BLOCK = new Block(BlockType.FLOWING_WATER);
	static final Block SEABOTTOM_BLOCK = new Block(BlockType.SAND);
	static final int DEEP_SEA_DEPTH = 5;
	static final public int SEA_DEPTH_MIN = 1;
	
	final int depth;

	public SeaBlockDefinition(int depth) {
		super(SEA_BLOCK);
		
		this.depth = Math.max( Math.min(depth, DEEP_SEA_DEPTH), SEA_DEPTH_MIN);
	}

	public SeaBlockDefinition() {
		this(DEEP_SEA_DEPTH);
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		//set water blocks down to depth
		for (int curDepth = 0; curDepth < depth; curDepth ++) {
			world.setBlock(x, y - curDepth, z, SEA_BLOCK);
		}
		//set sea bottom block
		world.setBlock(x, y - depth, z, SEABOTTOM_BLOCK);
		mapItemColors.setColor(x, z, MapItemColor.getColor(SEA_BLOCK, y));
	}
	
	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		//set water blocks down to depth
		for (int curDepth = 0; curDepth < depth; curDepth ++) {
			new BlockMT(x, y - curDepth, z, "default:water_source").addTo(blockList);
			//blockList.add(new BlockMT(x, y - curDepth, z, "default:water_source"));
		}
		//set sea bottom block
		new BlockMT(x, y - depth, z, "default:sand").addTo(blockList);
		//blockList.add(new BlockMT(x, y - depth, z, "default:water_source"));
		mapItemColors.setColor(x, z, MapItemColor.getColor(SEA_BLOCK, y));
	}
	
	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		switch (ImporterName) {
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
