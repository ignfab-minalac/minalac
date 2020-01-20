package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extension of the {@link java.io.DataInputStream} for NBT processing.
 */
class NBTDataInputStream extends DataInputStream implements NBTDataInput {
    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public NBTDataInputStream(InputStream in) {
        super(in);
    }

    @Override
    public NBT.Type readTagId() throws IOException {
        final int id = readByte();
        try {
            return NBT.Type.values()[id];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new NBTException("Unknown tag type: " + id);
        }
    }

    @Override
    public String readNBTString() throws IOException {
        final int length = readShort();

        if (length > 0) {
            final byte[] b = new byte[length];
            for (int i = 0; i < length; i++) {
                b[i] = readByte();
            }

            return new String(b, NBT.ENCODING);
        } else {
            return "";
        }
    }

    @Override
    public byte[] readNBTByteArray() throws IOException {
        final int length = readInt();
        final byte[] array = new byte[length];

        if (length > 0) {
            for (int i = 0; i < length; i++) {
                array[i] = readByte();
            }
        }

        return array;
    }

    @Override
    public int[] readNBTIntArray() throws IOException {
        final int length = readInt();
        final int[] array = new int[length];

        for (int i = 0; i < length; i++) {
            array[i] = readInt();
        }

        return array;
    }
}
