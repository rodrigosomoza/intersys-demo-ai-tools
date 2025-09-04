package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.dto.UserBalanceInfo;
import ch.innovation.ai.tools.demo.dto.UserBalanceRequest;
import ch.innovation.ai.tools.demo.dto.UserBalanceResponse;
import ch.innovation.ai.tools.demo.dto.UserBalanceUpdateRequest;
import ch.innovation.ai.tools.demo.dto.UserBalanceUpdateResponse;
import ch.innovation.ai.tools.demo.model.UserAccount;
import ch.innovation.ai.tools.demo.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public UserBalanceResponse getUserBalances(UserBalanceRequest request) {
        List<UserAccount> accounts = userAccountRepository.findByUserIdIn(request.userIds());

        List<UserBalanceInfo> userBalances = accounts.stream()
            .map(account -> new UserBalanceInfo(
                account.getUserId(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType()
            ))
            .toList();

        return new UserBalanceResponse(request.transactionId(), userBalances);
    }

    public UserBalanceUpdateResponse updateUserBalance(UserBalanceUpdateRequest request) {
        try {
            UserAccount account = userAccountRepository.findByUserId(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User account not found: " + request.userId()));

            account.setBalance(request.newBalance());
            userAccountRepository.save(account);

            return new UserBalanceUpdateResponse(
                request.transactionId(),
                request.userId(),
                "SUCCESS",
                "Balance updated successfully"
            );
        } catch (Exception e) {
            return new UserBalanceUpdateResponse(
                request.transactionId(),
                request.userId(),
                "FAILED",
                "Failed to update balance: " + e.getMessage()
            );
        }
    }

    // Keep the legacy method for backward compatibility
    public void updateUserBalance(String userId, BigDecimal newBalance) {
        UserAccount account = userAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User account not found: " + userId));

        account.setBalance(newBalance);
        userAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public UserAccount getUserAccount(String userId) {
        return userAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User account not found: " + userId));
    }
}
