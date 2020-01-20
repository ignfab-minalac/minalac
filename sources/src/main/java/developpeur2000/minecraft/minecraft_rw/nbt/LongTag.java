package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;

/**
 * Representation of the {@link NBT.Type#LONG} tag.
 */
public class LongTag extends NBT<Long> {
    LongTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public LongTag(long value) {
        setValue(value);
    }

    @Override
    public Type getType() {
        return Type.LONG;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readLong());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeLong(getValue());
    }

    @Override
    public Long getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(Long value) {
        super.setValue(value);
    }
}
