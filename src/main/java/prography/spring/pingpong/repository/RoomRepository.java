package prography.spring.pingpong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prography.spring.pingpong.model.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
