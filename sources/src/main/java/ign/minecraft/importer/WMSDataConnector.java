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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.geometry.DirectPosition2D;

import ign.minecraft.MineGenerator;
import ign.minecraft.MinecraftGenerationException;
import ign.minecraft.Utilities;

public class WMSDataConnector extends DataConnector {
    protected static final Logger LOGGER = Logger.getLogger("WMSDataConnector");

	protected String serviceKey = Utilities.properties.getProperty("serviceKeyWMS");
	protected String serviceHost = Utilities.properties.getProperty("serviceHostWMS");
	protected String serviceProtocol = Utilities.properties.getProperty("serviceProtocolWMS");
	protected String serviceName = null;
	protected String serviceLayer = null;
	protected String serviceFormat = null;
	protected String serviceAdditional = "";
	protected String serviceStyle = "";
	protected int serviceValueMaxSize = 10000;

	public WMSDataConnector(DataTreatment dataTreatment, String serviceName, String serviceLayer, String serviceFormat) {
		this(dataTreatment, serviceName, serviceLayer, serviceFormat, "", "");
	}
	public WMSDataConnector(DataTreatment dataTreatment, String serviceName, String serviceLayer,
			String serviceFormat, String serviceStyle, String serviceAdditional) {
		super(dataTreatment);
		
		this.serviceName = serviceName;
		this.serviceLayer = serviceLayer;
		this.serviceFormat = serviceFormat;
		this.serviceStyle = serviceStyle;
		this.serviceAdditional = serviceAdditional;
	}

	@Override
	public int getMaxReadSize() {
		//TODO : use a service capabilities request to set serviceValueMaxSize
		return serviceValueMaxSize;
	}

	@Override
	public void readData(double currentRealworldXMin, double currentRealworldXMax, double currentRealworldYMin,
			double currentRealworldYMax, int sizeX, int sizeY) throws MinecraftGenerationException {
		assert serviceKey != null;
		assert serviceHost != null;
		assert serviceName != null;
		assert serviceLayer != null;
		assert serviceFormat != null;

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

			sizeX = (int) (MineGenerator.getInstance().realworldBorderSize * MineGenerator.MINECRAFTMAP_RATIO);
			sizeY = sizeX;
		}

		final int sizeXfinal = sizeX;
		final int sizeYfinal = sizeY;

		/* make the call to the service */
		String url = serviceProtocol+"://"+serviceHost+"/"+serviceKey+"/geoportail/"+serviceName+"/wms?"
				+ "SERVICE=WMS&VERSION=1.3.0"
				+ "&REQUEST=GetMap"
				+ "&LAYERS="+serviceLayer
				+ "&CRS=" + localZone.crsName
				+ "&BBOX="+currentRealworldXMin+","+currentRealworldYMin+","+currentRealworldXMax+","+currentRealworldYMax
				+ "&WIDTH="+sizeXfinal+"&HEIGHT="+sizeYfinal
				+ "&STYLES="+serviceStyle+"&FORMAT="+serviceFormat+serviceAdditional
			;
		
		Utilities.retry(() -> {
		    	
			LOGGER.log(Level.INFO,"WMS Service called for layer " + serviceLayer + " : " + url);
				
			/* get the stream */
			InputStream rawDataStream;
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
			
			/* treat the stream */
			byte[] readBuffer,readFullData;
			readBuffer = new byte[4096];
			readFullData = new byte[sizeXfinal*sizeYfinal*4];
			int readNbBytes;
			int readIndex = 0;
		
			//read byte data from the stream
			try {
				while( (readNbBytes = rawDataStream.read(readBuffer)) != -1 )
				{
					System.arraycopy(readBuffer, 0, readFullData, readIndex, readNbBytes);
					readIndex += readNbBytes;
				}
			} catch (IOException e) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_MEMORY_ERROR, e);
			}
			
			//load byte data into values specific to end class
			try {
				dataTreatment.treatFeatureData(ByteBuffer.wrap(readFullData).order(ByteOrder.LITTLE_ENDIAN));
			} catch (Exception e) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.SERVICEIMPORT_RAWDATA_ERROR, e);
			}
				
		});
	}

}
