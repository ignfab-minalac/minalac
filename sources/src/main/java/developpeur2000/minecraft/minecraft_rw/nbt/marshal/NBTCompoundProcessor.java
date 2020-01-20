package developpeur2000.minecraft.minecraft_rw.nbt.marshal;

import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;

/**
 * Interface for classes annotated with {@link developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType}, which do not have
 * a tight  property binding.
 * <p/>
 * These can freely process the input compound in order to be unmarshalled or marshalled.
 */
public interface NBTCompoundProcessor {
    /**
     * Unmarshals an NBT compound.
     *
     * @param nbt the NBT compound.
     */
    public void unmarshalCompound(CompoundTag nbt);

    /**
     * Marshals this object into an NBT compound.
     *
     * @return the NBT compound.
     */
    public CompoundTag marshalCompound();
}
