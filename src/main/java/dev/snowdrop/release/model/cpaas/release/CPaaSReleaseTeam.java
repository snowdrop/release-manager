package dev.snowdrop.release.model.cpaas.release;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class CPaaSReleaseTeam {
    @JsonProperty
    private List<String> recipients;
    @JsonProperty("send-when")
    private String sendWhen;

    public CPaaSReleaseTeam() {
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getSendWhen() {
        return sendWhen;
    }

    public void setSendWhen(String sendWhen) {
        this.sendWhen = sendWhen;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CPaaSReleaseTeam{");
        sb.append("recipients=").append(recipients);
        sb.append(", sendWhen=").append(sendWhen);
        sb.append('}');
        return sb.toString();
    }
}
