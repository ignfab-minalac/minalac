package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for minecart entities.
 */
@NBTCompoundType
public class Minecart extends Entity {
    @NBTProperty(upperCase = true, optional = true)
    private boolean customDisplayTile;

    @NBTProperty(upperCase = true, optional = true)
    private String displayTile;

    @NBTProperty(upperCase = true, optional = true)
    private int displayData;

    @NBTProperty(upperCase = true, optional = true)
    private int displayOffset;


    /**
     * Constructs a new blank minecart (to use when loading from file)
     */
    public Minecart() {
    	this("Minecart");
    }

    /**
     * Constructs a new blank specific minecart (to use when loading from file from a superclass)
     */
    public Minecart(String id) {
    	super(id);
    }

    /**
     * copy constructor
     */
    public Minecart(Minecart src) {
    	super(src);
    	customDisplayTile = src.customDisplayTile;
    	displayTile = src.displayTile;
    	displayData = src.displayData;
    	displayOffset = src.displayOffset;
    }
    
    public boolean getCustomDisplayTile() {
        return customDisplayTile;
    }

    public void setCustomDisplayTile(boolean customDisplayTile) {
        this.customDisplayTile = customDisplayTile;
    }

    public String getDisplayTile() {
        return displayTile;
    }

    public void setDisplayTile(String displayTile) {
        this.displayTile = displayTile;
    }

    public int getDisplayData() {
        return displayData;
    }

    public void setDisplayData(int displayData) {
        this.displayData = displayData;
    }

    public int getDisplayOffset() {
        return displayOffset;
    }

    public void setDisplayOffset(int displayOffset) {
        this.displayOffset = displayOffset;
    }
}
