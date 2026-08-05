package com.hfh.api.modules.system.aspect;

import cn.dev33.satoken.util.SaResult;
import com.hfh.api.modules.system.mapping.DictMappingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 字典映射切面
 * <p>
 * 自动拦截所有 Controller 方法的返回值，递归扫描返回值中的对象字段。
 * 只要对象字段上标注了 {@link com.hfh.api.modules.system.annotation.DictMapping} 注解，
 * 就会根据字典类型和字段值从 Redis 缓存中查找对应标签，并写入注解指定的 labelField 字段。
 * </p>
 * <p>
 * 使用方式：在任意 VO/Entity 的字段上添加注解即可，例如：
 * </p>
 * <pre>
 * public class UserVO {
 *     // 字段本身存储字典值
 *     @DictMapping(dictType = "sex", labelField = "sexLabel")
 *     private String sex;
 *
 *     // 该字段会被自动填充为字典标签
 *     private String sexLabel;
 * }
 * </pre>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DictMappingAspect {

    private final DictMappingHelper dictMappingHelper;

    /**
     * 拦截所有 Controller 方法返回值（包括 SaResult 包装对象）
     */
    @AfterReturning(pointcut = "execution(public * com.hfh.api.modules..controller..*.*(..))", returning = "result")
    public void afterReturning(Object result) {
        if (result == null) {
            return;
        }
        try {
            // SaResult 中真正需要翻译的数据在 data 字段里
            if (result instanceof SaResult saResult) {
                dictMappingHelper.map(saResult.getData());
            } else {
                dictMappingHelper.map(result);
            }
        } catch (Exception e) {
            log.error("字典映射翻译失败", e);
        }
    }
}
