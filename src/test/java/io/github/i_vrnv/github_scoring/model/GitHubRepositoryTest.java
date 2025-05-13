package io.github.i_vrnv.github_scoring.model;

import io.github.i_vrnv.github_scoring.exception.InvalidRepositoryDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubRepositoryTest {

    @Test
    void constructor_ValidValues_CreatesInstance() {
        // Arrange
        int stars = 100;
        int forks = 50;
        OffsetDateTime updatedAt = OffsetDateTime.now().minusDays(1);

        // Act
        GitHubRepository repository = new GitHubRepository(stars, forks, updatedAt);

        // Assert
        assertThat(repository.stars()).isEqualTo(stars);
        assertThat(repository.forks()).isEqualTo(forks);
        assertThat(repository.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void constructor_ZeroStarsAndForks_CreatesInstance() {
        // Arrange
        int stars = 0;
        int forks = 0;
        OffsetDateTime updatedAt = OffsetDateTime.now().minusDays(1);

        // Act
        GitHubRepository repository = new GitHubRepository(stars, forks, updatedAt);

        // Assert
        assertThat(repository.stars()).isZero();
        assertThat(repository.forks()).isZero();
        assertThat(repository.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void constructor_NullUpdatedAt_CreatesInstance() {
        // Act
        GitHubRepository repository = new GitHubRepository(100, 50, null);

        // Assert
        assertThat(repository.updatedAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void constructor_NegativeStars_ThrowsInvalidRepositoryDataException(int stars) {
        // Act & Assert
        assertThatThrownBy(() -> new GitHubRepository(stars, 50, OffsetDateTime.now()))
                .isInstanceOf(InvalidRepositoryDataException.class)
                .hasMessageContaining("Stars count cannot be negative");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void constructor_NegativeForks_ThrowsInvalidRepositoryDataException(int forks) {
        // Act & Assert
        assertThatThrownBy(() -> new GitHubRepository(100, forks, OffsetDateTime.now()))
                .isInstanceOf(InvalidRepositoryDataException.class)
                .hasMessageContaining("Forks count cannot be negative");
    }

    @Test
    void constructor_FutureDate_ThrowsInvalidRepositoryDataException() {
        // Arrange
        OffsetDateTime futureDate = OffsetDateTime.now().plusDays(1);

        // Act & Assert
        assertThatThrownBy(() -> new GitHubRepository(100, 50, futureDate))
                .isInstanceOf(InvalidRepositoryDataException.class)
                .hasMessageContaining("Update date cannot be in the future");
    }
    
    @Test
    void toString_ValidRepository_ReturnsFormattedString() {
        // Arrange
        OffsetDateTime updatedAt = OffsetDateTime.now().minusDays(1);
        GitHubRepository repository = new GitHubRepository(100, 50, updatedAt);

        // Act
        String result = repository.toString();

        // Assert
        assertThat(result)
                .contains("stars=100")
                .contains("forks=50")
                .contains("updatedAt=" + updatedAt);
    }
}
