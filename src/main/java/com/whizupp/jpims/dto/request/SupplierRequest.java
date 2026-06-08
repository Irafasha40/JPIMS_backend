package com.whizupp.jpims.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Contact person is required")
    private String contact;

    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    private String address;
    private String paymentTerms;
    private String onboardingStatus;
}
