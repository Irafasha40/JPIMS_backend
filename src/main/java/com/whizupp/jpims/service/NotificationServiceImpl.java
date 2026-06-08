package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.entity.Notification;
import com.whizupp.jpims.entity.ProductionBatch;
import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.entity.SalesOrder;
import com.whizupp.jpims.entity.User;
import com.whizupp.jpims.enums.DomainEnums.NotificationType;
import com.whizupp.jpims.enums.DomainEnums.Role;
import com.whizupp.jpims.repository.NotificationRepository;
import com.whizupp.jpims.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void notifyLowStock(RawMaterial material) {
        createForRole(NotificationType.LOW_STOCK, "Low stock alert", "Low stock for raw material: " + material.getName(), Role.INVENTORY_MANAGER);
    }

    @Override
    @Transactional
    public void notifyNearExpiry(FinishedProduct product) {
        createForRole(NotificationType.NEAR_EXPIRY, "Near expiry alert", "Finished product near expiry: " + product.getProductName(), Role.INVENTORY_MANAGER);
        createForRole(NotificationType.NEAR_EXPIRY, "Near expiry alert", "Finished product near expiry: " + product.getProductName(), Role.SALES_STAFF);
    }

    @Override
    @Transactional
    public void notifyQcOfficers(ProductionBatch batch) {
        createForRole(NotificationType.QC_DUE, "QC due", "QC test required for batch: " + batch.getBatchNumber(), Role.QC_OFFICER);
    }

    @Override
    @Transactional
    public void notifyBatchComplete(ProductionBatch batch) {
        createForRole(NotificationType.BATCH_COMPLETE, "Batch complete", "Batch completed: " + batch.getBatchNumber(), Role.PRODUCTION_MANAGER);
    }

    @Override
    @Transactional
    public void notifyQcFailed(ProductionBatch batch) {
        createForRole(NotificationType.QC_DUE, "QC failed", "QC failed for batch: " + batch.getBatchNumber(), Role.PRODUCTION_MANAGER);
    }

    @Override
    @Transactional
    public void notifyNewOrder(SalesOrder order) {
        createForRole(NotificationType.NEW_ORDER, "New order", "New sales order: " + order.getOrderNumber(), Role.SALES_STAFF);
    }

    @Override
    @Transactional
    public void notifyOrderConfirmed(SalesOrder order) {
        User creator = order.getCreatedByUser();
        if (creator != null) {
            createSingle(creator, NotificationType.ORDER_CONFIRMED, "Order confirmed", "Order confirmed: " + order.getOrderNumber());
        }
    }

    @Override
    @Transactional
    public void broadcastToRole(String title, String message, Role role) {
        List<User> users = userRepository.findByRoleAndIsActiveTrue(role);
        users.forEach(user -> createSingle(user, NotificationType.NEW_ORDER, title, message));
    }

    @Override
    @Transactional
    public void broadcastToAll(String title, String message) {
        userRepository.findByIsActiveTrue().forEach(user -> createSingle(user, NotificationType.NEW_ORDER, title, message));
    }

    private void createForRole(NotificationType type, String title, String message, Role role) {
        userRepository.findByRoleAndIsActiveTrue(role)
                .forEach(user -> createSingle(user, type, title, message));
    }

    private void createSingle(User user, NotificationType type, String title, String message) {
        notificationRepository.save(Notification.builder()
                .recipient(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build());
        log.debug("Notification created for user {} type {}", user.getEmail(), type);
    }
}
