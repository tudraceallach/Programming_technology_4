package ru.example.accounts.backend.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class Operation {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String dateOperation;
    private String currency;

    @ManyToOne
    @JoinColumn(name = "fromAcc")
    private Account fromAcc;

    @ManyToOne
    @JoinColumn(name = "toAcc")
    private Account toAcc;

    private BigDecimal amount_before_transfer;
    private BigDecimal amount_after_transfer;

    public Operation() {
    }

    public Operation(String dateOperation, String currency, Account fromAcc, Account toAcc,
                     BigDecimal amount_before_transfer, BigDecimal amount_after_transfer) {
        this.dateOperation = dateOperation;
        this.currency = currency;
        this.fromAcc = fromAcc;
        this.toAcc = toAcc;
        this.amount_before_transfer = amount_before_transfer;
        this.amount_after_transfer = amount_after_transfer;
    }

    public Account getFromAcc() {
        return fromAcc;
    }

    public Account getIncoming() {
        return toAcc;
    }

    @Override
    public String toString() {
        return "Дата операции: " + dateOperation + ", валюта: " + currency +
                ", средств до перевода: " + amount_before_transfer + ", после перевода: " + amount_after_transfer;
    }
}