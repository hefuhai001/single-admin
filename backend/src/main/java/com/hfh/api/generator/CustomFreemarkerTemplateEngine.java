package com.hfh.api.generator;

import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Map;

public class CustomFreemarkerTemplateEngine extends FreemarkerTemplateEngine {

    private static final String JAVA_SUFFIX = ".java";
    private static final String XML_SUFFIX = ".xml";
    private static final String JS_SUFFIX = ".js";
    private static final String VUE_SUFFIX = ".vue";
    private static final String SQL_SUFFIX = ".sql";
    private String apiOutputPath;
    private String vueOutputPath;
    private String sqlOutputPath;

    public CustomFreemarkerTemplateEngine apiOutputPath(String apiOutputPath) {
        this.apiOutputPath = apiOutputPath;
        return this;
    }

    public CustomFreemarkerTemplateEngine vueOutputPath(String vueOutputPath) {
        this.vueOutputPath = vueOutputPath;
        return this;
    }

    public CustomFreemarkerTemplateEngine sqlOutputPath(String sqlOutputPath) {
        this.sqlOutputPath = sqlOutputPath;
        return this;
    }

    @Override
    public FreemarkerTemplateEngine init(ConfigBuilder configBuilder) {
        return super.init(configBuilder);
    }

    @Override
    protected void outputEntity(TableInfo tableInfo, Map<String, Object> objectMap) {
        String entityName = tableInfo.getEntityName();
        String entityPath = getPathInfo(OutputFile.entity);
        String fileName = entityPath + File.separator + entityName + JAVA_SUFFIX;
        outputFile(new File(fileName), objectMap, "/mybatis/entity.java.ftl", true);
    }

    @Override
    protected void outputMapper(TableInfo tableInfo, Map<String, Object> objectMap) {
        String mapperName = tableInfo.getMapperName();
        String mapperPath = getPathInfo(OutputFile.mapper);
        String fileName = mapperPath + File.separator + mapperName + JAVA_SUFFIX;
        outputFile(new File(fileName), objectMap, "/mybatis/mapper.java.ftl", true);

        String xmlName = tableInfo.getXmlName();
        String xmlPath = getPathInfo(OutputFile.xml);
        String xmlFileName = xmlPath + File.separator + xmlName + XML_SUFFIX;
        outputFile(new File(xmlFileName), objectMap, "/mybatis/mapper.xml.ftl", true);
    }

    @Override
    protected void outputService(TableInfo tableInfo, Map<String, Object> objectMap) {
        String serviceName = tableInfo.getServiceName();
        String servicePath = getPathInfo(OutputFile.service);
        String fileName = servicePath + File.separator + serviceName + JAVA_SUFFIX;
        outputFile(new File(fileName), objectMap, "/mybatis/service.java.ftl", true);

        String serviceImplName = tableInfo.getServiceImplName();
        String serviceImplPath = getPathInfo(OutputFile.serviceImpl);
        String implFileName = serviceImplPath + File.separator + serviceImplName + JAVA_SUFFIX;
        outputFile(new File(implFileName), objectMap, "/mybatis/serviceImpl.java.ftl", true);
    }

    @Override
    protected void outputController(TableInfo tableInfo, Map<String, Object> objectMap) {
        String controllerName = tableInfo.getControllerName();
        String controllerPath = getPathInfo(OutputFile.controller);
        String fileName = controllerPath + File.separator + controllerName + JAVA_SUFFIX;
        outputFile(new File(fileName), objectMap, "/mybatis/controller.java.ftl", true);

        outputApi(tableInfo, objectMap);
        outputVue(tableInfo, objectMap);
        outputInsertSql(tableInfo, objectMap);
    }

    protected void outputApi(TableInfo tableInfo, Map<String, Object> objectMap) {
        if (apiOutputPath == null || apiOutputPath.isEmpty()) {
            return;
        }
        String entityPath = StringUtils.capitalize(tableInfo.getEntityPath());
        entityPath = entityPath.substring(0, 1).toLowerCase() + entityPath.substring(1);
        entityPath = entityPath.replace("Entity", "");
        String fileName = apiOutputPath + File.separator + entityPath + JS_SUFFIX;
        outputFile(new File(fileName), objectMap, "/mybatis/api.js.ftl", true);
    }

    protected void outputVue(TableInfo tableInfo, Map<String, Object> objectMap) {
        if (vueOutputPath == null || vueOutputPath.isEmpty()) {
            return;
        }
        String entityPath = tableInfo.getEntityPath();
        entityPath = entityPath.substring(0, 1).toLowerCase() + entityPath.substring(1);
        entityPath = entityPath.replace("Entity", "");
        String fileName = vueOutputPath + File.separator + entityPath + File.separator + "index" + VUE_SUFFIX;
        outputFile(new File(fileName), objectMap, "/mybatis/index.vue.ftl", true);
    }

    protected void outputInsertSql(TableInfo tableInfo, Map<String, Object> objectMap) {
        if (sqlOutputPath == null || sqlOutputPath.isEmpty()) {
            return;
        }
        String entityPath = tableInfo.getEntityPath();
        entityPath = entityPath.substring(0, 1).toLowerCase() + entityPath.substring(1);
        entityPath = entityPath.replace("Entity", "");
        String fileName = sqlOutputPath + File.separator + "insert_" + entityPath + SQL_SUFFIX;
        outputFile(new File(fileName), objectMap, "/mybatis/insert.sql.ftl", true);
    }
}
