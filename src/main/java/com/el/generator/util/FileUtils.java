package com.el.generator.util;

import java.io.File;

public class FileUtils {
    public static boolean isJavaFileExists(String targetPath, String fullyQualifiedName) {
        String filename = targetPath + "/" + (fullyQualifiedName.replaceAll("\\.", "/") + ".java");
        File file = new File(filename);
        return file.exists();
    }
}
