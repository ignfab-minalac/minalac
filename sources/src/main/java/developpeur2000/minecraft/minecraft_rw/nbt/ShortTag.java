package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;

/**
 * Representation of the {@link NBT.Type#SHORT} tag.
 */
public class ShortTag extends NBT<Short> {
    ShortTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public ShortTag(short value) {
        setValue(value);
    }

    @Override
    public Type getType() {
        return Type.SHORT;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readShort());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeShort(getValue());
    }

    @Override
    public void setValue(Short value) {
        super.setValue(value);
    }

    @Override
    public Short getValue() {
        return super.getValue();
    }
}
