package prography.spring.pingpong.domain.common.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import prography.spring.pingpong.domain.common.model.dto.FakerUserDto;
import prography.spring.pingpong.domain.common.model.dto.InitRequestDto;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.service.UserService;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.UserStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommonService {

    private final UserService userService;
    private final DataCleanupService dataCleanupService;
    private final FakerApiService fakerApiService;

    @Value("${user-status.active-threshold}")
    private int activeThreshold;

    @Value("${user-status.wait-threshold}")
    private int waitThreshold;

    public ApiResponse<Void> initializeData(InitRequestDto request) {
        dataCleanupService.deleteAllData();

        List<FakerUserDto.FakerUser> fakerUsers = fakerApiService.fetchFakerUsers(request.seed(), request.quantity());
        if (fakerUsers == null) {
            return ApiResponse.serverError();
        }

        List<User> users = convertToUserEntities(fakerUsers);
        userService.saveAllUsers(users);
        log.info("✅ [CommonService] User 데이터 저장 완료");

        return ApiResponse.success(null);
    }

    private List<User> convertToUserEntities(List<FakerUserDto.FakerUser> fakerUsers) {
        log.info("📌 [CommonService] User 엔티티 변환 중...");

        List<User> users = fakerUsers.stream()
                .sorted(Comparator.comparingInt(FakerUserDto.FakerUser::getId)) // fakerId 기준 정렬
                .map(fakerUser -> {
                    UserStatus status = determineUserStatus(fakerUser.getId());

                    return User.builder()
                            .fakerId(fakerUser.getId())
                            .name(fakerUser.getUsername())
                            .email(fakerUser.getEmail())
                            .userstatus(status)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                }).collect(Collectors.toList());

        log.info("✅ [CommonService] User 엔티티 변환 완료 (총 {}명)", users.size());
        return users;
    }

    private UserStatus determineUserStatus(int userId) {
        if (userId <= activeThreshold) {
            return UserStatus.ACTIVE;
        } else if (userId <= waitThreshold) {
            return UserStatus.WAIT;
        } else {
            return UserStatus.NON_ACTIVE;
        }
    }
}
