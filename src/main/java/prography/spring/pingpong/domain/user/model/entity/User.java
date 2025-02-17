package prography.spring.pingpong.domain.user.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import prography.spring.pingpong.model.entity.UserStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer fakerId;
    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    private UserStatus userstatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void proUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
