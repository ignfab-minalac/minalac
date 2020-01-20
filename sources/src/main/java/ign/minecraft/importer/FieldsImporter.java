/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.MinecraftGenerationException.Definition;
import ign.minecraft.Utilities;
import ign.minecraft.definition.FieldBlockDefinition;
import ign.minecraft.definition.IdentifiedBlockFactory;

public class FieldsImporter extends RasterLoader implements DataTreatment {
    protected static final Logger LOGGER = Logger.getLogger("FieldsImporter");
    
    private static final String SERVICE_FILE_FORMAT = "image/png";
    
    private static enum Value {
    	//no data, gel, gel industriel, autres gels
    	NONE (new float[] {0,0,1, 0,0,0.94f, 0,0,0.69f, 0,0,0.82f}, null ),
    	//ble, mais, orge, autres cereales, riz, fourrage,
    	CEREAL (new float[] {0.1694f,0.44f,1, 0.3361f,1,0.99f, 0.1833f,0.62f,1, 0.1778f,1,0.92f, 0.6083f,0.44f,1, 0.2278f,0.53f,0.78f},
    			FieldBlockDefinition.FieldType.CEREAL),
    	//colza
    	YELLOWFLOWER (new float[] {0.125f,0.3f,1}, FieldBlockDefinition.FieldType.YELLOWFLOWER ),
    	//tournesol
    	SUNFLOWER (new float[] {0.1667f,1,1}, FieldBlockDefinition.FieldType.SUNFLOWER ),
    	//plantes à fibres, divers
    	PLANTS (new float[] {0.1306f,1,0.74f, 0.8361f,1,0.51f}, FieldBlockDefinition.FieldType.PLANTS ),
    	//semences
    	SEEDS (new float[] {0.1194f,1,0.38f}, FieldBlockDefinition.FieldType.SEEDS ),
    	//autres oleagineux, proteagineux, legumes-fleurs
    	SURFACE_VEGETABLES (new float[] {0.1278f,1,0.99f, 0.125f,1,0.94f, 0.9194f,0.37f,1}, FieldBlockDefinition.FieldType.SURFACE_VEGETABLES ),
    	//legumineuses à grain, autres cultures industrielles
    	GROUND_VEGETABLES (new float[] {0.0444f,0.51f,1, 0.5f,1,0.51f}, FieldBlockDefinition.FieldType.GROUND_VEGETABLES ),
    	//vergers
    	ORCHARD (new float[] {0,1,1}, FieldBlockDefinition.FieldType.ORCHARD ),
    	//vignes
    	VINEYARD (new float[] {0.8278f,1,0.89f}, FieldBlockDefinition.FieldType.VINEYARD ),
    	//fruits à coques
    	NUTS (new float[] {0.3333f,1,0.5f}, FieldBlockDefinition.FieldType.NUTS ),
    	//oliviers
    	OLIVES (new float[] {0.175f,1,0.65f}, FieldBlockDefinition.FieldType.OLIVES ),
    	//canne à sucre
    	REEDS (new float[] {0.6667f,1,1}, FieldBlockDefinition.FieldType.REEDS ),
    	//arboriculture
    	TREES (new float[] {0.4139f,1,0.71f}, FieldBlockDefinition.FieldType.TREES ),
    	//estives landes, prairies permanentes, prairies temporaires
    	MEADOW (new float[] {0.2333f,0.52f,0.90f, 0.2333f,0.62f,1, 0.2333f,0.30f,1 }, FieldBlockDefinition.FieldType.MEADOW )
    	;
    	
    	final float[] hsbValues;
    	final FieldBlockDefinition.FieldType type;
    	
    	Value(float[] hsbValues, FieldBlockDefinition.FieldType type) {
    		this.hsbValues = hsbValues;
    		this.type = type;
    	}
    }

	final private Value[] valueMap;
	
	public FieldsImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);

		String serviceLayer;
		switch (localZone.crsName) {
		case "EPSG:2154"://LAMBERT 93 (FRANCE METRO)
			serviceLayer = Utilities.properties.getProperty("fieldsLayer");
			break;
		case "EPSG:32620"://UTM 20 N (ANTILLES FRANCAISES)
			serviceLayer = Utilities.properties.getProperty("fieldsLayer");
			break;
		case "EPSG:2972"://UTM 22 N (GUYANE)
			serviceLayer = Utilities.properties.getProperty("fieldsLayer");
			break;
		case "EPSG:2975"://UTM 40 S (REUNION)
			serviceLayer = Utilities.properties.getProperty("fieldsLayer");
			break;
		case "EPSG:4471"://UTM 38 S (MAYOTTE)
			serviceLayer = Utilities.properties.getProperty("fieldsLayer");
			break;
		case "EPSG:4467"://UTM 21 N (ST PIERRE ET MIQUELON)
		default:
			throw new MinecraftGenerationException(Definition.SERVICEIMPORT_UNSUPPORTED_ZONE, new UnsupportedOperationException("unsupported CRS : " + localZone.crsName + " for " + this.getClass().getName()) );
		}
		//data connector
		dataConnector = new WMSDataConnector(this, "r", serviceLayer, SERVICE_FILE_FORMAT);
		
		valueMap = new FieldsImporter.Value[valueMapSize * valueMapSize];
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
		
		byte[] pixelBytes = null;
		//convert byte data into image to access pixel data
        Iterator<?> readers = ImageIO.getImageReadersByMIMEType(SERVICE_FILE_FORMAT);
        ImageReader reader = (ImageReader) readers.next();
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(dataAsByteBuffer.array());
			ImageInputStream iis = ImageIO.createImageInputStream(bis);
	        reader.setInput(iis, true);
	        BufferedImage image = reader.read(0);
	        Utilities.disposeImageReader(reader);
	        
	        if(MineGenerator.MINECRAFTMAP_ANGLE != 0) {
	        	AffineTransform at = new AffineTransform();
	        	at.rotate(MineGenerator.MINECRAFTMAP_ANGLE * (Math.PI / 180),image.getWidth()/2,image.getHeight()/2);
	        	BufferedImage newImage = new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
	        	AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
	        	op.filter(image, newImage);
	        	int width = image.getWidth();
	        	int height = image.getHeight();
	        	newImage = newImage.getSubimage((width - currentValuesToReadSizeX) / 2,(height - currentValuesToReadSizeY) / 2,currentValuesToReadSizeX, currentValuesToReadSizeY); 
	        	image = new BufferedImage(currentValuesToReadSizeX, currentValuesToReadSizeY, newImage.getType());
	        	Graphics g = image.createGraphics();
	        	g.drawImage(newImage, 0, 0, null);
	        }

	        DataBuffer dataBuffer = image.getData().getDataBuffer();
	        assert dataBuffer.getClass().equals(DataBufferByte.class);
	        pixelBytes = ((DataBufferByte) dataBuffer).getData();

			assert image.getType() == BufferedImage.TYPE_4BYTE_ABGR;
		} catch (IOException e) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
		}
		
		if(pixelBytes.length != currentValuesToReadSizeX*currentValuesToReadSizeY*4)
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_WMSUNEXPECTEDSIZE_ERROR);
        
		int pixelByteIndex, valueIndex, curHsbIndex;
		int pixelValueA, pixelValueB, pixelValueG, pixelValueR;
		final float[] pixelHSB = new float[3];
		//let's be careful that y is in reverted order to choose the start of our tile
		for (int y = valueMapSize - currentValueY - currentValuesToReadSizeY; y < valueMapSize - currentValueY; y++ ) {
			for (int x = currentValueX; x < currentValueX + currentValuesToReadSizeX; x++ ) {
				pixelByteIndex = (y - valueMapSize + currentValueY + currentValuesToReadSizeY)*currentValuesToReadSizeX + (x-currentValueX);
				pixelValueA = (int) pixelBytes[pixelByteIndex * 4] & 0xff;
				pixelValueB = (int) pixelBytes[pixelByteIndex * 4 + 1] & 0xff;
				pixelValueG = (int) pixelBytes[pixelByteIndex * 4 + 2] & 0xff;
				pixelValueR = (int) pixelBytes[pixelByteIndex * 4 + 3] & 0xff;
				Color.RGBtoHSB(pixelValueR,pixelValueG,pixelValueB,pixelHSB);
				valueIndex = y * valueMapSize + x;
				float tolerance = 0.02f;
				boolean found = false;
				if (pixelValueA == 0xff) {
					for (Value curTestedValue : Value.values()) {
						curHsbIndex = 0;
						while (curHsbIndex < curTestedValue.hsbValues.length) {
							if ( (Math.abs(curTestedValue.hsbValues[curHsbIndex] - pixelHSB[curHsbIndex % 3]) <= tolerance)
									&& (Math.abs(curTestedValue.hsbValues[curHsbIndex + 1] - pixelHSB[curHsbIndex % 3 + 1]) <= tolerance)
									&& (Math.abs(curTestedValue.hsbValues[curHsbIndex + 2] - pixelHSB[curHsbIndex % 3 + 2]) <= tolerance) ) {
								//found the color
								valueMap[valueIndex] = curTestedValue;
								found = true;
								break;
							}
							curHsbIndex += 3;
						}
						if (found) {
							break;
						}
					}
				}
				if (!found) {
					valueMap[valueIndex] = Value.NONE;
				}
			}
		}
		
	}
	
	@Override
	protected void applyValueMapToMineMap(MineMap map) throws MinecraftGenerationException {
		if (MineGenerator.getDebugMode().hasDebugInfo()) {
			//for debug purposes : copy values as image //////////
			byte[] pixelBytes = new byte[valueMapSize*valueMapSize*3];
			for(int x=0; x<valueMapSize; x++) {
				for(int y=0; y<valueMapSize; y++) {
					int rgbColor = Color.HSBtoRGB(valueMap[x + y * valueMapSize].hsbValues[0], valueMap[x + y * valueMapSize].hsbValues[1], valueMap[x + y * valueMapSize].hsbValues[2]);
					pixelBytes[ (y * valueMapSize + x)*3 ] = (byte) ((rgbColor >> 16) & 0xff);
					pixelBytes[ (y * valueMapSize + x)*3 + 1 ] = (byte) ((rgbColor >> 8) & 0xff);
					pixelBytes[ (y * valueMapSize + x)*3 + 2 ] = (byte) (rgbColor & 0xff);
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

		//first we determine ids of different fields
		int zoneId = 0;
		int zoneIdCounter = 1;//keep value 0 for indication of no zone
		int[] zoneIds = new int[valueMapSize*valueMapSize];
		int sameZoneId;
		HashMap<Integer,Integer> correspondingZones = new HashMap<Integer,Integer>();
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				if ((valueMap[y*valueMapSize + x] != null) && (valueMap[x + y * valueMapSize] != Value.NONE)
						&& map.getSurfaceBlock(x, y).canBeReplaced("FieldsImporter", x, y, valueMapSize)) {
					//to determine the id, we check if there are blocks of similar type around the current block
					// if yes, we use the same id, otherwise we create a new one
					// as we are creating the block definition right now,
					// no need to check in the upcoming coordinates (x+1 or y+1)
					// but we have to check for diagonal blocks previously set (x-1,y-1 and x-1,y+1)
					
					//as we will miss jonctions that are at a later Y value, when we copy from x,y-1,
					// we might set a correspondance between our id and the one at x-1,y
					if ((y > 0) && (valueMap[x + (y-1) * valueMapSize] != null)
							&& (valueMap[x + (y-1) * valueMapSize] != Value.NONE)
							&& (valueMap[x + (y-1) * valueMapSize].type == valueMap[x + y * valueMapSize].type)) {
						//copy id of previous block with same x
						assert zoneIds[x + (y-1) * valueMapSize] != 0;
						zoneId = zoneIds[x + (y-1) * valueMapSize];
						
						//if the block at x-1 is similar, we consider it is the same zone
						if ((x > 0) && (valueMap[(x-1) + y * valueMapSize] != null) && (valueMap[(x-1) + y * valueMapSize] != Value.NONE)) {
							sameZoneId = zoneIds[(x-1) + y * valueMapSize];
							assert sameZoneId != 0;
							if (valueMap[(x-1) + y * valueMapSize].type == valueMap[x + y * valueMapSize].type) {
								int currentCorrespondance = correspondingZones.containsKey(zoneId) ? correspondingZones.get(zoneId) : 0;
								while(correspondingZones.containsKey(sameZoneId)) {
									if ((sameZoneId == currentCorrespondance) || (sameZoneId == zoneId) ) {
										//we won't do nothing if the current zone or current correspondance
										// is matching one of the browsed ids
										break;
									}
									sameZoneId = correspondingZones.get(sameZoneId);
								}
								if (sameZoneId != zoneId) {
									if (currentCorrespondance == 0) {
										//we store the fact that our zone is the same as sameZoneId
										correspondingZones.put(zoneId, sameZoneId);
									} else if (currentCorrespondance != sameZoneId) {
										//we have already found an equivalent zone that is not related to this one
										// so we store the equivalence between the two
										correspondingZones.put(currentCorrespondance, sameZoneId);
									}
								}
							}
						}
					} else if ((x > 0) && (valueMap[(x-1) + y * valueMapSize] != null)
							&& (valueMap[(x-1) + y * valueMapSize] != Value.NONE)
							&& (valueMap[(x-1) + y * valueMapSize].type == valueMap[x + y * valueMapSize].type)) {
						//copy id of previous block with same y
						assert zoneIds[(x-1) + y * valueMapSize] != 0;
						zoneId = zoneIds[(x-1) + y * valueMapSize];
					} else if ((x > 0) && (y > 0) && (valueMap[(x-1) + (y-1) * valueMapSize] != null)
							&& (valueMap[(x-1) + (y-1) * valueMapSize] != Value.NONE)
							&& (valueMap[(x-1) + (y-1) * valueMapSize].type == valueMap[x + y * valueMapSize].type)) {
						assert zoneIds[(x-1) + (y-1) * valueMapSize] != 0;
						zoneId = zoneIds[(x-1) + (y-1) * valueMapSize];
					} else if ((x > 0) && (y < valueMapSize-1) && (valueMap[(x-1) + (y+1) * valueMapSize] != null)
							&& (valueMap[(x-1) + (y+1) * valueMapSize] != Value.NONE)
							&& (valueMap[(x-1) + (y+1) * valueMapSize].type == valueMap[x + y * valueMapSize].type)) {
						assert zoneIds[(x-1) + (y+1) * valueMapSize] != 0;
						zoneId = zoneIds[(x-1) + (y+1) * valueMapSize];
					} else {
						//no similar field nearby, use new id
						zoneId = zoneIdCounter;
						zoneIdCounter++;
					}
					zoneIds[x + y * valueMapSize] = zoneId;
				}
			}
		}
		
		//now really set the zone block definitions with determined IDs
		FieldBlockDefinition field;
		for(int x=0; x<valueMapSize; x++) {
			for(int y=0; y<valueMapSize; y++) {
				/* !! y here is z in the minecraft world !! */
				if ((valueMap[x + y * valueMapSize] != null) && (valueMap[x + y * valueMapSize] != Value.NONE)
						 && map.getSurfaceBlock(x, y).canBeReplaced("FieldsImporter",x, y, valueMapSize)) {
					assert zoneIds[x + y * valueMapSize] != 0;
					zoneId = zoneIds[x + y * valueMapSize];
					while(correspondingZones.containsKey(zoneId)) {
						zoneId = correspondingZones.get(zoneId);
					}
					field = (FieldBlockDefinition) IdentifiedBlockFactory.getBlockDefinition("FieldBlockDefinition", 
														zoneId,
														new Object[] { valueMap[x + y * valueMapSize].type });
					map.setSurfaceBlock(x, y, field);
				}
			}
		}


	}

}
