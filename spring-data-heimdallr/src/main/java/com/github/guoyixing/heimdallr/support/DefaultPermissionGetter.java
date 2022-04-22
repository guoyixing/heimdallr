package com.github.guoyixing.heimdallr.support;

import com.github.guoyixing.heimdallr.common.enums.SecurityLevelEnum;

/**
 * @author 敲代码的旺财
 * @date 2022/4/20 10:40
 */
public class DefaultPermissionGetter implements PermissionGetter {
    public DefaultPermissionGetter() {
    }

    @Override
    public Object[] getOwnedPermissions(Object securityLevelEnum) {
        if (securityLevelEnum instanceof SecurityLevelEnum) {
            return SecurityLevelEnum.getOwnedPermissions((SecurityLevelEnum) securityLevelEnum).toArray();
        }
        throw new IllegalArgumentException("DefaultPermissionGetter无法识别securityLevelEnum的类型");
    }

    @Override
    public Object[] getDenyPermissions(Object securityLevelEnum) {
        if (securityLevelEnum instanceof SecurityLevelEnum) {
            return SecurityLevelEnum.getDenyPermissions((SecurityLevelEnum) securityLevelEnum).toArray();
        }
        throw new IllegalArgumentException("DefaultPermissionGetter无法识别securityLevelEnum的类型");
    }

    @Override
    public Object getDefaultPermissions() {
        return SecurityLevelEnum.NON_SECRET;
    }
}
