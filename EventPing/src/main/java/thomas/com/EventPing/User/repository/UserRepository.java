package thomas.com.EventPing.User.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thomas.com.EventPing.User.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
}
