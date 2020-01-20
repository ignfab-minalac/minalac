package developpeur2000.minecraft.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Helper to deal with property annotations.
 */
public class PropertyAnnotationInfo<T extends Annotation> {
    private static List<Field> getAllFields(Class<?> targetClass) {
        final List<Field> fields = new LinkedList<>();
        while (targetClass != null) {
            fields.addAll(Arrays.asList(targetClass.getDeclaredFields()));
            targetClass = targetClass.getSuperclass();
        }

        return fields;
    }

    public class AnnotatedProperty {
        public final PropertyDescriptor descriptor;
        public final T annotation;

        private AnnotatedProperty(PropertyDescriptor descriptor, T annotation) {
            this.annotation = annotation;
            this.descriptor = descriptor;
        }
    }

    private final Class<?> targetClass;
    private final Class<T> annotationClass;
    private final List<AnnotatedProperty> annotatedProperties = new LinkedList<>();

    public PropertyAnnotationInfo(Class<?> targetClass, Class<T> annotationClass) throws IntrospectionException {
        this.targetClass = targetClass;
        this.annotationClass = annotationClass;
        findAnnotatedProperties();
    }

    private void findAnnotatedProperties() throws IntrospectionException {
        //Initialize descriptors
        final Map<String, PropertyDescriptor> descriptors = new HashMap<>();
        final BeanInfo beanInfo = Introspector.getBeanInfo(targetClass);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            descriptors.put(pd.getName(), pd);
        }

        //Find annotated fields
        for (Field field : getAllFields(targetClass)) {
            final T annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                final PropertyDescriptor descriptor = descriptors.get(field.getName());
                if (descriptor != null) {
                    annotatedProperties.add(new AnnotatedProperty(descriptor, annotation));
                } else {
                    throw new IntrospectionException(
                            "Field " + field.toString() + " is annotated with " + annotationClass +
                                    ", but does not conform the Java Beans standard."
                    );
                }
            }
        }
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Class<T> getAnnotationClass() {
        return annotationClass;
    }

    public List<AnnotatedProperty> getAnnotatedProperties() {
        return annotatedProperties;
    }
}
