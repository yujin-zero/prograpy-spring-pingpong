package prography.spring.pingpong.domain.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import prography.spring.pingpong.domain.room.model.dto.RoomCreateRequestDto;
import prography.spring.pingpong.domain.room.model.dto.RoomDetailResponseDto;
import prography.spring.pingpong.domain.room.model.dto.RoomListResponseDto;
import prography.spring.pingpong.domain.room.service.RoomService;
import prography.spring.pingpong.model.dto.ApiResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "방 생성 API", description = "유저가 새로운 방을 생성합니다.")
    @PostMapping
    public ApiResponse<Void> createRoom(@RequestBody RoomCreateRequestDto requestDto) {
        return roomService.createRoom(requestDto);
    }

    @Operation(summary = "방 전체 조회 API", description = "페이징 처리된 방 리스트를 반환합니다.")
    @GetMapping
    public ApiResponse<RoomListResponseDto> getAllRooms(
            @RequestParam int page,
            @RequestParam int size
    ){
        return roomService.getAllRooms(page, size);
    }

    @Operation(summary = "방 상세 조회 API", description = "특정 방의 상세 정보를 반환합니다.")
    @GetMapping("/{roomId}")
    public ApiResponse<RoomDetailResponseDto> getRoomDetail(@PathVariable Long roomId) {
        return roomService.getRoomDetail(roomId);
    }
}
