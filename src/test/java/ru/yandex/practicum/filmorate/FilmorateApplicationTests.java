package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTests {
	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;

	@Test
	public void testFindUserById() {
		User newUser = User.builder()
				.email("test@mail.ru")
				.login("test_login")
				.name("Test User")
				.birthday(LocalDate.of(2000, 1, 1))
				.build();
		userStorage.create(newUser);

		Optional<User> userOptional = userStorage.findById(1L);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
								.hasFieldOrPropertyWithValue("login", "test_login")
				);
	}

	@Test
	public void testFindAllUsers() {
		userStorage.create(User.builder().email("u1@m.ru").login("l1").birthday(LocalDate.now()).build());
		userStorage.create(User.builder().email("u2@m.ru").login("l2").birthday(LocalDate.now()).build());

		Collection<User> users = userStorage.findAll();

		assertThat(users).hasSize(2);
	}

	@Test
	public void testFindFilmById() {
		Film newFilm = Film.builder()
				.name("New Film")
				.description("Description")
				.releaseDate(LocalDate.of(2020, 1, 1))
				.duration(120)
				.mpa(new Mpa(1, "G"))
				.build();

		filmStorage.create(newFilm);

		Optional<Film> filmOptional = filmStorage.findById(1L);

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("name", "New Film")
								.hasFieldOrPropertyWithValue("duration", 120)
				);

		assertThat(filmOptional.get().getMpa().getName()).isEqualTo("G");
	}

	@Test
	public void testUpdateFilm() {
		Film film = Film.builder()
				.name("Old Name")
				.description("Old Desc")
				.releaseDate(LocalDate.of(2000, 1, 1))
				.duration(100)
				.mpa(new Mpa(1, "G"))
				.build();
		filmStorage.create(film);

		Film updatedFilm = Film.builder()
				.id(1L)
				.name("New Name")
				.description("New Desc")
				.releaseDate(LocalDate.of(2000, 1, 1))
				.duration(110)
				.mpa(new Mpa(2, "PG"))
				.build();

		filmStorage.update(updatedFilm);

		Optional<Film> filmOptional = filmStorage.findById(1L);

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(f ->
						assertThat(f).hasFieldOrPropertyWithValue("name", "New Name")
								.hasFieldOrPropertyWithValue("duration", 110)
				);
		assertThat(filmOptional.get().getMpa().getId()).isEqualTo(2);
	}
}