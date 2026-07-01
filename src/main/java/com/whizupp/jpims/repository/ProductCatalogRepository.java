package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.ProductCatalogItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCatalogRepository extends JpaRepository<ProductCatalogItem, UUID> {
    Optional<ProductCatalogItem> findByProductNameIgnoreCase(String productName);
    Page<ProductCatalogItem> findAllByOrderByProductNameAsc(Pageable pageable);
    boolean existsByProductNameIgnoreCase(String productName);
}
