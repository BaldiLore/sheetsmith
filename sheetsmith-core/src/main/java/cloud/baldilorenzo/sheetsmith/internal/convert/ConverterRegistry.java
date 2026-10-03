package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;

import java.lang.invoke.MethodType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Finds the converter registered for a type: application converters first, then built-in ones, each looked up by
 * exact type, closest superclass and closest implemented interface. Immutable and thread-safe.
 */
public final class ConverterRegistry {

    private final Map<Class<?>, CellConverter<?>> application;
    private final Map<Class<?>, CellConverter<?>> builtIn;

    /**
     * Creates a registry.
     *
     * @param application the application converters by type
     */
    public ConverterRegistry(Map<Class<?>, CellConverter<?>> application) {
        this(application, BuiltInConverters.all());
    }

    ConverterRegistry(Map<Class<?>, CellConverter<?>> application, Map<Class<?>, CellConverter<?>> builtIn) {
        this.application = Map.copyOf(application);
        this.builtIn = Map.copyOf(builtIn);
    }

    /**
     * Finds the converter for a declared type; primitive types are boxed.
     *
     * @param type the declared type
     * @return the converter, or null if none is registered
     * @throws AmbiguousConverterException if several interfaces at the same distance have a converter
     */
    public CellConverter<?> find(Class<?> type) {
        Class<?> boxed = box(Objects.requireNonNull(type, "type"));
        CellConverter<?> converter = find(boxed, application);
        return converter != null ? converter : find(boxed, builtIn);
    }

    /**
     * Returns the wrapper class of a primitive type, or the type itself.
     *
     * @param type a type
     * @return the boxed type
     */
    public static Class<?> box(Class<?> type) {
        return type.isPrimitive() ? MethodType.methodType(type).wrap().returnType() : type;
    }

    private static CellConverter<?> find(Class<?> type, Map<Class<?>, CellConverter<?>> converters) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            CellConverter<?> converter = converters.get(c);
            if (converter != null) {
                return converter;
            }
        }
        return findByInterface(type, converters);
    }

    /** Breadth-first walk of the supertypes; only interfaces are matched, superclasses were checked before. */
    private static CellConverter<?> findByInterface(Class<?> type, Map<Class<?>, CellConverter<?>> converters) {
        Set<Class<?>> visited = new HashSet<>();
        List<Class<?>> level = List.of(type);
        while (!level.isEmpty()) {
            Set<Class<?>> matches = new LinkedHashSet<>();
            List<Class<?>> next = new ArrayList<>();
            for (Class<?> c : level) {
                Deque<Class<?>> parents = new ArrayDeque<>(List.of(c.getInterfaces()));
                if (c.getSuperclass() != null) {
                    parents.addFirst(c.getSuperclass());
                }
                for (Class<?> parent : parents) {
                    if (visited.add(parent)) {
                        next.add(parent);
                        if (parent.isInterface() && converters.containsKey(parent)) {
                            matches.add(parent);
                        }
                    }
                }
            }
            if (matches.size() == 1) {
                return converters.get(matches.iterator().next());
            }
            if (matches.size() > 1) {
                throw new AmbiguousConverterException(type, List.copyOf(matches));
            }
            level = next;
        }
        return null;
    }

    /**
     * Thrown when a type implements several interfaces, at the same distance, that each have a converter.
     */
    public static final class AmbiguousConverterException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        AmbiguousConverterException(Class<?> type, List<Class<?>> candidates) {
            super("converter for type " + type.getName() + " is ambiguous: candidates "
                    + candidates.stream().map(Class::getName).toList());
        }
    }
}
