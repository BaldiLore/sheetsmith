package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Determines the type {@code T} handled by a {@link CellConverter} implementation from its generic declaration.
 */
public final class ConverterTypes {

    private ConverterTypes() {
    }

    /**
     * Returns the type handled by a converter class, following superclasses and superinterfaces and resolving type
     * variables bound along the way.
     *
     * @param converterClass the converter class
     * @return the raw handled type, or null if it cannot be determined (raw implementation, unresolved type
     *         variable, wildcard or generic array)
     */
    public static Class<?> handledType(Class<?> converterClass) {
        return resolve(converterClass, Map.of());
    }

    private static Class<?> resolve(Type type, Map<TypeVariable<?>, Type> bindings) {
        Class<?> raw;
        Map<TypeVariable<?>, Type> local = new HashMap<>();
        if (type instanceof ParameterizedType parameterized) {
            raw = (Class<?>) parameterized.getRawType();
            TypeVariable<?>[] variables = raw.getTypeParameters();
            Type[] arguments = parameterized.getActualTypeArguments();
            for (int i = 0; i < variables.length; i++) {
                Type argument = arguments[i];
                local.put(variables[i], argument instanceof TypeVariable<?> variable
                        ? bindings.getOrDefault(variable, variable)
                        : argument);
            }
        } else if (type instanceof Class<?> c) {
            raw = c;
        } else {
            return null;
        }

        if (raw == CellConverter.class) {
            return toClass(local.get(CellConverter.class.getTypeParameters()[0]));
        }
        for (Type parent : raw.getGenericInterfaces()) {
            Class<?> found = resolve(parent, local);
            if (found != null) {
                return found;
            }
        }
        Type superclass = raw.getGenericSuperclass();
        return superclass == null ? null : resolve(superclass, local);
    }

    private static Class<?> toClass(Type type) {
        if (type instanceof Class<?> c) {
            return c;
        }
        if (type instanceof ParameterizedType parameterized) {
            return (Class<?>) parameterized.getRawType();
        }
        return null;
    }
}
