package br.com.antonio.banking.common.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Generic paginated response wrapper.
 * Usage: PageResponse.from(repository.findAll(pageable), mapper::toResponse)
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T, E> PageResponse<T> from(Page<E> page,
                                              java.util.function.Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}