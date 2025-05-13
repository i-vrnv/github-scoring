package io.github.i_vrnv.github_scoring.model;

import io.github.i_vrnv.github_scoring.exception.InvalidRepositoryDataException;

import java.time.OffsetDateTime;

/**
 * Immutable record representing GitHub repository data used for score calculation.
 * Contains validation for its properties in the canonical constructor.
 */
public record GitHubRepository(
        int stars,
        int forks,
        OffsetDateTime updatedAt) {

    /**
     * Canonical constructor with validation of invariants.
     *
     * @param stars number of repository stars (must be non-negative)
     * @param forks number of repository forks (must be non-negative)
     * @param updatedAt date of last repository update (must not be in the future)
     * @throws InvalidRepositoryDataException if any invariant is violated
     */
    public GitHubRepository {
        if (stars < 0) {
            throw new InvalidRepositoryDataException("Stars count cannot be negative: " + stars);
        }
        if (forks < 0) {
            throw new InvalidRepositoryDataException("Forks count cannot be negative: " + forks);
        }
        if (updatedAt != null && updatedAt.isAfter(OffsetDateTime.now())) {
            throw new InvalidRepositoryDataException("Update date cannot be in the future: " + updatedAt);
        }
    }

    @Override
    public String toString() {
        return "GitHubRepository{" +
                "stars=" + stars +
                ", forks=" + forks +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
