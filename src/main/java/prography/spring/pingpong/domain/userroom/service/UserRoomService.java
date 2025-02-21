package prography.spring.pingpong.domain.userroom.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class UserRoomService {

    private final UserService userService;
    private final RoomService roomService;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public ApiResponse<Void> joinRoom(int roomId, RoomJoinRequestDto request) {
        log.info("📌 [UserRoomService] 방 참가 요청");

        Room room = validateRoom(roomId);
        User user = validateUser(request.userId());

        if (room == null || user == null || userRoomRepository.existsByUserId(request.userId())
                || user.getStatus() != UserStatus.ACTIVE) {
            return ApiResponse.badRequest();
        }

        List<UserRoom> userRooms = userRoomRepository.findByRoom(room);
        int maxCapacity = roomService.getMaxCapacity(room.getRoomType());
        if (userRooms.size() >= maxCapacity) {
            log.warn("🚨 [UserRoomService] 방 정원이 가득 참 (roomId={})",room.getId());
            return ApiResponse.badRequest();
        }

        Team assignedTeam = assignTeam(userRooms, maxCapacity);
        UserRoom userRoom = UserRoom.builder()
                .user(user)
                .room(room)
                .team(assignedTeam)
                .build();
        updateUserRoom(userRoom);

        room.setUpdatedAt(LocalDateTime.now());
        roomService.updateRoom(room);

        log.info("✅ [UserRoomService] 방 참가 완료");
        return ApiResponse.success(null);
    }

    @Transactional
    public ApiResponse<Void> exitRoom(int roomId, RoomExitRequestDto request) {
        log.info("📌 [UserRoomService] 방 나가기 요청");

        Room room = validateRoom(roomId);
        User user = validateUser(request.userId());
        UserRoom userRoom = getUserRoom(user, room);

        if (user == null || room == null || userRoom == null) {
            return ApiResponse.badRequest();
        }

        if (room.getHost().equals(user)) {
            room.setStatus(RoomStatus.FINISH);
            roomService.updateRoom(room);
            userRoomRepository.deleteByRoom(room);
        } else {
            userRoomRepository.delete(userRoom);
        }
        return ApiResponse.success(null);
    }

    @Transactional
    public ApiResponse<Void> changeTeam(int roomId, TeamChangeRequestDto request) {
        log.info("📌 [UserRoomService] 팀 변경 요청");

        Room room = validateRoom(roomId);
        User user = validateUser(request.userId());
        UserRoom userRoom = getUserRoom(user, room);

        if (user == null || room == null || userRoom == null) {
            return ApiResponse.badRequest();
        }

        List<UserRoom> userRooms = userRoomRepository.findByRoom(room);
        int maxCapacity = roomService.getMaxCapacity(room.getRoomType());
        long redCount = userRooms.stream().filter(ur -> ur.getTeam() == Team.RED).count();
        long blueCount = userRooms.stream().filter(ur -> ur.getTeam() == Team.BLUE).count();

        Team newTeam = (userRoom.getTeam() == Team.RED) ? Team.BLUE : Team.RED;
        if (isTeamChangeAllowed(newTeam, redCount, blueCount, maxCapacity)) {
            log.error("❌ 팀 변경시 에러2 newTeam={}, redCount={}, blueCount={}, maxCapacity={}",
                    newTeam, redCount,blueCount, maxCapacity);
            return ApiResponse.badRequest();
        }

        userRoom.setTeam(newTeam);
        updateUserRoom(userRoom);
        room.setUpdatedAt(LocalDateTime.now());
        roomService.updateRoom(room);

        return ApiResponse.success(null);
    }

    private User validateUser(int userId) {
        return userService.getUserById(userId);
    }

    private Room validateRoom(int roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null || room.getStatus() != RoomStatus.WAIT) {
            return null;
        }
        return room;
    }

    private boolean isTeamChangeAllowed(Team newTeam, long redCount, long blueCount, int maxCapacity) {
        return (newTeam == Team.RED && redCount >= maxCapacity / 2) ||
                (newTeam == Team.BLUE && blueCount >= maxCapacity / 2);
    }

    @Transactional(readOnly = true)
    public UserRoom getUserRoom(User user, Room room) {
        return userRoomRepository.findByUserAndRoom(user, room).orElse(null);
    }

    private Team assignTeam(List<UserRoom> userRooms, int maxCapacity) {
        long redCount = userRooms.stream().filter(ur -> ur.getTeam() == Team.RED).count();
        return (redCount < maxCapacity / 2) ? Team.RED : Team.BLUE;
    }

    @Transactional
    public void deleteAllUserRooms() {
        userRoomRepository.deleteAll();
        log.info("✅ [UserRoomService] 모든 UserRoom 데이터 삭제 완료");
    }

    @Transactional(readOnly = true)
    public List<UserRoom> getUserRoomsByRoom(Room room) {
        return userRoomRepository.findByRoom(room);
    }

    @Transactional
    public void deleteUserRoomsByRoom(Room room) {
        userRoomRepository.deleteByRoom(room);
    }

    @Transactional
    public void updateUserRoom(UserRoom userRoom) {
        userRoomRepository.save(userRoom);
    }

    public boolean isValidHost(int roomId, int userId) {
        Room room = roomService.getRoomById(roomId);
        return room != null && room.getHost().getId().equals(userId);
    }
}
