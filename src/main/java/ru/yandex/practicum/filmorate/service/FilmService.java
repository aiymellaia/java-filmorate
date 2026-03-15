package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage; // Добавили хранилище пользователей

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);

        // Проверяем, существует ли пользователь, прежде чем ставить лайк
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        film.getLikes().add(userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = findById(filmId);

        // Проверяем пользователя и здесь, чтобы тесты на удаление тоже проходили
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк от пользователя " + userId + " не найден");
        }
        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        findById(film.getId());
        return filmStorage.update(film);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }
}