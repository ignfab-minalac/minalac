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
import ign.minecraft.definition.IdentifiedBlockDefinition;
import ign.minecraft.definition.IdentifiedBlockFactory;
import ign.minecraft.definition.ValueBlockDefinition;

public class RoadSurfacesImporter extends PolygonReadyVectorLoader {
	
	public static final int COLOR_SURFACE = 0x000000FF;//RGBA value, last byte must be not null otherwise color will be set to null
	
	private static final int ID_SHIFT = 8;
	
	private int currentSurfaceIndex = 0;//store surface index while drawing them
	protected int[] surfacesIndexes = new int[valueMapSize * valueMapSize];//store surface id +1 for each pixel, +1 because 0 means no surface

	public RoadSurfacesImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//data connector
		dataConnector = new WFSDataConnector(this, Utilities.properties.getProperty("roadSurfaceLayer"));
		
		//redefine default color to be able to inject element id into it
		colorsToValues.clear();
		colorsToValues.put(COLOR_SURFACE, ValueBlockDefinition.DEFAULT_VALUE);
	}

	@Override
	public boolean treatFeature(Object feature) {
		//set a color that includes the id
		graphics.setColor( toColor((currentSurfaceIndex << ID_SHIFT) + COLOR_SURFACE) );
		
		currentSurfaceIndex ++;
		return true;
	}
	
	@Override
	protected void treatPixelValue(int x, int y, int pixelValueRGBA) {
		if(pixelValueRGBA != 0) {
			//separate surface id and color
			int surfaceId = pixelValueRGBA >>> ID_SHIFT;
			int colorValue = pixelValueRGBA & COLOR_SURFACE;
			assert surfaceId < currentSurfaceIndex;
			//store surface index and treat color normally
			surfacesIndexes[y*valueMapSize + x] = surfaceId + 1;//+1 to keep 0 as no surface
			super.treatPixelValue(x, y, colorValue);
		}
	}
	
	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		int x, y;
		int currentIndex;
		IdentifiedBlockDefinition surface;
		
		//for each planar coordinate set block definition
		for(x=0; x<valueMapSize; x++) {
			for(y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				//set surface block
				if ((valueMap[y*valueMapSize + x] != null)
						&& map.getSurfaceBlock(x, y).canBeReplaced("RoadSurfacesImporter", x, y, valueMapSize)) {
					currentIndex = surfacesIndexes[y*valueMapSize + x] - 1;
					assert currentIndex < currentSurfaceIndex;
					surface = IdentifiedBlockFactory.getBlockDefinition("RoadSurfaceBlockDefinition", currentIndex);
					map.setSurfaceBlock(x, y, surface);
				}
			}
		}

	}
}
