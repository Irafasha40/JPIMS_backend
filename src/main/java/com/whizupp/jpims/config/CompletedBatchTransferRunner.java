package com.whizupp.jpims.config;

import com.whizupp.jpims.service.BatchCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(25)
@RequiredArgsConstructor
public class CompletedBatchTransferRunner implements CommandLineRunner {
    private final BatchCompletionService batchCompletionService;

    @Value("${app.seed.admin.email:irapac40@gmail.com}")
    private String actorEmail;

    @Override
    public void run(String... args) {
        int transferred = batchCompletionService.syncAllCompletedBatches(actorEmail);
        if (transferred > 0) {
            log.info("Startup sync: {} completed batch(es) auto-transferred to finished products", transferred);
        }
    }
}
