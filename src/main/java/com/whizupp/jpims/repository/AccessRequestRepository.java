package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.AccessRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, UUID> {
}
