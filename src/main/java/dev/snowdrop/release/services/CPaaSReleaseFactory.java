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
import dev.snowdrop.release.model.CVE;
import dev.snowdrop.release.model.cpaas.ReleaseMustache;
import dev.snowdrop.release.model.cpaas.SecurityImpactEnum;
import dev.snowdrop.release.model.cpaas.product.CPaaSProductFile;
import dev.snowdrop.release.model.cpaas.release.CPaaSReleaseFile;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
@Singleton
public class CPaaSReleaseFactory {

    static final Logger LOG = Logger.getLogger(CPaaSReleaseFactory.class);

    private static final YAMLMapper MAPPER = new YAMLMapper();

    private static final String RELEASE_TEMPLATE = "cpaas_release.mustache";

    @Inject
    CVEService cveService;

    static {
        MAPPER.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS);
        final var factory = MAPPER.getFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public CPaaSProductFile createCPaaSProductFromStream(InputStream cpaasProductIs) throws IOException {
        CPaaSProductFile cpaaSProduct = MAPPER.readValue(cpaasProductIs, CPaaSProductFile.class);
        LOG.infof("Loaded cpaas product %s for release version %s", cpaaSProduct.getProduct().getName(), cpaaSProduct.getProduct().getRelease().getVersion());
        return cpaaSProduct;
    }

    public CPaaSReleaseFile createCPaaSReleaseFromTemplate(String release, String previousRelease, boolean createAdvisory, boolean isSecurityAdvisory, List<CVE> cveList) throws IOException {
        StringWriter writer = new StringWriter();
        List<String> advisoryCve = new ArrayList<>(cveList.size());
        String securityImpact = null;
        if (cveList.size() > 0) {
            advisoryCve = cveService.cveToAdvisory(cveList);
            Optional<CVE> maxImpactCVE = cveList.stream().max((i, j) -> i.getImpact().getGreater(j.getImpact().getValue()));
            securityImpact = SecurityImpactEnum.getLevelForPriority(maxImpactCVE.get().getImpact().getValue());
        }
        ReleaseMustache releaseMustache = new ReleaseMustache(createAdvisory, isSecurityAdvisory, advisoryCve, securityImpact, release, previousRelease);
        Utility.mf.compile(RELEASE_TEMPLATE).execute(writer, releaseMustache).flush();
        CPaaSReleaseFile cpaaSRelease = MAPPER.readValue(new ByteArrayInputStream(writer.toString().getBytes()), CPaaSReleaseFile.class);
        LOG.infof("Loaded cpaas release %s ", cpaaSRelease.getRelease().getName());
        return cpaaSRelease;
    }

    void saveTo(Object cpaasObject, File to, Class writeClass) {
        final var writer = MAPPER.writerFor(writeClass);
        try {
            writer.writeValue(to, cpaasObject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
