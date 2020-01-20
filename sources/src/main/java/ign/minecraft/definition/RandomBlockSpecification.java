/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;

/**
 * 
 * the following class creates a block definition
 * 	from block id variants and data variants,
 *  from which the block elements will be set randomly
 *  
 */
public class RandomBlockSpecification {
	private final BlockType[] blockTypes;
	private final byte[][] blockDatas;//per type

	public RandomBlockSpecification(BlockType blockType) {
		blockTypes = new BlockType[1];
		blockTypes[0] = blockType;
		blockDatas = null;
	}
	public RandomBlockSpecification(BlockType blockType, byte blockData) {
		blockTypes = new BlockType[1];
		blockTypes[0] = blockType;
		blockDatas = new byte [1][1];
		blockDatas[0][0] = blockData;
	}
	public RandomBlockSpecification(BlockType[] blockTypes) {
		this(blockTypes, (byte[][]) null);
	}
	public RandomBlockSpecification(BlockType[] blockTypes, byte[][] blockDatas) {
		assert blockTypes.length > 0;
		this.blockTypes = blockTypes;
		assert blockDatas == null || blockDatas.length == blockTypes.length;
		this.blockDatas = blockDatas;
	}
	
	public Block generateBlock() {
		//set a random choice between the variants
		BlockType type;
		byte data = 0;
		int randomTypeIndex = (int) Math.floor( blockTypes.length * Math.random() );
		type = blockTypes[randomTypeIndex];
		if (blockDatas != null && blockDatas[randomTypeIndex].length > 0) {
			int randomDataIndex = (int) Math.floor( blockDatas[randomTypeIndex].length * Math.random() );
			data = blockDatas[randomTypeIndex][randomDataIndex];
		}
		
		return new Block(type, data);
	}
}