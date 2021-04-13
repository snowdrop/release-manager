package dev.snowdrop.release.services;

import com.atlassian.jira.rest.client.api.domain.Issue;
import dev.snowdrop.release.model.Component;
import dev.snowdrop.release.model.Release;
import dev.snowdrop.release.model.buildconfig.BuildConfig;
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
public class BuildConfigUpdateService {
    private static final Logger LOG = Logger.getLogger(BuildConfigUpdateService.class);
    private static final String GAV_CAMEL_CASE_REGEX_PATTERN = "[ -\\.]";
    Matcher GAV_NAME_REGEX_PATTERN = Pattern.compile("([\\*\\+\\s]*)([0-9a-zA-Z\\.\\-]*):([0-9a-zA-Z\\-]*)\\s\\(.*[\\r\\n]?").matcher("");
    private static final int ELEMENTS_IN_GAV_GROUP=5;
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
            final String[] releaseVersionMajorMinorFix = releaseVersion.split("\\.");
            File buildConfigFile = factory.getBuildConfigRelativeTo(repo, releaseVersion);
            Map<String, String> variableMap = readVariablesFromFile(new FileInputStream(buildConfigFile));
            variableMap.put("majorVersion", releaseVersionMajorMinorFix[0]);
            variableMap.put("minorVersion", releaseVersionMajorMinorFix[1]);
            variableMap.put("patchVersion", releaseVersionMajorMinorFix[2]);
            variableMap.put("qualifier", qualifier);
            variableMap.put("milestone", milestone);
            BuildConfig buildConfigObj = factory.createFromRepo(buildConfigFile);
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
        int elementStartingPosition = 0;
        for (int arrayPos = 0; arrayPos < ((artifactArr.length - 1) / ELEMENTS_IN_GAV_GROUP); arrayPos++) {
            elementStartingPosition = arrayPos * ELEMENTS_IN_GAV_GROUP + 1;
            final String gavText = artifactArr[elementStartingPosition];
            if (GAV_NAME_REGEX_PATTERN.reset(gavText).matches()) {
                final String gid = GAV_NAME_REGEX_PATTERN.group(2);
                final String aid = GAV_NAME_REGEX_PATTERN.group(3);
                final String productVersionText = artifactArr[elementStartingPosition + 2];
                final String productVersion = productVersionText.split(":")[1].trim();
                final String supportedVersionText = artifactArr[elementStartingPosition + 3];
                final String supportedVersion = supportedVersionText.split(":")[1].trim();
                final var gavProd = toCamelCase(gid) + toCamelCase(aid) + "Prod";
                if (variableMap.containsKey(gavProd)) {
                    if (!supportedVersion.startsWith("[")) {
                        variableMap.put(gavProd, supportedVersion);
                    }
                } else {
                    variableMap.put(gavProd, (supportedVersion != null ? supportedVersion : ""));
                }
                final var gavUpstream = toCamelCase(gid) + toCamelCase(aid) + "Upstream";
                if (variableMap.containsKey(gavUpstream)) {
                    if (!productVersion.startsWith("[")) {
                        variableMap.put(gavUpstream, productVersion);
                    }
                } else {
                    variableMap.put(gavUpstream, (productVersion != null ? productVersion : ""));
                }
            } else {
                LOG.warnf("NO MATCH: ,%s---", gavText);
            }
        }
    }

}
