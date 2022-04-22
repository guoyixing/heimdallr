package org.springframework.data.jpa.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * 通过ImportBeanDefinitionRegistrar(Import形式的bean定义注册器)来启用{@link EnableHeimdallrJpaRepositories}注解
 *
 * @author Oliver Gierke
 */
class HeimdallrJpaRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableHeimdallrJpaRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new HeimdallrJpaRepositoryConfigExtension();
    }
}