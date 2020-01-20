package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.DataOutput;
import java.io.IOException;


/**
 * Extension of the {@link java.io.DataOutput} interface for NBT processing.
 */
interface NBTDataOutput extends DataOutput {
    /**
     * Writes a tag ID to the stream.
     *
     * @param id the ID to write.
     * @throws java.io.IOException
     */
    public void writeTagId(NBT.Type id) throws IOException;

    /**
     * Writes an NBT-formatted string to the stream.
     *
     * @param str the string to write.
     * @throws java.io.IOException
     */
    public void writeNBTString(String str) throws IOException;

    /**
     * Writes an NBT-formatted byte array to the stream.
     *
     * @param arr the array to write.
     * @throws java.io.IOException
     */
    public void writeNBTByteArray(byte[] arr) throws IOException;

    /**
     * Writes an NBT-formatted integer array to the stream.
     *
     * @param arr the array to write.
     * @throws java.io.IOException
     */
    public void writeNBTIntArray(int[] arr) throws IOException;
}
