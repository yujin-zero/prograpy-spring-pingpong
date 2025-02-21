package prography.spring.pingpong.domain.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.room.service.RoomService;
import prography.spring.pingpong.domain.user.service.UserService;
import prography.spring.pingpong.domain.userroom.service.UserRoomService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCleanupService {
    private final UserService userService;
    private final RoomService roomService;
    private final UserRoomService userRoomService;

    @Transactional
    public void deleteAllData() {
        userRoomService.deleteAllUserRooms();
        userService.deleteAllUsers();
        roomService.deleteAllRooms();
        log.info("✅ [DataCleanupService] 모든 데이터 삭제 완료");
    }
}
