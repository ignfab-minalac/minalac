package developpeur2000.minecraft.minecraft_rw.world;

/**
 * Enumeration of block types as of Minecraft version {@code 14w33c}.
 */
public enum MapItemColor {
	
	//TODO: enumerate orientations of logs, as just wood type in data will miss blocks with orientation with other values than 0
	
	TRANSPARENT(new int[] {255,255,255}, new BlockSpec[] {
			new BlockSpec(BlockType.GLASS), new BlockSpec(BlockType.GOLDEN_RAIL), new BlockSpec(BlockType.DETECTOR_RAIL),
			new BlockSpec(BlockType.TORCH), new BlockSpec(BlockType.REDSTONE_WIRE), new BlockSpec(BlockType.LADDER),
			new BlockSpec(BlockType.RAIL), new BlockSpec(BlockType.LEVER), new BlockSpec(BlockType.REDSTONE_TORCH),
			new BlockSpec(BlockType.WOODEN_BUTTON), new BlockSpec(BlockType.STONE_BUTTON), new BlockSpec(BlockType.TRIPWIRE_HOOK),
			new BlockSpec(BlockType.PORTAL), new BlockSpec(BlockType.CAKE), new BlockSpec(BlockType.UNPOWERED_REPEATER),
			new BlockSpec(BlockType.POWERED_REPEATER), new BlockSpec(BlockType.UNPOWERED_COMPARATOR), new BlockSpec(BlockType.POWERED_COMPARATOR),
			new BlockSpec(BlockType.GLASS_PANE), new BlockSpec(BlockType.END_PORTAL), new BlockSpec(BlockType.FLOWER_POT),
			new BlockSpec(BlockType.REDSTONE_LAMP), new BlockSpec(BlockType.BARRIER), new BlockSpec(BlockType.SKULL),
			new BlockSpec(BlockType.AIR) }),
	BASECOLOR_1(new int[] {125,176,55}, new BlockSpec[] { new BlockSpec(BlockType.GRASS), new BlockSpec(BlockType.SLIME_BLOCK) }),
	BASECOLOR_2(new int[] {244,230,161}, new BlockSpec[] {
			new BlockSpec(BlockType.SAND, BlockData.SAND.SAND), new BlockSpec(BlockType.SANDSTONE, BlockData.SANDSTONES.SANDSTONE), new BlockSpec(BlockType.SANDSTONE_STAIRS),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.SANDSTONE & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.SANDSTONE & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.SANDSTONE),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.SMOOTH_SANDSTONE_DOUBLEONLY),
			new BlockSpec(BlockType.SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE), new BlockSpec(BlockType.SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE),
			new BlockSpec(BlockType.GLOWSTONE), new BlockSpec(BlockType.END_STONE), new BlockSpec(BlockType.LOG, BlockData.LOG.BIRCH),
			new BlockSpec(BlockType.PLANKS, BlockData.WOODPLANKS.BIRCH), new BlockSpec(BlockType.BIRCH_FENCE),
			new BlockSpec(BlockType.BIRCH_FENCE_GATE), new BlockSpec(BlockType.BIRCH_STAIRS),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.BIRCH & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.BIRCH & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_WOODEN_SLAB, BlockData.WOODENSLABS.BIRCH) }),
	BASECOLOR_3_DEFAULT(new int[] {197,197,197}, new BlockSpec[] { }),//"other" color
	BASECOLOR_4(new int[] {252,0,0}, new BlockSpec[] {
			new BlockSpec(BlockType.LAVA), new BlockSpec(BlockType.TNT),
			new BlockSpec(BlockType.REDSTONE_BLOCK),new BlockSpec(BlockType.FIRE) }),
	BASECOLOR_5(new int[] {158,158,252}, new BlockSpec[] { new BlockSpec(BlockType.ICE), new BlockSpec(BlockType.PACKED_ICE) }),
	BASECOLOR_6(new int[] {165,165,165}, new BlockSpec[] {
			new BlockSpec(BlockType.IRON_BLOCK), new BlockSpec(BlockType.IRON_DOOR), new BlockSpec(BlockType.IRON_TRAPDOOR),
			new BlockSpec(BlockType.IRON_BARS), new BlockSpec(BlockType.BREWING_STAND),
			new BlockSpec(BlockType.ANVIL), new BlockSpec(BlockType.HEAVY_WEIGHTED_PRESSURE_PLATE) }),
	BASECOLOR_7(new int[] {0,123,0}, new BlockSpec[] {
			new BlockSpec(BlockType.SAPLING), new BlockSpec(BlockType.LEAVES), new BlockSpec(BlockType.LEAVES2),
			new BlockSpec(BlockType.TALLGRASS), new BlockSpec(BlockType.DEADBUSH), new BlockSpec(BlockType.RED_FLOWER), new BlockSpec(BlockType.YELLOW_FLOWER),
			new BlockSpec(BlockType.BROWN_MUSHROOM), new BlockSpec(BlockType.RED_MUSHROOM),
			new BlockSpec(BlockType.WHEAT), new BlockSpec(BlockType.CACTUS), new BlockSpec(BlockType.REEDS),
			new BlockSpec(BlockType.PUMPKIN_STEM), new BlockSpec(BlockType.MELON_STEM), new BlockSpec(BlockType.VINE), new BlockSpec(BlockType.WATERLILY) }),
	BASECOLOR_8(new int[] {252,252,252}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.WHITE), new BlockSpec(BlockType.CARPET, BlockData.COLORS.WHITE),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.WHITE), new BlockSpec(BlockType.SNOW), new BlockSpec(BlockType.SNOW_LAYER) }),
	BASECOLOR_9(new int[] {162,166,182}, new BlockSpec[] { new BlockSpec(BlockType.CLAY), new BlockSpec(BlockType.MONSTER_EGG) }),
	BASECOLOR_10(new int[] {149,108,76}, new BlockSpec[] {
			new BlockSpec(BlockType.DIRT, BlockData.DIRT.DIRT), new BlockSpec(BlockType.DIRT, BlockData.DIRT.COARSE_DIRT), new BlockSpec(BlockType.STONE, BlockData.STONE.GRANITE),
			new BlockSpec(BlockType.STONE, BlockData.STONE.POLISHED_GRANITE), new BlockSpec(BlockType.FARMLAND),
			new BlockSpec(BlockType.SAND, BlockData.SAND.RED_SAND), new BlockSpec(BlockType.RED_SANDSTONE, BlockData.SANDSTONES.SANDSTONE), new BlockSpec(BlockType.RED_SANDSTONE_STAIRS),
			new BlockSpec(BlockType.STONE_SLAB2, (byte) (BlockData.STONESLABS2.REDSTONE & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.STONE_SLAB2, (byte) (BlockData.STONESLABS2.REDSTONE & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB2, BlockData.STONESLABS2.REDSTONE),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB2, BlockData.STONESLABS2.SMOOTH_REDSTONE_DOUBLEONLY),
			new BlockSpec(BlockType.RED_SANDSTONE, BlockData.SANDSTONES.CHISELED_SANDSTONE), new BlockSpec(BlockType.RED_SANDSTONE, BlockData.SANDSTONES.SMOOTH_SANDSTONE),
			new BlockSpec(BlockType.COMMAND_BLOCK), new BlockSpec(BlockType.LOG, BlockData.LOG.JUNGLE),
			new BlockSpec(BlockType.PLANKS, BlockData.WOODPLANKS.JUNGLE), new BlockSpec(BlockType.JUNGLE_FENCE),
			new BlockSpec(BlockType.JUNGLE_FENCE_GATE), new BlockSpec(BlockType.JUNGLE_STAIRS),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.JUNGLE & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.JUNGLE & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_WOODEN_SLAB, BlockData.WOODENSLABS.JUNGLE) }),
	BASECOLOR_11(new int[] {111,111,111}, new BlockSpec[] { new BlockSpec(BlockType.STONE, BlockData.STONE.STONE),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.STONE & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.STONE & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.STONE),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.SMOOTH_STONE_DOUBLEONLY), new BlockSpec(BlockType.COBBLESTONE_WALL),
			new BlockSpec(BlockType.STONE_STAIRS),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.COBBLESTONE & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.COBBLESTONE & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.COBBLESTONE),
			new BlockSpec(BlockType.MOSSY_COBBLESTONE), new BlockSpec(BlockType.STONE, BlockData.STONE.ANDESITE), new BlockSpec(BlockType.STONE, BlockData.STONE.POLISHED_ANDESITE),
			new BlockSpec(BlockType.BEDROCK), new BlockSpec(BlockType.GOLD_ORE), new BlockSpec(BlockType.IRON_ORE), new BlockSpec(BlockType.COAL_ORE),
			new BlockSpec(BlockType.LAPIS_ORE), new BlockSpec(BlockType.DISPENSER), new BlockSpec(BlockType.DROPPER), new BlockSpec(BlockType.STICKY_PISTON),
			new BlockSpec(BlockType.PISTON), new BlockSpec(BlockType.PISTON_HEAD), new BlockSpec(BlockType.PISTON_EXTENSION), new BlockSpec(BlockType.MOB_SPAWNER),
			new BlockSpec(BlockType.DIAMOND_ORE), new BlockSpec(BlockType.FURNACE), new BlockSpec(BlockType.LIT_FURNACE), new BlockSpec(BlockType.STONE_PRESSURE_PLATE),
			new BlockSpec(BlockType.REDSTONE_ORE), new BlockSpec(BlockType.STONEBRICK), new BlockSpec(BlockType.STONE_BRICK_STAIRS),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.STONE_BRICK & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.STONE_BRICK & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.STONE_BRICK),
			new BlockSpec(BlockType.ENDER_CHEST), new BlockSpec(BlockType.HOPPER), new BlockSpec(BlockType.GRAVEL) }),
	BASECOLOR_12(new int[] {63,63,252}, new BlockSpec[] { new BlockSpec(BlockType.WATER), new BlockSpec(BlockType.FLOWING_WATER) }),
	BASECOLOR_13(new int[] {141,118,71}, new BlockSpec[] {
			new BlockSpec(BlockType.LOG, BlockData.LOG.OAK),
			new BlockSpec(BlockType.PLANKS, BlockData.WOODPLANKS.OAK), new BlockSpec(BlockType.FENCE),
			new BlockSpec(BlockType.FENCE_GATE), new BlockSpec(BlockType.OAK_STAIRS),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.OAK & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.OAK & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_WOODEN_SLAB, BlockData.WOODENSLABS.OAK), new BlockSpec(BlockType.NOTEBLOCK), new BlockSpec(BlockType.BOOKSHELF),
			new BlockSpec(BlockType.CHEST), new BlockSpec(BlockType.TRAPPED_CHEST), new BlockSpec(BlockType.CRAFTING_TABLE), new BlockSpec(BlockType.WOODEN_DOOR),
			new BlockSpec(BlockType.STANDING_SIGN), new BlockSpec(BlockType.WALL_SIGN), new BlockSpec(BlockType.WOODEN_PRESSURE_PLATE), new BlockSpec(BlockType.JUKEBOX), new BlockSpec(BlockType.TRAPDOOR),
			new BlockSpec(BlockType.BROWN_MUSHROOM_BLOCK), new BlockSpec(BlockType.STANDING_BANNER), new BlockSpec(BlockType.WALL_BANNER),
			new BlockSpec(BlockType.DAYLIGHT_DETECTOR), new BlockSpec(BlockType.DAYLIGHT_DETECTOR_INVERTED) }),
	BASECOLOR_14(new int[] {252,249,242}, new BlockSpec[] { new BlockSpec(BlockType.QUARTZ_BLOCK),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.QUARTZ & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.QUARTZ & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.QUARTZ),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.TILE_QUARTZ_DOUBLEONLY),
			new BlockSpec(BlockType.QUARTZ_STAIRS), new BlockSpec(BlockType.STONE, BlockData.STONE.DIORITE), new BlockSpec(BlockType.STONE, BlockData.STONE.POLISHED_DIORITE),
			new BlockSpec(BlockType.SEA_LANTERN) }),
	BASECOLOR_15(new int[] {213,125,50}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.ORANGE), new BlockSpec(BlockType.CARPET, BlockData.COLORS.ORANGE),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.ORANGE),
			new BlockSpec(BlockType.PUMPKIN), new BlockSpec(BlockType.LIT_PUMPKIN), new BlockSpec(BlockType.HARDENED_CLAY),
			new BlockSpec(BlockType.LOG2, BlockData.LOG2.ACACIA),
			new BlockSpec(BlockType.PLANKS, BlockData.WOODPLANKS.ACACIA), new BlockSpec(BlockType.ACACIA_FENCE),
			new BlockSpec(BlockType.ACACIA_FENCE_GATE), new BlockSpec(BlockType.ACACIA_STAIRS),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.ACACIA & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.ACACIA & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_WOODEN_SLAB, BlockData.WOODENSLABS.ACACIA) }),
	BASECOLOR_16(new int[] {176,75,213}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.MAGENTA), new BlockSpec(BlockType.CARPET, BlockData.COLORS.MAGENTA),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.MAGENTA) }),
	BASECOLOR_17(new int[] {101,151,213}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.LIGHT_BLUE), new BlockSpec(BlockType.CARPET, BlockData.COLORS.LIGHT_BLUE),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.LIGHT_BLUE) }),
	BASECOLOR_18(new int[] {226,226,50}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.YELLOW), new BlockSpec(BlockType.CARPET, BlockData.COLORS.YELLOW),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.YELLOW),
			new BlockSpec(BlockType.HAY_BLOCK), new BlockSpec(BlockType.SPONGE) }),
	BASECOLOR_19(new int[] {125,202,25}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.LIME), new BlockSpec(BlockType.CARPET, BlockData.COLORS.LIME),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.LIME), new BlockSpec(BlockType.MELON_BLOCK) }),
	BASECOLOR_20(new int[] {239,125,163}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.PINK), new BlockSpec(BlockType.CARPET, BlockData.COLORS.PINK),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.PINK) }),
	BASECOLOR_21(new int[] {75,75,75}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.GRAY), new BlockSpec(BlockType.CARPET, BlockData.COLORS.GRAY),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.GRAY), new BlockSpec(BlockType.CAULDRON) }),
	BASECOLOR_22(new int[] {151,151,151}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.LIGHT_GRAY), new BlockSpec(BlockType.CARPET, BlockData.COLORS.LIGHT_GRAY),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.LIGHT_GRAY) }),
	BASECOLOR_23(new int[] {75,125,151}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.CYAN), new BlockSpec(BlockType.CARPET, BlockData.COLORS.CYAN),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.CYAN), new BlockSpec(BlockType.PRISMARINE, BlockData.PRISMARINE.PRISMARINE) }),
	BASECOLOR_24(new int[] {125,62,176}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.PURPLE), new BlockSpec(BlockType.CARPET, BlockData.COLORS.PURPLE),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.PURPLE), new BlockSpec(BlockType.MYCELIUM) }),
	BASECOLOR_25(new int[] {50,75,176}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.BLUE), new BlockSpec(BlockType.CARPET, BlockData.COLORS.BLUE),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.BLUE) }),
	BASECOLOR_26(new int[] {101,75,50}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.BROWN), new BlockSpec(BlockType.CARPET, BlockData.COLORS.BROWN),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.BROWN), new BlockSpec(BlockType.LOG2, BlockData.LOG2.DARKOAK),
			new BlockSpec(BlockType.PLANKS, BlockData.WOODPLANKS.DARKOAK), new BlockSpec(BlockType.DARK_OAK_FENCE),
			new BlockSpec(BlockType.DARK_OAK_FENCE_GATE), new BlockSpec(BlockType.DARK_OAK_STAIRS),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.DARK_OAK & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.DARK_OAK & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_WOODEN_SLAB, BlockData.WOODENSLABS.DARK_OAK) }),
	BASECOLOR_27(new int[] {101,125,50}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.GREEN), new BlockSpec(BlockType.CARPET, BlockData.COLORS.GREEN),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.GREEN), new BlockSpec(BlockType.END_PORTAL_FRAME) }),
	BASECOLOR_28(new int[] {151,50,50}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.RED), new BlockSpec(BlockType.CARPET, BlockData.COLORS.RED),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.RED),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.BRICKS & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.STONE_SLAB, (byte) (BlockData.STONESLABS.BRICKS & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_STONE_SLAB, BlockData.STONESLABS.BRICKS),
			new BlockSpec(BlockType.BRICK_BLOCK), new BlockSpec(BlockType.BRICK_STAIRS),
			new BlockSpec(BlockType.RED_MUSHROOM_BLOCK), new BlockSpec(BlockType.ENCHANTING_TABLE) }),
	BASECOLOR_29(new int[] {25,25,25}, new BlockSpec[] {
			new BlockSpec(BlockType.WOOL, BlockData.COLORS.BLACK), new BlockSpec(BlockType.CARPET, BlockData.COLORS.BLACK),
			new BlockSpec(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.BLACK), new BlockSpec(BlockType.DRAGON_EGG),
			new BlockSpec(BlockType.COAL_BLOCK), new BlockSpec(BlockType.OBSIDIAN) }),
	BASECOLOR_30(new int[] {247,235,76}, new BlockSpec[] {
			new BlockSpec(BlockType.GOLD_BLOCK), new BlockSpec(BlockType.LIGHT_WEIGHTED_PRESSURE_PLATE) }),
	BASECOLOR_31(new int[] {91,216,210}, new BlockSpec[] {
			new BlockSpec(BlockType.DIAMOND_BLOCK), new BlockSpec(BlockType.PRISMARINE, BlockData.PRISMARINE.PRISMARINE_BRICKS),
			new BlockSpec(BlockType.PRISMARINE, BlockData.PRISMARINE.DARK_PRISMARINE), new BlockSpec(BlockType.BEACON) }),
	BASECOLOR_32(new int[] {73,129,252}, new BlockSpec[] {
			new BlockSpec(BlockType.LAPIS_BLOCK) }),
	BASECOLOR_33(new int[] {0,214,57}, new BlockSpec[] {
			new BlockSpec(BlockType.EMERALD_BLOCK) }),
	BASECOLOR_34(new int[] {127,85,48}, new BlockSpec[] {
			new BlockSpec(BlockType.DIRT, BlockData.DIRT.PODZOL),
			new BlockSpec(BlockType.LOG, BlockData.LOG.OAK),
			new BlockSpec(BlockType.PLANKS, BlockData.WOODPLANKS.OAK), new BlockSpec(BlockType.FENCE),
			new BlockSpec(BlockType.FENCE_GATE), new BlockSpec(BlockType.OAK_STAIRS),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.OAK & BlockData.SLABS.RIGHT_SIDE_UP)),
			new BlockSpec(BlockType.WOODEN_SLAB, (byte) (BlockData.WOODENSLABS.OAK & BlockData.SLABS.UPSIDE_DOWN)),
			new BlockSpec(BlockType.DOUBLE_WOODEN_SLAB, BlockData.WOODENSLABS.OAK)
			}),
	BASECOLOR_35(new int[] {111,2,0}, new BlockSpec[] {
			new BlockSpec(BlockType.NETHERRACK), new BlockSpec(BlockType.QUARTZ_ORE), new BlockSpec(BlockType.NETHER_WART),
			new BlockSpec(BlockType.NETHER_BRICK), new BlockSpec(BlockType.NETHER_BRICK_FENCE), new BlockSpec(BlockType.NETHER_BRICK_STAIRS) }),
	;
	
	private static class BlockSpec {
		static final byte DATA_ALL = -1;
		
		private final BlockType type;
		private final byte data;
		
		BlockSpec(BlockType type) {
			this(type, DATA_ALL);
		}
		BlockSpec(BlockType type, byte data) {
			this.type = type;
			this.data = data;
		}
		
		@Override
		public boolean equals(Object other) {
			if (BlockSpec.class.isAssignableFrom(other.getClass())) {
				BlockSpec otherBlockSpec = (BlockSpec) other;
				return otherBlockSpec.type == this.type && otherBlockSpec.data == this.data;
			}
			if (Block.class.isAssignableFrom(other.getClass())) {
				Block otherBlock = (Block) other;
				return otherBlock.getType() == this.type && (this.data == DATA_ALL || otherBlock.getData() == this.data);
			}
			return false;
		}
	}
	
	private final byte[] rgb = new byte[3];
	private final BlockSpec[] blocks;
	
	private MapItemColor(int[] rgb, BlockSpec[] blocks) {
		assert rgb.length == 3;
		for (int i = 0; i < 3; i ++) {
			assert rgb[i] >= 0;
			assert rgb[i] <= 0xff;
			this.rgb[i] = (byte) rgb[i];
		}
		this.blocks = blocks;
	}
	
	public byte[] getRGB() {
		return rgb;
	}
	
	public byte getColorByte() {
		//TODO : take in account the variant of the color
		// for the moment, only work with base colors
		return (byte) (this.ordinal() * 4 + 2);
	}
	
	static public byte getColor(Block block, int y) {
		MapItemColor baseColor = null;
		for (MapItemColor colorSpec : MapItemColor.values()) {
			for (BlockSpec blockSpec : colorSpec.blocks) {
				if (blockSpec.equals(block)) {
					baseColor = colorSpec;
					break;
				}
			}
			if (baseColor != null) {
				break;
			}
		}
		
		if (baseColor == null) {
			baseColor = BASECOLOR_3_DEFAULT;
		}
		
		return baseColor.getColorByte();
	}

}
