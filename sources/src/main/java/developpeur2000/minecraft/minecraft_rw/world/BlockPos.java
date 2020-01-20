package developpeur2000.minecraft.minecraft_rw.world;

import developpeur2000.minecraft.minecraft_rw.nbt.IntTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTTranslator;

/**
 * Represents a block position in the world, consisting of three integer coordinates.
 */
public class BlockPos {
    /**
     * NBT translator for block positions.
     */
    public static class Translator implements NBTTranslator<BlockPos> {
        @Override
        public BlockPos translateFromNBT(NBT[] nbt) {
            return new BlockPos(
                    ((IntTag) nbt[0]).getValue(),
                    ((IntTag) nbt[1]).getValue(),
                    ((IntTag) nbt[2]).getValue());
        }

        @Override
        public NBT[] translateToNBT(BlockPos x) {
            return new NBT[]{
                    new IntTag(x.getX()),
                    new IntTag(x.getY()),
                    new IntTag(x.getZ())};
        }
    }

    private int x;
    private int y;
    private int z;

    /**
     * Constructs a new block position.
     */
    public BlockPos() {
    }

    /**
     * Constructs a new block position with the specified coordinates.
     * @param x the X coordinate.
     * @param y the Y coordinate.
     * @param z the Z coordinate.
     */
    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
        return "BlockPos{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
