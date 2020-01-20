package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

/**
 * class for cow entities.
 */
@NBTCompoundType
public class Cow extends BreedableMob {

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Cow() {
    	super("Cow");
    }

    /**
     * copy constructor
     */
    public Cow(Cow src) {
    	super(src);
    	id = "Cow";
    }
}
