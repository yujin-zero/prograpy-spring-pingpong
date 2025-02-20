package prography.spring.pingpong.domain.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import prography.spring.pingpong.domain.common.model.dto.InitRequestDto;
import prography.spring.pingpong.domain.common.service.CommonService;
import prography.spring.pingpong.model.dto.ApiResponse;

@Tag(name = "Common", description = "공통 서비스 관련 API")
@RestController
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    @Operation(summary = "헬스 체크 API", description = "서버의 상태를 체크하는 API입니다.")
    @GetMapping("/health")
    public ApiResponse<Void> healthCheck() {
        return ApiResponse.success(null);
    }

    @Operation(summary = "초기화 API", description = "모든 데이터를 삭제하고 새로운 유저 데이터를 생성합니다.")
    @PostMapping("/init")
    public ApiResponse<Void> initData(@RequestBody InitRequestDto requestDto) {
        return commonService.initializeData(requestDto);
    }

}
