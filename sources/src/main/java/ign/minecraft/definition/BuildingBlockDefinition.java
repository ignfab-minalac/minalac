/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Map;

import developpeur2000.minecraft.minecraft_rw.entity.Entity;
import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineGenerator;
import ign.minecraft.MineMap;
import ign.minecraft.importer.AltiImporter;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class BuildingBlockDefinition extends IdentifiedBlockDefinition implements PhotoTreatedBlockDefinition {
	//static common storage of wall definition and roof colors
	// to avoid having one full array per instance although there is no overlap
	private static boolean[] walls;
	private static PhotoColor[] roofColors;
	public static int BUILDING_ALTITUDE_LIMIT = 255;
	
	public static void setMapSize(int mapSize) {
		BuildingBlockDefinition.walls = new boolean[mapSize * mapSize];
		BuildingBlockDefinition.roofColors = new PhotoColor[mapSize * mapSize];
	}
	
	private static short MIN_HEIGHT = 4;//nb blocks over floor, including roof
	
	private final BuildingCategory category;
	private BuildingSpecification building;
	private short height;//nb blocks over floor, including roof
	private int baseY;
	private int[] roofColorCount = new int[BuildingCategory.RoofColor.values().length];

	public BuildingBlockDefinition(Integer id, BuildingCategory category, Short height) {
		super(id);
		
		this.category = category;
		this.height = (short) Math.max(MIN_HEIGHT, height);
	}

	@Override
	public void applyPhotoColor(int x, int z, PhotoColor color) {
		//store the detected color, it will be used to determine the color of the roof
		roofColors[xz1D(x, z)] = color;
	}

	//prerender will flatten the surface of the group or check where walls should be
	@Override
	protected void preRenderInit() {
		baseY = BUILDING_ALTITUDE_LIMIT;
	}
	@Override
	protected void preRender(int x, int z) {
		baseY = Math.min(baseY, altitudes[xz1D(x, z)]);
		//check if the current point should be a wall
		if ( (x > 0 && blocks[xz1D(x - 1, z)] != this) || (x < (mapSize - 1) && blocks[xz1D(x + 1, z)] != this)
				|| (z > 0 && blocks[xz1D(x, z - 1)] != this) || (z < (mapSize - 1) && blocks[xz1D(x, z + 1)] != this) ) {
			walls[xz1D(x,z)] = true;
		}
		//count the roof color
		assert roofColors[xz1D(x,z)] != null;
		roofColorCount[ BuildingCategory.getRoofColor(roofColors[xz1D(x,z)]).ordinal() ] ++;
	}
	@Override
	protected void preRenderEnd() {
		//treat the case of super tall buildings that would end reach an altitude over our limit
		if ((baseY + height) > BUILDING_ALTITUDE_LIMIT) {
			height = (short) (BUILDING_ALTITUDE_LIMIT - baseY);
		}
		//get the main color of the roof
		BuildingCategory.RoofColor roofColor = BuildingCategory.RoofColor.NONE;
		int maxCount = 0;
		for (int colorOrdinal = 0; colorOrdinal < roofColorCount.length; colorOrdinal ++) {
			if (roofColorCount[colorOrdinal] > maxCount) {
				maxCount = roofColorCount[colorOrdinal];
				roofColor = BuildingCategory.RoofColor.values()[colorOrdinal];
			}
		}
		//create the building only now so we can base it on the roof color
		building = new BuildingSpecification(category, roofColor);
	}
	
	@Override
	protected void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		int localIndex = xz1D(bufferX, bufferY);
		int curY;
		
		if (walls[localIndex]) {
			//WALL PART
			Block[] buildingWall = building.getWallTiles(height+1);
			assert buildingWall.length == height+1;
			for (curY = baseY; curY <= baseY + height; curY ++) {
				world.setBlock(x, curY, z, buildingWall[curY - baseY]);
			}
			mapItemColors.setColor(x, z, MapItemColor.getColor(buildingWall[height], baseY + height));
		} else {
			//FLOOR AND ROOF PART
			world.setBlock(x, baseY, z, building.getGroundBlock());
			for (curY = baseY + 1; curY < baseY + height; curY ++) {
				world.setBlock(x, curY, z, Block.AIR_BLOCK);
			}
			world.setBlock(x, baseY + height, z, building.getRoofBlock());
			if(MineGenerator.getSnowMode() == MineGenerator.MODE_SNOW) {
				int numberOfHeightSlices = (MineGenerator.MINECRAFTMAP_SNOWHEIGHTMAX - MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN) + 1;
				int minAndMaxAltiDifference = (int)(AltiImporter.MAX_ALTI - AltiImporter.MIN_ALTI);
				int eachSliceSize = minAndMaxAltiDifference / numberOfHeightSlices;
				int snowHeight = MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN + (int)Math.floor(((baseY+height+1)-AltiImporter.MIN_ALTI)/eachSliceSize);

				for(int snowY=baseY+height+1; snowY<=baseY+height+snowHeight; snowY++) {
					world.setBlock(x, snowY, z, new Block(BlockType.SNOW));//snow
				}
				//snowfix for realism, one block height only even though you have > 1 height
				//same fix below as in vegetalzones, activate if needed and comment the for loop above in snow mode
				//if(snowHeight > 0) world.setBlock(x, baseY+height+1, z, new Block(BlockType.SNOW));//snow
			}
			mapItemColors.setColor(x, z, MapItemColor.getColor(building.getRoofBlock(), baseY + height));
			//place gameplay entity if needed
			if (building.needGameplayItem()) {
				if (hasEnoughGround(bufferX, bufferY, 2)) {
					world.addEntity(x + (x >= 0 ? 0.5 : -0.5), baseY + 1.5, z + (z >= 0 ? 0.5 : -0.5), GameplayEntity.generateItem());
					building.gameplayEntityPlaced();
				}
			}
			if (building.needGameplayAnimal()) {
				if (hasEnoughGround(bufferX, bufferY, 4)) {
					Entity animal = GameplayEntity.generateAnimal();
					world.addEntity(x + (x >= 0 ? 0.5 : -0.5), baseY + 1.5, z + (z >= 0 ? 0.5 : -0.5), animal);
					if (animal.getId() == "Villager") {
						building.gameplayAnimalIsHuman();
					}
					building.gameplayEntityPlaced();
				}
			}
			//place carpet (except if there is an animal in the building)
			if (!building.hasGameplayAnimal()) {
				//removed as with our computed lighting mobs cannot spawn inside buildings anymore
				//world.setBlock(x, baseY + 1, z, CARPET_BLOCK);
			}
		}
		//ensure there is no residue if bulding top is under old altitude 
		for (curY = baseY + height + 1; curY <= y; curY ++) {
			world.setBlock(x, curY, z, Block.AIR_BLOCK);
		}
	}
	
	@Override
	protected void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		int localIndex = xz1D(bufferX, bufferY);
		int curY;
		
		if (walls[localIndex]) {
			//WALL PART
			Block[] buildingWall = building.getWallTiles(height+1);
			assert buildingWall.length == height+1;
			for (curY = baseY; curY <= baseY + height; curY ++) {
				new BlockMT(x, curY, z, BlockTypeConverter.convert(buildingWall[curY - baseY])).addTo(blockList);
			}
			mapItemColors.setColor(x, z, MapItemColor.getColor(buildingWall[height], baseY + height));
		} else {
			//FLOOR AND ROOF PART
			new BlockMT(x, baseY, z, BlockTypeConverter.convert(building.getGroundBlock())).addTo(blockList);
			for (curY = baseY + 1; curY < baseY + height; curY ++) {
				new BlockMT(x, curY, z, BlockTypeConverter.convert(Block.AIR_BLOCK.getType())).addTo(blockList);
			}
			new BlockMT(x, baseY + height, z, BlockTypeConverter.convert(building.getRoofBlock())).addTo(blockList);
			if(MineGenerator.getSnowMode() == MineGenerator.MODE_SNOW) {
				int numberOfHeightSlices = (MineGenerator.MINECRAFTMAP_SNOWHEIGHTMAX - MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN) + 1;
				int minAndMaxAltiDifference = (int)(AltiImporter.MAX_ALTI - AltiImporter.MIN_ALTI);
				int eachSliceSize = minAndMaxAltiDifference / numberOfHeightSlices;
				int snowHeight = MineGenerator.MINECRAFTMAP_SNOWHEIGHTMIN + (int)Math.floor(((baseY+height+1)-AltiImporter.MIN_ALTI)/eachSliceSize);

				for(int snowY=baseY+height+1; snowY<=baseY+height+snowHeight; snowY++) {
					new BlockMT(x, snowY, z, BlockTypeConverter.convert(BlockType.SNOW)).addTo(blockList);//snow
				}
				//snowfix for realism, one block height only even though you have > 1 height
				//same fix below as in vegetalzones, activate if needed and comment the for loop above in snow mode
				//if(snowHeight > 0) new BlockMT(x, baseY+height+1, z, BlockTypeConverter.convert(BlockType.SNOW)).addTo(blockList);
			}
			mapItemColors.setColor(x, z, MapItemColor.getColor(building.getRoofBlock(), baseY + height));
		}
		//ensure there is no residue if bulding top is under old altitude 
		for (curY = baseY + height + 1; curY <= y; curY ++) {
			new BlockMT(x, curY, z, BlockTypeConverter.convert(Block.AIR_BLOCK.getType())).addTo(blockList);
		}
	}
	
	private boolean hasEnoughGround(int x, int z, int distance) {
		//first define the zone in which we'll be looking in
		int minX, maxX, minZ, maxZ;
		minX = Math.max(0, x - distance);
		maxX = Math.min(mapSize - 1, x + distance);
		minZ = Math.max(0, z - distance);
		maxZ = Math.min(mapSize - 1, z + distance);
		
		int curDistanceSquared;
		assert blocks[ xz1D(x,z) ] == this;
		int distanceSquared = distance * distance;
		for (int searchX = minX; searchX <= maxX; searchX++) {
			for (int searchZ = minZ; searchZ <= maxZ; searchZ++) {
				curDistanceSquared = (searchX - x) * (searchX - x) + (searchZ - z) * (searchZ - z);
				//if within the distance, check if we are still in the same building
				if (curDistanceSquared <= distanceSquared) {
					if (blocks[ xz1D(searchX,searchZ) ] != this) {
						//too close to the wall
						return false;
					}
				}
	
			}
		}
	
		return true;
	}

	
	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		switch (ImporterName) {
		case "HydroLinesImporter":
		case "HydroSurfacesImporter":
		case "RoadsImporter":
			return true;
		default:
			return false;
		}
	}
	@Override
	public boolean canPutOverlayLayer(int bufferX, int bufferY, int bufferSize) {
		return false;
	}
	
}