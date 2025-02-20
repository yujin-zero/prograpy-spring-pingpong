package prography.spring.pingpong.domain.userroom.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;

public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {
    boolean existsByUser(User user);

    List<UserRoom> findByRoom(Room room);

    Optional<UserRoom> findByUserAndRoom(User user, Room room);

    void deleteByRoom(Room room);

    boolean existsByUserId(Long aLong);
}
