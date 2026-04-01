package com.restaurant.Repository;

import com.restaurant.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Repository
public class DataRetriever {

    @Autowired private JdbcTemplate jdbc;

    private final RowMapper<Ingredient> iM = (rs, rowNum) -> {
        Ingredient i = new Ingredient();
        i.setId(rs.getInt("id"));
        i.setName(rs.getString("name"));
        i.setPrice(rs.getBigDecimal("price"));
        String cat = rs.getString("category");
        if (cat != null) {
            i.setCategory(CategoryEnum.valueOf(cat));
        }
        return i;
    };

    private final RowMapper<StockMovement> smM = (rs, rowNum) -> {
        StockMovement sm = new StockMovement();
        sm.setId(rs.getInt("id"));
        sm.setIngredientId(rs.getInt("ingredient_id"));
        sm.setValue(new StockValue(rs.getDouble("quantity"), Unit.valueOf(rs.getString("unit"))));
        sm.setType(MovementTypeEnum.valueOf(rs.getString("type")));
        sm.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
        return sm;
    };

    private final RowMapper<Dish> dM = (rs, rowNum) -> {
        Dish d = new Dish();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setPrice(rs.getBigDecimal("price"));
        return d;
    };

    public List<Ingredient> getAllIngredients() {
        return jdbc.query("SELECT * FROM ingredient", iM);
    }

    public Ingredient findIngredientById(Integer id) {
        List<Ingredient> results = jdbc.query("SELECT * FROM ingredient WHERE id = ?", iM, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public BigDecimal getStockValueAt(Integer id, Instant t) {
        BigDecimal stock = BigDecimal.ZERO;
        List<Map<String, Object>> last = jdbc.queryForList(
                "SELECT stock_value, timestamp FROM ingredient_stock WHERE ingredient_id=? AND timestamp<=? ORDER BY timestamp DESC LIMIT 1",
                id, t);

        if (!last.isEmpty()) {
            Object stockValue = last.get(0).get("STOCK_VALUE");
            if (stockValue != null) {
                stock = new BigDecimal(stockValue.toString());
            }
            Instant lastTime = ((java.sql.Timestamp) last.get(0).get("TIMESTAMP")).toInstant();

            List<StockMovement> movements = jdbc.query(
                    "SELECT * FROM stock_movement WHERE ingredient_id=? AND creation_datetime > ? AND creation_datetime <= ?",
                    smM, id, lastTime, t);

            for (StockMovement m : movements) {
                BigDecimal qty = BigDecimal.valueOf(m.getValue().getQuantity());
                if (m.getType() == MovementTypeEnum.IN) {
                    stock = stock.add(qty);
                } else {
                    stock = stock.subtract(qty);
                }
            }
        }
        return stock;
    }

    public Map<String, BigDecimal> getAllIngredientsStockAt(Instant t) {
        List<Ingredient> ingredients = getAllIngredients();
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Ingredient i : ingredients) {
            result.put(i.getName(), getStockValueAt(i.getId(), t));
        }
        return result;
    }

    public List<StockCalculation> getAllStockCalculationsAt(Instant t) {
        List<Ingredient> ingredients = getAllIngredients();
        List<StockCalculation> result = new ArrayList<>();

        for (Ingredient i : ingredients) {
            BigDecimal initialStock = BigDecimal.ZERO;
            Instant lastStockTime = Instant.EPOCH;

            List<Map<String, Object>> lastStockList = jdbc.queryForList(
                    "SELECT stock_value, timestamp FROM ingredient_stock WHERE ingredient_id=? AND timestamp<=? ORDER BY timestamp DESC LIMIT 1",
                    i.getId(), t);

            if (!lastStockList.isEmpty()) {
                Object val = lastStockList.get(0).get("STOCK_VALUE");
                if (val != null) {
                    initialStock = new BigDecimal(val.toString());
                }
                lastStockTime = ((java.sql.Timestamp) lastStockList.get(0).get("TIMESTAMP")).toInstant();
            }

            List<StockMovement> movements = jdbc.query(
                    "SELECT * FROM stock_movement WHERE ingredient_id=? AND creation_datetime > ? AND creation_datetime <= ?",
                    smM, i.getId(), lastStockTime, t);

            BigDecimal currentStock = initialStock;
            StringBuilder calculation = new StringBuilder(initialStock.toString());

            for (StockMovement m : movements) {
                BigDecimal qty = BigDecimal.valueOf(m.getValue().getQuantity());
                if (m.getType() == MovementTypeEnum.IN) {
                    currentStock = currentStock.add(qty);
                    calculation.append(" + ").append(qty);
                } else {
                    currentStock = currentStock.subtract(qty);
                    calculation.append(" - ").append(qty);
                }
            }

            result.add(new StockCalculation(i.getName(), calculation.toString(), currentStock));
        }
        return result;
    }

    public List<Dish> getAllDishes() {
        return jdbc.query("SELECT * FROM dish", dM);
    }

    public Dish findDishById(Integer id) {
        List<Dish> results = jdbc.query("SELECT * FROM dish WHERE id = ?", dM, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Ingredient> findIngredientsByDishId(Integer id, String ingredientName, BigDecimal ingredientPriceAround) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.* FROM ingredient i JOIN dish_ingredient di ON i.id = di.ingredient_id WHERE di.dish_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(id);

        if (ingredientName != null && !ingredientName.trim().isEmpty()) {
            sql.append(" AND i.name LIKE ?");
            params.add("%" + ingredientName + "%");
        }

        if (ingredientPriceAround != null) {
            sql.append(" AND i.price BETWEEN ? AND ?");
            params.add(ingredientPriceAround.subtract(BigDecimal.valueOf(50)));
            params.add(ingredientPriceAround.add(BigDecimal.valueOf(50)));
        }

        return jdbc.query(sql.toString(), iM, params.toArray());
    }

    public void updateDishIngredients(Integer dishId, List<Integer> ingredientIds) {
        jdbc.update("DELETE FROM dish_ingredient WHERE dish_id = ?", dishId);

        for (Integer ingredientId : ingredientIds) {
            jdbc.update("INSERT INTO dish_ingredient (dish_id, ingredient_id) VALUES (?, ?)",
                    dishId, ingredientId);
        }
    }

    public List<StockMovement> getAllStockMovements() {
        return jdbc.query("SELECT * FROM stock_movement", smM);
    }
}
