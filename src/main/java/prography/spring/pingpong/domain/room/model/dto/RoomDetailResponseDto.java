package prography.spring.pingpong.domain.room.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.model.entity.RoomStatus;
import prography.spring.pingpong.model.entity.RoomType;

@Getter
@Builder
public class RoomDetailResponseDto {
    private Integer id;
    private String title;
    private Integer hostId;
    private RoomType roomType;
    private RoomStatus status;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static RoomDetailResponseDto fromEntity(Room room) {
        return RoomDetailResponseDto.builder()
                .id(room.getId())
                .title(room.getTitle())
                .hostId(room.getHost().getId())
                .roomType(room.getRoomType())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
