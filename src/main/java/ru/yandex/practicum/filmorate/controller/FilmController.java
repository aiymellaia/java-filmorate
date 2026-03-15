package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.Valid;

import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 1;

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) { // Добавили @Valid
        if (film.getId() == 0 || !films.containsKey(film.getId())) {
            log.error("Фильм для обновления не найден: id {}", film.getId());
            throw new ValidationException("Фильм с таким id не существует");
        }
        films.put(film.getId(), film);
        log.info("Фильм обновлен: {}", film);
        return film;
    }
}