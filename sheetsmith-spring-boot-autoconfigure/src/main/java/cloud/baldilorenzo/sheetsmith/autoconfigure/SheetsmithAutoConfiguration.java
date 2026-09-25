package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.Sheetsmith;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-configuration of sheetsmith: a {@link Sheetsmith} bean configured from {@link SheetsmithProperties}, with
 * every {@link CellConverter} bean registered for its type, and optional startup validation of annotated classes.
 */
@AutoConfiguration
@ConditionalOnClass(Sheetsmith.class)
@EnableConfigurationProperties(SheetsmithProperties.class)
public class SheetsmithAutoConfiguration {

    /**
     * Creates the auto-configuration.
     */
    public SheetsmithAutoConfiguration() {
    }

    /**
     * Creates the {@link Sheetsmith} bean, unless the application defines one.
     *
     * @param properties  the sheetsmith properties
     * @param beanFactory the bean factory, to find converter beans and create field-level converters
     * @return the generator
     * @throws IllegalStateException    if the type of a converter bean cannot be resolved, or two converter beans
     *                                  handle the same type
     * @throws IllegalArgumentException if a property value is invalid
     */
    @Bean
    @ConditionalOnMissingBean
    public Sheetsmith sheetsmith(SheetsmithProperties properties, ConfigurableListableBeanFactory beanFactory) {
        Sheetsmith.Builder builder = Sheetsmith.builder()
                .defaults(properties.toDefaults())
                .converterFactory(new SpringConverterFactory(beanFactory));
        registerConverterBeans(builder, beanFactory);
        return builder.build();
    }

    /**
     * Creates the startup validator when {@code sheetsmith.validation.packages} is not empty.
     *
     * @param sheetsmith     the generator used to validate
     * @param properties     the sheetsmith properties
     * @param environment    the application environment
     * @param resourceLoader loads the scanned classes
     * @return the validator
     */
    @Bean
    @Conditional(OnValidationPackages.class)
    public SheetsmithStartupValidator sheetsmithStartupValidator(Sheetsmith sheetsmith,
                                                                 SheetsmithProperties properties,
                                                                 Environment environment,
                                                                 ResourceLoader resourceLoader) {
        return new SheetsmithStartupValidator(sheetsmith, properties.validation().packages(), environment,
                resourceLoader);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerConverterBeans(Sheetsmith.Builder builder,
                                               ConfigurableListableBeanFactory beanFactory) {
        Map<String, CellConverter> converters = beanFactory.getBeansOfType(CellConverter.class);
        Map<Class<?>, String> owners = new HashMap<>();
        converters.forEach((name, converter) -> {
            Class<?> type = handledType(name, converter, beanFactory);
            String previous = owners.putIfAbsent(type, name);
            if (previous != null) {
                throw new IllegalStateException("converter beans '" + previous + "' and '" + name
                        + "' both handle type " + type.getName() + "; keep only one of them");
            }
            builder.converter((Class) type, converter);
        });
    }

    /** The type from the bean definition, else from the target class of the bean. */
    private static Class<?> handledType(String name, CellConverter<?> converter,
                                        ConfigurableListableBeanFactory beanFactory) {
        Class<?> type = null;
        if (beanFactory.containsBeanDefinition(name)) {
            type = generic(beanFactory.getMergedBeanDefinition(name).getResolvableType());
        }
        if (type == null) {
            type = generic(ResolvableType.forClass(AopUtils.getTargetClass(converter)));
        }
        if (type == null) {
            throw new IllegalStateException("cannot resolve the type handled by converter bean '" + name
                    + "'; declare it as a class implementing CellConverter<T>, or as a @Bean method returning "
                    + "CellConverter<T>, instead of a lambda or a raw type");
        }
        return type;
    }

    private static Class<?> generic(ResolvableType type) {
        ResolvableType converter = type.as(CellConverter.class);
        return converter == ResolvableType.NONE ? null : converter.getGeneric(0).resolve();
    }

    /** Matches when {@code sheetsmith.validation.packages} is not empty. */
    static final class OnValidationPackages extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            List<String> packages = Binder.get(context.getEnvironment())
                    .bind("sheetsmith.validation.packages", Bindable.listOf(String.class))
                    .orElse(List.of());
            return packages.isEmpty()
                    ? ConditionOutcome.noMatch("sheetsmith.validation.packages is empty")
                    : ConditionOutcome.match("sheetsmith.validation.packages is " + packages);
        }
    }
}
