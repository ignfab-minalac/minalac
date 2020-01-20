package developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations;

import java.lang.annotation.*;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTDefaultTranslator;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.NBTTranslator;

/**
 * Annotation for named properties within an NBT compound.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NBTProperty {
    /**
     * @return the names of the NBTs within the compound.
     */
    public String[] value() default {};

    /**
     * @return the translator class to use.
     */
    public Class<? extends NBTTranslator> translator() default NBTDefaultTranslator.class;

    /**
     * @return for properties that derive from {@link java.util.List}, this must define the item class.
     */
    public Class<?> listItemType() default Object.class;

    /**
     * @return whether this property is allowed to not exist.
     */
    public boolean optional() default false;

    /**
     * @return whether or not the actual tag name begins with an upper case letter.
     */
    public boolean upperCase() default false;
}
