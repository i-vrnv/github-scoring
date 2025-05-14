package io.github.i_vrnv.github_scoring.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the top-level response from GitHub Search Repositories API.
 * The GitHub API returns a structure with total_count, incomplete_results flag,
 * and an items array containing the actual repositories.
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
