package com.hfh.api.generator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.Map;

/**
 * MyBatis-Plus 自定义模板代码生成器
 * <p>
 * 使用方式：
 * 1. 确保 application-test.yaml 中配置了正确的数据库连接信息。
 * 2. 推荐方式：在 IDE 中直接运行本类的 main 方法。
 * 3. 命令行方式（已添加 exec-maven-plugin 插件）：
 *    <pre>
 *        # PowerShell
 *        mvn exec:java -D"exec.mainClass"="com.hfh.api.generator.GenCustomTemplate" -D"exec.classpathScope"=compile
 *
 *        # CMD / Bash
 *        mvn exec:java -Dexec.mainClass=com.hfh.api.generator.GenCustomTemplate -Dexec.classpathScope=compile
 *    </pre>
 * 4. 注意：mvn -pl backend ... 命令适用于多模块项目，本项目 backend 是单模块，pom.xml 就在 backend 目录下，
 *    所以不需要 -pl backend 参数。直接执行 java GenCustomTemplate.java 也会报错，因为该类依赖 Spring 和 MyBatis-Plus 等 jar，
 *    需要由 Maven 或 IDE 加载完整 classpath。
 * </p>
 * <p>
 * 生成内容：
 * - Java 后端代码：Entity、Mapper、Service、ServiceImpl、Controller
 * - 自定义模板输出：API 接口文档、Vue 前端页面、SQL 脚本
 * </p>
 */
public class GenCustomTemplate {

    public static void main(String[] args) throws Exception {

        String moduleName = "system";
        String tablePrefix = "sys_";

        String[] tables = {"sys_config"};

        String projectPath = System.getProperty("user.dir");
        String apiOutputPath = projectPath + "/src/main/resources/api";
        String vueOutputPath = projectPath + "/src/main/resources/views";
        String sqlOutputPath = projectPath + "/src/main/resources/sql";

        // 从 yml 配置文件读取数据库连接信息
        Yaml yaml = new Yaml();
        ClassPathResource resource = new ClassPathResource("application-dev.yaml");
        Map<String, Object> configMap = yaml.load(resource.getInputStream());

        Map<String, Object> datasourceConfig = (Map<String, Object>) configMap.get("spring");
        Map<String, Object> dbConfig = (Map<String, Object>) datasourceConfig.get("datasource");

        String url = (String) dbConfig.get("url");
        String username = (String) dbConfig.get("username");
        String password = (String) dbConfig.get("password");

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.outputDir(projectPath + "/src/main/java")
                            .author("xiaoHe")
                            .enableSpringdoc()
                            .dateType(DateType.ONLY_DATE)
                            .commentDate("yyyy-MM-dd")
                            .disableOpenDir();
                })
                .injectionConfig(builder -> {
                    builder.beforeOutputFile((tableInfo, objectMap) -> {
                        System.out.println("生成文件: " + tableInfo.getEntityName());
                    });
                })
                .packageConfig(builder -> {
                    builder.parent("com.hfh.api.modules")
                            .moduleName(moduleName)
                            .controller("controller")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    projectPath + "/src/main/resources/mapper/" + moduleName
                            ));
                })
                .strategyConfig(builder -> {
                    builder.addInclude(tables)
                            .addTablePrefix(tablePrefix)
                            .entityBuilder()
                            .enableLombok()
                            .enableChainModel()
                            .enableTableFieldAnnotation()
                            .disableSerialVersionUID()
                            .idType(IdType.AUTO)
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .formatFileName("%sEntity")
                            .controllerBuilder()
                            .enableRestStyle()
                            .enableHyphenStyle()
                            .formatFileName("%sController")
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
                            .mapperBuilder()
                            .enableMapperAnnotation()
                            .enableBaseColumnList()
                            .enableBaseResultMap()
                            .formatMapperFileName("%sMapper")
                            .formatXmlFileName("%sMapper");
                })
                .templateEngine(new CustomFreemarkerTemplateEngine()
                        .apiOutputPath(apiOutputPath)
                        .vueOutputPath(vueOutputPath)
                        .sqlOutputPath(sqlOutputPath))
                .execute();

        System.out.println("代码生成完成！");
    }
}
