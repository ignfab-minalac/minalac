/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft.definition;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockData;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ign.minecraft.importer.AltiImporter;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class RoadBlockDefinition extends OverlaySimpleBlockDefinition {
	
	public static final Block BRIDGE_BLOCK = new Block(BlockType.STAINED_HARDENED_CLAY, BlockData.COLORS.GRAY);
	
	public static final Block HIGHWAY_BLOCK = new Block(BlockType.WOOL, BlockData.COLORS.BLACK);
	public static final Block ROAD_BLOCK = new Block(BlockType.WOOL, BlockData.COLORS.GRAY);
	public static final Block STONEROAD_BLOCK = new Block(BlockType.COBBLESTONE);
	public static final Block DIRTROAD_BLOCK = new Block(BlockType.DIRT, BlockData.DIRT.COARSE_DIRT);
	public static final Block CYCLELANE_BLOCK = new Block(BlockType.WOOL, BlockData.COLORS.LIGHT_GRAY);
	public static final Block TRAIL_BLOCK = new Block(BlockType.WOOL, BlockData.COLORS.BROWN);
	public static final Block STAIRS_BLOCK = new Block(BlockType.STONE);
	public static final Block RAIL_BLOCK = new Block(BlockType.RAIL);

	private int sumAltitudes = 0;
	private int nbAltitudes = 0;
	private int averageY;

	public enum OnWhat {
		GROUND,
		BRIDGE,
		DAM
	}
	
	private final Block onWhichBlock;
	private final int topY;
	//--
	public int groundLevel;
	public MineMap map;
	//--
	public RoadBlockDefinition(Block baseDefinition, BlockDefinition previousDefinition, OnWhat onWhat,
			int topY, int groundLevel, MineMap map
			 ) {
		super(baseDefinition, previousDefinition );
		this.groundLevel = groundLevel;
		this.map = map;
		this.topY = topY;
		switch (onWhat) {
		case GROUND:
			onWhichBlock = SimpleBlocks.EARTH.get().block;
			break;
		case BRIDGE:
			onWhichBlock = BRIDGE_BLOCK;
			break;
		case DAM:
			onWhichBlock = DamBlockDefinition.DAM_BLOCK;
			break;
		default:
			assert false;//unexpected
			onWhichBlock = null;
		}
	}

	@Override
	public void render(World world, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		int curY;

		//remove any previous block if the road is deeper
		for (curY = y; curY > topY; curY--) {
			world.setBlock(x, curY, z, Block.AIR_BLOCK);
		}
		//set road block
		world.setBlock(x, topY, z, block);
		mapItemColors.setColor(x, z, MapItemColor.getColor(block, topY));


		if(onWhichBlock == BRIDGE_BLOCK) {
			world.setBlock(x, topY-1, z, block);

			/*sert à rien a priori*/
			WaterSurfaceBlockDefinition nearSurfaceBlock = null;
			int nearSBx = 0, nearSBz = 0;

			// search WaterSurfaceBlockDefinition around
			for (int curX=-5; curX<5; curX++) {
				for (int curZ=-5; curZ<5 ; curZ++) {
					try {
						if(map.getSurfaceBlock(curX+bufferX,curZ+bufferY) instanceof WaterSurfaceBlockDefinition) {
							nearSurfaceBlock = (WaterSurfaceBlockDefinition) map.getSurfaceBlock(curX+x,curZ+z);
							nearSBx = curX+bufferX;
							nearSBz = curZ+bufferY;
							break;
						}
					} catch(Exception e) {
						//no surface block there
						//no big deal, continue the "window" search
					}
				}
			}

			if(nearSurfaceBlock != null)
				Logger.getLogger("MinecraftGenerator").log(Level.INFO,"(onbridge) nearsurfaceblock not null");
			/*sert à rien a priori*/


			/*else
				Logger.getLogger("MinecraftGenerator").log(Level.INFO,"(onbridge) nearsurfaceblock is null");*/

			// maybe should activate that or trigger a real prerender on that surfaceblock ?
			/*if(nearSurfaceBlock != null) {
				if (!WaterSurfaceBlockDefinition.waterAltitudesComputed) {
					WaterSurfaceBlockDefinition.computeAllWaterAltitudes();
					WaterSurfaceBlockDefinition.waterAltitudesComputed = true;
				}

				if (WaterSurfaceBlockDefinition.waterAltitudes[WaterSurfaceBlockDefinition.xz1D(nearSBx, nearSBz)] != 0) {
					sumAltitudes += WaterSurfaceBlockDefinition.waterAltitudes[WaterSurfaceBlockDefinition.xz1D(x, z)];
					nbAltitudes ++;
				}
			}*/

			/*sert à rien a priori*/
			if(nearSurfaceBlock != null) {
				averageY = nearSurfaceBlock.getAverageY();
			} else
				averageY = 0;

			if (!WaterSurfaceBlockDefinition.waterAltitudesComputed) {
				WaterSurfaceBlockDefinition.computeAllWaterAltitudes();
				WaterSurfaceBlockDefinition.waterAltitudesComputed = true;
			}

			int surfaceY = WaterSurfaceBlockDefinition.waterAltitudes[WaterSurfaceBlockDefinition.xz1D(bufferX, bufferY)] == 0 ?
					(averageY == 0 ? y : averageY )
					: WaterSurfaceBlockDefinition.waterAltitudes[WaterSurfaceBlockDefinition.xz1D(bufferX, bufferY)];

			if(WaterSurfaceBlockDefinition.waterAltitudes[WaterSurfaceBlockDefinition.xz1D(bufferX, bufferY)] == 0) {
				//Logger.getLogger("MinecraftGenerator").log(Level.INFO,"(onbridge) wateralti level is 0 ! taking averageY");
			} else {
				Logger.getLogger("MinecraftGenerator").log(Level.INFO,"(onbridge) wateralti level isn't 0 !!!");
			}
			/*sert à rien a priori*/ // quasi sûr que surfaceY renvoie y à chaque fois ? (sur le if ternaire)


		//if (groundLevel <= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO+1 ) {
			/*for (int curYBIS = y-depth+2 ; curYBIS<y ; curYBIS++ ) {
				world.setBlock(x, curYBIS, z,  new Block(BlockType.WATER));
			}*/

			if(surfaceY != topY) {
				for (int curDepth = 1; curDepth < WaterSurfaceBlockDefinition.SURFACE_DEPTH; curDepth ++) { // or curdepth = 0 pour le trou d'eau
					if (world.getBlock(x , surfaceY - curDepth, z) == Block.AIR_BLOCK ||
							world.getBlock(x , surfaceY - curDepth, z) == BRIDGE_BLOCK ||
							world.getBlock(x , surfaceY - curDepth, z).getType().equals(DIRTROAD_BLOCK.getType())) {
						world.setBlock(x, surfaceY - curDepth, z, new Block(BlockType.WATER));
					}
					// problem with dirt
					/* else {
						Logger.getLogger("MinecraftGenerator").log(Level.INFO, "(onbridge) the block is " + world.getBlock(x , surfaceY - curDepth, z).getType().toString());
					}*/
				}
				//map.setGroundLevel(x,y,surfaceY - WaterSurfaceBlockDefinition.SURFACE_DEPTH);
				//make sure there is no old block between surfaceY and y
				for (int curY2 = surfaceY + 1; curY2 <= y; curY2 ++) {
					world.setBlock(x, curY2, z, Block.AIR_BLOCK);
				}
			} else { // permet de remplir le trou entre le pont et la route
				for (int curDepth = 0; curDepth < WaterSurfaceBlockDefinition.SURFACE_DEPTH; curDepth ++) {
					world.setBlock(x, surfaceY - curDepth, z, block);
				}
				//make sure there is no old block between surfaceY and y
				for (int curY2 = surfaceY + 1; curY2 <= y; curY2 ++) {
					world.setBlock(x, curY2, z, Block.AIR_BLOCK);
				}
			}

			//map.setGroundLevel(x,y,AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO);

			/*int curX, curZ, i;
			for (curX=-5; curX<5; curX++) {
				for (curZ=-5; curZ<5 ; curZ++) {
					if (world.getBlock(curX , y, curZ) != Block.AIR_BLOCK && world.getBlock(curX ,y, curZ) != BRIDGE_BLOCK
						&&y >= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&&groundLevel >= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&& y>AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&&y<topY
						){
						for (int curYBIS = groundLevel; curYBIS>AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO -3 ; curYBIS-- ) {
							world.setBlock(x, curYBIS, z, block);
						}
					}
				}
			}*/
			/*int curX, curZ, i;
			for (curX=-5; curX<5; curX++) {
				for (curZ=-5; curZ<5 ; curZ++) {
					if (world.getBlock(curX+x , y, curZ+z) != Block.AIR_BLOCK && world.getBlock(curX+x ,y, curZ+z) != BRIDGE_BLOCK
						&&y >= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&&groundLevel >= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&& y>AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&&y<topY
						){
						for (int curYBIS = surfaceY; curYBIS>surfaceY-WaterSurfaceBlockDefinition.SURFACE_DEPTH ; curYBIS-- ) {
							world.setBlock(x, curYBIS, z, block);
						}
						//for (int curYBIS = groundLevel; curYBIS>y ; curYBIS-- ) {
						//	world.setBlock(x, curYBIS, z, new Block(BlockType.WATER));
						//}
					}
				}
			}*/
		}
		/*}
		else {
			int curX, curZ, i;
			for (curX=-5; curX<5; curX++) {
				for (curZ=-5; curZ<5 ; curZ++) {
					if (world.getBlock(curX+x , y, curZ+z) != Block.AIR_BLOCK && world.getBlock(curX+x ,y, curZ+z) != BRIDGE_BLOCK
						&&y >= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&&groundLevel >= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&& y>AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&&y<topY
						){
						for (int curYBIS = groundLevel-depth; curYBIS>AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO -3 ; curYBIS-- ) {
							world.setBlock(x, curYBIS, z, block);
						}
						for (int curYBIS = groundLevel; curYBIS>y-depth ; curYBIS-- ) {
							world.setBlock(x, curYBIS, z, new Block(BlockType.WATER));
						}
					}
				}
			}
		}*/
	}

	@Override
	public void render(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY, int bufferSize, MineMap.MapItemColors mapItemColors) {
		int curY;

		//remove any previous block if the road is deeper
		for (curY = y; curY > topY; curY--) {
			new BlockMT(x, curY, z, BlockTypeConverter.convert(Block.AIR_BLOCK.getType())).addTo(blockList);
		}
		//set road block
		new BlockMT(x, topY, z, BlockTypeConverter.convert(block)).addTo(blockList);
		new BlockMT(x, topY-1, z, BlockTypeConverter.convert(block)).addTo(blockList);
		mapItemColors.setColor(x, z, MapItemColor.getColor(block, topY));

		int depth = Math.max(SeaBlockDefinition.SEA_DEPTH_MIN, AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO - map.getGroundLevel(x, y));
		depth = Math.max( Math.min(depth, SeaBlockDefinition.DEEP_SEA_DEPTH), SeaBlockDefinition.SEA_DEPTH_MIN); // repris de SeaBlockDefinition
		if (groundLevel <= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO+1 ) {
			for (int curYBIS = y-depth+2 ; curYBIS<y ; curYBIS++ ) {
				new BlockMT(x, curYBIS, z, BlockTypeConverter.convert(BlockType.WATER)).addTo(blockList);
			}
			map.setGroundLevel(x,y,AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO);
		}
		else {
			int curX, curZ, i;
			for (curX=-5; curX<5; curX++) {
				for (curZ=-5; curZ<5 ; curZ++) {
					if (BlockMT.containsBlock(blockList, curX+x,y,curZ+z) && !BlockMT.getBlock(blockList, curX+x,y,curZ+z).type.equals(BlockTypeConverter.convert(Block.AIR_BLOCK.getType()))
						&& BlockMT.containsBlock(blockList, curX+x,y,curZ+z) && !BlockMT.getBlock(blockList, curX+x,y,curZ+z).type.equals(BlockTypeConverter.convert(BRIDGE_BLOCK.getType()))
						&&y >= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&&groundLevel >= AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&& y>AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO
						&&y<topY
						){
						for (int curYBIS = groundLevel; curYBIS>AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO -3 ; curYBIS-- ) {
							new BlockMT(x, curYBIS, z, BlockTypeConverter.convert(block)).addTo(blockList);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		return false;
	}
	@Override
	public boolean canPutOverlayLayer(int bufferX, int bufferY, int bufferSize) {
		return false;
	}
}
