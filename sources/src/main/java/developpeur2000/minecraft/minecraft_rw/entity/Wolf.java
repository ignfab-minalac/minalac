package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for wolf entities.
 */
@NBTCompoundType
public class Wolf extends BreedableMob {
    @NBTProperty(upperCase = true, optional = true)
    private String owner;

    @NBTProperty(upperCase = true, optional = true)
    private String ownerUUID;

    @NBTProperty(upperCase = true)
    private boolean sitting;

    @NBTProperty(upperCase = true)
    private boolean angry;

    @NBTProperty(upperCase = true)
    private byte collarColor;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Wolf() {
    	super("Wolf");
    	owner = "";
    	ownerUUID = "";
    	sitting = false;
    	angry = false;
    	collarColor = 14;
    }

    /**
     * copy constructor
     */
    public Wolf(Wolf src) {
    	super(src);
    	id = "Wolf";
    	owner = src.owner;
    	ownerUUID = src.ownerUUID;
    	sitting = src.sitting;
    	angry = src.angry;
    	collarColor = src.collarColor;
    }
    
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(String ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public boolean getSitting() {
        return sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public boolean getAngry() {
        return angry;
    }

    public void setAngry(boolean angry) {
        this.angry = angry;
    }

    public byte getCollarColor() {
        return collarColor;
    }

    public void setCollarColor(byte collarColor) {
        this.collarColor = collarColor;
    }

}
