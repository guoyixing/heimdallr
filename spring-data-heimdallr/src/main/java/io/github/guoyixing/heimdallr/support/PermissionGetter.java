package io.github.guoyixing.heimdallr.support;

/**
 * 权限获取器
 *
 * @author 敲代码的旺财
 * @date 2022/4/20 10:25
 */
public interface PermissionGetter {
    /**
     * 拥有的权限
     *
     * @param t 用于判断权限的标识
     * @return 拥有的所有权限
     */
    Object[] getOwnedPermissions(Object t);

    /**
     * 没有的权限（这个接口占时没有用）
     *
     * @param t 用于判断权限的标识
     * @return 没有的所有权限
     */
    Object[] getDenyPermissions(Object t);

    /**
     * 获取默认用于判断权限的标识
     * 获取到的是{@link #getOwnedPermissions}和{@link #getDenyPermissions}的入参
     *
     * @return 默认的权限标识
     */
    Object getDefaultPermissions();

}
