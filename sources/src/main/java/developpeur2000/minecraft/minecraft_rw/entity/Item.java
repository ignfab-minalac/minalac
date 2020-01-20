package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Class for item entities (the ones you can pick from the ground).
 */
@NBTCompoundType
public class Item extends Entity {
	
    @NBTProperty(upperCase = true)
    private short age;

    @NBTProperty(upperCase = true)
    private short health;

    @NBTProperty(upperCase = true)
    private short pickupDelay;

    @NBTProperty(upperCase = true, optional = true)
    private String owner;

    @NBTProperty(upperCase = true, optional = true)
    private String thrower;

    @NBTProperty(upperCase = true)
    private StoredItem item;
    
    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Item() {
    	super("Item");

        age = -32768;
        health = 5;
        pickupDelay = 0;
    }

    /**
     * Constructs a new item with specified values
     */
    public Item(String itemId, short damage, byte count) {
    	this();
    	item = new StoredItem(itemId, damage, count);
    }
    public Item(String itemId, byte count) {
    	this(itemId, (short) 0, count);
    }
    public Item(String itemId) {
    	this(itemId, (byte) 1);
    }
    
    /**
     * copy constructor
     */
    public Item(Item src) {
    	super(src);

        age = src.age;
        health = src.health;
        pickupDelay = src.pickupDelay;
        owner = src.owner;
        thrower = src.thrower;
    	item = new StoredItem(src.item);
    }

    public short getAge() {
        return age;
    }

    public void setAge(short age) {
        this.age = age;
    }

    public short getHealth() {
        return health;
    }

    public void setHealth(short health) {
        this.health = health;
    }

    public short getPickupDelay() {
        return pickupDelay;
    }

    public void setPickupDelay(short pickupDelay) {
        this.pickupDelay = pickupDelay;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getThrower() {
        return thrower;
    }

    public void setThrower(String thrower) {
        this.thrower = thrower;
    }

    public StoredItem getItem() {
        return item;
    }

    public void setItem(StoredItem item) {
    	this.item = item;
    }


}
