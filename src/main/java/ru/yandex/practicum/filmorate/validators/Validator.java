package ru.yandex.practicum.filmorate.validators;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;

public interface Validator<T> {
    void validate(T t);
    void logAndThrowException(ValidationException e);
}
