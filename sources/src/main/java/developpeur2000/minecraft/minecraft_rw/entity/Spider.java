package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

/**
 * Class for spider entities.
 */
@NBTCompoundType
public class Spider extends Mob {
    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Spider() {
    	super("Spider");
    }

    /**
     * copy constructor
     */
    public Spider(Spider src) {
    	super(src);
    	id = "Spider";
    }

}
