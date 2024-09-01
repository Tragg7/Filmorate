package ru.yandex.practicum.filmorate.storage;

import java.util.List;

public interface Storage<T> {
    T create(T t);
    T updateOrCreate(T t);
    List<T> getAll();
    T getById(int id);
}
