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

import dev.snowdrop.release.model.CVE;
import dev.snowdrop.release.model.cpaas.SecurityImpact;
import dev.snowdrop.release.model.cpaas.product.*;
import dev.snowdrop.release.model.cpaas.release.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@QuarkusTest
@TestProfile(TestProfiles.CoreTags.class)
public class CPaaSReleaseFactoryTest {
    private static final String RELEASE = "2.4.0";
    private static final String PREVIOUS_RELEASE = "2.3.6";
    private static final String CVE_SUMMARY_JIRA_1 = "CVE-2020-11996 tomcat: specially crafted sequence of HTTP/2 requests can lead to DoS [springboot-2]";
    private static final String CVE_SUMMARY_JIRA_2 = "CVE-2020-25638 hibernate-core: SQL injection vulnerability when both hibernate.use_sql_comments and JPQL String literals are used [springboot-2.3]";
    private static final String CVE_SUMMARY_CPAAS_1 = "tomcat: specially crafted sequence of HTTP/2 requests can lead to DoS (CVE-2020-11996)";
    private static final String CVE_SUMMARY_CPAAS_2 = "hibernate-core: SQL injection vulnerability when both hibernate.use_sql_comments and JPQL String literals are used (CVE-2020-25638)";

    @Inject
    CPaaSReleaseFactory factory;

    @Inject
    CVEService cveService;

    private List<CVE> getCVEListForTesting() {
        List<CVE> cveList = new ArrayList<CVE>(2) {{
            CVE cve = new CVE("9999", CVE_SUMMARY_JIRA_1, new ArrayList<>(1) {{
                add("2.4.0");
            }}, "", DateTime.now());
            cve.setImpact(SecurityImpact.LOW);
            add(cve);
            cve = new CVE("9999", CVE_SUMMARY_JIRA_2, new ArrayList<>(1) {{
                add("2.4.0");
            }}, "", DateTime.now());
            cve.setImpact(SecurityImpact.IMPORTANT);
            add(cve);
            cve = new CVE("9998", CVE_SUMMARY_JIRA_1, new ArrayList<>(1) {{
                add("2.4.0");
            }}, "", DateTime.now());
            cve.setImpact(SecurityImpact.MODERATE);
            add(cve);
        }};
        return cveList;
    }

    @Test
    public void checkProductParsing() throws Throwable {
        CPaaSProductFile productFile = factory.createCPaaSProductFromStream(HelperFunctions.getResourceAsStream("cpaas/product.yml"));
        CPaaSProduct product = productFile.getProduct();
        assertEquals("2.3.6", product.getRelease().getVersion());
        List<CPaaSProject> projects = product.getProjects();
        assertEquals(1, projects.size());
        CPaaSProject project = projects.get(0);
        assertEquals("spring-boot-parent", project.getName());
        assertEquals(1, project.getAdvisories().size());
        assertEquals("rhoar", project.getAdvisories().get(0));
        List<CPaaSComponent> components = project.getComponents();
        assertEquals(1, components.size());
        CPaaSComponent component = components.get(0);
        assertEquals("spring-boot", component.getName());
        List<CPaaSBuild> builds = component.getBuilds();
        assertEquals(1, builds.size());
        CPaaSBuild build = builds.get(0);
        assertEquals("pig", build.getType());
        assertEquals("latest", build.getPigVersion());
        List<CPaaSShip> ships = build.getShip();
        assertEquals(1, ships.size());
        CPaaSShip ship = ships.get(0);
        assertEquals("dev.snowdrop", ship.getArtifact().getGroup());
    }

    @Test
    public void checkReleaseTemplateParsingWithoutSecurityAdvisory() throws Throwable {
        List<CVE> cveList = getCVEListForTesting();
        CPaaSReleaseFile release = factory.createCPaaSReleaseFromTemplate(RELEASE, PREVIOUS_RELEASE, false, cveList);
        List<CPaaSPipelines> pipelines = release.getRelease().getPipelines();
        assertEquals(2, pipelines.size());
        pipelines.forEach(pipeline -> {
            if ("build".equalsIgnoreCase(pipeline.getName())) {
                List<CPaaSStage> stages = pipeline.getStages();
                stages.forEach(stage -> {
                    if ("post-build-checks".equalsIgnoreCase(stage.getName())) {
                        assertTrue(stage.getEnabled());
                    }
                });
            } else if ("release".equalsIgnoreCase(pipeline.getName())) {
                List<CPaaSStage> stages = pipeline.getStages();
                stages.forEach(stage -> {
                    if ("create-errata-tool-advisories".equalsIgnoreCase(stage.getName())) {
                        assertFalse(stage.getEnabled());
                    }
                });
            }
        });
        List<CPaaSTool> tools = release.getRelease().getTools();
        tools.forEach(tool -> {
            if ("errata".equalsIgnoreCase(tool.getType())) {
                final List<CPaaSAdvisory> advisories = tool.getAdvisories();
                advisories.forEach(advisory -> {
                    if ("RHOAR".equalsIgnoreCase(advisory.getName())) {
                        assertEquals("RHSA", advisory.getAdvisoryType());
                        assertTrue(advisory.getDescription().contains(CVE_SUMMARY_CPAAS_1));
                        assertTrue(advisory.getDescription().contains(CVE_SUMMARY_CPAAS_2));
                        assertTrue(advisory.getDescription().contains(RELEASE));
                        assertTrue(advisory.getDescription().contains(PREVIOUS_RELEASE));
                        assertTrue(advisory.getSynopsis().contains(RELEASE));
                        assertTrue(advisory.getSynopsis().contains("security update"), advisory.getSynopsis());
                        assertEquals(SecurityImpact.IMPORTANT.getImpact(), advisory.getSecurityImpact());
                        assertTrue(advisory.getSolution().startsWith("Before applying the update"), advisory.getSolution());
                        assertTrue(advisory.getTopic().startsWith("An update is now available for Red Hat OpenShift Application Runtimes."), advisory.getTopic());
                        assertTrue(advisory.getTopic().contains("Red Hat Product Security has rated"), advisory.getTopic());
                    }
                });
            }
        });
    }

    @Test
    public void checkReleaseTemplateParsingWithSecurityAdvisory() throws Throwable {
        List<CVE> cveList = getCVEListForTesting();
        CPaaSReleaseFile release = factory.createCPaaSReleaseFromTemplate(RELEASE, PREVIOUS_RELEASE, true, cveList);
        List<CPaaSPipelines> pipelines = release.getRelease().getPipelines();
        assertEquals(2, pipelines.size());
        pipelines.forEach(pipeline -> {
            if ("build".equalsIgnoreCase(pipeline.getName())) {
                List<CPaaSStage> stages = pipeline.getStages();
                stages.forEach(stage -> {
                    if ("create-delivery-repo".equalsIgnoreCase(stage.getName())) {
                        assertFalse(stage.getEnabled());
                    }
                });
            } else if ("build".equalsIgnoreCase(pipeline.getName())) {
                List<CPaaSStage> stages = pipeline.getStages();
                stages.forEach(stage -> {
                    if ("create-errata-tool-advisories".equalsIgnoreCase(stage.getName())) {
                        assertTrue(stage.getEnabled());
                    }
                });
            }
        });
        List<CPaaSTool> tools = release.getRelease().getTools();
        tools.forEach(tool -> {
            if ("errata".equalsIgnoreCase(tool.getType())) {
                final List<CPaaSAdvisory> advisories = tool.getAdvisories();
                advisories.forEach(advisory -> {
                    if ("RHOAR".equalsIgnoreCase(advisory.getName())) {
                        assertEquals("RHSA", advisory.getAdvisoryType());
                        assertTrue(advisory.getDescription().contains(CVE_SUMMARY_CPAAS_1));
                        assertTrue(advisory.getDescription().contains(CVE_SUMMARY_CPAAS_2));
                        assertTrue(advisory.getDescription().contains(RELEASE));
                        assertTrue(advisory.getDescription().contains(PREVIOUS_RELEASE));
                        assertTrue(advisory.getSynopsis().contains(RELEASE));
                        assertTrue(advisory.getSynopsis().contains("security update"), advisory.getSynopsis());
                        assertEquals(SecurityImpact.IMPORTANT.getImpact(), advisory.getSecurityImpact());
                        assertTrue(advisory.getSolution().startsWith("Before applying the update"), advisory.getSolution());
                        assertTrue(advisory.getTopic().startsWith("An update is now available for Red Hat OpenShift Application Runtimes."), advisory.getTopic());
                        assertTrue(advisory.getTopic().contains("Red Hat Product Security has rated"), advisory.getTopic());
                    }
                });
            }
        });
    }

    @Test
    public void advisoryWithEmptyCVEListIsRHBAAndDoesntHaveSecurityImpact() throws Throwable {
        List<CVE> cveList = new ArrayList<CVE>(0);
        CPaaSReleaseFile release = factory.createCPaaSReleaseFromTemplate(RELEASE, PREVIOUS_RELEASE, true, cveList);
        List<CPaaSPipelines> pipelines = release.getRelease().getPipelines();
        assertEquals(2, pipelines.size());
        pipelines.forEach(pipeline -> {
            if ("build".equalsIgnoreCase(pipeline.getName())) {
                List<CPaaSStage> stages = pipeline.getStages();
                stages.forEach(stage -> {
                    if ("create-errata-tool-advisories".equalsIgnoreCase(stage.getName())) {
                        assertTrue(stage.getEnabled());
                    }
                });
            }
        });
        List<CPaaSTool> tools = release.getRelease().getTools();
        tools.forEach(tool -> {
            if ("errata".equalsIgnoreCase(tool.getType())) {
                final List<CPaaSAdvisory> advisories = tool.getAdvisories();
                advisories.forEach(advisory -> {
                    if ("RHOAR".equalsIgnoreCase(advisory.getName())) {
                        assertEquals("RHBA", advisory.getAdvisoryType());
                        assertFalse(advisory.getDescription().contains(CVE_SUMMARY_CPAAS_1));
                        assertFalse(advisory.getDescription().contains(CVE_SUMMARY_CPAAS_2));
                        assertTrue(advisory.getDescription().contains(RELEASE));
                        assertTrue(advisory.getDescription().contains(PREVIOUS_RELEASE));
                        assertTrue(advisory.getSynopsis().contains(RELEASE), advisory.getSynopsis());
                        assertFalse(advisory.getSynopsis().contains("security update"), advisory.getSynopsis());
                        assertEquals(String.format("Red Hat support for Spring Boot %s update", RELEASE), advisory.getSynopsis());
                        assertNull(advisory.getSecurityImpact());
                        assertTrue(advisory.getSolution().startsWith("Before applying the update"), advisory.getSolution());
                        assertTrue(advisory.getTopic().startsWith("An update is now available for Red Hat OpenShift Application Runtimes."), advisory.getTopic());
                        assertFalse(advisory.getTopic().contains("Red Hat Product Security has rated"), advisory.getTopic());
                    }
                });
            }
        });
    }

    @Test
    public void checkCVEDescription() throws Throwable {
        List<CVE> cveList = getCVEListForTesting();
        final List<String> cpaasCVE = cveService.cveToAdvisory(cveList);
        assertEquals(3, cpaasCVE.size());
        assertEquals(CVE_SUMMARY_CPAAS_1, cpaasCVE.get(0));
        assertEquals(CVE_SUMMARY_CPAAS_2, cpaasCVE.get(1));
    }

}