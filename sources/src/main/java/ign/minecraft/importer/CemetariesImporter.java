/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;

import org.opengis.feature.simple.SimpleFeature;

import ign.minecraft.Utilities;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.definition.IdentifiedBlockDefinition;
import ign.minecraft.definition.IdentifiedBlockFactory;
import ign.minecraft.definition.ValueBlockDefinition;

public class CemetariesImporter extends PolygonReadyVectorLoader {
	
	public static final int COLOR_CEMETARY = 0x000000FF;//RGBA value, last byte must be not null otherwise color will be set to null
	
	private static final int ID_SHIFT = 8;
	
	private int currentCemetaryIndex = 0;//store surface index while drawing them
	protected int[] cemetariesIndexes = new int[valueMapSize * valueMapSize];//store id +1 for each pixel, +1 because 0 means no surface

	public CemetariesImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//data connector
		dataConnector = new WFSDataConnector(this, Utilities.properties.getProperty("cemetariesLayer"));
		
		//redefine defaulf color to be able to inject element id into it
		colorsToValues.clear();
		colorsToValues.put(COLOR_CEMETARY, ValueBlockDefinition.DEFAULT_VALUE);
	}

	@Override
	public boolean treatFeature(Object feature) {
		//only display constantly flowing rivers
		if(((SimpleFeature) feature).getAttribute("nature").toString().equalsIgnoreCase("surface d'eau")
			&& !((SimpleFeature) feature).getAttribute("regime").toString().equalsIgnoreCase("Permanent")) {
			return false;
		}
		
		//set a color that includes the id
		graphics.setColor( toColor((currentCemetaryIndex << ID_SHIFT) + COLOR_CEMETARY) );
		
		currentCemetaryIndex ++;
		
		return true;
	}
	
	@Override
	protected void treatPixelValue(int x, int y, int pixelValueRGBA) {
		if(pixelValueRGBA != 0) {
			//separate id and color
			int cemetaryId = pixelValueRGBA >>> ID_SHIFT;
			int colorValue = pixelValueRGBA & COLOR_CEMETARY;
			assert cemetaryId < currentCemetaryIndex;
			//store surface index and treat color normally
			cemetariesIndexes[y*valueMapSize + x] = cemetaryId + 1;//+1 to keep 0 as no surface
			super.treatPixelValue(x, y, colorValue);
		}
	}
	
	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		int x, y;
		int currentIndex;
		IdentifiedBlockDefinition cemetary;
		
		//for each planar coordinate set block definition
		for(x=0; x<valueMapSize; x++) {
			for(y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				//set surface block
				if ((valueMap[y*valueMapSize + x] != null)
						&& map.getSurfaceBlock(x, y).canBeReplaced("CemetariesImporter", x, y, valueMapSize)) {
					currentIndex = cemetariesIndexes[y*valueMapSize + x] - 1;
					assert currentIndex < currentCemetaryIndex;
					cemetary = IdentifiedBlockFactory.getBlockDefinition("CemetaryBlockDefinition", currentIndex);
					map.setSurfaceBlock(x, y, cemetary);
				}
			}
		}
	}
}
