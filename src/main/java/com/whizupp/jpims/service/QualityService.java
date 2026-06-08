package com.whizupp.jpims.service;

import com.whizupp.jpims.dto.request.QualityTestRequest;
import com.whizupp.jpims.dto.response.QualityTestResponse;
import com.whizupp.jpims.entity.ProductionBatch;
import com.whizupp.jpims.entity.QualityTest;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.Appearance;
import com.whizupp.jpims.enums.DomainEnums.BatchStatus;
import com.whizupp.jpims.enums.DomainEnums.ColorStatus;
import com.whizupp.jpims.enums.DomainEnums.TasteStatus;
import com.whizupp.jpims.enums.DomainEnums.TestResult;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.ProductionBatchRepository;
import com.whizupp.jpims.repository.QualityTestRepository;
import com.whizupp.jpims.repository.UserRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualityService {
    private final QualityTestRepository qualityTestRepository;
    private final ProductionBatchRepository batchRepository;
    private final UserRepository userRepository;
    private final BatchCompletionService batchCompletionService;
    private final NotificationService notificationService;

    @Value("${app.qc.ph-min}")
    private BigDecimal phMin;
    @Value("${app.qc.ph-max}")
    private BigDecimal phMax;
    @Value("${app.qc.brix-min}")
    private BigDecimal brixMin;
    @Value("${app.qc.brix-max}")
    private BigDecimal brixMax;

    @Transactional(readOnly = true)
    public Page<QualityTestResponse> list(Pageable pageable) {
        return qualityTestRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public QualityTestResponse getById(UUID id) {
        QualityTest test = qualityTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QC test not found"));
        return toResponse(test);
    }

    @Transactional
    public QualityTest create(QualityTestRequest request, String actorEmail) {
        ProductionBatch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        if (batch.getStatus() != BatchStatus.QC_PENDING) {
            throw new InvalidOperationException(
                    "QC tests can only be recorded for batches awaiting quality control (status QC_PENDING)");
        }

        User tester = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        QualityTest test = QualityTest.builder()
                .productionBatch(batch)
                .testedBy(tester)
                .phLevel(request.getPhLevel())
                .brixLevel(request.getBrixLevel())
                .appearance(parseAppearance(request.getAppearance()))
                .color(parseColor(request.getColor()))
                .taste(parseTaste(request.getTaste()))
                .notes(request.getNotes())
                .testDate(OffsetDateTime.now())
                .build();

        return persist(test);
    }

    private QualityTest persist(QualityTest test) {
        TestResult result = calculate(test);
        test.setResult(result);
        QualityTest saved = qualityTestRepository.save(test);

        ProductionBatch batch = saved.getProductionBatch();
        if (result == TestResult.PASS && batch.getStatus() == BatchStatus.QC_PENDING) {
            String actorEmail = saved.getTestedBy() != null ? saved.getTestedBy().getEmail() : "system@whizupp.local";
            batchCompletionService.completeAfterQcPass(batch, actorEmail);
        } else if (result == TestResult.FAIL) {
            notificationService.notifyQcFailed(batch);
            log.info("Batch {} QC FAIL — remains QC_PENDING", batch.getBatchNumber());
        }

        return saved;
    }

    private TestResult calculate(QualityTest test) {
        boolean phPass = test.getPhLevel().compareTo(phMin) >= 0 && test.getPhLevel().compareTo(phMax) <= 0;
        boolean brixPass = test.getBrixLevel().compareTo(brixMin) >= 0 && test.getBrixLevel().compareTo(brixMax) <= 0;
        boolean colorPass = test.getColor() == null || test.getColor() == ColorStatus.NORMAL;
        boolean tastePass = test.getTaste() == null
                || test.getTaste() == TasteStatus.NORMAL
                || test.getTaste() == TasteStatus.ACCEPTABLE;
        boolean appearancePass = test.getAppearance() == null
                || test.getAppearance() == Appearance.CLEAR
                || test.getAppearance() == Appearance.SLIGHT_HAZE;
        return (phPass && brixPass && appearancePass && colorPass && tastePass) ? TestResult.PASS : TestResult.FAIL;
    }

    public QualityTestResponse toResponse(QualityTest test) {
        ProductionBatch batch = test.getProductionBatch();
        User tester = test.getTestedBy();
        String testerLabel = tester != null
                ? (tester.getFullName() != null && !tester.getFullName().isBlank()
                        ? tester.getFullName()
                        : tester.getEmail())
                : null;

        return QualityTestResponse.builder()
                .id(test.getId())
                .batchId(batch != null ? batch.getId() : null)
                .batchNumber(batch != null ? batch.getBatchNumber() : null)
                .productName(batch != null ? batch.getProductName() : null)
                .phLevel(test.getPhLevel())
                .brixLevel(test.getBrixLevel())
                .appearance(test.getAppearance() != null ? test.getAppearance().name() : null)
                .color(test.getColor() != null ? test.getColor().name() : null)
                .taste(test.getTaste() != null ? test.getTaste().name() : null)
                .result(test.getResult() != null ? test.getResult().name() : null)
                .testedBy(testerLabel)
                .testDate(test.getTestDate() != null ? test.getTestDate() : test.getCreatedAt())
                .notes(test.getNotes())
                .build();
    }

    static Appearance parseAppearance(String appearance) {
        if (appearance == null || appearance.isBlank()) {
            return Appearance.CLEAR;
        }
        return Appearance.valueOf(appearance.trim().toUpperCase().replace('-', '_'));
    }

    static ColorStatus parseColor(String color) {
        if (color == null || color.isBlank()) {
            return ColorStatus.NORMAL;
        }
        return ColorStatus.valueOf(color.trim().toUpperCase().replace('-', '_'));
    }

    static TasteStatus parseTaste(String taste) {
        if (taste == null || taste.isBlank()) {
            return TasteStatus.NORMAL;
        }
        return TasteStatus.valueOf(taste.trim().toUpperCase().replace('-', '_'));
    }
}
