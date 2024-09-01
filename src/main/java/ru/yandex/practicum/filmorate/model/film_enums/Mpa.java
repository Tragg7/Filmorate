package ru.yandex.practicum.filmorate.model.film_enums;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Mpa {
    private int rate_id;
    private String name;
}

