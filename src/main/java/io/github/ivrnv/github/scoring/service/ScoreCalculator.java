package io.github.ivrnv.github.scoring.service;

import io.github.ivrnv.github.scoring.model.GitHubRepository;
import org.springframework.beans.factory.annotation.Value;
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

    private final double starsWeight;
    private final double forksWeight;
    private final double recencyWeight;
    private final long maxDaysForRecency;

    public ScoreCalculator(
            @Value("${github.scoring.weights.stars}") double starsWeight,
            @Value("${github.scoring.weights.forks}") double forksWeight,
            @Value("${github.scoring.weights.recency}") double recencyWeight,
            @Value("${github.scoring.max-days-for-recency}") long maxDaysForRecency) {
        this.starsWeight = starsWeight;
        this.forksWeight = forksWeight;
        this.recencyWeight = recencyWeight;
        this.maxDaysForRecency = maxDaysForRecency;
    }

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

        return (starsWeight * starsScore) +
               (forksWeight * forksScore) +
               (recencyWeight * recencyFactor);
    }

    /**
     * Calculates repository recency factor based on the last update date.
     * Factor varies from 0 (outdated) to 1 (very recent).
     * <p>
     * The formula 1.0 - ((double) daysSinceUpdate / maxDaysForRecency) creates a linear scale where:
     * If a repository was updated today (daysSinceUpdate = 0), the result is 1.0 (100% recency)
     * If a repository was updated maxDaysForRecency days ago (365 days or 1 year by default), the result is 0 (0% recency)
     * For updates between these extremes, the score decreases linearly as the repository gets older
     * <p>
     * For example: <br>
     * Updated today: 1.0 - (0/365) = 1.0 (100% recency) <br>
     * Updated 6 months ago (~182 days): 1.0 - (182/365) ≈ 0.5 (50% recency) <br>
     * Updated 10 months ago (~304 days): 1.0 - (304/365) ≈ 0.17 (17% recency) <br>
     * This recency factor then contributes to the overall repository score based on the recencyWeight.
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
        
        // If update is older than maxDaysForRecency, factor is 0
        if (daysSinceUpdate >= maxDaysForRecency) {
            return 0;
        }
        
        // Linear function from 1 (today) to 0 (maxDaysForRecency days ago or older)
        return 1.0 - ((double) daysSinceUpdate / maxDaysForRecency);
    }
}
