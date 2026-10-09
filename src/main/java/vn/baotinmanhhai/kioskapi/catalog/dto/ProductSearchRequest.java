package vn.baotinmanhhai.kioskapi.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchRequest {

    @NotBlank
    @Size(max = 250)
    private String name;

    private String categoryId;

    private Integer pageNumber;

    private Integer pageSize;
}
