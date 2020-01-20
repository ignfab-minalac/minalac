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

public class RandomBlockDefinition extends BlockDefinition {
	
	protected final BlockDefinition[] possibleBlockDefinitions;

	public RandomBlockDefinition(BlockDefinition[] possibleBlockDefinitions) {
		this.possibleBlockDefinitions = possibleBlockDefinitions;
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		int randomIndex = (int) (Math.random() * possibleBlockDefinitions.length);
		possibleBlockDefinitions[randomIndex].render(world, x, y, z, bufferX, bufferY, bufferSize, mapItemColors);
	}

	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize,
			MineMap.MapItemColors mapItemColors) {
		int randomIndex = (int) (Math.random() * possibleBlockDefinitions.length);
		possibleBlockDefinitions[randomIndex].render(blockList,x,y,z,bufferX,bufferY,bufferSize,mapItemColors);
	}
}
