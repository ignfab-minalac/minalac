package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Class for items as used by or contained in an entity or block entity.
 */
@NBTCompoundType
public class StoredItem {
	
    @NBTProperty()
    private String id;

    @NBTProperty(upperCase = true)
    private byte count;

    @NBTProperty(upperCase = true)
    private short damage;

    @NBTProperty(optional = true)
    private NBT tag;

    @NBTProperty(upperCase = true, optional = true)
    private byte slot;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public StoredItem() {
    	id = "";
    	count = 0;
    	damage = 0;
    }

    /**
     * Constructs a new item with specified values
     */
    public StoredItem(String id) {
    	this(id, (short) 0, (byte) 1);
    }
    public StoredItem(String id, byte count) {
    	this(id, (short) 0, count);
    }
    public StoredItem(String id, short damage, byte count) {
    	this.id = id;
    	this.count = count;
    	this.damage = damage;
    }
    
    /**
     * copy constructor
     */
    public StoredItem(StoredItem src) {
    	id = src.id;
    	count = src.count;
    	damage = src.damage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte getCount() {
        return count;
    }

    public void setCount(byte count) {
        this.count = count;
    }

    public short getDamage() {
        return damage;
    }

    public void setDamage(short damage) {
        this.damage = damage;
    }

    public NBT getTag() {
        return tag;
    }

    public void setTag(NBT tag) {
        this.tag = tag;
    }

    public byte getSlot() {
        return slot;
    }

    public void setSlot(byte slot) {
        this.slot = slot;
    }
}
