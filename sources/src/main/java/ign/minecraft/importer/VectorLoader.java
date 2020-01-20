/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.MineGenerator;
import ign.minecraft.definition.BlockDefinition;
import ign.minecraft.definition.ValueBlockDefinition;


abstract class VectorLoader extends GeoFluxLoader implements DataTreatment {
	public static final int COLOR_DEFAULT_FEATURE = 0xFFFFFFFF;//RGBA value, last byte must be not null otherwise color will be set to null
	
	public static final int COORD_GRID_CHUNK_SIZE = 1024;
	public static final int COORD_ALTITUDE_NONE = -1;
	
	public static Color toColor(int RGBAValue) {
		//use constructor with ARGB value
		return new Color( (RGBAValue >>> 8) | (RGBAValue << 24), true );
	}
	
	protected AltiImporter altiImporter;
	
	protected BlockDefinition[] valueMap;
	protected final TreeMap<Integer,BlockDefinition> colorsToValues = new TreeMap<Integer,BlockDefinition>();
	protected final TreeMap<Integer, Color> colors = new TreeMap<Integer, Color>();
	
	protected BufferedImage image;
	protected Graphics2D graphics;
	private GridGeometry2D gridGeom;
	private int[] coordGridX = new int[COORD_GRID_CHUNK_SIZE];
    private int[] coordGridY = new int[COORD_GRID_CHUNK_SIZE];
    private int[] coordGridZ = new int[COORD_GRID_CHUNK_SIZE];
    
    private boolean currentBBoxInit = false;//indicate if bbox has been initialized
    protected int[] currentBBox = new int[4];//store if needed the bbox of the current geometry as minX,maxX,minY,maxY

	public VectorLoader(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		valueMap = new BlockDefinition[valueMapSize * valueMapSize];
		
		// get alti importer to transform z coordinates
		//altiImporter = (AltiImporter) MinecraftGenerator.getInstance().getImporter("ign.minecraft.importer.AltiImporter");
		// TODO : SELECTION MINECRAFT/MINETEST DISPO EN VARIABLE GLOBALE OU SELECTION MINECRAFT/MINETEST VISIBLE VIA CLASSE INIT
		altiImporter = (AltiImporter) MineGenerator.getInstance().getImporter("ign.minecraft.importer.AltiImporter");
		
		
		/* init colors */
        colorsToValues.put(COLOR_DEFAULT_FEATURE, ValueBlockDefinition.DEFAULT_VALUE);
		
		/* create graphics stuff */
        image = new BufferedImage(valueMapSize, valueMapSize, BufferedImage.TYPE_4BYTE_ABGR);
        graphics = image.createGraphics();
        graphics.setColor(toColor(COLOR_DEFAULT_FEATURE));
	}
	
	protected void createColors() {
		//create the colors map from the colors to values keys
		colors.clear();
		for(Entry<Integer, BlockDefinition> entry : colorsToValues.entrySet()) {
			colors.put(entry.getKey(), toColor(entry.getKey()));
		}
	}

	@Override
	public void initTileTreatment() throws MinecraftGenerationException {
		//coordinate transform from returned world coordinates and or tile in graphic buffer
        gridGeom = new GridGeometry2D(
                new GridEnvelope2D(currentValueX, valueMapSize - currentValueY - currentValuesToReadSizeY,
                					currentValuesToReadSizeX, currentValuesToReadSizeY),
                new ReferencedEnvelope(currentRealworldXMin, currentRealworldXMax,
				                		currentRealworldYMin, currentRealworldYMax,
				                		localCrs) );
	}

	@Override
	public boolean treatFeature(Object feature) throws MinecraftGenerationException {
		return true;
	}

	@Override
	public void treatFeatureData(Object data) throws MinecraftGenerationException {
		assert Geometry.class.isAssignableFrom(data.getClass());
		
		try {
			treatGeometry((Geometry) data);
		} catch (InvalidGridGeometryException | TransformException e) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
		}
	}
	
	/**
	 * 
	 * treat a geometry from a WFS feature as values in the value map
	 * 
	 * @param geometry
	 * @throws TransformException 
	 * @throws InvalidGridGeometryException 
	 */
	protected void treatGeometry(Geometry geometry) throws InvalidGridGeometryException, TransformException {
		Geometries geomType = Geometries.get(geometry);
		switch (geomType) {
		    case MULTIPOLYGON:
		    case MULTILINESTRING:
		    case MULTIPOINT:
		        for (int i = 0; i < geometry.getNumGeometries(); i++) {
		            Geometry geomN = geometry.getGeometryN(i);
		            treatGeometry(geomN);
		        }
		        break;
		        
		    case POLYGON:
		    	//WFS layers with polygons should extend PolygonReadyWFSFluxLoader, not this class
		    	assert false;
		        break;
		    case LINESTRING:
		    case POINT:
		        drawGeometry(geomType, geometry, false, false);
		        break;
		        
		    default:
		    	//unexpected
		    	assert false;
		}
	}
	
	protected void drawGeometry(Geometries geomType, Geometry geometry, boolean isHole, boolean storeBBox) throws InvalidGridGeometryException, TransformException {
        Coordinate[] coords = geometry.getCoordinates();

        // Go through coordinate array in order received
        DirectPosition2D worldPos = new DirectPosition2D();
        // enlarge grid position buffer if needed
        if (coords.length > coordGridX.length) {
            int n = coords.length / COORD_GRID_CHUNK_SIZE + 1;
            coordGridX = new int[n * COORD_GRID_CHUNK_SIZE];
            coordGridY = new int[n * COORD_GRID_CHUNK_SIZE];
            coordGridZ = new int[n * COORD_GRID_CHUNK_SIZE];
        }
        //init bbox array if needed
        if (storeBBox && !currentBBoxInit) {
        	currentBBox[0] = valueMapSize - 1;//minX
        	currentBBox[1] = 0;//maxX
        	currentBBox[2] = valueMapSize - 1;//minY
        	currentBBox[3] = 0;//maxY
        	currentBBoxInit = true;
        }
        for (int n = 0; n < coords.length; n++) {
            worldPos.setLocation(coords[n].x, coords[n].y);
            GridCoordinates2D gridPos;
			gridPos = gridGeom.worldToGrid(worldPos);
            coordGridX[n] = gridPos.x;
            coordGridY[n] = gridPos.y;
            if (storeBBox) {
	            currentBBox[0] = Math.max(0,			Math.min(currentBBox[0], gridPos.x));
	            currentBBox[1] = Math.min(valueMapSize - 1, Math.max(currentBBox[1], gridPos.x));
	            currentBBox[2] = Math.max(0,			Math.min(currentBBox[2], gridPos.y));
	            currentBBox[3] = Math.min(valueMapSize - 1, Math.max(currentBBox[3], gridPos.y));
            }
			coordGridZ[n] = ( ((Double) coords[n].z).isNaN() || coords[n].z == 9999 ) ? COORD_ALTITUDE_NONE :  altiImporter.transformAltitude(coords[n].z);
        }
        
        drawFromCoords(geomType, coordGridX, coordGridY, coordGridZ, coords.length, isHole);
    }
	
	/**
	 * drawing primitives call
	 * can be overloaded to define other styles : draw a polygon outline or draw in a separate graphics for example
	 * 
	 * the feature is provided to give access to its data
	 */
	protected void drawFromCoords(Geometries geomType, int[] coordGridX, int[] coordGridY, int[] coordGridZ, int coordsLength, boolean isHole) {
        switch (geomType) {
	        case POLYGON:
	            graphics.fillPolygon(coordGridX, coordGridY, coordsLength);
	            break;
	            
	        case LINESTRING:  // includes LinearRing
	            graphics.drawPolyline(coordGridX, coordGridY, coordsLength);
	            break;
	            
	        case POINT:
	            graphics.fillRect(coordGridX[0], coordGridY[0], 1, 1);
	            break;
	            
	        default:
	            throw new IllegalArgumentException("Invalid geometry type: " + geomType.getName());
	    }
	}

	
	/**
	 * treatment of read data as a whole
	 * 
	 * @return true if everything went ok
	 * @throws MinecraftGenerationException 
	 */
	@Override
	protected void treatValues() throws MinecraftGenerationException {
		if(MineGenerator.getDebugMode().hasDebugInfo()) {
			//debug : save pixel data
			File outputfile = new File(MineGenerator.getDebugDir()).toPath().resolve(this.getClass().getName()+".png").toFile();
		    try {
				ImageIO.write(image, "png", outputfile);
			} catch (IOException e) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_DEBUGIMAGEDUMP_ERROR, e);
			}
		}
        
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
				treatPixelValue(x, y, pixelValueRGBA);
			}
		}
	}

	/**
	 * treatment of pixel data, by default store the corresponding value in colors to values
	 * 
	 */
	protected void treatPixelValue(int x, int y, int pixelValueRGBA) {
		if(colorsToValues.containsKey(pixelValueRGBA)) {
			valueMap[y * valueMapSize + x] = colorsToValues.get(pixelValueRGBA);
		}
	}
}
