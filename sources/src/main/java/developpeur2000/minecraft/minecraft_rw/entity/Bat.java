package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for bat entities.
 */
@NBTCompoundType
public class Bat extends Mob {
    @NBTProperty(upperCase = true)
    private boolean batFlags;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Bat() {
    	super("Bat");
    	batFlags = false;
    }

    /**
     * copy constructor
     */
    public Bat(Bat src) {
    	super(src);
    	id = "Bat";
    	batFlags = src.batFlags;
    }
    
    public boolean getBatFlags() {
        return batFlags;
    }

    public void setBatFlags(boolean batFlags) {
        this.batFlags = batFlags;
    }
}
