package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for minecart with TNT entities.
 */
@NBTCompoundType
public class MinecartTNT extends Minecart {
    @NBTProperty(upperCase = true)
    private int tNTFuse;

    /**
     * Constructs a new blank entity (to use when loading from file)
     */
    public MinecartTNT() {
    	super("MinecartTNT");
    	tNTFuse = -1;
    }

    /**
     * copy constructor
     */
    public MinecartTNT(MinecartTNT src) {
    	super(src);
    	tNTFuse = src.tNTFuse;
    }
    
    public int getTNTFuse() {
        return tNTFuse;
    }

    public void setTNTFuse(int tNTFuse) {
        this.tNTFuse = tNTFuse;
    }
}
