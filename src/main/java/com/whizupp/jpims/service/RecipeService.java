package com.whizupp.jpims.service;

import com.whizupp.jpims.entity.Recipe;
import com.whizupp.jpims.entity.RecipeIngredient;
import com.whizupp.jpims.entity.RawMaterial;
import com.whizupp.jpims.dto.request.RecipeRequest;
import com.whizupp.jpims.enums.DomainEnums.RecipeStatus;
import com.whizupp.jpims.exception.InvalidOperationException;
import com.whizupp.jpims.exception.ResourceNotFoundException;
import com.whizupp.jpims.repository.ProductionBatchRepository;
import com.whizupp.jpims.repository.RawMaterialRepository;
import com.whizupp.jpims.repository.RecipeIngredientRepository;
import com.whizupp.jpims.repository.RecipeRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final ProductionBatchRepository productionBatchRepository;

    public Page<Recipe> list(Pageable pageable) {
        return recipeRepository.findAll(pageable);
    }

    public Recipe getRecipe(UUID id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));
    }

    @Transactional
    public void replaceIngredients(UUID recipeId, List<RecipeRequest.RecipeIngredientRequest> ingredients) {
        recipeIngredientRepository.deleteByRecipeId(recipeId);
        for (RecipeRequest.RecipeIngredientRequest ingredient : ingredients) {
            addIngredient(recipeId, ingredient.getMaterialId(), ingredient.getQuantity());
        }
    }

    @Transactional
    public Recipe createRecipe(Recipe request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidOperationException("Recipe name is required");
        }
        if (request.getBaseQuantity() == null || request.getBaseQuantity().signum() <= 0) {
            throw new InvalidOperationException("Base quantity must be positive");
        }
        request.setStatus(RecipeStatus.DRAFT);
        return recipeRepository.saveAndFlush(request);
    }

    @Transactional
    public Recipe createRecipeWithIngredients(Recipe request, List<RecipeRequest.RecipeIngredientRequest> ingredients) {
        Recipe created = createRecipe(request);
        if (created.getId() == null) {
            throw new InvalidOperationException("Recipe could not be persisted");
        }
        replaceIngredients(created.getId(), ingredients);
        return getRecipe(created.getId());
    }

    @Transactional
    public Recipe updateRecipe(UUID id, Recipe request) {
        Recipe recipe = getRecipe(id);
        if (!recipe.getStatus().equals(RecipeStatus.DRAFT)) {
            throw new InvalidOperationException("Only DRAFT recipes can be updated");
        }
        recipe.setName(request.getName());
        recipe.setProductName(request.getProductName());
        recipe.setBaseQuantity(request.getBaseQuantity());
        recipe.setNotes(request.getNotes());
        recipe.setShelfLifeDays(request.getShelfLifeDays());
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe submitRecipe(UUID id) {
        Recipe recipe = getRecipe(id);
        if (!recipe.getStatus().equals(RecipeStatus.DRAFT)) {
            throw new InvalidOperationException("Only DRAFT recipes can be submitted");
        }
        recipe.setStatus(RecipeStatus.PENDING_APPROVAL);
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe approveRecipe(UUID id) {
        Recipe recipe = getRecipe(id);
        if (!recipe.getStatus().equals(RecipeStatus.PENDING_APPROVAL)) {
            throw new InvalidOperationException("Only PENDING_APPROVAL recipes can be approved");
        }
        recipe.setStatus(RecipeStatus.ACTIVE);
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe rejectRecipe(UUID id) {
        Recipe recipe = getRecipe(id);
        recipe.setStatus(RecipeStatus.DRAFT);
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe archiveRecipe(UUID id) {
        Recipe recipe = getRecipe(id);
        recipe.setStatus(RecipeStatus.ARCHIVED);
        return recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteRecipe(UUID id) {
        Recipe recipe = getRecipe(id);
        if (productionBatchRepository.countByRecipeId(id) > 0) {
            throw new InvalidOperationException("Recipe is used by production batches and cannot be deleted");
        }
        recipeIngredientRepository.deleteByRecipeId(id);
        recipeRepository.delete(recipe);
    }

    @Transactional
    public Recipe cloneRecipe(UUID sourceId) {
        Recipe source = getRecipe(sourceId);
        Recipe cloned = Recipe.builder()
                .name(source.getName() + " (Copy)")
                .productName(source.getProductName())
                .baseQuantity(source.getBaseQuantity())
                .status(RecipeStatus.DRAFT)
                .notes(source.getNotes())
                .shelfLifeDays(source.getShelfLifeDays())
                .build();
        Recipe saved = recipeRepository.save(cloned);

        List<RecipeIngredient> ingredients = recipeIngredientRepository.findByRecipeIdWithMaterial(sourceId);
        for (RecipeIngredient ingredient : ingredients) {
            recipeIngredientRepository.save(RecipeIngredient.builder()
                    .recipe(saved)
                    .rawMaterial(ingredient.getRawMaterial())
                    .quantity(ingredient.getQuantity())
                    .build());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<RecipeIngredient> getRecipeIngredients(UUID recipeId) {
        return recipeIngredientRepository.findByRecipeIdWithMaterial(recipeId);
    }

    @Transactional
    public void addIngredient(UUID recipeId, UUID materialId, java.math.BigDecimal quantity) {
        Recipe recipe = getRecipe(recipeId);
        RawMaterial material = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found"));

        recipeIngredientRepository.save(RecipeIngredient.builder()
                .recipe(recipe)
                .rawMaterial(material)
                .quantity(quantity)
                .build());
    }
}
