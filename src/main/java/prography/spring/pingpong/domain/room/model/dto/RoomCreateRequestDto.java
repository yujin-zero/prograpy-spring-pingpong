package prography.spring.pingpong.domain.room.model.dto;

public record RoomCreateRequestDto(Long userId, String roomType, String title) {
}
