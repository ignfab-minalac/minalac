package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;
import java.util.*;

/**
 * Representation of the {@link NBT.Type#COMPOUND} tag.
 */
public class CompoundTag extends NBT<Map<String, NBT>> implements Iterable<NBT> {
    public CompoundTag() {
        setValue(new LinkedHashMap<String, NBT>());
    }

    @Override
    public Type getType() {
        return Type.COMPOUND;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        while (true) {
            final Type next = input.readTagId();
            if (next == Type.END) {
                break;
            } else {
                final NBT nbt = next.createInstance();
                nbt.setName(input.readNBTString());
                nbt.readValue(input);
                getValue().put(nbt.getName(), nbt);
            }
        }
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        for (Map.Entry<String, NBT> e : getValue().entrySet()) {
            final NBT nbt = e.getValue();

            output.writeTagId(nbt.getType());
            output.writeNBTString(e.getKey());
            nbt.writeValue(output);
        }

        output.writeTagId(Type.END);
    }

    public int size() {
        return getValue().size();
    }

    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    public NBT get(String name) {
        return getValue().get(name);
    }

    public boolean contains(String name) {
        return getValue().containsKey(name);
    }

    public byte getByte(String name) {
        return (byte) get(name).getValue();
    }

    public boolean getBoolean(String name) {
        return (boolean) ((ByteTag) get(name)).getBooleanValue();
    }

    public short getShort(String name) {
        return (short) get(name).getValue();
    }

    public int getInt(String name) {
        return (int) get(name).getValue();
    }

    public long getLong(String name) {
        return (long) get(name).getValue();
    }

    public float getFloat(String name) {
        return (float) get(name).getValue();
    }

    public double getDouble(String name) {
        return (double) get(name).getValue();
    }

    public String getString(String name) {
        return (String) get(name).getValue();
    }

    public byte[] getByteArray(String name) {
        return (byte[]) get(name).getValue();
    }

    public int[] getIntArray(String name) {
        return (int[]) get(name).getValue();
    }

    public void put(String name, NBT nbt) {
        nbt.setName(name);
        getValue().put(name, nbt);
    }

    public void put(String name, byte x) {
        put(name, new ByteTag(x));
    }

    public void put(String name, boolean x) {
        put(name, new ByteTag(x));
    }

    public void put(String name, short x) {
        put(name, new ShortTag(x));
    }

    public void put(String name, int x) {
        put(name, new IntTag(x));
    }

    public void put(String name, long x) {
        put(name, new LongTag(x));
    }

    public void put(String name, float x) {
        put(name, new FloatTag(x));
    }

    public void put(String name, double x) {
        put(name, new DoubleTag(x));
    }

    public void put(String name, String x) {
        put(name, new StringTag(x));
    }

    public void put(String name, byte[] x) {
        put(name, new ByteArrayTag(x));
    }

    public void put(String name, int[] x) {
        put(name, new IntArrayTag(x));
    }

    public void remove(String name) {
        getValue().remove(name);
    }

    public ListTag getList(String name) {
        return (ListTag) get(name);
    }

    public CompoundTag getCompound(String name) {
        return (CompoundTag) get(name);
    }

    @Override
    public Iterator<NBT> iterator() {
        return getValue().values().iterator();
    }

    @Override
    String toString(int level) {
        final StringBuilder s = new StringBuilder();
        s.append(getType().toString()).append(" {");
        for (Map.Entry<String, NBT> e : getValue().entrySet()) {
            s.append("\n");
            indent(s, level + 1);
            s.append(e.getKey()).append(" = ");
            s.append(e.getValue().toString(level + 1));
        }

        if (size() > 0) {
            s.append("\n");
            indent(s, level);
        }

        s.append("}");

        return s.toString();
    }
}
