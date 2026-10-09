package vn.baotinmanhhai.kioskapi.catalog.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Trang ket qua theo dung cau truc rut gon duoc tai lieu yeu cau (bo pageable/sort/empty...). */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last
) {
    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}
