/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.snowdrop.release.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class CVE {
    private final String key;
    private final String summary;
    private final String resolution;
    private final List<String> fixVersions = new LinkedList<>();
    private final String status;
    private String impact;
    private long bugzilla;
    private final List<Blocker> blockedBy = new LinkedList<>();
    private String id;
    
    
    public CVE(String key, String summary, String resolution, Iterable<String> fixVersions, String status) {
        this.key = key;
        this.summary = summary;
        this.resolution = resolution;
        fixVersions.forEach(s -> this.fixVersions.add(s));
        this.status = status;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setImpact(String impact) {
        this.impact = impact;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setBugzilla(String bugzilla) {
        this.bugzilla = Long.parseLong(bugzilla);
    }
    
    public String getId() {
        return id;
    }
    
    public long getBugzilla() {
        return bugzilla;
    }
    
    public List<String> getFixVersions() {
        return fixVersions;
    }
    
    public Optional<String> getRevisit() {
        final var revisit = blockedBy.stream()
            .map(Blocker::getRevisit)
            .filter(Optional::isPresent)
            .map(o -> "- " + o.get())
            .collect(Collectors.joining("\n"));
        return revisit.isBlank() ? Optional.empty() : Optional.of(revisit);
    }
    
    public List<Blocker> getBlockedBy() {
        return blockedBy;
    }
    
    public void addBlocker(Blocker blocker) {
        blockedBy.add(blocker);
    }
    
    @FunctionalInterface
    public interface StatusReporter {
        String getStatus();
    }
    
    public static class Blocker {
        private final StatusReporter reporter;
        private String revisit;
        
        public Blocker(StatusReporter reporter) {
            this.reporter = reporter;
        }
        
        public Optional<String> getRevisit() {
            return Optional.ofNullable(revisit);
        }
        
        public void setRevisit(String revisit) {
            this.revisit = revisit;
        }
        
        @Override
        public String toString() {
            return reporter.getStatus();
        }
    }
}
