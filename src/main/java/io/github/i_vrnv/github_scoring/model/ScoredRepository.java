package io.github.i_vrnv.github_scoring.model;

import java.time.OffsetDateTime;

/**
 * DTO representing a GitHub repository with its calculated popularity score.
 * This is the main object returned by the service layer.
 */
public record ScoredRepository(
    String name,
    String owner,
    String url,
    int stars,
    int forks,
    OffsetDateTime lastUpdated,
    double popularityScore
) {
}
