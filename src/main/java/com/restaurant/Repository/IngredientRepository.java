package com.restaurant.Repository;

import com.restaurant.entity.Ingredient;
import com.restaurant.entity.CategoryEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class IngredientRepository {

    @Autowired
    private JdbcTemplate jdbc;

    private RowMapper<Ingredient> mapper = (rs, row) -> {
        Ingredient i = new Ingredient();
        i.setId(rs.getInt("id"));
        i.setName(rs.getString("name"));
        i.setPrice(rs.getBigDecimal("price"));
        i.setCategory(CategoryEnum.valueOf(rs.getString("category")));
        return i;
    };

    public List<Ingredient> findAll() {
        return jdbc.query("SELECT id, name, price, category FROM ingredient", mapper);
    }

    public Ingredient findById(Integer id) {
        List<Ingredient> list = jdbc.query("SELECT id, name, price, category FROM ingredient WHERE id = ?", mapper, id);
        return list.isEmpty() ? null : list.get(0);
    }
}