package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SendWhenEnum {
    ALWAYS("always"),
    NEVER("never"),
    ON_SUCCESS("on-success"),
    ON_FAILURE("on-failure");

    SendWhenEnum(String value) {
        this.value = value;
    }

    private String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
