package com.pulse.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Response returned from Server -> GUI after publishing to Canvas.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferResult {

    @JsonProperty("published")
    private int published;

    @JsonProperty("failures")
    private List<Failure> failures = new ArrayList<>();

    public TransferResult() {
    }

    public TransferResult(int published, List<Failure> failures) {
        this.published = published;
        this.failures = failures;
    }

    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }

    public List<Failure> getFailures() {
        return failures;
    }

    public void setFailures(List<Failure> failures) {
        this.failures = failures;
    }

    // Convenience helpers for GUI
    public int getFailureCount() {
        return failures == null ? 0 : failures.size();
    }

    public int getSuccessCount() {
        return published;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Failure {

        @JsonProperty("externalId")
        private String externalId;

        @JsonProperty("message")
        private String message;

        public Failure() {
        }

        public Failure(String externalId, String message) {
            this.externalId = externalId;
            this.message = message;
        }

        public String getExternalId() {
            return externalId;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
