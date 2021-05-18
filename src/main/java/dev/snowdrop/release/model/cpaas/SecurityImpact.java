package dev.snowdrop.release.model.cpaas;

import com.fasterxml.jackson.annotation.JsonValue;
import dev.snowdrop.release.model.JiraPriority;

import java.util.HashMap;
import java.util.Map;

public enum SecurityImpact {

    LOW("Low",1),
    MODERATE("Moderate",2),
    IMPORTANT("Important",3),
    CRITICAL("Critical",4);

    private String impact;

    private Integer level;

    private final static Map<String, Integer> PRIORITY = new HashMap<>() {{
        put(JiraPriority.TRIVIAL.getName(), 1);
        put(JiraPriority.OPTIONAL.getName(), 2);
        put(JiraPriority.MINOR.getName(), 3);
        put(JiraPriority.MAJOR.getName(), 4);
        put(JiraPriority.CRITICAL.getName(), 5);
        put(JiraPriority.BLOCKER.getName(), 6);
    }};

    private final static Map<Integer, SecurityImpact> PRIORITY_LEVEL_MAP = new HashMap<>() {{
        put(1,LOW);
        put(2,LOW);
        put(3,MODERATE);
        put(4,IMPORTANT);
        put(5,CRITICAL);
        put(6,CRITICAL);
    }};


    SecurityImpact(String impact, Integer level) {
        this.impact = impact;
        this.level = level;
    }

    @JsonValue
    public String getImpact() {
        return impact;
    }

    public void setImpact(String value) {
        this.impact = value;
    }

    public Integer getLevel() {
        return getLevel();
    }

    public static String getImpactForPriority(final String priority) {
        return PRIORITY_LEVEL_MAP.get(PRIORITY.get(priority)).impact;
    }
}
