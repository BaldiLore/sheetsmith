package cloud.baldilorenzo.sheetsmith.internal.convert;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Creates converters through their public no-argument constructor.
 */
public final class ReflectiveConverterFactory implements CellConverterFactory {

    /**
     * Creates the factory.
     */
    public ReflectiveConverterFactory() {
    }

    /**
     * Creates a converter through its public no-argument constructor.
     *
     * @param converterClass the converter class
     * @param <C>            the converter type
     * @return a new converter
     * @throws IllegalArgumentException if the class has no accessible public no-argument constructor, or the
     *                                  constructor fails
     */
    @Override
    public <C extends CellConverter<?>> C create(Class<C> converterClass) {
        Objects.requireNonNull(converterClass, "converterClass");
        try {
            return converterClass.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(converterClass.getName() + " has no public no-argument constructor", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("constructor of " + converterClass.getName() + " failed: "
                    + e.getCause(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("cannot instantiate " + converterClass.getName() + ": " + e, e);
        }
    }
}
