package com.whizupp.jpims.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMfaRequest {
    @NotBlank
    private String tempToken;
    @NotBlank
    private String mfaCode;
}
