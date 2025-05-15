package io.github.ivrnv.github.scoring.controller;

import io.github.ivrnv.github.scoring.model.Page;
import io.github.ivrnv.github.scoring.model.ScoredRepository;
import io.github.ivrnv.github.scoring.service.RepositoryScoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepositoryScoreController.class)
class RepositoryScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepositoryScoreService repositoryScoreService;

    @Test
    void returns200_andData_forValidRequest() throws Exception {
        // Arrange
        String language = "java";
        String createdAfter = "2023-01-01";

        OffsetDateTime updateTime = OffsetDateTime.now();
        Page<ScoredRepository> mockResponse = getScoredRepositories(updateTime);

        when(repositoryScoreService.getScoredRepositories(eq(language), any(LocalDate.class), any()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/repositories/scored")
                .param("language", language)
                .param("created_after", createdAfter)
                .param("page", "1")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("awesome-repo"))
                .andExpect(jsonPath("$.content[0].owner").value("user1"))
                .andExpect(jsonPath("$.content[0].stars").value(100))
                .andExpect(jsonPath("$.content[0].forks").value(20))
                .andExpect(jsonPath("$.content[0].popularityScore").value(85.5))
                .andExpect(jsonPath("$.content[1].name").value("cool-project"))
                .andExpect(jsonPath("$.content[1].owner").value("user2"))
                .andExpect(jsonPath("$.content[1].popularityScore").value(42.0))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void returns400_forMissingLanguageParameter() throws Exception {
        mockMvc.perform(get("/api/repositories/scored")
                .param("created_after", "2023-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400_forMissingCreatedAfterParameter() throws Exception {
        mockMvc.perform(get("/api/repositories/scored")
                .param("language", "java"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400_forInvalidCreatedAfterFormat() throws Exception {
        mockMvc.perform(get("/api/repositories/scored")
                .param("language", "java")
                .param("created_after", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns500_whenServiceThrowsException() throws Exception {
        // Arrange
        when(repositoryScoreService.getScoredRepositories(any(), any(), any()))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/repositories/scored")
                .param("language", "java")
                .param("created_after", "2023-01-01"))
                .andExpect(status().isInternalServerError());
    }

    private static Page<ScoredRepository> getScoredRepositories(OffsetDateTime updateTime) {
        ScoredRepository repo1 = new ScoredRepository(
                "awesome-repo",
                "user1",
                "https://github.com/user1/awesome-repo",
                100,
                20,
                updateTime,
                85.5);

        ScoredRepository repo2 = new ScoredRepository(
                "cool-project",
                "user2",
                "https://github.com/user2/cool-project",
                50,
                5,
                updateTime.minusDays(30),
                42.0);

        return Page.of(
                List.of(repo1, repo2),
                1,
                2,
                2
        );
    }
}
