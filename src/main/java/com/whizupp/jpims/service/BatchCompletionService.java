package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.entity.ProductionBatch;
import com.whizupp.jpims.enums.DomainEnums.BatchStatus;
import com.whizupp.jpims.enums.DomainEnums.TestResult;
import com.whizupp.jpims.repository.FinishedProductRepository;
import com.whizupp.jpims.repository.ProductionBatchRepository;
import com.whizupp.jpims.repository.QualityTestRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchCompletionService {
    private final ProductionBatchRepository batchRepository;
    private final QualityTestRepository qualityTestRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final FinishedProductService finishedProductService;
    private final NotificationService notificationService;

    /**
     * Marks batch COMPLETED (from QC_PENDING) and transfers to finished goods in one step.
     */
    @Transactional
    public ProductionBatch completeAfterQcPass(ProductionBatch batch, String actorEmail) {
        if (batch.getStatus() != BatchStatus.QC_PENDING) {
            throw new IllegalStateException("Batch must be QC_PENDING to complete");
        }
        batch.setStatus(BatchStatus.COMPLETED);
        batchRepository.saveAndFlush(batch);

        ProductionBatch fresh = batchRepository.findById(batch.getId()).orElseThrow();
        notificationService.notifyBatchComplete(fresh);
        finishedProductService.receiveFromCompletedBatch(fresh, actorEmail);
        log.info("Batch {} completed and auto-transferred to finished products", fresh.getBatchNumber());
        return fresh;
    }

    /**
     * Transfers any COMPLETED batch that has QC PASS but no finished-goods row yet.
     */
    @Transactional
    public int syncAllCompletedBatches(String actorEmail) {
        List<ProductionBatch> pending = batchRepository.findCompletedWithoutFinishedProduct(BatchStatus.COMPLETED);
        int transferred = 0;
        for (ProductionBatch batch : pending) {
            if (!qualityTestRepository.existsByProductionBatchIdAndResult(batch.getId(), TestResult.PASS)) {
                log.debug("Skipping batch {} — no QC pass", batch.getBatchNumber());
                continue;
            }
            if (finishedProductRepository.existsByProductionBatch_Id(batch.getId())) {
                continue;
            }
            try {
                finishedProductService.receiveFromCompletedBatch(batch, actorEmail);
                transferred++;
            } catch (Exception ex) {
                log.warn("Auto-transfer failed for batch {}: {}", batch.getBatchNumber(), ex.getMessage());
            }
        }
        return transferred;
    }

    @Transactional(readOnly = true)
    public boolean hasFinishedGoods(UUID batchId) {
        return finishedProductRepository.existsByProductionBatch_Id(batchId);
    }
}
