package org.springframework.data.jpa.repository.query;


import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;

/**
 * @author 敲代码的旺财
 * @date 2022/4/18 18:06
 */
public enum HeimdallrJpaQueryFactory {

    INSTANCE;

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /**
     * Creates a {@link RepositoryQuery} from the given {@link String} query.
     *
     * @param method                    must not be {@literal null}.
     * @param em                        must not be {@literal null}.
     * @param queryString               must not be {@literal null} or empty.
     * @param countQueryString
     * @param evaluationContextProvider
     * @return
     */
    AbstractJpaQuery fromMethodWithQueryString(JpaQueryMethod method, EntityManager em, String queryString,
                                               @Nullable String countQueryString,
                                               QueryMethodEvaluationContextProvider evaluationContextProvider) {

        return method.isNativeQuery()
                ? new NativeJpaQuery(method, em, queryString, countQueryString, evaluationContextProvider, PARSER)
                : new SimpleJpaQuery(method, em, queryString, countQueryString, evaluationContextProvider, PARSER);
    }

    /**
     * Creates a {@link StoredProcedureJpaQuery} from the given {@link JpaQueryMethod} query.
     *
     * @param method must not be {@literal null}.
     * @param em     must not be {@literal null}.
     * @return
     */
    public StoredProcedureJpaQuery fromProcedureAnnotation(JpaQueryMethod method, EntityManager em) {
        return new StoredProcedureJpaQuery(method, em);
    }

}
