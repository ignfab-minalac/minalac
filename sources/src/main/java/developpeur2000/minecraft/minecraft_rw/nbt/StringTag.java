package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;

/**
 * Representation of the {@link NBT.Type#STRING} tag.
 */
public class StringTag extends NBT<String> {
    StringTag() {
    }

    /**
     * Constructs a new tag with the specified parameters.
     *
     * @param value the tag's value.
     */
    public StringTag(String value) {
        setValue(value);
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        setValue(input.readNBTString());
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        output.writeNBTString(getValue());
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    String toString(int level) {
        return "\"" + getValue() + "\":" + getType().toString();
    }
}
