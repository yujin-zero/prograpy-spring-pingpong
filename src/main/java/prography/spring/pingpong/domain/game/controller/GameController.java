package prography.spring.pingpong.domain.game.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.service.OperationService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prography.spring.pingpong.domain.game.model.dto.GameStartRequestDto;
import prography.spring.pingpong.domain.game.service.GameService;
import prography.spring.pingpong.model.dto.ApiResponse;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final OperationService operationBuilder;

    @Operation(summary = "게임 시작 API", description = "호스트가 게임을 시작합니다.")
    @PutMapping("/start/{roomId}")
    public ApiResponse<Void> startGame(
            @PathVariable Long roomId,
            @RequestBody GameStartRequestDto requestDto
            ) {
        return gameService.startGame(roomId, requestDto);
    }
}
