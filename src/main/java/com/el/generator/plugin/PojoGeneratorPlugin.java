package com.el.generator.plugin;

import com.el.generator.util.CodeGeneratorUtil;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PojoGeneratorPlugin extends PluginAdapter {
    public PojoGeneratorPlugin() {
    }

    private static final Logger logger = LoggerFactory.getLogger(PojoGeneratorPlugin.class);

    @Override
    public boolean validate(List<String> list) {
        logger.info("--- PojoGeneratorPlugin invoke");
        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> generatedJavaFiles = super.contextGenerateAdditionalJavaFiles(introspectedTable);

        if (generatedJavaFiles == null) {
            generatedJavaFiles = new ArrayList<>();
        }

        String targetProject = properties.getProperty("targetProject");
        String reqPackage = properties.getProperty("reqPackage");
        String rspPackage = properties.getProperty("rspPackage");

        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        if (Boolean.parseBoolean(properties.getProperty("removeFirstChar"))) {
            domainObjectName = domainObjectName.substring(1);
        }

        String reqPojoName = domainObjectName + "Req";
        String queryParamPojoName = "Query" + domainObjectName + "Param";
        String rspPojoName = domainObjectName + "Rsp";

        String fullyQualifiedReqPojoName = reqPackage + "." + reqPojoName;
        String fullyQualifiedQueryParamPojoName = reqPackage + "." + queryParamPojoName;
        String fullyQualifiedRspPojoName = rspPackage + "." + rspPojoName;

        TopLevelClass reqTopLevelClass = new TopLevelClass(fullyQualifiedReqPojoName);
        reqTopLevelClass.setVisibility(JavaVisibility.PUBLIC);
        CodeGeneratorUtil.addFields(introspectedTable, reqTopLevelClass);
        CodeGeneratorUtil.addMethodGetter(introspectedTable, reqTopLevelClass);
        CodeGeneratorUtil.addMethodSetter(introspectedTable, reqTopLevelClass);

        TopLevelClass queryParamTopLevelClass = new TopLevelClass(fullyQualifiedQueryParamPojoName);
        queryParamTopLevelClass.setVisibility(JavaVisibility.PUBLIC);
        CodeGeneratorUtil.addFields(introspectedTable, queryParamTopLevelClass);
        CodeGeneratorUtil.addMethodGetter(introspectedTable, queryParamTopLevelClass);
        CodeGeneratorUtil.addMethodSetter(introspectedTable, queryParamTopLevelClass);

        TopLevelClass rspTopLevelClass = new TopLevelClass(fullyQualifiedRspPojoName);
        rspTopLevelClass.setVisibility(JavaVisibility.PUBLIC);
        CodeGeneratorUtil.addFields(introspectedTable, rspTopLevelClass);
        CodeGeneratorUtil.addMethodGetter(introspectedTable, rspTopLevelClass);
        CodeGeneratorUtil.addMethodSetter(introspectedTable, rspTopLevelClass);

        GeneratedJavaFile reqGeneratedJavaFile = new GeneratedJavaFile(reqTopLevelClass, targetProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile queryParamGeneratedJavaFile = new GeneratedJavaFile(queryParamTopLevelClass, targetProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile rspGeneratedJavaFile = new GeneratedJavaFile(rspTopLevelClass, targetProject, "UTF-8", getContext().getJavaFormatter());

        generatedJavaFiles.add(reqGeneratedJavaFile);
        generatedJavaFiles.add(queryParamGeneratedJavaFile);
        generatedJavaFiles.add(rspGeneratedJavaFile);
        return generatedJavaFiles;
    }
}
