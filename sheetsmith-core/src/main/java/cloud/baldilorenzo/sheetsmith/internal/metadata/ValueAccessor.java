package cloud.baldilorenzo.sheetsmith.internal.metadata;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

/**
 * Reads the value of a column from an instance of the exported class, through a {@link MethodHandle}.
 */
public final class ValueAccessor {

    private static final MethodType TYPE = MethodType.methodType(Object.class, Object.class);

    private final MethodHandle handle;
    private final String description;

    /**
     * Creates an accessor from a handle taking the instance and returning the value.
     *
     * @param handle      the handle, with one parameter and a non-void return type
     * @param description how the value is read, for diagnostics
     */
    public ValueAccessor(MethodHandle handle, String description) {
        this.handle = Objects.requireNonNull(handle, "handle").asType(TYPE);
        this.description = Objects.requireNonNull(description, "description");
    }

    /**
     * Reads the value.
     *
     * @param target the instance of the exported class
     * @return the value, possibly null
     * @throws UndeclaredThrowableException if the getter throws a checked exception
     */
    public Object get(Object target) {
        try {
            return (Object) handle.invokeExact(target);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Describes how the value is read, for example {@code getter getAmount()} or {@code field amount}.
     *
     * @return the description
     */
    @Override
    public String toString() {
        return description;
    }
}
