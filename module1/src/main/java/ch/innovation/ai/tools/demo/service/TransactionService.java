package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.client.UserServiceClient;
import ch.innovation.ai.tools.demo.dto.*;
import ch.innovation.ai.tools.demo.exception.InsufficientFundsException;
import ch.innovation.ai.tools.demo.exception.TransactionException;
import ch.innovation.ai.tools.demo.exception.TransactionIdMismatchException;
import ch.innovation.ai.tools.demo.model.Transaction;
import ch.innovation.ai.tools.demo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final UserServiceClient userServiceClient;
    
    public TransactionService(TransactionRepository transactionRepository, UserServiceClient userServiceClient) {
        this.transactionRepository = transactionRepository;
        this.userServiceClient = userServiceClient;
    }
    
    public TransactionResponse processTransaction(TransactionRequest request) {
        logger.info("Processing transaction from user {} to user {} for amount {}", 
            request.senderId(), request.receiverId(), request.amount());
        
        if (request.senderId().equals(request.receiverId())) {
            throw new TransactionException("Cannot transfer to the same account");
        }
        
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionException("Amount must be greater than zero");
        }
        
        String transactionId = generateTransactionId();
        
        Transaction transaction = new Transaction(
            transactionId,
            request.senderId(),
            request.receiverId(),
            request.amount()
        );
        
        transaction = transactionRepository.save(transaction);
        
        try {
            List<Long> userIds = Arrays.asList(request.senderId(), request.receiverId());
            UserInfoRequest userInfoRequest = new UserInfoRequest(transactionId, userIds);
            
            UserInfoBatchResponse userInfoResponse = userServiceClient.getUserInfo(userInfoRequest);
            
            if (!transactionId.equals(userInfoResponse.transactionId())) {
                throw new TransactionIdMismatchException("Transaction ID mismatch");
            }
            
            UserInfoResponse sender = userInfoResponse.users().stream()
                .filter(u -> u.id().equals(request.senderId()))
                .findFirst()
                .orElseThrow(() -> new TransactionException("User not found: Sender"));
            
            UserInfoResponse receiver = userInfoResponse.users().stream()
                .filter(u -> u.id().equals(request.receiverId()))
                .findFirst()
                .orElseThrow(() -> new TransactionException("User not found: Receiver"));
            
            if (sender.balance().compareTo(request.amount()) < 0) {
                transaction.setStatus(Transaction.TransactionStatus.INSUFFICIENT_FUNDS);
                transaction.setMessage("Insufficient funds");
                transactionRepository.save(transaction);
                throw new InsufficientFundsException("Insufficient funds for transaction");
            }
            
            BigDecimal senderNewBalance = sender.balance().subtract(request.amount());
            BigDecimal receiverNewBalance = receiver.balance().add(request.amount());
            
            userServiceClient.updateUserBalance(request.senderId(), senderNewBalance);
            userServiceClient.updateUserBalance(request.receiverId(), receiverNewBalance);
            
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            transaction.setSenderBalanceAfter(senderNewBalance);
            transaction.setReceiverBalanceAfter(receiverNewBalance);
            transaction.setMessage("Transaction completed successfully");
            transaction.setProcessedAt(LocalDateTime.now());
            
            transaction = transactionRepository.save(transaction);
            
            logger.info("Transaction {} completed successfully", transactionId);
            
            return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getStatus().toString(),
                transaction.getMessage(),
                transaction.getSenderBalanceAfter(),
                transaction.getReceiverBalanceAfter(),
                transaction.getProcessedAt()
            );
            
        } catch (InsufficientFundsException | TransactionIdMismatchException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Transaction failed: {}", e.getMessage());
            
            if (e instanceof TransactionException) {
                transaction.setStatus(Transaction.TransactionStatus.USER_NOT_FOUND);
                transaction.setMessage(e.getMessage());
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transaction.setMessage("Transaction failed: " + e.getMessage());
            }
            
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            throw e instanceof TransactionException ? (TransactionException) e : 
                new TransactionException("Transaction processing failed", e);
        }
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}