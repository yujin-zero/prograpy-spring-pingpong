package prography.spring.pingpong.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "탁구 게임 API 문서", version = "1.0", description = "Pingpong 프로젝트의 API 명세서")
)
public class SwaggerConfig {
}
