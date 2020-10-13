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

import java.util.Optional;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class CVE {
    private final String key;
    private final String summary;
    private final String resolution;
    private final Iterable<String> fixVersions;
    private final String status;
    private String impact;
    private final String lastUpdate;
    private long bugzilla;
    private Iterable<Blocked> blockedBy;
    private String id;
    
    public CVE(String key, String summary, String resolution, Iterable<String> fixVersions, String status, String lastUpdate) {
        this.key = key;
        this.summary = summary;
        this.resolution = resolution;
        this.fixVersions = fixVersions;
        this.status = status;
        this.lastUpdate = lastUpdate;
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
    
    public Iterable<String> getFixVersions() {
        return fixVersions;
    }
    
    public String getLastUpdate() {
        return lastUpdate;
    }
    
    public static class Blocked {
        private final String by;
        private final Optional<String> until;
        
        public Blocked(String by, Optional<String> until) {
            this.by = by;
            this.until = until;
        }
    }
}
