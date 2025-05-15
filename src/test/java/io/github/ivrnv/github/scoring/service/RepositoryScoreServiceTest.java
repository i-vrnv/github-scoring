package io.github.ivrnv.github.scoring.service;

import io.github.ivrnv.github.scoring.client.GitHubApiRepo;
import io.github.ivrnv.github.scoring.client.GitHubClient;
import io.github.ivrnv.github.scoring.model.ScoredRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryScoreServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    private RepositoryScoreService sut;

    @BeforeEach
    void setUp() {
        ScoreCalculator scoreCalculator = new ScoreCalculator(0.5, 0.3, 0.2, 365);
        sut = new RepositoryScoreService(gitHubClient, scoreCalculator);
    }

    @Test
    void returnsScoredRepositories_whenClientReturnsData() {
        // Given
        var language = "java";
        var createdAfter = LocalDate.parse("2023-01-01");

        OffsetDateTime recentUpdateTime = OffsetDateTime.now().minusDays(5);

        GitHubApiRepo repo1 = createGitHubApiRepo("repo1", "owner1", "https://github.com/owner1/repo1",
                500, 50, recentUpdateTime);
        GitHubApiRepo repo2 = createGitHubApiRepo("repo2", "owner2", "https://github.com/owner2/repo2",
                200, 20, recentUpdateTime.minusDays(30));

        List<GitHubApiRepo> mockRepos = List.of(repo1, repo2);

        when(gitHubClient.fetchRepositories(language, createdAfter)).thenReturn(mockRepos);

        // When
        List<ScoredRepository> result = sut.getScoredRepositories(language, createdAfter);

        // Then
        assertThat(result).hasSize(2);

        // Verify first repository
        ScoredRepository scoredRepo1 = result.getFirst();
        assertThat(scoredRepo1.name()).isEqualTo("repo1");
        assertThat(scoredRepo1.owner()).isEqualTo("owner1");
        assertThat(scoredRepo1.url()).isEqualTo("https://github.com/owner1/repo1");
        assertThat(scoredRepo1.stars()).isEqualTo(500);
        assertThat(scoredRepo1.forks()).isEqualTo(50);
        assertThat(scoredRepo1.lastUpdated()).isEqualTo(recentUpdateTime);

        // For the second repository, just verify it exists with the expected data
        ScoredRepository scoredRepo2 = result.get(1);
        assertThat(scoredRepo2.name()).isEqualTo("repo2");
        assertThat(scoredRepo2.owner()).isEqualTo("owner2");
        assertThat(scoredRepo2.url()).isEqualTo("https://github.com/owner2/repo2");
        assertThat(scoredRepo2.stars()).isEqualTo(200);
        assertThat(scoredRepo2.forks()).isEqualTo(20);
        assertThat(scoredRepo2.lastUpdated()).isEqualTo(recentUpdateTime.minusDays(30));
    }

    @Test
    void returnsEmptyList_whenClientReturnsEmpty() {
        // Given
        String language = "java";
        var createdAfter = LocalDate.parse("2023-01-01");

        when(gitHubClient.fetchRepositories(language, createdAfter)).thenReturn(Collections.emptyList());
        
        // When
        List<ScoredRepository> result = sut.getScoredRepositories(language, createdAfter);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void handlesExceptionFromClient() {
        // Given
        String language = "java";
        var createdAfter = LocalDate.parse("2023-01-01");

        when(gitHubClient.fetchRepositories(language, createdAfter)).thenThrow(new RuntimeException("API Error"));
        
        // When
        List<ScoredRepository> result = sut.getScoredRepositories(language, createdAfter);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    private GitHubApiRepo createGitHubApiRepo(String name,
                                              String ownerLogin,
                                              String htmlUrl,
                                              int stars,
                                              int forks,
                                              OffsetDateTime updatedAt) {
        var owner = new GitHubApiRepo.Owner(ownerLogin, htmlUrl);
        return new GitHubApiRepo(name, owner, htmlUrl, stars, forks, updatedAt);
    }
}
