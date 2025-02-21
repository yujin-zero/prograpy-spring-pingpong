package prography.spring.pingpong.domain.userroom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prography.spring.pingpong.domain.userroom.model.dto.RoomExitRequestDto;
import prography.spring.pingpong.domain.userroom.model.dto.RoomJoinRequestDto;
import prography.spring.pingpong.domain.userroom.service.UserRoomService;
import prography.spring.pingpong.model.dto.ApiResponse;

@Tag(name = "UserRoom", description = "유저와 방 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class UserRoomController {

    private final UserRoomService userRoomService;

    @Operation(summary = "방 참가 API", description = "유저가 특정 방에 참가합니다.")
    @PostMapping("/attention/{roomId}")
    public ApiResponse<Void> joinRoom(
            @PathVariable int roomId,
            @RequestBody RoomJoinRequestDto requestDto
            ) {
        return userRoomService.joinRoom(roomId, requestDto);
    }

    @Operation(summary = "방 나가기 API", description = "유저가 특정 방을 나갑니다.")
    @PostMapping("/out/{roomId}")
    public ApiResponse<Void> exitRoom(
            @PathVariable int roomId,
            @RequestBody RoomExitRequestDto requestDto
            ) {
        return userRoomService.exitRoom(roomId, requestDto);
    }
}
