package com.whizupp.jpims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "product_catalog")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCatalogItem extends BaseAuditEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String productName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    @Column(length = 500)
    private String description;
}
