# Spring-data-Jpa的增强插件
通过maven引入插件
```xml
<dependency>
  <groupId>io.github.guoyixing</groupId>
  <artifactId>spring-data-heimdallr</artifactId>
  <version>0.2</version>
</dependency>
```

## 一、功能
### 1、查询的时候根据用户的权限（或者指定安全等级）自动过滤数据
#### （1）基本使用
1. 使用@EnableHeimdallrJpaRepositories注解替换掉springdata原生的@EnableJpaRepositories注解
2. 在实体类需要作为安全等级的字段上添加@SecurityLevel注解
3. 实现PermissionGetter接口，并在@EnableHeimdallrJpaRepositories中的permissionGetter属性上指明实现类

#### （2）临时修改权限或者关闭权限查询的方法
1. 修改权限
    ```java
    SecurityLevelManager.getInstance().currentSecurityLevel(新的权限);
    ```
2. 关闭权限
    ```java
    SecurityLevelManager.getInstance().offSecurityLevel();
    ```
   
#### （3）说明
1. 功能支持Spring-data-Jpa中的方法名称的查询方式
2. 功能支持JpaRepository<T, ID>和JpaSpecificationExecutor<T>中的查询方法
   具体清单如下
   ```java
    Optional<T> findById(ID id);

    T getOne(ID id);

    T getById(ID id);

    List<T> findAll();

    List<T> findAllById(Iterable<ID> ids);

    List<T> findAll(Sort sort);

    Page<T> findAll(Pageable pageable);

    Optional<T> findOne(@Nullable Specification<T> spec);

    List<T> findAll(@Nullable Specification<T> spec);

    Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable);

    List<T> findAll(@Nullable Specification<T> spec, Sort sort);

    <S extends T> Optional<S> findOne(Example<S> example);

    <S extends T> List<S> findAll(Example<S> example);

    <S extends T> List<S> findAll(Example<S> example, Sort sort);

    <S extends T> Page<S> findAll(Example<S> example, Pageable pageable);
   ```
3. 不支持@Query注解，jpa中使用到@Query多半是非常复杂的查询，强行修改sql很容易出现问题
4. 不支持@QueryHints
5. @SecurityLevel注解支持大部分的基础数据类型和对应的包装类，如果使用枚举类型需要额外增加@Enumerated(EnumType.STRING)注解将注解的保存转成Sting类型
