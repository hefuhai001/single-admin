package com.hfh.api.modules.monitor.task.task;

import com.hfh.api.modules.monitor.task.entity.JobEntity;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobTaskExecutorTest {

    public static class Target {
        public String value = "";

        public void hello() {
            value = "no-arg";
        }

        public void hello(String name) {
            value = name;
        }

        public void fail() {
            throw new IllegalStateException("boom");
        }
    }

    private JobTaskExecutor executor(ApplicationContext ctx) {
        JobTaskExecutor executor = new JobTaskExecutor(ctx);
        return executor;
    }

    private String target() {
        return Target.class.getName();
    }

    @Test
    void shouldExecuteNoArgMethod() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        Target target = new Target();
        when(ctx.getBean(Target.class)).thenReturn(target);

        executor(ctx).execute(target() + "#hello");

        assertThat(target.value).isEqualTo("no-arg");
    }

    @Test
    void shouldExecuteMethodWithStringParameter() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        Target target = new Target();
        when(ctx.getBean(Target.class)).thenReturn(target);

        executor(ctx).execute(target() + "#hello(world)");

        assertThat(target.value).isEqualTo("world");
    }

    @Test
    void shouldRejectBlankAndMalformedTargets() {
        JobTaskExecutor executor = executor(mock(ApplicationContext.class));

        assertThatThrownBy(() -> executor.execute(null)).hasMessage("调用目标不能为空");
        assertThatThrownBy(() -> executor.execute("  ")).hasMessage("调用目标不能为空");
        assertThatThrownBy(() -> executor.execute("noSharp")).hasMessageContaining("格式错误");
        assertThatThrownBy(() -> executor.execute("Class#method(")).hasMessageContaining("参数格式错误");
        assertThatThrownBy(() -> executor.execute("Class(1)")).hasMessageContaining("格式错误");
    }

    @Test
    void shouldWrapInvocationFailure() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        Target target = new Target();
        when(ctx.getBean(Target.class)).thenReturn(target);

        assertThatThrownBy(() -> executor(ctx).execute(target() + "#fail"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("执行定时任务失败");
    }

    @Test
    void shouldWrapMissingClass() {
        assertThatThrownBy(() -> executor(mock(ApplicationContext.class)).execute("not.exists.Clazz#run"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("执行定时任务失败");
    }
}
