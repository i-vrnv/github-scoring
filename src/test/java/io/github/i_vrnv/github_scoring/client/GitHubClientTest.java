package io.github.i_vrnv.github_scoring.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.i_vrnv.github_scoring.exception.GitHubApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GitHubClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();
            
    private GitHubClient gitHubClient;
    
    @BeforeEach
    void setUp() {
        gitHubClient = new GitHubClient(wireMock.baseUrl());
    }
    
    @Test
    void fetchRepositories_returnsListOfRepositories_onSuccessfulResponse() {
        // Given
        String language = "java";
        LocalDate createdAfter = LocalDate.of(2023, 1, 1);
        String expectedQuery = "language:java created:>=2023-01-01";
        
        wireMock.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo(expectedQuery))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("github/successful_response.json")));
                        
        // When
        List<GitHubApiRepo> repositories = gitHubClient.fetchRepositories(language, createdAfter);
        
        // Then
        assertThat(repositories).isNotEmpty();
        assertThat(repositories).hasSize(2);
        assertThat(repositories.get(0).name()).isEqualTo("sample-repo-1");
        assertThat(repositories.get(0).stars()).isEqualTo(100);
        assertThat(repositories.get(0).forks()).isEqualTo(20);
        
        wireMock.verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo(expectedQuery))
                .withQueryParam("per_page", equalTo("30"))
                .withQueryParam("sort", equalTo("stars"))
                .withQueryParam("order", equalTo("desc")));
    }
    
    @Test
    void fetchRepositories_returnsEmptyList_whenNoResultsFound() {
        // Given
        String language = "java";
        LocalDate createdAfter = LocalDate.of(2023, 1, 1);
        String expectedQuery = "language:java created:>=2023-01-01";
        
        wireMock.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo(expectedQuery))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("github/empty_response.json")));
                        
        // When
        List<GitHubApiRepo> repositories = gitHubClient.fetchRepositories(language, createdAfter);
        
        // Then
        assertThat(repositories).isEmpty();
    }
    
    @Test
    void fetchRepositories_handlesApiError_gracefully() {
        // Given
        String language = "java";
        LocalDate createdAfter = LocalDate.of(2023, 1, 1);
        String expectedQuery = "language:java created:>=2023-01-01";
        
        wireMock.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo(expectedQuery))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("github/error_response.json")));
                        
        // When & Then
        GitHubApiException exception = assertThrows(GitHubApiException.class, () -> 
                gitHubClient.fetchRepositories(language, createdAfter));
                
        assertThat(exception.getMessage()).contains("API rate limit exceeded");
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    
    @Test
    void fetchRepositories_buildsCorrectQueryParameters() {
        // Given
        String language = "python";
        LocalDate createdAfter = LocalDate.of(2024, 1, 15);
        String expectedQuery = "language:python created:>=2024-01-15";
        
        wireMock.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo(expectedQuery))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("github/successful_response.json")));
                        
        // When
        gitHubClient.fetchRepositories(language, createdAfter);
        
        // Then
        wireMock.verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo(expectedQuery)));
    }
}
