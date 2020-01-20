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

public final class VegetalBlocks {
	//trees
	public static final BlockDefinition STANDALONETREE = new TreeBlockDefinition(TreeBlockDefinition.TreeType.OAK, true);
	public static final BlockDefinition BUSHTREE = new TreeBlockDefinition(TreeBlockDefinition.TreeType.ACACIA);
	public static final BlockDefinition DECIDUOUSTREE1 = new TreeBlockDefinition(TreeBlockDefinition.TreeType.OAK);
	public static final BlockDefinition DECIDUOUSTREE2 = new TreeBlockDefinition(TreeBlockDefinition.TreeType.OAK);
	public static final BlockDefinition DECIDUOUSTREE3 = new TreeBlockDefinition(TreeBlockDefinition.TreeType.BIRCH);
	public static final BlockDefinition PINETREEHIGH = new TreeBlockDefinition(TreeBlockDefinition.TreeType.SPRUCE);
	public static final BlockDefinition PINETREELOW = new TreeBlockDefinition(TreeBlockDefinition.TreeType.PINE);
	//leaves / bushes
	public static final BlockDefinition HEDGEGROUNDLEAVES = new SimpleBlockDefinition(new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY)));
	public static final BlockDefinition HEDGEGROUNDLEAVESDOUBLE = new VerticalBlockDefinition(new Block[]{ 
											new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY)),
											new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.OAK | BlockData.LEAVES.NO_DECAY)) });
	//SimpleBlockDefinition HEDGEGROUNDLEAVESDOUBLEUPPER (HEDGEGROUNDLEAVESDOUBLE),
	public static final BlockDefinition POPLARSGROUNDLEAVES = new SimpleBlockDefinition(new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.BIRCH | BlockData.LEAVES.NO_DECAY)));
	public static final BlockDefinition FORESTGROUNDLEAVES = new SimpleBlockDefinition(new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.SPRUCE | BlockData.LEAVES.NO_DECAY)));
	public static final BlockDefinition FORESTGROUNDLEAVESDOUBLE = new VerticalBlockDefinition(new Block[]{
											new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.SPRUCE | BlockData.LEAVES.NO_DECAY)),
											new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.SPRUCE | BlockData.LEAVES.NO_DECAY)) });
	//public static final BlockDefinition FORESTGROUNDLEAVESDOUBLEUPPER (FORESTGROUNDLEAVESDOUBLE),
	public static final BlockDefinition FORESTGROUNDLEAVES2 = new SimpleBlockDefinition(new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.JUNGLE | BlockData.LEAVES.NO_DECAY)));
	public static final BlockDefinition BUSHGROUNDLEAVES = new SimpleBlockDefinition(new Block(BlockType.LEAVES2, (byte) (BlockData.LEAVES2.ACACIA | BlockData.LEAVES2.NO_DECAY)));
	public static final BlockDefinition BUSHGROUNDLEAVESDOUBLE = new VerticalBlockDefinition(new Block[]{
											new Block(BlockType.LEAVES2, (byte) (BlockData.LEAVES2.ACACIA | BlockData.LEAVES2.NO_DECAY)),
											new Block(BlockType.LEAVES2, (byte) (BlockData.LEAVES2.ACACIA | BlockData.LEAVES2.NO_DECAY)) });
	//other vegetation
	public static final BlockDefinition VEGETATION1 = new SimpleBlockDefinition(new Block(BlockType.TALLGRASS, BlockData.TALLGRASS.FERN));
	public static final BlockDefinition VEGETATION1DOUBLE = new VerticalBlockDefinition(new Block[]{
											new Block(BlockType.DOUBLE_PLANT, BlockData.DOUBLEPLANT.LARGE_FERN),
											new Block(BlockType.DOUBLE_PLANT, (byte) (BlockData.DOUBLEPLANT.LARGE_FERN | BlockData.DOUBLEPLANT.TOP_HALF)) });
	public static final BlockDefinition VEGETATION2 = new SimpleBlockDefinition(new Block(BlockType.TALLGRASS, BlockData.TALLGRASS.TALL_GRASS));
	public static final BlockDefinition VEGETATION2DOUBLE =
											new VerticalBlockDefinition(new Block[]{ new Block(BlockType.DOUBLE_PLANT, BlockData.DOUBLEPLANT.DOUBLE_TALLGRASS),
											new Block(BlockType.DOUBLE_PLANT, (byte) (BlockData.DOUBLEPLANT.DOUBLE_TALLGRASS | BlockData.DOUBLEPLANT.TOP_HALF)) });
	
	private VegetalBlocks() {
		assert false; // should not be instantiated
	}
}

