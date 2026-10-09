package vn.baotinmanhhai.kioskapi.errorreport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ErrorItemDto {

    @NotBlank
    private String id;

    @NotBlank
    private String occurredAt;

    @NotNull
    private String type;

    @NotBlank
    @Size(max = 500)
    private String message;

    private Map<String, Object> context;
}
