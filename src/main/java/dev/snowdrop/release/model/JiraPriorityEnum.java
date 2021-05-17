package dev.snowdrop.release.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum JiraPriorityEnum {

    TRIVIAL("Trivial"),
    OPTIONAL("Optional"),
    MINOR("Minor"),
    MAJOR("Major"),
    CRITICAL("Critical"),
    BLOCKER("Blocker");

    private String value;

    private final static Map<String, Integer> PRIORITY = new HashMap<>() {{
        put("Trivial", 1);
        put("Optional", 2);
        put("Minor", 3);
        put("Major", 4);
        put("Critical", 5);
        put("Blocker", 6);
    }};

    JiraPriorityEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public Integer getGreater(final String newLevel) {
        return (PRIORITY.get(value) > PRIORITY.get(newLevel)) ? PRIORITY.get(value) : PRIORITY.get(newLevel);
    }

}
