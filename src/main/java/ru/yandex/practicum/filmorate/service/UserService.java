package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

@Service
public interface UserService {

    User addFriend(int initiatorId, int userId);
    User removeFriend(int initiatorId, int userIdToRemove);
    Set<User> getFriends(int userId);
    List<User> getMutualFriends(int userId, int otherUserId);
}
