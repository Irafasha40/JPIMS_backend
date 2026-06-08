package com.whizupp.jpims.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PackagingService {

    @Value("${app.packaging.bottles-per-liter:2}")
    private int bottlesPerLiter;

    @Value("${app.packaging.bottles-per-box:12}")
    private int bottlesPerBox;

    @Value("${app.packaging.unit-label:1L}")
    private String unitLabel;

    public PackagingPlan plan(BigDecimal volumeLiters) {
        if (volumeLiters == null || volumeLiters.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Volume must be greater than zero");
        }
        int bottles = volumeLiters
                .multiply(BigDecimal.valueOf(bottlesPerLiter))
                .setScale(0, RoundingMode.CEILING)
                .intValue();
        int boxes = (int) Math.ceil((double) bottles / bottlesPerBox);
        String summary = bottlesPerLiter + " bottles per " + unitLabel + ", "
                + bottlesPerBox + " bottles per box";
        return new PackagingPlan(volumeLiters, bottles, boxes, unitLabel, summary);
    }

    @Getter
    @AllArgsConstructor
    public static class PackagingPlan {
        private final BigDecimal volumeLiters;
        private final int bottlesRequired;
        private final int boxesRequired;
        private final String unitLabel;
        private final String ruleSummary;
    }
}
