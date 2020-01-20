package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Class for creeper entities.
 */
@NBTCompoundType
public class Creeper extends Mob {
    @NBTProperty(optional = true)
    private boolean powered;

    @NBTProperty(upperCase = true)
    private byte explosionRadius;

    @NBTProperty(upperCase = true)
    private short fuse;

    @NBTProperty()
    private boolean ignited;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Creeper() {
    	super("Creeper");
    	powered = false;
    	explosionRadius = 3;
    	fuse = 30;
    	ignited = false;
    }

    /**
     * copy constructor
     */
    public Creeper(Creeper src) {
    	super(src);
    	id = "Creeper";
    	powered = src.powered;
    	explosionRadius = src.explosionRadius;
    	fuse = src.fuse;
    	ignited = src.ignited;
    }
    
    public boolean getPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public byte getExplosionRadius() {
        return explosionRadius;
    }

    public void setExplosionRadius(byte explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    public short getFuse() {
        return fuse;
    }

    public void setFuse(short fuse) {
        this.fuse = fuse;
    }

    public boolean getIgnited() {
        return ignited;
    }

    public void setIgnited(boolean ignited) {
        this.ignited = ignited;
    }

}
