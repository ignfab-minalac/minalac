package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

/**
 * Class for witch entities.
 */
@NBTCompoundType
public class Witch extends Mob {

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Witch() {
    	super("Witch");
    }

    /**
     * copy constructor
     */
    public Witch(Witch src) {
    	super(src);
    	id = "Witch";
    }
}
