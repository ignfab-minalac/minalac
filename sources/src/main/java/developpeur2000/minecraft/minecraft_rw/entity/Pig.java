package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for pig entities.
 */
@NBTCompoundType
public class Pig extends BreedableMob {
    @NBTProperty(upperCase = true)
    private boolean saddle;


    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Pig() {
    	super("Pig");
    	saddle = false;
    }

    /**
     * copy constructor
     */
    public Pig(Pig src) {
    	super(src);
    	id = "Pig";
    	saddle = src.saddle;
    }
    
    public boolean getSaddle() {
        return saddle;
    }

    public void setSaddle(boolean saddle) {
        this.saddle = saddle;
    }
}
