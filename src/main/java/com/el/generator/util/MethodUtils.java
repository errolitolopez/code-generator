package com.el.generator.util;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.*;

import java.util.List;

public class MethodUtils {
    public static Method generateGetter(Field field) {
        String name = getName(field);
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName("get" + StringUtils.capitalize(name));
        method.addBodyLine("return " + name + ";");
        return method;
    }

    public static Method generateSetter(Field field) {
        String name = getName(field);
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("set" + StringUtils.capitalize(name));
        Parameter parameter = new Parameter(field.getType(), name);
        method.addParameter(parameter);
        method.addBodyLine("this." + name + " = " + name + ";");
        return method;
    }

    public static Method generateToString(String className, List<Field> fields) {
        FullyQualifiedJavaType javaType = new FullyQualifiedJavaType(className);
        Method method = new Method();
        method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("toString");
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.addBodyLine("return \"" + javaType.getShortNameWithoutTypeArguments() + "{\" +");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String line = i > 0 ? "\t\t\", " : "\t\t\"";
            line += getName(field);
            line += field.getType().equals(FullyQualifiedJavaType.getStringInstance()) ? "='\" + " : "=\" + ";
            line += getName(field);
            line += field.getType().equals(FullyQualifiedJavaType.getStringInstance()) ? " + '\\'' +" : " +";
            method.addBodyLine(line);
        }
        method.addBodyLine("'}';");
        return method;
    }

    public static Method genMethod(String methodName, String paramName, FullyQualifiedJavaType paramType, FullyQualifiedJavaType returnType) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(methodName);
        method.addParameter(new Parameter(paramType, paramName));
        method.setReturnType(returnType);
        return method;
    }

    public static Method genBuildCriteria(FullyQualifiedJavaType example, TopLevelClass topLevelClass) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PRIVATE);
        method.setName("buildCriteria");
        method.addParameter(new Parameter(topLevelClass.getType(), "req"));
        method.addParameter(new Parameter(new FullyQualifiedJavaType(example + ".Criteria"), "criteria"));
        for (Field field : topLevelClass.getFields()) {
            String get = "req.get" + StringUtils.capitalize(field.getName()) + "()";
            if (field.getType().equals(FullyQualifiedJavaType.getStringInstance())) {
                method.addBodyLine("if (" + get + " != null && " + get + ".trim().length() > 0) {");
            } else {
                method.addBodyLine("if (" + get + " != null) {");
            }
            method.addBodyLine("criteria.and" + StringUtils.capitalize(field.getName()) + "EqualTo(" + get + ");");
            method.addBodyLine("}");
        }
        return method;
    }

    private static String getName(Field field) {
        String name = field.getName();
        if (name.contains("=")) {
            String[] nameSplit = name.split("=");
            name = nameSplit[0];
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

}
