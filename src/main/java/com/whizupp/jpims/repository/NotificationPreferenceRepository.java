package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.NotificationPreference;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {
}
