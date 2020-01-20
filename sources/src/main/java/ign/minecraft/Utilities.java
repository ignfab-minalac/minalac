/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import ign.minecraft.importer.GeoFluxLoader.Zone;

public class Utilities {
	public static final Properties properties = new Properties();
	protected static final Logger LOGGER = Logger.getLogger("RetryModule");
	
	public interface IUnreliable<T extends Exception> {
		void tryRun ( ) throws T;
	}
	
	@SuppressWarnings("unchecked")
	// Imitates runtime exception throwing in Lambda expressions
	// Allows to omit "throws" clause in the signature of a function
	static <T extends Exception, R> R sneakyThrow(Exception t) throws T {
	    throw (T) t;
	}
	
	public static <T extends Exception> void retry (IUnreliable<T> runnable) {
		for(int retries = 0;; retries++) {
			try {
				runnable.tryRun();
				return;
			} catch (Exception e) {
				if (retries < 10) {
					try {
						Thread.sleep(5000); // wait to avoid another breakdown
					} catch (InterruptedException e1) {
						LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
					}
		        	LOGGER.log(Level.SEVERE, "An exception was thrown, retrying.", e);
		            continue; // retry an attempt
				} else {
					LOGGER.log(Level.SEVERE, "Crash limit exceeded, stopping the program.", e);
					sneakyThrow(e); // more than 10 retries -> throwing the error and exiting program
				}
			}
		}
	}
	
	public static double[] rotatePoints(double[] center, double[] point, double yaw) {
		// converting degrees to radians
		double angle = yaw * (Math.PI / 180);
		// translate to center
		double[] p2 = {point[0]-center[0], point[1]-center[1]};
		// rotate using matrix rotation
		int[] p3 = {(int)Math.round(1*p2[0]+(-Math.tan(angle/2))*p2[1]),(int)Math.round(0*p2[0]+1*p2[1])};
		int[] p3_1 = {(int)Math.round(1*p3[0]+0*p3[1]),(int)Math.round(Math.sin(angle)*p3[0]+1*p3[1])};
		int[] p3_2 = {(int)Math.round(1*p3_1[0]+(-Math.tan(angle/2))*p3_1[1]),(int)Math.round(0*p3_1[0]+1*p3_1[1])};
		
		// translate back to center
		double[] p4 = {p3_2[0]+center[0], p3_2[1]+center[1]};
		
		// return the rotated point
		return p4;
	}
	
	public static Zone getLocalZone(double realworldCenterLong, double realworldCenterLat) throws MinecraftGenerationException {
		
		if(MineGenerator.MINECRAFTMAP_RATIO <= 0.01) {
			return Zone.WORLDWIDE;
		}

		for (Zone curZone : Zone.values()) {
			if(curZone.equals(Zone.WORLDWIDE)) // do not check coordinates with worldwide
				continue;
			if ( (realworldCenterLong >= curZone.minLong) && (realworldCenterLong <= curZone.maxLong)
					&& (realworldCenterLat >= curZone.minLat) && (realworldCenterLat <= curZone.maxLat) ) {
				return curZone;
			}
		}
		
		
		throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_UNSUPPORTED_ZONE);
	}

	public static DirectPosition2D convertGeoToCarto(double realworldCenterLat, double realworldCenterLong) throws MinecraftGenerationException {
		Zone localZone = getLocalZone(realworldCenterLong, realworldCenterLat);
		CoordinateReferenceSystem wgs84Crs = null;
		CoordinateReferenceSystem localCrs = null;
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
		return pos;
	}

	public static void disposeImageReader(ImageReader reader) throws IOException {
		if(reader.getInput() != null && reader.getInput() instanceof ImageInputStream)
			((ImageInputStream)reader.getInput()).close();
		reader.dispose();
	}
}
