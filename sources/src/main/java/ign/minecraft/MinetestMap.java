/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.iq80.leveldb.util.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opengis.referencing.operation.MathTransform;
import org.zeromq.ZMQ;

import com.sk89q.worldedit.internal.gson.GsonBuilder;
import com.sk89q.worldedit.internal.gson.JsonParser;

import developpeur2000.minecraft.minecraft_rw.world.BlockType;
import developpeur2000.minecraft.minecraft_rw.world.Chunk;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.Region;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.definition.IdentifiedBlockDefinition;
import ign.minecraft.definition.OverlaySimpleBlockDefinition;
import ign.minecraft.definition.SimpleBlocks;
import ign.minecraft.definition.UndergroundCopyBlockDefinition;
import ign.minecraft.importer.AltiImporter;
import ignfab.minetest.BlockMT;
import ignfab.minetest.BlockSender;
import ignfab.minetest.BlockTypeConverter;
import ignfab.minetest.LuaModGenerator;
import ignfab.minetest.MapProto;

public class MinetestMap extends MineMap {
	
	private Map<BlockMT,Object> blockList;
	public static final Object PRESENT = new Object();
	private int nbBlocks;
	
	private byte[] answer = "".getBytes();
	private int totalBlocks;
	private int port;
	private int chunkSize = 4096;
	private int increment=Math.min(chunkSize,totalBlocks);
	private int actual=0;
	private boolean connected=false;
	private ZMQ.Socket publisher;
	
	private int mapSpawnX = 2;
	private int mapSpawnZ = -5;
	private int mapGroundLevel = AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO;
	
	public MinetestMap(int size, Path path, String name, Path resourcesPath) {
		super(size,path,name,resourcesPath);
		blockList = new LinkedHashMap<BlockMT,Object>(chunkSize);
	}

	@Override
	public void writeToDisk() throws IOException, MinecraftGenerationException {
		World minecraftWorld = new World(outputDir);
		//remove any existing file
		FileUtils.deleteRecursively(outputDir.resolve("region").toFile());
		FileUtils.deleteRecursively(outputDir.resolve("data").toFile());
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
		int shiftXMinusTableIndex, shiftZMinusTableIndex, shiftXPlusTableIndex, shiftZPlusTableIndex;
		
		Logger.getLogger("MinecraftGenerator").log(Level.INFO, "Starting to work...");
		
		Thread mte = startCommunication();
		
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
							undergroundBlockDefinition.render(blockList, x, currentGroundLevel - 4, z, x + shiftX, z + shiftZ, mapSize, mapItemColors);
							for (y = currentGroundLevel - 3; y < currentGroundLevel; y ++) {
								SimpleBlocks.EARTH.get().render(blockList, x, y, z, x + shiftX, z + shiftZ, mapSize, mapItemColors);
							}

							// groundLevel array indexes for levels around x,z (plus&minus)
							shiftZMinusTableIndex = ((z + shiftZ-1) * mapSize + x + shiftX);
							shiftZPlusTableIndex = ((z + shiftZ+1) * mapSize + x + shiftX);
							shiftXMinusTableIndex = ((z + shiftZ) * mapSize + x + shiftX-1);
							shiftXPlusTableIndex = ((z + shiftZ) * mapSize + x + shiftX+1);
							// fill underground gaps (especially in relief mode)
							if(shiftZMinusTableIndex < groundLevel.length && shiftZMinusTableIndex > 0
									&& currentGroundLevel-4-groundLevel[shiftZMinusTableIndex] > UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2) {
								new BlockMT(x, groundLevel[shiftZMinusTableIndex], z, BlockTypeConverter.convert(BlockType.BEDROCK)).addTo(blockList);
								for (int curY = groundLevel[shiftZMinusTableIndex]+1; curY <= currentGroundLevel - 4 - (UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2); curY ++) {
									new BlockMT(x, curY, z, BlockTypeConverter.convert(BlockType.STONE)).addTo(blockList);
								}
							} //else
							if(shiftZPlusTableIndex < groundLevel.length && shiftZPlusTableIndex > 0
									&& currentGroundLevel-4-groundLevel[shiftZPlusTableIndex] > UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2) {
								new BlockMT(x, groundLevel[shiftZPlusTableIndex], z, BlockTypeConverter.convert(BlockType.BEDROCK)).addTo(blockList);
								for (int curY = groundLevel[shiftZPlusTableIndex]+1; curY <= currentGroundLevel - 4 - (UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2); curY ++) {
									new BlockMT(x, curY, z, BlockTypeConverter.convert(BlockType.STONE)).addTo(blockList);
								}
							} //else
							if(shiftXMinusTableIndex < groundLevel.length && shiftXMinusTableIndex > 0
									&& currentGroundLevel-4-groundLevel[shiftXMinusTableIndex] > UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2) {
								new BlockMT(x, groundLevel[shiftXMinusTableIndex], z, BlockTypeConverter.convert(BlockType.BEDROCK)).addTo(blockList);
								for (int curY = groundLevel[shiftXMinusTableIndex]+1; curY <= currentGroundLevel - 4 - (UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2); curY ++) {
									new BlockMT(x, curY, z, BlockTypeConverter.convert(BlockType.STONE)).addTo(blockList);
								}
							} //else
							if(shiftXPlusTableIndex < groundLevel.length && shiftXPlusTableIndex > 0
									&& currentGroundLevel-4-groundLevel[shiftXPlusTableIndex] > UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2) {
								new BlockMT(x, groundLevel[shiftXPlusTableIndex], z, BlockTypeConverter.convert(BlockType.BEDROCK)).addTo(blockList);
								for (int curY = groundLevel[shiftXPlusTableIndex]+1; curY <= currentGroundLevel - 4 - (UndergroundCopyBlockDefinition.UNDERGROUND_DEFINITION_LIMIT/2); curY ++) {
									new BlockMT(x, curY, z, BlockTypeConverter.convert(BlockType.STONE)).addTo(blockList);
								}
							}

							surfaceBlocks[tableIndex].render(blockList, x, currentGroundLevel, z, x + shiftX, z + shiftZ, mapSize, mapItemColors);
						} else if ( !MineGenerator.getDebugMode().isFastGen()
								&& (x + shiftX + Region.BLOCKS) >= 0 && (x + shiftX) < (mapSize + Region.BLOCKS)
								&& (z + shiftZ + Region.BLOCKS) >= 0 && (z + shiftZ) < (mapSize + Region.BLOCKS) ) {
							//outside of our map within the border blocks, fill with border blocks up to level 255
							for (y = 0; y <= 64; y ++) {
								//SimpleBlocks.BORDERFILLING.get().render(blockList, x, y, z, x + shiftX, z + shiftZ, mapSize, mapItemColors);
							}

						}
						xInRegion++;
						x++;
						if(blockList.size()>=chunkSize) {
							nbBlocks += blockList.size();
							totalBlocks = blockList.size();
							increment=Math.min(chunkSize, totalBlocks-actual);
							while(actual<totalBlocks) {
								answer = publisher.recv();
								communicate();
							}
							blockList = new LinkedHashMap<BlockMT,Object>(chunkSize);
							actual = 0;
						}
					}
					zInRegion++;
					z++;
				}
				
				//unload underground region
				if (undergroundWorld != null) {
					undergroundWorld.unloadRegions();
				}
					
				//minecraftWorld.save(outputDir);
			}
		}
		
		for (int xShift = -1; xShift <= 1; xShift ++) {
			for (int zShift = -1; zShift <= 1; zShift ++) {
				//getGroundLevel needs zero based coordinates, not real minecraft coordinates
				// hence the adding of mapSize/2
				mapGroundLevel = Math.max(mapGroundLevel, getGroundLevel(mapSpawnX + mapSize/2 + xShift, mapSpawnZ + mapSize/2 + zShift));
			}			
		}
		
		generateMapItems();
			
		nbBlocks += blockList.size();
		if(blockList.size()!=0) {
			answer = publisher.recv();
			totalBlocks = blockList.size();
			increment=Math.min(chunkSize, totalBlocks-actual);
			communicate();
		}
		
		//minecraftWorld.unloadRegions();
		try {
			Logger.getLogger("MinecraftGenerator").log(Level.INFO, "Total blocks written " + nbBlocks);
			
			stopCommunication();
			while(!new String(answer).equals("OK"))
				answer = publisher.recv();
			blockList = null;
			System.gc();
			
			publisher.close();
			mte.join();
		} catch (InterruptedException e) {
			Logger.getLogger("MinecraftGenerator").log(Level.SEVERE, "BlockSender crashed. Exception: " + e.getMessage());
		}
		
		Files.copy(Paths.get("sources/minetest_engine/boilerplate/env_meta.txt"), Paths.get(outputDir.toAbsolutePath()+"/env_meta.txt"), REPLACE_EXISTING);
		Files.copy(Paths.get("sources/minetest_engine/map_" + port + ".sqlite"), Paths.get(outputDir.toAbsolutePath()+"/map.sqlite"), REPLACE_EXISTING);
		Files.copy(Paths.get("sources/minetest_engine/boilerplate/map_meta.txt"), Paths.get(outputDir.toAbsolutePath()+"/map_meta.txt"), REPLACE_EXISTING);
		Files.copy(Paths.get("sources/minetest_engine/boilerplate/world.mt"), Paths.get(outputDir.toAbsolutePath()+"/world.mt"), REPLACE_EXISTING);
		Files.createDirectory(Paths.get(outputDir.toAbsolutePath()+"/worldmods"));
		Files.createDirectory(Paths.get(outputDir.toAbsolutePath()+"/worldmods/respawn"));
		Files.createDirectory(Paths.get(outputDir.toAbsolutePath()+"/worldmods/ignfab"));
		Files.createDirectory(Paths.get(outputDir.toAbsolutePath()+"/worldmods/ignfab/textures"));
		Files.copy(resourcesDir.resolve(MAPITEMDATA_DIRNAME).resolve("logo-ign.png"), Paths.get(outputDir.toAbsolutePath()+"/worldmods/ignfab/textures/logo-ign.png"));
		Files.copy(resourcesDir.resolve(MAPITEMDATA_DIRNAME).resolve("logo-ignfab.png"), Paths.get(outputDir.toAbsolutePath()+"/worldmods/ignfab/textures/logo-ignfab.png"));
		FileUtils.copyDirectoryContents(resourcesDir.resolve(MAPMODS_DIRNAME).toFile(), Paths.get(outputDir.toAbsolutePath()+"/worldmods/").toFile());
		Files.createFile(Paths.get(outputDir.toAbsolutePath()+"/worldmods/respawn/init.lua"));
		Files.createFile(Paths.get(outputDir.toAbsolutePath()+"/worldmods/ignfab/init.lua"));
		Files.createFile(Paths.get(outputDir.toAbsolutePath()+"/geometry.dat"));
		Files.delete(Paths.get("sources/minetest_engine/map_" + port + ".sqlite"));

		String spawnPoint = "minetest.register_on_respawnplayer(function(player)\n" + 
				"    player:setpos({x=" + mapSpawnX +", y="+(mapGroundLevel+1)+", z="+-mapSpawnZ+"})\n" + 
				"    return true\n" + 
				"end)\n" +
				"minetest.register_on_newplayer(function(player)\n" + 
				"    player:setpos({x=" + mapSpawnX +", y="+(mapGroundLevel+1)+", z="+-mapSpawnZ+"})\n" +
				"    return true\n" + 
				"end)\n" +
				"minetest.register_on_joinplayer(function(player)\n" + 
				"    local settings = {}\n" + 
				"    settings.height = \"" + (AltiImporter.MAX_ALTI+50) + "\"\n" + // Changer plus tard pour que ce soit plutôt l'alti max des bâtiments qui soit prise (et non pas les altis de base)
				"    player:set_clouds(settings)\n" + 
				"    minetest.set_player_privs(\"singleplayer\", minetest.registered_privileges)\n" +
				"    return true\n" + 
				"end)\n" +
				"minetest.register_on_generated(function(minp, maxp, seed)\n" + 
				"	local vm = minetest.get_voxel_manip(minp, maxp)\n" + 
				"	vm:set_lighting({day = 15, night = 0}, minp, maxp)\n" + 
				"	vm:update_liquids()\n" + 
				"	vm:write_to_map()\n" + 
				"	vm:update_map()\n" + 
				"end)";
		Files.write(Paths.get(outputDir.toAbsolutePath()+"/worldmods/respawn/init.lua"), spawnPoint.getBytes(), StandardOpenOption.WRITE);
		
		String geoJson = new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(generateGeographyJson()));
		//String geoJson = generateGeographyJson();
		Files.write(Paths.get(outputDir.toAbsolutePath()+"/geometry.dat"), geoJson.getBytes(), StandardOpenOption.WRITE);
		
		generateMapOverview();
	}
	
	private void generateMapOverview() throws IOException {
		Runnable minetestMapper = () -> { 
			String[] cmdAsTokens;
			
			if(SystemUtils.IS_OS_LINUX) {
				Logger.getLogger("MinecraftGenerator").log(Level.INFO, "Generating overview...");
				cmdAsTokens = ("./minetestmapper -i ../../"+ outputDir +" -o ../../"+outputDir+"/worldmods/ignfab/textures/overview.png").split(" ");
			} else {
				cmdAsTokens = ("minetestmapper.exe -i ../../"+ outputDir + "/minetest_alac -o ../../"+outputDir+"/worldmods/ignfab/textures/overview.png").split(" ");
			}
			
			ProcessBuilder mtProcess = new ProcessBuilder( cmdAsTokens )
					.directory(new File("sources/minetest_mapper"))
					.redirectErrorStream(true)
					.redirectOutput(new File("/dev/null"))
					.redirectInput(new File("/dev/null"));
			
			try {
				Process process = mtProcess.start();
				process.waitFor();
			} catch (IOException e) {
				Logger.getLogger("MinecraftGenerator").log(Level.SEVERE, "Error while starting Minetest engine " + e.getMessage());
			} catch (InterruptedException e) {
				Logger.getLogger("MinecraftGenerator").log(Level.SEVERE, "Error while running Minetest engine " + e.getMessage());
			}
		};
		Thread mtm = new Thread(minetestMapper);
		mtm.start();
		try {
			mtm.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Logger.getLogger("MinecraftGenerator").log(Level.INFO, "Overview generation finished");
		
		//get overview image and resize it to 1000*1000
		File overviewFile = Paths.get(outputDir.toAbsolutePath()+"/worldmods/ignfab/textures/overview.png").toFile();
		BufferedImage overviewImage = ImageIO.read(overviewFile);
		overviewImage = Scalr.resize(overviewImage, Method.QUALITY, 1000, Scalr.OP_ANTIALIAS);
		
		File overlayFile = resourcesDir.resolve(MAPITEMDATA_DIRNAME).resolve("3x3-map-overlay.png").toFile();
		BufferedImage overlayImage = ImageIO.read(overlayFile);
		
		//add overlay
		BufferedImage combined = new BufferedImage(overviewImage.getWidth(), overviewImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = combined.getGraphics();
		g.drawImage(overviewImage, 0, 0, null);
		int centeredX = (overviewImage.getWidth() - overlayImage.getWidth())/2;
		int centeredY = (overviewImage.getHeight() - overlayImage.getHeight())/2;
		g.drawImage(overlayImage, centeredX, centeredY, null);

		//write final overview image
		ImageIO.write(combined, "PNG", overviewFile);
		
		String ignfabBlocks = LuaModGenerator.getInitLua(
				mapSpawnX, mapGroundLevel, mapSpawnZ,
				!(!MineGenerator.getDebugMode().isFastGen() && ((MineGenerator.getMode() & MineGenerator.MODE_PLAINUNDERGROUND) != MineGenerator.MODE_PLAINUNDERGROUND))
		);
		
		Files.write(Paths.get(outputDir.toAbsolutePath()+"/worldmods/ignfab/init.lua"), ignfabBlocks.getBytes(), StandardOpenOption.WRITE);
	}
	
	@SuppressWarnings("unchecked")
	private String generateGeographyJson() throws MinecraftGenerationException {
		// Ecriture du fichier geometry.dat
		double longitude = MineGenerator.MINECRAFTMAP_CENTER.getX();
		double latitude = MineGenerator.MINECRAFTMAP_CENTER.getY();

		double offset = (MineGenerator.MINECRAFTMAP_MAPNBREGIONS * MineGenerator.MINECRAFTMAP_MAPTILESIZE);

		DirectPosition2D pos = Utilities.convertGeoToCarto(latitude, longitude);
		String epsg = Utilities.getLocalZone(longitude, latitude).crsName;

		double realworldXMin = pos.getX() - offset/2;
		double realworldYMin = pos.getY() - offset/2;
		double realworldXMax = pos.getX() + offset/2;
		double realworldYMax = pos.getY() + offset/2;

		double[] srcPtsCarto = new double[] {realworldXMin, realworldYMax,
				realworldXMax, realworldYMax,
				realworldXMax, realworldYMin,
				realworldXMin, realworldYMin};

		if(MineGenerator.MINECRAFTMAP_ANGLE != 0) {
			double[] XMinYMax = {realworldXMin, realworldYMax};
			AffineTransform.getRotateInstance(MineGenerator.MINECRAFTMAP_ANGLE * (Math.PI / 180), pos.getCoordinate()[0], pos.getCoordinate()[1])
			  .transform(XMinYMax, 0, XMinYMax, 0, 1);

			double[] XMaxYMax = {realworldXMax, realworldYMax};
			AffineTransform.getRotateInstance(MineGenerator.MINECRAFTMAP_ANGLE * (Math.PI / 180), pos.getCoordinate()[0], pos.getCoordinate()[1])
			  .transform(XMaxYMax, 0, XMaxYMax, 0, 1);

			double[] XMaxYMin = {realworldXMax, realworldYMin};
			AffineTransform.getRotateInstance(MineGenerator.MINECRAFTMAP_ANGLE * (Math.PI / 180), pos.getCoordinate()[0], pos.getCoordinate()[1])
			  .transform(XMaxYMin, 0, XMaxYMin, 0, 1);

			double[] XMinYMin = {realworldXMin, realworldYMin};
			AffineTransform.getRotateInstance(MineGenerator.MINECRAFTMAP_ANGLE * (Math.PI / 180), pos.getCoordinate()[0], pos.getCoordinate()[1])
			  .transform(XMinYMin, 0, XMinYMin, 0, 1);

			srcPtsCarto = new double[] {XMinYMax[0], XMinYMax[1],
					XMaxYMax[0],XMaxYMax[1],
					XMaxYMin[0],XMaxYMin[1],
					XMinYMin[0],XMinYMin[1]
			};
		}

		//transform the meter based local coordinates back into realworld WGS84 coordinates
		//(in order to get the local projection deformation)
		MathTransform transformLocalToWgs84 = null;
		double[] destPtsGeo = new double[8];

		try {
			transformLocalToWgs84 = CRS.findMathTransform(CRS.decode(epsg), CRS.decode("EPSG:4326"));
			transformLocalToWgs84.transform(srcPtsCarto, 0, destPtsGeo, 0, 4);
		} catch (Exception e) {
			//unexpected
			assert false;
		}

		// Geo coordinates
		JSONArray coordinatesGeo = new JSONArray();
				
		JSONArray coordGeo1 = new JSONArray();
		coordGeo1.add(destPtsGeo[0]);
		coordGeo1.add(destPtsGeo[1]);
		
		JSONArray coordGeo2 = new JSONArray();
		coordGeo2.add(destPtsGeo[2]);
		coordGeo2.add(destPtsGeo[3]);
		
		JSONArray coordGeo3 = new JSONArray();
		coordGeo3.add(destPtsGeo[4]);
		coordGeo3.add(destPtsGeo[5]);
		
		JSONArray coordGeo4 = new JSONArray();
		coordGeo4.add(destPtsGeo[6]);
		coordGeo4.add(destPtsGeo[7]);
		
		coordinatesGeo.add(coordGeo1);
		coordinatesGeo.add(coordGeo2);
		coordinatesGeo.add(coordGeo3);
		coordinatesGeo.add(coordGeo4);
			
		// Carto coordinates
		JSONArray coordinatesCarto = new JSONArray();
		
		JSONArray coordCarto1 = new JSONArray();
		coordCarto1.add(realworldXMin);
		coordCarto1.add(realworldYMax);
		
		JSONArray coordCarto2 = new JSONArray();
		coordCarto2.add(realworldXMax);
		coordCarto2.add(realworldYMax);
		
		JSONArray coordCarto3 = new JSONArray();
		coordCarto3.add(realworldXMax);
		coordCarto3.add(realworldYMin);
		
		JSONArray coordCarto4 = new JSONArray();
		coordCarto4.add(realworldXMin);
		coordCarto4.add(realworldYMin);
		
		coordinatesCarto.add(coordCarto1);
		coordinatesCarto.add(coordCarto2);
		coordinatesCarto.add(coordCarto3);
		coordinatesCarto.add(coordCarto4);
		
		// Carto coordinates
		JSONArray coordinatesGame = new JSONArray();
				
		JSONArray coordGame1 = new JSONArray();
		// Offset (boundaries) in real world are divided by the selected ratio
		// But in-game there is no such division because for instance:
		// 2 cubes for 1 meter means that a 5Km*5Km initial map will now be of 2.5Km*2.5Km (IRL boundaries by ratio division)
		// But it'll have -2500;+2500 (so 5000) in-game map size (since we put 2 cubes per meter)
		offset = (MineGenerator.MINECRAFTMAP_MAPNBREGIONS * MineGenerator.MINECRAFTMAP_MAPTILESIZE);
		coordGame1.add(-offset/2);
		coordGame1.add(offset/2);
		
		JSONArray coordGame2 = new JSONArray();
		coordGame2.add(offset/2);
		coordGame2.add(offset/2);
		
		JSONArray coordGame3 = new JSONArray();
		coordGame3.add(offset/2);
		coordGame3.add(-offset/2);
			
		JSONArray coordGame4 = new JSONArray();
		coordGame4.add(-offset/2);
		coordGame4.add(-offset/2);
				
		coordinatesGame.add(coordGame1);
		coordinatesGame.add(coordGame2);
		coordinatesGame.add(coordGame3);
		coordinatesGame.add(coordGame4);
			
		// Geography data JSON
		JSONObject geographyData = new JSONObject();
				
		geographyData.put("coordinatesGeo", coordinatesGeo);
		geographyData.put("coordinatesCarto", coordinatesCarto);
		geographyData.put("coordinatesGame", coordinatesGame);
				
		geographyData.put("epsgCarto",epsg);
		geographyData.put("angleDegres", MineGenerator.MINECRAFTMAP_ANGLE);
		geographyData.put("echelle", MineGenerator.MINECRAFTMAP_RATIO);
		geographyData.put("altitudeZero", AltiImporter.BLOCK_ALTITUDE_ABSOLUTEZERO);
				
		return geographyData.toJSONString();
	}
	
	/**
	 * generate map items and save them on file
	 * @param generateBorder 
	 * 
	 * @throws IOException 
	 * 
	 */
	private void generateMapItems() throws IOException {
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
		
		//then generate 3x3 wall of maps
		for (int mapWallShiftX: new int[] {-1,0,1}) {
			for (int mapWallShiftY: new int[] {-1,0,1}) {
				//place block to support frame
				new BlockMT(mapSpawnX + mapWallShiftX, mapGroundLevel + 3 + mapWallShiftY, mapSpawnZ - 1,
						BlockTypeConverter.convert(BlockType.OBSIDIAN)).addTo(blockList);
			}
		}
		new BlockMT(mapSpawnX,mapGroundLevel + 3,mapSpawnZ,"ignfab:overview").addTo(blockList);
		
		//place block to support frame
		new BlockMT(mapSpawnX + 2, mapGroundLevel + 4, mapSpawnZ - 1,
				BlockTypeConverter.convert(BlockType.OBSIDIAN)).addTo(blockList);
		//ign logo nextby
		new BlockMT(mapSpawnX + 2, mapGroundLevel + 4, mapSpawnZ,"ignfab:ignlogo").addTo(blockList);
		
		//place block to support frame
		/*new BlockMT(mapSpawnX + 2, mapGroundLevel + 2, mapSpawnZ - 1,
				BlockTypeConverter.convert(BlockType.OBSIDIAN)).addTo(blockList);*/
		//ignfab logo nextby //deactivated
		//new BlockMT(mapSpawnX + 2, mapGroundLevel + 2, mapSpawnZ,"ignfab:ignfablogo").addTo(blockList);
	}
	
	
	
	public Thread startCommunication() throws MinecraftGenerationException {
		ServerSocket s;
		int p=9898;
		
		try {
			s = new ServerSocket(0);
			p = s.getLocalPort();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		port = p;
		
		
		Runnable minetestEngine = () -> { 
			// Ne pas oublier chmod 755 sur pypy3
			// /!\ Voir pour attacher ce process au moteur Java
			// Car quand le moteur Java est éteint abruptement (kill -9 ...)
			// Celui là reste ( il attend des données à télécharger )
			String[] cmdAsTokens;
			if(SystemUtils.IS_OS_LINUX) {
				//cmdAsTokens = ("../python/bin/pypy3 main.py " + port).split(" ");
				cmdAsTokens = ("./mcconvert " + port).split(" ");
			} else {
				Logger.getLogger("MinecraftGenerator").log(Level.WARNING, "The system isn't Linux-based. "
						+ "At the moment, Minetest map generation was only tested with Linux. "
						+ "Also, you have to install Python and the required packages in Windows.");
				cmdAsTokens = ("python main.py" + port).split(" ");
			}
			ProcessBuilder mtProcess = new ProcessBuilder( cmdAsTokens )
					.directory(new File("sources/minetest_engine"))
					.redirectErrorStream(true)
					.redirectOutput(new File("/dev/null"))
					.redirectInput(new File("/dev/null"));
			
			try {
				Process process = mtProcess.start();
				process.waitFor();
			} catch (IOException e) {
				Logger.getLogger("MinecraftGenerator").log(Level.SEVERE, "Error while starting Minetest engine " + e.getMessage());
			} catch (InterruptedException e) {
				Logger.getLogger("MinecraftGenerator").log(Level.SEVERE, "Error while running Minetest engine " + e.getMessage());
			}
		};
		Thread mte = new Thread(minetestEngine);
		mte.start();
		
		ZMQ.Context context = ZMQ.context( 1 );
		publisher = context.socket( ZMQ.PAIR );
		
		publisher.bind( "tcp://localhost:" + port);
		Logger.getLogger("MinecraftGenerator").log(Level.INFO, "Listening to port " + port + "... (awaiting map writer)");
		
		totalBlocks = blockList.size();
		
		return mte;
	}
	
	public void stopCommunication() {
		List<MapProto.Map.Block> dummy = new ArrayList<>();
		dummy.add(MapProto.Map.Block.newBuilder().setX(0).setY(0).setZ(0).setType("").build());
		publisher.send(BlockSender.compress(MapProto.Map.newBuilder()
				.addAllBlocks(dummy)
				.setChunkBlocks(-1)
				.build().toByteArray()));
		connected=false;
	}
	
	public void communicate() {
		if(new String(answer).equals("CNT")) {
			Logger.getLogger("MinecraftGenerator").log(Level.INFO, "Connection established between engine and map writer.");
			connected = true;
			MapProto.MapInfo mapInfo = 
					MapProto.MapInfo.newBuilder()
					.setPackageName("map")
					.setTotalBlocks(0)
					.build();
			publisher.send(BlockSender.compress(mapInfo.toByteArray()));
		}
		
		if(connected) {
			ignfab.minetest.MapProto.Map map = 
					ignfab.minetest.MapProto.Map.newBuilder()
					.addAllBlocks(blockList.keySet().stream().
							map(obj->ignfab.minetest.MapProto.Map.Block.newBuilder()
							.setX(obj.x)
							.setY(obj.y)
							.setZ(obj.z)
							.setType(obj.type)
							.build())
							.collect(Collectors.toList()).subList(actual, Math.min((actual+increment),totalBlocks)))
							//.collect(Collectors.toCollection(LinkedList::new)).subList(actual, Math.min((actual+increment),totalBlocks)))
					.setChunkBlocks(increment)
					.build();
			publisher.send( BlockSender.compress(map.toByteArray()), 0 );
			actual+=increment;
			increment=Math.min(chunkSize, totalBlocks-actual);
		}
	}
	
}
