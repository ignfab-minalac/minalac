/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

import java.io.File;

import developpeur2000.minecraft.minecraft_rw.entity.BlockEntity;
import developpeur2000.minecraft.minecraft_rw.entity.Entity;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.Chunk;
import developpeur2000.minecraft.minecraft_rw.world.Region;
import developpeur2000.minecraft.minecraft_rw.world.World;

public class MinecraftLibTester {

	public MinecraftLibTester() {
	}

	public static void main(String[] args) {
		String sourceFolder = (args.length >= 1) ? args[0] : null;
		File sourceDir = (sourceFolder != null) ? new File(sourceFolder) : null;
		if(sourceDir == null) {
			System.out.println("map de test non trouvee");
			return;
		}
		String destFolder = (args.length >= 2) ? args[1] : null;
		File destDir = (destFolder != null) ? new File(destFolder) : null;
		if(destDir == null) {
			System.out.println("chemin de destination non trouve");
			return;
		}
		try {
			World sourceWorld = new World(sourceDir.toPath());

			World destWorld = new World(destDir.toPath());

			//inject blocks region by region to be able to free regions from memory
			int regionXCount = 1;
			int regionZCount = 1;
			int regionXStart = 0;
			int regionZStart = 0;
			
			int x, y, z;
			int xInRegion, zInRegion;
			for (int regionX = regionXStart; regionX < (regionXStart + regionXCount) ; regionX ++) {
				for (int regionZ = regionZStart; regionZ < (regionZStart + regionZCount) ; regionZ ++) {
					x = regionX * Region.BLOCKS;
					xInRegion = 0;
					while (xInRegion < Region.BLOCKS) {
						z = regionZ * Region.BLOCKS;
						zInRegion = 0;
						while (zInRegion < Region.BLOCKS) {

							for (y = 0; y < 60; y++) {
								if (sourceWorld.getBlock(x, y, z).getType()==BlockType.MOB_SPAWNER) {
									System.out.println("spawner found : "+x+","+y+","+z);
								}
								destWorld.setBlock(x, y, z, sourceWorld.getBlock(x, y, z));
							}
							
							if (xInRegion % Chunk.BLOCKS == 0 && zInRegion % Chunk.BLOCKS == 0) {
								for (Entity entity : sourceWorld.listEntitiesInChunk(x, z)) {
									destWorld.addEntity(entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ(), entity);
								}
								for (BlockEntity blockEntity : sourceWorld.listBlockEntitiesInChunk(x, z)) {
									System.out.println("adding block entity "+blockEntity.toString());
									destWorld.addBlockEntity(blockEntity.getX(), blockEntity.getY(), blockEntity.getZ(), blockEntity);
								}
							}
							
							zInRegion++;
							z++;
						}
						xInRegion++;
						x++;
					}
					
					//save and then unload the region
					destWorld.save();
					destWorld.unloadRegions();
					sourceWorld.unloadRegions();
				}
			}
				
				
			System.out.println("fini !");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
