package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.math.Vec3d;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;

/**
 * Base class for Minecraft block entities.
 * <p/>
 * Mapped according to http://minecraft.gamepedia.com/Chunk_format#Block_entity_format.
 */
@NBTCompoundType
public class BlockEntity {
    @NBTProperty()
	protected String id;

    @NBTProperty()
    private int x;
    
    @NBTProperty()
    private int y;
    
    @NBTProperty()
    private int z;
    
    /**
     * Constructs a new blank entity (to use when loading from file)
     */
    public BlockEntity() {
        id = "";
    }

    /**
     * Constructs a new entity with specified values
     */
    public BlockEntity(String id) {
    	this();
    	this.id = id;
    }

    /**
     * copy constructor
     */
    public BlockEntity(BlockEntity src) {
        id = src.id;
    	x = src.x;
    	y = src.y;
    	z = src.z;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "BlockEntity{" +
                "id='" + id + '\'' +
                ", pos=" + x + "," + y + "," + z +
                '}';
    }
}
