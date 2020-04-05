package ru.example.accounts.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @Getter private String login;
    @Getter private String password;
    @Getter private String address;
    @Getter private String phone;
}