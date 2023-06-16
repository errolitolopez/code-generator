package com.el.generator.plugin;

import com.el.generator.util.BaseClassUtil;
import com.el.generator.util.CodeGeneratorUtil;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CodeGeneratorPlugin extends PluginAdapter {
    public CodeGeneratorPlugin() {
    }

    private static final Logger logger = LoggerFactory.getLogger(CodeGeneratorPlugin.class);

    private static boolean IS_FIRST_CHAR_REMOVED = false;

    @Override
    public boolean validate(List<String> list) {
        logger.info("--- CodeGeneratorPlugin invoke");
        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> generatedJavaFiles = super.contextGenerateAdditionalJavaFiles(introspectedTable);
        if (generatedJavaFiles == null) {
            generatedJavaFiles = new ArrayList<>();
        }

        if (Boolean.parseBoolean(properties.getProperty("removeFirstChar"))) {
            IS_FIRST_CHAR_REMOVED = true;
        }

        String domainName = CodeGeneratorUtil.firstCharToUpperCase(introspectedTable.getTableConfiguration().getDomainObjectName(), IS_FIRST_CHAR_REMOVED);
        String domainNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(domainName);

        String reqPackage = properties.getProperty("reqPackage");
        String rspPackage = properties.getProperty("rspPackage");
        String servicePackage = properties.getProperty("servicePackage");
        String facadePackage = properties.getProperty("facadePackage");
        String controllerPackage = properties.getProperty("controllerPackage");

        String targetDomainProject = properties.getProperty("targetDomainProject");
        String targetBizProject = properties.getProperty("targetBizProject");
        String targetControllerProject = properties.getProperty("targetControllerProject");

        FullyQualifiedJavaType controller = new FullyQualifiedJavaType(controllerPackage + "." + domainName + "Controller");
        FullyQualifiedJavaType facade = new FullyQualifiedJavaType(facadePackage + "." + domainName + "Facade");
        FullyQualifiedJavaType queryParam = new FullyQualifiedJavaType(reqPackage + ".Query" + domainName + "Param");
        FullyQualifiedJavaType req = new FullyQualifiedJavaType(reqPackage + "." + domainName + "Req");
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(rspPackage + "." + "Response");
        FullyQualifiedJavaType rsp = new FullyQualifiedJavaType(rspPackage + "." + domainName + "Rsp");
        FullyQualifiedJavaType service = new FullyQualifiedJavaType(servicePackage + "." + domainName + "Service");
        FullyQualifiedJavaType baseReq = new FullyQualifiedJavaType(reqPackage + "." + "BaseReq");
        FullyQualifiedJavaType paged = new FullyQualifiedJavaType(rspPackage + "." + "Paged");
        FullyQualifiedJavaType pagedRsp = new FullyQualifiedJavaType(paged.getShortName() + "<" + rsp.getShortName() + ">");

        FullyQualifiedJavaType aInt = FullyQualifiedJavaType.getIntInstance();
        FullyQualifiedJavaType annotationResource = new FullyQualifiedJavaType("javax.annotation.Resource");
        FullyQualifiedJavaType annotationService = new FullyQualifiedJavaType("org.springframework.stereotype.Service");
        FullyQualifiedJavaType baseRecord = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType example = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType mapper = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        FullyQualifiedJavaType primaryKey = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType();

        TopLevelClass queryParamTopLevelClass = CodeGeneratorUtil.getTopLevelClass(introspectedTable, queryParam);
        queryParamTopLevelClass.setSuperClass(baseReq);
        TopLevelClass reqTopLevelClass = CodeGeneratorUtil.getTopLevelClass(introspectedTable, req);
        TopLevelClass rspTopLevelClass = CodeGeneratorUtil.getTopLevelClass(introspectedTable, rsp);

        Interface serviceInterface = new Interface(service);
        serviceInterface.setVisibility(JavaVisibility.PUBLIC);
        serviceInterface.addImportedType(primaryKey);
        serviceInterface.addImportedType(queryParam);
        serviceInterface.addImportedType(req);
        serviceInterface.addImportedType(rsp);
        serviceInterface.addImportedType(paged);

        addMethod(serviceInterface, "query", queryParam, "param", pagedRsp);
        addMethod(serviceInterface, "selectById", primaryKey, "id", rsp);
        addMethod(serviceInterface, "create", req, "req", aInt);
        addMethod(serviceInterface, "updateById", req, "req", aInt);
        addMethod(serviceInterface, "deleteById", primaryKey, "id", aInt);

        TopLevelClass serviceImplTopLevelClass = new TopLevelClass(service.getPackageName() + ".impl." + service.getShortName() + "Impl");
        serviceImplTopLevelClass.setVisibility(JavaVisibility.PUBLIC);
        serviceImplTopLevelClass.addAnnotation("@Service");
        serviceImplTopLevelClass.addSuperInterface(service);

        Field mapperField = new Field();
        mapperField.addAnnotation("");
        mapperField.addAnnotation("@Resource");
        mapperField.setVisibility(JavaVisibility.PRIVATE);
        mapperField.setType(mapper);
        mapperField.setName(CodeGeneratorUtil.firstCharToLowerCase(mapper.getShortName(), IS_FIRST_CHAR_REMOVED));
        serviceImplTopLevelClass.addField(mapperField);

        serviceImplTopLevelClass.addImportedType(baseRecord);
        serviceImplTopLevelClass.addImportedType(example);
        serviceImplTopLevelClass.addImportedType(mapper);
        serviceImplTopLevelClass.addImportedType(queryParam);
        serviceImplTopLevelClass.addImportedType(req);
        serviceImplTopLevelClass.addImportedType(rsp);
        serviceImplTopLevelClass.addImportedType(service);
        serviceImplTopLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
        serviceImplTopLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        serviceImplTopLevelClass.addImportedType(annotationResource);
        serviceImplTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.beans.BeanUtils"));
        serviceImplTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.session.RowBounds"));
        serviceImplTopLevelClass.addImportedType(annotationService);
        serviceImplTopLevelClass.addImportedType(paged);

        addServiceQueryMethod(introspectedTable, serviceImplTopLevelClass, queryParam, rsp, pagedRsp);
        addServiceSelectByIdMethod(introspectedTable, serviceImplTopLevelClass, primaryKey, rsp);
        addServiceCreateMethod(introspectedTable, serviceImplTopLevelClass, req, aInt);
        addServiceUpdateByIdMethod(introspectedTable, serviceImplTopLevelClass, req, aInt);
        addServiceDeleteByIdMethod(introspectedTable, serviceImplTopLevelClass, primaryKey, aInt);
        addBuildCriteria(introspectedTable, serviceImplTopLevelClass, queryParam);

        Interface facadeInterface = new Interface(facade);
        facadeInterface.setVisibility(JavaVisibility.PUBLIC);
        facadeInterface.addImportedType(primaryKey);
        facadeInterface.addImportedType(queryParam);
        facadeInterface.addImportedType(req);
        facadeInterface.addImportedType(response);

        addMethod(facadeInterface, "query", queryParam, "param", response);
        addMethod(facadeInterface, "selectById", primaryKey, "id", response);
        addMethod(facadeInterface, "create", req, "req", response);
        addMethod(facadeInterface, "updateById", req, "req", response);
        addMethod(facadeInterface, "deleteById", primaryKey, "id", response);

        TopLevelClass facadeImplTopLevelClass = new TopLevelClass(facade.getPackageName() + ".impl." + facade.getShortName() + "Impl");
        facadeImplTopLevelClass.setVisibility(JavaVisibility.PUBLIC);
        facadeImplTopLevelClass.addAnnotation("@Service");
        facadeImplTopLevelClass.addSuperInterface(facade);

        Field serviceField = new Field();
        serviceField.addAnnotation("");
        serviceField.addAnnotation("@Resource");
        serviceField.setVisibility(JavaVisibility.PRIVATE);
        serviceField.setType(service);
        serviceField.setName(CodeGeneratorUtil.firstCharToLowerCase(service.getShortName()));
        facadeImplTopLevelClass.addField(serviceField);

        facadeImplTopLevelClass.addImportedType(facade);
        facadeImplTopLevelClass.addImportedType(queryParam);
        facadeImplTopLevelClass.addImportedType(req);
        facadeImplTopLevelClass.addImportedType(response);
        facadeImplTopLevelClass.addImportedType(rsp);
        facadeImplTopLevelClass.addImportedType(service);
        facadeImplTopLevelClass.addImportedType(annotationResource);
        facadeImplTopLevelClass.addImportedType(annotationService);
        facadeImplTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.transaction.annotation.Transactional"));
        facadeImplTopLevelClass.addImportedType(paged);

        addFacadeQueryMethod(facadeImplTopLevelClass, queryParam, service, pagedRsp, response);
        addFacadeSelectByIdMethod(facadeImplTopLevelClass, primaryKey, service, rsp, response);
        addFacadeCreateMethod(facadeImplTopLevelClass, req, service, response);
        addFacadeUpdateByIdMethod(facadeImplTopLevelClass, req, service, response);
        addFacadeDeleteByIdMethod(facadeImplTopLevelClass, primaryKey, service, rsp, response);

        TopLevelClass controllerTopLevelClass = new TopLevelClass(controller.getPackageName() + "." + controller.getShortName());
        controllerTopLevelClass.setVisibility(JavaVisibility.PUBLIC);
        controllerTopLevelClass.addAnnotation("@RestController");
        controllerTopLevelClass.addAnnotation("@RequestMapping(\"/" + domainNameLowerCaseFirstChar + "\")");

        Field facadeField = new Field();
        facadeField.addAnnotation("");
        facadeField.addAnnotation("@Resource");
        facadeField.setVisibility(JavaVisibility.PRIVATE);
        facadeField.setType(facade);
        facadeField.setName(CodeGeneratorUtil.firstCharToLowerCase(facade.getShortName()));
        controllerTopLevelClass.addField(facadeField);

        controllerTopLevelClass.addImportedType(annotationResource);
        controllerTopLevelClass.addImportedType(facade);
        controllerTopLevelClass.addImportedType(queryParam);
        controllerTopLevelClass.addImportedType(response);
        controllerTopLevelClass.addImportedType(req);
        controllerTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RestController"));
        controllerTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping"));
        controllerTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMethod"));
        controllerTopLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestBody"));

        addControllerQueryMethod(controllerTopLevelClass, queryParam, facade, response);
        addControllerSelectByIdMethod(controllerTopLevelClass, primaryKey, facade, response);
        addControllerCreateMethod(controllerTopLevelClass, req, facade, response);
        addControllerUpdateByIdMethod(controllerTopLevelClass, req, facade, response);
        addControllerDeleteByIdMethod(controllerTopLevelClass, primaryKey, facade, response);

        GeneratedJavaFile queryParamJavaFile = new GeneratedJavaFile(queryParamTopLevelClass, targetDomainProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile reqJavaFile = new GeneratedJavaFile(reqTopLevelClass, targetDomainProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile rspJavaFile = new GeneratedJavaFile(rspTopLevelClass, targetDomainProject, "UTF-8", getContext().getJavaFormatter());

        GeneratedJavaFile serviceJavaFile = new GeneratedJavaFile(serviceInterface, targetBizProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile serviceImplJavaFile = new GeneratedJavaFile(serviceImplTopLevelClass, targetBizProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile facadeJavaFile = new GeneratedJavaFile(facadeInterface, targetBizProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile facadeImplJavaFile = new GeneratedJavaFile(facadeImplTopLevelClass, targetBizProject, "UTF-8", getContext().getJavaFormatter());
        GeneratedJavaFile controllerJavaFile = new GeneratedJavaFile(controllerTopLevelClass, targetControllerProject, "UTF-8", getContext().getJavaFormatter());

        generatedJavaFiles.add(queryParamJavaFile);
        generatedJavaFiles.add(reqJavaFile);
        generatedJavaFiles.add(rspJavaFile);

        generatedJavaFiles.add(serviceJavaFile);
        generatedJavaFiles.add(serviceImplJavaFile);
        generatedJavaFiles.add(facadeJavaFile);
        generatedJavaFiles.add(facadeImplJavaFile);
        generatedJavaFiles.add(controllerJavaFile);

        if (BaseClassUtil.isJavaFileNotExists(targetDomainProject, response)) {
            GeneratedJavaFile javaFile = new GeneratedJavaFile(BaseClassUtil.generateResponseTopLevelClass(response), targetDomainProject, "UTF-8", getContext().getJavaFormatter());
            logger.info("file does not exists: {}", response.getShortName());
            logger.info("generating: \n{}", javaFile.toString());
            generatedJavaFiles.add(javaFile);
        }

        if (BaseClassUtil.isJavaFileNotExists(targetDomainProject, baseReq)) {
            GeneratedJavaFile javaFile = new GeneratedJavaFile(BaseClassUtil.generateBaseReqTopLevelClass(baseReq), targetDomainProject, "UTF-8", getContext().getJavaFormatter());
            logger.info("file does not exists: {}", response.getShortName());
            logger.info("generating: \n{}", javaFile);
            generatedJavaFiles.add(javaFile);
        }

        if (BaseClassUtil.isJavaFileNotExists(targetDomainProject, paged)) {
            GeneratedJavaFile javaFile = new GeneratedJavaFile(BaseClassUtil.generatePagedTopLevelClass(paged), targetDomainProject, "UTF-8", getContext().getJavaFormatter());
            logger.info("file does not exists: {}", paged.getShortName());
            logger.info("generating: \n{}", javaFile);
            generatedJavaFiles.add(javaFile);
        }

        return generatedJavaFiles;
    }

    private static void addMethod(Interface aInterface, String methodName, FullyQualifiedJavaType param, String paramName, FullyQualifiedJavaType aReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(methodName);
        method.addParameter(new Parameter(param, paramName));
        method.setReturnType(aReturn);
        aInterface.addMethod(method);
    }

    private static void addServiceQueryMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType aReturn, FullyQualifiedJavaType paged) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("query");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(param, "param"));

        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperName = CodeGeneratorUtil.firstCharToLowerCase(mapperJavaType.getShortName(), IS_FIRST_CHAR_REMOVED);

        String name = introspectedTable.getTableConfiguration().getDomainObjectName();
        String nameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(name, IS_FIRST_CHAR_REMOVED);

        FullyQualifiedJavaType listReturnJavaType = new FullyQualifiedJavaType("List<" + name + ">");
        FullyQualifiedJavaType rspListReturnJavaType = new FullyQualifiedJavaType("List<" + aReturn + ">");
        method.setReturnType(paged);

        FullyQualifiedJavaType exampleJavaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        String exampleName = exampleJavaType.getShortName();
        String exampleNameLowerFirstChar = CodeGeneratorUtil.firstCharToLowerCase(exampleName, IS_FIRST_CHAR_REMOVED);

        String listName = nameLowerCaseFirstChar + "List";
        String rspName = aReturn.getShortName();
        String rspLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(rspName);

        method.addBodyLine(paged.getShortName() + " paged = new " + paged.getShortName() + "();");
        method.addBodyLine("paged.setPageNo(param.getPageNo());");
        method.addBodyLine("paged.setPageSize(param.getPageSize());");
        method.addBodyLine(exampleName + " " + exampleNameLowerFirstChar + " = new " + exampleName + "();");
        method.addBodyLine(exampleNameLowerFirstChar + ".setOrderByClause(param.getOrderByClause());");
        method.addBodyLine(exampleName + ".Criteria " + exampleNameLowerFirstChar + "Criteria = " + exampleNameLowerFirstChar + ".createCriteria();");
        method.addBodyLine("buildCriteria(param, " + exampleNameLowerFirstChar + "Criteria);");
        method.addBodyLine("long total = " + mapperName + "." + introspectedTable.getCountByExampleStatementId() + "(" + exampleNameLowerFirstChar + ");");
        method.addBodyLine("paged.setTotal(total);");
        method.addBodyLine("if (total == 0) {");
        method.addBodyLine("return paged;");
        method.addBodyLine("}");
        method.addBodyLine("RowBounds rowBounds = new RowBounds(paged.offSet(), paged.limit());");
        method.addBodyLine(listReturnJavaType.getShortName() + " " + listName + " = " + mapperName + "." + "selectByExampleWithRowbounds(" + exampleNameLowerFirstChar + ", rowBounds);");
        method.addBodyLine(rspListReturnJavaType.getShortName() + " rspList = new ArrayList<>();");
        method.addBodyLine("for (" + name + " " + nameLowerCaseFirstChar + " : " + listName + ") {");
        method.addBodyLine(rspName + " " + rspLowerCaseFirstChar + " = new " + rspName + "();");
        method.addBodyLine("BeanUtils.copyProperties(" + nameLowerCaseFirstChar + ", " + rspLowerCaseFirstChar + ");");
        method.addBodyLine("rspList.add(" + rspLowerCaseFirstChar + ");");
        method.addBodyLine("}");
        method.addBodyLine("paged.setData(rspList);");
        method.addBodyLine("return paged;");
        topLevelClass.addMethod(method);
    }

    private static void addBuildCriteria(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType param) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PRIVATE);
        method.setName("buildCriteria");

        FullyQualifiedJavaType exampleJavaType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        String exampleName = exampleJavaType.getShortName();

        FullyQualifiedJavaType exampleCriteria = new FullyQualifiedJavaType(exampleName + ".Criteria");

        method.addParameter(new Parameter(param, "param"));
        method.addParameter(new Parameter(exampleCriteria, "criteria"));

        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            FullyQualifiedJavaType javaType = introspectedColumn.getFullyQualifiedJavaType();

            String name = introspectedColumn.getJavaProperty();
            String nameFirstCharToUpperCase = CodeGeneratorUtil.firstCharToUpperCase(name);

            String paramGet = "param.get" + nameFirstCharToUpperCase + "()";
            if (javaType.equals(FullyQualifiedJavaType.getStringInstance())) {
                method.addBodyLine("if (" + paramGet + " != null && " + paramGet + ".trim().length() > 0) {");
            } else {
                method.addBodyLine("if (" + paramGet + " != null) {");
            }
            method.addBodyLine("criteria.and" + nameFirstCharToUpperCase + "EqualTo(" + paramGet + ");");
            method.addBodyLine("}");
        }
        topLevelClass.addMethod(method);
    }

    private static void addServiceSelectByIdMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType aReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(aReturn);
        method.setName("selectById");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(param, "id"));

        FullyQualifiedJavaType mapperJavaType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperName = CodeGeneratorUtil.firstCharToLowerCase(mapperJavaType.getShortName(), IS_FIRST_CHAR_REMOVED);

        String name = introspectedTable.getTableConfiguration().getDomainObjectName();
        String nameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(name, IS_FIRST_CHAR_REMOVED);

        String rspName = aReturn.getShortName();
        String rspLowerFirstChar = CodeGeneratorUtil.firstCharToLowerCase(rspName);

        method.addBodyLine(rspName + " " + rspLowerFirstChar + " = new " + rspName + "();");
        method.addBodyLine(name + " " + nameLowerCaseFirstChar + " = " + mapperName + "." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(id);");
        method.addBodyLine("if (" + nameLowerCaseFirstChar + " == null) {");
        method.addBodyLine("return null;");
        method.addBodyLine("}");
        method.addBodyLine("BeanUtils.copyProperties(" + nameLowerCaseFirstChar + ", " + rspLowerFirstChar + ");");
        method.addBodyLine("return " + rspLowerFirstChar + ";");
        topLevelClass.addMethod(method);
    }

    private static void addServiceCreateMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType aReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(aReturn);
        method.setName("create");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(param, "req"));

        FullyQualifiedJavaType mapper = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperName = CodeGeneratorUtil.firstCharToLowerCase(mapper.getShortName(), IS_FIRST_CHAR_REMOVED);

        String name = introspectedTable.getTableConfiguration().getDomainObjectName();
        String nameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(name, IS_FIRST_CHAR_REMOVED);

        method.addBodyLine(name + " " + nameLowerCaseFirstChar + " = new " + name + "();");
        method.addBodyLine("BeanUtils.copyProperties(req, " + nameLowerCaseFirstChar + ");");
        method.addBodyLine(aReturn.getShortName() + " i = " + mapperName + "." + introspectedTable.getInsertSelectiveStatementId() + "(" + nameLowerCaseFirstChar + ");");
        method.addBodyLine("return i;");
        topLevelClass.addMethod(method);
    }

    private static void addServiceUpdateByIdMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType aReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(aReturn);
        method.setName("updateById");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(param, "req"));

        FullyQualifiedJavaType mapper = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperName = CodeGeneratorUtil.firstCharToLowerCase(mapper.getShortName(), IS_FIRST_CHAR_REMOVED);

        String domainName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String domainNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(domainName, IS_FIRST_CHAR_REMOVED);

        method.addBodyLine(domainName + " " + domainNameLowerCaseFirstChar + " = " + mapperName + "." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(req.get" + CodeGeneratorUtil.firstCharToUpperCase(introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()) + "());");
        method.addBodyLine("if (" + domainNameLowerCaseFirstChar + " == null) {");
        method.addBodyLine("return 0;");
        method.addBodyLine("}");
        method.addBodyLine("BeanUtils.copyProperties(req, " + domainNameLowerCaseFirstChar + ");");
        method.addBodyLine(aReturn.getShortName() + " i = " + mapperName + "." + introspectedTable.getUpdateByPrimaryKeySelectiveStatementId() + "(" + domainNameLowerCaseFirstChar + ");");
        method.addBodyLine("return i;");
        topLevelClass.addMethod(method);
    }

    private static void addServiceDeleteByIdMethod(IntrospectedTable introspectedTable, TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType aReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(aReturn);
        method.setName("deleteById");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(param, "id"));

        FullyQualifiedJavaType mapper = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        String mapperName = CodeGeneratorUtil.firstCharToLowerCase(mapper.getShortName(), IS_FIRST_CHAR_REMOVED);

        String domainName = introspectedTable.getTableConfiguration().getDomainObjectName();
        String domainNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(domainName, IS_FIRST_CHAR_REMOVED);

        method.addBodyLine(domainName + " " + domainNameLowerCaseFirstChar + " = " + mapperName + "." + introspectedTable.getSelectByPrimaryKeyStatementId() + "(id);");
        method.addBodyLine("if (" + domainNameLowerCaseFirstChar + " == null) {");
        method.addBodyLine("return 0;");
        method.addBodyLine("}");
        method.addBodyLine(aReturn.getShortName() + " i = " + mapperName + "." + introspectedTable.getDeleteByPrimaryKeyStatementId() + "(" + domainNameLowerCaseFirstChar + ".get" + CodeGeneratorUtil.firstCharToUpperCase(introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()) + "());");
        method.addBodyLine("return i;");
        topLevelClass.addMethod(method);
    }

    private static void addFacadeQueryMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType service, FullyQualifiedJavaType serviceReturn, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("query");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(param, "param"));

        String serviceName = service.getShortName();
        String serviceNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(serviceName);

        String rspName = serviceReturn.getShortName();
        String rspNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(serviceReturn.getShortNameWithoutTypeArguments());

        method.addBodyLine(mainReturn.getShortName() + " response = new " + mainReturn.getShortName() + "();");
        method.addBodyLine(rspName + " " + rspNameLowerCaseFirstChar + " = " + serviceNameLowerCaseFirstChar + ".query(param);");
        method.addBodyLine("if (" + rspNameLowerCaseFirstChar + ".getTotal() > 0) {");
        method.addBodyLine("response.setResult(" + rspNameLowerCaseFirstChar + ");");
        method.addBodyLine("}");
        method.addBodyLine("return response;");
        topLevelClass.addMethod(method);
    }

    private static void addFacadeSelectByIdMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType service, FullyQualifiedJavaType serviceReturn, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("selectById");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(param, "id"));

        String serviceName = service.getShortName();
        String serviceNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(serviceName);

        String rspName = serviceReturn.getShortName();
        String rspLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(rspName);

        method.addBodyLine(mainReturn.getShortName() + " response = new " + mainReturn.getShortName() + "();");
        method.addBodyLine(rspName + " " + rspLowerCaseFirstChar + " = " + serviceNameLowerCaseFirstChar + ".selectById(id);");
        method.addBodyLine("if (" + rspLowerCaseFirstChar + " != null) {");
        method.addBodyLine("response.setResult(" + rspLowerCaseFirstChar + ");");
        method.addBodyLine("}");
        method.addBodyLine("return response;");
        topLevelClass.addMethod(method);
    }

    private static void addFacadeCreateMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType service, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("create");
        method.addAnnotation("@Override");
        method.addAnnotation("@Transactional");
        method.addParameter(new Parameter(param, "req"));

        String serviceName = service.getShortName();
        String serviceNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(serviceName);

        method.addBodyLine(mainReturn.getShortName() + " response = new " + mainReturn.getShortName() + "();");
        method.addBodyLine(FullyQualifiedJavaType.getIntInstance().getShortName() + " i = " + serviceNameLowerCaseFirstChar + "." + "create(req);");
        method.addBodyLine("response.setResult(i);");
        method.addBodyLine("return response;");
        topLevelClass.addMethod(method);
    }

    private static void addFacadeUpdateByIdMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType service, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("updateById");
        method.addAnnotation("@Override");
        method.addAnnotation("@Transactional");
        method.addParameter(new Parameter(param, "req"));

        String serviceName = service.getShortName();
        String serviceNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(serviceName);

        method.addBodyLine(mainReturn.getShortName() + " response = new " + mainReturn.getShortName() + "();");
        method.addBodyLine(FullyQualifiedJavaType.getIntInstance().getShortName() + " i = " + serviceNameLowerCaseFirstChar + "." + "updateById(req);");
        method.addBodyLine("response.setResult(i);");
        method.addBodyLine("return response;");
        topLevelClass.addMethod(method);
    }

    private static void addFacadeDeleteByIdMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType service, FullyQualifiedJavaType serviceReturn, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("deleteById");
        method.addAnnotation("@Override");
        method.addAnnotation("@Transactional");
        method.addParameter(new Parameter(param, "id"));

        String serviceName = service.getShortName();
        String serviceNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(serviceName);

        method.addBodyLine(mainReturn.getShortName() + " response = new " + mainReturn.getShortName() + "();");
        method.addBodyLine(FullyQualifiedJavaType.getIntInstance().getShortName() + " i = " + serviceNameLowerCaseFirstChar + "." + "deleteById(id);");
        method.addBodyLine("response.setResult(i);");
        method.addBodyLine("return response;");
        topLevelClass.addMethod(method);
    }

    private static void addControllerQueryMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType facade, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("query");
        method.addAnnotation("@RequestMapping(method = RequestMethod.GET, value = \"/query\")");
        method.addParameter(new Parameter(param, "param"));

        String facadeName = facade.getShortName();
        String facadeNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(facadeName);

        method.addBodyLine("return " + facadeNameLowerCaseFirstChar + ".query(param);");
        topLevelClass.addMethod(method);
    }

    private static void addControllerSelectByIdMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType facade, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("selectById");
        method.addAnnotation("@RequestMapping(method = RequestMethod.GET, value = \"/select\")");
        method.addParameter(new Parameter(param, "id"));

        String facadeName = facade.getShortName();
        String facadeNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(facadeName);

        method.addBodyLine("return " + facadeNameLowerCaseFirstChar + ".selectById(id);");
        topLevelClass.addMethod(method);
    }

    private static void addControllerCreateMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType facade, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("create");
        method.addAnnotation("@RequestMapping(method = RequestMethod.POST, value = \"/create\")");
        Parameter parameter = new Parameter(param, "req");
        parameter.addAnnotation("@RequestBody");
        method.addParameter(parameter);

        String facadeName = facade.getShortName();
        String facadeNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(facadeName);

        method.addBodyLine("return " + facadeNameLowerCaseFirstChar + ".create(req);");
        topLevelClass.addMethod(method);
    }

    private static void addControllerUpdateByIdMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType facade, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("updateById");
        method.addAnnotation("@RequestMapping(method = RequestMethod.POST, value = \"/update\")");
        Parameter parameter = new Parameter(param, "req");
        parameter.addAnnotation("@RequestBody");
        method.addParameter(parameter);

        String facadeName = facade.getShortName();
        String facadeNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(facadeName);

        method.addBodyLine("return " + facadeNameLowerCaseFirstChar + ".updateById(req);");
        topLevelClass.addMethod(method);
    }

    private static void addControllerDeleteByIdMethod(TopLevelClass topLevelClass, FullyQualifiedJavaType param, FullyQualifiedJavaType facade, FullyQualifiedJavaType mainReturn) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(mainReturn);
        method.setName("delete");
        method.addAnnotation("@RequestMapping(method = RequestMethod.POST, value = \"/delete\")");
        method.addParameter(new Parameter(param, "id"));

        String facadeName = facade.getShortName();
        String facadeNameLowerCaseFirstChar = CodeGeneratorUtil.firstCharToLowerCase(facadeName);

        method.addBodyLine("return " + facadeNameLowerCaseFirstChar + ".deleteById(id);");
        topLevelClass.addMethod(method);
    }
}
