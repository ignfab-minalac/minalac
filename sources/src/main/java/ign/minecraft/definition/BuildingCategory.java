/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import ign.minecraft.definition.PhotoTreatedBlockDefinition.PhotoColor;

public enum BuildingCategory {
	GENERIC, CHURCH_CASTLE;
	
	public enum RoofColor {
		NONE,
		BLACK,
    	RED,
    	GREY,
    	GREEN,
	}
	
	public static RoofColor getRoofColor(PhotoColor photoColor) {
		switch (photoColor) {
		case YELLOWISH:
		case TRUEYELLOW:
		case GREY:
		case DARK:
			return RoofColor.BLACK;
		case CLEAR:
		case TRUEGREY:
		case BLUEISH:
			return RoofColor.GREY;
		case RED:
		case PURPLE:
			return RoofColor.RED;
		case GREEN:
		case DARKGREEN:
		case TRUEBLUE:
			return RoofColor.GREEN;
		case NONE:
		default:
			assert false;//unexpected
			return RoofColor.NONE;
		}
	}
	
	//hard coded correspondance between category, color and wall specifications
	static BuildingWallsSpecification getOneBuildingDefinition(BuildingCategory category, RoofColor roofColor) {
		switch (category) {
		case CHURCH_CASTLE:
			switch (roofColor) {
			case BLACK:
				return BuildingWallsSpecification.CHURCH_CASTLE_BLACKROOF;
			case RED:
				return BuildingWallsSpecification.CHURCH_CASTLE_REDROOF;
			case GREEN:
				return BuildingWallsSpecification.CHURCH_CASTLE_GREENROOF;
			case GREY:
			default:
				return BuildingWallsSpecification.CHURCH_CASTLE_GREYROOF;
			}
		case GENERIC:
		default:
			switch (roofColor) {
			case BLACK:
				return BuildingWallsSpecification.GENERIC_BLACKROOF_STONE;
			case RED:
				if (Math.random() < 0.5) {
					return BuildingWallsSpecification.GENERIC_REDROOF_CLEARSTONE;
				} else {
					return BuildingWallsSpecification.GENERIC_REDROOF_CLEARSTONE2;
				}
			case GREEN:
				return BuildingWallsSpecification.GENERIC_GREENROOF_CLEARSTONE;
			case GREY:
			default:
				if (Math.random() < 0.5) {
					return BuildingWallsSpecification.GENERIC_GREYROOF_CLEARSTONE;
				} else {
					return BuildingWallsSpecification.GENERIC_GREYROOF_CLEARSTONE2;
				}
			}
		}
	}
	
	public BuildingWallsSpecification getOneBuildingDefinition(RoofColor roofColor) {
		return getOneBuildingDefinition(this, roofColor);
	}
}
