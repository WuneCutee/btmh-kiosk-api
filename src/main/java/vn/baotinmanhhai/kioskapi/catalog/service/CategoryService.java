package vn.baotinmanhhai.kioskapi.catalog.service;

import org.springframework.stereotype.Service;
import vn.baotinmanhhai.kioskapi.catalog.dto.CategoryDto;
import vn.baotinmanhhai.kioskapi.catalog.repo.CategoryRepository;

import java.util.List;

/** II.2.5. */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> list() {
        return categoryRepository.findByEnabledTrueOrderByDisplayOrderAsc().stream()
                .map(c -> new CategoryDto(c.getId(), c.getCode(), c.getName(), c.getDescription()))
                .toList();
    }
}
