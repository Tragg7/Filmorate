package ru.yandex.practicum.filmorate.model.user_enims;

public enum FriendStatus {
    EXCEPT(0),
    ACCEPTED(1);

    private int id;
    FriendStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
