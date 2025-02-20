package prography.spring.pingpong.domain.room.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.room.model.dto.*;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.service.UserService;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;
import prography.spring.pingpong.domain.userroom.repository.UserRoomRepository;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final UserService userService;

    @Value("${room.max-capacity.single}")
    private int maxCapacitySingle;

    @Value("${room.max-capacity.multi}")
    private int maxCapacityMulti;

    @Transactional
    public ApiResponse<Void> createRoom(RoomCreateRequestDto requestDto) {
        log.info("📌 [RoomService] 방 생성 요청 (userId={}, roomType={}, title={})",
                requestDto.userId(), requestDto.roomType(), requestDto.title());

        User user = validateUser(requestDto.userId());
        if (user == null) return ApiResponse.badRequest();

        RoomType roomType = parseRoomType(requestDto.roomType());
        if (roomType == null) return ApiResponse.badRequest();

        Room room = createNewRoom(user, requestDto.title(), roomType);
        log.info("✅ [RoomService] 방 생성 완료 (roomId={})", room.getId());

        assignUserToRoom(user, room);
        log.info("✅ [RoomService] UserRoom 등록 완료 (userId={}, roomId={})",
                user.getId(), room.getId());

        return ApiResponse.success(null);
    }

    private User validateUser(Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            log.error("🚨 [RoomService] 유저를 찾을 수 없음. (userId={})", userId);
            return null;
        }
        if (user.getUserstatus() != UserStatus.ACTIVE) {
            log.warn("🚨 [RoomService] 유저 상태가 ACTIVE가 아님. (userId={})", userId);
            return null;
        }
        if (userRoomRepository.existsByUser(user)) {
            log.warn("🚨 [RoomService] 유저가 이미 다른 방에 참여 중. (userId={})", userId);
            return null;
        }
        return user;
    }

    private RoomType parseRoomType(String roomTypeStr) {
        try {
            return RoomType.valueOf(roomTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("🚨 [RoomService] 잘못된 방 타입 입력. (roomType={})", roomTypeStr);
            return null;
        }
    }

    private Room createNewRoom(User host, String title, RoomType roomType) {
        Room room = Room.builder()
                .host(host)
                .title(title)
                .roomType(roomType)
                .status(RoomStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return roomRepository.save(room);
    }

    private void assignUserToRoom(User user, Room room) {
        UserRoom userRoom = UserRoom.builder()
                .user(user)
                .room(room)
                .team(Team.RED)
                .build();
        userRoomRepository.save(userRoom);
    }

    @Transactional(readOnly = true)
    public ApiResponse<RoomListResponseDto> getAllRooms(int page, int size) {
        log.info("📌 [RoomService] 방 전체 조회 요청");

        PageRequest pageRequest = PageRequest.of(page, size, Direction.ASC,"id");
        Page<RoomResponseDto> roomPage = roomRepository.findAll(pageRequest)
                .map(RoomResponseDto::fromEntity);

        return ApiResponse.success(RoomListResponseDto.fromPage(roomPage));
    }

    @Transactional(readOnly = true)
    public ApiResponse<RoomDetailResponseDto> getRoomDetail(Long roomId) {
        log.info("📌 [RoomService] 방 상세 조회 요청");

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.warn("🚨 [RoomService] 존재하지 않는 방 (roomId={})", roomId);
        }

        assert room != null;
        return ApiResponse.success(RoomDetailResponseDto.fromEntity(room));
    }

    public boolean isRoomValidForGame(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return false;
        }
        return room.getStatus() == RoomStatus.WAIT;
    }

    @Transactional
    public void deleteAllRooms() {
        roomRepository.deleteAll();
        log.info("✅ [RoomService] 모든 Room 데이터 삭제 완료");
    }

    @Transactional
    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId).orElse(null);
    }

    @Transactional
    public void updateRoom(Room room) {
        roomRepository.save(room);
        log.info("✅ [RoomService] 방 정보 업데이트 완료 (roomId={}, status={})", room.getId(),room.getStatus());
    }

    public int getMaxCapacity(RoomType roomType) {
        return (roomType == RoomType.SINGLE) ? maxCapacitySingle : maxCapacityMulti;
    }
}
