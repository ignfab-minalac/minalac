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
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.MineMap;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockTypeConverter;

public class WaterSurfaceBlockDefinition extends IdentifiedBlockDefinition {

	static final Block WATER_BLOCK = new Block(BlockType.FLOWING_WATER);
	static final Block RIVERBED_BLOCK = new Block(BlockType.SAND);
	static final int SURFACE_DEPTH = 3;
	
	protected static Map<Integer, Map<Integer, Integer>> baseWaterAltitudes;//given from imported data
	public static int[] waterAltitudes;//computed from pondered neighbouring imported data
	public static boolean waterAltitudesComputed = false;
	
	public static void setWaterAltitudes(Map<Integer, Map<Integer, Integer>> definedWaterAltitudes) {
		baseWaterAltitudes = definedWaterAltitudes;
	}
	public static void computeAllWaterAltitudes() {
		waterAltitudes = new int[mapSize * mapSize];
		//compute water altitudes based on 3 nearest indicated water altitudes
		int curX, curZ;//planar coordinates of the currently treated water point
		int curId;//id of the water surface at the current point
		
		int otherX, otherZ;//currently searched coordinate in the loop
		int[] curAltitudes = new int[1024];//values of reference altitudes
		int[] curAltitudesDist = new int[1024];//weight (that will be inverted) of these altitudes on the currently computed position
		int curAltitudeIndex;//index of the next altitude to be found in the loop
		double computedAltitude, totalWeight;//to finalize the computing
		
		for (curX = 0; curX < mapSize; curX ++) {
			for (curZ = 0; curZ < mapSize; curZ ++) {
				if ( blocks[xz1D(curX, curZ)] != null
						&& WaterSurfaceBlockDefinition.class.isAssignableFrom( blocks[xz1D(curX, curZ)].getClass() ) ) {
					curId = ((WaterSurfaceBlockDefinition) blocks[xz1D(curX, curZ)]).getId();
					assert baseWaterAltitudes.containsKey(curId);
					Map<Integer, Integer> curSurfaceReferenceAltitudes = baseWaterAltitudes.get(curId);
					
					if ( curSurfaceReferenceAltitudes.containsKey(xz1D(curX, curZ)) ) {
						//we are on an altitude indication, simply copy it
						waterAltitudes[xz1D(curX, curZ)] = curSurfaceReferenceAltitudes.get(xz1D(curX, curZ));
					} else {
						//compute water altitude from other altitudes with weighting
						curAltitudeIndex = 0;
						totalWeight = 0;
						if (curAltitudes.length < curSurfaceReferenceAltitudes.size()) {
							curAltitudes = new int[curSurfaceReferenceAltitudes.size()];
							curAltitudesDist = new int[curSurfaceReferenceAltitudes.size()];
						}
						for (Map.Entry<Integer, Integer> otherRefAlti : curSurfaceReferenceAltitudes.entrySet()) {
							otherX = otherRefAlti.getKey() % mapSize;
							otherZ = otherRefAlti.getKey() / mapSize;
							curAltitudes[curAltitudeIndex] = otherRefAlti.getValue();
							//to use an int array we store dist, but weight is actually 1/dist
							curAltitudesDist[curAltitudeIndex] = (otherX - curX) * (otherX - curX)
									+ (otherZ - curZ) * (otherZ - curZ);
							totalWeight += 1 / ((double) curAltitudesDist[curAltitudeIndex]);
							curAltitudeIndex ++;
						}
						
						//compute the altitude from found reference altitudes
						computedAltitude = 0;
						for (int i = 0; i < curAltitudeIndex; i ++) {
							//weight is 1/distance
							computedAltitude += ((double) curAltitudes[i]) / curAltitudesDist[i];
						}
						computedAltitude = computedAltitude / totalWeight;
						
						waterAltitudes[xz1D(curX, curZ)] = (int) Math.round(computedAltitude);
					}
				}
			}			
		}
	}
	
	
	private int sumAltitudes;
	public int getAverageY() {
		return averageY;
	}
	private int nbAltitudes;
	private int averageY;
	
	public WaterSurfaceBlockDefinition(Integer id) {
		super(id);
	}

	//in pre-render, store the minimum y level of the water surface, to use it as ground level
	@Override
	protected void preRenderInit() {
		//launch interpolation of water blocks altitude the first time
		if (!waterAltitudesComputed) {
			computeAllWaterAltitudes();
		}
		sumAltitudes = 0;
		nbAltitudes = 0;
	}
	@Override
	protected void preRender(int x, int z) {
		if (waterAltitudes[xz1D(x, z)] != 0) {
			sumAltitudes += waterAltitudes[xz1D(x, z)];
			nbAltitudes ++;
		}
	}
	@Override
	protected void preRenderEnd() {
		averageY = nbAltitudes > 0 ? (sumAltitudes / nbAltitudes) : 0;
	}

	
	@Override
	protected void renderOneBlock(World world, int x, int y, int z, int bufferX, int bufferY, MineMap.MapItemColors mapItemColors) {
		int surfaceY = waterAltitudes[xz1D(bufferX, bufferY)] == 0 ?
				(averageY == 0 ? y : averageY )
				: waterAltitudes[xz1D(bufferX, bufferY)];
		
		//set water blocks down to depth
		for (int curDepth = 0; curDepth < SURFACE_DEPTH; curDepth ++) {
			world.setBlock(x, surfaceY - curDepth, z, WATER_BLOCK);
		}
		//set riverbed block
		world.setBlock(x, surfaceY - SURFACE_DEPTH, z, RIVERBED_BLOCK);
		//make sure there is no old block between surfaceY and y
		for (int curY = surfaceY + 1; curY <= y; curY ++) {
			world.setBlock(x, curY, z, Block.AIR_BLOCK);
		}
		//map color
		mapItemColors.setColor(x, z, MapItemColor.getColor(WATER_BLOCK, surfaceY));
	}
	
	@Override
	protected void renderOneBlock(Map<BlockMT,Object> blockList, int x, int y, int z, int bufferX, int bufferY,
			MineMap.MapItemColors mapItemColors) {
		int surfaceY = waterAltitudes[xz1D(bufferX, bufferY)] == 0 ?
				(averageY == 0 ? y : averageY )
				: waterAltitudes[xz1D(bufferX, bufferY)];
		
		//set water blocks down to depth
		for (int curDepth = 0; curDepth < SURFACE_DEPTH; curDepth ++) {
			new BlockMT(x, surfaceY - curDepth, z, BlockTypeConverter.convert(WATER_BLOCK.getType())).addTo(blockList);
		}
		//set riverbed block
		new BlockMT(x, surfaceY - SURFACE_DEPTH, z, BlockTypeConverter.convert(RIVERBED_BLOCK.getType())).addTo(blockList);
		//make sure there is no old block between surfaceY and y
		for (int curY = surfaceY + 1; curY <= y; curY ++) {
			new BlockMT(x, curY, z, BlockTypeConverter.convert(Block.AIR_BLOCK.getType())).addTo(blockList);
		}
		//map color
		mapItemColors.setColor(x, z, MapItemColor.getColor(WATER_BLOCK, surfaceY));
	}
	
	@Override
	public boolean canBeReplaced(String ImporterName, int bufferX, int bufferY, int bufferSize) {
		switch (ImporterName) {
		case "BuildingsImporter":
		case "LinearConstructionsImporter":
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
