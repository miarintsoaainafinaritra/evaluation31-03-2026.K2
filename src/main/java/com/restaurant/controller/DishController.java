package com.restaurant.controller;

import com.restaurant.entity.Dish;
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
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) BigDecimal price) {
        Dish dish = dataRetriever.findDishById(id);
        if (dish == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dish not found");
        }
        return ResponseEntity.ok(dataRetriever.findIngredientsByDishId(id, name, price));
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(@PathVariable Integer id,
                                                   @RequestBody List<Integer> ids) {
        if (ids == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request");
        }
        Dish dish = dataRetriever.findDishById(id);
        if (dish == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dish not found");
        }
        dataRetriever.updateDishIngredients(id, ids);
        return ResponseEntity.ok().build();
    }
}
