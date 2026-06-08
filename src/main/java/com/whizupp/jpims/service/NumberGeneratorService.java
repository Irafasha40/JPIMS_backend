package com.whizupp.jpims.service;

import com.whizupp.jpims.repository.ProductionBatchRepository;
import com.whizupp.jpims.repository.SalesOrderRepository;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NumberGeneratorService {
    private final ProductionBatchRepository batchRepository;
    private final SalesOrderRepository salesOrderRepository;

    public synchronized String nextBatchNumber() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "WH-" + year + "-";
        int next = batchRepository.findTopByBatchNumberStartingWithOrderByBatchNumberDesc(prefix)
                .map(v -> Integer.parseInt(v.getBatchNumber().substring(prefix.length())) + 1)
                .orElse(1);
        return prefix + String.format("%04d", next);
    }

    public synchronized String nextOrderNumber() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "ORD-" + year + "-";
        int next = salesOrderRepository.findTopByOrderNumberStartingWithOrderByOrderNumberDesc(prefix)
                .map(v -> Integer.parseInt(v.getOrderNumber().substring(prefix.length())) + 1)
                .orElse(1);
        return prefix + String.format("%04d", next);
    }
}
