/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import ign.minecraft.MineGenerator;
import ign.minecraft.MinecraftGenerationException;

public class ShapefileDataConnector extends DataConnector {
    protected static final Logger LOGGER = Logger.getLogger("ShapefileDataConnector");
    
    protected static String BASE_FILE_EXTENSION = "dbf";

    protected final Path resourcesPath;
    protected final String[] directories;
    protected final String fileName;
    protected final File[] dbFiles;
    
	public ShapefileDataConnector(DataTreatment treatment, Path resourcesPath, String directory, String fileName) throws MinecraftGenerationException {
		this(treatment, resourcesPath, new String[] {directory}, fileName);
	}
	public ShapefileDataConnector(DataTreatment treatment, Path resourcesPath, String[] directories, String fileName) throws MinecraftGenerationException {
		super(treatment);
		
		this.resourcesPath = resourcesPath;
		this.directories = directories;
		this.fileName = fileName;
		this.dbFiles = new File[directories.length];
		for (int i = 0; i < directories.length; i ++) {
			dbFiles[i] = new File(resourcesPath.resolve(directories[i]).resolve(fileName + ".dbf").toString());
			if (!dbFiles[i].exists()) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_SHAPEFILE_ERROR);
			}
		}
	}

	@Override
	public int getMaxReadSize() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void readData(double currentRealworldXMin, double currentRealworldXMax,
		double currentRealworldYMin, double currentRealworldYMax, int sizeX, int sizeY) throws MinecraftGenerationException {

		ShapefileReader reader;
		ShpFiles allShapeFiles;

		for (int fileIndex = 0; fileIndex < dbFiles.length; fileIndex ++) {
			LOGGER.log(Level.INFO,"Reading shapefile " + directories[fileIndex].toString() + "/" + fileName);
	
			//build the file reader
			try {
				allShapeFiles = new ShpFiles(dbFiles[fileIndex]);
				reader = new ShapefileReader(allShapeFiles, false, false, new GeometryFactory());
			} catch (Exception e) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_SHAPEFILE_ERROR, e);
			}
	
			try {
				while (reader.hasNext()) {
					ShapefileReader.Record currentFeature = reader.nextRecord();
					if (dataTreatment.treatFeature(currentFeature)) {
						Object g = currentFeature.shape();
						if(g != null && Geometry.class.isAssignableFrom(g.getClass()) ) {
							if(MineGenerator.MINECRAFTMAP_ANGLE != 0) {
				    			AffineTransformation at = new AffineTransformation();
				    			double centerX = currentRealworldXMin + (MineGenerator.getInstance().borderSize / MineGenerator.MINECRAFTMAP_RATIO)/2;
				    			double centerY = currentRealworldYMin + (MineGenerator.getInstance().borderSize / MineGenerator.MINECRAFTMAP_RATIO)/2;
				    			at.rotate(-MineGenerator.MINECRAFTMAP_ANGLE * (Math.PI / 180), centerX, centerY);
				    			g = at.transform((Geometry)g);
				    		}
							dataTreatment.treatFeatureData( g );
						}
					}
				}
			} catch (Exception e) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
			}
			try {
				reader.close();
			} catch (IOException e) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
			}
		}
	}

}
