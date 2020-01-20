package developpeur2000.minecraft.minecraft_rw.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.StringTag;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for mob spawner block entities.
 */
@NBTCompoundType
public class MobSpawner extends BlockEntity {
    private static final Logger LOGGER = Logger.getLogger("MobSpawner");
	
    @NBTProperty(upperCase = true, optional = true, listItemType=MobSpawnerSpawn.class)
    private ArrayList<MobSpawnerSpawn> spawnPotentials;

    //deprecated but still there for reading compatibility with pre-1.9 maps
    @NBTProperty(upperCase = true, optional = true)
    private String entityId;

    @NBTProperty(upperCase = true, optional = true)
    private CompoundTag spawnData;

    @NBTProperty(upperCase = true)
    private short spawnCount;

    @NBTProperty(upperCase = true)
    private short spawnRange;

    @NBTProperty(upperCase = true)
    private short delay;

    @NBTProperty(upperCase = true)
    private short minSpawnDelay;

    @NBTProperty(upperCase = true)
    private short maxSpawnDelay;

    @NBTProperty(upperCase = true)
    private short maxNearbyEntities;

    @NBTProperty(upperCase = true)
    private short requiredPlayerRange;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public MobSpawner() {
    	super("MobSpawner");
        spawnCount = 0;
        spawnRange = 4;
        delay = -1;
        minSpawnDelay = 10;
        maxSpawnDelay = 10;
        maxNearbyEntities = 1;
        requiredPlayerRange = 1;
    }

    /**
     * copy constructor
     */
    public MobSpawner(MobSpawner src) {
    	super(src);
    	id = "MobSpawner";
        spawnPotentials = src.spawnPotentials;
        spawnData = src.spawnData;
        spawnCount = src.spawnCount;
        spawnRange = src.spawnRange;
        delay = src.delay;
        minSpawnDelay = src.minSpawnDelay;
        maxSpawnDelay = src.maxSpawnDelay;
        maxNearbyEntities = src.maxNearbyEntities;
        requiredPlayerRange = src.requiredPlayerRange;
    }
    
    public ArrayList<MobSpawnerSpawn> getSpawnPotentials() {
        return spawnPotentials;
    }
    public void setSpawnPotentials(ArrayList<MobSpawnerSpawn> spawnPotentials) {
        this.spawnPotentials = spawnPotentials;
        //store as 1.8 value too
    	try {
    		entityId = (String) spawnPotentials.get(0).getEntity().get("id").getValue();
    	} catch (Exception e) {
        	LOGGER.log(Level.INFO,"failed to get entity id from spawn potentials "
        		+ "of MobSpawner at " + getX() + "," + getY() + "," + getZ());
    	}
    }
    
    public String getEntityId() {
        return entityId;
    }
    public void setEntityId(String entityId) {
    	this.entityId = entityId;
    	//store the deprecated value in the new format
    	HashMap<String, NBT> entity = new HashMap<String, NBT>();
    	entity.put("id", new StringTag(entityId));
    	spawnPotentials = new ArrayList<MobSpawnerSpawn>();
    	spawnPotentials.add( new MobSpawnerSpawn( entity ) );
    }

    public CompoundTag getSpawnData() {
        return spawnData;
    }
    public void setSpawnData(CompoundTag spawnData) {
        this.spawnData = spawnData;
    }

    public short getSpawnCount() {
        return spawnCount;
    }
    public void setSpawnCount(short spawnCount) {
        this.spawnCount = spawnCount;
    }

    public short getSpawnRange() {
        return spawnRange;
    }
    public void setSpawnRange(short spawnRange) {
        this.spawnRange = spawnRange;
    }

    public short getDelay() {
        return delay;
    }
    public void setDelay(short delay) {
        this.delay = delay;
    }

    public short getMinSpawnDelay() {
        return minSpawnDelay;
    }
    public void setMinSpawnDelay(short minSpawnDelay) {
        this.minSpawnDelay = minSpawnDelay;
    }

    public short getMaxSpawnDelay() {
        return maxSpawnDelay;
    }
    public void setMaxSpawnDelay(short maxSpawnDelay) {
        this.maxSpawnDelay = maxSpawnDelay;
    }

    public short getMaxNearbyEntities() {
        return maxNearbyEntities;
    }
    public void setMaxNearbyEntities(short maxNearbyEntities) {
        this.maxNearbyEntities = maxNearbyEntities;
    }

    public short getRequiredPlayerRange() {
        return requiredPlayerRange;
    }
    public void setRequiredPlayerRange(short requiredPlayerRange) {
        this.requiredPlayerRange = requiredPlayerRange;
    }
}
