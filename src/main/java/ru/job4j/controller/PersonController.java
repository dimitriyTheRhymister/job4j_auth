package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.job4j.domain.Person;
import ru.job4j.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/person")
public class PersonController {
    private final PersonRepository persons;

    public PersonController(final PersonRepository persons) {
        this.persons = persons;
    }

    @GetMapping("/")
    public List<Person> findAll() {
        List<Person> result = new ArrayList<>();
        this.persons.findAll().forEach(result::add);
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.persons.findById(id);
        return person.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        return new ResponseEntity<>(
                this.persons.save(person),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        // Проверяем, существует ли пользователь
        if (person.getId() == null || !this.persons.existsById(person.getId())) {
            return ResponseEntity.notFound().build();
        }
        this.persons.save(person);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        // Проверяем, существует ли пользователь
        if (!this.persons.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        this.persons.deleteById(id);
        return ResponseEntity.ok().build();
    }
}