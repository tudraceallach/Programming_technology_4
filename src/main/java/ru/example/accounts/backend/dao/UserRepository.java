package ru.example.accounts.backend.dao;

import org.springframework.data.repository.CrudRepository;
import ru.example.accounts.backend.model.User;

public interface UserRepository extends CrudRepository<User,Long> {
    User findByPhone(String phone);
    User findByLogin(String login);
}
