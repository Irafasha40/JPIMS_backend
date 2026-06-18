package com.whizupp.jpims.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private String userName;
    private String action;
    private String entity; // mapped to module
    private String oldValue;
    private String newValue;
    private String details;
    private OffsetDateTime timestamp;
    private String ipAddress;
    private Boolean isAnomaly;
}
