package ru.example.accounts.backend.dao;

import org.springframework.data.repository.CrudRepository;
import ru.example.accounts.backend.model.Account;
import ru.example.accounts.backend.model.Operation;

import java.util.List;

public interface OperationRepository extends CrudRepository<Operation, String> {
    List<Operation> findByFromAcc(Account account);
}
