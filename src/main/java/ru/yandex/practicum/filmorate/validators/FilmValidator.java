package ru.yandex.practicum.filmorate.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Component
@Slf4j
public class FilmValidator implements Validator<Film> {
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

    public static final int MAX_DESCRIPTION_LENGTH = 200;
    @Override
    public void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()){
            log.warn("Название фильма не должно быть пустым!");
            throw new ValidationException("Название фильма не должно быть пустым!");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH){
            log.warn("Длина описания не может превышать 200 символов!");
            throw new ValidationException("Длина описания не может превышать 200 символов!");
        }
        if (film.getReleaseDate().isBefore(MIN_DATE)){
            log.warn("Дата релиза не может быть меньше, чем " + MIN_DATE + "!");
            throw new ValidationException("Дата релиза не может быть меньше, чем " + MIN_DATE + "!");
        }
        if (film.getDuration() <= 0){
            log.warn("Продолжительность фильма не может быть отрицательной!");
            throw new ValidationException("Продолжительность фильма не может быть отрицательной!");
        }
    }

    @Override
    public void logAndThrowException(ValidationException e)  {
        log.warn(e.getMessage());
        throw e;
    }
}
