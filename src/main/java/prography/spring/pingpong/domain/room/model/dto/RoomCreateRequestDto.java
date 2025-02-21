package prography.spring.pingpong.domain.room.model.dto;

public record RoomCreateRequestDto(int userId, String roomType, String title) {
}
