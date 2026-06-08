package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.RecipeVersion;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeVersionRepository extends JpaRepository<RecipeVersion, UUID> {
}
