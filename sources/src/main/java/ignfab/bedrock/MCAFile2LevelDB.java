package ignfab.bedrock;

import com.boydti.fawe.Fawe;
import com.boydti.fawe.config.BBC;
import com.boydti.fawe.jnbt.anvil.MCAChunk;
import com.boydti.fawe.jnbt.anvil.MCAFile;
import com.boydti.fawe.jnbt.anvil.MCAFilter;
import com.boydti.fawe.jnbt.anvil.MCAQueue;
import com.boydti.fawe.jnbt.anvil.filters.DelegateMCAFilter;
import com.boydti.fawe.jnbt.anvil.filters.RemapFilter;
import com.boydti.fawe.nukkit.core.converter.ConverterFrame;
import com.boydti.fawe.nukkit.core.converter.MapConverter;
import com.boydti.fawe.object.FaweInputStream;
import com.boydti.fawe.object.FaweOutputStream;
import com.boydti.fawe.object.RunnableVal;
import com.boydti.fawe.object.clipboard.remap.ClipboardRemapper;
import com.boydti.fawe.object.io.LittleEndianOutputStream;
import com.boydti.fawe.object.number.MutableLong;
import com.boydti.fawe.util.MainUtil;
import com.boydti.fawe.util.MemUtil;
import com.boydti.fawe.util.ReflectionUtils;
import com.boydti.fawe.util.StringMan;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.zip.GZIPInputStream;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.iq80.leveldb.shaded.guava.io.LittleEndianDataInputStream;
import org.iq80.leveldb.shaded.guava.primitives.Bytes;

public class MCAFile2LevelDB extends MapConverter {
    private final byte[] VERSION = new byte[] { 4 };
    private final byte[] COMPLETE_STATE = new byte[] { 2, 0, 0, 0 };

    private final DB db;

	private final ClipboardRemapper remapper;
    private final ForkJoinPool pool;
    private boolean closed;
    private LongAdder submittedChunks = new LongAdder();
    private LongAdder submittedFiles = new LongAdder();

    private LongAdder totalOperations = new LongAdder();
    private long estimatedOperations;

    private long time;
    private boolean remap;

    private long startTime;

    private ConcurrentLinkedQueue<CompoundTag> portals = new ConcurrentLinkedQueue<>();
    
    private String worldName = "minecraft_alac";

    public MCAFile2LevelDB(File folderFrom, File folderTo, String mapName) {
        super(folderFrom, folderTo);
        
        this.worldName = mapName;
        this.startTime = System.currentTimeMillis();
        
        try {
            if (!folderTo.exists()) {
                folderTo.mkdirs();
            }
            
            try (PrintStream out = new PrintStream(new FileOutputStream(new File(folderTo, "levelname.txt")))) {
                out.print(worldName);
            }

            this.pool = new ForkJoinPool();
            this.remapper = new ClipboardRemapper(ClipboardRemapper.RemapPlatform.PC, ClipboardRemapper.RemapPlatform.PE);
            BundledBlockData.getInstance().loadFromResource();

            int bufferSize = (int) Math.min(Integer.MAX_VALUE, Math.max((long) (MemUtil.getFreeBytes() * 0.8), 134217728));
            this.db = Iq80DBFactory.factory.open(new File(folderTo, "db"),
                    new Options()
                            .createIfMissing(true)
                            .verifyChecksums(false)
                            .compressionType(CompressionType.ZLIB)
                            // Il faut jouer avec les valeurs pour qu'il n'y ait pas de chunks manquants sur 5Kmx5Km
                            // Peut-etre qu'on peut baisser le WriteBufferSize en augmentant CacheSize et BlockSize pour eviter d'avoir des fichiers
                            // pendant l'ecriture de 800MB. Les valeurs actuelles permettent de faire fonctionner une carte sans chunk perdus mais grosse ecriture 800mb
                            // Apercu de carte avec MCPE Viz Helper
                            .blockSize(262144) // 256K
                            //.blockSize(562144) // 256K
                            .cacheSize(8388608) // 8MB
                            //.cacheSize(16388608) // 8MB
                            //.writeBufferSize(134217728) // >=512MB
                            //.writeBufferSize(834217728) // >=834MB 
                            .writeBufferSize(1334217728) // >=1334MB // Valeur assez sensible
            );
//            try {
//                this.db.suspendCompactions();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double lastPercent;
    private double lastTimeRatio;
    
    public DB getDb() {
		return db;
	}

    private void resetProgress(int estimatedOperations) {
        startTime = System.currentTimeMillis();
        lastPercent = 0;
        lastTimeRatio = 0;
        this.totalOperations.reset();
        this.estimatedOperations = estimatedOperations;
    }

    private void progress(int increment) {
        totalOperations.add(increment);

        long completedOps = totalOperations.longValue();
        double percent = Math.max(0, Math.min(100, (Math.pow(completedOps, 1.3) * 100 / Math.pow(estimatedOperations, 1.3))));
        double lastPercent = (this.lastPercent == 0 ? percent : this.lastPercent);
        percent = (percent + lastPercent * 7) / 8;
        if (increment != 0) this.lastPercent = percent;

        double remaining = estimatedOperations - completedOps;
        long timeSpent = System.currentTimeMillis() - startTime;

        double totalTime = Math.pow(estimatedOperations, 1.3) * 1000;
        double estimatedTimeSpent = Math.pow(completedOps, 1.3) * 1000;

        double timeRemaining;
        if (completedOps > 16) {
            double timeRatio = (timeSpent * totalTime / estimatedTimeSpent);
            double lastTimeRatio = this.lastTimeRatio == 0 ? timeRatio : this.lastTimeRatio;
            timeRatio = (timeRatio + lastTimeRatio * 7) / 8;
            if (increment != 0) this.lastTimeRatio = timeRatio;
            timeRemaining = timeRatio - timeSpent;
        } else {
            timeRemaining = ((long) totalTime >> 4) - timeSpent;
        }
        String msg = MainUtil.secToTime((long) (timeRemaining / 1000));
    }

    public DelegateMCAFilter<MutableLong> toFilter(final int dimension) {
        RemapFilter filter = new RemapFilter(ClipboardRemapper.RemapPlatform.PC, ClipboardRemapper.RemapPlatform.PE);
        filter.setDimension(dimension);

        DelegateMCAFilter<MutableLong> delegate = new DelegateMCAFilter<MutableLong>(filter) {
            @Override
            public void finishFile(MCAFile file, MutableLong cache) {
                /*for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        MCAChunk chunk = file.getCachedChunk(x, z);
                        if (chunk != null) {
                            try {
                                write(chunk, !file.getFile().getName().endsWith(".mcapm"), dimension);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                file.clear();*/
            	file.forEachChunk(new RunnableVal<MCAChunk>() {
            		@Override
					public void run(MCAChunk value) {
            			try {
            				write(value, !false, dimension);
            			} catch (IOException e) {
            				e.printStackTrace();
            			}
					}
				});
				file.clear();

                progress(1);
                submittedFiles.increment();
                if ((submittedFiles.longValue() & 7) == 0) {
//                    flush();
                }
            }
        };
        return delegate;
    }

    public synchronized void accept(ConverterFrame app) {
    	accept();
    }

    public synchronized void accept() {

        File levelDat = new File(folderFrom, "level.dat");
        if (levelDat.exists()) {
            try {
                copyLevelDat(levelDat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<CompoundTag> portals = new ArrayList<>();
        String[] dimDirs = {"DIM-1/region", "DIM1/region", "region"};
        int[] dimIds = {1, 2, 0};
        File[] regionFolders = new File[dimDirs.length];
        int totalFiles = 0;
        for (int i = 0; i < dimDirs.length; i++) {
            File source = new File(folderFrom, dimDirs[i]);
            if (source.exists()) {
                regionFolders[i] = source;
                totalFiles += source.listFiles().length;
            }
        }
        this.estimatedOperations = totalFiles;

        /*Thread progressThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    progress(0);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        progressThread.start();*/

        for (int i = 0; i < regionFolders.length; i++) {
            File source = regionFolders[i];
            if (source != null) {
                Fawe.debugPlain(" - dimension " + dimIds[i] + " (" + dimDirs[i] + ")");

                DelegateMCAFilter filter = toFilter(dimIds[i]);
                MCAQueue queue = new MCAQueue(null, source, true);

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

                MCAFilter result = queue.filterWorld(filter, seqUnsPos);
                portals.addAll(((RemapFilter) filter.getFilter()).getPortals());
            }
        }

        Fawe.debugPlain(" - including portals");
        // Portals
        if (!portals.isEmpty()) {
            CompoundTag portalData = new CompoundTag(Collections.singletonMap("PortalRecords", new ListTag(CompoundTag.class, portals)));
            CompoundTag portalsTag = new CompoundTag(Collections.singletonMap("data", portalData));
            try {
                db.put("portals".getBytes(), write(Arrays.asList(portalsTag)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        CompoundTag overworldData = new CompoundTag(Collections.singletonMap("LimboEntities", new ListTag(CompoundTag.class, new ArrayList<CompoundTag>())));
        CompoundTag overworldTag = new CompoundTag(Collections.singletonMap("data", overworldData));
        try {
            db.put("Overworld".getBytes(), write(Arrays.asList(overworldTag)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        close();
        //progressThread.interrupt();
        Fawe.debugPlain("Starting compaction");
        compact();
    }

    @Override
    public synchronized void close() {
        try {
            if (closed == (closed = true)) return;

            Fawe.debugPlain("Collecting threads");
            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            resetProgress(cache.size());
            ArrayList<FileCache> files = new ArrayList<>(cache.values());
            Collections.sort(files);
            for (FileCache file : files) {
                file.close();
                progress(1);
            }

            Fawe.debugPlain("Closing");
            db.close();
            Fawe.debugPlain("Done! (but still compacting)");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public synchronized void compact() {
        // Since the library doesn't support it, only way to flush the cache is to loop over everything
        try (DB newDb = Iq80DBFactory.factory.open(new File(folderTo, "db"), new Options()
                .verifyChecksums(false)
                .blockSize(262144) // 256K
                .cacheSize(8388608) // 8MB
                .writeBufferSize(134217728) // >=128MB
        )) {
            newDb.close();
        } catch (Throwable ignore) {}
        Fawe.debug("Done compacting!");
    }

    public void copyLevelDat(File in) throws IOException {
        File levelDat = new File(folderTo, "level.dat");
        try (NBTInputStream nis = new NBTInputStream(new GZIPInputStream(new FileInputStream(in)))) {
            if (!levelDat.exists()) {
                levelDat.createNewFile();
            }
            NamedTag named = nis.readNamedTag();
            com.sk89q.jnbt.CompoundTag tag = (CompoundTag) ((CompoundTag) (named.getTag())).getValue().get("Data");
            Map<String, com.sk89q.jnbt.Tag> map = ReflectionUtils.getMap(tag.getValue());

            HashSet<String> allowed = new HashSet<>(Arrays.asList(
            "Difficulty", "GameType", "Generator", "LastPlayed", "RandomSeed", "StorageVersion", "Time", "commandsEnabled", "currentTick", "rainTime", "spawnMobs", "GameRules", "SpawnX", "SpawnY", "SpawnZ"
            ));
            Iterator<Map.Entry<String, com.sk89q.jnbt.Tag>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, com.sk89q.jnbt.Tag> entry = iterator.next();
                if (!allowed.contains(entry.getKey())) {
                    iterator.remove();
                }
            }
            {
                Map<String, com.sk89q.jnbt.Tag> gameRules = ((CompoundTag) map.remove("GameRules")).getValue();
                for (Map.Entry<String, com.sk89q.jnbt.Tag> entry : gameRules.entrySet()) {
                    String key = entry.getKey().toLowerCase();
                    String value = ((StringTag) entry.getValue()).getValue();
                    if (StringMan.isEqualIgnoreCaseToAny(value, "true", "false")) {
                        map.put(key, new ByteTag((byte) (value.equals("true") ? 1 : 0)));
                    }
                }
                map.put("LevelName", new StringTag(worldName));
                map.put("StorageVersion", new IntTag(5));
                Byte difficulty = tag.getByte("Difficulty");
                map.put("Difficulty", new IntTag(difficulty == null ? 2 : difficulty));
                String generatorName = tag.getString("generatorName");
                map.put("Generator", new IntTag("flat".equalsIgnoreCase(generatorName) ? 2 : 1));
                //map.put("Generator", new IntTag(1));
                //map.put("SpawnX", new DoubleTag(3.136));
                //map.put("SpawnY", new DoubleTag(52.12));
                //map.put("SpawnZ", new DoubleTag(0.0));
                map.put("commandsEnabled", new ByteTag((byte) 1));
                Long time = tag.getLong("Time");
                if (time != null) this.time = time;
                map.put("CurrentTick", new LongTag(time == null ? 0L : time));
                map.put("spawnMobs", new ByteTag((byte) 1));
                Long lastPlayed = tag.getLong("LastPlayed");
                if (lastPlayed != null && lastPlayed > Integer.MAX_VALUE) {
                    lastPlayed = lastPlayed / 1000;
                    map.put("LastPlayed", new LongTag(lastPlayed));
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (NBTOutputStream nos = new NBTOutputStream((DataOutput) new LittleEndianOutputStream(baos))) {
                nos.writeNamedTag("Name", tag);
            }
            LittleEndianOutputStream leos = new LittleEndianOutputStream(new FileOutputStream(levelDat));
            leos.writeInt(5);
            leos.writeInt(baos.toByteArray().length);
            leos.write(baos.toByteArray());
            leos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(MCAChunk chunk, boolean remap, int dim) throws IOException {
        submittedChunks.add(1);
        long numChunks = submittedChunks.longValue();
        if ((numChunks & 1023) == 0) {
            long queued = pool.getQueuedTaskCount() + pool.getQueuedSubmissionCount();
            if (queued > 127) {
                System.gc();
                while (queued > 64) {
                    try {
                        Thread.sleep(5);
                        queued = pool.getQueuedTaskCount() + pool.getQueuedSubmissionCount();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
        try {
            FileCache cached = getFileCache(chunk, dim);
            { // Data2D
                ByteBuffer data2d = ByteBuffer.wrap(new byte[512 + 256]);
                int[] heightMap = chunk.getHeightMapArray();
                for (int i = 0; i < heightMap.length; i++) {
                    data2d.putShort((short) heightMap[i]);
                }
                if (chunk.biomes != null) {
                    System.arraycopy(chunk.biomes, 0, data2d.array(), 512, 256);
                }
                cached.update(getKey(chunk, Tag.Data2D, dim), data2d.array());
            }

            { // SubChunkPrefix
                int maxLayer = chunk.ids.length - 1;
                while (maxLayer >= 0 && chunk.ids[maxLayer] == null) maxLayer--;
                if (maxLayer >= 0) {
                    for (int layer = 0; layer <= maxLayer; layer++) {
                        // Set layer
                        byte[] key = getSectionKey(chunk, layer, dim);
                        byte[] value = new byte[1 + 4096 + 2048 + 2048 + 2048];
                        byte[] ids = chunk.ids[layer];
                        if (ids == null) {
                            Arrays.fill(value, (byte) 0);
                        } else {
                            byte[] data = chunk.data[layer];
                            byte[] skyLight = chunk.skyLight[layer];
                            byte[] blockLight = chunk.blockLight[layer];

                            if (remap) {
                                copySection(ids, value, 1);
                                copySection(data, value, 1 + 4096);
                                copySection(skyLight, value, 1 + 4096 + 2048);
                                copySection(blockLight, value, 1 + 4096 + 2048 + 2048);
                            } else {
                                System.arraycopy(ids, 0, value, 1, ids.length);
                                System.arraycopy(data, 0, value, 1 + 4096, data.length);
                                System.arraycopy(skyLight, 0, value, 1 + 4096 + 2048, skyLight.length);
                                System.arraycopy(blockLight, 0, value, 1 + 4096 + 2048 + 2048, blockLight.length);
                            }
                        }
                        cached.update(key, value);
                    }
                }
            }

            {
                List<CompoundTag> tickList = null;

                // BlockEntity
                if (!chunk.tiles.isEmpty()) {
                    List<com.sk89q.jnbt.Tag> tiles = new ArrayList<>();
                    for (Map.Entry<Short, CompoundTag> entry : chunk.getTiles().entrySet()) {
                        CompoundTag tag = entry.getValue();
                        if (transform(chunk, tag, false) && time != 0l) {
                            // Needs tick
                            if (tickList == null) tickList = new ArrayList<>();

                            int x = tag.getInt("x");
                            int y = tag.getInt("y");
                            int z = tag.getInt("z");
                            BaseBlock block = chunk.getBlock(x & 15, y, z & 15);

                            Map<String, com.sk89q.jnbt.Tag> tickable = new HashMap<>();
                            tickable.put("tileID", new ByteTag((byte) block.getId()));
                            tickable.put("x", new IntTag(x));
                            tickable.put("y", new IntTag(y));
                            tickable.put("z", new IntTag(z));
                            tickable.put("time", new LongTag(1));
                            tickList.add(new CompoundTag(tickable));
                        }

                        tiles.add(tag);
                    }
                    cached.update(getKey(chunk, Tag.BlockEntity, dim), write(tiles));
                }

                // Entity
                if (!chunk.entities.isEmpty()) {
                    //List<com.sk89q.jnbt.Tag> entities = new ArrayList<>();
                    List<com.sk89q.jnbt.Tag> tiles = new ArrayList<>();
                    for (CompoundTag tag : chunk.getEntities()) {
                    	/*if(tag.getString("id").equals("ItemFrame") && tag.containsKey("TileX")) {
                    		com.sk89q.jnbt.CompoundTag item = (CompoundTag) tag.getValue().get("Item");
                            if (item != null) {
                            	//String id = StringUtils.rightPad(""+(item.getShort("Damage")), 10, "0"); //OLD(bckp)
                            	//tiles.add(createItemFrame(tag.getInt("TileX"), tag.getInt("TileY"), tag.getInt("TileZ"), -Long.parseLong(id))); //OLD(bckp)
                            	
                            	tiles.add(createItemFrame(tag.getInt("TileX"), tag.getInt("TileY"), tag.getInt("TileZ"), item.getShort("Damage")));
                            	
                            	Map<String, com.sk89q.jnbt.Tag> map = new HashMap<>();
                            	
                            	byte[] mapData = db.get(("map_"+item.getShort("Damage")).getBytes());
                            	
                            	for(NamedTag nt : read(mapData)) {
                            		if(nt.getTag().getClass().equals(com.sk89q.jnbt.CompoundTag.class)) {
                            			CompoundTag ctag = (CompoundTag)nt.getTag();
                                    	map =  new HashMap<>(ctag.getValue());
                            		} else {
                            			System.out.println("Not compound, rather " + nt.getTag().getClass().getName());
                            		}
                            	}
                            	
                            	Map<String, com.sk89q.jnbt.Tag> decorations = new HashMap<>();

                            	Map<String,com.sk89q.jnbt.Tag> data = new HashMap<>();
                            	data.put("rot", new IntTag(0));
                            	data.put("type", new IntTag(1));
                            	data.put("x", new IntTag(10));
                            	data.put("y", new IntTag(-13));

                            	Map<String,com.sk89q.jnbt.Tag> key = new HashMap<>();
                            	key.put("blockX", new IntTag(tag.getInt("TileX")));
                            	key.put("blockY", new IntTag(tag.getInt("TileY")));
                            	key.put("blockZ", new IntTag(tag.getInt("TileZ")));
                            	key.put("type", new IntTag(1));
                            	
                            	decorations.put("data", new CompoundTag(data));
                            	decorations.put("key", new CompoundTag(key));
                            	
                            	List<CompoundTag> mapList = new ArrayList<>(Arrays.asList(new CompoundTag(decorations)));
                            	
                            	//if(map.get("decorations")!=null) {
                            	//	ListTag lt = (ListTag) map.get("decorations");
                            	//	System.out.println(lt.getValue().size());
                            	//	mapList.add((CompoundTag) lt.getValue().get(0));
                            	//}
                            	
                            	map.put("decorations", new ListTag(CompoundTag.class, mapList));
                                
                            	db.delete(("map_"+item.getShort("Damage")).getBytes());
                            	db.put(("map_"+item.getShort("Damage")).getBytes(), write(Arrays.asList(new CompoundTag(map))));
                            }
                    	}*/
                        /*transform(chunk, tag, true);
                        entities.add(tag);*/ // Do not convert entities FOR THE MOMENT, TODO exclude only ItemFrame and Item (and other problem creating entities)
                    }
                    //cached.update(getKey(chunk, Tag.BlockEntity, dim), write(tiles)); 
                    //cached.update(getKey(chunk, Tag.Entity, dim), write(entities)); 
                }

                // PendingTicks
                if (tickList != null) {
                    HashMap<String, com.sk89q.jnbt.Tag> root = new HashMap<String, com.sk89q.jnbt.Tag>();
                    root.put("tickList", new ListTag(CompoundTag.class, tickList));
                    cached.update(getKey(chunk, Tag.PendingTicks, dim), write(Arrays.asList(new CompoundTag(root))));
                }
            }

            cached.update(getKey(chunk, Tag.FinalizedState, dim), COMPLETE_STATE);

            cached.update(getKey(chunk, Tag.Version, dim), VERSION);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private byte[] last;

    public class FileCache implements Comparable<FileCache>, Closeable {
        private FaweOutputStream os;
        private final File file;
        private int id;
        int numKeys = 0;

        public FileCache(int id) throws IOException {
            this.id = id;
            this.file = new File(getFolderTo() + File.separator + "cache" + File.separator + Integer.toHexString(id));
        }

        public void update(byte[] key, byte[] value) throws IOException {
            numKeys++;
            try {
                updateUnsafe(key, value);
            } catch (NullPointerException e) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                this.os = new FaweOutputStream(new BufferedOutputStream(new ZstdOutputStream(new LZ4BlockOutputStream(new FileOutputStream(file)))));
                updateUnsafe(key, value);
            }
        }

        private void updateUnsafe(byte[] key, byte[] value) throws IOException {
            os.writeVarInt(key.length);
            os.write(key);
            os.writeVarInt(value.length);
            os.write(value);
        }

        @Override
        public void close() throws IOException {
            try {
                this.os.close();

                FaweInputStream in = new FaweInputStream(new ZstdInputStream(new LZ4BlockInputStream(new BufferedInputStream(new FileInputStream(file)))));
                for (int i = 0; i < numKeys; i++) {
                    int len = in.readVarInt();
                    byte[] key = new byte[len];
                    in.readFully(key);

                    len = in.readVarInt();
                    byte[] value = new byte[len];
                    in.readFully(value);
                    db.put(key, value);
                }
                in.close();
                file.delete();
            } catch (Throwable e) {
                e.printStackTrace();
                throw e;
            }
        }

        @Override
        public int compareTo(FileCache other) {
            return Integer.compareUnsigned(this.id, other.id);
        }
    }

    private Int2ObjectOpenHashMap<FileCache> cache = new Int2ObjectOpenHashMap<>();

    public FileCache getFileCache(MCAChunk chunk, int dim) throws IOException {
        synchronized (cache) {
            int key = (chunk.getX() & 0xFFFFFF);
            if (dim == 0) {
//                key += 0xFFFFFFF;
            }
            FileCache cached = cache.get(key);
            if (cached == null) {
                cached = new FileCache(key);
                cache.put(key, cached);
            }
            return cached;
        }
    }

    public void copySection(byte[] src, byte[] dest, int destPos) {
        switch (src.length) {
            case 4096: {
                int index = 0;
                int i1, i2, i3;
                for (int y = 0; y < 16; y++) {
                    i1 = y;
                    for (int z = 0; z < 16; z++) {
                        i2 = i1 + (z << 4);
                        for (int x = 0; x < 16; x++) {
                            i3 = i2 + (x << 8);
                            dest[destPos + i3] = src[index];
                            index++;
                        }
                    }
                }
                break;
            }
            case 2048: {
                int index = 0;
                int i1, i2, i3, i4;
                for (int x = 0; x < 16;) {
                    {
                        i1 = x;
                        for (int z = 0; z < 16; z++) {
                            i2 = i1 + (z << 4);
                            for (int y = 0; y < 16; y += 2) {
                                i3 = i2 + (y << 8);
                                i4 = i2 + ((y + 1) << 8);
                                byte newVal = (byte) ((src[i3 >> 1] & 0xF) + ((src[i4 >> 1] & 0xF) << 4));
                                dest[destPos + index] = newVal;
                                index++;
                            }
                        }
                    }
                    x++;
                    {
                        i1 = x;
                        for (int z = 0; z < 16; z++) {
                            i2 = i1 + (z << 4);
                            for (int y = 0; y < 16; y += 2) {
                                i3 = i2 + (y << 8);
                                i4 = i2 + ((y + 1) << 8);
                                byte newVal = (byte) (((src[i3 >> 1] & 0xF0) >> 4) + ((src[i4 >> 1] & 0xF0)));
                                dest[destPos + index] = newVal;
                                index++;
                            }
                        }
                    }
                    x++;

                }
                break;
            }
            default:
                System.arraycopy(src, 0, dest, destPos, src.length);
        }
    }

    public byte[] write(Collection<com.sk89q.jnbt.Tag> tags) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NBTOutputStream nos = new NBTOutputStream(baos);
        nos.setLittleEndian();
        for (com.sk89q.jnbt.Tag tag : tags) {
            nos.writeNamedTag("", tag);
        }
        nos.close();
        return baos.toByteArray();
    }

    public List<NamedTag> read(byte[] data) {
        ArrayList<NamedTag> list = new ArrayList<>();
        ByteArrayInputStream baos = new ByteArrayInputStream(data);
        try (NBTInputStream in = new NBTInputStream((DataInput) new LittleEndianDataInputStream(baos))) {
            while (baos.available() > 0) {
                NamedTag nt = in.readNamedTag();
                list.add(nt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private CompoundTag transformItem(CompoundTag item) {
        String itemId = item.getString("id");
        short damage = item.getShort("Damage");
        BaseItem remapped = remapper.remapItem(itemId, damage);
        Map<String, com.sk89q.jnbt.Tag> map = ReflectionUtils.getMap(item.getValue());
        map.put("id", new ShortTag((short) remapped.getType()));
        map.put("Damage", new ShortTag((short) remapped.getData()));
        if (!map.containsKey("Count")) map.put("Count", new ByteTag((byte) 0));

        CompoundTag tag = (CompoundTag) item.getValue().get("tag");
        if (tag != null) {
            Map<String, com.sk89q.jnbt.Tag> tagMap = ReflectionUtils.getMap(tag.getValue());
            ListTag<CompoundTag> enchants = (ListTag<CompoundTag>) tagMap.get("ench");
            if (enchants != null) {
                for (CompoundTag ench : enchants.getValue()) {
                    Map<String, com.sk89q.jnbt.Tag> value = ReflectionUtils.getMap(ench.getValue());
                    String id = ench.getString("id");
                    String lvl = ench.getString("lvl");
                    if (id != null && !id.isEmpty()) value.put("id", new ShortTag(Short.parseShort(id)));
                    if (lvl != null && !lvl.isEmpty()) value.put("lvl", new ShortTag(Short.parseShort(lvl)));
                }
            }
            CompoundTag tile = (CompoundTag) tagMap.get("BlockEntityTag");
            if (tile != null) {
                tagMap.putAll(tile.getValue());
            }
        }
        return item;
    }

    private boolean transform(MCAChunk chunk, CompoundTag tag, boolean entity) {
        try {
            String id = tag.getString("id");
            if (id != null) {
                Map<String, com.sk89q.jnbt.Tag> map = ReflectionUtils.getMap(tag.getValue());
                if (entity) {
                    int legacyId = remapper.remapEntityId(id);
                    if (legacyId != -1) map.put("id", new IntTag(legacyId));
                } else {
                    id = remapper.remapBlockEntityId(id);
                    map.put("id", new StringTag(id));
                }
                { // Hand items
                    com.sk89q.jnbt.ListTag items = (ListTag) map.remove("HandItems");
                    if (items != null) {
                        CompoundTag hand = transformItem((CompoundTag) items.getValue().get(0));
                        CompoundTag offHand = transformItem((CompoundTag) items.getValue().get(1));
                        map.put("Mainhand", hand);
                        map.put("Offhand", offHand);
                    }
                }
                { // Convert armor
                    com.sk89q.jnbt.ListTag items = (ListTag) map.remove("ArmorItems");
                    if (items != null) {
                        ((List<CompoundTag>) items.getValue()).forEach(this::transformItem);
                        Collections.reverse(items.getValue());
                        map.put("Armor", items);
                    }
                }
                { // Convert items
                    com.sk89q.jnbt.ListTag items = tag.getListTag("Items");
                    if (items != null) {
                        ((List<CompoundTag>) items.getValue()).forEach(this::transformItem);
                    }
                }
                { // Convert color
                    for (String key : new String[] {"color", "Color"}) {
                        com.sk89q.jnbt.Tag value = map.get(key);
                        if (value instanceof IntTag) {
                            map.put(key, new ByteTag((byte) (int) ((IntTag) value).getValue()));
                        }
                    }
                }
                { // Convert item
                    String item = tag.getString("Item");
                    if (item != null) {
                        short damage = tag.getShort("Data");
                        BaseItem remapped = remapper.remapItem(item, damage);
                        map.put("Item", new ShortTag((short) remapped.getType()));
                        map.put("mData", new IntTag(remapped.getData()));
                    }
                }
                { // Health
                    com.sk89q.jnbt.Tag tVal = map.get("Health");
                    if (tVal != null) {
                        short newVal = ((Number) tVal.getValue()).shortValue();
                        map.put("Health", new ShortTag((short) (newVal * 2)));
                    }
                }
                for (String key : new String[] {"Age", "Health"}) {
                    com.sk89q.jnbt.Tag tVal = map.get(key);
                    if (tVal != null) {
                        short newVal = ((Number) tVal.getValue()).shortValue();
                        map.put(key, new ShortTag(newVal));
                    }
                }
                { // Orientation / Position
                    for (String key : new String[] {"Orientation", "Position", "Rotation", "Pos", "Motion"}) {
                        ListTag list = (ListTag) map.get(key);
                        if (list != null) {
                            List<com.sk89q.jnbt.Tag> value = list.getValue();
                            ArrayList<FloatTag> newList = new ArrayList<>();
                            for (com.sk89q.jnbt.Tag coord : value) {
                                newList.add(new FloatTag(((Number) coord.getValue()).floatValue()));
                            }
                            map.put(key, new ListTag(FloatTag.class, newList));
                        }
                    }
                }
                switch (id) {
                    case "EndGateway":
                    case "MobSpawner": {
                        map.clear();
                        break;
                    }
                    case "Sign": {
                        for (int line = 1; line <= 4; line++) {
                            String key = "Text" + line;
                            String text = tag.getString(key);
                            if (text != null && text.startsWith("{")) {
                                map.put(key, new StringTag(BBC.jsonToString(text)));
                            }
                        }
                        break;
                    }
                    case "CommandBlock": {
                        int x = tag.getInt("x");
                        int y = tag.getInt("y");
                        int z = tag.getInt("z");

                        map.put("Version", new IntTag(3));
                        BaseBlock block = chunk.getBlock(x & 15, y, z & 15);

                        int LPCommandMode = 0;
                        switch (block.getId()) {
                            case 189:
                                LPCommandMode = 2;
                                break;
                            case 188:
                                LPCommandMode = 1;
                                break;
                        }

                        // conditionMet

                        boolean conditional = block.getData() > 7;
                        byte auto = tag.getByte("auto");
                        map.putIfAbsent("isMovable", new ByteTag((byte) 1));
                        map.put("LPCommandMode", new IntTag(LPCommandMode));
                        map.put("LPCondionalMode", new ByteTag((byte) (conditional ? 1 : 0)));
                        map.put("LPRedstoneMode", new ByteTag((byte) (auto == 0 ? 1 : 0)));


                        if (LPCommandMode == 1 && ((auto == 1 || tag.getByte("powered") == 1) && (!conditional || tag.getByte("conditionMet") == 1))) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Fawe.debug("Error converting tag: " + tag);
            e.printStackTrace();
        }
        return false;
    }

    public byte[] getSectionKey(MCAChunk chunk, int layer, int dimension) {
        return getSectionKey(chunk.getX(), chunk.getZ(), layer, dimension);
    }

    private static byte[] getSectionKey(int x, int z, int layer, int dimension) {
        if (dimension == 0) {
            byte[] key = Tag.SubChunkPrefix.fill(x, z, new byte[10]);
            key[9] = (byte) layer;
            return key;
        }
        byte[] key = new byte[14];
        Tag.SubChunkPrefix.fill(x, z, key);
        key[12] = key[8];
        key[8] = (byte) dimension;
        key[13] = (byte) layer;
        return key;
    }

    public byte[] getKey(MCAChunk chunk, Tag tag, int dimension) {
        if (dimension == 0) {
            return tag.fill(chunk.getX(), chunk.getZ(), new byte[9]);
        }
        byte[] key = new byte[13];
        tag.fill(chunk.getX(), chunk.getZ(), key);
        key[12] = key[8];
        key[8] = (byte) dimension;
        return key;
    }
}