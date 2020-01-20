package developpeur2000.minecraft.minecraft_rw.nbt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterOutputStream;

/**
 * Represents a Minecraft Named Binary Tag (NBT).
 *
 * @param <T> the encapsulated payload type.
 */
public abstract class NBT<T> {
    static final Charset ENCODING = Charset.forName("UTF-8");
    
    public static final byte COMPRESSION_GZIP = 1;
    public static final byte COMPRESSION_7ZIP = 2;

    /**
     * Enumeration of known tag types.
     */
    public enum Type {
        /**
         * Marks the end of a compound tag.
         */
        END(null),

        /**
         * Announces a signed 8-bit integer.
         */
        BYTE(ByteTag.class),

        /**
         * Announces a signed 16-bit integer (big endian).
         */
        SHORT(ShortTag.class),

        /**
         * Announces a signed 32-bit integer (big endian).
         */
        INT(IntTag.class),

        /**
         * Announces a signed 64-bit integer (big endian).
         */
        LONG(LongTag.class),

        /**
         * Announces a 32-bit floating point value.
         */
        FLOAT(FloatTag.class),

        /**
         * Announces a 64-bit floating point value.
         */
        DOUBLE(DoubleTag.class),

        /**
         * Announces an array of bytes.
         */
        BYTE_ARRAY(ByteArrayTag.class),

        /**
         * Announces a UTF-8 string.
         */
        STRING(StringTag.class),

        /**
         * Announces a tag list.
         */
        LIST(ListTag.class),

        /**
         * Announces a tag compound.
         */
        COMPOUND(CompoundTag.class),

        /**
         * Announces an array of signed 32-bit integers (big endian).
         */
        INT_ARRAY(IntArrayTag.class);

        final Class<? extends NBT> tagClass;

        Type(Class<? extends NBT> tagClass) {
            this.tagClass = tagClass;
        }

        NBT createInstance() {
            try {
                return tagClass.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new NBTException("Failed to instantiate NBT of type: " + this, ex);
            }
        }
    }

    /**
     * Loads a tag tree from the specified input stream directly, without piping it through GZIP.
     * this enable the compression format to be set prior to the call
     *
     * @param in the stream to read from.
     * @return the read compound of tags.
     * @throws java.io.IOException
     */
    public static CompoundTag loadDirect(InputStream in) throws IOException {
        try (final NBTDataInputStream nbtIn = new NBTDataInputStream(in)) {
            final Type init = nbtIn.readTagId();
            if (init != Type.COMPOUND) {
                throw new NBTException("Wrong NBT format - initial tag needs to be a compound!");
            } else {
                final CompoundTag compound = new CompoundTag();
                compound.setName(nbtIn.readNBTString());
                compound.readValue(nbtIn);
                return compound;
            }
        }
    }

    /**
     * Loads an NBT file from the specified input stream.
     *
     * @param in the stream to read from.
     * @return the read compound of tags.
     * @throws java.io.IOException
     */
    public static CompoundTag load(InputStream in) throws IOException {
        return loadDirect(new GZIPInputStream(in));
    }

    /**
     * Loads a tag tree from the specified file.
     *
     * @param path the path to the file to read.
     * @return the read compound of tags.
     * @throws java.io.IOException
     */
    public static CompoundTag load(Path path) throws IOException {
        try (final InputStream in = Files.newInputStream(path)) {
            return load(in);
        }
    }

    /**
     * Writes a tag tree to the specified output stream.
     *
     * @param out      the stream to write to.
     * @param compound the compound of tags to write.
     * @throws IOException
     */
    public static void save(OutputStream out, CompoundTag compound) throws IOException, NBTException {
    	try {
			save(out, compound, "java.util.zip.DeflaterOutputStream");
		} catch (ClassNotFoundException e) {
			// Unexpected
			assert false;
		}
    }
    
    public static byte getDefaultSaveCompression() {
    	return NBT.COMPRESSION_7ZIP;
    }

    /**
     * Writes a tag tree to the specified output stream.
     *
     * @param out      the stream to write to.
     * @param compound the compound of tags to write.
     * @param compressionStreamClassName the name of the class to be used as compressing output stream class
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static void save(OutputStream out, CompoundTag compound, String compressionStreamClassName) throws IOException, ClassNotFoundException {
    	Class<?> compressionStreamBaseClass = Class.forName("java.util.zip.DeflaterOutputStream");
    	Class<?> compressionStreamClass = Class.forName(compressionStreamClassName);
    	if(!compressionStreamBaseClass.isAssignableFrom(compressionStreamClass)) {
    		throw new NBTException("cannot save NBT with compression stream class as " + compressionStreamClassName);
    	}
		Constructor<?> compressionStreamConstructor = null;
		try {
			compressionStreamConstructor = compressionStreamClass.getConstructor(OutputStream.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// Unexpected
			assert false;
			return;
		}

        try (final NBTDataOutputStream nbtOut = new NBTDataOutputStream( (DeflaterOutputStream) compressionStreamConstructor.newInstance(out) )) {
            nbtOut.writeTagId(Type.COMPOUND);
            nbtOut.writeNBTString(""); //empty name
            compound.writeValue(nbtOut);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
        	// Unexpected
			assert false;
			return;
		}
    }

    /**
     * Writes a tag tree to the specified file
     *
     * @param path     the path to the file to write.
     * @param compound the compound of tags to write.
     * @throws IOException
     */
    public static void save(Path path, CompoundTag compound) throws IOException {
        try (final OutputStream out = Files.newOutputStream(path)) {
            save(out, compound);
        }
    }

    private String name = "";
    private T value = null;

    /**
     * Default constructor, not to be exposed publicly.
     */
    NBT() {
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public T getValue() {
        return value;
    }

    void setValue(T value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Gets the tag value for this tag.
     *
     * @return the tag value.
     */
    public abstract Type getType();

    /**
     * Reads the tag data from the input - NOT including its own tag ID.
     *
     * @param input the input to read from.
     * @throws java.io.IOException
     */
    abstract void readValue(NBTDataInput input) throws IOException;

    /**
     * Writes the tag data to the output - NOT including its own tag ID.
     *
     * @param output the output to write to.
     * @throws IOException
     */
    abstract void writeValue(NBTDataOutput output) throws IOException;

    static void indent(StringBuilder s, int level) {
        for (; level > 0; level--) {
            s.append('\t');
        }
    }

    String toString(int level) {
        return value.toString() + ":" + getType().toString();
    }

    @Override
    public final String toString() {
        return toString(0);
    }
}
