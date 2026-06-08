package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.DataRetentionPolicy;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataRetentionPolicyRepository extends JpaRepository<DataRetentionPolicy, UUID> {
}
