package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.RateNotFoundException;
import ru.yandex.practicum.filmorate.model.film_enums.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
@Component
@Slf4j
@Qualifier("mpaDbStorage")
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa create(Mpa mpa) {
        return null;
    }

    @Override
    public Mpa updateOrCreate(Mpa mpa) {
        return null;
    }

    @Override
    public List<Mpa> getAll() {
        String sqlGetAll = "SELECT * FROM rate ORDER BY rate_id";

        return jdbcTemplate.query(sqlGetAll, this::mapToRowMpa);
    }

    @Override
    public Mpa getById(int id) {
        String sql = "SELECT rate_id, name FORM rate WHERE rate_id = ? ";

        Mpa mpa;
        try {
            mpa = jdbcTemplate.queryForObject(sql, this::mapToRowMpa);
        } catch (EmptyResultDataAccessException e){
            throw new RateNotFoundException(String.format("Оценка с id = %d не найдена", id));
        }
        return mpa;
    }

    private Mpa mapToRowMpa(ResultSet rs, int rowNum) throws SQLException {
        return Mpa.builder()
                .rate_id(rs.getInt("rate_id"))
                .name(rs.getString("name"))
                .build();
    }
}
