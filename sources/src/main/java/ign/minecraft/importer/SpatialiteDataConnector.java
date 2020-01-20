/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.spatialite.SQLiteConfig;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.io.WKTReader;

import ign.minecraft.MineGenerator;
import ign.minecraft.MinecraftGenerationException;

public class SpatialiteDataConnector extends DataConnector {
    protected static final Logger LOGGER = Logger.getLogger("SpatialiteDataConnector");
    
    protected static String BASE_FILE_EXTENSION = "dbf";

    protected final Path resourcesPath;
    protected final String[] natures;
    protected final String fileName;
    
	public SpatialiteDataConnector(DataTreatment treatment, Path resourcesPath, String directory, String fileName) throws MinecraftGenerationException {
		this(treatment, resourcesPath, new String[] {directory}, fileName);
	}
	public SpatialiteDataConnector(DataTreatment treatment, Path resourcesPath, String[] directories, String fileName) throws MinecraftGenerationException {
		super(treatment);
		
		this.resourcesPath = resourcesPath;
		this.natures = directories;
		this.fileName = fileName;
	}

	@Override
	public int getMaxReadSize() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void readData(double currentRealworldXMin, double currentRealworldXMax,
		double currentRealworldYMin, double currentRealworldYMax, int sizeX, int sizeY) throws MinecraftGenerationException {

		for (int fileIndex = 0; fileIndex < natures.length; fileIndex ++) {
			LOGGER.log(Level.INFO,resourcesPath.resolve(natures[fileIndex].toString()).resolve(fileName + ".sqlite").toFile().getAbsolutePath());
			
			SQLiteConfig config = new SQLiteConfig();
        	config.enableLoadExtension(true);;
        	
        	Connection conn = null;
			try {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:" + resourcesPath.resolve(natures[fileIndex].toString()).resolve(fileName + ".sqlite").toFile().getAbsolutePath().toString()
						, config.toProperties());
			} catch (SQLException e2) {
				continue;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				conn.createStatement().execute("SELECT load_extension('mod_spatialite')"); // For Ubuntu 16.04
			} catch (SQLException e2) {
			}
			
			try {
				conn.createStatement().execute("SELECT load_extension('/usr/local/lib/mod_spatialite.so')"); // For Ubuntu 14.04
			} catch (SQLException e2) {
			}
			
	        try {
	        	Statement stmt = conn.createStatement();
	        	
	        	String schema = fileName;
	        	if(schema.equals("france-metro")) 
	        		schema = "france_metro";
	        	
	        	String sqlQuery = "SELECT ST_AsText(GEOMETRY) AS geom FROM " + schema + " WHERE "
	        		+ "ST_Intersects("
        				+ "ST_GeomFromText("
		        			+ "'POLYGON(("+currentRealworldXMin + " " + currentRealworldYMin + ", "
		        						+ currentRealworldXMax + " " + currentRealworldYMin + ", "
		        						+ currentRealworldXMax + " " + currentRealworldYMax + ", "
		        						+ currentRealworldXMin + " " + currentRealworldYMax + ", "
		        						+ currentRealworldXMin + " " + currentRealworldYMin 
		        			+ "))'"
		        		+ ")"
        			+ ", ST_GeomFromText(geom))";
	        	//String sqlQuery = "SELECT ST_AsText(GEOMETRY) AS geom FROM " + schema;
	        	
	        	// TODO Vérifier si cela marche en dehors de la France métropolitaine (projections différentes)
	        	// Vérifier la projection de currentRealWorld (dans GeoFluxLoader) et vérifier la projection de GEOMETRY dans geom_cols_ref_sys (du fichier Spatialite)
	        	// Si besoin, ST_GeomFromText accepte un second argument qui est le SRID (EPSG) : exemple 2154 ou 4326.
	        	ResultSet result = stmt.executeQuery(sqlQuery);
	        	
	        	while(result.next()) {
	        		Geometry g = new WKTReader().read(result.getString("geom"));
	        		//ReferencedEnvelope envelope = new ReferencedEnvelope(currentRealworldXMin, currentRealworldYMin, currentRealworldXMax, currentRealworldYMax, DefaultGeographicCRS.WGS84);
	        		//if(g != null && Geometry.class.isAssignableFrom(g.getClass()) && envelope.intersects(g.getCoordinate())) {
	        		if(g != null && Geometry.class.isAssignableFrom(g.getClass())) {
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
	        	
	        	conn.close();
	        } catch (Exception e) {
	        	try {
					conn.close();
				} catch (SQLException e1) {
				}
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
	        }
		}
	}

}
