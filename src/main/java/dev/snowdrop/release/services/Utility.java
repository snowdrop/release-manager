package dev.snowdrop.release.services;

import java.util.function.BinaryOperator;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class Utility {
    private static final DateTimeFormatter dateParser = ISODateTimeFormat.date();
    public static final String JIRA_SERVER = "https://issues.redhat.com/";
    public static final String JIRA_ISSUES_API = "https://issues.redhat.com/rest/api/2/";
    public static final MustacheFactory mf = new DefaultMustacheFactory();
    private static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormat.forPattern("dd MMM YYYY");
    
    public static DateTime fromIsoDate(String dateTimeSt) {
        return dateParser.parseDateTime(dateTimeSt);
    }
    
    /**
     * Parses dates using {@link #DEFAULT_DATE_FORMAT}.
     *
     * @param readableDate
     * @return
     */
    public static DateTime fromReadableDate(String readableDate) {
        return DEFAULT_DATE_FORMAT.parseDateTime(readableDate);
    }
    
    public static String getFormatted(String isoDate) {
        return getFormatted(fromIsoDate(isoDate));
    }
    
    public static String getFormatted(DateTime dateTime) {
        return dateTime.toString(DEFAULT_DATE_FORMAT);
    }
    
    public static String getURLFor(String issueKey) {
        return JIRA_SERVER + "browse/" + issueKey;
    }
    
    public static boolean isStringNullOrBlank(String s) {
        return s == null || s.isBlank();
    }
    
    public static BinaryOperator<String> errorsFormatter(int level) {
        StringBuilder indent = new StringBuilder(level % 2 == 0 ? "- " : "+ ");
        while (level-- > 0) {
            indent.insert(0, "   ");
        }
        return (s, s2) -> s + indent + s2 + "\n";
    }
}
