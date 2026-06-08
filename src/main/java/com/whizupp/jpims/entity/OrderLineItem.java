package com.whizupp.jpims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "order_line_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItem extends BaseAuditEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private SalesOrder salesOrder;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "finished_product_id")
    private FinishedProduct finishedProduct;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;
}
