package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellConverterFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Objects;

/**
 * Creates field converters from the Spring application context. The auto-configured
 * {@link cloud.baldilorenzo.sheetsmith.Sheetsmith} bean uses it.
 * <p>
 * For a converter class declared with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#converter()}:
 * <ul>
 *   <li>if exactly one bean of that class exists, it is used;</li>
 *   <li>otherwise, with no bean or several, a new instance is created with
 *       {@link AutowireCapableBeanFactory#createBean(Class)}: it receives its dependencies through its constructor
 *       without being registered as a bean, and therefore without being registered as an application converter
 *       for its type.</li>
 * </ul>
 * The generator calls the factory once per converter class and reuses the converter. A converter declared as a
 * bean is also registered application-wide by {@link SheetsmithAutoConfiguration}: a converter meant for one column
 * only should not be a bean, and still receives its dependencies.
 *
 * @see CellConverterFactory
 * @see SheetsmithAutoConfiguration
 * @since 1.0.0
 */
public class SpringConverterFactory implements CellConverterFactory {

    private final ConfigurableListableBeanFactory beanFactory;

    /**
     * Creates the factory.
     *
     * @param beanFactory the bean factory of the application context, not null
     * @throws NullPointerException if {@code beanFactory} is null
     */
    public SpringConverterFactory(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory");
    }

    /**
     * Returns the only bean of the converter class, or creates a new instance with dependency injection.
     *
     * @param converterClass the converter class, not null
     * @param <C>            the converter type
     * @return the converter
     * @throws org.springframework.beans.BeansException if the converter cannot be created; the generator reports
     *                                                  it as a violation of rule V-12
     * @throws NullPointerException                     if {@code converterClass} is null
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
