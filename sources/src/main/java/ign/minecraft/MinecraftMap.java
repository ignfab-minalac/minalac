/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

import developpeur2000.minecraft.minecraft_rw.entity.Item;
import developpeur2000.minecraft.minecraft_rw.entity.ItemFrame;
import developpeur2000.minecraft.minecraft_rw.world.Block;
import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.Chunk;
import developpeur2000.minecraft.minecraft_rw.world.MapItem;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.Region;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.definition.IdentifiedBlockDefinition;
import ign.minecraft.definition.OverlaySimpleBlockDefinition;
import ign.minecraft.definition.SimpleBlocks;
import ign.minecraft.definition.UndergroundCopyBlockDefinition;
import ign.minecraft.importer.AltiImporter;

public class MinecraftMap extends MineMap {
	public MinecraftMap(int size, Path path, String name, Path resourcesPath) {
		super(size,path,name,resourcesPath);
	}
	
	/* (non-Javadoc)
	 * @see ign.minecraft.MineMap#writeToDisk()
	 */
	@Override
	public void writeToDisk() throws IOException, MinecraftGenerationException {
		World minecraftWorld = new World(outputDir);
		//remove any existing file
		for(File file: outputDir.resolve("region").toFile().listFiles()) {
			file.delete();
		}
		for(File file: outputDir.resolve("data").toFile().listFiles()) {
			file.delete();
		}
		minecraftWorld.getLevel().setName(name);
		
		int tableIndex;
		int x, y, z;
		IdentifiedBlockDefinition group;
		//now that block definitions won't be replaced anymore,
		// parse all blocks and fill group blocks
		for(tableIndex = 0; tableIndex < surfaceBlocks.length; tableIndex++) {
			if ( IdentifiedBlockDefinition.class.isAssignableFrom( surfaceBlocks[tableIndex].getClass() ) ) {
				//surface block is part of a group
				group = (IdentifiedBlockDefinition) surfaceBlocks[tableIndex];
				x = tableIndex % mapSize;
				z = tableIndex / mapSize;
				y = groundLevel[tableIndex];
				group.addBlock(x, y, z);
			}
			if ( OverlaySimpleBlockDefinition.class.isAssignableFrom( surfaceBlocks[tableIndex].getClass() )
					&& ((OverlaySimpleBlockDefinition) surfaceBlocks[tableIndex]).getBaseBlockDefinition() != null
					&& IdentifiedBlockDefinition.class.isAssignableFrom(
							((OverlaySimpleBlockDefinition) surfaceBlocks[tableIndex]).getBaseBlockDefinition().getClass() ) ) {
				//surface block is an overlay over a block that is part of a group
				group = (IdentifiedBlockDefinition) ((OverlaySimpleBlockDefinition) surfaceBlocks[tableIndex]).getBaseBlockDefinition();
				x = tableIndex % mapSize;
				z = tableIndex / mapSize;
				y = groundLevel[tableIndex];
				group.addBlock(x, y, z);
			}
		}
		//then do pre-render operations on group blocks
		IdentifiedBlockDefinition.launchPreRender();
		
		//create the world that will give us data for the underground
		World undergroundWorld = null;
		if (!MineGenerator.getDebugMode().isFastGen() && ((MineGenerator.getMode() & MineGenerator.MODE_PLAINUNDERGROUND) != MineGenerator.MODE_PLAINUNDERGROUND)) {
			File undergroundDirFile = resourcesDir.resolve(UNDERGOUNDDATA_DIRNAME).toFile();
			if (!undergroundDirFile.exists()) {
				throw new MinecraftGenerationException(MinecraftGenerationException.Definition.MINECRAFTEXPORT_GLOBAL_NOUNDERGOUNDDATA);
			}
			undergroundWorld = new World(resourcesDir.resolve(UNDERGOUNDDATA_DIRNAME));
			minecraftWorld.getLevel().setRandomSeed( undergroundWorld.getLevel().getRandomSeed() );
		}
		
		//inject blocks region by region to be able to free regions from memory
		int regionXCount = mapSize/Region.BLOCKS;
		if (mapSize % Region.BLOCKS != 0) {
			regionXCount++;
		}
		int regionZCount = mapSize/Region.BLOCKS;
		if (mapSize % Region.BLOCKS != 0) {
			regionZCount++;
		}
		//coordinates written to minecraftWorld are shifted to center the map on 0,0
		int shiftX = mapSize / 2;
		int shiftZ = mapSize / 2;
		int regionXStart = -regionXCount / 2;
		if (regionXCount % 2 != 0) {
			regionXCount++;
			regionXStart--;
		}
		int regionZStart = -regionZCount / 2;
		if (regionZCount % 2 != 0) {
			regionZCount++;
			regionZStart--;
		}
		if (!MineGenerator.getDebugMode().isFastGen()) {
			//on each axis add one region before and after to make a border
			regionXStart--;
			regionXCount+=2;
			regionZStart--;
			regionZCount+=2;
			mapItemColors = new MapItemColors(mapSize + 2 * Region.BLOCKS, shiftX + Region.BLOCKS, shiftZ + Region.BLOCKS);
		} else {
			mapItemColors = new MapItemColors(mapSize, shiftX, shiftZ);
		}
		
		int xInRegion, zInRegion;
		int currentGroundLevel;
		UndergroundCopyBlockDefinition undergroundBlockDefinition = new UndergroundCopyBlockDefinition(undergroundWorld);
		
		for (int regionZ = regionZStart; regionZ < (regionZStart + regionZCount) ; regionZ ++) {
			for (int regionX = regionXStart; regionX < (regionXStart + regionXCount) ; regionX ++) {
				//fill one region
				
				z = regionZ * Region.BLOCKS;
				zInRegion = 0;
				while (zInRegion < Region.BLOCKS) {
					x = regionX * Region.BLOCKS;
					xInRegion = 0;
					while (xInRegion < Region.BLOCKS) {
						if ( x >= -shiftX && x < (mapSize - shiftX)
								&& z >= -shiftZ && z < (mapSize - shiftZ) ) {
							//inside our generated map
							
							tableIndex = (z + shiftZ) * mapSize + x + shiftX;
							
							currentGroundLevel = groundLevel[tableIndex];
							assert currentGroundLevel > 4;
							
							//at start of chunk, copy entities for underground
							if (x % Chunk.BLOCKS == 0 && z % Chunk.BLOCKS == 0) {
								undergroundBlockDefinition.copyEntities(minecraftWorld,
										x, z, groundLevel, mapSize, 4, shiftX, shiftZ);
							}

							//get underground blocks from template (if specified) until the last 3 layers
							undergroundBlockDefinition.render(minecraftWorld, x, currentGroundLevel - 4, z, x + shiftX, z + shiftZ, mapSize, mapItemColors);
							for (y = currentGroundLevel - 3; y < currentGroundLevel; y ++) {
								SimpleBlocks.EARTH.get().render(minecraftWorld, x, y, z, x + shiftX, z + shiftZ, mapSize, mapItemColors);
							}
							//render the current surface block
							surfaceBlocks[tableIndex].render(minecraftWorld, x, currentGroundLevel, z, x + shiftX, z + shiftZ, mapSize, mapItemColors);
						
						} else if ( !MineGenerator.getDebugMode().isFastGen()
								&& ((MineGenerator.getBorderFilling() & MineGenerator.BORDER_FILLING) == MineGenerator.BORDER_FILLING)
								&& (x + shiftX + Region.BLOCKS) >= 0 && (x + shiftX) < (mapSize + Region.BLOCKS)
								&& (z + shiftZ + Region.BLOCKS) >= 0 && (z + shiftZ) < (mapSize + Region.BLOCKS) ) {
							//outside of our map within the border blocks, fill with border blocks up to level 255
							for (y = 0; y <= 64; y ++) {
								SimpleBlocks.BORDERFILLING.get().render(minecraftWorld, x, y, z, x + shiftX, z + shiftZ, mapSize, mapItemColors);
							}

						}
						xInRegion++;
						x++;
					}
					zInRegion++;
					z++;
				}
				
				//unload underground region
				if (undergroundWorld != null) {
					undergroundWorld.unloadRegions();
				}
				
				//save and then unload the region
				minecraftWorld.save(outputDir);
				minecraftWorld.unloadRegions();
			}
		}
		
		//render the map items
		generateMapItems(minecraftWorld);
		minecraftWorld.save(outputDir);
		minecraftWorld.unloadRegions();
	}

	public void generateMapOverview() {
		Runnable minecraftMapper = () -> {
			Logger.getLogger("MinecraftGenerator").log(Level.INFO, "Generating overview...");

			ProcessBuilder mtProcess = new ProcessBuilder( new String[] {"bash","-l","launch_mapping.sh",outputDir.getParent().toFile().getName()+File.separator+outputDir.toFile().getName()})
					.directory(new File("sources/minecraft_mapper"))
					//.redirectOutput(new File("sources/target/mapper_error.txt"))
					//.redirectErrorStream(true)
					.redirectOutput(new File("/dev/null"))
					.redirectInput(new File("/dev/null"));

			try {
				mtProcess.environment().put("BASH_ENV", "/opt/qt510/bin/qt510-env.sh");
				Process process = mtProcess.start();
				process.waitFor();
			} catch (IOException e) {
				Logger.getLogger("MinecraftGenerator").log(Level.SEVERE,"Error while starting Minecraft overview engine " + e.getMessage());
			} catch (InterruptedException e) {
				Logger.getLogger("MinecraftGenerator").log(Level.SEVERE,"Error while running Minecraft overview engine " + e.getMessage());
			}
		};
		Thread mtm = new Thread(minecraftMapper);
		mtm.start();
		try {
			mtm.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * generate map items and save them on file
	 * @param generateBorder 
	 * 
	 * @throws IOException 
	 * 
	 */
	private void generateMapItems(World minecraftWorld) throws IOException {
		if(MineGenerator.getDebugMode().hasDebugInfo()) {
			//debug : save pixel data
			MapItemColor[] colorsDefinitions = MapItemColor.values();
			int colorsSize = mapItemColors.getSize();
			byte[] colors = mapItemColors.getColors();
			byte[] curRGB;
			byte[] pixelBytes = new byte[colorsSize*colorsSize*3];
			for(int x=0; x<colorsSize; x++) {
				for(int y=0; y<colorsSize; y++) {
					curRGB = colorsDefinitions[ ( ((int) colors[y * colorsSize + x]) & 0xff ) / 4 ].getRGB();
					pixelBytes[ (y * colorsSize + x)*3 ] = curRGB[2];
					pixelBytes[ (y * colorsSize + x)*3 + 1 ] = curRGB[1];
					pixelBytes[ (y * colorsSize + x)*3 + 2 ] = curRGB[0];
				}
			}
			BufferedImage image = new BufferedImage(colorsSize, colorsSize, BufferedImage.TYPE_3BYTE_BGR);
		    byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		    System.arraycopy(pixelBytes, 0, imgData, 0, pixelBytes.length);
			File outputfile = new File(MineGenerator.getDebugDir()).toPath().resolve("globalMapItem.png").toFile();
			ImageIO.write(image, "png", outputfile);
		}

		int mapSpawnX = 2;
		int mapSpawnZ = -5;
		int mapGroundLevel = AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO;
		for (int xShift = -1; xShift <= 1; xShift ++) {
			for (int zShift = -1; zShift <= 1; zShift ++) {
				//getGroundLevel needs zero based coordinates, not real minecraft coordinates
				// hence the adding of mapSize/2
				mapGroundLevel = Math.max(mapGroundLevel, getGroundLevel(mapSpawnX + mapSize/2 + xShift, mapSpawnZ + mapSize/2 + zShift));
			}			
		}
		//first generate map item to be picked up
		MapItem droppedMapItem = generateMapItem(MapItem.MAP_SCALE_3, 0, 0);
		//save the map item file
		minecraftWorld.addMapItem(droppedMapItem);
        //set the entity in the world
		minecraftWorld.addEntity(mapSpawnX, mapGroundLevel + 0.5, mapSpawnZ + 1, new Item("minecraft:filled_map", droppedMapItem.getId(), (byte) 1));

		copyElement(minecraftWorld, resourcesDir.resolve(ELEMENTSDATA_DIRNAME).resolve("blood.dat"));
		copyElement(minecraftWorld, resourcesDir.resolve(ELEMENTSDATA_DIRNAME).resolve("d.dat"));
		copyElement(minecraftWorld, resourcesDir.resolve(ELEMENTSDATA_DIRNAME).resolve("fs1.dat"));
		copyElement(minecraftWorld, resourcesDir.resolve(ELEMENTSDATA_DIRNAME).resolve("fs2.dat"));
		copyElement(minecraftWorld, resourcesDir.resolve(ELEMENTSDATA_DIRNAME).resolve("icfd.dat"));
		copyElement(minecraftWorld, resourcesDir.resolve(ELEMENTSDATA_DIRNAME).resolve("ma.dat"));
		copyElement(minecraftWorld, resourcesDir.resolve(ELEMENTSDATA_DIRNAME).resolve("md.dat"));
		copyElement(minecraftWorld, resourcesDir.resolve(ELEMENTSDATA_DIRNAME).resolve("mo.dat"));

		// replace old-style overview by PNG library-generated overview
		generateMapOverview();

		InputStream stream = new FileInputStream(outputDir.resolve("overview.png").toFile());
		BufferedImage image = ImageIO.read(stream);

		if(image != null) {
			OutputStream overviewStream = new FileOutputStream(outputDir.resolve("overview.png").toFile());
			BufferedImage overviewImage = image;

			// force full size overview for sizes which are different than 5km per 5km
			// (could be enhanced by avoiding 5120 size necessity [implies editing generateMapItem() OR wallMapScale with dynamic value] ?)
			//if(mapSize != 10 * MineGenerator.MINECRAFTMAP_MAPTILESIZE) {
				if (!MineGenerator.getDebugMode().isFastGen())
					overviewImage = Scalr.resize(image, Method.QUALITY, 5120 + 2 * Region.BLOCKS, Scalr.OP_ANTIALIAS);
				else
					overviewImage = Scalr.resize(image, Method.QUALITY, 5120, Scalr.OP_ANTIALIAS);
				ImageIO.write(overviewImage, "png", overviewStream);
			//}
			//ImageIO.write(overviewImage, "png", overviewStream);

			// replaces color byte array with converted stuff from PNG image
			byte[] colorsFromImage = null;

			try {
				// TODO enhance image to minecraft .dat conversion
				// could be way better in terms of image quality ! (especially for big maps)
				colorsFromImage = getColorsFromImage(outputDir.resolve("overview.png"),false);
				mapItemColors.setColors(colorsFromImage);
			} catch (MinecraftGenerationException e) {
				Logger.getLogger("MinecraftGenerator").log(Level.SEVERE,"getColorsInImage failed : " + e.getMessage());
			}

			overviewStream.close();
			Files.delete(outputDir.resolve("overview.png").toAbsolutePath());
		}

		stream.close();

		//then generate 3x3 wall of maps
		byte wallMapScale = MapItem.MAP_SCALE_4;
		Block supportBlock = new Block(BlockType.OBSIDIAN);
		int wallMapTileSize = MapItem.MAP_ITEM_SIZE * MapItem.getPixelRatio(wallMapScale);
		MapItem wallMapItem;
		ItemFrame itemFrame;
		for (int mapWallShiftX: new int[] {-1,0,1}) {
			for (int mapWallShiftY: new int[] {-1,0,1}) {
				//generate map item (shift in map y coordinates is inverted compared to y shift in display block)
				wallMapItem = generateMapItem(wallMapScale, mapWallShiftX * wallMapTileSize, -mapWallShiftY * wallMapTileSize);
				//add overlay
				applyImageOnMapItem(wallMapItem, resourcesDir.resolve(MAPITEMDATA_DIRNAME).resolve("3x3-map-overlay.png"),
						(mapWallShiftX + 1) * 128, (1 - mapWallShiftY) * 128 );//y is inverted
				//save the map item file
				minecraftWorld.addMapItem(wallMapItem);
				//place block to support frame
				minecraftWorld.setBlock(mapSpawnX + mapWallShiftX, mapGroundLevel + 3 + mapWallShiftY, mapSpawnZ - 1,
						supportBlock);
				//create frame object
				itemFrame = new ItemFrame(mapSpawnX + mapWallShiftX, mapGroundLevel + 3 + mapWallShiftY, mapSpawnZ,
						ItemFrame.FACING_SOUTH, (byte) 0, "minecraft:filled_map", wallMapItem.getId());
				minecraftWorld.addEntity(mapSpawnX + mapWallShiftX, mapGroundLevel + 3 + mapWallShiftY, mapSpawnZ,
						itemFrame);
				//place air around the frame
				minecraftWorld.getRegionAt(mapSpawnX + mapWallShiftX, mapSpawnZ + 1).setBlock(mapSpawnX + mapWallShiftX, mapGroundLevel + 3 + mapWallShiftY, mapSpawnZ + 1, 
						Block.AIR_BLOCK);
			}
		}
		//ign logo nextby
		wallMapItem = generateMapItemFromImage(resourcesDir.resolve(MAPITEMDATA_DIRNAME).resolve("logo-ign.png"));
		minecraftWorld.addMapItem(wallMapItem);
		//place block to support frame
		minecraftWorld.setBlock(mapSpawnX + 2, mapGroundLevel + 4, mapSpawnZ - 1, supportBlock);
		//create frame object
		itemFrame = new ItemFrame(mapSpawnX + 2, mapGroundLevel + 4, mapSpawnZ,
				ItemFrame.FACING_SOUTH, (byte) 0, "minecraft:filled_map", wallMapItem.getId());
		minecraftWorld.addEntity(mapSpawnX + 2, mapGroundLevel + 4, mapSpawnZ,
				itemFrame);
		//ignfab logo nextby //deactivated
		/*wallMapItem = generateMapItemFromImage(resourcesDir.resolve(MAPITEMDATA_DIRNAME).resolve("logo-ignfab.png"));
		minecraftWorld.addMapItem(wallMapItem);
		//place block to support frame
		minecraftWorld.setBlock(mapSpawnX + 2, mapGroundLevel + 2, mapSpawnZ - 1, supportBlock);
		//create frame object
		itemFrame = new ItemFrame(mapSpawnX + 2, mapGroundLevel + 2, mapSpawnZ,
				ItemFrame.FACING_SOUTH, (byte) 0, "minecraft:filled_map", wallMapItem.getId());
		minecraftWorld.addEntity(mapSpawnX + 2, mapGroundLevel + 2, mapSpawnZ,
				itemFrame);*/
		//place air around the frame
		minecraftWorld.getRegionAt(mapSpawnX + 2, mapSpawnZ + 1).setBlock(mapSpawnX + 2, mapGroundLevel + 2, mapSpawnZ + 1, 
				Block.AIR_BLOCK);

	}
	
	private MapItem generateMapItem(byte scale, int xCenter, int zCenter) throws IOException {
		MapItem mapItem = new MapItem(scale, MapItem.MAP_DIMENSION_OVERWORLD, xCenter, zCenter);
		//full map data
		final int fullMapSize = mapItemColors.getSize();
		final byte[] fullMapColors = mapItemColors.getColors();
		//size of merge buffer is number of blocks in one pixel (= ratio x ratio)
		final int mergeSize = MapItem.getPixelRatio(scale);
		//zero-based coordinates (in map) of first point of the map
		final int mapOriginX = xCenter + fullMapSize/2 - mergeSize * MapItem.MAP_ITEM_SIZE / 2;
		final int mapOriginZ = zCenter + fullMapSize/2 - mergeSize * MapItem.MAP_ITEM_SIZE / 2;
		//buffer that will hold real size pixels to be transformed into one map pixel
		final byte[] mergedColors = new byte[ mergeSize * mergeSize ];
		int x, z, mergeX, mergeZ;
		int fullMapX, fullMapZ, curMapX, curMapZ;
		for (x = 0; x < MapItem.MAP_ITEM_SIZE; x ++) {
			for (z = 0; z < MapItem.MAP_ITEM_SIZE; z ++) {
				fullMapX = mapOriginX + x * mergeSize;
				fullMapZ = mapOriginZ + z * mergeSize;
				if (fullMapX >= 0 && fullMapX < fullMapSize && fullMapZ >= 0 && fullMapZ < fullMapSize) {
					if (mergeSize > 1) {
						for (mergeX = 0; mergeX < mergeSize; mergeX ++) {
							for (mergeZ = 0; mergeZ < mergeSize; mergeZ ++) {
								curMapX = fullMapX - mergeSize/2 + mergeX;
								curMapZ = fullMapZ - mergeSize/2 + mergeZ;
								if (curMapX >= 0 && curMapX < fullMapSize && curMapZ >= 0 && curMapZ < fullMapSize) {
									mergedColors[mergeZ * mergeSize + mergeX] =
											fullMapColors[curMapZ * fullMapSize + curMapX];
								}
							}
						}
						mapItem.setColor(x, z, mergedColors);
					} else {
						mapItem.setColor(x, z, fullMapColors[fullMapZ * fullMapSize + fullMapX]);
					}
				}
			}
		}
		
		if(MineGenerator.getDebugMode().hasDebugInfo()) {
			//debug : save pixel data
			MapItemColor[] colorsDefinitions = MapItemColor.values();
			byte[] curRGB;
			byte[] pixelBytes = new byte[MapItem.MAP_ITEM_SIZE*MapItem.MAP_ITEM_SIZE*3];
			for(int debugX=0; debugX<MapItem.MAP_ITEM_SIZE; debugX++) {
				for(int debugY=0; debugY<MapItem.MAP_ITEM_SIZE; debugY++) {
					curRGB = colorsDefinitions[ (((int) mapItem.getColors()[debugY * MapItem.MAP_ITEM_SIZE + debugX]) & 0xff) / 4 ].getRGB();
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 ] = curRGB[2];
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 + 1 ] = curRGB[1];
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 + 2 ] = curRGB[0];
				}
			}
			BufferedImage image = new BufferedImage(MapItem.MAP_ITEM_SIZE, MapItem.MAP_ITEM_SIZE, BufferedImage.TYPE_3BYTE_BGR);
		    byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		    System.arraycopy(pixelBytes, 0, imgData, 0, pixelBytes.length);
			File outputfile = new File(MineGenerator.getDebugDir()).toPath().resolve("MapItem" + mapItem.getId() + ".png").toFile();
			ImageIO.write(image, "png", outputfile);
		}

		return mapItem;
	}

	private void copyElement(World minecraftWorld, Path filePath) {
		MapItem mapItem;
		try {
			mapItem = minecraftWorld.loadMapItem(filePath);
			Block supportBlock = new Block(BlockType.OBSIDIAN);
			int x,y,z;
			x = (int) (Math.random() * mapSize) - mapSize/2;
			z = (int) (Math.random() * mapSize) - mapSize/2;
			y = (int) (Math.random() * 10) + 4;
			minecraftWorld.addMapItem(mapItem);
			minecraftWorld.setBlock(x-1, y, z-1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x, y, z-1, supportBlock);
			minecraftWorld.setBlock(x+1, y, z-1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x-1, y, z, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x, y, z, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x+1, y, z, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x-1, y, z+1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x, y, z+1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x+1, y, z+1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x-1, y+1, z-1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x, y+1, z-1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x+1, y+1, z-1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x-1, y+1, z, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x, y+1, z, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x+1, y+1, z, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x-1, y+1, z+1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x, y+1, z+1, Block.AIR_BLOCK);
			minecraftWorld.setBlock(x+1, y+1, z+1, Block.AIR_BLOCK);
			ItemFrame itemFrame = new ItemFrame(x, y, z,
					ItemFrame.FACING_SOUTH, (byte) 0, "minecraft:filled_map", mapItem.getId());
			minecraftWorld.addEntity(x, y, z, itemFrame);
		} catch (Exception e) {
		}
	}

	private MapItem generateMapItemFromImage(Path filePath) throws IOException {
		return generateMapItemFromImage(filePath, 0, 0);
	}
	private MapItem generateMapItemFromImage(Path filePath, int shiftX, int shiftY) throws IOException {
		MapItem mapItem = new MapItem(true);
		
		try {
			applyColorsFromImage(mapItem, filePath, shiftX, shiftY, false);
		} catch (MinecraftGenerationException e) {
			assert false;
			return null;
		}
		
		if(MineGenerator.getDebugMode().hasDebugInfo()) {
			//debug : save pixel data
			MapItemColor[] colorsDefinitions = MapItemColor.values();
			byte[] curRGB;
			byte[] pixelBytes = new byte[MapItem.MAP_ITEM_SIZE*MapItem.MAP_ITEM_SIZE*3];
			for(int debugX=0; debugX<MapItem.MAP_ITEM_SIZE; debugX++) {
				for(int debugY=0; debugY<MapItem.MAP_ITEM_SIZE; debugY++) {
					curRGB = colorsDefinitions[ ((int) mapItem.getColors()[debugY * MapItem.MAP_ITEM_SIZE + debugX] & 0xff) / 4 ].getRGB();
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 ] = curRGB[2];
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 + 1 ] = curRGB[1];
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 + 2 ] = curRGB[0];
				}
			}
			BufferedImage image = new BufferedImage(MapItem.MAP_ITEM_SIZE, MapItem.MAP_ITEM_SIZE, BufferedImage.TYPE_3BYTE_BGR);
		    byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		    System.arraycopy(pixelBytes, 0, imgData, 0, pixelBytes.length);
			File outputfile = new File(MineGenerator.getDebugDir()).toPath().resolve("MapItem" + mapItem.getId() + ".png").toFile();
			ImageIO.write(image, "png", outputfile);
		}

		return mapItem;
	}

	@SuppressWarnings("unused")
	private void applyImageOnMapItem(MapItem mapItem, Path filePath) throws IOException {
		applyImageOnMapItem(mapItem, filePath, 0, 0);
	}
	private void applyImageOnMapItem(MapItem mapItem, Path filePath, int shiftX, int shiftY) throws IOException {
		try {
			applyColorsFromImage(mapItem, filePath, shiftX, shiftY, true);
		} catch (MinecraftGenerationException e) {
			assert false;
			return;
		}
		
		if(MineGenerator.getDebugMode().hasDebugInfo()) {
			//debug : save pixel data
			MapItemColor[] colorsDefinitions = MapItemColor.values();
			byte[] curRGB;
			byte[] pixelBytes = new byte[MapItem.MAP_ITEM_SIZE*MapItem.MAP_ITEM_SIZE*3];
			for(int debugX=0; debugX<MapItem.MAP_ITEM_SIZE; debugX++) {
				for(int debugY=0; debugY<MapItem.MAP_ITEM_SIZE; debugY++) {
					curRGB = colorsDefinitions[ ((int) mapItem.getColors()[debugY * MapItem.MAP_ITEM_SIZE + debugX] & 0xff) / 4 ].getRGB();
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 ] = curRGB[2];
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 + 1 ] = curRGB[1];
					pixelBytes[ (debugY * MapItem.MAP_ITEM_SIZE + debugX)*3 + 2 ] = curRGB[0];
				}
			}
			BufferedImage image = new BufferedImage(MapItem.MAP_ITEM_SIZE, MapItem.MAP_ITEM_SIZE, BufferedImage.TYPE_3BYTE_BGR);
		    byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		    System.arraycopy(pixelBytes, 0, imgData, 0, pixelBytes.length);
			File outputfile = new File(MineGenerator.getDebugDir()).toPath().resolve(
					"MapItem" + mapItem.getId() + "+" + filePath.getFileName().toString() + ".png" ).toFile();
			ImageIO.write(image, "png", outputfile);
		}
	}
	
	@SuppressWarnings("unused")
	private void applyColorsFromImage(MapItem mapItem, Path filePath, boolean merge) throws IOException, MinecraftGenerationException {
		applyColorsFromImage(mapItem, filePath, 0, 0, merge);
	}
	private void applyColorsFromImage(MapItem mapItem, Path filePath, int shiftX, int shiftY, boolean merge) throws IOException, MinecraftGenerationException {
		if (!filePath.toFile().exists()) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.MINECRAFTEXPORT_MAPITEM_MISSINGIMAGEFILE);
		}
		
		BufferedImage image = ImageIO.read(filePath.toFile());
		//only manage ABGR and BGR files ( = PNG)
		if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR && image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.MINECRAFTEXPORT_MAPITEM_BADIMAGEFILE);
		}
		int byteShift = image.getType() == BufferedImage.TYPE_4BYTE_ABGR ? 1 : 0;
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

        DataBuffer dataBuffer = image.getData().getDataBuffer();
        assert dataBuffer.getClass().equals(DataBufferByte.class);
        byte[] pixelBytes = ((DataBufferByte) dataBuffer).getData();

        assert (pixelBytes.length == imageWidth * imageHeight * (3 + byteShift));
		if( (shiftX + MapItem.MAP_ITEM_SIZE) > imageWidth || (shiftY + MapItem.MAP_ITEM_SIZE) > imageHeight ) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.MINECRAFTEXPORT_MAPITEM_BADIMAGEFILE);
		}
        
		int pixelByteIndex;
		int pixelValueB, pixelValueG, pixelValueR;
		byte[] curRGB;
		MapItemColor bestColor;
		int curScore, bestColorScore;
		for (int x = 0; x < MapItem.MAP_ITEM_SIZE; x ++) {
			for (int y = 0; y < MapItem.MAP_ITEM_SIZE; y ++) {
				pixelByteIndex = (y + shiftY) * imageWidth + x + shiftX;
				pixelValueB = (int) pixelBytes[pixelByteIndex * (3 + byteShift) + byteShift] & 0xff;
				pixelValueG = (int) pixelBytes[pixelByteIndex * (3 + byteShift) + 1 + byteShift] & 0xff;
				pixelValueR = (int) pixelBytes[pixelByteIndex * (3 + byteShift) + 2 + byteShift] & 0xff;
				
				//if merge, only apply color if not transparent
				// => apply color if not merge, or image has no transparency, or alpha channel is not zero
				if (!merge || image.getType() != BufferedImage.TYPE_4BYTE_ABGR
								||  pixelBytes[pixelByteIndex * (3 + byteShift)] != 0) {
					//compute match of each map item colors with current color
					bestColor = null;
					bestColorScore = 255*255*3;//score between black and white
					for (MapItemColor curColor : MapItemColor.values()) {
						if (curColor != MapItemColor.TRANSPARENT && curColor.ordinal() < 31) {
							curRGB = curColor.getRGB();
							//super bad score evaluation, color should be compared in
							// a more human eye friendly color space such as LAB
							curScore = (((int) curRGB[0] & 0xff) - pixelValueR) * (((int) curRGB[0] & 0xff) - pixelValueR)
									+ (((int) curRGB[1] & 0xff) - pixelValueG) * (((int) curRGB[1] & 0xff) - pixelValueG)
									+ (((int) curRGB[2] & 0xff) - pixelValueB) * (((int) curRGB[2] & 0xff) - pixelValueB);
							if (curScore < bestColorScore) {
								bestColorScore = curScore;
								bestColor = curColor;
							}
						}
					}
					
					assert bestColor != null;
					mapItem.setColor(x, y, bestColor.getColorByte());
				}
			}
		}
	}

	private byte[] getColorsFromImage(Path filePath, boolean merge) throws IOException, MinecraftGenerationException {
		if (!filePath.toFile().exists()) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.MINECRAFTEXPORT_MAPITEM_MISSINGIMAGEFILE);
		}

		BufferedImage image = ImageIO.read(filePath.toFile());
		//only manage ABGR and BGR files ( = PNG)
		if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR && image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			throw new MinecraftGenerationException(MinecraftGenerationException.Definition.MINECRAFTEXPORT_MAPITEM_BADIMAGEFILE);
		}
		int byteShift = image.getType() == BufferedImage.TYPE_4BYTE_ABGR ? 1 : 0;
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

        DataBuffer dataBuffer = image.getData().getDataBuffer();
        assert dataBuffer.getClass().equals(DataBufferByte.class);
        byte[] pixelBytes = ((DataBufferByte) dataBuffer).getData();

        byte[] colors = new byte[imageWidth*imageHeight];

        assert (pixelBytes.length == imageWidth * imageHeight * (3 + byteShift));

		int pixelByteIndex;
		int pixelValueB, pixelValueG, pixelValueR;
		byte[] curRGB;
		MapItemColor bestColor;
		int curScore, bestColorScore;
		for (int x = 0; x < imageWidth; x ++) {
			for (int y = 0; y < imageHeight; y ++) {
				pixelByteIndex = y * imageWidth + x;
				pixelValueB = (int) pixelBytes[pixelByteIndex * (3 + byteShift) + byteShift] & 0xff;
				pixelValueG = (int) pixelBytes[pixelByteIndex * (3 + byteShift) + 1 + byteShift] & 0xff;
				pixelValueR = (int) pixelBytes[pixelByteIndex * (3 + byteShift) + 2 + byteShift] & 0xff;

				//if merge, only apply color if not transparent
				// => apply color if not merge, or image has no transparency, or alpha channel is not zero
				if (!merge || image.getType() != BufferedImage.TYPE_4BYTE_ABGR
								||  pixelBytes[pixelByteIndex * (3 + byteShift)] != 0) {
					//compute match of each map item colors with current color
					bestColor = null;
					bestColorScore = 255*255*3;//score between black and white
					for (MapItemColor curColor : MapItemColor.values()) {
						if (curColor != MapItemColor.TRANSPARENT && curColor.ordinal() < 31) {
							curRGB = curColor.getRGB();
							//super bad score evaluation, color should be compared in
							// a more human eye friendly color space such as LAB
							curScore = (((int) curRGB[0] & 0xff) - pixelValueR) * (((int) curRGB[0] & 0xff) - pixelValueR)
									+ (((int) curRGB[1] & 0xff) - pixelValueG) * (((int) curRGB[1] & 0xff) - pixelValueG)
									+ (((int) curRGB[2] & 0xff) - pixelValueB) * (((int) curRGB[2] & 0xff) - pixelValueB);
							if (curScore < bestColorScore) {
								bestColorScore = curScore;
								bestColor = curColor;
							}
						}
					}

					assert bestColor != null;
					colors[ y * imageWidth + x ] = bestColor.getColorByte();
				}
			}
		}
		return colors;
	}
}
