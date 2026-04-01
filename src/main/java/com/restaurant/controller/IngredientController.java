package com.restaurant.controller;

import com.restaurant.entity.Ingredient;
import com.restaurant.Repository.DataRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    @Autowired
    private DataRetriever dataRetriever;

    @GetMapping
    public List<Ingredient> getAllIngredients() {
        return dataRetriever.getAllIngredients();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable Integer id) {
        Ingredient ingredient = dataRetriever.findIngredientById(id);
        if (ingredient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ingredient.id=" + id + " is not found");
        }
        return ResponseEntity.ok(ingredient);
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStockValue(@PathVariable Integer id,
                                           @RequestParam String at,
                                           @RequestParam String unit) {
        if (at == null || unit == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Either mandatory query parameter `at` or `unit` is not provided.");
        }

        Ingredient ingredient = dataRetriever.findIngredientById(id);
        if (ingredient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ingredient.id=" + id + " is not found");
        }

        Instant instant = Instant.parse(at);
        BigDecimal stockValue = dataRetriever.getStockValueAt(id, instant);

        return ResponseEntity.ok(new StockResponse(unit, stockValue));
    }

    static class StockResponse {
        private String unit;
        private BigDecimal value;

        public StockResponse(String unit, BigDecimal value) {
            this.unit = unit;
            this.value = value;
        }
        public String getUnit() { return unit; }
        public BigDecimal getValue() { return value; }
    }
}