package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;
import java.util.Arrays;

/**
 * Representation of the {@link NBT.Type#INT_ARRAY} tag.
 */
public class IntArrayTag extends NBT<int[]> {
    IntArrayTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public IntArrayTag(int[] value) {
        setValue(value);
    }

    @Override
    public Type getType() {
        return Type.INT_ARRAY;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readNBTIntArray());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeNBTIntArray(getValue());
    }

    @Override
    public int[] getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(int[] value) {
        super.setValue(value);
    }

    @Override
    String toString(int level) {
        return Arrays.toString(getValue()) + ":" + getType().toString();
    }
}
