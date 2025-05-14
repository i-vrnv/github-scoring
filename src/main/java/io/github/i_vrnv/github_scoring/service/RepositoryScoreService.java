package io.github.i_vrnv.github_scoring.service;

import io.github.i_vrnv.github_scoring.client.GitHubApiRepo;
import io.github.i_vrnv.github_scoring.client.GitHubClient;
import io.github.i_vrnv.github_scoring.model.GitHubRepository;
import io.github.i_vrnv.github_scoring.model.ScoredRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for fetching repositories from GitHub and calculating their popularity scores.
 */
@Service
public class RepositoryScoreService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryScoreService.class);
    
    private final GitHubClient gitHubClient;
    private final ScoreCalculator scoreCalculator;

    public RepositoryScoreService(GitHubClient gitHubClient, ScoreCalculator scoreCalculator) {
        this.gitHubClient = gitHubClient;
        this.scoreCalculator = scoreCalculator;
    }

    /**
     * Retrieves GitHub repositories matching the provided criteria and calculates their popularity scores.
     *
     * @param language     The programming language to filter repositories by
     * @param createdAfter The minimum creation date for repositories
     * @return A list of repositories with their calculated popularity scores
     */
    public List<ScoredRepository> getScoredRepositories(String language, LocalDate createdAfter) {
        try {
            List<GitHubApiRepo> repositories = gitHubClient.fetchRepositories(language, createdAfter);
            
            return repositories.stream()
                .map(this::convertToScoredRepository)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error while fetching or scoring repositories", e);
            return Collections.emptyList();
        }
    }

    private ScoredRepository convertToScoredRepository(GitHubApiRepo repo) {
        GitHubRepository repoForScoring = new GitHubRepository(
            repo.stars(),
            repo.forks(),
            repo.updatedAt()
        );
        
        double score = scoreCalculator.calculateScore(repoForScoring);
        
        return new ScoredRepository(
            repo.name(),
            repo.owner().login(),
            repo.url(),
            repo.stars(),
            repo.forks(),
            repo.updatedAt(),
            score
        );
    }
}
