package prography.spring.pingpong.domain.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prography.spring.pingpong.model.dto.ApiResponse;

@Slf4j
@RestController
@RequestMapping("/health")
public class CommonController {

    @Operation(summary = "서버 헬스체크 API", description = "서버의 상태를 체크하는 API입니다.")
    @GetMapping
    public ApiResponse<Void> healthCheck() {
        log.info("📢 [HealthCheck] /health API 호출됨");
        return ApiResponse.success(null);
    }
}
