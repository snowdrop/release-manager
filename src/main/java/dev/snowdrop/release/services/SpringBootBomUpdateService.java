package dev.snowdrop.release.services;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

@ApplicationScoped
public class SpringBootBomUpdateService {
    private static final Logger LOG = Logger.getLogger(SpringBootBomUpdateService.class);

    @Inject
    GitService git;

    public void newMajorMinor(GitService.GitConfig bomGitConfig) throws IOException {
        git.commitAndPush("chore: update release issues' key [issues-manager]", bomGitConfig, repo -> {
            LOG.infof("repo-> %s", repo.getAbsolutePath());
            FileFilter fileFilter = new WildcardFileFilter("release-*.yml");
            File[] files = repo.listFiles(fileFilter);
            if (files != null) {
                return Arrays.stream(files).map(file-> {
                    LOG.infof("file-> %s", file.getAbsoluteFile());
                    file.delete();
                    return file;
                });
            }
            return Stream.empty();
        });
    }
}
