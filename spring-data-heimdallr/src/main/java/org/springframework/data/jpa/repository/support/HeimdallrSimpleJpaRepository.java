package org.springframework.data.jpa.repository.support;

import io.github.guoyixing.heimdallr.strategy.SecurityLevelSpecification;
import io.github.guoyixing.heimdallr.support.SecurityLevelManager;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class HeimdallrSimpleJpaRepository<T, ID> extends SimpleJpaRepository<T, ID> {

    private final Class<?> tClass;

    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

    @Override
    public void setEscapeCharacter(EscapeCharacter escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public HeimdallrSimpleJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.tClass = entityInformation.getJavaType();
    }

    public HeimdallrSimpleJpaRepository(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        tClass = domainClass;
    }

    @Override
    public Optional<T> findById(ID id) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = SecurityLevelSpecification.getSpecification(tClass, id);
            return super.findOne(specification);
        }
        return super.findById(id);
    }

    @Deprecated
    @Override
    public T getOne(ID id) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = SecurityLevelSpecification.getSpecification(tClass, id);
            return super.findOne(specification).get();
        }
        return super.getOne(id);
    }

    @Deprecated
    @Override
    public T getById(ID id) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = SecurityLevelSpecification.getSpecification(tClass, id);
            return super.findOne(specification).get();
        }
        return super.getById(id);
    }

    @Override
    public List<T> findAll() {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = SecurityLevelSpecification.getSpecification(tClass);
            return super.findAll(specification);
        }
        return super.findAll();
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = SecurityLevelSpecification.getSpecification(tClass, ids);
            return super.findAll(specification);
        }
        return super.findAllById(ids);
    }

    @Override
    public List<T> findAll(Sort sort) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = SecurityLevelSpecification.getSpecification(tClass);
            return super.findAll(specification, sort);
        }
        return super.findAll(sort);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = SecurityLevelSpecification.getSpecification(tClass);
            return super.findAll(specification, pageable);
        }
        return super.findAll(pageable);
    }

    @Override
    public Optional<T> findOne(@Nullable Specification<T> spec) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = getSpecification(spec);
            return super.findOne(specification);
        }
        return super.findOne(spec);
    }


    @Override
    public List<T> findAll(@Nullable Specification<T> spec) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = getSpecification(spec);
            return super.findAll(specification);
        }
        return super.findAll(spec);
    }

    @Override
    public Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = getSpecification(spec);
            return super.findAll(specification, pageable);
        }
        return super.findAll(spec, pageable);
    }

    @Override
    public List<T> findAll(@Nullable Specification<T> spec, Sort sort) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<T> specification = SecurityLevelSpecification.getSpecification(tClass);
            return super.findAll(specification, sort);
        }
        return super.findAll(spec, sort);
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<S> specification = SecurityLevelSpecification.getSpecification(tClass
                    , new ExampleSpecification<>(example, escapeCharacter));
            try {
                return Optional.of(getQuery(specification
                        , example.getProbeType()
                        , Sort.unsorted()).getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        }
        return super.findOne(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<S> specification = SecurityLevelSpecification.getSpecification(tClass
                    , new ExampleSpecification<>(example, escapeCharacter));
            return getQuery(specification, example.getProbeType(), Sort.unsorted())
                    .getResultList();
        }
        return super.findAll(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<S> specification = SecurityLevelSpecification.getSpecification(tClass
                    , new ExampleSpecification<>(example, escapeCharacter));
            return getQuery(specification, example.getProbeType(), sort)
                    .getResultList();
        }
        return super.findAll(example, sort);
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        if (SecurityLevelManager.getInstance().securityLevelOnOff()) {
            Specification<S> spec = SecurityLevelSpecification.getSpecification(tClass
                    , new ExampleSpecification<>(example, escapeCharacter));
            Class<S> probeType = example.getProbeType();
            TypedQuery<S> query = getQuery(SecurityLevelSpecification.getSpecification(tClass
                    , new ExampleSpecification<>(example, escapeCharacter)), probeType, pageable);

            return pageable.isUnpaged() ? new PageImpl<>(query.getResultList()) : readPage(query, probeType, pageable, spec);
        }
        return super.findAll(example, pageable);
    }


    private Specification<T> getSpecification(@Nullable Specification<T> spec) {
        Specification<T> specification;
        if (spec == null) {
            specification = SecurityLevelSpecification.getSpecification(tClass);
        } else {
            specification = SecurityLevelSpecification.getSpecification(tClass, spec);
        }
        return specification;
    }

    static class ExampleSpecification<T> implements Specification<T> {
        private static final long serialVersionUID = 1L;
        private final Example<T> example;
        private final EscapeCharacter escapeCharacter;

        ExampleSpecification(Example<T> example, EscapeCharacter escapeCharacter) {
            Assert.notNull(example, "Example must not be null!");
            Assert.notNull(escapeCharacter, "EscapeCharacter must not be null!");
            this.example = example;
            this.escapeCharacter = escapeCharacter;
        }

        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return QueryByExamplePredicateBuilder.getPredicate(root, cb, this.example, this.escapeCharacter);
        }
    }

}