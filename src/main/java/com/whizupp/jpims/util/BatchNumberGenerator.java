package com.whizupp.jpims.util;

import com.whizupp.jpims.repository.ProductionBatchRepository;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchNumberGenerator {
    private final ProductionBatchRepository batchRepository;

    public synchronized String generate() {
        int year = Year.now().getValue();
        String prefix = "WH-" + year + "-";
        int seq = batchRepository.findTopByBatchNumberStartingWithOrderByBatchNumberDesc(prefix)
                .map(b -> Integer.parseInt(b.getBatchNumber().substring(prefix.length())) + 1)
                .orElse(1);
        String generated = String.format("WH-%d-%04d", year, seq);
        log.debug("Generated batch number {}", generated);
        return generated;
    }
}
