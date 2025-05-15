package io.github.ivrnv.github.scoring.controller;

import io.github.ivrnv.github.scoring.model.Page;
import io.github.ivrnv.github.scoring.model.ScoredRepository;
import io.github.ivrnv.github.scoring.service.PageRequest;
import io.github.ivrnv.github.scoring.service.RepositoryScoreService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller for GitHub repository scoring endpoints.
 */
@RestController
@RequestMapping("/api/repositories")
@Validated
public class RepositoryScoreController {
    
    private static final Logger logger = LoggerFactory.getLogger(RepositoryScoreController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final RepositoryScoreService repositoryScoreService;

    public RepositoryScoreController(RepositoryScoreService repositoryScoreService) {
        this.repositoryScoreService = repositoryScoreService;
    }

    @GetMapping("/scored")
    public ResponseEntity<Page<ScoredRepository>> getScoredRepositories(
            @RequestParam("language") @NotBlank String language,
            @RequestParam("created_after") @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String createdAfter,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(value = "size", defaultValue = "30") @Min(1) @Max(100) int size
            ) {
        
        try {
            LocalDate createdAfterDate = LocalDate.parse(createdAfter, DATE_FORMATTER);
            PageRequest pageRequest = new PageRequest(page, size);
            Page<ScoredRepository> scoredRepositories = repositoryScoreService.getScoredRepositories(language, createdAfterDate, pageRequest);
            return ResponseEntity.ok(scoredRepositories);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}", createdAfter, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error processing repository scoring request", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
