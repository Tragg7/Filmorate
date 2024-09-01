package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.film_enums.Genre;
import ru.yandex.practicum.filmorate.model.film_enums.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.validators.Validator;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Component
@Slf4j
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Validator validator;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    public FilmDbStorage(JdbcTemplate jdbcTemplate, Validator validator, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.validator = validator;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @Override
    public List<Film> getBest(int size) {
        String sqlGetBestFilms = "SELECT id, name, description, release_date, duration, rate_id FROM films" +
                "WHERE films LEFT JOIN (SELECT film_id, count(user_id) FROM likes GROUP BY film_id" +
                "ORDER BY count(user_id) DESC) AS f" +
                "ON film_id = f.film_id ORDER BY id DESC " +
                "LIMIT = ?";

        return jdbcTemplate.query(sqlGetBestFilms, this::mapToRowFilm, size);
    }

    @Override
    public Film create(Film film) {
        validator.validate(film);
        if (film.getId() == 0){
            createFilm(film);
        } else {
            ValidationException e = new ValidationException(String.format("Не удалось создать фильм с таким id: %d ", film.getId()));
            validator.logAndThrowException(e);
        }
        return this.getById(film.getId());
    }

    private void createFilm(Film film){
        String sqlCreate = "INSERT INTO films(name, description, release_date, duration, rate_id)" +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(sqlCreate, new String[]{"id"});
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            preparedStatement.setInt(5, film.getMpa().getRate_id());
            return preparedStatement;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey().intValue()));

        if(film.getGenres() != null && !film.getGenres().isEmpty()){
            createFilmsGenre(film);
        }
        log.info("Создание фильма успешно, фильм {}", film);
    }

    private void createFilmsGenre(Film film){
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO film_genre(film_id, genre_id)" +
                "VALUES ");

        Set<Genre> genres = new HashSet<>(film.getGenres());
        genres.forEach(genre -> stringBuilder
                .append("(")
                .append(film.getId())
                .append(", ")
                .append(genre.getId())
                .append("), ")
        );
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        jdbcTemplate.update(String.valueOf(stringBuilder));
    }


    private void updateFilm(Film film) {
        String sqlUpdate = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rate_id = ?" +
                "WHERE id = ?";

        jdbcTemplate.update(sqlUpdate
                , film.getName()
                , film.getDescription()
                , Date.valueOf(film.getReleaseDate())
                , film.getDuration()
                , film.getMpa().getRate_id()
                , film.getId()
                );
        deleteFilmGenre(film);
        if (film.getGenres() == null && !film.getGenres().isEmpty()) {
            createFilmsGenre(film);
        }

        log.info("Фильм обновлен успешно. Фильм : {} ", film);
    }

    private void deleteFilmGenre(Film film) {
        String sqlDelete = "DELETE FROM film_genre WHERE film_id = ?";

        jdbcTemplate.update(sqlDelete, film.getId());
    }

    @Override
    public List<Film> getAll() {
        String sqlGetAll = "SELECT * FROM films";

        List<Film> allFilms = jdbcTemplate.query(sqlGetAll, this::mapToRowFilm);

        log.info("Найдено {} фильмов", allFilms.size());
        return allFilms;

    }

    @Override
    public Film getById(int filmId) {
        String sqlGetById = "SELECT id, name, description, release_date, duration, rate_id FROM films WHERE id = ?";

        Film film;

        try {
            film = jdbcTemplate.queryForObject(sqlGetById, this::mapToRowFilm, filmId);
        } catch (ValidationException e) {
            throw new FilmNotFoundException(String.format("Фильм с данным ID = %d не найден", filmId));
        }
        log.info("Фильм найден {}", film);
        return film;
    }

    @Override
    public Film updateOrCreate(Film film) {
        validator.validate(film);
        if (film.getId() == 0) {
            createFilm(film);
        } else if (getById(film.getId()) != null) {
            updateFilm(film);
        } else {
            throw new ValidationException("Создать или обновить фильм не удалось.");
        }
        return this.getById(film.getId());
    }

    private Film mapToRowFilm(ResultSet rs, int rowNum) throws SQLException{
        int rateId = rs.getInt("rate_id");
        int filmId = rs.getInt("id");
        Mpa rate = mpaStorage.getById(rateId);
        List<Genre> genreList = genreStorage.getFilmsGenre(filmId);
        Set<Integer> likes = new LinkedHashSet<>(this.getLikes(filmId));

        return Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .mpa(rate)
                .likes(likes)
                .genres(genreList)
                .build();
    }

    private List<Integer> getLikes(int filmId){
        String sqlGetLikes = "SELECT user_id FROM likes WHERE film_id = ?";

        return jdbcTemplate.query(sqlGetLikes, this::mapToRowUserId, filmId);
    }

    private Integer mapToRowUserId(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("user_id");
    }
}
