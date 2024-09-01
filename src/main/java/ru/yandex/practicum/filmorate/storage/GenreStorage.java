package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film_enums.Genre;

import java.util.List;

public interface GenreStorage extends Storage<Genre>{
    List<Genre> getFilmsGenre(int filmId);
}
