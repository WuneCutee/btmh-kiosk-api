package vn.baotinmanhhai.kioskapi.errorreport.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ErrorReportRequest {

    @NotEmpty
    @Size(max = 50)
    @Valid
    private List<ErrorItemDto> errors;
}
