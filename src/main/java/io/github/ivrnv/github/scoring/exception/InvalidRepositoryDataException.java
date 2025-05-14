package io.github.ivrnv.github.scoring.exception;

/**
 * Custom exception thrown when repository data validation fails.
 * This exception is used to indicate invalid inputs when creating a GitHubRepository.
 */
public class InvalidRepositoryDataException extends RuntimeException {

    public InvalidRepositoryDataException(String message) {
        super(message);
    }

}
