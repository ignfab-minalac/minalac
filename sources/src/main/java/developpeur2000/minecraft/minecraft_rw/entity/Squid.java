package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

/**
 * Class for squid entities.
 */
@NBTCompoundType
public class Squid extends Mob {
    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Squid() {
    	super("Squid");
    }

    /**
     * copy constructor
     */
    public Squid(Squid src) {
    	super(src);
    	id = "Squid";
    }

}
