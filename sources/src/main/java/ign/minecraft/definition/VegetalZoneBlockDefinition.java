/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Map;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.importer.AltiImporter;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class VegetalZoneBlockDefinition extends OverlayIdentifiedBlockDefinition {
	//static common storage of block definitions and altitudes
	// to avoid having one full array per instance although there is no overlap
	private static BlockDefinition[] trees;
	private static BlockDefinition[] plants;
	
	public static void setMapSize(int mapSize) {
		trees = new BlockDefinition[mapSize*mapSize];
		plants = new BlockDefinition[mapSize*mapSize];
	}
	
	public static final Block BASE = new Block(BlockType.GRASS);
	public static final Block FIELDBASE = new Block(BlockType.FARMLAND, BlockData.FARMLAND.FARMLAND_WET_6);
	
	public enum ZoneType {
		TREES,
		DECIDUOUS_FOREST,
		PINE_FOREST_LOW,
		PINE_FOREST_HIGH,
		PINE_FOREST_MEDIUM,
		MIXED_FOREST,
		CLEAR_FOREST,
		POPLARS,
		HEDGE,
		BUSHES,
		ORCHARD,
		VINEYARD,
		WOOD
	}
	
	final private ZoneType zoneType;
	
	public VegetalZoneBlockDefinition(Integer id, ZoneType zoneType) {
		super(id);
		
		this.zoneType = zoneType;
	}

	@Override
	protected void preRender(int x, int z) {
		super.preRender(x, z);//to prerender base blocks
		
		if (!baseBlocks[xz1D(x, z)].canPutOverlayBlock(x, z, mapSize)) {
			return;
		}

		if (mustPlantTree(x, z)) {
			trees[ xz1D(x, z) ] = getTreeType();
		} else if (mustPlantVegetation(x, z)) {
			plants[ xz1D(x, z) ] = getVegetationType();
		}
	}
	
	@Override
	protected void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		int localIndex = xz1D(bufferX, bufferY);
		//first set surface block
		if (zoneType == ZoneType.TREES || !baseBlocks[localIndex].canBeReplaced("", bufferX, bufferY, mapSize)) {
			//in this case we keep the block defined by previous block definition
			assert baseBlocks[localIndex] != null;
			baseBlocks[localIndex].render(world, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
			y = baseBlocks[localIndex].getOverlayLevel(bufferX, bufferY, y, mapSize);
		} else {
			//turn the block into grass in all the zone
			world.setBlock(x, y, z, BASE);
			if(MineGenerator.getSnowMode() == MineGenerator.MODE_SNOW) {
				int numberOfHeightSlices = (MineGenerator.MINECRAFTMAP_SNOWHEIGHTMAX - MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN) + 1;
				int minAndMaxAltiDifference = (int)(AltiImporter.MAX_ALTI - AltiImporter.MIN_ALTI);
				int eachSliceSize = minAndMaxAltiDifference / numberOfHeightSlices;
				int snowHeight = MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN + (int)Math.floor(((y + 1)-AltiImporter.MIN_ALTI)/eachSliceSize);
				int yInitial = y;

				/*for(int snowY = yInitial + 1; snowY <= yInitial + snowHeight; snowY++) {
					y++;
					world.setBlock(x, snowY, z, new Block(BlockType.SNOW));
				}//snow*/

				//snowfix for realism, one block height only even though you have > 1 height
				//(de)activate if needed and (de)comment the for loop above in snow mode
				y++;
				if(snowHeight > 0) world.setBlock(x, yInitial + 1, z, new Block(BlockType.SNOW));
				//snowfix//
			}
			y++;//to place tree or plant on top of base block
			mapItemColors.setColor(x, z, MapItemColor.getColor(BASE, y));
		}

		//then put eventual tree or vegetal
		if(trees[localIndex] != null) {
			trees[localIndex].render(world, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
		} else if(plants[localIndex] != null) {
			plants[localIndex].render(world, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
		}
	}
	
	@Override
	protected void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY,
			MineMap.MapItemColors mapItemColors) {
		int localIndex = xz1D(bufferX, bufferY);
		//first set surface block
		if (zoneType == ZoneType.TREES || !baseBlocks[localIndex].canBeReplaced("", bufferX, bufferY, mapSize)) {
			//in this case we keep the block defined by previous block definition
			assert baseBlocks[localIndex] != null;
			baseBlocks[localIndex].render(blockList, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
			y = baseBlocks[localIndex].getOverlayLevel(bufferX, bufferY, y, mapSize);
		} else {
			//turn the block into grass in all the zone
			new BlockMT(x, y, z, BlockTypeConverter.convert(BASE.getType())).addTo(blockList);
			if(MineGenerator.getSnowMode() == MineGenerator.MODE_SNOW) {
				int numberOfHeightSlices = (MineGenerator.MINECRAFTMAP_SNOWHEIGHTMAX - MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN) + 1;
				int minAndMaxAltiDifference = (int)(AltiImporter.MAX_ALTI - AltiImporter.MIN_ALTI);
				int eachSliceSize = minAndMaxAltiDifference / numberOfHeightSlices;
				int snowHeight = MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN + (int)Math.floor(((y + 1)-AltiImporter.MIN_ALTI)/eachSliceSize);
				int yInitial = y;

				/*for(int snowY = yInitial + 1; snowY <= yInitial + snowHeight; snowY++) {
					y++;
					new BlockMT(x, snowY, z, BlockTypeConverter.convert(BlockType.SNOW)).addTo(blockList);//snow
				}//snow*/

				//snowfix for realism, one block height only even though you have > 1 height
				//(de)activate if needed and (de)comment the for loop above in snow mode
				y++;
				if(snowHeight > 0) new BlockMT(x, yInitial + 1, z, BlockTypeConverter.convert(BlockType.SNOW)).addTo(blockList);
				//snowfix//
			}
			y++;//to place tree or plant on top of base block
			mapItemColors.setColor(x, z, MapItemColor.getColor(BASE, y));
		}

		//then put eventual tree or vegetal
		if(trees[localIndex] != null) {
			trees[localIndex].render(blockList, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
		} else if(plants[localIndex] != null) {
			plants[localIndex].render(blockList, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
		}
	}	
	
	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		return false;
	}
	@Override
	public boolean canPutOverlayBlock(int bufferX, int bufferY, int bufferSize) {
		return false;
	}

	
	private boolean mustPlantTree(int x, int z) {
		boolean mustPlant = false;
		
		int treeDistance = 7;
		int borderDistance = 4;
		double probability = 0;
		switch (zoneType) {
		case TREES:
			treeDistance = 7;
			probability = 0.95;
		case DECIDUOUS_FOREST:
			treeDistance = 7;
			probability = 0.2;
			break;
		case PINE_FOREST_HIGH:
		case PINE_FOREST_LOW:
		case PINE_FOREST_MEDIUM:
			treeDistance = 7;
			probability = 0.2;
			break;
		case MIXED_FOREST:
			treeDistance = 7;
			probability = 0.2;
			break;
		case WOOD:
		case CLEAR_FOREST:
			treeDistance = 10;
			probability = 0.2;
			break;
		case POPLARS:
			treeDistance = 7;
			probability = 0.8;
			break;
		case BUSHES:
			treeDistance = 30;
			probability = 0.05;
			break;
		default:
			break;
		}
		if (probability == 0) {
			//no need to go further
			return false;
		}

		//first define the zone in which we'll be looking in
		// for already planted trees or other zones
		int minX, maxX, minZ, maxZ;
		minX = Math.max(0, Math.min(x - borderDistance, x - treeDistance));
		maxX = Math.min(mapSize - 1, Math.max(x + borderDistance, x + treeDistance));
		minZ = Math.max(0, Math.min(z - borderDistance, z - treeDistance));
		maxZ = Math.min(mapSize - 1, Math.max(z + borderDistance, z + treeDistance));
		
		int curDistanceSquared;
		int borderDistanceSquared = borderDistance * borderDistance;
		int treeDistanceSquared = treeDistance * treeDistance;
		int curLocalIndex;
		for (int searchX = minX; searchX <= maxX; searchX++) {
			for (int searchZ = minZ; searchZ <= maxZ; searchZ++) {
				curLocalIndex = xz1D(searchX, searchZ);
				curDistanceSquared = (searchX - x) * (searchX - x) + (searchZ - z) * (searchZ - z);
				//if within the border distance, check if the currently browsed position is a vegetal zone or not 
				if (curDistanceSquared <= borderDistanceSquared) {
					if (blocks[curLocalIndex] != this) {
						//we're too close to the border of the zone
						return false;
					}
				}
				//if within the tree distance, check if there is a tree on the currently browsed position
				if (curDistanceSquared <= treeDistanceSquared) {
					if ((blocks[curLocalIndex] == this) && (trees[curLocalIndex] != null)) {
						//we're too close to another tree
						return false;
					}
				}
			}
		}
		
		//arriving to this point means we are ok in terms of distance to border and other trees
		//we plant according to the probability that has been set
		mustPlant = Math.random() < probability;
		
		return mustPlant;
	}
	
	private BlockDefinition getTreeType() {
		double randomValue = Math.random();
		switch (zoneType) {
		case TREES:
			return VegetalBlocks.STANDALONETREE;
		case DECIDUOUS_FOREST:
			if (randomValue < 0.47) {
				return VegetalBlocks.DECIDUOUSTREE1;
			} else if (randomValue < 0.94) {
				return VegetalBlocks.DECIDUOUSTREE2;
			} else {
				return VegetalBlocks.DECIDUOUSTREE3;
			}
		case PINE_FOREST_HIGH:
			return VegetalBlocks.PINETREEHIGH;
		case PINE_FOREST_LOW:
			return VegetalBlocks.PINETREELOW;
		case PINE_FOREST_MEDIUM:
			if (randomValue < 0.5) {
				return VegetalBlocks.PINETREEHIGH;
			} else {
				return VegetalBlocks.PINETREELOW;
			}
		case MIXED_FOREST:
			if (randomValue < 0.25) {
				return VegetalBlocks.DECIDUOUSTREE1;
			} else if (randomValue < 0.50) {
				return VegetalBlocks.DECIDUOUSTREE2;
			} else if (randomValue < 0.65) {
				return VegetalBlocks.DECIDUOUSTREE3;
			} else if (randomValue < 0.85) {
				return VegetalBlocks.PINETREELOW;
			} else {
				return VegetalBlocks.PINETREEHIGH;
			}
		case WOOD:
		case CLEAR_FOREST:
			if (randomValue < 0.35) {
				return VegetalBlocks.DECIDUOUSTREE1;
			} else if (randomValue < 0.70) {
				return VegetalBlocks.DECIDUOUSTREE2;
			} else {
				return VegetalBlocks.DECIDUOUSTREE3;
			}
		case POPLARS:
			if (randomValue < 0.10) {
				return VegetalBlocks.DECIDUOUSTREE1;
			} else {
				return VegetalBlocks.DECIDUOUSTREE3;
			}
		case BUSHES:
			return VegetalBlocks.BUSHTREE;
		default:
		}
		//unexpected
		assert false;
		return SimpleBlocks.DEFAULTSURFACE.get();
	}
	
	private boolean mustPlantVegetation(int x, int z) {
		boolean mustPlant = false;
		
		int plantDistance = 2;
		int borderDistance = 2;
		double probability = 0;
		switch (zoneType) {
		case DECIDUOUS_FOREST:
			probability = 0.2;
			break;
		case PINE_FOREST_HIGH:
		case PINE_FOREST_LOW:
		case PINE_FOREST_MEDIUM:
			probability = 0.2;
			break;
		case MIXED_FOREST:
			probability = 0.25;
			break;
		case WOOD:
		case CLEAR_FOREST:
			probability = 0.3;
			break;
		case POPLARS:
			probability = 0.05;
			break;
		case BUSHES:
			probability = 0.4;
			break;
		case HEDGE:
			plantDistance = 1;
			probability = 0.8;
			break;
		default:
			break;
		}
		if (probability == 0) {
			//no need to go further
			return false;
		}

		//first define the zone in which we'll be looking in
		// for already planted plants or other zones
		int minX, maxX, minZ, maxZ;
		minX = Math.max(0, Math.min(x - borderDistance, x - plantDistance));
		maxX = Math.min(mapSize - 1, Math.max(x + borderDistance, x + plantDistance));
		minZ = Math.max(0, Math.min(z - borderDistance, z - plantDistance));
		maxZ = Math.min(mapSize - 1, Math.max(z + borderDistance, z + plantDistance));
		
		int curDistanceSquared;
		int borderDistanceSquared = borderDistance * borderDistance;
		int plantDistanceSquared = plantDistance * plantDistance;
		int curLocalIndex;
		for (int searchX = minX; searchX <= maxX; searchX++) {
			for (int searchZ = minZ; searchZ <= maxZ; searchZ++) {
				curLocalIndex = xz1D(x,z);
				curDistanceSquared = (searchX - x) * (searchX - x) + (searchZ - z) * (searchZ - z);
				//if within the border distance, check if the currently browsed position is a vegetal zone or not 
				if (curDistanceSquared <= borderDistanceSquared) {
					if (blocks[curLocalIndex] != this) {
						//we're too close to the border of the zone
						return false;
					}
				}
				//if within the plant distance, check if there is a plant on the currently browsed position
				if (curDistanceSquared <= plantDistanceSquared) {
					if ((blocks[curLocalIndex] == this) && (plants[curLocalIndex] != null)) {
						//we're too close to another plant
						return false;
					}
				}
			}
		}
		
		//arriving to this point means we are ok in terms of distance to border and other trees
		//we plant according to the probability that has been set
		mustPlant = Math.random() < probability;
		
		return mustPlant;
	}
	
	private BlockDefinition getVegetationType() {
		double randomValue = Math.random();
		switch (zoneType) {
		case DECIDUOUS_FOREST:
			if (randomValue < 0.36) {
				return VegetalBlocks.FORESTGROUNDLEAVES;
			} else if (randomValue < 0.45) {
				return VegetalBlocks.FORESTGROUNDLEAVES2;
			} else if (randomValue < 0.54) {
				return VegetalBlocks.FORESTGROUNDLEAVESDOUBLE;
			} else if (randomValue < 0.63) {
				return VegetalBlocks.VEGETATION1;
			} else if (randomValue < 0.72) {
				return VegetalBlocks.VEGETATION1DOUBLE;
			} else if (randomValue < 0.94) {
				return VegetalBlocks.VEGETATION2;
			} else {
				return VegetalBlocks.VEGETATION2DOUBLE;
			}
		case PINE_FOREST_HIGH:
		case PINE_FOREST_LOW:
		case PINE_FOREST_MEDIUM:
			if (randomValue < 0.10) {
				return VegetalBlocks.FORESTGROUNDLEAVES;
			} else if (randomValue < 0.13) {
				return VegetalBlocks.FORESTGROUNDLEAVESDOUBLE;
			} else if (randomValue < 0.53) {
				return VegetalBlocks.VEGETATION1;
			} else if (randomValue < 0.56) {
				return VegetalBlocks.VEGETATION1DOUBLE;
			} else if (randomValue < 0.96) {
				return VegetalBlocks.VEGETATION2;
			} else {
				return VegetalBlocks.VEGETATION2DOUBLE;
			}
		case MIXED_FOREST:
			if (randomValue < 0.22) {
				return VegetalBlocks.FORESTGROUNDLEAVES;
			} else if (randomValue < 0.28) {
				return VegetalBlocks.FORESTGROUNDLEAVES2;
			} else if (randomValue < 0.34) {
				return VegetalBlocks.FORESTGROUNDLEAVESDOUBLE;
			} else if (randomValue < 0.56) {
				return VegetalBlocks.VEGETATION1;
			} else if (randomValue < 0.62) {
				return VegetalBlocks.VEGETATION1DOUBLE;
			} else if (randomValue < 0.94) {
				return VegetalBlocks.VEGETATION2;
			} else {
				return VegetalBlocks.VEGETATION2DOUBLE;
			}
		case WOOD:
		case CLEAR_FOREST:
			if (randomValue < 0.30) {
				return VegetalBlocks.FORESTGROUNDLEAVES;
			} else if (randomValue < 0.33) {
				return VegetalBlocks.FORESTGROUNDLEAVESDOUBLE;
			} else if (randomValue < 0.63) {
				return VegetalBlocks.VEGETATION1;
			} else if (randomValue < 0.66) {
				return VegetalBlocks.VEGETATION1DOUBLE;
			} else if (randomValue < 0.96) {
				return VegetalBlocks.VEGETATION2;
			} else {
				return VegetalBlocks.VEGETATION2DOUBLE;
			}
		case POPLARS:
			if (randomValue < 0.11) {
				return VegetalBlocks.POPLARSGROUNDLEAVES;
			} else if (randomValue < 0.55) {
				return VegetalBlocks.VEGETATION1;
			} else {
				return VegetalBlocks.VEGETATION2;
			}
		case BUSHES:
			if (randomValue < 0.15) {
				return VegetalBlocks.BUSHGROUNDLEAVES;
			} else if (randomValue < 0.22) {
				return VegetalBlocks.BUSHGROUNDLEAVES;
			} else if (randomValue < 0.44) {
				return VegetalBlocks.VEGETATION1;
			} else if (randomValue < 0.59) {
				return VegetalBlocks.VEGETATION1DOUBLE;
			} else if (randomValue < 0.93) {
				return VegetalBlocks.VEGETATION2;
			} else {
				return VegetalBlocks.VEGETATION2DOUBLE;
			}
		case HEDGE:
			if (randomValue < 0.3) {
				return VegetalBlocks.HEDGEGROUNDLEAVES;
			} else {
				return VegetalBlocks.HEDGEGROUNDLEAVESDOUBLE;
			}
		default:
		}
		//unexpected
		assert false;
		return SimpleBlocks.DEFAULTSURFACE.get();
	}
}
