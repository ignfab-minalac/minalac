package developpeur2000.minecraft.minecraft_rw.nbt;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class NBTDataOutputStream extends DataOutputStream implements NBTDataOutput {
    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream. The counter <code>written</code> is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     * @see java.io.FilterOutputStream#out
     */
    public NBTDataOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void writeTagId(NBT.Type id) throws IOException {
        writeByte(id.ordinal());
    }

    @Override
    public void writeNBTString(String str) throws IOException {
    	byte[] bytes = str.getBytes(NBT.ENCODING);
        writeShort(bytes.length);
        write(bytes);
    }

    @Override
    public void writeNBTByteArray(byte[] arr) throws IOException {
        writeInt(arr.length);
        write(arr);
    }

    @Override
    public void writeNBTIntArray(int[] arr) throws IOException {
        writeInt(arr.length);
        for (int x : arr) {
            writeInt(x);
        }
    }
}
