package com.hfh.api.modules.monitor.task.task;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class JobTaskExecutor {

    /** 默认允许调用的任务类包前缀，可通过 job.invoke.allowed-packages 覆盖（逗号分隔） */
    private static final String DEFAULT_ALLOWED_PACKAGES = "com.hfh.api.modules.monitor.task.task";

    private final ApplicationContext applicationContext;

    /**
     * 允许被反射调用的包白名单。
     * 只允许调用白名单包内的 Bean，避免任意类/任意方法被反射执行。
     */
    @Value("${job.invoke.allowed-packages:" + DEFAULT_ALLOWED_PACKAGES + "}")
    private String allowedPackages = DEFAULT_ALLOWED_PACKAGES;

    public void execute(String invokeTarget) {
        if (invokeTarget == null || invokeTarget.trim().isEmpty()) {
            throw new RuntimeException("调用目标不能为空");
        }
        String target = invokeTarget.trim();
        String className;
        String methodName;
        String params = null;

        if (target.contains("(")) {
            int open = target.lastIndexOf('(');
            int close = target.lastIndexOf(')');
            if (close < 0 || open > close) {
                throw new RuntimeException("调用目标参数格式错误");
            }
            int sharp = target.lastIndexOf('#', open);
            if (sharp < 0) {
                throw new RuntimeException("调用目标格式错误，应为 类名#方法名 或 类名#方法名(参数)");
            }
            className = target.substring(0, sharp);
            methodName = target.substring(sharp + 1, open);
            params = target.substring(open + 1, close).trim();
        } else if (target.contains("#")) {
            int split = target.indexOf('#');
            className = target.substring(0, split);
            methodName = target.substring(split + 1);
        } else {
            throw new RuntimeException("调用目标格式错误，应为 类名#方法名 或 类名#方法名(参数)");
        }

        try {
            Class<?> clazz = Class.forName(className);
            assertAllowed(clazz);
            Object bean = applicationContext.getBean(clazz);
            Method method;
            Object[] args;
            if (params == null || params.isEmpty()) {
                method = clazz.getMethod(methodName);
                args = new Object[0];
            } else {
                method = clazz.getMethod(methodName, String.class);
                args = new Object[]{params};
            }
            method.invoke(bean, args);
        } catch (Exception e) {
            throw new RuntimeException("执行定时任务失败：" + e.getMessage(), e);
        }
    }

    /**
     * 校验目标类是否位于允许调用的包内
     *
     * @param clazz 目标类
     */
    private void assertAllowed(Class<?> clazz) {
        String packageName = clazz.getPackageName();
        for (String prefix : allowedPackages.split(",")) {
            String allowed = prefix.trim();
            if (allowed.isEmpty()) {
                continue;
            }
            if (packageName.equals(allowed) || packageName.startsWith(allowed + ".")) {
                return;
            }
        }
        throw new IllegalStateException("调用目标不在允许的白名单内：" + clazz.getName());
    }
}
