package com.restaurant.controller;

import com.restaurant.entity.Dish;
import com.restaurant.entity.Ingredient;
import com.restaurant.Repository.DataRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishController {

    @Autowired
    private DataRetriever dataRetriever;

    @GetMapping
    public List<Dish> getAllDishes() {
        return dataRetriever.getAllDishes();
    }

    @GetMapping("/{id}/ingredients")
    public ResponseEntity<?> getDishIngredients(@PathVariable Integer id,
                                               @RequestParam(required = false) String ingredientName,
                                               @RequestParam(required = false) BigDecimal ingredientPriceAround) {
        Dish dish = dataRetriever.findDishById(id);
        if (dish == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Dish.id=" + id + " is not found");
        }

        List<Ingredient> ingredients = dataRetriever.findIngredientsByDishId(id, ingredientName, ingredientPriceAround);
        return ResponseEntity.ok(ingredients);
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(@PathVariable Integer id,
                                                   @RequestBody List<Integer> ingredientIds) {
        if (ingredientIds == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Request body is required and must contain a list of ingredient IDs");
        }

        Dish dish = dataRetriever.findDishById(id);
        if (dish == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Dish.id=" + id + " is not found");
        }

        dataRetriever.updateDishIngredients(id, ingredientIds);
        return ResponseEntity.ok().build();
    }
}