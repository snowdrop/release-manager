package dev.snowdrop.release.services;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;
import com.google.common.base.CaseFormat;
import dev.snowdrop.release.model.Component;
import dev.snowdrop.release.model.IssueSource;
import dev.snowdrop.release.model.Release;
import dev.snowdrop.release.model.buildconfig.BuildConfig;
import io.atlassian.util.concurrent.Promise;
import org.apache.commons.lang3.text.WordUtils;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.snowdrop.release.model.Issue.LINK_TYPE;
import static dev.snowdrop.release.services.Utility.*;

@ApplicationScoped
public class AutoUpdateService {
    private static final Logger LOG = Logger.getLogger(AutoUpdateService.class);

    Matcher GAV_NAME_REGEX_PATTERN = Pattern.compile("([\\*\\+\\s]*)([a-zA-Z\\.\\-]*)(:)([a-zA-Z\\-]*)(\\s\\(.*)").matcher("");

    @Inject
    BuildConfigFactory factory;

    @Inject
    GitlabService gitlab;

    private String gitRef;

    //    @Inject
//    GitService git;

    @Inject
    IssueService issueSvc;

    @Inject
    JiraRestClient restClient;

    public void start(Release releaseObj,final String gitRef, final String releaseVersion, final String qualifier, final String milestone, final String username, final String token) throws Throwable {
        LOG.infof("#initRepository(%s,***;%s)...", gitRef, releaseVersion);
        this.gitRef = gitRef;
        gitlab.initRepository(gitRef, releaseVersion, username, token);
        updateBuildConfig(releaseObj, releaseVersion, qualifier, milestone);
        LOG.infof("#initRepository(%s,***;%s)!", gitRef, releaseVersion);
    }


    /**
     *
     * @param release <p>Release object</p>
     * @param releaseVersion <p>Release version number</p>
     * @throws Throwable
     */
    public void updateBuildConfig(Release release, final String releaseVersion, final String qualifier, final String milestone) throws Throwable {
        LOG.infof("#getComponentRequests(Release)...");
        LOG.debugf("  -> release: %s", release);
        final String[] releaseMMF = releaseVersion.split("\\.");
        final String releaseMM = releaseMMF[0]+"."+releaseMMF[1];
        BuildConfig buildConfigObj = factory.createFromGitRef(gitRef, releaseVersion);
        LOG.infof("buildConfigObj: %s", buildConfigObj);
        buildConfigObj.setVersion(releaseVersion);
        buildConfigObj.setMilestone(milestone);
        buildConfigObj.setGroup("spring-boot-"+releaseMM+"-all");
//        buildConfigObj.getBuilds().
        LOG.infof("buildConfigObj: %s", buildConfigObj);
//        release.getComponents().stream().forEach(component -> {
//            // TODO: Check if it's a product or a component only template
////            manageComponentOnly(component);
//            manageProduct(component);
//        });
        factory.pushChanges(buildConfigObj);
    }

    private void manageProduct(Component component) {
        LOG.infof("#manageProduct: %s", component);
        final Issue issue = issueSvc.getIssue(component);
        LOG.infof("issue: %s", issue.getKey());
        final String description = issue.getDescription();
//            LOG.infof("description: %s", description);
        final String versionSection = description.substring(description.lastIndexOf("==="));
        LOG.infof("versionSection: %s", versionSection);
//            final String[] artifactArr = versionSection.split("[ ]*\\* \\+\\*[a-zA-Z\\,\\.\\(\\:\\-)]*");
        final String[] artifactArr = versionSection.split("\\n[\\n \\r]*");
        LOG.infof("artifactArr: %s", Arrays.toString(artifactArr));
        // Skip the 1st 2 lines
        for (int arrayPos = 0; arrayPos < ((artifactArr.length - 1) / 5); arrayPos++) {
            final String gavText = artifactArr[arrayPos * 5 + 1];
            LOG.infof("GAV: %d, %s", arrayPos, gavText);
            if (GAV_NAME_REGEX_PATTERN.reset(gavText).matches()) {
                final String gid = GAV_NAME_REGEX_PATTERN.group(2);
                final String aid = GAV_NAME_REGEX_PATTERN.group(4);
                final String varName = WordUtils.capitalizeFully(aid, '-', '.').replaceAll("-", "");
//                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, aid);
                LOG.infof("gid: %s; aid: %s; varName: %s", gid, aid, varName);
                final String productNameText = artifactArr[arrayPos * 5 + 1 + 1];
                final String productVersionText = artifactArr[arrayPos * 5 + 1 + 2];
                final String supportedVersionText = artifactArr[arrayPos * 5 + 1 + 3];
                final String supportedVersion = supportedVersionText.split(":")[1];
                final String eolText = artifactArr[arrayPos * 5 + 1 + 4];
                LOG.infof("product name: %d, %s", arrayPos, productNameText);
                LOG.infof("product name: %d, %s", arrayPos, productNameText.split(":")[1]);
                LOG.infof("product version: %d, %s", arrayPos, productVersionText.split(":")[1]);
                LOG.infof("supported version: %d, %s", arrayPos, supportedVersion);
                LOG.infof("end of support: %d, %s", arrayPos, eolText.split(":")[1]);
                updateBuildConfiguration(aid, supportedVersion);
            } else {
                LOG.warnf("NO MATCH: ,%s---", gavText);
            }
        }
    }


    public void updateBuildConfiguration(final String artifactName, final String artifactVersion) {
        LOG.infof("#updateBuildConfiguration(%s, %s)...", artifactName, artifactVersion);
        LOG.infof("#updateBuildConfiguration(String, String).", artifactName, artifactVersion);
    }
}
