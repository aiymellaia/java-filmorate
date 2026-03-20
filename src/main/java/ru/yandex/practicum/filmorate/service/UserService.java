package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);

        ((UserDbStorage) userStorage).addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);

        ((UserDbStorage) userStorage).deleteFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        findById(userId);
        return ((UserDbStorage) userStorage).getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        findById(userId);
        findById(otherId);

        return ((UserDbStorage) userStorage).getCommonFriends(userId, otherId);
    }

    public User create(User user) {
        validateName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        findById(user.getId());
        validateName(user);
        return userStorage.update(user);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}