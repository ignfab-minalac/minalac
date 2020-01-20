package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

/**
 * Base class for air portals block entities.
 */
@NBTCompoundType
public class AirPortal extends BlockEntity {

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public AirPortal() {
    	super("AirPortal");
    }

    /**
     * copy constructor
     */
    public AirPortal(AirPortal src) {
    	super(src);
    	id = "AirPortal";
    }
    
}
