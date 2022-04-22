package io.github.guoyixing.heimdallr.strategy;

import io.github.guoyixing.heimdallr.support.SecurityLevelField;
import io.github.guoyixing.heimdallr.support.SecurityLevelManager;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 敲代码的旺财
 * @date 2022/4/20 9:45
 */
public class SecurityLevelSpecification {

    public static <T, ID> Specification<T> getSpecification(Class<?> tClass, Iterable<ID> ids) {
        SecurityLevelManager securityLevelManager = SecurityLevelManager.getInstance();
        SecurityLevelField field = securityLevelManager.get(tClass);
        Object[] ownedPermissions = securityLevelManager.getOwnedPermissions();

        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            CriteriaBuilder.In<Object> idList = criteriaBuilder
                    .in(root.get(field.getIdName()));
            for (ID id : ids) {
                idList.value(id);
            }
            predicates.add(criteriaBuilder.and(idList));

            CriteriaBuilder.In<Object> securityLevels = getSecurityLevelIn(ownedPermissions, criteriaBuilder, root.get(field.getFieldName()));
            predicates.add(criteriaBuilder.and(securityLevels));
            Predicate[] predicate = predicates.toArray(new Predicate[0]);
            return criteriaBuilder.and(predicate);
        };
    }

    public static <T> Specification<T> getSpecification(Class<?> tClass, Specification<T> spec) {
        SecurityLevelManager securityLevelManager = SecurityLevelManager.getInstance();
        SecurityLevelField field = securityLevelManager.get(tClass);
        Object[] ownedPermissions = securityLevelManager.getOwnedPermissions();

        Specification<T> securityLevelSpec = getSecurityLevelSpec(field, ownedPermissions);
        return spec.and(securityLevelSpec);
    }

    public static <T, ID> Specification<T> getSpecification(Class<?> tClass, ID id) {
        SecurityLevelManager securityLevelManager = SecurityLevelManager.getInstance();
        SecurityLevelField field = securityLevelManager.get(tClass);
        Object[] ownedPermissions = securityLevelManager.getOwnedPermissions();

        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get(field.getIdName()), id));

            CriteriaBuilder.In<Object> securityLevels = getSecurityLevelIn(ownedPermissions, criteriaBuilder, root.get(field.getFieldName()));
            predicates.add(criteriaBuilder.and(securityLevels));
            Predicate[] predicate = predicates.toArray(new Predicate[0]);
            return criteriaBuilder.and(predicate);
        };
    }

    public static <T> Specification<T> getSpecification(Class<?> tClass) {
        SecurityLevelManager securityLevelManager = SecurityLevelManager.getInstance();
        SecurityLevelField field = securityLevelManager.get(tClass);
        Object[] ownedPermissions = securityLevelManager.getOwnedPermissions();

        return getSecurityLevelSpec(field, ownedPermissions);
    }

    private static <T> Specification<T> getSecurityLevelSpec(SecurityLevelField field, Object[] ownedPermissions) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            CriteriaBuilder.In<Object> securityLevels = getSecurityLevelIn(ownedPermissions
                    , criteriaBuilder
                    , root.get(field.getFieldName()));
            predicates.add(criteriaBuilder.and(securityLevels));
            Predicate[] predicate = predicates.toArray(new Predicate[0]);
            return criteriaBuilder.and(predicate);
        };
    }

    private static CriteriaBuilder.In<Object> getSecurityLevelIn(Object[] ownedPermissions, CriteriaBuilder criteriaBuilder, Path<Object> objectPath) {
        CriteriaBuilder.In<Object> securityLevels = criteriaBuilder
                .in(objectPath);
        for (Object securityLevel : ownedPermissions) {
            securityLevels.value(securityLevel);
        }
        return securityLevels;
    }

}
