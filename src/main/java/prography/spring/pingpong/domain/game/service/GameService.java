package prography.spring.pingpong.domain.game.service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.game.event.GameStartedEvent;
import prography.spring.pingpong.domain.game.model.dto.GameStartRequestDto;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.service.RoomService;
import prography.spring.pingpong.domain.userroom.model.entity.UserRoom;
import prography.spring.pingpong.domain.userroom.service.UserRoomService;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.RoomStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class GameService {

    private final GameTransactionService gameTransactionService;
    private final RoomService roomService;
    private final UserRoomService userRoomService;
    private final ScheduledExecutorService scheduler;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${game.duration-ms}")
    private long gameDurationMs;

    @Transactional
    public ApiResponse<Void> startGame(int roomId, GameStartRequestDto request) {
        if (!roomService.isRoomValidForGame(roomId) || !userRoomService.isValidHost(roomId, request.userId())) {
            return ApiResponse.badRequest();
        }

        Room room = roomService.getRoomById(roomId);

        List<UserRoom> userRooms = userRoomService.getUserRoomsByRoom(room);
        int maxCapacity = roomService.getMaxCapacity(room.getRoomType());

        if (userRooms.size() < maxCapacity) {
            log.warn("🚨 [GameService] 방에 필요한 사용자 수가 채워지지 않음 (userRoomsSize={}, maxCapacity={})",
                    userRooms.size(), maxCapacity);
            return ApiResponse.badRequest();
        }

        room.setStatus(RoomStatus.PROGRESS);
        roomService.updateRoom(room);
        log.info("✅ [GameService] 게임 시작됨 (roomId={})", roomId);

        eventPublisher.publishEvent(new GameStartedEvent(roomId));

        return ApiResponse.success(null);
    }

    public void scheduleGameEnd(int roomId) {
        scheduler.schedule(() -> {
            try {
                log.info("⏳ [GameService] 게임 종료 실행 (roomId={})", roomId);
                gameTransactionService.endGameTransactional(roomId);
            } catch (Exception e) {
                log.error("🚨 [GameService] 게임 종료 중 오류 발생 (roomId={})", roomId, e);
            }
        }, gameDurationMs, TimeUnit.MILLISECONDS);
    }
}
