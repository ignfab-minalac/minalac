package developpeur2000.minecraft.minecraft_rw.entity;

import java.util.ArrayList;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for horse entities.
 */
@NBTCompoundType
public class EntityHorse extends BreedableMob {
	static final int TYPE_HORSE = 0;
	static final int TYPE_DONKEY = 1;
	static final int TYPE_MULE = 2;
	static final int TYPE_ZOMBIE = 3;
	static final int TYPE_SKELETON = 4;
	
	static final int COLOR_WHITE = 0;
	static final int COLOR_CREAMY = 1;
	static final int COLOR_CHESTNUT = 2;
	static final int COLOR_BROWN = 3;
	static final int COLOR_BLACK = 4;
	static final int COLOR_GRAY = 5;
	static final int COLOR_DARKBROWN = 6;
	
	static final int MARKINGS_NONE = 0;
	static final int MARKINGS_WHITE = 1;
	static final int MARKINGS_WHITEFIELD = 2;
	static final int MARKINGS_WHITEDOTS = 3;
	static final int MARKINGS_BLACKDOTS = 4;
	
    @NBTProperty(upperCase = true)
    private boolean bred;

    @NBTProperty(upperCase = true)
    private boolean chestedHorse;

    @NBTProperty(upperCase = true)
    private boolean eatingHaystack;

    @NBTProperty(upperCase = true)
    private boolean hasReproduced;

    @NBTProperty(upperCase = true)
    private boolean tame;

    @NBTProperty(upperCase = true)
    private int temper;

    @NBTProperty(upperCase = true)
    private int type;

    @NBTProperty(upperCase = true)
    private int variant;

    @NBTProperty(upperCase = true, optional = true)
    private String owner;

    @NBTProperty(upperCase = true, optional = true)
    private String ownerUUID;

    @NBTProperty(upperCase = true, optional = true, listItemType=StoredItem.class)
    private ArrayList<StoredItem> items;

    @NBTProperty(upperCase = true, optional = true)
    private StoredItem armorItem;

    @NBTProperty(upperCase = true, optional = true)
    private StoredItem saddleItem;

    @NBTProperty(upperCase = true, optional = true)
    private boolean saddle;

    @NBTProperty(upperCase = true, optional = true)
    private boolean skeletonTrap;

    @NBTProperty(upperCase = true, optional = true)
    private int skeletonTrapTime;

    
    private int variant(int color, int markings) {
    	return color | (markings << 8);
    }

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public EntityHorse() {
    	super("EntityHorse");
        bred = false;
        chestedHorse = false;
        eatingHaystack = false;
        hasReproduced = false;
        tame = false;
        temper = 0;
        type = TYPE_HORSE;
        variant = variant(COLOR_WHITE,MARKINGS_NONE);
        saddle = false;
        skeletonTrap = false;
        skeletonTrapTime = 0;
	}

    /**
     * copy constructor
     */
    public EntityHorse(EntityHorse src) {
    	super(src);
    	id = "EntityHorse";
        bred = src.bred;
        chestedHorse = src.chestedHorse;
        eatingHaystack = src.eatingHaystack;
        hasReproduced = src.hasReproduced;
        tame = src.tame;
        temper = src.temper;
        type = src.type;
        variant = src.variant;
        owner = src.owner;
        ownerUUID = src.ownerUUID;
        items = src.items;
        armorItem = src.armorItem;
        saddleItem = src.saddleItem;
        saddle = src.saddle;
        skeletonTrap = src.skeletonTrap;
        skeletonTrapTime = src.skeletonTrapTime;
	}
    
    public boolean getBred() {
        return bred;
    }

    public void setBred(boolean bred) {
        this.bred = bred;
    }

    public boolean getChestedHorse() {
        return chestedHorse;
    }
    public void setChestedHorse(boolean chestedHorse) {
        this.chestedHorse = chestedHorse;
    }
    
    public boolean getEatingHaystack() {
        return eatingHaystack;
    }
    public void setEatingHaystack(boolean eatingHaystack) {
        this.eatingHaystack = eatingHaystack;
    }

    public boolean getHasReproduced() {
        return hasReproduced;
    }
    public void setHasReproduced(boolean hasReproduced) {
        this.hasReproduced = hasReproduced;
    }

    public boolean getTame() {
        return tame;
    }
    public void setTame(boolean tame) {
        this.tame = tame;
    }

    public int getTemper() {
        return temper;
    }
    public void setTemper(int temper) {
        this.temper = temper;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public int getVariant() {
        return variant;
    }
    public void setVariant(int variant) {
        this.variant = variant;
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

    public ArrayList<StoredItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<StoredItem> items) {
        this.items = items;
    }

    public StoredItem getArmorItem() {
        return armorItem;
    }
    public void SetArmorItem(StoredItem armorItem) {
        this.armorItem = armorItem;
    }

    public StoredItem getSaddleItem() {
        return saddleItem;
    }
    public void setSaddleItem(StoredItem saddleItem) {
        this.saddleItem = saddleItem;
    }

    public boolean getSaddle() {
        return saddle;
    }
    public void setSaddle(boolean saddle) {
        this.saddle = saddle;
    }

    public boolean getSkeletonTrap() {
        return skeletonTrap;
    }
    public void setSkeletonTrap(boolean skeletonTrap) {
        this.skeletonTrap = skeletonTrap;
    }

    public int getSkeletonTrapTime() {
        return skeletonTrapTime;
    }
    public void setSkeletonTrapTime(int skeletonTrapTime) {
        this.skeletonTrapTime = skeletonTrapTime;
    }

}
