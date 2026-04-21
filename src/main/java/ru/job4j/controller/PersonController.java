package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Person;
import ru.job4j.exception.InvalidPasswordException;
import ru.job4j.repository.PersonRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/person")
public class PersonController {
    private final PersonRepository persons;
    private final BCryptPasswordEncoder encoder;

    public PersonController(final PersonRepository persons,
                            BCryptPasswordEncoder encoder) {
        this.persons = persons;
        this.encoder = encoder;
    }

    private void validatePerson(Person person) {
        if (person == null) {
            throw new NullPointerException("Person object cannot be null");
        }
        if (person.getLogin() == null || person.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be empty");
        }
        if (person.getPassword() == null || person.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 6) {
            throw new InvalidPasswordException("Password must be at least 6 characters long");
        }
        if (!password.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Password must contain at least one digit");
        }
    }

    @GetMapping("/")
    public List<Person> findAll() {
        List<Person> result = new ArrayList<>();
        this.persons.findAll().forEach(result::add);
        result.forEach(p -> {
            p.setPassword(null);
            p.setRole(null);
        });
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.persons.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Person with id " + id + " not found"
                ));

        person.setPassword(null);
        return ResponseEntity.ok(person); /*  ← Билдер! */
    }

    @GetMapping("/by-login")
    public ResponseEntity<Person> findByLogin(@RequestParam String login) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Login parameter cannot be empty");
        }

        var person = persons.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Person with login '" + login + "' not found"
                ));

        person.setPassword(null);
        return ResponseEntity.ok(person); /*  ← Билдер! */
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> signUp(@RequestBody Person person) {
        validatePerson(person);
        validatePassword(person.getPassword());

        /* Проверка на дубликат логина */
        if (persons.findByLogin(person.getLogin()).isPresent()) {
            throw new IllegalArgumentException("User with login '" + person.getLogin() + "' already exists");
        }

        System.out.println("Получен пароль: " + person.getPassword());

        person.setPassword(encoder.encode(person.getPassword()));
        person.setRole("ROLE_USER");

        Person saved = this.persons.save(person);
        saved.setPassword(null);
        saved.setRole(null);
        return new ResponseEntity<>(saved, HttpStatus.CREATED); /*  ← Конструктор! */
    }

    @PostMapping("/")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        validatePerson(person);
        validatePassword(person.getPassword());

        /* Проверка на дубликат логина */
        if (persons.findByLogin(person.getLogin()).isPresent()) {
            throw new IllegalArgumentException("User with login '" + person.getLogin() + "' already exists");
        }

        person.setPassword(encoder.encode(person.getPassword()));
        Person saved = this.persons.save(person);
        saved.setPassword(null);
        saved.setRole(null);
        return new ResponseEntity<>(saved, HttpStatus.CREATED); /*  ← Конструктор! */
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        if (person == null) {
            throw new NullPointerException("Person object cannot be null");
        }
        if (person.getId() == null) {
            throw new IllegalArgumentException("Person ID cannot be null for update");
        }
        if (!this.persons.existsById(person.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Person with id " + person.getId() + " not found"
            );
        }

        /* Проверка на дубликат логина при обновлении */
        var existingPerson = persons.findByLogin(person.getLogin());
        if (existingPerson.isPresent() && !existingPerson.get().getId().equals(person.getId())) {
            throw new IllegalArgumentException("User with login '" + person.getLogin() + "' already exists");
        }

        if (person.getPassword() != null && !person.getPassword().isEmpty()) {
            validatePassword(person.getPassword());
            person.setPassword(encoder.encode(person.getPassword()));
        } else {
            Person oldPerson = persons.findById(person.getId()).get();
            person.setPassword(oldPerson.getPassword());
        }

        if (person.getLogin() == null || person.getLogin().trim().isEmpty()) {
            Person oldPerson = persons.findById(person.getId()).get();
            person.setLogin(oldPerson.getLogin());
        }

        this.persons.save(person);
        return ResponseEntity.ok().build(); /*  ← Билдер (без тела)! */
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (!this.persons.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Person with id " + id + " not found"
            );
        }
        this.persons.deleteById(id);
        return ResponseEntity.ok().build(); /*  ← Билдер (без тела)! */
    }

    @GetMapping("/public/test")
    public String test() {
        return "OK - Server is working!";
    }

    @ExceptionHandler(value = { InvalidPasswordException.class })
    public ResponseEntity<Map<String, Object>> handleInvalidPassword(InvalidPasswordException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());
        errorResponse.put("type", e.getClass().getSimpleName());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}