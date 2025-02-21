package prography.spring.pingpong.domain.game.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import prography.spring.pingpong.domain.game.service.GameService;

@RequiredArgsConstructor
@Component
public class GameEventListener {

    private final GameService gameService;

    @Async
    @TransactionalEventListener
    public void onGameStarted(GameStartedEvent event) {
        gameService.scheduleGameEnd(event.roomId());
    }
}
