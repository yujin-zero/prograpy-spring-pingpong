package prography.spring.pingpong.domain.room.model.dto;

import lombok.Builder;
import lombok.Getter;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.model.entity.RoomStatus;
import prography.spring.pingpong.model.entity.RoomType;

@Getter
@Builder
public class RoomResponseDto {

    private Long id;
    private String title;
    private Long hostId;
    private RoomType roomType;
    private RoomStatus status;

    public static RoomResponseDto fromEntity(Room room) {
        return RoomResponseDto.builder()
                .id(room.getId())
                .title(room.getTitle())
                .hostId(room.getHost().getId())
                .roomType(room.getRoomType())
                .status(room.getStatus())
                .build();
    }
}
