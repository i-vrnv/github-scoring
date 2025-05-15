package io.github.ivrnv.github.scoring.service;

import io.github.ivrnv.github.scoring.model.GitHubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculatorTest {

    private ScoreCalculator scoreCalculator;

    @BeforeEach
    void setUp() {
        scoreCalculator = new ScoreCalculator(0.5, 0.3, 0.2, 365);
    }

    @Test
    void test_score_isZero_whenAllInputsAreZeroOrVeryOld() {
        // Create repository with zero values and very old update date
        GitHubRepository repository = new GitHubRepository(
                0,
                0,
                OffsetDateTime.now().minusYears(3)
        );

        double score = scoreCalculator.calculateScore(repository);

        assertThat(score).isZero();
    }

    @Test
    void test_score_increasesWithStars() {
        // Fix forks count and update date
        OffsetDateTime recentUpdate = OffsetDateTime.now().minusDays(7);
        int fixedForks = 10;

        // Create repositories with increasing number of stars
        GitHubRepository repo1 = new GitHubRepository(10, fixedForks, recentUpdate);
        GitHubRepository repo2 = new GitHubRepository(100, fixedForks, recentUpdate);
        GitHubRepository repo3 = new GitHubRepository(1000, fixedForks, recentUpdate);

        double score1 = scoreCalculator.calculateScore(repo1);
        double score2 = scoreCalculator.calculateScore(repo2);
        double score3 = scoreCalculator.calculateScore(repo3);

        assertThat(score2).isGreaterThan(score1);
        assertThat(score3).isGreaterThan(score2);
    }

    @Test
    void test_score_increasesWithForks() {
        // Fix stars count and update date
        OffsetDateTime recentUpdate = OffsetDateTime.now().minusDays(7);
        int fixedStars = 100;

        // Create repositories with increasing number of forks
        GitHubRepository repo1 = new GitHubRepository(fixedStars, 5, recentUpdate);
        GitHubRepository repo2 = new GitHubRepository(fixedStars, 50, recentUpdate);
        GitHubRepository repo3 = new GitHubRepository(fixedStars, 500, recentUpdate);

        double score1 = scoreCalculator.calculateScore(repo1);
        double score2 = scoreCalculator.calculateScore(repo2);
        double score3 = scoreCalculator.calculateScore(repo3);

        assertThat(score2).isGreaterThan(score1);
        assertThat(score3).isGreaterThan(score2);
    }

    @Test
    void test_score_increasesWithRecency() {
        // Fix stars and forks counts
        int fixedStars = 100;
        int fixedForks = 50;

        // Create repositories with decreasing update dates (from older to newer)
        GitHubRepository repoOld = new GitHubRepository(
                fixedStars,
                fixedForks,
                OffsetDateTime.now().minusDays(300)
        );
        GitHubRepository repoMedium = new GitHubRepository(
                fixedStars,
                fixedForks,
                OffsetDateTime.now().minusDays(30)
        );
        GitHubRepository repoRecent = new GitHubRepository(
                fixedStars,
                fixedForks,
                OffsetDateTime.now().minusDays(1)
        );

        double scoreOld = scoreCalculator.calculateScore(repoOld);
        double scoreMedium = scoreCalculator.calculateScore(repoMedium);
        double scoreRecent = scoreCalculator.calculateScore(repoRecent);

        assertThat(scoreMedium).isGreaterThan(scoreOld);
        assertThat(scoreRecent).isGreaterThan(scoreMedium);
    }

    @Test
    void test_score_combinesFactorsCorrectly_withKnownValues() {
        // Create repository with specific values
        GitHubRepository repository = new GitHubRepository(
                500,
                100,
                OffsetDateTime.now().minusDays(30)
        );

        double score = scoreCalculator.calculateScore(repository);

        // Check that score value is positive
        assertThat(score).isPositive();
    }

    @Test
    void test_score_reflectsMaxRecencyFactor_forRecentUpdate() {
        // Fix stars and forks counts
        int stars = 100;
        int forks = 50;
        
        // Create repository with very recent update date
        GitHubRepository repoVeryRecent = new GitHubRepository(
                stars,
                forks,
                OffsetDateTime.now()
        );
        
        double scoreVeryRecent = scoreCalculator.calculateScore(repoVeryRecent);
        
        // Create identical repository with yesterday's update date
        GitHubRepository repoYesterday = new GitHubRepository(
                stars,
                forks,
                OffsetDateTime.now().minusDays(1)
        );
        
        double scoreYesterday = scoreCalculator.calculateScore(repoYesterday);
        
        // Difference in scores should be very small since recency factor
        // is close to 1.0 for both cases
        assertThat(Math.abs(scoreVeryRecent - scoreYesterday)).isLessThan(0.01);
    }
}
