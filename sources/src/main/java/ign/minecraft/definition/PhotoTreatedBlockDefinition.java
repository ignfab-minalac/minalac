/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

public interface PhotoTreatedBlockDefinition {
	public enum PhotoColor {
		NONE,
    	DARK,
    	CLEAR,
    	GREY,
    	TRUEGREY,
    	RED,
    	GREEN,
    	DARKGREEN,
    	YELLOWISH,
    	TRUEYELLOW,
    	BLUEISH,
    	TRUEBLUE,
    	PURPLE
	}
	
	public void applyPhotoColor(int x, int z, PhotoColor color);
}
