package developpeur2000.minecraft.minecraft_rw.world;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import developpeur2000.minecraft.minecraft_rw.entity.BlockEntity;
import developpeur2000.minecraft.minecraft_rw.entity.Entity;
import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTMarshal;
import ign.minecraft.MineGenerator;
import ign.minecraft.Utilities;

/**
 * Represents a Minecraft world.
 */
public class World {
    private static final Logger LOGGER = Logger.getLogger("World");
    
    private static long regionToKey(int x, int z) {
        return ((long) x << 32) | ((long) z & 0xffffffffL);
    }
    private static int regionKeyX(long key) {
        return (int) (key >>> 32);
    }
    private static int regionKeyZ(long key) {
        return (int) (key & 0xffffffffL);
    }

    private static int blockToRegion(int b) {
        return (int) Math.floorDiv(b, Region.BLOCKS);
    }

    private final Path baseDir;
    private boolean readOnly;
    private final Map<Long, Region> regions = new HashMap<>();
    private developpeur2000.minecraft.minecraft_rw.world.Level level;

    /**
     * Load or construct a new, empty world.
     *
     * @param baseDir the world's base directory on the file system.
     * @param readOnly if the world should be opened in read only.
     * @throws IOException 
     */
    public World(Path baseDir, boolean readOnly) throws IOException {
        this.baseDir = Objects.requireNonNull(baseDir);
        this.readOnly = readOnly;
        
        File testDirFile = baseDir.toFile();
        if (!testDirFile.exists()) {
    		throw new WorldException("cannot create a world in a non existant baseDir");
        }
        //create necessary subdirectories if needed
        for (String subDirName : new String[] { "region", "data" }) {
	        testDirFile = baseDir.resolve(subDirName).toFile();
	        if (!testDirFile.exists()) {
	        	testDirFile.mkdir();
	        }
        }
        
        if ( !loadLevelFile(baseDir.resolve("level.dat")) ) {
        	//create default level
        	level = new developpeur2000.minecraft.minecraft_rw.world.Level();
        }
    }
    public World(Path baseDir) throws IOException {
    	this(baseDir, false);
    }
    
    private void saveLevelFile(Path levelFilePath) throws IOException {
    	LOGGER.log(Level.INFO,"saving level file at " + levelFilePath.toString());
        try (	final DataOutputStream outputStream = new DataOutputStream (new FileOutputStream(levelFilePath.toString())) ) {
	        CompoundTag levelDataNbt = (CompoundTag) NBTMarshal.marshal(level);
	        CompoundTag levelNbt = new CompoundTag();
	        levelNbt.put("Data", levelDataNbt);
	        
			NBT.save(outputStream, levelNbt, "java.util.zip.GZIPOutputStream");
        } catch (ClassNotFoundException e) {
			assert false;
			return;
		}
        level.saved();
    }

    private boolean loadLevelFile(Path levelFilePath) throws IOException {
        if (!Files.exists(levelFilePath)) {
        	return false;
        }

        try (	final DataInputStream inputStream = new DataInputStream (new FileInputStream(levelFilePath.toString())) ) {
	        CompoundTag levelDataNbt = NBT.load(inputStream);
	        level = NBTMarshal.unmarshal(developpeur2000.minecraft.minecraft_rw.world.Level.class, levelDataNbt.getCompound("Data"));
        }
        
        return true;
    }

    /**
     * Saves dirty regions back to the file system. and saves level file if needed
     */
    public void save() throws Exception {
    	if (readOnly) {
    		throw new WorldException("you are in readonly mode and cannot save the map");
    	}
    	
        for (Region region : regions.values()) {
            if (region.isDirty()) {
                region.save();
            }
        }
        if (level.isDirty()) {
        	saveLevelFile(baseDir.resolve("level.dat"));
        }
    }
    
    /**
     * Saves regions to the file system at the specified Path.
     * 
     * @param path
     * @throws IOException
     */
    public void save(Path path) throws IOException {
    	int regionX, regionZ;
    	Path regionFile;
    	File regionDirectory;
    	for (Entry<Long, Region> entry : regions.entrySet()) {
            regionX = regionKeyX(entry.getKey());
            regionZ = regionKeyZ(entry.getKey());
            regionFile = getRegionFile(path, regionX, regionZ);
            regionDirectory = regionFile.getParent().toFile();
			if (!regionDirectory.exists()) {
				regionDirectory.mkdir();
		    }
            try {
            	LOGGER.log(Level.INFO, "saving region " + regionFile.toString());
            	entry.getValue().save(regionFile);
            } catch (IOException ex) {
            	throw new WorldException("Failed to save region at " + regionFile.toString(), ex);
            }
        }
    	
       	saveLevelFile(path.resolve("level.dat"));
    }
    
    public developpeur2000.minecraft.minecraft_rw.world.Level getLevel() {
    	return level;
    }
        
    
    private Path getRegionFile(int x, int z) {
        return getRegionFile(baseDir,x,z);
    }
    private Path getRegionFile(Path worldDir, int x, int z) {
        return worldDir.resolve("region/r." + x + "." + z + ".mca");
    }

    public Region getRegionAt(int x, int z) {
        final int regionX = blockToRegion(x);
        final int regionZ = blockToRegion(z);
        final long regionKey = regionToKey(regionX, regionZ);

        Region region = regions.get(regionKey);
        if (region == null) {
            final Path regionFile = getRegionFile(regionX, regionZ);
            try {
                LOGGER.log(Level.INFO, "Initializing region " + regionFile.toString());
                region = new Region(regionFile);
            } catch (IOException ex) {
                throw new WorldException("Failed to initialize region from " + regionFile.toString(), ex);
            }
            
            if (readOnly && region.isDirty()) {
            	//means the region was just created, in readonly mode we consider this region does not exist
            	return null;
            }

            regions.put(regionKey, region);
        }

        return region;
    }

    /**
     * Gets the block at the specified position.
     *
     * @param x the block's X coordinate in the world.
     * @param y the block's Y coordinate in the world.
     * @param z the block's Z coordinate in the world.
     * @return the block at the specified positon, guaranteed to be non-null.
     */
    public Block getBlock(int x, int y, int z) {
    	final Region region = getRegionAt(x, z);
        final Block block = (region != null) ? region.getBlock(x, y, z) : null;
        return (block != null) ? block : Block.AIR_BLOCK;
    }

    /**
     * tells if a block is defined at the specified position.
     *
     * @param x the block's X coordinate in the world.
     * @param y the block's Y coordinate in the world.
     * @param z the block's Z coordinate in the world.
     * @return true if a block is defined.
     */
    public boolean hasBlock(int x, int y, int z) {
    	final Region region = getRegionAt(x, z);
        return (region != null) ? (getRegionAt(x, z).getBlock(x, y, z) != null) : false;
    }
    
    /**
     * Sets the block at the specified position.
     *
     * @param x     the block's X coordinate in the world.
     * @param y     the block's Y coordinate in the world.
     * @param z     the block's Z coordinate in the world.
     * @param rotate whether we rotate or not
     * @param block the new block for the specified position. May be {@code null} to indicate air.
     */
    public void setBlock(int x, int y, int z, Block block) {
    	if (readOnly) {
    		throw new WorldException("you are in readonly mode and cannot set blocks");
    	}
    	
        if (block != null && block.getType() == BlockType.AIR) {
            block = null; //don't actually save air blocks...
        }

        getRegionAt(x, z).setBlock(x, y, z, block);
    }
    
    public void addEntity(double x, double y, double z, Entity entity) {
    	if (readOnly) {
    		throw new WorldException("you are in readonly mode and cannot set blocks");
    	}
    	
        getRegionAt((int) x, (int) z).addEntity(x, y, z, entity);
    }
    
	public ArrayList<Entity> listEntitiesInChunk(int x, int z) {
    	final Region region = getRegionAt(x, z);
        return (region != null) ? getRegionAt(x, z).listEntitiesInChunk(x, z) : new ArrayList<Entity>();
	}
	
    public void addBlockEntity(int x, int y, int z, BlockEntity blockEntity) {
    	if (readOnly) {
    		throw new WorldException("you are in readonly mode and cannot set blocks");
    	}
    	
        getRegionAt(x, z).addBlockEntity(x, y, z, blockEntity);
    }
    
	public ArrayList<BlockEntity> listBlockEntitiesInChunk(int x, int z) {
    	final Region region = getRegionAt(x, z);
        return (region != null) ? getRegionAt(x, z).listBlockEntitiesInChunk(x, z) : new ArrayList<BlockEntity>();
	}
	
    public void addMapItem(MapItem mapItem) throws IOException {
    	String filePath = baseDir.resolve("data").resolve(mapItem.getFilename()).toString();
    	LOGGER.log(Level.INFO,"saving map item " + filePath);
        try (	final DataOutputStream outputStream = new DataOutputStream (new FileOutputStream(filePath)) ) {
	        CompoundTag mapDataNbt = (CompoundTag) NBTMarshal.marshal(mapItem);
	        
			NBT.save(outputStream, mapDataNbt, "java.util.zip.GZIPOutputStream");
        } catch (ClassNotFoundException e) {
			assert false;
			return;
		}
        mapItem.saved();
    }

    public MapItem loadMapItem(Path mapFilePath) throws IOException {
        if (!Files.exists(mapFilePath)) {
        	return null;
        }

        MapItem mapItem = null;
        try (	final DataInputStream inputStream = new DataInputStream (new FileInputStream(mapFilePath.toString())) ) {
	        CompoundTag mapItemDataNbt = NBT.load(inputStream);
	        mapItem = NBTMarshal.unmarshal(developpeur2000.minecraft.minecraft_rw.world.MapItem.class, mapItemDataNbt);
        }
        return mapItem;
    }


    /**
     * Unload regions from memory
     */
	public void unloadRegions() {
		for (Entry<Long, Region> entry : regions.entrySet()) {
			assert !entry.getValue().isDirty();
		}
		regions.clear();
	}
}
