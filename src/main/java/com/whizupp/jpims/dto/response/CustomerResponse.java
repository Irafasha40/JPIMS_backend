package com.whizupp.jpims.dto.response;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private UUID id;
    private String name;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private Integer totalOrders;
    private LocalDate lastOrder;
}
