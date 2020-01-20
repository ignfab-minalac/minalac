/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import ign.minecraft.BedrockMap;
import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.MinecraftMap;
import ign.minecraft.MinetestMap;
import ign.minecraft.Utilities;
import ign.minecraft.definition.BuildingBlockDefinition;
import ign.minecraft.definition.UndergroundCopyBlockDefinition;
import ign.minecraft.importer.AltiImporter;

public class CommandLauncher {
	
	public static void main(String[] args) {
		disableCertSSL();
		try {
		    File jarPath = new File(CommandLauncher.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		    String propertiesPath = jarPath.getParentFile().getAbsolutePath();
		    Utilities.properties.load(new FileInputStream(propertiesPath + "/config.properties"));
		} catch (IOException e) {
			Logger.getLogger("MinecraftGenerator").log(Level.SEVERE, "Couldn't load config.properties file. Exception: " + e.getMessage());
		}
		
		//final int MAP_NBREGIONS = 1;
		Double mapNbRegions = 10.0;
		
		Class<? extends MineMap> mapClass;
		
		String saveFolder = "";
		String name = "";
		Double coordLong = 0.0;
		Double coordLat = 0.0;
		String resourcesMapFolder = null;
		String mapType = "";
		Double ratio = 0.0;
		Double altitudeRatio = 0.0;
		Double angle = 0.0;
		int snowHeightMin = 0;
		int snowHeightMax = 5;
		String streamChoice = null;
		boolean validArguments = false;

		int startArgIndex = 0;
		while ( (args.length > startArgIndex) && (args[startArgIndex].startsWith("--")) ) {
			//options
			switch (args[startArgIndex].substring(2, args[startArgIndex].length())) {
			case "fastgen":
				MineGenerator.SetDebugMode( MineGenerator.getDebugMode().hasDebugInfo()
						? MineGenerator.DebugMode.DEBUG_FASTGEN : MineGenerator.DebugMode.FASTGEN,
					null);
				break;
			case "debuginfo":
				MineGenerator.SetDebugMode(MineGenerator.getDebugMode().isFastGen()
						? MineGenerator.DebugMode.DEBUG_FASTGEN : MineGenerator.DebugMode.DEBUGINFO,
					null);
				break;
			case "plainunderground":
				MineGenerator.SetMode(MineGenerator.MODE_PLAINUNDERGROUND);
				break;
			case "noborder":
				MineGenerator.setBorderFilling(MineGenerator.NO_BORDER_FILLING);
				break;
			case "snow":
				MineGenerator.setSnowMode(MineGenerator.MODE_SNOW);
				break;
			default:
				System.out.println("133 une option (premiers arguments commençant par --) est inconnue. options possibles : --fastgen --plainunderground --noborder --snow");
				return;
			}
			startArgIndex++;
		}
		if(args.length >= startArgIndex + 11) {
			saveFolder = args[startArgIndex];
			name = args[startArgIndex + 1];
			coordLong = Double.parseDouble(args[startArgIndex + 2]);
			coordLat = Double.parseDouble(args[startArgIndex + 3]);
			resourcesMapFolder = args[startArgIndex + 4];
			mapType = args[startArgIndex + 5];
			ratio = Double.parseDouble(args[startArgIndex + 6]);
			altitudeRatio = Double.parseDouble(args[startArgIndex + 7]);
			mapNbRegions = Double.parseDouble(args[startArgIndex + 8]);
			angle = Double.parseDouble(args[startArgIndex + 9]);
			streamChoice = args[startArgIndex + 10];
			if(args.length >= startArgIndex + 13 && MineGenerator.getSnowMode() == MineGenerator.MODE_SNOW) {
				snowHeightMin = Integer.parseInt(args[startArgIndex + 11]); // optional argument (goes along w/ snow option)
				snowHeightMax = Integer.parseInt(args[startArgIndex + 12]);
			}
			if ((name.length() > 0)
					 && (coordLong != 0) && !coordLong.isNaN()
					 && (coordLat != 0) && !coordLat.isNaN()
					 && (resourcesMapFolder.length() > 0)
					 && (mapType.length() > 0) 
					 && (ratio != 0) && !ratio.isNaN()
					 && (altitudeRatio != 0) && !altitudeRatio.isNaN()
					 && (mapNbRegions > 0)
					 && (angle >= -90) && (angle <= 90)
					 && (streamChoice.length() > 0)) {
				validArguments = true;
			}
		}
		
		if (!validArguments) {
			System.out.println("128 les arguments n'ont pas pu être interprétés. usage : minecraftsavefolder nom coordX coordY resourcesFolder");
			return;
		}
		
		File outputDir = new File(saveFolder);
		if(!outputDir.getParentFile().exists() || !outputDir.getParentFile().isDirectory()) {
			System.out.println("131 Le répertoire parent pour la génération n'existe pas");
			return;
		}
		try {
			// if the directory does not exist, create it
			if (!outputDir.exists()) {
				outputDir.mkdir();
		    }
		} catch (Exception e) {
			System.out.println("132 Les répertoires de génération n'ont pas pu être créés");
			return;
		}
		
		//set debug info dir in the output dir now that we have it
		if (MineGenerator.getDebugMode().hasDebugInfo()) {
			Path debugPath = outputDir.toPath().resolve("debug");
			debugPath.toFile().mkdir();
			MineGenerator.setDebugDir(debugPath.toString());
		}
		
		MineGenerator generator = MineGenerator.getInstance();
		
		File resourcesDir = new File(resourcesMapFolder);
		try {
			generator.setMapSize((int)(MineGenerator.getDebugMode().isFastGen() ? 2 : mapNbRegions) * MineGenerator.MINECRAFTMAP_MAPTILESIZE);
			MineGenerator.MINECRAFTMAP_MAPNBREGIONS = mapNbRegions;
			
			if(mapType.equals("minetest")) { 
				AltiImporter.BLOCK_ALTITUDE_MAX = Integer.parseInt(Utilities.properties.getProperty("maxBlockAltitude"));
				BuildingBlockDefinition.BUILDING_ALTITUDE_LIMIT = Integer.parseInt(Utilities.properties.getProperty("maxBuildingHeight"));
				UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT = Integer.parseInt(Utilities.properties.getProperty("maxUndergroundDefinitionMT"));
				UndergroundCopyBlockDefinition.UNDERGROUND_WATER_LIMIT = Integer.parseInt(Utilities.properties.getProperty("maxUndergroundWaterMT"));
			}
			
			MineGenerator.MINECRAFTMAP_RATIO = Math.max(Math.min(ratio,2.0),0.002); // Map ratio between 1 and 2 only
			MineGenerator.MINECRAFTMAP_ALTIRATIO = Math.max(Math.min(altitudeRatio,5.0), 1.0);
			MineGenerator.MINECRAFTMAP_ANGLE = Math.max(Math.min(angle,90.0), -90.0); // Map angle between -90 and 180° only
			MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN = Math.max(Math.min(snowHeightMin,5), 0);
			MineGenerator.MINECRAFTMAP_SNOWHEIGHTMAX = Math.max(Math.min(snowHeightMax,5), 1);
			
			switch(mapType) {
				default:
				case "minecraft":
					mapClass = MinecraftMap.class;
					break;
				case "minetest":
				case "kidscode":
					mapClass = MinetestMap.class;
					break;
				case "bedrock":
				case "edu":
					mapClass = BedrockMap.class;
					break;
			}
			
			generator.generateMap(coordLong, coordLat, outputDir.toPath(), name, streamChoice, resourcesDir.toPath(), mapClass);
		} catch (MinecraftGenerationException e) {
			System.out.println(e.getId() + " " + e.getMessage());
			//TODO: log the dump related to source MinecraftGenerationException
		    final Logger logger = Logger.getLogger("CommandLauncher");
		    Exception loggedException = e;
		    while(loggedException != null) {
		    	logger.log(Level.SEVERE, loggedException.getMessage(), loggedException);
		    	loggedException =  ((loggedException.getCause() != null)
		    							&& Exception.class.isAssignableFrom(loggedException.getCause().getClass()))
		    		? (Exception) loggedException.getCause() : null;
		    }
			return;
		}
		
		generator = null;
		MineGenerator.destroyInstance();
		
		System.out.println("0 la génération s'est correctement terminée");
		
		if(mapClass == BedrockMap.class) // Temporary.
			// TODO : Eliminate the remaining thread (Timer-0) inside the Bedrock conversion library.
			// Seems to be due to the calls to getCombined() function of FaweCache by other classes.
			// => Create an issue on the Bedrock conversion library's GitHub.
			System.exit(0);
	}
	
	public static void disableCertSSL() {
		// Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509ExtendedTrustManager() {
	        	@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
						throws CertificateException {
				}
				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
						throws CertificateException {
				}
				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
						throws CertificateException {
				}
				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
						throws CertificateException {
				}
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}
				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
            }
        };
 
        // Install the all-trusting trust manager
        SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
		}
		
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
 
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

}
