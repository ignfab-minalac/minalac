/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import ign.minecraft.MineGenerator;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.Utilities;

public class WFSDataConnector extends DataConnector {
    protected static final Logger LOGGER = Logger.getLogger("WFSDataConnector");

	protected String serviceKey = Utilities.properties.getProperty("serviceKeyWFS");
	protected String serviceHost = Utilities.properties.getProperty("serviceHostWFS");
	protected String serviceProtocol = Utilities.properties.getProperty("serviceProtocolWFS");
	protected String serviceLayer = null;
	protected String[] serviceLayers = null;
	protected String serviceAdditional = "";
	protected int serviceValueMaxSize = 2048;
	protected int maxFeatures = Integer.parseInt(Utilities.properties.getProperty("serviceMaxFeaturesWFS"));
	private int objectCount;
	private InputStream rawDataStream;
	
	
	public WFSDataConnector(DataTreatment treatment, String serviceLayer) {
		this(treatment, serviceLayer, "");
		this.serviceLayer = serviceLayer;
		this.serviceLayers = serviceLayer.split(",");
	}
	public WFSDataConnector(DataTreatment treatment, String serviceLayer, String serviceAdditional) {
		super(treatment);

		this.serviceLayer = serviceLayer;
		this.serviceLayers = serviceLayer.split(",");
		this.serviceAdditional = serviceAdditional;
	}

	@Override
	public int getMaxReadSize() {
		return serviceValueMaxSize;
	}

	@Override
	public void readData(double currentRealworldXMin, double currentRealworldXMax,
			double currentRealworldYMin, double currentRealworldYMax, int sizeX, int sizeY) throws MinecraftGenerationException {
		assert serviceKey != null;
		assert serviceHost != null;
		assert serviceLayer != null;
		
		if(MineGenerator.MINECRAFTMAP_ANGLE != 0) {
			DirectPosition2D pos = Utilities.convertGeoToCarto(MineGenerator.MINECRAFTMAP_CENTER.getY(), MineGenerator.MINECRAFTMAP_CENTER.getX());

			currentRealworldXMin = (pos.getX() - MineGenerator.getInstance().realworldBorderSize/2) + ((GeoFluxLoader)super.dataTreatment).currentValueX * (MineGenerator.getInstance().realworldBorderSize/((GeoFluxLoader)super.dataTreatment).valueMapSize);
			currentRealworldXMax = currentRealworldXMin + ((GeoFluxLoader)super.dataTreatment).currentValuesToReadSizeX * (MineGenerator.getInstance().realworldBorderSize/((GeoFluxLoader)super.dataTreatment).valueMapSize);
			currentRealworldYMin = (pos.getY() - MineGenerator.getInstance().realworldBorderSize/2) + ((GeoFluxLoader)super.dataTreatment).currentValueY * (MineGenerator.getInstance().realworldBorderSize/((GeoFluxLoader)super.dataTreatment).valueMapSize);
			currentRealworldYMax = currentRealworldYMin + ((GeoFluxLoader)super.dataTreatment).currentValuesToReadSizeY * (MineGenerator.getInstance().realworldBorderSize/((GeoFluxLoader)super.dataTreatment).valueMapSize);
			/*currentRealworldXMin = (currentRealworldXMin - (pos.getX() - (MineGenerator.getInstance().borderSize/MineGenerator.MINECRAFTMAP_RATIO)/2)) + (pos.getX() - MineGenerator.getInstance().realworldBorderSize/2);
  			currentRealworldYMin = (currentRealworldYMin - (pos.getY() - (MineGenerator.getInstance().borderSize/MineGenerator.MINECRAFTMAP_RATIO)/2)) + (pos.getY() - MineGenerator.getInstance().realworldBorderSize/2);
  			currentRealworldXMax = (currentRealworldXMax - (pos.getX() + (MineGenerator.getInstance().borderSize/MineGenerator.MINECRAFTMAP_RATIO)/2)) + (pos.getX() + MineGenerator.getInstance().realworldBorderSize/2);
			currentRealworldYMax = (currentRealworldYMax - (pos.getY() + (MineGenerator.getInstance().borderSize/MineGenerator.MINECRAFTMAP_RATIO)/2)) + (pos.getY() + MineGenerator.getInstance().realworldBorderSize/2);*/
		}

		// For each layer, get the object/feature count around the BBOX area
		// And then fetch them (using paging with maxFeatures variable and WFS index)
		for(String layer : serviceLayers) {
			getObjectCount(currentRealworldXMin, currentRealworldXMax, currentRealworldYMin, currentRealworldYMax, layer, 0);
			int totalObjectCount = objectCount;
			
			while(objectCount == maxFeatures) {
				getObjectCount(currentRealworldXMin, currentRealworldXMax, currentRealworldYMin, currentRealworldYMax, layer, totalObjectCount);
				totalObjectCount += objectCount;
			}
			//LOGGER.log(Level.INFO, "WFS object count for this request : " + totalObjectCount + " ; Last object count : " + objectCount);

			fetchFeatures(currentRealworldXMin, currentRealworldXMax, currentRealworldYMin, currentRealworldYMax, layer, totalObjectCount);
			//LOGGER.log(Level.INFO, "Fetched all features !");
		}
	}
	
	public void getObjectCount(double currentRealworldXMin, double currentRealworldXMax,
			double currentRealworldYMin, double currentRealworldYMax, String layer, int actualObjectCount) throws MinecraftGenerationException {
		objectCount = -1;
		
		/* make the call to the service */
		String url = serviceProtocol+"://"+serviceHost+"/"+serviceKey+"/geoportail/wfs?"
				+ "SERVICE=WFS&VERSION=1.1.0"
				+ "&REQUEST=GetFeature"
				+ "&TYPENAME="+layer
				+ "&SRSNAME="+localZone.crsName
				+ "&BBOX="+currentRealworldXMin+","+currentRealworldYMin+","
							+currentRealworldXMax+","+currentRealworldYMax+","+localZone.crsName
				+ "&STARTINDEX="+actualObjectCount
				+ "&MAXFEATURES="+maxFeatures
				+ "&RESULTTYPE=hits"
				+ serviceAdditional;
			;
		//LOGGER.log(Level.INFO,"WFS Service called for layer " + layer + " : " + url);
		
		Utilities.retry(() -> {
			/* get the stream */
			getStream(url);
	
			//treat the stream
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			GetHitsHandler handler = new GetHitsHandler();
			try {
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(rawDataStream, handler);
				objectCount = handler.getObjectCount();
			} catch (SAXException | IOException | ParserConfigurationException e) {
				LOGGER.log(Level.SEVERE, "XML parsing error on Geoportail req : " + e.getMessage(), e);
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
			}
		});
	}
	
	public void fetchFeatures(double currentRealworldXMin, double currentRealworldXMax, double currentRealworldYMin, double currentRealworldYMax, 
			String layer, int totalObjectCount) throws MinecraftGenerationException {
		objectCount = 0;

		do {
			/* make the call to the service */
			String url = serviceProtocol+"://"+serviceHost+"/"+serviceKey+"/geoportail/wfs?"
					+ "SERVICE=WFS&VERSION=1.1.0"
					+ "&REQUEST=GetFeature"
					+ "&TYPENAME="+layer
					+ "&SRSNAME="+localZone.crsName
					+ "&BBOX="+currentRealworldXMin+","+currentRealworldYMin+","
								+currentRealworldXMax+","+currentRealworldYMax+","+localZone.crsName
					+ "&STARTINDEX="+objectCount
					+ "&MAXFEATURES="+maxFeatures
					+ serviceAdditional;
				;
			
			Utilities.retry(() -> {
				LOGGER.log(Level.INFO,"WFS Service called for layer " + layer + " : " + url);
					
				/* get the stream */
				getStream(url);
					
				//treat the stream
				GML gml = new GML(Version.WFS1_1);
				gml.setCoordinateReferenceSystem(wgs84Crs);
				SimpleFeatureCollection features = null;
				try {
					features = gml.decodeFeatureCollection(rawDataStream);
				} catch (Exception e) {
					throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
				}
				if(features != null) {
					SimpleFeatureIterator it = features.features();
					while (it.hasNext()) {
						SimpleFeature currentFeature = it.next();
						if(MineGenerator.MINECRAFTMAP_ANGLE != 0) {
			    			AffineTransformation at = new AffineTransformation();
			    			DirectPosition2D pos = Utilities.convertGeoToCarto(MineGenerator.MINECRAFTMAP_CENTER.getY(), MineGenerator.MINECRAFTMAP_CENTER.getX());
			    			at.rotate(-MineGenerator.MINECRAFTMAP_ANGLE * (Math.PI / 180), pos.getX(), pos.getY());
			    			currentFeature.setDefaultGeometry(at.transform((Geometry)currentFeature.getDefaultGeometry()));
			    		}
						if (dataTreatment.treatFeature(currentFeature)) {
							Object g = currentFeature.getDefaultGeometry();
							if(g != null && Geometry.class.isAssignableFrom(g.getClass()) ) {
								dataTreatment.treatFeatureData( g );
							}
						}
					}
				}
			});
			
			//objectCount += Math.min(maxFeatures,totalObjectCount-objectCount);
			objectCount += maxFeatures;
		} while(objectCount < totalObjectCount);
	}
	
	public void getStream(String url) throws MinecraftGenerationException {
		Utilities.retry(() -> {
			URLConnection urlConnection;
			try {
				URL urlGetFeature = new URL(url);
				urlConnection = urlGetFeature.openConnection();
			} catch (Exception e) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_CONNECTION_ERROR, e);
			}
			try {
				rawDataStream = urlConnection.getInputStream();
			} catch (Exception e) {
				//try to get info on the error
				assert HttpURLConnection.class.isAssignableFrom(urlConnection.getClass());
				String error;
				try {
					error = "http connection error : " + ((HttpURLConnection) urlConnection).getResponseCode() + " => "
						+ ((HttpURLConnection) urlConnection).getResponseMessage();
				} catch (IOException e1) {
					error = "could not retrieve the http connection error code";
				}
				LOGGER.log(Level.SEVERE, error, e);
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_CONNECTION_ERROR, e);
			}
		});
	}

}

class GetHitsHandler extends DefaultHandler {
	int objectCount = -1;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		objectCount = Integer.parseInt(attributes.getValue("numberOfFeatures"));
	}
	
	public int getObjectCount() {
		return objectCount;
	}
}