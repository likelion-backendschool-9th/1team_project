package com.dipsource.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HandleReportRequest(
        @NotBlank String decision,
        @NotBlank @Size(min = 5, max = 1000) String memo) {
}
