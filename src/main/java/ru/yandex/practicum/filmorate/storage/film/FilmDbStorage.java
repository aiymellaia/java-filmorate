package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        updateGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE film_id = ?";

        int rowsAffected = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rowsAffected > 0) {
            jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
            updateGenres(film);
            return findById(film.getId()).get();
        }
        return film;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", id);
    }

    @Override
    public Collection<Film> findAll() {
        String sqlQuery = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";

        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
        loadGenres(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sqlQuery = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.get(0);
        loadGenres(List.of(film));
        return Optional.of(film);
    }

    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "GROUP BY f.film_id, m.name " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        loadGenres(films);
        return films;
    }

    private void updateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) return;

        String ids = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(","));

        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id IN (" + ids + ")";

        jdbcTemplate.query(sql, (rs) -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));

            films.stream()
                    .filter(f -> f.getId() == filmId)
                    .findFirst()
                    .ifPresent(f -> {
                        if (f.getGenres() == null) f.setGenres(new LinkedHashSet<>());
                        f.getGenres().add(genre);
                    });
        });
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")))
                .genres(new LinkedHashSet<>())
                .build();
    }
}