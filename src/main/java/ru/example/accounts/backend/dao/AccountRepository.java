package ru.example.accounts.backend.dao;

import org.springframework.data.repository.CrudRepository;
import ru.example.accounts.backend.model.Account;

public interface AccountRepository extends CrudRepository<Account, String> {
}
