package prography.spring.pingpong.domain.userroom.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.service.RoomService;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.service.UserService;
import prography.spring.pingpong.domain.userroom.model.dto.RoomExitRequestDto;
import prography.spring.pingpong.domain.userroom.model.dto.RoomJoinRequestDto;
import prography.spring.pingpong.domain.userroom.model.dto.TeamChangeRequestDto;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;
import prography.spring.pingpong.domain.userroom.repository.UserRoomRepository;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.RoomStatus;
import prography.spring.pingpong.model.entity.Team;
import prography.spring.pingpong.model.entity.UserStatus;


@ExtendWith(MockitoExtension.class)
class UserRoomServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RoomService roomService;

    @Mock
    private UserRoomRepository userRoomRepository;

    @InjectMocks
    private UserRoomService userRoomService;

    private Room testRoom;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .name("테스트 유저")
                .status(UserStatus.ACTIVE)
                .build();

        testRoom = Room.builder()
                .id(100)
                .host(testUser)
                .status(RoomStatus.WAIT)
                .build();
    }

    @Test
    @DisplayName("✅ 방 참가 - 성공")
    void joinRoom_Success() {
        RoomJoinRequestDto request = new RoomJoinRequestDto(testUser.getId());

        when(roomService.getRoomById(testRoom.getId())).thenReturn(testRoom);
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userRoomRepository.existsByUserId(testUser.getId())).thenReturn(false);
        when(userRoomRepository.findByRoom(testRoom)).thenReturn(List.of());
        when(roomService.getMaxCapacity(testRoom.getRoomType())).thenReturn(4);

        ApiResponse<Void> response = userRoomService.joinRoom(testRoom.getId(), request);

        assertEquals(200, response.code());
        verify(userRoomRepository, times(1)).save(any(UserRoom.class));
    }

    @Test
    @DisplayName("✅ 방 참가 - 실패 (방 정원 초과)")
    void joinRoom_FullCapacity() {
        RoomJoinRequestDto request = new RoomJoinRequestDto(testUser.getId());

        when(roomService.getRoomById(testRoom.getId())).thenReturn(testRoom);
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userRoomRepository.existsByUserId(testUser.getId())).thenReturn(false);
        when(userRoomRepository.findByRoom(testRoom)).thenReturn(List.of(new UserRoom(), new UserRoom()));

        ApiResponse<Void> response = userRoomService.joinRoom(testRoom.getId(), request);

        assertEquals(201, response.code());
        verify(userRoomRepository, never()).save(any(UserRoom.class));
    }

    @Test
    @DisplayName("✅ 방 나가기 - 성공")
    void exitRoom_Success() {
        User nonHostUser = User.builder()
                .id(2)
                .name("일반 유저")
                .status(UserStatus.ACTIVE)
                .build();

        RoomExitRequestDto request = new RoomExitRequestDto(nonHostUser.getId());

        UserRoom nonHostUserRoom = UserRoom.builder()
                .id(11)
                .user(nonHostUser)
                .room(testRoom)
                .team(Team.RED)
                .build();

        when(roomService.getRoomById(testRoom.getId())).thenReturn(testRoom);
        when(userService.getUserById(nonHostUser.getId())).thenReturn(nonHostUser);
        when(userRoomRepository.findByUserAndRoom(nonHostUser, testRoom)).thenReturn(Optional.of(nonHostUserRoom));

        ApiResponse<Void> response = userRoomService.exitRoom(testRoom.getId(), request);

        assertEquals(200, response.code());
        verify(userRoomRepository, times(1)).delete(nonHostUserRoom);  // 개별 유저 삭제 검증
        verify(userRoomRepository, never()).deleteByRoom(testRoom);
    }

    @Test
    @DisplayName("✅ 방 나가기 - 실패 (존재하지 않는 유저)")
    void exitRoom_UserNotFound() {
        RoomExitRequestDto request = new RoomExitRequestDto(999);

        when(userService.getUserById(999)).thenReturn(null);

        ApiResponse<Void> response = userRoomService.exitRoom(testRoom.getId(), request);

        assertEquals(201, response.code());
        verify(userRoomRepository, never()).delete(any(UserRoom.class));
    }

    @Test
    @DisplayName("✅ 팀 변경 - 성공")
    void changeTeam_Success() {
        TeamChangeRequestDto request = new TeamChangeRequestDto(testUser.getId());

        UserRoom testUserRoom = UserRoom.builder()
                .id(10)
                .user(testUser)
                .room(testRoom)
                .team(Team.RED)
                .build();

        List<UserRoom> userRooms = List.of(testUserRoom);

        when(roomService.getRoomById(testRoom.getId())).thenReturn(testRoom);
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userRoomRepository.findByUserAndRoom(testUser, testRoom)).thenReturn(Optional.of(testUserRoom));
        when(userRoomRepository.findByRoom(testRoom)).thenReturn(userRooms);
        when(roomService.getMaxCapacity(testRoom.getRoomType())).thenReturn(4);

        ApiResponse<Void> response = userRoomService.changeTeam(testRoom.getId(), request);

        assertEquals(200, response.code());
        assertEquals(Team.BLUE, testUserRoom.getTeam());
        verify(userRoomRepository, times(1)).save(testUserRoom);
    }

    @Test
    @DisplayName("✅ 팀 변경 - 실패 (유저가 방에 없음)")
    void changeTeam_UserNotInRoom() {
        TeamChangeRequestDto request = new TeamChangeRequestDto(testUser.getId());

        when(roomService.getRoomById(testRoom.getId())).thenReturn(testRoom);
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userRoomRepository.findByUserAndRoom(testUser, testRoom)).thenReturn(Optional.empty());

        ApiResponse<Void> response = userRoomService.changeTeam(testRoom.getId(), request);

        assertEquals(201, response.code());
        verify(userRoomRepository, never()).save(any(UserRoom.class));
    }

    @Test
    @DisplayName("✅ 팀 변경 - 실패 (팀 정원 초과)")
    void changeTeam_TeamFull() {
        TeamChangeRequestDto request = new TeamChangeRequestDto(testUser.getId());

        UserRoom testUserRoom = UserRoom.builder()
                .id(10)
                .user(testUser)
                .room(testRoom)
                .team(Team.RED)
                .build();

        List<UserRoom> userRooms = List.of(
                testUserRoom,
                UserRoom.builder().team(Team.RED).build(),
                UserRoom.builder().team(Team.BLUE).build(),
                UserRoom.builder().team(Team.BLUE).build()
        );

        when(roomService.getRoomById(testRoom.getId())).thenReturn(testRoom);
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userRoomRepository.findByUserAndRoom(testUser, testRoom)).thenReturn(Optional.of(testUserRoom));
        when(userRoomRepository.findByRoom(testRoom)).thenReturn(userRooms);
        when(roomService.getMaxCapacity(testRoom.getRoomType())).thenReturn(4); // 최대 정원 4명

        ApiResponse<Void> response = userRoomService.changeTeam(testRoom.getId(), request);

        assertEquals(201, response.code());
        verify(userRoomRepository, never()).save(any(UserRoom.class));
    }
}