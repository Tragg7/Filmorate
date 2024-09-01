package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserValidator userValidate;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserValidator userValidate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userValidate = userValidate;
    }

    @Override
    public User create(User user) {
        userValidate.validate(user);
        if(user.getId() == 0) {
            createUser(user);
            log.info("Пользователь успешно создан!");
        }
        else {
            ValidationException ex =  new ValidationException("Создать нового пользователя с ID невозможно");
            userValidate.logAndThrowException(ex);
        }
        return user;
    }

    public void createUser(User user) {
        String sql = "INSERT INTO users_storage(email, name, login, birthday)" +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setString(3, user.getLogin());
            preparedStatement.setDate(4, Date.valueOf(user.getBirthday()));
            return preparedStatement;
                }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey().intValue()));
        log.info("Создание пользователя успешно, пользователь: {}", user);
    }

    @Override
    public User updateOrCreate(User user) {
        userValidate.validate(user);
        if(user.getId() == 0) {
            create(user);
            log.info("Пользователь успешно создан!");
        } else if (getById(user.getId()) != null) {
            String sqlQuery = "UPDATE users_storage SET email = ?, name = ?, login = ?, birthday = ?," +
                    "WHERE id = ?";
            jdbcTemplate.update(sqlQuery
                    , user.getEmail()
                    , user.getName()
                    , user.getLogin()
                    , Date.valueOf(user.getBirthday())
                    , user.getId());
            log.info("Пользователь {} успешно обновлён",
                    user);
        } else {
            throw new UserNotFoundException("Обновить пользователя не удалось");
        }
        return user;
    }

    @Override
    public User getById(int userId) {
        String sqlFindById = "SELECT id, email, name, login, birthday FROM users_storage WHERE id = ?";
        User user;
        try {
            user = jdbcTemplate.queryForObject(sqlFindById, this::mapRowToUser, userId);
        } catch (EmptyResultDataAccessException e) {
            log.info("Пользователь с данным ID = {} не найден", userId);
            throw new UserNotFoundException(String.format("Пользователь с ID = %d не найден", userId));
        }
        log.info("Пользователь найден : {}", user);
        return user;
    }
    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException{
        return User.builder()
                .id(rs.getInt("id"))
                .email(rs.getString("email"))
                .name(rs.getString("name"))
                .login(rs.getString("login"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }

    @Override
    public List<User> getAll() {
        String sqlFindAll = "SELECT id, email, name, login, birthday FROM users_storage";

        return jdbcTemplate.query(sqlFindAll, this::mapRowToUser);
    }
}
