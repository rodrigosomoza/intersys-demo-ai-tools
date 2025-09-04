package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.dto.TransactionRequest;
import ch.innovation.ai.tools.demo.dto.TransactionResult;
import ch.innovation.ai.tools.demo.dto.UserLookupDtos;
import ch.innovation.ai.tools.demo.model.User;
import ch.innovation.ai.tools.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final RestClient restClient;
    private final UserRepository userRepository;
    private final String module2BaseUrl;

    public TransactionService(UserRepository userRepository,
                              @Value("${module2.base-url:http://localhost:8082}") String module2BaseUrl) {
        this.userRepository = userRepository;
        this.module2BaseUrl = module2BaseUrl;
        this.restClient = RestClient.builder().baseUrl(module2BaseUrl).build();
    }

    @Transactional
    public TransactionResult process(TransactionRequest req) {
        // Call module2 to fetch users info
        var lookupReq = new UserLookupDtos.UserLookupRequest(req.transactionId(), List.of(req.fromUserId(), req.toUserId()));
        var response = restClient.post()
                .uri("/users/lookup")
                .body(lookupReq)
                .retrieve()
                .body(UserLookupDtos.UserLookupResponse.class);

        if (response == null) {
            return new TransactionResult(req.transactionId(), "failed", null);
        }
        if (!req.transactionId().equals(response.transactionId())) {
            return new TransactionResult(req.transactionId(), "failed", null);
        }
        // find user A info
        BigDecimal fromBalance = response.users().stream()
                .filter(u -> u.userId().equals(req.fromUserId()))
                .map(UserLookupDtos.UserInfo::balance)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        if (fromBalance.compareTo(req.amount()) < 0) {
            return new TransactionResult(req.transactionId(), "failed", fromBalance);
        }
        BigDecimal newBalance = fromBalance.subtract(req.amount());

        // upsert user in our DB with new balance
        Optional<User> existing = userRepository.findById(req.fromUserId());
        User user = existing.orElseGet(() -> new User(req.fromUserId(), newBalance));
        user.setBalance(newBalance);
        userRepository.save(user);

        log.info("Transaction {} succeeded. {} balance updated to {}", req.transactionId(), req.fromUserId(), newBalance);
        return new TransactionResult(req.transactionId(), "succeed", newBalance);
    }
}
