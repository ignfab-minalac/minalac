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
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;

public class PhotoTreatedSimpleBlockDefinition extends SimpleBlockDefinition implements PhotoTreatedBlockDefinition {
	
	private final Map<PhotoColor,BlockDefinition> photoAjustments;
	private BlockDefinition substitute = null;
	
	public PhotoTreatedSimpleBlockDefinition(Block block, Map<PhotoColor, BlockDefinition> photoAjustments) {
		super(block);
		
		this.photoAjustments = photoAjustments;
	}
	
	public PhotoTreatedSimpleBlockDefinition clone() {
		return new PhotoTreatedSimpleBlockDefinition(block, photoAjustments);
	}
	
	@Override
	public void applyPhotoColor(int x, int z, PhotoColor color) {
		if (photoAjustments.containsKey(color)) {
			this.substitute = photoAjustments.get(color);
		}
	}
	
	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		if (substitute != null) {
			substitute.render(world, x, y, z, bufferX, bufferY, bufferSize, mapItemColors);
		} else {
			super.render(world, x, y, z, bufferX, bufferY, bufferSize, mapItemColors);
		}
	}
	
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		if (substitute != null) {
			substitute.render(blockList, x, y, z, bufferX, bufferY, bufferSize, mapItemColors);
		} else {
			super.render(blockList, x, y, z, bufferX, bufferY, bufferSize, mapItemColors);
		}
	}
}
