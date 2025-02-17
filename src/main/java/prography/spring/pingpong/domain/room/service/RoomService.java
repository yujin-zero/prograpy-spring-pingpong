package prography.spring.pingpong.domain.room.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import prography.spring.pingpong.domain.room.model.dto.RoomCreateRequestDto;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.repository.UserRepository;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;
import prography.spring.pingpong.domain.userroom.repository.UserRoomRepository;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.RoomStatus;
import prography.spring.pingpong.model.entity.RoomType;
import prography.spring.pingpong.model.entity.Team;
import prography.spring.pingpong.model.entity.UserStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public ApiResponse<Void> createRoom(RoomCreateRequestDto requestDto) {
        log.info("📌 [RoomService] 방 생성 요청 (userId={}, roomType={}, title={})",
                requestDto.userId(), requestDto.roomType(), requestDto.title());

        // 유저 조회
        User user = userRepository.findById(requestDto.userId()).orElse(null);
        if (user == null) {
            log.error("🚨 [RoomService] 유저를 찾을 수 없음. (userId={})",requestDto.userId());
            return ApiResponse.badRequest();
        }

        // 유저 상태 확인
        if (user.getUserstatus() != UserStatus.ACTIVE) {
            log.warn("🚨 [RoomService] 유저 상태가 ACTIVE가 아님. (userId={})",requestDto.userId());
            return ApiResponse.badRequest();
        }

        // 유저가 이미 참여한 방이 있는지 확인
        boolean isUserInRoom = userRoomRepository.existsByUser(user);
        if (isUserInRoom) {
            log.warn("🚨 [RoomService] 유저가 이미 다른 방에 참여 중. (userId={})",requestDto.userId());
            return ApiResponse.badRequest();
        }

        // 방 타입 변환
        RoomType roomType;
        try {
            roomType = RoomType.valueOf(requestDto.roomType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("🚨 [RoomService] 잘못된 방 타입 입력. (roomType={})",requestDto.roomType());
            return ApiResponse.badRequest();
        }

        // 방 생성
        Room room = Room.builder()
                .host(user)
                .title(requestDto.title())
                .roomType(roomType)
                .status(RoomStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        roomRepository.save(room);
        log.info("✅ [RoomService] 방 생성 완료 (roomId={})", room.getId());

        // 생성한 유저를 UserRoom 테이블에 추가 (RED팀 우선 배정)
        UserRoom userRoom = UserRoom.builder()
                .user(user)
                .room(room)
                .team(Team.RED)
                .build();

        userRoomRepository.save(userRoom);
        log.info("✅ [RoomService] UserRoom 등록 완료 (userId={}, roomId={}, team={})",
                user.getId(), room.getId(), userRoom.getTeam());

        return ApiResponse.success(null);
    }
}
