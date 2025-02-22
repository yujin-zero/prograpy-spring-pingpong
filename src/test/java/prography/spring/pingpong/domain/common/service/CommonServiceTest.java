package prography.spring.pingpong.domain.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prography.spring.pingpong.domain.common.controller.CommonController;
import prography.spring.pingpong.domain.common.model.dto.InitRequestDto;
import prography.spring.pingpong.domain.user.service.UserService;
import prography.spring.pingpong.model.dto.ApiResponse;

@ExtendWith(MockitoExtension.class)
class CommonServiceTest {

    @InjectMocks
    private CommonController commonController;

    @InjectMocks
    private CommonService commonService;

    @Mock
    private DataCleanupService dataCleanupService;

    @Mock
    private FakerApiService fakerApiService;

    @Mock
    private UserService userService;

    @Test
    @DisplayName("✅ 헬스 체크 API - 성공")
    void healthCheck_Success() {
        ApiResponse<Void> response = commonController.healthCheck();

        assertNotNull(response);
        assertEquals(200, response.code());
        assertEquals("API 요청이 성공했습니다.", response.message());
    }

    @Test
    @DisplayName("✅ 초기화 API - 성공")
    void initializeData_Success() {
        InitRequestDto request = new InitRequestDto(1,10);

        when(fakerApiService.fetchFakerUsers(request.seed(), request.quantity())).thenReturn(List.of());

        ApiResponse<Void> response = commonService.initializeData(request);

        assertEquals(200, response.code());
        verify(dataCleanupService).deleteAllData();
        verify(userService).saveAllUsers(any());
    }
}