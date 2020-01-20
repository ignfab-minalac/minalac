/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

public abstract class PhotoTreatedIdentifiedBlockDefinition extends IdentifiedBlockDefinition implements PhotoTreatedBlockDefinition {
	
	//static common storage of photo colors
	// to avoid having one full array per instance although there is no overlap
	protected static PhotoColor[] photoColors;
	
	public static void setMapSize(int mapSize) {
		photoColors = new PhotoColor[mapSize * mapSize];
	}
	
	public PhotoTreatedIdentifiedBlockDefinition(Integer id) {
		super(id);
	}

	@Override
	public void applyPhotoColor(int x, int z, PhotoColor color) {
		photoColors[xz1D(x, z)] = color;
	}
}
