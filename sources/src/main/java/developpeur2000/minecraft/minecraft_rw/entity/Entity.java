package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.math.Rot2f;
import developpeur2000.minecraft.minecraft_rw.math.Vec3d;
import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

import java.util.UUID;

/**
 * Base class for Minecraft entities.
 * <p/>
 * Mapped according to http://minecraft.gamepedia.com/Chunk_format#Entity_Format.
 */
@NBTCompoundType
public class Entity {
    @NBTProperty(optional = true)
	protected String id;

    @NBTProperty(upperCase = true)
    private Vec3d pos;

    @NBTProperty(upperCase = true)
    private Vec3d motion;

    @NBTProperty(upperCase = true)
    private Rot2f rotation;

    @NBTProperty(upperCase = true)
    private float fallDistance;

    @NBTProperty(upperCase = true)
    private short fire;

    @NBTProperty(upperCase = true)
    private short air;

    @NBTProperty(upperCase = true)
    private boolean onGround;

    @NBTProperty(upperCase = true)
    private int dimension;

    @NBTProperty(upperCase = true)
    private boolean invulnerable;

    @NBTProperty(upperCase = true)
    private int portalCooldown;

    @NBTProperty(value = {"UUIDLeast", "UUIDMost"}, translator = UUIDTranslator.class)
    private UUID uuid;

    @NBTProperty(upperCase = true, optional = true)
    private String customName;

    @NBTProperty(upperCase = true, optional = true)
    private boolean customNameVisible;

    @NBTProperty(upperCase = true, optional = true)
    private boolean silent;

    //TODO: Map to Entity (requires a factory with awareness of possible entity IDs)
    @NBTProperty(upperCase = true, optional = true)
    private CompoundTag riding;

    @NBTProperty(upperCase = true, optional = true)
    private CommandStats commandStats;
    
    /**
     * Constructs a new blank entity (to use when loading from file)
     */
    public Entity() {
        id = "";
        pos = new Vec3d();
        motion = new Vec3d();
        rotation = new Rot2f();
        fallDistance = 0;
        fire = -1;
        air = 300;
        onGround = true;
        invulnerable = false;
        portalCooldown = 0;
        uuid = UUID.randomUUID();
    }

    /**
     * Constructs a new entity with specified values
     */
    public Entity(String id) {
    	this();
    	this.id = id;
    }

    /**
     * copy constructor
     */
    public Entity(Entity src) {
        id = src.id;
        pos = src.pos;
        motion = src.motion;
        rotation = src.rotation;
        fallDistance = src.fallDistance;
        fire = src.fire;
        air = src.air;
        onGround = src.onGround;
        invulnerable = src.invulnerable;
        portalCooldown = src.portalCooldown;
        uuid = src.uuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vec3d getPos() {
        return pos;
    }

    public void setPos(Vec3d pos) {
        this.pos = pos;
    }

    public Vec3d getMotion() {
        return motion;
    }

    public void setMotion(Vec3d motion) {
        this.motion = motion;
    }

    public Rot2f getRotation() {
        return rotation;
    }

    public void setRotation(Rot2f rotation) {
        this.rotation = rotation;
    }

    public float getFallDistance() {
        return fallDistance;
    }

    public void setFallDistance(float fallDistance) {
        this.fallDistance = fallDistance;
    }

    public short getFire() {
        return fire;
    }

    public void setFire(short fire) {
        this.fire = fire;
    }

    public short getAir() {
        return air;
    }

    public void setAir(short air) {
        this.air = air;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public int getPortalCooldown() {
        return portalCooldown;
    }

    public void setPortalCooldown(int portalCooldown) {
        this.portalCooldown = portalCooldown;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    public void setCustomNameVisible(boolean customNameVisible) {
        this.customNameVisible = customNameVisible;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public CompoundTag getRiding() {
        return riding;
    }

    public void setRiding(CompoundTag riding) {
        this.riding = riding;
    }

    public CommandStats getCommandStats() {
        return commandStats;
    }

    public void setCommandStats(CommandStats commandStats) {
        this.commandStats = commandStats;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id='" + id + '\'' +
                ", uuid=" + uuid +
                '}';
    }
}
