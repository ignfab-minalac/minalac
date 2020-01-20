package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Representation of the {@link NBT.Type#LIST} tag.
 */
public class ListTag extends NBT<List<NBT>> implements Iterable<NBT> {
    private static interface NBTParser<T> {
        public NBT parse(T x);
    }

    private Type itemType = null;

    public ListTag() {
        setValue(new ArrayList<NBT>());
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(NBT... items) {
        this();
        for (NBT x : items) {
            checkType(x);
            add(x);
        }
    }

    private <T> ListTag(NBTParser<T> converter, T[] items) {
        this();
        for (T x : items) {
            final NBT nbt = converter.parse(x);
            checkType(nbt);
            add(nbt);
        }
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(Short... items) {
        //Java 8: this(ShortTag::new, items);
        this(new NBTParser<Short>() {
            @Override
            public NBT parse(Short x) {
                return new ShortTag(x);
            }
        }, items);
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(Integer... items) {
        //Java 8: this(IntTag::new, items);
        this(new NBTParser<Integer>() {
            @Override
            public NBT parse(Integer x) {
                return new IntTag(x);
            }
        }, items);
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(Long... items) {
        //Java 8: this(LongTag::new, items);
        this(new NBTParser<Long>() {
            @Override
            public NBT parse(Long x) {
                return new LongTag(x);
            }
        }, items);
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(Float... items) {
        //Java 8: this(FloatTag::new, items);
        this(new NBTParser<Float>() {
            @Override
            public NBT parse(Float x) {
                return new FloatTag(x);
            }
        }, items);
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(Double... items) {
        //Java 8: this(DoubleTag::new, items);
        this(new NBTParser<Double>() {
            @Override
            public NBT parse(Double x) {
                return new DoubleTag(x);
            }
        }, items);
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(String... items) {
        //Java 8: this(StringTag::new, items);
        this(new NBTParser<String>() {
            @Override
            public NBT parse(String x) {
                return new StringTag(x);
            }
        }, items);
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(byte[]... items) {
        //Java 8: this(ByteArrayTag::new, items);
        this(new NBTParser<byte[]>() {
            @Override
            public NBT parse(byte[] x) {
                return new ByteArrayTag(x);
            }
        }, items);
    }

    /**
     * Constructs a new list with the specified items.
     *
     * @param items the list items.
     */
    public ListTag(int[]... items) {
        //Java 8: this(IntArrayTag::new, items);
        this(new NBTParser<int[]>() {
            @Override
            public NBT parse(int[] x) {
                return new IntArrayTag(x);
            }
        }, items);
    }

    @Override
    public Type getType() {
        return Type.LIST;
    }

    @Override
    void readValue(NBTDataInput input) throws IOException {
        itemType = input.readTagId();
        final int length = input.readInt();

        for (int i = 0; i < length; i++) {
            final NBT nbt = itemType.createInstance();
            //list items do not have a name
            nbt.readValue(input);
            add(nbt);
        }
    }

    @Override
    void writeValue(NBTDataOutput output) throws IOException {
        if (itemType == null) {
            output.writeTagId(Type.END);
            output.writeInt(0);
        } else {
            output.writeTagId(itemType);
            output.writeInt(getValue().size());
            for (NBT nbt : getValue()) {
                nbt.writeValue(output);
            }
        }
    }

    void checkType(NBT x) {
        if (itemType == null) {
            itemType = x.getType();
        } else if (x.getType() != itemType) {
            throw new NBTException("All items of this list need to be of type " + itemType + ".");
        }
    }

    public void setType(Type type) {
        if (itemType == null) {
            itemType = type;
        } else {
            throw new NBTException("Cannot set type on a list that already has a type or some elements");
        }
    }

    public int size() {
        return getValue().size();
    }

    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    public NBT get(int i) {
        return getValue().get(i);
    }

    public byte getByte(int i) {
        return (byte) get(i).getValue();
    }

    public short getShort(int i) {
        return (short) get(i).getValue();
    }

    public int getInt(int i) {
        return (int) get(i).getValue();
    }

    public long getLong(int i) {
        return (long) get(i).getValue();
    }

    public float getFloat(int i) {
        return (float) get(i).getValue();
    }

    public double getDouble(int i) {
        return (double) get(i).getValue();
    }

    public String getString(int i) {
        return (String) get(i).getValue();
    }

    public byte[] getByteArray(int i) {
        return (byte[]) get(i).getValue();
    }

    public int[] getIntArray(int i) {
        return (int[]) get(i).getValue();
    }

    public ListTag getList(int i) {
        return (ListTag) get(i);
    }

    public CompoundTag getCompound(int i) {
        return (CompoundTag) get(i);
    }

    public void add(NBT nbt) {
        checkType(nbt);
        getValue().add(nbt);
    }

    public void add(byte x) {
        add(new ByteTag(x));
    }

    public void add(short x) {
        add(new ShortTag(x));
    }

    public void add(int x) {
        add(new IntTag(x));
    }

    public void add(long x) {
        add(new LongTag(x));
    }

    public void add(float x) {
        add(new FloatTag(x));
    }

    public void add(double x) {
        add(new DoubleTag(x));
    }

    public void add(String x) {
        add(new StringTag(x));
    }

    public void add(byte[] x) {
        add(new ByteArrayTag(x));
    }

    public void add(int[] x) {
        add(new IntArrayTag(x));
    }


    public NBT set(int i, NBT element) {
        checkType(element);
        return getValue().set(i, element);
    }

    @Override
    public Iterator<NBT> iterator() {
        return getValue().iterator();
    }

    @Override
    String toString(int level) {
        final StringBuilder s = new StringBuilder();
        s.append(getType().toString()).append(" {");
        for (NBT item : this) {
            s.append("\n");
            indent(s, level + 1);
            s.append(item.toString(level + 1));
        }

        if (size() > 0) {
            s.append("\n");
            indent(s, level);
        }
        s.append("}");

        return s.toString();
    }
}
