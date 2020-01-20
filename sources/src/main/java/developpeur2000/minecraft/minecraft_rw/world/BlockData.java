package developpeur2000.minecraft.minecraft_rw.world;

/**
 * Block data associated to different blocks and items as of Minecraft version {@code 14w33c}.
 * 
 */
public final class BlockData {
	public final static byte NONE = 0x00;
	
	public final class WOODPLANKS {
		public final static byte OAK = 0x00;
		public final static byte SPRUCE = 0x01;
		public final static byte BIRCH = 0x02;
		public final static byte JUNGLE = 0x03;
		public final static byte ACACIA = 0x04;
		public final static byte DARKOAK = 0x05;
	}
	public final class STONE {
		public final static byte STONE = 0x00;
		public final static byte GRANITE = 0x01;
		public final static byte POLISHED_GRANITE = 0x02;
		public final static byte DIORITE = 0x03;
		public final static byte POLISHED_DIORITE = 0x04;
		public final static byte ANDESITE = 0x05;
		public final static byte POLISHED_ANDESITE = 0x06;
	}
	public final class DIRT {
		public final static byte DIRT = 0x00;
		public final static byte COARSE_DIRT = 0x01;
		public final static byte PODZOL = 0x02;
	}
	public final class SAPLINGS {
		public final static byte OAK = 0x00;
		public final static byte SPRUCE = 0x01;
		public final static byte BIRCH = 0x02;
		public final static byte JUNGLE = 0x03;
		public final static byte ACACIA = 0x04;
		public final static byte DARKOAK = 0x05;
		public final static byte TREETYPE_FILTER = 0x05;
		public final static byte READY_TO_GROW = 0x08;
	}
	public final class FLOWING { //for FLOWING_WATER and FLOWING_LAVA
		public final static byte LEVEL_HIGHEST = 0x00;
		public final static byte LEVEL_7 = 0x01;
		public final static byte LEVEL_6 = 0x02;
		public final static byte LEVEL_5 = 0x03;
		public final static byte LEVEL_4 = 0x04;
		public final static byte LEVEL_3 = 0x05;
		public final static byte LEVEL_2 = 0x06;
		public final static byte LEVEL_1 = 0x07;
		public final static byte LEVEL_FALLING = 0x08;
	}
	public final class SAND {
		public final static byte SAND = 0x00;
		public final static byte RED_SAND = 0x01;
	}
	public final class LOG {
		public final static byte OAK = 0x00;
		public final static byte SPRUCE = 0x01;
		public final static byte BIRCH = 0x02;
		public final static byte JUNGLE = 0x03;
		public final static byte ORIENTATION_UD = 0x00;
		public final static byte ORIENTATION_EW = 0x04;
		public final static byte ORIENTATION_NS = 0x08;
		public final static byte ORIENTATION_BARK = 0x0C;
	}
	public final class LOG2 {
		public final static byte ACACIA = 0x00;
		public final static byte DARKOAK = 0x01;
		public final static byte ORIENTATION_UD = 0x00;
		public final static byte ORIENTATION_EW = 0x04;
		public final static byte ORIENTATION_NS = 0x08;
		public final static byte ORIENTATION_BARK = 0x0C;
	}
	public final class LEAVES {
		public final static byte OAK = 0x00;
		public final static byte SPRUCE = 0x01;
		public final static byte BIRCH = 0x02;
		public final static byte JUNGLE = 0x03;
		public final static byte NO_DECAY = 0x04;
		public final static byte CHECK_DECAY = 0x08;
	}
	public final class LEAVES2 {
		public final static byte ACACIA = 0x00;
		public final static byte DARKOAK = 0x01;
		public final static byte NO_DECAY = 0x04;
		public final static byte CHECK_DECAY = 0x08;
	}

	public final class COLORS { // for WOOL, STAINED CLAY, STAINED GLASS and CARPET
		public final static byte WHITE = 0x00;
		public final static byte ORANGE = 0x01;
		public final static byte MAGENTA = 0x02;
		public final static byte LIGHT_BLUE = 0x03;
		public final static byte YELLOW = 0x04;
		public final static byte LIME = 0x05;
		public final static byte PINK = 0x06;
		public final static byte GRAY = 0x07;
		public final static byte LIGHT_GRAY = 0x08;
		public final static byte CYAN = 0x09;
		public final static byte PURPLE = 0x0A;
		public final static byte BLUE = 0x0B;
		public final static byte BROWN = 0x0C;
		public final static byte GREEN = 0x0D;
		public final static byte RED = 0x0E;
		public final static byte BLACK = 0x0F;
	}
	public final class TORCHES { // also for REDSTONE TORCHES
		public final static byte FACING_EAST = 0x01;
		public final static byte FACING_WEST = 0x02;
		public final static byte FACING_SOUTH = 0x03;
		public final static byte FACING_NORTH = 0x04;
		public final static byte FACING_UP = 0x05;
	}
	public final class SLABS {
		public final static byte RIGHT_SIDE_UP = 0x00;
		public final static byte UPSIDE_DOWN = 0x08;
	}
	public final class STONESLABS { /* common to stoneslab and double stoneslab */
		public final static byte STONE = 0x00;
		public final static byte SANDSTONE = 0x01;
		public final static byte WOODEN = 0x02;
		public final static byte COBBLESTONE = 0x03;
		public final static byte BRICKS = 0x04;
		public final static byte STONE_BRICK = 0x05;
		public final static byte NETHER_BRICK = 0x06;
		public final static byte QUARTZ = 0x07;
		public final static byte SMOOTH_STONE_DOUBLEONLY = 0x08;
		public final static byte SMOOTH_SANDSTONE_DOUBLEONLY = 0x09;
		public final static byte TILE_QUARTZ_DOUBLEONLY = 0x07;
	}
	public final class STONESLABS2 { /* common to stoneslab2 and double stoneslab2 */
		public final static byte REDSTONE = 0x00;
		public final static byte SMOOTH_REDSTONE_DOUBLEONLY = 0x08;
	}
	public final class WOODENSLABS { /* common to woodenslab and double woodenslab */
		public final static byte OAK = 0x00;
		public final static byte SPRUCE = 0x01;
		public final static byte BIRCH = 0x02;
		public final static byte JUNGLE = 0x03;
		public final static byte ACACIA = 0x04;
		public final static byte DARK_OAK = 0x05;
	}
	public final class SANDSTONES { /* common to sandstone and red sandstone */
		public final static byte SANDSTONE = 0x00;
		public final static byte CHISELED_SANDSTONE = 0x01;
		public final static byte SMOOTH_SANDSTONE = 0x02;
	}
	public final class BED {
		public final static byte FACING_SOUTH = 0x00;
		public final static byte FACING_WEST = 0x01;
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_EAST = 0x03;
		public final static byte EMPTY = 0x00;
		public final static byte OCCUPIED = 0x04;
		public final static byte FOOT = 0x00;
		public final static byte HEAD = 0x08;
	}
	public final class TALLGRASS {
		public final static byte SHRUB = 0x00;
		public final static byte TALL_GRASS = 0x01;
		public final static byte FERN = 0x02;
	}
	public final class REDFLOWER {
		public final static byte POPPY = 0x00;
		public final static byte BLUE_ORCHID = 0x01;
		public final static byte ALLIUM = 0x02;
		public final static byte AZURE_BLUET = 0x03;
		public final static byte RED_TULIP = 0x04;
		public final static byte ORANGE_TULIP = 0x05;
		public final static byte WHITE_TULIP = 0x06;
		public final static byte PINK_TULIP = 0x07;
		public final static byte OXEYE_DAISY = 0x08;
	}
	public final class DOUBLEPLANT {
		public final static byte SUNFLOWER = 0x00;
		public final static byte LILAC = 0x01;
		public final static byte DOUBLE_TALLGRASS = 0x02;
		public final static byte LARGE_FERN = 0x03;
		public final static byte ROSE_BUSH = 0x04;
		public final static byte PEONY = 0x05;
		public final static byte TOP_HALF = 0x08;
	}
	public final class PISTONS { /* common to piston and piston extension */
		public final static byte HEAD_DOWN = 0x00;
		public final static byte HEAD_UP = 0x01;
		public final static byte HEAD_NORTH = 0x02;
		public final static byte HEAD_SOUTH = 0x03;
		public final static byte HEAD_WEST = 0x04;
		public final static byte HEAD_EAST = 0x05;
	}
	public final class PISTON {
		public final static byte RETRACTED = 0x00;
		public final static byte PUSHED = 0x08;
	}
	public final class PISTONEXTENSION {
		public final static byte REGULAR = 0x00;
		public final static byte STICKY = 0x08;
	}
	public final class STAIRS {
		public final static byte FULLBLOCK_SIDE_EAST = 0x00;
		public final static byte FULLBLOCK_SIDE_WEST = 0x01;
		public final static byte FULLBLOCK_SIDE_SOUTH = 0x02;
		public final static byte FULLBLOCK_SIDE_NORTH = 0x03;
		public final static byte UPSIDE_DOWN = 0x04;
	}
	public final class REDSTONEPOWER { /* common to redstone wire, daylight sensor */
		public final static byte REDSTONEPOWER_NONE = 0x00;
		public final static byte REDSTONEPOWER_1 = 0x01;
		public final static byte REDSTONEPOWER_2 = 0x02;
		public final static byte REDSTONEPOWER_3 = 0x03;
		public final static byte REDSTONEPOWER_4 = 0x04;
		public final static byte REDSTONEPOWER_5 = 0x05;
		public final static byte REDSTONEPOWER_6 = 0x06;
		public final static byte REDSTONEPOWER_7 = 0x07;
		public final static byte REDSTONEPOWER_8 = 0x08;
		public final static byte REDSTONEPOWER_9 = 0x09;
		public final static byte REDSTONEPOWER_10 = 0x0A;
		public final static byte REDSTONEPOWER_11 = 0x0B;
		public final static byte REDSTONEPOWER_12 = 0x0C;
		public final static byte REDSTONEPOWER_13 = 0x0D;
		public final static byte REDSTONEPOWER_14 = 0x0E;
		public final static byte REDSTONEPOWER_MAX = 0x0F;
	}
	public final class CROPSGENERIC { /* common to wheat, carrot and potato */
		public final static byte CROPGROW_0 = 0x00;
		public final static byte CROPGROW_1 = 0x01;
		public final static byte CROPGROW_2 = 0x02;
		public final static byte CROPGROW_3 = 0x03;
		public final static byte CROPGROW_4 = 0x04;
		public final static byte CROPGROW_5 = 0x05;
		public final static byte CROPGROW_6 = 0x06;
		public final static byte CROPGROW_MAX = 0x07;
	}
	public final class BEETROOT {
		public final static byte CROPGROW_0 = 0x00;
		public final static byte CROPGROW_1 = 0x01;
		public final static byte CROPGROW_2 = 0x02;
		public final static byte CROPGROW_MAX = 0x03;
	}
	public final class FARMLAND {
		public final static byte FARMLAND_DRY = 0x00;
		public final static byte FARMLAND_WET_1 = 0x01;
		public final static byte FARMLAND_WET_2 = 0x02;
		public final static byte FARMLAND_WET_3 = 0x03;
		public final static byte FARMLAND_WET_4 = 0x04;
		public final static byte FARMLAND_WET_5 = 0x05;
		public final static byte FARMLAND_WET_6 = 0x06;
		public final static byte FARMLAND_WET_MAX = 0x07;
	}
	public final class STANDING_BANNER_SIGN {
		public final static byte SOUTH = 0x00;
		public final static byte SOUTH_SOUTHWEST = 0x01;
		public final static byte SOUTHWEST = 0x02;
		public final static byte WEST_SOUTHWEST = 0x03;
		public final static byte WEST = 0x04;
		public final static byte WEST_NORTHWEST = 0x05;
		public final static byte NORTHWEST = 0x06;
		public final static byte NORTH_NORTHWEST = 0x07;
		public final static byte NORTH = 0x08;
		public final static byte NORTH_NORTHEAST = 0x09;
		public final static byte NORTHEAST = 0x0A;
		public final static byte EAST_NORTHEAST = 0x0B;
		public final static byte EAST = 0x0C;
		public final static byte EAST_SOUTHEAST = 0x0D;
		public final static byte SOUTHEAST = 0x0E;
		public final static byte SOUTH_SOUTHEAST = 0x0F;
	}
	public final class WALL_BANNER_SIGN {
		public final static byte NORTH = 0x02;
		public final static byte SOUTH = 0x03;
		public final static byte WEST = 0x04;
		public final static byte EAST = 0x05;
	}
	public final class DOORS {
		public final static byte BOTTOM_HALF = 0x00;
		public final static byte TOP_HALF = 0x08;
		public final static byte TOPHALFONLY_HINGE_RIGHT = 0x00;
		public final static byte TOPHALFONLY_HINGE_LEFT = 0x01;
		public final static byte TOPHALFONLY_UNPOWERED = 0x00;
		public final static byte TOPHALFONLY_POWERED = 0x02;
		public final static byte BOTTOMHALFONLY_CLOSED = 0x00;
		public final static byte BOTTOMHALFONLY_OPENED = 0x01;
		public final static byte BOTTOMHALFONLY_FACING_WEST = 0x00;
		public final static byte BOTTOMHALFONLY_FACING_NORTH = 0x01;
		public final static byte BOTTOMHALFONLY_FACING_EAST = 0x02;
		public final static byte BOTTOMHALFONLY_FACING_SOUTH = 0x03;
	}
	public final class RAILS {
		public final static byte FLAT_NS = 0x00;
		public final static byte FLAT_EW = 0x01;
		public final static byte SLOPED_EAST = 0x02;
		public final static byte SLOPED_WEST = 0x03;
		public final static byte SLOPED_NORTH = 0x04;
		public final static byte SLOPED_SOUTH = 0x05;
		public final static byte PLAINRAILONLY_CURVED_SE = 0x06;
		public final static byte PLAINRAILONLY_CURVED_SW = 0x07;
		public final static byte PLAINRAILONLY_CURVED_NW = 0x08;
		public final static byte PLAINRAILONLY_CURVED_NE = 0x09;
		public final static byte REDSTONERAILONLY_NOT_ACTIVE = 0x00;
		public final static byte REDSTONERAILONLY_ACTIVE = 0x08;
	}
	public final class LADDERS_FURNACES_CHESTS { /* also appliable to trapped chests */
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_SOUTH = 0x03;
		public final static byte FACING_WEST = 0x04;
		public final static byte FACING_EAST = 0x05;
	}
	public final class DROPPER_DISPENSER {
		public final static byte FACING_DOWN = 0x00;
		public final static byte FACING_UP = 0x01;
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_SOUTH = 0x03;
		public final static byte FACING_WEST = 0x04;
		public final static byte FACING_EAST = 0x05;
		public final static byte NOT_ACTIVE = 0x00;
		public final static byte ACTIVE = 0x08;
	}
	public final class HOPPER {
		public final static byte FACING_DOWN = 0x00;
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_SOUTH = 0x03;
		public final static byte FACING_WEST = 0x04;
		public final static byte FACING_EAST = 0x05;
		public final static byte NOT_ACTIVE = 0x00;
		public final static byte ACTIVE = 0x08;
	}
	public final class LEVER {// on top or bottom direction indicated is the one the lever points at when off
		public final static byte ONBOTTOM_EAST = 0x00;
		public final static byte ONSIDE_EAST = 0x01;
		public final static byte ONSIDE_WEST = 0x02;
		public final static byte ONSIDE_SOUTH = 0x03;
		public final static byte ONSIDE_NORTH = 0x04;
		public final static byte ONTOP_SOUTH = 0x05;
		public final static byte ONTOP_EAST = 0x06;
		public final static byte ONBOTTOM_SOUTH = 0x07;
		public final static byte NOT_ACTIVE = 0x00;
		public final static byte ACTIVE = 0x08;
	}
	public final class PRESSURE_PLATES {
		public final static byte NOT_ACTIVE = 0x00;
		public final static byte ACTIVE = 0x01;
	}
	public final class BUTTONS {
		public final static byte FACING_DOWN = 0x00;
		public final static byte FACING_EAST = 0x01;
		public final static byte FACING_WEST = 0x02;
		public final static byte FACING_SOUTH = 0x03;
		public final static byte FACING_NORTH = 0x04;
		public final static byte FACING_UP = 0x05;
		public final static byte NOT_ACTIVE = 0x00;
		public final static byte ACTIVE = 0x08;
	}
	public final class SNOW {
		public final static byte LAYER_ONE = 0x00;
		public final static byte LAYER_TWO = 0x01;
		public final static byte LAYER_THREE = 0x02;
		public final static byte LAYER_FOUR = 0x03;
		public final static byte LAYER_FIVE = 0x04;
		public final static byte LAYER_SIX = 0x05;
		public final static byte LAYER_SEVEN = 0x06;
		public final static byte LAYER_EIGHT = 0x07;
	}
	public final class CACTI_REEDS {
		public final static byte AGE_0 = 0x00;
		public final static byte AGE_1 = 0x01;
		public final static byte AGE_2 = 0x02;
		public final static byte AGE_3 = 0x03;
		public final static byte AGE_4 = 0x04;
		public final static byte AGE_5 = 0x05;
		public final static byte AGE_6 = 0x06;
		public final static byte AGE_7 = 0x07;
		public final static byte AGE_8 = 0x08;
		public final static byte AGE_9 = 0x09;
		public final static byte AGE_10 = 0x0A;
		public final static byte AGE_11 = 0x0B;
		public final static byte AGE_12 = 0x0C;
		public final static byte AGE_13 = 0x0D;
		public final static byte AGE_14 = 0x0E;
		public final static byte AGE_MAX_15 = 0x0F;
	}
	public final class JUKEBOX {
		public final static byte EMPTY = 0x00;
		public final static byte WITH_DISC = 0x01;
	}
	public final class PUMPKINS { /* also appliable to jack'o lanterns */
		public final static byte FACING_SOUTH = 0x00;
		public final static byte FACING_WEST = 0x01;
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_EAST = 0x03;
		public final static byte WITH_FACE = 0x00;
		public final static byte WITHOUT_FACE = 0x04;
	}
	public final class CAKE {
		public final static byte FULL = 0x00;
		public final static byte EATEN_1 = 0x01;
		public final static byte EATEN_2 = 0x02;
		public final static byte EATEN_3 = 0x03;
		public final static byte EATEN_4 = 0x04;
		public final static byte EATEN_5 = 0x05;
		public final static byte EATEN_6 = 0x06;
	}
	public final class REDSTONE_REPEATER {
		public final static byte FACING_NORTH = 0x00;
		public final static byte FACING_EAST = 0x01;
		public final static byte FACING_SOUTH = 0x02;
		public final static byte FACING_WEST = 0x03;
		public final static byte DELAY_ONE = 0x00;
		public final static byte DELAY_TWO = 0x04;
		public final static byte DELAY_THREE = 0x08;
		public final static byte DELAY_FOUR = 0x0C;
	}
	public final class REDSTONE_COMPARATOR {
		public final static byte FACING_NORTH = 0x00;
		public final static byte FACING_EAST = 0x01;
		public final static byte FACING_SOUTH = 0x02;
		public final static byte FACING_WEST = 0x03;
		public final static byte ADDITIVE = 0x00;
		public final static byte SUBSTRACTIVE = 0x04;
		public final static byte UNPOWERED = 0x00;
		public final static byte POWERED = 0x08;
	}
	public final class TRAPDOORS {
		public final static byte SIDE_SOUTH = 0x00;
		public final static byte SIDE_NORTH = 0x01;
		public final static byte SIDE_EAST = 0x02;
		public final static byte SIDE_WEST = 0x03;
		public final static byte UNOPENED = 0x00;
		public final static byte OPENED = 0x04;
		public final static byte BOTTOM_HALF = 0x00;
		public final static byte TOP_HALF = 0x08;
	}
	public final class MONSTER_EGG {
		public final static byte STONE = 0x00;
		public final static byte COBBLESTONE = 0x01;
		public final static byte STONE_BRICK = 0x02;
		public final static byte MOSSY_STONE_BRICK = 0x03;
		public final static byte CRACKED_STONE_BRICK = 0x04;
		public final static byte CHISELED_STONE_BRICK = 0x05;
	}
	public final class STONE_BRICKS {
		public final static byte STONE_BRICK = 0x00;
		public final static byte MOSSY_STONE_BRICK = 0x01;
		public final static byte CRACKED_STONE_BRICK = 0x02;
		public final static byte CHISELED_STONE_BRICK = 0x03;
	}
	public final class PRISMARINE {
		public final static byte PRISMARINE = 0x00;
		public final static byte PRISMARINE_BRICKS = 0x01;
		public final static byte DARK_PRISMARINE = 0x02;
	}
	public final class SPONGE {
		public final static byte SPONGE = 0x00;
		public final static byte WET_SPONGE = 0x01;
	}
	public final class MUSHROOM_BLOCKS {
		public final static byte ALL_PORES = 0x00;
		public final static byte CAP_TOP_WN = 0x01;
		public final static byte CAP_TOP_N = 0x02;
		public final static byte CAP_TOP_NE = 0x03;
		public final static byte CAP_TOP_W = 0x04;
		public final static byte CAP_TOP = 0x05;
		public final static byte CAP_TOP_E = 0x06;
		public final static byte CAP_TOP_SW = 0x07;
		public final static byte CAP_TOP_S = 0x08;
		public final static byte CAP_TOP_SE = 0x09;
		public final static byte STEM_PORES = 0x0A;
		public final static byte ALL_CAP = 0x0E;
		public final static byte ALL_STEM = 0x0F;
	}
	public final class PUMPKIN_MELON_STEM {
		public final static byte GROWTH_0 = 0x00;
		public final static byte GROWTH_1 = 0x01;
		public final static byte GROWTH_2 = 0x02;
		public final static byte GROWTH_3 = 0x03;
		public final static byte GROWTH_4 = 0x04;
		public final static byte GROWTH_5 = 0x05;
		public final static byte GROWTH_6 = 0x06;
		public final static byte GROWTH_7_MAX = 0x07;
	}
	public final class VINES {
		public final static byte ON_TOP = 0x00;
		public final static byte ON_SOUTH = 0x01;
		public final static byte ON_WEST = 0x02;
		public final static byte ON_NORTH = 0x04;
		public final static byte ON_EAST = 0x08;
	}
	public final class FENCE_GATES {
		public final static byte FACING_SOUTH = 0x00;
		public final static byte FACING_WEST = 0x01;
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_EAST = 0x03;
		public final static byte CLOSED = 0x00;
		public final static byte OPENED = 0x04;
	}
	public final class NETHER_WART {
		public final static byte GROWTH_0 = 0x00;
		public final static byte GROWTH_1 = 0x01;
		public final static byte GROWTH_2 = 0x02;
		public final static byte GROWTH_3_MAX = 0x03;
	}
	public final class BREWING_STAND {
		public final static byte BOTTLE_IN_SLOT_EAST = 0x01;
		public final static byte BOTTLE_IN_SLOT_SOUTHWEST = 0x02;
		public final static byte BOTTLE_IN_SLOT_NORTHWEST = 0x04;
	}
	public final class CAULDRON {
		public final static byte EMPTY = 0x00;
		public final static byte ONETHIRD_FILLED = 0x01;
		public final static byte TWOTHIRD_FILLED = 0x02;
		public final static byte FULLY_FILLED = 0x03;
	}
	public final class END_PORTAL_FRAME {
		public final static byte FACING_SOUTH = 0x00;
		public final static byte FACING_WEST = 0x01;
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_EAST = 0x03;
		public final static byte NO_EYE_OF_ENDER = 0x00;
		public final static byte HAS_EYE_OF_ENDER = 0x04;
	}
	public final class COCOA {
		public final static byte ATTACHED_ON_NORTH = 0x00;
		public final static byte ATTACHED_ON_EAST = 0x01;
		public final static byte ATTACHED_ON_SOUTH = 0x02;
		public final static byte ATTACHED_ON_WEST = 0x03;
		public final static byte GROWTH_1 = 0x00;
		public final static byte GROWTH_2 = 0x04;
		public final static byte GROWTH_3_MAX = 0x08;
	}
	public final class TRIPWIRE_HOOK {
		public final static byte FACING_SOUTH = 0x00;
		public final static byte FACING_WEST = 0x01;
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_EAST = 0x03;
		public final static byte NOT_CONNECTED = 0x00;
		public final static byte CONNECTED = 0x04;
		public final static byte ACTIVATED = 0x08;
	}
	public final class TRIPWIRE {
		public final static byte ACTIVATED = 0x01;
		public final static byte NOT_CONNECTED = 0x00;
		public final static byte CONNECTED = 0x04;
		public final static byte DISARMED = 0x08;
	}
	public final class COBBLESTONE_WALL {
		public final static byte COBBLESTONE = 0x00;
		public final static byte MOSSY_COBBLESTONE = 0x01;
	}
	public final class FLOWERPOTS { //deprecated since 1.7 but still readable by minecraft
		public final static byte EMPTY = 0x00;
		public final static byte POPPY = 0x01;
		public final static byte DANDELION = 0x02;
		public final static byte OAK_SAPLING = 0x03;
		public final static byte SPRUCE_SAPLING = 0x04;
		public final static byte BIRCH_SAPLING = 0x05;
		public final static byte JUNGLE_SAPLING = 0x06;
		public final static byte RED_MUSHROOM = 0x07;
		public final static byte BROWN_MUSHROOM = 0x08;
		public final static byte CACTUS = 0x0A;
		public final static byte DEAD_BUSH = 0x0B;
		public final static byte FERN = 0x0C;
		public final static byte ACACIA_SAPLING = 0x0D;
		public final static byte DARK_OAK_SAPLING = 0x0E;
	}
	public final class HEADS_BLOCK {
		public final static byte ONFLOOR = 0x01;
		public final static byte ONWALL_FACING_NORTH = 0x02;
		public final static byte ONWALL_FACING_SOUTH = 0x03;
		public final static byte ONWALL_FACING_EAST = 0x04;
		public final static byte ONWALL_FACING_WEST = 0x05;
	}
	public final class HEADS_ITEM {
		public final static byte SKELETON = 0x00;
		public final static byte WITHER_SKELETON = 0x01;
		public final static byte ZOMBIE = 0x02;
		public final static byte HEAD = 0x03;
		public final static byte CREEPER = 0x04;
	}
	public final class QUARTZ {
		public final static byte QUARTZ = 0x00;
		public final static byte CHISELED_QUARTZ = 0x01;
		public final static byte PILLAR_QUARTZ_VERTICAL = 0x02;
		public final static byte PILLAR_QUARTZ_HORIZONTAL_NS = 0x03;
		public final static byte PILLAR_QUARTZ_HORIZONTAL_EW = 0x04;
	}
	public final class COAL {
		public final static byte COAL = 0x00;
		public final static byte CHARCOAL = 0x01;
	}
	public final class DYES {
		public final static byte INK_SAC = 0x00;
		public final static byte ROSE_RED = 0x01;
		public final static byte CACTUS_GREEN = 0x02;
		public final static byte COCOA_BEANS = 0x03;
		public final static byte LAPIS_LAZULI = 0x04;
		public final static byte PURPLE_DYE = 0x05;
		public final static byte CYAN_DYE = 0x06;
		public final static byte LIGHT_GRAY_DYE = 0x07;
		public final static byte GRAY_DYE = 0x08;
		public final static byte PINK_DYE = 0x09;
		public final static byte LIME_DYE = 0x0A;
		public final static byte DANDELION_YELLOW = 0x0B;
		public final static byte LIGHT_BLUE_DYE = 0x0C;
		public final static byte MAGENTA_DYE = 0x0D;
		public final static byte ORANGE_DYE = 0x0E;
		public final static byte BONE_MEAL = 0x0F;
	}
	public final class FISHES {
		public final static byte RAW_FISH = 0x00;
		public final static byte RAW_SALMON = 0x01;
		public final static byte CLOWNFISH = 0x02;
		public final static byte PUFFERFISH = 0x03;
	}

	public final class ANVIL_BLOCK {
		public final static byte ORIENTATION_NS = 0x00;
		public final static byte ORIENTATION_EW = 0x01;
		public final static byte ORIENTATION_SN = 0x02;
		public final static byte ORIENTATION_WE = 0x03;
		public final static byte DAMAGE_NONE = 0x00;
		public final static byte DAMAGE_LIGHT = 0x04;
		public final static byte DAMAGE_HEAVY = 0x08;
	}
	public final class ANVIL_ITEM {
		public final static byte DAMAGE_NONE = 0x00;
		public final static byte DAMAGE_LIGHT = 0x01;
		public final static byte DAMAGE_HEAVY = 0x02;
	}
	
	//TODO: potions data

	public final class GOLDEN_APPLE {
		public final static byte GOLDEN_APPLE = 0x00;
		public final static byte ENCHANTED_GOLDEN_APPLE = 0x01;
	}
	public final class STRUCTURE_BLOCK {
		public final static byte SAVE = 0x00;
		public final static byte LOAD = 0x01;
		public final static byte CORNER = 0x02;
		public final static byte DATA = 0x03;
	}
	public final class CHORUS_FLOWER {
		public final static byte GROWTH_0 = 0x00;
		public final static byte GROWTH_1 = 0x01;
		public final static byte GROWTH_2 = 0x02;
		public final static byte GROWTH_3 = 0x03;
		public final static byte GROWTH_4 = 0x04;
		public final static byte GROWTH_5_MAX = 0x05;
	}
	public final class END_ROD {
		public final static byte FACING_DOWN = 0x00;
		public final static byte FACING_UP = 0x01;
		public final static byte FACING_NORTH = 0x02;
		public final static byte FACING_SOUTH = 0x03;
		public final static byte FACING_WEST = 0x04;
		public final static byte FACING_EAST = 0x05;
	}
}