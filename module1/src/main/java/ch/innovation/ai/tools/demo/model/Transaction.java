package ch.innovation.ai.tools.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;
    
    @NotNull
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    @NotNull
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;
    
    @NotNull
    @Positive
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;
    
    @Column(length = 500)
    private String message;
    
    @Column(name = "sender_balance_after", precision = 19, scale = 2)
    private BigDecimal senderBalanceAfter;
    
    @Column(name = "receiver_balance_after", precision = 19, scale = 2)
    private BigDecimal receiverBalanceAfter;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED,
        INSUFFICIENT_FUNDS,
        USER_NOT_FOUND
    }
    
    public Transaction() {
    }
    
    public Transaction(String transactionId, Long senderId, Long receiverId, BigDecimal amount) {
        this.transactionId = transactionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public Long getSenderId() {
        return senderId;
    }
    
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    
    public Long getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public BigDecimal getSenderBalanceAfter() {
        return senderBalanceAfter;
    }
    
    public void setSenderBalanceAfter(BigDecimal senderBalanceAfter) {
        this.senderBalanceAfter = senderBalanceAfter;
    }
    
    public BigDecimal getReceiverBalanceAfter() {
        return receiverBalanceAfter;
    }
    
    public void setReceiverBalanceAfter(BigDecimal receiverBalanceAfter) {
        this.receiverBalanceAfter = receiverBalanceAfter;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}