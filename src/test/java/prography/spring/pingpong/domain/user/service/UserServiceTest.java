package prography.spring.pingpong.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import prography.spring.pingpong.domain.user.model.dto.UserListResponseDto;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.repository.UserRepository;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.UserStatus;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .fakerId(1001)
                .name("테스트 유저")
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("✅ 유저 전체 조회 API - 성공")
    void getAllUsers_Success() {
        int page = 0;
        int size = 10;

        when(userRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(testUser)));

        ApiResponse<UserListResponseDto> response = userService.getAllUsers(page, size);

        assertNotNull(response);
        assertEquals(200, response.code());
        assertNotNull(response.result());
        assertEquals(1, response.result().getTotalElements());
        assertEquals(1, response.result().getUserList().size());
        assertEquals("테스트 유저", response.result().getUserList().get(0).getName());

        verify(userRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("✅ 유저 전체 조회 API - 유저가 없을 때")
    void getAllUsers_EmptyList() {
        int page = 0, size = 10;

        when(userRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        ApiResponse<UserListResponseDto> response = userService.getAllUsers(page, size);

        assertNotNull(response);
        assertEquals(200, response.code());
        assertNotNull(response.result());
        assertEquals(0, response.result().getTotalElements());
        assertEquals(0, response.result().getUserList().size());

        verify(userRepository, times(1)).findAll(any(PageRequest.class));
    }


}