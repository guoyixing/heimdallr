package com.github.guoyixing.heimdallr.common.enums;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 敲代码的旺财
 * @date 2022/4/20 10:33
 */
public enum SecurityLevelEnum {

    /**
     * 非密
     */
    NON_SECRET(0, "非密"),
    /**
     * 秘密
     */
    SECRET(1, "秘密"),
    /**
     * 机密
     */
    CONFIDENTIAL(2, "机密"),
    /**
     * 绝密
     */
    TOP_SECRET(3, "绝密");

    private Integer key;
    private String value;

    SecurityLevelEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public static List<SecurityLevelEnum> getOwnedPermissions(SecurityLevelEnum securityLevelEnum) {
        if (securityLevelEnum == null) {
            throw new IllegalArgumentException("SecurityLevelEnum is null");
        }
        List<SecurityLevelEnum> SecurityLevelEnums = new ArrayList<>();
        for (SecurityLevelEnum value : SecurityLevelEnum.values()) {
            if (value.getKey() <= securityLevelEnum.key) {
                SecurityLevelEnums.add(value);
            }
        }
        return SecurityLevelEnums;
    }

    public static List<SecurityLevelEnum> getDenyPermissions(SecurityLevelEnum securityLevelEnum) {
        if (securityLevelEnum == null) {
            throw new IllegalArgumentException("SecurityLevelEnum is null");
        }
        List<SecurityLevelEnum> SecurityLevelEnums = new ArrayList<>();
        for (SecurityLevelEnum value : SecurityLevelEnum.values()) {
            if (value.getKey() > securityLevelEnum.key) {
                SecurityLevelEnums.add(value);
            }
        }
        return SecurityLevelEnums;
    }


    public static SecurityLevelEnum getByKey(Integer documentLevel) {
        if (documentLevel == null) {
            return null;
        }
        for (SecurityLevelEnum value : SecurityLevelEnum.values()) {
            if (value.getKey().equals(documentLevel)) {
                return value;
            }
        }
        return null;
    }

    public static SecurityLevelEnum getByKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        for (SecurityLevelEnum enumV : SecurityLevelEnum.values()) {
            if (enumV.getKey().equals(Integer.valueOf(key))) {
                return enumV;
            }
        }
        return null;
    }

    public static SecurityLevelEnum getByValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        for (SecurityLevelEnum enumV : SecurityLevelEnum.values()) {
            if (enumV.getValue().equals(value)) {
                return enumV;
            }
        }
        return null;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
