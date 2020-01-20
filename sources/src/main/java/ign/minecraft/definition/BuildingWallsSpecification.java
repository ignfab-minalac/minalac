/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Arrays;

import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;

/**
 * 
 * the following enum details random elements to generate each kind of building
 * 
 * at least must be defined one floor tile, the floor upper filling tile and the roof tile
 *
 */
public enum BuildingWallsSpecification {
	GENERIC_GREYROOF_CLEARSTONE(
				//ground tile
				new RandomBlockSpecification(new BlockType[] { BlockType.STONE, BlockType.SANDSTONE },
						new byte[][] { { BlockData.STONE.STONE }, { BlockData.SANDSTONES.SANDSTONE } }),//ground tile
				//base layer
				null,
				//floor base filling
				new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
						new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),//floor base filling

			new RandomBlockSpecification[] {
				//floor tiles
				new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
						new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
				new RandomBlockSpecification(BlockType.STAINED_GLASS, BlockData.COLORS.WHITE),
				new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
						new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
			},
				//floor upper filling
				new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
						new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),//floor upper filling
				//roof
				new RandomBlockSpecification(new BlockType[] { BlockType.STAINED_HARDENED_CLAY },
						new byte[][] { { BlockData.COLORS.LIGHT_GRAY, BlockData.COLORS.GRAY } })
		),
	GENERIC_GREYROOF_CLEARSTONE2(
				//ground tile
				new RandomBlockSpecification(new BlockType[] { BlockType.STONE, BlockType.SANDSTONE },
					new byte[][] { { BlockData.STONE.STONE }, { BlockData.SANDSTONES.SANDSTONE } }),
				//base layer
				null,
				//floor base filling
				new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
			new RandomBlockSpecification[] {
				//floor tiles
				new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
				new RandomBlockSpecification(BlockType.WOOL, BlockData.COLORS.WHITE),
				new RandomBlockSpecification(BlockType.STAINED_GLASS, BlockData.COLORS.WHITE),
				new RandomBlockSpecification(BlockType.WOOL, BlockData.COLORS.WHITE),
			},
				//floor upper filling
				new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
				//roof
				new RandomBlockSpecification(new BlockType[] { BlockType.STAINED_HARDENED_CLAY },
					new byte[][] { { BlockData.COLORS.LIGHT_GRAY, BlockData.COLORS.GRAY } })
		),
	GENERIC_BLACKROOF_STONE(
			//ground tile
			new RandomBlockSpecification(BlockType.STONE, BlockData.STONE.STONE),
			//base layer
			null,
			//floor base filling
			null,
		new RandomBlockSpecification[] {
			//floor tiles
			new RandomBlockSpecification(new BlockType[] { BlockType.STONEBRICK, BlockType.STONE }), //floor tiles
			new RandomBlockSpecification(BlockType.STONE),
			new RandomBlockSpecification(BlockType.STAINED_GLASS, BlockData.COLORS.LIGHT_GRAY),
			new RandomBlockSpecification(new BlockType[] { BlockType.STONEBRICK, BlockType.STONE }),
		},
			new RandomBlockSpecification(BlockType.STONEBRICK),//floor upper filling
			new RandomBlockSpecification(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.BLACK)//roof
		),
	GENERIC_REDROOF_CLEARSTONE(
			new RandomBlockSpecification(new BlockType[] { BlockType.STONE, BlockType.SANDSTONE },
					new byte[][] { { BlockData.STONE.STONE }, { BlockData.SANDSTONES.SANDSTONE } }),//ground tile
			null,//base layer
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),//floor base filling

		new RandomBlockSpecification[] { //floor tiles
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
			new RandomBlockSpecification(BlockType.STAINED_GLASS, BlockData.COLORS.WHITE),
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
		},

			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),//floor upper filling
			new RandomBlockSpecification(new BlockType[] { BlockType.STAINED_HARDENED_CLAY },
					new byte[][] { { BlockData.COLORS.RED }, { BlockData.COLORS.RED } })
	),
	GENERIC_REDROOF_CLEARSTONE2(
			//ground tile
			new RandomBlockSpecification(new BlockType[] { BlockType.STONE, BlockType.SANDSTONE },
				new byte[][] { { BlockData.STONE.STONE }, { BlockData.SANDSTONES.SANDSTONE } }),
			//base layer
			null,
			//floor base filling
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
				new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
		new RandomBlockSpecification[] {
			//floor tiles
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
				new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
			new RandomBlockSpecification(BlockType.WOOL, BlockData.COLORS.WHITE),
			new RandomBlockSpecification(BlockType.STAINED_GLASS, BlockData.COLORS.WHITE),
			new RandomBlockSpecification(BlockType.WOOL, BlockData.COLORS.WHITE),
		},
			//floor upper filling
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
				new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
			//roof
			new RandomBlockSpecification(new BlockType[] { BlockType.STAINED_HARDENED_CLAY },
					new byte[][] { { BlockData.COLORS.RED }, { BlockData.COLORS.RED } })
	),
	GENERIC_GREENROOF_CLEARSTONE(
			new RandomBlockSpecification(new BlockType[] { BlockType.STONE, BlockType.SANDSTONE },
					new byte[][] { { BlockData.STONE.STONE }, { BlockData.SANDSTONES.SANDSTONE } }),//ground tile
			null,//base layer
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),//floor base filling

		new RandomBlockSpecification[] { //floor tiles
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
			new RandomBlockSpecification(BlockType.STAINED_GLASS, BlockData.COLORS.WHITE),
			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),
		},

			new RandomBlockSpecification(new BlockType[] { BlockType.SANDSTONE },
					new byte[][] { { BlockData.SANDSTONES.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE } }),//floor upper filling
			new RandomBlockSpecification(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.GREEN)//roof
	),
	CHURCH_CASTLE_BLACKROOF(
			//ground tile
			new RandomBlockSpecification(new BlockType[] { BlockType.STONEBRICK },
					new byte[][] { { BlockData.STONE_BRICKS.STONE_BRICK, BlockData.STONE_BRICKS.CRACKED_STONE_BRICK } }),
			null,//base layer
			null,//floor base filling
			new RandomBlockSpecification[] {	new RandomBlockSpecification(BlockType.STONE), //floor tiles
										},
			new RandomBlockSpecification(BlockType.STONE),//floor upper filling
			new RandomBlockSpecification(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.GRAY)//roof
		),
	CHURCH_CASTLE_GREYROOF(
			//ground tile
			new RandomBlockSpecification(new BlockType[] { BlockType.STONEBRICK },
					new byte[][] { { BlockData.STONE_BRICKS.STONE_BRICK, BlockData.STONE_BRICKS.CRACKED_STONE_BRICK } }),
			null,//base layer
			null,//floor base filling
			new RandomBlockSpecification[] {	new RandomBlockSpecification(BlockType.STONE), //floor tiles
										},
			new RandomBlockSpecification(BlockType.STONE),//floor upper filling
			new RandomBlockSpecification(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.LIGHT_GRAY)//roof
		),
	CHURCH_CASTLE_GREENROOF(
			//ground tile
			new RandomBlockSpecification(new BlockType[] { BlockType.STONEBRICK },
					new byte[][] { { BlockData.STONE_BRICKS.STONE_BRICK, BlockData.STONE_BRICKS.CRACKED_STONE_BRICK } }),
			null,//base layer
			null,//floor base filling
			new RandomBlockSpecification[] {	new RandomBlockSpecification(BlockType.STONE), //floor tiles
										},
			new RandomBlockSpecification(BlockType.STONE),//floor upper filling
			new RandomBlockSpecification(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.GREEN)//roof
		),
	CHURCH_CASTLE_REDROOF(
			//ground tile
			new RandomBlockSpecification(new BlockType[] { BlockType.STONEBRICK },
					new byte[][] { { BlockData.STONE_BRICKS.STONE_BRICK, BlockData.STONE_BRICKS.CRACKED_STONE_BRICK } }),
			null,//base layer
			null,//floor base filling
			new RandomBlockSpecification[] {	new RandomBlockSpecification(BlockType.STONE), //floor tiles
										},
			new RandomBlockSpecification(BlockType.STONE),//floor upper filling
			new RandomBlockSpecification(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.RED)//roof
		),
	;

	public final RandomBlockSpecification groundTile;
	public final RandomBlockSpecification baseLayerTile;
	public final RandomBlockSpecification floorBaseFillingTile;
	public final RandomBlockSpecification[] floorTiles;
	public final RandomBlockSpecification floorUpperFillingTile;
	public final RandomBlockSpecification roofTile;

	BuildingWallsSpecification(RandomBlockSpecification groundTile, RandomBlockSpecification wallTile, RandomBlockSpecification roofTile) {
		assert groundTile != null;
		assert wallTile != null;
		assert roofTile != null;
		this.groundTile = groundTile;
		this.baseLayerTile = null;
		this.floorBaseFillingTile = null;
		this.floorTiles = new RandomBlockSpecification[1];
		this.floorTiles[0] = wallTile;
		this.floorUpperFillingTile = wallTile;
		this.roofTile = roofTile;
	}

	BuildingWallsSpecification(RandomBlockSpecification groundTile, RandomBlockSpecification baseLayerTile, RandomBlockSpecification floorBaseFillingTile,
			RandomBlockSpecification[] floorTiles, RandomBlockSpecification floorUpperFillingTile, RandomBlockSpecification roofTile) {
		assert groundTile != null;
		assert floorTiles != null;
		assert floorTiles.length > 0;
		assert floorUpperFillingTile != null;
		assert roofTile != null;
		this.groundTile = groundTile;
		this.baseLayerTile = baseLayerTile;
		this.floorBaseFillingTile = floorBaseFillingTile;
		this.floorTiles = Arrays.copyOf(floorTiles, floorTiles.length);
		this.floorUpperFillingTile = floorUpperFillingTile;
		this.roofTile = roofTile;
	}
	
	BuildingWallsSpecification(BuildingWallsSpecification copyFrom) {
		this.groundTile = copyFrom.groundTile;
		this.baseLayerTile = copyFrom.baseLayerTile;
		this.floorBaseFillingTile = copyFrom.floorBaseFillingTile;
		this.floorTiles = Arrays.copyOf(copyFrom.floorTiles, copyFrom.floorTiles.length);
		this.floorUpperFillingTile = copyFrom.floorUpperFillingTile;
		this.roofTile = copyFrom.roofTile;
	}
}