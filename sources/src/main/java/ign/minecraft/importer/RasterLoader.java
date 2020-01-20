/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import java.nio.file.Path;

import ign.minecraft.MinecraftGenerationException;

abstract class RasterLoader extends GeoFluxLoader {
	public RasterLoader(double realworldSquareSize,  double realworldCenterLong,double realworldCenterLat, int pixelMapSize, Path resourcesPath) throws MinecraftGenerationException {
		super(realworldSquareSize, realworldCenterLong, realworldCenterLat, pixelMapSize, resourcesPath);
		
		//round coordinates to make sure our bounding box starts and ends on service pixels
		realworldXMin = Math.round(realworldXMin);
		realworldXMax = Math.round(realworldXMax);
		realworldYMin = Math.round(realworldYMin);
		realworldYMax = Math.round(realworldYMax);
	}
}
