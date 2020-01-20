package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Class for enderman entities.
 */
@NBTCompoundType
public class Enderman extends Mob {
    @NBTProperty(optional = true)
    private short carried;

    @NBTProperty(optional = true)
    private short carriedData;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Enderman() {
    	super("Enderman");
    	carried = 0;
    	carriedData = 0;
    }

    /**
     * copy constructor
     */
    public Enderman(Enderman src) {
    	super(src);
    	id = "Enderman";
    	carried = src.carried;
    	carriedData = src.carriedData;
    }
    
    public short getCarried() {
        return carried;
    }

    public void setCarried(short carried) {
        this.carried = carried;
    }

    public short getCarriedData() {
        return carriedData;
    }

    public void setCarriedData(short carriedData) {
        this.carriedData = carriedData;
    }

}
