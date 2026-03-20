package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController controller;
    private Validator validator;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        controller = new UserController(userService);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldLoginBeUsedAsNameIfNameIsEmpty() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("common_login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User savedUser = controller.create(user);

        assertEquals("common_login", savedUser.getName(), "Если имя пустое, должен использоваться логин");
    }

    @Test
    void shouldFailOnInvalidEmail() {
        User user = new User();
        user.setEmail("it-is-not-email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Валидация должна поймать некорректный email");
    }

    @Test
    void shouldFailOnLoginWithSpaces() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("login with spaces");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Логин не должен содержать пробелы");
    }

    @Test
    void shouldFailOnFutureBirthday() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Дата рождения не может быть в будущем");
    }
}