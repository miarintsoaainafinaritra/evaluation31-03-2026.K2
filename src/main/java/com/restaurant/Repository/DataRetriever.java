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

    public List<Ingredient> getAllIngredients() {
        return jdbc.query("SELECT * FROM ingredient", iM);
    }

    public BigDecimal getStockValueAt(Integer id, Instant t) {
        BigDecimal stock = BigDecimal.ZERO;
        List<Map<String, Object>> last = jdbc.queryForList(
                "SELECT stock_value, timestamp FROM ingredient_stock WHERE ingredient_id=? AND timestamp<=? ORDER BY timestamp DESC LIMIT 1", id, t);
        if (!last.isEmpty()) {
            stock = new BigDecimal(last.get(0).get("STOCK_VALUE").toString());
            Instant lastTime = ((java.sql.Timestamp) last.get(0).get("TIMESTAMP")).toInstant();
            for (StockMovement m : jdbc.query("SELECT * FROM stock_movement WHERE ingredient_id=? AND creation_datetime > ? AND creation_datetime <= ?", smM, id, lastTime, t)) {
                BigDecimal qty = BigDecimal.valueOf(m.getValue().getQuantity());
                stock = m.getType() == MovementTypeEnum.IN ? stock.add(qty) : stock.subtract(qty);
            }
        }
        return stock;
    }
}
