package ch.innovation.ai.tools.demo.repository;

import ch.innovation.ai.tools.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
