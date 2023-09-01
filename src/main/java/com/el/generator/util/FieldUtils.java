package com.el.generator.util;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FieldUtils {
    public static List<Field> getFields(IntrospectedTable table) {
        return table.getAllColumns()
                .stream()
                .map(column -> {
                    Field field = new Field();
                    field.setVisibility(JavaVisibility.PRIVATE);
                    field.setName(column.getJavaProperty());
                    field.setType(column.getFullyQualifiedJavaType());
                    return field;
                }).collect(Collectors.toList());
    }

    public static List<Field> getQueryReqBaseFields() {
        List<Field> fields = new ArrayList<>();
        Field pageNo = new Field();
        pageNo.setVisibility(JavaVisibility.PRIVATE);
        pageNo.setName("pageNo = 1");
        pageNo.setType(new FullyQualifiedJavaType("Integer"));
        fields.add(pageNo);

        Field pageSize = new Field();
        pageSize.setVisibility(JavaVisibility.PRIVATE);
        pageSize.setName("pageSize = 20");
        pageSize.setType(new FullyQualifiedJavaType("Integer"));
        fields.add(pageSize);

        Field orderBy = new Field();
        orderBy.setVisibility(JavaVisibility.PRIVATE);
        orderBy.setName("orderBy");
        orderBy.setType(FullyQualifiedJavaType.getStringInstance());
        fields.add(orderBy);
        return fields;
    }

    public static List<Field> getResponseFields() {
        List<Field> fields = new ArrayList<>();
        Field status = new Field();
        status.setVisibility(JavaVisibility.PRIVATE);
        status.setName("status = 100");
        status.setType(new FullyQualifiedJavaType("Integer"));
        fields.add(status);

        Field message = new Field();
        message.setVisibility(JavaVisibility.PRIVATE);
        message.setName("message = \"SUCCESS\"");
        message.setType(new FullyQualifiedJavaType("String"));
        fields.add(message);

        Field data = new Field();
        data.setVisibility(JavaVisibility.PRIVATE);
        data.setName("result");
        data.setType(new FullyQualifiedJavaType("T"));
        fields.add(data);
        return fields;
    }

    public static List<Field> getPagedFields() {
        List<Field> fields = new ArrayList<>();
        Field pageNo = new Field();
        pageNo.setVisibility(JavaVisibility.PRIVATE);
        pageNo.setName("pageNo = 1");
        pageNo.setType(new FullyQualifiedJavaType("Integer"));
        fields.add(pageNo);

        Field pageSize = new Field();
        pageSize.setVisibility(JavaVisibility.PRIVATE);
        pageSize.setName("pageSize = 20");
        pageSize.setType(new FullyQualifiedJavaType("Integer"));
        fields.add(pageSize);

        Field total = new Field();
        total.setVisibility(JavaVisibility.PRIVATE);
        total.setName("total = 0L");
        total.setType(new FullyQualifiedJavaType("Long"));
        fields.add(total);

        Field data = new Field();
        data.setVisibility(JavaVisibility.PRIVATE);
        data.setName("data = new ArrayList<>()");
        data.setType(new FullyQualifiedJavaType("java.util.List<T>"));
        fields.add(data);
        return fields;
    }

    public static Field genResourceField(FullyQualifiedJavaType javaType) {
        Field field = new Field();
        field.addAnnotation("");
        field.addAnnotation("@Resource");
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(javaType);
        field.setName(StringUtils.uncapitalize(javaType.getShortNameWithoutTypeArguments()));
        return field;
    }
}
