package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.SupplierCommunication;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierCommunicationRepository extends JpaRepository<SupplierCommunication, UUID> {
}
