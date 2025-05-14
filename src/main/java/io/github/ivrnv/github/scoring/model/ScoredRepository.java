package io.github.ivrnv.github.scoring.model;

import java.time.OffsetDateTime;

/**
 * Model representing a GitHub repository with its calculated popularity score.
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
