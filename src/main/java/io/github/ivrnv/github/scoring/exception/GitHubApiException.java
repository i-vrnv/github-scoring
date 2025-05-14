package io.github.ivrnv.github.scoring.exception;

import org.springframework.http.HttpStatusCode;

/**
 * Exception thrown when there's an error communicating with the GitHub API.
 */
public class GitHubApiException extends RuntimeException {
    private final HttpStatusCode statusCode;
    
    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }
    
    public GitHubApiException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
