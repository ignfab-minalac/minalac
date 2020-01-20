package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for breedable mob entities.
 */
@NBTCompoundType
public abstract class BreedableMob extends Mob {
    @NBTProperty(upperCase = true, optional = true)
    private int inLove;

    @NBTProperty(upperCase = true)
    private int age;

    @NBTProperty(upperCase = true)
    private int forcedAge;

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public BreedableMob(String mobId) {
    	super(mobId);
    	inLove = 0;
    	age = 0;
    	forcedAge = 0;
    }

    /**
     * copy constructor
     */
    public BreedableMob(BreedableMob src) {
    	super(src);
    	this.inLove = src.inLove;
    	this.age = src.age;
    	this.forcedAge = src.forcedAge;
    }
    
    public int getInLove() {
        return inLove;
    }

    public void setInLove(int inLove) {
        this.inLove = inLove;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getForcedAge() {
        return forcedAge;
    }

    public void setForcedAge(int forcedAge) {
        this.forcedAge = forcedAge;
    }

}
