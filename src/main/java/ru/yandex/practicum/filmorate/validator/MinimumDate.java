package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumDateValidator.class)
@Documented
public @interface MinimumDate {
    String message() default "Дата должна быть не раньше {value}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String value() default "1895-12-28";
}