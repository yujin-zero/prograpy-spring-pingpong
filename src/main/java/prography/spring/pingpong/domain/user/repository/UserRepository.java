package prography.spring.pingpong.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prography.spring.pingpong.domain.user.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
