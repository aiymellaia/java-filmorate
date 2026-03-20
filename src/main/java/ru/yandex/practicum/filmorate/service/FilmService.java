package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmDbStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        findById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        findById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopular(count);
    }

    public Film create(Film film) {
        validateDependencies(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        findById(film.getId());
        validateDependencies(film);
        return filmStorage.update(film);
    }

    private void validateDependencies(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден"));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            LinkedHashSet<Genre> uniqueGenres = film.getGenres().stream()
                    .distinct()
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            film.setGenres(uniqueGenres);

            for (Genre genre : uniqueGenres) {
                genreStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
            }
        }
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }
}