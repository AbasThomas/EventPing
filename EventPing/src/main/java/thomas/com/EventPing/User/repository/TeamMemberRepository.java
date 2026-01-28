package thomas.com.EventPing.User.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thomas.com.EventPing.User.model.TeamMember;
import thomas.com.EventPing.User.model.User;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByOwner(User owner);
    long countByOwner(User owner);
}
