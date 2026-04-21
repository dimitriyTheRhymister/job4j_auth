package ru.job4j.dto;

import ru.job4j.domain.Person;

public class PersonResponseDTO {
    private Integer id;
    private String login;
    private String role;

    public PersonResponseDTO(Person person) {
        this.id = person.getId();
        this.login = person.getLogin();
        this.role = person.getRole();
        /* Пароль просто НЕ копируем */
    }

    /* Геттеры (сеттеры не нужны, объект неизменяемый) */
    public Integer getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }
}