package ru.job4j.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.job4j.domain.Person;
import ru.job4j.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/")
    public List<Person> findAll() {
        List<Person> result = new ArrayList<>();
        this.persons.findAll().forEach(result::add);
        // Скрываем пароли при возврате
        result.forEach(p -> p.setPassword(null));
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.persons.findById(id);
        person.ifPresent(p -> p.setPassword(null));  // Скрываем пароль
        return person.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> signUp(@RequestBody Person person) {
        System.out.println("Получен пароль: " + person.getPassword());  // ТОЛЬКО ЭТА СТРОКА

        person.setPassword(encoder.encode(person.getPassword()));
        person.setRole("ROLE_USER");

        Person saved = this.persons.save(person);
        saved.setPassword(null);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        person.setPassword(encoder.encode(person.getPassword()));
        Person saved = this.persons.save(person);
        saved.setPassword(null);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        if (person.getId() == null || !this.persons.existsById(person.getId())) {
            return ResponseEntity.notFound().build();
        }
        // Если пароль передан новый - шифруем
        if (person.getPassword() != null && !person.getPassword().isEmpty()) {
            person.setPassword(encoder.encode(person.getPassword()));
        } else {
            // Сохраняем старый пароль
            Person oldPerson = persons.findById(person.getId()).get();
            person.setPassword(oldPerson.getPassword());
        }
        this.persons.save(person);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (!this.persons.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        this.persons.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public/test")
    public String test() {
        return "OK - Server is working!";
    }
}