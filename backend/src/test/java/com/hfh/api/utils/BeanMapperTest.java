package com.hfh.api.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeanMapperTest {

    static class Source {
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    static class Target {
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    @Test
    void mapShouldCreateTargetAndCopyMatchingProperties() {
        Source source = new Source();
        source.setName("alice");
        source.setAge(20);

        Target target = BeanMapper.map(source, Target.class);

        assertThat(target).isNotNull();
        assertThat(target.getName()).isEqualTo("alice");
        assertThat(target.getAge()).isEqualTo(20);
    }

    @Test
    void mapShouldReturnNullWhenSourceIsNull() {
        assertThat(BeanMapper.map(null, Target.class)).isNull();
    }

    @Test
    void copyShouldCopyIntoExistingTarget() {
        Source source = new Source();
        source.setName("bob");
        Target target = new Target();

        BeanMapper.copy(source, target);

        assertThat(target.getName()).isEqualTo("bob");
        assertThat(target.getAge()).isNull();
    }
}
