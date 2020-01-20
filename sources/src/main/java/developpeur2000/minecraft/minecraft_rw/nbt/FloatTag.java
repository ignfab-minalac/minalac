package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;

/**
 * Representation of the {@link NBT.Type#FLOAT} tag.
 */
public class FloatTag extends NBT<Float> {
    FloatTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public FloatTag(float value) {
        setValue(value);
    }

    @Override
    public Type getType() {
        return Type.FLOAT;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readFloat());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeFloat(getValue());
    }

    @Override
    public Float getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(Float value) {
        super.setValue(value);
    }
}
