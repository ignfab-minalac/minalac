/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.importer;

import ign.minecraft.MinecraftGenerationException;

public interface DataTreatment {
	public void initTileTreatment() throws MinecraftGenerationException;
	public boolean treatFeature(Object feature) throws MinecraftGenerationException;
	public void treatFeatureData(Object data) throws MinecraftGenerationException;
}
