package developpeur2000.minecraft.minecraft_rw.nbt.marshal;

import developpeur2000.minecraft.minecraft_rw.nbt.CompoundTag;
import developpeur2000.minecraft.minecraft_rw.nbt.ListTag;
import developpeur2000.minecraft.minecraft_rw.nbt.NBT;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTCompoundType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTListItem;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTListType;
import developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations.NBTProperty;
import developpeur2000.minecraft.util.PropertyAnnotationInfo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides functionality to unmarshal NBT into POJOs and to marshal POJOs into NBT.
 * <p/>
 * The target classes are expected to conform the Java beans standard and to be annotated with the
 * annotations provided by the {@link developpeur2000.minecraft.minecraft_rw.nbt.marshal.annotations} package.
 * <p/>
 * Only annotated fields will be regarded by the marshaller / unmarshaller.
 */
public class NBTMarshal {
    private static final Logger LOGGER = Logger.getLogger("NBTMarshal");
    private static final Map<Class<? extends NBTTranslator>, NBTTranslator> TRANSLATORS = new HashMap<>();

    private static NBTTranslator<?> translatorInstance(Class<? extends NBTTranslator> translatorClass) {
        NBTTranslator<?> translator = TRANSLATORS.get(translatorClass);
        if (translator == null) {
            try {
                translator = translatorClass.newInstance();
                TRANSLATORS.put(translatorClass, translator);
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new NBTMarshalException("Failed to instantiate translator.", ex);
            }
        }

        return translator;
    }

    private static boolean containsNull(Object[] array) {
        for (Object x : array) {
            if (x == null) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private static Object unmarshalValue(
            Class<?> type,
            NBT[] valueTags,
            NBTTranslator<?> translator,
            Class<?> listItemType) {

        Object value;
        if (NBT.class.isAssignableFrom(type)) {
            value = valueTags[0];
        } else if (canUnmarshal(type, valueTags[0])) {
            value = unmarshal(type, valueTags[0]);
        } else if (listItemType != null && List.class.isAssignableFrom(type) && valueTags[0] instanceof ListTag) {
            if (listItemType == Object.class) {
                throw new NBTMarshalException("No list item type provided.");
            }

            final List list;
            try {
                list = (List) type.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new NBTMarshalException("Failed to instantiate list.", ex);
            }

            for (NBT item : (ListTag) valueTags[0]) {
                list.add(unmarshalValue(listItemType, new NBT[]{item}, translator, null));
            }

            value = list;
        } else {
            value = translator.translateFromNBT(valueTags);
        }

        if (value instanceof Byte && (type == Boolean.class || type == Boolean.TYPE)) {
            value = ((Byte) value != 0) ? Boolean.TRUE : Boolean.FALSE;
        }

        return value;
    }

    private static void setValue(
            Object target,
            PropertyDescriptor property,
            NBT[] valueTags,
            NBTTranslator translator,
            Class<?> listItemType) {

        final Object value = unmarshalValue(property.getPropertyType(), valueTags, translator, listItemType);
        try {
            property.getWriteMethod().invoke(target, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new NBTMarshalException("Failed to set value " + value + " for property " + property.getName(), ex);
        }

        LOGGER.log(Level.FINE, property.getName() + " -> " + value);
    }
    
    private static NBT[] marshalValue(
            Object value,
            NBTTranslator translator) {

    	NBT[] nbt;
        if (NBT.class.isAssignableFrom(value.getClass())) {
        	nbt = new NBT[]{(NBT) value};
        } else if (canMarshal(value)) {
            nbt = new NBT[]{marshal(value)};
        } else if (List.class.isAssignableFrom(value.getClass())) {
            nbt = new NBT[]{new ListTag()};
            for (Object singleValue : (List) value) {
            	NBT[] nbtItem = marshalValue(singleValue, translator);
            	if (nbtItem.length != 1) {
            		throw new NBTMarshalException("individual values in a list cannot be translated in NBT as several elements");
            	}
            	((ListTag) nbt[0]).add( nbtItem[0] );
            }
        } else {
        	nbt = translator.translateToNBT(value);
        }

        return nbt;
    }

    private static void unmarshalCompound(Object target, CompoundTag nbt) {
        if (target instanceof NBTCompoundProcessor) {
            ((NBTCompoundProcessor) target).unmarshalCompound(nbt);
        } else {
            try {
                for (PropertyAnnotationInfo<NBTProperty>.AnnotatedProperty prop :
                        NBTPropertyHelper.getNBTProperties(target.getClass())) {

                    final String[] nbtNames;
                    if (prop.annotation.value().length > 0) {
                        nbtNames = prop.annotation.value();
                    } else {
                        String name = prop.descriptor.getName();
                        if (prop.annotation.upperCase()) {
                            name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        }

                        nbtNames = new String[]{name};
                    }

                    final NBT<?>[] propertyTags = new NBT[nbtNames.length];
                    for (int i = 0; i < nbtNames.length; i++) {
                        propertyTags[i] = nbt.get(nbtNames[i]);
                    }

                    if (!containsNull(propertyTags)) {
                        setValue(target, prop.descriptor, propertyTags,
                                translatorInstance(prop.annotation.translator()),
                                prop.annotation.listItemType());
                    } else if (!prop.annotation.optional()) {
                        throw new NBTMarshalException("Non-optional property \"" + prop.descriptor.getName() + "\"" +
                                " does not exist in compound of " + target.toString());
                    }
                }
            } catch (IntrospectionException ex) {
                throw new NBTMarshalException("Failed to introspect target object.", ex);
            }
        }
    }
    
    private static NBT marshalCompound(Object object) {
        if (object instanceof NBTCompoundProcessor) {
            return ((NBTCompoundProcessor) object).marshalCompound();
        } else {
        	CompoundTag compound = new CompoundTag();
            try {
                for (PropertyAnnotationInfo<NBTProperty>.AnnotatedProperty prop :
                        NBTPropertyHelper.getNBTProperties(object.getClass())) {

                    final String[] nbtNames;
                    if (prop.annotation.value().length > 0) {
                        nbtNames = prop.annotation.value();
                    } else {
                    	String name = prop.descriptor.getName();
                        if (prop.annotation.upperCase()) {
                            name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        }

                        nbtNames = new String[]{name};
                    }

                    final Object value;
                    try {
                    	value = prop.descriptor.getReadMethod().invoke(object);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        throw new NBTMarshalException("Failed to read value for property " + prop.descriptor.getName(), ex);
                    }

                    if (value != null) {
                    	final NBT[] nbtTags = marshalValue(value, translatorInstance(prop.annotation.translator()));
                    	if (nbtTags.length != nbtNames.length) {
                            throw new NBTMarshalException("Mismatch of number of NBT tags between annotation and translator for property " + prop.descriptor.getName() + " in " + object.getClass());
                    	}
                        for (int i = 0; i < nbtNames.length; i++) {
                        	compound.put( nbtNames[i], nbtTags[i] );
                    	}
                    } else if (!prop.annotation.optional()) {
                        throw new NBTMarshalException("Non-optional property \"" + prop.descriptor.getName() + "\"" +
                                " is not set in object " + object.getClass().getName() + ".");
                    }
                }
            } catch (IntrospectionException ex) {
                throw new NBTMarshalException("Failed to introspect source object.", ex);
            }
            return compound;
        }
    }

    private static void unmarshalList(Object target, ListTag nbt) {
        if (target instanceof NBTListProcessor) {
            ((NBTListProcessor) target).unmarshalList(nbt);
        } else {
            try {
                for (PropertyAnnotationInfo<NBTListItem>.AnnotatedProperty prop :
                        NBTPropertyHelper.getNBTListItems(target.getClass())) {


                    final NBT<?> value = nbt.get(prop.annotation.value());
                    setValue(target, prop.descriptor, new NBT[]{value},
                            translatorInstance(prop.annotation.translator()),
                            prop.annotation.listItemType());
                }
            } catch (IntrospectionException ex) {
                throw new NBTMarshalException("Failed to introspect target object.", ex);
            }
        }
    }
    
    private static NBT<?> marshalList(Object object) {
        if (object instanceof NBTCompoundProcessor) {
            return ((NBTListProcessor) object).marshalList();
        } else {
        	ListTag list = new ListTag();
            try {
//            	final HashMap<Integer, PropertyAnnotationInfo<NBTListItem>.AnnotatedProperty> indexedProperties
//            		= new HashMap<Integer, PropertyAnnotationInfo<NBTListItem>.AnnotatedProperty>();
            	List<PropertyAnnotationInfo<NBTListItem>.AnnotatedProperty> listItems = NBTPropertyHelper.getNBTListItems(object.getClass());
            	NBT[] nbtValues = new NBT[listItems.size()];
                for (PropertyAnnotationInfo<NBTListItem>.AnnotatedProperty prop : listItems) {
                	if ((prop.annotation.value() >= nbtValues.length) || (nbtValues[prop.annotation.value()] != null)) {
                		throw new NBTMarshalException("Incorrect list indexes for in " + object.getClass() + " detected in property " + prop.descriptor.getName());
                	}

                	final Object value;
                    try {
                    	value = prop.descriptor.getReadMethod().invoke(object);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        throw new NBTMarshalException("Failed to read value for property " + prop.descriptor.getName(), ex);
                    }

                    if (value != null) {
                    	final NBT[] nbtTags = marshalValue(value, translatorInstance(prop.annotation.translator()));
                    	if (nbtTags.length != 1) {
                            throw new NBTMarshalException("Only one NBT tag per value is allowed for list property " + prop.descriptor.getName() + " in " + object.getClass());
                    	}

                    	nbtValues[prop.annotation.value()] = nbtTags[0];
                    } else {
                        throw new NBTMarshalException("List property \"" + prop.descriptor.getName() + "\"" +
                                " is not set in object " + object.getClass().getName() + ".");
                    }
                }
                
                for (int i = 0; i < nbtValues.length; i++) {
                	list.add( nbtValues[i] );
                }
            } catch (IntrospectionException ex) {
                throw new NBTMarshalException("Failed to introspect source object.", ex);
            }
            return list;
        }
    }

    static boolean canUnmarshal(Class<?> targetClass, NBT nbt) {
        return (targetClass.isAnnotationPresent(NBTCompoundType.class) && nbt instanceof CompoundTag)
                || (targetClass.isAnnotationPresent(NBTListType.class) && nbt instanceof ListTag);
    }

    static boolean canMarshal(Object object) {
        return object.getClass().isAnnotationPresent(NBTCompoundType.class)
                || object.getClass().isAnnotationPresent(NBTListType.class);
    }

    /**
     * Attempts to unmarshal the given NBT into a POJO of the specified target class.
     *
     * @param targetClass the target class.
     * @param nbt         the NBT to unmarshal.
     * @param <T>         the target type.
     * @return the unmarshalled POJO.
     */
    public static <T> T unmarshal(Class<T> targetClass, NBT nbt) {
        if (canUnmarshal(targetClass, nbt)) {
            final T target;
            try {
                target = targetClass.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new NBTMarshalException("Could not instantiate " + targetClass, ex);
            }

            if (nbt instanceof CompoundTag) {
                unmarshalCompound(target, (CompoundTag) nbt);
            } else if (nbt instanceof ListTag) {
                unmarshalList(target, (ListTag) nbt);
            }

            return target;
        } else {
            throw new NBTMarshalException("Cannot unmarshal " + nbt.getType() + " into " + targetClass);
        }
    }
    
    /**
     * Attempts to marshal the given Object into an NBT.
     *
     * @param object      the object containing data to be marshalled.
     * @return an NBT object corresponding to the marshalled Object.
     */
    public static NBT<?> marshal(Object object) {
        if (canMarshal(object)) {
        	NBT<?> nbt = null;
	        if (object.getClass().isAnnotationPresent(NBTCompoundType.class)) {
	        	nbt = marshalCompound(object);
	        } else if (object.getClass().isAnnotationPresent(NBTListType.class)) {
	        	nbt = marshalList(object);
	        }
	        return nbt;
        } else {
        	throw new NBTMarshalException("Cannot marshal object of class " + object.getClass());
        }
    }
}
