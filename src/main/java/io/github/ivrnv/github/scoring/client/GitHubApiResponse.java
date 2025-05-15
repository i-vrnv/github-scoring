package io.github.ivrnv.github.scoring.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Model for GitHub API search response.
 * Contains the total count of results, whether the results are incomplete,
 * and the list of repository items for the current page.
 */
public record GitHubApiResponse(
    @JsonProperty("total_count") 
    long totalCount,
    
    @JsonProperty("incomplete_results") 
    boolean incompleteResults,
    
    @JsonProperty("items") 
    List<GitHubApiRepo> repositories
) {}
