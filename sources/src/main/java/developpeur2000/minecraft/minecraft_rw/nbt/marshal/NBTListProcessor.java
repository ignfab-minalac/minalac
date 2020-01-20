package developpeur2000.minecraft.minecraft_rw.nbt.marshal;

import developpeur2000.minecraft.minecraft_rw.nbt.ListTag;

/**
 * Interface for classes annotated with {@link developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTListType}, which do not have
 * a tight item index binding.
 * <p>
 * These can freely process the input list in order to be unmarshalled or marshalled.
 */
public interface NBTListProcessor {
    /**
     * Unmarshals an NBT list.
     *
     * @param list the NBT list.
     */
    public void unmarshalList(ListTag list);

    /**
     * Marshals this object into an NBT list.
     *
     * @return the NBT list.
     */
    public ListTag marshalList();
}
