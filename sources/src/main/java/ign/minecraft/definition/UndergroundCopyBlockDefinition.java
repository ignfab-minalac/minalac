/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import developpeur2000.minecraft.minecraft_rw.entity.BlockEntity;
import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.Chunk;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ign.minecraft.Utilities;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class UndergroundCopyBlockDefinition extends BlockDefinition {
    protected static final Logger LOGGER = Logger.getLogger("UndergroundCopyBlockDefinition");

	private static final Block DEFAULT_UNDERGROUND = new Block(BlockType.STONE);

	//limit of underground blocks defined in the underground template 
	public static int UNDERGROUND_DEFINITION_LIMIT = Integer.parseInt(Utilities.properties.getProperty("maxUndergroundDefinition"));
	//limit over which we transform water into rock
	public static int UNDERGROUND_WATER_LIMIT = Integer.parseInt(Utilities.properties.getProperty("maxUndergroundWater"));
	
	final World sourceWorld;
	
	public UndergroundCopyBlockDefinition(World undergroundWorld) {
		sourceWorld = undergroundWorld;
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		//assert ((sourceWorld == null) || sourceWorld.hasBlock(x, 0, z));
		//test underground world and tell us if some blocks need to be generated
		if ((sourceWorld != null) && !sourceWorld.hasBlock(x, 0, z)) {
			if (x % Chunk.BLOCKS == 0 && z % Chunk.BLOCKS == 0) {
				LOGGER.log(Level.WARNING, "missing underground chunk at planar position : " + x + "," + z);
			}
		}

		int curY;

		if (sourceWorld == null || !sourceWorld.hasBlock(x, 0, z)) {
			for (curY = 0 ; curY <= y; curY ++) {
				world.setBlock(x, curY, z, DEFAULT_UNDERGROUND);
			}
			return;
		}
		
		final Block[] undergroundBlocks = new Block[y+1];
		//read blocks up to underground definition limit
		assert UNDERGROUND_WATER_LIMIT < UNDERGROUND_DEFINITION_LIMIT;
		for (curY = 0 ; curY <= Math.min(y, UNDERGROUND_DEFINITION_LIMIT); curY ++) {
			undergroundBlocks[curY] = sourceWorld.getBlock(x, curY, z);
			if ( (curY >= UNDERGROUND_WATER_LIMIT)
					&& (undergroundBlocks[curY].getType() == BlockType.FLOWING_WATER)
					&& (undergroundBlocks[curY].getType() == BlockType.WATER) ){
				undergroundBlocks[curY] = DEFAULT_UNDERGROUND;
			}
		}
		//copy blocks after that limit
		for (curY = UNDERGROUND_DEFINITION_LIMIT ; curY <= y; curY ++) {
			//don't copy everything when repeating the pattern
			//only allow a few blocks to be copied, others will be replaced by default block
			switch (undergroundBlocks[curY % UNDERGROUND_DEFINITION_LIMIT].getType()) {
			case STONE:
			case GRAVEL:
			case DIRT:
			case SAND:
			case CLAY:
				undergroundBlocks[curY] = undergroundBlocks[curY % UNDERGROUND_DEFINITION_LIMIT];
				break;
			default:
				undergroundBlocks[curY] = DEFAULT_UNDERGROUND;
			}
		}
		//write blocks in our generated map
		for (curY = 0 ; curY <= y; curY ++) {
			world.setBlock(x, curY, z, undergroundBlocks[curY]);
		}
	}
	
	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize,
			MineMap.MapItemColors mapItemColors) {
		//assert ((sourceWorld == null) || sourceWorld.hasBlock(x, 0, z));
		//test underground world and tell us if some blocks need to be generated
		if ((sourceWorld != null) && !sourceWorld.hasBlock(x, 0, z)) {
			if (x % Chunk.BLOCKS == 0 && z % Chunk.BLOCKS == 0) {
				LOGGER.log(Level.WARNING, "missing underground chunk at planar position : " + x + "," + z);
			}
		}

		int curY;

		if (sourceWorld == null || !sourceWorld.hasBlock(x, 0, z)) {
			for (curY = y-(UNDERGROUND_DEFINITION_LIMIT/2)-1; curY <= y; curY ++) {
				new BlockMT(x, curY, z, BlockTypeConverter.convert(DEFAULT_UNDERGROUND)).addTo(blockList);
			}
			new BlockMT(x, y-(UNDERGROUND_DEFINITION_LIMIT/2), z, "default:cloud").addTo(blockList);  // Layer of Minetest bedrock
			return;
		}
		
		final Block[] undergroundBlocks = new Block[y+1];
		//read blocks from y to y-underground definition limit
		//minetest specific behaviour as we can get very high altis
		//we can't allow ourselves to write such big undergrounds (down to y=0) for nothing
		int plainUndergroundSize = 10;
		assert UNDERGROUND_WATER_LIMIT < UNDERGROUND_DEFINITION_LIMIT;
		assert plainUndergroundSize < UNDERGROUND_DEFINITION_LIMIT;
		for (curY = y-plainUndergroundSize; curY <= y; curY ++) {
			new BlockMT(x, curY, z, BlockTypeConverter.convert(DEFAULT_UNDERGROUND)).addTo(blockList);
		}
		for (curY = y-UNDERGROUND_DEFINITION_LIMIT-1 ; curY <= y-plainUndergroundSize; curY ++) {
			if ( (curY >= UNDERGROUND_WATER_LIMIT)
					&& (sourceWorld.getBlock(x, curY, z).getType() == BlockType.FLOWING_WATER)
					&& (sourceWorld.getBlock(x, curY, z).getType() == BlockType.WATER) ){
				new BlockMT(x, curY, z, BlockTypeConverter.convert(DEFAULT_UNDERGROUND)).addTo(blockList);
			} else {
				new BlockMT(x, curY, z, BlockTypeConverter.convert(sourceWorld.getBlock(x, curY, z))).addTo(blockList);
			}
		}
		new BlockMT(x, y-UNDERGROUND_DEFINITION_LIMIT, z, "default:cloud").addTo(blockList); // Layer of Minetest bedrock
	}
	
	//specific method to copy entities and block entities
	public void copyEntities(World world, int ChunkXMin, int ChunkZMin,
								short[] altitudes, int altitudesSize, int altitudesCopyShift,
								int altiShiftX, int altiShiftZ) {
		if (sourceWorld == null) {
			return;
		}

		int altitudeIndex;
		for (BlockEntity blockEntity : sourceWorld.listBlockEntitiesInChunk(ChunkXMin, ChunkZMin)) {
			altitudeIndex = (blockEntity.getZ() + altiShiftZ) * altitudesSize + (blockEntity.getX() + altiShiftX);
			assert(altitudeIndex >= 0 && altitudeIndex < altitudesSize * altitudesSize);
			if (blockEntity.getY() <= altitudes[altitudeIndex] - altitudesCopyShift) {
				world.addBlockEntity(blockEntity.getX(), blockEntity.getY(), blockEntity.getZ(), blockEntity);
			}
		}

	}
}
