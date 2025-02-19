package prography.spring.pingpong.domain.game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
import prography.spring.pingpong.domain.userroom.repository.UserRoomRepository;
import prography.spring.pingpong.model.entity.RoomStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class GameTransactionService {

    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public void endGameTransactional(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || room.getStatus() != RoomStatus.PROGRESS) {
            log.warn("🚨 [GameTransactionService] 종료할 방이 없음 또는 이미 종료됨 (roomId={})", roomId);
            return;
        }

        room.setStatus(RoomStatus.FINISH);

        log.info("⏳ [GameTransactionService] 방 종료 시, 모든 유저 제거 (roomId={})", roomId);
        userRoomRepository.deleteByRoom(room);

        roomRepository.save(room);
        log.info("✅ [GameTransactionService] 게임 종료 및 모든 유저 제거 완료 (roomId={})", roomId);
    }
}
