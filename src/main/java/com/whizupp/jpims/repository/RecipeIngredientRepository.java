package com.whizupp.jpims.repository;

import com.whizupp.jpims.entity.RecipeIngredient;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, UUID> {

    @Query("SELECT ri FROM RecipeIngredient ri WHERE ri.recipe.id = :recipeId")
    List<RecipeIngredient> findByRecipeId(@Param("recipeId") UUID recipeId);

    @Query("SELECT ri FROM RecipeIngredient ri JOIN FETCH ri.rawMaterial WHERE ri.recipe.id = :recipeId")
    List<RecipeIngredient> findByRecipeIdWithMaterial(@Param("recipeId") UUID recipeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RecipeIngredient ri WHERE ri.recipe.id = :recipeId")
    void deleteByRecipeId(@Param("recipeId") UUID recipeId);
}
