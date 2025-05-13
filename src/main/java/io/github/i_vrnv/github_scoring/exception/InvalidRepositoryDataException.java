package io.github.i_vrnv.github_scoring.exception;

/**
 * Custom exception thrown when repository data validation fails.
 * This exception is used to indicate invalid inputs when creating a GitHubRepository.
 */
public class InvalidRepositoryDataException extends RuntimeException {

    /**
     * Constructs a new InvalidRepositoryDataException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidRepositoryDataException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidRepositoryDataException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidRepositoryDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
