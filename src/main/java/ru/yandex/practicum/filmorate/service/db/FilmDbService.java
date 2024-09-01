package ru.yandex.practicum.filmorate.service.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Component
@Qualifier("filmDbService")
@Slf4j
public class FilmDbService implements FilmService {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public FilmDbService(JdbcTemplate jdbcTemplate,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public Film addLike(int filmId, int userId) {
        String sqlAddLike = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";

        if (filmStorage.getById(filmId) != null && userStorage.getById(userId) != null){
            jdbcTemplate.update(sqlAddLike, filmId, userId);
        }
        return filmStorage.getById(filmId);
    }

    @Override
    public Film removeLike(int filmId, int userId) {
        String sqlRemoveLike = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        if (filmStorage.getById(filmId) != null && userStorage.getById(userId) != null){
            jdbcTemplate.update(sqlRemoveLike, filmId, userId);
        }
        return filmStorage.getById(filmId);
    }

    @Override
    public List<Film> mostLikedFilms(int size) {
        return filmStorage.getBest(size);
    }
}
