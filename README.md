# GitHub Repository Scoring API

A Spring Boot application that retrieves GitHub repositories based on language and creation date, then scores them according to their popularity.

## Overview

This service provides an API endpoint that:
1. Takes a programming language and creation date as input
2. Queries the GitHub Search API for matching repositories
3. Calculates a popularity score for each repository based on:
   - Number of stars (higher is better)
   - Number of forks (higher is better)
   - Recency of updates (more recent is better)
4. Returns a sorted list of repositories with their calculated scores

## Scoring Algorithm

The popularity score is calculated using the following factors:

- **Stars Score**: Uses a logarithmic scale (`log10(stars + 1)`) to reduce the excessive influence of repositories with very high star counts
- **Forks Score**: Uses a logarithmic scale (`log10(forks + 1)`) for the same reason
- **Recency Factor**: A value from 0 to 1 where:
  - 1.0 = updated today
  - 0.0 = updated a year ago or older
  - Linear scale between these points

The final formula is:
```
score = (starsWeight * log10(stars + 1)) + (forksWeight * log10(forks + 1)) + (recencyWeight * recencyFactor)
```

Default weights (configurable in `application.properties`):
- starsWeight = 0.6
- forksWeight = 0.3
- recencyWeight = 0.1

## API Endpoint

### Get Scored Repositories

```
GET /api/v1/repositories/scored
```

**Parameters:**
- `language` (required): Programming language to filter repositories by (e.g., "java", "python")
- `created_after` (required): Minimum repository creation date in ISO format (YYYY-MM-DD)
- `page` (optional): Page number (default: 1)
- `size` (optional): Page size (default: 30, max: 100)

**Example Request:**
```
GET /api/v1/repositories/scored?language=java&created_after=2023-01-01&page=1&size=10
```

```sh
curl "http://localhost:8080/api/v1/repositories/scored?language=java&created_after=2023-01-01&page=1&size=10"
```

**Example Response:**
```json
{
  "content": [
    {
      "name": "example-repo",
      "owner": "example-user",
      "url": "https://github.com/example-user/example-repo",
      "stars": 1250,
      "forks": 75,
      "lastUpdated": "2023-05-15T14:30:45Z",
      "popularityScore": 4.82
    }
    // Additional repositories...
  ],
  "page": 1,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10
}
```

## Building and Running

### Prerequisites

- JDK 21+
- Gradle 8.x

### Build

```sh
./gradlew bootJar
```

### Run

```sh
java -jar build/libs/app.jar
```

Or with Docker:

```sh
docker build -t github-scoring .
docker run -p 8080:8080 github-scoring
```

## Configuration

The application configuration is defined in `src/main/resources/application.yml`. This includes:

- GitHub API connection settings
- Scoring algorithm weights
- Recency calculation parameters

See the comments in the configuration file for detailed explanations of each setting.

## Implementation Details

- **Spring Boot 3.x**: For the web framework and dependency management
- **Java 21**: For modern language features
- **Service Architecture**:
  - `RepositoryScoreController`: Handles HTTP requests and validation
  - `RepositoryScoreService`: Orchestrates fetching and scoring repositories
  - `GitHubClient`: Interacts with GitHub's API
  - `ScoreCalculator`: Implements the scoring algorithm

## Design Trade-offs

- **Servlet-based Spring MVC** for simplicity and maintainability
- **Spring RestClient** for synchronous HTTP calls
- **Weighted composite score** with logarithmic scaling for stars and forks
- **Layered architecture** for clear separation of concerns

## Limitations and Potential Improvements

- Pagination is limited to GitHub's default result count
- Additional filtering, sorting, and custom scoring weights could be added
- Caching, parallel processing, and rate limit awareness could improve performance
- Resilience features like circuit breakers and retries are not yet implemented
- Security and API authentication are not included in the current version

## License

MIT

`
`
