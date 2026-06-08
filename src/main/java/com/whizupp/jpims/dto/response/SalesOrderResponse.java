package com.whizupp.jpims.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate orderDate;
    private String paymentMethod;
    private String notes;
    private List<OrderLineItemResponse> lineItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineItemResponse {
        private UUID productId;
        private String productName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
