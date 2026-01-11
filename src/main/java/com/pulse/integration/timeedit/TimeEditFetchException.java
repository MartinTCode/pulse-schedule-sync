package com.pulse.integration.timeedit;

import com.pulse.util.ErrorCode;

public class TimeEditFetchException extends RuntimeException {

	private final ErrorCode errorCode;
	private final Integer upstreamStatus;

	public TimeEditFetchException(ErrorCode errorCode, String message, Integer upstreamStatus) {
		super(message);
		this.errorCode = errorCode;
		this.upstreamStatus = upstreamStatus;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public Integer getUpstreamStatus() {
		return upstreamStatus;
	}
}
