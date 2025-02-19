package prography.spring.pingpong.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import prography.spring.pingpong.domain.user.model.dto.UserListResponseDto;
import prography.spring.pingpong.domain.user.service.UserService;
import prography.spring.pingpong.model.dto.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "유저 전체 조회 API", description = "페이징 처리된 유저 리스트를 반환합니다.")
    @GetMapping
    public ApiResponse<UserListResponseDto> getAllUsers(
            @RequestParam int page,
            @RequestParam int size) {
        return userService.getAllUsers(page, size);
    }
}
