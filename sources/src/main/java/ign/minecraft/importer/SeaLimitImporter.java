/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;

import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.MinecraftGenerationException.Definition;
import ign.minecraft.definition.SeaBlockDefinition;

public class SeaLimitImporter extends PolygonReadyVectorLoader {
	
	public SeaLimitImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		String fileName;
		String[] directories;
		switch (localZone.crsName) {
		case "EPSG:2154"://LAMBERT 93 (FRANCE METRO)
			fileName = "france-metro";
			directories = new String[] {"sea-limit","sea-sand","sea-gravel","sea-rock"};
			break;
		case "EPSG:4471"://UTM 38 S (MAYOTTE)
			fileName = "mayotte";
			directories = new String[] {"sea-limit","sea-sand"};
			break;
		case "EPSG:32620"://UTM 20 N (ANTILLES FRANCAISES)
			if(localZone.equals(Zone.GUADELOUPE)) {
				directories = new String[] {"sea-rock"};
				fileName = "guadeloupe";
			} else
				throw new MinecraftGenerationException(Definition.SERVICEIMPORT_UNSUPPORTED_ZONE, new UnsupportedOperationException("CRS found : " + localZone.crsName + " for " + this.getClass().getName() + " but unsupported zone"));
			break;
		case "EPSG:2972"://UTM 22 N (GUYANE)
		case "EPSG:2975"://UTM 40 S (REUNION)
		case "EPSG:4467"://UTM 21 N (ST PIERRE ET MIQUELON)
			//not yet available
		default:
			throw new MinecraftGenerationException(Definition.SERVICEIMPORT_UNSUPPORTED_ZONE, new UnsupportedOperationException("unsupported CRS : " + localZone.crsName + " for " + this.getClass().getName()) );
		}

		//data connector
		if(fileName.equals("france-metro")) {
			dataConnector = new SpatialiteDataConnector(this, resourcesPath, directories, fileName);
		} else {
			dataConnector = new ShapefileDataConnector(this, resourcesPath, directories, fileName);
		}
	}
	
	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		//for each planar coordinate check we are on land
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				if (valueMap[y*valueMapSize + x] != null
						&& map.getSurfaceBlock(x, y).canBeReplaced("SeaLimitImporter", x, y, valueMapSize)
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
