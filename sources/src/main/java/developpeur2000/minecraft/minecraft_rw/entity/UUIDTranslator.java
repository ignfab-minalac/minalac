package developpeur2000.minecraft.minecraft_rw.entity;

import developpeur2000.minecraft.minecraft_rw.nbt.LongTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTTranslator;

import java.util.UUID;

/**
 * NBT Translator for UUIDs.
 */
public class UUIDTranslator implements NBTTranslator<UUID> {
    @Override
    public UUID translateFromNBT(NBT[] nbt) {
        return new UUID(((LongTag) nbt[0]).getValue(), ((LongTag) nbt[1]).getValue());
    }

    @Override
    public NBT[] translateToNBT(UUID x) {
        return new NBT[]{
                new LongTag(x.getLeastSignificantBits()),
                new LongTag(x.getMostSignificantBits()),
        };
    }
}
