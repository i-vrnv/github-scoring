package io.github.ivrnv.github.scoring.service;

import io.github.ivrnv.github.scoring.model.GitHubRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Component for calculating GitHub repository popularity score.
 * Takes into account three factors:
 * 1. Number of stars - higher values increase the score
 * 2. Number of forks - higher values increase the score
 * 3. Repository recency - more recent updates increase the score
 */
@Component
public class ScoreCalculator {

    // Weights for each factor in the scoring formula
    private static final double STARS_WEIGHT = 0.5;    // 50% impact on the score
    private static final double FORKS_WEIGHT = 0.3;    // 30% impact on the score
    private static final double RECENCY_WEIGHT = 0.2;  // 20% impact on the score

    // Maximum period in days after which recency is considered zero
    private static final long MAX_DAYS_FOR_RECENCY = 365; // 1 year

    /**
     * Calculates the overall popularity score for a repository.
     *
     * @param repository object with repository data
     * @return numerical popularity score
     */
    public double calculateScore(GitHubRepository repository) {
        // Use logarithmic scale for stars and forks
        // This reduces the excessive influence of very large values and smooths the difference
        double starsScore = Math.log10(repository.stars() + 1); // +1 to avoid log(0)
        double forksScore = Math.log10(repository.forks() + 1);

        double recencyFactor = calculateRecencyFactor(repository.updatedAt());

        return (STARS_WEIGHT * starsScore) +
               (FORKS_WEIGHT * forksScore) +
               (RECENCY_WEIGHT * recencyFactor);
    }

    /**
     * Calculates repository recency factor based on the last update date.
     * Factor varies from 0 (outdated) to 1 (very recent).
     * <p>
     * The formula 1.0 - ((double) daysSinceUpdate / MAX_DAYS_FOR_RECENCY) creates a linear scale where:
     * If a repository was updated today (daysSinceUpdate = 0), the result is 1.0 (100% recency)
     * If a repository was updated MAX_DAYS_FOR_RECENCY days ago (365 days or 1 year), the result is 0 (0% recency)
     * For updates between these extremes, the score decreases linearly as the repository gets older
     * <p>
     * For example: <br>
     * Updated today: 1.0 - (0/365) = 1.0 (100% recency) <br>
     * Updated 6 months ago (~182 days): 1.0 - (182/365) ≈ 0.5 (50% recency) <br>
     * Updated 10 months ago (~304 days): 1.0 - (304/365) ≈ 0.17 (17% recency) <br>
     * This recency factor then contributes 20% to the overall repository score (based on RECENCY_WEIGHT = 0.2).
     *
     * @param updatedAt date of the last repository update
     * @return recency factor from 0 to 1
     */
    private double calculateRecencyFactor(OffsetDateTime updatedAt) {
        if (updatedAt == null) {
            return 0;
        }

        // Calculate days since last update
        long daysSinceUpdate = ChronoUnit.DAYS.between(updatedAt, OffsetDateTime.now());
        
        // If update is older than MAX_DAYS_FOR_RECENCY, factor is 0
        if (daysSinceUpdate >= MAX_DAYS_FOR_RECENCY) {
            return 0;
        }
        
        // Linear function from 1 (today) to 0 (MAX_DAYS_FOR_RECENCY days ago or older)
        return 1.0 - ((double) daysSinceUpdate / MAX_DAYS_FOR_RECENCY);
    }
}
