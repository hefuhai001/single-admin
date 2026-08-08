package com.hfh.api.modules.monitor.task.task;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class JobTaskExecutor {

    private final ApplicationContext applicationContext;

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
}
