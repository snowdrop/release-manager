/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package dev.snowdrop.release.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.snowdrop.release.model.buildconfig.BuildConfig;

import java.io.*;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@Singleton
public class BuildConfigFactory {
    public static final String BUILD_CONFIG_FILE_NAME = "build-config.yaml";
    private static final Logger LOG = Logger.getLogger(BuildConfigFactory.class);
    private static final YAMLMapper MAPPER = new YAMLMapper();

    static {
        MAPPER.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS,
            MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS);
        final var factory = MAPPER.getFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        factory.disable(YAMLGenerator.Feature.SPLIT_LINES);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    void saveTo(BuildConfig buildConfig, final String variableMap, File to) throws IOException {
        final var writer = MAPPER.writerFor(BuildConfig.class);
        final var buildConfigStr = writer.writeValueAsString(buildConfig);
        FileOutputStream fos = new FileOutputStream(to);
        OutputStreamWriter outStream = new OutputStreamWriter(fos);
        outStream.write("###############################################################################\r\n");
        outStream.write("\r\n");
        outStream.write("# Variable definition\r\n");
        outStream.write("# WARNING: this section is updated automatically with the issues-manager\r\n");
        outStream.write("\r\n");
        outStream.write(variableMap);
        outStream.write("\r\n");
        outStream.write("####################### AUTOMATIC UPDATE SECTION FINISH ########################\r\n");
        outStream.write("\r\n");
        outStream.write(buildConfigStr);
        outStream.close();
    }

    public File getBuildConfigRelativeTo(File repo, String version) {
        final String[] releaseMMF = version.split("\\.");
        final var releaseVersionMajMin = releaseMMF[0] + "." + releaseMMF[1];
        final String relativePath = "spring-boot/" + releaseVersionMajMin + "/" + BUILD_CONFIG_FILE_NAME;
        return new File(repo, relativePath);
    }
    public BuildConfig createFromRepo(File buildConfigFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(buildConfigFile);
        BuildConfig buildConfig = MAPPER.readValue(inputStream, BuildConfig.class);
        System.out.println("Loaded release " + buildConfig.getVersion() + "." + buildConfig.getMilestone());
        return buildConfig;
    }
}
