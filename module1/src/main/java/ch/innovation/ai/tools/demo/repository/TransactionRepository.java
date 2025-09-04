package ch.innovation.ai.tools.demo.repository;

import ch.innovation.ai.tools.demo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    List<Transaction> findBySenderUserIdOrderByCreatedAtDesc(String senderUserId);
    
    List<Transaction> findByReceiverUserIdOrderByCreatedAtDesc(String receiverUserId);
    
    @Query("SELECT t FROM Transaction t WHERE t.senderUserId = :userId OR t.receiverUserId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
}
