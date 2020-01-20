/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.MinecraftGenerationException.Definition;
import ign.minecraft.Utilities;
import ign.minecraft.definition.BlockDefinition;
import ign.minecraft.definition.HypsometricBlockDefinition;
import ign.minecraft.definition.HypsometricBlocks;
import ign.minecraft.definition.SeaBlockDefinition;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class AltiImporter extends RasterLoader implements DataTreatment {
    protected static final Logger LOGGER = Logger.getLogger("AltiImporter");

    private static final int SEA_ALTI_VALUE = -20;/* we consider anything below this altitude is the sea */
    public static final int BLOCK_ALTITUDE_ABSOLUTEZERO = 40; /* altitude level zero in minecraft map reference (0-255) */
    //public static final int BLOCK_ALTITUDE_MAX = 150; /* maximum altitude in blocks relative to level zero */
    public static int BLOCK_ALTITUDE_MAX = 150; /* maximum altitude in blocks relative to level zero */
    public static double MAX_ALTI = 0;
    public static double MIN_ALTI = 0;
    public static boolean HYPSOMETRIC = false;
    
	private float[] valueMap;
	
	public AltiImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		String serviceLayer;
		if(MineGenerator.MINECRAFTMAP_RATIO < 1 && MineGenerator.MINECRAFTMAP_RATIO > 0.01)
			serviceLayer = Utilities.properties.getProperty("altiLayerRELIEF");
		else {
			switch (localZone.crsName) {
			case "EPSG:2154"://LAMBERT 93 (FRANCE METRO)
				serviceLayer = Utilities.properties.getProperty("altiLayerFRANCEMETRO");
				break;
			case "EPSG:4471"://UTM 38 S (MAYOTTE)
				serviceLayer = Utilities.properties.getProperty("altiLayerMAYOTTE");
				break;
			case "EPSG:32620"://UTM 20 N (ANTILLES FRANCAISES)
				if(localZone.equals(Zone.MARTINIQUE)) {
					serviceLayer = Utilities.properties.getProperty("altiLayerMARTINIQUE");
				} else if(localZone.equals(Zone.GUADELOUPE)) {
					serviceLayer = Utilities.properties.getProperty("altiLayerGUADELOUPE");
				} else {
					throw new MinecraftGenerationException(Definition.SERVICEIMPORT_UNSUPPORTED_ZONE, new UnsupportedOperationException("CRS found : " + localZone.crsName + " for " + this.getClass().getName() + " but unknown zone"));
				}
				break;
			case "EPSG:2975"://UTM 40 S (REUNION)
				serviceLayer = Utilities.properties.getProperty("altiLayerREUNION");
				break;
			case "EPSG:4467"://UTM 21 N (ST PIERRE ET MIQUELON)
				serviceLayer = Utilities.properties.getProperty("altiLayerSPM");
				break;
			case "EPSG:2972"://UTM 22 N (GUYANE)
				serviceLayer = Utilities.properties.getProperty("altiLayerGUYANE");
				break;
			case "EPSG:3857"://WORLDWIDE SRTM (RATIO < CERTAIN AMOUNT)
				serviceLayer = Utilities.properties.getProperty("altiLayerSRTM");
				break;
			default:
				throw new MinecraftGenerationException(Definition.SERVICEIMPORT_UNSUPPORTED_ZONE, new UnsupportedOperationException("unsupported CRS : " + localZone.crsName + " for " + this.getClass().getName()) );
			}
		}
		//data connector
		dataConnector = new WMSDataConnector(this, "r", serviceLayer, "image/x-bil;bits=32");

		valueMap = new float[valueMapSize * valueMapSize];
	}

	@Override
	public boolean isToBeReused() {
		return true;
	}
	
	@Override
	public void initTileTreatment() throws MinecraftGenerationException {
		//nothing to do
	}

	@Override
	public boolean treatFeature(Object feature) throws MinecraftGenerationException {
		assert false;//never called by WMS data connector
		return true;
	}
	
	@Override
	public void treatFeatureData(Object data) throws MinecraftGenerationException {
		assert ByteBuffer.class.isAssignableFrom(data.getClass());
		ByteBuffer dataAsByteBuffer = (ByteBuffer) data;
		FloatBuffer dataAsFloatBuffer = ((ByteBuffer) data).asFloatBuffer();

		// readComplete might be different of currentValuesToReadsize depending on the
		// rotation (some might require to get a bigger sized image in order to avoid
		// rotation cuts)
		int readComplete = (int) Math.sqrt(dataAsByteBuffer.remaining() / 4);

		float[] floatArray;
		if (dataAsFloatBuffer.hasArray()) {
			floatArray = dataAsFloatBuffer.array();
		} else {
			floatArray = new float[dataAsFloatBuffer.capacity()];
			dataAsFloatBuffer.get(floatArray);
		}

		FloatProcessor s = new FloatProcessor(readComplete, readComplete, floatArray);

		if (MineGenerator.MINECRAFTMAP_ANGLE != 0) {
			s.setInterpolationMethod(ImageProcessor.BILINEAR);
			s.rotate(MineGenerator.MINECRAFTMAP_ANGLE);
			s.setRoi((readComplete - currentValuesToReadSizeX) / 2, (readComplete - currentValuesToReadSizeY) / 2,
					readComplete - (readComplete - currentValuesToReadSizeX) / 2,
					readComplete - (readComplete - currentValuesToReadSizeY) / 2);
			s = s.crop().convertToFloatProcessor();
		}

		if(MineGenerator.MINECRAFTMAP_RATIO < 1) {
			s.min(0); // force < 0 values into 0 for relief mode
		}

		int convolveSize = 1;
		if(MineGenerator.MINECRAFTMAP_RATIO <= 1.2)
			convolveSize = 3;
		else if(MineGenerator.MINECRAFTMAP_RATIO <= 1.5)
			convolveSize = 5;
		else if(MineGenerator.MINECRAFTMAP_RATIO <= 1.8)
			convolveSize = 7;
		else
			convolveSize = 9;

		float[] kernel = new float[convolveSize*convolveSize];
		for(int i=0;i<convolveSize*convolveSize;i++)
			kernel[i] = 1;
		s.convolve(kernel, convolveSize, convolveSize);

		if (MineGenerator.MINECRAFTMAP_RATIO != 1) s.multiply(MineGenerator.MINECRAFTMAP_RATIO);
		if (MineGenerator.MINECRAFTMAP_ALTIRATIO > 1) s.multiply(MineGenerator.MINECRAFTMAP_ALTIRATIO);

		float[] floatArrayEnlarged = new float[currentValuesToReadSizeX * currentValuesToReadSizeY];
		for (int i = 0; i < currentValuesToReadSizeX; i++) {
			for (int j = 0; j < currentValuesToReadSizeY; j++) {
				floatArrayEnlarged[(i * currentValuesToReadSizeX + j)] = s.getf(j, i);
			}
		}

		dataAsFloatBuffer = FloatBuffer.wrap(floatArrayEnlarged);

		if(dataAsByteBuffer.remaining() < currentValuesToReadSizeX*currentValuesToReadSizeY*4)
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_WMSUNEXPECTEDSIZE_ERROR);

		//convert byte data into float array
		FloatBuffer valueBuffer = dataAsFloatBuffer;
		//let's be careful that y axis is inverted when we choose the start of our tile
		for (int y = valueMapSize - currentValueY - currentValuesToReadSizeY; y < valueMapSize - currentValueY; y++ ) {
			assert valueBuffer.remaining() == (currentValuesToReadSizeY - (y - valueMapSize + currentValueY + currentValuesToReadSizeY)) * currentValuesToReadSizeX;
			assert (y * valueMapSize + currentValueX) < valueMapSize * valueMapSize;

			valueBuffer.get(valueMap, y * valueMapSize + currentValueX, currentValuesToReadSizeX);
		}
	}

	private double minRealAlti;//min real altitude value on the whole region
	private double centerRealAlti;//real altitude value at spawn point, around where we want a transformation ratio close to 1:1
	private double maxRealAlti;//max real altitude value on the whole region
	private int centerMapAlti;//minecraft altitude between 0 and BLOCK_ALTITUDE_MAX, adjusted by the position of centerRealAlti between min and max values
	private double valueRatioDown;//the ratio between minecraft altitude and real altitude for the part below centerRealAlti
	private double valueRatioUp;//the ratio between minecraft altitude and real altitude for the part over centerRealAlti
	static private double halfpi = (double) Math.PI / 2;
	private int nbSinusDown;//number of sinus functions used to approximate a 1:1 ratio near centerRealAlti, in the part below its value
	private double xTemperDown;//temper factor to refine the approximation
	private int nbSinusUp;//number of sinus functions used to approximate a 1:1 ratio near centerRealAlti, in the part over its value
	private double xTemperUp;//temper factor to refine the approximation
	private void computeValuesForAltitudeTransform() {
		//Below is described the way of calculating the parameters to get close to a 1:1 ratio on low values
		// based on values from minRealAlti to maxRealAlti converted in values from 0 to BLOCK_ALTITUDE_MAX
		//To have this effect applied on the values near centerAlti,
		// we compute separately values above and below, and for each have a function.
		//They will treat values from minRealAlti to centerRealAlti or from centerRealAlti to maxRealAlti
		// and convert them in values between 0 and centerMapAlti or centerMapAlti to BLOCK_ALTITUDE_MAX
		// but the principle is exactly the same
		
		//We compute values to end up with a curve that will be close to a 1:1 ratio for values near 0
		//and will end at BLOCK_ALTITUDE_MAX for an input value of maxValue
		//We base or transformation on sinus function, if all intervals are brought to [0,1]
		// sinus will reach 1 at pi/2, so the real base func is sin(halfpi*x)
		//Using the real intervals, function becomes BLOCK_ALTITUDE_MAX * sin(halfpi*(x-minValue)/(maxValue-minValue))
		//let's simplify and consider x = x-minValue and xRange = maxValue-minValue
		//so the func becomes BLOCK_ALTITUDE_MAX * sin(halfpi*x/xRange)
		//Our goal is to have a tangent at 0 that is y = x
		//tangent at 0 of our function is y = BLOCK_ALTITUDE_MAX * halfpi / xRange * x = valueRatio * halfpi * x
		//if valueRatio is smaller than 1/halfpi, we can make the tangent steeper by replacing our x/xRange by another sinus function
		//the resulting func would be BLOCK_ALTITUDE_MAX * sin(halfpi* sin(halfpi*x/xRange) )
		//and the tangent becomes y = valueRatio * halfpi * halfpi * x
		//and so on until we compensate the ratio
		//if then we are two far ahead and the tangent is too steep, we can soften it by mixing the final sinus func with some direct x
		//that would end in BLOCK_ALTITUDE_MAX * sin(halfpi* ( sin(halfpi*x/xRange) + xTemper*x ) / (xTemper + 1) )
		// tangent will then would be y = valueRatio * halfpi * (halfpi+xTemper)/(xTemper+1) * x
		//we can now approximate the best tangent !
		nbSinusDown = 1;
		xTemperDown = 0;
		nbSinusUp = 1;
		xTemperUp = 0;
		double halfpiPoweredDown = halfpi;
		double halfpiPoweredUp = halfpi;

		while ((1/valueRatioDown) > halfpiPoweredDown) {
			halfpiPoweredDown = halfpiPoweredDown * halfpi;
			nbSinusDown ++;
		}
		while ((1/valueRatioUp) > halfpiPoweredUp) {
			halfpiPoweredUp = halfpiPoweredUp * halfpi;
			nbSinusUp ++;
		}
		
		//ratio * halfpi^(nbSinus-1) * (halfpi + xTemper)/(xTemper + 1) = 1
		//ratio * halfpi^(nbSinus-1) = (xTemper + 1)/(halfpi + xTemper)
		//ratio * halfpi^nbSinus + ratio * halfpi^(nbSinus-1) * xTemper = xTemper + 1
		//xTemper * (1 - ratio * halfpi^(nbSinus-1)) = ratio * halfpi^nbSinus - 1
		//xTemper = (ratio * halfpi^nbSinus - 1) / (1 - ratio * halfpi^(nbSinus-1))
		
		//don't bother if we are very close to the ratio with nbSinus-1
		//because that might lead to a gigantic xTemper number
		if((1 - valueRatioDown * halfpiPoweredDown/halfpi) < 0.001) {
			nbSinusDown--;
			xTemperDown = 0;
		} else {
			xTemperDown = (valueRatioDown * halfpiPoweredDown - 1) / (1 - valueRatioDown * halfpiPoweredDown/halfpi);
		}
		if((1 - valueRatioUp * halfpiPoweredUp/halfpi) < 0.001) {
			nbSinusUp--;
			xTemperUp = 0;
		} else {
			xTemperUp = (valueRatioUp * halfpiPoweredUp - 1) / (1 - valueRatioUp * halfpiPoweredUp/halfpi);
		}
		
		LOGGER.log(Level.INFO, "parametres de transformation d'altitude basse : nbSinus=" + nbSinusDown + " xTemper=" + xTemperDown);
		LOGGER.log(Level.INFO, "parametres de transformation d'altitude haute : nbSinus=" + nbSinusUp + " xTemper=" + xTemperUp);
	}
	private int nonLinearAltitude(double curRealAlti) {
		double mapRange, x, nbSinus, xTemper;
		if (curRealAlti < centerRealAlti) {
			//here we have a negative x, as low values will be the ones near centerAlti 
			x = (curRealAlti - centerRealAlti) / (centerRealAlti - minRealAlti);
			mapRange = centerMapAlti;
			nbSinus = nbSinusDown;
			xTemper = xTemperDown;
		} else {
			x = (curRealAlti - centerRealAlti) / (maxRealAlti - centerRealAlti);
			mapRange = BLOCK_ALTITUDE_MAX - centerMapAlti;
			nbSinus = nbSinusUp;
			xTemper = xTemperUp;
		}
		double baseFuncResult = (Math.sin(halfpi * x) + xTemper * x) / (xTemper + 1);
		for(int i = 1; i < nbSinus; i++) {
			baseFuncResult = Math.sin(halfpi*baseFuncResult);
		}
		return (int) Math.round( mapRange * baseFuncResult );
	}

	public int transformAltitude(double curRealAlti) {
		double valueRatio;
		valueRatio = (curRealAlti < centerRealAlti) ? valueRatioDown : valueRatioUp;
		if (valueRatio < 1) {
			return BLOCK_ALTITUDE_ABSOLUTEZERO + centerMapAlti + nonLinearAltitude(curRealAlti);
		} else {
			if (curRealAlti < centerRealAlti) {
				return BLOCK_ALTITUDE_ABSOLUTEZERO + (int) Math.round(curRealAlti - minRealAlti);
			} else {
				return BLOCK_ALTITUDE_ABSOLUTEZERO + centerMapAlti + (int) Math.round(curRealAlti - centerRealAlti);
				
			}
		}
	}

	@Override
	protected void applyValueMapToMineMap(MineMap map) throws MinecraftGenerationException {
		int valueMapLength = valueMapSize*valueMapSize;
		//get min and max values and compute value ratio
		//to turn values into block indexes
		float minValue=10000;
		float maxValue=0;
		for(int i=0; i<valueMapLength; i++) {
			//don't take in account any negative value
			if(valueMap[i] > 0) {
				//determinate min and max
				minValue = Math.min(minValue, valueMap[i]);
				maxValue = Math.max(maxValue, valueMap[i]);
			} else {
				minValue = 0;
			}
		}
		
		minRealAlti = (double) minValue;
		maxRealAlti = (double) maxValue;
		centerRealAlti = getRealAlti(valueMapSize/2, valueMapSize/2);
		centerMapAlti = BLOCK_ALTITUDE_MAX/2;
		//if centerRealAlti is close to maxRealalti, then we bring up centerMapAlti to optimize the render of lower altitudes
		centerMapAlti = Math.max(centerMapAlti, (int) Math.floor(BLOCK_ALTITUDE_MAX - (maxRealAlti - centerRealAlti)));
		//center map alti cannot be over the equivalent of lower altitudes at 1:1 ratio
		centerMapAlti = Math.min(centerMapAlti, (int) Math.ceil(centerRealAlti - minRealAlti));
		//compute ratio under and over center alti
		valueRatioDown = (centerRealAlti == minRealAlti) ? 1 : Math.min(1, centerMapAlti/(centerRealAlti-minRealAlti));
		valueRatioUp = (centerRealAlti == maxRealAlti) ? 1 : Math.min(1, (BLOCK_ALTITUDE_MAX-centerMapAlti)/(maxRealAlti-centerRealAlti));
		LOGGER.log(Level.INFO, "ratio altitude minecraft / valeur reelle : " + valueRatioDown + "(en dessous du spawn) " + valueRatioUp + "(au dessus du spawn) ");
		computeValuesForAltitudeTransform();
		AltiImporter.MAX_ALTI = (maxRealAlti <= SEA_ALTI_VALUE) ? 0 : transformAltitude(maxRealAlti);
		AltiImporter.MIN_ALTI = (minRealAlti <= SEA_ALTI_VALUE) ? 0 : transformAltitude(minRealAlti);
		
		if (MineGenerator.getDebugMode().hasDebugInfo()) {
			//for debug purposes : copy values as image //////////
			int curValue;
			byte[] pixelBytes = new byte[valueMapSize*valueMapSize*3];
			for(int x=0; x<valueMapSize; x++) {
				for(int y=0; y<valueMapSize; y++) {
					curValue = (valueMap[ y * valueMapSize + x ] <= SEA_ALTI_VALUE) ? 0 : transformAltitude(valueMap[ y * valueMapSize + x ]);
					pixelBytes[ (y * valueMapSize + x)*3 ] = (byte) Math.max(0, Math.min( 255, curValue) );
					pixelBytes[ (y * valueMapSize + x)*3 + 1 ] = pixelBytes[ (y * valueMapSize + x)*3 + 2 ] = pixelBytes[ (y * valueMapSize + x)*3 ];
				}
			}
			BufferedImage image = new BufferedImage(valueMapSize, valueMapSize, BufferedImage.TYPE_3BYTE_BGR);
		    byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		    System.arraycopy(pixelBytes, 0, imgData, 0, pixelBytes.length);
			File outputfile = new File(MineGenerator.getDebugDir()).toPath().resolve(this.getClass().getName()+".png").toFile();
		    try {
				ImageIO.write(image, "png", outputfile);
			} catch (IOException e) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_DEBUGIMAGEDUMP_ERROR, e);
			}
		    //////////////////////////////////////////////////////
		}
		
		assert valueMapSize == map.mapSize;
		//for each planar coordinate set default surface and altitude
		BlockDefinition deepSea = new SeaBlockDefinition();

		/* hypsometric shades */
		double numberOfSlices = 8;//16; // 16 color palette in minecraft
		double minAndMaxAltiDifference = AltiImporter.MAX_ALTI - AltiImporter.MIN_ALTI;
		double eachSliceSize = minAndMaxAltiDifference / (numberOfSlices - 1);
		int transformedGroundLevel, hypsometricShade;
		/* hypsometric shades */

		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				if(valueMap[ y * valueMapSize + x ] <= SEA_ALTI_VALUE) {
					//sea blocks
					map.setSurfaceBlock(x, y, deepSea);
					map.setGroundLevel(x, y, BLOCK_ALTITUDE_ABSOLUTEZERO);
				} else {
					transformedGroundLevel = transformAltitude(valueMap[ y * valueMapSize + x ]);
					if(HYPSOMETRIC) {
						if(valueMap[ y * valueMapSize + x ] == 0) {
							map.setSurfaceBlock(x, y, new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.BLUE)));
						} else {
							hypsometricShade = (int)(Math.round((transformedGroundLevel-MIN_ALTI) / eachSliceSize));
							map.setSurfaceBlock(x, y, HypsometricBlocks.values()[hypsometricShade].get());
						}
					} else if(MineGenerator.MINECRAFTMAP_RATIO < 1) {
						if(valueMap[ y * valueMapSize + x ] == 0)
							map.setSurfaceBlock(x, y, new HypsometricBlockDefinition(new Block(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.BLUE)));
						else
							map.setSurfaceBlock(x, y, new HypsometricBlockDefinition(new Block(BlockType.STONE)));
					}
					map.setGroundLevel(x, y, transformedGroundLevel );
					//map.setGroundLevel(x, y, (int)valueMap[ y * valueMapSize + x ]);
				}
			}
		}
	}
	
	public double getRealAlti(int x, int y) {
		return (double) Math.max(0, valueMap[y * valueMapSize + x]);//avoid negative values
	}

}
