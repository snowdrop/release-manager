package dev.snowdrop.release.services;

import com.atlassian.jira.rest.client.api.domain.Issue;
import dev.snowdrop.release.model.Component;
import dev.snowdrop.release.model.Release;
import dev.snowdrop.release.model.buildconfig.BuildConfig;
import org.apache.commons.lang3.text.WordUtils;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class AutoUpdateService {
    private static final Logger LOG = Logger.getLogger(AutoUpdateService.class);
    private static final String GAV_CAMEL_CASE_REGEX_PATTERN = "[ -\\.]";
    Matcher GAV_NAME_REGEX_PATTERN = Pattern.compile("([\\*\\+\\s]*)([a-zA-Z\\.\\-]*)(:)([a-zA-Z\\-]*)(\\s\\(.*[\\r\\n]?)").matcher("");
    @Inject
    BuildConfigFactory factory;

    @Inject
    IssueService issueSvc;

    /**
     * <p>Parses the comments from the yaml file to obtain variable definitions. These start with #!.</p>
     *
     * @param buildConfig <p>Contents of the Build Configuration file</p>
     * @return
     */
    private static Map<String, String> readVariablesFromFile(InputStream buildConfig) {
        String contents = "";
        @SuppressWarnings("resource")
        Scanner s = new Scanner(buildConfig);

        s.useDelimiter("\\A");

        if (s.hasNext()) {
            contents = s.next();
        }
        LinkedHashMap<String, String> variables = new LinkedHashMap<>();
        // Look for the variable defs i.e. #!myVariable=1.0.0
        List<String> matches = getAllMatches(contents, "#![a-zA-Z0-9_-]*=.*");
        for (String temp : matches) {
            String[] nameValue = temp.split("=", 2);
            // remove the !# of the name
            variables.put(nameValue[0].substring(2), nameValue[1]);
        }
        return variables;
    }

    private static List<String> getAllMatches(String text, String regex) {
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(text);
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }

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

    /**
     * <p>Main process for updating the Build Configuration file.</p>
     *
     * @param repo
     * @param release        <p>Release object</p>
     * @param releaseVersion <p>Release version number</p>
     * @param qualifier
     * @param milestone
     * @return
     * @throws Throwable
     */
    public File updateBuildConfig(File repo, Release release, final String releaseVersion, final String qualifier, final String milestone) {
        try {
            final String[] releaseMMFA = releaseVersion.split("\\.");
            File buildConfigFile = factory.getBuildConfigRelativeTo(repo, releaseVersion);
            Map<String, String> variableMap = readVariablesFromFile(new FileInputStream(buildConfigFile));
            variableMap.put("majorVersion", releaseMMFA[0]);
            variableMap.put("minorVersion", releaseMMFA[1]);
            variableMap.put("patchVersion", releaseMMFA[2]);
            variableMap.put("qualifier", qualifier);
            variableMap.put("milestone", milestone);
            BuildConfig buildConfigObj = factory.createFromRepo(buildConfigFile);
            final String[] releaseMMF = releaseVersion.split("\\.");
            final String releaseMM = releaseMMF[0] + "." + releaseMMF[1];
            release.getComponents().stream().forEach(component -> {
//            // TODO: Check if it's a product or a component only template
//            manageComponentOnly(component);
                manageProduct(component, variableMap);
            });

            factory.saveTo(buildConfigObj, generateVariableConfiguration(variableMap), buildConfigFile);
            return buildConfigFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateVariableConfiguration(Map<String, String> variableMap) {
        return variableMap.entrySet()
            .stream()
            .map(entry -> "#!" + entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private void manageProduct(Component component, Map<String, String> variableMap) {
        final Issue issue = issueSvc.getIssue(component);
        final String description = issue.getDescription();
        final String versionSection = description.substring(description.lastIndexOf("==="));
        final String[] artifactArr = versionSection.split("\\n[\\n \\r]*");
        // Skip the 1st 2 lines
        for (int arrayPos = 0; arrayPos < ((artifactArr.length - 1) / 5); arrayPos++) {
            final String gavText = artifactArr[arrayPos * 5 + 1];
            LOG.infof("gavText: %s", gavText);
            if (GAV_NAME_REGEX_PATTERN.reset(gavText).matches()) {
                final String gid = GAV_NAME_REGEX_PATTERN.group(2);
                final String aid = GAV_NAME_REGEX_PATTERN.group(4);
                final String varName = WordUtils.capitalizeFully(aid, '-', '.').replaceAll("-", "");
                final String productNameText = artifactArr[arrayPos * 5 + 1 + 1];
                final String productVersionText = artifactArr[arrayPos * 5 + 1 + 2];
                final String productVersion = productVersionText.split(":")[1];
                final String supportedVersionText = artifactArr[arrayPos * 5 + 1 + 3];
                final String supportedVersion = supportedVersionText.split(":")[1];
                final String eolText = artifactArr[arrayPos * 5 + 1 + 4];
                final var gavProd = toCamelCase(gid) + toCamelCase(aid) + "Prod";
                if (variableMap.containsKey(gavProd)) {
                    if (!supportedVersion.stripLeading().startsWith("[")) {
                        variableMap.put(gavProd, supportedVersion.stripLeading().stripTrailing());
                    }
                } else {
                    variableMap.put(gavProd, (supportedVersion != null ? supportedVersion : "").stripLeading().stripTrailing());
                }
                final var gavUpstream = toCamelCase(gid) + toCamelCase(aid) + "Upstream";
                if (variableMap.containsKey(gavUpstream)) {
                    if (!productVersion.stripLeading().startsWith("[")) {
                        variableMap.put(gavUpstream, productVersion.stripLeading().stripTrailing());
                    }
                } else {
                    variableMap.put(gavUpstream, (productVersion != null ? productVersion : "").stripLeading().stripTrailing());
                }
            } else {
                LOG.warnf("NO MATCH: ,%s---", gavText);
            }
        }
    }

}
