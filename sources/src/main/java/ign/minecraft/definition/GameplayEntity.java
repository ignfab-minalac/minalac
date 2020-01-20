/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import developpeur2000.minecraft.minecraft_rw.entity.Cow;
import developpeur2000.minecraft.minecraft_rw.entity.Item;
import developpeur2000.minecraft.minecraft_rw.entity.Mob;
import developpeur2000.minecraft.minecraft_rw.entity.Pig;
import developpeur2000.minecraft.minecraft_rw.entity.Sheep;
import developpeur2000.minecraft.minecraft_rw.entity.Villager;

/**
 * 
 * the following class creates a block definition
 * 	from block id variants and data variants,
 *  from which the block elements will be set randomly
 *  
 */
public final class GameplayEntity {
	public enum GameplayItem {
		WHEAT_SEEDS (new Item("minecraft:wheat_seeds", (byte) 6)),
		CARROT (new Item("minecraft:carrot", (byte) 6)),
		POTATO (new Item("minecraft:potato", (byte) 6)),
		REEDS (new Item("minecraft:reeds", (byte) 6)),
		MELON_SEEDS (new Item("minecraft:melon_seeds", (byte) 6)),
		PUMPKIN_SEEDS (new Item("minecraft:pumpkin_seeds", (byte) 6)),
		SAPLING0 (new Item("minecraft:sapling", (short)0, (byte) 2)),
		SAPLING1 (new Item("minecraft:sapling", (short)1, (byte) 2)),
		SAPLING2 (new Item("minecraft:sapling", (short)2, (byte) 2)),
		SAPLING3 (new Item("minecraft:sapling", (short)3, (byte) 2)),
		SAPLING4 (new Item("minecraft:sapling", (short)4, (byte) 2)),
		SAPLING5 (new Item("minecraft:sapling", (short)5, (byte) 2)),
		CACTUS (new Item("minecraft:cactus", (byte) 4)),
		YELLOW_FLOWER (new Item("minecraft:yellow_flower", (byte) 6)),
		RED_FLOWER0 (new Item("minecraft:red_flower", (short)0, (byte) 6)),
		RED_FLOWER1 (new Item("minecraft:red_flower", (short)1, (byte) 6)),
		RED_FLOWER2 (new Item("minecraft:red_flower", (short)2, (byte) 6)),
		RED_FLOWER3 (new Item("minecraft:red_flower", (short)3, (byte) 6)),
		RED_FLOWER4 (new Item("minecraft:red_flower", (short)4, (byte) 6)),
		RED_FLOWER5 (new Item("minecraft:red_flower", (short)5, (byte) 6)),
		RED_FLOWER6 (new Item("minecraft:red_flower", (short)6, (byte) 6)),
		RED_FLOWER7 (new Item("minecraft:red_flower", (short)7, (byte) 6)),
		RED_FLOWER8 (new Item("minecraft:red_flower", (short)8, (byte) 6)),
		VINE (new Item("minecraft:vine", (byte) 6)),
		COCOA_BEANS (new Item("minecraft:dye", (short)3, (byte) 6)),
		;
	
		private final Item item;
	
		GameplayItem(Item item) {
			this.item = item;
		}
	}
	
	public enum GameplayAnimal {
		PIG (new Pig()),
		SHEEP (new Sheep()),
		COW (new Cow()),
		VILLAGER (new Villager()),
		;
	
		private final Mob mob;
	
		GameplayAnimal(Mob mob) {
			this.mob = mob;
		}
	}
	
	static public Item generateItem() {
		//set a random choice between the variants
		int randomIndex = (int) Math.floor( GameplayItem.values().length * Math.random() );
		return new Item( GameplayItem.values()[randomIndex].item );
	}

	static public Mob generateAnimal() {
		//set a random choice between the variants
		int randomIndex = (int) Math.floor( GameplayAnimal.values().length * Math.random() );
		switch (GameplayAnimal.values()[randomIndex].mob.getId()) {
		case "Pig":
			return new Pig( (Pig) GameplayAnimal.values()[randomIndex].mob );
		case "Sheep":
			return new Sheep( (Sheep) GameplayAnimal.values()[randomIndex].mob );
		case "Cow":
			return new Cow( (Cow) GameplayAnimal.values()[randomIndex].mob );
		case "Villager":
			return new Villager( (Villager) GameplayAnimal.values()[randomIndex].mob );
		}
		assert false;
		return null;
	}
}