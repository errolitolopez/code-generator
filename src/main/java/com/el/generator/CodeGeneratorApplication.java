package com.el.generator;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CodeGeneratorApplication {

    private static final Logger logger = LoggerFactory.getLogger(CodeGeneratorApplication.class);

    private final List<String> warnings = new ArrayList<>();

    public static void main(String[] args) {
        try {
            CodeGeneratorApplication cga = new CodeGeneratorApplication();
            cga.generate();
        } catch (Exception e) {
            logger.error("{}", e.getMessage(), e);
        }
    }

    public void generate() throws Exception {
        logger.info("invoke generate");
        File configFile = new File("mbg-config.xml");
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);

        for (String warning : warnings) {
            if (warning.contains("was overwritten")) {
                logger.warn("{}", warning);
            } else {
                logger.error("error: {}", warning);
            }
        }
    }
}
