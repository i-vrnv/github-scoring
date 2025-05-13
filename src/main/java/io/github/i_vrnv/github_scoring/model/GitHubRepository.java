package io.github.i_vrnv.github_scoring.model;

import java.time.OffsetDateTime;

public record GitHubRepository(
        int stars,
        int forks,
        OffsetDateTime updatedAt) {

    @Override
    public String toString() {
        return "GitHubRepositoryInput{" +
                "stars=" + stars +
                ", forks=" + forks +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
