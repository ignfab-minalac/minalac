package developpeur2000.minecraft.minecraft_rw.nbt.marshal;

import developpeur2000.minecraft.minecraft_rw.nbt.NBTException;

/**
 * Exception type for NBT marshalling errors.
 */
public class NBTMarshalException extends NBTException {
    public NBTMarshalException(String message) {
        super(message);
    }

    public NBTMarshalException(String message, Throwable cause) {
        super(message, cause);
    }

    public NBTMarshalException(Throwable cause) {
        super(cause);
    }
}
