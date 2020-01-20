/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

import java.io.File;
import java.nio.file.Path;
import java.util.TreeMap;

import ign.minecraft.MineGenerator;
import ign.minecraft.MinecraftMap;

public class LocalTester {

	public static void main(String[] args) {
		//directory setting
		final String OUTPUT_DIR = "D:/David/cartes minecraft generees";
		final String DEBUGIMAGES_DIR = "D:/David/images generees";
		//generation parameters
		final int MAP_NBREGIONS = 2;
		final MineGenerator.DebugMode DEBUG_MODE = MineGenerator.DebugMode.DEBUG_FASTGEN;
		final String RENDERED_MAP = "test";
		
		final TreeMap<String,double[]> MAP_CENTERS = new TreeMap<String,double[]>();
		double[] coords;

		coords = new double[2];
		coords[0] = -1.15080195;
		coords[1] = 46.15705787;
		MAP_CENTERS.put("test", coords);

		coords = new double[2];
		coords[0] = 1.44334096;
		coords[1] = 43.60451270;
		MAP_CENTERS.put("toulouse", coords);

		coords = new double[2];
		coords[0] = -1.1569067;
		coords[1] = 46.15511445;
		MAP_CENTERS.put("la rochelle", coords);
		
		coords = new double[2];
		coords[0] = 6.7785769;
		coords[1] = 45.5748726;
		MAP_CENTERS.put("les arcs", coords);
		
		coords = new double[2];
		coords[0] = 2.50100519;
		coords[1] = 48.79373069;
		MAP_CENTERS.put("saint maur", coords);
		
		coords = new double[2];
		coords[0] = -1.4426901;
		coords[1] = 43.65562662;
		MAP_CENTERS.put("hossegor plage", coords);
		
		coords = new double[2];
		coords[0] = 2.33411421;
		coords[1] = 48.86145656;
		MAP_CENTERS.put("paris louvre", coords);
		
		coords = new double[2];
		coords[0] = -1.0525892;
		coords[1] = 48.8070412;
		MAP_CENTERS.put("normandie", coords);
		
		coords = new double[2];
		coords[0] = 5.0998065;
		coords[1] = 44.1374394;
		MAP_CENTERS.put("le barroux", coords);

		coords = new double[2];
		coords[0] = 5.0865305;
		coords[1] = 44.6525716;
		MAP_CENTERS.put("saou", coords);

		coords = new double[2];
		coords[0] = 2.3472252;
		coords[1] = 48.8535865;
		MAP_CENTERS.put("paris cite", coords);

		coords = new double[2];
		coords[0] = 2.2933280;
		coords[1] = 48.85896053;
		MAP_CENTERS.put("paris eiffel", coords);

		coords = new double[2];
		coords[0] = 4.8314374;
		coords[1] = 45.7576227;
		MAP_CENTERS.put("lyon", coords);
		
		coords = new double[2];
		coords[0] = 45.282106;
		coords[1] = -12.807446;
		MAP_CENTERS.put("mayotte", coords);
		
		try {
			assert MAP_CENTERS.containsKey(RENDERED_MAP);

			final Path outputPath = new File(OUTPUT_DIR).toPath();
			File outputDir = outputPath.resolve(RENDERED_MAP).toFile();
			File resourcesDir = new File(System.getProperty("user.dir")).toPath().resolve("resources").toFile();
			// if the directory does not exist, create it
			if (!outputDir.exists()) {
				outputDir.mkdir();
		    }
			    
			double[] mapOrigin = MAP_CENTERS.get(RENDERED_MAP);
			MineGenerator.SetDebugMode(DEBUG_MODE,DEBUGIMAGES_DIR);
			MineGenerator generator = MineGenerator.getInstance();
			
			generator.setMapSize(MAP_NBREGIONS * MineGenerator.MINECRAFTMAP_MAPTILESIZE);
			generator.generateMap(mapOrigin[0], mapOrigin[1], outputDir.toPath(), RENDERED_MAP, "1,2,3,4,5", resourcesDir.toPath(), MinecraftMap.class);
			
			generator = null;
			MineGenerator.destroyInstance();
			
			System.out.println("done!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
