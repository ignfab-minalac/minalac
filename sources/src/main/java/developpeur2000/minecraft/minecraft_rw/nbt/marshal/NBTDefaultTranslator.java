package developpeur2000.minecraft.minecraft_rw.nbt.marshal;

import java.util.Map;
import java.util.Set;

import developpeur2000.minecraft.minecraft_rw.nbt.*;

/**
 * The default NBT translator.
 */
public class NBTDefaultTranslator implements NBTTranslator<Object> {
    @Override
    public Object translateFromNBT(NBT[] nbt) {
        if (nbt.length > 0) {
            return nbt[0].getValue();
        } else {
            return null;
        }
    }

    @Override
    public NBT[] translateToNBT(Object x) {
        NBT nbt = null;
        if (x instanceof Byte) {
            nbt = new ByteTag((byte) x);
        } else if (x instanceof Boolean) {
            nbt = new ByteTag((boolean) x ? (byte) 1 : (byte) 0);
        } else if (x instanceof Short) {
            nbt = new ShortTag((short) x);
        } else if (x instanceof Integer) {
            nbt = new IntTag((int) x);
        } else if (x instanceof Long) {
            nbt = new LongTag((long) x);
        } else if (x instanceof Double) {
            nbt = new DoubleTag((double) x);
        } else if (x instanceof Float) {
            nbt = new FloatTag((float) x);
        } else if (x instanceof String) {
            nbt = new StringTag((String) x);
        } else if (x instanceof byte[]) {
            nbt = new ByteArrayTag((byte[]) x);
        } else if (x instanceof int[]) {
            nbt = new IntArrayTag((int[]) x);
        } else if (x instanceof Map) {
        	Map map = (Map)x;
        	if ( (!map.isEmpty())
        			&& (map.keySet().toArray()[0] instanceof String)
        			&& (map.values().toArray()[0] instanceof NBT) ) {
        		//a Map<String,NBT> will be considered as a compound object
        		nbt = new CompoundTag();
        		Set<Map.Entry<String,NBT>> set = (Set<Map.Entry<String,NBT>>) map.entrySet();
        		for (Map.Entry<String,NBT> entry : set) {
        			((CompoundTag) nbt).put(entry.getKey(), entry.getValue());
        		}
        	}
        }

        return (nbt == null) ? new NBT[0] : new NBT[]{nbt};
    }
}
