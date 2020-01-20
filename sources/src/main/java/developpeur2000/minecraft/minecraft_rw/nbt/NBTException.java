package developpeur2000.minecraft.minecraft_rw.nbt;

/**
 * Exception type for NBT processing errors.
 */
public class NBTException extends RuntimeException {
    private static final long serialVersionUID = -3788560483370598289L;

    public NBTException(String message) {
        super(message);
    }

    public NBTException(Throwable cause) {
        super(cause);
    }

    public NBTException(String message, Throwable cause) {
        super(message, cause);
    }
}
