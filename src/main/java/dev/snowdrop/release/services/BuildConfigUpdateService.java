package dev.snowdrop.release.services;

import com.atlassian.jira.rest.client.api.domain.Issue;
import dev.snowdrop.release.exception.JiraGavDescriptionNotParsableException;
import dev.snowdrop.release.exception.JiraGavNotMatchException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class BuildConfigUpdateService {
    private static final Logger LOG = Logger.getLogger(BuildConfigUpdateService.class);
    @Inject
    BuildConfigFactory buildConfigFactory;

    @Inject
    JiraIssueFactory jiraIssueFactory;

    @Inject
    IssueService issueSvc;

    @Inject
    GitService git;

    public void newMajorMinor(GitService.GitConfig buildConfigGitlabConfig, final String releaseMajorVersion, final String releaseMinorVersion, final String prevReleaseMajorVersion, final String prevReleaseMinorVersion) throws IOException {
            git.commitAndPush("chore: update release issues' key [issues-manager]", buildConfigGitlabConfig, repo -> {
                final String repoPath = repo.getAbsolutePath();
                LOG.infof("repo-> %s", repo.getAbsolutePath());
                final Path destPath = Paths.get(String.format(repoPath+"/spring-boot/%s.%s", releaseMajorVersion, releaseMinorVersion));
                final Path destFile = Paths.get(String.format(repoPath+"/spring-boot/%s.%s/build-config.yaml", releaseMajorVersion, releaseMinorVersion));
                if (!destFile.toFile().exists()) {
                    final Path sourceFile = Paths.get(String.format(repoPath + "/spring-boot/%s.%s/build-config.yaml", prevReleaseMajorVersion, prevReleaseMinorVersion));
                    try {
                        Files.createDirectories(destPath);
                        LOG.infof("sourceFile-> %s", sourceFile);
                        LOG.infof("destFile-> %s", destFile);
                        Files.copy(sourceFile, destFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Stream.of(destFile.toFile());
                } else {
                    return Stream.empty();
                }
            });
    }

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
            File buildConfigFile = buildConfigFactory.getBuildConfigRelativeTo(repo, releaseVersion);
            Map<String, String> variableMap = readVariablesFromFile(new FileInputStream(buildConfigFile));
            variableMap.put("majorVersion", releaseVersionMajorMinorFix[0]);
            variableMap.put("minorVersion", releaseVersionMajorMinorFix[1]);
            variableMap.put("patchVersion", releaseVersionMajorMinorFix[2]);
            variableMap.put("qualifier", qualifier);
            variableMap.put("milestone", milestone);
            BuildConfig buildConfigObj = buildConfigFactory.createFromRepo(buildConfigFile);
            release.getComponents().stream().forEach(component -> {
//            // TODO: Check if it's a product or a component only template
//            manageComponentOnly(component);
                manageProduct(component, variableMap);
            });

            buildConfigFactory.saveTo(buildConfigObj, generateVariableConfiguration(variableMap), buildConfigFile);
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
        try {
            jiraIssueFactory.extractGAVArrayForProduct(issue.getDescription());
            for (int arrayPos = 0; arrayPos < jiraIssueFactory.getGroupCount(); arrayPos++) {
                try {
                    final Map<String, String> gavMap = jiraIssueFactory.getInfoForProductGroup(arrayPos);
                    final String gavPrefix = gavMap.get(JiraIssueFactory.MAP_KEY_NAME_GAV);
                    final String productVersion = gavMap.get(JiraIssueFactory.MAP_KEY_NAME_UPSTREAM_VERSION);
                    final String supportedVersion = gavMap.get(JiraIssueFactory.MAP_KEY_NAME_SUPPORTED_VERSION);
                    final var gavProd = gavPrefix + "Prod";
                    if (variableMap.containsKey(gavProd)) {
                        if (!supportedVersion.startsWith("[")) {
                            variableMap.put(gavProd, supportedVersion);
                        }
                    } else {
                        variableMap.put(gavProd, (supportedVersion != null ? supportedVersion : ""));
                    }
                    final var gavUpstream = gavPrefix + "Upstream";
                    if (variableMap.containsKey(gavUpstream)) {
                        if (!productVersion.startsWith("[")) {
                            variableMap.put(gavUpstream, productVersion);
                        }
                    } else {
                        variableMap.put(gavUpstream, (productVersion != null ? productVersion : ""));
                    }
                } catch (JiraGavNotMatchException ex) {
                    // TODO: Add these errors to an error log report.
                    LOG.warnf("NO MATCH: ,%s---", ex.getMessage());
                }
            }
        } catch (JiraGavDescriptionNotParsableException ex) {
            // TODO: Add these errors to an error log report.
            LOG.warnf("INVALID DESCRIPTION: ,%s---", issue.getDescription());
        }
    }

}
