package dev.snowdrop.release.model.cpaas;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Comparator;

public enum SecurityImpact {

    LOW("Low",1),
    MODERATE("Moderate",2),
    IMPORTANT("Important",3),
    CRITICAL("Critical",4);

    private String impact;

    private Integer level;

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

    public static Comparator<SecurityImpact> impactLevelComparator = new Comparator<SecurityImpact>() {
        @Override
        public int compare(SecurityImpact d1, SecurityImpact d2) {
            return Integer.compare(d1.level, d2.level);
        }
    };
}
