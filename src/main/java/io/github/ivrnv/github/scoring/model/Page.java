package io.github.ivrnv.github.scoring.model;

import java.util.List;

/**
 * Generic pagination response wrapper.
 * 
 * @param <T> The type of content being paginated
 */
public record Page<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages
) {
    public static <T> Page<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        return new Page<>(content, pageNumber, pageSize, totalElements, totalPages);
    }
}
