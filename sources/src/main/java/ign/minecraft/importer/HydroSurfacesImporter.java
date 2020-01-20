/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.geotools.geometry.jts.Geometries;
import org.opengis.feature.simple.SimpleFeature;

import ign.minecraft.Utilities;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.definition.IdentifiedBlockDefinition;
import ign.minecraft.definition.IdentifiedBlockFactory;
import ign.minecraft.definition.ValueBlockDefinition;
import ign.minecraft.definition.WaterSurfaceBlockDefinition;

public class HydroSurfacesImporter extends PolygonReadyVectorLoader {
	
	public static final int COLOR_WATER = 0x000000FF;//RGBA value, last byte must be not null otherwise color will be set to null
	
	private static final int ID_SHIFT = 8;
	
	private int currentSurfaceIndex = 0;//store surface index while drawing them
	protected int[] surfacesIndexes = new int[valueMapSize * valueMapSize];//store surface id +1 for each pixel, +1 because 0 means no surface
	//protected int[] surfacesAltis = new int[valueMapSize * valueMapSize];//store altitude for each surface point
	protected Map<Integer, Map<Integer, Integer>> surfacesReferenceAltis = new TreeMap<Integer, Map<Integer, Integer>>();

	public HydroSurfacesImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//data connector
		dataConnector = new WFSDataConnector(this, Utilities.properties.getProperty("hydroSurfacesLayer"));
		
		//redefine defaulf color to be able to inject element id into it
		colorsToValues.clear();
		colorsToValues.put(COLOR_WATER, ValueBlockDefinition.DEFAULT_VALUE);
	}

	@Override
	public boolean treatFeature(Object feature) {
		SimpleFeature wfsFeature = (SimpleFeature) feature;
		//only display constantly flowing rivers
		if(wfsFeature.getAttribute("nature").toString().equalsIgnoreCase("surface d'eau")
			&& !wfsFeature.getAttribute("regime").toString().equalsIgnoreCase("Permanent")) {
			return false;
		}
		
		//set a color that includes the id
		graphics.setColor( toColor((currentSurfaceIndex << ID_SHIFT) + COLOR_WATER) );
		
		surfacesReferenceAltis.put(currentSurfaceIndex, new TreeMap<Integer, Integer>());
		
		currentSurfaceIndex ++;
		
		return true;
	}
	
	@Override
	protected void drawFromCoords(Geometries geomType, int[] coordGridX, int[] coordGridY, int[] coordGridZ, int coordsLength, boolean isHole) {
		//at drawing level, keep the altitude of each reference point
		if (isHole == false) {
			assert coordGridX.length == coordGridY.length;
			assert coordGridX.length == coordGridZ.length;
	        for (int n = 0; n < coordGridX.length; n++) {
	        	if (coordGridX[n] >= 0 && coordGridX[n] < valueMapSize && coordGridY[n] >= 0 && coordGridY[n] < valueMapSize) {
	        		//surfacesAltis[coordGridY[n]*valueMapSize + coordGridX[n]] = coordGridZ[n];
	        		assert surfacesReferenceAltis.containsKey(currentSurfaceIndex - 1);
	        		if (coordGridZ[n] > 0) {
	        			//in some cases of unknown altitude, this value will be set to a negative value
		        		surfacesReferenceAltis.get(currentSurfaceIndex - 1)
		        			.put(coordGridY[n]*valueMapSize + coordGridX[n], coordGridZ[n]);
	        		}
	        	}
	        }
		}
		
		super.drawFromCoords(geomType, coordGridX, coordGridY, coordGridZ, coordsLength, isHole);
	}

	@Override
	protected void treatPixelValue(int x, int y, int pixelValueRGBA) {
		if(pixelValueRGBA != 0) {
			//separate surface id and color
			int surfaceId = pixelValueRGBA >>> ID_SHIFT;
			int colorValue = pixelValueRGBA & COLOR_WATER;
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
						&& map.getSurfaceBlock(x, y).canBeReplaced("HydroSurfacesImporter", x, y, valueMapSize)) {
					currentIndex = surfacesIndexes[y*valueMapSize + x] - 1;
					assert currentIndex < currentSurfaceIndex;
					surface = IdentifiedBlockFactory.getBlockDefinition("WaterSurfaceBlockDefinition", currentIndex);
					map.setSurfaceBlock(x, y, surface);
				}
			}
		}
		
		Map<Integer, Integer> modifiedReferenceAltis;
		int curAlti;
		int curX, curY;
		int otherX, otherY;
		int levelMaxSquareDist = 900;//30 * 30

		//set the reference altitudes
		for (currentIndex = 0; currentIndex < currentSurfaceIndex; currentIndex++) {
			assert surfacesReferenceAltis.containsKey(currentIndex);
			Set<Map.Entry<Integer, Integer>> curSurfaceReferenceAltis = surfacesReferenceAltis.get(currentIndex).entrySet();
			//store result in another map, so we keep original data during the treatment
			// otherwise first value could spread to all values
			modifiedReferenceAltis = new TreeMap<Integer, Integer>(); 
			for (Map.Entry<Integer, Integer> adjustedRefAlti : curSurfaceReferenceAltis) {
				//get currently adjusted alti, and coordinates
				curAlti = adjustedRefAlti.getValue();
				curX = adjustedRefAlti.getKey() % valueMapSize;
				curY = adjustedRefAlti.getKey() / valueMapSize;
				//level with surrounding altis
				for (Map.Entry<Integer, Integer> otherRefAlti : curSurfaceReferenceAltis) {
					if(otherRefAlti.getKey() != adjustedRefAlti.getKey()) {
						otherX = otherRefAlti.getKey() % valueMapSize;
						otherY = otherRefAlti.getKey() / valueMapSize;
						if ( ((otherX - curX) * (otherX - curX) + (otherY - curY) * (otherY - curY)) <= levelMaxSquareDist ) {
							curAlti = Math.min(curAlti,
									Math.min(otherRefAlti.getValue(),
											map.getGroundLevel(otherX, otherY) - 1));
						}
					}
				}
				//store eventually modified alti
				modifiedReferenceAltis.put(adjustedRefAlti.getKey(), curAlti);
			}
			//store the all set of treated altitudes
			surfacesReferenceAltis.put(currentIndex, modifiedReferenceAltis);
		}

		WaterSurfaceBlockDefinition.setWaterAltitudes(surfacesReferenceAltis);
	}
}
