package prography.spring.pingpong.domain.userroom.model.entity;

import jakarta.persistence.*;
import lombok.*;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.model.entity.Team;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_room")
public class UserRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Team team;

}
