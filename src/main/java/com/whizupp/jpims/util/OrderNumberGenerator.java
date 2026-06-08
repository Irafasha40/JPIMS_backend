package com.whizupp.jpims.util;

import com.whizupp.jpims.repository.SalesOrderRepository;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {
    private final SalesOrderRepository salesOrderRepository;

    public synchronized String generate() {
        int year = Year.now().getValue();
        String prefix = "ORD-" + year + "-";
        int seq = salesOrderRepository.findTopByOrderNumberStartingWithOrderByOrderNumberDesc(prefix)
                .map(o -> Integer.parseInt(o.getOrderNumber().substring(prefix.length())) + 1)
                .orElse(1);
        String generated = String.format("ORD-%d-%04d", year, seq);
        log.debug("Generated order number {}", generated);
        return generated;
    }
}
