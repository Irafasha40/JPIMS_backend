package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.enums.DomainEnums.FinishedProductStatus;
import com.whizupp.jpims.repository.FinishedProductRepository;
import java.time.LocalDate;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiryMonitoringService {
    private final FinishedProductRepository finishedProductRepository;
    private final NotificationService notificationService;

    @Value("${app.inventory.expiry-alert-days:30}")
    private int alertDays;

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void checkExpiryAlerts() {
        LocalDate nearExpiryDate = LocalDate.now().plusDays(alertDays);
        finishedProductRepository.findByExpiryDateLessThanEqual(nearExpiryDate)
                .forEach(product -> {
                    if (product.getStatus() == FinishedProductStatus.AVAILABLE) {
                        product.setStatus(FinishedProductStatus.NEAR_EXPIRY);
                        notificationService.notifyNearExpiry(product);
                    }
                });
        log.debug("Expiry monitoring check completed - products marked NEAR_EXPIRY only (manual expiry marking required)");
    }
}
