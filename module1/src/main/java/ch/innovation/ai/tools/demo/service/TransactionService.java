package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.dto.*;
import ch.innovation.ai.tools.demo.model.Transaction;
import ch.innovation.ai.tools.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public TransactionService(TransactionRepository transactionRepository,
                            RestTemplate restTemplate,
                            @Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    public TransactionResponse processTransaction(TransactionRequest request) {
        // Generate unique transaction ID
        String transactionId = UUID.randomUUID().toString();

        // Create transaction entity
        Transaction transaction = new Transaction(
            transactionId,
            request.senderUserId(),
            request.receiverUserId(),
            request.amount()
        );

        try {
            // Get user balances from user service
            UserBalanceRequest balanceRequest = new UserBalanceRequest(
                transactionId,
                List.of(request.senderUserId(), request.receiverUserId())
            );

            UserBalanceResponse balanceResponse = restTemplate.postForObject(
                userServiceBaseUrl + "/users/balances",
                balanceRequest,
                UserBalanceResponse.class
            );

            // Validate response transaction ID
            if (balanceResponse == null || !transactionId.equals(balanceResponse.transactionId())) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                throw new IllegalStateException("Invalid transaction ID in response");
            }

            // Find sender and receiver balances
            UserBalanceInfo senderBalance = balanceResponse.users().stream()
                .filter(user -> request.senderUserId().equals(user.userId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

            UserBalanceInfo receiverBalance = balanceResponse.users().stream()
                .filter(user -> request.receiverUserId().equals(user.userId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

            // Check if sender has sufficient balance
            if (senderBalance.balance().compareTo(request.amount()) < 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                return new TransactionResponse(
                    transactionId,
                    request.senderUserId(),
                    request.receiverUserId(),
                    request.amount(),
                    "FAILED",
                    senderBalance.balance()
                );
            }

            // Calculate new balances
            BigDecimal newSenderBalance = senderBalance.balance().subtract(request.amount());
            BigDecimal newReceiverBalance = receiverBalance.balance().add(request.amount());

            // Update sender balance in user service
            UserBalanceUpdateRequest senderUpdateRequest = new UserBalanceUpdateRequest(
                transactionId,
                request.senderUserId(),
                newSenderBalance
            );

            UserBalanceUpdateResponse senderUpdateResponse = restTemplate.exchange(
                userServiceBaseUrl + "/users/balances",
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(senderUpdateRequest),
                UserBalanceUpdateResponse.class
            ).getBody();

            // Update receiver balance in user service
            UserBalanceUpdateRequest receiverUpdateRequest = new UserBalanceUpdateRequest(
                transactionId,
                request.receiverUserId(),
                newReceiverBalance
            );

            UserBalanceUpdateResponse receiverUpdateResponse = restTemplate.exchange(
                userServiceBaseUrl + "/users/balances",
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(receiverUpdateRequest),
                UserBalanceUpdateResponse.class
            ).getBody();

            // Check if both balance updates were successful
            if (senderUpdateResponse != null && "SUCCESS".equals(senderUpdateResponse.status()) &&
                receiverUpdateResponse != null && "SUCCESS".equals(receiverUpdateResponse.status())) {

                transaction.setStatus(TransactionStatus.SUCCESS);
                transactionRepository.save(transaction);

                return new TransactionResponse(
                    transactionId,
                    request.senderUserId(),
                    request.receiverUserId(),
                    request.amount(),
                    "SUCCESS",
                    newSenderBalance
                );
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                return new TransactionResponse(
                    transactionId,
                    request.senderUserId(),
                    request.receiverUserId(),
                    request.amount(),
                    "FAILED",
                    senderBalance.balance()
                );
            }

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction processing failed", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
