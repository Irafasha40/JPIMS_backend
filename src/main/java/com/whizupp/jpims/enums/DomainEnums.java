package com.whizupp.jpims.enums;

public final class DomainEnums {
    private DomainEnums() {
    }

    public enum Role {
        ADMINISTRATOR, PRODUCTION_MANAGER, INVENTORY_MANAGER, QC_OFFICER, SALES_STAFF
    }

    public enum AccessRequestStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum StockMovementType {
        STOCK_IN, STOCK_OUT
    }

    public enum RecipeStatus {
        DRAFT, PENDING_APPROVAL, ACTIVE, ARCHIVED
    }

    public enum SupplierOnboardingStatus {
        PENDING, ACTIVE, INACTIVE
    }

    public enum SupplierCommunicationType {
        CALL, EMAIL, MEETING, OTHER
    }

    public enum SupplierDocumentType {
        CONTRACT, CERTIFICATE, INVOICE, OTHER
    }

    public enum PurchaseOrderStatus {
        PENDING, RECEIVED, PARTIAL, CANCELLED
    }

    public enum BatchStatus {
        PLANNED, ISSUED, IN_PROGRESS, QC_PENDING, COMPLETED, ON_HOLD
    }

    public enum Appearance {
        CLEAR, SLIGHT_HAZE, CLOUDY
    }

    public enum ColorStatus {
        NORMAL, OFF_COLOR
    }

    public enum TasteStatus {
        NORMAL, OFF_TASTE, ACCEPTABLE
    }

    public enum TestResult {
        PASS, FAIL
    }

    public enum FinishedProductStatus {
        AVAILABLE, NEAR_EXPIRY, EXPIRED, OUT_OF_STOCK, RECALLED
    }

    public enum FinishedProductMovementType {
        PRODUCTION_IN, SALES_OUT, ADJUSTMENT, RECALL
    }

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, CREDIT, OTHER
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }

    public enum NotificationType {
        LOW_STOCK, NEAR_EXPIRY, BATCH_COMPLETE, QC_DUE, ORDER_CONFIRMED, NEW_ORDER
    }

    public enum ReportFrequency {
        DAILY, WEEKLY, MONTHLY
    }
}
