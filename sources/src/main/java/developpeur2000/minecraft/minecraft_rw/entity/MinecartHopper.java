package developpeur2000.minecraft.minecraft_rw.entity;

import java.util.ArrayList;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for minecart with hopper entities.
 */
@NBTCompoundType
public class MinecartHopper extends Minecart {
    @NBTProperty(upperCase = true, optional = true, listItemType=StoredItem.class)
    private ArrayList<StoredItem> items;

    @NBTProperty(upperCase = true)
    private int transferCooldown;

    @NBTProperty(upperCase = true)
    private boolean enabled;

    @NBTProperty(upperCase = true, optional = true)
    private String lootTable;

    @NBTProperty(upperCase = true, optional = true)
    private long lootTableSeed;

    /**
     * Constructs a new blank entity (to use when loading from file)
     */
    public MinecartHopper() {
    	super("MinecartHopper");
    	transferCooldown = 0;
    	enabled = false;
    }

    /**
     * copy constructor
     */
    public MinecartHopper(MinecartHopper src) {
    	super(src);
    	items = src.items;
    	transferCooldown = src.transferCooldown;
    	enabled = src.enabled;
    	lootTable = src.lootTable;
    	lootTableSeed = src.lootTableSeed;
    }
    
    public ArrayList<StoredItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<StoredItem> items) {
        this.items = items;
    }

    public int getTransferCooldown() {
        return transferCooldown;
    }

    public void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void getEnabled(boolean enabled) {
        this.enabled = enabled;
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
