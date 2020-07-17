package dev.snowdrop.jira;

import java.io.File;
import java.net.URL;

public class Utility {

    // Get file from classpath, resources folder
    protected static File getFileFromResources(String fileName) {
        ClassLoader classLoader = Utility.class.getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }
}
