package prography.spring.pingpong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prography.spring.pingpong.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
