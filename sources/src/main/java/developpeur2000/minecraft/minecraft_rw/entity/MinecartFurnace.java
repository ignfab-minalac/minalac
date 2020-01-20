package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for minecart with furnace entities.
 */
@NBTCompoundType
public class MinecartFurnace extends Minecart {
    @NBTProperty(upperCase = true)
    private double pushX;

    @NBTProperty(upperCase = true)
    private double pushZ;

    @NBTProperty(upperCase = true)
    private short fuel;

    /**
     * Constructs a new blank entity (to use when loading from file)
     */
    public MinecartFurnace() {
    	super("MinecartFurnace");
    }

    /**
     * copy constructor
     */
    public MinecartFurnace(MinecartFurnace src) {
    	super(src);
    	pushX = src.pushX;
    	pushZ = src.pushZ;
    	fuel = src.fuel;
    }
    
    public double getPushX() {
        return pushX;
    }

    public void setPushX(double pushX) {
        this.pushX = pushX;
    }

    public double getPushZ() {
        return pushZ;
    }

    public void setPushZ(double pushZ) {
        this.pushZ = pushZ;
    }

    public short getFuel() {
        return fuel;
    }

    public void setFuel(short fuel) {
        this.fuel = fuel;
    }

}
