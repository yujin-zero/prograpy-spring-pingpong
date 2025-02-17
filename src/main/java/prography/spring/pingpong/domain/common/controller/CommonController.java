package prography.spring.pingpong.domain.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import prography.spring.pingpong.domain.common.model.dto.InitRequestDto;
import prography.spring.pingpong.domain.common.service.CommonService;
import prography.spring.pingpong.model.dto.ApiResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    @Operation(summary = "헬스 체크 API", description = "서버의 상태를 체크하는 API입니다.")
    @GetMapping("/health")
    public ApiResponse<Void> healthCheck() {
        log.info("📢 [Common] /health API 호출됨");
        return ApiResponse.success(null);
    }

    @Operation(summary = "초기화 API", description = "모든 데이터를 삭제하고 새로운 유저 데이터를 생성합니다.")
    @PostMapping("/init")
    public ApiResponse<Void> initData(@RequestBody InitRequestDto requestDto) {
        log.info("📢 [Common] /init API 호출됨. 요청 데이터: seed={}, quantity={}",requestDto.seed(),requestDto.quantity());
        return commonService.initializeData(requestDto);
    }

}
