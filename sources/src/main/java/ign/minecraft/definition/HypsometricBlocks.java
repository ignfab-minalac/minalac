/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;

public enum HypsometricBlocks {
	// 16 colors palette from Minecraft (wool only for the moment, could be enhanced with other blocks representing a color)
	/*DARK(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.BLACK))),
	CLEAR(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.WHITE))),
	GREY(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.LIGHT_GRAY))),
	TRUEGREY(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.GRAY))),
	RED(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.RED))),
	BROWN(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.BROWN))),
	YELLOWISH(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.ORANGE))),
	TRUEYELLOW(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.YELLOW))),
	GREEN(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.LIME))),
	DARKGREEN(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.GREEN))),
	BLUEISH(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.LIGHT_BLUE))),
	CYAN(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.CYAN))),
	TRUEBLUE(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.BLUE))),
	PURPLE(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.PURPLE))),
	PINK(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.PINK))),
	MAGENTA(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.MAGENTA))),;*/

	LIME(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.LIME))),
	GREEN(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.GREEN))),
	TRUEYELLOW(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.YELLOW))),
	YELLOWISH(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.ORANGE))),
	RED(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.RED))),
	BROWN(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.BROWN))),
	BLACK(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.BLACK))),
	WHITE(new HypsometricBlockDefinition(new Block(BlockType.WOOL, BlockData.COLORS.WHITE)));

	private final HypsometricBlockDefinition blockDefinition;

	private HypsometricBlocks(HypsometricBlockDefinition blockDefinition) {
		this.blockDefinition = blockDefinition;
	}

	public HypsometricBlockDefinition get() {
		return this.blockDefinition;
	}
}