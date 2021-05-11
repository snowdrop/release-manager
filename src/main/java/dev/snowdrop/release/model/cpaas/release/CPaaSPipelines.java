package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.json.JsonObject;
import java.util.List;

public class CPaaSPipelines {
    @JsonProperty
    private String name;
    @JsonProperty
    private Integer timeout;
    @JsonProperty
    private CPaaSNotification notifications;
    @JsonProperty
    private List<CPaaSStage> stages;

    public CPaaSPipelines() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public CPaaSNotification getNotifications() {
        return notifications;
    }

    public void setNotifications(CPaaSNotification notifications) {
        this.notifications = notifications;
    }

    public List<CPaaSStage> getStages() {
        return stages;
    }

    public void setStages(List<CPaaSStage> stages) {
        this.stages = stages;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSPipelines{");
        sb.append("name='").append(name).append('\'');
        sb.append(", timeout=").append(timeout);
        sb.append(", notifications=").append(notifications);
        sb.append(", stages=").append(stages);
        sb.append('}');
        return sb.toString();
    }
}
