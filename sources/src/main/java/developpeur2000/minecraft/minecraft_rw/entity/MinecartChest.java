package developpeur2000.minecraft.minecraft_rw.entity;

import java.util.ArrayList;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for minecart with chest entities.
 */
@NBTCompoundType
public class MinecartChest extends Minecart {
    @NBTProperty(upperCase = true, optional = true, listItemType=StoredItem.class)
    private ArrayList<StoredItem> items;

    @NBTProperty(upperCase = true, optional = true)
    private String lootTable;

    @NBTProperty(upperCase = true, optional = true)
    private long lootTableSeed;

    /**
     * Constructs a new blank entity (to use when loading from file)
     */
    public MinecartChest() {
    	super("MinecartChest");
    }

    /**
     * copy constructor
     */
    public MinecartChest(MinecartChest src) {
    	super(src);
    	items = src.items;
    	lootTable = src.lootTable;
    	lootTableSeed = src.lootTableSeed;
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
