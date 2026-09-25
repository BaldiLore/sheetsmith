package cloud.baldilorenzo.sheetsmith.autoconfigure;

import cloud.baldilorenzo.sheetsmith.ConfigurationError;
import cloud.baldilorenzo.sheetsmith.Sheetsmith;
import cloud.baldilorenzo.sheetsmith.SheetsmithConfigurationException;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Validates, once all singletons are created, every class annotated with {@link ExcelSheet} found in the configured
 * packages. Invalid classes stop the application with one {@link SheetsmithConfigurationException} listing the
 * errors of all of them.
 */
public class SheetsmithStartupValidator implements SmartInitializingSingleton {

    private final Sheetsmith sheetsmith;
    private final List<String> packages;
    private final Environment environment;
    private final ResourceLoader resourceLoader;

    /**
     * Creates the validator.
     *
     * @param sheetsmith     validates each class
     * @param packages       the packages to scan
     * @param environment    the application environment
     * @param resourceLoader loads the scanned classes
     */
    public SheetsmithStartupValidator(Sheetsmith sheetsmith, List<String> packages, Environment environment,
                                      ResourceLoader resourceLoader) {
        this.sheetsmith = Objects.requireNonNull(sheetsmith, "sheetsmith");
        this.packages = List.copyOf(packages);
        this.environment = Objects.requireNonNull(environment, "environment");
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader");
    }

    /**
     * Scans the packages and validates every annotated class found.
     *
     * @throws SheetsmithConfigurationException listing the errors of every invalid class
     */
    @Override
    public void afterSingletonsInstantiated() {
        List<ConfigurationError> errors = new ArrayList<>();
        for (Class<?> type : annotatedClasses()) {
            try {
                sheetsmith.validate(type);
            } catch (SheetsmithConfigurationException e) {
                errors.addAll(e.errors());
            }
        }
        if (!errors.isEmpty()) {
            throw new SheetsmithConfigurationException(errors);
        }
    }

    private List<Class<?>> annotatedClasses() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, environment);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ExcelSheet.class));
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
