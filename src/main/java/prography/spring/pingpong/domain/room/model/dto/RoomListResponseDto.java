package prography.spring.pingpong.domain.room.model.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class RoomListResponseDto {

    private Integer totalElements;
    private int totalPages;
    private List<RoomResponseDto> roomList;

    public static RoomListResponseDto fromPage(Page<RoomResponseDto> roomPage) {
        return RoomListResponseDto.builder()
                .totalElements((int) roomPage.getTotalElements())
                .totalPages(roomPage.getTotalPages())
                .roomList(roomPage.getContent())
                .build();
    }
}
