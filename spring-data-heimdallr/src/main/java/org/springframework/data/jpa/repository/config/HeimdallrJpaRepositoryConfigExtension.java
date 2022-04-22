package org.springframework.data.jpa.repository.config;

import com.github.guoyixing.heimdallr.support.DefaultPermissionGetter;
import com.github.guoyixing.heimdallr.support.PermissionGetter;
import com.github.guoyixing.heimdallr.support.SecurityLevelManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfiguration;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AbstractRepositoryMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author 敲代码的旺财
 * @date 2022/4/19 13:22
 */
public class HeimdallrJpaRepositoryConfigExtension extends JpaRepositoryConfigExtension {
    private static final Log logger = LogFactory.getLog(JpaRepositoryConfigExtension.class);

    private static final String PERMISSION_GETTER_ATTRIBUTE = "permissionGetter";

    @Override
    public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
        super.postProcess(builder, source);
        SecurityLevelManager.getInstance().setPermissionGetter(getPermissionGetter(source));
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
        super.postProcess(builder, config);
        SecurityLevelManager.getInstance().setPermissionGetter(getPermissionGetter(config));
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {
        super.postProcess(builder, config);
        SecurityLevelManager.getInstance().setPermissionGetter(getPermissionGetter(config));
    }

    private PermissionGetter getPermissionGetter(RepositoryConfigurationSource source) {
        Optional<Class> attribute = source.getAttribute(PERMISSION_GETTER_ATTRIBUTE, Class.class);
        Class<DefaultPermissionGetter> clazz = attribute.orElse(DefaultPermissionGetter.class);
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("permissionGetter必须有空构造器", e);
        }
    }


    public <T extends RepositoryConfigurationSource> Collection<RepositoryConfiguration<T>> getRepositoryConfigurations(
            T configSource, ResourceLoader loader, boolean strictMatchesOnly) {

        Assert.notNull(configSource, "ConfigSource must not be null!");
        Assert.notNull(loader, "Loader must not be null!");

        Set<RepositoryConfiguration<T>> result = new HashSet<>();

        for (BeanDefinition candidate : configSource.getCandidates(loader)) {

            RepositoryConfiguration<T> configuration = getRepositoryConfiguration(candidate, configSource);
            Class<?> repositoryInterface = loadRepositoryInterface(configuration,
                    getConfigurationInspectionClassLoader(loader));

            if (repositoryInterface == null) {
                result.add(configuration);
                continue;
            }

            RepositoryMetadata metadata = AbstractRepositoryMetadata.getMetadata(repositoryInterface);

            SecurityLevelManager.getInstance().put(metadata.getDomainType());

            boolean qualifiedForImplementation = !strictMatchesOnly || configSource.usesExplicitFilters()
                    || isStrictRepositoryCandidate(metadata);

            if (qualifiedForImplementation && useRepositoryConfiguration(metadata)) {
                result.add(configuration);
            }
        }

        return result;
    }

    @Nullable
    private Class<?> loadRepositoryInterface(RepositoryConfiguration<?> configuration, @Nullable ClassLoader classLoader) {
        String repositoryInterface = configuration.getRepositoryInterface();

        try {
            return ClassUtils.forName(repositoryInterface, classLoader);
        } catch (LinkageError | ClassNotFoundException var5) {
            logger.warn(String.format("%s - Could not load type %s using class loader %s.", this.getModuleName(), repositoryInterface, classLoader), var5);
            return null;
        }
    }

}
