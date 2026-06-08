package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.Recipe;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
}
