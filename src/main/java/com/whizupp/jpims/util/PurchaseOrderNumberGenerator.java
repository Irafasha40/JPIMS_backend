package com.whizupp.jpims.util;

import com.whizupp.jpims.repository.PurchaseOrderRepository;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseOrderNumberGenerator {
    private final PurchaseOrderRepository purchaseOrderRepository;

    public synchronized String generate() {
        int year = Year.now().getValue();
        String prefix = "PO-" + year + "-";
        int seq = purchaseOrderRepository.findTopByPoNumberStartingWithOrderByPoNumberDesc(prefix)
                .map(po -> Integer.parseInt(po.getPoNumber().substring(prefix.length())) + 1)
                .orElse(1);
        String generated = String.format("PO-%d-%04d", year, seq);
        log.debug("Generated purchase order number {}", generated);
        return generated;
    }
}
