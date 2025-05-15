package io.github.ivrnv.github.scoring.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Model for GitHub API repository search results.
 * Contains only the fields needed for our repository scoring application.
 */
public record GitHubApiRepo(
        String name,
        Owner owner,
        @JsonProperty("html_url") String url,
        @JsonProperty("stargazers_count") int stars,
        @JsonProperty("forks_count") int forks,
        @JsonProperty("updated_at") OffsetDateTime updatedAt
) {
    /**
     * Owner details from GitHub API response.
     */
    public record Owner(
            String login,
            @JsonProperty("html_url") String url
    ) {}
}
