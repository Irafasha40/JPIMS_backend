package com.whizupp.jpims.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private UUID id;
    private String name;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private String paymentTerms;
    private String status;
    private Double rating;
}
