package developpeur2000.minecraft.minecraft_rw.world;

import developpeur2000.minecraft.minecraft_rw.entity.BlockEntity;
import developpeur2000.minecraft.minecraft_rw.entity.Entity;
import developpeur2000.minecraft.minecraft_rw.math.Vec3d;
import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.ListTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT.Type;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTCompoundProcessor;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTMarshal;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a chunk, consisting of 16 Y sections.
 */
@NBTCompoundType
public class Chunk implements NBTCompoundProcessor {
    private static final Logger LOGGER = Logger.getLogger("Chunk");
    
    public static final int BLOCKS = 16; //in blocks
    static final int BLOCKS_SQ = BLOCKS * BLOCKS;
    
    static final byte BIOME_DEFAULT = Biome.PLAINS;
    static final byte MAX_LIGHT = 0x0f;
    
    private static byte yToSection(int y) {
        return (byte)(y / Section.BLOCKS);
    }

    private static int xz1D(int x, int z) {
        return z * BLOCKS + x;
    }

    private int x;
    private int z;
    private long lastUpdate;
    private long inhabitedTime;
    private final byte[] biomes = new byte[BLOCKS_SQ];;
    private final int[] heightmap = new int[BLOCKS_SQ];
    private boolean lightPopulated;
    private boolean terrainPopulated;
    private final Map<Byte, Section> sections = new HashMap<>();
    private final ArrayList<Entity> entities = new ArrayList<>();
    private final ArrayList<BlockEntity> tileEntities = new ArrayList<>();

    private transient boolean dirty = false;

    /**
     * Constructs an empty chunk structure, destined to be filled by unmarshalling data.
     */
    public Chunk() {
    }

    /**
     * Constructs a new chunk with basic data.
     */
    public Chunk(int x, int z) {
    	this.x = x;
    	this.z = z;
    	terrainPopulated = true;
    	lightPopulated = false;
    	//set default biome
    	for (int i = 0; i < BLOCKS_SQ; i++) {
    		biomes[i] = BIOME_DEFAULT;
    	}
    }

    @Override
    public void unmarshalCompound(CompoundTag nbt) {
    	try {
	        x = nbt.getInt("xPos");
	        z = nbt.getInt("zPos");
	        if (nbt.contains("Biomes")) {
	        	System.arraycopy(nbt.getByteArray("Biomes"), 0, biomes, 0, BLOCKS_SQ);
	        }
	        System.arraycopy(nbt.getIntArray("HeightMap"), 0, heightmap, 0, BLOCKS_SQ);
	
	        lightPopulated = nbt.getBoolean("LightPopulated");
	        if (nbt.contains("TerrainPopulated")) {
	            terrainPopulated = nbt.getBoolean("TerrainPopulated");
	        }
	        else {
	        	terrainPopulated = true;
	        }
	
	        lastUpdate = nbt.getLong("LastUpdate");
	        if (nbt.contains("InhabitedTime")) {
	        	inhabitedTime = nbt.getLong("InhabitedTime");
	        }
	        else {
	        	//chunk does not contain InhabitedTime as it should, but minecraft accept this so we do too
	        	inhabitedTime = 0;
	        }
	
	        for (NBT<?> sectionNbt : nbt.getList("Sections")) {
	            final Section section = NBTMarshal.unmarshal(Section.class, sectionNbt);
	            sections.put(section.getY(), section);
	        }
	        
	        for (NBT<?> entityNbt : nbt.getList("Entities")) {
	            Entity entity = NBTMarshal.unmarshal(Entity.class, entityNbt);
            	try {
	            	entity = (Entity) NBTMarshal.unmarshal(Class.forName("de.pdinklag.minecraft.entity."+entity.getId()), entityNbt);
            	} catch (ClassNotFoundException e) {
	            	//not implemented yet
                	LOGGER.log(Level.INFO,"failed to load entity of type " + entity.getId()
                		+ " at " + entity.getPos().toString() + " because it is not implemented yet");

	            	//TODO: projectiles, xpOrbs, vehicles, dynamicTiles, Other
	            	entity = null;
            	}
	            if (entity != null) {
	            	entities.add(entity);
	            }
	        }
	        
	        for (NBT<?> blockEntityNbt : nbt.getList("TileEntities")) {
	        	BlockEntity blockEntity = NBTMarshal.unmarshal(BlockEntity.class, blockEntityNbt);
            	try {
            		blockEntity = (BlockEntity) NBTMarshal.unmarshal(Class.forName("de.pdinklag.minecraft.entity."+blockEntity.getId()), blockEntityNbt);
            	} catch (ClassNotFoundException e) {
	            	//not implemented yet
                	LOGGER.log(Level.INFO,"failed to load block entity of type " + blockEntity.getId()
                		+ " at " + blockEntity.getX() + "," + blockEntity.getY() + "," + blockEntity.getZ()
                		+ " because it is not implemented yet");
                	blockEntity = null;
            	}
	            if (blockEntity != null) {
	            	tileEntities.add(blockEntity);
	            }
	        }

	        dirty = false;
    	}
    	catch (Exception e) {
	        throw new WorldException("malformed chunk", e);
    	}
    }

    @Override
    public CompoundTag marshalCompound() {
    	//compute the lighting if needed before marshalling the data
    	computeLighting();    	
    	
        CompoundTag root = new CompoundTag();
        root.put("xPos", x);
        root.put("zPos", z);
        //we consider the marshalling as the trigger for updating the lastUpdate field
    	Date date= new Date();
        lastUpdate = date.getTime();
        root.put("LastUpdate", lastUpdate);
        root.put("LightPopulated", lightPopulated);
        root.put("TerrainPopulated", terrainPopulated);
        root.put("V", (byte)1);
        root.put("InhabitedTime", inhabitedTime);
       	root.put("Biomes", biomes);
        root.put("HeightMap", heightmap);
        //create sections as NBT
        ListTag sectionListNbt = new ListTag();
        for(Section section: sections.values()) {
        	if(!section.isEmpty()) {
        		sectionListNbt.add(NBTMarshal.marshal(section));
        	}
        }
        root.put("Sections", sectionListNbt);
        
        ListTag nbtList;
        
        nbtList = new ListTag();
        if (entities.size() == 0) {
        	nbtList.setType(Type.BYTE);//because list is empty
        } else {
	        for(Entity entity: entities) {
	        	nbtList.add(NBTMarshal.marshal(entity));
	        }
        }
        root.put("Entities", nbtList);

        nbtList = new ListTag();
        if (tileEntities.size() == 0) {
        	nbtList.setType(Type.BYTE);//because list is empty
        } else {
	        for(BlockEntity blockEntity: tileEntities) {
	        	nbtList.add(NBTMarshal.marshal(blockEntity));
	        }
        }
        root.put("TileEntities", nbtList);
        
        return root;
    }

    public Block getBlock(int x, int y, int z) {
        final Section section = sections.get(yToSection(y));
        if (section != null) {
            return section.getBlock(x, y, z);
        } else {
            return null;
        }
    }

    public void setBlock(int x, int y, int z, Block block) {
        final byte secY = yToSection(y);

        Section section = sections.get(secY);
        if (section == null) {
            section = new Section(); //lazily create new section
            section.setY(secY);
            sections.put(secY, section);
        }

        if (section != null) {
            section.setBlock(x, y, z, block);

            //update heightmap
            x = Section.blockInSection(x);
            z = Section.blockInSection(z);
            heightmap[xz1D(x,z)] = Math.max(heightmap[xz1D(x,z)], y);
            
            lightPopulated = false; //invalidate lightmap
            dirty = true;
        }
    }

    public void addEntity(double x, double y, double z, Entity entity) {
    	entity.setPos(new Vec3d(x,y,z));
    	entities.add(entity);
    	
        dirty = true;
    }
    
    public void addBlockEntity(int x, int y, int z, BlockEntity blockEntity) {
    	blockEntity.setX(x);
    	blockEntity.setY(y);
    	blockEntity.setZ(z);
    	tileEntities.add(blockEntity);
    	
        dirty = true;
    }
    
    public void computeLighting () {
    	//only compute if it is out of date
    	if(lightPopulated) {
    		return;
    	}
    	
    	//simplistic method : set skylight to full between top and surface block
    	// then only go down straight through transparent blocks
    	for (int xInChunk = 0; xInChunk < BLOCKS; xInChunk++) {
    		for (int zInChunk = 0; zInChunk < BLOCKS; zInChunk++) {
    			int curHeight = heightmap[xz1D(xInChunk,zInChunk)];
    			byte curLight = MAX_LIGHT;
    			for (int y = 255 ; y >= 0; y--) {
    				byte sectionY = yToSection(y);
    				Section section = sections.get(sectionY);
    				if (section == null) {
    					//go straight to next section
    					y -= Section.BLOCKS - 1;
    				} else {
    					if (y >= curHeight) {
    						section.setSkyLight(xInChunk, y, zInChunk, MAX_LIGHT);
    					}
    					else {
    						//compute the light dimming or stopping depending on the block type
    						if (section.getBlockInChunk(xInChunk, y, zInChunk) != null) {
	    						switch (section.getBlockInChunk(xInChunk, y, zInChunk).getType()) {
	       						case AIR:
	    						case GLASS:
	       						case CARPET:
	       							//totally transparent block : keep same amount of light
	       							break;
	    						case REDSTONE_BLOCK:
	    						case LIT_FURNACE:
	    						case GLOWSTONE:
	    						case ICE:
	    						case LIT_PUMPKIN:
	    						case LEAVES:
	    						case LEAVES2:
	    						case PISTON:
	    						case REDSTONE_LAMP:
	    						//case REDSTONE_ORE: //only when active TODO
	       						case STICKY_PISTON:
	       						case TNT:
	       						case ANVIL:
	       						case BED:
	       						case BREWING_STAND:
	       						case CAKE:
	       						case CAULDRON:
	       						case CHEST:
	       						case COBBLESTONE_WALL:
	       						case DAYLIGHT_DETECTOR:
	       						case DAYLIGHT_DETECTOR_INVERTED:
	       						case WOODEN_DOOR:
	       						case SPRUCE_DOOR:
	       						case BIRCH_DOOR:
	       						case JUNGLE_DOOR:
	       						case ACACIA_DOOR:
	       						case DARK_OAK_DOOR:
	       						case IRON_DOOR:
	       						case ENCHANTING_TABLE:
	       						case ENDER_CHEST:
	       						case FENCE:
	       						case SPRUCE_FENCE:
	       						case BIRCH_FENCE:
	       						case JUNGLE_FENCE:
	       						case DARK_OAK_FENCE:
	       						case ACACIA_FENCE:
	       						case FENCE_GATE:
	       						case SPRUCE_FENCE_GATE:
	       						case BIRCH_FENCE_GATE:
	       						case JUNGLE_FENCE_GATE:
	       						case DARK_OAK_FENCE_GATE:
	       						case ACACIA_FENCE_GATE:
	       						case GLASS_PANE:
	       						case HOPPER:
	       						case IRON_BARS:
	       						case LADDER:
	       						case WATERLILY:
	       						case NETHER_BRICK_FENCE:
	       						case POWERED_REPEATER:
	       						case POWERED_COMPARATOR:
	       						case SNOW_LAYER:
	       						case TRAPDOOR:
	       						case TRAPPED_CHEST:
	       						case VINE:
	       						case STONE_BUTTON:
	       						case WOODEN_BUTTON:
	       						case LEVER:
	       						case STONE_PRESSURE_PLATE:
	       						case WOODEN_PRESSURE_PLATE:
	       						case LIGHT_WEIGHTED_PRESSURE_PLATE:
	       						case HEAVY_WEIGHTED_PRESSURE_PLATE:
	       						case RAIL:
	       						case GOLDEN_RAIL:
	       						case DETECTOR_RAIL:
	       						case ACTIVATOR_RAIL:
	       						case REDSTONE_WIRE:
	       						case REDSTONE_TORCH:
	       						case END_PORTAL:
	       						case FIRE:
	       						case PORTAL:
	       						case WALL_SIGN:
	       						case STANDING_SIGN:
	       						case TORCH:
	       						case CACTUS:
	       						case WHEAT:
	       						case MELON_BLOCK:
	       						case PUMPKIN:
	       						case PUMPKIN_STEM:
	       						case REEDS:
	       						case POTATOES:
	       						case CARROTS:
	       						case COCOA:
	       						case NETHER_WART:
	       						case YELLOW_FLOWER:
	       						case RED_FLOWER:
	       						case DOUBLE_PLANT:
	       						case TALLGRASS:
	       						case BROWN_MUSHROOM:
	       						case RED_MUSHROOM:
	       						case SAPLING:
	       						case LAVA:
	       						case FLOWING_LAVA:
	       						case WATER:
	       						case FLOWING_WATER:
	       						case END_PORTAL_FRAME:
	       						case MOB_SPAWNER:
	       						case SLIME_BLOCK:
	       							//other transparent block : light diminished by 1
	       							curLight--;
	       							break;
	       						default:
	       							//opaque block
	       							curLight = 0;
	    						}
    						}
    						if (curLight > 0) {
    							section.setSkyLight(xInChunk, y, zInChunk, curLight);
    						} else {
	    						//no need to go to lower blocks
	    						break;
    						}
    					}
    				}
    			}
        	}	
    	}
    	
    	lightPopulated = true;
    }

    @SuppressWarnings("unchecked")
	public ArrayList<Entity> getEntities() {
    	return (ArrayList<Entity>) entities.clone();
    }

    @SuppressWarnings("unchecked")
	public ArrayList<BlockEntity> getBlockEntities() {
    	return (ArrayList<BlockEntity>) tileEntities.clone();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void saved() {
        this.dirty = false;
    }

    public boolean isLightPopulated() {
        return lightPopulated;
    }

    public void setLightPopulated(boolean lightPopulated) {
        this.lightPopulated = lightPopulated;
    }

    public boolean isTerrainPopulated() {
        return terrainPopulated;
    }

    public void setTerrainPopulated(boolean terrainPopulated) {
        this.terrainPopulated = terrainPopulated;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getInhabitedTime() {
        return inhabitedTime;
    }

    public void setInhabitedTime(long inhabitedTime) {
        this.inhabitedTime = inhabitedTime;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
