package org.springframework.data.jpa.repository.query;

import com.github.guoyixing.heimdallr.support.SecurityLevelField;
import com.github.guoyixing.heimdallr.support.SecurityLevelManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 敲代码的旺财
 * @date 2022/4/21 12:16
 */
public class HeimdallrPartTreeJpaQuery extends PartTreeJpaQuery {

    private final PartTree tree;
    private final JpaParameters parameters;

    private final QueryPreparer query;
    private final QueryPreparer countQuery;
    private final EntityManager em;
    private final EscapeCharacter escape;
    private final Class<?> domainClass;

    /**
     * Creates a new {@link PartTreeJpaQuery}.
     *
     * @param method must not be {@literal null}.
     * @param em     must not be {@literal null}.
     */
    HeimdallrPartTreeJpaQuery(JpaQueryMethod method, EntityManager em) {
        this(method, em, EscapeCharacter.DEFAULT);
    }

    /**
     * Creates a new {@link PartTreeJpaQuery}.
     *
     * @param method must not be {@literal null}.
     * @param em     must not be {@literal null}.
     * @param escape character used for escaping characters used as patterns in LIKE-expressions.
     */
    HeimdallrPartTreeJpaQuery(JpaQueryMethod method, EntityManager em, EscapeCharacter escape) {

        super(method, em);

        this.em = em;
        this.escape = escape;
        this.domainClass = method.getEntityInformation().getJavaType();
        this.parameters = method.getParameters();

        boolean recreationRequired = parameters.hasDynamicProjection() || parameters.potentiallySortsDynamically();

        try {
            this.tree = new PartTree(method.getName(), domainClass);
            validate(tree, parameters, method.toString());
            this.countQuery = new CountQueryPreparer(recreationRequired);
            this.query = tree.isCountProjection() ? countQuery : new QueryPreparer(recreationRequired);

        } catch (Exception o_O) {
            throw new IllegalArgumentException(
                    String.format("Failed to create query for method %s! %s", method, o_O.getMessage()), o_O);
        }
    }

    @Override
    public Query doCreateQuery(JpaParametersParameterAccessor accessor) {
        return query.createQuery(accessor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypedQuery<Long> doCreateCountQuery(JpaParametersParameterAccessor accessor) {
        return (TypedQuery<Long>) countQuery.createQuery(accessor);
    }

    @Override
    protected JpaQueryExecution getExecution() {

        if (this.tree.isDelete()) {
            return new JpaQueryExecution.DeleteExecution(em);
        } else if (this.tree.isExistsProjection()) {
            return new JpaQueryExecution.ExistsExecution();
        }

        return super.getExecution();
    }

    private static void validate(PartTree tree, JpaParameters parameters, String methodName) {

        int argCount = 0;

        Iterable<Part> parts = () -> tree.stream().flatMap(Streamable::stream).iterator();

        for (Part part : parts) {

            int numberOfArguments = part.getNumberOfArguments();

            for (int i = 0; i < numberOfArguments; i++) {

                throwExceptionOnArgumentMismatch(methodName, part, parameters, argCount);

                argCount++;
            }
        }
    }

    private static void throwExceptionOnArgumentMismatch(String methodName, Part part, JpaParameters parameters,
                                                         int index) {

        Part.Type type = part.getType();
        String property = part.getProperty().toDotPath();

        if (!parameters.getBindableParameters().hasParameterAt(index)) {
            throw new IllegalStateException(String.format(
                    "Method %s expects at least %d arguments but only found %d. This leaves an operator of type %s for property %s unbound.",
                    methodName, index + 1, index, type.name(), property));
        }

        JpaParameters.JpaParameter parameter = parameters.getBindableParameter(index);

        if (expectsCollection(type) && !parameterIsCollectionLike(parameter)) {
            throw new IllegalStateException(wrongParameterTypeMessage(methodName, property, type, "Collection", parameter));
        } else if (!expectsCollection(type) && !parameterIsScalarLike(parameter)) {
            throw new IllegalStateException(wrongParameterTypeMessage(methodName, property, type, "scalar", parameter));
        }
    }

    private static String wrongParameterTypeMessage(String methodName, String property, Part.Type operatorType,
                                                    String expectedArgumentType, JpaParameters.JpaParameter parameter) {

        return String.format("Operator %s on %s requires a %s argument, found %s in method %s.", operatorType.name(),
                property, expectedArgumentType, parameter.getType(), methodName);
    }

    private static boolean parameterIsCollectionLike(JpaParameters.JpaParameter parameter) {
        return Iterable.class.isAssignableFrom(parameter.getType()) || parameter.getType().isArray();
    }

    /**
     * Arrays are may be treated as collection like or in the case of binary data as scalar
     */
    private static boolean parameterIsScalarLike(JpaParameters.JpaParameter parameter) {
        return !Iterable.class.isAssignableFrom(parameter.getType());
    }

    private static boolean expectsCollection(Part.Type type) {
        return type == Part.Type.IN || type == Part.Type.NOT_IN;
    }


    /**
     * Query preparer to create {@link CriteriaQuery} instances and potentially cache them.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class QueryPreparer {

        private final @Nullable
        CriteriaQuery<?> cachedCriteriaQuery;
        private final @Nullable
        ParameterBinder cachedParameterBinder;
        private final QueryParameterSetter.QueryMetadataCache metadataCache = new QueryParameterSetter.QueryMetadataCache();

        QueryPreparer(boolean recreateQueries) {

            JpaQueryCreator creator = createCreator(null);

            if (recreateQueries) {
                this.cachedCriteriaQuery = null;
                this.cachedParameterBinder = null;
            } else {
                this.cachedCriteriaQuery = creator.createQuery();
                this.cachedParameterBinder = getBinder(creator.getParameterExpressions());
            }
        }

        /**
         * Creates a new {@link Query} for the given parameter values.
         */
        public Query createQuery(JpaParametersParameterAccessor accessor) {

            CriteriaQuery<?> criteriaQuery = cachedCriteriaQuery;
            ParameterBinder parameterBinder = cachedParameterBinder;

            if (cachedCriteriaQuery == null || accessor.hasBindableNullValue()) {
                JpaQueryCreator creator = createCreator(accessor);
                criteriaQuery = creator.createQuery(getDynamicSort(accessor));
                List<ParameterMetadataProvider.ParameterMetadata<?>> expressions = creator.getParameterExpressions();
                parameterBinder = getBinder(expressions);
            }

            if (parameterBinder == null) {
                throw new IllegalStateException("ParameterBinder is null!");
            }

            TypedQuery<?> query = createQuery(criteriaQuery);

            return restrictMaxResultsIfNecessary(invokeBinding(parameterBinder, query, accessor, this.metadataCache));
        }

        /**
         * Restricts the max results of the given {@link Query} if the current {@code tree} marks this {@code query} as
         * limited.
         */
        @SuppressWarnings("ConstantConditions")
        private Query restrictMaxResultsIfNecessary(Query query) {

            if (tree.isLimiting()) {

                if (query.getMaxResults() != Integer.MAX_VALUE) {
                    /*
                     * In order to return the correct results, we have to adjust the first result offset to be returned if:
                     * - a Pageable parameter is present
                     * - AND the requested page number > 0
                     * - AND the requested page size was bigger than the derived result limitation via the First/Top keyword.
                     */
                    if (query.getMaxResults() > tree.getMaxResults() && query.getFirstResult() > 0) {
                        query.setFirstResult(query.getFirstResult() - (query.getMaxResults() - tree.getMaxResults()));
                    }
                }

                query.setMaxResults(tree.getMaxResults());
            }

            if (tree.isExistsProjection()) {
                query.setMaxResults(1);
            }

            return query;
        }

        /**
         * Checks whether we are working with a cached {@link CriteriaQuery} and synchronizes the creation of a
         * {@link TypedQuery} instance from it. This is due to non-thread-safety in the {@link CriteriaQuery} implementation
         * of some persistence providers (i.e. Hibernate in this case), see DATAJPA-396.
         *
         * @param criteriaQuery must not be {@literal null}.
         */
        private TypedQuery<?> createQuery(CriteriaQuery<?> criteriaQuery) {

            SecurityLevelManager instance = SecurityLevelManager.getInstance();


            if (this.cachedCriteriaQuery != null) {
                synchronized (this.cachedCriteriaQuery) {
                    if (instance.securityLevelOnOff(domainClass)) {
                        return setSecurityLevel(criteriaQuery, instance);
                    } else {
                        return getEntityManager().createQuery(criteriaQuery);
                    }
                }
            }


            if (instance.securityLevelOnOff(domainClass)) {
                return setSecurityLevel(criteriaQuery, instance);
            } else {
                return getEntityManager().createQuery(criteriaQuery);
            }
        }

        private TypedQuery<?> setSecurityLevel(CriteriaQuery<?> criteriaQuery, SecurityLevelManager instance) {
            SecurityLevelField field = instance.get(domainClass);
            Predicate restriction = criteriaQuery.getRestriction();


            ArrayList<Root<?>> roots = new ArrayList<>(criteriaQuery.getRoots());
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaBuilder.In<Object> securityLevels = criteriaBuilder.in(roots.get(0).get(field.getFieldName()));
            for (Object securityLevel : instance.getOwnedPermissions()) {
                securityLevels.value(securityLevel);
            }
            criteriaQuery.where(restriction, securityLevels);
            TypedQuery<?> query = getEntityManager().createQuery(criteriaQuery);
            return query;
        }

        protected JpaQueryCreator createCreator(@Nullable JpaParametersParameterAccessor accessor) {

            EntityManager entityManager = getEntityManager();

            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            ResultProcessor processor = getQueryMethod().getResultProcessor();

            ParameterMetadataProvider provider;
            ReturnedType returnedType;

            if (accessor != null) {
                provider = new ParameterMetadataProvider(builder, accessor, escape);
                returnedType = processor.withDynamicProjection(accessor).getReturnedType();
            } else {
                provider = new ParameterMetadataProvider(builder, parameters, escape);
                returnedType = processor.getReturnedType();
            }

            return new JpaQueryCreator(tree, returnedType, builder, provider);
        }

        /**
         * Invokes parameter binding on the given {@link TypedQuery}.
         */
        protected Query invokeBinding(ParameterBinder binder, TypedQuery<?> query, JpaParametersParameterAccessor accessor,
                                      QueryParameterSetter.QueryMetadataCache metadataCache) {

            QueryParameterSetter.QueryMetadata metadata = metadataCache.getMetadata("query", query);

            return binder.bindAndPrepare(query, metadata, accessor);
        }

        private ParameterBinder getBinder(List<ParameterMetadataProvider.ParameterMetadata<?>> expressions) {

            ParameterBinder parameterBinder = ParameterBinderFactory.createCriteriaBinder(parameters, expressions);
            return parameterBinder;
        }

        private Sort getDynamicSort(JpaParametersParameterAccessor accessor) {

            return parameters.potentiallySortsDynamically() //
                    ? accessor.getSort() //
                    : Sort.unsorted();
        }
    }

    /**
     * Special {@link QueryPreparer} to create count queries.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class CountQueryPreparer extends QueryPreparer {

        CountQueryPreparer(boolean recreateQueries) {
            super(recreateQueries);
        }

        @Override
        protected JpaQueryCreator createCreator(@Nullable JpaParametersParameterAccessor accessor) {

            EntityManager entityManager = getEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();

            ParameterMetadataProvider provider;

            if (accessor != null) {
                provider = new ParameterMetadataProvider(builder, accessor, escape);
            } else {
                provider = new ParameterMetadataProvider(builder, parameters, escape);
            }

            return new JpaCountQueryCreator(tree, getQueryMethod().getResultProcessor().getReturnedType(), builder, provider);
        }

        /**
         * Customizes binding by skipping the pagination.
         */
        @Override
        protected Query invokeBinding(ParameterBinder binder, TypedQuery<?> query, JpaParametersParameterAccessor accessor,
                                      QueryParameterSetter.QueryMetadataCache metadataCache) {

            QueryParameterSetter.QueryMetadata metadata = metadataCache.getMetadata("countquery", query);

            return binder.bind(query, metadata, accessor);
        }
    }

}
