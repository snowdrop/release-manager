package dev.snowdrop.release.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum JiraPriority {

    TRIVIAL("Trivial"),
    OPTIONAL("Optional"),
    MINOR("Minor"),
    MAJOR("Major"),
    CRITICAL("Critical"),
    BLOCKER("Blocker");

    private String name;

    private final static Map<String, Integer> PRIORITY = new HashMap<>() {{
        put(TRIVIAL.getName(), 1);
        put(OPTIONAL.getName(), 2);
        put(MINOR.getName(), 3);
        put(MAJOR.getName(), 4);
        put(CRITICAL.getName(), 5);
        put(BLOCKER.getName(), 6);
    }};

    JiraPriority(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public Integer getGreater(final String newLevel) {
        return (PRIORITY.get(name) > PRIORITY.get(newLevel)) ? PRIORITY.get(name) : PRIORITY.get(newLevel);
    }

}
