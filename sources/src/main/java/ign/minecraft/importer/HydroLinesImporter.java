/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.awt.BasicStroke;
import java.nio.file.Path;

import org.opengis.feature.simple.SimpleFeature;

import ign.minecraft.Utilities;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.definition.SimpleBlocks;

public class HydroLinesImporter extends VectorLoader {

	public HydroLinesImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//data connector
		dataConnector = new WFSDataConnector(this, Utilities.properties.getProperty("hydroLinesLayer"));
		
		//set width of 2 to avoid water blocks linked only by their corners
		graphics.setStroke(new BasicStroke(2));
	}

	@Override
	public boolean treatFeature(Object feature) {
		SimpleFeature wfsFeature = (SimpleFeature) feature;
		//do not display fictive feature (vectors created to keep a continuity in hydro)
		if(wfsFeature.getAttribute("fictif").toString().equalsIgnoreCase("oui")) {
			return false;
		}
		//do not display underground rivers
		if(wfsFeature.getAttribute("franchisst").toString().equalsIgnoreCase("Barrage")
				|| wfsFeature.getAttribute("franchisst").toString().equalsIgnoreCase("Pont-canal")
				|| wfsFeature.getAttribute("franchisst").toString().equalsIgnoreCase("Tunnel")) {
			return false;
		}
		return true;
	}

	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		//for each planar coordinate set river block if needed
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				if ((valueMap[y*valueMapSize + x] != null)
						&& map.getSurfaceBlock(x, y).canBeReplaced("HydroLinesImporter", x, y, valueMapSize)) {
					map.setSurfaceBlock(x, y, SimpleBlocks.RIVER.get() );
				}
			}
		}
	}
}
