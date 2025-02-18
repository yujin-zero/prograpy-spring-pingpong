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
        if (room == null) {
            log.warn("🚨 [UserRoomService] 존재하지 않는 방 (roomId={})",roomId);
            return ApiResponse.badRequest();
        }

        if (room.getStatus() != RoomStatus.WAIT) {
            log.warn("🚨 [UserRoomService] 방이 WAIT 상태가 아님 (roomId={}, status={})",roomId,room.getStatus());
            return ApiResponse.badRequest();
        }

        User user = userRepository.findById(request.userId()).orElse(null);
        if (user == null) {
            log.warn("🚨 [UserRoomService] 존재하지 않는 유저 (userId={})",user.getId());
            return ApiResponse.badRequest();
        }

        if (userRoomRepository.existsByUser(user)) {
            log.warn("🚨 [UserRoomService] 유저가 이미 다른 방에 참여 중 (userId={})",user.getId());
            return ApiResponse.badRequest();
        }

        List<UserRoom> userRooms = userRoomRepository.findByRoom(room);
        int maxCapacity = room.getRoomType() == RoomType.SINGLE ? 2 : 4;
        if (userRooms.size() >= maxCapacity) {
            log.warn("🚨 [UserRoomService] 방 정원이 가득 참 (roomId={})",room.getId());
            return ApiResponse.badRequest();
        }

        long redCount = userRooms.stream().filter(ur -> ur.getTeam() == Team.RED).count();
        Team assignedTeam = (redCount < maxCapacity/2) ? Team.RED : Team.BLUE;

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
        if (room == null) {
            return ApiResponse.badRequest();
        }

        if (room.getStatus() == RoomStatus.PROGRESS || room.getStatus() == RoomStatus.FINISH) {
            return ApiResponse.badRequest();
        }

        User user = userRepository.findById(request.userId()).orElse(null);
        if (user == null) {
            return ApiResponse.badRequest();
        }

        UserRoom userRoom = (UserRoom) userRoomRepository.findByUserAndRoom(user, room).orElse(null);
        if (userRoom == null) {
            return ApiResponse.badRequest();
        }

        if (room.getHost().equals(user)) {
            userRoomRepository.deleteByRoom(room);
            room.setStatus(RoomStatus.FINISH);
            roomRepository.save(room);
            return ApiResponse.success(null);
        }

        userRoomRepository.delete(userRoom);

        return ApiResponse.success(null);
    }
}
