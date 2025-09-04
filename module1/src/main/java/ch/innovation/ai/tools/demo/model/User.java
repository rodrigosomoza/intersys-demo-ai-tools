package ch.innovation.ai.tools.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    protected User() {
    }

    public User(String id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
