package com.el.generator.util;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;

public class CodeGeneratorUtil {
    public static void addFields(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            FullyQualifiedJavaType javaType = introspectedColumn.getFullyQualifiedJavaType();

            Field field = new Field();
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setName(introspectedColumn.getJavaProperty());
            field.setType(javaType);

            topLevelClass.addField(field);
            topLevelClass.addImportedType(javaType);
        }
    }

    public static void addMethodGetter(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            FullyQualifiedJavaType javaType = introspectedColumn.getFullyQualifiedJavaType();

            String name = introspectedColumn.getJavaProperty();
            String nameFirstCharToUpperCase = firstCharToUpperCase(name);

            Method method = new Method();
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setReturnType(javaType);
            method.setName("get" + nameFirstCharToUpperCase);

            method.addBodyLine("return " + name + ";");
            topLevelClass.addMethod(method);
        }
    }


    public static void addMethodSetter(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            FullyQualifiedJavaType javaType = introspectedColumn.getFullyQualifiedJavaType();

            String name = introspectedColumn.getJavaProperty();
            String nameFirstCharToUpperCase = firstCharToUpperCase(name);

            Method method = new Method();
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setName("set" + nameFirstCharToUpperCase);

            Parameter parameter = new Parameter(javaType, name);
            method.addParameter(parameter);

            method.addBodyLine("this." + name + " = " + name + ";");
            topLevelClass.addMethod(method);
        }
    }

    public static String firstCharToLowerCase(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }

    public static String firstCharToLowerCase(String s, boolean isRemovedFirstChar) {
        if (isRemovedFirstChar) {
            return firstCharToLowerCase(s.substring(1));
        }
        return firstCharToLowerCase(s);
    }


    public static String firstCharToUpperCase(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }

    public static String firstCharToUpperCase(String s, boolean isRemovedFirstChar) {
        if (isRemovedFirstChar) {
            return firstCharToUpperCase(s.substring(1));
        }
        return firstCharToUpperCase(s);
    }
}
