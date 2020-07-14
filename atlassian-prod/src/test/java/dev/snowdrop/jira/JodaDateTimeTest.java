package dev.snowdrop.jira;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static dev.snowdrop.jira.atlassian.Utility.toDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JodaDateTimeTest {

    @Test
    public void checkMonthTest() {
        String date = "2020-08-18";
        DateTime jodaDate = toDateTime(date);
        assertEquals("18 Aug 2020",jodaDate.toString("dd MMM YYYY"));
    }

}
