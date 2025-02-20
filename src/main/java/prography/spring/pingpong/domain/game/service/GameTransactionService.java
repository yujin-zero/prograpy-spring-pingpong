package prography.spring.pingpong.domain.game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.service.RoomService;
import prography.spring.pingpong.domain.userroom.service.UserRoomService;
import prography.spring.pingpong.model.entity.RoomStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class GameTransactionService {

    private final RoomService roomService;
    private final UserRoomService userRoomService;

    @Transactional
    public void endGameTransactional(Long roomId) {
        try {
            Room room = roomService.getRoomById(roomId);
            if (room == null || room.getStatus() != RoomStatus.PROGRESS) {
                log.warn("🚨 [GameTransactionService] 종료할 방이 없음 또는 이미 종료됨 (roomId={})", roomId);
                return;
            }

            room.setStatus(RoomStatus.FINISH);
            log.info("⏳ [GameTransactionService] 방 종료 시, 모든 유저 제거 (roomId={})", roomId);

            userRoomService.deleteUserRoomsByRoom(room);
            roomService.updateRoom(room);

            log.info("✅ [GameTransactionService] 게임 종료 및 모든 유저 제거 완료 (roomId={})", roomId);
        } catch (Exception e) {
            log.error("🚨 [GameTransactionService] 게임 종료 처리 중 오류 발생",e);
        }

    }
}
