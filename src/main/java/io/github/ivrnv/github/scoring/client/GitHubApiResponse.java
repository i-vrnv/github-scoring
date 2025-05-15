package io.github.ivrnv.github.scoring.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GitHubApiResponse(
    @JsonProperty("total_count") 
    long totalCount,
    
    @JsonProperty("incomplete_results") 
    boolean incompleteResults,
    
    @JsonProperty("items") 
    List<GitHubApiRepo> repositories
) {}
