package developpeur2000.minecraft.minecraft_rw.math;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTListItem;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTListType;

/**
 * Represents a view rotator, defined by yaw and pitch.
 */
@NBTListType
public class Rot2f {
    @NBTListItem(0)
    private float yaw;

    @NBTListItem(1)
    private float pitch;

    /**
     * Constructs a new rotator.
     */
    public Rot2f() {
    }

    /**
     * Constructs a new rotator with the specified parameters.
     *
     * @param yaw   the yaw.
     * @param pitch the pitch.
     */
    public Rot2f(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public String toString() {
        return "Rot2f{" +
                "yaw=" + yaw +
                ", pitch=" + pitch +
                '}';
    }
}
