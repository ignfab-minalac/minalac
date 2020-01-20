/** **************************************************************************************************************
 * Map generation engine ("Minecraft à la carte" service from IGN)
 * Generate sandbox games maps with geo data from IGN
 * ***************************************************************************************************************
 * Copyright (c) Institut national de l'information géographique et forestière
 * This program and the accompanying materials are made available under the terms of the GPL License, Version 3.0.
 * ***************************************************************************************************************/

package ign.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.util.FileUtils;

import com.boydti.fawe.jnbt.anvil.MCAChunk;
import com.boydti.fawe.jnbt.anvil.MCAFile;
import com.boydti.fawe.jnbt.anvil.MCAQueue;
import com.boydti.fawe.jnbt.anvil.filters.DelegateMCAFilter;
import com.boydti.fawe.jnbt.anvil.filters.RemapFilter;
import com.boydti.fawe.nukkit.core.converter.MapConverter.Tag;
import com.boydti.fawe.object.clipboard.remap.ClipboardRemapper;
import com.boydti.fawe.object.number.MutableLong;
import com.boydti.fawe.util.MainUtil;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.worldedit.blocks.BaseBlock;

import developpeur2000.minecraft.minecraft_rw.world.MapItem;
import developpeur2000.minecraft.minecraft_rw.world.MapItemColor;
import developpeur2000.minecraft.minecraft_rw.world.World;
import ign.minecraft.importer.AltiImporter;
import ignfab.bedrock.MCAFile2LevelDB;
import ignfab.bedrock.MCAFile2LevelDB.FileCache;

public class BedrockMap extends MinecraftMap {
	private MCAFile2LevelDB ConverterEngine;
	
	public BedrockMap(int size, Path path, String name, Path resourcesPath) {
		super(size,path,name,resourcesPath);
	}
	
	/* (non-Javadoc)
	 * @see ign.minecraft.MineMap#writeToDisk()
	 */
	@Override
	public void writeToDisk() throws IOException, MinecraftGenerationException {
		super.writeToDisk();
		convertToBedrock();
	}
	
	public void convertToBedrock() {
		// MC = Minecraft
		Logger.getLogger("MinecraftGenerator").log(Level.INFO, "Converting to Bedrock format...");
		
		ConverterEngine = new MCAFile2LevelDB(outputDir.toFile(),outputDir.resolve("convert").toFile(),name);
		putMapData(); // Convert and write "overview map" data (RGBA colors, dimensions...)
		ConverterEngine.accept(); // Convert the map from MC Java to MC Bedrock, then close and compact
		
		ConverterEngine = new MCAFile2LevelDB(outputDir.toFile(),outputDir.resolve("convert").toFile(),name);
		generateMapItems(); // Place ItemFrames and MapItems related to "map overview" in the Bedrock map (as they don't get converted automatically)
		ConverterEngine.close();
        ConverterEngine.compact();
		
		System.gc();
		System.gc();
		
		// Removing Minecraft map files, replacing with Bedrock ones
		FileUtils.deleteRecursively(outputDir.resolve("data").toFile());
		FileUtils.deleteRecursively(outputDir.resolve("region").toFile());
		outputDir.resolve("level.dat").toFile().delete();
		//FileUtils.copyDirectoryContents(outputDir.resolve("convert").toFile(), outputDir.toFile());
		//FileUtils.deleteRecursively(outputDir.resolve("convert").toFile());

		// Compressing Bedrock map content into *.mcworld extension (zip)
		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputDir.toString() + File.separator + "bedrock_alac.mcworld"));
			compressDirectoryToZipfile(Paths.get(outputDir.toString(),"convert").toString(),"",out);
			IOUtils.closeQuietly(out);
			FileUtils.deleteRecursively(outputDir.resolve("convert").toFile());
		} catch (FileNotFoundException e) {
			Logger.getLogger("MinecraftGenerator").log(Level.SEVERE, "During the mcworld compression, the following error happened : " + e.getMessage());
		} catch (IOException e) {
			Logger.getLogger("MinecraftGenerator").log(Level.SEVERE, "During the mcworld compression, the following error happened : " + e.getMessage());
		}
	}
	
	private void putMapData() {
		try {
			World minecraftWorld = new World(outputDir);
			try (Stream<Path> paths = Files.walk(outputDir.resolve("data"))) {
			    paths
			        .filter(Files::isRegularFile)
			        .filter(f -> f.getFileName().toString().matches("map_(.*).dat"))
			        .forEach(f -> {
			        	try {
							MapItem m = minecraftWorld.loadMapItem(f);
							
							byte[] colors = new byte[MapItem.MAP_ITEM_SIZE * MapItem.MAP_ITEM_SIZE * 4];
							
							MapItemColor[] colorsDefinitions = MapItemColor.values();
							byte[] curRGB;
							
							// Convert to from BGR (without Alpha) to RGB32 (RGBA)
							// as MC Java uses BGR, and MC Bedrock uses RGBA
							for(int x = 0; x < MapItem.MAP_ITEM_SIZE; x++ ) {
								for(int y = 0; y < MapItem.MAP_ITEM_SIZE; y++ ) {
									curRGB = colorsDefinitions[ ((int) m.getColors()[y * MapItem.MAP_ITEM_SIZE + x] & 0xff) / 4 ].getRGB();
									colors[ (y * MapItem.MAP_ITEM_SIZE + x)*4 ] = curRGB[0]; // Red
									colors[ (y * MapItem.MAP_ITEM_SIZE + x)*4 + 1 ] = curRGB[1]; // Green
									colors[ (y * MapItem.MAP_ITEM_SIZE + x)*4 + 2 ] = curRGB[2]; // Blue
									colors[ (y * MapItem.MAP_ITEM_SIZE + x)*4 + 3 ] = (byte)0xFF; // Alpha
								}
							}
							
							Pattern p = Pattern.compile("map_(.*).dat");   // the pattern to search for
						    Matcher matcher = p.matcher(f.getFileName().toString());

						    if (matcher.find()) {
						    	writeMapData(Long.parseLong(matcher.group(1))+150,
										m.marshalCompound().getCompound("data").getByte("scale"),
										m.marshalCompound().getCompound("data").getByte("dimension"),
										m.marshalCompound().getCompound("data").getShort("height"),
										m.marshalCompound().getCompound("data").getShort("width"),
										m.marshalCompound().getCompound("data").getInt("xCenter"),
										m.marshalCompound().getCompound("data").getInt("zCenter"),
										colors);
						    }
						} catch (IOException e) {
							e.printStackTrace();
						}
			        });
			} 
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void generateMapItems() {
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
		int mapSpawnY = mapGroundLevel;
		
		//Map<List<Integer>, Short> mapItemId = new HashMap<>();
		
		//List<com.sk89q.jnbt.Tag> entities = new ArrayList<>();
		List<CompoundTag> entitiesCP = new ArrayList<>(); // Stockage des informations d'entités, pour poser les blocs correspondants
		//List<CompoundTag> tickList = new ArrayList<>();
		
		for (int mapWallShiftX: new int[] {-1,0,1}) {
			for (int mapWallShiftY: new int[] {-1,0,1}) {
				List<Integer> mapCoords = new ArrayList<Integer>();
				mapCoords.add(mapSpawnX + mapWallShiftX);
				mapCoords.add(mapSpawnZ);
				
				CompoundTag actualEntity = createItemFrame(mapSpawnX + mapWallShiftX, mapGroundLevel + 3 + mapWallShiftY, mapSpawnZ, (short)150);//mapItemId.get(mapCoords));
				//entities.add(actualEntity); // actualEntity
				entitiesCP.add(actualEntity);
				
				/*int x = actualEntity.getInt("x");
                int y = actualEntity.getInt("y");
                int z = actualEntity.getInt("z");
                //BaseBlock block = chunk.getBlock(x & 15, y, z & 15);

                Map<String, com.sk89q.jnbt.Tag> tickable = new HashMap<>();
                tickable.put("tileID", new ByteTag((byte) 199));
                tickable.put("x", new IntTag(x));
                tickable.put("y", new IntTag(y));
                tickable.put("z", new IntTag(z));
                tickable.put("time", new LongTag(1));
                tickList.add(new CompoundTag(tickable));*/
			}
		}
		
        String[] dimDirs = {"DIM-1/region", "DIM1/region", "region"};
        int[] dimIds = {1, 2, 0};
        File[] regionFolders = new File[dimDirs.length];
        int totalFiles = 0;
        for (int i = 0; i < dimDirs.length; i++) {
            File source = new File(ConverterEngine.getFolderFrom(), dimDirs[i]);
            if (source.exists()) {
                regionFolders[i] = source;
                totalFiles += source.listFiles().length;
            }
        }
		
        File source = regionFolders[2];
        if (source != null) {
            int dimId = 0;
            RemapFilter filter = new RemapFilter(ClipboardRemapper.RemapPlatform.PC, ClipboardRemapper.RemapPlatform.PE);
            filter.setDimension(dimId);
                
            DelegateMCAFilter<MutableLong> delegate = new DelegateMCAFilter<MutableLong>(filter) {
	            @Override
	            public void finishFile(MCAFile file, MutableLong cache) {                    	
	            	MCAChunk chunk =  file.getCachedChunk(mapSpawnX >> 4, mapSpawnZ >> 4);
	            	
	                try {
	                	if(chunk!=null && chunk.getX()==mapSpawnX >> 4 && chunk.getZ()==mapSpawnZ >> 4) {
	                		System.out.println("Chunk " + chunk.getX() + " " + chunk.getZ());
	                    	FileCache cached = ConverterEngine.getFileCache(chunk, dimId);
	                    		   
	                    	//cached.update(ConverterEngine.getKey(chunk, Tag.BlockEntity, dimId), ConverterEngine.write(entities));
	                    	
	                    	List<com.sk89q.jnbt.Tag> tiles = new ArrayList<>();
	                    	for (CompoundTag tag : chunk.getEntities()) {
	                        	if(tag.getString("id").equals("ItemFrame") && tag.containsKey("TileX")) {
	                        		com.sk89q.jnbt.CompoundTag item = (CompoundTag) tag.getValue().get("Item");
	                                if (item != null) {
	                                	// TODO remplacer (item.getShort("Damage")+(short)150) par une variable UUID et tester
	                                	
	                                	// Ajout d'un BlockEntity d'ItemFrame (X, Y, Z, Map UUID) dans Minecraft Bedrock
	                                	tiles.add(createItemFrame(tag.getInt("TileX"), tag.getInt("TileY"), tag.getInt("TileZ"), (item.getShort("Damage")+(short)150) ));
	                                	
	                                	// Récupération de la map affichée par cette ItemFrame (retrouvée avec l'UUID) [ optionnel ? ]
	                                	Map<String, com.sk89q.jnbt.Tag> map = new HashMap<>();
	                                	
	                                	byte[] mapData = ConverterEngine.getDb().get( ("map_"+(item.getShort("Damage")+(short)150) ).getBytes());
	                                	
	                                	for(NamedTag nt : ConverterEngine.read(mapData)) {
	                                		if(nt.getTag().getClass().equals(com.sk89q.jnbt.CompoundTag.class)) {
	                                			CompoundTag ctag = (CompoundTag)nt.getTag();
	                                        	map =  new HashMap<>(ctag.getValue());
	                                		}
	                                	}
	                                	
	                                	// Ajout du ListTag Decorations dans cette map [ optionnel ? ]
	                                	Map<String, com.sk89q.jnbt.Tag> decorations = new HashMap<>();

	                                	Map<String,com.sk89q.jnbt.Tag> data = new HashMap<>();
	                                	data.put("rot", new IntTag(0));
	                                	data.put("type", new IntTag(1));
	                                	data.put("x", new IntTag(10)); // A DETERMINER (inconnu) Voir avec Pocketmine
	                                	data.put("y", new IntTag(-13)); // A DETERMINER (inconnu) Voir avec Pocketmine

	                                	Map<String,com.sk89q.jnbt.Tag> key = new HashMap<>();
	                                	key.put("blockX", new IntTag(tag.getInt("TileX")));
	                                	key.put("blockY", new IntTag(tag.getInt("TileY")));
	                                	key.put("blockZ", new IntTag(tag.getInt("TileZ")));
	                                	key.put("type", new IntTag(1));
	                                	
	                                	decorations.put("data", new CompoundTag(data));
	                                	decorations.put("key", new CompoundTag(key));
	                                	
	                                	List<CompoundTag> mapList = new ArrayList<>(Arrays.asList(new CompoundTag(decorations)));
	                                	
	                                	map.put("decorations", new ListTag(CompoundTag.class, mapList));
	                                    
	                                	// Ecriture des modifications [ optionnel ? ]
	                                	ConverterEngine.getDb().delete( ("map_"+(item.getShort("Damage")+(short)150) ).getBytes());
	                                	ConverterEngine.getDb().put( ("map_"+(item.getShort("Damage")+(short)150) ).getBytes(), ConverterEngine.write(Arrays.asList(new CompoundTag(map))) );
	                                }
	                        	}
	                        }
	                    	// Ecriture des BlockEntity d'ItemFrame ajoutés
	                        cached.update(ConverterEngine.getKey(chunk, Tag.BlockEntity, dimId), ConverterEngine.write(tiles)); 
	                        
	                        // Ajout des blocs d'ItemFrame
	                    	for(CompoundTag entity : entitiesCP) {
	                    		chunk.setBlock(entity.getInt("x"), entity.getInt("y"), entity.getInt("z"), new BaseBlock(199,2)); // Overview maps
	                    	}
	                    	
	                    	chunk.setBlock(mapSpawnX + 2, mapSpawnY + 4, mapSpawnZ, new BaseBlock(199,2)); // IGN logo
	                    	chunk.setBlock(mapSpawnX + 2, mapSpawnY + 2, mapSpawnZ, new BaseBlock(199,2)); // IGNfab logo

	                    	// Ecriture des blocs d'ItemFrame ajoutés
	                    	ConverterEngine.write(chunk, false, dimId);
	                    	ConverterEngine.write(chunk, true, dimId);
	                    }
	                } catch (IOException e) {
	                	e.printStackTrace();
	                }
	                file.clear();
	            }
            };
                
            Comparator<File> seqUnsPos = new Comparator<File>() {
            	@Override
            	public int compare(File f1, File f2) {
            		int[] left = MainUtil.regionNameToCoords(f1.getPath());
            		int[] right = MainUtil.regionNameToCoords(f2.getPath());
//
            		int minLength = Math.min(left.length, right.length);

            		for(int i = 0; i < minLength; ++i) {
            			int lb = left[i] << 5;
            			int rb = right[i] << 5;
            			for (int j = 0; j < 4; j++) {
            				int shift = (j << 3);
            				int sbl = (lb >> shift) & 0xFF;
            				int sbr = (rb >> shift) & 0xFF;
            				int result = sbl - sbr;
            				if(result != 0) {
            					return result;
            				}
            			}
            		}

            		return left.length - right.length;
            	}
            };
                
            MCAQueue queue = new MCAQueue(null,source,true);
            queue.filterWorld(delegate, seqUnsPos);
        }
	}

	private CompoundTag createItemFrame(int x, int y, int z, long uuid) {
		// Item frame
		Map<String, com.sk89q.jnbt.Tag> itemframe = new HashMap<>();
		
		// Filled map (Bedrock version)
		Map<String, com.sk89q.jnbt.Tag> item = new HashMap<>();
		Map<String, com.sk89q.jnbt.Tag> tag = new HashMap<>();
		
		tag.put("map_display_players", new ByteTag((byte)1));
		tag.put("map_name_index", new IntTag(1));
		tag.put("map_uuid", new LongTag(uuid));
		
		item.put("tag", new CompoundTag(tag));
		item.put("Count", new ByteTag((byte)1));
		item.put("Damage", new ShortTag((short) 0));
		item.put("id", new ShortTag((short)358));
		
		itemframe.put("Item", new CompoundTag(item));/**/
		itemframe.put("id", new StringTag("ItemFrame"));
		itemframe.put("isMovable", new ByteTag((byte)1));
		itemframe.put("ItemDropChance", new FloatTag(1));//
        itemframe.put("ItemRotation", new ByteTag((byte)0));//
        itemframe.put("x", new IntTag(x));
        itemframe.put("y", new IntTag(y));
        itemframe.put("z", new IntTag(z));
        
		return new CompoundTag(itemframe);
	}
	
	private void writeMapData(long id, byte scale, byte dimension, short height, short width, int xCenter, int zCenter, byte[] colors) {
		DB db = ConverterEngine.getDb();
		List<com.sk89q.jnbt.Tag> map_list = new ArrayList<>();
		
		Map<String, com.sk89q.jnbt.Tag> map = new HashMap<>();
		
		map.put("dimension", new ByteTag(dimension));
		map.put("fullyExplored", new ByteTag((byte)0));
		map.put("height", new ShortTag(height));
		map.put("mapId", new LongTag(id));
		//map.put("parentMapId", new LongTag(id));
		map.put("parentMapId", new LongTag(-1));
		//map.put("scale", new ByteTag(scale)); // Some values causes crash
		map.put("scale", new ByteTag((byte)4));
		map.put("unlimitedTracking", new ByteTag((byte)0));
		map.put("width", new ShortTag(width));
		map.put("xCenter", new IntTag(xCenter));
		map.put("zCenter", new IntTag(zCenter));
		map.put("colors", new ByteArrayTag(colors));
		
		map_list.add(new CompoundTag(map));
		
		try {
			db.put(("map_"+id).getBytes(),
					ConverterEngine.write(map_list));
		} catch (DBException | IOException e) {
			e.printStackTrace(); // TODO throw une vraie exception et LOG severe
		}
	}

	private static void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipOutputStream out) throws IOException, FileNotFoundException {
		String dir = Paths.get(rootDir, sourceDir).toString();
	    for (File file : new File(dir).listFiles()) {
	        if (file.isDirectory()) {
	            compressDirectoryToZipfile(rootDir, Paths.get(sourceDir,file.getName()).toString(), out);
	        } else {
	            ZipEntry entry = new ZipEntry(Paths.get(sourceDir,file.getName()).toString());
	            out.putNextEntry(entry);

	            FileInputStream in = new FileInputStream(Paths.get(rootDir, sourceDir, file.getName()).toString());
	            IOUtils.copy(in, out);
	            IOUtils.closeQuietly(in);
	        }
	    }
	}
}
