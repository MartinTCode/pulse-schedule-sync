package com.pulse.server.dto;

import java.util.List;

public class PublishResult {

    private final int published;
    private final int failed;
    private final List<Failure> failures;

    public PublishResult(int published, List<Failure> failures) {
        this.published = published;
        this.failures = failures;
        this.failed = failures == null ? 0 : failures.size();
    }

    public int getPublished() {
        return published;
    }

    public int getFailed() {
        return failed;
    }

    public List<Failure> getFailures() {
        return failures;
    }
}
