package prography.spring.pingpong.domain.game.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import prography.spring.pingpong.domain.game.model.dto.GameStartRequestDto;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.service.RoomService;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;
import prography.spring.pingpong.domain.userroom.service.UserRoomService;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.RoomStatus;
import prography.spring.pingpong.model.entity.Team;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private RoomService roomService;

    @Mock
    private UserRoomService userRoomService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GameService gameService;

    private Room testRoom;
    private UserRoom hostUserRoom;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id(100)
                .status(RoomStatus.WAIT)
                .build();

        hostUserRoom = UserRoom.builder()
                .id(1)
                .room(testRoom)
                .team(Team.RED)
                .build();
    }

    @Test
    @DisplayName("✅ 게임 시작 - 성공")
    void startGame_Success() {
        GameStartRequestDto request = new GameStartRequestDto(1);

        when(roomService.isRoomValidForGame(testRoom.getId())).thenReturn(true);
        when(userRoomService.isValidHost(testRoom.getId(), request.userId())).thenReturn(true);
        when(roomService.getRoomById(testRoom.getId())).thenReturn(testRoom);
        when(userRoomService.getUserRoomsByRoom(testRoom)).thenReturn(List.of(hostUserRoom));
        when(roomService.getMaxCapacity(testRoom.getRoomType())).thenReturn(1);

        ApiResponse<Void> response = gameService.startGame(testRoom.getId(), request);

        assertEquals(200, response.code());
        assertEquals(RoomStatus.PROGRESS, testRoom.getStatus());
        verify(roomService, times(1)).updateRoom(testRoom);
    }

    @Test
    @DisplayName("✅ 게임 시작 - 실패 (호스트가 아님)")
    void startGame_Fail_NotHost() {
        GameStartRequestDto request = new GameStartRequestDto(999);

        when(roomService.isRoomValidForGame(testRoom.getId())).thenReturn(true);
        when(userRoomService.isValidHost(testRoom.getId(), request.userId())).thenReturn(false);

        ApiResponse<Void> response = gameService.startGame(testRoom.getId(), request);

        assertEquals(201, response.code());
        verify(roomService, never()).updateRoom(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("✅ 게임 시작 - 실패 (방 정원 미달)")
    void startGame_Fail_NotEnoughPlayers() {
        GameStartRequestDto request = new GameStartRequestDto(1);

        when(roomService.isRoomValidForGame(testRoom.getId())).thenReturn(true);
        when(userRoomService.isValidHost(testRoom.getId(), request.userId())).thenReturn(true);
        when(roomService.getRoomById(testRoom.getId())).thenReturn(testRoom);
        when(userRoomService.getUserRoomsByRoom(testRoom)).thenReturn(List.of(hostUserRoom));
        when(roomService.getMaxCapacity(testRoom.getRoomType())).thenReturn(2);

        ApiResponse<Void> response = gameService.startGame(testRoom.getId(), request);

        assertEquals(201, response.code());
        verify(roomService, never()).updateRoom(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
