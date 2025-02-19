package prography.spring.pingpong.domain.game.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.game.model.dto.GameStartRequestDto;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
import prography.spring.pingpong.domain.room.service.RoomService;
import prography.spring.pingpong.domain.user.repository.UserRepository;
import prography.spring.pingpong.domain.user.service.UserService;
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
    private final RoomService roomService;
    private final UserService userService;

    @Value("${game.duration-ms}")
    private long gameDurationMs;

    @Value("${room.max-capacity.single}")
    private int maxCapacitySingle;

    @Value("${room.max-capacity.multi}")
    private int maxCapacityMulti;

    @Transactional
    public ApiResponse<Void> startGame(Long roomId, GameStartRequestDto request) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (!roomService.isRoomValidForGame(roomId) || !userService.isValidHost(roomId, request.userId())) {
            return ApiResponse.badRequest();
        }

        List<UserRoom> userRooms = userRoomRepository.findByRoom(room);
        int maxCapacity = getMaxCapacity(room.getRoomType());
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

    @Async
    public void scheduleGameEnd(Long roomId) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("⏳ [GameService] 게임 종료 스케줄링 시작 (roomId={})", roomId);
                Thread.sleep(gameDurationMs);
                gameTransactionService.endGameTransactional(roomId);
            } catch (InterruptedException e) {
                log.error("🚨 [GameService] 게임 종료 스케줄링 오류 (roomId={})", roomId, e);
                Thread.currentThread().interrupt();
            }
        });
    }

    public int getMaxCapacity(RoomType roomType) {
        return (roomType == RoomType.SINGLE) ? maxCapacitySingle : maxCapacityMulti;
    }
}
