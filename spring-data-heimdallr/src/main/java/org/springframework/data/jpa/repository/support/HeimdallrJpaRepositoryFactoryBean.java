package org.springframework.data.jpa.repository.support;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;

/**
 * @author 敲代码的旺财
 * @date 2022/4/18 15:30
 */
public class HeimdallrJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

    private @Nullable
    EntityManager entityManager;
    private EntityPathResolver entityPathResolver;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private JpaQueryMethodFactory queryMethodFactory;


    public HeimdallrJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {

        JpaRepositoryFactory jpaRepositoryFactory = new HeimdallrJpaRepositoryFactory(entityManager);
        jpaRepositoryFactory.setEntityPathResolver(entityPathResolver);
        jpaRepositoryFactory.setEscapeCharacter(escapeCharacter);

        if (queryMethodFactory != null) {
            jpaRepositoryFactory.setQueryMethodFactory(queryMethodFactory);
        }

        return jpaRepositoryFactory;
    }


    @Autowired
    public void setEntityPathResolver(ObjectProvider<EntityPathResolver> resolver) {
        this.entityPathResolver = resolver.getIfAvailable(() -> SimpleEntityPathResolver.INSTANCE);
    }

    @Autowired
    public void setQueryMethodFactory(@Nullable JpaQueryMethodFactory factory) {
        if (factory != null) {
            this.queryMethodFactory = factory;
        }
    }

    public void setEscapeCharacter(char escapeCharacter) {
        this.escapeCharacter = EscapeCharacter.of(escapeCharacter);
    }

}
