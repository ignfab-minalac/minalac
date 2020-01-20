package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;

/**
 * Representation of the {@link NBT.Type#DOUBLE} tag.
 */
public class DoubleTag extends NBT<Double> {
    DoubleTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public DoubleTag(double value) {
        setValue(value);
    }

    @Override
    public Type getType() {
        return Type.DOUBLE;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readDouble());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeDouble(getValue());
    }

    @Override
    public Double getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(Double value) {
        super.setValue(value);
    }
}
