package prography.spring.pingpong.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prography.spring.pingpong.domain.room.model.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Integer> {
}
