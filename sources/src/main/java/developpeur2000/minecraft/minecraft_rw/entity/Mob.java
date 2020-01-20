package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

import java.util.ArrayList;

/**
 * Base class for mob entities.
 */
@NBTCompoundType
public class Mob extends Entity {
	//healF and health are not optional but if we deal with map files
	// that are anterior to minecraft version 1.9, value might be stored as healF
	// Pre-1.9 value stored as "health" is an int and is not supported
	//value will be stored as float in health in all cases
	
    @NBTProperty(optional = true, upperCase = true)
    private float healF;

    @NBTProperty(optional = true, upperCase = true)
    private float health;

    @NBTProperty(upperCase = true)
    private float absorptionAmount;

    @NBTProperty(upperCase = true, optional = true)
    private short attackTime;

    @NBTProperty(upperCase = true)
    private short hurtTime;

    @NBTProperty(upperCase = true)
    private int hurtByTimestamp;

    @NBTProperty(upperCase = true)
    private short deathTime;

    //TODO: Map Attribute class
    @NBTProperty(upperCase = true, listItemType = CompoundTag.class)
    private ArrayList<CompoundTag> attributes;

    //TODO: Map Effect class
    @NBTProperty(upperCase = true, optional = true, listItemType = CompoundTag.class)
    private ArrayList<CompoundTag> activeEffects;

    //TODO: Map Item class
    @NBTProperty(upperCase = true, optional = true, listItemType = CompoundTag.class)
    private ArrayList<CompoundTag> equipment;

    @NBTProperty(upperCase = true, optional = true, listItemType = Float.class)
    private ArrayList<Float> dropChances;

    @NBTProperty(upperCase = true, optional = true)
    private boolean canPickUpLoot;

    @NBTProperty(upperCase = true, optional = true)
    private boolean noAI;

    @NBTProperty(upperCase = true, optional = true)
    private boolean persistenceRequired;

    @NBTProperty(upperCase = true, optional = true)
    private boolean leashed;

    //TODO: Map Leash class
    @NBTProperty(upperCase = true, optional = true)
    private CompoundTag leash;
    
    /**
     * Constructs a new blank mob (to use when loading from file)
     */
    public Mob(String mobId) {
    	super(mobId);
    	health = 10;
    	absorptionAmount = 0;
    	hurtTime = 0;
        hurtByTimestamp = 0;
        deathTime = 0;
        attributes = new ArrayList<CompoundTag>();
        canPickUpLoot = false;
        persistenceRequired = true;
        leashed = false;
        attributes = new ArrayList<CompoundTag>();
    }

    /**
     * copy constructor
     */
    public Mob(Mob src) {
    	super(src);

    	health = src.health;
    	absorptionAmount = src.absorptionAmount;
    	hurtTime = src.hurtTime;
        hurtByTimestamp = src.hurtByTimestamp;
        deathTime = src.deathTime;
        attributes = src.attributes;
        canPickUpLoot = src.canPickUpLoot;
        persistenceRequired = src.persistenceRequired;
        leashed = src.leashed;
    }

    

    public float getHealF() {
        return health;
    }

    public void setHealF(float healF) {
        this.health = healF;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getAbsorptionAmount() {
        return absorptionAmount;
    }

    public void setAbsorptionAmount(float absorptionAmount) {
        this.absorptionAmount = absorptionAmount;
    }

    public short getAttackTime() {
        return attackTime;
    }

    public void setAttackTime(short attackTime) {
        this.attackTime = attackTime;
    }

    public short getHurtTime() {
        return hurtTime;
    }

    public void setHurtTime(short hurtTime) {
        this.hurtTime = hurtTime;
    }

    public int getHurtByTimestamp() {
        return hurtByTimestamp;
    }

    public void setHurtByTimestamp(int hurtByTimestamp) {
        this.hurtByTimestamp = hurtByTimestamp;
    }

    public short getDeathTime() {
        return deathTime;
    }

    public void setDeathTime(short deathTime) {
        this.deathTime = deathTime;
    }

    public ArrayList<CompoundTag> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<CompoundTag> attributes) {
        this.attributes = attributes;
    }

    public ArrayList<CompoundTag> getActiveEffects() {
        return activeEffects;
    }

    public void setActiveEffects(ArrayList<CompoundTag> activeEffects) {
        this.activeEffects = activeEffects;
    }

    public ArrayList<CompoundTag> getEquipment() {
        return equipment;
    }

    public void setEquipment(ArrayList<CompoundTag> equipment) {
        this.equipment = equipment;
    }

    public ArrayList<Float> getDropChances() {
        return dropChances;
    }

    public void setDropChances(ArrayList<Float> dropChances) {
        this.dropChances = dropChances;
    }

    public boolean isCanPickUpLoot() {
        return canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
    }

    public boolean isNoAI() {
        return noAI;
    }

    public void setNoAI(boolean noAI) {
        this.noAI = noAI;
    }

    public boolean isPersistenceRequired() {
        return persistenceRequired;
    }

    public void setPersistenceRequired(boolean persistenceRequired) {
        this.persistenceRequired = persistenceRequired;
    }

    public boolean isLeashed() {
        return leashed;
    }

    public void setLeashed(boolean leashed) {
        this.leashed = leashed;
    }

    public CompoundTag getLeash() {
        return leash;
    }

    public void setLeash(CompoundTag leash) {
        this.leash = leash;
    }
}
