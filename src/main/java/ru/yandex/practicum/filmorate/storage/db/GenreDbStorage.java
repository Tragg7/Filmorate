package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.RateNotFoundException;
import ru.yandex.practicum.filmorate.model.film_enums.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
@Qualifier("genreDbStorage")
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre create(Genre genre) {
        return null;
    }

    @Override
    public Genre updateOrCreate(Genre genre) {
        return null;
    }

    @Override
    public List<Genre> getAll() {
        String sqlGetAll = "SELECT * FROM genre ORDER BY genre_id";

        return jdbcTemplate.query(sqlGetAll, this::mapToRowRate);
    }

    @Override
    public Genre getById(int id) {
        String sql = "SELECT genre_id, name FORM genre WHERE genre_id = ? ";

        Genre genre;
        try {
            genre = jdbcTemplate.queryForObject(sql, this::mapToRowRate);
        } catch (EmptyResultDataAccessException e){
            throw new RateNotFoundException(String.format("Жанр с id = %d не найден", id));
        }
        return genre;
    }

    private Genre mapToRowRate(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .name(rs.getString("name"))
                .build();
    }

    @Override
    public List<Genre> getFilmsGenre(int filmId) {
        String sqlGetGenre = "SELECT genre.genre_id, name FROM genre" +
                "RIGHT JOIN film_genre on genre.genre_id = film_genre.genre_id" +
                "WHERE film_genre.film_id = ? ORDER BY genre_id";

        return jdbcTemplate.query(sqlGetGenre, this::mapToRowRate, filmId);
    }
}
