/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft;

import java.io.IOException;
import java.nio.file.Path;

import ign.minecraft.definition.BlockDefinition;
import ign.minecraft.definition.SimpleBlocks;

public abstract class MineMap {
	
	public class MapItemColors {
		private int size;
		private byte[] colors;
		final private int shiftX;
		final private int shiftZ;
		
		public MapItemColors(int size, int shiftX, int shiftZ) {
			this.size = size;
			this.colors = new byte[size*size];
			this.shiftX = shiftX;
			this.shiftZ = shiftZ;
		}
		
		public void setColor(int x, int z, byte color) {
			assert x + shiftX >= 0;
			assert x + shiftX < size;
			assert z + shiftZ >= 0;
			assert z + shiftZ < size;
			this.colors[ (z + shiftZ) * size + x + shiftX ] = color;
		}
		
		public byte[] getColors() {
			return colors;
		}
		
		public void setColors(byte[] array) {
			this.colors = array;
			this.size = (int)Math.sqrt(this.colors.length);
		}

		public int getSize() {
			return size;
		}

		public int getShiftX() {
			return shiftX;
		}

		public int getShiftZ() {
			return shiftZ;
		}
	}
	
	protected static final String UNDERGOUNDDATA_DIRNAME = "underground-map";
	protected static final String MAPITEMDATA_DIRNAME = "map-items";
	protected static final String MAPMODS_DIRNAME = "map-mods";
	protected static final String ELEMENTSDATA_DIRNAME = "elements";
	
	public int mapSize;//size of the size of our square map (without auto generated borders)
	// flattened arrays of blocks description, and levels
	// all these arrays are 0,0 based
	// centering the map in 0,0 is done when writing the map to region files
	protected final short[] groundLevel; //altitudes of the surface blocks
	protected final BlockDefinition[] surfaceBlocks;// definition of surface blocks
	protected final Path outputDir;
	protected final String name;
	protected final Path resourcesDir;//path of the level files containing the data that will be used as underground
	protected MapItemColors mapItemColors;//colors that can be used in map items
	
	public MineMap(int size, Path path, String name, Path resourcesPath) {
		mapSize = size;
		//instantiate arrays
		groundLevel = new short[mapSize * mapSize];
		surfaceBlocks = new BlockDefinition[mapSize * mapSize];
		
		//init default surface block
		for(int x = 0; x < mapSize; x ++) {
			for(int z = 0; z < mapSize; z ++) {
				surfaceBlocks[z * mapSize + x] = SimpleBlocks.DEFAULTSURFACE.get();
			}
		}

		outputDir = path;
		resourcesDir = resourcesPath;
		this.name = name;
	}
		
	/**
	 * set one specific block to a block Id
	 * 
	 */
	public void setSurfaceBlock(int x, int z, BlockDefinition blockDefinition) {
		surfaceBlocks[z * mapSize + x] = blockDefinition;
	}
	
	/**
	 * get the block id of one specific block
	 * 
	 */
	public BlockDefinition getSurfaceBlock(int x, int z) {
		return surfaceBlocks[z * mapSize + x];
	}
	
	/**
	 * set the ground level at a specified height on a specified position
	 * 
	 */
	public void setGroundLevel(int x, int z, int y) {
		groundLevel[z * mapSize + x] = (short) y;
	}
	
	/**
	 * get the ground level at a specified position
	 * 
	 */
	public short getGroundLevel(int x, int z) {
		return groundLevel[z * mapSize + x];
	}

	/**
	 * generate the structure that can be directly translated as minecraft data (regions, chunks, etc)
	 * from the current stored blocks
	 * and save it on file
	 * @param generateBorder 
	 * 
	 * @throws IOException 
	 * @throws MinecraftGenerationException 
	 * 
	 */
	abstract void writeToDisk() throws IOException, MinecraftGenerationException;

}