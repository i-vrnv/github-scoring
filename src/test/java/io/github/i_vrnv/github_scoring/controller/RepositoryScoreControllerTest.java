package io.github.i_vrnv.github_scoring.controller;

import io.github.i_vrnv.github_scoring.model.ScoredRepository;
import io.github.i_vrnv.github_scoring.service.RepositoryScoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        List<ScoredRepository> mockResponse = getScoredRepositories(updateTime);

        when(repositoryScoreService.getScoredRepositories(eq(language), any(LocalDate.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/repositories/scored")
                .param("language", language)
                .param("created_after", createdAfter))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("awesome-repo"))
                .andExpect(jsonPath("$[0].owner").value("user1"))
                .andExpect(jsonPath("$[0].stars").value(100))
                .andExpect(jsonPath("$[0].forks").value(20))
                .andExpect(jsonPath("$[0].popularityScore").value(85.5))
                .andExpect(jsonPath("$[1].name").value("cool-project"))
                .andExpect(jsonPath("$[1].owner").value("user2"))
                .andExpect(jsonPath("$[1].popularityScore").value(42.0));
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
        when(repositoryScoreService.getScoredRepositories(any(), any()))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/repositories/scored")
                .param("language", "java")
                .param("created_after", "2023-01-01"))
                .andExpect(status().isInternalServerError());
    }

    private static List<ScoredRepository> getScoredRepositories(OffsetDateTime updateTime) {
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

        return List.of(repo1, repo2);
    }
}
