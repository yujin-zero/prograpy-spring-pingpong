package prography.spring.pingpong.domain.game.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.game.model.dto.GameStartRequestDto;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.repository.UserRepository;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;
import prography.spring.pingpong.domain.userroom.repository.UserRoomRepository;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.RoomStatus;
import prography.spring.pingpong.model.entity.RoomType;

@Slf4j
@RequiredArgsConstructor
@Service
public class GameService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;
    private final GameTransactionService gameTransactionService;

    @Transactional
    public ApiResponse<Void> startGame(Long roomId, GameStartRequestDto request) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || !isHostUser(room, request.userId()) || !isRoomReadyForGame(room)) {
            return ApiResponse.badRequest();
        }

        List<UserRoom> userRooms = userRoomRepository.findByRoom(room);
        int maxCapacity = (room.getRoomType() == RoomType.SINGLE) ? 2 : 4;
        if (userRooms.size() < maxCapacity) {
            log.warn("🚨 [GameService] 방에 필요한 사용자 수가 채워지지 않음 (userRoomsSize={}, maxCapacity={})",
                    userRooms.size(), maxCapacity);
            return ApiResponse.badRequest();
        }

        room.setStatus(RoomStatus.PROGRESS);
        roomRepository.save(room);
        log.info("✅ [GameService] 게임 시작됨 (roomId={})", roomId);

        scheduleGameEnd(roomId);

        return ApiResponse.success(null);
    }

    private boolean isHostUser(Room room, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("🚨 [GameService] 사용자 없음 (userId={})", userId);
            return false;
        }
        return room.getHost().equals(user);
    }

    private boolean isRoomReadyForGame(Room room) {
        log.warn("🚨 [GameService] 방이 게임 준비 상태가 아님 (roomId={})", room.getId());
        return room.getStatus() == RoomStatus.WAIT;
    }

    @Async
    public void scheduleGameEnd(Long roomId) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("⏳ [GameService] 게임 종료 스케줄링 시작 (roomId={})", roomId);
                Thread.sleep(60 * 1000);
                gameTransactionService.endGameTransactional(roomId);
            } catch (InterruptedException e) {
                log.error("🚨 [GameService] 게임 종료 스케줄링 오류 (roomId={})", roomId, e);
                Thread.currentThread().interrupt();
            }
        });
    }
}
