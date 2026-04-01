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
    private final RowMapper<Dish> dM = new BeanPropertyRowMapper<>(Dish.class);
    private final RowMapper<StockMovement> smM = (rs, rowNum) -> {
        StockMovement sm = new StockMovement();
        sm.setId(rs.getInt("id"));
        sm.setIngredientId(rs.getInt("ingredient_id"));
        sm.setValue(new StockValue(rs.getDouble("quantity"), Unit.valueOf(rs.getString("unit"))));
        sm.setType(MovementTypeEnum.valueOf(rs.getString("type")));
        sm.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
        return sm;
    };

    public List<Ingredient> getAllIngredients() { return jdbc.query("SELECT * FROM ingredient", iM); }
    public Ingredient findIngredientById(Integer id) { return jdbc.query("SELECT * FROM ingredient WHERE id=?", iM, id).stream().findFirst().orElse(null); }
    public BigDecimal getStockValueAt(Integer id, Instant t) {
      
        BigDecimal initialStock = BigDecimal.ZERO;
        Instant lastStockTime = Instant.EPOCH;
        List<Map<String, Object>> lastStockList = jdbc.queryForList(
                "SELECT stock_value, timestamp FROM ingredient_stock WHERE ingredient_id=? AND timestamp<=? ORDER BY timestamp DESC LIMIT 1",
                id, t);
        if (!lastStockList.isEmpty()) {
            Map<String, Object> lastStock = lastStockList.get(0);
            Object val = lastStock.get("STOCK_VALUE");
            if (val instanceof BigDecimal) {
                initialStock = (BigDecimal) val;
            } else if (val instanceof Number) {
                initialStock = BigDecimal.valueOf(((Number) val).doubleValue());
            }
            lastStockTime = ((java.sql.Timestamp) lastStock.get("TIMESTAMP")).toInstant();
        }
        List<StockMovement> movements = jdbc.query(
            "SELECT * FROM stock_movement WHERE ingredient_id=? AND creation_datetime > ? AND creation_datetime <= ?",
            smM, id, lastStockTime, t);

        BigDecimal currentStock = initialStock;
        for (StockMovement m : movements) {
            BigDecimal qty = BigDecimal.valueOf(m.getValue().getQuantity());
            if (m.getType() == MovementTypeEnum.IN) {
                currentStock = currentStock.add(qty);
            } else {
                currentStock = currentStock.subtract(qty);
            }
        }
        return currentStock;
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
                Map<String, Object> lastStock = lastStockList.get(0);
                Object val = lastStock.get("STOCK_VALUE");
                if (val instanceof BigDecimal) {
                    initialStock = (BigDecimal) val;
                } else if (val instanceof Number) {
                    initialStock = BigDecimal.valueOf(((Number) val).doubleValue());
                }
                lastStockTime = ((java.sql.Timestamp) lastStock.get("TIMESTAMP")).toInstant();
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
    public List<Dish> getAllDishes() { return jdbc.query("SELECT * FROM dish", dM); }
    public Dish findDishById(Integer id) { return jdbc.query("SELECT * FROM dish WHERE id=?", dM, id).stream().findFirst().orElse(null); }
    public void updateDishIngredients(Integer id, List<Integer> ids) {
        jdbc.update("DELETE FROM dish_ingredient WHERE dish_id=?", id);
        ids.forEach(i -> jdbc.update("INSERT INTO dish_ingredient VALUES (?,?)", id, i));
    }
    public List<Ingredient> findIngredientsByDishId(Integer id, String n, BigDecimal p) {
        String s = "SELECT i.* FROM ingredient i JOIN dish_ingredient di ON i.id=di.ingredient_id WHERE di.dish_id=?";
        List<Object> a = new ArrayList<>(List.of(id));
        if (n != null) { s += " AND i.name ILIKE ?"; a.add("%"+n+"%"); }
        if (p != null) { s += " AND i.price BETWEEN ? AND ?"; a.add(p.subtract(BigDecimal.valueOf(50))); a.add(p.add(BigDecimal.valueOf(50))); }
        return jdbc.query(s, iM, a.toArray());
    }
    public List<StockMovement> getAllStockMovements() {
        return jdbc.query("SELECT * FROM stock_movement", smM);
    }

}
