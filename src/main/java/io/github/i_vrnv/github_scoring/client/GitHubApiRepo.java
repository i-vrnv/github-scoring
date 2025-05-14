package io.github.i_vrnv.github_scoring.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.i_vrnv.github_scoring.model.GitHubRepository;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object (DTO) for GitHub API repository search results.
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

    /**
     * Converts this API DTO to the domain model used for score calculation.
     *
     * @return GitHubRepository domain object
     */
    public GitHubRepository toGitHubRepository() {
        return new GitHubRepository(
                stars,
                forks,
                updatedAt
        );
    }
}
