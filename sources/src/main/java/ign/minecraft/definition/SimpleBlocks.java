/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;


/**
 * this class centralize the definition of simple blocks
 * do not use the instances directly but call get() for each block instead
 * as some block definition need to be cloned
 * 
 * @author David Frémont
 *
 */
public enum SimpleBlocks {


	
	// internal blocks //////////////////////////////
	BORDERFILLING(new SimpleBlockDefinition(new Block(BlockType.BEDROCK))),
	EARTH(new SimpleBlockDefinition(new Block(BlockType.DIRT))),
	
	// surface blocks ///////////////////////////////
	DEFAULTSURFACE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.GRASS), PhotoTreatments.GRASSZONE)),
	// zones from BD Carto
	BUILDINGSZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.STONE, BlockData.STONE.ANDESITE), PhotoTreatments.BUILDINGZONE)),
	BUSHZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.GRASS), PhotoTreatments.GRASSZONE)),
	QUARRYZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.IRON_ORE), PhotoTreatments.ROCKZONE)),
	FORESTZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.GRASS), PhotoTreatments.GRASSZONE)),
	GLACIERZONE(new SimpleBlockDefinition(new Block(BlockType.ICE))),
	MARSHZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.DIRT, BlockData.DIRT.PODZOL), PhotoTreatments.MARSHZONE)),
	SALTMARSHZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.DIRT, BlockData.DIRT.PODZOL), PhotoTreatments.MARSHZONE)),
	MEADOWZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.GRASS), PhotoTreatments.GRASSZONE)),
	ROCKSZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.STONE), PhotoTreatments.ROCKZONE)),
	SANDZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.SAND, BlockData.SAND.SAND), PhotoTreatments.SANDZONE)),
	ORCHARDZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.GRASS), PhotoTreatments.GRASSZONE)),
	RIVERZONE(new PhotoTreatedSimpleBlockDefinition(new Block(BlockType.GRAVEL), PhotoTreatments.ROCKZONE)),
	//specific nature ///////////////////////////////
	PURESAND(new SimpleBlockDefinition(new Block(BlockType.SAND, BlockData.SAND.SAND))),
	PUREGRAVEL(new SimpleBlockDefinition(new Block(BlockType.GRAVEL))),

	//rivers simple blocks ///////////////////////////////
	RIVER(new RiverBlockDefinition()),
	
	// other simple blocks ///////////////////////////////
	FLATBUILDING(new SimpleBlockDefinition(new Block(BlockType.COBBLESTONE))),
	;
	

	
	private final SimpleBlockDefinition blockDefinition;
	
	private SimpleBlocks(SimpleBlockDefinition blockDefinition) {
		this.blockDefinition = blockDefinition;
	}

	public SimpleBlockDefinition get() {
		if (PhotoTreatedSimpleBlockDefinition.class.isAssignableFrom( this.blockDefinition.getClass() )) {
			return ((PhotoTreatedSimpleBlockDefinition) this.blockDefinition).clone();
		} else {
			return this.blockDefinition;//no cloning required
		}
	}
}

