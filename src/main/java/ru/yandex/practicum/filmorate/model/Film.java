package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validator.MinimumDate;

import java.time.LocalDate;
import java.util.LinkedHashSet;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @MinimumDate(message = "Дата релиза — не раньше 28 декабря 1895 года")
    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    private Mpa mpa;

    private LinkedHashSet<Genre> genres = new LinkedHashSet<>();
}