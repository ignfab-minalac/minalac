/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ignfab.minetest;

import java.util.Arrays;
import java.util.HashMap;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;

public class BlockTypeConverter {
	public static HashMap<String, String> h;
	
	public static String convert(BlockType bt) {
		if(h==null)
			BlockTypeConverter.getCorrespondanceTable();
		String occ = h.get(bt.name());
		if(occ==null) {
			return "air"; // BlockType not found
		} else {
			return occ;
		}
	}
	
	public static String convert(BlockType bt, byte bd) {
		if(h==null)
			BlockTypeConverter.getCorrespondanceTable();
		String occ;
		BlockType[] typed = {
				BlockType.WOOL, 
				BlockType.STAINED_HARDENED_CLAY, 
				BlockType.STONE_SLAB, 
				BlockType.WOODEN_SLAB,
				BlockType.LEAVES};
		if(Arrays.asList(typed).contains(bt) && bd!=0) {
			occ = h.get(bt.name() + ":" + bd);
		} else {
			occ = h.get(bt.name());
		}
			
		if(occ==null) {
			return "air";
		} else {
			return occ;
		}
	}
	
	public static String convert(Block bl) {
		return BlockTypeConverter.convert(bl.getType(),bl.getData());
	}
	
	public static HashMap<String,String> getCorrespondanceTable() {
		h = new HashMap<String,String>();
		
		h.put("AIR",null);
		h.put("STONE","default:stone");
		h.put("GRASS","default:dirt_with_grass");
		h.put("DIRT","default:dirt");
		h.put("COBBLESTONE","default:cobble");
		h.put("PLANKS","default:wood");
		h.put("SAPLING","default:sapling");
		h.put("BEDROCK","default:obsidian");
		h.put("FLOWING_WATER","default:water_source");
		h.put("WATER","default:water_source");
		h.put("FLOWING_LAVA","default:lava_source");
		h.put("LAVA","default:lava_source");
		h.put("SAND","default:sand");
		h.put("GRAVEL","default:gravel");
		h.put("GOLD_ORE","default:stone_with_gold");
		h.put("IRON_ORE","default:stone_with_iron");
		h.put("COAL_ORE","default:stone_with_coal");
		h.put("LOG","default:tree");
		h.put("LEAVES","default:leaves");
		h.put("LEAVES:4","default:leaves");
		h.put("LEAVES:5","default:pine_needles");
		h.put("LEAVES:6","default:leaves");
		h.put("LEAVES:7","default:jungleleaves");
		h.put("SPONGE","farming:straw");
		h.put("GLASS","default:glass");
		h.put("LAPIS_ORE","default:stone");
		h.put("LAPIS_BLOCK","default:stone");
		h.put("DISPENSER","default:furnace");
		h.put("SANDSTONE","default:sandstonebrick");
		h.put("NOTEBLOCK","default:desert_cobble");
		h.put("BED","beds:fancy_bed_bottom");
		h.put("GOLDEN_RAIL","carts:powerrail");
		h.put("DETECTOR_RAIL","carts:powerrail");
		h.put("STICKY_PISTON","default:stone");
		h.put("WEB",null);
		h.put("TALLGRASS","default:grass_1");
		h.put("DEADBUSH","default:dry_grass_1");
		h.put("PISTON","default:stone");
		h.put("PISTON_HEAD","default:stone");
		h.put("WOOL","wool:white");
		h.put("WOOL:","wool:white");
		h.put("WOOL:0","wool:white");
		h.put("WOOL:1", "wool:orange");
		h.put("WOOL:2", "wool:magenta");
		h.put("WOOL:3", "wool:cyan");
		h.put("WOOL:4", "wool:yellow");
		h.put("WOOL:5", "wool:green");
		h.put("WOOL:6", "wool:pink");
		h.put("WOOL:7", "wool:dark_grey");
		h.put("WOOL:8", "wool:grey");
		h.put("WOOL:9", "wool:cyan");
		h.put("WOOL:10", "wool:violet");
		h.put("WOOL:11", "wool:blue");
		h.put("WOOL:12", "wool:brown");
		h.put("WOOL:13", "wool:dark_green");
		h.put("WOOL:14", "wool:red");
		h.put("WOOL:15", "wool:black");
		h.put("STAINED_HARDENED_CLAY","hardenedclay:hardened_clay_white");
		h.put("STAINED_HARDENED_CLAY:","hardenedclay:hardened_clay_white");
		h.put("STAINED_HARDENED_CLAY:0","hardenedclay:hardened_clay_white");
		h.put("STAINED_HARDENED_CLAY:1", "hardenedclay:hardened_clay_orange");
		h.put("STAINED_HARDENED_CLAY:2", "hardenedclay:hardened_clay_magenta");
		h.put("STAINED_HARDENED_CLAY:3", "hardenedclay:hardened_clay_cyan");
		h.put("STAINED_HARDENED_CLAY:4", "hardenedclay:hardened_clay_yellow");
		h.put("STAINED_HARDENED_CLAY:5", "hardenedclay:hardened_clay_lime");
		h.put("STAINED_HARDENED_CLAY:6", "hardenedclay:hardened_clay_pink");
		h.put("STAINED_HARDENED_CLAY:7", "hardenedclay:hardened_clay_gray");
		h.put("STAINED_HARDENED_CLAY:8", "hardenedclay:hardened_clay_light_gray");
		h.put("STAINED_HARDENED_CLAY:9", "hardenedclay:hardened_clay_cyan");
		h.put("STAINED_HARDENED_CLAY:10", "hardenedclay:hardened_clay_purple");
		h.put("STAINED_HARDENED_CLAY:11", "hardenedclay:hardened_clay_blue");
		h.put("STAINED_HARDENED_CLAY:12", "hardenedclay:hardened_clay_brown");
		h.put("STAINED_HARDENED_CLAY:13", "hardenedclay:hardened_clay_green");
		h.put("STAINED_HARDENED_CLAY:14", "hardenedclay:hardened_clay_red");
		h.put("STAINED_HARDENED_CLAY:15", "hardenedclay:hardened_clay_black");
		h.put("PISTON_EXTENSION","default:stone");
		h.put("YELLOW_FLOWER","flowers:dandelion_yellow");
		h.put("RED_FLOWER","flowers:rose");
		h.put("BROWN_MUSHROOM","flowers:mushroom_brown");
		h.put("RED_MUSHROOM","flowers:mushroom_red");
		h.put("GOLD_BLOCK","default:goldblock");
		h.put("IRON_BLOCK","default:steelblock");
		h.put("DOUBLE_STONE_SLAB","default:stone");
		h.put("STONE_SLAB","stairs:slab_silver_sandstone_block");
		h.put("STONE_SLAB:1", "stairs:slab_sandstone");
		h.put("STONE_SLAB:2", "stairs:slab_wood");
		h.put("STONE_SLAB:3", "stairs:slab_cobble");
		h.put("STONE_SLAB:4", "stairs:slab_brick");
		h.put("STONE_SLAB:5", "stairs:slab_stonebrick");
		h.put("STONE_SLAB:6", "stairs:slab_desert_stone");
		h.put("STONE_SLAB:7", "stairs:slab_steelblock");
		h.put("STONE_SLAB:9", "default:sandstonebrick");
		h.put("BRICK_BLOCK","default:brick");
		h.put("TNT","tnt:tnt");
		h.put("BOOKSHELF","default:bookshelf");
		h.put("MOSSY_COBBLESTONE","default:mossycobble");
		h.put("OBSIDIAN","default:obsidian_block");
		h.put("TORCH","default:torch");
		h.put("FIRE","fire:basic_flame");
		h.put("MOB_SPAWNER","default:stone");
		h.put("OAK_STAIRS","stairs:stair_wood");
		h.put("CHEST","default:chest");
		h.put("REDSTONE_WIRE",null);
		h.put("DIAMOND_ORE","default:stone_with_diamond");
		h.put("DIAMOND_BLOCK","default:diamondblock");
		h.put("CRAFTING_TABLE","default:wood");
		h.put("WHEAT","farming:wheat_8");
		h.put("FARMLAND","default:dirt");
		h.put("FURNACE","default:furnace");
		h.put("LIT_FURNACE","default:furnace_active");
		h.put("STANDING_SIGN","default:sign_wall_wood");
		h.put("WOODEN_DOOR","doors:door_wood");
		h.put("LADDER","default:ladder_wood");
		h.put("RAIL","carts:rail");
		h.put("STONE_STAIRS","stairs:stair_stone");
		h.put("WALL_SIGN","default:sign_wall_wood");
		h.put("LEVER","default:torch");
		h.put("STONE_PRESSURE_PLATE","stairs:slab_stone");
		h.put("IRON_DOOR","doors:door_steel");
		h.put("WOODEN_PRESSURE_PLATE","stairs:slab_wood");
		h.put("REDSTONE_ORE","default:stone_with_iron");
		h.put("LIT_REDSTONE_ORE","default:stone_with_iron");
		h.put("UNLIT_REDSTONE_TORCH","default:torch");
		h.put("REDSTONE_TORCH","default:torch");
		h.put("STONE_BUTTON",null);
		h.put("SNOW_LAYER",null);
		h.put("ICE","default:ice");
		h.put("SNOW","default:snowblock");
		h.put("CACTUS","default:cactus");
		h.put("CLAY","default:clay");
		h.put("REEDS","default:papyrus");
		h.put("JUKEBOX","default:desert_stone");
		h.put("FENCE","default:fence_wood");
		h.put("PUMPKIN","default:mese");
		h.put("NETHERRACK","default:coral_orange");
		h.put("SOUL_SAND","default:coral_brown");
		h.put("GLOWSTONE","default:coral_brown");
		h.put("PORTAL",null);
		h.put("LIT_PUMPKIN","default:mese");
		h.put("CAKE",null);
		h.put("UNPOWERED_REPEATER",null);
		h.put("POWERED_REPEATER",null);
		h.put("STAINED_GLASS","default:obsidian_glass");
		h.put("TRAPDOOR","doors:trapdoor");
		h.put("MONSTER_EGG",null);
		h.put("STONEBRICK","default:stonebrick");
		h.put("BROWN_MUSHROOM_BLOCK","default:coral_brown");
		h.put("RED_MUSHROOM_BLOCK","default:coral_orange");
		h.put("IRON_BARS","xpanes:bar");
		h.put("GLASS_PANE","xpanes:pane");
		h.put("MELON_BLOCK","default:mese");
		h.put("PUMPKIN_STEM","default:acacia_bush_sapling");
		h.put("MELON_STEM","default:acacia_bush_sapling");
		h.put("VINE","default:acacia_bush_sapling");
		h.put("FENCE_GATE","doors:gate_wood_closed");
		h.put("BRICK_STAIRS","stairs:stair_brick");
		h.put("STONE_BRICK_STAIRS","stairs:stair_stone");
		h.put("MYCELIUM","default:dirt_with_rainforest_litter");
		h.put("WATERLILY","flowers:waterlily");
		h.put("NETHER_BRICK","default:desert_stone");
		h.put("NETHER_BRICK_FENCE","doors:gate_acacia_wood_closed");
		h.put("NETHER_BRICK_STAIRS","stairs:stair_acacia_wood");
		h.put("NETHER_WART","flowers:rose");
		h.put("ENCHANTING_TABLE","default:bookshelf");
		h.put("BREWING_STAND","default:bookshelf");
		h.put("CAULDRON",null);
		h.put("END_PORTAL",null);
		h.put("END_PORTAL_FRAME",null);
		h.put("END_STONE","default:stone");
		h.put("DRAGON_EGG",null);
		h.put("REDSTONE_LAMP","default:torch");
		h.put("LIT_REDSTONE_LAMP","default:torch");
		h.put("DOUBLE_WOODEN_SLAB","default:wood");
		h.put("WOODEN_SLAB","stairs:slab_wood");
		h.put("WOODEN_SLAB:1","stairs:slab_junglewood");
		h.put("WOODEN_SLAB:2","stairs:slab_pine");
		h.put("WOODEN_SLAB:3","stairs:slab_wood");
		h.put("WOODEN_SLAB:4","stairs:slab_acacia");
		h.put("WOODEN_SLAB:5","stairs:slab_junglewood");
		h.put("COCOA","default:tree");
		h.put("SANDSTONE_STAIRS","stairs:stair_sandstone");
		h.put("EMERALD_ORE","default:stone_with_gold");
		h.put("ENDER_CHEST",null);
		h.put("TRIPWIRE_HOOK",null);
		h.put("TRIPWIRE",null);
		h.put("EMERALD_BLOCK","default:cactus");
		h.put("SPRUCE_STAIRS","stairs:stair_pine_wood");
		h.put("BIRCH_STAIRS","stairs:stair_junglewood");
		h.put("JUNGLE_STAIRS","stairs:stair_junglewood");
		h.put("COMMAND_BLOCK","default:stone");
		h.put("BEACON",null);
		h.put("COBBLESTONE_WALL","default:cobble");
		h.put("FLOWER_POT",null);
		h.put("CARROTS",null);
		h.put("POTATOES",null);
		h.put("WOODEN_BUTTON",null);
		h.put("SKULL",null);
		h.put("ANVIL",null);
		h.put("TRAPPED_CHEST","default:stone");
		h.put("LIGHT_WEIGHTED_PRESSURE_PLATE",null);
		h.put("HEAVY_WEIGHTED_PRESSURE_PLATE",null);
		h.put("UNPOWERED_COMPARATOR",null);
		h.put("POWERED_COMPARATOR",null);
		h.put("DAYLIGHT_DETECTOR",null);
		h.put("REDSTONE_BLOCK","default:coral_orange");
		h.put("QUARTZ_ORE","default:stone_with_tin");
		h.put("HOPPER","default:stone");
		h.put("QUARTZ_BLOCK","default:tinblock");
		h.put("QUARTZ_STAIRS","stairs:stair_tinblock");
		h.put("ACTIVATOR_RAIL",null);
		h.put("DROPPER",null);
		h.put("STAINED_HARDENED_CLAY","default:clay");
		h.put("STAINED_GLASS_PANE","xpanes:pane");
		h.put("LEAVES2","default:leaves");
		h.put("LOG2","default:tree");
		h.put("ACACIA_STAIRS","stairs:stair_acacia_wood");
		h.put("DARK_OAK_STAIRS","stairs:stair_junglewood");
		h.put("SLIME_BLOCK","default:mese");
		h.put("BARRIER","default:fence_wood");
		h.put("IRON_TRAPDOOR","doors:trapdoor_steel");
		h.put("PRISMARINE","wool:cyan");
		h.put("SEA_LANTERN","wool:white");
		h.put("HAY_BLOCK","farming:straw");
		h.put("CARPET",null);
		h.put("HARDENED_CLAY","default:clay");
		h.put("COAL_BLOCK","default:coalblock");
		h.put("PACKED_ICE","default:ice");
		h.put("DOUBLE_PLANT",null);
		h.put("STANDING_BANNER","default:sign_wall_wood");
		h.put("WALL_BANNER","default:sign_wall_wood");
		h.put("DAYLIGHT_DETECTOR_INVERTED",null);
		h.put("RED_SANDSTONE","default:stone");
		h.put("RED_SANDSTONE_STAIRS","default:stone");
		h.put("DOUBLE_STONE_SLAB2","default:stone");
		h.put("STONE_SLAB2","stairs:slab_stone");
		h.put("SPRUCE_FENCE_GATE","doors:gate_junglewood_closed");
		h.put("BIRCH_FENCE_GATE","doors:gate_pine_wood_closed");
		h.put("JUNGLE_FENCE_GATE","doors:gate_junglewood_closed");
		h.put("DARK_OAK_FENCE_GATE","doors:gate_junglewood_closed");
		h.put("ACACIA_FENCE_GATE","doors:gate_acacia_wood_closed");
		h.put("SPRUCE_FENCE","default:fence_junglewood");
		h.put("BIRCH_FENCE","default:fence_pine_wood");
		h.put("JUNGLE_FENCE","default:fence_junglewood");
		h.put("DARK_OAK_FENCE","default:fence_junglewood");
		h.put("ACACIA_FENCE","default:fence_acacia_wood");
		h.put("SPRUCE_DOOR","doors:door_wood");
		h.put("BIRCH_DOOR","doors:door_wood");
		h.put("JUNGLE_DOOR","doors:door_wood");
		h.put("ACACIA_DOOR","doors:door_wood");
		h.put("DARK_OAK_DOOR","doors:door_wood");
		
		return h;
	}
}
