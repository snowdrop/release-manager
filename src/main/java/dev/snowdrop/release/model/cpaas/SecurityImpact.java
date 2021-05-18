package dev.snowdrop.release.model.cpaas;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum SecurityImpactEnum {

    LOW("Low",1),
    MODERATE("Moderate",2),
    IMPORTANT("Important",3),
    CRITICAL("Critical",4);

    private String value;

    private Integer level;

    private final static Map<String, Integer> PRIORITY = new HashMap<>() {{
        put("Trivial", 1);
        put("Optional", 2);
        put("Minor", 3);
        put("Major", 4);
        put("Critical", 5);
        put("Blocker", 6);
    }};

    private final static Map<Integer, SecurityImpactEnum> PRIORITY_LEVEL_MAP = new HashMap<>() {{
        put(1,LOW);
        put(2,LOW);
        put(3,MODERATE);
        put(4,IMPORTANT);
        put(5,CRITICAL);
        put(6,CRITICAL);
    }};


    SecurityImpactEnum(String value, Integer level) {
        this.value = value;
        this.level = level;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getLevel() {
        return getLevel();
    }

    public static String getLevelForPriority(final String priority) {
        return PRIORITY_LEVEL_MAP.get(PRIORITY.get(priority)).value;
    }
}
