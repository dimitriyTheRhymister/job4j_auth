package ru.job4j.dto;

import jakarta.validation.constraints.Size;

public class PersonPatchDTO {

    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    private String login;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String role;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}