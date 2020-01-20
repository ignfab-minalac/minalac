/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import ign.minecraft.Utilities;
import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.MinecraftGenerationException.Definition;
import ign.minecraft.definition.PhotoTreatedBlockDefinition;
import ign.minecraft.definition.PhotoTreatedSimpleBlockDefinition;

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
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class PhotoImporter extends RasterLoader implements DataTreatment {
    protected static final Logger LOGGER = Logger.getLogger("PhotoImporter");
    
    private static final String SERVICE_FILE_FORMAT = "image/png";

    protected static void rgb2lab(int R, int G, int B, int[] lab) {
        //comes from http://www.brucelindbloom.com

        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f/24389.f;
        float k = 24389.f/27.f;

        float Xr = 0.964221f;  // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = R/255.f; //R 0..1
        g = G/255.f; //G 0..1
        b = B/255.f; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r/12;
        else
            r = (float) Math.pow((r+0.055)/1.055,2.4);

        if (g <= 0.04045)
            g = g/12;
        else
            g = (float) Math.pow((g+0.055)/1.055,2.4);

        if (b <= 0.04045)
            b = b/12;
        else
            b = (float) Math.pow((b+0.055)/1.055,2.4);


        X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
        Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
        Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;

        // XYZ to Lab
        xr = X/Xr;
        yr = Y/Yr;
        zr = Z/Zr;

        if ( xr > eps )
            fx =  (float) Math.pow(xr, 1/3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if ( yr > eps )
            fy =  (float) Math.pow(yr, 1/3.);
        else
        fy = (float) ((k * yr + 16.) / 116.);

        if ( zr > eps )
            fz =  (float) Math.pow(zr, 1/3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = ( 116 * fy ) - 16;
        as = 500*(fx-fy);
        bs = 200*(fy-fz);

        lab[0] = (int) (Ls + .5);
        lab[1] = (int) (as + .5); 
        lab[2] = (int) (bs + .5);       
    }
    
	final private PhotoTreatedBlockDefinition.PhotoColor[] valueMap;
	
	public PhotoImporter(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);

		String serviceLayer;
		switch (localZone.crsName) {
		case "EPSG:2154"://LAMBERT 93 (FRANCE METRO)
			serviceLayer = Utilities.properties.getProperty("photoLayerFRANCEMETRO");
			break;
		case "EPSG:32620"://UTM 20 N (ANTILLES FRANCAISES)
			serviceLayer = Utilities.properties.getProperty("photoLayerANTILLES");
			break;
		case "EPSG:2972"://UTM 22 N (GUYANE)
			serviceLayer = Utilities.properties.getProperty("photoLayerGUYANE");
			break;
		case "EPSG:2975"://UTM 40 S (REUNION)
			serviceLayer = Utilities.properties.getProperty("photoLayerREUNION");
			break;
		case "EPSG:4467"://UTM 21 N (ST PIERRE ET MIQUELON)
			serviceLayer = Utilities.properties.getProperty("photoLayerSPM");
			break;
		case "EPSG:4471"://UTM 38 S (MAYOTTE)
			serviceLayer = Utilities.properties.getProperty("photoLayerMAYOTTE");
			break;
		default:
			throw new MinecraftGenerationException(Definition.SERVICEIMPORT_UNSUPPORTED_ZONE, new UnsupportedOperationException("unsupported CRS : " + localZone.crsName + " for " + this.getClass().getName()) );
		}
		//data connector
		dataConnector = new WMSDataConnector(this, "r", serviceLayer, SERVICE_FILE_FORMAT);
		
		valueMap = new PhotoTreatedBlockDefinition.PhotoColor[valueMapSize * valueMapSize];
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

			assert image.getAlphaRaster() == null;
		} catch (IOException e) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
		}
		
		if(pixelBytes.length != currentValuesToReadSizeX*currentValuesToReadSizeY*3)
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_WMSUNEXPECTEDSIZE_ERROR);

		int pixelByteIndex, valueIndex;
		int pixelValueB, pixelValueG, pixelValueR;
		final int[] pixelLab = new int[3];
		double saturation;
		//let's be careful that y is in reverted order to choose the start of our tile
		for (int y = valueMapSize - currentValueY - currentValuesToReadSizeY; y < valueMapSize - currentValueY; y++ ) {
			for (int x = currentValueX; x < currentValueX + currentValuesToReadSizeX; x++ ) {
				pixelByteIndex = (y - valueMapSize + currentValueY + currentValuesToReadSizeY)*currentValuesToReadSizeX + (x-currentValueX);
				pixelValueB = (int) pixelBytes[pixelByteIndex * 3] & 0xff;
				pixelValueG = (int) pixelBytes[pixelByteIndex * 3 + 1] & 0xff;
				pixelValueR = (int) pixelBytes[pixelByteIndex * 3 + 2] & 0xff;
				//we convert the colors to Lab which separates color from luminance
				// and is much better in terms of human color perception than HSV
				rgb2lab(pixelValueR,pixelValueG,pixelValueB,pixelLab);
				//get saturation values from AB values and make it betwwen 0 and 1
				saturation = Math.sqrt(pixelLab[1]*pixelLab[1] + pixelLab[2]*pixelLab[2]) / (128 * Math.sqrt(2));
				
				valueIndex = y * valueMapSize + x;
				//first check lightness to classify black-like and white-like colors
				//then check saturation for grey colors
				//then first check on green - red values
				// then fallback on yellow - blue values
				if (pixelLab[0] < 30) {
					//too dark
					valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.DARK;
				} else if (pixelLab[0] > 85) {
					//too bright
					valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.CLEAR;
				} else if (saturation < 0.04) {
					//not saturated enough
					if (pixelLab[0] > 60) {
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.TRUEGREY;
					} else {
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.GREY;
					}
				} else
				if (pixelLab[1] >= 0.3) {
					if (saturation < 0.06) {
						//not saturated enough to be considered red
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.TRUEGREY;
					} else {
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.RED;
					}
				} else if (pixelLab[1] <= -0.3) {
					if (pixelLab[2] < - 0.3) {
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.TRUEBLUE;
					} else {
						if (pixelLab[0] > 48) {
							valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.GREEN;
						} else {
							valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.DARKGREEN;
						}
					}
				} else if (pixelLab[2] >= 0.3) {
					if (pixelLab[2] > 0.7 && pixelLab[0] > 70) {
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.TRUEYELLOW;
					} else {
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.YELLOWISH;
					}
				} else if (pixelLab[2] <= -0.3) {
					if (pixelLab[2] >- 0.5) {
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.BLUEISH;
					} else {
						valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.TRUEBLUE;
					}
				} else {
					valueMap[valueIndex] = PhotoTreatedSimpleBlockDefinition.PhotoColor.GREY;
				}
			}
		}
		
	}
	
	@Override
	protected void applyValueMapToMineMap(MineMap map) throws MinecraftGenerationException {
		if (MineGenerator.getDebugMode().hasDebugInfo()) {
			//for debug purposes : copy values as image //////////
			byte[] curValue = new byte[3];
			byte[] pixelBytes = new byte[valueMapSize*valueMapSize*3];
			for(int x=0; x<valueMapSize; x++) {
				for(int y=0; y<valueMapSize; y++) {
					switch(valueMap[x + y * valueMapSize]) {
					case DARK:
						curValue[0] = (byte) 0x00;
						curValue[1] = (byte) 0x00;
						curValue[2] = (byte) 0x00;
						break;
					case CLEAR:
						curValue[0] = (byte) 0xff;
						curValue[1] = (byte) 0xff;
						curValue[2] = (byte) 0xff;
						break;
					case GREY:
						curValue[0] = (byte) 0x70;
						curValue[1] = (byte) 0x70;
						curValue[2] = (byte) 0x70;
						break;
					case TRUEGREY:
						curValue[0] = (byte) 0xcc;
						curValue[1] = (byte) 0xcc;
						curValue[2] = (byte) 0xcc;
						break;
					case RED:
						curValue[0] = (byte) 0x00;
						curValue[1] = (byte) 0x00;
						curValue[2] = (byte) 0xff;
						break;
					case GREEN:
						curValue[0] = (byte) 0x00;
						curValue[1] = (byte) 0xff;
						curValue[2] = (byte) 0x00;
						break;
					case DARKGREEN:
						curValue[0] = (byte) 0x00;
						curValue[1] = (byte) 0x70;
						curValue[2] = (byte) 0x00;
						break;
					case YELLOWISH:
						curValue[0] = (byte) 0x00;
						curValue[1] = (byte) 0x70;
						curValue[2] = (byte) 0x70;
						break;
					case TRUEYELLOW:
						curValue[0] = (byte) 0x00;
						curValue[1] = (byte) 0xff;
						curValue[2] = (byte) 0xff;
						break;
					case BLUEISH:
						curValue[0] = (byte) 0x70;
						curValue[1] = (byte) 0x00;
						curValue[2] = (byte) 0x00;
						break;
					case TRUEBLUE:
						curValue[0] = (byte) 0xff;
						curValue[1] = (byte) 0x00;
						curValue[2] = (byte) 0x00;
						break;
					case PURPLE:
						curValue[0] = (byte) 0xff;
						curValue[1] = (byte) 0x00;
						curValue[2] = (byte) 0xff;
						break;
					default :
						curValue[0] = (byte) 0x00;
						curValue[1] = (byte) 0x00;
						curValue[2] = (byte) 0x00;
						break;
					}
					pixelBytes[ (y * valueMapSize + x)*3 ] = curValue[0];
					pixelBytes[ (y * valueMapSize + x)*3 + 1 ] = curValue[1];
					pixelBytes[ (y * valueMapSize + x)*3 + 2 ] = curValue[2];
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
		
		for(int y=0; y<valueMapSize; y++) {
			for(int x=0; x<valueMapSize; x++) {
			/* !! y here is z in the minecraft world !! */
				if (PhotoTreatedBlockDefinition.class.isAssignableFrom(map.getSurfaceBlock(x, y).getClass())) {
					//if block can be treated with photo, apply the current value
					((PhotoTreatedBlockDefinition) map.getSurfaceBlock(x, y)).applyPhotoColor(x, y, valueMap[x + y * valueMapSize]);
				}
			}
		}


	}

}
