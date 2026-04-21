package ru.job4j.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.domain.Person;
import ru.job4j.dto.PersonPatchDTO;
import ru.job4j.exception.InvalidPasswordException;
import ru.job4j.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SimplePersonService implements PersonService {

    private final PersonRepository personRepository;
    private final BCryptPasswordEncoder encoder;

    public SimplePersonService(PersonRepository personRepository,
                               BCryptPasswordEncoder encoder) {
        this.personRepository = personRepository;
        this.encoder = encoder;
    }

    @Override
    public Person create(Person person) {
        /* Бизнес-логика создания пользователя */
        if (person.getLogin() == null || person.getLogin().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be empty");
        }
        if (person.getPassword() == null || person.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (person.getPassword().length() < 6) {
            throw new InvalidPasswordException("Password must be at least 6 characters");
        }
        if (personRepository.findByLogin(person.getLogin()).isPresent()) {
            throw new IllegalArgumentException("User with login '" + person.getLogin() + "' already exists");
        }

        person.setPassword(encoder.encode(person.getPassword()));
        person.setRole("ROLE_USER");

        return personRepository.save(person);
    }

    @Override
    public Person update(Person person) {
        /* Бизнес-логика обновления */
        if (person.getId() == null) {
            throw new IllegalArgumentException("ID cannot be null for update");
        }
        if (!personRepository.existsById(person.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }

        Person existing = personRepository.findById(person.getId()).get();

        if (person.getLogin() != null && !person.getLogin().isEmpty()) {
            /* Проверка дубликата при смене логина */
            if (!existing.getLogin().equals(person.getLogin())
                    && personRepository.findByLogin(person.getLogin()).isPresent()) {
                throw new IllegalArgumentException("Login already exists");
            }
            existing.setLogin(person.getLogin());
        }

        if (person.getPassword() != null && !person.getPassword().isEmpty()) {
            if (person.getPassword().length() < 6) {
                throw new InvalidPasswordException("Password must be at least 6 characters");
            }
            existing.setPassword(encoder.encode(person.getPassword()));
        }

        return personRepository.save(existing);
    }

    @Override
    public Person patchUpdate(int id, PersonPatchDTO patchData) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found"));

        boolean updated = false;

        if (patchData.getLogin() != null && !patchData.getLogin().isEmpty()) {
            if (!person.getLogin().equals(patchData.getLogin())
                    && personRepository.findByLogin(patchData.getLogin()).isPresent()) {
                throw new IllegalArgumentException("Login already exists");
            }
            person.setLogin(patchData.getLogin());
            updated = true;
        }

        if (patchData.getPassword() != null && !patchData.getPassword().isEmpty()) {
            if (patchData.getPassword().length() < 6) {
                throw new InvalidPasswordException("Password must be at least 6 characters");
            }
            person.setPassword(encoder.encode(patchData.getPassword()));
            updated = true;
        }

        if (patchData.getRole() != null && !patchData.getRole().isEmpty()) {
            person.setRole(patchData.getRole());
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("No fields to update");
        }

        return personRepository.save(person);
    }

    @Override
    public Optional<Person> findById(int id) {
        return personRepository.findById(id);
    }

    @Override
    public Optional<Person> findByLogin(String login) {
        return personRepository.findByLogin(login);
    }

    @Override
    public List<Person> findAll() {
        return (List<Person>) personRepository.findAll();
    }

    @Override
    public void delete(int id) {
        if (!personRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
        personRepository.deleteById(id);
    }
}