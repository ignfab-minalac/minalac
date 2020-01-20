package developpeur2000.minecraft.minecraft_rw.nbt.marshal;

import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTListItem;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;
import developpeur2000.minecraft.util.PropertyAnnotationInfo;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NBTPropertyHelper {
    private static final Map<Class<?>, PropertyAnnotationInfo<NBTProperty>> PROPERTY_CACHE = new HashMap<>();
    private static final Map<Class<?>, PropertyAnnotationInfo<NBTListItem>> LIST_ITEM_CACHE = new HashMap<>();

    public static List<PropertyAnnotationInfo<NBTProperty>.AnnotatedProperty> getNBTProperties(Class<?> targetClass)
            throws IntrospectionException {

        PropertyAnnotationInfo<NBTProperty> info = PROPERTY_CACHE.get(targetClass);
        if (info == null) {
            info = new PropertyAnnotationInfo<>(targetClass, NBTProperty.class);
            PROPERTY_CACHE.put(targetClass, info);
        }

        return info.getAnnotatedProperties();
    }

    public static List<PropertyAnnotationInfo<NBTListItem>.AnnotatedProperty> getNBTListItems(Class<?> targetClass)
            throws IntrospectionException {

        PropertyAnnotationInfo<NBTListItem> info = LIST_ITEM_CACHE.get(targetClass);
        if (info == null) {
            info = new PropertyAnnotationInfo<>(targetClass, NBTListItem.class);
            LIST_ITEM_CACHE.put(targetClass, info);
        }

        return info.getAnnotatedProperties();
    }
}
