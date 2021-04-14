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
import dev.snowdrop.release.exception.JiraGavDescriptionNotParsableException;
import dev.snowdrop.release.exception.JiraGavNotMatchException;
import dev.snowdrop.release.model.buildconfig.BuildConfig;
import org.jboss.logging.Logger;

import javax.inject.Singleton;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="ant@redhat.com">Antonio Costa</a>
 */
@Singleton
public class JiraIssueFactory {
    public static final String MAP_KEY_NAME_GAV = "GAV_NAME";
    public static final String MAP_KEY_NAME_UPSTREAM_VERSION = "UPSTREAM_VERSION";
    public static final String MAP_KEY_NAME_SUPPORTED_VERSION = "SUPPORTED_VERSION";
    private static final Logger LOG = Logger.getLogger(JiraIssueFactory.class);
    private static final String JIRA_DESC_SPLITTER_REGEX_PATTERN = "\\n[\\n \\r]*";
    private static final String GAV_CAMEL_CASE_REGEX_PATTERN = "[ -\\.]";
    private static final Matcher GAV_NAME_REGEX_PATTERN = Pattern.compile("([\\*\\+\\s]*)([0-9a-zA-Z\\.\\-]*):([0-9a-zA-Z\\-]*)\\s\\(.*[\\r\\n]?").matcher("");

    private static final int ELEMENTS_IN_PROD_GAV_GROUP = 5;

    private static final int ELEMENTS_IN_COMP_GAV_GROUP = 5;

    private String[] gavArray;
    private int groupCount = 0;

    public static String toCamelCase(final String init) {
        if (init == null)
            return null;
        final StringBuilder ret = new StringBuilder(init.length());
        for (final String word : init.split(GAV_CAMEL_CASE_REGEX_PATTERN)) {
            if (!word.isEmpty()) {
                ret.append(Character.toUpperCase(word.charAt(0)));
                ret.append(word.substring(1).toLowerCase());
            }
        }
        return ret.toString();
    }

    public void extractGAVArrayForProduct(final String jiraDescription) throws JiraGavDescriptionNotParsableException {
        if (!jiraDescription.contains("===")) {
            throw new JiraGavDescriptionNotParsableException();
        }
        final String versionSection = jiraDescription.substring(jiraDescription.lastIndexOf("==="));
        gavArray = versionSection.split(JIRA_DESC_SPLITTER_REGEX_PATTERN);
        if (gavArray.length <= 0) {
            throw new JiraGavDescriptionNotParsableException();
        }
        groupCount = ((gavArray.length - 1) / ELEMENTS_IN_PROD_GAV_GROUP);
        if (groupCount <= 0) {
            throw new JiraGavDescriptionNotParsableException();
        }
    }

    public Map<String, String> getInfoForProductGroup(final int groupNumber) throws JiraGavNotMatchException {
        final int elementStartingPosition = groupNumber * ELEMENTS_IN_PROD_GAV_GROUP + 1;
        final String gavText = gavArray[elementStartingPosition];
        Map<String, String> retMap = new HashMap<>(3);
        if (GAV_NAME_REGEX_PATTERN.reset(gavText).matches()) {
            final String gid = GAV_NAME_REGEX_PATTERN.group(2);
            final String aid = GAV_NAME_REGEX_PATTERN.group(3);
            final String gavPrefix = toCamelCase(gid) + toCamelCase(aid);
            final String productVersionText = gavArray[elementStartingPosition + 2];
            final String productVersion = productVersionText.split(":")[1].trim();
            final String supportedVersionText = gavArray[elementStartingPosition + 3];
            final String supportedVersion = supportedVersionText.split(":")[1].trim();
            retMap.put(MAP_KEY_NAME_GAV, gavPrefix);
            retMap.put(MAP_KEY_NAME_UPSTREAM_VERSION, productVersion);
            retMap.put(MAP_KEY_NAME_SUPPORTED_VERSION, supportedVersion);
            return retMap;
        } else {
            LOG.warnf("NO MATCH: ,%s---", gavText);
            throw new JiraGavNotMatchException(gavText);
        }
    }

    public String[] getGavArray() {
        return gavArray;
    }

    public int getGroupCount() {
        return groupCount;
    }

}
