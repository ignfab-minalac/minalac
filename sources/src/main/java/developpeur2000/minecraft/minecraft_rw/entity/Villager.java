package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;

/**
 * Class for villager entities.
 */
@NBTCompoundType
public class Villager extends BreedableMob {

    /**
     * Constructs a new blank item (to use when loading from file)
     */
    public Villager() {
    	super("Villager");
    }

    /**
     * copy constructor
     */
    public Villager(Villager src) {
    	super(src);
    	id = "Villager";
    }
}
