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
 * this class can be stored in importers value maps and can provided simple values to be replaced
 * by more complex settings when importing is finished
 * 
 * @author David Fremont
 *
 */
public class ValueBlockDefinition extends BlockDefinition {
	
	public static ValueBlockDefinition DEFAULT_VALUE = new ValueBlockDefinition(0);
	
	public final int value;
	
	public ValueBlockDefinition(int value) {
		this.value = value;
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		//should not be rendered
		assert false;
	}

	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize,
			MineMap.MapItemColors mapItemColors) {
		//should not be rendered
		assert false;
	}
}
