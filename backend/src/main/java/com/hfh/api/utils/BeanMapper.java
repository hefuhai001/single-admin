package com.hfh.api.utils;

import org.springframework.beans.BeanUtils;

/**
 * DTO / VO / Entity 属性映射的统一入口。
 * <p>
 * Controller 不再分散调用 {@link BeanUtils#copyProperties}：新建目标对象与属性拷贝
 * 合并为一步，减少样板代码与字段漏拷风险；后续如需切换 MapStruct、增加字段映射校验
 * 或忽略策略，只需修改本类，无需改动各业务 Controller。
 * </p>
 */
public final class BeanMapper {

    private BeanMapper() {
    }

    /**
     * 新建目标类型实例并拷贝同名属性。
     *
     * @param source     源对象，为 null 时返回 null
     * @param targetType 目标类型（需有无参构造）
     * @param <T>        目标类型
     * @return 填充后的目标对象
     */
    public static <T> T map(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        T target = BeanUtils.instantiateClass(targetType);
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 将同名属性拷贝到已有目标对象。
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }
}
