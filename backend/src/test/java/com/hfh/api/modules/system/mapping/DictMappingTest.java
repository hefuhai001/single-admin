package com.hfh.api.modules.system.mapping;

import cn.dev33.satoken.util.SaResult;
import com.hfh.api.modules.system.cache.DictCacheService;
import com.hfh.api.modules.system.annotation.DictMapping;
import com.hfh.api.modules.system.aspect.DictMappingAspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DictMappingTest {

    @Mock
    private DictCacheService dictCacheService;

    private DictMappingHelper helper;

    @BeforeEach
    void setUp() {
        helper = new DictMappingHelper(dictCacheService);
        when(dictCacheService.getDictLabel(eq("status"), anyString())).thenAnswer(inv -> {
            String value = inv.getArgument(1);
            return "1".equals(value) ? "启用" : null;
        });
        when(dictCacheService.getDictLabel("sex", "1")).thenReturn("男");
        when(dictCacheService.getDictLabel("sex", "0")).thenReturn("女");
    }

    static class Child {
        @DictMapping(dictType = "sex", labelField = "sexLabel")
        private String sex;
        private String sexLabel;
    }

    static class Demo {
        @DictMapping(dictType = "status", labelField = "statusLabel")
        private Integer status;
        private String statusLabel;

        @DictMapping(dictType = "sex", labelField = "sexText")
        private Boolean male;
        private String sexText;

        @DictMapping(dictType = "status", labelField = "missingLabel")
        private Integer missing;

        @DictMapping(dictType = "status", labelField = "nullValueLabel")
        private Integer nullValue;
        private String nullValueLabel;

        private Child child;
        private int age;
        private String name;
    }

    @Test
    void mapShouldIgnoreNullAndJdkTypes() {
        helper.map(null);
        helper.map("plain");
        helper.map(1);

        verify(dictCacheService, never()).getDictLabel(anyString(), anyString());
    }

    @Test
    void mapShouldTranslateAnnotatedFields() {
        Demo demo = new Demo();
        demo.status = 1;
        demo.male = true;
        demo.child = new Child();
        demo.child.sex = "0";
        demo.name = "alice";
        demo.age = 18;

        helper.map(demo);

        assertThat(demo.statusLabel).isEqualTo("启用");
        assertThat(demo.sexText).isEqualTo("男");
        assertThat(demo.child.sexLabel).isEqualTo("女");
    }

    @Test
    void mapShouldHandleMissingLabelFieldAndUnknownValue() {
        Demo demo = new Demo();
        demo.missing = 1;
        demo.status = 9;
        demo.male = false;

        helper.map(demo);

        assertThat(demo.statusLabel).isNull();
        assertThat(demo.sexText).isEqualTo("女");
    }

    @Test
    void mapShouldWalkCollectionAndMap() {
        Demo first = new Demo();
        first.status = 1;
        Demo second = new Demo();
        second.status = 1;

        helper.map(List.of(first, second));
        assertThat(first.statusLabel).isEqualTo("启用");
        assertThat(second.statusLabel).isEqualTo("启用");

        Demo inMap = new Demo();
        inMap.status = 1;
        helper.map(Map.of("k", inMap));
        assertThat(inMap.statusLabel).isEqualTo("启用");
    }

    @Test
    void mapShouldHandleSaResult() {
        Demo demo = new Demo();
        demo.status = 1;
        SaResult result = SaResult.ok().setData(demo);

        helper.map(result);

        assertThat(demo.statusLabel).isEqualTo("启用");
    }

    @Test
    void aspectShouldMapResultAndSwallowErrors() {
        DictMappingHelper mockHelper = org.mockito.Mockito.mock(DictMappingHelper.class);
        DictMappingAspect aspect = new DictMappingAspect(mockHelper);
        Demo demo = new Demo();
        SaResult saResult = SaResult.ok().setData(demo);

        aspect.afterReturning(null);
        verify(mockHelper, never()).map(any());

        aspect.afterReturning(saResult);
        verify(mockHelper).map(demo);

        aspect.afterReturning(demo);
        verify(mockHelper, org.mockito.Mockito.times(2)).map(demo);

        doThrow(new RuntimeException("boom")).when(mockHelper).map(any());
        aspect.afterReturning(demo);
    }
}
