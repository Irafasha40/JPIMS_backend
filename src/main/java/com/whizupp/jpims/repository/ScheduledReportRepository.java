package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.ScheduledReport;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, UUID> {
}
