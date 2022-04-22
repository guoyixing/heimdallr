package io.github.guoyixing.heimdallr.support;


import lombok.Getter;
import lombok.Setter;

/**
 * @author 敲代码的旺财
 * @date 2022/4/19 13:40
 */
@Getter
@Setter
public class SecurityLevelField {

    private Class<?> sourceClass;

    private Class<?> fieldClass;

    private String fieldName;

    private String dbFieldName;

    private String idName;

    private String dbIdName;

}
