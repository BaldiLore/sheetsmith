package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.Sheetsmith;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheetTemplate;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Validates the sheet classes and the sheet templates of the configured packages when the application starts.
 * <p>
 * Registered by {@link SheetsmithAutoConfiguration} when {@code sheetsmith.validation.packages} is not empty. Once
 * all singletons are created, it scans the packages and their subpackages, and calls
 * {@link Sheetsmith#validate(Class)} with the {@link Sheetsmith} bean of the context, so that converter beans are
 * taken into account.
 * <p>
 * Every type annotated directly with {@link ExcelSheet} or {@link ExcelSheetTemplate} is validated: concrete and
 * abstract classes, interfaces, and nested classes, static or not. Annotation types are not validated, even when
 * annotated with {@code @ExcelSheet} or {@code @ExcelSheetTemplate}, and neither are types only meta-annotated with
 * them, through another annotation. A template is validated on its own even when no sheet class references it.
 * <p>
 * The errors of all the invalid types are collected into one {@link SheetsmithConfigurationException}, which stops
 * the application: mistakes in sheet classes and templates surface at startup instead of at the first generation.
 * Each error is reported once, even when several sheet classes reference the same invalid template or style
 * sheet.
 *
 * @see SheetsmithProperties.Validation
 * @since 1.0.0
 */
public class SheetsmithStartupValidator implements SmartInitializingSingleton {

    private final Sheetsmith sheetsmith;
    private final List<String> packages;
    private final Environment environment;
    private final ResourceLoader resourceLoader;

    /**
     * Creates the validator.
     *
     * @param sheetsmith     the generator that validates each class, not null
     * @param packages       the packages to scan, not null
     * @param environment    the application environment, not null
     * @param resourceLoader loads the scanned classes, not null
     * @throws NullPointerException if an argument is null
     */
    public SheetsmithStartupValidator(Sheetsmith sheetsmith, List<String> packages, Environment environment,
                                      ResourceLoader resourceLoader) {
        this.sheetsmith = Objects.requireNonNull(sheetsmith, "sheetsmith");
        this.packages = List.copyOf(packages);
        this.environment = Objects.requireNonNull(environment, "environment");
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader");
    }

    /**
     * Scans the packages and validates every sheet class and sheet template found.
     *
     * @throws SheetsmithConfigurationException listing the errors of every invalid class
     */
    @Override
    public void afterSingletonsInstantiated() {
        // several sheet classes can report the same errors of a shared template or style sheet
        LinkedHashSet<ConfigurationError> errors = new LinkedHashSet<>();
        for (Class<?> type : annotatedClasses()) {
            try {
                sheetsmith.validate(type);
            } catch (SheetsmithConfigurationException e) {
                errors.addAll(e.errors());
            }
        }
        if (!errors.isEmpty()) {
            throw new SheetsmithConfigurationException(List.copyOf(errors));
        }
    }

    private List<Class<?>> annotatedClasses() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, environment) {
                    /** Every type found by the filter, abstract and nested ones included, except annotations. */
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return !beanDefinition.getMetadata().isAnnotation();
                    }
                };
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ExcelSheet.class, false));
        scanner.addIncludeFilter(new AnnotationTypeFilter(ExcelSheetTemplate.class, false));
        TreeSet<String> names = new TreeSet<>();
        for (String basePackage : packages) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                names.add(candidate.getBeanClassName());
            }
        }
        List<Class<?>> classes = new ArrayList<>(names.size());
        for (String name : names) {
            classes.add(ClassUtils.resolveClassName(name, resourceLoader.getClassLoader()));
        }
        return classes;
    }
}
