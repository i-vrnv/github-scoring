package io.github.ivrnv.github.scoring.client;

import io.github.ivrnv.github.scoring.exception.GitHubApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client for interacting with GitHub's REST API.
 */
@Component
public class GitHubClient {
    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);
    private static final String SEARCH_REPOS_ENDPOINT = "/search/repositories";
    private static final int DEFAULT_PER_PAGE = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final RestClient restClient;
    
    public GitHubClient(@Value("${github.api.baseUrl:https://api.github.com}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    /**
     * Fetches repositories from GitHub.
     *
     * @param language The programming language to filter repositories by
     * @param createdAfter The date after which repositories should have been created
     * @return List of GitHub repositories matching the criteria
     * @throws GitHubApiException if there's an error communicating with the GitHub API
     */
    public List<GitHubApiRepo> fetchRepositories(String language, LocalDate createdAfter) {
        logger.info("Fetching GitHub repositories for language: {} created after: {}", 
                language, createdAfter);
        
        try {
            String query = buildQuery(language, createdAfter);
            
            GitHubApiResponse result = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_REPOS_ENDPOINT)
                            .queryParam("q", query)
                            .queryParam("per_page", DEFAULT_PER_PAGE)
                            .queryParam("sort", "stars")
                            .queryParam("order", "desc")
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        String errorBody;
                        errorBody = getErrorBody(response);
                        logger.error("GitHub API error: {} - {}", response.getStatusCode(), errorBody);
                        throw new GitHubApiException(errorBody, response.getStatusCode());
                    })
                    .body(GitHubApiResponse.class);
            
            return result != null ? result.repositories() : Collections.emptyList();
        } catch (GitHubApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching repositories from GitHub", e);
            throw new GitHubApiException("Error fetching repositories from GitHub", e);
        }
    }

    private String getErrorBody(ClientHttpResponse response) {
        String errorBody;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            errorBody = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            errorBody = "Could not read error response body";
            logger.error("Failed to read error response", e);
        }
        return errorBody;
    }

    private String buildQuery(String language, LocalDate createdAfter) {
        return String.format("language:%s created:>=%s", 
                language, 
                createdAfter.format(DATE_FORMATTER));
    }
}
