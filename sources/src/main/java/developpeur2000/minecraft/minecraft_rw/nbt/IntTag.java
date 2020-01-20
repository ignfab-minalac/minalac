package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;

/**
 * Representation of the {@link NBT.Type#INT} tag.
 */
public class IntTag extends NBT<Integer> {
    IntTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public IntTag(int value) {
        setValue(value);
    }

    @Override
    public Type getType() {
        return Type.INT;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readInt());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeInt(getValue());
    }

    @Override
    public Integer getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(value);
    }
}
