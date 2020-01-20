package developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations;

import java.lang.annotation.*;

/**
 * Annotation for classes that can be read from and written to NBT lists.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NBTListType {
}
