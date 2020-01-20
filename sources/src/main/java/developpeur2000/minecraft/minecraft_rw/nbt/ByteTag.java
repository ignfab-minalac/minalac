package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;

/**
 * Representation of the {@link NBT.Type#BYTE} tag.
 */
public class ByteTag extends NBT<Byte> {
    private static byte bool(boolean x) {
        return x ? (byte) 1 : (byte) 0;
    }

    ByteTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public ByteTag(byte value) {
        setValue(value);
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public ByteTag(boolean value) {
        this(bool(value));
    }

    @Override
    public Type getType() {
        return Type.BYTE;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readByte());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeByte(getValue());
    }

    @Override
    public Byte getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(Byte value) {
        super.setValue(value);
    }

    public boolean getBooleanValue() {
        return (getValue() != 0);
    }

    public void setBooleanValue(boolean value) {
        super.setValue(bool(value));
    }
}
