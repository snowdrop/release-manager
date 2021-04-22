package dev.snowdrop.release.services;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
public class TestProfiles  {
    public static class NoTags implements QuarkusTestProfile {
    }

    public static class CoreTags implements QuarkusTestProfile {
        @Override
        public Set<Class<?>> getEnabledAlternatives() {
            return Collections.singleton(MockJiraRestClient.class);
        }

        @Override
        public Set<String> tags() {
            return Collections.singleton("core");
        }
    }

    public static class IntegrationTests implements QuarkusTestProfile {
        @Override
        public Set<String> tags() {
            return Collections.singleton("it");
        }
    }

}
