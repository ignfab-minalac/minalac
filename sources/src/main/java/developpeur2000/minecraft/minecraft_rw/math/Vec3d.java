package developpeur2000.minecraft.minecraft_rw.math;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTListItem;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTListType;

/**
 * Represents a three-dimensional vector, defined by three double coordinates.
 */
@NBTListType
public class Vec3d {
    @NBTListItem(0)
    private double x;

    @NBTListItem(1)
    private double y;

    @NBTListItem(2)
    private double z;

    /**
     * Constructs a new vector.
     */
    public Vec3d() {
    }

    /**
     * Constructs a new vector with the specified coordinates.
     *
     * @param x the X coordinate.
     * @param y the Y coordinate.
     * @param z the Z coordinate.
     */
    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Vec3d{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
