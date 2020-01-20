package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for rabbit entities.
 */
@NBTCompoundType
public class Rabbit extends BreedableMob {
	static final int TYPE_BROWN = 0;
	static final int TYPE_WHITE = 1;
	static final int TYPE_BLACK = 2;
	static final int TYPE_BLACKANDWHITE = 3;
	static final int TYPE_GOLD = 4;
	static final int TYPE_SALTANDPEPPER = 5;
	static final int TYPE_KILLERBUNNY = 99;
	
    @NBTProperty(upperCase = true)
    private int rabbitType;

    @NBTProperty(upperCase = true)
    private int moreCarrotTicks;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Rabbit() {
    	super("Rabbit");
    	rabbitType = TYPE_BROWN;
    	moreCarrotTicks = 0;
    }

    /**
     * copy constructor
     */
    public Rabbit(Rabbit src) {
    	super(src);
    	id = "Rabbit";
    	rabbitType = src.rabbitType;
    	moreCarrotTicks = src.moreCarrotTicks;
    }
    
    public int getRabbitType() {
        return rabbitType;
    }

    public void setRabbitType(int rabbitType) {
        this.rabbitType = rabbitType;
    }

    public int getMoreCarrotTicks() {
        return moreCarrotTicks;
    }

    public void setMoreCarrotTicks(int moreCarrotTicks) {
        this.moreCarrotTicks = moreCarrotTicks;
    }
}
