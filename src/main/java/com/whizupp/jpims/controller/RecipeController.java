package com.whizupp.jpims.controller;

import com.whizupp.jpims.dto.request.RecipeRequest;
import com.whizupp.jpims.dto.response.RecipeResponse;
import com.whizupp.jpims.entity.Recipe;
import com.whizupp.jpims.entity.RecipeIngredient;
import com.whizupp.jpims.service.RecipeService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR','QC_OFFICER')")
    public ResponseEntity<Page<RecipeResponse>> list(@RequestParam(required = false) String status, @RequestParam(required = false) String productName, Pageable pageable) {
        Page<Recipe> recipes = recipeService.list(pageable);
        return ResponseEntity.ok(recipes.map(this::mapToResponse));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<RecipeResponse> create(@Valid @RequestBody RecipeRequest body) {
        Recipe recipe = Recipe.builder()
                .name(body.getName())
                .productName(body.getProductName())
                .baseQuantity(body.getBaseQuantity())
                .notes(body.getNotes())
                .shelfLifeDays(body.getShelfLifeDays())
                .build();
        Recipe created = recipeService.createRecipeWithIngredients(recipe, body.getIngredients());
        return ResponseEntity.status(201).body(mapToResponse(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR','QC_OFFICER')")
    public ResponseEntity<RecipeResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(recipeService.getRecipe(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<RecipeResponse> update(@PathVariable UUID id, @Valid @RequestBody RecipeRequest body) {
        Recipe recipe = Recipe.builder()
                .name(body.getName())
                .productName(body.getProductName())
                .baseQuantity(body.getBaseQuantity())
                .notes(body.getNotes())
                .shelfLifeDays(body.getShelfLifeDays())
                .build();
        Recipe updated = recipeService.updateRecipe(id, recipe);
        recipeService.replaceIngredients(updated.getId(), body.getIngredients());
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<RecipeResponse> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(recipeService.submitRecipe(id)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<RecipeResponse> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(recipeService.approveRecipe(id)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<RecipeResponse> reject(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(mapToResponse(recipeService.rejectRecipe(id)));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<RecipeResponse> archive(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(recipeService.archiveRecipe(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/clone")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<RecipeResponse> cloneRecipe(@PathVariable UUID id) {
        return ResponseEntity.status(201).body(mapToResponse(recipeService.cloneRecipe(id)));
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<Page<Map<String, Object>>> versions(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(Page.empty(pageable));
    }

    @GetMapping("/{id}/export")
    @PreAuthorize("hasAnyRole('PRODUCTION_MANAGER','ADMINISTRATOR')")
    public ResponseEntity<RecipeResponse> export(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToResponse(recipeService.getRecipe(id)));
    }

    private RecipeResponse mapToResponse(Recipe recipe) {
        List<RecipeIngredient> ingredients = recipeService.getRecipeIngredients(recipe.getId());
        List<RecipeResponse.RecipeIngredientResponse> ingredientResponses = ingredients.stream()
                .map(ing -> RecipeResponse.RecipeIngredientResponse.builder()
                        .materialId(ing.getRawMaterial().getId())
                        .materialName(ing.getRawMaterial().getName())
                        .quantity(ing.getQuantity())
                        .unitOfMeasure(ing.getRawMaterial().getUnitOfMeasure())
                        .build())
                .collect(Collectors.toList());

        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .productName(recipe.getProductName())
                .baseQuantity(recipe.getBaseQuantity())
                .status(recipe.getStatus() != null ? recipe.getStatus().toString() : null)
                .notes(recipe.getNotes())
                .shelfLifeDays(recipe.getShelfLifeDays())
                .ingredients(ingredientResponses)
                .build();
    }
}
