/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.Utilities;
import ign.minecraft.definition.BuildingBlockDefinition;
import ign.minecraft.definition.BuildingCategory;
import ign.minecraft.definition.IdentifiedBlockFactory;
import ign.minecraft.definition.SimpleBlocks;
import ign.minecraft.definition.ValueBlockDefinition;

public class BuildingsImporter extends PolygonReadyVectorLoader {

	public static final int COLOR_BUILDING = 0x000000FF;//RGBA value, last byte must be not null otherwise color will be set to null

	private static final int ID_SHIFT = 8;

    public static boolean TRACE_ONLY = false;
	private int currentBuildingIndex = 0;//store building index while drawing them
	protected int[] buildingsIndexes = new int[valueMapSize * valueMapSize];//store building id +1 for each pixel, +1 because 0 means no building
	protected TreeMap<Integer,Short> buildingsHeights = new TreeMap<Integer,Short>();//store height for building id corresponding to buildings that have a non null height
	protected TreeMap<Integer,Geometry> buildingsGeometries = new TreeMap<Integer,Geometry>();//store geometry for building id corresponding to buildings that have a non null height
	protected TreeMap<Integer,BuildingCategory> buildingsTypes = new TreeMap<Integer,BuildingCategory>();//store a constant representing the type of building if it is specific
	
	protected boolean redrawing = false; // will be set to true when redrawing buildings after max height sorting
	
	public BuildingsImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		String serviceLayer = Utilities.properties.getProperty("buildingsLayer");
		
		//data connector
		dataConnector = new WFSDataConnector(this, serviceLayer);
		
		/* set colors */
		colorsToValues.clear();
		colorsToValues.put(COLOR_BUILDING, ValueBlockDefinition.DEFAULT_VALUE);
	}

	@Override
	public boolean treatFeature(Object feature) {
		SimpleFeature wfsFeature = (SimpleFeature) feature;
		//set a color that includes the id
		graphics.setColor( toColor((currentBuildingIndex << ID_SHIFT) + COLOR_BUILDING) );
		//store height and geometry of building
    	int buildingHeight = Integer.parseInt(wfsFeature.getAttribute("hauteur").toString());
    	if(buildingHeight > 0) {
    		buildingsHeights.put(currentBuildingIndex, (short) (buildingHeight*MineGenerator.MINECRAFTMAP_RATIO));
    		//store geometry to redraw buildings after sorting by roof altitude (in minecraft world)
    		buildingsGeometries.put(currentBuildingIndex, (Geometry) wfsFeature.getDefaultGeometry());
    	}
    	//store type of building if there is one
    	Object typeAttribute = wfsFeature.getAttribute("nature");
    	if(typeAttribute != null) {
    		switch (typeAttribute.toString().toLowerCase()) {
    		case "château":
    		case "eglise":
    		case "tour, donjon, moulin":
    			buildingsTypes.put(currentBuildingIndex, BuildingCategory.CHURCH_CASTLE);
    			break;
    		default:
    		}
    	}
		//set index for next building
		currentBuildingIndex++;
		return true;
	}
	
	@Override
	protected void treatPixelValue(int x, int y, int pixelValueRGBA) {
		if(pixelValueRGBA != 0) {
			//separate building id and color
			int buildingId = pixelValueRGBA >>> ID_SHIFT;
			int colorValue = pixelValueRGBA & COLOR_BUILDING;
			assert buildingId < currentBuildingIndex;
			//store building index and treat color normally
			buildingsIndexes[y*valueMapSize + x] = buildingId + 1;//+1 to keep 0 as no building
			super.treatPixelValue(x, y, colorValue);
		}
	}


	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		short[] buildingsAltis;//store base altitude for each building id

		//browse each planar coordinate to compute altitude of buildings
		buildingsAltis = new short[ currentBuildingIndex ];
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				if(buildingsIndexes[y*valueMapSize + x] > 0) {
					assert (buildingsIndexes[y*valueMapSize + x] - 1) < currentBuildingIndex;
					if (buildingsAltis[ buildingsIndexes[y*valueMapSize + x] - 1 ] == 0) {
						buildingsAltis[ buildingsIndexes[y*valueMapSize + x] - 1 ] = map.getGroundLevel(x, y);
					} else {
						buildingsAltis[ buildingsIndexes[y*valueMapSize + x] - 1 ] = 
								(short) Math.min(buildingsAltis[ buildingsIndexes[y*valueMapSize + x] - 1 ],
												map.getGroundLevel(x, y));
					}
				}
			}
		}
		
		//now we have altitudes, sort geometries by increasing top heights of buildings (base altitude + height)
		List< Entry<Integer, Geometry> > geometriesSortedList = new LinkedList< Entry<Integer, Geometry> >(buildingsGeometries.entrySet());
		Collections.sort(geometriesSortedList,
				new Comparator< Entry<Integer, Geometry> >() {
					@Override
					public int compare(Entry<Integer, Geometry> o1, Entry<Integer, Geometry> o2) {
						assert (o1.getKey() < buildingsAltis.length) && (o2.getKey() < buildingsAltis.length);
						assert buildingsHeights.containsKey(o1.getKey()) && buildingsHeights.containsKey(o2.getKey());
						return (buildingsAltis[o1.getKey()] + buildingsHeights.get(o1.getKey()))
								- (buildingsAltis[o2.getKey()] + buildingsHeights.get(o2.getKey()));
					}
				}
			);
		
		// redraw buildings we just sorted
		redrawing = true;
		Iterator< Entry<Integer, Geometry> > sortedGeometriesIterator = geometriesSortedList.iterator();
		while ( sortedGeometriesIterator.hasNext() ) {
			Entry<Integer, Geometry> geometryEntry = sortedGeometriesIterator.next();
			//create colors for drawing
			int buildingIndex = geometryEntry.getKey();
			//bugfix : some buildings were erased by later drawn buildings in the first pass
			//   we should fix to avoid this erasing but for the moment we don't treat them here
			//   otherwise they could appear and we would miss the alti information
			if (buildingsAltis[buildingIndex] > 0) {
				graphics.setColor( toColor((buildingIndex << ID_SHIFT) + COLOR_BUILDING) );
				//do the drawing
				try {
					treatGeometry(geometryEntry.getValue());
				} catch (Exception e) {
					//unexpected
					assert false;
				}
			}
		}
		//re-read data from pixels
		try {
			treatValues();
		} catch (MinecraftGenerationException e) {
			//unexpected
			assert false;
		}

		//browse each planar coordinate to set stuff
		int buildingIndex;
		short buildingHeight;
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				if ((valueMap[y*valueMapSize + x] != null)
						&& map.getSurfaceBlock(x, y).canBeReplaced("BuildingsImporter", x, y, valueMapSize)) {
					if (buildingsHeights.containsKey( buildingsIndexes[y*valueMapSize + x] - 1 ) && !TRACE_ONLY) {
						BuildingBlockDefinition building;
						buildingIndex = buildingsIndexes[y*valueMapSize + x] - 1;
						buildingHeight = buildingsHeights.get(buildingIndex);
						building = (BuildingBlockDefinition) IdentifiedBlockFactory.getBlockDefinition("BuildingBlockDefinition",
								buildingIndex,
								new Object[]{
									buildingsTypes.containsKey(buildingIndex) ? buildingsTypes.get(buildingIndex) : BuildingCategory.GENERIC,
									buildingHeight
								});
						map.setSurfaceBlock(x, y, building );
					} else {
						//null height
						map.setSurfaceBlock(x, y, SimpleBlocks.FLATBUILDING.get() );
					}
				}

			}
		}
	}
}
