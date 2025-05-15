package io.github.ivrnv.github.scoring.service;

import io.github.ivrnv.github.scoring.client.GitHubApiRepo;
import io.github.ivrnv.github.scoring.client.GitHubApiResponse;
import io.github.ivrnv.github.scoring.client.GitHubClient;
import io.github.ivrnv.github.scoring.model.Page;
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
        var pageable = new PageRequest(1, 30);

        OffsetDateTime recentUpdateTime = OffsetDateTime.now().minusDays(5);

        GitHubApiRepo repo1 = createGitHubApiRepo("repo1", "owner1", "https://github.com/owner1/repo1",
                500, 50, recentUpdateTime);
        GitHubApiRepo repo2 = createGitHubApiRepo("repo2", "owner2", "https://github.com/owner2/repo2",
                200, 20, recentUpdateTime.minusDays(30));

        GitHubApiResponse mockResponse = new GitHubApiResponse(2, false, List.of(repo1, repo2));

        when(gitHubClient.fetchRepositories(language, createdAfter, pageable)).thenReturn(mockResponse);

        // When
        Page<ScoredRepository> result = sut.getScoredRepositories(language, createdAfter, pageable);

        // Then
        assertThat(result.content()).hasSize(2);

        // Verify first repository
        ScoredRepository scoredRepo1 = result.content().getFirst();
        assertThat(scoredRepo1.name()).isEqualTo("repo1");
        assertThat(scoredRepo1.owner()).isEqualTo("owner1");
        assertThat(scoredRepo1.url()).isEqualTo("https://github.com/owner1/repo1");
        assertThat(scoredRepo1.stars()).isEqualTo(500);
        assertThat(scoredRepo1.forks()).isEqualTo(50);
        assertThat(scoredRepo1.lastUpdated()).isEqualTo(recentUpdateTime);

        // For the second repository, just verify it exists with the expected data
        ScoredRepository scoredRepo2 = result.content().get(1);
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
        var pageable = new PageRequest(1, 30);

        GitHubApiResponse mockResponse = new GitHubApiResponse(0, false, Collections.emptyList());
        when(gitHubClient.fetchRepositories(language, createdAfter, pageable)).thenReturn(mockResponse);
        
        // When
        Page<ScoredRepository> result = sut.getScoredRepositories(language, createdAfter, pageable);
        
        // Then
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void handlesExceptionFromClient() {
        // Given
        String language = "java";
        var createdAfter = LocalDate.parse("2023-01-01");
        var pageable = new PageRequest(1, 30);

        when(gitHubClient.fetchRepositories(language, createdAfter, pageable))
            .thenThrow(new RuntimeException("API Error"));
        
        // When
        Page<ScoredRepository> result = sut.getScoredRepositories(language, createdAfter, pageable);
        
        // Then
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
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
