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
import ign.minecraft.definition.BlockDefinition;
import ign.minecraft.definition.FieldBlockDefinition;
import ign.minecraft.definition.IdentifiedBlockFactory;
import ign.minecraft.definition.ValueBlockDefinition;
import ign.minecraft.definition.VegetalZoneBlockDefinition;

public class VegetalZonesImporter extends PolygonReadyVectorLoader {
	
	private static final double PINE_LOW_ALTI_MAX = 300;
	private static final double PINE_MEDIUM_ALTI_MAX = 600;

	private static final int COLORVARIATION_TREES = 0x000001FF;
	private static final int COLORVARIATION_DECIDUOUS_FOREST = 0x000002FF;
	private static final int COLORVARIATION_PINE_FOREST = 0x000003FF;
	private static final int COLORVARIATION_MIXED_FOREST = 0x000004FF;
	private static final int COLORVARIATION_CLEAR_FOREST = 0x000005FF;
	private static final int COLORVARIATION_POPLARS = 0x000006FF;
	private static final int COLORVARIATION_HEDGE = 0x000007FF;
	private static final int COLORVARIATION_BUSHES = 0x000008FF;
	private static final int COLORVARIATION_ORCHARD = 0x000009FF;
	private static final int COLORVARIATION_VINEYARD = 0x00000AFF;
	private static final int COLORVARIATION_WOOD = 0x00000BFF;
	private static final int COLORVARIATION_FILTER = 0x00000FFF;

	private static final int ID_SHIFT = 12;

	private int currentZoneIndex = 0;//store building index while drawing them
	protected final int[] zonesIndexes = new int[valueMapSize * valueMapSize];//store building id +1 for each pixel, +1 because 0 means no building

	protected final boolean[] trees = new boolean[valueMapSize * valueMapSize];//indicates if a tree was generated for each position
	protected final boolean[] plants = new boolean[valueMapSize * valueMapSize];//indicates if a plant was generated for each position
	
	public VegetalZonesImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//data connector
		dataConnector = new WFSDataConnector(this, Utilities.properties.getProperty("vegetalZonesLayer"));
		
		/* set colors */
		colorsToValues.clear();
		colorsToValues.put(COLORVARIATION_TREES, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.TREES.ordinal()));
		colorsToValues.put(COLORVARIATION_DECIDUOUS_FOREST, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.DECIDUOUS_FOREST.ordinal()));
		colorsToValues.put(COLORVARIATION_PINE_FOREST, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.PINE_FOREST_MEDIUM.ordinal()));
		colorsToValues.put(COLORVARIATION_MIXED_FOREST, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.MIXED_FOREST.ordinal()));
		colorsToValues.put(COLORVARIATION_CLEAR_FOREST, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.CLEAR_FOREST.ordinal()));
		colorsToValues.put(COLORVARIATION_POPLARS, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.POPLARS.ordinal()));
		colorsToValues.put(COLORVARIATION_HEDGE, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.HEDGE.ordinal()));
		colorsToValues.put(COLORVARIATION_BUSHES, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.BUSHES.ordinal()));
		colorsToValues.put(COLORVARIATION_ORCHARD, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.ORCHARD.ordinal()));
		colorsToValues.put(COLORVARIATION_VINEYARD, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.VINEYARD.ordinal()));
		colorsToValues.put(COLORVARIATION_WOOD, new ValueBlockDefinition(VegetalZoneBlockDefinition.ZoneType.WOOD.ordinal()));
	}

	@Override
	public boolean treatFeature(Object feature) {
    	//set color depending on nature
    	Object typeAttribute = ((SimpleFeature) feature).getAttribute("nature");
    	if(typeAttribute != null) {
    		switch (typeAttribute.toString().toLowerCase()) {
    		case "zone arborée":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_TREES) );
    			break;
    		case "forêt fermée de feuillus":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_DECIDUOUS_FOREST) );
    			break;
    		case "forêt fermée de conifères":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_PINE_FOREST) );
    			break;
    		case "forêt fermée mixte":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_MIXED_FOREST) );
    			break;
    		case "forêt ouverte":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_CLEAR_FOREST) );
    			break;
    		case "peupleraie":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_POPLARS) );
    			break;
    		case "haie":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_HEDGE) );
    			break;
    		case "lande ligneuse":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_BUSHES) );
    			break;
    		case "verger":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_ORCHARD) );
    			break;
    		case "vigne":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_VINEYARD) );
    			break;
    		case "bois":
    			graphics.setColor( toColor((currentZoneIndex << ID_SHIFT) + COLORVARIATION_WOOD) );
    			break;
    		case "bananeraie":
    		case "mangrove":
    		case "canne à sucre":
    			//not treated
    			return false;
    		default:
    			assert false;//unexpected
    			return false;
    		}
    	}
		//set index for next building
		currentZoneIndex++;
		return true;
	}
	
	@Override
	protected void treatPixelValue(int x, int y, int pixelValueRGBA) {
		if(pixelValueRGBA != 0) {
			//separate zone id and color
			int zoneId = pixelValueRGBA >>> ID_SHIFT;
			int colorValue = pixelValueRGBA & COLORVARIATION_FILTER;
			assert zoneId < currentZoneIndex;
			//store zone index and treat color normally
			zonesIndexes[y*valueMapSize + x] = zoneId + 1;//+1 to keep 0 as no zone
			super.treatPixelValue(x, y, colorValue);
		}
	}


	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;

		VegetalZoneBlockDefinition.ZoneType curType;
		BlockDefinition zone;
		//browse each planar coordinate to set stuff
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				//check there is not already something here
				if ((valueMap[y*valueMapSize + x] != null)
						&& map.getSurfaceBlock(x, y).canPutOverlayLayer(x, y, valueMapSize)) {
					curType = VegetalZoneBlockDefinition.ZoneType.values()[ ((ValueBlockDefinition)valueMap[y*valueMapSize + x]).value ];
					switch ( curType ) {
					case ORCHARD:
						//specific treatment for orchards and vineyards
						zone = IdentifiedBlockFactory.getBlockDefinition("FieldBlockDefinition",
								zonesIndexes[y*valueMapSize + x] - 1,
								new Object[] { FieldBlockDefinition.FieldType.ORCHARD });
						break;
					case VINEYARD:
						//specific treatment for orchards and vineyards
						zone = IdentifiedBlockFactory.getBlockDefinition("FieldBlockDefinition",
								zonesIndexes[y*valueMapSize + x] - 1,
								new Object[] { FieldBlockDefinition.FieldType.VINEYARD });
						break;
					case PINE_FOREST_LOW:
					case PINE_FOREST_MEDIUM:
					case PINE_FOREST_HIGH:
						//adjust type according to altitude
						//if the zone has already been created, the type will stay as the first one given
						double spawnAlti = this.altiImporter.getRealAlti(x, y);
						if (spawnAlti <= PINE_LOW_ALTI_MAX) {
							curType = VegetalZoneBlockDefinition.ZoneType.PINE_FOREST_LOW;
						} else if (spawnAlti <= PINE_MEDIUM_ALTI_MAX) {
							curType = VegetalZoneBlockDefinition.ZoneType.PINE_FOREST_MEDIUM;
						} else {
							curType = VegetalZoneBlockDefinition.ZoneType.PINE_FOREST_HIGH;
						}
					default:
						//generic case : create or get the zone with the specified id and type
						zone = IdentifiedBlockFactory.getBlockDefinition("VegetalZoneBlockDefinition",
								zonesIndexes[y*valueMapSize + x] - 1,
								new Object[]{ curType });
						((VegetalZoneBlockDefinition) zone).setBaseBlock(x, y, map.getSurfaceBlock(x, y));
					}
					
					map.setSurfaceBlock(x, y, zone);
				}
			}
		}
	}


}
