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

public final class FieldBlocks {
	public static final BlockDefinition SOIL = new SimpleBlockDefinition(new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6));
	public static final BlockDefinition WATER = new SimpleBlockDefinition(new Block(BlockType.WATER));
	public static final BlockDefinition CEREAL = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.WHEAT, BlockData.CROPSGENERIC.CROPGROW_MAX)
		});
	public static final BlockDefinition YELLOWFLOWER = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.YELLOW_FLOWER)
		});
	public static final BlockDefinition SUNFLOWER = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.DOUBLE_PLANT, BlockData.DOUBLEPLANT.SUNFLOWER),
			new Block(BlockType.DOUBLE_PLANT, (byte) (BlockData.DOUBLEPLANT.SUNFLOWER | BlockData.DOUBLEPLANT.TOP_HALF))
		});
	public static final BlockDefinition PLANTS = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.DOUBLE_PLANT, BlockData.DOUBLEPLANT.DOUBLE_TALLGRASS),
			new Block(BlockType.DOUBLE_PLANT, (byte) (BlockData.DOUBLEPLANT.DOUBLE_TALLGRASS | BlockData.DOUBLEPLANT.TOP_HALF))
		});
	public static final BlockDefinition SEEDS = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.WHEAT, BlockData.CROPSGENERIC.CROPGROW_0)
		});
	public static final BlockDefinition SURFACE_VEG = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.POTATOES, BlockData.CROPSGENERIC.CROPGROW_MAX)
		});
	public static final BlockDefinition GROUND_VEG = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.CARROTS, BlockData.CROPSGENERIC.CROPGROW_MAX)
		});
	public static final BlockDefinition ORCHARDTREE = new TreeBlockDefinition(TreeBlockDefinition.TreeType.ORCHARD);
	public static final BlockDefinition VINEYARDLEAF = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			Block.AIR_BLOCK,
			new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.JUNGLE | BlockData.LEAVES.NO_DECAY))
		});
	public static final BlockDefinition VINEYARDPLANT = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.DARK_OAK_FENCE),
			new Block(BlockType.LEAVES, (byte) (BlockData.LEAVES.JUNGLE | BlockData.LEAVES.NO_DECAY))
		});
	public static final BlockDefinition NUTSTREE = new TreeBlockDefinition(TreeBlockDefinition.TreeType.NUTS);
	public static final BlockDefinition OLIVESTREE = new TreeBlockDefinition(TreeBlockDefinition.TreeType.OLIVE);
	public static final BlockDefinition REEDS = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6),
			new Block(BlockType.REEDS),
			new Block(BlockType.REEDS),
			new Block(BlockType.REEDS)
		});
	public static final BlockDefinition CULTIVATEDTREE = new TreeBlockDefinition(TreeBlockDefinition.TreeType.CULTIVATED);
	
	private FieldBlocks() {
		assert false; // should not be instantiated
	}
}

