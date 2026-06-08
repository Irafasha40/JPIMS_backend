package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.FinishedProduct;
import com.whizupp.jpims.entity.ProductionBatch;
import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.entity.SalesOrder;
import com.whizupp.jpims.enums.DomainEnums.Role;

public interface NotificationService {
    void notifyLowStock(RawMaterial material);

    void notifyNearExpiry(FinishedProduct product);

    void notifyQcOfficers(ProductionBatch batch);

    void notifyBatchComplete(ProductionBatch batch);

    void notifyQcFailed(ProductionBatch batch);

    void notifyNewOrder(SalesOrder order);

    void notifyOrderConfirmed(SalesOrder order);

    void broadcastToRole(String title, String message, Role role);

    void broadcastToAll(String title, String message);
}
