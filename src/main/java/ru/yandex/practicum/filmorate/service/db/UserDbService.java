package ru.yandex.practicum.filmorate.service.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Qualifier("userDbService")
@Slf4j
public class UserDbService implements UserService {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserDbService(JdbcTemplate jdbcTemplate,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public User addFriend(int initiatorId, int userId) {
        User user = userStorage.getById(initiatorId);
        if (isAlreadyFreind(initiatorId, userId).isEmpty() && userStorage.getById(userId) != null){
            String sqlAddFriend = "INSERT INTO friends(first_user_id, second_user_id) " +
                    "VALUES (?, ?)";
            jdbcTemplate.update(sqlAddFriend, initiatorId, userId);
            log.info("Теперь {} и {} дружат", initiatorId, userId);
        } else {
            log.info("Пользователь {} и {} уже дружат", initiatorId, userId);
        }
        user.setFriends(this.getFriendsId(initiatorId));
        return user;
    }

    private Set<Integer> getFriendsId(int userId) {
        String sqlAllFriends = "SELECT second_user_id FROM friends " +
                "WHERE first_user_id = ?";
        return new LinkedHashSet<>(jdbcTemplate.query(sqlAllFriends, this::mapToRowUserId, userId));
    }

    private Optional<Integer> isAlreadyFreind(int initiatorId, int userId) {
        String sqlCheckFriend = "SELECT first_user_id, second_user_id FROM friends " +
                "WHERE first_user_id = ? AND second_user_id = ?";
        Optional<Integer> friendId = Optional.empty();
        try {
            friendId = Optional.ofNullable(jdbcTemplate.queryForObject
                    (sqlCheckFriend, this::mapToRowUserId, initiatorId, userId));
        } catch (EmptyResultDataAccessException e){
            log.info("Между пользователями {} и {} уже есть дружба!", initiatorId, userId);
        }
        return friendId;
    }

    private Integer mapToRowUserId(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("second_user_id");
    }

    @Override
    public User removeFriend(int initiatorId, int userIdToRemove) {
        User user = userStorage.getById(initiatorId);
        if (isAlreadyFreind(initiatorId, userIdToRemove).isPresent()
                && userStorage.getById(initiatorId) != null && userStorage.getById(userIdToRemove) != null){
            String sqlDeleteFriends = "DELETE FROM friends " +
                    "WHERE first_user_id = ? AND second_user_id = ?";
            jdbcTemplate.update(sqlDeleteFriends, initiatorId, userIdToRemove);
            user = userStorage.getById(initiatorId);
            log.info("Дружба между {} и {} удалена", initiatorId, userIdToRemove);
        }
        return user;
    }

    @Override
    public Set<User> getFriends(int userId) {
        String sqlAllFriends = "SELECT second_user_id FROM friends " +
                "WHERE first_user_id = ?";
        return new LinkedHashSet<>(jdbcTemplate.query(sqlAllFriends, this::mapToUser, userId));
    }

    private User mapToUser(ResultSet rs, int rowNum) throws SQLException{
        return userStorage.getById(rs.getInt("second_user_id"));
    }

    @Override
    public List<User> getMutualFriends(int userId, int otherUserId) {
        String sqlGetMutualFriends = "SELECT second_user_id FROM friends" +
                "WHERE first_user_id IN(?, ?)" +
                "GROUP BY second_user_id" +
                "HAVING COUNT(first_user_id) = 2";

        if (userStorage.getById(userId) != null && userStorage.getById(otherUserId) != null){
            return jdbcTemplate.query(sqlGetMutualFriends, this::mapToUser, userId, otherUserId);
        }
        return List.of();
    }
}
