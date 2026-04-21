package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Person;
import ru.job4j.dto.PersonPatchDTO;
import ru.job4j.dto.PersonResponseDTO;
import ru.job4j.exception.InvalidPasswordException;
import ru.job4j.service.PersonService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /* ========== GET методы ========== */

    @GetMapping("/")
    public List<PersonResponseDTO> findAll() {
        return personService.findAll().stream()
                .map(PersonResponseDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> findById(@PathVariable int id) {
        Person person = personService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Person with id " + id + " not found"
                ));
        return ResponseEntity.ok(new PersonResponseDTO(person));
    }

    @GetMapping("/by-login")
    public ResponseEntity<PersonResponseDTO> findByLogin(@RequestParam String login) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Login parameter cannot be empty");
        }

        Person person = personService.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Person with login '" + login + "' not found"
                ));
        return ResponseEntity.ok(new PersonResponseDTO(person));
    }

    /* ========== POST методы ========== */

    @PostMapping("/sign-up")
    public ResponseEntity<PersonResponseDTO> signUp(@RequestBody Person person) {
        Person saved = personService.create(person);
        return new ResponseEntity<>(new PersonResponseDTO(saved), HttpStatus.CREATED);
    }

    @PostMapping("/")
    public ResponseEntity<PersonResponseDTO> create(@RequestBody Person person) {
        Person saved = personService.create(person);
        return new ResponseEntity<>(new PersonResponseDTO(saved), HttpStatus.CREATED);
    }

    /* ========== PUT метод (полное обновление) ========== */

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        personService.update(person);
        return ResponseEntity.ok().build();
    }

    /* ========== PATCH метод (частичное обновление) ========== */

    @PatchMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> patchUpdate(
            @PathVariable int id,
            @RequestBody PersonPatchDTO patchData) {
        Person updated = personService.patchUpdate(id, patchData);
        return ResponseEntity.ok(new PersonResponseDTO(updated));
    }

    /* ========== DELETE метод ========== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        personService.delete(id);
        return ResponseEntity.ok().build();
    }

    /* ========== Тестовый публичный метод ========== */

    @GetMapping("/public/test")
    public String test() {
        return "OK - Server is working!";
    }

    /* ========== Обработчики исключений ========== */

    @ExceptionHandler(value = {InvalidPasswordException.class})
    public ResponseEntity<Map<String, Object>> handleInvalidPassword(InvalidPasswordException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());
        errorResponse.put("type", e.getClass().getSimpleName());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}