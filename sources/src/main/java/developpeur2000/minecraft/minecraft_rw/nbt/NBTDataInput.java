package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.DataInput;
import java.io.IOException;

/**
 * Extension of the {@link java.io.DataInput} interface for NBT processing.
 */
interface NBTDataInput extends DataInput {
    /**
     * Reads a tag ID from the stream.
     *
     * @return the read tag ID.
     * @throws java.io.IOException
     */
    public NBT.Type readTagId() throws IOException;

    /**
     * Reads a NBT-formatted string from the stream.
     *
     * @return the read string.
     * @throws java.io.IOException
     */
    public String readNBTString() throws IOException;

    /**
     * Reads an NBT-formatted byte array from the stream.
     *
     * @return the read byte array.
     * @throws java.io.IOException
     */
    public byte[] readNBTByteArray() throws IOException;

    /**
     * Reads an NBT-formatted integer array from the stream.
     *
     * @return the read integer array.
     * @throws java.io.IOException
     */
    public int[] readNBTIntArray() throws IOException;
}
