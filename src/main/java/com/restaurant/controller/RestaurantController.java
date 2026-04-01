package com.restaurant.controller;

import com.restaurant.Repository.DataRetriever;
import com.restaurant.entity.StockCalculation;
import com.restaurant.entity.StockMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restaurant")
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private DataRetriever dataRetriever;

    @GetMapping("/stock-movements")
    public List<StockMovement> getAllStockMovements() {
        return dataRetriever.getAllStockMovements();
    }

    @GetMapping("/stock-at-date")
    public Map<String, BigDecimal> getStockAtDate(
            @RequestParam(required = false, defaultValue = "2024-01-06T12:00:00Z") String date) {
        return dataRetriever.getAllIngredientsStockAt(Instant.parse(date));
    }

    @GetMapping("/stock-calculation")
    public List<StockCalculation> getStockCalculation(
            @RequestParam(required = false, defaultValue = "2024-01-06T12:00:00Z") String date) {
        try {
            return dataRetriever.getAllStockCalculationsAt(Instant.parse(date));
        } catch (Exception e) {
            logger.error("Error calculating stock", e);
            throw e;
        }
    }
}
