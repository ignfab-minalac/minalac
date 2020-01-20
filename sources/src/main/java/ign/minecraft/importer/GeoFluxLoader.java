/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;
import java.util.Map;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;

public abstract class GeoFluxLoader {
	
	public enum Zone {
		FRANCE_METRO(-5.22, 9.60, 41.30, 51.10, "EPSG:2154"),
		GUADELOUPE(-61.83, -60.97, 15.81, 16.54,"EPSG:32620"),
		MARTINIQUE(-61.25, -60.78, 14.37, 14.91, "EPSG:32620"),
		GUYANE(-54.67, -51.44, 2.06, 5.86, "EPSG:2972"),
		REUNION(55.18, 55.85, -21.40, -20.85, "EPSG:2975"),
		STPIERRE_ET_MIQUELON(-56.52, -56.07, 46.74, 47.16, "EPSG:4467"),
		MAYOTTE(44.88, 45.33, -13.10, -12.53, "EPSG:4471"),
		WORLDWIDE(0.00,0.00,0.00,0.00, "EPSG:3857")
		;

		public final double minLong;
		public final double maxLong;
		public final double minLat;
		public final double maxLat;
		public final String crsName;
		
		private Zone(double minLong, double maxLong, double minLat, double maxLat, String crsName) {
			this.minLong = minLong;
			this.maxLong = maxLong;
			this.minLat = minLat;
			this.maxLat = maxLat;
			this.crsName = crsName;
		}
	}
	
	private void setLocalZone(double realworldCenterLong, double realworldCenterLat) throws MinecraftGenerationException {

		if(MineGenerator.MINECRAFTMAP_RATIO <= 0.01) {
			localZone = Zone.WORLDWIDE;
			return;
		}

		for (Zone curZone : Zone.values()) {
			if(curZone.equals(Zone.WORLDWIDE)) // do not check coordinates with worldwide
				continue;
			if ( (realworldCenterLong >= curZone.minLong) && (realworldCenterLong <= curZone.maxLong)
					&& (realworldCenterLat >= curZone.minLat) && (realworldCenterLat <= curZone.maxLat) ) {
				localZone = curZone;
				return;
			}
		}
		
		
		throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_UNSUPPORTED_ZONE);
	}
	
	Zone localZone;
	CoordinateReferenceSystem wgs84Crs;
	CoordinateReferenceSystem localCrs;
	
	DataConnector dataConnector;
	
	protected Path resourcesPath;
	
	protected Map<String, Long> times;

	//x and y min and max (in local coordinates) for the whole treated area
	protected double realworldXMin;
	protected double realworldXMax;
	protected double realworldYMin;
	protected double realworldYMax;
	double[] realworldMin;
	double[] realworldMax;
	
	protected int valueMapSize;
	protected double valueToRealWorldRatio;
	
	//store the current coordinates (real world and in pixel/block of our value map)
	double currentRealworldXMin;
	double currentRealworldXMax;
	double currentRealworldYMin;
	double currentRealworldYMax;
	protected int currentValueX = 0;
	protected int currentValueY = 0;
	protected int currentValuesToReadSizeX = 0;
	protected int currentValuesToReadSizeY = 0;
	
	protected boolean hasMore = false;
	
	public GeoFluxLoader(double realworldSquareSize, double realworldCenterLong, double realworldCenterLat, int valueMapSize, Path resourcesPath) throws MinecraftGenerationException {
		//determine the zone and projection to use from requested latitude and longitude
		setLocalZone(realworldCenterLong, realworldCenterLat);

		try {
			wgs84Crs = CRS.decode("EPSG:4326");
			localCrs = CRS.decode( localZone.crsName );
		} catch (Exception e) {
			//unexpected
			assert false;
		}

		//transform the realworld WGS84 coordinates into meter based local coordinates
		MathTransform transformWgs84ToLocal = null;
		try {
			transformWgs84ToLocal = CRS.findMathTransform(wgs84Crs, localCrs);
		} catch (Exception e) {
			//unexpected
			assert false;
		}
		assert transformWgs84ToLocal.getSourceDimensions() == 2;
		assert transformWgs84ToLocal.getTargetDimensions() == 2;
		DirectPosition2D pos = new DirectPosition2D(localCrs);
		try {
			//careful as coordinates are entered in the order lat long! 
			transformWgs84ToLocal.transform(new DirectPosition2D(wgs84Crs, realworldCenterLat, realworldCenterLong), pos);
		} catch (Exception e) {
			//unexpected
			assert false;
		}
		realworldXMin = pos.getX() - realworldSquareSize/2;
		realworldYMin = pos.getY() - realworldSquareSize/2;
		realworldXMax = pos.getX() + realworldSquareSize/2;
		realworldYMax = pos.getY() + realworldSquareSize/2;
		
		assert valueMapSize % 2 == 0;
		this.valueMapSize = valueMapSize;
		
		valueToRealWorldRatio = realworldSquareSize / valueMapSize;
		
		this.resourcesPath = resourcesPath;
	}
	
	/* is to  be reused ? ( = has data that will be used by other importers ) */
	public boolean isToBeReused() {
		return false; // false by default
	}

	/* set the map in which timer values will be put */
	public void setTimes(Map<String, Long> times) {
		this.times = times;
	}


	/* different steps of import */
	protected void loadFlux() throws MinecraftGenerationException {
		assert dataConnector != null;
		
		dataConnector.setCRS(localZone, wgs84Crs);
		int readOnceMaxSize = dataConnector.getMaxReadSize();
		
		/* prepare bbox values */
		currentValuesToReadSizeX = valueMapSize - currentValueX;
		currentValuesToReadSizeY = valueMapSize - currentValueY;
		boolean hasMoreX = false;
		boolean hasMoreY = false;
		hasMore = false;

		/* tiling system only activated on raster layers (above 10000*10000 pixels limit which in theory isn't happening since 5km limit)
		 * because WFS dataconnector has its own paging system which is more accurate and reliable when applied to vector data
		 */
		if(dataConnector instanceof WMSDataConnector) {
			if(currentValuesToReadSizeX > readOnceMaxSize)
			{
				hasMoreX = true;
				currentValuesToReadSizeX = readOnceMaxSize;
			}
			if(currentValuesToReadSizeY > readOnceMaxSize)
			{
				hasMoreY = true;
				currentValuesToReadSizeY = readOnceMaxSize;
			}
		}

		//compute the current real world bounding box from current value index
		currentRealworldXMin = realworldXMin + currentValueX * valueToRealWorldRatio;
		currentRealworldXMax = currentRealworldXMin + currentValuesToReadSizeX * valueToRealWorldRatio;
		currentRealworldYMin = realworldYMin + currentValueY * valueToRealWorldRatio;
		currentRealworldYMax = currentRealworldYMin + currentValuesToReadSizeY * valueToRealWorldRatio;

		assert currentRealworldXMax <= realworldXMax;
		assert currentRealworldYMax <= realworldYMax;
		
		dataConnector.initAndReadData(currentRealworldXMin, currentRealworldXMax, currentRealworldYMin, currentRealworldYMax,
				currentValuesToReadSizeX, currentValuesToReadSizeY);
		
		// prepare variables for next call if there is more
		if(hasMore = hasMoreX || hasMoreY) {
			if(hasMoreX) {
				currentValueX = currentValueX + currentValuesToReadSizeX;
			} else if(hasMoreY) {
				currentValueX = 0;
				currentValueY = currentValueY + currentValuesToReadSizeY;
			}
		}
	}
	protected void loadTile() throws MinecraftGenerationException { }
	protected void treatValues() throws MinecraftGenerationException {}
	protected abstract void applyValueMapToMineMap(MineMap map) throws MinecraftGenerationException;
	
	public void importToMineMap(MineMap map) throws MinecraftGenerationException {
		boolean mustLoad = true;
		long timerStart = System.currentTimeMillis();
		while (mustLoad) {
			loadFlux();
			mustLoad = hasMore;
		}
		times.put(this.getClass().getName() + " - Flux Loading", System.currentTimeMillis() - timerStart);
		timerStart = System.currentTimeMillis();
		treatValues();
		times.put(this.getClass().getName() + " - Value Treatment", System.currentTimeMillis() - timerStart);
		timerStart = System.currentTimeMillis();
		applyValueMapToMineMap(map);
		times.put(this.getClass().getName() + " - Map Converting", System.currentTimeMillis() - timerStart);
	}
	
}
