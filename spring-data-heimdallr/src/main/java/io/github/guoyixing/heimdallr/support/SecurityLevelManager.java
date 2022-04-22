package io.github.guoyixing.heimdallr.support;

import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全等级管理器
 *
 * @author 敲代码的旺财
 * @date 2022/4/19 13:37
 */
public class SecurityLevelManager {
    private volatile static PermissionGetter permissionGetter;

    private final ThreadLocal<Object> currentSecurityLevel;

    private final ThreadLocal<Boolean> securityLevelOnOff;

    private volatile static SecurityLevelManager instance;

    private final Map<Class<?>, SecurityLevelField> securityLevelFieldCache;

    private SecurityLevelManager() {
        currentSecurityLevel = new ThreadLocal<>();
        securityLevelOnOff = new ThreadLocal<>();
        securityLevelFieldCache = new ConcurrentHashMap<>();
    }

    public static SecurityLevelManager getInstance() {
        if (instance == null) {
            synchronized (SecurityLevelManager.class) {
                if (instance == null) {
                    instance = new SecurityLevelManager();
                }
            }
        }
        return instance;
    }

    public Boolean securityLevelOnOff(Class<?> clazz) {
        return securityLevelOnOff() && instance.get(clazz) != null;
    }

    public void currentSecurityLevel(Object permissionGetter) {
        currentSecurityLevel.set(permissionGetter);
    }

    public void offSecurityLevel() {
        securityLevelOnOff.set(Boolean.FALSE);
    }

    public Boolean securityLevelOnOff() {
        if (securityLevelOnOff.get() == null) {
            return Boolean.TRUE;
        }
        return securityLevelOnOff.get();
    }

    public void setPermissionGetter(PermissionGetter permissionGetter) {
        SecurityLevelManager.permissionGetter = permissionGetter;
    }

    public Object[] getOwnedPermissions() {
        Object useSecurityLevel = currentSecurityLevel.get();

        if (useSecurityLevel == null) {
            useSecurityLevel = permissionGetter.getDefaultPermissions();
        }
        if (useSecurityLevel == null) {
            throw new NullPointerException("无法获取到安全等级，尝试使用SecurityLevelManager类进行设置");
        }

        return permissionGetter.getOwnedPermissions(useSecurityLevel);
    }

    public void put(Class<?> clazz) {
        SecurityLevelField securityLevelField = getSecurityLevelField(clazz);

        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }

        for (Field clazzField : fieldList) {
            SecurityLevel securityLevelAnnotation = clazzField.getAnnotation(SecurityLevel.class);
            if (securityLevelAnnotation != null) {
                setSecurityLevel(securityLevelField, clazzField);
            }
            Id idAnnotation = clazzField.getAnnotation(Id.class);
            if (idAnnotation != null) {
                setId(securityLevelField, clazzField);
            }
        }

        //有SecurityLevel注解的才保存
        if (securityLevelField.getFieldClass() != null) {
            securityLevelFieldCache.put(securityLevelField.getSourceClass(), securityLevelField);
        }
    }

    public SecurityLevelField get(Class<?> clazz) {
        return securityLevelFieldCache.get(clazz);
    }

    private SecurityLevelField getSecurityLevelField(Class<?> clazz) {
        SecurityLevelField field = new SecurityLevelField();
        field.setSourceClass(clazz);
        return field;
    }

    private void setSecurityLevel(SecurityLevelField field, Field clazzField) {
        field.setFieldClass(clazzField.getType());
        field.setFieldName(clazzField.getName());

        Column column = clazzField.getAnnotation(Column.class);
        if (column != null && StringUtils.hasText(column.name())) {
            field.setDbFieldName(column.name());
        } else {
            field.setDbFieldName(clazzField.getName());
        }
    }

    private void setId(SecurityLevelField field, Field clazzField) {
        field.setIdName(clazzField.getName());

        Column column = clazzField.getAnnotation(Column.class);
        if (column != null && StringUtils.hasText(column.name())) {
            field.setDbIdName(column.name());
        } else {
            field.setDbIdName(clazzField.getName());
        }
    }
}
