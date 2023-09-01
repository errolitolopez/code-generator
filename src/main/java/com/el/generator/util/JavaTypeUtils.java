package com.el.generator.util;

import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaTypeUtils {
    public static Set<FullyQualifiedJavaType> getJavaTypesFromMethods(List<Method> methods) {
        if (methods == null) {
            return new HashSet<>();
        }
        Set<FullyQualifiedJavaType> javaTypes = new HashSet<>();
        for (Method method : methods) {
            FullyQualifiedJavaType returnType = method.getReturnType();
            if (returnType != null) {
                javaTypes.add(new FullyQualifiedJavaType(returnType.getFullyQualifiedNameWithoutTypeParameters()));
                for (FullyQualifiedJavaType arg1 : returnType.getTypeArguments()) {
                    javaTypes.add(arg1);
                    for (FullyQualifiedJavaType arg2 : arg1.getTypeArguments()) {
                        javaTypes.add(arg2);
                    }
                }
            }
            for (Parameter parameter : method.getParameters()) {
                javaTypes.add(parameter.getType());
            }
        }
        return javaTypes;
    }

    public static Set<FullyQualifiedJavaType> getJavaTypesFromFields(List<Field> fields) {
        if (fields == null) {
            return new HashSet<>();
        }
        Set<FullyQualifiedJavaType> javaTypes = new HashSet<>();
        for (Field field : fields) {
            javaTypes.add(field.getType());
        }
        return javaTypes;
    }
}
