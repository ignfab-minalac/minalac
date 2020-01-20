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
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class CemetaryBlockDefinition extends IdentifiedBlockDefinition {
	public static final Block UNDERGROUNDBLOCK = new Block(BlockType.SANDSTONE, (byte) (BlockData.SANDSTONES.SANDSTONE));

	public static final BlockDefinition GROUND = new SimpleBlockDefinition(
			new Block(BlockType.STONE_SLAB,
					(byte) (BlockData.SLABS.RIGHT_SIDE_UP | BlockData.STONESLABS.SANDSTONE)));
	public static final BlockDefinition MAIN_ALLEY = new SimpleBlockDefinition(
			new Block(BlockType.STONE_SLAB,
					(byte) (BlockData.SLABS.RIGHT_SIDE_UP | BlockData.STONESLABS.STONE)));
	public static final BlockDefinition TOMB_TAIL = new RandomBlockDefinition(new BlockDefinition[] {
		new SimpleBlockDefinition(new Block(BlockType.COBBLESTONE)),
		new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.COBBLESTONE),
			new Block(BlockType.FLOWER_POT, BlockData.FLOWERPOTS.EMPTY)
		}),
		new VerticalBlockDefinition(new Block[]{
				new Block(BlockType.COBBLESTONE),
				new Block(BlockType.FLOWER_POT, BlockData.FLOWERPOTS.POPPY)
		}),
		new VerticalBlockDefinition(new Block[]{
				new Block(BlockType.COBBLESTONE),
				new Block(BlockType.FLOWER_POT, BlockData.FLOWERPOTS.POPPY)//second time to increase occurence
		}),
		new VerticalBlockDefinition(new Block[]{
				new Block(BlockType.COBBLESTONE),
				new Block(BlockType.FLOWER_POT, BlockData.FLOWERPOTS.DANDELION)
		}),
		new VerticalBlockDefinition(new Block[]{
				new Block(BlockType.COBBLESTONE),
				new Block(BlockType.FLOWER_POT, BlockData.FLOWERPOTS.OAK_SAPLING)
		}),
		new VerticalBlockDefinition(new Block[]{
				new Block(BlockType.COBBLESTONE),
				new Block(BlockType.FLOWER_POT, BlockData.FLOWERPOTS.DEAD_BUSH)
		}),
		new VerticalBlockDefinition(new Block[]{
				new Block(BlockType.COBBLESTONE),
				new Block(BlockType.FLOWER_POT, BlockData.FLOWERPOTS.FERN)
		}),
		new VerticalBlockDefinition(new Block[]{
				new Block(BlockType.COBBLESTONE),
				new Block(BlockType.FLOWER_POT, BlockData.FLOWERPOTS.JUNGLE_SAPLING)
		})

	});
		
	public static final BlockDefinition TOMB_HEAD = new VerticalBlockDefinition(new Block[]{
			new Block(BlockType.COBBLESTONE),
			new Block(BlockType.COBBLESTONE_WALL)
		});
	
	//7x8 pattern with 6 tombs
	public static final BlockDefinition[][] cemetaryPattern =  new BlockDefinition[][] {
				new BlockDefinition[] { MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY },
    			new BlockDefinition[] { MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY, MAIN_ALLEY },
    			new BlockDefinition[] { MAIN_ALLEY, GROUND, TOMB_TAIL, GROUND, TOMB_TAIL, GROUND, TOMB_TAIL, GROUND },
    			new BlockDefinition[] { MAIN_ALLEY, GROUND, TOMB_HEAD, GROUND, TOMB_HEAD, GROUND, TOMB_HEAD, GROUND },
    			new BlockDefinition[] { MAIN_ALLEY, GROUND, GROUND, GROUND, GROUND, GROUND, GROUND, GROUND },
    			new BlockDefinition[] { MAIN_ALLEY, GROUND, TOMB_HEAD, GROUND, TOMB_HEAD, GROUND, TOMB_HEAD, GROUND },
    			new BlockDefinition[] { MAIN_ALLEY, GROUND, TOMB_TAIL, GROUND, TOMB_TAIL, GROUND, TOMB_TAIL, GROUND },
    		};
	
	//static common storage of wall definition
	// to avoid having one full array per instance although there is no overlap
	private static boolean[] walls;
	private static int[] patternLevel;
	public static void setMapSize(int mapSize) {
		walls = new boolean[mapSize * mapSize];
		//we don't have to add 1 if mapSize is a multiple of length of pattern, but it doesn't hurt
		patternLevel = new int[ (mapSize / cemetaryPattern.length + 1) * (mapSize / cemetaryPattern[0].length + 1) ];
	}

	enum PatternOrientation {
		NORMAL,
		ROT90,
		ROT180,
		ROT270
	}
	
	final private PatternOrientation orientation = PatternOrientation.values()[ (int) Math.floor( Math.random() * PatternOrientation.values().length ) ];
	
	public CemetaryBlockDefinition(Integer id) {
		super(id);
	}
	
	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		switch (ImporterName) {
		case "BuildingsImporter":
			return true;
		default:
			return false;
		}
	}
	@Override
	public boolean canPutOverlayLayer(int bufferX, int bufferY, int bufferSize) {
		return true;
	}
	@Override
	public boolean canPutOverlayBlock(int bufferX, int bufferY, int bufferSize) {
		if (walls[xz1D(bufferX, bufferY)]) {
			return false;
		}
		
		int patternX = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferX : bufferY;
		int patternY = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferY : bufferX;
		int xInPattern = patternX % cemetaryPattern[0].length;
		int yInPattern = patternY % cemetaryPattern.length;
		
		//avoid putting stuff over tombs or alleys
		return cemetaryPattern[yInPattern][xInPattern] == GROUND;
	}
	@Override
	public int getOverlayLevel(int bufferX, int bufferY, int y, int bufferSize) {
		if (walls[xz1D(bufferX, bufferY)]) {
			return y;
		}
		
		int patternX = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferX : bufferY;
		int patternY = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferY : bufferX;
		int patternLevelIndex = patternY/cemetaryPattern.length * (mapSize/cemetaryPattern[0].length + 1) + patternX/cemetaryPattern[0].length;
		if (patternLevel[patternLevelIndex] == 0) {
			return y;
		}
		return Math.abs(patternLevel[patternLevelIndex]);
	}

	//prerender will check where walls should be and will flatten each pattern
	@Override
	protected void preRender(int x, int z) {
		//check if the current point should be a wall
		//also set blocks in the diagonal of the outside as wall so cobblestone wall parts will join in minecraft
		boolean isWall = false;
		for (int[] shiftsXZ : new int[][] {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}}) {
			if ( x + shiftsXZ[0] >= 0 && z + shiftsXZ[1] >= 0 && x + shiftsXZ[0] < mapSize && z + shiftsXZ[1] < mapSize ) {
				if ( blocks[xz1D(x + shiftsXZ[0], z + shiftsXZ[1])] == null
						|| !blocks[xz1D(x + shiftsXZ[0], z + shiftsXZ[1])]
								.isSameDefinition(x + shiftsXZ[0], z + shiftsXZ[1], mapSize, this)) {
					isWall = true;
					break;
				}
				
			}
		}
		if (isWall) {
			walls[xz1D(x,z)] = true;
		} else {
			int patternX = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
					? x : z;
			int patternY = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
					? z : x;
			int patternLevelIndex = patternY/cemetaryPattern.length * (mapSize/cemetaryPattern[0].length + 1) + patternX/cemetaryPattern[0].length;

			//is this pattern already set as not fitting ? => check for negative value
			if (patternLevel[patternLevelIndex] < 0) {
				patternLevel[patternLevelIndex] = Math.max(patternLevel[patternLevelIndex], -altitudes[xz1D(x,z)]);
				return;
			}			

			if (patternLevel[patternLevelIndex] == 0) {
				//check if the pattern fits totally in our group
				int indexX, indexZ, sizeX, sizeZ;//index and size of the pattern in x and z is orientation dependant
				if (orientation == PatternOrientation.NORMAL || orientation == PatternOrientation.ROT180) {
					sizeX = cemetaryPattern[0].length;
					sizeZ = cemetaryPattern.length;
					indexX = patternX / sizeX;
					indexZ = patternY / sizeZ;
				} else {
					sizeX = cemetaryPattern.length;
					sizeZ = cemetaryPattern[0].length;
					indexX = patternY / sizeX;
					indexZ = patternX / sizeZ;
				}
				for (int curX = indexX * sizeX; curX < (indexX + 1) * sizeX; curX ++) {
					for (int curZ = indexZ * sizeZ; curZ < (indexZ + 1) * sizeZ; curZ ++) {
						if (curX >= 0 && curX < mapSize && curZ >= 0 && curZ < mapSize) {
							if ( blocks[xz1D(curX, curZ)] == null
									|| !blocks[xz1D(curX, curZ)].isSameDefinition(curX, curZ, mapSize, this)
									|| walls[xz1D(curX, curZ)]) {
								//not fitting
								patternLevel[patternLevelIndex] = -255;//set negative value
								return;
							}
						}
					}
				}
	
				//init value
				patternLevel[patternLevelIndex] = 255;
			}
			patternLevel[patternLevelIndex] = Math.min(patternLevel[patternLevelIndex], altitudes[xz1D(x,z)]);
		}
	}
	
	@Override
	protected void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		if (walls[xz1D(bufferX,bufferY)]) {
			//check if blocks around are also part of the wall and are higher,
			// to adjust the height of this part of wall
			int height = 1;
			int curIndex, tmpIndex;
			curIndex = xz1D(bufferX, bufferY);
			assert altitudes[curIndex] > 0;
			if (bufferX > 0) {
				tmpIndex = xz1D(bufferX - 1, bufferY);
				if (blocks[tmpIndex] == this && walls[tmpIndex]) {
					assert altitudes[tmpIndex] > 0;
					height = Math.max(height, 1 + altitudes[tmpIndex] - altitudes[curIndex]);
				}
			}
			if (bufferY > 0) {
				tmpIndex = xz1D(bufferX, bufferY - 1);
				if (blocks[tmpIndex] == this && walls[tmpIndex]) {
					assert altitudes[tmpIndex] > 0;
					height = Math.max(height, 1 + altitudes[tmpIndex] - altitudes[curIndex]);
				}
			}
			if (bufferX < (mapSize - 1)) {
				tmpIndex = xz1D(bufferX + 1, bufferY);
				if (blocks[tmpIndex] == this && walls[tmpIndex]) {
					assert altitudes[tmpIndex] > 0;
					height = Math.max(height, 1 + altitudes[tmpIndex] - altitudes[curIndex]);
				}
			}
			if (bufferY < (mapSize - 1)) {
				tmpIndex = xz1D(bufferX, bufferY + 1);
				if (blocks[tmpIndex] == this && walls[tmpIndex]) {
					assert altitudes[tmpIndex] > 0;
					height = Math.max(height, 1 + altitudes[tmpIndex] - altitudes[curIndex]);
				}
			}
			
			BlockDefinition wall = new VariableHeightBlockDefinition(
					new Block[] { new Block(BlockType.COBBLESTONE) },
					new Block[] { new Block(BlockType.COBBLESTONE_WALL) },
					height );
			wall.render(world, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
			return;
		}
		
		int patternX = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferX : bufferY;
		int patternY = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferY : bufferX;

		int patternLevelIndex = patternY/cemetaryPattern.length * (mapSize/cemetaryPattern[0].length + 1) + patternX/cemetaryPattern[0].length;
		assert patternLevel[patternLevelIndex] != 0;

		//set 2 blocks underground to avoid edges of earth visible
		world.setBlock(x, y - 2, z, UNDERGROUNDBLOCK);
		world.setBlock(x, y - 1, z, UNDERGROUNDBLOCK);
		
		if (patternLevel[patternLevelIndex] < 0) {
			GROUND.render(world, x, -patternLevel[patternLevelIndex], z, bufferX, bufferY, mapSize, mapItemColors);
			//erase any blocks over our leveled Y
			for (int curY = -patternLevel[patternLevelIndex] + 1; curY <= y; curY++) {
				world.setBlock(x, curY, z, Block.AIR_BLOCK);
			}
		} else {
			int xInPattern = patternX % cemetaryPattern[0].length;
			int yInPattern = patternY % cemetaryPattern.length;
			assert cemetaryPattern[yInPattern].length == cemetaryPattern[0].length;
			
			//erase any blocks over our leveled Y
			for (int curY = patternLevel[patternLevelIndex]; curY <= y; curY++) {
				world.setBlock(x, curY, z, Block.AIR_BLOCK);
			}
			
			cemetaryPattern[yInPattern][xInPattern].render(world, x, patternLevel[patternLevelIndex], z, bufferX, bufferY, mapSize, mapItemColors);
		}
	}
	
	@Override
	protected void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY,
			MineMap.MapItemColors mapItemColors) {
		if (walls[xz1D(bufferX,bufferY)]) {
			//check if blocks around are also part of the wall and are higher,
			// to adjust the height of this part of wall
			int height = 1;
			int curIndex, tmpIndex;
			curIndex = xz1D(bufferX, bufferY);
			assert altitudes[curIndex] > 0;
			if (bufferX > 0) {
				tmpIndex = xz1D(bufferX - 1, bufferY);
				if (blocks[tmpIndex] == this && walls[tmpIndex]) {
					assert altitudes[tmpIndex] > 0;
					height = Math.max(height, 1 + altitudes[tmpIndex] - altitudes[curIndex]);
				}
			}
			if (bufferY > 0) {
				tmpIndex = xz1D(bufferX, bufferY - 1);
				if (blocks[tmpIndex] == this && walls[tmpIndex]) {
					assert altitudes[tmpIndex] > 0;
					height = Math.max(height, 1 + altitudes[tmpIndex] - altitudes[curIndex]);
				}
			}
			if (bufferX < (mapSize - 1)) {
				tmpIndex = xz1D(bufferX + 1, bufferY);
				if (blocks[tmpIndex] == this && walls[tmpIndex]) {
					assert altitudes[tmpIndex] > 0;
					height = Math.max(height, 1 + altitudes[tmpIndex] - altitudes[curIndex]);
				}
			}
			if (bufferY < (mapSize - 1)) {
				tmpIndex = xz1D(bufferX, bufferY + 1);
				if (blocks[tmpIndex] == this && walls[tmpIndex]) {
					assert altitudes[tmpIndex] > 0;
					height = Math.max(height, 1 + altitudes[tmpIndex] - altitudes[curIndex]);
				}
			}
			
			BlockDefinition wall = new VariableHeightBlockDefinition(
					new Block[] { new Block(BlockType.COBBLESTONE) },
					new Block[] { new Block(BlockType.COBBLESTONE_WALL) },
					height );
			wall.render(blockList, x, y, z, bufferX, bufferY, mapSize, mapItemColors);
			return;
		}
		
		int patternX = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferX : bufferY;
		int patternY = ((orientation == PatternOrientation.NORMAL) || (orientation == PatternOrientation.ROT180))
				? bufferY : bufferX;

		int patternLevelIndex = patternY/cemetaryPattern.length * (mapSize/cemetaryPattern[0].length + 1) + patternX/cemetaryPattern[0].length;
		assert patternLevel[patternLevelIndex] != 0;

		//set 2 blocks underground to avoid edges of earth visible
		new BlockMT(x, y - 2, z, BlockTypeConverter.convert(UNDERGROUNDBLOCK.getType())).addTo(blockList);
		new BlockMT(x, y - 1, z, BlockTypeConverter.convert(UNDERGROUNDBLOCK.getType())).addTo(blockList);
		
		if (patternLevel[patternLevelIndex] < 0) {
			GROUND.render(blockList, x, -patternLevel[patternLevelIndex], z, bufferX, bufferY, mapSize, mapItemColors);
			//erase any blocks over our leveled Y
			for (int curY = -patternLevel[patternLevelIndex] + 1; curY <= y; curY++) {
				new BlockMT(x, curY, z, BlockTypeConverter.convert(Block.AIR_BLOCK.getType())).addTo(blockList);
			}
		} else {
			int xInPattern = patternX % cemetaryPattern[0].length;
			int yInPattern = patternY % cemetaryPattern.length;
			assert cemetaryPattern[yInPattern].length == cemetaryPattern[0].length;
			
			//erase any blocks over our leveled Y
			for (int curY = patternLevel[patternLevelIndex]; curY <= y; curY++) {
				new BlockMT(x, curY, z, BlockTypeConverter.convert(Block.AIR_BLOCK.getType())).addTo(blockList);
			}
			
			cemetaryPattern[yInPattern][xInPattern].render(blockList, x, patternLevel[patternLevelIndex], z, bufferX, bufferY, mapSize, mapItemColors);
		}
		
	}
}
