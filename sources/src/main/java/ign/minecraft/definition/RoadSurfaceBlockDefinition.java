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
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;

public class RoadSurfaceBlockDefinition extends PhotoTreatedIdentifiedBlockDefinition {
	private static PhotoTreatedSimpleBlockDefinition ROADSURFACE = new PhotoTreatedSimpleBlockDefinition(
			new Block(BlockType.WOOL, BlockData.COLORS.GRAY), PhotoTreatments.ROADSURFACE );
	
	public RoadSurfaceBlockDefinition(Integer id) {
		super(id);
	}

	@Override
	protected void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		//set surface block
		PhotoTreatedSimpleBlockDefinition surfaceBlockDef = ROADSURFACE.clone();
		surfaceBlockDef.applyPhotoColor(bufferX, bufferY, photoColors[xz1D(bufferX,bufferY)]);
		surfaceBlockDef.render(world, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
	}
	
	@Override
	protected void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY,
			MineMap.MapItemColors mapItemColors) {
		//set surface block
		PhotoTreatedSimpleBlockDefinition surfaceBlockDef = ROADSURFACE.clone();
		surfaceBlockDef.applyPhotoColor(bufferX, bufferY, photoColors[xz1D(bufferX,bufferY)]);
		surfaceBlockDef.render(blockList, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
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

}
