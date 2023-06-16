package com.el.generator.util;

import org.mybatis.generator.api.dom.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class BaseClassUtil {

    private static Logger logger = LoggerFactory.getLogger(BaseClassUtil.class);

    public static boolean isJavaFileNotExists(String basePath, FullyQualifiedJavaType javaType) {
        String filename = basePath + "/" + (javaType.toString().replaceAll("\\.", "/") + ".java");
        File file = new File(filename);
        logger.info("filename: {}, isExists: {}", filename, file.exists());
        return !file.exists();
    }

    public static TopLevelClass generateResponseTopLevelClass(FullyQualifiedJavaType fullyQualifiedJavaType) {
        TopLevelClass topLevelClass = new TopLevelClass(fullyQualifiedJavaType.getFullyQualifiedName() + "<T>");
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);

        Field statusField = new Field();
        statusField.setVisibility(JavaVisibility.PRIVATE);
        statusField.setName("status = 1");
        statusField.setType(new FullyQualifiedJavaType("Integer"));

        Field msgField = new Field();
        msgField.setVisibility(JavaVisibility.PRIVATE);
        msgField.setName("msg = \"SUCCESS\"");
        msgField.setType(FullyQualifiedJavaType.getStringInstance());

        Field resultField = new Field();
        resultField.setVisibility(JavaVisibility.PRIVATE);
        resultField.setName("result");
        resultField.setType(new FullyQualifiedJavaType("T"));

        addGetterSetter(topLevelClass, statusField);
        addGetterSetter(topLevelClass, msgField);
        addGetterSetter(topLevelClass, resultField);

        topLevelClass.addField(statusField);
        topLevelClass.addField(msgField);
        topLevelClass.addField(resultField);

        return topLevelClass;
    }

    public static TopLevelClass generateBaseReqTopLevelClass(FullyQualifiedJavaType fullyQualifiedJavaType) {
        TopLevelClass topLevelClass = new TopLevelClass(fullyQualifiedJavaType.getFullyQualifiedName() + "<T>");
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);

        Field pageNoField = new Field();
        pageNoField.setVisibility(JavaVisibility.PRIVATE);
        pageNoField.setName("pageNo = 1");
        pageNoField.setType(new FullyQualifiedJavaType("Integer"));

        Field pageSizeField = new Field();
        pageSizeField.setVisibility(JavaVisibility.PRIVATE);
        pageSizeField.setName("pageSize = 20");
        pageSizeField.setType(new FullyQualifiedJavaType("Integer"));

        Field orderByClauseField = new Field();
        orderByClauseField.setVisibility(JavaVisibility.PRIVATE);
        orderByClauseField.setName("orderByClause = \"CREATED_DATE DESC\"");
        orderByClauseField.setType(FullyQualifiedJavaType.getStringInstance());

        addGetterSetter(topLevelClass, pageNoField);
        addGetterSetter(topLevelClass, pageSizeField);
        addGetterSetter(topLevelClass, orderByClauseField);

        topLevelClass.addField(pageNoField);
        topLevelClass.addField(pageSizeField);
        topLevelClass.addField(orderByClauseField);

        return topLevelClass;
    }

    public static TopLevelClass generatePagedTopLevelClass(FullyQualifiedJavaType fullyQualifiedJavaType) {
        TopLevelClass topLevelClass = new TopLevelClass(fullyQualifiedJavaType.getFullyQualifiedName() + "<T>");
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.ArrayList"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.List"));

        Field pageNoField = new Field();
        pageNoField.setVisibility(JavaVisibility.PRIVATE);
        pageNoField.setName("pageNo = 1");
        pageNoField.setType(new FullyQualifiedJavaType("Integer"));

        Field pageSizeField = new Field();
        pageSizeField.setVisibility(JavaVisibility.PRIVATE);
        pageSizeField.setName("pageSize = 20");
        pageSizeField.setType(new FullyQualifiedJavaType("Integer"));

        Field totalField = new Field();
        totalField.setVisibility(JavaVisibility.PRIVATE);
        totalField.setName("total = 0L");
        totalField.setType(new FullyQualifiedJavaType("Long"));

        Field dataField = new Field();
        dataField.setVisibility(JavaVisibility.PRIVATE);
        dataField.setName("data = new ArrayList<>()");
        dataField.setType(new FullyQualifiedJavaType("java.util.List<T>"));

        addGetterSetter(topLevelClass, pageNoField);
        addGetterSetter(topLevelClass, pageSizeField);
        addGetterSetter(topLevelClass, totalField);
        addGetterSetter(topLevelClass, dataField);

        Method offset = new Method();
        offset.setVisibility(JavaVisibility.PUBLIC);
        offset.setName("offset");
        offset.setReturnType(new FullyQualifiedJavaType("Integer"));
        offset.addBodyLine("return (this.pageNo - 1) * this.pageSize;");

        Method limit = new Method();
        limit.setVisibility(JavaVisibility.PUBLIC);
        limit.setName("limit");
        limit.setReturnType(new FullyQualifiedJavaType("Integer"));
        limit.addBodyLine("return this.pageSize;");

        topLevelClass.addMethod(offset);
        topLevelClass.addMethod(limit);

        topLevelClass.addField(pageNoField);
        topLevelClass.addField(pageSizeField);
        topLevelClass.addField(totalField);
        topLevelClass.addField(dataField);
        return topLevelClass;
    }


    private static void addGetterSetter(TopLevelClass responseTopLevelClass, Field statusField) {
        String name = statusField.getName();
        if (name.contains("=")) {
            String[] nameSplit = name.split("=");
            name = nameSplit[0];
            name = name.substring(0, name.length() - 1);
        }
        String nameFirstCharToUpperCase = CodeGeneratorUtil.firstCharToUpperCase(name);

        Method setter = new Method();
        setter.setVisibility(JavaVisibility.PUBLIC);
        setter.setName("set" + nameFirstCharToUpperCase);

        Parameter parameter = new Parameter(statusField.getType(), name);
        setter.addParameter(parameter);

        setter.addBodyLine("this." + name + " = " + name + ";");

        Method getter = new Method();
        getter.setVisibility(JavaVisibility.PUBLIC);
        getter.setReturnType(statusField.getType());
        getter.setName("get" + nameFirstCharToUpperCase);

        getter.addBodyLine("return " + name + ";");

        responseTopLevelClass.addMethod(getter);
        responseTopLevelClass.addMethod(setter);
    }
}
