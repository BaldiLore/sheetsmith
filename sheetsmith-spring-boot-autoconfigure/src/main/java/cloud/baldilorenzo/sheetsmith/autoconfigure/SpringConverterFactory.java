package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Objects;

/**
 * Creates field-level converters from the Spring context.
 * <p>
 * If exactly one bean of the converter class exists, it is used. Otherwise the converter is created with
 * {@link AutowireCapableBeanFactory#createBean(Class)}, so it receives dependencies through its constructor
 * without being a bean, and therefore without being registered for its type application-wide.
 */
public class SpringConverterFactory implements CellConverterFactory {

    private final ConfigurableListableBeanFactory beanFactory;

    /**
     * Creates the factory.
     *
     * @param beanFactory the bean factory of the application context
     */
    public SpringConverterFactory(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory");
    }

    /**
     * Returns the only bean of the converter class, or creates a new instance with dependency injection.
     *
     * @param converterClass the converter class
     * @param <C>            the converter type
     * @return the converter
     */
    @Override
    public <C extends CellConverter<?>> C create(Class<C> converterClass) {
        Objects.requireNonNull(converterClass, "converterClass");
        String[] names = beanFactory.getBeanNamesForType(converterClass);
        if (names.length == 1) {
            return beanFactory.getBean(names[0], converterClass);
        }
        return beanFactory.createBean(converterClass);
    }
}
