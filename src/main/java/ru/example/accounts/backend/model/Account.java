package ru.example.accounts.backend.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class Account {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User user;

    private BigDecimal amount;
    private String accCode;

    public Account() {
    }

    public Account(String id, User user, BigDecimal amount, String accCode) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.accCode = accCode;
    }

    public Account(User user, BigDecimal amount, String accCode) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.accCode = accCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getAccCode() {
        return accCode;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
