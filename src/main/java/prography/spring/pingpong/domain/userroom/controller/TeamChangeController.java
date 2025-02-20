package prography.spring.pingpong.domain.userroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prography.spring.pingpong.domain.userroom.model.dto.TeamChangeRequestDto;
import prography.spring.pingpong.domain.userroom.service.UserRoomService;
import prography.spring.pingpong.model.dto.ApiResponse;

@Tag(name = "TeamChange", description = "팀 변경 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/team")
public class TeamChangeController {

    private final UserRoomService userRoomService;

    @Operation(summary = "팀 변경 API", description = "유저가 특정 방에서 팀을 변경합니다.")
    @PutMapping("/{roomId}")
    public ApiResponse<Void> chageTeam(
            @PathVariable Long roomId,
            @RequestBody TeamChangeRequestDto requestDto
            ) {
        return userRoomService.changeTeam(roomId, requestDto);
    }
}
