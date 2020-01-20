package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Class for zobie entities.
 */
@NBTCompoundType
public class Zombie extends Mob {
    @NBTProperty(upperCase = true, optional = true)
    private boolean isVillager;

    @NBTProperty(upperCase = true, optional = true)
    private boolean isBaby;

    @NBTProperty(upperCase = true)
    private int conversionTime;

    @NBTProperty(upperCase = true)
    private boolean canBreakDoors;

    @NBTProperty(upperCase = true, optional = true)
    private int villagerProfession;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Zombie() {
    	super("Zombie");
    	isVillager = false;
    	isBaby = false;
    	conversionTime = -1;
    	canBreakDoors = false;
    }

    /**
     * copy constructor
     */
    public Zombie(Zombie src) {
    	super(src);
    	id = "Zombie";
    	isVillager = src.isVillager;
    	isBaby = src.isBaby;
    	conversionTime = src.conversionTime;
    	canBreakDoors = src.canBreakDoors;
    	villagerProfession = src.villagerProfession;
    }
    
    public boolean getIsVillager() {
        return isVillager;
    }

    public void setIsVillager(boolean isVillager) {
        this.isVillager = isVillager;
    }

    public boolean getIsBaby() {
        return isBaby;
    }

    public void setIsBaby(boolean isBaby) {
        this.isBaby = isBaby;
    }

    public int getConversionTime() {
        return conversionTime;
    }

    public void setConversionTime(int conversionTime) {
        this.conversionTime = conversionTime;
    }

    public boolean getCanBreakDoors() {
        return canBreakDoors;
    }

    public void setCanBreakDoors(boolean canBreakDoors) {
        this.canBreakDoors = canBreakDoors;
    }

    public int getVillagerProfession() {
        return villagerProfession;
    }

    public void setVillagerProfession(int villagerProfession) {
        this.villagerProfession = villagerProfession;
    }

}
