package com.pulse.server.dto;

public class Failure {

    private final String externalId;
    private final String reason;

    public Failure(String externalId, String reason) {
        this.externalId = externalId;
        this.reason = reason;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getReason() {
        return reason;
    }
}
