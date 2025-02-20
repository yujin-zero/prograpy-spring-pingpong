package prography.spring.pingpong.domain.userroom.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.repository.UserRepository;
import prography.spring.pingpong.domain.userroom.model.dto.RoomExitRequestDto;
import prography.spring.pingpong.domain.userroom.model.dto.RoomJoinRequestDto;
import prography.spring.pingpong.domain.userroom.model.dto.TeamChangeRequestDto;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;
import prography.spring.pingpong.domain.userroom.repository.UserRoomRepository;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.RoomStatus;
import prography.spring.pingpong.model.entity.RoomType;
import prography.spring.pingpong.model.entity.Team;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserRoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public ApiResponse<Void> joinRoom(Long roomId, RoomJoinRequestDto request) {
        log.info("📌 [UserRoomService] 방 참가 요청");

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || room.getStatus() != RoomStatus.WAIT) {
            return ApiResponse.badRequest();
        }

        User user = userRepository.findById(request.userId()).orElse(null);
        if (user == null || userRoomRepository.existsByUser(user)) {
            return ApiResponse.badRequest();
        }

        List<UserRoom> userRooms = userRoomRepository.findByRoom(room);
        int maxCapacity = room.getRoomType() == RoomType.SINGLE ? 2 : 4;
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
        userRoomRepository.save(userRoom);

        log.info("✅ [UserRoomService] 방 참가 완료");
        return ApiResponse.success(null);
    }

    @Transactional
    public ApiResponse<Void> exitRoom(Long roomId, RoomExitRequestDto request) {
        log.info("📌 [UserRoomService] 방 나가기 요청");

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || room.getStatus() == RoomStatus.PROGRESS || room.getStatus() == RoomStatus.FINISH) {
            return ApiResponse.badRequest();
        }

        User user = getUserById(request.userId());
        if (user == null) {
            return ApiResponse.badRequest();
        }

        UserRoom userRoom = getUserRoom(user, room);
        if (userRoom == null) {
            return ApiResponse.badRequest();
        }

        if (room.getHost().equals(user)) {
            userRoomRepository.deleteByRoom(room);
            room.setStatus(RoomStatus.FINISH);
            roomRepository.save(room);
        } else {
            userRoomRepository.delete(userRoom);
        }
        return ApiResponse.success(null);
    }

    @Transactional
    public ApiResponse<Void> changeTeam(Long roomId, TeamChangeRequestDto requestDto) {
        log.info("📌 [UserRoomService] 팀 변경 요청");

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || room.getStatus() != RoomStatus.WAIT) {
            return ApiResponse.badRequest();
        }

        User user = getUserById(requestDto.userId());
        if (user == null) {
            return ApiResponse.badRequest();
        }

        UserRoom userRoom = getUserRoom(user, room);
        if (userRoom == null) {
            return ApiResponse.badRequest();
        }

        List<UserRoom> userRooms = userRoomRepository.findByRoom(room);
        int maxCapacity = room.getRoomType() == RoomType.SINGLE ? 2 : 4;
        long redCount = userRooms.stream().filter(ur -> ur.getTeam() == Team.RED).count();
        long blueCount = userRooms.stream().filter(ur -> ur.getTeam() == Team.BLUE).count();

        Team newTeam = (userRoom.getTeam() == Team.RED) ? Team.BLUE : Team.RED;
        if ((newTeam == Team.RED && redCount >= maxCapacity/2) ||
                (newTeam == Team.BLUE && blueCount >= maxCapacity/2)) {
            return ApiResponse.badRequest();
        }

        userRoom.setTeam(newTeam);
        userRoomRepository.save(userRoom);

        return ApiResponse.success(null);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    private UserRoom getUserRoom(User user, Room room) {
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
}
