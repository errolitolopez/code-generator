package com.el.generator.plugin;

import com.el.generator.util.CodeGeneratorUtil;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ServiceGeneratorPlugin extends PluginAdapter {
    public ServiceGeneratorPlugin() {
    }

    private static final Logger logger = LoggerFactory.getLogger(ServiceGeneratorPlugin.class);

    private static boolean IS_FIRST_CHAR_REMOVED = false;

    @Override
    public boolean validate(List<String> list) {
        logger.info("--- ServiceGeneratorPlugin invoke");
        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> generatedJavaFiles = super.contextGenerateAdditionalJavaFiles(introspectedTable);

        if (generatedJavaFiles == null) {
            generatedJavaFiles = new ArrayList<>();
        }

        String targetProject = properties.getProperty("targetProject");
        String servicePackage = properties.getProperty("servicePackage");
        String reqPackage = properties.getProperty("reqPackage");
        String rspPackage = properties.getProperty("rspPackage");

        FullyQualifiedJavaType exampleJavaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        FullyQualifiedJavaType primaryKeyJavaType = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType();
        FullyQualifiedJavaType baseRecordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();

        if (Boolean.parseBoolean(properties.getProperty("removeFirstChar"))) {
            IS_FIRST_CHAR_REMOVED = true;
            domainObjectName = domainObjectName.substring(1);
        }

        String mapperShortNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(mapperJavaType.getShortName(), IS_FIRST_CHAR_REMOVED);

        FullyQualifiedJavaType queryParamJavaType = new FullyQualifiedJavaType(reqPackage + ".Query" + domainObjectName + "Param");
        FullyQualifiedJavaType reqJavaType = new FullyQualifiedJavaType(reqPackage + "." + domainObjectName + "Req");
        FullyQualifiedJavaType rspJavaType = new FullyQualifiedJavaType(rspPackage + "." + domainObjectName + "Rsp");
        FullyQualifiedJavaType serviceJavaType = new FullyQualifiedJavaType(servicePackage + "." + domainObjectName + "Service");

        Interface serviceInterface = new Interface(serviceJavaType);
        serviceInterface.setVisibility(JavaVisibility.PUBLIC);

        serviceInterface.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        serviceInterface.addImportedType(primaryKeyJavaType);
        serviceInterface.addImportedType(queryParamJavaType);
        serviceInterface.addImportedType(reqJavaType);
        serviceInterface.addImportedType(rspJavaType);

        addMethod(serviceInterface, "query", queryParamJavaType, "param", "List<" + rspJavaType.getShortName() + ">");
        addMethod(serviceInterface, "selectById", primaryKeyJavaType, "id", rspJavaType.getShortName());
        addMethod(serviceInterface, "create", reqJavaType, "req", FullyQualifiedJavaType.getIntInstance().getShortName());
        addMethod(serviceInterface, "updateById", reqJavaType, "req", FullyQualifiedJavaType.getIntInstance().getShortName());
        addMethod(serviceInterface, "deleteById", primaryKeyJavaType, "id", FullyQualifiedJavaType.getIntInstance().getShortName());

        TopLevelClass serviceImplTopLevelClass = new TopLevelClass(serviceJavaType.getPackageName() + ".impl." + serviceJavaType.getShortName() + "Impl");
        serviceImplTopLevelClass.setVisibility(JavaVisibility.PUBLIC);
        serviceImplTopLevelClass.addAnnotation("@Service");
        serviceImplTopLevelClass.addSuperInterface(serviceJavaType);

        Field mapperField = new Field();
        mapperField.addAnnotation("");
        mapperField.addAnnotation("@Resource");
        mapperField.setVisibility(JavaVisibility.PRIVATE);
        mapperField.setType(mapperJavaType);
        mapperField.setName(mapperShortNameLowerCaseFirstChar);
        serviceImplTopLevelClass.addField(mapperField);

        serviceImplTopLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
        serviceImplTopLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        serviceImplTopLevelClass.addImportedType(mapperJavaType);
        serviceImplTopLevelClass.addImportedType(exampleJavaType);
        serviceImplTopLevelClass.addImportedType(baseRecordType);
        serviceImplTopLevelClass.addImportedType(new FullyQualifiedJavaType("javax.annotation.Resource"));
        serviceImplTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Service"));
        serviceImplTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.beans.BeanUtils"));
        serviceImplTopLevelClass.addImportedType(queryParamJavaType);
        serviceImplTopLevelClass.addImportedType(reqJavaType);
        serviceImplTopLevelClass.addImportedType(rspJavaType);
        serviceImplTopLevelClass.addImportedType(serviceJavaType);

        addQueryMethod(introspectedTable, serviceImplTopLevelClass, queryParamJavaType, rspJavaType);
        addSelectByIdMethod(introspectedTable, serviceImplTopLevelClass, primaryKeyJavaType, rspJavaType);
        addCreateMethod(introspectedTable, serviceImplTopLevelClass, reqJavaType, FullyQualifiedJavaType.getIntInstance());
        addUpdateByIdMethod(introspectedTable, serviceImplTopLevelClass, reqJavaType, FullyQualifiedJavaType.getIntInstance());
        addDeleteByIdMethod(introspectedTable, serviceImplTopLevelClass, primaryKeyJavaType, FullyQualifiedJavaType.getIntInstance());

        GeneratedJavaFile serviceGeneratedFile = new GeneratedJavaFile(serviceInterface, targetProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile serviceImplGeneratedFile = new GeneratedJavaFile(serviceImplTopLevelClass, targetProject, "UTF-8", getContext().getJavaFormatter());

        generatedJavaFiles.add(serviceGeneratedFile);
        generatedJavaFiles.add(serviceImplGeneratedFile);
        return generatedJavaFiles;
    }

    private static void addMethod(Interface aInterface, String methodName, FullyQualifiedJavaType paramJavaType, String paramName, String fullyQualifiedReturnType) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(methodName);
        method.addParameter(new Parameter(paramJavaType, paramName));
        method.setReturnType(new FullyQualifiedJavaType(fullyQualifiedReturnType));
        aInterface.addMethod(method);
    }

    private static void addQueryMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType paramJavaType, FullyQualifiedJavaType returnJavaType) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("query");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(paramJavaType, "param"));

        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperShortName = CodeGeneratorUtil.firstCharToLowerCase(mapperJavaType.getShortName(), IS_FIRST_CHAR_REMOVED);

        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String domainObjectNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(domainObjectName, IS_FIRST_CHAR_REMOVED);

        FullyQualifiedJavaType listReturnJavaType = new FullyQualifiedJavaType("List<" + domainObjectName + ">");
        FullyQualifiedJavaType rspListReturnJavaType = new FullyQualifiedJavaType("List<" + returnJavaType + ">");
        method.setReturnType(rspListReturnJavaType);

        FullyQualifiedJavaType exampleJavaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        String exampleShortName = exampleJavaType.getShortName();
        String exampleShortNameLowerFirstChar = CodeGeneratorUtil.firstCharToLowerCase(exampleShortName, IS_FIRST_CHAR_REMOVED);

        String listName = domainObjectNameLowerCaseFirstChar + "s";
        String rspShortName = returnJavaType.getShortName();
        String rspShortNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(rspShortName);

        method.addBodyLine(rspListReturnJavaType.getShortName() + " rspList = new ArrayList<>();");
        method.addBodyLine(exampleShortName + " " + exampleShortNameLowerFirstChar + " = new " + exampleShortName + "();");
        method.addBodyLine(listReturnJavaType.getShortName() + " " + listName + " = " + mapperShortName + "." + introspectedTable.getSelectByExampleStatementId() + "(" + exampleShortNameLowerFirstChar + ");");
        method.addBodyLine("if (!" + listName + ".isEmpty()) {");
        method.addBodyLine("for(" + domainObjectName + " " + domainObjectNameLowerCaseFirstChar + " : " + listName + ") {");
        method.addBodyLine(rspShortName + " " + rspShortNameLowerCaseFirstChar + " = new " + rspShortName + "();");
        method.addBodyLine("BeanUtils.copyProperties(" + domainObjectNameLowerCaseFirstChar + ", " + rspShortNameLowerCaseFirstChar + ");");
        method.addBodyLine("rspList.add(" + rspShortNameLowerCaseFirstChar + ");");
        method.addBodyLine("}");
        method.addBodyLine("}");
        method.addBodyLine("return rspList;");
        topLevelClass.addMethod(method);
    }

    private static void addSelectByIdMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType paramJavaType, FullyQualifiedJavaType returnJavaType) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(returnJavaType);
        method.setName("selectById");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(paramJavaType, "id"));

        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperShortName = CodeGeneratorUtil.firstCharToLowerCase(mapperJavaType.getShortName(), IS_FIRST_CHAR_REMOVED);

        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String domainObjectNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(domainObjectName, IS_FIRST_CHAR_REMOVED);

        String rspShortName = returnJavaType.getShortName();
        String rspShortNameLowerFirstChar = CodeGeneratorUtil.firstCharToLowerCase(rspShortName);

        method.addBodyLine(rspShortName + " " + rspShortNameLowerFirstChar + " = new " + rspShortName + "();");
        method.addBodyLine(domainObjectName + " " + domainObjectNameLowerCaseFirstChar + " = " + mapperShortName + "." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(id);");
        method.addBodyLine("if (" + domainObjectNameLowerCaseFirstChar + " == null) {");
        method.addBodyLine("return null;");
        method.addBodyLine("}");
        method.addBodyLine("BeanUtils.copyProperties(" + domainObjectNameLowerCaseFirstChar + ", " + rspShortNameLowerFirstChar + ");");
        method.addBodyLine("return " + rspShortNameLowerFirstChar + ";");
        topLevelClass.addMethod(method);
    }

    private static void addCreateMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType paramJavaType, FullyQualifiedJavaType returnJavaType) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(returnJavaType);
        method.setName("create");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(paramJavaType, "req"));

        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperShortName = CodeGeneratorUtil.firstCharToLowerCase(mapperJavaType.getShortName(), IS_FIRST_CHAR_REMOVED);

        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String domainObjectNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(domainObjectName, IS_FIRST_CHAR_REMOVED);

        method.addBodyLine(domainObjectName + " " + domainObjectNameLowerCaseFirstChar + " = new " + domainObjectName + "();");
        method.addBodyLine("BeanUtils.copyProperties(req, " + domainObjectNameLowerCaseFirstChar + ");");
        method.addBodyLine(returnJavaType.getShortName() + " i = " + mapperShortName + "." + introspectedTable.getInsertSelectiveStatementId() + "(" + domainObjectNameLowerCaseFirstChar + ");");
        method.addBodyLine("return i;");
        topLevelClass.addMethod(method);
    }

    private static void addUpdateByIdMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType paramJavaType, FullyQualifiedJavaType returnJavaType) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(returnJavaType);
        method.setName("updateById");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(paramJavaType, "req"));

        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperShortName = CodeGeneratorUtil.firstCharToLowerCase(mapperJavaType.getShortName(), IS_FIRST_CHAR_REMOVED);

        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String domainObjectNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(domainObjectName, IS_FIRST_CHAR_REMOVED);

        method.addBodyLine(domainObjectName + " " + domainObjectNameLowerCaseFirstChar + " = " + mapperShortName + "." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(req.get" + CodeGeneratorUtil.firstCharToUpperCase(introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()) + "());");
        method.addBodyLine("BeanUtils.copyProperties(req, " + domainObjectNameLowerCaseFirstChar + ");");
        method.addBodyLine(returnJavaType.getShortName() + " i = " + mapperShortName + "." + introspectedTable.getUpdateByPrimaryKeySelectiveStatementId() + "(" + domainObjectNameLowerCaseFirstChar + ");");
        method.addBodyLine("return i;");
        topLevelClass.addMethod(method);
    }

    private static void addDeleteByIdMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType paramJavaType, FullyQualifiedJavaType returnJavaType) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(returnJavaType);
        method.setName("deleteById");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(paramJavaType, "id"));

        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperShortName = CodeGeneratorUtil.firstCharToLowerCase(mapperJavaType.getShortName(), IS_FIRST_CHAR_REMOVED);

        String domainObjectName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String domainObjectNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(domainObjectName, IS_FIRST_CHAR_REMOVED);

        method.addBodyLine(domainObjectName + " " + domainObjectNameLowerCaseFirstChar + " = " + mapperShortName + "." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(id);");
        method.addBodyLine("if (" + domainObjectNameLowerCaseFirstChar + " == null) {");
        method.addBodyLine("return 0;");
        method.addBodyLine("}");
        method.addBodyLine(returnJavaType.getShortName() + " i = " + mapperShortName + "." + introspectedTable.getDeleteByPrimaryKeyStatementId() + "(" + domainObjectNameLowerCaseFirstChar + ".get" + CodeGeneratorUtil.firstCharToUpperCase(introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()) + "());");
        method.addBodyLine("return i;");
        topLevelClass.addMethod(method);
    }
}
