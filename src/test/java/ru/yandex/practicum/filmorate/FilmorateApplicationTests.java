package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {
	private FilmController controller;
	private Validator validator;

	@BeforeEach
	void setUp() {
		controller = new FilmController();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void shouldNotValidateFilmWithOldReleaseDate() {
		Film film = new Film();
		film.setName("Arrival of a Train");
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		var violations = validator.validate(film);

		assertFalse(violations.isEmpty(), "Валидация должна поймать слишком раннюю дату релиза");
	}

	@Test
	void shouldFailOnEmptyName() {
		Film film = new Film();
		film.setName("");
		film.setReleaseDate(LocalDate.now());
		film.setDuration(100);

		var violations = validator.validate(film);
		assertFalse(violations.isEmpty(), "Валидация должна поймать пустое имя");
	}

	@Test
	void shouldFailOnTooLongDescription() {
		Film film = new Film();
		film.setName("Movie");
		film.setDescription("a".repeat(201));
		film.setReleaseDate(LocalDate.now());
		film.setDuration(100);

		var violations = validator.validate(film);
		assertFalse(violations.isEmpty(), "Описание более 200 символов должно быть отклонено");
	}

	@Test
	void shouldValidateCorrectFilm() {
		Film film = new Film();
		film.setName("Inception");
		film.setReleaseDate(LocalDate.of(2010, 7, 16));
		film.setDuration(148);

		Film saved = controller.create(film);
		assertNotNull(saved);
		assertEquals(1, saved.getId());
	}
}