/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.TreeMap;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;

final class PhotoTreatments {
	private static final BlockDefinition STONE = new SimpleBlockDefinition(new Block(BlockType.STONE));
	private static final BlockDefinition DIRT = new SimpleBlockDefinition(new Block(BlockType.DIRT, BlockData.DIRT.COARSE_DIRT));
	private static final BlockDefinition GRASS = new SimpleBlockDefinition(new Block(BlockType.GRASS));
	private static final BlockDefinition SAND = new SimpleBlockDefinition(new Block(BlockType.SAND, BlockData.SAND.SAND));
	
	//definition of photo treatment replacements
	public static final TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition> GRASSZONE = new TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition>();
	static {
		GRASSZONE.put(PhotoTreatedBlockDefinition.PhotoColor.CLEAR, STONE);
		GRASSZONE.put(PhotoTreatedBlockDefinition.PhotoColor.TRUEGREY, STONE);
		GRASSZONE.put(PhotoTreatedBlockDefinition.PhotoColor.RED, DIRT);
		GRASSZONE.put(PhotoTreatedBlockDefinition.PhotoColor.YELLOWISH, DIRT);
	}
	public static final TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition> BUILDINGZONE = new TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition>();
	static {
		BUILDINGZONE.put(PhotoTreatedBlockDefinition.PhotoColor.GREEN, GRASS);
		BUILDINGZONE.put(PhotoTreatedBlockDefinition.PhotoColor.DARKGREEN, GRASS);
		BUILDINGZONE.put(PhotoTreatedBlockDefinition.PhotoColor.BLUEISH, GRASS);
		BUILDINGZONE.put(PhotoTreatedBlockDefinition.PhotoColor.CLEAR, SAND);
		BUILDINGZONE.put(PhotoTreatedBlockDefinition.PhotoColor.TRUEYELLOW, SAND);
		BUILDINGZONE.put(PhotoTreatedBlockDefinition.PhotoColor.YELLOWISH, DIRT);
		BUILDINGZONE.put(PhotoTreatedBlockDefinition.PhotoColor.RED, DIRT);
	}
	public static final TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition> ROCKZONE = new TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition>();
	static {
		ROCKZONE.put(PhotoTreatedBlockDefinition.PhotoColor.GREEN, GRASS);
		ROCKZONE.put(PhotoTreatedBlockDefinition.PhotoColor.DARKGREEN, GRASS);
		ROCKZONE.put(PhotoTreatedBlockDefinition.PhotoColor.BLUEISH, GRASS);
		ROCKZONE.put(PhotoTreatedBlockDefinition.PhotoColor.YELLOWISH, DIRT);
		ROCKZONE.put(PhotoTreatedBlockDefinition.PhotoColor.RED, DIRT);
	}
	public static final TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition> SANDZONE = new TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition>();
	static {
		SANDZONE.put(PhotoTreatedBlockDefinition.PhotoColor.GREEN, GRASS);
		SANDZONE.put(PhotoTreatedBlockDefinition.PhotoColor.DARKGREEN, GRASS);
		SANDZONE.put(PhotoTreatedBlockDefinition.PhotoColor.TRUEGREY, STONE);
		SANDZONE.put(PhotoTreatedBlockDefinition.PhotoColor.GREY, STONE);
	}
	public static final TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition> MARSHZONE = new TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition>();
	static {
		MARSHZONE.put(PhotoTreatedBlockDefinition.PhotoColor.GREEN, GRASS);
		MARSHZONE.put(PhotoTreatedBlockDefinition.PhotoColor.DARKGREEN, GRASS);
	}
	public static final TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition> ROADSURFACE = new TreeMap<PhotoTreatedBlockDefinition.PhotoColor, BlockDefinition>();
	static {
		ROADSURFACE.put(PhotoTreatedBlockDefinition.PhotoColor.GREEN, GRASS);
		ROADSURFACE.put(PhotoTreatedBlockDefinition.PhotoColor.DARKGREEN, GRASS);
		ROADSURFACE.put(PhotoTreatedBlockDefinition.PhotoColor.BLUEISH, GRASS);
		ROADSURFACE.put(PhotoTreatedBlockDefinition.PhotoColor.TRUEYELLOW, SAND);
		ROADSURFACE.put(PhotoTreatedBlockDefinition.PhotoColor.RED, DIRT);
	}

	private PhotoTreatments() {
		assert false;//should not be instantiated
	}

}
