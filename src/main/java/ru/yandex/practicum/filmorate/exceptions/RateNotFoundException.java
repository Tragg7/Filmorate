package ru.yandex.practicum.filmorate.exceptions;

public class RateNotFoundException extends RuntimeException{
    public RateNotFoundException(String message) {
        super(message);
    }
}
