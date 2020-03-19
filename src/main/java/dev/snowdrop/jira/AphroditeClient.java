package dev.snowdrop.jira;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.set.aphrodite.Aphrodite;
import org.jboss.set.aphrodite.common.Utils;
import org.jboss.set.aphrodite.spi.AphroditeException;

public class AphroditeClient {

    private static Aphrodite client;
    private static final Log LOG = LogFactory.getLog(AphroditeClient.class);
    public static final String JSON_FILE_PROPERTY = "aphrodite.json";

    public static void main(String[] argv) {
        System.setProperty("aphrodite.config", JSON_FILE_PROPERTY);
        init();
    }

    private static void init() {
        try {
            client = Aphrodite.instance();
        } catch (AphroditeException e) {
            Utils.logException(LOG, e);
        }
    }
}
