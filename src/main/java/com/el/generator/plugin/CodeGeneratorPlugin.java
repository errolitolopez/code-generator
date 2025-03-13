package com.el.generator.plugin;

import com.el.generator.util.FieldUtils;
import com.el.generator.util.FileUtils;
import com.el.generator.util.JavaTypeUtils;
import com.el.generator.util.MethodUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CodeGeneratorPlugin extends PluginAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CodeGeneratorPlugin.class);

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable table) {
        logger.info("invoke CodeGeneratorPlugin");
        List<GeneratedJavaFile> generatedJavaFiles = super.contextGenerateAdditionalJavaFiles(table);
        if (generatedJavaFiles == null) {
            generatedJavaFiles = new ArrayList<>();
        }
        // PROPERTIES
        String className = table.getTableConfiguration().getDomainObjectName();
        if (Boolean.parseBoolean(properties.getProperty("removedFirstChar"))) {
            className = className.substring(1);
        }

        String targetPath = properties.getProperty("targetPath");
        String reqPackageNonClassName = properties.getProperty("reqPackage");
        String rspPackageNonClassName = properties.getProperty("rspPackage");
        String reqPackage = reqPackageNonClassName + "." + className.toLowerCase();
        String rspPackage = rspPackageNonClassName + "." + className.toLowerCase();
        String servicePackage = properties.getProperty("servicePackage");
        String facadePackage = properties.getProperty("facadePackage");
        String controllerPackage = properties.getProperty("controllerPackage");
        // JAVA TYPES
        FullyQualifiedJavaType queryReqJavaType = new FullyQualifiedJavaType(reqPackage + ".Query" + className + "Req");
        FullyQualifiedJavaType reqJavaType = new FullyQualifiedJavaType(reqPackage + "." + className + "Req");
        FullyQualifiedJavaType rspJavaType = new FullyQualifiedJavaType(rspPackage + "." + className + "Rsp");
        FullyQualifiedJavaType baseReqJavaType = new FullyQualifiedJavaType(reqPackageNonClassName + ".BaseReq");
        FullyQualifiedJavaType pagedJavaType = new FullyQualifiedJavaType(rspPackageNonClassName + ".Paged<T>");
        FullyQualifiedJavaType pagedRspJavaType = new FullyQualifiedJavaType(pagedJavaType.getFullyQualifiedNameWithoutTypeParameters() + "<" + rspJavaType.getFullyQualifiedName() + ">");
        FullyQualifiedJavaType responseJavaType = new FullyQualifiedJavaType(rspPackageNonClassName + ".Response");
        FullyQualifiedJavaType responseRspJavaType = new FullyQualifiedJavaType(responseJavaType.getFullyQualifiedName() + "<" + rspJavaType.getFullyQualifiedName() + ">");
        FullyQualifiedJavaType responsePagedRspJavaType = new FullyQualifiedJavaType(responseJavaType.getFullyQualifiedName() + "<" + pagedRspJavaType.getShortName() + ">");
        FullyQualifiedJavaType serviceJavaType = new FullyQualifiedJavaType(servicePackage + "." + className + "Service");
        FullyQualifiedJavaType serviceImplJavaType = new FullyQualifiedJavaType(servicePackage + ".impl." + className + "ServiceImpl");
        FullyQualifiedJavaType facadeJavaType = new FullyQualifiedJavaType(facadePackage + "." + className + "Facade");
        FullyQualifiedJavaType facadeImplJavaType = new FullyQualifiedJavaType(facadePackage + ".impl." + className + "FacadeImpl");
        FullyQualifiedJavaType controllerJavaType = new FullyQualifiedJavaType(controllerPackage + "." + className + "ApiController");

        FullyQualifiedJavaType integerJavaType = new FullyQualifiedJavaType("Integer");
        FullyQualifiedJavaType responseIntegerJavaType = new FullyQualifiedJavaType(responseJavaType.getFullyQualifiedName() + "<" + integerJavaType.getFullyQualifiedName() + ">");

        FullyQualifiedJavaType baseJavaType = new FullyQualifiedJavaType(table.getBaseRecordType());
        FullyQualifiedJavaType primaryKeyJavaType = table.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType();
        FullyQualifiedJavaType exampleJavaType = new FullyQualifiedJavaType(table.getExampleType());
        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(table.getMyBatis3JavaMapperType());
        FullyQualifiedJavaType rowBoundsJavaType = new FullyQualifiedJavaType("org.apache.ibatis.session.RowBounds");
        FullyQualifiedJavaType beanUtilsJavaType = new FullyQualifiedJavaType("org.springframework.beans.BeanUtils");
        FullyQualifiedJavaType serviceAnnotationJavaType = new FullyQualifiedJavaType("org.springframework.stereotype.Service");
        FullyQualifiedJavaType resourceAnnotationJavaType = new FullyQualifiedJavaType("jakarta.annotation.Resource");

        List<Field> fields = FieldUtils.getFields(table);
        // POJO
        if (!FileUtils.isJavaFileExists(targetPath, baseReqJavaType.getFullyQualifiedName())) {
            TopLevelClass queryReqClass = buildPojo(FieldUtils.getQueryReqBaseFields(), baseReqJavaType);
            generatedJavaFiles.add(buildJavaFile(queryReqClass, targetPath));
        }
        if (!FileUtils.isJavaFileExists(targetPath, responseJavaType.getFullyQualifiedName())) {
            TopLevelClass responseClass = buildPojo(FieldUtils.getResponseFields(), new FullyQualifiedJavaType(responseJavaType.getFullyQualifiedName() + "<T>"));
            generatedJavaFiles.add(buildJavaFile(responseClass, targetPath));
        }
        if (!FileUtils.isJavaFileExists(targetPath, pagedJavaType.getFullyQualifiedNameWithoutTypeParameters())) {
            TopLevelClass pagedClass = buildPojo(FieldUtils.getPagedFields(), pagedJavaType);
            pagedClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
            generatedJavaFiles.add(buildJavaFile(pagedClass, targetPath));
        }

        TopLevelClass queryReqClass = buildPojo(fields, queryReqJavaType);
        queryReqClass.setSuperClass(baseReqJavaType);
        queryReqClass.addImportedType(baseReqJavaType);
        generatedJavaFiles.add(buildJavaFile(queryReqClass, targetPath));

        TopLevelClass reqClass = buildPojo(fields, reqJavaType);
        generatedJavaFiles.add(buildJavaFile(reqClass, targetPath));

        TopLevelClass rspClass = buildPojo(fields, rspJavaType);
        generatedJavaFiles.add(buildJavaFile(rspClass, targetPath));

        // SERVICE
        Interface serviceInterface = buildInterface(serviceJavaType);
        serviceInterface.addMethod(MethodUtils.genMethod("query", "req", queryReqJavaType, pagedRspJavaType));
        serviceInterface.addMethod(MethodUtils.genMethod("create", "req", reqJavaType, integerJavaType));
        serviceInterface.addMethod(MethodUtils.genMethod("selectById", "id", primaryKeyJavaType, rspJavaType));
        serviceInterface.addMethod(MethodUtils.genMethod("deleteById", "id", primaryKeyJavaType, integerJavaType));
        serviceInterface.addMethod(MethodUtils.genMethod("updateById", "req", reqJavaType, integerJavaType));
        serviceInterface.addImportedTypes(JavaTypeUtils.getJavaTypesFromMethods(serviceInterface.getMethods()));
        generatedJavaFiles.add(buildJavaFile(serviceInterface, targetPath));
        // SERVICE IMPLEMENTATION
        TopLevelClass serviceImplClass = new TopLevelClass(serviceImplJavaType);
        serviceImplClass.setVisibility(JavaVisibility.PUBLIC);
        serviceImplClass.addAnnotation("@Service");
        serviceImplClass.addSuperInterface(serviceJavaType);

        Field mapperField = FieldUtils.genResourceField(mapperJavaType);
        serviceImplClass.addField(mapperField);

        Method serviceImplQueryMethod = MethodUtils.genMethod("query", "req", queryReqJavaType, pagedRspJavaType);
        serviceImplQueryMethod.addAnnotation("@Override");
        serviceImplQueryMethod.addBodyLine(pagedRspJavaType.getShortName() + " paged = new " + pagedRspJavaType.getShortName() + "();");
        serviceImplQueryMethod.addBodyLine("paged.setPageNo(req.getPageNo());");
        serviceImplQueryMethod.addBodyLine("paged.setPageSize(req.getPageSize());");
        serviceImplQueryMethod.addBodyLine(exampleJavaType.getShortName() + " example = new " + exampleJavaType.getShortName() + "();");
        serviceImplQueryMethod.addBodyLine("if (req.getOrderBy() != null && req.getOrderBy().trim().length() > 0) {");
        serviceImplQueryMethod.addBodyLine("example.setOrderByClause(req.getOrderBy());");
        serviceImplQueryMethod.addBodyLine("}");
        serviceImplQueryMethod.addBodyLine(exampleJavaType.getShortName() + ".Criteria criteria = example.createCriteria();");
        serviceImplQueryMethod.addBodyLine("buildCriteria(req, criteria);");
        serviceImplQueryMethod.addBodyLine("long total = " + mapperField.getName() + ".countByExample(example);");
        serviceImplQueryMethod.addBodyLine("paged.setTotal(total);");
        serviceImplQueryMethod.addBodyLine("if (total == 0) {");
        serviceImplQueryMethod.addBodyLine("return paged;");
        serviceImplQueryMethod.addBodyLine("}");
        serviceImplQueryMethod.addBodyLine(rowBoundsJavaType.getShortName() + " rowBounds = new " + rowBoundsJavaType.getShortName() + "((paged.getPageNo() - 1) * paged.getPageSize(), paged.getPageSize());");
        serviceImplQueryMethod.addBodyLine("List<" + baseJavaType.getShortName() + "> " + StringUtils.uncapitalize(className) + "List = " + mapperField.getName() + ".selectByExampleWithRowbounds(example, rowBounds);");
        serviceImplQueryMethod.addBodyLine("List<" + rspJavaType.getShortName() + "> " + StringUtils.uncapitalize(className) + "RspList = new ArrayList<>();");
        serviceImplQueryMethod.addBodyLine("for (" + baseJavaType.getShortName() + " " + StringUtils.uncapitalize(baseJavaType.getShortName()) + " : " + StringUtils.uncapitalize(className) + "List) {");
        serviceImplQueryMethod.addBodyLine(rspJavaType.getShortName() + " " + StringUtils.uncapitalize(rspJavaType.getShortName()) + " = new " + rspJavaType.getShortName() + "();");
        serviceImplQueryMethod.addBodyLine("BeanUtils.copyProperties(" + StringUtils.uncapitalize(baseJavaType.getShortName()) + ", " + StringUtils.uncapitalize(rspJavaType.getShortName()) + ");");
        serviceImplQueryMethod.addBodyLine(StringUtils.uncapitalize(className) + "RspList.add(" + StringUtils.uncapitalize(rspJavaType.getShortName()) + ");");
        serviceImplQueryMethod.addBodyLine("}");
        serviceImplQueryMethod.addBodyLine("paged.setData(" + StringUtils.uncapitalize(className) + "RspList);");
        serviceImplQueryMethod.addBodyLine("return paged;");
        serviceImplClass.addMethod(serviceImplQueryMethod);

        Method serviceImplCreateMethod = MethodUtils.genMethod("create", "req", reqJavaType, integerJavaType);
        serviceImplCreateMethod.addAnnotation("@Override");
        serviceImplCreateMethod.addBodyLine(baseJavaType.getShortName() + " " + StringUtils.uncapitalize(baseJavaType.getShortName()) + " = new " + baseJavaType.getShortName() + "();");
        serviceImplCreateMethod.addBodyLine("BeanUtils.copyProperties(req, " + StringUtils.uncapitalize(baseJavaType.getShortName()) + ");");
        serviceImplCreateMethod.addBodyLine("return " + mapperField.getName() + ".insertSelective(" + StringUtils.uncapitalize(baseJavaType.getShortName()) + ");");
        serviceImplClass.addMethod(serviceImplCreateMethod);

        Method serviceImplSelectByIdMethod = MethodUtils.genMethod("selectById", "id", primaryKeyJavaType, rspJavaType);
        serviceImplSelectByIdMethod.addAnnotation("@Override");
        serviceImplSelectByIdMethod.addBodyLine("if (id == null) {");
        serviceImplSelectByIdMethod.addBodyLine("return null;");
        serviceImplSelectByIdMethod.addBodyLine("}");
        serviceImplSelectByIdMethod.addBodyLine(baseJavaType.getShortName() + " " + StringUtils.uncapitalize(baseJavaType.getShortName()) + " = " + mapperField.getName() + ".selectByPrimaryKey(id);");
        serviceImplSelectByIdMethod.addBodyLine("if (" + StringUtils.uncapitalize(baseJavaType.getShortName()) + " == null) {");
        serviceImplSelectByIdMethod.addBodyLine("return null;");
        serviceImplSelectByIdMethod.addBodyLine("}");
        serviceImplSelectByIdMethod.addBodyLine(rspJavaType.getShortName() + " " + StringUtils.uncapitalize(rspJavaType.getShortName()) + " = new " + rspJavaType.getShortName() + "();");
        serviceImplSelectByIdMethod.addBodyLine("BeanUtils.copyProperties(" + StringUtils.uncapitalize(baseJavaType.getShortName()) + ", " + StringUtils.uncapitalize(rspJavaType.getShortName()) + ");");
        serviceImplSelectByIdMethod.addBodyLine("return " + StringUtils.uncapitalize(rspJavaType.getShortName()) + ";");
        serviceImplClass.addMethod(serviceImplSelectByIdMethod);

        Method serviceImplDeleteByIdMethod = MethodUtils.genMethod("deleteById", "id", primaryKeyJavaType, integerJavaType);
        serviceImplDeleteByIdMethod.addAnnotation("@Override");
        serviceImplDeleteByIdMethod.addBodyLine("if (id == null) {");
        serviceImplDeleteByIdMethod.addBodyLine("return 0;");
        serviceImplDeleteByIdMethod.addBodyLine("}");
        serviceImplDeleteByIdMethod.addBodyLine(baseJavaType.getShortName() + " " + StringUtils.uncapitalize(baseJavaType.getShortName()) + " = " + mapperField.getName() + ".selectByPrimaryKey(id);");
        serviceImplDeleteByIdMethod.addBodyLine("if (" + StringUtils.uncapitalize(baseJavaType.getShortName()) + " == null) {");
        serviceImplDeleteByIdMethod.addBodyLine("return 0;");
        serviceImplDeleteByIdMethod.addBodyLine("}");
        serviceImplDeleteByIdMethod.addBodyLine("return " + mapperField.getName() + ".deleteByPrimaryKey(id);");
        serviceImplClass.addMethod(serviceImplDeleteByIdMethod);

        Method serviceImplUpdateByIdMethod = MethodUtils.genMethod("updateById", "req", reqJavaType, integerJavaType);
        serviceImplUpdateByIdMethod.addAnnotation("@Override");
        serviceImplUpdateByIdMethod.addBodyLine("if (req == null || req.get" + StringUtils.capitalize(table.getPrimaryKeyColumns().get(0).getJavaProperty()) + "() == null) {");
        serviceImplUpdateByIdMethod.addBodyLine("return 0;");
        serviceImplUpdateByIdMethod.addBodyLine("}");
        serviceImplUpdateByIdMethod.addBodyLine(baseJavaType.getShortName() + " " + StringUtils.uncapitalize(baseJavaType.getShortName()) + " = " + mapperField.getName() + ".selectByPrimaryKey(req.get" + StringUtils.capitalize(table.getPrimaryKeyColumns().get(0).getJavaProperty()) + "());");
        serviceImplUpdateByIdMethod.addBodyLine("if (" + StringUtils.uncapitalize(baseJavaType.getShortName()) + " == null) {");
        serviceImplUpdateByIdMethod.addBodyLine("return 0;");
        serviceImplUpdateByIdMethod.addBodyLine("}");
        serviceImplUpdateByIdMethod.addBodyLine("BeanUtils.copyProperties(req, " + StringUtils.uncapitalize(baseJavaType.getShortName()) + ");");
        serviceImplUpdateByIdMethod.addBodyLine("return " + mapperField.getName() + ".updateByPrimaryKeySelective(" + StringUtils.uncapitalize(baseJavaType.getShortName()) + ");");
        serviceImplClass.addMethod(serviceImplUpdateByIdMethod);

        serviceImplClass.addMethod(MethodUtils.genBuildCriteria(exampleJavaType, queryReqClass));

        serviceImplClass.addImportedType(baseJavaType);
        serviceImplClass.addImportedType(serviceJavaType);
        serviceImplClass.addImportedType(exampleJavaType);
        serviceImplClass.addImportedType(rowBoundsJavaType);
        serviceImplClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        serviceImplClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
        serviceImplClass.addImportedType(beanUtilsJavaType);
        serviceImplClass.addImportedType(serviceAnnotationJavaType);
        serviceImplClass.addImportedType(resourceAnnotationJavaType);
        serviceImplClass.addImportedTypes(JavaTypeUtils.getJavaTypesFromMethods(serviceImplClass.getMethods()));
        serviceImplClass.addImportedTypes(JavaTypeUtils.getJavaTypesFromFields(serviceImplClass.getFields()));
        generatedJavaFiles.add(buildJavaFile(serviceImplClass, targetPath));
        // FACADE
        Interface facadeInterface = buildInterface(facadeJavaType);
        facadeInterface.addMethod(MethodUtils.genMethod("query", "req", queryReqJavaType, responsePagedRspJavaType));
        facadeInterface.addMethod(MethodUtils.genMethod("create", "req", reqJavaType, responseIntegerJavaType));
        facadeInterface.addMethod(MethodUtils.genMethod("selectById", "id", primaryKeyJavaType, responseRspJavaType));
        facadeInterface.addMethod(MethodUtils.genMethod("deleteById", "id", primaryKeyJavaType, responseIntegerJavaType));
        facadeInterface.addMethod(MethodUtils.genMethod("updateById", "req", reqJavaType, responseIntegerJavaType));
        facadeInterface.addImportedType(pagedJavaType);
        facadeInterface.addImportedTypes(JavaTypeUtils.getJavaTypesFromMethods(facadeInterface.getMethods()));
        generatedJavaFiles.add(buildJavaFile(facadeInterface, targetPath));
        // FACADE IMPLEMENTATION
        TopLevelClass facadeImplClass = new TopLevelClass(facadeImplJavaType);
        facadeImplClass.setVisibility(JavaVisibility.PUBLIC);
        facadeImplClass.addAnnotation("@Service");
        facadeImplClass.addSuperInterface(facadeJavaType);

        Field serviceField = FieldUtils.genResourceField(serviceJavaType);
        facadeImplClass.addField(serviceField);

        Method facadeImplQueryMethod = MethodUtils.genMethod("query", "req", queryReqJavaType, responsePagedRspJavaType);
        facadeImplQueryMethod.addAnnotation("@Override");
        facadeImplQueryMethod.addBodyLine("Response response = new Response();");
        facadeImplQueryMethod.addBodyLine("response.setResult(" + serviceField.getName() + ".query(req));");
        facadeImplQueryMethod.addBodyLine("return response;");
        facadeImplClass.addMethod(facadeImplQueryMethod);

        Method facadeImplCreateMethod = MethodUtils.genMethod("create", "req", reqJavaType, responseIntegerJavaType);
        facadeImplCreateMethod.addAnnotation("@Override");
        facadeImplCreateMethod.addBodyLine("Response response = new Response();");
        facadeImplCreateMethod.addBodyLine("response.setResult(" + serviceField.getName() + ".create(req));");
        facadeImplCreateMethod.addBodyLine("return response;");
        facadeImplClass.addMethod(facadeImplCreateMethod);

        Method facadeImplSelectByIdMethod = MethodUtils.genMethod("selectById", "id", primaryKeyJavaType, responseRspJavaType);
        facadeImplSelectByIdMethod.addAnnotation("@Override");
        facadeImplSelectByIdMethod.addBodyLine("Response response = new Response();");
        facadeImplSelectByIdMethod.addBodyLine("response.setResult(" + serviceField.getName() + ".selectById(id));");
        facadeImplSelectByIdMethod.addBodyLine("return response;");
        facadeImplClass.addMethod(facadeImplSelectByIdMethod);

        Method facadeImplDeleteByIdMethod = MethodUtils.genMethod("deleteById", "id", primaryKeyJavaType, responseIntegerJavaType);
        facadeImplDeleteByIdMethod.addAnnotation("@Override");
        facadeImplDeleteByIdMethod.addBodyLine("Response response = new Response();");
        facadeImplDeleteByIdMethod.addBodyLine("response.setResult(" + serviceField.getName() + ".deleteById(id));");
        facadeImplDeleteByIdMethod.addBodyLine("return response;");
        facadeImplClass.addMethod(facadeImplDeleteByIdMethod);

        Method facadeImplUpdateByIdMethod = MethodUtils.genMethod("updateById", "req", reqJavaType, responseIntegerJavaType);
        facadeImplUpdateByIdMethod.addAnnotation("@Override");
        facadeImplUpdateByIdMethod.addBodyLine("Response response = new Response();");
        facadeImplUpdateByIdMethod.addBodyLine("response.setResult(" + serviceField.getName() + ".updateById(req));");
        facadeImplUpdateByIdMethod.addBodyLine("return response;");
        facadeImplClass.addMethod(facadeImplUpdateByIdMethod);

        facadeImplClass.addImportedType(facadeJavaType);
        facadeImplClass.addImportedType(pagedJavaType);
        facadeImplClass.addImportedTypes(JavaTypeUtils.getJavaTypesFromMethods(facadeImplClass.getMethods()));
        facadeImplClass.addImportedTypes(JavaTypeUtils.getJavaTypesFromFields(facadeImplClass.getFields()));
        facadeImplClass.addImportedType(serviceAnnotationJavaType);
        facadeImplClass.addImportedType(resourceAnnotationJavaType);
        generatedJavaFiles.add(buildJavaFile(facadeImplClass, targetPath));
        // CONTROLLER
        TopLevelClass controllerClass = new TopLevelClass(controllerJavaType);
        controllerClass.setVisibility(JavaVisibility.PUBLIC);
        controllerClass.addAnnotation("@RestController");
        controllerClass.addAnnotation("@RequestMapping(\"/" + toHyphen(className) + "\")");

        Field facadeField = FieldUtils.genResourceField(facadeJavaType);
        controllerClass.addField(facadeField);

        Method controllerQueryMethod = MethodUtils.genMethod("query", "req", queryReqJavaType, responsePagedRspJavaType);
        controllerQueryMethod.addAnnotation("@GetMapping(\"/query\")");
        controllerQueryMethod.addBodyLine("return " + facadeField.getName() + ".query(req);");
        controllerClass.addMethod(controllerQueryMethod);

        Method controllerCreateMethod = MethodUtils.genMethod("create", "req", reqJavaType, responseIntegerJavaType);
        controllerCreateMethod.addAnnotation("@PostMapping(\"/create\")");
        controllerCreateMethod.addBodyLine("return " + facadeField.getName() + ".create(req);");
        controllerClass.addMethod(controllerCreateMethod);

        Method controllerSelectByIdMethod = MethodUtils.genMethod("selectById", "id", primaryKeyJavaType, responseRspJavaType);
        controllerSelectByIdMethod.addAnnotation("@GetMapping(\"/select\")");
        controllerSelectByIdMethod.addBodyLine("return " + facadeField.getName() + ".selectById(id);");
        controllerClass.addMethod(controllerSelectByIdMethod);

        Method controllerDeleteByIdMethod = MethodUtils.genMethod("deleteById", "id", primaryKeyJavaType, responseIntegerJavaType);
        controllerDeleteByIdMethod.addAnnotation("@PostMapping(\"/delete\")");
        controllerDeleteByIdMethod.addBodyLine("return " + facadeField.getName() + ".deleteById(id);");
        controllerClass.addMethod(controllerDeleteByIdMethod);

        Method controllerUpdateByIdMethod = MethodUtils.genMethod("updateById", "req", reqJavaType, responseIntegerJavaType);
        controllerUpdateByIdMethod.addAnnotation("@PostMapping(\"/update\")");
        controllerUpdateByIdMethod.addBodyLine("return " + facadeField.getName() + ".updateById(req);");
        controllerClass.addMethod(controllerUpdateByIdMethod);

        controllerClass.addImportedType(pagedJavaType);
        controllerClass.addImportedTypes(JavaTypeUtils.getJavaTypesFromMethods(controllerClass.getMethods()));
        controllerClass.addImportedTypes(JavaTypeUtils.getJavaTypesFromFields(controllerClass.getFields()));
        controllerClass.addImportedType(resourceAnnotationJavaType);
        controllerClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.GetMapping"));
        controllerClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.PostMapping"));
        controllerClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping"));
        controllerClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RestController"));
        generatedJavaFiles.add(buildJavaFile(controllerClass, targetPath));
        return generatedJavaFiles;
    }

    private TopLevelClass buildPojo(List<Field> fields, FullyQualifiedJavaType javaType) {
        TopLevelClass topLevelClass = new TopLevelClass(javaType.getFullyQualifiedName());
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        for (Field field : fields) {
            topLevelClass.addField(field);
            topLevelClass.addMethod(MethodUtils.generateGetter(field));
            topLevelClass.addMethod(MethodUtils.generateSetter(field));
        }
        topLevelClass.addMethod(MethodUtils.generateToString(javaType.getFullyQualifiedName(), fields));
        topLevelClass.addImportedTypes(JavaTypeUtils.getJavaTypesFromMethods(topLevelClass.getMethods()));
        return topLevelClass;
    }

    private Interface buildInterface(FullyQualifiedJavaType javaType) {
        Interface anInterface = new Interface(javaType);
        anInterface.setVisibility(JavaVisibility.PUBLIC);
        return anInterface;
    }

    private GeneratedJavaFile buildJavaFile(CompilationUnit compilationUnit, String targetPath) {
        return new GeneratedJavaFile(compilationUnit, targetPath, "UTF-8", getContext().getJavaFormatter());
    }

    private String toHyphen(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        String hyphenated = "";
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                hyphenated += "-" + ch;
            } else {
                hyphenated += ch;
            }
        }
        if (hyphenated.startsWith("-")) {
            hyphenated = hyphenated.substring(1);
        }
        return hyphenated.toLowerCase();
    }
}
