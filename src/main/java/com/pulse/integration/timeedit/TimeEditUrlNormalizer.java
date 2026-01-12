package com.pulse.integration.timeedit;

import java.net.URI;

public final class TimeEditUrlNormalizer {

	private TimeEditUrlNormalizer() {
	}

	public static String ensureJsonUrl(String timeeditUrl) {
		if (timeeditUrl == null) {
			return null;
		}

		URI uri = URI.create(timeeditUrl);
		String path = uri.getPath();
		if (path == null || path.isBlank() || "/".equals(path)) {
			throw new IllegalArgumentException("URL path is empty");
		}

		String normalizedPath = normalizePathToJson(path);
		if (normalizedPath.equals(path)) {
			return timeeditUrl;
		}

		try {
			URI normalized = new URI(
					uri.getScheme(),
					uri.getAuthority(),
					normalizedPath,
					uri.getRawQuery(),
					uri.getRawFragment()
			);
			return normalized.toString();
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to normalize URL to .json", e);
		}
	}

	private static String normalizePathToJson(String path) {
		if (path.endsWith(".json")) {
			return path;
		}

		if (path.endsWith(".html")) {
			return path.substring(0, path.length() - ".html".length()) + ".json";
		}

		int lastSlash = path.lastIndexOf('/');
		String lastSegment = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
		if (lastSegment.isBlank()) {
			throw new IllegalArgumentException("URL path ends with '/'");
		}

		if (lastSegment.contains(".")) {
			int lastDot = path.lastIndexOf('.');
			if (lastDot <= lastSlash) {
				return path + ".json";
			}
			return path.substring(0, lastDot) + ".json";
		}

		return path + ".json";
	}
}
