/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ign.minecraft.MinecraftGenerationException;

/**
 * this class defines the interface to load data to an importer 
 * 
 * @author David Fremont
 *
 */
public abstract class DataConnector {
	
	protected final DataTreatment dataTreatment;
	protected GeoFluxLoader.Zone localZone;
	protected CoordinateReferenceSystem wgs84Crs;

	public DataConnector(DataTreatment dataTreatment) {
		this.dataTreatment = dataTreatment;
	}

	public abstract int getMaxReadSize();
	
	public void initAndReadData(double currentRealworldXMin, double currentRealworldXMax,
			double currentRealworldYMin, double currentRealworldYMax, int sizeX, int sizeY) throws MinecraftGenerationException {
		dataTreatment.initTileTreatment();
		readData(currentRealworldXMin, currentRealworldXMax, currentRealworldYMin, currentRealworldYMax,
				sizeX, sizeY);
	}
	protected abstract void readData(double currentRealworldXMin, double currentRealworldXMax,
			double currentRealworldYMin, double currentRealworldYMax, int sizeX, int sizeY) throws MinecraftGenerationException;

	public void setCRS(GeoFluxLoader.Zone localZone, CoordinateReferenceSystem wgs84Crs) {
		this.localZone = localZone;
		this.wgs84Crs = wgs84Crs;
	}
}
