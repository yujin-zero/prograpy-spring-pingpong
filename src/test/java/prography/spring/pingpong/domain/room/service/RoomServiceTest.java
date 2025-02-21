package prography.spring.pingpong.domain.room.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import prography.spring.pingpong.domain.room.model.dto.*;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.service.UserService;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;
import prography.spring.pingpong.domain.userroom.repository.UserRoomRepository;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRoomRepository userRoomRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RoomService roomService;

    private User testUser;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .name("테스트 유저")
                .status(UserStatus.ACTIVE)
                .build();

        testRoom = Room.builder()
                .id(100)
                .title("테스트 방")
                .host(testUser)
                .roomType(RoomType.SINGLE)
                .status(RoomStatus.WAIT)
                .build();
    }

    @Test
    @DisplayName("✅ 방 생성 - 성공")
    void createRoom_Success() {
        RoomCreateRequestDto requestDto = new RoomCreateRequestDto(testUser.getId(), "SINGLE", "새로운 방");

        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(userRoomRepository.existsByUser(testUser)).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        ApiResponse<Void> response = roomService.createRoom(requestDto);

        assertEquals(200, response.code());
        verify(roomRepository, times(1)).save(any(Room.class));
        verify(userRoomRepository, times(1)).save(any(UserRoom.class));
    }

    @Test
    @DisplayName("✅ 방 생성 - 실패 (존재하지 않는 유저)")
    void createRoom_Fail_UserNotFound() {
        RoomCreateRequestDto requestDto = new RoomCreateRequestDto(999, "SINGLE", "방 제목");

        when(userService.getUserById(999)).thenReturn(null);

        ApiResponse<Void> response = roomService.createRoom(requestDto);

        assertEquals(201, response.code());
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ 방 전체 조회 - 성공")
    void getAllRooms_Success() {
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Room> roomPage = new PageImpl<>(List.of(testRoom), pageRequest, 1);

        when(roomRepository.findAll(any(PageRequest.class))).thenReturn(roomPage);

        ApiResponse<RoomListResponseDto> response = roomService.getAllRooms(page, size);

        assertEquals(200, response.code());
        assertNotNull(response.result());
        assertEquals(1, response.result().getTotalElements());
        assertEquals(1, response.result().getRoomList().size());
        verify(roomRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("✅ 방 상세 조회 - 성공")
    void getRoomDetail_Success() {
        when(roomRepository.findById(testRoom.getId())).thenReturn(Optional.of(testRoom));

        ApiResponse<RoomDetailResponseDto> response = roomService.getRoomDetail(testRoom.getId());

        assertEquals(200, response.code());
        assertNotNull(response.result());
        assertEquals(testRoom.getId(), response.result().getId());
        assertEquals(testRoom.getTitle(), response.result().getTitle());
        verify(roomRepository, times(1)).findById(testRoom.getId());
    }

    @Test
    @DisplayName("✅ 방 상세 조회 - 실패 (존재하지 않는 방)")
    void getRoomDetail_Fail_RoomNotFound() {
        when(roomRepository.findById(999)).thenReturn(Optional.empty());

        ApiResponse<RoomDetailResponseDto> response = roomService.getRoomDetail(999);

        assertEquals(201, response.code());
        verify(roomRepository, times(1)).findById(999);
    }
}
