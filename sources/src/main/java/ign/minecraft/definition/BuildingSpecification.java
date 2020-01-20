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
 * this class defines blocks that constitutes a building
 * and provides method to help setting the blocks for building them
 */
public class BuildingSpecification {
	
	private static final RandomBlockSpecification GAMEPLAY_GROUNDS = new RandomBlockSpecification(
				new BlockType[] { BlockType.SAND, BlockType.GRAVEL, BlockType.CLAY },
				new byte[][] { {BlockData.NONE}, {BlockData.NONE}, {BlockData.NONE} });
	
	private static final Block GAMEPLAY_ANIMAL_GROUND = new Block(BlockType.GRASS);
	
	private static boolean gameplayGroundProbability() {
		return (Math.random() >= 0.80);
	}
	private static boolean gameplayItemProbability() {
		return (Math.random() >= 0.70);
	}
	private static boolean gameplayAnimalProbability() {
		return (Math.random() >= 0.90);
	}
	
	private final boolean gameplayGround;
	private final boolean gameplayItem;
	private final boolean gameplayAnimal;
	private final Block groundTile;
	private final Block baseLayerTile;
	private final Block floorBaseFillingTile;
	private final Block[] floorTiles;
	private final Block floorUpperFillingTile;
	private final Block roofTile;
	
	private boolean gameplayEntityPlaced = false;
	private boolean gameplayAnimalIsHuman = false;
	
	public BuildingSpecification(BuildingCategory buildingCategory, BuildingCategory.RoofColor roofColor) {
		//get a random definition from the category
		BuildingWallsSpecification buildingDefinition = buildingCategory.getOneBuildingDefinition(roofColor);
		//set gameplay data randomly
		gameplayGround = (buildingCategory == BuildingCategory.CHURCH_CASTLE) ? false : gameplayGroundProbability();
		gameplayItem = (buildingCategory == BuildingCategory.CHURCH_CASTLE) ? false : gameplayItemProbability();
		gameplayAnimal = (buildingCategory == BuildingCategory.CHURCH_CASTLE) ? false : 
			((gameplayGround || gameplayItem) ? false : gameplayAnimalProbability());
		//creates building blocks from the building definition
		if (gameplayGround) {
			groundTile = GAMEPLAY_GROUNDS.generateBlock();
		} else if (gameplayAnimal) {
			groundTile = GAMEPLAY_ANIMAL_GROUND;
		} else {
			groundTile = buildingDefinition.groundTile.generateBlock();
		}
		baseLayerTile = (buildingDefinition.baseLayerTile == null) ? null
								: buildingDefinition.baseLayerTile.generateBlock();
		floorBaseFillingTile = (buildingDefinition.floorBaseFillingTile == null) ? null
								: buildingDefinition.floorBaseFillingTile.generateBlock();
		assert buildingDefinition.floorTiles.length > 0;
		floorTiles = new Block[buildingDefinition.floorTiles.length];
		for (int tileIndex = 0; tileIndex < buildingDefinition.floorTiles.length; tileIndex++) {
			floorTiles[tileIndex] = buildingDefinition.floorTiles[tileIndex].generateBlock();
		}
		assert buildingDefinition.floorUpperFillingTile != null;
		floorUpperFillingTile = buildingDefinition.floorUpperFillingTile.generateBlock();
		assert buildingDefinition.roofTile != null;
		roofTile = buildingDefinition.roofTile.generateBlock();
	}
	
	public boolean hasGameplayGround() {
		return gameplayGround;
	}
	
	public boolean needGameplayItem() {
		return gameplayItem && !gameplayEntityPlaced;
	}
	
	public void gameplayAnimalIsHuman() {
		gameplayAnimalIsHuman = true;
	}
	public boolean hasGameplayAnimal() {
		return gameplayAnimal && !gameplayAnimalIsHuman;
	}
	public boolean needGameplayAnimal() {
		return gameplayAnimal && !gameplayEntityPlaced;
	}
	
	public void gameplayEntityPlaced() {
		gameplayEntityPlaced = true;
	}
	
	public Block[] getWallTiles(int height) {
		Block[] column = new Block[height];
		int currentIndex = 0;
		int i;
		
		assert(height > 0);
		
		int reservedHeight = 1;//for roof
		boolean generateBaseLayer = false;
		if (baseLayerTile != null && height > reservedHeight) {
			generateBaseLayer = true;
			reservedHeight++;
		}
		int nbFloors = (height - reservedHeight) / floorTiles.length;
		int nbFillingBlocks = height - reservedHeight - nbFloors * floorTiles.length;
		assert nbFillingBlocks >= 0;
		
		//base layer
		if(generateBaseLayer) {
			column[currentIndex] = baseLayerTile;
			currentIndex++;
		}
		if (floorBaseFillingTile != null) {
			//bottom filling
			for (i = 0; i < (nbFillingBlocks - nbFillingBlocks / 2); i++) {
				column[currentIndex] = floorBaseFillingTile;
				currentIndex++;
			}
		}
		//floors
		for (int floorIndex = 0; floorIndex < nbFloors; floorIndex++) {
			for (i = 0; i < floorTiles.length; i++) {
				column[currentIndex] = floorTiles[i];
				currentIndex++;
			}
		}
		//upper filling
		while (currentIndex < height - 1) {
			column[currentIndex] = floorUpperFillingTile;
			currentIndex++;
		}
		//roof
		assert currentIndex == height - 1;
		column[currentIndex] = roofTile;
		
		return column;
	}
	
	public Block getGroundBlock() {
		return groundTile;
	}
	
	public Block getRoofBlock() {
		return roofTile;
	}
}
