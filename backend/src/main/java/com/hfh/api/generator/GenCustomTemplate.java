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

public class GenCustomTemplate {

    public static void main(String[] args) throws Exception {

        String moduleName = "form";
        String tablePrefix = "qus_";

        String[] tables = {"qus_question"};

        String projectPath = System.getProperty("user.dir") + "/backend";
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
