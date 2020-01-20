package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;
import java.util.Arrays;

/**
 * Representation of the {@link NBT.Type#BYTE_ARRAY} tag.
 */
public class ByteArrayTag extends NBT<byte[]> {
    ByteArrayTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public ByteArrayTag(byte[] value) {
        setValue(value);
    }

    @Override
    public Type getType() {
        return Type.BYTE_ARRAY;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readNBTByteArray());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeNBTByteArray(getValue());
    }

    @Override
    public byte[] getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(byte[] value) {
        super.setValue(value);
    }

    @Override
    String toString(int level) {
        return Arrays.toString(getValue()) + ":" + getType().toString();
    }
}
