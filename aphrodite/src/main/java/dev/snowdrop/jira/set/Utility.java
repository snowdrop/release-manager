package dev.snowdrop.jira.set;

public class Utility {

    protected static String template = "The snowdrop team is really pleased to contact you as we will release in 12 weeks a new Snowdrop BOM based on the following Spring Boot version: 2.3.0.RELEASE\n" +
            "As product owner of the following component and/or starter: A.B.C, we would like to know if you plan to release a new component (see version defined within the BOM file) that we could test in 8 weeks.\n" +
            "If you don't plan to release a new component/starter, could you please test and control if your component will continue to work with this version of spring boot and patch it for CVEs.\n" +
            "\n" +
            "The EOL of this Snowdrop release is scheduled: yyyy/Mm/dd\n" +
            "\n" +
            "We expect to get from you:\n" +
            "\n" +
            "Product name and version : Code Name - Version (e.g. JWS - 5.3.1)\n" +
            "Artifact / Component name & Version supported: Apache Tomcat - 9.0.30-redhat-00001\n" +
            "PNC/Indy URL : http://indy.psi.redhat.com/browse/maven\n" +
            "MRRC URL (if already released) : https://maven.repository.redhat.com/ga/\n" +
            "where A.B.C could be: Apache Tomcat Embed, Undertow, Resteasy, ...\"";
}
