package ru.job4j.service;

import ru.job4j.domain.Person;
import ru.job4j.dto.PersonPatchDTO;
import java.util.List;
import java.util.Optional;

public interface PersonService {
    Person create(Person person);

    Person update(Person person);

    Person patchUpdate(int id, PersonPatchDTO patchData);

    Optional<Person> findById(int id);

    Optional<Person> findByLogin(String login);

    List<Person> findAll();

    void delete(int id);
}