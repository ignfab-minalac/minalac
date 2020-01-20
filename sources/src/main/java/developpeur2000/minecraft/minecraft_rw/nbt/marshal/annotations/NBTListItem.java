package developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTDefaultTranslator;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTTranslator;

/**
 * Annotation for items within an NBT list.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NBTListItem {
    /**
     * @return the list item index.
     */
    public int value();

    /**
     * @return the translator class to use.
     */
    public Class<? extends NBTTranslator> translator() default NBTDefaultTranslator.class;

    /**
     * @return for properties that derive from {@link java.util.List}, this must define the item class.
     */
    public Class<?> listItemType() default Object.class;
}
