/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

import ign.minecraft.MineGenerator;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.MinecraftMap;
import ign.minecraft.importer.GeoFluxLoader;

import java.io.File;

public class FullScaleTester {

	public static void main(String[] args) throws MinecraftGenerationException {
		
		File outputDir = new File("./fullscale-test-generation");
		File resourcesDir = new File("./resources");
		// if the directory does not exist, create it
		if (!outputDir.exists()) {
			outputDir.mkdir();
	    }
		    
		File debugDir = new File("./fullscale-test-debug");
		if (!debugDir.exists()) {
			debugDir.mkdir();
	    } else {
	    	File[] files = debugDir.listFiles();
	        if(files!=null) { //some JVMs return null for empty dirs
	            for(File f: files) {
                    f.delete();
	            }
	        }
	    }
		
		MineGenerator.SetDebugMode(MineGenerator.DebugMode.DEBUGINFO, debugDir.getPath());
		MineGenerator generator = MineGenerator.getInstance();

		double longitude;
		double latitude;
		if (Math.random() < 0.5) {
			//test a lot on paris for intense object usage
			longitude = 2.33411421;
			latitude = 48.86145656;
		} else {
			GeoFluxLoader.Zone zone;
			zone = (Math.random() < 0.9) ? GeoFluxLoader.Zone.FRANCE_METRO : GeoFluxLoader.Zone.MAYOTTE;
			//try to stay in the center of the map to avoid entire zone being outside
			longitude = zone.minLong + (0.3 + Math.random() * 0.4) * (zone.maxLong - zone.minLong);
			latitude = zone.minLat + (0.3 + Math.random() * 0.4) * (zone.maxLat - zone.minLat);
		}
		generator.generateMap(longitude, latitude, outputDir.toPath(), "fullscale test", "1,2,3,4,5", resourcesDir.toPath(), MinecraftMap.class);
		
		generator = null;
		MineGenerator.destroyInstance();
		
		System.out.println("done!");
	}

}
