package io.github.ivrnv.github.scoring.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Top-level response from GitHub Search Repositories API.
 */
public record GitHubApiResponse(
        @JsonProperty("total_count") Integer totalCount,
        @JsonProperty("incomplete_results") Boolean incompleteResults,
        @JsonProperty("items") List<GitHubApiRepo> repositories
) {
    /**
     * Constructor with defensive copy for repositories list
     */
    public GitHubApiResponse {
        repositories = repositories != null ? List.copyOf(repositories) : List.of();
    }
}
