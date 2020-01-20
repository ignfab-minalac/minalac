/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft;


import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import ign.minecraft.definition.BuildingBlockDefinition;
import ign.minecraft.definition.CemetaryBlockDefinition;
import ign.minecraft.definition.IdentifiedBlockDefinition;
import ign.minecraft.definition.OverlayIdentifiedBlockDefinition;
import ign.minecraft.definition.PhotoTreatedIdentifiedBlockDefinition;
import ign.minecraft.definition.VegetalZoneBlockDefinition;
import ign.minecraft.importer.AltiImporter;
import ign.minecraft.importer.BuildingsImporter;
import ign.minecraft.importer.GeoFluxLoader;

public class MineGenerator {
    protected static final Logger LOGGER = Logger.getLogger("MinecraftGenerator");

	static public final int MINECRAFTMAP_MAPTILESIZE = 512; /* size of the side of a region file, in blocks */
	static public double MINECRAFTMAP_MAPNBREGIONS = 10;
	static public double MINECRAFTMAP_RATIO = 1; /* 1 realworld meter = 1 block in minecraft * PIXELMAP_RATIO */
	static public double MINECRAFTMAP_ALTIRATIO = 1;
	static public double MINECRAFTMAP_ANGLE = 0;
	static public int MINECRAFTMAP_SNOWHEIGHTMIN = 0;
	static public int MINECRAFTMAP_SNOWHEIGHTMAX = 5;
	static public Point MINECRAFTMAP_CENTER;

	public enum DebugMode {
		NONE,
		FASTGEN,
		DEBUGINFO,
		DEBUG_FASTGEN;
		
		public boolean isFastGen() {
			return (this==FASTGEN || this==DEBUG_FASTGEN);
		}
		public boolean hasDebugInfo() {
			return (this==DEBUGINFO || this==DEBUG_FASTGEN);
		}
	}
	static private DebugMode debugMode = DebugMode.NONE;
	static private String debugDir = ".";

	static public int MODE_NORMAL = 0;
	static public int MODE_PLAINUNDERGROUND = 0x01;
	static private int mode = MODE_NORMAL;

	static public int NO_BORDER_FILLING = 0;
	static public int BORDER_FILLING = 0x01;
	static private int borderFilling = BORDER_FILLING;

	static public int MODE_SNOW = 0x01;
	static private int snowMode = MODE_NORMAL;


	static private MineGenerator instance;
	
	static public MineGenerator getInstance() {
		if(instance == null)
			instance = new MineGenerator();
		return instance;
	}

	static public void destroyInstance() {
		instance = null;
	}
	
	static public void SetDebugMode(DebugMode debugMode, String debugDir) {
		MineGenerator.debugMode  = debugMode;
		MineGenerator.debugDir = debugDir;
	}
	static public DebugMode getDebugMode() {
		return debugMode;
	}
	static public void setDebugDir(String debugDir) {
		MineGenerator.debugDir = debugDir;
	}
	static public String getDebugDir() {
		return debugDir;
	}
	
	static public void SetMode(int mode) {
		MineGenerator.mode  = mode;
	}
	static public int getMode() {
		return mode;
	}

	static public void setBorderFilling(int mode) {
		MineGenerator.borderFilling = mode;
	}
	static public int getBorderFilling() {
		return MineGenerator.borderFilling;
	}

	static public void setSnowMode(int mode) {
		MineGenerator.snowMode = mode;
	}
	static public int getSnowMode() {
		return MineGenerator.snowMode;
	}

	public int borderSize = 10 * MINECRAFTMAP_MAPTILESIZE;
	public double realworldBorderSize;
	
	private final Map<String, Long> times = new LinkedHashMap<String, Long>();
	private final Map<String,GeoFluxLoader> reusedImporters = new HashMap<String,GeoFluxLoader>();
	
	/**
	 * set the size of one border of the total generated map
	 * 
	 * @param mapSize
	 */
	public void setMapSize(int mapSize) {
		borderSize = mapSize;
	}
	
	public void generateMap(double realworldCenterLong, double realworldCenterLat, Path path, String name, String importerNumbers, Path resourcesPath, Class<? extends MineMap> mapClass ) throws MinecraftGenerationException {
		LOGGER.log(Level.INFO, "coordonnées de generation long/lat : " + realworldCenterLong + "," + realworldCenterLat);
		
		MINECRAFTMAP_CENTER = JTSFactoryFinder.getGeometryFactory().createPoint(new Coordinate(realworldCenterLong, realworldCenterLat));
		
		if(!path.toFile().exists() || !path.toFile().isDirectory()) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.MINECRAFTEXPORT_GLOBAL_INVALIDGENERATIONPATH);
		}
		
		//record start time
		times.put("Global - Start", System.currentTimeMillis());
		
		realworldBorderSize = borderSize / MINECRAFTMAP_RATIO;

		if(MineGenerator.MINECRAFTMAP_ANGLE != 0) {
			// Rotation in degrees
			DirectPosition2D pos = Utilities.convertGeoToCarto(realworldCenterLat, realworldCenterLong);

			double realworldSquareSize = borderSize / MINECRAFTMAP_RATIO;
			double realworldXMin = pos.getX() - realworldSquareSize/2;
			double realworldYMin = pos.getY() - realworldSquareSize/2;
			double realworldXMax = pos.getX() + realworldSquareSize/2;
			double realworldYMax = pos.getY() + realworldSquareSize/2;

			double[] realworldMin = new double[] { realworldXMin, realworldYMin };
			double[] realworldMax = new double[] { realworldXMax, realworldYMax };
			double width = realworldMax[0] - realworldMin[0];
			double height = realworldMax[1] - realworldMin[1];

			AffineTransform at = new AffineTransform();
			at.rotate(-MineGenerator.MINECRAFTMAP_ANGLE * (Math.PI / 180),pos.getCoordinate()[0],pos.getCoordinate()[1]);
			Rectangle bounds = new Rectangle((int)width,(int)height);
			Rectangle rotatedBound = at.createTransformedShape(bounds).getBounds();

			// getting the enveloppe bbox around the rotated rectangle (so that we get a bigger one and lose no info when really rotating in WFS and WMS data connectors)
			// we get width/height (rect size) by substracting extremities (max-min) of each axis of the rotated rectangle
			width = rotatedBound.getWidth();
			height = rotatedBound.getHeight();
			realworldSquareSize = Math.max(width, height); // new square size (square envelope around rotated rectangle)

			realworldBorderSize = (int) realworldSquareSize;
		}

		//init some block definition with map size
		IdentifiedBlockDefinition.setMapSize(borderSize);
		OverlayIdentifiedBlockDefinition.setMapSize(borderSize);
		PhotoTreatedIdentifiedBlockDefinition.setMapSize(borderSize);
		VegetalZoneBlockDefinition.setMapSize(borderSize);
		BuildingBlockDefinition.setMapSize(borderSize);
		CemetaryBlockDefinition.setMapSize(borderSize);

		// create importers from different data
		ArrayList<String> importerNamesList = new ArrayList<String>();
		// AltiImporter is mandatory
		importerNamesList.add("AltiImporter");

		for(String importerName : importerNumbers.split(",")) {
			switch(importerName) {
				// ALTI (is added anyway, kept for stats purpose)
				case "1":
					//importerNamesList.add("AltiImporter");
					break;
				// HYDRO
				case "2":
					importerNamesList.add("SeaLimitImporter");
					importerNamesList.add("SandBeachImporter");
					importerNamesList.add("GravelBeachImporter");
					importerNamesList.add("HydroLinesImporter");
					importerNamesList.add("HydroSurfacesImporter");
					break;
				// LAND
				case "3":
					importerNamesList.add("CountryLandImporter");
					importerNamesList.add("OCSImporter");
					importerNamesList.add("FieldsImporter");
					importerNamesList.add("CemetariesImporter");
					importerNamesList.add("VegetalZonesImporter");
					break;
				// ROADS
				case "4":
					importerNamesList.add("RoadSurfacesImporter");
					importerNamesList.add("RoadsImporter");
					break;
				// BUILDINGS TRACES ONLY
				case "6":
					BuildingsImporter.TRACE_ONLY = true;
				// BUILDINGS
				case "5":
					importerNamesList.add("BuildingsImporter");
					importerNamesList.add("LinearConstructionsImporter");
					break;
				// HYPSOMETRIC LAYER
				case "7":
					AltiImporter.HYPSOMETRIC = true;
					break;
			}
		}

		//if(importerNamesList.contains("AltiImporter") || importerNamesList.contains("FieldsImporter")) {
		importerNamesList.add("PhotoImporter");
		//}

		String[] importerNames = {
			"AltiImporter", "SeaLimitImporter", "CountryLandImporter",
			"OCSImporter", "FieldsImporter",
			"SandBeachImporter", "GravelBeachImporter",
			"CemetariesImporter", "RoadSurfacesImporter", "BuildingsImporter",
			"HydroLinesImporter", "HydroSurfacesImporter",
			"LinearConstructionsImporter", "RoadsImporter",
			"VegetalZonesImporter", "PhotoImporter"
		};

		// Use ArrayList construction whenever we have an order with a layer missing or with add-ons (hypso...)
		if(!importerNumbers.equals("1,2,3,4,5")) {
			ArrayList<String> retainedImporters = new ArrayList<String>(Arrays.asList(importerNames));
			retainedImporters.retainAll(importerNamesList);
			importerNames = retainedImporters.toArray(new String[0]);
		}

		GeoFluxLoader[] importers = new GeoFluxLoader[importerNames.length];
		for(int i=0; i<importers.length; i++) {
			Class<?> importerClass;
			try {
				importerClass = Class.forName("ign.minecraft.importer."+importerNames[i]);
			} catch (ClassNotFoundException e) {
				assert false;//unexpected
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_GLOBAL_ERROR, e);
			}
			Constructor<?> constructor;
			try {
				constructor = importerClass.getConstructor(double.class,double.class,double.class,int.class, Path.class);
			} catch (NoSuchMethodException | SecurityException e) {
				assert false;//unexpected
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_GLOBAL_ERROR, e);
			}
			try {
				// borderSize => minecraft map size (5km default)
				// borderSize / ratio => real map size (which might be smaller than minecraft map size, if we take 2 cubes for 1 meter for ex.)
				importers[i] = (GeoFluxLoader) constructor.newInstance(borderSize / MINECRAFTMAP_RATIO, realworldCenterLong, realworldCenterLat, borderSize, resourcesPath);
				importers[i].setTimes(times);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
				assert false;//unexpected
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_GLOBAL_ERROR, e);
			} catch (InvocationTargetException e) {
				//constructor throws an exception
				/*switch (importerNames[i]) {
				case "SeaLimitImporter":
				case "SandBeachImporter":
				case "GravelBeachImporter":
				case "OCSImporter":
				case "PhotoImporter":
				case "FieldsImporter":*/
					//optional importers, as we can be in an unsupported zone, and the generation can end fine without them
					importers[i] = null;
				/*	break;
				default:
					throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_GLOBAL_ERROR, e);
				}*/
			}
			if(importers[i] != null && importers[i].isToBeReused()) {
				reusedImporters.put(importers[i].getClass().getName(), importers[i]);
			}
		}
		
		long timerStart;
		// create the map, make the import and fill the map object
		try {
			MineMap mapObject = mapClass
					.getConstructor(int.class,Path.class,String.class,Path.class)
					.newInstance(borderSize, path, name, resourcesPath);
			
			for(int i=0; i<importers.length; i++) {
				if (importers[i] != null) {
					timerStart = System.currentTimeMillis();
					importers[i].importToMineMap(mapObject);
					times.put(importerNames[i] + " - Import", System.currentTimeMillis() - timerStart);
					importers[i] = null; //free memory
				}
			}
			timerStart = System.currentTimeMillis();
			times.put("Global - Import", timerStart - times.get("Global - Start"));
			mapObject.writeToDisk();
			times.put("Global - Map Export", System.currentTimeMillis() - timerStart);
		}
		catch (Exception e) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.MINECRAFTEXPORT_GLOBAL_ERROR, e);
		}
		
		long timerTotal = System.currentTimeMillis() - times.get("Global - Start");
		times.put("Global - Total Time", timerTotal);
		for (String timerEventName : times.keySet()) {
			if (timerEventName == "Global - Start") {
				LOGGER.log(Level.INFO, "timer start : " + (new Date(times.get(timerEventName))).toString() );
			} else {
				LOGGER.log(Level.INFO, "timer event : " + timerEventName + " => " + ((float) times.get(timerEventName) / 1000) + "s"
							+ " (" + ((int) (100 * times.get(timerEventName) / timerTotal)) + "%)");
			}
		}
	}
	
	public GeoFluxLoader getImporter(String importerClass) {
		assert (reusedImporters.containsKey(importerClass));
		return reusedImporters.get(importerClass);
	}

}
