/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;

import ign.minecraft.Utilities;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.definition.SeaBlockDefinition;

public class CountryLandImporter extends PolygonReadyVectorLoader {
	
	public CountryLandImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//data connector
		dataConnector = new WFSDataConnector(this, Utilities.properties.getProperty("countryLandLayer"));
	}

	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		//for each planar coordinate check we are on land
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				if ((valueMap[y*valueMapSize + x] == null)
						&& !SeaBlockDefinition.class.isAssignableFrom(map.getSurfaceBlock(x, y).getClass())
						&& map.getGroundLevel(x, y) <= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO) {
					//not in commune (and not already set as sea), we consider it's the sea
					//set depth considering eventually shore negative altitude
					map.setSurfaceBlock(x, y, new SeaBlockDefinition( Math.max(SeaBlockDefinition.SEA_DEPTH_MIN, AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO - map.getGroundLevel(x, y)) ));
					map.setGroundLevel(x, y, AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO);
				}
			}
		}
	}
}
