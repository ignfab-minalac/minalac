/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.feature.simple.SimpleFeature;

import ign.minecraft.Utilities;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.definition.SimpleBlocks;
import ign.minecraft.definition.ValueBlockDefinition;

public class OCSImporter extends PolygonReadyVectorLoader {
	private static final Logger LOGGER = Logger.getLogger("OCSImporter");
	
	private final static int COLOR_BATI = 0x000000FF;
	private final static int COLOR_BUSH = 0x00000FFF;
	private final static int COLOR_QUARRY = 0x0000F0FF;
	private final static int COLOR_WATER = 0x000F00FF;
	private final static int COLOR_FOREST = 0x00F000FF;
	private final static int COLOR_GLACIER = 0x0F0000FF;
	private final static int COLOR_MANGROVE = 0xF0000FFF;
	private final static int COLOR_MARSH = 0xF0000FFF;
	private final static int COLOR_SALTMARSH = 0xF000F0FF;
	private final static int COLOR_MEADOW = 0xF00F00FF;
	private final static int COLOR_ROCKS = 0xF0F000FF;
	private final static int COLOR_SAND = 0xFF0000FF;
	private final static int COLOR_ORCHARD = 0xFF000FFF;
	private final static int COLOR_ACTIVITYZONE = 0xFF00F0FF;
	
	public OCSImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//BDCARTO layer is zone dependant
		String serviceLayer;
		serviceLayer = Utilities.properties.getProperty("ocsLayer");
		
		//data connector
		dataConnector = new WFSDataConnector(this, serviceLayer);
		
		// set colors to values 
		colorsToValues.clear();
		colorsToValues.put(COLOR_BATI, new ValueBlockDefinition(SimpleBlocks.BUILDINGSZONE.ordinal()));
		colorsToValues.put(COLOR_BUSH, new ValueBlockDefinition(SimpleBlocks.BUSHZONE.ordinal()));
		colorsToValues.put(COLOR_QUARRY, new ValueBlockDefinition(SimpleBlocks.QUARRYZONE.ordinal()));
		colorsToValues.put(COLOR_WATER, new ValueBlockDefinition(SimpleBlocks.RIVERZONE.ordinal()));
		colorsToValues.put(COLOR_FOREST, new ValueBlockDefinition(SimpleBlocks.FORESTZONE.ordinal()));
		colorsToValues.put(COLOR_GLACIER, new ValueBlockDefinition(SimpleBlocks.GLACIERZONE.ordinal()));
		colorsToValues.put(COLOR_MANGROVE, new ValueBlockDefinition(SimpleBlocks.MARSHZONE.ordinal()));
		colorsToValues.put(COLOR_MARSH, new ValueBlockDefinition(SimpleBlocks.MARSHZONE.ordinal()));
		colorsToValues.put(COLOR_SALTMARSH, new ValueBlockDefinition(SimpleBlocks.SALTMARSHZONE.ordinal()));
		colorsToValues.put(COLOR_MEADOW, new ValueBlockDefinition(SimpleBlocks.MEADOWZONE.ordinal()));
		colorsToValues.put(COLOR_ROCKS, new ValueBlockDefinition(SimpleBlocks.ROCKSZONE.ordinal()));
		colorsToValues.put(COLOR_SAND, new ValueBlockDefinition(SimpleBlocks.SANDZONE.ordinal()));
		colorsToValues.put(COLOR_ORCHARD, new ValueBlockDefinition(SimpleBlocks.ORCHARDZONE.ordinal()));
		colorsToValues.put(COLOR_ACTIVITYZONE, new ValueBlockDefinition(SimpleBlocks.BUILDINGSZONE.ordinal()));
		// set colors
		createColors();
	}

	@Override
	public boolean treatFeature(Object feature) {
		assert SimpleFeature.class.isAssignableFrom(feature.getClass());
		//set color according to nature
    	Object typeAttribute = ((SimpleFeature) feature).getAttribute("nature");
    	if(typeAttribute != null) {
    		switch (typeAttribute.toString().toLowerCase()) {
    		case "bâti":
    			graphics.setColor(colors.get(COLOR_BATI));
    			break;
    		case "broussailles":
    			graphics.setColor(colors.get(COLOR_BUSH));
    			break;
    		case "carrière, décharge":
    			graphics.setColor(colors.get(COLOR_QUARRY));
    			break;
    		case "eau libre":
    			graphics.setColor(colors.get(COLOR_WATER));
    			break;
    		case "forêt":
    			graphics.setColor(colors.get(COLOR_FOREST));
    			break;
    		case "glacier, névé":
    			graphics.setColor(colors.get(COLOR_GLACIER));
    			break;
    		case "mangrove":
    			graphics.setColor(colors.get(COLOR_MANGROVE));
    			break;
    		case "marais, tourbière":
    			graphics.setColor(colors.get(COLOR_MARSH));
    			break;
    		case "marais salant":
    			graphics.setColor(colors.get(COLOR_SALTMARSH));
    			break;
    		case "prairie":
    			graphics.setColor(colors.get(COLOR_MEADOW));
    			break;
    		case "rocher, éboulis":
    			graphics.setColor(colors.get(COLOR_ROCKS));
    			break;
    		case "sable, gravier":
    			graphics.setColor(colors.get(COLOR_SAND));
    			break;
    		case "vigne, verger":
    			graphics.setColor(colors.get(COLOR_ORCHARD));
    			break;
    		case "zone d'activités":
    			graphics.setColor(colors.get(COLOR_ACTIVITYZONE));
    			break;
    		default:
    			LOGGER.log(Level.WARNING, "unknown OCS nature : " + typeAttribute.toString());
    			assert false;
    			return false;
    		}
    	}
		return true;
	}

	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		//for each planar coordinate set default surface and altitude
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				//only apply value is base block is not the sea
				if ((valueMap[y*valueMapSize + x] != null)
						&& map.getSurfaceBlock(x, y).canBeReplaced("OCSImporter", x, y, valueMapSize)) {
					map.setSurfaceBlock(x, y,
						SimpleBlocks.values()[ ((ValueBlockDefinition)valueMap[y*valueMapSize + x]).value ].get() );
				}
			}
		}
	}
}
