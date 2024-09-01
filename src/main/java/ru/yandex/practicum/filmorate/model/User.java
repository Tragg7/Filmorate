package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User {
    int id;
    @Email
    String email;
    String login;
    String name;
    LocalDate birthday;
    Set<Integer> friends;
}
