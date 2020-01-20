package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.ByteTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.ShortTag;
import developpeur2000.minecraft.minecraft_rw.nbt.StringTag;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

import java.util.HashMap;

/**
 * Class for item frame entities.
 */
@NBTCompoundType
public class ItemFrame extends Entity {
	
	public static final byte FACING_SOUTH = 0;
	public static final byte FACING_WEST = 1;
	public static final byte FACING_NORTH = 2;
	public static final byte FACING_EAST = 3;
	
    @NBTProperty(upperCase = true)
    private int tileX;

    @NBTProperty(upperCase = true)
    private int tileY;

    @NBTProperty(upperCase = true)
    private int tileZ;

    @NBTProperty(upperCase = true)
    private byte facing;

    @NBTProperty(upperCase = true, optional = true)
    private HashMap<String,NBT> item;
    
    @NBTProperty(upperCase = true)
    private float itemDropChance;

    @NBTProperty(upperCase = true)
    private byte itemRotation;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public ItemFrame() {
    	super("ItemFrame");
    	
    	itemDropChance = 0;
    }

    /**
     * Constructs a new item with specified values
     */
    public ItemFrame(int x, int y, int z, byte facing, byte itemRotation, String itemId, short damage) {
    	this();
    	this.tileX = x;
    	this.tileY = y;
    	this.tileZ = z;
    	this.facing = facing;
    	this.itemRotation = itemRotation;
    	item = new HashMap<String,NBT>();
    	item.put("id", new StringTag(itemId));
    	item.put("Count", new ByteTag((byte) 1));
    	item.put("Damage", new ShortTag(damage));
    }
    
    public ItemFrame(int x, int y, int z, byte facing) {
    	this();
    	this.tileX = x;
    	this.tileY = y;
    	this.tileZ = z;
    	this.facing = facing;
    	this.itemRotation = 0;
    }
    
    /**
     * copy constructor
     */
    public ItemFrame(ItemFrame src) {
    	super(src);

    	this.tileX = src.tileX;
    	this.tileY = src.tileY;
    	this.tileZ = src.tileZ;
    	this.facing = src.facing;
    	this.itemRotation = src.itemRotation;
    	this.item = new HashMap<String,NBT>(src.item);
    }

    public int getTileX() {
        return tileX;
    }

    public void setTileX(int tileX) {
        this.tileX = tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTileY(int tileY) {
        this.tileY = tileY;
    }

    public int getTileZ() {
        return tileZ;
    }

    public void setTileZ(int tileZ) {
        this.tileZ = tileZ;
    }

    public byte getFacing() {
        return facing;
    }

    public void setFacing(byte facing) {
        this.facing = facing;
    }

    public float getItemDropChance() {
        return itemDropChance;
    }

    public void setItemDropChance(float itemDropChance) {
        this.itemDropChance = itemDropChance;
    }

    public byte getItemRotation() {
        return itemRotation;
    }

    public void setOItemRotation(byte itemRotation) {
        this.itemRotation = itemRotation;
    }

    public HashMap<String,NBT> getItem() {
        return item;
    }

    public void setItem(HashMap<String,NBT> item) {
    	this.item = item;
    }


}
