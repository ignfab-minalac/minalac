package developpeur2000.minecraft.minecraft_rw.entity;

import java.util.ArrayList;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for chest block entities.
 */
@NBTCompoundType
public class Chest extends BlockEntity {

    @NBTProperty(upperCase = true, optional = true)
    private String customName;

    @NBTProperty(upperCase = true, optional = true)
    private String lock;

    @NBTProperty(upperCase = true, optional = true, listItemType=StoredItem.class)
    private ArrayList<StoredItem> items;

    @NBTProperty(upperCase = true, optional = true)
    private String lootTable;

    @NBTProperty(upperCase = true, optional = true)
    private long lootTableSeed;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Chest() {
    	super("Chest");
    }

    /**
     * copy constructor
     */
    public Chest(Chest src) {
    	super(src);
    	id = "Chest";
    	customName = src.customName;
    	lock = src.lock;
    	items = src.items;
    	lootTable = src.lootTable;
    	lootTableSeed = src.lootTableSeed;
    }
    
    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getLock() {
        return lock;
    }

    public void setLock(String lock) {
        this.lock = lock;
    }

    public ArrayList<StoredItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<StoredItem> items) {
        this.items = items;
    }

    public String getLootTable() {
        return lootTable;
    }

    public void setLootTable(String lootTable) {
        this.lootTable = lootTable;
    }

    public long getLootTableSeed() {
        return lootTableSeed;
    }

    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }
}
