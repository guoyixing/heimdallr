package org.springframework.data.jpa.repository.support;

import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.DefaultJpaQueryMethodFactory;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.HeimdallrJpaQueryLookupStrategy;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.util.Optional;

/**
 * @author 敲代码的旺财
 * @date 2022/4/18 15:40
 */
public class HeimdallrJpaRepositoryFactory extends JpaRepositoryFactory {

    private final EntityManager entityManager;

    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

    private JpaQueryMethodFactory queryMethodFactory;

    public HeimdallrJpaRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
        this.queryMethodFactory = new DefaultJpaQueryMethodFactory(PersistenceProvider.fromEntityManager(entityManager));
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key,
                                                                   QueryMethodEvaluationContextProvider evaluationContextProvider) {

        return Optional.of(HeimdallrJpaQueryLookupStrategy.create(entityManager, queryMethodFactory, key, evaluationContextProvider,
                escapeCharacter));
    }


    public void setEscapeCharacter(EscapeCharacter escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public void setQueryMethodFactory(JpaQueryMethodFactory queryMethodFactory) {

        Assert.notNull(queryMethodFactory, "QueryMethodFactory must not be null!");

        this.queryMethodFactory = queryMethodFactory;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return HeimdallrSimpleJpaRepository.class;
    }

}
