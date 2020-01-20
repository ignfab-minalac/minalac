/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.nio.file.Path;
import java.util.TreeMap;

import org.opengis.feature.simple.SimpleFeature;

import ign.minecraft.Utilities;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.definition.IdentifiedBlockDefinition;
import ign.minecraft.definition.IdentifiedBlockFactory;
import ign.minecraft.definition.ValueBlockDefinition;
import ign.minecraft.definition.WallBlockDefinition;

public class LinearConstructionsImporter extends VectorLoader {

	public static final byte VALUE_RUINS = 1;
	public static final byte VALUE_WALL = 2;
	public static final byte VALUE_DAM = 3;
	public static final byte VALUE_BRIDGE = 4;
	private static final int COLORVARIATION_RUINS = 0x000001FF;
	private static final int COLORVARIATION_WALL = 0x000002FF;
	private static final int COLORVARIATION_DAM = 0x000003FF;
	private static final int COLORVARIATION_BRIDGE = 0x000004FF;
	private static final int COLORVARIATION_FILTER = 0x000007FF;

	private static final int ID_SHIFT = 11;
	
	private static final Stroke STROKE_RUINS = new BasicStroke(1);
	private static final Stroke STROKE_WALL = new BasicStroke(1);
	private static final Stroke STROKE_DAM = new BasicStroke(3);
	//private static final Stroke STROKE_BRIDGE = new BasicStroke(2);
	
	private int currentConstructionIndex = 0;//store construction index while drawing them
	protected int[] constructionsIndexes = new int[valueMapSize * valueMapSize];//store construction id +1 for each pixel, +1 because 0 means no construction
	protected final TreeMap<Integer, Short> constructionsAltis = new TreeMap<Integer, Short>();//store top altitude for each construction id
	
	public LinearConstructionsImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//data connector
		dataConnector = new WFSDataConnector(this, Utilities.properties.getProperty("linearConstructionsLayer"));
		
		// set colors
		colorsToValues.clear();
		colorsToValues.put(COLORVARIATION_RUINS, new ValueBlockDefinition(VALUE_RUINS));
		colorsToValues.put(COLORVARIATION_WALL, new ValueBlockDefinition(VALUE_WALL));
		colorsToValues.put(COLORVARIATION_DAM, new ValueBlockDefinition(VALUE_DAM));
		colorsToValues.put(COLORVARIATION_BRIDGE, new ValueBlockDefinition(VALUE_BRIDGE));
	}

	@Override
	public boolean treatFeature(Object feature) {
		SimpleFeature wfsFeature = (SimpleFeature) feature;
    	//store type of building if there is one
    	Object typeAttribute = wfsFeature.getAttribute("nature");
    	double bdtopoZ;
    	if(typeAttribute != null) {
    		switch (typeAttribute.toString().toLowerCase()) {
    		case "indifférencié":
    			graphics.setColor( toColor((currentConstructionIndex << ID_SHIFT) + COLORVARIATION_WALL) );
    			graphics.setStroke(STROKE_WALL);
    			break;
    		case "ruines":
    			graphics.setColor( toColor((currentConstructionIndex << ID_SHIFT) + COLORVARIATION_RUINS) );
    			graphics.setStroke(STROKE_RUINS);
    			break;
    		case "barrage":
    			graphics.setColor( toColor((currentConstructionIndex << ID_SHIFT) + COLORVARIATION_DAM) );
    			graphics.setStroke(STROKE_DAM);
    	    	//store altitude given by BD Topo
    			bdtopoZ = Double.parseDouble(wfsFeature.getAttribute("z_max").toString());
    			if (bdtopoZ < 9999) {
    				constructionsAltis.put(currentConstructionIndex,
        	    			(short) altiImporter.transformAltitude( bdtopoZ ));
    			} else {
    				constructionsAltis.put(currentConstructionIndex, (short) 0);
    			}
    	    	
    			break;
    		case "pont":
    			//don't treat linear bridges here, roads on them will be treated to ensure the correct altitude and width
    			/*
    			graphics.setColor( toColor((currentConstructionIndex << ID_SHIFT) + COLORVARIATION_BRIDGE) );
    			graphics.setStroke(STROKE_BRIDGE);
    			constructionsTypes.put(currentConstructionIndex, VALUE_BRIDGE);
    			break;
    			*/
    			return false;
    		default:
    			return false; // don't treat other constructions
    		}
    	} else {
    		return false;
    	}
		//set index for next building
    	currentConstructionIndex++;
		return true;
	}
	
	@Override
	protected void treatPixelValue(int x, int y, int pixelValueRGBA) {
		if(pixelValueRGBA != 0) {
			//separate construction id and color
			int constructionId = pixelValueRGBA >>> ID_SHIFT;
			int colorValue = pixelValueRGBA & COLORVARIATION_FILTER;
			//store construction index and treat color normally
			constructionsIndexes[y*valueMapSize + x] = constructionId + 1;//+1 to keep 0 as no building
			super.treatPixelValue(x, y, colorValue);
		}
	}


	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		int constructionIndex;

		//browse each planar coordinate to set stuff
		IdentifiedBlockDefinition construction = null;
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				constructionIndex = constructionsIndexes[y*valueMapSize + x] - 1;
				//set surface block
				if ((valueMap[y*valueMapSize + x] != null)
						&& map.getSurfaceBlock(x, y).canBeReplaced("LinearConstructionsImporter", x, y, valueMapSize)) {
					switch (((ValueBlockDefinition)valueMap[y*valueMapSize + x]).value) {
					case VALUE_RUINS :
						construction = IdentifiedBlockFactory.getBlockDefinition("WallBlockDefinition", constructionIndex, new Object[] { (Integer) WallBlockDefinition.SIMPLE_WALL_HEIGHT });
						break;
					case VALUE_WALL :
						construction = IdentifiedBlockFactory.getBlockDefinition("WallBlockDefinition", constructionIndex, new Object[] { (Integer) WallBlockDefinition.RUIN_WALL_HEIGHT });
						break;
					case VALUE_DAM :
						construction = IdentifiedBlockFactory.getBlockDefinition("DamBlockDefinition", constructionIndex, new Object[] { constructionsAltis.get(constructionIndex) });
						break;
					default:
						assert false;//unexpected
					}
					map.setSurfaceBlock(x, y, construction);
				}
			}
		}
	}
}
