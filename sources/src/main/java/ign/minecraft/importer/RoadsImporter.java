/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.geometry.jts.Geometries;
import org.opengis.feature.simple.SimpleFeature;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import ign.minecraft.Utilities;
import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.definition.BlockDefinition;
import ign.minecraft.definition.DamBlockDefinition;
import ign.minecraft.definition.RoadBlockDefinition;
import ign.minecraft.definition.ValueBlockDefinition;

public class RoadsImporter extends ColorTreatmentVectorLoader {
	private static final Logger LOGGER = Logger.getLogger("RoadsImporter");
	
	//drawing roads as gradient will enable to even the altitude of the road on perpendicular axes
	//so altitude should only change along the road
	private final static int ROADGRADIENT_FILTER = 0x000000FF;//255 values of gradient
	private final static int ROADGRADIENT_STARTGRADIENT = 0x00000001;//avoid 0 alpha
	private final static int ROADGRADIENT_MAXENDGRADIENT = 0x000000FF;

	// higher values will prevail when drawing
	private final static int COLORVARIATION_HIGHWAY = 0x00000700;
	private final static int COLORVARIATION_ROAD = 0x00000600;
	private final static int COLORVARIATION_STONEROAD = 0x00000500;
	private final static int COLORVARIATION_DIRTROAD = 0x00000400;
	private final static int COLORVARIATION_CYCLELANE = 0x00000300;
	private final static int COLORVARIATION_TRAIL = 0x00000200;
	private final static int COLORVARIATION_STAIRS = 0x00000100;
	private final static int COLORVARIATION_FILTER = 0x00000700;
	private final static int COLORVARIATION_FILTER_ARGB = 0x00000007;
	
	private static final int ID_SHIFT = 11;
	private static final int MAX_ID = 0x1FFFFF;
	
	private final static byte VALUE_HIGHWAY = 1;
	private final static byte VALUE_ROAD = 2;
	private final static byte VALUE_STONEROAD = 3;
	private final static byte VALUE_DIRTROAD = 4;
	private final static byte VALUE_CYCLELANE = 5;
	private final static byte VALUE_TRAIL = 6;
	private final static byte VALUE_STAIRS = 7;

    protected SimpleFeature currentFeature;//store current feature inside the rendering loop

    // second image and graphics to draw separately bridges
	// and bridge dedicated value map
	protected BufferedImage bridgesImage;
	protected Graphics2D bridgesGraphics;
	protected BlockDefinition[] bridgesValueMap;

	/* for each element we store one color per altitude, combined with the element id and the type color */
	private int currentRoadIndex = 0;//store road index while drawing them, ands with number of roads
	private final int[] roadsIndexes = new int[valueMapSize * valueMapSize];//store road id +1 for each pixel, +1 because 0 means no building
	private final byte[] roadsGradientValues = new byte[valueMapSize * valueMapSize];//store gradient value of each pixel
	private final TreeMap<Integer, TreeMap<Byte,Short>> altiByGradientByRoad = new TreeMap<Integer, TreeMap<Byte,Short>>();//store altitude of each gradient value for each road
	
	private final int[] bridgesIndexes = new int[valueMapSize * valueMapSize];//store road id +1 for each pixel on bridge layer, +1 because 0 means no building
	private final byte[] bridgesGradientValues = new byte[valueMapSize * valueMapSize];//store gradient value of each pixel on bridge layer
	private final ArrayList<Integer> bridgesIds = new ArrayList<Integer>(16);//bridges road id
	// same one with global start and end of bridges
	private final TreeMap<Integer, Integer> bridgesAdjustedJunctions = new TreeMap<Integer, Integer>();
	
	// junction coord saving : save road id on start and end coordinates
	// ends with a map coordinate => road id list
	// map coordinate is stores as 32bits int : higher half is Y, lower half is X
	private final TreeMap<Integer, ArrayList<Integer>> roadsJunctionCoords = new TreeMap<Integer, ArrayList<Integer>>();
	// junction saving : save coordinate of start and end of roads, save start with id as key and end with -id as key
	private final TreeMap<Integer, Integer> roadsJunctions = new TreeMap<Integer, Integer>();
	
	private static int junctionCoord(int x, int y) {
		assert x <= 0xffff;
		assert y <= 0xffff;
		return y << 16 | x;
	}
	
	public RoadsImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		bridgesValueMap = new BlockDefinition[valueMapSize * valueMapSize];

		//data connector
		dataConnector = new WFSDataConnector(this, Utilities.properties.getProperty("roadLayer"));
		
		// set colors
		colorsToValues.clear();
		colorsToValues.put(COLORVARIATION_HIGHWAY, new ValueBlockDefinition(VALUE_HIGHWAY));
		colorsToValues.put(COLORVARIATION_ROAD, new ValueBlockDefinition(VALUE_ROAD));
		colorsToValues.put(COLORVARIATION_STONEROAD, new ValueBlockDefinition(VALUE_STONEROAD));
		colorsToValues.put(COLORVARIATION_DIRTROAD, new ValueBlockDefinition(VALUE_DIRTROAD));
		colorsToValues.put(COLORVARIATION_CYCLELANE, new ValueBlockDefinition(VALUE_CYCLELANE));
		colorsToValues.put(COLORVARIATION_TRAIL, new ValueBlockDefinition(VALUE_TRAIL));
		colorsToValues.put(COLORVARIATION_STAIRS, new ValueBlockDefinition(VALUE_STAIRS));
		
		// create second set of graphic stuff for bridges */
		bridgesImage = new BufferedImage(valueMapSize, valueMapSize, BufferedImage.TYPE_4BYTE_ABGR);
        bridgesGraphics = bridgesImage.createGraphics();
        bridgesGraphics.setComposite(new CustomBlendComposite(this));
	}

	@Override
	public boolean treatFeature(Object feature) throws MinecraftGenerationException {
		if(currentRoadIndex > MAX_ID) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.ROADIMPORT_TOOMANYROADS_ERROR);
		}
		
		//store current feature, for use in drawing methods during the rendering
		currentFeature = (SimpleFeature) feature;

		//eliminate some types
		String nature = currentFeature.getAttribute("nature").toString();
		switch (nature.toLowerCase()) {
		case "autoroute":
		case "quasi-autoroute":
		case "bretelle":
		case "route à 2 chaussées":
		case "route à 1 chaussée":
		case "route empierrée":
		case "chemin":
		case "piste cyclable":
		case "sentier":
		case "escalier":
			break;
		case "bac auto":
		case "bac piéton":
			return false;
		default:
			assert false;//unexpected
			return false;
		}
		
		//for the moment, don't treat tunnels
		if(currentFeature.getAttribute("franchisst").toString().equalsIgnoreCase("tunnel")) {
			return false;
		}
		
		currentRoadIndex++;
		
		return true;
	}

	@Override
	protected void drawFromCoords(Geometries geomType, int[] coordGridX, int[] coordGridY, int[] coordGridZ, int coordsLength, boolean isHole) {
		assert isHole == false;

		int roadId = currentRoadIndex - 1;//-1 because id has already been advanced for next loop in treatFeature
		
		//store junctions
		int junctionCoord;
		ArrayList<Integer> junctions;
		int startX, startY, endX, endY;
		//make sure the coordinate that is stored is inside the value map
		startX = coordGridX[0];
		startY = coordGridY[0];
		endX = coordGridX[coordsLength-1];
		endY = coordGridY[coordsLength-1];
		if (startX >= valueMapSize || endX >= valueMapSize || startY >= valueMapSize || endY >= valueMapSize
				|| startX < 0 || endX < 0 || startY < 0 || endY < 0 ) {
			//feature is partly outside our map, trim it until start and end are into our map
			int startInsideIndex, endInsideIndex;
			for (startInsideIndex = 0; startInsideIndex < coordsLength; startInsideIndex++) {
				if (coordGridX[startInsideIndex] >= 0 && coordGridX[startInsideIndex] < valueMapSize
						&& coordGridY[startInsideIndex] >= 0 && coordGridY[startInsideIndex] < valueMapSize) {
					//inside our map
					break;
				}
			}
			for (endInsideIndex = coordsLength - 1; endInsideIndex >= 0; endInsideIndex--) {
				if (coordGridX[endInsideIndex] >= 0 && coordGridX[endInsideIndex] < valueMapSize
						&& coordGridY[endInsideIndex] >= 0 && coordGridY[endInsideIndex] < valueMapSize) {
					//inside our map
					break;
				}
			}
			if ((endInsideIndex - startInsideIndex) < 1) {
				//not even 2 points left, drop the feature
				return;
			}
			//make new reduced coord arrays
			int[] tmpCoordGridX = new int[endInsideIndex - startInsideIndex + 1];
			int[] tmpCoordGridY = new int[endInsideIndex - startInsideIndex + 1];
			int[] tmpCoordGridZ = new int[endInsideIndex - startInsideIndex + 1];
			for (int copyIndex = startInsideIndex; copyIndex <= endInsideIndex; copyIndex ++) {
				tmpCoordGridX[copyIndex - startInsideIndex] = coordGridX[copyIndex];
				tmpCoordGridY[copyIndex - startInsideIndex] = coordGridY[copyIndex];
				tmpCoordGridZ[copyIndex - startInsideIndex] = coordGridZ[copyIndex];
			}
			coordsLength = endInsideIndex - startInsideIndex + 1;
			coordGridX = tmpCoordGridX;
			coordGridY = tmpCoordGridY;
			coordGridZ = tmpCoordGridZ;
			startX = coordGridX[0];
			startY = coordGridY[0];
			endX = coordGridX[coordsLength-1];
			endY = coordGridY[coordsLength-1];
		}

		//start point
		junctionCoord = junctionCoord(startX, startY);
		//link the coordinate to the road 
		roadsJunctions.put(roadId, junctionCoord);
		//store the road linked to the coordinate
		if (!roadsJunctionCoords.containsKey(junctionCoord)) {
			junctions = new ArrayList<Integer>(4);
			roadsJunctionCoords.put(junctionCoord,junctions);
		} else {
			junctions = roadsJunctionCoords.get(junctionCoord);
		}
		junctions.add(roadId);

		//end point
		junctionCoord = junctionCoord(endX, endY);
		//link the coordinate to the road
		roadsJunctions.put(-roadId, junctionCoord);//negative because it's endpoint
		//store the road linked to the coordinate
		if (!roadsJunctionCoords.containsKey(junctionCoord)) {
			junctions = new ArrayList<Integer>(4);
			roadsJunctionCoords.put(junctionCoord,junctions);
		} else {
			junctions = roadsJunctionCoords.get(junctionCoord);
		}
		junctions.add(-roadId);//negative because it's endpoint
		
		//set alternative graphics for bridges
		Graphics2D backupGraphics = null;
		if(currentFeature.getAttribute("franchisst").toString().equalsIgnoreCase("pont")) {
			//store id
			bridgesIds.add(roadId);
			//set bridges graphics
			backupGraphics = graphics;
			graphics = bridgesGraphics;
		}
		
		//get width
		float largeur = Float.parseFloat(currentFeature.getAttribute("largeur").toString());
		if(largeur == 0)
			largeur = 1;
		
		String nature = currentFeature.getAttribute("nature").toString();
		//get base color
		int natureColorVariation;
		switch (nature.toLowerCase()) {
		case "autoroute":
		case "quasi-autoroute":
		case "bretelle":
			natureColorVariation = COLORVARIATION_HIGHWAY;
			largeur = Math.max(largeur, 3);
			break;
		case "route à 2 chaussées":
		case "route à 1 chaussée":
			natureColorVariation = COLORVARIATION_ROAD;
			largeur = Math.max(largeur, 3);
			break;
		case "route empierrée":
			natureColorVariation = COLORVARIATION_STONEROAD;
			largeur = Math.max(largeur, 3);
			break;
		case "chemin":
			natureColorVariation = COLORVARIATION_DIRTROAD;
			largeur = Math.max(largeur, 2);
			break;
		case "piste cyclable":
			natureColorVariation = COLORVARIATION_CYCLELANE;
			break;
		case "sentier":
			natureColorVariation = COLORVARIATION_TRAIL;
			break;
		case "escalier":
			natureColorVariation = COLORVARIATION_STAIRS;
			break;
		default:
			assert false;
			return;
		}
		graphics.setStroke(new BasicStroke(largeur));


		//set color gradient for drawing
		int[] gradientValues = new int[coordsLength];
		//break multilines into segments and compute all lengths and use cumulated length as gradients
		int totalLength = 0;
		for(int coordIndex = 0; coordIndex < coordsLength-1; coordIndex++) {
			gradientValues[coordIndex] = totalLength;
			totalLength += (int) Math.sqrt( 
					(coordGridX[coordIndex+1] - coordGridX[coordIndex]) * (coordGridX[coordIndex+1]-coordGridX[coordIndex])
					+ (coordGridY[coordIndex+1] - coordGridY[coordIndex]) * (coordGridY[coordIndex+1]-coordGridY[coordIndex]) );
		}
		//last gradient is totalLength
		gradientValues[coordsLength-1] = totalLength;
		//depending on type, set a gradient divisor to smooth the levels along the road
		double gradientDivisor = 1;
		switch (nature.toLowerCase()) {
		case "autoroute":
		case "quasi-autoroute":
		case "bretelle":
			gradientDivisor =  4;//one altitude value for 4 blocks
			break;
		case "route à 2 chaussées":
		case "route à 1 chaussée":
		case "route empierrée":
		case "chemin":
		case "piste cyclable":
			gradientDivisor = 3;//one altitude value for 3 blocks
			break;
		case "sentier":
		case "escalier":
			break;
		default:
			assert false;
			return;
		}
		
		//limit maximum gradient value to our max value
		if ((ROADGRADIENT_STARTGRADIENT + totalLength / gradientDivisor) > ROADGRADIENT_MAXENDGRADIENT) {
			gradientDivisor = totalLength / (float) (ROADGRADIENT_MAXENDGRADIENT - ROADGRADIENT_STARTGRADIENT);
		}
		
		// actually do the drawing of each segment
		int startGradient, endGradient;
		Color startColor;
		Color endColor;
		int[] segmentCoordX = new int[2];
		int[] segmentCoordY = new int[2];
		int[] segmentCoordZ = new int[2];
		for(int segmentIndex = 0; segmentIndex < coordsLength-1; segmentIndex++) {
			startGradient = ROADGRADIENT_STARTGRADIENT + (int) (gradientValues[segmentIndex]/gradientDivisor);
			assert startGradient >= ROADGRADIENT_STARTGRADIENT;
			assert startGradient <= ROADGRADIENT_MAXENDGRADIENT;
			endGradient = ROADGRADIENT_STARTGRADIENT + (int) (gradientValues[segmentIndex+1]/gradientDivisor);
			assert startGradient >= ROADGRADIENT_STARTGRADIENT;
			assert endGradient <= ROADGRADIENT_MAXENDGRADIENT;
			startColor = toColor((roadId << ID_SHIFT) | natureColorVariation | startGradient);
			endColor = toColor((roadId << ID_SHIFT) | natureColorVariation | endGradient);
			System.arraycopy(coordGridX, segmentIndex, segmentCoordX, 0, 2);
			System.arraycopy(coordGridY, segmentIndex, segmentCoordY, 0, 2);
			System.arraycopy(coordGridZ, segmentIndex, segmentCoordZ, 0, 2);
			graphics.setPaint( new GradientPaint(segmentCoordX[0], segmentCoordY[0], startColor,
													segmentCoordX[1], segmentCoordY[1], endColor) );
			super.drawFromCoords(geomType, segmentCoordX, segmentCoordY, segmentCoordZ, 2, false);
		}
		
		//set back normal graphics if needed
		if(backupGraphics != null) {
			graphics = backupGraphics;
		}
	}

	@Override
	public int blendColors(int srcPixelValueARGB, int dstPixelValueARGB) {
		// keep highest color value
		return ((dstPixelValueARGB & COLORVARIATION_FILTER_ARGB) > (srcPixelValueARGB & COLORVARIATION_FILTER_ARGB))
					? dstPixelValueARGB : srcPixelValueARGB;
	}

	@Override
	protected void treatValues() throws MinecraftGenerationException {
		if(MineGenerator.getDebugMode().hasDebugInfo()) {
			//debug : save pixel data
		    try {
				File outputfile = new File(MineGenerator.getDebugDir()).toPath().resolve(this.getClass().getName()+".png").toFile();
				ImageIO.write(image, "png", outputfile);
				outputfile = new File(MineGenerator.getDebugDir()).toPath().resolve(this.getClass().getName()+"-bridges.png").toFile();
			    ImageIO.write(bridgesImage, "png", outputfile);
			} catch (IOException e) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_DEBUGIMAGEDUMP_ERROR, e);
			}
		}
        
		//treat values for ground level roads
	    treatValuesFromImage(image, "treatPixelValue");
	    //treat values for bridges
	    treatValuesFromImage(bridgesImage, "treatBridgePixelValue");
	}

	protected void treatValuesFromImage(BufferedImage image, String treatPixelValueMethod) {
		/* get pixel data from image */
        DataBuffer dataBuffer = image.getData().getDataBuffer();
        assert dataBuffer.getClass().equals(DataBufferByte.class);
        byte[] pixelBytes = ((DataBufferByte) dataBuffer).getData();

		assert pixelBytes.length == valueMapSize*valueMapSize*4;
		
		int pixelByteIndex;
		int pixelValueA, pixelValueB, pixelValueG, pixelValueR, pixelValueRGBA;
		for (int y = 0; y < valueMapSize; y++ ) {
			for (int x = 0; x < valueMapSize; x++ ) {
				pixelByteIndex = ( y * valueMapSize + x ) * 4 ;
				pixelValueA = (int) pixelBytes[pixelByteIndex] & 0xff;
				pixelValueB = (int) pixelBytes[pixelByteIndex + 1] & 0xff;
				pixelValueG = (int) pixelBytes[pixelByteIndex + 2] & 0xff;
				pixelValueR = (int) pixelBytes[pixelByteIndex + 3] & 0xff;
				pixelValueRGBA = (pixelValueR << 24) + (pixelValueG << 16) + (pixelValueB << 8) + pixelValueA;
				try {
					getClass().getDeclaredMethod(treatPixelValueMethod, int.class, int.class, int.class)
									.invoke(this, x, y, pixelValueRGBA);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					//unexpected
					assert false;
				}
			}
		}
	}

	@Override
	protected void treatPixelValue(int x, int y, int pixelValueRGBA) {
		if(pixelValueRGBA != 0) {
			//separate road id and color
			int roadId = pixelValueRGBA >>> ID_SHIFT;
			int colorValue = pixelValueRGBA & COLORVARIATION_FILTER;
			assert colorValue != 0;
			byte gradient = (byte) (pixelValueRGBA & ROADGRADIENT_FILTER);
			assert gradient != 0;
			//store road index, gradient value and treat color normally
			roadsIndexes[y*valueMapSize + x] = roadId + 1;//+1 to keep 0 as no road
			roadsGradientValues[y*valueMapSize + x] = gradient;
			super.treatPixelValue(x, y, colorValue);
		}
	}
	
	protected void treatBridgePixelValue(int x, int y, int pixelValueRGBA) {
		if(pixelValueRGBA != 0) {
			//separate road id and color
			int roadId = pixelValueRGBA >>> ID_SHIFT;
			int colorValue = pixelValueRGBA & COLORVARIATION_FILTER;
			assert colorValue != 0;
			byte gradient = (byte) (pixelValueRGBA & ROADGRADIENT_FILTER);
			assert gradient != 0;
			//store road index, gradient value and treat color normally
			bridgesIndexes[y*valueMapSize + x] = roadId + 1;//+1 to keep 0 as no road
			bridgesGradientValues[y*valueMapSize + x] = gradient;
			assert colorsToValues.containsKey(colorValue);
			bridgesValueMap[y * valueMapSize + x] = colorsToValues.get(colorValue);
		}
	}

	
	private int getNextGroundLevelJunctionCoordinate(int startBridgeId) {
		int currentJunctionCoord = 0;
		int currentRoadId, nextRoadId, tmpRoadId;
		ArrayList<Integer> junctions;
		Iterator<Integer> junctionsIterator;
		
		assert(bridgesIds.contains(Math.abs(startBridgeId)));
		currentRoadId = startBridgeId;
		//careful that currentroadId might be negative
		ArrayList<Integer> goneThrough = new ArrayList<Integer>(16);
		while(bridgesIds.contains(Math.abs(currentRoadId))) {
			currentJunctionCoord = roadsJunctions.get(currentRoadId);
			assert(roadsJunctionCoords.containsKey(currentJunctionCoord));
			junctions = roadsJunctionCoords.get(currentJunctionCoord);
			assert(junctions.contains(currentRoadId));
			nextRoadId = currentRoadId;
			goneThrough.add(Math.abs(currentRoadId));
			junctionsIterator = junctions.iterator();
			while(junctionsIterator.hasNext()) {
				tmpRoadId = junctionsIterator.next();
				if((tmpRoadId != currentRoadId) && !goneThrough.contains(Math.abs(tmpRoadId))) {
					nextRoadId = tmpRoadId;
					if(!bridgesIds.contains(Math.abs(nextRoadId))) {
						break;
					}
				}
			}
			if(nextRoadId == currentRoadId) {
				//dead end, keep last current coordinate
				break;
			} else {
				//set nextRoad as current
				//invert id to get the other end of it
				currentRoadId = -nextRoadId;
			}
		}
		//return last explored junction coordinate
		return currentJunctionCoord;
	}
	
	private void buildRoadAltitudesGradientMap(MineMap map) {
		TreeMap<Byte,Short> altiGradientMap;
		int roadIndex;
		/* treat ground level roads for each pixel */
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				if(valueMap[y*valueMapSize + x] != null) {
					assert roadsIndexes[y*valueMapSize + x] != 0;
					assert roadsGradientValues[y*valueMapSize + x] != 0;
					roadIndex = roadsIndexes[y*valueMapSize + x] - 1;
					//create or get map of gradient value to altitude
					if (!altiByGradientByRoad.containsKey(roadIndex)) {
						altiGradientMap = new TreeMap<Byte,Short>();
						altiByGradientByRoad.put(roadIndex, altiGradientMap);
					} else {
						altiGradientMap = altiByGradientByRoad.get(roadIndex);
					}
					//compute altitude of the current gradient value of the specific road, based on ground level
					if (!altiGradientMap.containsKey(roadsGradientValues[y*valueMapSize + x])) {
						altiGradientMap.put(roadsGradientValues[y*valueMapSize + x], map.getGroundLevel(x, y));
					} else {
						altiGradientMap.put(roadsGradientValues[y*valueMapSize + x],
									(short) Math.min(map.getGroundLevel(x, y),
													altiGradientMap.get(roadsGradientValues[y*valueMapSize + x])) );
					}
				}
			}
		}
	}

	private void correctOneRoadAltitudesGradientMap(Set<Entry<Byte, Short>> altiGradientSet, TreeMap<Byte, Short> altiGradientMap) {
		Iterator<Entry<Byte, Short>> iterator = altiGradientSet.iterator();
		Entry<Byte, Short> gradientAlti;

		int nbTotalAltis = altiGradientMap.size();
		int nbWrongValues = 2;//number of values that cannot be trusted at the end of the road

		int curAlti, lastAlti, nbAltis;

		lastAlti = -1;
		nbAltis = 0;
		while (iterator.hasNext()) {
			gradientAlti = iterator.next();
			curAlti = gradientAlti.getValue();

			if ((nbTotalAltis - nbAltis) <= nbWrongValues) {
				//values we need to correct
				if(lastAlti > 0) {
					curAlti = lastAlti;
					assert curAlti <= 0xFFFF;
					//save the corrected value
					assert altiGradientMap.containsKey(gradientAlti.getKey());
					altiGradientMap.put(gradientAlti.getKey(), (short) curAlti);
				} else {
					//not enough values to have a reference
				}
			}

			nbAltis++;
			lastAlti = curAlti;
		}
	}

	private void correctBridgeLimitAltitudesGradientMap() {
		for(Entry<Integer, TreeMap<Byte, Short>> entry : altiByGradientByRoad.entrySet()) {
			//check if road is connected to a bridge
			int junctionCoord;
			int bridgeAttach = 0;//0 = none, 1 = at start, -1 = at end, 2 = on both ends
			junctionCoord = roadsJunctions.get(entry.getKey());
			for(Integer junctionRoadId : roadsJunctionCoords.get(junctionCoord)) {
				if(bridgesIds.contains(Math.abs(junctionRoadId))) {
					bridgeAttach = 1;
					break;
				}
			}
			junctionCoord = roadsJunctions.get(-entry.getKey());
			for(Integer junctionRoadId : roadsJunctionCoords.get(junctionCoord)) {
				if(bridgesIds.contains(Math.abs(junctionRoadId))) {
					bridgeAttach = (bridgeAttach == 1) ? 2 : -1;
					break;
				}
			}
			//TODO? : treat case where road is beetwen two bridges ?
			if((bridgeAttach != 0) && (bridgeAttach != 2)) {
				if(bridgeAttach == 1) {
					//consider good values are at end of road
					//so reverse the set of values
					TreeMap<Byte, Short> reverseMap = new TreeMap<Byte, Short>(Collections.reverseOrder());
					reverseMap.putAll(entry.getValue());
					correctOneRoadAltitudesGradientMap( reverseMap.entrySet(), entry.getValue() );
				} else {
					//good values are at the beginning of the road
					correctOneRoadAltitudesGradientMap( entry.getValue().entrySet(), entry.getValue() );
				}
			}
		}		
	}
	
	private void buildBridgeAltitudesGradientMap(MineMap map) {
		TreeMap<Byte,Short> altiGradientMap;
		int roadIndex;

		int startJunction, endJunction;
		int startX, startY, endX, endY;
		double distanceFromStartSquared, distanceFromEndSquared;
		double startPercent, endPercent;
		int junctionRoadIndex;
		int startLevel, endLevel;
		short bridgeAlti;
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				if(bridgesValueMap[y*valueMapSize + x] != null) {
					assert bridgesIndexes[y*valueMapSize + x] != 0;
					assert bridgesGradientValues[y*valueMapSize + x] != 0;
					roadIndex = bridgesIndexes[y*valueMapSize + x] - 1;
					assert bridgesIds.contains(roadIndex);
					//create or get map of gradient value to altitude
					if (!altiByGradientByRoad.containsKey(roadIndex)) {
						altiGradientMap = new TreeMap<Byte,Short>();
						altiByGradientByRoad.put(roadIndex, altiGradientMap);
					} else {
						altiGradientMap = altiByGradientByRoad.get(roadIndex);
					}
					
					//compute altitude of current bridge by checking start and endpoint
					// where it should join with a ground level road

					//get start and endpoint
					assert bridgesAdjustedJunctions.containsKey(roadIndex);
					assert bridgesAdjustedJunctions.containsKey(-roadIndex);
					startJunction = bridgesAdjustedJunctions.get(roadIndex);
					endJunction = bridgesAdjustedJunctions.get(-roadIndex);
					startX = startJunction & 0xffff;
					startY = startJunction >>> 16;
					endX = endJunction & 0xffff;
					endY = endJunction >>> 16;
					assert startX < valueMapSize;
					assert startY < valueMapSize;
					assert endX < valueMapSize;
					assert endY < valueMapSize;
					//compute % of distance from start and from end
					distanceFromStartSquared = (startX - x)*(startX - x) + (startY - y)*(startY - y);
					distanceFromEndSquared = (endX - x)*(endX - x) + (endY - y)*(endY - y);
					//percent are computed from the other distance (a null distance from start means we 100% on start position)
					startPercent = distanceFromEndSquared / (distanceFromStartSquared + distanceFromEndSquared)+10/100;
					endPercent = distanceFromStartSquared / (distanceFromStartSquared + distanceFromEndSquared+10/100);
					//to get the level, try to get the ground level road that joins with the bridge at start or end
					if (roadsIndexes[startY*valueMapSize + startX] != 0) {
						assert roadsGradientValues[startY*valueMapSize + startX] != 0;
						junctionRoadIndex = roadsIndexes[startY*valueMapSize + startX] - 1;
						startLevel = altiByGradientByRoad.get(junctionRoadIndex).get( roadsGradientValues[startY*valueMapSize + startX] );
					} else {
						startLevel = 0;
					}
					if (roadsIndexes[endY*valueMapSize + endX] != 0) {
						assert roadsGradientValues[endY*valueMapSize + endX] != 0;
						junctionRoadIndex = roadsIndexes[endY*valueMapSize + endX] - 1;
						endLevel = altiByGradientByRoad.get(junctionRoadIndex).get( roadsGradientValues[endY*valueMapSize + endX] );
					} else {
						endLevel = startLevel;
					}
					if (startLevel == 0) {
						if (endLevel != 0) {
							startLevel = endLevel;
						} else {
							//should not happen very often
							// (probably caused by start and end of bridge outside of the visible map)
							// in this case take ground level
							startLevel = map.getGroundLevel(startX, startY);
							endLevel = map.getGroundLevel(endX, endY);
						}
					}
					//compute level mixing start and end levels using these percentages
					bridgeAlti = (short) Math.round(startPercent * startLevel + endPercent * endLevel);
					//store altitude if bigger than what's already there
					if (!altiGradientMap.containsKey(bridgesGradientValues[y*valueMapSize + x])) {
						altiGradientMap.put(bridgesGradientValues[y*valueMapSize + x], bridgeAlti);
					} else {
						altiGradientMap.put(bridgesGradientValues[y*valueMapSize + x],
									(short) Math.max(bridgeAlti,
													altiGradientMap.get(bridgesGradientValues[y*valueMapSize + x])) );
					}
				}
			}
		}
	}
	
	@Override
	protected void applyValueMapToMineMap(MineMap map) {
		assert valueMapSize == map.mapSize;
		LOGGER.log(Level.INFO, "all roads retrieved, number of roads : " + currentRoadIndex + " max is " + MAX_ID);

		int roadIndex;
		
		//browse all bridges junctions to make sure
		// we reach a point that belongs to a ground level road
		// to set a base altitude
		for(int bridgeId : bridgesIds) {
			//start point exploration
			bridgesAdjustedJunctions.put(bridgeId, getNextGroundLevelJunctionCoordinate(bridgeId));
			//end point exploration
			bridgesAdjustedJunctions.put(-bridgeId, getNextGroundLevelJunctionCoordinate(-bridgeId));
		}
		
		//build maps for gradient/altitude equivalence for each road
		buildRoadAltitudesGradientMap(map);
		
		//for each road connected to a bridge, correct abnormal altitude values at start and end of roads
		// to correct when a road "drops" before a bridge
		correctBridgeLimitAltitudesGradientMap();
		
		//build maps for gradient/altitude equivalence for each road on bridge level
		buildBridgeAltitudesGradientMap(map);

		//for each planar coordinate set default surface and altitude
		BlockDefinition currentValue;
		RoadBlockDefinition.OnWhat onWhat;
		Block baseBlock = null;
		byte gradientValue;
		int groundLevel, blockAlti;
		TreeMap<Byte,Short> altiGradientMap;
		boolean overlay;
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				overlay = map.getSurfaceBlock(x, y).canPutOverlayLayer(x, y, valueMapSize);
				if ( ((valueMap[y*valueMapSize + x] != null) || (bridgesValueMap[y*valueMapSize + x] != null))
						&& (overlay || map.getSurfaceBlock(x, y).canBeReplaced("RoadsImporter", x, y, valueMapSize))) {
					onWhat = (bridgesValueMap[y*valueMapSize + x] != null)
								? RoadBlockDefinition.OnWhat.BRIDGE
								: RoadBlockDefinition.OnWhat.GROUND;

					//TODO: combine ground level and bridge info to manage road going under bridges
					currentValue = (onWhat == RoadBlockDefinition.OnWhat.BRIDGE)
										? bridgesValueMap[y*valueMapSize + x]
										: valueMap[y*valueMapSize + x];
					if(onWhat == RoadBlockDefinition.OnWhat.BRIDGE) {
						assert bridgesIndexes[y*valueMapSize + x] != 0;
						roadIndex = bridgesIndexes[y*valueMapSize + x] - 1;
					} else {
						assert roadsIndexes[y*valueMapSize + x] != 0;
						roadIndex = roadsIndexes[y*valueMapSize + x] - 1;
					}
					
					// get base block
					assert ValueBlockDefinition.class.isAssignableFrom( currentValue.getClass() );
					switch ( ((ValueBlockDefinition) currentValue).value ) {
					case VALUE_HIGHWAY :
						baseBlock = RoadBlockDefinition.HIGHWAY_BLOCK;
						break;
					case VALUE_ROAD :
						baseBlock = RoadBlockDefinition.ROAD_BLOCK;
						break;
					case VALUE_STONEROAD :
						baseBlock = RoadBlockDefinition.STONEROAD_BLOCK;
						break;
					case VALUE_DIRTROAD :
						baseBlock = RoadBlockDefinition.DIRTROAD_BLOCK;
						break;
					case VALUE_CYCLELANE :
						baseBlock = RoadBlockDefinition.CYCLELANE_BLOCK;
						break;
					case VALUE_TRAIL :
						baseBlock = RoadBlockDefinition.TRAIL_BLOCK;
						break;
					case VALUE_STAIRS :
						baseBlock = RoadBlockDefinition.STAIRS_BLOCK;
						break;
					default:
						assert false;//unexpected value
					}
					
					//get gradient values for our specific road
					// in order to get computed altitude at the gradient level in the current position
					assert altiByGradientByRoad.containsKey(roadIndex);
					if(onWhat == RoadBlockDefinition.OnWhat.BRIDGE) {
						assert bridgesGradientValues[y*valueMapSize + x] != 0;
						gradientValue = bridgesGradientValues[y*valueMapSize + x];
					} else {
						assert roadsGradientValues[y*valueMapSize + x] != 0;
						gradientValue = roadsGradientValues[y*valueMapSize + x];
					}
					altiGradientMap = altiByGradientByRoad.get(roadIndex);
					assert altiGradientMap.containsKey(gradientValue);

					//are we on a dam ?
					if ( DamBlockDefinition.class.isAssignableFrom( map.getSurfaceBlock(x, y).getClass() ) ) {
						onWhat = RoadBlockDefinition.OnWhat.DAM;
					}

					//adjust altitude to overlay level if needed
					blockAlti = altiGradientMap.get(gradientValue);
					if (overlay) {
						groundLevel = map.getGroundLevel(x, y);
						//shift by difference between current overlaylevel and normal overlay level
						blockAlti += map.getSurfaceBlock(x, y).getOverlayLevel(x, y, groundLevel, valueMapSize)	- (groundLevel + 1);
					}
					//set the block definition
					map.setSurfaceBlock(x, y, new RoadBlockDefinition(baseBlock,
																		overlay ? map.getSurfaceBlock(x, y) : null,
																		onWhat, blockAlti,
																		map.getGroundLevel(x, y),
																		map));
				}
			}
		}
	}
}
