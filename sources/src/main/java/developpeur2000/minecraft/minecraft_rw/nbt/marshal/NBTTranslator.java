package developpeur2000.minecraft.minecraft_rw.nbt.marshal;

import developpeur2000.minecraft.minecraft_rw.nbt.NBT;

/**
 * Interface for translators between NBT tags and the specified type.
 *
 * @param <T> the type that can be translated from and to by this translator.
 */
public interface NBTTranslator<T> {
    /**
     * Translates the given NBT into the translator's target type.
     *
     * @param nbt the NBT to translate.
     * @return the translated target object.
     */
    public T translateFromNBT(NBT[] nbt);

    /**
     * Translates the given object into an NBT.
     *
     * @param x the object to translate.
     * @return the translated NBT.
     */
    public NBT[] translateToNBT(T x);
}
