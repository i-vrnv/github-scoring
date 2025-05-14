package io.github.i_vrnv.github_scoring.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing error responses from the GitHub API.
 * Used for better error handling when API calls fail.
 */
public record GitHubErrorResponse(
        String message,
        @JsonProperty("documentation_url") String documentationUrl
) {}
