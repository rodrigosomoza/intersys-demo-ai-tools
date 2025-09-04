package ch.innovation.ai.tools.demo.repository;

import ch.innovation.ai.tools.demo.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    
    Optional<UserAccount> findByUserId(String userId);
    
    List<UserAccount> findByUserIdIn(List<String> userIds);
    
    List<UserAccount> findByAccountType(String accountType);
}
