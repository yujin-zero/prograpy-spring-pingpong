package prography.spring.pingpong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prography.spring.pingpong.model.entity.UserRoom;

public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {
}
