/*
 * Copyright 2012-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.repository.config;

import com.github.guoyixing.heimdallr.support.DefaultPermissionGetter;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.support.HeimdallrJpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import java.lang.annotation.*;

/**
 * 注解启动jap，在默认情况下，将会扫描包下的配置注解
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(HeimdallrJpaRepositoriesRegistrar.class)
public @interface EnableHeimdallrJpaRepositories {

    /**
     * 与{@link #basePackages()}相同
     */
    String[] value() default {};

    /**
     * 扫描的包路径
     * 也可以使用{@link #basePackageClasses()}基于类的方式扫描
     */
    String[] basePackages() default {};

    /**
     * 基于类的扫描方式
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * 从已经扫描的对象中进一步筛选对象
     */
    Filter[] includeFilters() default {};

    /**
     * 配除吊不需要的对象，从扫描的对象总排除一些对象
     */
    Filter[] excludeFilters() default {};

    /**
     * 设置自定义“repository”时，对应的实现类的名称
     * 如果自定义的“repository”，叫做“PersonRepository”，其实现类叫做“PersonRepositoryImpl”
     */
    String repositoryImplementationPostfix() default "Impl";

    /**
     * 指定命名查询的配置文件位置
     * 默认位置在META-INF/jpa-named-queries.properties
     */
    String namedQueriesLocation() default "";

    /**
     * 查询方法的查找策略
     * 默认为{@link Key#CREATE_IF_NOT_FOUND}
     * <p>
     * 一共有3个值
     * {@link Key#CREATE} 直接根据方法名进行创建
     * {@link Key#USE_DECLARED_QUERY} 通过声明的方式查找
     * {@link Key#CREATE_IF_NOT_FOUND} 上面两种的结合，先查找，找不到再创建
     */
    Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

    /**
     * 指定jpa的库工厂，用于创建jpa的”repository“实例
     * 默认为{@link JpaRepositoryFactoryBean}
     */
    Class<?> repositoryFactoryBeanClass() default HeimdallrJpaRepositoryFactoryBean.class;

    /**
     * 指定在创建”repository“代理时所使用的基类
     * 默认为{@link DefaultRepositoryBaseClass}
     */
    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    // JPA specific configuration

    /**
     * 配置EntityManagerFactory bean的名称
     * 默认叫做“entityManagerFactory”
     */
    String entityManagerFactoryRef() default "entityManagerFactory";

    /**
     * 配置PlatformTransactionManager bean的名称
     * 默认叫做“transactionManager”
     */
    String transactionManagerRef() default "transactionManager";

    /**
     * 是否进行嵌套查询，是否去发现嵌套的Repository接口，如定义为内部类
     * 默认为false
     */
    boolean considerNestedRepositories() default false;

    /**
     * 指定是否启动默认的事物
     * 默认为true
     * <p>
     * 如果为false,需要使用facade(外观模式？？)，看官网的意思就是在service层使用@Transactional注解，或者给“repository”中的方法添加事物
     */
    boolean enableDefaultTransactions() default true;

    /**
     * 配置“repository”什么时候被初始化
     * 默认值为{@link BootstrapMode#DEFAULT}
     * <p>
     * 一共三个值
     * {@link BootstrapMode#DEFAULT} 立刻初始化，除非使用了@Lazy注解
     * {@link BootstrapMode#LAZY}惰性初始化，只有第一次使用的时候才会进行初始化
     * {@link BootstrapMode#DEFERRED} 延迟初始化，与lazy很像，但是在程序上下文初始化完毕之后，会进行“repository”初始化
     *
     * @since 2.1
     */
    BootstrapMode bootstrapMode() default BootstrapMode.DEFAULT;

    /**
     * 指定转义符
     * 当通过方法名创建查询语句的时候，碰到
     * IsStartingWith, StartingWith, StartsWith, IsEndingWith, EndingWith, EndsWith, IsNotContaining, NotContaining, NotContains, IsContaining, Containing, Contains
     * 这些方法中有 _ 和 % 时会使用此转义符进行转义
     */
    char escapeCharacter() default '\\';

    Class<?> permissionGetter() default DefaultPermissionGetter.class;
}
